package com.spring.aicodemother.controller;

import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.model.entity.InviteRecord;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.service.InviteRecordService;
import com.spring.aicodemother.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.StrUtil.isBlank;

/**
 * 邀请控制器
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@RestController
@RequestMapping("/invite")
@Tag(name = "邀请管理", description = "邀请相关接口")
public class InviteController {

    @Resource
    private InviteRecordService inviteRecordService;

    @Resource
    private UserService userService;

    /**
     * 获取用户的邀请码
     *
     * @param request HTTP请求
     * @return 邀请码
     */
    @GetMapping("/code")
    @Operation(summary = "获取邀请码", description = "获取当前用户的邀请码")
    public BaseResponse<Map<String, String>> getInviteCode(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String inviteCode = inviteRecordService.getUserInviteCode(loginUser.getId());

        Map<String, String> result = new HashMap<>();
        result.put("inviteCode", inviteCode);
        result.put("inviteUrl", buildInviteUrl(request, inviteCode));

        return ResultUtils.success(result);
    }

    /**
     * 获取用户的邀请记录
     *
     * @param request HTTP请求
     * @return 邀请记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "获取邀请记录", description = "查询当前用户的邀请记录列表")
    public BaseResponse<List<InviteRecord>> getInviteRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<InviteRecord> records = inviteRecordService.getUserInviteRecords(loginUser.getId());
        return ResultUtils.success(records);
}

    private String buildInviteUrl(HttpServletRequest request, String inviteCode) {
        String origin = request.getHeader("Origin");
        String baseUrl;
        if (!isBlank(origin)) {
            baseUrl = origin;
        } else {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            if (port == 80 || port == 443) {
                baseUrl = String.format("%s://%s", scheme, host);
            } else {
                baseUrl = String.format("%s://%s:%d", scheme, host, port);
            }
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "user/register?inviteCode=" + inviteCode;
    }

}
