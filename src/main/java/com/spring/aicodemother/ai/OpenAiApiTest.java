package com.spring.aicodemother.ai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API 格式测试类
 * 测试兼容 OpenAI 格式的第三方 API 端点
 *
 * @author AI Code Mother
 * @version 1.0
 */
@Slf4j
public class OpenAiApiTest {

    // 配置常量
    private static final String BASE_URL = "https://204992.xyz/v1";
    private static final String API_KEY = "sk-6d9175726392996b969a9ded28fe2a47dce1884c2023f28fc3cb666d22db57d8";
    private static final String MODEL_NAME = "gpt-5-codex";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    /**
     * AI 助手接口
     */
    interface Assistant {
        String chat(String userMessage);
    }

    /**
     * 创建 AI 助手实例
     */
    private static Assistant createAssistant(double temperature, int maxTokens, int timeoutSeconds) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName(MODEL_NAME)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .logRequests(true)
                .logResponses(true)
                .maxRetries(0) // 禁用内部重试，使用我们自己的重试机制
                .build();

        return AiServices.create(Assistant.class, chatModel);
    }

    /**
     * 带重试机制的 API 调用
     */
    private static String generateWithRetry(Assistant assistant, String prompt, String testName) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                log.info("【{}】第 {} 次尝试...", testName, attempt);

                long startTime = System.currentTimeMillis();
                String response = assistant.chat(prompt);
                long duration = System.currentTimeMillis() - startTime;

                log.info("【{}】调用成功！耗时: {} ms", testName, duration);
                return response;

            } catch (Exception e) {
                lastException = e;
                log.warn("【{}】第 {} 次尝试失败: {}", testName, attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        log.info("等待 {} ms 后重试...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试等待被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("【" + testName + "】经过 " + MAX_RETRIES + " 次重试后仍然失败", lastException);
    }

    /**
     * 测试1: 基本连接测试
     */
    public static TestResult testBasicConnection() {
        String testName = "基本连接测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.7, 500, 30);
            String prompt = "请简单介绍一下你自己，控制在50字以内";
            log.info("发送提示: {}", prompt);

            String response = generateWithRetry(assistant, prompt, testName);

            log.info("响应内容: {}", response);
            return new TestResult(testName, true, response, null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 测试2: 代码生成测试
     */
    public static TestResult testCodeGeneration() {
        String testName = "代码生成测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.3, 2000, 60);
            String prompt = "用Java编写一个计算斐波那契数列的递归方法，要求包含注释";
            log.info("发送提示: {}", prompt);

            String response = generateWithRetry(assistant, prompt, testName);

            log.info("生成的代码:\n{}", response);
            return new TestResult(testName, true, response, null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 测试3: 结构化输出测试
     */
    public static TestResult testStructuredOutput() {
        String testName = "结构化输出测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.5, 1000, 45);
            String prompt = "请以JSON格式返回一个用户对象，包含以下字段：name(姓名), age(年龄), email(邮箱), hobbies(爱好数组)";
            log.info("发送提示: {}", prompt);

            String response = generateWithRetry(assistant, prompt, testName);

            log.info("结构化输出:\n{}", response);
            return new TestResult(testName, true, response, null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 测试4: 长文本处理测试
     */
    public static TestResult testLongTextProcessing() {
        String testName = "长文本处理测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.7, 3000, 90);
            String prompt = "请详细解释Spring Boot的自动配置原理，包括@SpringBootApplication注解、" +
                    "@EnableAutoConfiguration的作用、以及自动配置的加载流程";
            log.info("发送提示: {}", prompt);

            String response = generateWithRetry(assistant, prompt, testName);

            log.info("响应长度: {} 字符", response.length());
            log.info("响应内容:\n{}", response);
            return new TestResult(testName, true, response, null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 测试5: 多轮对话模拟测试
     */
    public static TestResult testMultiTurnConversation() {
        String testName = "多轮对话模拟测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.8, 1500, 60);

            String[] prompts = {
                "什么是RESTful API？",
                "它有哪些主要特点？",
                "请举一个实际的例子"
            };

            StringBuilder allResponses = new StringBuilder();

            for (int i = 0; i < prompts.length; i++) {
                log.info("【轮次 {}】发送提示: {}", i + 1, prompts[i]);
                String response = generateWithRetry(assistant, prompts[i], testName + "-轮次" + (i + 1));
                log.info("【轮次 {}】响应: {}", i + 1, response);
                allResponses.append("轮次").append(i + 1).append(": ").append(response).append("\n\n");
            }

            return new TestResult(testName, true, allResponses.toString(), null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 测试6: 性能压力测试
     */
    public static TestResult testPerformance() {
        String testName = "性能压力测试";
        log.info("\n========== {} ==========", testName);

        try {
            Assistant assistant = createAssistant(0.7, 500, 30);
            int testCount = 5;
            List<Long> durations = new ArrayList<>();

            for (int i = 0; i < testCount; i++) {
                String prompt = "请用一句话回答：" + (i + 1) + " + " + (i + 1) + " = ?";
                log.info("【请求 {}】发送提示: {}", i + 1, prompt);

                long startTime = System.currentTimeMillis();
                String response = generateWithRetry(assistant, prompt, testName + "-请求" + (i + 1));
                long duration = System.currentTimeMillis() - startTime;

                durations.add(duration);
                log.info("【请求 {}】响应: {}, 耗时: {} ms", i + 1, response, duration);
            }

            // 计算平均响应时间
            double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0);
            String result = String.format("完成 %d 次请求，平均响应时间: %.2f ms", testCount, avgDuration);
            log.info(result);

            return new TestResult(testName, true, result, null);

        } catch (Exception e) {
            log.error("【{}】失败", testName, e);
            return new TestResult(testName, false, null, e.getMessage());
        }
    }

    /**
     * 主方法 - 运行所有测试
     */
    public static void main(String[] args) {
        log.info("╔════════════════════════════════════════════════════╗");
        log.info("║       OpenAI API 兼容性测试套件                    ║");
        log.info("║       Base URL: {}             ║", BASE_URL);
        log.info("║       Model: {}           ║", MODEL_NAME);
        log.info("╚════════════════════════════════════════════════════╝");

        List<TestResult> results = new ArrayList<>();
        long totalStartTime = System.currentTimeMillis();

        try {
            // 执行所有测试
            results.add(testBasicConnection());
            results.add(testCodeGeneration());
            results.add(testStructuredOutput());
            results.add(testLongTextProcessing());
            results.add(testMultiTurnConversation());
            results.add(testPerformance());

        } catch (Exception e) {
            log.error("测试套件执行异常", e);
        } finally {
            // 生成测试报告
            long totalDuration = System.currentTimeMillis() - totalStartTime;
            printTestReport(results, totalDuration);
        }
    }

    /**
     * 打印测试报告
     */
    private static void printTestReport(List<TestResult> results, long totalDuration) {
        log.info("\n\n╔════════════════════════════════════════════════════╗");
        log.info("║                  测试报告                          ║");
        log.info("╚════════════════════════════════════════════════════╝");

        int passed = 0;
        int failed = 0;

        for (TestResult result : results) {
            if (result.isSuccess()) {
                passed++;
                log.info("✓ {} - 通过", result.getTestName());
            } else {
                failed++;
                log.error("✗ {} - 失败: {}", result.getTestName(), result.getErrorMessage());
            }
        }

        log.info("\n总计: {} 个测试", results.size());
        log.info("通过: {} 个", passed);
        log.info("失败: {} 个", failed);
        log.info("总耗时: {} ms ({} 秒)", totalDuration, totalDuration / 1000.0);
        log.info("成功率: {}", String.format("%.2f%%", (passed * 100.0) / results.size()));

        log.info("\n╔════════════════════════════════════════════════════╗");
        if (failed == 0) {
            log.info("║            所有测试通过！🎉                        ║");
        } else {
            log.error("║            部分测试失败，请检查日志                ║");
        }
        log.info("╚════════════════════════════════════════════════════╝");

        // 如果有失败的测试，退出码设为1
        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * 测试结果类
     */
    private static class TestResult {
        private final String testName;
        private final boolean success;
        private final String response;
        private final String errorMessage;

        public TestResult(String testName, boolean success, String response, String errorMessage) {
            this.testName = testName;
            this.success = success;
            this.response = response;
            this.errorMessage = errorMessage;
        }

        public String getTestName() {
            return testName;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResponse() {
            return response;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
