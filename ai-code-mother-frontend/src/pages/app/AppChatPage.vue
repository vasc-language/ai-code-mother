<template>
  <div class="lovable-app-chat-page">
    <!-- 顶部导航栏 -->
    <header class="lovable-header">
      <div class="header-left">
        <router-link to="/" class="logo-link">
          <img src="/favicon.ico" alt="Logo" class="app-logo" />
        </router-link>
        <div class="app-info">
          <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
          <span v-if="appInfo?.codeGenType" class="code-gen-badge">
            {{ formatCodeGenType(appInfo.codeGenType) }}
          </span>
        </div>
      </div>
      <div class="header-actions">
        <!-- 视图切换按钮组 -->
        <div class="view-toggle-group">
          <button
            class="view-toggle-btn"
            :class="{ active: currentView === 'preview' }"
            @click="currentView = 'preview'"
            title="预览"
          >
            <EyeOutlined />
          </button>
          <button
            class="view-toggle-btn"
            :class="{ active: currentView === 'code' }"
            @click="currentView = 'code'"
            title="代码"
          >
            <CodeOutlined />
          </button>
        </div>

        <!-- 分隔线 -->
        <div class="header-divider"></div>

        <button class="header-btn" @click="showAppDetail" title="应用详情">
          <InfoCircleOutlined />
          <span>详情</span>
        </button>
        <button v-if="isOwner" class="header-btn" @click="showVersionHistory" title="历史版本">
          <HistoryOutlined />
          <span>历史</span>
        </button>
        <button
          class="header-btn"
          @click="downloadCode"
          :disabled="!isOwner || downloading"
          title="下载代码"
        >
          <DownloadOutlined />
          <span>{{ downloading ? '下载中...' : '下载' }}</span>
        </button>
        <button
          class="header-btn header-btn-primary"
          @click="deployApp"
          :disabled="deploying"
          title="部署应用"
        >
          <CloudUploadOutlined />
          <span>{{ deploying ? '部署中...' : '部署' }}</span>
        </button>
      </div>
    </header>

    <!-- 主内容区 - 分屏布局 -->
    <main class="lovable-main">
      <!-- 左侧：聊天面板 (ChatGPT 风格) -->
      <div class="left-panel chat-panel-container">
        <ChatPanelLovable
          :messages="messages"
          :hasMoreHistory="hasMoreHistory"
          :loadingHistory="loadingHistory"
          :userAvatar="loginUserStore.loginUser.userAvatar"
          :aiAvatar="currentAiAvatar"
          :selectedElementInfo="selectedElementInfo"
          v-model:userInput="userInput"
          :isOwner="isOwner"
          :isGenerating="isGenerating"
          :primaryActionDisabled="primaryActionDisabled"
          :primaryActionTitle="primaryActionTitle"
          :defaultModelKey="selectedModelKey"
          @load-more="loadMoreHistory"
          @clear-selected-element="clearSelectedElement"
          @primary-action="onPrimaryActionClick"
          @keydown="onInputKeydown"
          @model-change="handleModelChange"
        />
      </div>

      <!-- 中间：可调节分隔条 -->
      <div class="resizer" @mousedown="startResize"></div>

      <!-- 右侧：代码编辑器/预览面板 (VS Code 风格) -->
      <div class="right-panel code-panel-container">
        <CodePanelLovable
          :currentView="currentView"
          :codeGenType="appInfo?.codeGenType"
          :showPreview="generationFinished"
          :previewUrl="previewUrl"
          :simpleCodeFile="simpleCodeFile"
          :multiFiles="completedFiles"
          :activeFileKey="activeFileKey"
          :generationFinished="generationFinished"
          @preview-loaded="onPreviewLoaded"
          @update:activeFileKey="activeFileKey = $event"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  HistoryOutlined,
  DownloadOutlined,
  CloudUploadOutlined,
  EyeOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'

