package com.spring.aicodemother.schedule;

import com.mybatisflex.core.query.QueryWrapper;
import com.spring.aicodemother.model.entity.PointsRecord;
import com.spring.aicodemother.model.entity.UserPoints;
import com.spring.aicodemother.model.enums.PointsStatusEnum;
import com.spring.aicodemother.service.PointsRecordService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 积分数据一致性校验定时任务
 * 每天凌晨3点执行，检查用户积分账户与流水记录的一致性
 *
 * @author AI Assistant
 */
@Slf4j
@Component
public class PointsConsistencyChecker {

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private PointsRecordService pointsRecordService;

    /**
     * 每天凌晨3点执行数据一致性检查
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkPointsConsistency() {
        log.info("开始执行积分数据一致性检查任务");
        long startTime = System.currentTimeMillis();

        try {
            // 获取所有用户积分账户
            List<UserPoints> allUsers = userPointsService.list();
            
            if (allUsers.isEmpty()) {
                log.info("没有用户积分数据需要检查");
                return;
            }

            int totalUsers = allUsers.size();
            int inconsistentCount = 0;
            int fixedCount = 0;

            for (UserPoints userPoints : allUsers) {
                Long userId = userPoints.getUserId();
                
                try {
                    // 检查单个用户的数据一致性
                    ConsistencyCheckResult result = checkUserPointsConsistency(userId, userPoints);
                    
                    if (!result.isConsistent()) {
                        inconsistentCount++;
                        log.warn("发现用户 {} 积分不一致：账户显示={}，流水计算={}，差异={}",
                                userId, 
                                result.getAccountBalance(), 
                                result.getCalculatedBalance(),
                                result.getDifference());
                        
                        // 尝试自动修复（可选，谨慎使用）
                        if (result.isAutoFixable()) {
                            boolean fixed = autoFixUserPoints(userId, userPoints, result);
                            if (fixed) {
                                fixedCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("检查用户 {} 积分一致性失败", userId, e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("积分数据一致性检查完成，总用户数：{}，发现不一致：{}，已自动修复：{}，耗时：{}ms",
                    totalUsers, inconsistentCount, fixedCount, (endTime - startTime));
            
            // 如果发现不一致且无法自动修复，发送告警
            if (inconsistentCount > fixedCount) {
                sendAlert(inconsistentCount - fixedCount);
            }

        } catch (Exception e) {
            log.error("积分数据一致性检查任务执行失败", e);
        }
    }

    /**
     * 检查单个用户的积分一致性
     *
     * @param userId 用户ID
     * @param userPoints 用户积分账户
     * @return 检查结果
     */
    private ConsistencyCheckResult checkUserPointsConsistency(Long userId, UserPoints userPoints) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        result.setUserId(userId);
        result.setAccountBalance(userPoints.getAvailablePoints());

        // 方法1：通过 remaining_points 计算（FIFO策略后的精确方法）
        Integer calculatedByRemaining = calculateBalanceByRemainingPoints(userId);
        
        // 方法2：通过流水总和计算（传统方法，用于验证）
        Integer calculatedBySum = calculateBalanceBySum(userId);

        result.setCalculatedBalance(calculatedByRemaining);
        result.setCalculatedBySum(calculatedBySum);

        // 判断是否一致
        boolean consistent = userPoints.getAvailablePoints().equals(calculatedByRemaining);
        result.setConsistent(consistent);

        if (!consistent) {
            result.setDifference(userPoints.getAvailablePoints() - calculatedByRemaining);
            
            // 判断是否可以自动修复（差异小于100积分且流水记录完整）
            boolean autoFixable = Math.abs(result.getDifference()) < 100 
                    && calculatedByRemaining > 0;
            result.setAutoFixable(autoFixable);
        }

