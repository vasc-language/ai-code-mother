package com.spring.aicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.entity.AiModelConfig;
import com.spring.aicodemother.service.AiModelConfigService;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 动态AI模型工厂
 * 根据modelKey从数据库获取配置并动态创建StreamingChatModel
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Service
@Slf4j
public class DynamicAiModelFactory {

    @Resource
    private AiModelConfigService aiModelConfigService;

    @Resource
    private com.spring.aicodemother.monitor.AiModelMonitorListener aiModelMonitorListener;

    /**
     * 首选：流式模型配置中的 API Key
     */
    @Value("${langchain4j.open-ai.streaming-chat-model.api-key:}")
    private String streamingApiKey;

    /**
     * 兼容：普通 chat-model 配置中的 API Key
     */
    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String chatModelApiKey;

    /**
     * 环境变量（OpenAI 兼容）
     */
    @Value("${OPENAI_API_KEY:}")
    private String openAiApiKeyFromEnv;

    /**
     * 环境变量（DeepSeek 官方）
     */
    @Value("${DEEPSEEK_API_KEY:}")
    private String deepSeekApiKeyFromEnv;


    /**
     * StreamingChatModel 缓存
     * 缓存策略：
     * - 最大缓存 100 个模型实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, StreamingChatModel> modelCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("StreamingChatModel实例被移除，modelKey: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据modelKey获取StreamingChatModel（带缓存）
     *
     * @param modelKey 模型key
     * @return StreamingChatModel实例
     */
    public StreamingChatModel getStreamingChatModel(String modelKey) {
        if (modelKey == null || modelKey.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型key不能为空");
        }

        return modelCache.get(modelKey, key -> createStreamingChatModel(key));
    }

    /**
     * 创建新的StreamingChatModel实例
     *
     * @param modelKey 模型key
     * @return StreamingChatModel实例
     */
    private StreamingChatModel createStreamingChatModel(String modelKey) {
        // 从数据库获取模型配置
        AiModelConfig modelConfig = aiModelConfigService.getByModelKey(modelKey);
        if (modelConfig == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型配置不存在: " + modelKey);
        }

        String apiKeySource = resolveApiKeySource();
        String resolvedApiKey = resolveApiKey(apiKeySource);

        if (modelConfig.getIsEnabled() == null || modelConfig.getIsEnabled() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型已禁用: " + modelKey);
        }

        log.info("创建StreamingChatModel实例 - modelKey: {}, modelName: {}, provider: {}, baseUrl: {}",
                modelKey, modelConfig.getModelName(), modelConfig.getProvider(), modelConfig.getBaseUrl());
        if (!"langchain4j.open-ai.streaming-chat-model.api-key".equals(apiKeySource)) {
            log.info("DynamicAiModelFactory API Key 回退来源: {}", apiKeySource);
        }

        String requestModelName = resolveRequestModelName(modelConfig, modelKey);
        if (!modelKey.equals(requestModelName)) {
            log.warn("模型名兼容映射生效 - modelKey: {} -> requestModelName: {}", modelKey, requestModelName);
        }

        // 动态创建OpenAiStreamingChatModel
        // 使用统一的API密钥，支持所有55个模型
        // ✅ 注意：OpenAI的流式API会在最后一条消息中返回完整的Token usage统计（包括工具调用的Token）
        //    监听器会在 onResponse 中自动收集这些Token，并通过全局缓存累加
        return OpenAiStreamingChatModel.builder()
                .apiKey(resolvedApiKey)  // 使用解析后的 API Key（支持多来源回退）
                .modelName(requestModelName) // 向上游实际发送的模型名称（可能经过兼容映射）
                .baseUrl(modelConfig.getBaseUrl())
                .maxTokens(8192)         // 默认最大token数
                .temperature(0.0)        // 降低温度以提升工具调用稳定性
                .returnThinking(true)    // 读取推理字段，写入 AiMessage.thinking
                .sendThinking(true)      // 在后续 tool-calls 请求中回传 reasoning_content
                .logRequests(true)
                .logResponses(true)
                .listeners(java.util.List.of(aiModelMonitorListener))  // ✅ 注册监听器，监控Token使用
                .build();
    }

    private String resolveApiKeySource() {
        if (isNotBlank(streamingApiKey)) {
            return "langchain4j.open-ai.streaming-chat-model.api-key";
        }
        if (isNotBlank(chatModelApiKey)) {
            return "langchain4j.open-ai.chat-model.api-key";
        }
        if (isNotBlank(openAiApiKeyFromEnv)) {
            return "OPENAI_API_KEY";
        }
        if (isNotBlank(deepSeekApiKeyFromEnv)) {
            return "DEEPSEEK_API_KEY";
        }
        return "";
    }

    private String resolveApiKey(String source) {
        return switch (source) {
            case "langchain4j.open-ai.streaming-chat-model.api-key" -> streamingApiKey.trim();
            case "langchain4j.open-ai.chat-model.api-key" -> chatModelApiKey.trim();
            case "OPENAI_API_KEY" -> openAiApiKeyFromEnv.trim();
            case "DEEPSEEK_API_KEY" -> deepSeekApiKeyFromEnv.trim();
            default -> throw new BusinessException(
                    ErrorCode.OPERATION_ERROR,
                    "未配置 API Key，无法调用 AI 模型（已检查: streaming-chat-model/chat-model/OPENAI_API_KEY/DEEPSEEK_API_KEY）");
        };
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 解析不同供应商/端点下的实际模型名，避免配置漂移导致上游报 Model Not Exist
     */
    private String resolveRequestModelName(AiModelConfig modelConfig, String modelKey) {
        if (modelConfig == null || modelKey == null) {
            return modelKey;
        }
        String normalizedModelKey = modelKey.trim().toLowerCase();
        String provider = modelConfig.getProvider() == null ? "" : modelConfig.getProvider().trim().toLowerCase();
        String baseUrl = modelConfig.getBaseUrl() == null ? "" : modelConfig.getBaseUrl().trim().toLowerCase();

        // 官方 DeepSeek 兼容接口：常见可用模型是 deepseek-chat / deepseek-reasoner
        boolean officialDeepSeekEndpoint = "deepseek".equals(provider) || baseUrl.contains("api.deepseek.com");
        if (officialDeepSeekEndpoint) {
            if ("deepseek-r1".equals(normalizedModelKey) || "deepseek-r1-0528-free".equals(normalizedModelKey)) {
                return "deepseek-reasoner";
            }
            if ("deepseek-v3".equals(normalizedModelKey)
                    || "deepseek-v3.1".equals(normalizedModelKey)
                    || "deepseek-v3.2".equals(normalizedModelKey)) {
                return "deepseek-chat";
            }
        }

        return modelKey;
    }

    /**
     * 清除指定模型的缓存
     *
     * @param modelKey 模型key
     */
    public void invalidateCache(String modelKey) {
        modelCache.invalidate(modelKey);
        log.info("已清除模型缓存: {}", modelKey);
    }

    /**
     * 清除所有缓存
     */
    public void invalidateAllCache() {
        modelCache.invalidateAll();
        log.info("已清除所有模型缓存");
    }
}
