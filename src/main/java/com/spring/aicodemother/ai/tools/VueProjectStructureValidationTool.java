package com.spring.aicodemother.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue 项目结构验证工具
 * 用于在生成前3个核心文件后，验证项目结构是否符合 Vue 3 工程标准
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Component
public class VueProjectStructureValidationTool extends BaseTool {

    @Tool("验证 Vue 项目结构是否符合标准，必须在创建 package.json、vite.config.js、index.html 这三个文件后立即调用")
    public String validateVueProjectStructure(@ToolMemoryId Long appId) {
        try {
            // 获取项目目录
            String projectDirName = "vue_project_" + appId;
            File projectDir = new File(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);

            if (!projectDir.exists()) {
                return "❌ 错误：项目目录不存在: " + projectDirName;
            }

            log.info("开始验证 Vue 项目结构: {}", projectDir.getAbsolutePath());

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // 1. 检查是否有 styles/ 或 scripts/ 目录（这表示生成了原生 HTML 项目）
            File stylesDir = new File(projectDir, "styles");
            File scriptsDir = new File(projectDir, "scripts");

            if (stylesDir.exists()) {
                errors.add("❌ 发现 styles/ 目录！这是原生 HTML 项目的结构，不是 Vue 项目！");
                errors.add("   → CSS 文件应该放在 src/styles/ 或组件的 <style> 标签中");
            }

            if (scriptsDir.exists()) {
                errors.add("❌ 发现 scripts/ 目录！这是原生 HTML 项目的结构，不是 Vue 项目！");
                errors.add("   → JS 文件应该放在 src/ 目录中，使用 ES Module 方式导入");
            }

            // 2. 检查必需文件是否存在
            File packageJson = new File(projectDir, "package.json");
            File viteConfig = new File(projectDir, "vite.config.js");
            File indexHtml = new File(projectDir, "index.html");

            if (!packageJson.exists()) {
                errors.add("❌ 缺少 package.json 文件！这是 Vue 项目的核心配置文件，必须存在");
            }

            if (!viteConfig.exists()) {
                errors.add("❌ 缺少 vite.config.js 文件！这是 Vite 构建工具的配置文件，必须存在");
            }

            if (!indexHtml.exists()) {
                errors.add("❌ 缺少 index.html 文件！这是应用的入口 HTML 文件，必须存在");
            }

            // 如果核心文件缺失，不再继续检查
            if (!errors.isEmpty()) {
                return formatValidationResult(errors, warnings, false);
            }

            // 3. 验证 package.json 内容
            String packageJsonContent = FileUtil.readString(packageJson, StandardCharsets.UTF_8);
            validatePackageJson(packageJsonContent, errors, warnings);

            // 4. 验证 vite.config.js 内容
            String viteConfigContent = FileUtil.readString(viteConfig, StandardCharsets.UTF_8);
            validateViteConfig(viteConfigContent, errors, warnings);

            // 5. 验证 index.html 内容
            String indexHtmlContent = FileUtil.readString(indexHtml, StandardCharsets.UTF_8);
            validateIndexHtml(indexHtmlContent, errors, warnings);

            // 6. 检查是否有根目录下的 .css 或 .js 文件
            File[] rootFiles = projectDir.listFiles();
            if (rootFiles != null) {
                for (File file : rootFiles) {
                    if (!file.isFile()) {
                        continue;
                    }
                    String fileName = file.getName();
                    if (fileName.endsWith(".css")) {
                        warnings.add("⚠️ 根目录下发现 CSS 文件: " + fileName);
                        warnings.add("   → 建议将 CSS 放在 src/styles/ 目录或组件中");
                    }
                    if (fileName.endsWith(".js") && !fileName.equals("vite.config.js")) {
                        warnings.add("⚠️ 根目录下发现 JS 文件: " + fileName);
                        warnings.add("   → 建议将 JS 代码放在 src/ 目录中");
                    }
                }
            }

            return formatValidationResult(errors, warnings, errors.isEmpty());

        } catch (Exception e) {
            log.error("验证 Vue 项目结构失败: {}", e.getMessage(), e);
            return "❌ 验证失败: " + e.getMessage();
        }
    }

    /**
     * 验证 package.json 内容
     */
    private void validatePackageJson(String content, List<String> errors, List<String> warnings) {
        try {
            JSONObject pkg = JSONUtil.parseObj(content);

            // 检查 dependencies
            JSONObject dependencies = pkg.getJSONObject("dependencies");
            if (dependencies == null) {
                errors.add("❌ package.json 缺少 dependencies 字段！");
                return;
            }

            if (!dependencies.containsKey("vue")) {
                errors.add("❌ package.json 缺少 vue 依赖！必须添加 \"vue\": \"^3.3.4\"");
            }

            // 检查 devDependencies
            JSONObject devDependencies = pkg.getJSONObject("devDependencies");
            if (devDependencies == null) {
                errors.add("❌ package.json 缺少 devDependencies 字段！");
                return;
            }

            if (!devDependencies.containsKey("@vitejs/plugin-vue")) {
                errors.add("❌ package.json 缺少 @vitejs/plugin-vue 依赖！必须添加 \"@vitejs/plugin-vue\": \"^4.2.3\"");
            }

            if (!devDependencies.containsKey("vite")) {
                errors.add("❌ package.json 缺少 vite 依赖！必须添加 \"vite\": \"^4.4.5\"");
            }

            // 检查 scripts
            JSONObject scripts = pkg.getJSONObject("scripts");
            if (scripts == null) {
                warnings.add("⚠️ package.json 缺少 scripts 字段，建议添加 dev 和 build 脚本");
            } else {
                if (!scripts.containsKey("dev")) {
                    warnings.add("⚠️ package.json 缺少 dev 脚本，建议添加 \"dev\": \"vite\"");
                }
                if (!scripts.containsKey("build")) {
                    warnings.add("⚠️ package.json 缺少 build 脚本，建议添加 \"build\": \"vite build\"");
                }
            }

        } catch (Exception e) {
            errors.add("❌ package.json 格式错误，无法解析: " + e.getMessage());
        }
    }

