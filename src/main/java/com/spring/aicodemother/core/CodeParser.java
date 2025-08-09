package com.spring.aicodemother.core;

import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * <p>
 * 这是一个工具类，提供一系列静态方法，用于从大段的字符串内容中（通常是AI模型的响应）
 * 解析和提取出被特定标记（如Markdown代码块）包裹的源代码。
 * </p>
 */
public class CodeParser {

    /**
     * 用于匹配并提取HTML代码块的正则表达式。
     * <p>
     * 它能识别以 "```html" 开头（不区分大小写），并以 "```" 结尾的代码块。
     * `\s*` 匹配 `html` 标识符后的任意空白字符。
     * `\n` 确保从新的一行开始捕获。
     * `([\s\S]*?)` 是一个非贪婪模式的捕获组，用于捕获两个标记之间的所有内容，包括换行符。
     * </p>
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 用于匹配并提取CSS代码块的正则表达式。
     * <p>
     * 结构与 {@link #HTML_CODE_PATTERN} 类似，但标识符为 "css"。
     * </p>
     */
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 用于匹配并提取JavaScript代码块的正则表达式。
     * <p>
     * 结构与 {@link #HTML_CODE_PATTERN} 类似，但标识符可以是 "js" 或 "javascript"。
     * `(?:js|javascript)` 是一个非捕获组，用于匹配两种常见的JavaScript标识符。
     * </p>
     */
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析主要用于生成单个HTML页面的代码内容。
     * <p>
     * 此方法首先会尝试使用 {@link #HTML_CODE_PATTERN} 来寻找被 ```html ... ``` 包裹的代码块。
     * 如果成功找到，则返回提取出的代码。
     * 如果没有找到任何代码块，该方法会采取一种宽容的策略：将整个输入字符串 {@code codeContent}
     * 去除首尾空白后，作为HTML代码返回。这种设计旨在兼容AI模型可能直接返回纯HTML代码而不带标记的情况。
     * </p>
     *
     * @param codeContent 包含代码的完整文本内容，可能包含Markdown代码块，也可能就是纯代码。
     * @return 一个 {@link HtmlCodeResult} 对象，其中包含了提取出的HTML代码。如果输入为空或仅包含空白，则代码字段也可能为空。
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 优先尝试提取被 ```html ... ``` 包裹的代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个输入内容视为HTML代码
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 解析包含多个代码块（HTML, CSS, JS）的文本内容。
     * <p>
     * 此方法会分别使用 {@link #HTML_CODE_PATTERN}, {@link #CSS_CODE_PATTERN}, 和 {@link #JS_CODE_PATTERN}
     * 来扫描输入字符串，并提取出所有匹配的代码块。
     * 提取出的代码将被填充到 {@link MultiFileCodeResult} 对象中对应的字段。
     * 如果某种语言的代码块在输入内容中不存在，其在返回对象中的对应字段将为 null。
     * </p>
     *
     * @param codeContent 包含一种或多种语言代码块的完整文本内容。
     * @return 一个 {@link MultiFileCodeResult} 对象，包含了所有成功提取的HTML、CSS和JS代码。
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 分别提取各类代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 私有辅助方法，专门用于从文本中提取第一个匹配的HTML代码块。
     *
     * @param content 待搜索的原始字符串。
     * @return 提取出的HTML代码字符串（捕获组1的内容），如果未找到匹配项则返回 null。
     */
    private static String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 通用的私有辅助方法，根据给定的正则表达式模式从文本中提取代码。
     *
     * @param content 待搜索的原始字符串。
     * @param pattern 用于匹配的 {@link Pattern} 对象。
     * @return 提取出的代码字符串（正则表达式的第一个捕获组的内容），如果未找到匹配项则返回 null。
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}


