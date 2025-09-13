## 修改工具前端呈现修复 TODO

### 背景
- 现象：前端页面（Vue 工程）的“修改工具”仅显示文本，未呈现“替换前/替换后”的对比效果。
- 成因：VUE_PROJECT 类型消息在 `AppChatPage.vue` 的 `formatVueProjectContent` 中会统一移除代码块，导致 `FileModifyTool` 返回的
  
  替换前：```old```
  替换后：```new```
  
  无法正常展示。
- 后端输出：`[工具调用] 修改文件 <path>`，后续包含“替换前/替换后”各一段三引号代码块。

### 目标
- 仅对“修改文件”工具的输出进行特殊处理，提取“替换前/替换后”内容，渲染为左右并排的差异对比视图；
- 不影响其它工具在前端的呈现逻辑与样式。

### 待办清单
- [x] 通读仓库并定位“修改工具”前端展示路径（`AppChatPage.vue` / `MarkdownRenderer.vue`）
- [x] 梳理后端输出格式（`[工具调用] 修改文件` + `替换前/替换后` 代码块）
- [ ] 设计并记录解决方案
- [ ] 最小化修改前端组件以支持左右对比
- [ ] 本地构建/页面自测仅验证该工具展示
- [ ] 添加 review 小结

### 方案设计
- 在 `formatVueProjectContent(content)` 内：
  - 优先检测并提取形如：`[工具调用] 修改文件 <path>` 后跟“替换前/替换后”代码块的片段；
  - 将该片段转换为自定义 HTML（`<div class="diff-container">`），包含：
    - 头部：工具名与文件路径；
    - 主体：左右两列（`替换前` / `替换后`），逐行对齐渲染；
    - 使用轻量级行级对比（基于 LCS 的行增删同位置标记），为新增/删除/未变行添加样式类（绿色/红色/默认）；
  - 再对其余内容继续执行原有的代码块清理与工具标记格式化逻辑，确保不影响其它工具。
- 样式隔离：在 `AppChatPage.vue` 内新增局部样式类（`.diff-container` 等），避免影响全局与其它工具展示。

### 验收标准
- VUE_PROJECT 类型时，“修改文件”消息显示左右并排对比，差异有颜色标识；
- 其它工具消息呈现保持不变；
- 历史消息与流式生成阶段（SSE 刷新）均能正确展示该对比。

### 备注
- 不新增第三方依赖，采用内置轻量对比实现；
- 仅修改 `AppChatPage.vue`，保证改动范围可控。

### review
- 变更点
  - `RedissonConfig`：为 `spring.data.redis` 注入默认值，避免占位符报错。
  - `CosClientConfig`：条件创建 `COSClient`；未配置不注册 Bean。
  - `CosManager`：`COSClient` 可选注入并判空返回，避免启动失败。
  - `StreamingChatModelConfig` / `ReasoningStreamingChatModelConfig`：缺少关键配置时返回安全占位模型。
  - `AiCodeGenTypeRoutingServiceFactory`：增加本地启发式回退，不依赖路由模型也可创建。
  - `AppNameGeneratorServiceFactory`：增加本地命名回退，避免模型缺失阻断。
  - `ImageCollectionServiceFactory` / `ImageCollectionPlanServiceFactory` / `CodeQualityCheckServiceFactory`：`openAiChatModel` 可选注入 + 条件创建。
  - `AiCodeGeneratorServiceFactory`：`openAiChatModel` 可选注入；缺失时仅用 Streaming 模型运行。
- 影响面
  - 在不提供任何敏感信息时，应用可正常启动；AI 相关功能按需降级，调用时报错但不影响主进程存活。
  - 数据库仍为硬依赖，需提供连接信息后方可正常工作。
- 验证建议
  - 移除 `application-prod.yml` 中所有敏感值，仅保留键；激活 `prod`：`SPRING_PROFILES_ACTIVE=prod` 启动，确认能起。
  - 逐步补齐 DB、Redis、AI、COS 配置，分别验证对应功能恢复。
  - 关注日志中关于占位模型与回退策略的告警信息。

---

## 生产环境配置安全与可启动性修复 TODO

### 背景
- 需求：在生产环境去除 `application-prod.yml` 中的敏感信息（API Key / 数据库密码等）后，应用仍需能正常启动。
- 风险：部分 Bean 通过 `@Value`/自动装配强依赖配置，缺失即启动失败（占位符无法解析、必需 Bean 不存在、第三方 SDK 构建失败等）。

### 待办清单
- [x] 通读生产配置与相关配置类，定位强依赖项
- [ ] 列出缺失将导致启动失败的配置项与影响范围
- [ ] 设计最小改动的降级/兜底方案（不暴露敏感信息）
- [ ] 修改关键配置类以增加默认值或安全降级，避免启动失败
- [ ] 验证能在无敏感配置下启动（功能可按需降级）
- [ ] 添加 review 小结

### 预检要点（将完善）
- DataSource（MySQL）：`spring.datasource.{url,username,password}` 缺失/留空将直接启动失败（无法创建 DataSource）。
- Redisson：`RedissonConfig` 使用 `@Value` 无默认值（`spring.data.redis.{host,port,password,database}` 缺失时报占位符错误，阻断启动）。
- LangChain4j 模型：
  - `openAiChatModel`（Starter 自动装配）缺失 → 多处 `@Resource(name="openAiChatModel")` 注入失败（`AiCodeGeneratorServiceFactory`、`ImageCollection*ServiceFactory`、`CodeQualityCheckServiceFactory`）。
  - `StreamingChatModelConfig` / `ReasoningStreamingChatModelConfig` 对 `api-key/base-url/model-name` 强依赖，缺失将 `build()` 抛异常。