// 导入 composables
import { useAppInfo } from './composables/useAppInfo'
import { useChatMessages } from './composables/useChatMessages'
import { useCodeGeneration } from './composables/useCodeGeneration'
import { useAppDeployment } from './composables/useAppDeployment'
import { useVisualEditor } from './composables/useVisualEditor'

// 导入内容过滤函数
import {
  filterHtmlContent,
  filterOutCodeBlocks,
  formatVueProjectContent,
} from './utils/contentFilters'

// 导入 Lovable 风格组件
import ChatPanelLovable from './components/lovable/ChatPanelLovable.vue'
import CodePanelLovable from './components/lovable/CodePanelLovable.vue'

// 导入用户状态
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const loginUserStore = useLoginUserStore()

// ========== 应用信息 ==========
const { appInfo, appId, isOwner, currentAiAvatar, fetchAppInfo } = useAppInfo()

// ========== 聊天消息 ==========
const {
  messages,
  userInput,
  hasMoreHistory,
  loadingHistory,
  historyLoaded,
  loadChatHistory,
  addUserMessage,
  addAiMessagePlaceholder,
  updateAiMessage,
  scrollToBottom,
} = useChatMessages()

// ========== 代码生成 ==========
const {
  isGenerating,
  generationFinished,
  simpleCodeFile,
  multiFiles,
  activeMultiFileKey,
  previewUrl,
  previewReady,
  startCodeGeneration,
  stopGeneration,
  cleanup: cleanupCodeGeneration,
} = useCodeGeneration()

// ========== 部署管理 ==========
const {
  downloading,
  deploying,
  deployModalVisible,
  deployUrl,
  downloadCode: downloadCodeAction,
  deployApp: deployAppAction,
} = useAppDeployment()

// ========== 可视化编辑器 ==========
const {
  isEditMode,
  selectedElementInfo,
  toggleEditMode,
  clearSelectedElement,
  getInputWithElementContext,
  afterMessageSent,
} = useVisualEditor()

// ========== UI 状态 ==========
const currentView = ref<'preview' | 'code'>('preview')
const completedFiles = computed(() => multiFiles.value)
const activeFileKey = computed({
  get: () => activeMultiFileKey.value,
  set: (val) => (activeMultiFileKey.value = val),
})

// ========== AI 模型选择 ==========
const selectedModelKey = ref<string>('codex-mini-latest')
const selectedModel = ref<API.AiModelConfig | null>(null)

const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  selectedModel.value = model
  // console.log('[AppChatPage] 模型切换:', modelKey, model)  // 生产环境可注释
}

// ========== 按钮状态 ==========
const primaryActionDisabled = computed(() => !userInput.value.trim() || isGenerating.value)
const primaryActionTitle = computed(() => (isGenerating.value ? '停止' : '发送'))

// ========== 工具函数 ==========
const formatCodeGenType = (type: string) => {
  const typeMap: Record<string, string> = {
    HTML: 'HTML',
    MULTI_FILE: '多文件',
    VUE_PROJECT: 'Vue项目',
  }
  return typeMap[type] || type
}

// ========== 事件处理 ==========
const showAppDetail = () => {
  message.info('应用详情功能')
}

const showVersionHistory = () => {
  message.info('版本历史功能')
}

const downloadCode = async () => {
  await downloadCodeAction(appId.value)
}

const deployApp = async () => {
  await deployAppAction(appId.value, async () => {
    await fetchAppInfo()
  })
}

/**
 * 加载更多历史消息
 */
const loadMoreHistory = async () => {
  await loadChatHistory(
    appId.value,
    appInfo.value?.codeGenType,
    true, // isLoadMore
    {
      filterHtmlContent,
      filterOutCodeBlocks,
      formatVueProjectContent,
    }
  )
}

/**
 * 切换编辑模式（用于右侧代码面板的"编辑"按钮）
 */
const handleToggleEditMode = () => {
  toggleEditMode()
  if (isEditMode.value) {
    message.info('编辑模式已启用，点击预览中的元素进行选择')
  } else {
    message.info('编辑模式已关闭')
  }
}

