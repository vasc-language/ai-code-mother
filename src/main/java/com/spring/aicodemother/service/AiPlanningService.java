package com.spring.aicodemother.service;

import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.model.entity.AiModelConfig;
import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import com.spring.aicodemother.model.vo.DevelopmentPlanVO;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI规划服务
 * 使用轻量级模型生成开发计划（不执行任何工具）
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Service
@Slf4j
public class AiPlanningService {

    @Resource
    private AiModelConfigService aiModelConfigService;

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String unifiedApiKey;

    /**
     * 内部AI助手接口
     */
    private interface PlanningAssistant {
        String chat(String prompt);
    }

    /**
     * 生成开发计划（不执行任何工具）
     *
     * @param userRequirement 用户需求
     * @param codeGenType     代码生成类型
     * @return 开发计划VO
     */
    public DevelopmentPlanVO generatePlan(String userRequirement, String codeGenType) {
        // 优先使用轻量级模型（qwen-turbo）降低成本
        AiModelConfig lightModel = aiModelConfigService.getByModelKey("qwen-turbo");
        if (lightModel == null || lightModel.getIsEnabled() != 1) {
            // 如果 qwen-turbo 不可用，回退到 deepseek-chat
            log.warn("轻量级模型 qwen-turbo 不可用，回退到 deepseek-chat");
            lightModel = aiModelConfigService.getByModelKey("deepseek-chat");
        }
        if (lightModel == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无可用的规划模型");
        }

        log.info("使用模型 {} 生成开发计划", lightModel.getModelName());

        // 构建ChatModel（不注册任何工具）
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(unifiedApiKey)
                .baseUrl(lightModel.getBaseUrl())
                .modelName(lightModel.getModelName())
                .temperature(0.7)
                .maxTokens(3000) // 计划不需要太长
                .logRequests(true)
                .logResponses(true)
                .build();

        // 构建完整的提示词（System Prompt + User Message）
        String systemPrompt = buildPlanningSystemPrompt(codeGenType);
        String userMessage = buildPlanningUserMessage(userRequirement);
        String fullPrompt = systemPrompt + "\n\n" + userMessage;

        // 调用AI生成计划
        try {
            // 使用AiServices创建助手
            PlanningAssistant assistant = dev.langchain4j.service.AiServices.create(PlanningAssistant.class, model);

            String planContent = assistant.chat(fullPrompt);
            log.info("开发计划生成成功，长度: {} 字符", planContent.length());

            // 解析计划内容，提取结构化信息
            return parsePlanContent(planContent);
        } catch (Exception e) {
            log.error("生成开发计划失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成开发计划失败: " + e.getMessage());
        }
    }

    /**
     * 构建规划的System Prompt
     *
     * @param codeGenType 代码生成类型
     * @return System Prompt
     */
    private String buildPlanningSystemPrompt(String codeGenType) {
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        String typeText = codeGenTypeEnum != null ? codeGenTypeEnum.getText() : "未知类型";

        return """
                你是一位经验丰富的全栈开发专家，擅长制定详细的开发计划。
                
                当前项目类型：%s
                
                你的任务是根据用户需求，制定一份详细的开发计划，必须包含以下部分：
                
                ## 1. 需求分析
                - 核心功能点
                - 用户使用场景
                - 关键技术难点
                
                ## 2. 技术选型
                - 前端框架/库及版本（如Vue 3、React等）
                - 状态管理方案（如Pinia、Vuex等）
                - UI组件库（如Ant Design Vue、Element Plus等）
                - 其他关键依赖
                
                ## 3. 目录结构
                - 完整的项目目录树
                - 每个目录的用途说明
                
                ## 4. 核心模块
                - 列出主要功能模块
                - 每个模块的职责
                - 模块间的依赖关系
                
                ## 5. 开发步骤
                - 详细的分步实施计划（每步一行，格式：步骤X: 具体内容）
                - 每步的预期产出
                - 建议的开发顺序
                
                ## 6. 预估工作量
                - 预计文件数量
                - 预计代码行数
                - 开发建议时长
                
                注意：
                1. 计划要详细但简洁，避免冗余
                2. 使用Markdown格式，结构清晰
                3. 不要包含具体代码，只描述要做什么
                4. 确保计划可执行性强
                5. 技术栈要明确列出，用逗号分隔（例如：Vue 3, Pinia, Ant Design Vue）
                """.formatted(typeText);
    }

    /**
     * 构建规划的User Message
     *
     * @param userRequirement 用户需求
     * @return User Message
     */
    private String buildPlanningUserMessage(String userRequirement) {
        return "用户需求：\n" + userRequirement +
                "\n\n请为以上需求制定详细的开发计划。";
    }

    /**
     * 解析计划内容，提取结构化信息
     *
     * @param planContent 计划内容
     * @return DevelopmentPlanVO
     */
    private DevelopmentPlanVO parsePlanContent(String planContent) {
        DevelopmentPlanVO vo = new DevelopmentPlanVO();
        
        // 在计划内容末尾追加友好的交互引导语
        String enhancedContent = planContent + """
                
                
                ---
                
                📋 **开发计划已生成**
                
                ✅ 如果您认可这个方案，请直接回复「可以」、「没问题」或「开始吧」
                ✏️ 如果需要调整方案，请告诉我您的想法，我会重新规划
                
                **您觉得这个计划合适吗？**
                """;
        
        vo.setContent(enhancedContent);
        vo.setCreatedAt(LocalDateTime.now());

        // 提取开发步骤数
        vo.setEstimatedSteps(extractStepCount(planContent));

        // 提取预计文件数量
        vo.setEstimatedFiles(extractFileCount(planContent));

        // 提取技术栈
        vo.setTechStack(extractTechStack(planContent));

        return vo;
    }

    /**
     * 提取开发步骤数
     *
     * @param content 计划内容
     * @return 步骤数
     */
    private Integer extractStepCount(String content) {
        // 匹配 "步骤1"、"步骤2" 等模式
        Pattern pattern = Pattern.compile("步骤\\d+[:：]");
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count > 0 ? count : null;
    }

    /**
     * 提取预计文件数量
     *
     * @param content 计划内容
     * @return 文件数量
     */
    private Integer extractFileCount(String content) {
        // 匹配 "预计X个文件"、"约X个文件" 等模式
        Pattern pattern = Pattern.compile("(?:预计|约)\\s*(\\d+)\\s*(?:个)?文件");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("解析文件数量失败: {}", matcher.group(1));
            }
        }
        return null;
    }

    /**
     * 提取技术栈
     *
     * @param content 计划内容
     * @return 技术栈列表
     */
    private List<String> extractTechStack(String content) {
        List<String> techStack = new ArrayList<>();

        // 查找技术选型部分
        Pattern sectionPattern = Pattern.compile("##\\s*2\\.\\s*技术选型[\\s\\S]*?(?=##|$)");
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String techSection = sectionMatcher.group();

            // 提取常见的技术栈关键词
            String[] keywords = {
                    "Vue", "React", "Angular", "Pinia", "Vuex", "Redux",
                    "Ant Design", "Element", "TypeScript", "JavaScript",
                    "Vite", "Webpack", "Node.js", "Express"
            };

            for (String keyword : keywords) {
                if (techSection.contains(keyword)) {
                    // 避免重复添加
                    if (!techStack.contains(keyword)) {
                        techStack.add(keyword);
                    }
                }
            }
        }

        return techStack.isEmpty() ? null : techStack;
    }
}
