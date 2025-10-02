package com.spring.aicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.mapper.SignInRecordMapper;
import com.spring.aicodemother.model.entity.SignInRecord;
import com.spring.aicodemother.model.enums.PointsTypeEnum;
import com.spring.aicodemother.service.SignInRecordService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 签到记录 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class SignInRecordServiceImpl extends ServiceImpl<SignInRecordMapper, SignInRecord> implements SignInRecordService {

    @Resource
    private UserPointsService userPointsService;

    // 签到积分配置
    private static final int BASE_SIGN_IN_POINTS = 5; // 基础签到积分
    private static final int CONTINUOUS_3_DAYS_BONUS = 3; // 连续3天额外奖励
    private static final int CONTINUOUS_7_DAYS_BONUS = 10; // 连续7天额外奖励
    private static final int CONTINUOUS_30_DAYS_BONUS = 50; // 连续30天额外奖励

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> dailySignIn(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 检查今日是否已签到
        if (hasSignedInToday(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日已签到，请勿重复签到");
        }

        // 计算连续签到天数
        Integer continuousDays = calculateContinuousDays(userId);

        // 计算本次签到获得的积分
        Integer pointsEarned = calculateSignInPoints(continuousDays);

        // 保存签到记录
        SignInRecord signInRecord = SignInRecord.builder()
                .userId(userId)
                .signInDate(LocalDate.now())
                .continuousDays(continuousDays)
                .pointsEarned(pointsEarned)
                .build();
        boolean saved = this.save(signInRecord);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存签到记录失败");

        // 发放积分
        boolean pointsAdded = userPointsService.addPoints(
                userId,
                pointsEarned,
                PointsTypeEnum.SIGN_IN.getValue(),
                "每日签到奖励",
                null
        );
        ThrowUtils.throwIf(!pointsAdded, ErrorCode.SYSTEM_ERROR, "发放签到积分失败");

        log.info("用户 {} 签到成功，连续签到 {} 天，获得 {} 积分", userId, continuousDays, pointsEarned);

        // 返回签到结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("continuousDays", continuousDays);
        result.put("pointsEarned", pointsEarned);
        result.put("message", "签到成功！");

        // 添加连续签到提示
        if (continuousDays == 3) {
            result.put("bonusMessage", "连续签到3天，额外获得3积分！");
        } else if (continuousDays == 7) {
            result.put("bonusMessage", "连续签到7天，额外获得10积分！");
        } else if (continuousDays == 30) {
            result.put("bonusMessage", "连续签到30天，额外获得50积分！");
        }

        return result;
    }

    @Override
    public boolean hasSignedInToday(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(SignInRecord::getUserId, userId)
                .eq(SignInRecord::getSignInDate, LocalDate.now());

        return this.count(queryWrapper) > 0;
    }

    @Override
    public Integer getContinuousDays(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 获取最近的签到记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(SignInRecord::getUserId, userId)
                .orderBy(SignInRecord::getSignInDate, false)
                .limit(1);

        SignInRecord latestRecord = this.getOne(queryWrapper);

        if (latestRecord == null) {
            return 0;
        }

        // 如果最近签到日期是今天或昨天，返回连续天数，否则返回0
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate signInDate = latestRecord.getSignInDate();

        if (signInDate.equals(today) || signInDate.equals(yesterday)) {
            return latestRecord.getContinuousDays();
        }

        return 0;
    }

    @Override
    public Map<String, Object> getSignInStatus(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        Map<String, Object> status = new HashMap<>();
        status.put("hasSignedInToday", hasSignedInToday(userId));
        status.put("continuousDays", getContinuousDays(userId));
        status.put("signedDates", getSignedDates(userId));

        return status;
    }

    /**
     * 计算连续签到天数
     *
     * @param userId 用户ID
     * @return 连续签到天数
     */
    private Integer calculateContinuousDays(Long userId) {
        // 获取最近的签到记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(SignInRecord::getUserId, userId)
                .orderBy(SignInRecord::getSignInDate, false)
                .limit(1);

        SignInRecord latestRecord = this.getOne(queryWrapper);

        if (latestRecord == null) {
            // 第一次签到
            return 1;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate signInDate = latestRecord.getSignInDate();

        if (signInDate.equals(yesterday)) {
            // 昨天签到了，连续天数+1
            return latestRecord.getContinuousDays() + 1;
        } else {
            // 断签了，重新开始
            return 1;
        }
    }

    /**
     * 根据连续签到天数计算积分
     *
     * @param continuousDays 连续签到天数
     * @return 本次签到获得的积分
     */
    private Integer calculateSignInPoints(Integer continuousDays) {
        int points = BASE_SIGN_IN_POINTS;

        // 连续签到奖励
        if (continuousDays >= 30) {
            points += CONTINUOUS_30_DAYS_BONUS;
        } else if (continuousDays >= 7) {
            points += CONTINUOUS_7_DAYS_BONUS;
        } else if (continuousDays >= 3) {
            points += CONTINUOUS_3_DAYS_BONUS;
        }

        return points;
    }

    /**
     * 获取近一个月的签到日期集合
     */
    private List<String> getSignedDates(Long userId) {
        LocalDate startDate = LocalDate.now().minusDays(30);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(SignInRecord::getUserId, userId)
                .ge(SignInRecord::getSignInDate, startDate)
                .orderBy(SignInRecord::getSignInDate, true);

        return this.list(queryWrapper).stream()
                .map(record -> record.getSignInDate().toString())
                .collect(Collectors.toList());
    }

}
