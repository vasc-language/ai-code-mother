package com.spring.aicodemother.monitor;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文持有者
 * 支持跨线程的Token累加（用于工具调用场景）
 */
@Slf4j
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_THREAD = new ThreadLocal<>();
    
    /**
     * ✅ 全局Context缓存：支持跨线程访问（key: userId+appId）
     * 用于工具调用场景：工具调用在新线程中执行，无法通过ThreadLocal获取context
     */
    private static final ConcurrentHashMap<String, MonitorContext> GLOBAL_CONTEXT_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置监控上下文（同时存储到ThreadLocal和全局缓存）
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_THREAD.set(context);
        // 同时存储到全局缓存，支持跨线程访问
        if (context != null && context.getUserId() != null && context.getAppId() != null) {
            String cacheKey = buildCacheKey(context.getUserId(), context.getAppId());
            GLOBAL_CONTEXT_CACHE.put(cacheKey, context);
            log.info("[Context缓存] 存储到全局缓存: {}", cacheKey);
        }
    }

    /**
     * 获取当前监控上下文（优先从ThreadLocal，否则尝试从全局缓存）
     */
    public static MonitorContext getContext() {
        return CONTEXT_THREAD.get();
    }

    /**
     * ✅ 根据userId和appId从全局缓存获取Context（用于工具调用场景）
     */
    public static MonitorContext getContextFromCache(String userId, String appId) {
        if (userId == null || appId == null) {
            return null;
        }
        String cacheKey = buildCacheKey(userId, appId);
        return GLOBAL_CONTEXT_CACHE.get(cacheKey);
    }

    /**
     * ✅ 累加Token到Context（支持跨线程）
     * @param userId 用户ID
     * @param appId 应用ID
     * @param additionalTokens 额外的Token数
     */
    public static void accumulateTokens(String userId, String appId, Long additionalTokens) {
        if (userId == null || appId == null || additionalTokens == null || additionalTokens <= 0) {
            return;
        }
        String cacheKey = buildCacheKey(userId, appId);
        MonitorContext context = GLOBAL_CONTEXT_CACHE.get(cacheKey);
        if (context != null) {
            synchronized (context) {
                Long current = context.getTotalTokens();
                long newTotal = (current == null ? 0 : current) + additionalTokens;
                context.setTotalTokens(newTotal);
                log.info("[Token累加] userId:{}, appId:{}, +{} = {}", userId, appId, additionalTokens, newTotal);
            }
        } else {
            log.warn("[Token累加失败] 未找到Context: {}", cacheKey);
        }
    }

    /**
     * ✅ 更新Context到全局缓存（用于积分扣费失败标记等）
     * @param userId 用户ID
     * @param appId 应用ID
     * @param context 更新后的Context
     */
    public static void updateContext(String userId, String appId, MonitorContext context) {
        if (userId == null || appId == null || context == null) {
            return;
        }
        String cacheKey = buildCacheKey(userId, appId);
        GLOBAL_CONTEXT_CACHE.put(cacheKey, context);
        log.debug("[Context更新] 更新全局缓存: {}", cacheKey);
    }

    /**
     * 清除监控上下文（同时清除全局缓存）
     */
    public static void clearContext() {
        MonitorContext context = CONTEXT_THREAD.get();
        CONTEXT_THREAD.remove();
        
        // 清除全局缓存
        if (context != null && context.getUserId() != null && context.getAppId() != null) {
            String cacheKey = buildCacheKey(context.getUserId(), context.getAppId());
            GLOBAL_CONTEXT_CACHE.remove(cacheKey);
            log.info("[Context缓存] 从全局缓存清除: {}", cacheKey);
        }
    }

    /**
     * 构建缓存Key
     */
    private static String buildCacheKey(String userId, String appId) {
        return userId + ":" + appId;
    }
}
