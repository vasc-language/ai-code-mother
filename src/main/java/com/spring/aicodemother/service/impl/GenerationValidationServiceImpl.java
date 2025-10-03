package com.spring.aicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.spring.aicodemother.service.GenerationValidationService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 生成验证服务实现类
 */
@Service
@Slf4j
public class GenerationValidationServiceImpl implements GenerationValidationService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private com.spring.aicodemother.monitor.PointsMetricsCollector pointsMetricsCollector;

    // Redis key前缀
    private static final String KEY_WARNING_COUNT = "generation:warning:count:"; // 每日警告次数
    private static final String KEY_GENERATION_HASH = "generation:hash:"; // 24小时内生成记录
    private static final String KEY_BAN_FLAG = "generation:ban:"; // 禁止生成标记
    private static final String KEY_TOKEN_USAGE = "generation:token:usage:"; // 每日Token消耗
    private static final String KEY_GENERATION_COUNT = "generation:count:"; // 每日生成次数

    // 配置常量
    private static final int MAX_WARNINGS_PER_DAY = 3; // 每日最大警告次数
    private static final int PUNISHMENT_POINTS = 10; // 惩罚扣除积分
    private static final int MIN_CONTENT_LENGTH = 100; // 最小有效内容长度
    private static final int MAX_TOKENS_PER_DAY = 120000; // 每日最大Token消耗（12万）
    private static final int MAX_GENERATIONS_PER_DAY = 30; // 每日最大生成次数

    @Override
    public boolean isUserBannedToday(Long userId) {
        String key = KEY_BAN_FLAG + LocalDate.now() + ":" + userId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return "1".equals(value);
    }

    @Override
    public boolean isDuplicateGeneration(Long userId, String message) {
        if (StrUtil.isBlank(message)) {
            return false;
        }
        String messageHash = DigestUtil.md5Hex(message.trim());
        String key = KEY_GENERATION_HASH + userId;
        // 检查是否存在相同的hash
        Boolean exists = stringRedisTemplate.opsForSet().isMember(key, messageHash);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void recordGeneration(Long userId, String message) {
        if (StrUtil.isBlank(message)) {
            return;
        }
        String messageHash = DigestUtil.md5Hex(message.trim());
        String key = KEY_GENERATION_HASH + userId;
        // 添加到Set中，24小时过期
        stringRedisTemplate.opsForSet().add(key, messageHash);
        stringRedisTemplate.expire(key, Duration.ofHours(24));
    }

    @Override
    public boolean validateGenerationResult(String content) {
        // 检查内容是否为空或过短
        if (StrUtil.isBlank(content)) {
            log.warn("[无效生成] 生成内容为空");
            return false;
        }

        // 去除空白字符后检查长度
        String trimmed = content.trim();
        if (trimmed.length() < MIN_CONTENT_LENGTH) {
            log.warn("[无效生成] 生成内容过短: {} 字符", trimmed.length());
            return false;
        }

        // TODO: 后续可以添加安全过滤（色情、暴力、政治敏感内容检测）
        // if (containsSensitiveContent(content)) {
        //     log.warn("[无效生成] 包含敏感内容");
        //     return false;
        // }

        return true;
    }

    @Override
    public int recordWarningAndPunish(Long userId, String reason) {
        String today = LocalDate.now().toString();
        String countKey = KEY_WARNING_COUNT + today + ":" + userId;

        // 增加警告次数
        Long count = stringRedisTemplate.opsForValue().increment(countKey);
        if (count == null) {
            count = 1L;
        }

        // 设置过期时间为今日结束（到明天0点）
        long secondsUntilMidnight = Duration.between(
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)
        ).getSeconds();
        stringRedisTemplate.expire(countKey, secondsUntilMidnight, TimeUnit.SECONDS);

        log.warn("[生成警告] 用户={}, 原因={}, 今日警告次数={}", userId, reason, count);

        // 记录监控指标
        String metricReason = reason.contains("重复") ? "duplicate" : "other";
        pointsMetricsCollector.recordInvalidGeneration(userId.toString(), metricReason);

        // 额外扣减10积分作为惩罚
        try {
            userPointsService.deductPoints(
                    userId,
                    PUNISHMENT_POINTS,
                    com.spring.aicodemother.model.enums.PointsTypeEnum.GENERATE.getValue(),
                    "无效生成惩罚：" + reason,
                    null
            );
            log.info("[积分惩罚] 用户={}, 扣除{}积分", userId, PUNISHMENT_POINTS);
        } catch (Exception e) {
            log.error("[积分惩罚失败] 用户={}, 错误={}", userId, e.getMessage());
        }

        // 如果达到3次警告，设置禁止标记
        if (count >= MAX_WARNINGS_PER_DAY) {
            String banKey = KEY_BAN_FLAG + today + ":" + userId;
            stringRedisTemplate.opsForValue().set(banKey, "1", secondsUntilMidnight, TimeUnit.SECONDS);
            log.warn("[禁止生成] 用户={}, 今日警告次数达到{}", userId, MAX_WARNINGS_PER_DAY);
        }

        return count.intValue();
    }

    @Override
    public int getTodayWarningCount(Long userId) {
        String today = LocalDate.now().toString();
        String countKey = KEY_WARNING_COUNT + today + ":" + userId;
        String value = stringRedisTemplate.opsForValue().get(countKey);
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public boolean isTokenLimitExceeded(Long userId) {
        int usage = getTodayTokenUsage(userId);
        return usage >= MAX_TOKENS_PER_DAY;
    }

    @Override
    public boolean isGenerationCountExceeded(Long userId) {
        int count = getTodayGenerationCount(userId);
        return count >= MAX_GENERATIONS_PER_DAY;
    }

    @Override
    public void recordTokenUsage(Long userId, int tokens) {
        String today = LocalDate.now().toString();
        String key = KEY_TOKEN_USAGE + today + ":" + userId;

        // 增加Token消耗
        Long total = stringRedisTemplate.opsForValue().increment(key, tokens);

        // 设置过期时间为今日结束
        long secondsUntilMidnight = getSecondsUntilMidnight();
        stringRedisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);

        int todayTotal = total == null ? getTodayTokenUsage(userId) : total.intValue();
        if (todayTotal >= MAX_TOKENS_PER_DAY) {
            pointsMetricsCollector.recordTokenLimitExceeded(userId.toString());
        }

        log.info("[Token消耗] 用户={}, 本次消耗={}tokens, 今日累计={}tokens",
                userId, tokens, todayTotal);
    }

    @Override
    public int incrementGenerationCount(Long userId) {
        String today = LocalDate.now().toString();
        String key = KEY_GENERATION_COUNT + today + ":" + userId;

        // 增加生成次数
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 1L;
        }

        // 设置过期时间为今日结束
        long secondsUntilMidnight = getSecondsUntilMidnight();
        stringRedisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);

        log.info("[生成次数] 用户={}, 今日生成次数={}", userId, count);
        return count.intValue();
    }

    @Override
    public int getTodayTokenUsage(Long userId) {
        String today = LocalDate.now().toString();
        String key = KEY_TOKEN_USAGE + today + ":" + userId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public int getTodayGenerationCount(Long userId) {
        String today = LocalDate.now().toString();
        String key = KEY_GENERATION_COUNT + today + ":" + userId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * 获取到今日结束的秒数
     */
    private long getSecondsUntilMidnight() {
        return Duration.between(
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)
        ).getSeconds();
    }
}
