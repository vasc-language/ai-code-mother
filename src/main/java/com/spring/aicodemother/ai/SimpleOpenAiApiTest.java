package com.spring.aicodemother.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI API 简单测试类
 * 使用 Hutool HTTP 直接调用，避免 LangChain4j 解析问题
 *
 * @author AI Code Mother
 * @version 1.0
 */
@Slf4j
public class SimpleOpenAiApiTest {

    private static final String BASE_URL = "https://204992.xyz/v1";
    private static final String API_KEY = "sk-6d9175726392996b969a9ded28fe2a47dce1884c2023f28fc3cb666d22db57d8";
    private static final String MODEL_NAME = "gpt-5-codex-medium";

    /**
     * 调用 OpenAI Chat API
     */
    private static String chat(String userMessage, int maxTokens) {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL_NAME);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);
        messages.add(message);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);

        String requestJson = JSONUtil.toJsonStr(requestBody);

        log.info("发送请求:");
        log.info("URL: {}/chat/completions", BASE_URL);
        log.info("Body: {}", requestJson);

        // 发送 HTTP POST 请求
        long startTime = System.currentTimeMillis();
        String responseBody = HttpRequest.post(BASE_URL + "/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body(requestJson)
                .timeout(30000)
                .execute()
                .body();
        long duration = System.currentTimeMillis() - startTime;

        log.info("收到响应 (耗时: {} ms):", duration);
        log.info("Response: {}", responseBody);

        // 解析响应
        JSONObject response = JSONUtil.parseObj(responseBody);
        String content = response.getByPath("choices[0].message.content", String.class);

        return content;
    }

    /**
     * 测试1: 基本连接测试
     */
    public static void testBasicConnection() {
        log.info("\n========== 基本连接测试 ==========");
        try {
            String response = chat("你好，请用一句话介绍你自己", 100);
            log.info("✓ 测试通过");
            log.info("AI 回复: {}", response);
        } catch (Exception e) {
            log.error("✗ 测试失败", e);
        }
    }

    /**
     * 测试2: 代码生成测试
     */
    public static void testCodeGeneration() {
        log.info("\n========== 代码生成测试 ==========");
        try {
            String response = chat("用Java写一个计算阶乘的方法", 500);
            log.info("✓ 测试通过");
            log.info("生成的代码:\n{}", response);
        } catch (Exception e) {
            log.error("✗ 测试失败", e);
        }
    }

    /**
     * 测试3: JSON 结构化输出
     */
    public static void testJsonOutput() {
        log.info("\n========== JSON 结构化输出测试 ==========");
        try {
            String response = chat("以JSON格式返回一个用户对象，包含name、age、email字段", 200);
            log.info("✓ 测试通过");
            log.info("JSON 输出:\n{}", response);
        } catch (Exception e) {
            log.error("✗ 测试失败", e);
        }
    }

    /**
     * 测试4: 多模型测试
     */
    public static void testMultipleModels() {
        log.info("\n========== 多模型测试 ==========");

        String[] models = {
            "gpt-5-codex-medium",
            "gpt-5-codex",
            "deepseek-v3",
            "qwen3-max"
        };

        for (String model : models) {
            log.info("\n--- 测试模型: {} ---", model);
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);

                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", "Say hello in one sentence");
                messages.add(message);

                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 50);

                String responseBody = HttpRequest.post(BASE_URL + "/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .body(JSONUtil.toJsonStr(requestBody))
                        .timeout(30000)
                        .execute()
                        .body();

                JSONObject response = JSONUtil.parseObj(responseBody);
                String content = response.getByPath("choices[0].message.content", String.class);

                log.info("✓ 模型 {} 响应: {}", model, content);

            } catch (Exception e) {
                log.error("✗ 模型 {} 测试失败: {}", model, e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        log.info("╔════════════════════════════════════════════════════╗");
        log.info("║       OpenAI API 简单测试套件                      ║");
        log.info("║       Base URL: {}             ║", BASE_URL);
        log.info("║       Model: {}           ║", MODEL_NAME);
        log.info("╚════════════════════════════════════════════════════╝");

        try {
            testBasicConnection();
            Thread.sleep(1000);

            testCodeGeneration();
            Thread.sleep(1000);

            testJsonOutput();
            Thread.sleep(1000);

            testMultipleModels();

            log.info("\n╔════════════════════════════════════════════════════╗");
            log.info("║            测试完成！                              ║");
            log.info("╚════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("测试过程中发生错误", e);
        }
    }
}
