<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
        <a-tag v-if="appInfo?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ formatCodeGenType(appInfo.codeGenType) }}
        </a-tag>
      </div>
      <div class="header-right">
        <a-button type="default" @click="showAppDetail">
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button
          type="primary"
          ghost
          @click="downloadCode"
          :loading="downloading"
          :disabled="!isOwner"
        >
          <template #icon>
            <DownloadOutlined />
          </template>
          下载代码
        </a-button>
        <a-button type="primary" @click="deployApp" :loading="deploying">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button type="link" @click="loadMoreHistory" :loading="loadingHistory" size="small">
              加载更多历史消息
            </a-button>
          </div>
          <div v-for="(message, index) in messages" :key="index" class="message-item">
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-avatar">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              </div>
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <a-avatar :src="aiAvatar" />
              </div>
              <div class="message-content">
                <!-- 只显示非代码块内容，代码块在右侧显示 -->
                <MarkdownRenderer v-if="message.content" :content="filterOutCodeBlocks(message.content)" />
                <div v-if="message.loading" class="loading-indicator">
                  <a-spin size="small" />
                  <span>AI 正在思考...</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 工具步骤显示区域 -->
          <div v-if="generationSteps.length > 0" class="steps-section">
            <div class="steps-header">
              <h4>AI 操作步骤</h4>
            </div>
            <div class="steps-container">
              <div 
                v-for="step in generationSteps" 
                :key="step.id" 
                class="step-item"
                :class="{ 'step-running': step.status === 'running', 'step-completed': step.status === 'completed' }"
              >
                <div class="step-header">
                  <span class="step-number">STEP {{ step.number }}</span>
                  <span class="step-title">{{ step.title }}</span>
                  <a-badge :status="getStepStatus(step)" />
                </div>
                
                <!-- 工具调用列表 -->
                <div v-if="step.toolCalls && step.toolCalls.length > 0" class="tool-calls">
                  <div 
                    v-for="call in step.toolCalls" 
                    :key="call.id" 
                    class="tool-call-item"
                  >
                    <div class="tool-selection">
                      <a-tag :color="getToolColor(call.toolType)">{{ call.toolType }}</a-tag>
                    </div>
                    <div class="tool-execution">
                      <span class="tool-action">{{ call.action }}</span>
                      <span class="file-path">{{ call.filePath }}</span>
                      <p class="operation-desc">{{ call.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 选中元素信息展示 -->
        <a-alert
          v-if="selectedElementInfo"
          class="selected-element-alert"
          type="info"
          closable
          @close="clearSelectedElement"
        >
          <template #message>
            <div class="selected-element-info">
              <div class="element-header">
                <span class="element-tag">
                  选中元素：{{ selectedElementInfo.tagName.toLowerCase() }}
                </span>
                <span v-if="selectedElementInfo.id" class="element-id">
                  #{{ selectedElementInfo.id }}
                </span>
                <span v-if="selectedElementInfo.className" class="element-class">
                  .{{ selectedElementInfo.className.split(' ').join('.') }}
                </span>
              </div>
              <div class="element-details">
                <div v-if="selectedElementInfo.textContent" class="element-item">
                  内容: {{ selectedElementInfo.textContent.substring(0, 50) }}
                  {{ selectedElementInfo.textContent.length > 50 ? '...' : '' }}
                </div>
                <div v-if="selectedElementInfo.pagePath" class="element-item">
                  页面路径: {{ selectedElementInfo.pagePath }}
                </div>
                <div class="element-item">
                  选择器:
                  <code class="element-selector-code">{{ selectedElementInfo.selector }}</code>
                </div>
              </div>
            </div>
          </template>
        </a-alert>

        <!-- 用户消息输入框 -->
        <div class="input-container">
          <div class="input-wrapper">
            <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea
                v-model:value="userInput"
                :placeholder="getInputPlaceholder()"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating || !isOwner"
              />
            </a-tooltip>
            <a-textarea
              v-else
              v-model:value="userInput"
              :placeholder="getInputPlaceholder()"
              :rows="4"
              :maxlength="1000"
              @keydown.enter.prevent="sendMessage"
              :disabled="isGenerating"
            />
            <div class="input-actions">
              <a-button
                type="primary"
                @click="sendMessage"
                :loading="isGenerating"
                :disabled="!isOwner"
              >
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 右侧代码生成展示区域 -->
      <div class="code-generation-section">
        <div class="section-header">
          <h3>代码生成过程</h3>
          <div class="header-actions">
            <a-button 
              v-if="completedFiles.length > 0" 
              type="link" 
              @click="clearAllFiles"
              size="small"
            >
              清空文件
            </a-button>
            <a-button 
              v-if="previewUrl" 
              type="link" 
              @click="openInNewTab"
              size="small"
            >
              <template #icon>
                <ExportOutlined />
              </template>
              预览网站
            </a-button>
          </div>
        </div>
        
        <div class="code-output-container">
          <!-- 当前生成的文件 -->
          <div v-if="currentGeneratingFile" class="current-file">
            <div class="file-header">
              <div class="file-tab">
                <FileOutlined class="file-icon" />
                <span class="file-name">{{ currentGeneratingFile.name }}</span>
                <a-button 
                  type="link" 
                  size="small" 
                  @click="minimizeCurrentFile"
                  v-if="currentGeneratingFile.completed"
                >
                  <MinusOutlined />
                </a-button>
              </div>
            </div>
            <div class="code-content">
              <pre class="code-stream"><code>{{ currentGeneratingFile.content }}</code></pre>
              <div class="typing-cursor" v-if="!currentGeneratingFile.completed">|</div>
            </div>
          </div>

          <!-- 已完成的文件列表 -->
          <div class="completed-files">
            <a-collapse v-model:activeKey="activeFileKeys" v-if="completedFiles.length > 0">
              <a-collapse-panel 
                v-for="file in completedFiles" 
                :key="file.id"
              >
                <template #header>
                  <div class="file-panel-header">
                    <FileOutlined class="file-icon" />
                    <span class="file-name">{{ file.name }}</span>
                    <span class="file-path">{{ file.path }}</span>
                  </div>
                </template>
                <div class="file-content-wrapper">
                  <pre class="code-content"><code>{{ file.content }}</code></pre>
                </div>
              </a-collapse-panel>
            </a-collapse>
          </div>

          <!-- 占位符 -->
          <div v-if="!currentGeneratingFile && completedFiles.length === 0 && !isGenerating" class="code-placeholder">
            <div class="placeholder-icon">📄</div>
            <p>AI 生成的代码文件将在这里实时显示</p>
          </div>
          
          <div v-else-if="!currentGeneratingFile && completedFiles.length === 0 && isGenerating" class="code-loading">
            <a-spin size="large" />
            <p>正在分析需求，准备生成代码...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="appInfo"
      :show-actions="isOwner || isAdmin"
      @edit="editApp"
      @delete="deleteApp"
    />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      @open-site="openDeployedSite"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  getAppVoById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
} from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenTypes'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import aiAvatar from '@/assets/aiAvatar.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'

import {
  CloudUploadOutlined,
  SendOutlined,
  ExportOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
  EditOutlined,
  FileOutlined,
  MinusOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<any>()

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
}

// 工具步骤相关
interface GenerationStep {
  id: string
  number: number
  title: string
  status: 'pending' | 'running' | 'completed' | 'error'
  toolCalls?: ToolCall[]
  startTime?: string
  endTime?: string
}

interface ToolCall {
  id: string
  toolType: '写入文件' | '删除文件' | '读取目录' | '修改文件' | '读取文件'
  action: string
  filePath: string
  description: string
  status: 'pending' | 'running' | 'completed'
  timestamp?: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 工具步骤相关状态
const generationSteps = ref<GenerationStep[]>([])
const currentStep = ref<GenerationStep | null>(null)

// 代码生成文件相关状态
interface GeneratedFile {
  id: string
  name: string
  path: string
  content: string
  language: string
  completed: boolean
  generatedAt: string
  lastUpdated?: string
}

const currentGeneratingFile = ref<GeneratedFile | null>(null)
const completedFiles = ref<GeneratedFile[]>([])
const activeFileKeys = ref<string[]>([])

// 对话历史相关
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>()
const historyLoaded = ref(false)

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 下载相关
const downloading = ref(false)

// 可视化编辑相关
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo: ElementInfo) => {
    selectedElementInfo.value = elementInfo
  },
})

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 应用详情相关
const appDetailVisible = ref(false)

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 加载对话历史
const loadChatHistory = async (isLoadMore = false) => {
  if (!appId.value || loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10,
    }
    // 如果是加载更多，传递最后一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []
      if (chatHistories.length > 0) {
        // 将对话历史转换为消息格式，并按时间正序排列（老消息在前）
        const historyMessages: Message[] = chatHistories
          .map((chat) => ({
            type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
            content: chat.message || '',
            createTime: chat.createTime,
          }))
          .reverse() // 反转数组，让老消息在前
        if (isLoadMore) {
          // 加载更多时，将历史消息添加到开头
          messages.value.unshift(...historyMessages)
        } else {
          // 初始加载，直接设置消息列表
          messages.value = historyMessages
        }
        // 更新游标
        lastCreateTime.value = chatHistories[chatHistories.length - 1]?.createTime
        // 检查是否还有更多历史
        hasMoreHistory.value = chatHistories.length === 10
      } else {
        hasMoreHistory.value = false
      }
      historyLoaded.value = true
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 先加载对话历史
      await loadChatHistory()
      // 如果有至少2条对话记录，展示对应的网站
      if (messages.value.length >= 2) {
        updatePreview()
      }
      // 检查是否需要自动发送初始提示词
      // 只有在是自己的应用且没有对话历史时才自动发送
      if (
        appInfo.value.initPrompt &&
        isOwner.value &&
        messages.value.length === 0 &&
        historyLoaded.value
      ) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) {
    return
  }

  let message = userInput.value.trim()
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElementInfo.value) {
    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }
    message += elementContext
  }
  userInput.value = ''
  // 添加用户消息（包含元素信息）
  messages.value.push({
    type: 'user',
    content: message,
  })

  // 发送消息后，清除选中元素并退出编辑模式
  if (selectedElementInfo.value) {
    clearSelectedElement()
    if (isEditMode.value) {
      toggleEditMode()
    }
  }

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          
          // 解析流式内容并更新右侧代码生成区域
          parseStreamingContent(content, fullContent)
          
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })

    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function (event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'
        messages.value[aiMessageIndex].content = `❌ ${errorMessage}`
        messages.value[aiMessageIndex].loading = false
        message.error(errorMessage)

        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })


    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
    const newPreviewUrl = getStaticPreviewUrl(codeGenType, appId.value)
    previewUrl.value = newPreviewUrl
    previewReady.value = true
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 下载代码
const downloadCode = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    message.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    message.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    const res = await deployAppApi({
      appId: appId.value as unknown as number,
    })

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      message.success('部署成功')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败：', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe) {
    visualEditor.init(iframe)
    visualEditor.onIframeLoad()
  }
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 可视化编辑相关函数
const toggleEditMode = () => {
  // 检查 iframe 是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保 visualEditor 已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }
  const newEditMode = visualEditor.toggleEditMode()
  isEditMode.value = newEditMode
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const getInputPlaceholder = () => {
  if (selectedElementInfo.value) {
    return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改...`
  }
  return '请描述你想生成的网站，越详细效果越好哦'
}

// 工具步骤相关函数
const getStepStatus = (step: GenerationStep): 'default' | 'processing' | 'success' | 'error' => {
  switch (step.status) {
    case 'pending': return 'default'
    case 'running': return 'processing'
    case 'completed': return 'success'
    case 'error': return 'error'
    default: return 'default'
  }
}

const getToolColor = (toolType: string): string => {
  const colorMap: Record<string, string> = {
    '写入文件': 'blue',
    '读取文件': 'green', 
    '修改文件': 'orange',
    '删除文件': 'red',
    '读取目录': 'purple'
  }
  return colorMap[toolType] || 'default'
}

// 代码生成相关函数
const minimizeCurrentFile = () => {
  if (currentGeneratingFile.value && currentGeneratingFile.value.completed) {
    // 将当前文件移动到已完成列表
    completedFiles.value.push(currentGeneratingFile.value)
    activeFileKeys.value = [currentGeneratingFile.value.id] // 自动展开这个文件
    currentGeneratingFile.value = null
  }
}

const clearAllFiles = () => {
  completedFiles.value = []
  currentGeneratingFile.value = null
  activeFileKeys.value = []
}

const extractFileName = (filePath: string): string => {
  if (!filePath) return '未知文件'
  return filePath.split(/[/\\]/).pop() || filePath
}

const detectLanguage = (filePath: string): string => {
  const ext = filePath.split('.').pop()?.toLowerCase()
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'vue': 'vue',
    'html': 'html',
    'css': 'css',
    'scss': 'scss',
    'json': 'json',
    'md': 'markdown',
    'py': 'python',
    'java': 'java'
  }
  return languageMap[ext || ''] || 'text'
}

// 过滤代码块内容，只保留文本描述
const filterOutCodeBlocks = (content: string): string => {
  if (!content) return ''
  
  // 移除代码块（```language code ```）
  let filteredContent = content.replace(/```[\w-]*\n[\s\S]*?```/g, '')
  
  // 移除单行代码（`code`）但保留必要的标记文本
  filteredContent = filteredContent.replace(/`([^`\n]+)`/g, '$1')
  
  return filteredContent.trim()
}

// 流式内容解析器
const parseStreamingContent = (chunk: string, fullContent: string) => {
  try {
    // 检查是否包含工具调用标识
    if (chunk.includes('[工具调用]') && chunk.includes('写入文件')) {
      parseFileWriteToolCall(chunk, fullContent)
    }
    
    // 检查是否包含代码块
    if (chunk.includes('```')) {
      parseCodeBlock(fullContent)
    }
    
    // 检查步骤信息
    if (chunk.includes('STEP ')) {
      parseStepInfo(chunk)
    }
  } catch (error) {
    console.error('解析流式内容失败:', error)
  }
}

// 解析文件写入工具调用
const parseFileWriteToolCall = (chunk: string, fullContent: string) => {
  // 匹配工具调用模式：[工具调用] 写入文件 path/to/file.ext
  const toolCallPattern = /\[工具调用\]\s*写入文件\s+([^\n\r]+)/g
  const match = toolCallPattern.exec(chunk)
  
  if (match) {
    const filePath = match[1].trim()
    const fileName = extractFileName(filePath)
    const fileId = Date.now().toString() + Math.random().toString(36).substr(2, 9)
    
    // 如果当前已有文件正在生成，先将其完成并移到已完成列表
    if (currentGeneratingFile.value && !currentGeneratingFile.value.completed) {
      currentGeneratingFile.value.completed = true
      completedFiles.value.push(currentGeneratingFile.value)
    }
    
    // 创建新的生成文件
    currentGeneratingFile.value = {
      id: fileId,
      name: fileName,
      path: filePath,
      content: '',
      language: detectLanguage(filePath),
      completed: false,
      generatedAt: new Date().toISOString()
    }
  }
}

// 解析代码块
const parseCodeBlock = (fullContent: string) => {
  if (!currentGeneratingFile.value) return
  
  // 首先查找最近的工具调用位置
  const toolCallIndex = fullContent.lastIndexOf('[工具调用] 写入文件')
  if (toolCallIndex === -1) return
  
  // 从工具调用位置开始查找代码块
  const contentAfterTool = fullContent.substring(toolCallIndex)
  
  // 匹配完整的代码块：```language\ncode content\n```
  const completeCodeBlockPattern = /```(?:[\w-]+)?\n([\s\S]*?)```/g
  const completeMatch = completeCodeBlockPattern.exec(contentAfterTool)
  
  if (completeMatch) {
    // 找到完整的代码块
    const newCodeContent = completeMatch[1]
    
    // 如果内容发生变化，实现流式更新
    if (currentGeneratingFile.value.content !== newCodeContent) {
      // 逐步更新内容以实现流式效果
      streamCodeContent(newCodeContent, true)
    }
  } else {
    // 查找正在生成的代码块（不完整）
    const incompleteCodeBlockPattern = /```(?:[\w-]+)?\n([\s\S]*)$/
    const incompleteMatch = incompleteCodeBlockPattern.exec(contentAfterTool)
    
    if (incompleteMatch) {
      // 正在流式生成代码
      const newCodeContent = incompleteMatch[1]
      
      // 如果内容发生变化，实现流式更新
      if (currentGeneratingFile.value.content !== newCodeContent) {
        streamCodeContent(newCodeContent, false)
      }
    }
  }
}

// 流式更新代码内容
const streamCodeContent = (targetContent: string, isComplete: boolean) => {
  if (!currentGeneratingFile.value) return
  
  const currentContent = currentGeneratingFile.value.content
  
  // 如果新内容比当前内容长，则逐字符添加
  if (targetContent.length > currentContent.length) {
    // 直接更新到新内容（后端已经是流式发送的）
    currentGeneratingFile.value.content = targetContent
    currentGeneratingFile.value.lastUpdated = new Date().toISOString()
    currentGeneratingFile.value.completed = isComplete
    
    // 滚动到代码区域底部
    nextTick(() => {
      const codeElement = document.querySelector('.current-file .code-content')
      if (codeElement) {
        codeElement.scrollTop = codeElement.scrollHeight
      }
    })
  } else if (targetContent !== currentContent) {
    // 如果内容完全不同，直接更新
    currentGeneratingFile.value.content = targetContent
    currentGeneratingFile.value.lastUpdated = new Date().toISOString()
    currentGeneratingFile.value.completed = isComplete
  }
}

// 解析步骤信息
const parseStepInfo = (chunk: string) => {
  // 匹配步骤模式：STEP 1: 步骤描述
  const stepPattern = /STEP\s+(\d+):\s*(.+)/g
  const match = stepPattern.exec(chunk)
  
  if (match) {
    const stepNumber = parseInt(match[1])
    const stepTitle = match[2].trim()
    const stepId = `step-${stepNumber}`
    
    // 检查步骤是否已存在
    const existingStep = generationSteps.value.find(s => s.id === stepId)
    
    if (!existingStep) {
      // 创建新步骤
      const newStep: GenerationStep = {
        id: stepId,
        number: stepNumber,
        title: stepTitle,
        status: 'running',
        startTime: new Date().toISOString(),
        toolCalls: []
      }
      
      generationSteps.value.push(newStep)
      currentStep.value = newStep
    } else {
      // 更新现有步骤状态
      existingStep.status = 'running'
      currentStep.value = existingStep
    }
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()

  // 监听 iframe 消息
  window.addEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })
})

// 清理资源
onUnmounted(() => {
  // EventSource 会在组件卸载时自动清理
})
</script>

<style scoped>
#appChatPage {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #fdfdfd;
}

/* 顶部栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-gen-type-tag {
  font-size: 12px;
}

.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 8px;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 0.9;
  padding: 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-item {
  margin-bottom: 12px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #1a1a1a;
  padding: 8px 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 加载更多按钮 */
.load-more-container {
  text-align: center;
  padding: 8px 0;
  margin-bottom: 16px;
}

/* 工具步骤区域样式 */
.steps-section {
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.steps-header {
  padding: 12px 0;
  
  h4 {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.steps-container {
  .step-item {
    margin-bottom: 12px;
    padding: 12px;
    background: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &.step-running {
      background: #e6f7ff;
      border-color: #91d5ff;
    }
    
    &.step-completed {
      background: #f6ffed;
      border-color: #b7eb8f;
    }
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .step-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      
      .step-number {
        font-weight: bold;
        color: #1890ff;
        font-size: 12px;
      }
      
      .step-title {
        flex: 1;
        font-size: 13px;
        color: #333;
      }
    }
    
    .tool-calls {
      .tool-call-item {
        margin: 6px 0;
        padding: 8px;
        background: white;
        border-radius: 4px;
        border: 1px solid #e1e4e8;
        
        .tool-selection {
          margin-bottom: 4px;
        }
        
        .tool-execution {
          .tool-action {
            font-weight: 500;
            color: #333;
            margin-right: 8px;
          }
          
          .file-path {
            color: #666;
            font-family: 'Monaco', 'Menlo', monospace;
            font-size: 12px;
            word-break: break-all;
          }
          
          .operation-desc {
            margin: 4px 0 0 0;
            font-size: 12px;
            color: #666;
          }
        }
      }
    }
  }
}

/* 输入区域 */
.input-container {
  padding: 16px;
  background: white;
}

.input-wrapper {
  position: relative;
}

.input-wrapper .ant-input {
  padding-right: 50px;
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

/* 右侧代码生成区域 */
.code-generation-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.code-output-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .current-file {
    background: white;
    border-bottom: 1px solid #e8e8e8;
    
    .file-header {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      background: #f8f9fa;
      
      .file-tab {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .file-icon {
          color: #1890ff;
          font-size: 14px;
        }
        
        .file-name {
          font-weight: 500;
          color: #333;
        }
      }
    }
    
    .code-content {
      position: relative;
      padding: 16px;
      background: #fafbfc;
      max-height: 400px;
      overflow-y: auto;
      
      .code-stream {
        font-family: 'Monaco', 'Menlo', 'Cascadia Code', monospace;
        font-size: 13px;
        line-height: 1.5;
        color: #333;
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
        
        code {
          background: transparent;
          padding: 0;
          font-family: inherit;
        }
      }
      
      .typing-cursor {
        animation: blink 1s infinite;
        display: inline-block;
        color: #1890ff;
        font-weight: bold;
      }
    }
  }
  
  .completed-files {
    flex: 1;
    overflow-y: auto;
    
    .ant-collapse {
      border: none;
      background: transparent;
      
      .ant-collapse-item {
        border-bottom: 1px solid #f0f0f0;
        
        .ant-collapse-header {
          padding: 12px 16px !important;
          
          .file-panel-header {
            display: flex;
            align-items: center;
            gap: 8px;
            width: 100%;
            
            .file-icon {
              color: #52c41a;
            }
            
            .file-name {
              font-weight: 500;
              color: #333;
            }
            
            .file-path {
              margin-left: auto;
              font-size: 12px;
              color: #999;
            }
          }
        }
        
        .ant-collapse-content {
          .ant-collapse-content-box {
            padding: 0;
          }
          
          .file-content-wrapper {
            padding: 16px;
            background: #fafbfc;
            
            .code-content {
              font-family: 'Monaco', 'Menlo', 'Cascadia Code', monospace;
              font-size: 13px;
              line-height: 1.5;
              color: #333;
              margin: 0;
              max-height: 300px;
              overflow-y: auto;
              white-space: pre-wrap;
              word-wrap: break-word;
              
              code {
                background: transparent;
                padding: 0;
                font-family: inherit;
              }
            }
          }
        }
      }
    }
  }
  
  .code-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #666;
    
    .placeholder-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }
  }
  
  .code-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #666;
    
    p {
      margin-top: 16px;
    }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.selected-element-alert {
  margin: 0 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }

  .chat-section,
  .code-generation-section {
    flex: none;
    height: 50vh;
  }
}

@media (max-width: 768px) {
  .header-bar {
    padding: 12px 16px;
  }

  .app-name {
    font-size: 16px;
  }

  .main-content {
    padding: 8px;
    gap: 8px;
  }

  .message-content {
    max-width: 85%;
  }

  /* 选中元素信息样式 */
  .selected-element-alert {
    margin: 0 16px;
  }

  .selected-element-info {
    line-height: 1.4;
  }

  .element-header {
    margin-bottom: 8px;
  }

  .element-details {
    margin-top: 8px;
  }

  .element-item {
    margin-bottom: 4px;
    font-size: 13px;
  }

  .element-item:last-child {
    margin-bottom: 0;
  }

  .element-tag {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 14px;
    font-weight: 600;
    color: #007bff;
  }

  .element-id {
    color: #28a745;
    margin-left: 4px;
  }

  .element-class {
    color: #ffc107;
    margin-left: 4px;
  }

  .element-selector-code {
    font-family: 'Monaco', 'Menlo', monospace;
    background: #f6f8fa;
    padding: 2px 4px;
    border-radius: 3px;
    font-size: 12px;
    color: #d73a49;
    border: 1px solid #e1e4e8;
  }

  /* 编辑模式按钮样式 */
  .edit-mode-active {
    background-color: #52c41a !important;
    border-color: #52c41a !important;
    color: white !important;
  }

  .edit-mode-active:hover {
    background-color: #73d13d !important;
    border-color: #73d13d !important;
  }
}
</style>
