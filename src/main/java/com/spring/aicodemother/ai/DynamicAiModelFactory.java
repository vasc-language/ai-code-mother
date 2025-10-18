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

    /**
     * 统一的API密钥（从配置文件读取）
     */
    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String unifiedApiKey;

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

        if (modelConfig.getIsEnabled() == null || modelConfig.getIsEnabled() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模型已禁用: " + modelKey);
        }

        log.info("创建StreamingChatModel实例 - modelKey: {}, modelName: {}, provider: {}, baseUrl: {}",
                modelKey, modelConfig.getModelName(), modelConfig.getProvider(), modelConfig.getBaseUrl());

        // 动态创建OpenAiStreamingChatModel
        // 使用统一的API密钥，支持所有55个模型
        return OpenAiStreamingChatModel.builder()
                .apiKey(unifiedApiKey)  // 使用统一的API密钥
                .modelName(modelKey)     // 使用modelKey作为模型名称
                .baseUrl(modelConfig.getBaseUrl())
                .maxTokens(8192)         // 默认最大token数
                .temperature(0.0)        // 降低温度以提升工具调用稳定性
                .logRequests(true)
                .logResponses(true)
                .build();
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
