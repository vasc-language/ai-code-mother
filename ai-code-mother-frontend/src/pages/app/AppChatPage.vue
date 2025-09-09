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
                <!-- 根据项目类型显示内容 -->
                <MarkdownRenderer v-if="message.content" :content="message.content" />
                <div v-if="message.loading" class="loading-indicator">
                  <a-spin size="small" />
                  <span>AI 正在思考...</span>
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
            <!-- 蓝色浮动按钮：停止 / 继续 -->
            <button
              v-if="isOwner"
              class="stream-toggle"
              :title="isGenerating ? '停止生成' : (stoppedByUser ? '继续生成' : '开始生成')"
              @click="onToggleStream"
            >
              <span v-if="isGenerating">■</span>
              <span v-else>▶</span>
            </button>
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
          <!-- Vue项目类型的当前生成文件 -->
          <div v-if="currentGeneratingFile && !isSimpleCodeGenerating" class="current-file">
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
                <CodeHighlight
                  :code="currentGeneratingFile.content"
                  :language="currentGeneratingFile.language"
                  :fileName="currentGeneratingFile.name"
                  theme="atom-one-dark"
                />
                <div class="typing-cursor" v-if="!currentGeneratingFile.completed">|</div>
              </div>
            </div>

          <!-- HTML类型的简单代码文件 -->
          <div v-if="simpleCodeFile" class="current-file">
            <div class="file-header">
              <div class="file-tab">
                <FileOutlined class="file-icon" />
                <span class="file-name">{{ simpleCodeFile.name }}</span>
                <a-tag color="blue" size="small">{{ formatCodeGenType(appInfo?.codeGenType) }}</a-tag>
                </div>
            </div>
            <div class="code-content">
              <CodeHighlight
                :code="simpleCodeFile.content"
                :language="simpleCodeFile.language"
                :fileName="simpleCodeFile.name"
                theme="atom-one-dark"
              />
              <div class="typing-cursor" v-if="!simpleCodeFile.completed">|</div>
            </div>
          </div>

          <!-- MULTI_FILE类型的多文件显示 - 改为和Vue项目一样的样式 -->
          <div v-if="multiFiles.length > 0">
            <!-- 当前正在生成的文件 -->
            <div v-if="currentMultiFile && isMultiFileGenerating" class="current-file">
              <div class="file-header">
                <div class="file-tab">
                  <FileOutlined class="file-icon" />
                  <span class="file-name">{{ currentMultiFile }}</span>
                  <a-tag color="green" size="small">{{ formatCodeGenType(appInfo?.codeGenType) }}</a-tag>
                </div>
              </div>
              <div class="code-content">
                <CodeHighlight
                  :code="getCurrentMultiFileContent()"
                  :language="getCurrentMultiFileLanguage()"
                  :fileName="currentMultiFile"
                  theme="atom-one-dark"
                />
                <div class="typing-cursor" v-if="isMultiFileGenerating">|</div>
              </div>
            </div>

            <!-- 已完成的多文件列表 -->
            <div class="completed-files">
              <a-collapse v-model:activeKey="activeFileKeys" v-if="multiFiles.length > 0">
                <a-collapse-panel
                  v-for="file in multiFiles"
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
                    <CodeHighlight
                      :code="file.content"
                      :language="file.language"
                      :fileName="file.name"
                      theme="atom-one-dark"
                    />
                  </div>
                </a-collapse-panel>
              </a-collapse>
            </div>
          </div>

          <!-- 已完成文件列表 -->
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
                  <CodeHighlight
                    :code="file.content"
                    :language="file.language"
                    :fileName="file.name"
                    theme="atom-one-dark"
                  />
                </div>
              </a-collapse-panel>
            </a-collapse>
          </div>

          <!-- 占位符 -->
          <div v-if="!currentGeneratingFile && !simpleCodeFile && multiFiles.length === 0 && completedFiles.length === 0 && !isGenerating" class="code-placeholder">
            <div class="placeholder-icon">📄</div>
            <p>AI 生成的代码文件将在这里实时显示</p>
          </div>

          <div v-else-if="!currentGeneratingFile && !simpleCodeFile && multiFiles.length === 0 && completedFiles.length === 0 && isGenerating" class="code-loading">
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
import CodeHighlight from '@/components/CodeHighlight.vue'
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
  toolType: string
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
// Streaming control state
let eventSource: EventSource | null = null
const currentRunId = ref<string | null>(null)
const stoppedByUser = ref(false)
const lastUserMessage = ref('')
const currentAiMessageIndex = ref<number | null>(null)
// 最近一次手动停止的时间戳，用于判断是否为“停止后重连”
const lastStoppedAt = ref<number>(0)

