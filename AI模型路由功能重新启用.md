# AI模型路由功能重新启用 ✅

## 📋 功能说明

项目包含两个AI路由功能，用于自动化应用创建流程：

### 1. **应用名称生成器** (AppNameGeneratorService)
- **功能**: 根据用户输入的提示词，AI自动生成简洁的应用名称
- **示例**: 输入"制作一个在线商城" → 生成"在线商城系统"

### 2. **代码生成类型路由** (AiCodeGenTypeRoutingService)
- **功能**: 根据用户需求，AI自动判断应该使用哪种生成模式
- **三种模式**:
  - `HTML`: 单页面HTML项目
  - `MULTI_FILE`: 多文件项目（默认）
  - `VUE_PROJECT`: Vue 3完整工程

---

## 🔧 修改内容

### 修改1: 优化路由模型配置 (application.yml)

**修改前**:
```yaml
routing-chat-model:
  model-name: qwen3-max  # EXPERT级别，15积分/1K tokens
```

**修改后**:
```yaml
routing-chat-model:
  model-name: codex-mini-latest  # SIMPLE级别，1积分/1K tokens
```

**优化理由**:
- 路由任务是简单的分类工作
- 使用SIMPLE级别模型成本降低**93%** (15积分 → 1积分)
- `codex-mini-latest` 足够处理分类任务

---

### 修改2: 重新启用AI路由功能 (AppServiceImpl.java)

**修改前**:
```java
// 使用简化策略生成项目名称（暂时不调用AI以避免API错误）
String appName = initPrompt.substring(0, Math.min(initPrompt.length(), 12));

// 使用默认代码生成类型（暂时不调用AI路由）
app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
```

**修改后**:
```java
// 使用AI生成项目名称
String appName;
try {
    AppNameGeneratorService nameGenerator =
        appNameGeneratorServiceFactory.createAppNameGeneratorService();
    appName = nameGenerator.generateAppName(initPrompt);
    log.info("AI生成应用名称成功: {}", appName);
} catch (Exception e) {
    log.warn("AI生成应用名称失败，使用简化策略: {}", e.getMessage());
    appName = initPrompt.substring(0, Math.min(initPrompt.length(), 12));
}

// 使用AI路由判断代码生成类型
CodeGenTypeEnum codeGenType;
try {
    AiCodeGenTypeRoutingService routingService =
        aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
    codeGenType = routingService.routeCodeGenType(initPrompt);
    log.info("AI路由判断生成类型成功: {}", codeGenType.getText());
} catch (Exception e) {
    log.warn("AI路由判断失败，使用默认类型: {}", e.getMessage());
    codeGenType = CodeGenTypeEnum.MULTI_FILE;
}
```

**新特性**:
- ✅ 启用AI路由功能
- ✅ 异常容错机制（AI失败时自动降级）
- ✅ 详细日志记录

---

## 🎯 工作流程

### 用户创建应用时的完整流程：

```
1. 用户输入提示词
   ↓
2. AI自动生成应用名称（codex-mini-latest模型）
   ↓
3. AI自动判断生成类型（codex-mini-latest模型）
   ↓
4. 保存到数据库
   ↓
5. 返回应用ID
```

### 路由判断示例：

| 用户提示词 | AI判断类型 | 说明 |
|----------|----------|------|
| "制作一个登录页面" | HTML | 单页面，简单 |
| "开发一个博客系统，包含首页、文章列表、文章详情" | MULTI_FILE | 多文件，中等复杂度 |
| "创建一个Vue后台管理系统，包含路由、状态管理" | VUE_PROJECT | Vue工程，完整项目 |

---

## 📊 成本对比

### 路由模型成本优化：

| 项目 | 修改前 | 修改后 | 节省 |
|-----|-------|-------|------|
| 模型 | qwen3-max | codex-mini-latest | - |
| 等级 | EXPERT | SIMPLE | ⬇️ 3级 |
| 费用 | 15积分/1K tokens | 1积分/1K tokens | **93%** |
| 单次路由成本 | ~1.5积分 (100 tokens) | ~0.1积分 (100 tokens) | **93%** |

### 使用AI路由的好处：

1. **用户体验提升**：自动生成合适的应用名称
2. **智能分类**：自动选择最适合的生成模式
3. **节省时间**：用户无需手动选择生成类型
4. **成本优化**：使用经济型模型处理简单任务

---

## 🚀 测试建议

### 测试场景1: 创建HTML项目
```
提示词: "制作一个精美的登录页面"
预期结果:
  - 应用名称: "登录页面" 或类似
  - 生成类型: HTML
```

### 测试场景2: 创建多文件项目
```
提示词: "开发一个在线商城，包含首页、商品列表、购物车"
预期结果:
  - 应用名称: "在线商城" 或 "商城系统"
  - 生成类型: MULTI_FILE
```

### 测试场景3: 创建Vue工程
```
提示词: "创建一个Vue管理后台，包含路由、状态管理、组件化"
预期结果:
  - 应用名称: "管理后台" 或 "后台系统"
  - 生成类型: VUE_PROJECT
```

### 测试场景4: AI失败降级
```
操作: 断开网络或模型不可用
预期结果:
  - 应用名称: 使用提示词前12个字符
  - 生成类型: MULTI_FILE（默认）
  - 日志显示降级警告
```

---

## ⚠️ 重要说明

### 降级机制
- AI路由失败时**不会阻断**应用创建
- 自动降级为简化策略
- 记录详细警告日志便于排查

### 性能优化
- 路由模型使用 `@Scope("prototype")` 多例模式
- 每次请求创建新实例，支持并发
- `max-tokens: 100` 限制，路由任务快速完成

### 日志监控
```
AI生成应用名称成功: xxx
AI路由判断生成类型成功: xxx
应用创建成功，ID: xxx, 名称: xxx, 类型: xxx, 模型: xxx
```

---

## ✅ 功能状态

- ✅ 路由模型配置优化（qwen3-max → codex-mini-latest）
- ✅ AI应用名称生成器已启用
- ✅ AI代码类型路由已启用
- ✅ 异常降级机制已完善
- ✅ 详细日志记录已添加

---

## 📝 后续优化建议

### 1. 添加路由缓存
对于相同或相似的提示词，可以缓存路由结果：
```java
// 使用Caffeine缓存路由结果
Cache<String, CodeGenTypeEnum> routingCache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofHours(1))
    .build();
```

### 2. 路由准确度监控
记录AI路由的准确度，持续优化prompt：
```java
// 记录用户是否修改了AI判断的类型
if (!userSelectedType.equals(aiRoutedType)) {
    log.info("路由判断可能不准确: AI判断={}, 用户选择={}", aiRoutedType, userSelectedType);
}
```

### 3. 支持自定义路由规则
允许管理员配置关键词规则：
```yaml
routing-rules:
  vue-keywords: ["vue", "router", "pinia", "组件化"]
  html-keywords: ["单页", "简单页面", "landing page"]
```

---

## 🎉 总结

现在您的项目已经恢复了完整的AI路由功能：
1. ✅ 自动生成应用名称
2. ✅ 智能判断生成类型
3. ✅ 成本优化（节省93%路由成本）
4. ✅ 异常容错（AI失败不影响功能）

重启后端服务即可测试这些功能！
