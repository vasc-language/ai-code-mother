package com.spring.aicodemother.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * 使用大模型基于用户的 prompt 生成简洁的项目名称
 */
public interface AppNameGeneratorService {

    /**
     * 根据用户输入生成精简的项目名称（仅返回纯文本名称）
     *
     * @param userPrompt 用户的需求描述
     * @return 精简后的项目名称
     */
    @SystemMessage(fromResource = "prompt/app-name-system-prompt.txt")
    String generateName(String userPrompt);
}

