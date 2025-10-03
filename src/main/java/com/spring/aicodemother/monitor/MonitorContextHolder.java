package com.spring.aicodemother.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * 上下文持有者
 */
@Slf4j
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_THREAD = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_THREAD.set(context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_THREAD.get();
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext() {
        CONTEXT_THREAD.remove();
    }
}