// SSE micro-batching buffers and timer (to reduce per-chunk overhead)
let ssePendingChunks: string[] = []
let sseFullContent = ''
const sseFlushTimer = ref<any>(null)

const clearSseTimerAndBuffers = () => {
  if (sseFlushTimer.value) {
    clearTimeout(sseFlushTimer.value)
    sseFlushTimer.value = null
  }
  ssePendingChunks = []
  sseFullContent = ''
}

// 轻量级性能指标
let sseMetrics: {
  runId: string | null
  codeGenType: string
  afterStop: boolean
  t0: number
  t1?: number // onopen
  t2?: number // first onmessage
  firstFlushAt?: number
  totalBytes: number
  flushCount: number
} = {
  runId: null,
  codeGenType: '',
  afterStop: false,
  t0: 0,
  totalBytes: 0,
  flushCount: 0,
}

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

// 代码流式输出定时器
const codeStreamTimer = ref<any>(null)

// HTML专用的代码流式输出状态
const simpleCodeFile = ref<GeneratedFile | null>(null)
const simpleCodeContent = ref('')
const isSimpleCodeGenerating = ref(false)
const inSimpleCodeBlock = ref(false)

// MULTI_FILE专用的多文件流式输出状态
const multiFiles = ref<GeneratedFile[]>([])
const currentMultiFile = ref<string | null>(null)
const isMultiFileGenerating = ref(false)
const multiFileContents = ref<Record<string, string>>({})
const activeMultiFileKey = ref<string>('')

