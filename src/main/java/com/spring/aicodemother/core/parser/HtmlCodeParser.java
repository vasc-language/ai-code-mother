package com.spring.aicodemother.core.parser;

import com.spring.aicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 单文件代码解析器
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parserCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode);
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent);
        }
        return null;
    }

    /**
     * 提取代码的内容
     *
     * @param content htmlCode + description
     * @return htmlCode
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
