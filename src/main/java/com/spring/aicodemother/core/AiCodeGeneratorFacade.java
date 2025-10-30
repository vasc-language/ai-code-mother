package com.spring.aicodemother.core;

import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.ai.AiCodeGeneratorService;
import com.spring.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.spring.aicodemother.ai.model.HtmlCodeResult;
import com.spring.aicodemother.ai.model.MultiFileCodeResult;
import com.spring.aicodemother.ai.model.message.AiResponseMessage;
import com.spring.aicodemother.ai.model.message.ToolExecutedMessage;
import com.spring.aicodemother.ai.model.message.ToolRequestMessage;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.core.build.VueProjectBuilder;
import com.spring.aicodemother.core.control.GenerationControlRegistry;
import com.spring.aicodemother.core.control.GenerationControlRegistry.GenerationControl;
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

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private GenerationControlRegistry generationControlRegistry;

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
     * @param appId           应用ID
     * @param control         生成控制
     * @param modelKey        模型key（支持动态模型选择）
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId,
                                                  GenerationControl control, String modelKey) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        // 根据 appId、codeGenType 和 modelKey 获取相对应的 AI Service
        AiCodeGeneratorService aiCodeGeneratorService;
        if (modelKey != null && !modelKey.isBlank()) {
            log.info("使用动态模型创建AI服务 - appId: {}, codeGenType: {}, modelKey: {}", appId, codeGenTypeEnum, modelKey);
            aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum, modelKey);
        } else {
            log.info("使用默认模型创建AI服务 - appId: {}, codeGenType: {}", appId, codeGenTypeEnum);
            aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        }

        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, control);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, control);
            }
            case VUE_PROJECT -> {
                TokenStream codeTokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 如果是我们内置的实现，注入取消感知，便于在停止后阻断工具执行
                try {
                    if (codeTokenStream instanceof dev.langchain4j.service.AiServiceTokenStream stream) {
                        stream.withCancellation(() -> control != null && control.isCancelled());
                    }
                } catch (Throwable ignored) {}
                yield processTokenStream(codeTokenStream, appId, control);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式，兼容旧版）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId,
                                                  GenerationControl control) {
        return generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId, control, null);
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId, GenerationControl control) {
        return Flux.create(sink -> {
            // 本轮运行的轻量级重复写入守卫：用于检测明显的循环趋势
            final java.util.Set<String> writtenPaths = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
            final java.util.concurrent.atomic.AtomicInteger duplicateWriteCount = new java.util.concurrent.atomic.AtomicInteger(0);
            // 项目完成状态检查
            final java.util.concurrent.atomic.AtomicBoolean projectCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);
            final java.util.concurrent.atomic.AtomicInteger confirmationAttempts = new java.util.concurrent.atomic.AtomicInteger(0);
            try {
                java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);
                // 当下游取消或释放时，触发控制器取消，以便上层尽早停止
                if (control != null) {
                    sink.onCancel(control::cancel);
                    sink.onDispose(control::cancel);
                }
                tokenStream.onPartialResponse((String partialResponse) -> {
                            try {
                                if (control != null && control.isCancelled()) {
                                    if (completed.compareAndSet(false, true)) {
                                        sink.complete();
                                    }
                                    return;
                                }
                                
                                // 检查项目是否已完成
                                if (!projectCompleted.get() && isProjectCompleted(appId)) {
                                    projectCompleted.set(true);
                                    log.info("[项目完成检测] Vue项目已生成完成，后续将过滤确认性内容");
                                }
                                
                                // 如果项目已完成，检查并过滤确认性内容
                                if (projectCompleted.get() && containsConfirmationContent(partialResponse)) {
                                    int attempts = confirmationAttempts.incrementAndGet();
                                    log.warn("[UX优化] 项目已完成，过滤AI的确认性输出 (第{}次), 内容: {}", attempts, 
                                            partialResponse.length() > 100 ? partialResponse.substring(0, 100) + "..." : partialResponse);
                                    
                                    // 如果是第一次检测到确认性内容，发送一个替代消息
                                    if (attempts == 1) {
                                        AiResponseMessage completionMessage = new AiResponseMessage("\n\n✅ 项目生成已完成！您可以预览和使用生成的应用了。\n\n");
                                        sink.next(JSONUtil.toJsonStr(completionMessage));
                                    }
                                    return; // 跳过原始的确认性内容
                                }
                                
                                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                            } catch (Exception e) {
                                log.error("处理AI响应消息失败: {}", e.getMessage());
                                // 继续处理，不中断流
                            }
                        })
                        .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                            try {
                                if (control != null && control.isCancelled()) {
                                    return;
                                }
                                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                            } catch (Exception e) {
                                log.error("处理工具请求消息失败: {}", e.getMessage());
                                // 继续处理，不中断流
                            }
                        })
                        .onToolExecuted((ToolExecution toolExecution) -> {
                            try {
                                if (control != null && control.isCancelled()) {
                                    return;
                                }
                                // 可选守卫：检测同一路径的重复写入趋势，给出提示以避免循环
                                try {
                                    String toolName = toolExecution.request().name();
                                    if ("writeFile".equals(toolName)) {
                                        String args = toolExecution.request().arguments();
                                        String path = null;
                                        try {
                                            cn.hutool.json.JSONObject obj = cn.hutool.json.JSONUtil.parseObj(args);
                                            path = obj.getStr("relativeFilePath");
                                        } catch (Throwable ignored) {}
                                        if (path != null) {
                                            boolean firstTime = writtenPaths.add(path);
                                            if (!firstTime) {
                                                int dup = duplicateWriteCount.incrementAndGet();
                                                if (dup == 3) { // 第3次触发时提示一次
                                                    AiResponseMessage warn = new AiResponseMessage("\n\n[警告] 检测到重复写入同一路径的趋势(≥3)。系统已对相同内容的重复写入进行跳过处理。建议立即调用【退出工具调用】以结束生成，避免循环。\n\n");
                                                    sink.next(cn.hutool.json.JSONUtil.toJsonStr(warn));
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable guardEx) {
                                    // 守卫失败不影响主流程
                                }
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
                            // 同步构造 Vue 项目（同步执行，确保预览时项目已就绪）
                            String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                            if (control == null || !control.isCancelled()) {
                                // 先检查项目是否有实际内容，避免AI只输出计划未生成代码就触发构建
                                if (isValidProjectDirectory(projectPath)) {
                                    com.spring.aicodemother.core.build.BuildResult buildResult = vueProjectBuilder.buildProject(projectPath);
                                    if (!buildResult.isSuccess()) {
                                        log.error("Vue 项目构建失败: {}", buildResult.getErrorSummary());
                                    }
                                } else {
                                    log.warn("项目目录不存在或内容不足，跳过构建: {}", projectPath);
                                    // 不发送警告消息给用户
                                    // 如果AI在等待确认，这是正常流程；如果真的失败了，校验会拦截
                                }
                            }
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
        .takeUntilOther(control == null ? reactor.core.publisher.Mono.never() : control.cancelFlux())
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
     * 检查项目目录是否有效（存在且包含必要文件）
     *
     * @param projectPath 项目路径
     * @return 是否有效
     */
    private boolean isValidProjectDirectory(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            return false;
        }
        
        // 检查是否至少有package.json文件
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            return false;
        }
        
        // 检查目录是否有足够的文件（至少3个文件，避免只有一个空文件的情况）
        File[] files = projectDir.listFiles();
        int fileCount = 0;
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !".DS_Store".equals(file.getName())) {
                    fileCount++;
                }
            }
        }
        
        return fileCount >= 3; // 至少需要package.json和其他2个文件
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId,
                                           GenerationControl control) {
        // 使用cache()确保流可以被多次订阅
        Flux<String> controlled = (control == null) ? codeStream : codeStream.takeUntilOther(control.cancelFlux());
        Flux<String> cachedStream = controlled.cache();

        // 异步处理保存操作，不阻塞原流
        cachedStream.collect(StringBuilder::new, StringBuilder::append)
                .doOnSuccess(codeBuilder -> {
                    try {
                        if (control != null && control.isCancelled()) {
                            return; // 用户取消，跳过保存
                        }
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

    /**
     * 检查Vue项目是否已完成（存在核心文件且构建成功）
     * 
     * @param appId 应用ID
     * @return 是否完成
     */
    private boolean isProjectCompleted(Long appId) {
        try {
            String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
            File projectDir = new File(projectPath);
            
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                return false;
            }
            
            // 检查核心文件是否存在
            String[] coreFiles = {"package.json", "src/main.js", "src/App.vue", "vite.config.js"};
            for (String coreFile : coreFiles) {
                File file = new File(projectDir, coreFile);
                if (!file.exists()) {
                    return false;
                }
            }
            
            // 检查是否已构建（dist目录存在）
            File distDir = new File(projectDir, "dist");
            if (!distDir.exists() || !distDir.isDirectory()) {
                return false;
            }
            
            // 检查dist目录是否有内容
            File[] distFiles = distDir.listFiles();
            return distFiles != null && distFiles.length > 0;
            
        } catch (Exception e) {
            log.error("检查项目完成状态失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查内容是否包含确认性询问
     * 
     * @param content 内容
     * @return 是否包含确认性内容
     */
    private boolean containsConfirmationContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        // 确认性短语列表（与 GenerationValidationServiceImpl 保持一致）
        String[] confirmationPhrases = {
            "您觉得这个计划",
            "你觉得这个计划",
            "这个方案",
            "是否满意",
            "是否可以",
            "请确认",
            "请问是否",
            "可以开始吗",
            "是否开始",
            "合适吗",
            "同意吗",
            "没问题吗",
            "可以开始生成代码吗",
            "开始生成代码吗",
            "✨小计划如下",
            "小计划如下"
        };
        
        for (String phrase : confirmationPhrases) {
            if (lowerContent.contains(phrase.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}

