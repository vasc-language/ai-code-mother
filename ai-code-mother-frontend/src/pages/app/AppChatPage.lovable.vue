<template>
  <div class="lovable-app-chat-page">
    <!-- 顶部导航栏 -->
    <header class="lovable-header">
      <div class="header-left">
        <div class="app-info">
          <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
          <span v-if="appInfo?.codeGenType" class="code-gen-badge">
            {{ formatCodeGenType(appInfo.codeGenType) }}
          </span>
        </div>
      </div>
      <div class="header-actions">
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
          :isEditMode="isEditMode"
          :selectedElementInfo="selectedElementInfo"
          v-model:userInput="userInput"
          :isOwner="isOwner"
          :isGenerating="isGenerating"
          :primaryActionDisabled="primaryActionDisabled"
          :primaryActionTitle="primaryActionTitle"
          @load-more="loadMoreHistory"
          @clear-selected-element="clearSelectedElement"
          @primary-action="onPrimaryActionClick"
          @keydown="onInputKeydown"
        />
      </div>

      <!-- 中间：可调节分隔条 -->
      <div class="resizer" @mousedown="startResize"></div>

      <!-- 右侧：代码编辑器/预览面板 (VS Code 风格) -->
      <div class="right-panel code-panel-container">
        <CodePanelLovable
          :codeGenType="appInfo?.codeGenType"
          :showPreview="generationFinished"
          :previewUrl="previewUrl"
          :isEditMode="isEditMode"
          :simpleCodeFile="simpleCodeFile"
          :multiFiles="completedFiles"
          :activeFileKey="activeFileKey"
          :generationFinished="generationFinished"
          @toggle-edit="toggleEditMode"
          @preview-loaded="onPreviewLoaded"
          @update:activeFileKey="activeFileKey = $event"
          @clear-files="clearAllFiles"
          @open-preview="openInNewTab"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  HistoryOutlined,
  DownloadOutlined,
  CloudUploadOutlined,
} from '@ant-design/icons-vue'

// 导入已有的 composables
import { useAppInfo } from './composables/useAppInfo'
import { useChatMessages } from './composables/useChatMessages'

// 导入 Lovable 风格组件
import ChatPanelLovable from './components/lovable/ChatPanelLovable.vue'
import CodePanelLovable from './components/lovable/CodePanelLovable.vue'

// 导入用户状态
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const loginUserStore = useLoginUserStore()

// 应用信息
const { appInfo, isOwner, currentAiAvatar, fetchAppInfo } = useAppInfo()

// 聊天消息
const { messages, userInput, hasMoreHistory, loadingHistory } = useChatMessages()

// 部署功能 - 暂时使用简单实现
const downloading = ref(false)
const deploying = ref(false)

// 其他状态
const isGenerating = ref(false)
const isEditMode = ref(false)
const selectedElementInfo = ref<any>(null)
const primaryActionDisabled = computed(() => !userInput.value.trim() || isGenerating.value)
const primaryActionTitle = computed(() => isGenerating.value ? '停止' : '发送')

// 方法实现
const formatCodeGenType = (type: string) => {
  const typeMap: Record<string, string> = {
    'HTML': 'HTML',
    'MULTI_FILE': '多文件',
    'VUE_PROJECT': 'Vue项目'
  }
  return typeMap[type] || type
}

const showAppDetail = () => {
  message.info('应用详情功能')
}

const showVersionHistory = () => {
  message.info('版本历史功能')
}

const downloadCode = () => {
  downloading.value = true
  setTimeout(() => {
    downloading.value = false
    message.success('下载成功')
  }, 1000)
}

const deployApp = () => {
  deploying.value = true
  setTimeout(() => {
    deploying.value = false
    message.success('部署成功')
  }, 1000)
}

const loadMoreHistory = () => {
  message.info('加载更多历史')
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
}

const onPrimaryActionClick = () => {
  if (isGenerating.value) {
    isGenerating.value = false
    message.info('已停止生成')
  } else {
    // 发送消息
    if (userInput.value.trim()) {
      message.success('消息已发送')
      userInput.value = ''
    }
  }
}

const onInputKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    onPrimaryActionClick()
  }
}

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
}

const onPreviewLoaded = () => {
  console.log('预览加载完成')
}

// 代码生成相关
const generationFinished = ref(false)
const previewUrl = ref('')
const simpleCodeFile = ref<any>(null)
const completedFiles = ref<any[]>([])
const activeFileKey = ref('')

// 清空文件
const clearAllFiles = () => {
  completedFiles.value = []
  simpleCodeFile.value = null
  message.success('文件已清空')
}

// 在新标签页打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 分隔栏调整
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

// 初始化
onMounted(async () => {
  await fetchAppInfo()
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

/* ========== 顶部导航栏 ========== */
.lovable-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 var(--spacing-lg);
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border-primary);
  box-shadow: var(--shadow-md);
  z-index: var(--z-sticky);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
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
  padding: 4px 12px;
  background: var(--color-primary);
  color: var(--text-inverse);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 var(--spacing-md);
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition-fast);
  white-space: nowrap;
}

.header-btn:hover:not(:disabled) {
  background: var(--bg-tertiary);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.header-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.header-btn-primary {
  background: var(--color-primary);
  color: var(--text-inverse);
  border-color: var(--color-primary);
}

.header-btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
  color: var(--text-inverse);
}

/* ========== 主内容区 ========== */
.lovable-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 左侧聊天面板 */
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
  width: 4px;
  background: var(--border-primary);
  cursor: col-resize;
  position: relative;
  transition: background var(--transition-fast);
}

.resizer:hover {
  background: var(--color-primary);
}

.resizer::before {
  content: '';
  position: absolute;
  top: 0;
  left: -4px;
  right: -4px;
  bottom: 0;
}

/* 右侧代码面板 */
.right-panel {
  width: 60%;
  height: 100%;
  min-width: 480px;
  background: var(--code-bg);
  overflow: hidden;
  transition: width 0.1s ease;
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
