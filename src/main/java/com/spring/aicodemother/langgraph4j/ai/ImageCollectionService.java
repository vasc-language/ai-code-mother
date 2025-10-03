package com.spring.aicodemother.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ImageCollectionService {

    /**
     * 根据用户提示词收集所需的图片资源
     * AI 会根据需求自主选择调用相应的工具
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
