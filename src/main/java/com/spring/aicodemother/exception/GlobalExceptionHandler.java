package com.spring.aicodemother.exception;

import cn.hutool.json.JSONUtil;

import com.spring.aicodemother.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理器
 * 
 * @author system
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> businessExceptionHandler(BusinessException e, HttpServletRequest request) {
        log.error("BusinessException", e);
        
        // 检查是否是SSE流式请求
        if (isSSERequest(request)) {
            // 对于SSE请求，返回纯文本错误消息
            String errorMessage = "data: " + JSONUtil.toJsonStr(Map.of("d", "[错误] " + e.getMessage())) + "\n\n";
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(errorMessage);
        }
        
        // 普通请求返回JSON响应
        return ResponseEntity.ok(ResultUtils.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeExceptionHandler(RuntimeException e, HttpServletRequest request) {
        log.error("RuntimeException", e);
        
        // 检查是否是SSE流式请求
        if (isSSERequest(request)) {
            // 特殊处理LangChain4j工具调用JSON解析错误
            String errorMessage;
            if (e.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
                errorMessage = "[错误] AI工具调用参数解析失败，请重试";
                log.error("LangChain4j工具调用JSON解析错误: {}", e.getCause().getMessage());
            } else {
                errorMessage = "[错误] 系统错误，请稍后重试";
            }
            
            // 对于SSE请求，返回符合SSE格式的错误消息
            String sseErrorMessage = "data: " + JSONUtil.toJsonStr(Map.of("d", errorMessage)) + "\n\n"
                    + "event: done\n"
                    + "data: \n\n";
            
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(sseErrorMessage);
        }
        
        // 普通请求返回JSON响应
        return ResponseEntity.ok(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"));
    }
    
    /**
     * 检查是否是SSE流式请求
     */
    private boolean isSSERequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        // 检查Accept头
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            return true;
        }
        
        // 检查请求路径
        String requestURI = request.getRequestURI();
        return requestURI != null && requestURI.contains("/chat/gen/code");
    }
}
