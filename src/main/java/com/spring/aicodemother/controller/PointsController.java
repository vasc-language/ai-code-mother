package com.spring.aicodemother.controller;

import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.model.entity.PointsRecord;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.entity.UserPoints;
import com.spring.aicodemother.service.PointsRecordService;
import com.spring.aicodemother.service.UserPointsService;
import com.spring.aicodemother.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分控制器
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@RestController
@RequestMapping("/points")
@Tag(name = "积分管理", description = "积分相关接口")
public class PointsController {

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private PointsRecordService pointsRecordService;

    @Resource
    private UserService userService;

    /**
     * 获取用户积分概览
     *
     * @param request HTTP请求
     * @return 积分概览信息
     */
    @GetMapping("/overview")
    @Operation(summary = "获取积分概览", description = "查询用户的积分统计信息")
    public BaseResponse<Map<String, Object>> getPointsOverview(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        UserPoints userPoints = userPointsService.getOrCreateUserPoints(loginUser.getId());

        // 计算累计获得和消耗
        List<PointsRecord> allRecords = pointsRecordService.getUserPointsRecords(loginUser.getId(), null);
        int totalEarned = 0;
        int totalConsumed = 0;

        for (PointsRecord record : allRecords) {
            if (record.getPoints() > 0) {
                totalEarned += record.getPoints();
            } else {
                totalConsumed += Math.abs(record.getPoints());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("availablePoints", userPoints != null ? userPoints.getAvailablePoints() : 0);
        result.put("totalEarned", totalEarned);
        result.put("totalConsumed", totalConsumed);

        return ResultUtils.success(result);
    }

    /**
     * 获取积分明细记录
     *
     * @param type    积分类型（可选）
     * @param request HTTP请求
     * @return 积分记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "获取积分明细", description = "查询用户的积分变动记录")
    public BaseResponse<List<PointsRecord>> getPointsRecords(
            @Parameter(description = "积分类型（SIGN_IN/REGISTER/INVITE/GENERATE/FIRST_GENERATE）")
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<PointsRecord> records = pointsRecordService.getUserPointsRecords(loginUser.getId(), type);
        return ResultUtils.success(records);
    }

    /**
     * 获取当前用户积分
     *
     * @param request HTTP请求
     * @return 当前可用积分
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前积分", description = "查询用户当前可用积分")
    public BaseResponse<Map<String, Integer>> getCurrentPoints(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        UserPoints userPoints = userPointsService.getOrCreateUserPoints(loginUser.getId());

        Map<String, Integer> result = new HashMap<>();
        result.put("availablePoints", userPoints != null ? userPoints.getAvailablePoints() : 0);
        result.put("frozenPoints", userPoints != null ? userPoints.getFrozenPoints() : 0);

        return ResultUtils.success(result);
    }

}