/**
 * 主要操作：发送消息或停止生成
 */
const onPrimaryActionClick = async () => {
  if (isGenerating.value) {
    // 停止生成
    stopGeneration()
  } else {
    // 发送消息并开始生成
    await sendMessage()
  }
}

/**
 * 发送消息并启动 SSE 代码生成
 */
const sendMessage = async () => {
  const rawMessage = userInput.value.trim()
  if (!rawMessage || isGenerating.value) return

  // ✅ 使用可视化编辑器增强输入（如果有选中元素）
  const enhancedMessage = isEditMode.value
    ? getInputWithElementContext(rawMessage)
    : rawMessage

  // 清空输入框
  userInput.value = ''

  // 添加用户消息到聊天列表（显示原始消息）
  addUserMessage(rawMessage)

  // 添加AI消息占位符
  const aiMessageIndex = addAiMessagePlaceholder()

  // ✅ 清理可视化编辑器状态
  afterMessageSent()

  // 开始代码生成（使用增强后的消息）
  await startCodeGeneration(
    appId.value,
    enhancedMessage,
    appInfo.value?.codeGenType,
    // 更新AI消息回调
    (content: string) => {
      updateAiMessage(aiMessageIndex, content, true)
      scrollToBottom()
    },
    // 完成回调
    () => {
      updateAiMessage(aiMessageIndex, messages.value[aiMessageIndex].content, false)
      scrollToBottom()
    },
    // 传入用户选中的 modelKey（优先使用），如果未选择则使用应用默认的
    selectedModelKey.value || appInfo.value?.modelKey
  )
}

const onInputKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (!primaryActionDisabled.value) {
      onPrimaryActionClick()
    }
  }
}

const onPreviewLoaded = () => {
  console.log('预览加载完成')
}

// ========== 分隔栏调整 ==========
const isResizing = ref(false)
const startResize = (e: MouseEvent) => {
  isResizing.value = true
  document.addEventListener('mousemove', doResize)
  document.addEventListener('mouseup', stopResize)
}

const doResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  const container = document.querySelector('.lovable-main') as HTMLElement
  if (!container) return

  const containerRect = container.getBoundingClientRect()
  const leftWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100

  // 限制宽度范围：25% - 75%
  if (leftWidth >= 25 && leftWidth <= 75) {
    const leftPanel = document.querySelector('.left-panel') as HTMLElement
    const rightPanel = document.querySelector('.right-panel') as HTMLElement
    if (leftPanel && rightPanel) {
      leftPanel.style.width = `${leftWidth}%`
      rightPanel.style.width = `${100 - leftWidth}%`
    }
  }
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', doResize)
  document.removeEventListener('mouseup', stopResize)
}

// ========== 生命周期 ==========
onMounted(async () => {
  await fetchAppInfo()

  // ✅ 初始化选中的模型为应用的默认模型
  if (appInfo.value?.modelKey) {
    selectedModelKey.value = appInfo.value.modelKey
    // console.log('[AppChatPage] 初始化模型:', appInfo.value.modelKey)  // 调试用
  }

  // ✅ 加载聊天历史
  if (!historyLoaded.value) {
    await loadChatHistory(
      appId.value,
      appInfo.value?.codeGenType,
      false, // 初始加载
      {
        filterHtmlContent,
        filterOutCodeBlocks,
        formatVueProjectContent,
      }
    )
  }
})

onUnmounted(() => {
  cleanupCodeGeneration()
})
</script>

<style scoped>
/* 导入 Lovable 主题 */
@import '@/styles/lovable-theme.css';

.lovable-app-chat-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  color: var(--text-primary);
  overflow: hidden;
}

/* ========== 顶部导航栏 - Lovable 风格 ========== */
.lovable-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 var(--spacing-xl);
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  backdrop-filter: blur(20px);
  border-bottom: none;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.08);
  z-index: var(--z-sticky);
  border-radius: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  transition: all 0.3s ease;
}

