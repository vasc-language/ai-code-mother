package com.spring.aicodemother.core.handler;

import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 支持实时流式输出，为前端提供代码片段标记
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser, CodeGenTypeEnum codeGenType,
                               java.util.function.BooleanSupplier cancelled,
                               java.util.function.Consumer<String> finalResponseConsumer) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        StringBuilder codeBuffer = new StringBuilder();
        boolean[] inCodeBlock = new boolean[]{false};

        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    
                    // 为HTML和MULTI_FILE类型添加代码块标记
                    if (codeGenType == CodeGenTypeEnum.HTML || codeGenType == CodeGenTypeEnum.MULTI_FILE) {
                        return processCodeChunk(chunk, codeBuffer, inCodeBlock);
                    }
                    
                    return chunk;
                })
                .doOnComplete(() -> {
                    if (cancelled != null && cancelled.getAsBoolean()) {
                        return;
                    }
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    finalResponseConsumer.accept(aiResponse);
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                })
                .doOnError(error -> {
                    if (cancelled != null && cancelled.getAsBoolean()) {
                        return;
                    }
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
    
    /**
     * 处理代码块，为前端添加标记
     */
    private String processCodeChunk(String chunk, StringBuilder codeBuffer, boolean[] inCodeBlock) {
        // 检测代码块开始
        if (chunk.contains("```")) {
            if (!inCodeBlock[0]) {
                // 代码块开始，添加开始标记
                inCodeBlock[0] = true;
                return "[CODE_BLOCK_START]" + chunk;
            } else {
                // 代码块结束，添加结束标记
                inCodeBlock[0] = false;
                return chunk + "[CODE_BLOCK_END]";
            }
        }
        
        // 如果在代码块中，添加代码流标记
        if (inCodeBlock[0]) {
            return "[CODE_STREAM]" + chunk;
        }
        
        return chunk;
    }
}

