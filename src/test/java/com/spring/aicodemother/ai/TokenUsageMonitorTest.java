package com.spring.aicodemother.ai;

import com.spring.aicodemother.core.AiCodeGeneratorFacade;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.monitor.AiModelMonitorListener;
import com.spring.aicodemother.monitor.MonitorContext;
import com.spring.aicodemother.monitor.MonitorContextHolder;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token 消耗监听测试类
 * 用于测试不同复杂度项目的 Token 消耗情况
 *
 * @author Join2049
 */
@SpringBootTest
@Slf4j
class TokenUsageMonitorTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 测试简单 HTML 页面的 Token 消耗
     */
    @Test
    void testSimpleHtmlTokenUsage() {
        log.info("========== 开始测试：简单 HTML 页面 Token 消耗 ==========");

        String userMessage = "制作一个简单的个人名片页面，包含姓名、邮箱、技能标签，使用渐变背景";

        TokenUsageStats stats = monitorTokenUsage(() -> {
            AiCodeGeneratorService service = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(9999L, CodeGenTypeEnum.HTML);
            Flux<String> codeStream = service.generateHtmlCodeStream(userMessage);
            return codeStream.collectList().block();
        }, "简单HTML页面");

        printTokenStats(stats, "简单 HTML 页面");
        log.info("========== 测试完成：简单 HTML 页面 ==========\n");
    }

    /**
     * 测试中等复杂度的多文件项目 Token 消耗
     */
    @Test
    void testMediumMultiFileTokenUsage() {
        log.info("========== 开始测试：中等复杂度多文件项目 Token 消耗 ==========");

        String userMessage = "制作一个待办事项管理应用，包含任务列表、添加任务、标记完成、删除任务功能，使用 HTML + CSS + JavaScript";

        TokenUsageStats stats = monitorTokenUsage(() -> {
            AiCodeGeneratorService service = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(9998L, CodeGenTypeEnum.MULTI_FILE);
            Flux<String> codeStream = service.generateMultiFileCodeStream(userMessage);
            return codeStream.collectList().block();
        }, "中等多文件项目");

        printTokenStats(stats, "中等复杂度多文件项目");
        log.info("========== 测试完成：中等多文件项目 ==========\n");
    }

    /**
     * 测试简单 Vue 项目的 Token 消耗
     */
    @Test
    void testSimpleVueProjectTokenUsage() {
        log.info("========== 开始测试：简单 Vue 项目 Token 消耗 ==========");

        String userMessage = "开发一个简单的个人博客首页，包含文章列表、文章详情，使用 Vue 3 + TypeScript，总代码量不超过 300 行";

        TokenUsageStats stats = monitorTokenUsage(() -> {
            // 设置监控上下文
            MonitorContext context = new MonitorContext();
            context.setUserId("test-user-001");
            context.setAppId("9997");
            MonitorContextHolder.setContext(context);

            try {
                Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        userMessage,
                        CodeGenTypeEnum.VUE_PROJECT,
                        9997L,
                        null  // GenerationControl 参数，测试时传 null
                );

                List<String> result = codeStream.collectList().block();

                // 从上下文中获取 Token 统计
                Long totalTokens = context.getTotalTokens();
                if (totalTokens != null) {
                    log.info("从监控上下文获取到 Total Tokens: {}", totalTokens);
                }

                return result;
            } finally {
                MonitorContextHolder.clearContext();
            }
        }, "简单Vue项目");

        printTokenStats(stats, "简单 Vue 项目（300行）");
        log.info("========== 测试完成：简单 Vue 项目 ==========\n");
    }

    /**
     * 测试中等复杂度 Vue 项目的 Token 消耗
     */
    @Test
    void testMediumVueProjectTokenUsage() {
        log.info("========== 开始测试：中等复杂度 Vue 项目 Token 消耗 ==========");

        String userMessage = "开发一个任务管理系统，包含用户登录、任务列表、任务创建/编辑/删除、状态筛选、数据持久化（localStorage），使用 Vue 3 + TypeScript + Pinia，总代码量约 500-800 行";

        TokenUsageStats stats = monitorTokenUsage(() -> {
            MonitorContext context = new MonitorContext();
            context.setUserId("test-user-002");
            context.setAppId("9996");
            MonitorContextHolder.setContext(context);

            try {
                Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        userMessage,
                        CodeGenTypeEnum.VUE_PROJECT,
                        9996L,
                        null  // GenerationControl 参数，测试时传 null
                );

                List<String> result = codeStream.collectList().block();

                Long totalTokens = context.getTotalTokens();
                if (totalTokens != null) {
                    log.info("从监控上下文获取到 Total Tokens: {}", totalTokens);
                }

                return result;
            } finally {
                MonitorContextHolder.clearContext();
            }
        }, "中等Vue项目");

        printTokenStats(stats, "中等复杂度 Vue 项目（500-800行）");
        log.info("========== 测试完成：中等 Vue 项目 ==========\n");
    }

    /**
     * 测试复杂 Vue 项目的 Token 消耗
     */
    @Test
    void testComplexVueProjectTokenUsage() {
        log.info("========== 开始测试：复杂 Vue 项目 Token 消耗 ==========");

        String userMessage = "开发一个完整的电商管理后台系统，包含：\n" +
                "1. 用户认证（登录/注册/退出）\n" +
                "2. 商品管理（列表/新增/编辑/删除/分类）\n" +
                "3. 订单管理（列表/详情/状态管理）\n" +
                "4. 数据统计图表（销售趋势、商品排行）\n" +
                "5. 使用 Vue 3 + TypeScript + Pinia + Vue Router + Ant Design Vue\n" +
                "6. 总代码量约 1500-2000 行";

        TokenUsageStats stats = monitorTokenUsage(() -> {
            MonitorContext context = new MonitorContext();
            context.setUserId("test-user-003");
            context.setAppId("9995");
            MonitorContextHolder.setContext(context);

            try {
                Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        userMessage,
                        CodeGenTypeEnum.VUE_PROJECT,
                        9995L,
                        null  // GenerationControl 参数，测试时传 null
                );

                List<String> result = codeStream.collectList().block();

                Long totalTokens = context.getTotalTokens();
                if (totalTokens != null) {
                    log.info("从监控上下文获取到 Total Tokens: {}", totalTokens);
                }

                return result;
            } finally {
                MonitorContextHolder.clearContext();
            }
        }, "复杂Vue项目");

        printTokenStats(stats, "复杂 Vue 项目（1500-2000行）");
        log.info("========== 测试完成：复杂 Vue 项目 ==========\n");
    }

    /**
     * 监控 Token 使用情况的通用方法
     */
    private TokenUsageStats monitorTokenUsage(TokenUsageTask task, String taskName) {
        AtomicLong inputTokens = new AtomicLong(0);
        AtomicLong outputTokens = new AtomicLong(0);
        AtomicLong totalTokens = new AtomicLong(0);

        Instant startTime = Instant.now();

        try {
            log.info("[{}] 开始生成代码...", taskName);

            List<String> result = task.execute();

            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // 从监控上下文获取 Token 信息
            MonitorContext context = MonitorContextHolder.getContext();
            if (context != null && context.getTotalTokens() != null) {
                totalTokens.set(context.getTotalTokens());
                log.info("[{}] 从上下文获取 Total Tokens: {}", taskName, totalTokens.get());
            }

            log.info("[{}] 生成完成！", taskName);
            log.info("[{}] 生成内容块数: {}", taskName, result != null ? result.size() : 0);
            log.info("[{}] 耗时: {} 秒", taskName, duration.getSeconds());

            return new TokenUsageStats(
                    taskName,
                    inputTokens.get(),
                    outputTokens.get(),
                    totalTokens.get(),
                    duration.getSeconds(),
                    result != null ? result.size() : 0
            );

        } catch (Exception e) {
            log.error("[{}] 生成失败: {}", taskName, e.getMessage(), e);
            throw new RuntimeException("Token 监控测试失败: " + taskName, e);
        }
    }

    /**
     * 打印 Token 统计信息
     */
    private void printTokenStats(TokenUsageStats stats, String description) {
        log.info("\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           Token 消耗统计报告: {}           ", String.format("%-20s", description));
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  任务名称: {}", String.format("%-47s", stats.taskName));
        log.info("║  ────────────────────────────────────────────────────────  ║");
        log.info("║  Input Tokens:  {:>10} tokens                          ║", stats.inputTokens);
        log.info("║  Output Tokens: {:>10} tokens                          ║", stats.outputTokens);
        log.info("║  Total Tokens:  {:>10} tokens  👈 重要指标            ║", stats.totalTokens);
        log.info("║  ────────────────────────────────────────────────────────  ║");
        log.info("║  生成耗时:      {:>10} 秒                              ║", stats.durationSeconds);
        log.info("║  内容块数:      {:>10} 块                              ║", stats.chunkCount);
        log.info("║  ────────────────────────────────────────────────────────  ║");
        log.info("║  💰 积分消耗估算（按不同等级）:                          ║");
        log.info("║    🟢 简单级 (1积分/1K):   {:>6} 积分                  ║", calculatePoints(stats.totalTokens, 1));
        log.info("║    🟡 中等级 (3积分/1K):   {:>6} 积分                  ║", calculatePoints(stats.totalTokens, 3));
        log.info("║    🟠 困难级 (8积分/1K):   {:>6} 积分                  ║", calculatePoints(stats.totalTokens, 8));
        log.info("║    🔴 专家级 (15积分/1K):  {:>6} 积分                  ║", calculatePoints(stats.totalTokens, 15));
        log.info("╚════════════════════════════════════════════════════════════╝");
        log.info("\n");
    }

    /**
     * 计算积分消耗
     */
    private int calculatePoints(long totalTokens, int pointsPerKToken) {
        if (totalTokens == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalTokens / 1000 * pointsPerKToken);
    }

    /**
     * Token 使用统计数据类
     */
    private record TokenUsageStats(
            String taskName,
            long inputTokens,
            long outputTokens,
            long totalTokens,
            long durationSeconds,
            int chunkCount
    ) {}

    /**
     * Token 使用任务函数式接口
     */
    @FunctionalInterface
    private interface TokenUsageTask {
        List<String> execute() throws Exception;
    }

    /**
     * 综合测试报告 - 运行所有测试后查看
     */
    @Test
    void generateFullReport() {
        log.info("\n\n");
        log.info("═══════════════════════════════════════════════════════════════════════════");
        log.info("                    AI 代码生成 Token 消耗综合测试                        ");
        log.info("═══════════════════════════════════════════════════════════════════════════");
        log.info("\n");
        log.info("📋 测试说明:");
        log.info("  本测试用于评估不同复杂度项目的 Token 消耗情况，");
        log.info("  为积分定价系统提供数据支撑。");
        log.info("\n");
        log.info("🎯 测试场景:");
        log.info("  1. 简单 HTML 页面      - 个人名片（单文件）");
        log.info("  2. 中等多文件项目      - 待办事项（3-5个文件）");
        log.info("  3. 简单 Vue 项目       - 博客首页（300行）");
        log.info("  4. 中等 Vue 项目       - 任务管理（500-800行）");
        log.info("  5. 复杂 Vue 项目       - 电商后台（1500-2000行）");
        log.info("\n");
        log.info("💡 使用方法:");
        log.info("  1. 单独运行各个测试方法（如 testSimpleVueProjectTokenUsage）");
        log.info("  2. 查看控制台输出的 Token 统计报告");
        log.info("  3. 根据实际消耗调整积分定价策略");
        log.info("\n");
        log.info("⚠️  注意事项:");
        log.info("  - 每次测试会调用真实的 AI API，会产生实际费用");
        log.info("  - Token 消耗会因 prompt 和模型响应而有所波动");
        log.info("  - 建议先从简单测试开始，逐步测试复杂项目");
        log.info("  - 测试前确保 Redis 服务已启动");
        log.info("\n");
        log.info("═══════════════════════════════════════════════════════════════════════════");
        log.info("\n");
    }
}
