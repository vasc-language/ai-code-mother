package com.spring.aicodemother.ai;

import com.spring.aicodemother.langgraph4j.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工厂：基于路由模型（qwen-turbo）创建用于项目命名的 AI 服务
 */
@Slf4j
@Configuration
public class AppNameGeneratorServiceFactory {

    /**
     * 创建一个新的 AI 命名服务实例（原型/多例）
     */
    public AppNameGeneratorService createAppNameGeneratorService() {
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AppNameGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个可注入的 Bean
     */
    @Bean
    public AppNameGeneratorService appNameGeneratorService() {
        return createAppNameGeneratorService();
    }
}

