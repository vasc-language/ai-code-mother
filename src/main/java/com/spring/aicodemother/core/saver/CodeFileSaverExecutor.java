package com.spring.aicodemother.core.saver;

import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件代码保存执行器
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeSaverTemplate multiFileCodeSaverTemplate = new MultiFileCodeSaverTemplate();

    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiFileCodeSaverTemplate.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