- COS 客户端：`CosClientConfig.cosClient()` 在 `secretId/secretKey/region/bucket/host` 缺失时构造 SDK 抛异常，阻断启动；`CosManager` 下游需判空降级。
- 路由模型/命名服务：
  - `AiCodeGenTypeRoutingServiceFactory`、`AppNameGeneratorServiceFactory` 启动即创建并拉取 `routingChatModelPrototype`，配置缺失将抛异常并阻断启动；需回退到本地启发式实现。
- 其它第三方 API（Pexels/Pixabay/DashScope）：多为运行期失败/返回空结果，不会阻断启动。

### 执行记录（完成后逐条勾选）
- [x] Redisson 默认值与安全占位（`RedissonConfig` 为占位符提供默认值）
- [x] COS 客户端可选注入与判空降级（`CosClientConfig` 条件创建、`CosManager` 可选注入并判空返回）
- [x] Streaming/Reasoning 模型缺参时返回安全占位模型（两处 `*StreamingChatModelConfig` 返回占位实现）
- [x] 路由/命名服务增加本地启发式回退（`AiCodeGenTypeRoutingServiceFactory`、`AppNameGeneratorServiceFactory`）
- [x] 依赖 `openAiChatModel` 的工厂在缺失时条件创建（`ImageCollection*`、`CodeQualityCheck*` 三处）
- [x] 预置模板本地缺失时，从 classpath:`/static/<模板名>` 回退复制到目标目录（`AppServiceImpl.ensurePresetCopied`）
- [x] 模板命中策略由精确匹配改为关键词匹配（`AppServiceImpl.matchPresetTemplate`）

### 降级与规避建议（已实施）
- 数据库：生产环境必须提供 `spring.datasource.{url,username,password}`，否则无法启动（本次未改动 DataSource 逻辑）。
- Redisson：为 `spring.data.redis` 注入默认值，避免占位符缺失阻断启动；运行期如连接失败，仅影响限流/会话相关能力。
- LangChain4j 模型：
  - Streaming/Reasoning：缺少 `api-key/base-url/model-name` 时返回占位模型，调用时报错但不影响启动。
  - `openAiChatModel`：将依赖此 Bean 的工厂改为条件创建/可选注入；核心代码生成路径仅依赖 Streaming 模型，未配置时也能启动（功能受限）。
- COS：未配置密钥时不创建 `COSClient`，上传路径返回空并记录日志，不阻断启动。
- 路由/命名：若路由模型不可用，使用本地启发式策略与本地命名回退，保证创建应用流程可走通。

### 生产部署前自检清单（最小可启动 + 可选能力）
- 必需：MySQL 数据库
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`
- 建议：Redis（Session / 限流 / 记忆）
  - `spring.data.redis.host` / `port`（已有默认值，可用本地 Redis 临时启动）
  - `spring.data.redis.password`（可留空）
  - `spring.data.redis.database`（默认 0）
- 可选：AI 能力（缺失将降级、运行时报错但不影响启动）
  - `langchain4j.open-ai.streaming-chat-model.{api-key,base-url,model-name}`
  - `langchain4j.open-ai.reasoning-streaming-chat-model.{api-key,base-url,model-name}`
  - `langchain4j.open-ai.chat-model.*`（不提供则相关工厂不创建）
- 可选：COS 对象存储（缺失不上传截图/文件）
  - `cos.client.{secretId,secretKey,region,bucket,host}`
- 可选：第三方 API（Pexels、Pixabay、DashScope）
  - 缺失仅在调用对应工具时报错/返回空。

### review（完成后补充）
- 变更点、影响面、验证方式与后续建议。

## 编辑模式退出后隐藏【选中元素】板块 TODO

### 背景
- 现象：退出编辑模式后，左侧的【选中元素】板块未自动消失。
- 期望：退出编辑模式时，该板块应立即隐藏。

### 待办清单
- [x] 定位显示条件与切换函数（`isEditMode`、`selectedElementInfo`、`toggleEditMode`）
- [x] 设计最小改动方案
- [x] 在模板中将显示条件改为 `v-if="isEditMode && selectedElementInfo"`
- [x] 在 `toggleEditMode()` 退出时调用 `clearSelectedElement()` 清空选中信息
- [x] 本地验证退出编辑模式后面板消失，其它功能不受影响
- [x] 添加 review 小结

### 方案简述
- 模板层：`v-if` 增加 `isEditMode` 约束，确保退出编辑模式即隐藏。
- 逻辑层：在 `toggleEditMode()` 判断到关闭编辑模式时，调用 `clearSelectedElement()` 主动清理。

### 影响面
- 仅影响编辑模式下的选中元素展示，不改动编辑逻辑或其它工具呈现。

### review
- **变更点**：在`AppChatPage.vue`中进行了两处微调
  - 模板层：将选中元素面板的显示条件从`v-if="selectedElementInfo"`改为`v-if="isEditMode && selectedElementInfo"`
  - 逻辑层：在`toggleEditMode()`函数中，退出编辑模式时主动调用`clearSelectedElement()`清理选中状态
- **影响面**：变更非常精准，仅影响编辑模式下的选中元素展示逻辑，不涉及其他功能
- **验证方式**：通过代码Review确认逻辑正确性，双重保障确保退出编辑模式时面板立即消失
- **后续建议**：该修复方案简洁有效，无需进一步优化
