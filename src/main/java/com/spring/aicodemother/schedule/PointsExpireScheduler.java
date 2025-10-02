package com.spring.aicodemother.schedule;

import com.mybatisflex.core.query.QueryWrapper;
import com.spring.aicodemother.constants.PointsConstants;
import com.spring.aicodemother.model.entity.PointsRecord;
import com.spring.aicodemother.model.entity.UserPoints;
import com.spring.aicodemother.model.enums.PointsTypeEnum;
import com.spring.aicodemother.service.PointsRecordService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 积分过期定时任务
 * 每天凌晨1点执行，扫描并处理过期积分
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Component
public class PointsExpireScheduler {

    @Resource
    private PointsRecordService pointsRecordService;

    @Resource
    private UserPointsService userPointsService;

    /**
     * 每天凌晨1点执行积分过期检查
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expirePoints() {
        log.info("开始执行积分过期检查任务");
        long startTime = System.currentTimeMillis();

        try {
            // 提前发送到期提醒
            sendExpireReminders();

            // 查询所有已过期但未处理的积分记录
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .le(PointsRecord::getExpireTime, LocalDateTime.now())
                    .isNotNull(PointsRecord::getExpireTime)
                    .gt(PointsRecord::getPoints, 0) // 只处理增加积分的记录
                    .ne(PointsRecord::getType, PointsTypeEnum.EXPIRE.getValue()); // 排除已经标记为过期的记录

            List<PointsRecord> expiredRecords = pointsRecordService.list(queryWrapper);

            if (expiredRecords.isEmpty()) {
                log.info("没有需要处理的过期积分");
                return;
            }

            // 按用户分组统计过期积分
            Map<Long, Integer> userExpiredPointsMap = new HashMap<>();
            for (PointsRecord record : expiredRecords) {
                Long userId = record.getUserId();
                Integer expiredPoints = record.getPoints();
                userExpiredPointsMap.put(userId,
                        userExpiredPointsMap.getOrDefault(userId, 0) + expiredPoints);
            }

            // 处理每个用户的过期积分
            int processedUsers = 0;
            int totalExpiredPoints = 0;
            for (Map.Entry<Long, Integer> entry : userExpiredPointsMap.entrySet()) {
                Long userId = entry.getKey();
                Integer expiredPoints = entry.getValue();

                try {
                    // 获取用户积分信息
                    UserPoints userPoints = userPointsService.getOrCreateUserPoints(userId);

                    // 计算实际可扣减的积分（不能超过当前可用积分）
                    Integer pointsToDeduct = Math.min(expiredPoints, userPoints.getAvailablePoints());

                    if (pointsToDeduct > 0) {
                        // 扣减过期积分
                        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - pointsToDeduct);
                        userPointsService.updateById(userPoints);

                        // 记录过期扣减明细
                        PointsRecord expireRecord = PointsRecord.builder()
                                .userId(userId)
                                .points(-pointsToDeduct)
                                .balance(userPoints.getAvailablePoints())
                                .type(PointsTypeEnum.EXPIRE.getValue())
                                .reason("积分过期自动扣减")
                                .build();
                        pointsRecordService.save(expireRecord);

                        log.info("用户 {} 的 {} 积分已过期并扣减", userId, pointsToDeduct);
                        processedUsers++;
                        totalExpiredPoints += pointsToDeduct;
                    }
                } catch (Exception e) {
                    log.error("处理用户 {} 的过期积分失败: {}", userId, e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("积分过期检查任务完成，处理了 {} 个用户，共 {} 积分过期，耗时 {} ms",
                    processedUsers, totalExpiredPoints, (endTime - startTime));

        } catch (Exception e) {
            log.error("积分过期检查任务执行失败", e);
            throw e;
    }
}

    private void sendExpireReminders() {
        try {
            LocalDate targetDate = LocalDate.now().plusDays(PointsConstants.POINTS_EXPIRE_REMIND_DAYS);
            LocalDateTime dayStart = targetDate.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            QueryWrapper queryWrapper = QueryWrapper.create()
                    .ge(PointsRecord::getExpireTime, dayStart)
                    .lt(PointsRecord::getExpireTime, dayEnd)
                    .gt(PointsRecord::getPoints, 0)
                    .ne(PointsRecord::getType, PointsTypeEnum.EXPIRE.getValue());

            List<PointsRecord> expiringRecords = pointsRecordService.list(queryWrapper);
            if (expiringRecords.isEmpty()) {
                return;
            }

            Set<Long> expiringRecordIds = expiringRecords.stream()
                    .map(PointsRecord::getId)
                    .collect(Collectors.toSet());

            if (expiringRecordIds.isEmpty()) {
                return;
            }

            QueryWrapper reminderQuery = QueryWrapper.create()
                    .eq(PointsRecord::getType, PointsTypeEnum.EXPIRE_REMINDER.getValue())
                    .in(PointsRecord::getRelatedId, expiringRecordIds);

            Set<Long> remindedRecordIds = pointsRecordService.list(reminderQuery).stream()
                    .map(PointsRecord::getRelatedId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (PointsRecord record : expiringRecords) {
                if (remindedRecordIds.contains(record.getId())) {
                    continue;
                }

                UserPoints userPoints = userPointsService.getOrCreateUserPoints(record.getUserId());
                PointsRecord reminder = PointsRecord.builder()
                        .userId(record.getUserId())
                        .points(0)
                        .balance(userPoints.getAvailablePoints())
                        .type(PointsTypeEnum.EXPIRE_REMINDER.getValue())
                        .reason(String.format("积分将于%s过期，请及时使用", record.getExpireTime().toLocalDate()))
                        .relatedId(record.getId())
                        .expireTime(record.getExpireTime())
                        .build();
                pointsRecordService.save(reminder);
            }
        } catch (Exception e) {
            log.error("积分到期提醒发送失败", e);
        }
    }
}
