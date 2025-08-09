package com.spring.aicodemother.core.parser;

/**
 * 代码解析器策略接口
 */
public interface CodeParser<T> {
    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析代码后的内容（结构对象）
     */
    T parserCode(String codeContent);
}
