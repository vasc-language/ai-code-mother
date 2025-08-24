package com.spring.aicodemother.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.ai.model.message.*;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.core.build.VueProjectBuilder;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.spring.aicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    // 异步构造 Vue 项目
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        try {
            // 解析 JSON
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
            
            switch (typeEnum) {
                case AI_RESPONSE -> {
                    try {
                        AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                        String data = aiMessage.getData();
                        if (data != null) {
                            // 直接拼接响应
                            chatHistoryStringBuilder.append(data);
                            return data;
                        }
                        return "";
                    } catch (Exception e) {
                        log.error("解析AI响应消息失败: {}", e.getMessage());
                        return "";
                    }
                }
                case TOOL_REQUEST -> {
                    try {
                        ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                        String toolId = toolRequestMessage.getId();
                        // 检查是否是第一次看到这个工具 ID
                        if (toolId != null && !seenToolIds.contains(toolId)) {
                            // 第一次调用这个工具，记录 ID 并完整返回工具信息
                            seenToolIds.add(toolId);
                            return "\n\n[选择工具] 写入文件\n\n";
                        } else {
                            // 不是第一次调用这个工具，直接返回空
                            return "";
                        }
                    } catch (Exception e) {
                        log.error("解析工具请求消息失败: {}", e.getMessage());
                        return "\n\n[工具调用] 工具请求解析失败\n\n";
                    }
                }
                case TOOL_EXECUTED -> {
                    try {
                        ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                        if (toolExecutedMessage.getArguments() == null) {
                            log.warn("工具执行消息参数为空");
                            return "\n\n[工具调用] 文件写入完成\n\n";
                        }
                        
                        JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                        String relativeFilePath = jsonObject.getStr("relativeFilePath", "未知文件");
                        String suffix = FileUtil.getSuffix(relativeFilePath);
                        String content = jsonObject.getStr("content", "");
                        
                        // 限制内容长度显示
                        String displayContent = content.length() > 500 ? 
                            content.substring(0, 500) + "\n... (内容过长，已截断)" : 
                            content;
                        
                        String result = String.format("""
                                [工具调用] 写入文件 %s
                                ```%s
                                %s
                                ```
                                """, relativeFilePath, suffix, displayContent);
                        // 输出前端和要持久化的内容
                        String output = String.format("\n\n%s\n\n", result);
                        chatHistoryStringBuilder.append(output);
                        return output;
                    } catch (Exception e) {
                        log.error("解析工具执行消息失败: {}", e.getMessage(), e);
                        String errorOutput = "\n\n[错误] 工具执行信息解析失败，但流程继续\n\n";
                        chatHistoryStringBuilder.append(errorOutput);
                        return errorOutput;
                    }
                }
                default -> {
                    log.error("不支持的消息类型: {}", typeEnum);
                    return "";
                }
            }
        } catch (Exception e) {
            log.error("解析消息块失败，原始内容: {}, 错误: {}", chunk, e.getMessage());
            // 发生解析错误时，返回错误提示但不中断流程
            String errorMessage = "\n\n[错误] 消息解析失败，但流程继续\n\n";
            chatHistoryStringBuilder.append(errorMessage);
            return errorMessage;
        }
    }
}