        return result;
    }

    /**
     * 通过 remaining_points 字段计算用户可用积分
     * （FIFO策略下的精确计算）
     *
     * @param userId 用户ID
     * @return 计算出的可用积分
     */
    private Integer calculateBalanceByRemainingPoints(Long userId) {
        // 查询所有 ACTIVE 和 PARTIAL_CONSUMED 状态的记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(PointsRecord::getUserId, userId)
                .in(PointsRecord::getStatus, Arrays.asList(
                        PointsStatusEnum.ACTIVE.getValue(),
                        PointsStatusEnum.PARTIAL_CONSUMED.getValue()
                ))
                .gt(PointsRecord::getPoints, 0); // 只统计增加积分的记录

        List<PointsRecord> activeRecords = pointsRecordService.list(queryWrapper);

        int totalRemaining = 0;
        for (PointsRecord record : activeRecords) {
            Integer remaining = record.getRemainingPoints();
            if (remaining != null && remaining > 0) {
                totalRemaining += remaining;
            } else if (remaining == null) {
                // 兼容旧数据：如果没有 remaining_points，使用原始 points
                totalRemaining += record.getPoints();
            }
        }

        return totalRemaining;
    }

    /**
     * 通过流水记录总和计算用户可用积分
     * （传统方法，用于验证）
     *
     * @param userId 用户ID
     * @return 计算出的可用积分
     */
    private Integer calculateBalanceBySum(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(PointsRecord::getUserId, userId);

        List<PointsRecord> allRecords = pointsRecordService.list(queryWrapper);

        int totalPoints = 0;
        for (PointsRecord record : allRecords) {
            totalPoints += record.getPoints();
        }

        return Math.max(0, totalPoints); // 确保不为负数
    }

    /**
     * 自动修复用户积分
     *
     * @param userId 用户ID
     * @param userPoints 用户积分账户
     * @param checkResult 检查结果
     * @return 是否修复成功
     */
    private boolean autoFixUserPoints(Long userId, UserPoints userPoints, ConsistencyCheckResult checkResult) {
        try {
            log.info("尝试自动修复用户 {} 的积分，将 {} 修正为 {}",
                    userId, checkResult.getAccountBalance(), checkResult.getCalculatedBalance());

            // 更新用户积分账户
            userPoints.setAvailablePoints(checkResult.getCalculatedBalance());
            boolean updated = userPointsService.updateById(userPoints);

            if (updated) {
                log.info("用户 {} 积分自动修复成功", userId);
                return true;
            } else {
                log.error("用户 {} 积分自动修复失败：数据库更新失败", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("用户 {} 积分自动修复失败", userId, e);
            return false;
        }
    }

    /**
     * 发送告警通知
     *
     * @param unfixedCount 未修复的不一致数量
     */
    private void sendAlert(int unfixedCount) {
        // TODO: 接入告警系统（邮件、钉钉、企业微信等）
        log.error("【积分系统告警】发现 {} 个用户积分不一致且无法自动修复，需要人工介入！", unfixedCount);
    }

    /**
     * 一致性检查结果
     */
    private static class ConsistencyCheckResult {
        private Long userId;
        private Integer accountBalance;      // 账户余额
        private Integer calculatedBalance;   // 计算余额（通过remaining_points）
        private Integer calculatedBySum;     // 计算余额（通过流水总和）
        private boolean consistent;          // 是否一致
        private Integer difference;          // 差异
        private boolean autoFixable;         // 是否可自动修复

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getAccountBalance() {
            return accountBalance;
        }

        public void setAccountBalance(Integer accountBalance) {
            this.accountBalance = accountBalance;
        }

        public Integer getCalculatedBalance() {
            return calculatedBalance;
        }

        public void setCalculatedBalance(Integer calculatedBalance) {
            this.calculatedBalance = calculatedBalance;
        }

        public Integer getCalculatedBySum() {
            return calculatedBySum;
        }

        public void setCalculatedBySum(Integer calculatedBySum) {
            this.calculatedBySum = calculatedBySum;
        }

        public boolean isConsistent() {
            return consistent;
        }

        public void setConsistent(boolean consistent) {
            this.consistent = consistent;
        }

        public Integer getDifference() {
            return difference;
        }

        public void setDifference(Integer difference) {
            this.difference = difference;
        }

        public boolean isAutoFixable() {
            return autoFixable;
        }

        public void setAutoFixable(boolean autoFixable) {
            this.autoFixable = autoFixable;
        }
    }
}
