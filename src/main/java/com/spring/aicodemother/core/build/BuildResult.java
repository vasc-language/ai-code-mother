package com.spring.aicodemother.core.build;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建结果
 */
@Data
public class BuildResult {
    
    /**
     * 是否构建成功
     */
    private boolean success;
    
    /**
     * 退出码
     */
    private int exitCode;
    
    /**
     * 标准输出内容
     */
    private List<String> stdoutLines;
    
    /**
     * 错误输出内容
     */
    private List<String> stderrLines;
    
    /**
     * 错误信息摘要（方便快速查看）
     */
    private String errorSummary;
    
    public BuildResult() {
        this.stdoutLines = new ArrayList<>();
        this.stderrLines = new ArrayList<>();
    }
    
    public BuildResult(boolean success) {
        this();
        this.success = success;
    }
    
    public BuildResult(boolean success, int exitCode, List<String> stdoutLines, List<String> stderrLines) {
        this.success = success;
        this.exitCode = exitCode;
        this.stdoutLines = stdoutLines;
        this.stderrLines = stderrLines;
        this.errorSummary = generateErrorSummary();
    }
    
    /**
     * 生成错误摘要
     */
    private String generateErrorSummary() {
        if (stderrLines == null || stderrLines.isEmpty()) {
            return "";
        }
        // 取前10行错误信息作为摘要
        int limit = Math.min(10, stderrLines.size());
        return String.join("\n", stderrLines.subList(0, limit));
    }
    
    /**
     * 获取完整的错误信息
     */
    public String getFullErrorMessage() {
        if (stderrLines == null || stderrLines.isEmpty()) {
            return "";
        }
        return String.join("\n", stderrLines);
    }
}
