package com.spring.aicodemother.service;

import com.spring.aicodemother.model.dto.plan.PlanCacheData;
import com.spring.aicodemother.model.vo.DevelopmentPlanVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 计划缓存服务
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Service
@Slf4j
public class PlanCacheService {

    @Resource
    private RedissonClient redissonClient;

    private static final String PLAN_KEY_PREFIX = "dev_plan:";
    private static final long EXPIRE_HOURS = 24;

    /**
     * 保存计划到缓存
     *
     * @param appId       应用ID
     * @param userId      用户ID
     * @param requirement 用户需求
     * @param plan        开发计划
     * @return 计划ID
     */
    public String savePlan(Long appId, Long userId, String requirement, DevelopmentPlanVO plan) {
        String planId = generatePlanId(appId, userId);
        String key = PLAN_KEY_PREFIX + planId;

        RBucket<PlanCacheData> bucket = redissonClient.getBucket(key);
        PlanCacheData data = new PlanCacheData();
        data.setAppId(appId);
        data.setUserId(userId);
        data.setRequirement(requirement);
        data.setPlan(plan);
        data.setCreatedAt(LocalDateTime.now());

        bucket.set(data, EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("保存开发计划到缓存，planId={}, appId={}, userId={}", planId, appId, userId);
        return planId;
    }

    /**
     * 获取计划
     *
     * @param planId 计划ID
     * @return 计划数据
     */
    public Optional<PlanCacheData> getPlan(String planId) {
        String key = PLAN_KEY_PREFIX + planId;
        RBucket<PlanCacheData> bucket = redissonClient.getBucket(key);
        PlanCacheData data = bucket.get();
        if (data != null) {
            log.info("从缓存获取开发计划，planId={}", planId);
        }
        return Optional.ofNullable(data);
    }

    /**
     * 删除计划（用户开始开发后）
     *
     * @param planId 计划ID
     */
    public void deletePlan(String planId) {
        String key = PLAN_KEY_PREFIX + planId;
        boolean deleted = redissonClient.getBucket(key).delete();
        if (deleted) {
            log.info("删除开发计划缓存，planId={}", planId);
        }
    }

    /**
     * 生成计划ID
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 计划ID
     */
    private String generatePlanId(Long appId, Long userId) {
        return appId + "_" + userId + "_" + System.currentTimeMillis();
    }
}