// MULTI_FILE专用的流式输出定时器
const multiFileStreamTimer = ref<any>(null)


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
          .map((chat) => {
            let content = chat.message || ''
            // 如果是AI消息，根据项目类型过滤掉代码相关信息
            if (chat.messageType === 'ai') {
              if (appInfo.value?.codeGenType === CodeGenTypeEnum.HTML) {
                content = filterHtmlContent(content)
              } else if (appInfo.value?.codeGenType === CodeGenTypeEnum.MULTI_FILE) {
                content = filterOutCodeBlocks(content)
              } else if (appInfo.value?.codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
                content = formatVueProjectContent(content)
              }
            }
            return {
              type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
              content: content,
              createTime: chat.createTime,
            }
          })
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
  lastUserMessage.value = message
  currentAiMessageIndex.value = aiMessageIndex
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let streamCompleted = false
  // 新一轮生成前，重置“手动停止”标志，防止误判
  stoppedByUser.value = false
  // 重置 SSE 批处理缓冲
  clearSseTimerAndBuffers()
  // 初始化性能指标
  sseMetrics = {
    runId: null,
    codeGenType: (appInfo.value?.codeGenType || CodeGenTypeEnum.HTML) as unknown as string,
    afterStop: lastStoppedAt.value > 0 && Date.now() - lastStoppedAt.value < 1000,
    t0: performance.now(),
    totalBytes: 0,
    flushCount: 0,
  }

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
      runId: (currentRunId.value = (crypto as any)?.randomUUID?.() || `run_${Date.now()}_${Math.random()
        .toString(36)
        .slice(2)}`),
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    // 先关闭旧连接
    const hadOld = !!eventSource
    eventSource?.close()
    // 当为“停止后重连”或存在旧连接时，做一次轻微去抖，规避网络拥塞
    if (hadOld || sseMetrics.afterStop) {
      await new Promise((r) => setTimeout(r, 80))
    }
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    // 连接打开时间
    eventSource.onopen = function () {
      sseMetrics.t1 = performance.now()
    }

    // 批量刷新到 UI，降低每包处理成本
    const flushToUi = () => {
      try {
        if (streamCompleted || (!isGenerating.value && !stoppedByUser.value)) return
        if (ssePendingChunks.length === 0) return
        const batch = ssePendingChunks.join('')
        ssePendingChunks = []
        sseFullContent += batch
        const fullContent = sseFullContent

        const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
        let textForLeft = ''
        if (codeGenType === CodeGenTypeEnum.HTML) {
          textForLeft = filterHtmlContent(fullContent)
        } else if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
          textForLeft = filterOutCodeBlocks(fullContent)
        } else if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
          textForLeft = formatVueProjectContent(fullContent)
        } else {
          textForLeft = fullContent
        }

        messages.value[aiMessageIndex].content = textForLeft || 'AI 正在生成，右侧代码实时输出中…'
        messages.value[aiMessageIndex].loading = false

        // 解析流式内容并更新右侧代码区（传入本次批次与完整内容）
        parseStreamingContent(batch, fullContent)
        scrollToBottom()
        sseMetrics.flushCount += 1
      } catch (e) {
        console.error('批量刷新失败:', e)
      } finally {
        sseFlushTimer.value = null
      }
    }

    // 处理接收到的消息（改为收集 + 定时批量刷新）
    eventSource.onmessage = function (event) {
      if (streamCompleted) return
      try {
        const parsed = JSON.parse(event.data)
        const content = parsed.d
        if (content !== undefined && content !== null) {
          // 首个消息到达时间
          if (!sseMetrics.t2) sseMetrics.t2 = performance.now()
          ssePendingChunks.push(content)
          sseMetrics.totalBytes += (typeof content === 'string' ? content.length : 0)
          if (!sseFlushTimer.value) {
            sseFlushTimer.value = setTimeout(() => {
              if (!sseMetrics.firstFlushAt) sseMetrics.firstFlushAt = performance.now()
              flushToUi()
            }, 40)
          }
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted || !isGenerating.value) return

      // 先刷新剩余内容
      flushToUi()
      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()
      eventSource = null
      stoppedByUser.value = false
      currentRunId.value = null
      clearSseTimerAndBuffers()

      // 打印性能指标
      try {
        const now = performance.now()
        const t0 = sseMetrics.t0
        const t1 = sseMetrics.t1 || now
        const t2 = sseMetrics.t2 || now
        const ttftOpen = Math.round(t1 - t0)
        const ttftMsg = Math.round(t2 - t0)
        const duration = Math.max(1, (now - (sseMetrics.t2 || now)) / 1000)
        const tps = Math.round((sseMetrics.totalBytes / duration) * 100) / 100
        console.info('[SSE-METRICS][done]', {
          runId: currentRunId.value,
          codeGenType: sseMetrics.codeGenType,
          afterStop: sseMetrics.afterStop,
          ttftOpenMs: ttftOpen,
          ttftFirstMsgMs: ttftMsg,
          totalBytes: sseMetrics.totalBytes,
          flushCount: sseMetrics.flushCount,
          avgBytesPerSec: tps,
        })
      } catch (e) {}

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })

    // 处理interrupted事件（手动停止）
    eventSource.addEventListener('interrupted', function () {
      if (streamCompleted) return
      // 先刷新剩余内容
      flushToUi()
      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()
      eventSource = null
      // 保持 stoppedByUser = true，以便显示继续按钮语义
      currentRunId.value = null
      clearSseTimerAndBuffers()
      // 打印性能指标
      try {
        const now = performance.now()
        const t0 = sseMetrics.t0
        const t1 = sseMetrics.t1 || now
        const t2 = sseMetrics.t2 || now
        const ttftOpen = Math.round(t1 - t0)
        const ttftMsg = Math.round(t2 - t0)
        const duration = Math.max(1, (now - (sseMetrics.t2 || now)) / 1000)
        const tps = Math.round((sseMetrics.totalBytes / duration) * 100) / 100
        console.info('[SSE-METRICS][interrupted]', {
          runId: currentRunId.value,
          codeGenType: sseMetrics.codeGenType,
          afterStop: sseMetrics.afterStop,
          ttftOpenMs: ttftOpen,
          ttftFirstMsgMs: ttftMsg,
          totalBytes: sseMetrics.totalBytes,
          flushCount: sseMetrics.flushCount,
          avgBytesPerSec: tps,
        })
      } catch (e) {}
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
        // 先刷新剩余内容
        flushToUi()
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
        eventSource = null
        stoppedByUser.value = false
        currentRunId.value = null
        clearSseTimerAndBuffers()
        try {
          const now = performance.now()
          const t0 = sseMetrics.t0
          const t1 = sseMetrics.t1 || now
          const t2 = sseMetrics.t2 || now
          const ttftOpen = Math.round(t1 - t0)
          const ttftMsg = Math.round(t2 - t0)
          const duration = Math.max(1, (now - (sseMetrics.t2 || now)) / 1000)
          const tps = Math.round((sseMetrics.totalBytes / duration) * 100) / 100
          console.info('[SSE-METRICS][onerror-connect]', {
            runId: currentRunId.value,
            codeGenType: sseMetrics.codeGenType,
            afterStop: sseMetrics.afterStop,
            ttftOpenMs: ttftOpen,
            ttftFirstMsgMs: ttftMsg,
            totalBytes: sseMetrics.totalBytes,
            flushCount: sseMetrics.flushCount,
            avgBytesPerSec: tps,
          })
        } catch (e) {}

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

// 手动停止/继续切换
const onToggleStream = async () => {
  if (!isOwner.value) return
  if (isGenerating.value) {
    // 停止当前生成
    stoppedByUser.value = true
    isGenerating.value = false
    // 清理所有流式定时器，释放主线程
    if (codeStreamTimer.value) {
      clearInterval(codeStreamTimer.value)
      codeStreamTimer.value = null
    }
    if (multiFileStreamTimer.value) {
      clearInterval(multiFileStreamTimer.value)
      multiFileStreamTimer.value = null
    }
    // 复位 HTML / MULTI_FILE 进行中状态
    isSimpleCodeGenerating.value = false
    inSimpleCodeBlock.value = false
    // 不清空 simpleCodeFile，保留已输出部分
    isMultiFileGenerating.value = false
    currentMultiFile.value = null
    clearSseTimerAndBuffers()
    if (currentAiMessageIndex.value !== null) {
      // 停止后去掉该条的 loading 态
      const msg = messages.value[currentAiMessageIndex.value]
      msg.loading = false
      if (!msg.content || msg.content.trim().length === 0) {
        msg.content = '⏹ 已停止，未生成内容'
      } else {
        msg.content = msg.content + '\n\n⏹ 已停止'
      }
    }
    try {
      const rid = currentRunId.value
      eventSource?.close()
      eventSource = null
      if (rid) {
        await request('/app/chat/stop', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          data: { runId: rid },
        })
      }
      lastStoppedAt.value = Date.now()
      message.info('已停止生成')
    } catch (e) {
      // ignore errors for stop
    }
  } else if (stoppedByUser.value && lastUserMessage.value && currentAiMessageIndex.value !== null) {
    // 继续，以新的 runId 重新开始
    isGenerating.value = true
    await generateCode(lastUserMessage.value, currentAiMessageIndex.value!)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
  eventSource?.close()
  eventSource = null
  currentRunId.value = null
  stoppedByUser.value = false
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
  // 清空MULTI_FILE相关状态
  multiFiles.value = []
  currentMultiFile.value = null
  multiFileContents.value = {}
  isMultiFileGenerating.value = false
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

// HTML模式专用：完全移除代码片段，只保留AI的文本描述
const filterHtmlContent = (content: string): string => {
  if (!content) return ''

  // 移除完整代码块（```language code ```）及其前后内容
  let filteredContent = content.replace(/(\n\s*)?```[\w-]*\n[\s\S]*?```(\n\s*)?/g, '')

  // 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/(\n\s*)?```[\w-]*\n[\s\S]*$/g, '')

  // 移除HTML代码流式输出的特殊标记及其周围的代码内容
  filteredContent = filteredContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除内联代码标记
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')
  
  // 移除任何包含代码标记的行
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*`.*$/gm, '')
  
  // 清理多余的空行
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  filteredContent = filteredContent.replace(/^\n+/, '') // 移除开头的空行
  filteredContent = filteredContent.replace(/\n\s*$/, '') // 移除结尾的空行

  return filteredContent.trim()
}

// MULTI_FILE模式专用：完全移除代码片段，只保留AI的文本描述
const filterOutCodeBlocks = (content: string): string => {
  if (!content) return ''

  // 移除完整代码块（```language code ```）及其前后内容
  let filteredContent = content.replace(/(\n\s*)?```[\w-]*\n[\s\S]*?```(\n\s*)?/g, '')

  // 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/(\n\s*)?```[\w-]*\n[\s\S]*$/g, '')

  // 移除所有MULTI_FILE相关标记及其周围的内容
  filteredContent = filteredContent.replace(/\[MULTI_FILE_START:[^\]]+\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[MULTI_FILE_CONTENT:[^\]]+\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[MULTI_FILE_END:[^\]]+\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除特殊标记及其周围内容
  filteredContent = filteredContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除内联代码标记
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 移除工具调用信息（完全移除，不显示在左边框）
  filteredContent = filteredContent.replace(/\[选择工具\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[工具调用\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除步骤信息
  filteredContent = filteredContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 移除任何包含代码标记的行
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*`.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[MULTI_FILE_.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[CODE_.*$/gm, '')

  // 清理多余的空行
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  filteredContent = filteredContent.replace(/^\n+/, '') // 移除开头的空行
  filteredContent = filteredContent.replace(/\n\s*$/, '') // 移除结尾的空行

  return filteredContent.trim()
}

// VUE项目专用：只格式化工具调用信息，过滤代码片段
const formatVueProjectContent = (content: string): string => {
  if (!content) return ''
  
  let formattedContent = content

  // 移除完整代码块（```language code ```）
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')

  // 移除不完整的代码块（```开头但没有结束的）
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 移除特殊标记
  formattedContent = formattedContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\]/g, '')

  // 移除MULTI_FILE相关标记
  formattedContent = formattedContent.replace(/\[MULTI_FILE_START:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_CONTENT:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_END:[^\]]+\]/g, '')

  // 移除步骤信息
  formattedContent = formattedContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 移除单行代码（`code`）但保留必要的标记文本
  formattedContent = formattedContent.replace(/`([^`\n]+)`/g, '$1')

  // 移除包含MULTI_FILE_CONTENT的整行
  formattedContent = formattedContent.replace(/^.*\[MULTI_FILE_CONTENT:.*$/gm, '')

  // 关键修复：在所有工具调用标记前后添加换行符，确保它们被正确分隔
  formattedContent = formattedContent.replace(/(\[选择工具\])/g, '\n$1')
  formattedContent = formattedContent.replace(/(\[工具调用\])/g, '\n$1')

  // 将工具调用格式化为更易读的格式，确保有换行效果
  // [选择工具] 格式化为加粗并换行，匹配到下一个方括号或结尾为止
  formattedContent = formattedContent.replace(/\[选择工具\]\s*([^\[\n\r]*)/g, (match, toolName) => {
    return `**[选择工具]** ${toolName.trim()}\n\n`
  })

  // [工具调用] 格式化为加粗并换行，匹配到下一个方括号或结尾为止
  formattedContent = formattedContent.replace(/\[工具调用\]\s*([^\[\n\r]*)/g, (match, content) => {
    return `**[工具调用]** ${content.trim()}\n\n`
  })

  // 清理多余的空行
  formattedContent = formattedContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  formattedContent = formattedContent.replace(/^\n+/, '') // 移除开头的空行

  return formattedContent.trim()
}


// 流式内容解析器
const parseStreamingContent = (chunk: string, fullContent: string) => {
  try {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML

    // HTML类型使用简单的代码流式输出
    if (codeGenType === CodeGenTypeEnum.HTML) {
      parseSimpleCodeStreaming(chunk, fullContent)
    }
    // MULTI_FILE类型使用专用的多文件流式输出
    else if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
      parseMultiFileStreaming(chunk, fullContent)
    } else {
      // Vue项目类型的复杂处理逻辑
      if (chunk.includes('[工具调用]')) {
        // 通用工具调用解析
        parseToolCall(chunk, fullContent)

        // 特殊处理写入文件（用于代码生成）
        if (chunk.includes('写入文件')) {
          parseFileWriteToolCall(chunk, fullContent)
        }
      }

      if (chunk.includes('```')) {
        parseCodeBlock(fullContent)
      }

      if (chunk.includes('STEP ')) {
        parseStepInfo(chunk)
      }
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

// 通用工具调用解析器
const parseToolCall = (chunk: string, fullContent: string) => {
  // 匹配所有工具调用模式：[工具调用] 工具名称 文件路径/参数
  const toolCallPattern = /\[工具调用\]\s*([^:\n]+)(?:\s*([^\n]*))?/g
  const match = toolCallPattern.exec(chunk)

  if (match) {
    const toolDisplayName = match[1].trim()
    const filePath = match[2] ? match[2].trim() : ''

    // 创建工具调用记录
    const toolCall: ToolCall = {
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
      toolType: toolDisplayName,
      action: '执行',
      filePath: filePath,
      description: `${toolDisplayName}${filePath ? ': ' + filePath : ''}`,
      status: 'completed',
      timestamp: new Date().toISOString()
    }

    // 添加到当前步骤
    if (currentStep.value) {
      if (!currentStep.value.toolCalls) {
        currentStep.value.toolCalls = []
      }
      currentStep.value.toolCalls.push(toolCall)
    }

    // 在右侧显示区域添加工具调用信息
    showToolCallInfo(toolCall, toolDisplayName, filePath)
  }
}

// 显示工具调用信息
const showToolCallInfo = (toolCall: ToolCall, toolDisplayName: string, filePath: string) => {
  // 屏蔽右边框的工具调用信息显示，避免敏感信息泄露
  // 不再向右侧边栏添加工具调用信息
  console.log(`工具调用已执行: ${toolDisplayName} - ${filePath || '无文件路径'}`)
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

// 流式更新代码内容 - 实现打字机效果
const streamCodeContent = (targetContent: string, isComplete: boolean) => {
  if (!currentGeneratingFile.value) return

  // 清理现有定时器
  if (codeStreamTimer.value) {
    clearInterval(codeStreamTimer.value)
    codeStreamTimer.value = null
  }

  const currentContent = currentGeneratingFile.value.content

  // 如果目标内容与当前内容相同，直接完成
  if (targetContent === currentContent) {
    currentGeneratingFile.value.completed = isComplete
    return
  }

  // 如果内容完全不同，先重置内容
  if (targetContent.length < currentContent.length || !targetContent.startsWith(currentContent)) {
    currentGeneratingFile.value.content = ''
  }

  let currentIndex = currentGeneratingFile.value.content.length

  // 设置打字机效果的定时器
  codeStreamTimer.value = setInterval(() => {
    if (currentIndex < targetContent.length) {
      // 每次添加一个字符
      if (currentGeneratingFile.value) {
        currentGeneratingFile.value.content += targetContent[currentIndex]
        currentGeneratingFile.value.lastUpdated = new Date().toISOString()
      }
      currentIndex++

      // 自动滚动到底部
      nextTick(() => {
        // 尝试多种选择器找到正确的滚动容器
        const selectors = [
          '.current-file .hljs',
          '.current-file pre[class*="language-"]',
          '.current-file pre',
          '.current-file .code-content'
        ]

        let scrollElement = null
        for (const selector of selectors) {
          const element = document.querySelector(selector)
          if (element && element.scrollHeight > element.clientHeight) {
            scrollElement = element
            break
          }
        }

        if (scrollElement) {
          scrollElement.scrollTop = scrollElement.scrollHeight
          // 延迟再次滚动确保完全到达底部
          setTimeout(() => {
            scrollElement.scrollTop = scrollElement.scrollHeight
          }, 50)
        }
      })
    } else {
      // 完成输出
      clearInterval(codeStreamTimer.value)
      codeStreamTimer.value = null
      if (currentGeneratingFile.value) {
        currentGeneratingFile.value.completed = isComplete

        // 如果是完整的代码块，自动将文件移动到已完成列表
        if (isComplete) {
          // 将当前文件移动到已完成列表
          completedFiles.value.push({...currentGeneratingFile.value})
          activeFileKeys.value = [currentGeneratingFile.value.id] // 自动展开这个文件
          currentGeneratingFile.value = null
        }
      }
    }
  }, 10) // 每10毫秒添加一个字符，可根据需要调整速度
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

// HTML和MULTI_FILE专用的简单代码流式处理
const parseSimpleCodeStreaming = (chunk: string, fullContent: string) => {
  try {
    // 支持前端 micro-batching：一个 batch 可能同时包含多个标记
    const hasStart = chunk.includes('[CODE_BLOCK_START]')
    const hasEnd = chunk.includes('[CODE_BLOCK_END]')

    if (hasStart && !inSimpleCodeBlock.value) {
      // 代码块开始，创建简单的代码文件
      startSimpleCodeFile()
    }

    // 去除所有标记，留下纯代码内容
    const codeContent = chunk.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\]/g, '')
    if (codeContent && codeContent.length > 0) {
      updateSimpleCodeContent(codeContent)
    }

    if (hasEnd && inSimpleCodeBlock.value) {
      // 代码块结束
      completeSimpleCodeFile()
    }
  } catch (error) {
    console.error('解析简单代码流失败:', error)
  }
}

// 开始简单的代码文件生成
const startSimpleCodeFile = () => {
  isSimpleCodeGenerating.value = true
  inSimpleCodeBlock.value = true
  simpleCodeContent.value = ''

  // 根据应用类型确定文件名和语言
  const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
  let fileName = 'index'
  let language = 'html'
  let fileExtension = '.html'

  if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
    fileName = 'main'
    language = 'javascript'
    fileExtension = '.js'
  }

  simpleCodeFile.value = {
    id: Date.now().toString(),
    name: fileName + fileExtension,
    path: fileName + fileExtension,
    content: '',
    language: language,
    completed: false,
    generatedAt: new Date().toISOString()
  }
}

// 更新简单代码内容
const updateSimpleCodeContent = (content: string) => {
  if (!simpleCodeFile.value || !inSimpleCodeBlock.value) return

  simpleCodeContent.value += content
  simpleCodeFile.value.content = simpleCodeContent.value
  simpleCodeFile.value.lastUpdated = new Date().toISOString()

  // 自动滚动到底部
  nextTick(() => {
    // 尝试多种选择器找到正确的滚动容器
    const selectors = [
      '.current-file .hljs',
      '.current-file pre[class*="language-"]',
      '.current-file pre',
      '.current-file .code-content'
    ]

    let scrollElement = null
    for (const selector of selectors) {
      const element = document.querySelector(selector)
      if (element && element.scrollHeight > element.clientHeight) {
        scrollElement = element
        break
      }
    }

    if (scrollElement) {
      scrollElement.scrollTop = scrollElement.scrollHeight
      // 延迟再次滚动确保完全到达底部
      setTimeout(() => {
        scrollElement.scrollTop = scrollElement.scrollHeight
      }, 50)
    }
  })
}

// 完成简单代码文件生成
const completeSimpleCodeFile = () => {
  if (!simpleCodeFile.value) return

  inSimpleCodeBlock.value = false
  isSimpleCodeGenerating.value = false
  simpleCodeFile.value.completed = true

  // 保持代码在红色框内显示，不清空
}

// MULTI_FILE专用的多文件流式处理
const parseMultiFileStreaming = (chunk: string, fullContent: string) => {
  try {
    // 检查MULTI_FILE专用标记
    if (chunk.includes('[MULTI_FILE_START:')) {
      // 文件开始标记
      const match = chunk.match(/\[MULTI_FILE_START:([^\]]+)\]/)
      if (match) {
        const fileName = match[1]
        startMultiFile(fileName)
      }
    } else if (chunk.includes('[MULTI_FILE_CONTENT:')) {
      // 文件内容标记
      const match = chunk.match(/\[MULTI_FILE_CONTENT:([^\]]+)\](.*)$/s)
      if (match) {
        const fileName = match[1]
        const content = match[2]
        updateMultiFileContent(fileName, content)
      }
    } else if (chunk.includes('[MULTI_FILE_END:')) {
      // 文件结束标记
      const match = chunk.match(/\[MULTI_FILE_END:([^\]]+)\]/)
      if (match) {
        const fileName = match[1]
        completeMultiFile(fileName)
      }
    }
  } catch (error) {
    console.error('解析多文件流失败:', error)
  }
}

