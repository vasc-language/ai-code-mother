# 修复应用生成页面模型选择器功能

## 🐛 问题描述

在应用生成页面（`AppChatPage.old-optimized.vue`）中，左边框输入框左下角的模型选择器**无法切换模型**。

### 问题现象
- 用户在模型列表中选择了新模型
- 但发送消息时，仍然使用主页创建应用时选择的模型
- **模型选择器形同虚设，选择无效**

### 根本原因
`generateCode` 函数在构建请求参数时，使用的是 `appInfo.value?.modelKey`（应用创建时保存的模型），而不是用户当前选择的 `selectedModelKey.value`。

```typescript
// ❌ 修复前（第 1680 行）
const params = new URLSearchParams({
  appId: appId.value || '',
  message: userMessage,
  runId: currentRunId.value,
  modelKey: appInfo.value?.modelKey || '',  // ← 问题：使用应用保存的模型
})
```

---

## ✅ 解决方案

### 修改 1: 修复请求参数中的模型

**文件**: `src/pages/app/AppChatPage.old-optimized.vue`  
**位置**: 第 1680 行

```typescript
// ✅ 修复后
const params = new URLSearchParams({
  appId: appId.value || '',
  message: userMessage,
  runId: currentRunId.value,
  modelKey: selectedModelKey.value || appInfo.value?.modelKey || '',  // ← 修复：优先使用用户选择的模型
})
```

**优先级**:
1. **selectedModelKey.value** - 用户当前选择的模型 ⭐
2. **appInfo.value?.modelKey** - 应用创建时的模型（回退）
3. **''** - 空字符串（最后的回退）

### 修改 2: 修复AI头像图标不更新

**文件**: `src/pages/app/AppChatPage.old-optimized.vue`  
**位置**: 第 1075 行

```typescript
// ✅ 修复后：AI头像根据用户选择的模型动态变化
const currentAiAvatar = computed(() => {
  // 使用用户当前选择的模型，而不是应用创建时的模型
  const modelKey = selectedModelKey.value?.toLowerCase() || ''
  
  // 根据modelKey匹配对应的图标
  if (modelKey.includes('deepseek')) {
    return deepseekIcon
  } else if (modelKey.includes('qwen')) {
    return qwenIcon
  } else if (modelKey.includes('gpt') || modelKey.includes('o3') || modelKey.includes('o4')) {
    return openaiIcon
  } else if (modelKey.includes('kimi')) {
    return kimiIcon
  }
  
  // 默认返回通用AI头像
  return aiAvatar
})
```

**作用**:
- 聊天消息中的AI头像会根据选择的模型动态变化
- 用户可以直观看到当前使用的是哪个模型
- 提升用户体验，避免混淆

### 修改 3: 初始化时同步应用的模型

**文件**: `src/pages/app/AppChatPage.old-optimized.vue`  
**位置**: 第 1499-1504 行（新增）

```typescript
// ✅ 新增：初始化选中的模型
if (res.data.code === 0 && res.data.data) {
  appInfo.value = res.data.data

  // 初始化选中的模型为应用创建时使用的模型
  if (appInfo.value.modelKey) {
    selectedModelKey.value = appInfo.value.modelKey
  }

  // 先加载对话历史
  await loadChatHistory()
```

**作用**:
- 页面加载时，模型选择器显示应用原本使用的模型
- 用户可以看到当前正在使用哪个模型
- 提供良好的用户体验

---

## 🎯 修改摘要

| 项目 | 修改前 | 修改后 |
|-----|--------|--------|
| **请求参数模型** | 固定使用 `appInfo.modelKey` | 优先使用 `selectedModelKey.value` |
| **AI头像图标** | 固定显示创建时的模型图标 | 动态显示当前选择的模型图标 |
| **是否生效** | ❌ 选择无效 | ✅ 选择立即生效 |
| **初始值** | 默认 `'codex-mini-latest'` | 从 `appInfo.modelKey` 获取 |
| **视觉反馈** | ❌ 无变化，用户困惑 | ✅ 图标变化，清晰直观 |
| **用户体验** | 困惑：选了也没用 | 清晰：所选即所用 |

---

## 📋 工作流程对比

### 修复前的流程 ❌

```
1. 用户进入应用生成页面
   └─ selectedModelKey = 'codex-mini-latest' (默认值)

2. 用户点击模型选择器，选择 'qwen3-max'
   └─ selectedModelKey = 'qwen3-max' (已更新)

3. 用户发送消息
   └─ 请求参数: modelKey = appInfo.modelKey  ← 问题：使用旧模型
   └─ 实际使用: 'codex-mini-latest' (主页创建时的模型)

4. 结果：用户选择无效 ❌
```

### 修复后的流程 ✅

```
1. 用户进入应用生成页面
   └─ selectedModelKey = appInfo.modelKey (如 'codex-mini-latest')

2. 用户点击模型选择器，选择 'qwen3-max'
   └─ selectedModelKey = 'qwen3-max' (已更新)

3. 用户发送消息
   └─ 请求参数: modelKey = selectedModelKey.value  ← 修复：使用新模型
   └─ 实际使用: 'qwen3-max' (用户选择的模型)

4. 结果：用户选择立即生效 ✅
```

---

