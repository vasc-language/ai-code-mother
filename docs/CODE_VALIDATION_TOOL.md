# 代码验证工具 (validateCode)

## 概述

**CodeValidationTool** 是一个集成到工具调用系统中的代码质量验证工具，AI可以主动调用它来检查生成的Vue项目代码质量。

## 设计理念

### 为什么用Tool而不是自动验证？

**之前的方案问题**：
- ❌ 复杂的验证流程（ESLint → AI修复 → 重试）
- ❌ AI修复成功率低（模板语法冲突、Token消耗大）
- ❌ 用户无法控制修复流程

**新方案优势**：
- ✅ **AI自主决策**：AI看到错误后自己决定如何修复
- ✅ **简单可靠**：只做检测，不做自动修复
- ✅ **符合工具理念**：和 writeFile、readFile 一样的使用方式
- ✅ **更灵活**：AI可以多次调用 validateCode → 修改文件 → 再次验证

---

## 工作流程

```
AI生成代码
  ↓
AI调用 writeFile 写入所有文件
  ↓
AI调用 validateCode 检查代码
  ↓
返回错误列表（如果有）
  ↓
AI分析错误并调用 writeFile 修复
  ↓
AI再次调用 validateCode 验证
  ↓
验证通过 → AI调用 exit 结束
```

---

## 工具接口

### 方法签名

```java
@Tool("验证Vue项目代码质量，检查语法错误、未定义变量等问题。建议在完成所有文件编写后调用此工具进行检查。")
public String validateCode(
    @P("要验证的项目相对路径，通常为当前项目根目录，传入 '.' 即可")
    String projectPath,
    @ToolMemoryId Long appId
)
```

### 参数说明

| 参数 | 类型 | 说明 | 示例 |
|------|------|------|------|
| projectPath | String | 项目相对路径 | `"."` 或 `""` |
| appId | Long | 应用ID（自动注入） | 336015842249949184 |

### 返回值

**验证通过**：
```
代码验证通过，未发现错误。可以继续构建项目。
```

**发现错误**：
```
发现 3 个代码错误：

1. src/App.vue:12:5 - 'ref' is not defined (no-undef)
2. src/components/Navigation.vue:8:3 - The "UserCard" component has been registered but not used (vue/no-unused-components)
3. src/views/Home.vue:15:10 - Parsing error: Unexpected token } 

请修复这些错误后重新验证。
```

---

## AI使用示例

### 场景1：首次验证（发现错误）

**AI生成代码后**：
```json
{
  "toolName": "validateCode",
  "arguments": {
    "projectPath": "."
  }
}
```

**返回**：
```
发现 1 个代码错误：

1. src/App.vue:12:5 - 'ref' is not defined (no-undef)

请修复这些错误后重新验证。
```

**AI的下一步**：
```json
{
  "toolName": "writeFile",
  "arguments": {
    "relativeFilePath": "src/App.vue",
    "content": "<script setup>\nimport { ref } from 'vue';\n\nconst count = ref(0);\n</script>"
  }
}
```

**AI再次验证**：
```json
{
  "toolName": "validateCode",
  "arguments": {
    "projectPath": "."
  }
}
```

**返回**：
```
代码验证通过，未发现错误。可以继续构建项目。
```

**AI完成**：
```json
{
  "toolName": "exit"
}
```

---

## 自动配置ESLint

工具会自动处理ESLint的配置，无需AI或用户手动操作：

### 1. 检测 package.json

- 如果缺少 `scripts.lint`，自动添加
- 如果缺少 `eslint` 依赖，自动添加

### 2. 安装依赖

```bash
npm install
```

### 3. 创建配置文件

根据 `package.json` 的 `type` 字段：
- **ES Module** (`"type": "module"`) → 创建 `.eslintrc.cjs`
- **CommonJS** → 创建 `.eslintrc.js`

### 默认配置

```javascript
module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  plugins: ['vue'],
  rules: {
    'no-console': 'off',
    'no-debugger': 'warn',
    'vue/multi-word-component-names': 'off'
  }
};
```

---

## 技术实现

