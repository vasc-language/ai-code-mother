# 图标定位 TODO

- [已完成] 通读仓库结构与前端位置
- [已完成] 查看截图识别图标与上下文
- [已完成] 全局搜索匹配的图标/文案
- [已完成] 定位具体页面与代码路径
- [已完成] 在 todo.md 记录结果路径
- [已完成] 撰写 review 总结

---

备注：按照工作流小步执行，尽量不改代码，仅做定位与记录。

## 定位结果

1) 导航「首页/主页」左侧房子图标
- 页面/组件：`ai-code-mother-frontend/src/components/GlobalHeader.vue`
- 代码位置：菜单项 `originItems` 中设置 `icon: () => h(HomeOutlined)`，图标来自 `@ant-design/icons-vue`。

2) 首页输入框右下角蓝色圆形按钮内的上箭头
- 页面：`ai-code-mother-frontend/src/pages/HomePage.vue`
- 代码位置：模板 `input-actions` 内的 `<a-button>` 自定义 `#icon` 插槽：`<template #icon><span>↑</span></template>`。

（截图为首页 Hero 区域，标题「AI 应用生成平台」，与上述 HomePage.vue 一致。）

## review
- 本次未修改业务代码，仅定位图标来源与文件路径。
- 导航房子图标：`GlobalHeader.vue` 使用 `HomeOutlined`；首页输入框按钮箭头：`HomePage.vue` 里用纯文本箭头字符。
- 若需替换为统一的 AntD 图标（如 `ArrowUpOutlined`），可在 `HomePage.vue` 引入后替换 `#icon` 插槽内容。

---

# 任务：定位“代码输出”使用的风格组件

- [已完成] 确认“代码输出”指代范围
- [已完成] 搜索并定位代码输出组件
- [已完成] 梳理样式与主题来源
- [已完成] 在 todo.md 记录结论与路径

## 结论
- 组件：`ai-code-mother-frontend/src/components/CodeHighlight.vue`（自研封装）
- 主题/风格：基于 highlight.js 的主题，默认 `atom-one-dark`，支持传参 `theme`（可选：`github` / `vs2015` / `atom-one-dark` / `monokai-sublime`），通过 CDN 动态加载对应 CSS。
- 字体与样式：深色背景 `#282c34`，等宽字体组 `Monaco/Menlo/Ubuntu Mono/Cascadia Code`，头部含文件信息与“复制”按钮（使用 Ant Design Vue 的 `a-button`）。
- 使用位置：`ai-code-mother-frontend/src/pages/app/AppChatPage.vue` 多处引用，用于展示“代码生成过程/多文件/已完成文件”等区域的代码输出。

## 修改建议（如需变更风格）
- 局部：在使用处传入 `theme` 属性（例如 `theme="github"`）。
- 全局默认：在 `CodeHighlight.vue` 中将 `currentTheme` 的默认值改为目标主题。

---

# 任务：将代码输出风格改为 GitHub 主题

- [已完成] 确认改动范围与文件
- [已完成] 替换 AppChatPage 中所有 `theme="atom-one-dark"` 为 `theme="github"`
- [已完成] 调整 `CodeHighlight.vue` 背景为透明，避免与浅色页面冲突
- [已完成] 全局搜索自检，确认无遗漏引用
- [已完成] review 总结

## 变更明细
- 修改 `ai-code-mother-frontend/src/pages/app/AppChatPage.vue` 5 处 `CodeHighlight` 组件属性：`theme="github"`
- 修改 `ai-code-mother-frontend/src/components/CodeHighlight.vue` 样式：`.code-content { background: transparent; }`

## review
- 选择 GitHub 明亮主题以贴合项目整体浅色 + 玻璃态风格；透明背景可继承父容器的浅色底（`#fafbfc` 等），视觉更统一。
- 未改动业务逻辑；主题 CSS 仍按组件内 CDN 动态加载策略工作。
- 如需进一步全局默认 GitHub 主题，可将 `currentTheme` 默认值改为 `'github'`（目前因各处均显式传参，非必须）。

---

# 新需求：基于 Qwen 生成精简项目名并应用到三处展示

- [已完成] 阅读后端创建应用流程
- [已完成] 新增 AI 命名服务接口与工厂（Qwen-Turbo）
- [已完成] 编写系统提示词文件（仅输出名称）
- [已完成] 接入创建流程并加兜底（失败回退为前 12 字）
- [已完成] 影响页面检查（我的作品/作品集/项目生成页均使用 `appName`）
- [已完成] review 总结

## 改动文件
- 后端新增：
  - `src/main/java/com/spring/aicodemother/ai/AppNameGeneratorService.java`
  - `src/main/java/com/spring/aicodemother/ai/AppNameGeneratorServiceFactory.java`
  - `src/main/resources/prompt/app-name-system-prompt.txt`
- 后端修改：
  - `src/main/java/com/spring/aicodemother/service/impl/AppServiceImpl.java`
    - 在 `createApp` 中调用 AI 命名服务生成 `appName`，失败回退到 `initPrompt` 截断

## 生效位置（无需前端改动）
- 首页「我的作品」与「精选案例」卡片标题：`AppCard.vue` 使用 `app.appName`
- 项目生成页面标题：`AppChatPage.vue` 使用 `appInfo.appName`

## review
- 使用现有 `routingChatModelPrototype`（已配置为 DashScope 兼容 OpenAI，模型 `qwen-turbo`）。
- 统一后端生成名称，前端展示自然生效；回退策略保证无 API Key 时仍可用。
- 建议在 `application.yml` 配置 `langchain4j.open-ai.routing-chat-model.api-key` 为 DashScope 的 Key；若为空则会回退。
