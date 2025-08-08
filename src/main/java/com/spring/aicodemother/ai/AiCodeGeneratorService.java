package com.spring.aicodemother.ai;

import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

/**
 * LangChain4j 封装好的AI与Java程序交互的组件
 */
public interface AiCodeGeneratorService {
    /**
     * 生成 HTML 单文件
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
