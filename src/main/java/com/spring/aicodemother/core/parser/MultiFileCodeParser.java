package com.spring.aicodemother.core.parser;

import com.spring.aicodemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析（HTML + CSS + JS）
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public MultiFileCodeResult parserCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        String htmlCode = extractMultiFileCode(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractMultiFileCode(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractMultiFileCode(codeContent, JS_CODE_PATTERN);

        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 根据正则规则解析代码
     */
    private String extractMultiFileCode(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
