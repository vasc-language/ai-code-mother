package com.spring.aicodemother.core.handler;

import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.spring.aicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件流处理器
 * 专门处理 MULTI_FILE 类型的流式响应，支持HTML、CSS、JS多文件并行流式输出
 */
@Slf4j
public class MultiFileStreamHandler {

    // 文件类型检测正则模式
    private static final Pattern HTML_FILE_PATTERN = Pattern.compile(".*\\.html?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_FILE_PATTERN = Pattern.compile(".*\\.css", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_FILE_PATTERN = Pattern.compile(".*\\.js", Pattern.CASE_INSENSITIVE);
    
    // 代码块检测模式
    private static final Pattern CODE_BLOCK_START_PATTERN = Pattern.compile("```(\\w+)?");
    private static final Pattern CODE_BLOCK_END_PATTERN = Pattern.compile("```");

    /**
     * 处理 MULTI_FILE 流式响应
     * 为多文件提供专用的流式标记系统
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser,
                               java.util.function.BooleanSupplier cancelled,
                               java.util.function.Consumer<String> finalResponseConsumer) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        MultiFileContext context = new MultiFileContext();

        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    
                    // 处理多文件流式标记
                    return processMultiFileChunk(chunk, context);
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
     * 处理多文件流式数据块
     */
    private String processMultiFileChunk(String chunk, MultiFileContext context) {
        StringBuilder result = new StringBuilder();
        
        // 检测代码块开始和结束
        if (chunk.contains("```")) {
            if (!context.inCodeBlock) {
                // 代码块开始，尝试检测语言和文件类型
                context.inCodeBlock = true;
                String fileName = detectFileTypeFromCodeBlock(chunk, context);
                
                if (fileName != null) {
                    // 如果有当前文件，先结束它
                    if (context.currentFileName != null) {
                        result.append(String.format("[MULTI_FILE_END:%s]", context.currentFileName));
                    }
                    
                    // 开始新文件
                    context.currentFileName = fileName;
                    result.append(String.format("[MULTI_FILE_START:%s]", fileName));
                }
                
                result.append(chunk);
            } else {
                // 代码块结束
                context.inCodeBlock = false;
                result.append(chunk);
                if (context.currentFileName != null) {
                    result.append(String.format("[MULTI_FILE_END:%s]", context.currentFileName));
                    context.currentFileName = null;
                }
            }
        } else if (context.inCodeBlock && context.currentFileName != null) {
            // 在代码块中且有当前文件，添加内容标记
            result.append(String.format("[MULTI_FILE_CONTENT:%s]", context.currentFileName));
            // 确保换行符被正确保留
            result.append(chunk);
        } else {
            // 普通文本，检测是否提及了文件名
            String detectedFileName = detectFileName(chunk);
            if (detectedFileName != null && !detectedFileName.equals(context.currentFileName)) {
                // 检测到新文件名但不在代码块中，记录待用
                context.nextFileName = detectedFileName;
            }
            result.append(chunk);
        }
        
        return result.toString();
    }

    /**
     * 检测文件名
     */
    private String detectFileName(String chunk) {
        // 检测常见的文件名模式
        String[] lines = chunk.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            
            // 检测 HTML 文件
            if (HTML_FILE_PATTERN.matcher(trimmed).matches() || 
                trimmed.contains("index.html") || 
                trimmed.contains(".html")) {
                if (trimmed.contains("index.html")) return "index.html";
                if (trimmed.endsWith(".html")) return extractFileName(trimmed);
            }
            
            // 检测 CSS 文件
            if (CSS_FILE_PATTERN.matcher(trimmed).matches() || 
                trimmed.contains("style.css") || 
                trimmed.contains(".css")) {
                if (trimmed.contains("style.css")) return "style.css";
                if (trimmed.endsWith(".css")) return extractFileName(trimmed);
            }
            
            // 检测 JS 文件
            if (JS_FILE_PATTERN.matcher(trimmed).matches() || 
                trimmed.contains("script.js") || 
                trimmed.contains("main.js") ||
                trimmed.contains(".js")) {
                if (trimmed.contains("script.js")) return "script.js";
                if (trimmed.contains("main.js")) return "main.js";
                if (trimmed.endsWith(".js")) return extractFileName(trimmed);
            }
        }
        
        return null;
    }

    /**
     * 从文本中提取文件名
     */
    private String extractFileName(String text) {
        String[] parts = text.split("\\s+");
        for (String part : parts) {
            if (part.contains(".html") || part.contains(".css") || part.contains(".js")) {
                return part.replaceAll("[^\\w.-]", "");
            }
        }
        return null;
    }

    /**
     * 根据代码块检测文件类型
     */
    private String detectFileTypeFromCodeBlock(String chunk, MultiFileContext context) {
        String lowerChunk = chunk.toLowerCase();
        
        // 如果有待用的文件名，优先使用
        if (context.nextFileName != null) {
            String fileName = context.nextFileName;
            context.nextFileName = null;
            return fileName;
        }
        
        // 根据代码块语言标识推断文件类型
        if (lowerChunk.contains("```html") || lowerChunk.contains("html") || 
            (lowerChunk.contains("<!doctype") || lowerChunk.contains("<html") || lowerChunk.contains("<head"))) {
            return "index.html";
        } else if (lowerChunk.contains("```css") || lowerChunk.contains("css") ||
                   (lowerChunk.contains("body {") || lowerChunk.contains(".") || lowerChunk.contains("font-") || lowerChunk.contains("color:"))) {
            return "style.css";
        } else if (lowerChunk.contains("```javascript") || lowerChunk.contains("```js") || 
                   lowerChunk.contains("javascript") || lowerChunk.contains("js") ||
                   (lowerChunk.contains("function") || lowerChunk.contains("const ") || 
                    lowerChunk.contains("let ") || lowerChunk.contains("var ") || 
                    lowerChunk.contains("document.") || lowerChunk.contains("console."))) {
            return "script.js";
        }
        
        // 如果无法确定，根据上下文推断下一个可能的文件类型
        if (!context.hasHtml) {
            context.hasHtml = true;
            return "index.html";
        } else if (!context.hasCss) {
            context.hasCss = true;
            return "style.css";
        } else if (!context.hasJs) {
            context.hasJs = true;
            return "script.js";
        }
        
        return null;
    }

    /**
     * 多文件处理上下文
     */
    private static class MultiFileContext {
        String currentFileName = null;
        String nextFileName = null;
        boolean inCodeBlock = false;
        boolean hasHtml = false;
        boolean hasCss = false;
        boolean hasJs = false;
    }
}
