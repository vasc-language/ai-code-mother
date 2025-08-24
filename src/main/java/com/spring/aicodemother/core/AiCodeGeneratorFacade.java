package com.spring.aicodemother.core;

import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.ai.AiCodeGeneratorService;
import com.spring.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;
import com.spring.aicodemother.ai.model.message.AiResponseMessage;
import com.spring.aicodemother.ai.model.message.ToolExecutedMessage;
import com.spring.aicodemother.ai.model.message.ToolRequestMessage;
import com.spring.aicodemother.core.parser.CodeParserExecutor;
import com.spring.aicodemother.core.saver.CodeFileSaverExecutor;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

@Service
@Slf4j
public class AiCodeGeneratorFacade {

//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService; // 当前所有对话都使用一个 AI Service

//    @Autowired
//    private GenericResponseService responseBuilder;
//    @Autowired
//    private View error;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码（非流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取相对应的 AI Service
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
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
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取相对应的 AI Service
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream codeTokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(codeTokenStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            try {
                tokenStream.onPartialResponse((String partialResponse) -> {
                            try {
                                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                            } catch (Exception e) {
                                log.error("处理AI响应消息失败: {}", e.getMessage());
                                // 继续处理，不中断流
                            }
                        })
                        .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                            try {
                                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                            } catch (Exception e) {
                                log.error("处理工具请求消息失败: {}", e.getMessage());
                                // 继续处理，不中断流
                            }
                        })
                        .onToolExecuted((ToolExecution toolExecution) -> {
                            try {
                                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                                sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                            } catch (Exception e) {
                                log.error("处理工具执行消息失败: {}", e.getMessage());
                                // 发送错误消息给前端
                                try {
                                    AiResponseMessage errorMessage = new AiResponseMessage("\n\n[错误] 工具调用出现问题，但代码生成继续进行\n\n");
                                    sink.next(JSONUtil.toJsonStr(errorMessage));
                                } catch (Exception nested) {
                                    log.error("发送错误消息失败: {}", nested.getMessage());
                                }
                            }
                        })
                        .onCompleteResponse((ChatResponse response) -> {
                            log.info("AI响应完成");
                            sink.complete();
                        })
                        .onError((Throwable error) -> {
                            log.error("TokenStream处理出现错误: {}", error.getMessage(), error);
                            
                            // 特殊处理JSON解析错误
                            if (error.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
                                log.error("检测到LangChain4j工具调用JSON解析错误，尝试恢复");
                                try {
                                    AiResponseMessage errorMessage = new AiResponseMessage("\n\n[错误] 工具调用参数解析失败，请重新尝试或简化您的请求\n\n");
                                    sink.next(JSONUtil.toJsonStr(errorMessage));
                                    sink.complete(); // 优雅结束，而不是错误终止
                                    return;
                                } catch (Exception e) {
                                    log.error("发送错误恢复消息失败: {}", e.getMessage());
                                }
                            }
                            
                            // 其他错误情况
                            sink.error(error);
                        })
                        .start();
            } catch (Exception e) {
                log.error("启动TokenStream失败: {}", e.getMessage(), e);
                sink.error(e);
            }
        })
        // 添加错误恢复操作符，确保流不会因为单次错误而完全中断
        .onErrorResume(throwable -> {
            log.error("Flux流处理错误，尝试恢复: {}", throwable.getMessage());
            AiResponseMessage errorMessage = new AiResponseMessage("\n\n[错误] 处理过程中遇到问题，请重试\n\n");
            return Flux.just(JSONUtil.toJsonStr(errorMessage));
        })
        // 确保返回类型为 Flux<String>
        .cast(String.class);
    }



    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 使用cache()确保流可以被多次订阅
        Flux<String> cachedStream = codeStream.cache();

        // 异步处理保存操作，不阻塞原流
        cachedStream.collect(StringBuilder::new, StringBuilder::append)
                .doOnSuccess(codeBuilder -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        // 使用执行器解析代码
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        // 使用执行器保存代码
                        File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                        log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                        // 注意：这里的异常不会传播到主流，如果需要可以考虑其他处理方式
                    }
                })
                .doOnError(error -> log.error("流处理失败: {}", error.getMessage()))
                .subscribe(); // 启动异步处理

        // 返回缓存的流，保持流式特性
        return cachedStream;
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
//
//    /**
//     * 生成 HTML 模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private File generateAndSaveHtmlCode(String userMessage) {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(result);
//    }
//
//    /**
//     * 生成多文件模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private File generateAndSaveMultiFileCode(String userMessage) {
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//        return CodeFileSaver.saveMultiFileCodeResult(result);
//    }
//
//    /**
//     * 生成 HTML 模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//        Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage); // htmlCode + description
//        StringBuilder codeBuilder = new StringBuilder();
//        return htmlCodeStream
//                .doOnNext(chunk -> {
//                    // 实时收集代码片段
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    try {
//                        // 流式返回完成后保存代码
//                        // 将 Code 提取出来，不需要 description
//                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(codeBuilder.toString());
//                        // 将 htmlCodeResult 保存到文件中
//                        File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                        log.info("保存成功，代码路径：{}", savedDir.getAbsolutePath());
//                    }  catch (Exception e) {
//                        log.error("保存失败：{}", e.getMessage());
//                    }
//                });
//
//    }
//
//    /**
//     * 生成多文件模式的代码并保存
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage); // MultiCode + description
//        StringBuilder codeBuilder = new StringBuilder();
//        return multiFileCodeStream
//                .doOnNext(chunk -> {
//                    // 实时接收吐出来的代码块
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    try {
//                        // 先解析代码块
//                        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeBuilder.toString());
//                        // 对文件进行保存
//                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(result);
//                        log.info("保存文件成功，路径为：{}", savedDir.getAbsolutePath());
//                    } catch (Exception e) {
//                        log.error("保存文件失败：{}", e.getMessage());
//                    }
//                });
//    }
}

