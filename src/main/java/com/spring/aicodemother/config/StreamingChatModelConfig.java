package com.spring.aicodemother.config;

import com.spring.aicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.time.Duration;
import java.util.List;

/**
 * 使用普通的流式对话模型
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private boolean logRequests;

    private boolean logResponses;

    @Bean
    @Scope("prototype") // 告诉 Spring 容器，每次获取 Bean 都创建一个新的实例，而不是复用单例
    public StreamingChatModel streamingChatModelPrototype() {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(modelName)) {
            return new FallbackStreamingChatModel("streaming-chat-model 未配置，使用占位模型");
        }
        return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .returnThinking(true)
            .sendThinking(true)
            .timeout(Duration.ofMinutes(5)) // 增加超时时间到 5 分钟，避免生成大型项目时超时
            .logRequests(logRequests)
            .logResponses(logResponses)
            .listeners(List.of(aiModelMonitorListener))
            .build();
    }

    /**
     * 安全占位 StreamingChatModel：仅在未配置模型参数时提供，调用即返回错误。
     */
    static class FallbackStreamingChatModel implements StreamingChatModel {
        private final String message;
        FallbackStreamingChatModel(String message) { this.message = message; }
        @Override
        public void doChat(dev.langchain4j.model.chat.request.ChatRequest chatRequest,
                           StreamingChatResponseHandler handler) {
            handler.onError(new IllegalStateException(message));
        }
    }
}
