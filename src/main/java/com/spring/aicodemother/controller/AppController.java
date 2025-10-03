package com.spring.aicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.spring.aicodemother.annotation.AuthCheck;
import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.DeleteRequest;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.constant.UserConstant;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.dto.app.*;
import com.spring.aicodemother.model.vo.AppVO;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.ratelimit.annotation.RateLimit;
import com.spring.aicodemother.ratelimit.enums.RateLimitType;
import com.spring.aicodemother.service.ProjectDownloadService;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import com.spring.aicodemother.core.control.GenerationControlRegistry;
import com.spring.aicodemother.core.control.GenerationControlRegistry.GenerationControl;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @Resource
    private GenerationControlRegistry generationControlRegistry;

    @Resource
    private org.redisson.api.RedissonClient redissonClient;

//    @Resource
//    private ProjectDownloadService projectDownloadService;

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60, message = "AI 对话请求过于频繁，请稍后再试")
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       @RequestParam(required = false) String runId,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");

        // IP级别限流检查（每分钟10次）
        checkIpRateLimit(request);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 生成控制器：用于手动取消
        final String rid = StrUtil.isBlank(runId) ? java.util.UUID.randomUUID().toString() : runId;
        final Long finalAppId = appId;
        final Long finalUserId = loginUser.getId();
        GenerationControl control = generationControlRegistry.register(rid, String.valueOf(finalUserId), finalAppId);
        log.info("[SSE-START] runId={}, appId={}, userId={}", rid, finalAppId, finalUserId);
        // 调用服务生成代码（SSE 流式返回）并绑定取消信号
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser, control);
        // 将业务流与取消信号融合，取消时立刻向前端发送 interrupted 事件并终止
        // 为了提升首包到达速度（TTFT），在真实内容前先发送一条轻量 keepalive 数据帧
        ServerSentEvent<String> keepalive = ServerSentEvent.<String>builder()
                .data("{\"d\":\"\"}")
                .build();
        Flux<ServerSentEvent<String>> dataEvents = Flux
                .just(keepalive)
                .concatWith(
                        contentFlux
                .takeUntilOther(control.cancelFlux())
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                )
                .concatWith(Mono.fromSupplier(() -> ServerSentEvent.<String>builder().event("done").data("").build()));

        Flux<ServerSentEvent<String>> interruptEvent = control.cancelFlux()
                .map(v -> ServerSentEvent.<String>builder().event("interrupted").data("").build());

        return Flux.merge(dataEvents, interruptEvent)
                // 一旦发送了 interrupted 事件就结束 SSE
                .takeUntil(sse -> "interrupted".equals(sse.event()))
                // 前端主动断开 SSE（未显式调用 /stop）时，同样触发取消，确保后台保存/构建链路停止
                .doOnCancel(() -> {
                    generationControlRegistry.cancel(rid);
                    log.info("[SSE-CANCEL] runId={} cancelled by client disconnect", rid);
                })
                .doFinally(sig -> {
                    generationControlRegistry.remove(rid);
                    log.info("[SSE-END] runId={}, appId={}, userId={}, signal={}", rid, finalAppId, finalUserId, sig);
                });
    }

    /**
     * 手动停止当前运行
     */
    @PostMapping(value = "/chat/stop", consumes = APPLICATION_JSON_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 20, rateInterval = 60, message = "操作过于频繁，请稍后")
    public BaseResponse<Boolean> stopChat(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String runId = payload == null ? null : payload.get("runId");
        ThrowUtils.throwIf(StrUtil.isBlank(runId), ErrorCode.PARAMS_ERROR, "runId 不能为空");
        // 权限校验：仅拥有者可取消
        User loginUser = userService.getLoginUser(request);
        GenerationControl control = generationControlRegistry.get(runId);
        ThrowUtils.throwIf(control == null, ErrorCode.NOT_FOUND_ERROR, "运行不存在或已结束");
        if (control.getOwnerUserId() != null && !String.valueOf(loginUser.getId()).equals(control.getOwnerUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权停止该生成");
        }
        generationControlRegistry.cancel(runId);
        log.info("[SSE-CANCEL] runId={} cancelled by userId={}", runId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        // 检查部署请求是否为空
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取应用 ID
        Long appId = appDeployRequest.getAppId();
        // 检查应用 ID 是否为空
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        // 返回部署 URL
        return ResultUtils.success(deployUrl);
    }

//    /**
//     * 下载应用代码
//     *
//     * @param appId    应用ID
//     * @param request  请求
//     * @param response 响应
//     */
//    @GetMapping("/download/{appId}")
//    public void downloadAppCode(@PathVariable Long appId,
//                                HttpServletRequest request,
//                                HttpServletResponse response) {
//        // 1. 基础校验
//        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
//        // 2. 查询应用信息
//        App app = appService.getById(appId);
//        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
//        // 3. 权限校验：只有应用创建者可以下载代码
//        User loginUser = userService.getLoginUser(request);
//        if (!app.getUserId().equals(loginUser.getId())) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
//        }
//        // 4. 构建应用代码目录路径（生成目录，非部署目录）
//        String codeGenType = app.getCodeGenType();
//        String sourceDirName = codeGenType + "_" + appId;
//        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
//        // 5. 检查代码目录是否存在
//        File sourceDir = new File(sourceDirPath);
//        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
//                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
//        // 6. 生成下载文件名（不建议添加中文内容）
//        String downloadFileName = String.valueOf(appId);
//        // 7. 调用通用下载服务
//        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
//    }

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request       请求
     * @return 应用 id
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        log.info("开始创建应用，请求参数: {}", appAddRequest);
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        Long appId = appService.createApp(appAddRequest, loginUser);
        return ResultUtils.success(appId);
    }

    /**
     * 更新应用（用户只能更新自己的应用名称）
     *
     * @param appUpdateRequest 更新请求
     * @param request          请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除应用（用户只能删除自己的应用）
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id) {
        log.info("开始获取应用详情，应用ID: {}", id);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        
        try {
            // 查询数据库
            App app = appService.getById(id);
            log.info("数据库查询结果，应用ID: {}，查询结果: {}", id, app != null ? "找到应用" : "未找到应用");
            
            if (app != null) {
                log.info("应用详情 - ID: {}, 名称: {}, 创建者: {}, 创建时间: {}", 
                        app.getId(), app.getAppName(), app.getUserId(), app.getCreateTime());
            }
            
            ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, 
                String.format("应用不存在，应用ID: %d", id));
            
            // 获取封装类（包含用户信息）
            AppVO appVO = appService.getAppVO(app);
            log.info("成功获取应用详情，应用ID: {}", id);
            return ResultUtils.success(appVO);
        } catch (Exception e) {
            log.error("获取应用详情失败，应用ID: {}，错误信息: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/vo")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.spring.aicodemother.utils.CacheKeyUtils).generateKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10"
    )
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }

    /**
     * IP级别限流检查（每分钟10次）
     * Redis故障时降级允许通过，避免服务不可用
     */
    private void checkIpRateLimit(HttpServletRequest request) {
        String ip = getClientIP(request);
        String key = "rate_limit:ip:app_gen:" + ip;
        String lockKey = "lock:rate_limit:init:" + ip;

        try {
            // 使用 Redisson 的分布式限流器
            org.redisson.api.RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

            // 使用分布式锁避免并发重复设置限流参数
            if (!rateLimiter.isExists()) {
                org.redisson.api.RLock lock = redissonClient.getLock(lockKey);
                try {
                    // 尝试获取锁，最多等待1秒
                    if (lock.tryLock(1, 5, java.util.concurrent.TimeUnit.SECONDS)) {
                        try {
                            // 双重检查，防止重复设置
                            if (!rateLimiter.isExists()) {
                                // 设置限流器参数：每分钟最多10次请求
                                rateLimiter.trySetRate(
                                    org.redisson.api.RateType.OVERALL,
                                    com.spring.aicodemother.constants.PointsConstants.IP_RATE_LIMIT_PER_MINUTE,
                                    com.spring.aicodemother.constants.PointsConstants.IP_RATE_LIMIT_WINDOW_SECONDS,
                                    org.redisson.api.RateIntervalUnit.SECONDS
                                );
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("IP限流器初始化获取锁被中断: {}", ip);
                }
            }

            // 设置1小时过期时间
            rateLimiter.expire(java.time.Duration.ofHours(1));

            // 尝试获取一个令牌，如果获取失败则限流
            if (!rateLimiter.tryAcquire(1)) {
                log.warn("IP {} 请求过于频繁，已限流", ip);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "该IP请求过于频繁，请稍后再试");
            }
        } catch (BusinessException e) {
            // 业务异常直接抛出（限流异常）
            throw e;
        } catch (Exception e) {
            // Redis故障或其他异常，记录日志后降级允许通过
            log.error("IP限流检查失败，降级允许通过。IP: {}, 错误: {}", ip, e.getMessage());
        }
    }

    /**
     * 获取客户端IP（标准化处理，支持IPv6）
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 标准化IPv6地址
        ip = normalizeIP(ip != null ? ip : "unknown");
        return ip;
    }

    /**
     * 标准化IP地址（IPv6转标准格式）
     */
    private String normalizeIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }
        // 标准化IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

}
//@RestController
//@RequestMapping("/app")
//public class AppController {
//
//    @Resource
//    private AppService appService;
//
//    @Resource
//    private UserService userService;
//
//    /**
//     * 应用聊天生成代码（流式 SSE）
//     *
//     * @param appId   应用 ID
//     * @param message 用户消息
//     * @param request 请求对象
//     * @return 生成结果流
//     */
//    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
//                                                       @RequestParam String message,
//                                                       HttpServletRequest request) {
//        // 参数校验
//        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
//        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
//        // 获取当前登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 调用服务生成代码（流式）
//        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
//        // 转换为 ServerSentEvent 格式
//        return contentFlux
//                .map(chunk -> {
//                    // 将内容包装成JSON对象
//                    Map<String, String> wrapper = Map.of("d", chunk);
//                    String jsonData = JSONUtil.toJsonStr(wrapper);
//                    return ServerSentEvent.<String>builder()
//                            .data(jsonData)
//                            .build();
//                })
//                .concatWith(Mono.just(
//                        // 发送结束事件
//                        ServerSentEvent.<String>builder()
//                                .event("done")
//                                .data("")
//                                .build()
//                ));
//    }
//
//
//    /**
//     * 创建应用
//     *
//     * @param appAddRequest 创建应用请求
//     * @param request       请求
//     * @return 应用 id
//     */
//    @PostMapping("/add")
//    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
//        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
//        // 参数校验
//        String initPrompt = appAddRequest.getInitPrompt();
//        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
//        // 获取当前登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 构造入库对象
//        App app = new App();
//        BeanUtil.copyProperties(appAddRequest, app);
//        app.setUserId(loginUser.getId());
//        // 应用名称暂时为 initPrompt 前 12 位
//        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
//        // 暂时设置为多文件生成
//        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
//        // 插入数据库
//        boolean result = appService.save(app);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        return ResultUtils.success(app.getId());
//    }
//
//    /**
//     * 更新应用（用户只能更新自己的应用名称）
//     *
//     * @param appUpdateRequest 更新请求
//     * @param request          请求
//     * @return 更新结果
//     */
//    @PostMapping("/update")
//    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
//        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        long id = appUpdateRequest.getId();
//        // 判断是否存在
//        App oldApp = appService.getById(id);
//        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人可更新
//        if (!oldApp.getUserId().equals(loginUser.getId())) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        App app = new App();
//        app.setId(id);
//        app.setAppName(appUpdateRequest.getAppName());
//        // 设置编辑时间
//        app.setEditTime(LocalDateTime.now());
//        boolean result = appService.updateById(app);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        return ResultUtils.success(true);
//    }
//
//    /**
//     * 删除应用（用户只能删除自己的应用）
//     *
//     * @param deleteRequest 删除请求
//     * @param request       请求
//     * @return 删除结果
//     */
//    @PostMapping("/delete")
//    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
//        if (deleteRequest == null || deleteRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        long id = deleteRequest.getId();
//        // 判断是否存在
//        App oldApp = appService.getById(id);
//        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可删除
//        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = appService.removeById(id);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 根据 id 获取应用详情
//     *
//     * @param id      应用 id
//     * @return 应用详情
//     */
//    @GetMapping("/get/vo")
//    public BaseResponse<AppVO> getAppVOById(long id) {
//        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
//        // 查询数据库
//        App app = appService.getById(id);
//        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
//        // 获取封装类（包含用户信息）
//        return ResultUtils.success(appService.getAppVO(app));
//    }
//
//    /**
//     * 分页获取精选应用列表
//     *
//     * @param appQueryRequest 查询请求
//     * @return 精选应用列表
//     */
//    @PostMapping("/good/list/page/vo")
//    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
//        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
//        // 限制每页最多 20 个
//        long pageSize = appQueryRequest.getPageSize();
//        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
//        long pageNum = appQueryRequest.getPageNum();
//        // 只查询精选的应用
//        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
//        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
//        // 分页查询
//        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
//        // 数据封装
//        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
//        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
//        appVOPage.setRecords(appVOList);
//        return ResultUtils.success(appVOPage);
//    }
//
//    /**
//     * 管理员删除应用
//     *
//     * @param deleteRequest 删除请求
//     * @return 删除结果
//     */
//    @PostMapping("/admin/delete")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
//        if (deleteRequest == null || deleteRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long id = deleteRequest.getId();
//        // 判断是否存在
//        App oldApp = appService.getById(id);
//        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
//        boolean result = appService.removeById(id);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 管理员更新应用
//     *
//     * @param appAdminUpdateRequest 更新请求
//     * @return 更新结果
//     */
//    @PostMapping("/admin/update")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
//        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long id = appAdminUpdateRequest.getId();
//        // 判断是否存在
//        App oldApp = appService.getById(id);
//        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
//        App app = new App();
//        BeanUtil.copyProperties(appAdminUpdateRequest, app);
//        // 设置编辑时间
//        app.setEditTime(LocalDateTime.now());
//        boolean result = appService.updateById(app);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        return ResultUtils.success(true);
//    }
//
//    /**
//     * 管理员分页获取应用列表
//     *
//     * @param appQueryRequest 查询请求
//     * @return 应用列表
//     */
//    @PostMapping("/admin/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
//        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
//        long pageNum = appQueryRequest.getPageNum();
//        long pageSize = appQueryRequest.getPageSize();
//        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
//        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
//        // 数据封装
//        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
//        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
//        appVOPage.setRecords(appVOList);
//        return ResultUtils.success(appVOPage);
//    }
//
//    /**
//     * 管理员根据 id 获取应用详情
//     *
//     * @param id 应用 id
//     * @return 应用详情
//     */
//    @GetMapping("/admin/get/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
//        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
//        // 查询数据库
//        App app = appService.getById(id);
//        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
//        // 获取封装类
//        return ResultUtils.success(appService.getAppVO(app));
//    }
//
//    /**
//     * 应用部署
//     *
//     * @param appDeployRequest 部署请求
//     * @param request          请求
//     * @return 部署 URL
//     */
//    @PostMapping("/deploy")
//    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
//        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
//        Long appId = appDeployRequest.getAppId();
//        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
//        // 获取当前登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 调用服务部署应用
//        String deployUrl = appService.deployApp(appId, loginUser);
//        return ResultUtils.success(deployUrl);
//    }
//
//
//
//
//
//    // /**
//    //  * 保存应用。
//    //  *
//    //  * @param app 应用
//    //  * @return {@code true} 保存成功，{@code false} 保存失败
//    //  */
//    // @PostMapping("save")
//    // public boolean save(@RequestBody App app) {
//    //     return appService.save(app);
//    // }
//
//    // /**
//    //  * 根据主键删除应用。
//    //  *
//    //  * @param id 主键
//    //  * @return {@code true} 删除成功，{@code false} 删除失败
//    //  */
//    // @DeleteMapping("remove/{id}")
//    // public boolean remove(@PathVariable Long id) {
//    //     return appService.removeById(id);
//    // }
//
//    // /**
//    //  * 根据主键更新应用。
//    //  *
//    //  * @param app 应用
//    //  * @return {@code true} 更新成功，{@code false} 更新失败
//    //  */
//    // @PutMapping("update")
//    // public boolean update(@RequestBody App app) {
//    //     return appService.updateById(app);
//    // }
//
//    // /**
//    //  * 查询所有应用。
//    //  *
//    //  * @return 所有数据
//    //  */
//    // @GetMapping("list")
//    // public List<App> list() {
//    //     return appService.list();
//    // }
//
//    // /**
//    //  * 根据主键获取应用。
//    //  *
//    //  * @param id 应用主键
//    //  * @return 应用详情
//    //  */
//    // @GetMapping("getInfo/{id}")
//    // public App getInfo(@PathVariable Long id) {
//    //     return appService.getById(id);
//    // }
//
//    // /**
//    //  * 分页查询应用。
//    //  *
//    //  * @param page 分页对象
//    //  * @return 分页对象
//    //  */
//    // @GetMapping("page")
//    // public Page<App> page(Page<App> page) {
//    //     return appService.page(page);
//    // }
//
//}
