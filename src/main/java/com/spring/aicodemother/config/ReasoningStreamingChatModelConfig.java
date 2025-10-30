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
 * 推理模型流式输出
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private Boolean logRequests = false;

    private Boolean logResponses = false;

    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(modelName)) {
            return new FallbackStreamingChatModel("reasoning-streaming-chat-model 未配置，使用占位模型");
        }
        return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .timeout(Duration.ofMinutes(5)) // 增加超时时间到 5 分钟，避免推理任务超时
            .logRequests(logRequests)
            .logResponses(logResponses)
            .listeners(List.of(aiModelMonitorListener))
            .build();
    }

    /** 占位 StreamingChatModel，与普通占位一致 */
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

