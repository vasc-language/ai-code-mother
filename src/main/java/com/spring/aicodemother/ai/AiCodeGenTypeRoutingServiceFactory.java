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
 * 本地启发式路由：在未配置模型或AI失败时使用，尽量不阻断主流程。
 */
class HeuristicRoutingService implements AiCodeGenTypeRoutingService {
    @Override
    public CodeGenTypeEnum routeCodeGenType(String userPrompt) {
        if (userPrompt == null) {
            return CodeGenTypeEnum.MULTI_FILE;
        }
        String p = userPrompt.toLowerCase();
        String original = userPrompt; // 保留原始大小写用于中文匹配

        // 优先级1: 明确技术栈要求
        String[] vueExplicitHints = new String[]{
            "vue", "VUE", "Vue", "vue模式", "VUE模式", "使用vue", "使用VUE",
            "router", "路由", "pinia", "状态管理", "组件化"
        };
        for (String hint : vueExplicitHints) {
            if (p.contains(hint.toLowerCase()) || original.contains(hint)) {
                return CodeGenTypeEnum.VUE_PROJECT;
            }
        }

        // 优先级2: 复杂项目关键词
        String[] vueComplexHints = new String[]{
            "管理后台", "后台", "系统", "平台", "工程", "dashboard",
            "作品展示", "画廊", "瀑布流", "前端项目", "单页应用", "spa"
        };
        int complexCount = 0;
        for (String h : vueComplexHints) {
            if (p.contains(h) || original.contains(h)) {
                complexCount++;
            }
        }
        // 包含2个以上复杂关键词，或者文字很长且包含1个
        if (complexCount >= 2 || (complexCount >= 1 && userPrompt.length() > 50)) {
            return CodeGenTypeEnum.VUE_PROJECT;
        }

        // 优先级3: 简单HTML页面
        String[] htmlHints = new String[]{"登录页", "注册页", "404", "简单页面", "单页"};
        for (String h : htmlHints) {
            if (original.contains(h)) {
                return CodeGenTypeEnum.HTML;
            }
        }

        // 默认返回多文件模式
        return CodeGenTypeEnum.MULTI_FILE;
    }
}


