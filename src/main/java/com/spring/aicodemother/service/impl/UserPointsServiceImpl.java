package com.spring.aicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.constants.PointsConstants;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.mapper.UserPointsMapper;
import com.spring.aicodemother.model.entity.PointsRecord;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.entity.UserPoints;
import com.spring.aicodemother.model.enums.UserRoleEnum;
import com.spring.aicodemother.mapper.UserMapper;
import com.spring.aicodemother.service.PointsRecordService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户积分 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {

    @Resource
    private PointsRecordService pointsRecordService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private com.spring.aicodemother.monitor.PointsMetricsCollector pointsMetricsCollector;

    @Resource
    private UserMapper userMapper;

    private static final String POINTS_LOCK_PREFIX = "points:lock:";

    @Override
    public UserPoints getOrCreateUserPoints(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 先查询是否存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId);
        UserPoints userPoints = this.getOne(queryWrapper);

        // 如果不存在则创建
        if (userPoints == null) {
            userPoints = UserPoints.builder()
                    .userId(userId)
                    .totalPoints(0)
                    .availablePoints(0)
                    .frozenPoints(0)
                    .build();
            boolean saved = this.save(userPoints);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "创建用户积分记录失败");
            log.info("为用户 {} 创建了积分账户", userId);
        }

        return userPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String type, String reason, Long relatedId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(points == null || points <= 0, ErrorCode.PARAMS_ERROR, "积分数量必须大于0");
        ThrowUtils.throwIf(type == null || type.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "积分类型不能为空");

        if (isAdmin(userId)) {
            log.debug("管理员 {} 发放 {} 积分，无需记录账户变动", userId, points);
            return true;
        }

        String lockKey = POINTS_LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待3秒，锁自动释放时间为配置的超时时间
            boolean locked = lock.tryLock(3, PointsConstants.POINTS_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
            }

            // 获取或创建用户积分记录
            UserPoints userPoints = getOrCreateUserPoints(userId);

            // 更新积分
            userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
            userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
            boolean updated = this.updateById(userPoints);
            ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "更新用户积分失败");

            // 记录积分变动明细
            LocalDateTime expireTime = LocalDateTime.now().plusDays(PointsConstants.POINTS_VALIDITY_DAYS);
            PointsRecord record = PointsRecord.builder()
                    .userId(userId)
                    .points(points)
                    .balance(userPoints.getAvailablePoints())
                    .type(type)
                    .reason(reason)
                    .relatedId(relatedId)
                    .expireTime(expireTime)
                    .build();
            boolean recordSaved = pointsRecordService.save(record);
            ThrowUtils.throwIf(!recordSaved, ErrorCode.SYSTEM_ERROR, "保存积分明细失败");

            // 记录监控指标
            pointsMetricsCollector.recordPointsGranted(userId.toString(), type, points);

            log.info("用户 {} 增加了 {} 积分，类型：{}，原因：{}，剩余：{}",
                    userId, points, type, reason, userPoints.getAvailablePoints());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("用户 {} 增加积分时获取锁被中断", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } finally {
            // 释放锁（只有当前线程持有锁时才释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points, String type, String reason, Long relatedId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(points == null || points <= 0, ErrorCode.PARAMS_ERROR, "积分数量必须大于0");
        ThrowUtils.throwIf(type == null || type.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "积分类型不能为空");

        if (isAdmin(userId)) {
            log.debug("管理员 {} 扣减 {} 积分被跳过（无限额度）", userId, points);
            return true;
        }

        String lockKey = POINTS_LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待3秒，锁自动释放时间为配置的超时时间
            boolean locked = lock.tryLock(3, PointsConstants.POINTS_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
            }

            // 获取用户积分记录
            UserPoints userPoints = getOrCreateUserPoints(userId);

            // 检查积分是否充足
            if (userPoints.getAvailablePoints() < points) {
                log.warn("用户 {} 积分不足，当前：{}，需要：{}", userId, userPoints.getAvailablePoints(), points);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "积分不足");
            }

            // 扣减积分
            userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
            boolean updated = this.updateById(userPoints);
            ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "更新用户积分失败");

            // 记录积分变动明细（负数表示扣减）
            PointsRecord record = PointsRecord.builder()
                    .userId(userId)
                    .points(-points)
                    .balance(userPoints.getAvailablePoints())
                    .type(type)
                    .reason(reason)
                    .relatedId(relatedId)
                    .build();
            boolean recordSaved = pointsRecordService.save(record);
            ThrowUtils.throwIf(!recordSaved, ErrorCode.SYSTEM_ERROR, "保存积分明细失败");

            // 记录监控指标
            pointsMetricsCollector.recordPointsConsumed(userId.toString(), points);

            log.info("用户 {} 扣减了 {} 积分，类型：{}，原因：{}，剩余：{}",
                    userId, points, type, reason, userPoints.getAvailablePoints());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("用户 {} 扣减积分时获取锁被中断", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        } finally {
            // 释放锁（只有当前线程持有锁时才释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Integer getAvailablePoints(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        UserPoints userPoints = getOrCreateUserPoints(userId);
        return userPoints.getAvailablePoints();
    }

    @Override
    public boolean checkPointsSufficient(Long userId, Integer points) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(points == null || points < 0, ErrorCode.PARAMS_ERROR, "积分数量不能为负数");

        if (isAdmin(userId)) {
            return true;
        }

        Integer availablePoints = getAvailablePoints(userId);
        return availablePoints >= points;
    }

    private boolean isAdmin(Long userId) {
        User user = userMapper.selectOneByQuery(QueryWrapper.create().eq("id", userId).limit(1));
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}