// 开始多文件生成
const startMultiFile = (fileName: string) => {
  if (!fileName) return

  isMultiFileGenerating.value = true
  currentMultiFile.value = fileName

  // 确定文件语言类型
  let language = 'html'
  if (fileName.endsWith('.css')) language = 'css'
  else if (fileName.endsWith('.js')) language = 'javascript'

  // 检查文件是否已存在
  let existingFile = multiFiles.value.find(file => file.name === fileName)

  if (!existingFile) {
    // 创建新文件
    const newFile: GeneratedFile = {
      id: Date.now().toString() + '_' + fileName,
      name: fileName,
      path: fileName,
      content: '',
      language: language,
      completed: false,
      generatedAt: new Date().toISOString()
    }

    multiFiles.value.push(newFile)
    multiFileContents.value[fileName] = ''

    // 如果是第一个文件，设置为活动标签
    if (multiFiles.value.length === 1) {
      activeMultiFileKey.value = fileName
    }
  } else {
    // 重置现有文件
    existingFile.completed = false
    existingFile.lastUpdated = new Date().toISOString()
    multiFileContents.value[fileName] = existingFile.content
  }
}

// 更新多文件内容 - 直接更新，不使用定时器
const updateMultiFileContent = (fileName: string, content: string) => {
  if (!fileName || !multiFileContents.value.hasOwnProperty(fileName)) return

  // 清理内容中的标记符号，保留原始格式
  let cleanContent = content
    .replace(/\[MULTI_FILE_CONTENT:[^\]]+\]/g, '')
    .replace(/\[MULTI_FILE_START:[^\]]+\]/g, '')
    .replace(/\[MULTI_FILE_END:[^\]]+\]/g, '')

  // 直接更新内容，不使用定时器
  const currentContent = multiFileContents.value[fileName] || ''
  const newContent = currentContent + cleanContent

  // 只有当内容真正变化时才更新
  if (newContent !== currentContent) {
    multiFileContents.value[fileName] = newContent

    // 更新文件对象
    const file = multiFiles.value.find(f => f.name === fileName)
    if (file) {
      file.content = newContent
      file.lastUpdated = new Date().toISOString()
    }

    // 智能滚动：只在内容增加时滚动
    nextTick(() => {
      smartScrollToBottom()
    })
  }
}

