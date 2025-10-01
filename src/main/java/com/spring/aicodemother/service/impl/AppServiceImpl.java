package com.spring.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.ai.AiCodeGenTypeRoutingService;
import com.spring.aicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.core.AiCodeGeneratorFacade;
import com.spring.aicodemother.core.build.VueProjectBuilder;
import com.spring.aicodemother.core.handler.StreamHandlerExecutor;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.dto.app.AppAddRequest;
import com.spring.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.model.vo.UserVO;
import com.spring.aicodemother.model.dto.app.AppQueryRequest;
import com.spring.aicodemother.model.vo.AppVO;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.mapper.AppMapper;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.monitor.MonitorContext;
import com.spring.aicodemother.monitor.MonitorContextHolder;
import com.spring.aicodemother.service.AppService;
import com.spring.aicodemother.service.ChatHistoryService;
import com.spring.aicodemother.service.ScreenshotService;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Value("${code.deploy-host:http://localhost}")
    private String deployHost;

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Resource
    private com.spring.aicodemother.ai.AppNameGeneratorServiceFactory appNameGeneratorServiceFactory;

    @Resource
    private com.spring.aicodemother.service.AppVersionService appVersionService;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // Backward compatibility: run without external cancellation
        return chatToGenCode(appId, message, loginUser, null);
    }

    // New entry with cancellation control
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser,
                                      com.spring.aicodemother.core.control.GenerationControlRegistry.GenerationControl control) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户提示词不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());

        // 5.1 若为 Vue 工程模式，尝试命中预置模板并进行首次目录拷贝，同时为当前轮拼接模板说明
        String finalMessage = message;
        try {
            if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
                String presetKey = matchPresetTemplate(message);
                if (StrUtil.isNotBlank(presetKey)) {
                    boolean copied = ensurePresetCopied(appId, presetKey);
                    finalMessage = buildTemplateAwareMessage(message, presetKey, copied);
                    log.info("[TEMPLATE-CACHED] appId={}, preset='{}', copied={}", appId, presetKey, copied);
                } else {
                    log.info("[TEMPLATE-NO-HIT] appId={}, 未命中预置模板", appId);
                }
            }
        } catch (Exception e) {
            // 不阻断主流程，模板机制失败时继续按原逻辑生成
            log.warn("[TEMPLATE-ERROR] appId={}, 处理预置模板时出错：{}", appId, e.getMessage());
        }
        // 6. 设置监控上下文
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .userId(loginUser.getId().toString())
                        .appId(appId.toString())
                        .build()
        );
        // 7. 调用 AI 生成代码（流式）并绑定取消信号（若命中模板，则使用增强后的 finalMessage）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(finalMessage, codeGenTypeEnum, appId, control);
        // 8. 收集AI响应内容并在完成后记录到对话历史（根据取消状态抑制副作用）
        java.util.function.BooleanSupplier cancelled = (control == null) ? (() -> false) : control::isCancelled;
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum,
                        cancelled)
                .doFinally(signalType -> {
                    // 无论成功与否，最后流结束都清除
                    MonitorContextHolder.clearContext();
                });
    }

    /**
     * 命中模板：当前仅支持两个精确匹配的中文提示词
     * @param userMessage 用户输入
     * @return 预置模板 key：portfolio 或 enterprise，未命中返回 null
     */
    private String matchPresetTemplate(String userMessage) {
        if (userMessage == null) return null;
        String trimmed = userMessage.trim();
        // 兼容原始的精确匹配
        String tpl1 = "制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。";
        String tpl2 = "设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。";
        if (tpl1.equals(trimmed)) return "portfolio";
        if (tpl2.equals(trimmed)) return "enterprise";

        // 关键词匹配（更易命中）
        String text = trimmed.toLowerCase();
        String[] portfolioHints = new String[]{
                "作品展示", "作品集", "作品", "画廊", "图库", "摄影", "艺术家", "个人简历", "portfolio",
                "预览大图", "瀑布流", "网格布局", "筛选", "分类"
        };
        String[] enterpriseHints = new String[]{
                "企业官网", "官网", "企业", "公司", "产品服务", "新闻资讯", "联系我们", "商务风格", "轮播图",
                "产品展示", "团队介绍", "客户案例", "多语言", "在线客服"
        };

        int enterpriseScore = countMatches(text, enterpriseHints, userMessage);
        int portfolioScore = countMatches(text, portfolioHints, userMessage);

        if (enterpriseScore > portfolioScore && enterpriseScore > 0) return "enterprise";
        if (portfolioScore > 0) return "portfolio";
        return null;
    }

    private int countMatches(String textLower, String[] hints, String original) {
        int c = 0;
        for (String h : hints) {
            if (h == null || h.isEmpty()) continue;
            // 中文大小写无感知；英文用 lower 比较
            if (isAscii(h)) {
                if (textLower.contains(h.toLowerCase())) c++;
            } else {
                if (original.contains(h)) c++;
            }
        }
        return c;
    }

    private boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) > 127) return false;
        return true;
    }

    /**
     * 若命中模板：仅在目标目录不存在或为空时，复制模板到项目根目录
     * @param appId 应用ID
     * @param presetKey 预置模板key
     * @return 是否发生了复制
     */
    private boolean ensurePresetCopied(Long appId, String presetKey) {
        String targetDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + (CodeGenTypeEnum.VUE_PROJECT.getValue() + "_" + appId);
        File targetDir = new File(targetDirPath);
        boolean needCopy = isDirEmpty(targetDir);
        if (!needCopy) {
            return false;
        }
        String templateDirName = getPresetTemplateDirName(presetKey);
        if (templateDirName == null) {
            return false;
        }
        // 优先从本地 tmp 目录拷贝；不存在时回退到 classpath:static 下的模板
        String templateDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + templateDirName;
        File templateDir = new File(templateDirPath);
        if (templateDir.exists() && templateDir.isDirectory()) {
            try {
                FileUtil.copyContent(templateDir, targetDir, true);
                return true;
            } catch (Exception e) {
                log.warn("复制预置模板失败: from={} to={}, err={}", templateDirPath, targetDirPath, e.getMessage());
                // 继续尝试从 classpath 回退
            }
        }
        // 回退：从 classpath:/static/<templateDirName>/** 拷贝
        boolean copied = copyPresetFromClasspath(templateDirName, targetDir);
        if (!copied) {
            log.warn("未找到预置模板（本地与classpath均未命中）：{}", templateDirName);
        }
        return copied;
    }

    private boolean isDirEmpty(File dir) {
        if (dir == null) return true;
        if (!dir.exists()) return true;
        if (!dir.isDirectory()) return true;
        File[] files = dir.listFiles(file -> !".DS_Store".equals(file.getName()));
        return files == null || files.length == 0;
    }

    private String getPresetTemplateDirName(String presetKey) {
        if ("portfolio".equals(presetKey)) {
            return "vue_project_323345718267260928";
        }
        if ("enterprise".equals(presetKey)) {
            return "vue_project_317749662884204544";
        }
        return null;
    }

    /**
     * 从 classpath 的 static 目录复制模板到目标目录
     */
    private boolean copyPresetFromClasspath(String templateDirName, File targetDir) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = "classpath*:/static/" + templateDirName + "/**";
            org.springframework.core.io.Resource[] resources = resolver.getResources(pattern);
            if (resources == null || resources.length == 0) {
                return false;
            }
            FileUtil.mkdir(targetDir);
            int copied = 0;
            String marker = "/static/" + templateDirName + "/";
            for (org.springframework.core.io.Resource resource : resources) {
                try {
                    String url = resource.getURL().toString();
                    int idx = url.indexOf(marker);
                    if (idx < 0) {
                        continue;
                    }
                    String relative = url.substring(idx + marker.length());
                    if (relative.isEmpty() || relative.endsWith("/")) {
                        // 目录占位，确保目录存在
                        FileUtil.mkdir(new File(targetDir, relative));
                        continue;
                    }
                    File dest = new File(targetDir, relative);
                    FileUtil.mkParentDirs(dest);
                    try (java.io.InputStream in = resource.getInputStream()) {
                        FileUtil.writeFromStream(in, dest);
                        copied++;
                    }
                } catch (Exception ex) {
                    // 可能是目录或不可读资源，忽略继续
                }
            }
            log.info("从 classpath 复制模板 '{}' 完成，文件数={}，目标目录={}", templateDirName, copied, targetDir.getAbsolutePath());
            return copied > 0;
        } catch (Exception e) {
            log.warn("从 classpath 复制模板失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 拼接模板使用说明（仅对当前轮的提示词增强，不影响对话历史）
     */
    private String buildTemplateAwareMessage(String original, String presetKey, boolean copied) {
        String presetDesc = "portfolio".equals(presetKey) ? "作品展示网站模板" : ("enterprise".equals(presetKey) ? "企业官网模板" : "预置模板");
        StringBuilder sb = new StringBuilder();
        sb.append(original == null ? "" : original.trim());
        sb.append("\n\n[模板信息] ");
        sb.append("已为本项目准备预置代码：").append(presetDesc).append("。");
        if (copied) {
            sb.append("已将模板文件复制到项目根目录（vue_project）。");
        } else {
            sb.append("项目目录已存在模板文件，继续在此基础上修改。");
        }
        sb.append(" 请先使用【读取目录】与【读取文件】了解结构，再基于需要进行【文件修改】或【写入文件】的最小变更，避免重复创建已存在的文件。完成后调用【退出工具】结束本轮任务。");
        return sb.toString();
    }


    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 使用 AI 生成简洁项目名称（失败则回退到截断策略）
        String appName = null;
        try {
            var nameService = appNameGeneratorServiceFactory.createAppNameGeneratorService();
            String rawName = nameService.generateName(initPrompt);
            if (StrUtil.isNotBlank(rawName)) {
                // 清洗：去除引号/换行/特殊符号，限制长度
                appName = rawName.replaceAll("[\r\n\t]", " ")
                        .replaceAll("^[\"'`，。！？,.\s]+|[\"'`，。！？,.\s]+$", "")
                        .trim();
                if (appName.length() > 20) {
                    appName = appName.substring(0, 20);
                }
            }
        } catch (Exception e) {
            log.warn("AI 生成项目名称失败，使用回退策略：{}", e.getMessage());
        }
        if (StrUtil.isBlank(appName)) {
            appName = initPrompt.substring(0, Math.min(initPrompt.length(), 12));
        }
        app.setAppName(appName);
        // 使用 AI 智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


    /**
     * 项目部署
     *
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 返回可访问的 URL
        // String appDeployKey = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        String appDeployKey = String.format("%s/%s/", deployHost, deployKey);
        // 11. 保存版本（部署成功后保存代码版本）
        try {
            app.setDeployKey(deployKey); // 确保app对象包含最新的deployKey
            appVersionService.saveVersion(app, appDeployKey, loginUser);
            log.info("应用版本保存成功：appId={}", appId);
        } catch (Exception e) {
            log.error("保存应用版本失败：appId={}", appId, e);
            // 版本保存失败不影响部署流程
        }
        // 12. 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployKey);
        return appDeployKey;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }
}
