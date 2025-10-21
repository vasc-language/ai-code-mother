package com.spring.aicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 用户选择的AI模型Key（如 qwen-turbo, gpt-5-codex）
     */
    private String modelKey;

    private static final long serialVersionUID = 1L;
}