// 智能滚动函数 - 只在用户接近底部时滚动
const smartScrollToBottom = () => {
  const selectors = [
    '.current-file .hljs',
    '.current-file pre[class*="language-"]',
    '.current-file pre',
    '.current-file .code-content'
  ]

  let scrollElement = null
  for (const selector of selectors) {
    const element = document.querySelector(selector)
    if (element && element.scrollHeight > element.clientHeight) {
      scrollElement = element
      break
    }
  }

  if (scrollElement) {
    // 检查是否已经接近底部（100px范围内）
    const isNearBottom = scrollElement.scrollHeight - scrollElement.scrollTop - scrollElement.clientHeight < 100

    if (isNearBottom) {
      scrollElement.scrollTop = scrollElement.scrollHeight
    }
  }
}

// 新增专门的流式输出函数 - 实现打字机效果（已废弃，保留备用）
const streamMultiFileContent = (fileName: string, newContent: string) => {
  if (!fileName || !multiFileContents.value.hasOwnProperty(fileName)) return

  // 清理现有定时器
  if (multiFileStreamTimer.value) {
    clearInterval(multiFileStreamTimer.value)
    multiFileStreamTimer.value = null
  }

  const currentContent = multiFileContents.value[fileName] || ''
  const targetContent = currentContent + newContent

  // 如果目标内容与当前内容相同，直接返回
  if (targetContent === currentContent) {
    return
  }

  let currentIndex = currentContent.length

  // 设置打字机效果的定时器
  multiFileStreamTimer.value = setInterval(() => {
    if (currentIndex < targetContent.length) {
      // 每次添加一个字符
      multiFileContents.value[fileName] += targetContent[currentIndex]
      currentIndex++

      // 更新文件对象
      const file = multiFiles.value.find(f => f.name === fileName)
      if (file) {
        file.content = multiFileContents.value[fileName]
        file.lastUpdated = new Date().toISOString()
      }

      // 每次添加字符后都触发滚动
      nextTick(() => {
        // 使用和Vue项目完全相同的滚动选择器和逻辑
        const selectors = [
          '.current-file .hljs',
          '.current-file pre[class*="language-"]',
          '.current-file pre',
          '.current-file .code-content'
        ]

        let scrollElement = null
        for (const selector of selectors) {
          const element = document.querySelector(selector)
          // 只有当元素真正可滚动时才滚动
          if (element && element.scrollHeight > element.clientHeight) {
            scrollElement = element
            break
          }
        }

        if (scrollElement) {
          scrollElement.scrollTop = scrollElement.scrollHeight
          // 延迟再次滚动确保完全到达底部
          setTimeout(() => {
            scrollElement.scrollTop = scrollElement.scrollHeight
          }, 50)
        }
      })
    } else {
      // 完成输出
      clearInterval(multiFileStreamTimer.value)
      multiFileStreamTimer.value = null
    }
  }, 10) // 每10毫秒添加一个字符，可根据需要调整速度
}