.logo-link:hover {
  transform: scale(1.05);
}

.app-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: contain;
}

.app-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.app-name {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.code-gen-badge {
  padding: 6px 16px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  border-radius: 30px;
  font-size: var(--text-xs);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.35);
  letter-spacing: 0.5px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* 视图切换按钮组 */
.view-toggle-group {
  display: flex;
  gap: 6px;
  padding: 4px;
  background: white;
  border-radius: 30px;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.08);
}

.view-toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: transparent;
  color: var(--text-tertiary);
  border: none;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.view-toggle-btn:hover:not(.active) {
  background: rgba(255, 107, 53, 0.08);
  color: #ff6b35;
  transform: scale(1.05);
}

.view-toggle-btn.active {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.35);
}

/* 分隔线 */
.header-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 107, 53, 0.15);
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 20px;
  background: white;
  color: #2d3748;
  border: 2px solid rgba(255, 107, 53, 0.1);
  border-radius: 30px;
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.08);
}

.header-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  border-color: rgba(255, 107, 53, 0.3);
  color: #ff6b35;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.15);
}

.header-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.header-btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  border: none;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.35);
}

.header-btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b35 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 10px 24px rgba(255, 107, 53, 0.45);
  color: white;
}

/* ========== 主内容区 ========== */
.lovable-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* ========== 主内容区 - 更新后 ========== */
.left-panel {
  width: 40%;
  height: 100%;
  min-width: 320px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-primary);
  overflow: hidden;
  transition: width 0.1s ease;
}

/* 中间分隔条 */
.resizer {
  width: 8px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 140, 66, 0.12) 100%);
  cursor: col-resize;
  position: relative;
  transition: all var(--transition-base) cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow:
    0 0 20px rgba(255, 107, 53, 0.06),
    inset 0 0 10px rgba(255, 107, 53, 0.03);
}

.resizer:hover {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 140, 66, 0.2) 100%);
  box-shadow:
    0 0 30px rgba(255, 107, 53, 0.12),
    inset 0 0 15px rgba(255, 107, 53, 0.08);
  transform: scaleX(1.2);
}

.resizer::before {
  content: '';
  position: absolute;
  top: 0;
  left: -4px;
  right: -4px;
  bottom: 0;
}

.resizer::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 3px;
  height: 40px;
  background: linear-gradient(180deg,
    rgba(255, 107, 53, 0.3) 0%,
    rgba(255, 140, 66, 0.5) 50%,
    rgba(255, 107, 53, 0.3) 100%);
  border-radius: 10px;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.resizer:hover::after {
  opacity: 1;
}

/* 左侧面板 - 添加圆角 */
.left-panel {
  width: 40%;
  height: 100%;
  min-width: 320px;
  background: var(--bg-primary);
  border-right: none;
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  overflow: hidden;
  transition: width 0.1s ease;
  box-shadow: var(--shadow-md);
}

/* 右侧面板 - 添加圆角 */
.right-panel {
  width: 60%;
  height: 100%;
  min-width: 480px;
  background: var(--code-bg);
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  overflow: hidden;
  transition: width 0.1s ease;
  box-shadow: var(--shadow-md);
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
  .left-panel {
    width: 50%;
    min-width: 280px;
  }

  .right-panel {
    width: 50%;
    min-width: 280px;
  }

  .header-btn span {
    display: none;
  }

  .header-btn {
    padding: 0 12px;
    min-width: 36px;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .lovable-main {
    flex-direction: column;
  }

  .left-panel,
  .right-panel {
    width: 100% !important;
    height: 50%;
    min-width: unset;
    min-height: 200px;
  }

  .resizer {
    width: 100%;
    height: 4px;
    cursor: row-resize;
  }

  .header-actions {
    gap: 4px;
  }

  .app-name {
    font-size: var(--text-base);
  }
}

/* ========== 用户选择禁用（调整大小时） ========== */
body.resizing * {
  user-select: none !important;
  cursor: col-resize !important;
}
</style>
