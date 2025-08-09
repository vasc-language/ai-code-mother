package com.spring.aicodemother.core;

import com.spring.aicodemother.ai.AiCodeGeneratorService;
import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;
import com.spring.aicodemother.core.parser.CodeParserExecutor;
import com.spring.aicodemother.core.saver.CodeFileSaverExecutor;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


//    /**
//     * 统一入口：根据类型生成并保存代码
//     *
//     * @param userMessage     用户提示词
//     * @param codeGenTypeEnum 生成类型
//     * @return 保存的目录
//     */
//    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
//        if (codeGenTypeEnum == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
//        }
//        return switch (codeGenTypeEnum) {
//            case HTML -> generateAndSaveHtmlCode(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
//            default -> {
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
//            }
//        };
//    }
//
//    /**
//     * 统一入口：根据类型生成并保存代码（流式输出接口）
//     *
//     * @param userMessage     用户提示词
//     * @param codeGenTypeEnum 生成类型
//     * @return 保存的目录
//     */
//    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
//        if (codeGenTypeEnum == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
//        }
//        return switch (codeGenTypeEnum) {
//            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
//            default -> {
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
//            }
//        };
//    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage); // htmlCode + description
        StringBuilder codeBuilder = new StringBuilder();
        return htmlCodeStream
                .doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        // 流式返回完成后保存代码
                        // 将 Code 提取出来，不需要 description
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(codeBuilder.toString());
                        // 将 htmlCodeResult 保存到文件中
                        File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                        log.info("保存成功，代码路径：{}", savedDir.getAbsolutePath());
                    }  catch (Exception e) {
                        log.error("保存失败：{}", e.getMessage());
                    }
                });

    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage); // MultiCode + description
        StringBuilder codeBuilder = new StringBuilder();
        return multiFileCodeStream
                .doOnNext(chunk -> {
                    // 实时接收吐出来的代码块
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        // 先解析代码块
                        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeBuilder.toString());
                        // 对文件进行保存
                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(result);
                        log.info("保存文件成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存文件失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType);
                log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

}