    /**
     * 验证 vite.config.js 内容
     */
    private void validateViteConfig(String content, List<String> errors, List<String> warnings) {
        // 检查是否导入了 Vue 插件
        if (!content.contains("@vitejs/plugin-vue")) {
            errors.add("❌ vite.config.js 没有导入 @vitejs/plugin-vue 插件！");
            errors.add("   → 必须添加: import vue from '@vitejs/plugin-vue'");
        }

        // 检查是否使用了 Vue 插件
        if (!content.contains("vue()")) {
            errors.add("❌ vite.config.js 没有在 plugins 中使用 vue() 插件！");
        }

        // 检查是否配置了 base
        if (!content.contains("base:") && !content.contains("base :")) {
            warnings.add("⚠️ vite.config.js 建议配置 base: './' 以支持子路径部署");
        }

        // 检查是否配置了别名
        if (!content.contains("alias")) {
            warnings.add("⚠️ vite.config.js 建议配置 @ 别名指向 src 目录");
        }
    }

    /**
     * 验证 index.html 内容
     */
    private void validateIndexHtml(String content, List<String> errors, List<String> warnings) {
        // 检查是否有 app 容器
        if (!content.contains("id=\"app\"") && !content.contains("id='app'")) {
            errors.add("❌ index.html 缺少 <div id=\"app\"></div>！这是 Vue 应用挂载的容器");
        }

        // 检查是否引入了 main.js
        if (!content.contains("/src/main.js") && !content.contains("src/main.js")) {
            errors.add("❌ index.html 没有引入 /src/main.js！必须添加: <script type=\"module\" src=\"/src/main.js\"></script>");
        }

        // 检查是否有多个 script 标签（排除 main.js）
        int scriptCount = 0;
        int fromIndex = 0;
        while ((fromIndex = content.indexOf("<script", fromIndex)) != -1) {
            scriptCount++;
            fromIndex++;
        }

        if (scriptCount > 1) {
            warnings.add("⚠️ index.html 有多个 <script> 标签！Vue 项目通常只需要一个入口 script");
            warnings.add("   → 其他 JS 文件应该通过 ES Module 的 import 方式导入");
        }

        // 检查是否有 link stylesheet 标签
        if (content.contains("<link") && content.contains("stylesheet")) {
            warnings.add("⚠️ index.html 引入了外部 CSS 文件！");
            warnings.add("   → 建议将样式放在 Vue 组件的 <style> 标签中或 src/styles/ 目录");
        }
    }

    /**
     * 格式化验证结果
     */
    private String formatValidationResult(List<String> errors, List<String> warnings, boolean success) {
        StringBuilder result = new StringBuilder();

        if (success) {
            result.append("✅ Vue 项目结构验证通过！\n\n");
            result.append("已确认：\n");
            result.append("- ✅ 项目结构符合 Vue 3 + Vite 标准\n");
            result.append("- ✅ 核心配置文件齐全且正确\n");
            result.append("- ✅ 依赖声明完整\n\n");

            if (!warnings.isEmpty()) {
                result.append("⚠️ 建议优化项（不影响运行）：\n");
                for (String warning : warnings) {
                    result.append(warning).append("\n");
                }
                result.append("\n");
            }

            result.append("👉 可以继续创建 src/main.js、src/App.vue 等源代码文件");

        } else {
            result.append("❌ Vue 项目结构验证失败！\n\n");
            result.append("发现以下问题：\n");
            for (String error : errors) {
                result.append(error).append("\n");
            }
            result.append("\n");

            if (!warnings.isEmpty()) {
                result.append("⚠️ 其他提示：\n");
                for (String warning : warnings) {
                    result.append(warning).append("\n");
                }
                result.append("\n");
            }

            result.append("🚨 请根据上述错误修复文件内容后，重新调用此工具验证！\n");
            result.append("🚫 在验证通过前，不得继续创建其他文件！");
        }

        return result.toString();
    }

    @Override
    public String getToolName() {
        return "validateVueProjectStructure";
    }

    @Override
    public String getDisplayName() {
        return "验证Vue项目结构";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return String.format("\n\n[工具调用] %s\n\n", getDisplayName());
    }
}
