<template>
  <div id="appChatPageRefactored">
    <!-- 顶部操作栏 -->
    <AppHeaderBar
      :app-info="appInfo"
      :is-owner="isOwner"
      :downloading="downloading"
      :deploying="deploying"
      @show-detail="appDetailVisible = true"
      @show-version-history="handleShowVersionHistory"
      @download-code="handleDownloadCode"
      @deploy="handleDeploy"
    />

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧聊天面板 (2/5宽度) -->
      <ChatPanel
        :messages="messages"
        :messages-container="messagesContainer"
        :has-more-history="hasMoreHistory"
        :loading-history="loadingHistory"
        :user-avatar="loginUserStore.loginUser.userAvatar"
        :ai-avatar="currentAiAvatar"
        :is-edit-mode="isEditMode"
        :selected-element-info="selectedElementInfo"
        v-model:user-input="userInput"
        :is-owner="isOwner"
        :is-generating="isGenerating"
        :primary-action-disabled="primaryActionDisabled"
        :primary-action-title="primaryActionTitle"
        @load-more="loadMoreHistory"
        @clear-selected-element="clearSelectedElement"
        @primary-action="onPrimaryActionClick"
        @keydown="onInputKeydown"
      />

      <!-- 右侧代码/预览面板 (3/5宽度) -->
      <CodePreviewPanel
        :code-gen-type="appInfo?.codeGenType"
        :show-preview="generationFinished && previewReady"
        :preview-url="previewUrl"
        :is-edit-mode="isEditMode"
        :simple-code-file="simpleCodeFile"
        :multi-files="multiFiles"
        v-model:active-file-key="activeMultiFileKey"
        @toggle-edit="toggleEditMode"
        @preview-loaded="handlePreviewLoaded"
      />
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal v-model:visible="appDetailVisible" :app-info="appInfo" />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      :app-name="appInfo?.appName"
    />

    <!-- 版本历史弹窗 (保留原有实现) -->
    <!-- ... -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'

// Composables
import { useAppInfo } from './composables/useAppInfo'
import { useChatMessages } from './composables/useChatMessages'
import { useAppDeployment } from './composables/useAppDeployment'
import { useVersionManagement } from './composables/useVersionManagement'

// Components
import AppHeaderBar from './components/AppHeaderBar.vue'
import ChatPanel from './components/ChatPanel.vue'
import CodePreviewPanel from './components/CodePreviewPanel.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'

const loginUserStore = useLoginUserStore()

// 应用信息
const {
  appInfo,
  appId,
  isOwner,
  isAdmin,
  currentAiAvatar,
  fetchAppInfo,
} = useAppInfo()

// 聊天消息
const {
  messages,
  userInput,
  messagesContainer,
  loadingHistory,
  hasMoreHistory,
  historyLoaded,
  loadChatHistory,
  scrollToBottom,
  addUserMessage,
  addAiMessagePlaceholder,
  updateAiMessage,
} = useChatMessages()

// 部署管理
const { downloading, deploying, deployModalVisible, deployUrl, downloadCode, deployApp } =
  useAppDeployment()

// 版本管理
const {
  versionModalVisible,
  versionList,
  loadingVersions,
  showVersionHistory,
  handleRollback,
} = useVersionManagement()

// 应用详情
const appDetailVisible = ref(false)

// 代码生成状态 (简化版,完整版需要从原文件迁移)
const isGenerating = ref(false)
const generationFinished = ref(false)
const previewUrl = ref('')
const previewReady = ref(false)
const simpleCodeFile = ref(null)
const multiFiles = ref([])
const activeMultiFileKey = ref('')

// 可视化编辑器 (简化版)
const isEditMode = ref(false)
const selectedElementInfo = ref(null)

// 按钮状态
type BtnState = 'send' | 'stop' | 'continue' | 'disabled'
const btnState = computed<BtnState>(() => {
  if (!isOwner.value) return 'disabled'
  if (isGenerating.value) return 'stop'
  const hasInput = !!(userInput.value && userInput.value.trim())
  return hasInput ? 'send' : 'disabled'
})

const primaryActionDisabled = computed(() => btnState.value === 'disabled')
const primaryActionTitle = computed(() => {
  switch (btnState.value) {
    case 'stop':
      return '停止生成'
    case 'send':
      return '发送'
    default:
      return '请输入提示词'
  }
})

// 事件处理
const onInputKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (btnState.value === 'send') {
      sendMessage()
    }
  }
}

const onPrimaryActionClick = async () => {
  if (!isOwner.value) return
  if (btnState.value === 'send') {
    await sendMessage()
  }
  // TODO: 处理stop和continue状态
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) return

  const message = userInput.value.trim()
  userInput.value = ''

  addUserMessage(message)
  const aiMessageIndex = addAiMessagePlaceholder()

  isGenerating.value = true
  // TODO: 调用代码生成逻辑
  // await generateCode(message, aiMessageIndex)
}

const loadMoreHistory = async () => {
  // TODO: 实现加载更多
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
}

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
}

const handlePreviewLoaded = () => {
  previewReady.value = true
}

const handleShowVersionHistory = async () => {
  await showVersionHistory(appId.value)
}

const handleDownloadCode = async () => {
  await downloadCode(appId.value)
}

const handleDeploy = async () => {
  await deployApp(appId.value, async () => {
    await fetchAppInfo()
  })
}

// 生命周期
onMounted(async () => {
  await fetchAppInfo()
  // TODO: 加载聊天历史
  // TODO: 自动发送初始消息
})

onUnmounted(() => {
  // TODO: 清理SSE连接和事件监听
})
</script>

<style scoped>
#appChatPageRefactored {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f0f0;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.main-content > :first-child {
  flex: 2;
  border-right: 1px solid #e8e8e8;
}

.main-content > :last-child {
  flex: 3;
}
</style>
