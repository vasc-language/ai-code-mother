package com.spring.aicodemother.ai;

import com.spring.aicodemother.langgraph4j.utils.SpringContextUtil;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        try {
            // 动态获取多例的路由 ChatModel，支持并发
            ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
            return AiServices.builder(AiCodeGenTypeRoutingService.class)
                    .chatModel(chatModel)
                    .build();
        } catch (Exception e) {
            log.warn("routingChatModelPrototype 不可用，使用启发式本地路由: {}", e.getMessage());
            return new HeuristicRoutingService();
        }
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}

/**
 * 本地启发式路由：在未配置模型时使用，尽量不阻断主流程。
 */
class HeuristicRoutingService implements AiCodeGenTypeRoutingService {
    @Override
    public CodeGenTypeEnum routeCodeGenType(String userPrompt) {
        if (userPrompt == null) {
            return CodeGenTypeEnum.HTML;
        }
        String p = userPrompt.toLowerCase();
        // 触发 Vue 项目关键词（中英文）
        String[] vueHints = new String[]{
                "vue", "vite", "component", "router", "pinia", "dashboard",
                "管理后台", "后台", "系统", "页面路由", "组件化", "前端项目", "单页应用"
        };
        for (String h : vueHints) {
            if (p.contains(h) || userPrompt.contains(h)) {
                return CodeGenTypeEnum.VUE_PROJECT;
            }
        }
        return CodeGenTypeEnum.HTML;
    }
}


