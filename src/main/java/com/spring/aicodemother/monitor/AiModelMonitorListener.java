package com.spring.aicodemother.monitor;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 监听器触发指令收集
 */
@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    // 用于存储请求开始时间的键
    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    // 用于监控上下文传递（因为请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    @Resource
    private com.spring.aicodemother.service.UserPointsService userPointsService;

    @Resource
    private com.spring.aicodemother.service.AiModelConfigService aiModelConfigService;

    @Autowired
    private View error;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 记录请求开始时间
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        // 从监控上下文中获取信息
        MonitorContext context = MonitorContextHolder.getContext();
        
        // ✅ 修复：即使在工具调用场景（context为null）也要存储标记，确保onResponse能识别
        if (context == null) {
            log.debug("[工具调用检测] MonitorContext为null，这是工具调用的子请求");
            // 不直接返回，继续处理，在 onResponse 中统一收集 Token
        } else {
            // 主请求：存储完整的 context
            String userId = context.getUserId();
            String appId = context.getAppId();
            requestContext.attributes().put(MONITOR_CONTEXT_KEY, context);
            // 获取模型名称
            String modelName = requestContext.chatRequest().modelName();
            // 记录请求指标
            aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
            log.info("[AI请求-主流程] userId:{}, appId:{}, model:{}", userId, appId, modelName);
        }
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 从属性中获取监控信息（由 onRequest 方法存储）
        Map<Object, Object> attributes = responseContext.attributes();
        // 从监控上下文中获取信息
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        
        // ✅ 修复：区分主请求和工具调用，确保Token统计不丢失
        if (context == null) {
            // 工具调用场景：尝试从 ThreadLocal 再次获取（可能主线程已设置）
            context = MonitorContextHolder.getContext();
            if (context == null) {
                // ⚠️ 关键：由于配置了includeUsage=true，工具调用的Token会在主请求的最终响应中汇总
                // 此处跳过单独的工具调用监控，等待主请求的最终usage统计
                log.debug("[工具调用-跳过] 等待主请求的最终Token统计（已启用includeUsage）");
                return;
            }
            log.debug("[工具调用-Token收集] 从ThreadLocal获取到context，记录工具调用Token");
        }
        
        String userId = context.getUserId();
        String appId = context.getAppId();
        
        // ✅ 安全检查：确保userId和appId不为空
        if (userId == null || appId == null) {
            log.warn("[监控异常] context存在但userId或appId为null，跳过");
            return;
        }
        // 获取模型名称
        String modelName = responseContext.chatResponse().modelName();
        // 记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        // 记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);
        // 记录 Token 使用情况（会更新 context.totalTokens）
        recordTokenUsage(responseContext, context, userId, appId, modelName);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // ✅ 修复：从attributes和全局缓存获取context，避免NPE
        Map<Object, Object> attributes = errorContext.attributes();
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        
        // 如果attributes中没有，尝试从ThreadLocal获取
        if (context == null) {
            context = MonitorContextHolder.getContext();
        }
        
        // 如果还是null，说明是工具调用或其他异常场景，跳过监控
        if (context == null) {
            log.debug("[错误监控-跳过] MonitorContext为null，跳过错误监控");
            return;
        }
        
        String userId = context.getUserId();
        String appId = context.getAppId();
        
        // 安全检查
        if (userId == null || appId == null) {
            log.warn("[错误监控-跳过] userId或appId为null");
            return;
        }
        
        // 获取模型名称和错误类型
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
        // 记录响应时间（即使是错误响应）
        recordResponseTime(attributes, userId, appId, modelName);
        
        log.info("[错误监控] userId:{}, appId:{}, model:{}, error:{}", userId, appId, modelName, errorMessage);
    }


    /**
     * 记录响应时间
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        Duration responseTime = Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }

    /**
     * 记录Token使用情况(并扣除积分)
     * ✅ 修复：支持跨线程Token统计，自动累加工具调用的Token
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, MonitorContext context,
                                  String userId, String appId, String modelName) {
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        
        if (tokenUsage != null) {
            Integer inputTokens = tokenUsage.inputTokenCount();
            Integer outputTokens = tokenUsage.outputTokenCount();
            Integer totalTokens = tokenUsage.totalTokenCount();
            
            log.info("[Token消耗] userId:{}, appId:{}, model:{}, input:{}, output:{}, total:{}", 
                    userId, appId, modelName, inputTokens, outputTokens, totalTokens);
            
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", inputTokens);
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", outputTokens);
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", totalTokens);
            
            // ✅ 关键修复：使用全局缓存累加Token（支持跨线程）
            if (totalTokens != null && totalTokens > 0) {
                MonitorContextHolder.accumulateTokens(userId, appId, totalTokens.longValue());
                log.info("[Token累加] userId:{}, appId:{}, 本次消耗:{}", userId, appId, totalTokens);
            }

            // ✅ 修复：从全局缓存获取最新的context（可能已被累加更新）
            if (context != null) {
                // 从全局缓存获取最新的context（包含累加的Token）
                MonitorContext latestContext = MonitorContextHolder.getContextFromCache(userId, appId);
                if (latestContext != null) {
                    context = latestContext;
                }

                // 获取modelKey并扣除积分
                String modelKey = context.getModelKey();
                
                if (StrUtil.isNotBlank(modelKey) && totalTokens != null && totalTokens > 0) {
                    try {
                        // 计算需要扣除的积分
                        Integer points = aiModelConfigService.calculatePoints(modelKey, totalTokens);
                        if (points != null && points > 0) {
                            // ⚠️ 临时禁用积分扣除（测试期间）- 测试完成后请取消注释
                            /*
                            userPointsService.deductPointsWithModel(
                                Long.valueOf(userId),
                                points,
                                com.spring.aicodemother.model.enums.PointsTypeEnum.AI_GENERATE.getValue(),
                                String.format("AI生成消耗(%s, %d tokens)", modelKey, totalTokens),
                                Long.valueOf(appId),
                                modelKey,
                                totalTokens
                            );
                            */
                            log.info("[积分扣除-测试模式] userId:{}, modelKey:{}, tokens:{}, points:{} (未实际扣除)", 
                                    userId, modelKey, totalTokens, points);
                        }
                    } catch (Exception e) {
                        log.error("[积分扣除失败] userId:{}, modelKey:{}, tokens:{}, error:{}", 
                                userId, modelKey, totalTokens, e.getMessage(), e);
                    }
                } else {
                    log.warn("[跳过积分扣除] modelKey:{}, totalTokens:{}", modelKey, totalTokens);
                }
            } else {
                log.warn("[监控上下文为null] 跳过积分扣除");
            }
        } else {
            log.warn("[TokenUsage为null] userId:{}, appId:{}, modelName:{}", userId, appId, modelName);
        }
    }
}