## 🧪 测试验证

### 1. 功能测试

```bash
# 1. 启动应用
cd ai-code-mother-frontend
npm run dev

# 2. 测试步骤
```

**测试步骤**:
1. 在主页创建一个新应用，选择模型 A（如 `codex-mini-latest`）
2. 进入应用生成页面
3. 验证左下角模型选择器显示的是模型 A ✓
4. 点击模型选择器，切换到模型 B（如 `qwen3-max`）
5. 发送一条消息
6. 查看浏览器开发者工具 Network 标签
7. 找到 `/app/chat/gen/code?` 请求
8. 验证 URL 参数中 `modelKey=qwen3-max` ✓

### 2. 控制台验证

打开浏览器控制台，应该看到：

```javascript
// 切换模型时
切换模型: qwen3-max {modelKey: "qwen3-max", ...}

// 发送消息时（Network请求）
GET /api/app/chat/gen/code?appId=xxx&message=xxx&runId=xxx&modelKey=qwen3-max
                                                              ↑
                                                    ✓ 使用了新选择的模型
```

### 3. 对比测试

| 测试场景 | 修复前 | 修复后 |
|---------|--------|--------|
| 主页创建应用用模型A | ✓ 使用A | ✓ 使用A |
| 页面显示初始模型 | ✗ 显示默认模型 | ✓ 显示A |
| 切换到模型B发送消息 | ✗ 仍使用A | ✓ 使用B |
| 再次切换到模型C | ✗ 仍使用A | ✓ 使用C |
| 刷新页面 | ✗ 显示默认模型 | ✓ 显示A |

---

## 📝 代码差异

### 1. generateCode 函数
```diff
  const params = new URLSearchParams({
    appId: appId.value || '',
    message: userMessage,
    runId: currentRunId.value,
-   modelKey: appInfo.value?.modelKey || '',
+   modelKey: selectedModelKey.value || appInfo.value?.modelKey || '',
  })
```

### 2. currentAiAvatar computed 属性
```diff
  const currentAiAvatar = computed(() => {
-   const modelKey = appInfo.value?.modelKey?.toLowerCase() || ''
+   // 使用用户当前选择的模型，而不是应用创建时的模型
+   const modelKey = selectedModelKey.value?.toLowerCase() || ''
    
    // 根据modelKey匹配对应的图标
    if (modelKey.includes('deepseek')) {
      return deepseekIcon
    }
    // ...
  })
```

### 3. fetchAppInfo 函数
```diff
  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

+     // 初始化选中的模型为应用创建时使用的模型
+     if (appInfo.value.modelKey) {
+       selectedModelKey.value = appInfo.value.modelKey
+     }

      // 先加载对话历史
      await loadChatHistory()
```

---

## 🔄 对比主页实现

### 主页 (HomePage.vue)

```typescript
// ✓ 主页正确实现
const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  showModelSelector.value = false
}

const doCreateApp = async () => {
  const res = await addApp({
    initPrompt: userPrompt.value.trim(),
    modelKey: selectedModelKey.value,  // ← 正确传递选择的模型
  })
}
```

### 应用生成页 (修复后)

```typescript
// ✓ 应用生成页修复后与主页一致
const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  showModelSelector.value = false
  console.log('切换模型:', modelKey, model)
}

const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  const params = new URLSearchParams({
    appId: appId.value || '',
    message: userMessage,
    runId: currentRunId.value,
    modelKey: selectedModelKey.value || appInfo.value?.modelKey || '',  // ← 修复：使用选择的模型
  })
}
```

---

## ✅ 检查清单

修复后，请验证以下功能：

- [ ] 进入应用生成页面时，模型选择器显示正确的初始模型
- [ ] 点击模型选择器可以看到完整的模型列表
- [ ] 选择新模型后，模型选择器图标更新为新模型
- [ ] 发送消息时，Network请求中的 `modelKey` 参数为选择的模型
- [ ] 切换多次模型，每次都能正确使用新选择的模型
- [ ] 刷新页面后，模型选择器恢复为应用创建时的模型
- [ ] 不同的应用可以使用不同的模型

---

## 📦 相关文件

- `src/pages/app/AppChatPage.old-optimized.vue` - 修复的主文件
- `src/pages/HomePage.vue` - 参考实现
- `src/components/AiModelSelector.vue` - 模型选择器组件

---

## 🎉 总结

### 修复内容
1. ✅ 修改 `generateCode` 函数，优先使用 `selectedModelKey.value`
2. ✅ 修改 `currentAiAvatar` computed，动态显示当前选择的模型图标
3. ✅ 修改 `fetchAppInfo` 函数，初始化时同步模型

### 影响范围
- ✅ 仅影响应用生成页面的模型切换功能
- ✅ 不影响主页的模型选择功能
- ✅ 不影响已有应用的数据

### 用户体验
- ✅ 模型选择器现在可以正常工作
- ✅ 用户可以随时切换模型进行对话
- ✅ 页面初始化时显示正确的当前模型
- ✅ **AI头像图标会实时变化，提供视觉反馈** ⭐ 新增

---

**修复时间**: 2025-01-XX  
**修复文件**: 1 个  
**修改行数**: +6 行  
**测试状态**: ✅ 待测试
