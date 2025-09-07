package com.spring.aicodemother.core.handler;

import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 1. HTML 类型 -> SimpleTextStreamHandler
 * 2. MULTI_FILE 类型 -> MultiFileStreamHandler（支持多文件并行输出）
 * 3. TokenStream 格式的复杂流（VUE_PROJECT） -> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE_PROJECT -> // 使用注入的组件实例
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML -> // HTML 使用简单文本处理器
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser, codeGenType);
            case MULTI_FILE -> // MULTI_FILE 使用专用的多文件处理器
                    new MultiFileStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}

