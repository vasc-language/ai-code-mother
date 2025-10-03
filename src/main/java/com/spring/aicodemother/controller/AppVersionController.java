package com.spring.aicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.dto.app.AppVersionQueryRequest;
import com.spring.aicodemother.model.dto.app.AppVersionRollbackRequest;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.vo.AppVersionVO;
import com.spring.aicodemother.service.AppService;
import com.spring.aicodemother.service.AppVersionService;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 应用版本管理 控制层
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@RestController
@RequestMapping("/app/version")
public class AppVersionController {

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 分页查询应用的版本列表
     *
     * @param appVersionQueryRequest 查询请求
     * @param request                请求
     * @return 版本列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<AppVersionVO>> listVersionsByPage(
            @RequestBody AppVersionQueryRequest appVersionQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appVersionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appVersionQueryRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 查询应用信息并校验权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        User loginUser = userService.getLoginUser(request);
        // 仅应用创建者可以查看版本历史
        if (!Objects.equals(app.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的版本历史");
        }

        Page<AppVersionVO> versionPage = appVersionService.listVersionsByPage(appVersionQueryRequest);
        return ResultUtils.success(versionPage);
    }

    /**
     * 查询应用的所有版本列表（不分页）
     *
     * @param appId   应用ID
     * @param request 请求
     * @return 版本列表
     */
    @GetMapping("/list/{appId}")
    public BaseResponse<List<AppVersionVO>> listVersionsByAppId(
            @PathVariable Long appId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 查询应用信息并校验权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        User loginUser = userService.getLoginUser(request);
        // 仅应用创建者可以查看版本历史
        if (!Objects.equals(app.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的版本历史");
        }

        List<AppVersionVO> versionList = appVersionService.listVersionsByAppId(appId);
        return ResultUtils.success(versionList);
    }

    /**
     * 获取版本详情
     *
     * @param versionId 版本ID
     * @param request   请求
     * @return 版本详情
     */
    @GetMapping("/get/{versionId}")
    public BaseResponse<AppVersionVO> getVersionVO(
            @PathVariable Long versionId,
            HttpServletRequest request) {
        ThrowUtils.throwIf(versionId == null || versionId <= 0, ErrorCode.PARAMS_ERROR, "版本ID不能为空");

        AppVersionVO versionVO = appVersionService.getVersionVO(versionId);
        ThrowUtils.throwIf(versionVO == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");

        // 查询应用信息并校验权限
        App app = appService.getById(versionVO.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        User loginUser = userService.getLoginUser(request);
        // 仅应用创建者可以查看版本详情
        if (!Objects.equals(app.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该版本详情");
        }

        return ResultUtils.success(versionVO);
    }

    /**
     * 版本回滚
     *
     * @param rollbackRequest 回滚请求
     * @param request         请求
     * @return 是否成功
     */
    @PostMapping("/rollback")
    public BaseResponse<Boolean> rollbackToVersion(
            @RequestBody AppVersionRollbackRequest rollbackRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(rollbackRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = rollbackRequest.getAppId();
        Long versionId = rollbackRequest.getVersionId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(versionId == null || versionId <= 0, ErrorCode.PARAMS_ERROR, "版本ID不能为空");

        // 查询应用信息并校验权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        User loginUser = userService.getLoginUser(request);
        // 仅应用创建者可以回滚版本
        if (!Objects.equals(app.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限回滚该应用版本");
        }

        boolean result = appVersionService.rollbackToVersion(versionId, appId, loginUser);
        return ResultUtils.success(result);
    }
}
