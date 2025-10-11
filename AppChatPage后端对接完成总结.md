# AppChatPage 后端接口对接完成总结

## ✅ 已完成的工作

### 1. 创建核心 Composables

#### **useCodeGeneration.ts** - SSE 流式代码生成
- ✅ SSE 连接管理（使用 @microsoft/fetch-event-source）
- ✅ 实时流式接收 AI 生成的代码
- ✅ 自动解析单文件（HTML）和多文件项目
- ✅ 文件内容解析（支持 Markdown 代码块格式）
- ✅ 预览 URL 生成（Blob URL）
- ✅ 停止生成功能
- ✅ 资源清理机制

**关键功能：**
```typescript
startCodeGeneration(appId, message, codeGenType, onMessageUpdate, onComplete)
stopGeneration()
parseGeneratedContent(content, codeGenType)
createPreviewUrl(htmlContent)
```

#### **useAppDeployment.ts** - 部署和下载
- ✅ 代码下载功能（ZIP 文件）
- ✅ 应用部署到云端
- ✅ 部署状态管理
- ✅ 文件下载处理

#### **useAppInfo.ts** - 应用信息管理
- ✅ 获取应用详情
- ✅ 权限判断（isOwner, isAdmin）
- ✅ AI 模型头像动态显示
- ✅ 支持多种 AI 模型图标

#### **useChatMessages.ts** - 聊天消息管理
- ✅ 消息列表维护
- ✅ 添加用户/AI 消息
- ✅ 消息更新（实时流式）
- ✅ 自动滚动到底部
- ✅ 聊天历史加载

### 2. 整合到 AppChatPage.vue

#### **完整的业务流程：**

1. **页面初始化**
   - 获取应用详情信息
   - 加载聊天历史（TODO）
   - 初始化 UI 状态

2. **发送消息流程**
   ```
   用户输入 → 点击发送 → 添加用户消息 →
   启动 SSE 连接 → 实时接收 AI 响应 →
   解析代码文件 → 创建预览 URL → 显示结果
   ```

3. **代码生成流程**
   - SSE 流式接收内容
   - 实时更新 AI 消息显示
   - 自动解析 HTML/多文件格式
   - 生成预览链接
   - 文件树展示

4. **下载/部署功能**
   - 下载：直接下载 ZIP 文件
   - 部署：调用后端 API，获取部署 URL

### 3. 技术实现细节

#### **SSE 连接（Server-Sent Events）**
```typescript
// 连接后端 SSE 端点
GET /app/chat/gen/code?appId=${appId}&userMessage=${message}

// 实时接收数据流
onmessage: (event) => {
  accumulatedContent += event.data
  onMessageUpdate(accumulatedContent)
}
```

#### **文件解析逻辑**
- **HTML 单文件：** 从 Markdown 代码块提取 HTML
- **多文件项目：** 解析 `### filename` 格式的文件块
- **语言检测：** 根据文件扩展名自动识别语言

#### **预览机制**
- 使用 Blob URL 创建临时预览链接
- iframe 方式加载 HTML 预览
- 自动资源清理（onUnmounted）

### 4. 数据流转

```
AppChatPage.vue
├── useAppInfo → 应用信息
├── useChatMessages → 消息管理
├── useCodeGeneration → 代码生成
│   ├── SSE 连接
│   ├── 文件解析
│   └── 预览生成
└── useAppDeployment → 下载/部署

↓ 数据传递到子组件

CodePanelLovable.vue
├── currentView (preview/code)
├── simpleCodeFile (HTML)
├── multiFiles (多文件)
├── previewUrl (预览链接)
└── activeFileKey (当前文件)
```

### 5. 安装的依赖

```json
{
  "@microsoft/fetch-event-source": "^2.0.1"
}
```

## 📋 待完成事项（TODO）

1. **聊天历史加载**
   - 在 `onMounted` 中调用 `loadChatHistory`
   - 实现历史消息过滤逻辑

2. **应用详情弹窗**
   - 创建 AppDetailModal 组件
   - 实现详情展示

3. **版本历史功能**
   - 集成 useVersionManagement
   - 实现版本回滚

4. **错误处理优化**
   - 添加重试机制
   - 更详细的错误提示

## 🎯 核心 API 接口

### 已对接接口：

1. **GET /app/get/vo** - 获取应用详情
2. **GET /app/chat/gen/code** - SSE 流式代码生成
3. **GET /app/download/{appId}** - 下载代码
4. **POST /app/deploy** - 部署应用

### 待对接接口：

1. **GET /app/chat/history** - 聊天历史
2. **POST /app/chat/stop** - 停止生成

## 🚀 使用方式

### 启动开发服务器
```bash
cd ai-code-mother-frontend
npm install  # 安装新依赖
npm run dev  # 启动开发服务器
```

### 测试流程
1. 访问应用生成页面：`/app/chat/{appId}`
2. 输入提示词，点击发送
3. 观察 SSE 流式生成过程
4. 查看文件树和代码预览
5. 切换预览/代码视图
6. 测试下载和部署功能

## 📁 涉及文件

### 新创建：
- `src/pages/app/composables/useCodeGeneration.ts`

### 已存在（已优化）：
- `src/pages/app/composables/useAppInfo.ts`
- `src/pages/app/composables/useChatMessages.ts`
- `src/pages/app/composables/useAppDeployment.ts`

### 主要修改：
- `src/pages/app/AppChatPage.vue` - 完整后端对接
- `src/pages/app/components/lovable/CodePanelLovable.vue` - VSCode 风格实现

## 🎨 UI 特性

### 当前页面：AppChatPage.vue
- ✅ Lovable 橙色主题
- ✅ 顶部导航栏（详情、历史、下载、部署）
- ✅ 预览/代码视图切换
- ✅ 左右分栏可拖拽调节
- ✅ 圆角和渐变效果

### 代码面板：CodePanelLovable.vue
- ✅ VSCode 深色主题
- ✅ 左侧文件资源管理器
- ✅ 右侧代码编辑器
- ✅ 文件标签栏
- ✅ 状态栏
- ✅ 语法高亮

## ⚡ 性能优化

1. **SSE 连接管理**
   - 使用 AbortController 控制连接
   - 组件卸载时自动清理

2. **Blob URL 管理**
   - 创建新 URL 前先清理旧的
   - 避免内存泄漏

3. **消息滚动优化**
   - 使用 nextTick 确保 DOM 更新后滚动
   - 仅在新消息时触发滚动

## 🔧 调试技巧

### 查看 SSE 连接
```javascript
// Chrome DevTools → Network → Filter: event-stream
// 查看实时数据流
```

### 测试代码生成
```javascript
// 控制台测试
console.log(simpleCodeFile.value)
console.log(multiFiles.value)
console.log(previewUrl.value)
```

---

**总结：** AppChatPage.vue 已完成核心后端接口对接，支持完整的代码生成、预览、下载和部署流程。所有功能模块化为 composables，代码结构清晰，易于维护和扩展。