### 核心方法

1. **`ensureESLintInstalled()`** - 确保ESLint已安装和配置
2. **`runESLint()`** - 执行ESLint检查
3. **`parseESLintErrors()`** - 解析JSON格式的错误输出

### 命令执行

```java
npm run lint -- --format json --output-file eslint-result.json
```

使用 `--output-file` 参数（而不是shell重定向），避免路径问题。

### 错误解析

从ESLint JSON输出中提取：
- 文件路径
- 行号和列号
- 错误消息
- 规则名称

---

## 与现有系统集成

### 注册到ToolManager

工具继承 `BaseTool` 并使用 `@Component` 注解，Spring会自动扫描并注册到 `ToolManager`：

```java
@Component
public class CodeValidationTool extends BaseTool {
    // ...
}
```

### 在AI服务中使用

`AiCodeGeneratorServiceFactory` 会自动注入所有工具：

```java
@Resource
private ToolManager toolManager;

AiServices.builder(AiCodeGeneratorService.class)
    .streamingChatModel(model)
    .tools(toolManager.getAllTools())  // 包含 validateCode
    .build();
```

---

## 优势对比

| 特性 | 自动修复方案（旧） | 工具调用方案（新） |
|------|-------------------|-------------------|
| **实现复杂度** | ❌ 高（需要AI修复逻辑） | ✅ 低（只做检测） |
| **成功率** | ❌ 中等（AI修复可能失败） | ✅ 高（AI自己决策） |
| **灵活性** | ❌ 固定流程 | ✅ AI自主控制 |
| **Token消耗** | ❌ 高（多次AI调用） | ✅ 中等（按需调用） |
| **可维护性** | ❌ 低（复杂逻辑） | ✅ 高（简单明确） |
| **用户体验** | ❌ 黑盒操作 | ✅ 透明可见 |

---

## 日志示例

### 成功场景

```
[INFO] 注册工具: validateCode -> 验证代码
[INFO] 开始验证代码质量: D:/project/vue_project_123
[INFO] ESLint已安装
[INFO] 执行命令: npm.cmd run lint -- --format json --output-file eslint-result.json
[INFO] ESLint未生成输出文件，可能没有错误
```

### 发现错误场景

```
[INFO] 开始验证代码质量: D:/project/vue_project_123
[INFO] 检测到缺少ESLint配置
[INFO] 已添加ESLint配置到package.json
[INFO] 安装ESLint依赖...
[INFO] 已创建ESLint配置文件: .eslintrc.cjs
[INFO] 执行命令: npm.cmd run lint -- --format json --output-file eslint-result.json
[INFO] 发现 3 个错误，返回给AI处理
```

---

## 未来改进

1. **支持TypeScript类型检查** - 集成 `vue-tsc`
2. **增量验证** - 只检查修改过的文件
3. **严格模式** - 可配置的规则严格程度
4. **自定义规则** - 允许AI生成项目时指定ESLint规则
5. **性能优化** - 缓存验证结果

---

## 常见问题

### Q1: AI会滥用这个工具吗？

**A**: 不会。工具描述中建议"在完成所有文件编写后调用"，AI会理解这个提示。

### Q2: 如果ESLint安装失败怎么办？

**A**: 工具会返回错误信息，AI会看到并可能选择跳过验证或提示用户。

### Q3: 验证会阻塞构建吗？

**A**: 不会。这只是一个可选的工具，AI可以选择不调用它，直接退出工具调用。

### Q4: 如何调试工具？

**A**: 查看日志输出，工具会记录所有关键步骤和错误信息。

---

## 总结

**CodeValidationTool** 是一个轻量级的代码质量检查工具，通过工具调用的方式让AI自主决策如何处理代码错误，比自动修复方案更加灵活和可靠。

**核心理念**：
- 🔍 **只检测，不修复** - 保持工具单一职责
- 🤖 **AI自主决策** - 让AI根据错误信息自己修复
- 🛠️ **工具链协作** - validateCode + writeFile + readFile 配合使用
- ✅ **简单可靠** - 减少复杂逻辑，提高成功率