// 完成多文件生成
const completeMultiFile = (fileName: string) => {
  const file = multiFiles.value.find(f => f.name === fileName)
  if (file) {
    file.completed = true

    // 将完成的文件移动到已完成列表
    completedFiles.value.push(file)
    activeFileKeys.value = [file.id] // 自动展开这个文件
  }

  // 如果是当前文件，清除当前状态
  if (currentMultiFile.value === fileName) {
    currentMultiFile.value = null
  }

  // 检查是否所有文件都已完成
  const allCompleted = multiFiles.value.length > 0 && multiFiles.value.every(f => f.completed)
  if (allCompleted) {
    isMultiFileGenerating.value = false
  }
}

// 获取当前多文件内容
const getCurrentMultiFileContent = () => {
  if (!currentMultiFile.value) return ''
  return multiFileContents.value[currentMultiFile.value] || ''
}

// 获取当前多文件语言
const getCurrentMultiFileLanguage = () => {
  if (!currentMultiFile.value) return 'text'
  const fileName = currentMultiFile.value
  if (fileName.endsWith('.css')) return 'css'
  else if (fileName.endsWith('.js')) return 'javascript'
  else if (fileName.endsWith('.html')) return 'html'
  return 'text'
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
  // 清理代码流式定时器
  if (codeStreamTimer.value) {
    clearInterval(codeStreamTimer.value)
    codeStreamTimer.value = null
  }
  // 清理MULTI_FILE专用的流式输出定时器
  if (multiFileStreamTimer.value) {
    clearInterval(multiFileStreamTimer.value)
    multiFileStreamTimer.value = null
  }
  // 主动关闭 SSE 连接并通知后端取消，确保释放资源
  const rid = currentRunId.value
  if (eventSource) {
    try {
      eventSource.close()
      eventSource = null
    } catch (e) {}
  }
  if (rid) {
    // 异步 fire-and-forget，不阻塞卸载
    try {
      fetch((request.defaults.baseURL || '') + '/app/chat/stop', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ runId: rid }),
      })
    } catch (e) {}
  }
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

/* 蓝色浮动的流控制按钮 */
.stream-toggle {
  position: absolute;
  right: 56px; /* 不遮挡发送按钮 */
  bottom: 8px;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: #1677ff;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  line-height: 1;
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
  overflow: auto;

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
      min-height: 300px;
      max-height: 600px;
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
              max-height: 500px;
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

/* 轻量级流式展示，避免频繁语法高亮的性能开销 */
.code-plain {
  white-space: pre-wrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  padding: 8px 12px;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 6px;
  overflow: auto;
  max-height: 520px;
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
