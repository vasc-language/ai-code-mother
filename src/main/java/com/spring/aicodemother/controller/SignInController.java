package com.spring.aicodemother.controller;

import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.service.SignInRecordService;
import com.spring.aicodemother.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 签到控制器
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@RestController
@RequestMapping("/signin")
@Tag(name = "签到管理", description = "签到相关接口")
public class SignInController {

    @Resource
    private SignInRecordService signInRecordService;

    @Resource
    private UserService userService;

    /**
     * 每日签到
     *
     * @param request HTTP请求
     * @return 签到结果
     */
    @PostMapping("/daily")
    @Operation(summary = "每日签到", description = "用户每日签到，获取积分奖励")
    public BaseResponse<Map<String, Object>> dailySignIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<String, Object> result = signInRecordService.dailySignIn(loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取签到状态
     *
     * @param request HTTP请求
     * @return 签到状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取签到状态", description = "查询用户今日签到状态和连续签到天数")
    public BaseResponse<Map<String, Object>> getSignInStatus(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<String, Object> status = signInRecordService.getSignInStatus(loginUser.getId());
        return ResultUtils.success(status);
    }

}
