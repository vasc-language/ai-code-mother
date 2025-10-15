<template>
  <div class="lovable-app-chat-page">
    <!-- 顶部导航栏 - Lovable 风格 -->
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
      <!-- 左侧：聊天面板 -->
      <div class="left-panel chat-panel-container">
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
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <img :src="currentAiAvatar" alt="AI" class="ai-avatar-img" />
              </div>
              <div class="message-content">
                <!-- 根据项目类型显示内容 -->
                <div v-if="message.loading && !message.content" class="loading-indicator">
                  <a-spin size="small" />
                  <span>Thinking</span>
                </div>
                <!-- 思考完成标签（显示在消息内容上方） -->
                <div v-else-if="message.thinkingDuration && message.content" class="thinking-complete">
                  <img src="@/assets/thinking.png" alt="Thinking" class="thinking-icon" />
                  <span class="thinking-duration">Thought for {{ message.thinkingDuration }} seconds</span>
                </div>
                <MarkdownRenderer v-if="message.content" :content="message.content" />
                <!-- 刷新提示 - 仅在生成流程彻底完成时显示 -->
                <div v-if="message.content && index === messages.length - 1 && generationFinished" class="refresh-reminder">
                  <img src="@/assets/refresh.svg" alt="Refresh" class="refresh-icon" />
                  <span class="refresh-text">Haste reloads, patience restores.</span>
                </div>
                <div v-else-if="!message.loading" class="empty-message">
                  <span class="empty-message-text">Even lightning needs a moment to strike</span><span class="loading-dots"><span>.</span><span>.</span><span>.</span></span>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- 选中元素信息展示 -->
        <a-alert
          v-if="isEditMode && selectedElementInfo"
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
        <div class="input-wrapper">
          <!-- AI模型选择器弹出层 -->
          <div
            class="model-selector-popup"
            v-if="showModelSelector"
            @mouseenter="handlePlusButtonHover"
            @mouseleave="handleSelectorAreaLeave"
          >
            <AiModelSelector
              ref="modelSelectorRef"
              :disabled="isGenerating"
              @change="handleModelChange"
            />
          </div>

          <!-- 主输入框区域 -->
          <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
            <a-textarea
              v-model:value="userInput"
              :placeholder="getInputPlaceholder()"
              :rows="2"
              :maxlength="1000"
              @keydown="onInputKeydown"
              @input="autoResizeTextarea"
              :disabled="isGenerating || !isOwner"
            />
          </a-tooltip>
          <a-textarea
            v-else
            v-model:value="userInput"
            :placeholder="getInputPlaceholder()"
            :rows="2"
            :maxlength="1000"
            @keydown="onInputKeydown"
            @input="autoResizeTextarea"
            :disabled="isGenerating"
          />

          <!-- 底部按钮行 -->
          <div class="input-actions-row">
            <!-- 左下角按钮组 -->
            <div class="input-left-actions">
              <!-- 模型列表按钮 -->
              <button
                class="action-btn model-btn"
                @click="toggleModelSelector"
                @mouseenter="handlePlusButtonHover"
                @mouseleave="handlePlusButtonLeave"
                :title="showModelSelector ? '收起模型选择' : '展开模型选择'"
                :disabled="isGenerating || !isOwner"
              >
                <img
                  v-if="getCurrentModelIcon()"
                  :src="getCurrentModelIcon()!"
                  alt="模型图标"
                  class="model-icon-img"
                />
                <img v-else :src="currentAiAvatar" alt="AI模型" class="model-icon-img" />
              </button>
              <!-- 编辑模式按钮 -->
              <button
                class="action-btn edit-btn"
                :class="{ active: isEditMode }"
                @click="toggleEditMode"
                :title="isEditMode ? '退出编辑模式' : '进入编辑模式'"
              >
                <EditOutlined />
              </button>
            </div>

            <!-- 右下角发送按钮 -->
            <div class="input-right-actions">
              <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
                <button
                  class="stream-toggle"
                  :title="primaryActionTitle"
                  :disabled="true"
                  @click="onPrimaryActionClick"
                >
                  <span v-if="btnState === 'stop'">■</span>
                  <span v-else-if="btnState === 'continue'">▶</span>
                  <SendOutlined v-else :style="{ opacity: 0.5 }" />
                </button>
              </a-tooltip>
              <button
                v-else
                class="stream-toggle"
                :title="primaryActionTitle"
                :disabled="primaryActionDisabled"
                @click="onPrimaryActionClick"
              >
                <span v-if="btnState === 'stop'">■</span>
                <span v-else-if="btnState === 'continue'">▶</span>
                <SendOutlined v-else :style="{ opacity: btnState === 'disabled' ? 0.5 : 1 }" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：可调节分隔条 -->
      <div
        class="resizer"
        :class="{ 'is-resizing': isResizing }"
        @mousedown="startResize"
        @touchstart="startTouchResize"
      ></div>

      <!-- 右侧：代码/预览面板 -->
      <div class="right-panel code-panel-container">
        <!-- 代码视图 - VSCode风格左右分栏 -->
        <div v-if="currentView === 'code'" class="code-view-container vscode-layout">
          <!-- 左侧：文件树 -->
          <div class="file-explorer" :style="{ width: codeExplorerWidth + 'px' }">
            <div class="explorer-header">
              <div class="header-title">
                <FolderOutlined class="title-icon" />
                <span>文件资源管理器</span>
              </div>
            </div>

            <div class="explorer-content">
              <!-- 项目根目录 -->
              <div v-if="hasAnyFiles" class="project-root">
                <FolderOpenOutlined class="root-icon" />
                <span class="root-name">{{ projectName }}</span>
              </div>

              <!-- 文件列表 -->
              <div class="file-list">
                <!-- 文件树节点 -->
                <div
                  v-for="node in flatFileTree"
                  :key="node.path"
                  class="file-item"
                  :class="{
                    active: node.type === 'file' && selectedFileId === node.file?.id,
                    'is-folder': node.type === 'folder'
                  }"
                  :style="{ paddingLeft: `${node.level * 16 + 8}px` }"
                  @click="node.type === 'folder' ? toggleFolder(node.path) : selectFile(node.file!.id)"
                >
                  <!-- 目录节点 -->
                  <template v-if="node.type === 'folder'">
                    <RightOutlined v-if="!isFolderExpanded(node.path)" class="folder-arrow" />
                    <DownOutlined v-else class="folder-arrow" />
                    <FolderOutlined v-if="!isFolderExpanded(node.path)" class="file-icon" />
                    <FolderOpenOutlined v-else class="file-icon" />
                    <span class="file-name">{{ node.name }}</span>
                  </template>

                  <!-- 文件节点 -->
                  <template v-else>
                    <FileTextOutlined class="file-icon file-icon-indent" />
                    <span class="file-name">{{ node.name }}</span>
                    <span
                      v-if="node.file && !node.file.completed"
                      class="file-badge"
                    >生成中</span>
                  </template>
                </div>

                <!-- 空状态 -->
                <div v-if="!hasAnyFiles && !isGenerating" class="explorer-empty">
                  <CodeOutlined class="empty-icon-small" />
                  <p class="empty-text-small">等待文件生成</p>
                </div>

                <div v-if="!hasAnyFiles && isGenerating" class="explorer-empty">
                  <a-spin size="small" />
                  <p class="empty-text-small">正在生成中...</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 竖向分隔条 -->
          <div
            class="vertical-resizer"
            @mousedown="startCodeResize"
            @dblclick="resetCodeSize"
          >
            <div class="resizer-handle"></div>
          </div>

          <!-- 右侧：代码编辑器区域 -->
          <div class="code-editor-area">
              <!-- 文件标签栏 -->
              <div v-if="selectedFile" class="file-tabs">
                <div class="file-tab active">
                  <FileTextOutlined class="tab-icon" />
                  <span class="tab-name">{{ selectedFile.name }}</span>
                </div>
                <!-- 历史版本按钮 -->
                <button v-if="isOwner" class="history-btn-icon" @click="showVersionHistory" title="查看历史版本">
                  <img :src="versionHistoryIcon" alt="历史版本" class="version-history-icon" />
                </button>
                <!-- 复制按钮 -->
                <button class="copy-btn-icon" @click="copyCode(selectedFile.content)" title="复制代码">
                  <CopyOutlined />
                </button>
              </div>

              <!-- 代码编辑器主体 -->
              <div class="editor-main">
                <div v-if="selectedFile" class="editor-container">
                  <!-- 代码内容 -->
                  <div class="code-content">
                    <CodeHighlight
                      :code="selectedFile.content"
                      :language="selectedFile.language"
                      :fileName="selectedFile.name"
                    />
                    <div class="typing-cursor" v-if="isFileGenerating(selectedFile)">|</div>
                  </div>
                </div>

                <!-- 空状态 -->
                <div v-else class="empty-state">
                  <div class="empty-icon">
                    <CodeOutlined />
                  </div>
                  <p class="empty-text">{{ isGenerating ? '正在生成代码...' : '等待代码生成' }}</p>
                  <p class="empty-hint">AI 生成的代码文件将在这里实时显示</p>
                </div>
              </div>

              <!-- 状态栏 -->
              <div v-if="selectedFile" class="status-bar">
                <div class="status-left">
                  <span class="status-item">
                    <span class="status-icon">⚡</span>
                    {{ formatCodeGenType(appInfo?.codeGenType) }}
                  </span>
                </div>
                <div class="status-right">
                  <span class="status-item">LF</span>
                  <span class="status-item">{{ selectedFile.language || 'plaintext' }}</span>
                  <span class="status-item">UTF-8</span>
                </div>
              </div>
            </div>
        </div>

        <!-- 预览视图 -->
        <div v-else class="preview-view-container">
          <div class="preview-container">
            <!-- 加载中状态 -->
            <PreviewLoading v-if="appGenerationStore.isGenerating && !previewUrl" />
            <!-- 预览iframe -->
            <iframe
              v-else-if="previewUrl"
              :src="previewUrl"
              class="preview-iframe"
              frameborder="0"
              @load="onIframeLoad"
            />
            <!-- 空状态 -->
            <div v-else class="preview-placeholder">
              <a-empty description="暂无预览，请先生成或部署" />
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="appInfo"
      :show-actions="isOwner || isAdmin"
      @edit="editApp"
      @delete="deleteApp"
    />

    <!-- 部署中弹窗 -->
    <DeployingModal v-model:open="deployingModalVisible" />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      @open-site="openDeployedSite"
    />

    <!-- 版本历史弹窗 -->
    <a-modal
      v-model:open="versionModalVisible"
      title="版本历史"
      :width="800"
      :footer="null"
      @cancel="closeVersionModal"
    >
      <a-spin :spinning="loadingVersions">
        <a-timeline v-if="versionList.length > 0" mode="left">
          <a-timeline-item
            v-for="version in versionList"
            :key="version.id"
            :color="version.versionNum === currentVersionNum ? 'green' : 'blue'"
          >
            <template #dot>
              <span class="version-dot">{{ version.versionTag }}</span>
            </template>
            <div class="version-item">
              <div class="version-header">
                <h4>
                  {{ version.versionTag }}
                  <a-tag v-if="version.versionNum === currentVersionNum" color="success"
                    >当前版本</a-tag
                  >
                </h4>
              </div>
              <div class="version-info">
                <p class="version-info-item">
                  <ClockCircleOutlined class="version-icon" />
                  <span><strong>部署时间：</strong>{{
                    formatDateTime(version.deployedTime)
                  }}</span>
                </p>
                <p v-if="version.user" class="version-info-item">
                  <UserOutlined class="version-icon" />
                  <span><strong>部署用户：</strong>{{ version.user.userName }}</span>
                </p>
                <p v-if="version.deployUrl" class="version-info-item">
                  <LinkOutlined class="version-icon" />
                  <span>
                    <strong>部署地址：</strong>
                    <a :href="version.deployUrl" target="_blank">{{
                      version.deployUrl
                    }}</a>
                  </span>
                </p>
                <div class="version-actions">
                  <a-button
                    type="link"
                    size="small"
                    @click="viewVersionDetail(version)"
                  >
                    查看详情
                  </a-button>
                  <a-popconfirm
                    v-if="version.versionNum !== currentVersionNum"
                    title="确定要回滚到此版本吗？回滚后将恢复该版本的所有代码文件。"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleRollback(version)"
                  >
                    <a-button type="link" size="small" danger>回滚</a-button>
                  </a-popconfirm>
                </div>
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无版本历史" />
      </a-spin>
    </a-modal>

    <!-- 版本详情弹窗 -->
    <a-modal
      v-model:open="versionDetailModalVisible"
      :title="`版本详情 - ${selectedVersion?.versionTag}`"
      :width="900"
      :footer="null"
    >
      <div v-if="selectedVersion" class="version-detail">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="版本号">{{
            selectedVersion.versionTag
          }}</a-descriptions-item>
          <a-descriptions-item label="部署时间">{{
            formatDateTime(selectedVersion.deployedTime)
          }}</a-descriptions-item>
          <a-descriptions-item label="部署用户" v-if="selectedVersion.user">{{
            selectedVersion.user.userName
          }}</a-descriptions-item>
          <a-descriptions-item label="部署地址" v-if="selectedVersion.deployUrl">
            <a :href="selectedVersion.deployUrl" target="_blank">{{
              selectedVersion.deployUrl
            }}</a>
          </a-descriptions-item>
          <a-descriptions-item label="部署标识" v-if="selectedVersion.deployKey">{{
            selectedVersion.deployKey
          }}</a-descriptions-item>
          <a-descriptions-item label="备注" v-if="selectedVersion.remark">{{
            selectedVersion.remark
          }}</a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, onActivated, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useAppGenerationStore } from '@/stores/appGeneration'
import {
  getAppVoById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
} from '@/api/appController'
import {
  listVersionsByAppId,
  rollbackToVersion as rollbackToVersionApi,
} from '@/api/appVersionController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenTypes'
import { initMessageCollapse } from '@/utils/messageCollapse'
import request from '@/request'
import { useCodeGeneration } from './composables/useCodeGeneration'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import CodeHighlight from '@/components/CodeHighlight.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import DeployingModal from '@/components/DeployingModal.vue'
import AiModelSelector from '@/components/AiModelSelector.vue'
import PreviewLoading from '@/components/PreviewLoading.vue'
import aiAvatar from '@/assets/aiAvatar.png'

// 导入模型SVG图标
import deepseekIcon from '@/assets/deepseek-color.svg'
import qwenIcon from '@/assets/qwen-color.svg'
import openaiIcon from '@/assets/openai.svg'
import kimiIcon from '@/assets/kimi-color.svg'
// 导入历史版本图标
import versionHistoryIcon from '@/assets/Version  history.svg'

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
  HistoryOutlined,
  ClockCircleOutlined,
  UserOutlined,
  LinkOutlined,
  EyeOutlined,
  CodeOutlined,
  EditOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  FileTextOutlined,
  CopyOutlined,
  RightOutlined,
  DownOutlined,
} from '@ant-design/icons-vue'

defineOptions({
  name: 'AppChatPage',
})

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appGenerationStore = useAppGenerationStore()
const {
  parseGeneratedContent: parseGeneratedContentOnce,
  simpleCodeFile: parsedSimpleCodeFile,
  multiFiles: parsedMultiFiles,
} = useCodeGeneration()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<any>()

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  rawContent?: string // AI消息的原始完整内容（包含代码块，用于刷新后恢复）
  loading?: boolean
  createTime?: string
  thinkingStartTime?: number // 思考开始时间戳
  thinkingDuration?: number // 思考持续时间（秒）
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

// AI模型选择相关
const modelSelectorRef = ref()
const selectedModelKey = ref('codex-mini-latest')
const showModelSelector = ref(false)
let hoverTimeout: number | null = null
const messagesContainer = ref<HTMLElement>()

// Streaming control state - 使用全局 store 管理 EventSource，避免页面切换时中断
const currentRunId = ref<string | null>(null)
const stoppedByUser = ref(false)
const lastUserMessage = ref('')
const currentAiMessageIndex = ref<number | null>(null)
// 最近一次手动停止的时间戳，用于判断是否为"停止后重连"
const lastStoppedAt = ref<number>(0)

// SSE micro-batching buffers and timer (to reduce per-chunk overhead)
let ssePendingChunks: string[] = []
let sseFullContent = ''
let sseStructuredMessages: any[] = []
const sseFlushTimer = ref<any>(null)

const clearSseTimerAndBuffers = () => {
  if (sseFlushTimer.value) {
    clearTimeout(sseFlushTimer.value)
    sseFlushTimer.value = null
  }
  ssePendingChunks = []
  sseFullContent = ''
  sseStructuredMessages = []
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

// 文件树节点接口
interface TreeNode {
  name: string // 节点名称（文件名或目录名）
  path: string // 完整路径
  type: 'file' | 'folder' // 节点类型
  children?: TreeNode[] // 子节点（仅目录有）
  file?: GeneratedFile // 文件数据（仅文件有）
  level: number // 层级（用于缩进）
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
// 生成完成后，右侧仅展示预览
const generationFinished = ref(false)

// 部署相关
const deploying = ref(false)
const deployingModalVisible = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 版本管理相关
const versionModalVisible = ref(false)
const versionDetailModalVisible = ref(false)
const loadingVersions = ref(false)
const versionList = ref<API.AppVersionVO[]>([])
const selectedVersion = ref<API.AppVersionVO | null>(null)
const currentVersionNum = ref<number>(0)

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

// UI 视图控制（新布局需要）
// ✅ 从localStorage恢复上次的视图状态
const CURRENT_VIEW_STORAGE_KEY = 'appChatPage_currentView'
const savedView = localStorage.getItem(CURRENT_VIEW_STORAGE_KEY) as 'preview' | 'code' | null
const currentView = ref<'preview' | 'code'>(savedView || 'preview')

// ✅ 监听视图变化，自动保存到localStorage
watch(currentView, (newView) => {
  localStorage.setItem(CURRENT_VIEW_STORAGE_KEY, newView)
  console.log('[视图状态] 保存currentView到localStorage:', newView)
})

// ========== VSCode 风格文件树状态 ==========
// 代码编辑器左侧文件树宽度
const codeExplorerWidth = ref(248)

// 当前选中的文件ID
const selectedFileId = ref<string | null>(null)

// 文件树展开状态（记录哪些目录是展开的）
const expandedFolders = ref<Set<string>>(new Set())

// 计算属性：获取所有文件
const allFiles = computed(() => {
  const files = []
  if (simpleCodeFile.value) {
    files.push(simpleCodeFile.value)
  }
  files.push(...multiFiles.value)
  files.push(...completedFiles.value)
  return files
})

// 计算属性：是否有任何文件
const hasAnyFiles = computed(() => {
  return allFiles.value.length > 0
})

// 计算属性：项目名称
const projectName = computed(() => {
  if (appInfo.value?.codeGenType === 'HTML') return 'html-project'
  if (appInfo.value?.codeGenType === 'MULTI_FILE') return 'multi-file-project'
  if (appInfo.value?.codeGenType === 'VUE') return 'vue-project'
  return 'project'
})

// 计算属性：当前选中的文件对象
const selectedFile = computed(() => {
  if (!selectedFileId.value) return null
  return allFiles.value.find(f => f.id === selectedFileId.value) || null
})

// 选择文件
const selectFile = (fileId: string) => {
  selectedFileId.value = fileId
}

// 判断文件是否正在生成
const isFileGenerating = (file: any) => {
  if (!file) return false

  // HTML 单文件
  if (file.id === simpleCodeFile.value?.id) {
    return !simpleCodeFile.value?.completed
  }

  // 多文件项目
  if (multiFiles.value.some(f => f.id === file.id)) {
    return currentMultiFile.value === file.name && isMultiFileGenerating.value
  }

  // Vue项目
  if (file.id === currentGeneratingFile.value?.id) {
    return !currentGeneratingFile.value?.completed
  }

  return false
}

// 获取文件行数
const getLineCount = (content: string) => {
  if (!content) return 0
  return content.split('\n').length
}

// 获取文件大小
const getFileSize = (content: string) => {
  if (!content) return '0 B'
  const bytes = new Blob([content]).size
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// ========== 文件树转换函数 ==========
/**
 * 将扁平的文件列表转换为树形结构
 * @param files 文件数组
 * @returns 树形节点数组
 */
const buildFileTree = (files: GeneratedFile[]): TreeNode[] => {
  if (!files || files.length === 0) return []

  const root: TreeNode[] = []
  const folderMap = new Map<string, TreeNode>()

  // 遍历所有文件
  files.forEach(file => {
    // 解析文件路径，分割成路径段
    const pathSegments = file.path.split('/').filter(seg => seg.length > 0)

    let currentLevel = root
    let currentPath = ''

    // 遍历路径段，构建目录结构
    pathSegments.forEach((segment, index) => {
      currentPath = currentPath ? `${currentPath}/${segment}` : segment
      const isLastSegment = index === pathSegments.length - 1

      if (isLastSegment) {
        // 最后一段是文件
        const fileNode: TreeNode = {
          name: segment,
          path: currentPath,
          type: 'file',
          file: file,
          level: index
        }
        currentLevel.push(fileNode)
      } else {
        // 中间段是目录
        let folderNode = folderMap.get(currentPath)

        if (!folderNode) {
          // 创建新目录节点
          folderNode = {
            name: segment,
            path: currentPath,
            type: 'folder',
            children: [],
            level: index
          }
          folderMap.set(currentPath, folderNode)
          currentLevel.push(folderNode)
        }

        // 移动到下一层
        currentLevel = folderNode.children!
      }
    })
  })

  return root
}

/**
 * 切换目录的展开/收起状态
 * @param folderPath 目录路径
 */
const toggleFolder = (folderPath: string) => {
  if (expandedFolders.value.has(folderPath)) {
    expandedFolders.value.delete(folderPath)
  } else {
    expandedFolders.value.add(folderPath)
  }
}

/**
 * 判断目录是否展开
 * @param folderPath 目录路径
 */
const isFolderExpanded = (folderPath: string): boolean => {
  return expandedFolders.value.has(folderPath)
}

// 计算属性：文件树结构
const fileTree = computed(() => {
  // 合并所有文件（HTML单文件 + 多文件项目 + Vue项目文件）
  const allFilesList: GeneratedFile[] = []

  if (simpleCodeFile.value) {
    allFilesList.push(simpleCodeFile.value)
  }

  allFilesList.push(...multiFiles.value)
  allFilesList.push(...completedFiles.value)

  // 使用 Map 进行去重，以文件路径作为唯一标识
  const fileMap = new Map<string, GeneratedFile>()
  allFilesList.forEach(file => {
    fileMap.set(file.path, file)
  })

  // 将去重后的文件转换为数组
  const uniqueFilesList = Array.from(fileMap.values())

  // 转换为树形结构
  return buildFileTree(uniqueFilesList)
})

/**
 * 将树形结构展平为可渲染的列表（带层级信息）
 * @param nodes 树形节点数组
 * @returns 展平的节点列表
 */
const flattenTree = (nodes: TreeNode[]): TreeNode[] => {
  const result: TreeNode[] = []

  const traverse = (nodeList: TreeNode[]) => {
    nodeList.forEach(node => {
      result.push(node)

      // 如果是目录且已展开，递归处理子节点
      if (node.type === 'folder' && node.children && isFolderExpanded(node.path)) {
        traverse(node.children)
      }
    })
  }

  traverse(nodes)
  return result
}

// 计算属性：展平的文件树（用于渲染）
const flatFileTree = computed(() => {
  return flattenTree(fileTree.value)
})

// 复制代码
const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    message.success('代码已复制到剪贴板')
  } catch (err) {
    message.error('复制失败')
  }
}

// 代码编辑器左侧文件树宽度调节
const startCodeResize = (e: MouseEvent) => {
  const startX = e.clientX
  const startWidth = codeExplorerWidth.value

  const onMouseMove = (e: MouseEvent) => {
    const delta = e.clientX - startX
    const newWidth = Math.max(180, Math.min(500, startWidth + delta))
    codeExplorerWidth.value = newWidth
  }

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 重置代码编辑器文件树宽度
const resetCodeSize = () => {
  codeExplorerWidth.value = 248
}

// 监听文件变化，自动选中第一个文件
watch(allFiles, (newFiles) => {
  if (newFiles.length > 0 && !selectedFileId.value) {
    selectedFileId.value = newFiles[0].id
  }
}, { immediate: true })

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 根据应用的modelKey动态获取AI头像
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

// 合一按钮状态机：send | stop | continue | disabled
type BtnState = 'send' | 'stop' | 'continue' | 'disabled'
const btnState = computed<BtnState>(() => {
  if (!isOwner.value) return 'disabled'
  if (isGenerating.value) return 'stop'
  const hasInput = !!(userInput.value && userInput.value.trim())
  if (stoppedByUser.value) {
    if (hasInput) return 'send'
    if (lastUserMessage.value && currentAiMessageIndex.value !== null) return 'continue'
    return 'disabled'
  }
  return hasInput ? 'send' : 'disabled'
})

const primaryActionDisabled = computed(() => btnState.value === 'disabled')
const primaryActionTitle = computed(() => {
  switch (btnState.value) {
    case 'stop':
      return '停止生成'
    case 'continue':
      return '继续生成'
    case 'send':
      return '发送'
    default:
      return '请输入提示词'
  }
})

// 自动调整textarea高度
const autoResizeTextarea = (e: Event) => {
  const textarea = e.target as HTMLTextAreaElement
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px' // 最大高度200px
}

// 文本框按键：Enter 发送，Shift+Enter 换行
const onInputKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (btnState.value === 'send') {
      sendMessage()
    }
  }
  // 自动调整高度
  setTimeout(() => autoResizeTextarea(e), 0)
}

const onPrimaryActionClick = async () => {
  if (!isOwner.value) {
    message.info('无法在别人的作品下对话哦~')
    return
  }
  const state = btnState.value
  if (state === 'stop' || state === 'continue') {
    await onToggleStream()
    return
  }
  if (state === 'send') {
    await sendMessage()
    return
  }
  message.info('请输入提示词')
}

// ========== AI模型选择器相关方法 ==========
// 鼠标悬停在+号上,延迟显示模型列表
const handlePlusButtonHover = () => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
  }
  hoverTimeout = window.setTimeout(() => {
    showModelSelector.value = true
  }, 200) // 200ms延迟,避免误触
}

// 鼠标离开+号按钮
const handlePlusButtonLeave = () => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
    hoverTimeout = null
  }
}

// 鼠标离开整个模型选择区域
const handleSelectorAreaLeave = () => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
    hoverTimeout = null
  }
  showModelSelector.value = false
}

// 点击+号按钮切换模型选择器
const toggleModelSelector = () => {
  showModelSelector.value = !showModelSelector.value
}

// 处理模型选择变化
const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  // 选择模型后自动关闭列表
  showModelSelector.value = false
  console.log('切换模型:', modelKey, model)
}

// 获取当前选中模型的图标
const getCurrentModelIcon = () => {
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

  // 默认返回null,显示AI头像
  return null
}

// 应用详情相关
const appDetailVisible = ref(false)

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 全局快捷键：不再用 Enter 终止，保留 Esc 停止（可选）
const globalKeyHandler = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && isGenerating.value) {
    e.preventDefault()
    if (isOwner.value) {
      onToggleStream()
    }
  }
}

// ========== 分隔条拖动功能 ==========
const isResizing = ref(false)

const startResize = (e: MouseEvent) => {
  // 只响应鼠标左键
  if (e.button !== 0) return

  isResizing.value = true
  e.preventDefault()
  e.stopPropagation()

  // 禁用用户选择，防止拖动时选中文本
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'

  document.addEventListener('mousemove', doResize, { passive: false })
  document.addEventListener('mouseup', stopResize)
}

const doResize = (e: MouseEvent) => {
  if (!isResizing.value) return

  e.preventDefault()
  e.stopPropagation()

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
  if (!isResizing.value) return

  isResizing.value = false

  // 恢复用户选择和光标
  document.body.style.userSelect = ''
  document.body.style.cursor = ''

  document.removeEventListener('mousemove', doResize)
  document.removeEventListener('mouseup', stopResize)
}

// ========== 触摸事件处理（手机端） ==========
const startTouchResize = (e: TouchEvent) => {
  e.preventDefault() // 防止页面滚动
  isResizing.value = true

  // 禁用用户选择，防止拖动时选中文本
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'

  document.addEventListener('touchmove', doTouchResize, { passive: false })
  document.addEventListener('touchend', stopTouchResize)
}

const doTouchResize = (e: TouchEvent) => {
  if (!isResizing.value) return
  e.preventDefault() // 防止页面滚动

  const touch = e.touches[0]
  const container = document.querySelector('.lovable-main') as HTMLElement
  if (!container) return

  const containerRect = container.getBoundingClientRect()

  // 判断是横向还是纵向布局
  const isVerticalLayout = window.innerWidth <= 768

  if (isVerticalLayout) {
    // 手机端：纵向拖动
    const topHeight = ((touch.clientY - containerRect.top) / containerRect.height) * 100

    // 限制高度范围：25% - 75%
    if (topHeight >= 25 && topHeight <= 75) {
      const leftPanel = document.querySelector('.left-panel') as HTMLElement
      const rightPanel = document.querySelector('.right-panel') as HTMLElement
      if (leftPanel && rightPanel) {
        leftPanel.style.height = `${topHeight}%`
        rightPanel.style.height = `${100 - topHeight}%`
      }
    }
  } else {
    // 桌面端：横向拖动
    const leftWidth = ((touch.clientX - containerRect.left) / containerRect.width) * 100

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
}

const stopTouchResize = () => {
  if (!isResizing.value) return

  isResizing.value = false

  // 恢复用户选择和光标
  document.body.style.userSelect = ''
  document.body.style.cursor = ''

  document.removeEventListener('touchmove', doTouchResize)
  document.removeEventListener('touchend', stopTouchResize)
}

onMounted(() => {
  window.addEventListener('keydown', globalKeyHandler)
})

onActivated(() => {
  nextTick(() => {
    scrollToBottom()
  })
})

// 同步UI状态到全局store
const syncUIStateToStore = () => {
  if (!appGenerationStore.isGenerating) return

  appGenerationStore.updateUIState({
    messages: messages.value,
    currentAiMessageIndex: currentAiMessageIndex.value,
    lastUserMessage: lastUserMessage.value,
    completedFiles: completedFiles.value,
    currentGeneratingFile: currentGeneratingFile.value,
    simpleCodeFile: simpleCodeFile.value,
    multiFiles: multiFiles.value,
    currentMultiFile: currentMultiFile.value,
    multiFileContents: multiFileContents.value,
    generationFinished: generationFinished.value,
  })
}

// 从全局store恢复UI状态
const restoreUIStateFromStore = () => {
  if (!appGenerationStore.isGeneratingForApp(appId.value)) return

  // 恢复UI状态
  messages.value = [...appGenerationStore.messages]
  currentAiMessageIndex.value = appGenerationStore.currentAiMessageIndex
  lastUserMessage.value = appGenerationStore.lastUserMessage
  completedFiles.value = [...appGenerationStore.completedFiles]
  currentGeneratingFile.value = appGenerationStore.currentGeneratingFile
    ? { ...appGenerationStore.currentGeneratingFile }
    : null
  simpleCodeFile.value = appGenerationStore.simpleCodeFile
    ? { ...appGenerationStore.simpleCodeFile }
    : null
  multiFiles.value = [...appGenerationStore.multiFiles]
  currentMultiFile.value = appGenerationStore.currentMultiFile
  multiFileContents.value = { ...appGenerationStore.multiFileContents }
  generationFinished.value = appGenerationStore.generationFinished

  // 恢复生成状态
  isGenerating.value = true
  currentRunId.value = appGenerationStore.currentRunId

  console.log('已从全局store恢复UI状态', {
    messagesCount: messages.value.length,
    completedFilesCount: completedFiles.value.length,
    multiFilesCount: multiFiles.value.length,
  })
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
            const rawContent = chat.message || ''
            let content = rawContent
            // 如果是AI消息，根据项目类型过滤掉代码相关信息（仅用于显示）
            if (chat.messageType === 'ai') {
              // if (appInfo.value?.codeGenType === CodeGenTypeEnum.HTML) {  // 已注释：HTML模式
              //   content = filterHtmlContent(rawContent)
              // } else if (appInfo.value?.codeGenType === CodeGenTypeEnum.MULTI_FILE) {  // 已注释：多文件模式
              //   content = filterOutCodeBlocks(rawContent)
              // } else if (appInfo.value?.codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
              //   content = formatVueProjectContent(rawContent)
              // }
              // 新逻辑：固定使用VUE项目模式
              content = formatVueProjectContent(rawContent)
            }
            return {
              type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
              content: content,
              rawContent: chat.messageType === 'ai' ? rawContent : undefined, // AI消息保存原始内容
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

      // 初始化选中的模型为应用创建时使用的模型
      if (appInfo.value.modelKey) {
        selectedModelKey.value = appInfo.value.modelKey
      }

      // 先加载对话历史
      await loadChatHistory()

      // ✅ 刷新后恢复代码文件：从最后一条AI消息的rawContent解析代码
      if (messages.value.length > 0 && !isGenerating.value) {
        const lastAiMessage = [...messages.value].reverse().find(msg => msg.type === 'ai' && msg.rawContent)
        if (lastAiMessage && lastAiMessage.rawContent) {
          console.log('[代码恢复] 从历史消息恢复代码文件')
          try {
            // 使用useCodeGeneration的解析方法
            await parseGeneratedContentOnce(lastAiMessage.rawContent, appInfo.value?.codeGenType)

            // 将解析结果复制到组件的文件变量（补充必需字段）
            if (parsedSimpleCodeFile.value) {
              simpleCodeFile.value = {
                ...parsedSimpleCodeFile.value,
                path: parsedSimpleCodeFile.value.path || parsedSimpleCodeFile.value.name,
                completed: true,
                generatedAt: new Date().toISOString()
              }
              console.log('[代码恢复] 恢复HTML文件:', simpleCodeFile.value?.name)
            }
            if (parsedMultiFiles.value.length > 0) {
              multiFiles.value = parsedMultiFiles.value.map(f => ({
                ...f,
                path: f.path || f.name,
                completed: true,
                generatedAt: new Date().toISOString()
              }))
              console.log('[代码恢复] 恢复多文件项目:', multiFiles.value.length, '个文件')
            }
          } catch (error) {
            console.error('[代码恢复] 解析历史代码失败:', error)
          }
        }
      }

      // 如果有至少2条对话记录，展示对应的网站
      if (messages.value.length >= 2) {
        updatePreview()
        // 非生成阶段且已有可用预览时，右侧切换为预览展示
        if (!isGenerating.value) {
          generationFinished.value = true
        }
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
    thinkingStartTime: Date.now(), // 记录开始时间
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
    thinkingStartTime: Date.now(), // 记录开始时间
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  lastUserMessage.value = message
  currentAiMessageIndex.value = aiMessageIndex
  await generateCode(message, aiMessageIndex)
}

// 实时过滤函数：判断chunk是否应该显示在左边框
const shouldShowInLeftPanel = (chunk: string): boolean => {
  if (!chunk || typeof chunk !== 'string') return false
  
  const trimmed = chunk.trim()
  if (!trimmed) return false
  
  // ✅ VUE模式：完全不过滤，保持原始行为（使用formatVueProjectContent后处理）
  // const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML  // 旧逻辑：支持多种类型
  const codeGenType = CodeGenTypeEnum.VUE_PROJECT  // 新逻辑：固定使用VUE类型
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return true  // VUE模式全部通过，保持原逻辑
  }
  
  // ❌ HTML/MULTI_FILE模式：严格过滤代码片段
  
  // 1. 检测代码块标记
  if (trimmed.includes('```')) return false
  
  // 2. 检测特殊标记（代码流式输出、多文件标记等）
  if (trimmed.includes('[CODE_BLOCK_START]')) return false
  if (trimmed.includes('[CODE_BLOCK_END]')) return false
  if (trimmed.includes('[CODE_STREAM]')) return false
  if (trimmed.includes('[MULTI_FILE_START:')) return false
  if (trimmed.includes('[MULTI_FILE_CONTENT:')) return false
  if (trimmed.includes('[MULTI_FILE_END:')) return false
  
  // 3. 检测工具调用标记（HTML/MULTI_FILE模式不需要工具调用信息）
  if (trimmed.includes('[选择工具]')) return false
  if (trimmed.includes('[工具调用]')) return false
  
  // 4. 检测HTML标签（单个chunk中含有多个标签就可能是代码）
  const htmlTagCount = (chunk.match(/<[^>]+>/g) || []).length
  if (htmlTagCount >= 2) return false
  
  // 5. 检测完整的HTML标签行
  if (/^\s*<[^>]+>\s*$/.test(trimmed)) return false
  
  // 6. 检测JavaScript/TypeScript代码特征
  if (/^\s*(?:function|const|let|var|if|else|for|while|class|return|import|export)\s+/.test(trimmed)) return false
  
  // 7. 检测CSS样式代码特征
  if (/^\s*[a-zA-Z-]+\s*:\s*[^;]+;?\s*$/.test(trimmed)) return false
  if (/^\s*[.#]?[\w-]+\s*\{/.test(trimmed)) return false
  
  // 8. 检测大量特殊符号（可能是代码）
  const specialCharCount = (chunk.match(/[{}<>()[\];:=+\-*/%&|^~]/g) || []).length
  if (specialCharCount > 5) return false
  
  // 9. 检测JSON对象（可能是结构化消息）
  if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
    try {
      JSON.parse(trimmed)
      return false // 是JSON对象，不显示
    } catch (e) {
      // 不是有效的JSON，继续判断
    }
  }
  
  // 通过所有检查，可以显示在左边框
  return true
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let streamCompleted = false
  // 新一轮生成开始，标记为未完成，右侧保持代码展示
  generationFinished.value = false
  // 清空预览URL，确保显示加载动画
  previewUrl.value = ''
  previewReady.value = false
  // 新一轮生成前，重置"手动停止"标志，防止误判
  stoppedByUser.value = false
  // 重置 SSE 批处理缓冲
  clearSseTimerAndBuffers()
  // 清空上一轮的文件状态，确保重新解析完整代码
  clearAllFiles()
  // 初始化性能指标
  sseMetrics = {
    runId: null,
    // codeGenType: (appInfo.value?.codeGenType || CodeGenTypeEnum.HTML) as unknown as string,  // 旧逻辑：支持多种类型
    codeGenType: CodeGenTypeEnum.VUE_PROJECT as unknown as string,  // 新逻辑：固定使用VUE类型
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
      modelKey: selectedModelKey.value || appInfo.value?.modelKey || '',
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`
    // 捕获本次运行的 runId，用于丢弃已过期的分片/事件
    const myRunId = currentRunId.value

    // 创建 EventSource 连接
    // 先关闭旧连接
    const hadOld = !!appGenerationStore.eventSource
    appGenerationStore.eventSource?.close()
    // 当为"停止后重连"或存在旧连接时,做一次轻微去抖,规避网络拥塞
    if (hadOld || sseMetrics.afterStop) {
      await new Promise((r) => setTimeout(r, 80))
    }
    const newEventSource = new EventSource(url, {
      withCredentials: true,
    })

    // 将新连接保存到全局 store
    appGenerationStore.startGeneration(appId.value || '', myRunId || '', newEventSource)

    // 连接打开时间
    newEventSource.onopen = function () {
      sseMetrics.t1 = performance.now()
    }

    // 批量刷新到 UI，降低每包处理成本
    const flushToUi = () => {
      try {
        // 若已不是当前运行，直接丢弃（防止旧 run 的定时器残留影响新 UI）
        if (currentRunId.value !== myRunId) return
        if (streamCompleted || (!isGenerating.value && !stoppedByUser.value)) return
        if (ssePendingChunks.length === 0) return
        
        const batch = ssePendingChunks.join('')
        ssePendingChunks = []
        
        // 注意：sseFullContent 已经在 onmessage 中累积了所有原始内容（包括代码）
        
        // const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML  // 旧逻辑：支持多种类型
        const codeGenType = CodeGenTypeEnum.VUE_PROJECT  // 新逻辑：固定使用VUE类型
        let displayContent = ''
        
        // ✅ VUE模式：需要使用formatVueProjectContent过滤完整内容后再显示
        if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
          // VUE模式使用完整内容进行过滤（保持原逻辑）
          displayContent = formatVueProjectContent(sseFullContent).trim()
        } else {
          // HTML/MULTI_FILE模式：已经在shouldShowInLeftPanel中实时过滤，直接使用
          displayContent = batch.trim()
        }

        // 添加调试日志
        console.log('[SSE Debug] 代码生成类型:', codeGenType)
        console.log('[SSE Debug] 批次内容长度:', batch.length)
        console.log('[SSE Debug] 显示内容长度:', displayContent.length)
        console.log('[SSE Debug] 完整内容长度:', sseFullContent.length)

        // 只有当有实际文本内容时才更新消息并关闭loading状态
        if (displayContent) {
          // VUE模式：完全替换（因为每次都是对完整内容过滤）
          // HTML/MULTI_FILE模式：追加显示（流式效果）
          if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
            messages.value[aiMessageIndex].content = displayContent
          } else {
            const currentContent = messages.value[aiMessageIndex].content || ''
            messages.value[aiMessageIndex].content = currentContent + batch
          }
          messages.value[aiMessageIndex].loading = false

          // 计算思考持续时间（只在第一次有内容时计算）
          const currentContent = messages.value[aiMessageIndex].content || ''
          if (currentContent && messages.value[aiMessageIndex].thinkingStartTime && !messages.value[aiMessageIndex].thinkingDuration) {
            const endTime = Date.now()
            const startTime = messages.value[aiMessageIndex].thinkingStartTime!
            const durationMs = endTime - startTime
            messages.value[aiMessageIndex].thinkingDuration = Math.round(durationMs / 1000)
          }
        }
        // 如果没有文本内容，保持loading状态，继续显示"Thinking"动画

        scrollToBottom()
        sseMetrics.flushCount += 1
      } catch (e) {
        console.error('批量刷新失败:', e)
      } finally {
        sseFlushTimer.value = null
      }
    }

    // 处理接收到的消息（改为收集 + 定时批量刷新）
    newEventSource.onmessage = function (event) {
      if (currentRunId.value !== myRunId) return
      if (streamCompleted) return
      try {
        const parsed = JSON.parse(event.data)
        const content = parsed.d
        if (content !== undefined && content !== null) {
          // 首个消息到达时间
          if (!sseMetrics.t2) sseMetrics.t2 = performance.now()
          
          // ✅ 累积所有原始内容（包括代码），用于右侧代码解析
          sseFullContent += content
          
          // ✅ 实时过滤：只有非代码内容才添加到左边框显示缓冲区
          if (shouldShowInLeftPanel(content)) {
            ssePendingChunks.push(content)
          }
          
          // 结构化消息捕获（用于右侧代码解析）
          captureStructuredSseMessage(content)
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
    newEventSource.addEventListener('done', async function () {
      if (currentRunId.value !== myRunId) return
      if (streamCompleted || !isGenerating.value) return

      // 先刷新剩余内容
      flushToUi()

      await refreshCodeFilesFromFullContent(sseFullContent)

      // 确保loading状态被关闭
      if (messages.value[aiMessageIndex]) {
        messages.value[aiMessageIndex].loading = false
        // 如果最终还是没有内容，设置一个默认消息
        if (!messages.value[aiMessageIndex].content) {
          messages.value[aiMessageIndex].content = '✅ 代码生成完成，请查看右侧代码区'
        }
      }

      streamCompleted = true
      isGenerating.value = false
      // 标记本轮生成已完成，切换到预览模式
      generationFinished.value = true
      appGenerationStore.finishGeneration()
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
    newEventSource.addEventListener('interrupted', async function () {
      if (currentRunId.value !== myRunId) return
      if (streamCompleted) return
      // 先刷新剩余内容
      flushToUi()
      await refreshCodeFilesFromFullContent(sseFullContent)
      streamCompleted = true
      isGenerating.value = false
      appGenerationStore.stopGeneration()
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
    newEventSource.addEventListener('business-error', function (event: MessageEvent) {
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
        appGenerationStore.stopGeneration()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })


    // 处理错误
    newEventSource.onerror = async function () {
      if (currentRunId.value !== myRunId) return
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (appGenerationStore.eventSource?.readyState === EventSource.CONNECTING) {
        // 先刷新剩余内容
        flushToUi()
        await refreshCodeFilesFromFullContent(sseFullContent)
        streamCompleted = true
        isGenerating.value = false
        appGenerationStore.stopGeneration()
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
      appGenerationStore.stopGeneration()
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
    // 让左侧同一条消息重新进入 loading
    const idx = currentAiMessageIndex.value!
    if (messages.value[idx]) {
      messages.value[idx].loading = true
      // 清空旧内容，避免造成“继续追加”的错觉
      messages.value[idx].content = ''
    }
    isGenerating.value = true
    await generateCode(lastUserMessage.value, currentAiMessageIndex.value!)
  } else {
    // 未在生成且未曾手动停止：作为“开始生成”快捷操作
    const text = (userInput.value || '').trim()
    if (!text) {
      message.info('请输入提示词后再开始')
      return
    }
    await sendMessage()
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
  appGenerationStore.stopGeneration()
  currentRunId.value = null
  stoppedByUser.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    // const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML  // 旧逻辑：支持多种类型
    const codeGenType = CodeGenTypeEnum.VUE_PROJECT  // 新逻辑：固定使用VUE类型
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
  deployingModalVisible.value = true // 显示部署中弹窗

  try {
    const res = await deployAppApi({
      appId: appId.value as unknown as number,
    })

    deployingModalVisible.value = false // 关闭部署中弹窗

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      message.success('部署成功')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('部署失败：', error)
    deployingModalVisible.value = false // 关闭部署中弹窗

    // 区分超时和其他错误
    const isTimeout = error?.code === 'ECONNABORTED' ||
                     error?.message?.toLowerCase().includes('timeout')

    if (isTimeout) {
      message.warning('部署时间较长，正在后台处理中，请稍后刷新页面查看部署结果', 8)
    } else {
      const errorMsg = error?.response?.data?.message || error?.message || '请重试'
      message.error('部署失败：' + errorMsg)
    }
  } finally {
    deploying.value = false
  }
}

// 显示版本历史
const showVersionHistory = async () => {
  versionModalVisible.value = true
  await fetchVersionList()
}

// 获取版本列表
const fetchVersionList = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  loadingVersions.value = true
  try {
    const res = await listVersionsByAppId({
      appId: appId.value as unknown as number,
    })

    if (res.data.code === 0 && res.data.data) {
      versionList.value = res.data.data
      // 当前版本号为列表中最大的版本号
      if (versionList.value.length > 0) {
        currentVersionNum.value = Math.max(
          ...versionList.value.map((v) => v.versionNum || 0)
        )
      }
    } else {
      message.error('获取版本列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取版本列表失败：', error)
    message.error('获取版本列表失败，请重试')
  } finally {
    loadingVersions.value = false
  }
}

// 关闭版本历史弹窗
const closeVersionModal = () => {
  versionModalVisible.value = false
}

// 查看版本详情
const viewVersionDetail = (version: API.AppVersionVO) => {
  selectedVersion.value = version
  versionDetailModalVisible.value = true
}

// 处理版本回滚
const handleRollback = async (version: API.AppVersionVO) => {
  if (!appId.value || !version.id) {
    message.error('参数错误')
    return
  }

  try {
    const res = await rollbackToVersionApi({
      appId: appId.value as unknown as number,
      versionId: version.id as unknown as number,
    })

    if (res.data.code === 0) {
      message.success('版本回滚成功')
      // 关闭模态框
      versionModalVisible.value = false
      // 刷新页面以显示回滚后的代码
      window.location.reload()
    } else {
      message.error('版本回滚失败：' + res.data.message)
    }
  } catch (error) {
    console.error('版本回滚失败：', error)
    message.error('版本回滚失败，请重试')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
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
  // 退出编辑模式时，清除左侧选中元素面板
  if (!newEditMode && selectedElementInfo.value) {
    clearSelectedElement()
  }
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
  // 清空HTML单文件状态
  simpleCodeFile.value = null
  simpleCodeContent.value = ''
  isSimpleCodeGenerating.value = false
  inSimpleCodeBlock.value = false
  // 重置步骤信息
  generationSteps.value = []
  currentStep.value = null
  // 清空当前选中的文件
  selectedFileId.value = null
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

const captureStructuredSseMessage = (rawChunk: string) => {
  if (!rawChunk || typeof rawChunk !== 'string') return
  const trimmed = rawChunk.trim()
  if (!trimmed || trimmed.length < 2) return

  const candidates = splitPotentialJsonPayloads(trimmed)
  for (const candidate of candidates) {
    if (!candidate || !candidate.startsWith('{') || !candidate.endsWith('}')) continue
    try {
      const parsed = JSON.parse(candidate)
      if (parsed && typeof parsed === 'object') {
        sseStructuredMessages.push(parsed)
      }
    } catch (error) {
      // 非 JSON 片段直接忽略
    }
  }
}

const splitPotentialJsonPayloads = (payload: string): string[] => {
  // SSE片段通常是单个JSON字符串；极端情况可能拼接多个，按换行或】【简单拆分
  if (!payload.includes('}{')) {
    return [payload]
  }
  const segments: string[] = []
  let buffer = ''
  let depth = 0
  for (const char of payload) {
    if (char === '{') {
      if (depth === 0 && buffer) {
        buffer = ''
      }
      depth += 1
    }
    if (char === '}') {
      depth = Math.max(0, depth - 1)
    }
    buffer += char
    if (depth === 0 && buffer.trim()) {
      segments.push(buffer.trim())
      buffer = ''
    }
  }
  if (buffer.trim()) {
    segments.push(buffer.trim())
  }
  return segments.length ? segments : [payload]
}

const normalizeCodeGenType = (type?: string): CodeGenTypeEnum => {
  // 已简化：只使用VUE_PROJECT类型
  // if (!type) return CodeGenTypeEnum.HTML
  // const lower = type.toLowerCase()
  // if (lower === 'html' || lower === CodeGenTypeEnum.HTML) return CodeGenTypeEnum.HTML
  // if (lower === 'multi_file' || lower === 'multi-file' || lower === CodeGenTypeEnum.MULTI_FILE) {
  //   return CodeGenTypeEnum.MULTI_FILE
  // }
  // if (
  //   lower === 'vue' ||
  //   lower === 'vue_project' ||
  //   lower === 'vue-project' ||
  //   lower === CodeGenTypeEnum.VUE_PROJECT
  // ) {
  //   return CodeGenTypeEnum.VUE_PROJECT
  // }
  // return CodeGenTypeEnum.HTML
  return CodeGenTypeEnum.VUE_PROJECT
}

const buildFilesFromToolExecutions = (generatedAt: string): GeneratedFile[] => {
  if (!sseStructuredMessages.length) return []

  const fileMap = new Map<string, GeneratedFile>()
  let counter = 0

  for (const message of sseStructuredMessages) {
    if (!message || typeof message !== 'object') continue
    const type = (message.type || message.messageType || '').toString()
    if (type !== 'tool_executed' && type !== 'toolExecuted') continue

    let args: any = {}
    if (typeof message.arguments === 'string') {
      try {
        args = JSON.parse(message.arguments)
      } catch (error) {
        args = {}
      }
    } else if (message.arguments && typeof message.arguments === 'object') {
      args = message.arguments
    }

    let relativePath: string = (args?.relativeFilePath || args?.filePath || args?.path || '').toString().trim()
    let content: string = ''

    if (typeof args?.content === 'string') {
      content = args.content
    } else if (typeof args?.newContent === 'string') {
      content = args.newContent
    } else if (Array.isArray(args?.lines)) {
      content = args.lines.join('\n')
    }

    if ((!relativePath || !content) && typeof message.result === 'string') {
      const parsed = parseToolResultSnippet(message.result)
      if (parsed) {
        if (!relativePath) relativePath = parsed.path
        if (!content) content = parsed.content
      }
    }

    if (!relativePath || !content) continue

    const name = extractFileName(relativePath)
    const fileId = `${relativePath}-${counter++}-${Date.now()}`
    const generatedFile: GeneratedFile = {
      id: fileId,
      name,
      path: relativePath,
      content,
      language: detectLanguage(relativePath),
      completed: true,
      generatedAt,
      lastUpdated: generatedAt,
    }

    fileMap.set(relativePath, generatedFile)
  }

  return Array.from(fileMap.values())
}

const parseToolResultSnippet = (result: string): { path: string; content: string } | null => {
  if (!result) return null
  const cleaned = result.replace(/\r/g, '')
  const match = cleaned.match(/\[工具调用\]\s+[^\s]+\s+([^\s]+)\s*```([\w-]*)?\n([\s\S]*?)```/)
  if (!match) return null
  const [, filePath, _lang, code] = match
  return {
    path: filePath.trim(),
    content: code,
  }
}

const mapParsedFilesToGeneratedFiles = (sourceFiles: any[], generatedAt: string): GeneratedFile[] => {
  return (sourceFiles || [])
    .filter((file) => file && typeof file.content === 'string' && file.content.trim() !== '')
    .map((file, index) => {
      const path = file.path || file.name || `file-${index}`
      const id = file.id || `${path}-${index}-${Date.now()}`
      return {
        id,
        name: file.name || extractFileName(path),
        path,
        content: file.content,
        language: file.language || detectLanguage(path),
        completed: true,
        generatedAt,
        lastUpdated: generatedAt,
      } as GeneratedFile
    })
}

const applyGeneratedFiles = (files: GeneratedFile[], normalizedType: CodeGenTypeEnum, singleFile?: GeneratedFile | null) => {
  clearAllFiles()

  // if (singleFile) {  // 已注释：HTML单文件模式
  //   simpleCodeFile.value = singleFile
  //   simpleCodeContent.value = singleFile.content
  // }

  // if (normalizedType === CodeGenTypeEnum.MULTI_FILE) {  // 已注释：多文件模式
  //   multiFiles.value = files
  //   multiFileContents.value = files.reduce<Record<string, string>>((acc, file) => {
  //     acc[file.name] = file.content
  //     return acc
  //   }, {})
  //   activeFileKeys.value = files.length ? [files[0].id] : []
  // } else if (normalizedType === CodeGenTypeEnum.VUE_PROJECT) {
  //   completedFiles.value = files
  //   activeFileKeys.value = files.length ? [files[0].id] : []
  // }
  
  // 新逻辑：固定使用VUE项目模式
  completedFiles.value = files
  activeFileKeys.value = files.length ? [files[0].id] : []

  const firstFileId =
    singleFile?.id ||
    multiFiles.value[0]?.id ||
    completedFiles.value[0]?.id ||
    null

  selectedFileId.value = firstFileId
  syncUIStateToStore()
}

const refreshCodeFilesFromFullContent = async (fullContent: string) => {
  const normalizedType = normalizeCodeGenType(appInfo.value?.codeGenType)
  const generatedAt = new Date().toISOString()

  if (normalizedType === CodeGenTypeEnum.VUE_PROJECT) {
    const toolFiles = buildFilesFromToolExecutions(generatedAt)
    if (toolFiles.length > 0) {
      applyGeneratedFiles(toolFiles, normalizedType)
      return
    }
  }

  if (!fullContent) {
    applyGeneratedFiles([], normalizedType)
    return
  }

  try {
    await parseGeneratedContentOnce(fullContent, normalizedType)
  } catch (error) {
    console.error('解析完整代码失败:', error)
    return
  }

  // if (normalizedType === CodeGenTypeEnum.HTML && parsedSimpleCodeFile.value) {  // 已注释：HTML模式
  //   const parsedFile = parsedSimpleCodeFile.value
  //   const finalFile: GeneratedFile = {
  //     id: parsedFile.id || `html-${Date.now()}`,
  //     name: parsedFile.name || 'index.html',
  //     path: parsedFile.path || parsedFile.name || 'index.html',
  //     content: parsedFile.content,
  //     language: parsedFile.language || 'html',
  //     completed: true,
  //     generatedAt,
  //     lastUpdated: generatedAt,
  //   }
  //   applyGeneratedFiles([], normalizedType, finalFile)
  //   return
  // }

  // 新逻辑：固定使用VUE项目模式
  const parsedFiles = mapParsedFilesToGeneratedFiles(parsedMultiFiles.value || [], generatedAt)
  applyGeneratedFiles(parsedFiles, normalizedType)
}

// HTML模式专用：完全移除代码片段，只保留AI的文本描述
const filterHtmlContent = (content: string): string => {
  if (!content) return ''

  let filteredContent = content

  // 1. 移除完整的markdown代码块（```language ... ```）
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')

  // 2. 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 3. 移除HTML代码流式输出的特殊标记及其内容
  filteredContent = filteredContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][^\n]*(?:\n|$)/g, '')

  // 4. 移除看起来像HTML代码的连续行（包含大量HTML标签的内容）
  // 匹配连续的HTML标签行（至少3行以上的HTML标签密集区域）
  filteredContent = filteredContent.replace(/(?:^|\n)(?:<[^>]+>[\s\S]*?){3,}(?=\n|$)/gm, '')

  // 5. 移除单行HTML标签（整行都是HTML标签的）
  filteredContent = filteredContent.replace(/^\s*<[^>]+>\s*$/gm, '')

  // 6. 移除包含大量尖括号的行（可能是HTML代码）
  filteredContent = filteredContent.replace(/^[^<\n]*(?:<[^>]+>){2,}[^>\n]*$/gm, '')

  // 7. 移除JavaScript代码片段（function, const, let, var 等关键字开头的行）
  filteredContent = filteredContent.replace(/^\s*(?:function|const|let|var|if|for|while|class|return|document|window|console)\s+.*/gm, '')

  // 8. 移除CSS样式代码（包含 { } 和 : 的样式定义）
  filteredContent = filteredContent.replace(/^\s*[a-zA-Z-]+\s*:\s*[^;]+;?\s*$/gm, '')
  filteredContent = filteredContent.replace(/^\s*[.#]?[\w-]+\s*\{[\s\S]*?\}/gm, '')

  // 9. 移除内联代码标记（`code`）
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 10. 移除工具调用信息
  filteredContent = filteredContent.replace(/\[选择工具\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[工具调用\][\s\S]*?(?=\n\n|$)/g, '')

  // 11. 清理多余的空行（保留最多1个空行）
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n+/g, '\n\n')

  // 12. 移除开头和结尾的空行
  filteredContent = filteredContent.replace(/^\n+/, '')
  filteredContent = filteredContent.replace(/\n\s*$/, '')

  return filteredContent.trim()
}

// MULTI_FILE模式专用：完全移除代码片段，只保留AI的文本描述
const filterOutCodeBlocks = (content: string): string => {
  if (!content) return ''

  let filteredContent = content

  // 1. 移除完整的markdown代码块（```language ... ```）
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')

  // 2. 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 3. 移除所有MULTI_FILE相关标记及其内容
  filteredContent = filteredContent.replace(/\[MULTI_FILE_START:[^\]]+\][^\n]*(?:\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[MULTI_FILE_CONTENT:[^\]]+\][^\n]*(?:\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[MULTI_FILE_END:[^\]]+\][^\n]*(?:\n|$)/g, '')

  // 4. 移除特殊标记及其内容
  filteredContent = filteredContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][^\n]*(?:\n|$)/g, '')

  // 5. 移除工具调用信息（完全移除，不显示在左边框）
  filteredContent = filteredContent.replace(/\[选择工具\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[工具调用\][\s\S]*?(?=\n\n|$)/g, '')

  // 6. 移除步骤信息
  filteredContent = filteredContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 7. 移除看起来像代码的连续行（包含大量特殊符号的内容）
  filteredContent = filteredContent.replace(/(?:^|\n)(?:[^a-zA-Z\u4e00-\u9fa5\n]*[{}<>()[\];:=+\-*/%&|^~]{2,}[^\n]*\n){3,}/gm, '')

  // 8. 移除单行代码标签
  filteredContent = filteredContent.replace(/^\s*<[^>]+>\s*$/gm, '')

  // 9. 移除JavaScript/TypeScript代码片段
  filteredContent = filteredContent.replace(/^\s*(?:function|const|let|var|if|else|for|while|class|return|import|export|from|default)\s+.*/gm, '')

  // 10. 移除CSS样式代码
  filteredContent = filteredContent.replace(/^\s*[a-zA-Z-]+\s*:\s*[^;]+;?\s*$/gm, '')
  filteredContent = filteredContent.replace(/^\s*[.#]?[\w-]+\s*\{[\s\S]*?\}/gm, '')

  // 11. 移除内联代码标记（`code`）
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 12. 移除包含特殊标记的行
  filteredContent = filteredContent.replace(/^.*\[MULTI_FILE_.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[CODE_.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')

  // 13. 清理多余的空行（保留最多1个空行）
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n+/g, '\n\n')

  // 14. 移除开头和结尾的空行
  filteredContent = filteredContent.replace(/^\n+/, '')
  filteredContent = filteredContent.replace(/\n\s*$/, '')

  return filteredContent.trim()
}

// —— 工具辅助：HTML 转义 ——
const escapeHtml = (s: string): string => {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// —— 工具辅助：行级 LCS 对比 ——
type DiffOp = { type: 'equal' | 'add' | 'remove', value: string }
const diffLines = (oldStr: string, newStr: string): DiffOp[] => {
  const a = oldStr.replace(/\r\n/g, '\n').split('\n')
  const b = newStr.replace(/\r\n/g, '\n').split('\n')
  const n = a.length
  const m = b.length
  const dp: number[][] = Array.from({ length: n + 1 }, () => Array(m + 1).fill(0))
  for (let i = n - 1; i >= 0; i--) {
    for (let j = m - 1; j >= 0; j--) {
      dp[i][j] = a[i] === b[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1])
    }
  }
  const ops: DiffOp[] = []
  let i = 0, j = 0
  while (i < n && j < m) {
    if (a[i] === b[j]) {
      ops.push({ type: 'equal', value: a[i] })
      i++
      j++
    } else if (dp[i + 1][j] >= dp[i][j + 1]) {
      ops.push({ type: 'remove', value: a[i] })
      i++
    } else {
      ops.push({ type: 'add', value: b[j] })
      j++
    }
  }
  while (i < n) { ops.push({ type: 'remove', value: a[i++] }) }
  while (j < m) { ops.push({ type: 'add', value: b[j++] }) }
  return ops
}

// —— Diff折叠功能初始化 ——
const initDiffCollapse = () => {
  // 处理diff容器的函数
  const processDiffContainers = () => {
    nextTick(() => {
      const diffContainers = document.querySelectorAll('.diff-container')

      diffContainers.forEach((container) => {
        // 检查是否已经初始化过
        if (container.hasAttribute('data-initialized')) return
        container.setAttribute('data-initialized', 'true')

        // 获取自动折叠延迟时间
        const autoCollapseDelay = parseInt(container.getAttribute('data-auto-collapse') || '0')

        // 为每个可折叠标题添加点击事件
        const collapsibleTitles = container.querySelectorAll('.diff-title.collapsible')

        collapsibleTitles.forEach((title) => {
          const target = title.getAttribute('data-target')
          const codeElement = container.querySelector(`.diff-code.${target}-code`) as HTMLElement
          const icon = title.querySelector('.collapse-icon')

          if (!codeElement || !icon) return

          // 点击切换折叠状态
          const toggleCollapse = () => {
            // 判断当前是否折叠：检查类名或maxHeight
            const isCollapsed = title.classList.contains('collapsed') || codeElement.style.maxHeight === '0px'

            if (isCollapsed) {
              // 展开
              codeElement.style.maxHeight = codeElement.scrollHeight + 'px'
              codeElement.style.opacity = '1'
              icon.textContent = '▼'
              title.classList.remove('collapsed')

              // 等待过渡完成后移除maxHeight限制
              setTimeout(() => {
                if (!title.classList.contains('collapsed')) {
                  codeElement.style.maxHeight = 'none'
                }
              }, 300)
            } else {
              // 折叠
              codeElement.style.maxHeight = codeElement.scrollHeight + 'px'
              void codeElement.offsetHeight // 强制重绘
              codeElement.style.maxHeight = '0px'
              codeElement.style.opacity = '0'
              icon.textContent = '▶'
              title.classList.add('collapsed')
            }
          }

          title.addEventListener('click', toggleCollapse)
        })

        // 自动折叠
        if (autoCollapseDelay > 0) {
          setTimeout(() => {
            collapsibleTitles.forEach((title) => {
              const target = title.getAttribute('data-target')
              const codeElement = container.querySelector(`.diff-code.${target}-code`) as HTMLElement
              const icon = title.querySelector('.collapse-icon')

              if (codeElement && icon) {
                codeElement.style.maxHeight = codeElement.scrollHeight + 'px'
                void codeElement.offsetHeight // 强制重绘
                codeElement.style.maxHeight = '0px'
                codeElement.style.opacity = '0'
                icon.textContent = '▶'
                title.classList.add('collapsed')
              }
            })
          }, autoCollapseDelay)
        }
      })
    })
  }

  // 使用 MutationObserver 监听DOM变化
  const observer = new MutationObserver(() => {
    processDiffContainers()
  })

  // 监听消息容器的变化
  const messagesContainer = document.querySelector('.messages-container')
  if (messagesContainer) {
    observer.observe(messagesContainer, {
      childList: true,
      subtree: true
    })
  }

  // 初始化已存在的diff容器
  processDiffContainers()
}

// —— 生成上下对比 HTML（替换前在上，替换后在下）——
const buildModifyDiffHtml = (filePath: string, oldCnt: string, newCnt: string): string => {
  const ops = diffLines(oldCnt, newCnt)
  const beforeRows: string[] = []
  const afterRows: string[] = []
  let beforeLineNo = 1
  let afterLineNo = 1

  for (const op of ops) {
    if (op.type === 'equal') {
      const val = escapeHtml(op.value)
      beforeRows.push(`<div class="line unchanged"><span class="gutter">${beforeLineNo++}</span><span class="content">${val}</span></div>`)
      afterRows.push(`<div class="line unchanged"><span class="gutter">${afterLineNo++}</span><span class="content">${val}</span></div>`)
    } else if (op.type === 'remove') {
      const val = escapeHtml(op.value)
      beforeRows.push(`<div class="line removed"><span class="gutter">${beforeLineNo++}</span><span class="content">${val}</span></div>`)
    } else if (op.type === 'add') {
      const val = escapeHtml(op.value)
      afterRows.push(`<div class="line added"><span class="gutter">${afterLineNo++}</span><span class="content">${val}</span></div>`)
    }
  }

  const safePath = escapeHtml(filePath || '未知文件')
  const uniqueId = `diff-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

  return (
    `<div class="diff-container vertical" id="${uniqueId}" data-auto-collapse="5000">
      <div class="diff-header">
        <span class="tool">修改文件</span>
        <span class="file-path">${safePath}</span>
      </div>
      <div class="diff-section">
        <div class="diff-title before-title collapsible" data-target="before">
          <span class="collapse-icon">▼</span>
          <span class="title-text">替换前</span>
          <span class="line-count">${beforeRows.length} 行</span>
        </div>
        <div class="diff-code before-code">${beforeRows.join('')}</div>
      </div>
      <div class="diff-section">
        <div class="diff-title after-title collapsible" data-target="after">
          <span class="collapse-icon">▼</span>
          <span class="title-text">替换后</span>
          <span class="line-count">${afterRows.length} 行</span>
        </div>
        <div class="diff-code after-code">${afterRows.join('')}</div>
      </div>
    </div>`
  )
}

// VUE项目专用：只格式化工具调用信息，过滤代码片段（对“修改文件”特殊渲染差异对比）
const formatVueProjectContent = (content: string): string => {
  if (!content) return ''

  let formattedContent = content

  // 1) 先将“修改文件”工具的替换前/后片段转换为自定义对比 HTML（保留其内容）
  const modifyPattern = /\[工具调用\]\s*修改文件\s+([^\n\r]+)[\s\S]*?替换前：\s*```(?:[\w-]*)?\s*([\s\S]*?)\s*```[\s\S]*?替换后：\s*```(?:[\w-]*)?\s*([\s\S]*?)\s*```/g
  formattedContent = formattedContent.replace(modifyPattern, (_m, filePath: string, oldBlock: string, newBlock: string) => {
    try {
      return buildModifyDiffHtml(filePath, oldBlock, newBlock)
    } catch (e) {
      console.warn('构建修改文件对比失败，将回退为原文本:', e)
      return _m
    }
  })

  // 2) 再移除其余完整/不完整代码块（避免影响其它工具的清理逻辑）
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 3) 移除特殊标记与 MULTI_FILE 标记
  formattedContent = formattedContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_START:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_CONTENT:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_END:[^\]]+\]/g, '')

  // 4) 移除步骤信息
  formattedContent = formattedContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 5) 移除单行代码反引号
  formattedContent = formattedContent.replace(/`([^`\n]+)`/g, '$1')

  // 6) 清理特定残留行
  formattedContent = formattedContent.replace(/^.*\[MULTI_FILE_CONTENT:.*$/gm, '')

  // 7) 工具标记格式化（其余工具保持原有样式）
  formattedContent = formattedContent.replace(/(\[选择工具\])/g, '\n$1')
  formattedContent = formattedContent.replace(/(\[工具调用\])/g, '\n$1')
  formattedContent = formattedContent.replace(/\[选择工具\]\s*([^\[\n\r]*)/g, (match, toolName) => {
    return `**[选择工具]** ${toolName.trim()}\n\n`
  })
  formattedContent = formattedContent.replace(/\[工具调用\]\s*([^\[\n\r]*)/g, (match, info) => {
    return `**[工具调用]** ${info.trim()}\n\n`
  })

  // 8) 清理多余空行
  formattedContent = formattedContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  formattedContent = formattedContent.replace(/^\n+/, '')

  return formattedContent.trim()
}


// 流式内容解析器
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const parseStreamingContent = (chunk: string, fullContent: string) => {
  try {
    // const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML  // 旧逻辑：支持多种类型
    // const codeGenType = CodeGenTypeEnum.VUE_PROJECT  // 新逻辑：固定使用VUE类型

    // // HTML类型使用简单的代码流式输出（已注释，仅保留VUE逻辑）
    // if (codeGenType === CodeGenTypeEnum.HTML) {
    //   parseSimpleCodeStreaming(chunk, fullContent)
    // }
    // // MULTI_FILE类型使用专用的多文件流式输出
    // else if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
    //   parseMultiFileStreaming(chunk, fullContent)
    // } else {
    //   // Vue项目类型的复杂处理逻辑
    // }
    
    // 新逻辑：固定使用VUE项目类型的复杂处理逻辑
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
    // }  // 已注释：原 else 分支的闭合

    // 同步UI状态到全局store，以便页面切换后恢复
    syncUIStateToStore()
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

// HTML 专用的简单代码流式处理（严格提取标记区间内的代码）
const parseSimpleCodeStreaming = (chunk: string, fullContent: string) => {
  try {
    // 仅提取 [CODE_BLOCK_START] 与 [CODE_BLOCK_END] 之间的内容；忽略区间外文本
    const START = '[CODE_BLOCK_START]'
    const END = '[CODE_BLOCK_END]'

    let remaining = chunk
    // 循环处理，支持同一分片内多个开始/结束标记
    while (remaining && remaining.length > 0) {
      if (!inSimpleCodeBlock.value) {
        const idxStart = remaining.indexOf(START)
        if (idxStart === -1) {
          // 本分片不含开始标记，忽略掉区间外文本
          break
        }

        // 丢弃开始标记之前的所有文本
        remaining = remaining.slice(idxStart + START.length)

        // 开启一个新的代码文件（仅在未处于代码块时）
        startSimpleCodeFile()
      }

      // 此时处于代码块内，查找结束标记
      const idxEnd = remaining.indexOf(END)
      if (idxEnd !== -1) {
        // 取结束标记之前的内容作为代码，移除中间的 [CODE_STREAM]
        const segment = remaining.slice(0, idxEnd).replace(/\[CODE_STREAM\]/g, '')
        if (segment) {
          updateSimpleCodeContent(segment)
        }

        // 跳过结束标记，完成当前代码块
        remaining = remaining.slice(idxEnd + END.length)
        completeSimpleCodeFile()

        // 继续循环以处理后续内容（可能还有下一个开始标记）
        continue
      } else {
        // 未出现结束标记，整段内容（去除 [CODE_STREAM]）均为代码流的一部分
        const segment = remaining.replace(/\[CODE_STREAM\]/g, '')
        if (segment) {
          updateSimpleCodeContent(segment)
        }
        // 本分片消费完毕
        break
      }
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
  // const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML  // 旧逻辑：支持多种类型
  // const codeGenType = CodeGenTypeEnum.VUE_PROJECT  // 新逻辑：固定使用VUE类型
  let fileName = 'index'
  let language = 'html'
  let fileExtension = '.html'

  // if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {  // 已注释：多文件模式
  //   fileName = 'main'
  //   language = 'javascript'
  //   fileExtension = '.js'
  // }
  
  // 新逻辑：VUE项目模式默认值（保持index.html）

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
  const existingFile = multiFiles.value.find(file => file.name === fileName)

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
  const cleanContent = content
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

  // 检查是否有正在进行的生成任务
  if (appGenerationStore.isGeneratingForApp(appId.value)) {
    // 恢复完整的UI状态
    restoreUIStateFromStore()
    message.info('检测到正在进行的生成任务，已自动恢复连接和生成状态')

    // 注意：EventSource 连接已经在全局 store 中，不需要重新创建
    // 消息的更新会通过已存在的 EventSource 回调继续进行
  }

  // 监听 iframe 消息
  window.addEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })

  // 监听浏览器关闭/刷新事件，只有在这种情况下才真正停止生成
  const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (appGenerationStore.isGenerating) {
      // 在浏览器关闭前提示用户
      e.preventDefault()
      e.returnValue = '正在生成中，确定要离开吗？'
    }
  }
  window.addEventListener('beforeunload', handleBeforeUnload)

  // 初始化diff折叠功能
  initDiffCollapse()

  // 初始化消息摘要折叠功能
  initMessageCollapse()

  // 在组件卸载时移除事件监听
  onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
  })
})

// 清理资源
onUnmounted(() => {
  window.removeEventListener('keydown', globalKeyHandler)
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

  // 重要修改：不再在组件卸载时关闭 EventSource 和停止生成
  // 这样即使用户离开页面,应用生成也会在后台继续进行
  // EventSource 连接保存在全局 store 中,由 store 管理生命周期
  // 只有在用户主动点击停止按钮时才会真正停止生成
})

// 监听路由参数变化，实现多应用切换
watch(
  () => route.params.id,
  async (newId, oldId) => {
    // 只有当ID真正变化时才重新初始化
    if (newId && newId !== oldId) {
      console.log(`[AppChatPage] 路由参数变化: ${oldId} -> ${newId}`)

      // 提示用户正在切换应用
      const loadingMsg = message.loading('正在加载新应用...', 0)

      try {
        // 如果当前有正在生成的任务，但不是这个新应用的任务，则清理旧状态
        if (appGenerationStore.isGenerating && appGenerationStore.generatingAppId !== newId) {
          console.log(`[AppChatPage] 检测到正在为其他应用生成，停止旧任务并清理状态`)
          // 停止旧的生成任务
          appGenerationStore.stopGeneration()
        }

        // 清理当前组件的UI状态，准备显示新应用
        console.log(`[AppChatPage] 清理UI状态，准备加载新应用`)
        messages.value = []
        completedFiles.value = []
        currentGeneratingFile.value = null
        simpleCodeFile.value = null
        multiFiles.value = []
        currentMultiFile.value = null
        multiFileContents.value = {}
        generationFinished.value = false
        isGenerating.value = false
        previewReady.value = false

        // 重新获取应用信息
        await fetchAppInfo()

        // 加载历史消息
        await loadChatHistory()

        loadingMsg()
        message.success('应用加载完成')
      } catch (error) {
        loadingMsg()
        console.error('[AppChatPage] 加载应用失败:', error)
        message.error('加载应用失败，请重试')
      }
    }
  },
  { immediate: false }
)
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

/* 左侧面板 */
.left-panel {
  width: 50%;
  height: 100%;
  min-width: 320px;
  background: var(--bg-primary);
  border-right: none;
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  overflow: hidden;
  transition: width 0.1s ease;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
}

/* 右侧面板 */
.right-panel {
  width: 50%;
  height: 100%;
  min-width: 480px;
  background: #FFFFFF;
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  overflow: hidden;
  transition: width 0.1s ease;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: row;
}

/* ========== 中间分隔条 - Lovable风格 ========== */
.resizer {
  width: 12px; /* 增加宽度，更容易抓取 */
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 140, 66, 0.12) 100%);
  cursor: col-resize;
  position: relative;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow:
    0 0 20px rgba(255, 107, 53, 0.06),
    inset 0 0 10px rgba(255, 107, 53, 0.03);
  flex-shrink: 0;
  z-index: 10;
  user-select: none;
  /* 触摸设备支持 */
  touch-action: none;
  -webkit-touch-callout: none;
  -webkit-user-select: none;
}

.resizer:hover {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.2) 0%, rgba(255, 140, 66, 0.3) 100%);
  box-shadow:
    0 0 30px rgba(255, 107, 53, 0.15),
    inset 0 0 15px rgba(255, 107, 53, 0.1);
  transform: scaleX(1.4);
}

/* 拖动中的样式 */
.resizer.is-resizing {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.35) 0%, rgba(255, 140, 66, 0.45) 100%);
  box-shadow:
    0 0 40px rgba(255, 107, 53, 0.3),
    inset 0 0 20px rgba(255, 107, 53, 0.2);
  transform: scaleX(1.6);
}

.resizer:active {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.3) 0%, rgba(255, 140, 66, 0.4) 100%);
  box-shadow:
    0 0 40px rgba(255, 107, 53, 0.25),
    inset 0 0 20px rgba(255, 107, 53, 0.15);
  transform: scaleX(1.6);
}

/* 中间的拖动把手 */
.resizer::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4px;
  height: 50px;
  background: linear-gradient(
    180deg,
    rgba(255, 107, 53, 0.4) 0%,
    rgba(255, 140, 66, 0.7) 50%,
    rgba(255, 107, 53, 0.4) 100%
  );
  border-radius: 10px;
  opacity: 0.6;
  transition: all 0.2s ease;
}

.resizer:hover::before,
.resizer.is-resizing::before {
  opacity: 1;
  height: 60px;
  width: 5px;
}

.resizer:active::before {
  opacity: 1;
  width: 5px;
  height: 60px;
  background: linear-gradient(
    180deg,
    rgba(255, 107, 53, 0.6) 0%,
    rgba(255, 140, 66, 0.9) 50%,
    rgba(255, 107, 53, 0.6) 100%
  );
}

/* ========== 左侧聊天面板内容 ========== */

.messages-container {
  flex: 1;
  padding: 24px 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-item {
  margin-bottom: 24px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 12px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 12px;
}

.message-content {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-content {
  max-width: 65%;
  background: linear-gradient(135deg, #f8f6f0 0%, #faf8f3 100%);
  color: #2d3748;
  border-radius: 24px;
  border-bottom-right-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.ai-message .message-content {
  max-width: 90%;
  background: transparent;
  color: #1a1a1a;
  padding: 12px 16px;
}

.message-avatar {
  flex-shrink: 0;
}

/* AI头像图标样式 */
.ai-avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: contain;
  display: block;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 思考完成标签 - 米色雾化风格 */
.thinking-complete {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(250, 235, 215, 0.6) 0%, rgba(255, 248, 220, 0.5) 100%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(222, 184, 135, 0.3);
  border-radius: 20px;
  margin-bottom: 12px;
  box-shadow:
    0 4px 16px rgba(222, 184, 135, 0.15),
    0 2px 8px rgba(255, 255, 255, 0.5) inset;
  animation: thinkingFadeIn 0.4s ease;
}

@keyframes thinkingFadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.thinking-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  filter: drop-shadow(0 1px 2px rgba(139, 90, 43, 0.3));
  opacity: 0.85;
}

.thinking-duration {
  color: #8B5A2B;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.empty-message {
  color: #999;
  font-style: italic;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 文字呼吸效果 */
.empty-message-text {
  animation: breathe 3s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 0.8;
  }
}

/* 加载点动画 */
.loading-dots {
  display: inline-flex;
  gap: 1px;
}

.loading-dots span {
  animation: dot-fade 1.5s ease-in-out infinite;
  opacity: 0.4;
}

.loading-dots span:nth-child(1) {
  animation-delay: 0s;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.3s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes dot-fade {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 1;
  }
}

/* 加载更多按钮 */
.load-more-container {
  text-align: center;
  padding: 8px 0;
  margin-bottom: 16px;
}

/* 工具步骤区域样式 - 优化版本 */
.steps-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 2px solid #f0f2f5;
}

.steps-header {
  padding: 12px 0 16px;

  h4 {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    letter-spacing: 0.3px;
  }
}

.steps-container {
  .step-item {
    margin-bottom: 16px;
    padding: 16px;
    background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
    border: 1px solid #e8eaed;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    /* 添加左侧装饰条 */
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: linear-gradient(109deg, rgb(0, 56, 255) 9.43%, rgb(0, 209, 255) 96.31%);
      opacity: 0.3;
      transition: opacity 0.3s ease;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

      &::before {
        opacity: 1;
      }
    }

    &.step-running {
      background: linear-gradient(135deg, #e8f4ff 0%, #d6ebff 100%);
      border-color: #91d5ff;
      box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);

      &::before {
        background: linear-gradient(180deg, #1890ff 0%, #0050b3 100%);
        opacity: 1;
      }
    }

    &.step-completed {
      background: linear-gradient(135deg, #f6ffed 0%, #edf9e6 100%);
      border-color: #b7eb8f;

      &::before {
        background: linear-gradient(180deg, #52c41a 0%, #389e0d 100%);
        opacity: 1;
      }
    }

    &:last-child {
      margin-bottom: 0;
    }

    .step-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .step-number {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 28px;
        height: 28px;
        background: linear-gradient(109deg, rgb(0, 56, 255) 9.43%, rgb(0, 209, 255) 96.31%);
        color: white;
        border-radius: 50%;
        font-weight: 700;
        font-size: 13px;
        box-shadow: 0 3px 8px rgba(0, 56, 255, 0.3);
      }

      .step-title {
        flex: 1;
        font-size: 14px;
        font-weight: 600;
        color: #1a1a1a;
        letter-spacing: 0.2px;
      }
    }

    .tool-calls {
      .tool-call-item {
        margin: 8px 0;
        padding: 12px;
        background: white;
        border-radius: 8px;
        border: 1px solid #e8eaed;
        transition: all 0.2s ease;

        &:hover {
          border-color: rgb(0, 56, 255);
          box-shadow: 0 2px 8px rgba(0, 56, 255, 0.15);
        }

        .tool-selection {
          margin-bottom: 6px;
          color: rgb(0, 56, 255);
          font-weight: 500;
          font-size: 13px;
        }

        .tool-execution {
          .tool-action {
            display: inline-block;
            font-weight: 600;
            color: #1a1a1a;
            margin-right: 10px;
            padding: 2px 8px;
            background: linear-gradient(135deg, #f0f4ff 0%, #e8edff 100%);
            border-radius: 4px;
            font-size: 12px;
          }

          .file-path {
            color: #666;
            font-family: 'Monaco', 'Menlo', 'Cascadia Code', monospace;
            font-size: 12px;
            word-break: break-all;
            background: #f5f5f5;
            padding: 2px 6px;
            border-radius: 3px;
          }

          .operation-desc {
            margin: 6px 0 0 0;
            font-size: 12px;
            color: #666;
            line-height: 1.5;
          }
        }
      }
    }
  }
}

/* 输入框卡片 - Lovable风格 */
.input-wrapper {
  position: relative;
  background: var(--white);
  border-radius: 20px;
  padding: 10px 14px;
  margin: 12px;
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.1),
    0 2px 8px rgba(255, 107, 53, 0.08),
    0 8px 24px rgba(255, 140, 66, 0.06);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

/* AI模型选择器弹出层 - 固定在输入框上方 */
.model-selector-popup {
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  z-index: 1000;
  background: white;
  border-radius: 16px;
  border: 2px solid rgba(255, 107, 53, 0.15);
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.08),
    0 8px 24px rgba(255, 107, 53, 0.15),
    0 16px 48px rgba(255, 107, 53, 0.1);
  overflow: hidden;
  animation: popupFadeIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  min-width: 360px;
  max-height: 480px;
}

@keyframes popupFadeIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.input-wrapper:hover {
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.15),
    0 4px 12px rgba(255, 107, 53, 0.12),
    0 12px 32px rgba(255, 140, 66, 0.1);
  transform: translateY(-1px);
}

.input-wrapper:focus-within {
  box-shadow:
    0 0 0 2px rgba(255, 107, 53, 0.3),
    0 4px 16px rgba(255, 107, 53, 0.15),
    0 12px 40px rgba(255, 140, 66, 0.12);
  transform: translateY(-1px);
}

/* textarea 样式优化 - Lovable风格 */
.input-wrapper textarea.ant-input {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
  background: transparent;
  font-size: 15px;
  line-height: 22px;
  resize: none;
  padding: 2px 0;
  color: #1a1a1a;
  width: 100%;
  margin-bottom: 6px;
  min-height: 44px;
  max-height: 200px;
  overflow-y: auto;
  transition: height 0.1s ease;
}

.input-wrapper textarea.ant-input::placeholder {
  color: #9ca3af;
}

.input-wrapper textarea.ant-input:focus,
.input-wrapper textarea.ant-input:hover {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}

/* textarea 滚动条样式 */
.input-wrapper textarea.ant-input::-webkit-scrollbar {
  width: 4px;
}

.input-wrapper textarea.ant-input::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.input-wrapper textarea.ant-input::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* 底部按钮行 - 左右对齐 */
.input-actions-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

/* 左下角按钮组 */
.input-left-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 右下角按钮组 */
.input-right-actions {
  display: flex;
  align-items: center;
}

/* 操作按钮通用样式 - Lovable风格 */
.action-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f3f4f6;
  color: #6b7280;
  font-size: 16px;
}

.action-btn:hover {
  background: #e5e7eb;
  transform: scale(1.05);
  color: #ff6b35;
}

/* 模型列表按钮 */
.model-btn {
  padding: 0;
  overflow: hidden;
}

.model-btn .model-icon-img {
  width: 20px;
  height: 20px;
  object-fit: contain;
  border-radius: 50%;
}

.model-btn:hover {
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.2);
}

/* 编辑模式按钮 */
.edit-btn.active {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.edit-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

/* Lovable 风格的发送按钮 - 橙色渐变 */
.stream-toggle {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  line-height: 1;
  font-size: 18px;
  box-shadow:
    0 2px 8px rgba(255, 107, 53, 0.25),
    0 4px 16px rgba(255, 140, 66, 0.15);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  align-self: flex-end;
  position: static;
}

.stream-toggle:not(:disabled):hover {
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b35 100%);
  transform: scale(1.1) rotate(5deg);
  box-shadow:
    0 4px 16px rgba(255, 107, 53, 0.35),
    0 8px 24px rgba(255, 140, 66, 0.2);
}

.stream-toggle:not(:disabled):active {
  transform: scale(0.95);
}

.stream-toggle:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.5;
  box-shadow: none;
}

.stream-toggle .send-icon {
  font-size: 18px;
}

/* 右侧代码生成区域 */
.code-generation-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  border-left: 1px solid #e5e5e5;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e5e5;
  background: white;

  h3 {
    margin: 0;
    font-size: 15px;
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

/* 代码和预览视图容器：占满整个右侧面板 */
.code-view-container,
.preview-view-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* VSCode 风格布局：横向排列 */
.code-view-container.vscode-layout {
  flex-direction: row;
}

/* ========== VSCode 风格左右分栏布局 ========== */
/* 左侧：文件树 */
.file-explorer {
  display: flex;
  flex-direction: column;
  background: #FAF6F0;
  border-right: 1px solid #E8E4DC;
  overflow: hidden;
  flex-shrink: 0;
}

.explorer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 35px;
  padding: 0 12px;
  background: #FAF6F0;
  border-bottom: 1px solid #E8E4DC;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  color: #586069;
  letter-spacing: 0.5px;
}

.title-icon {
  font-size: 14px;
  color: var(--color-primary);
}

.explorer-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.project-root {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 12px;
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #24292E;
  cursor: pointer;
  user-select: none;
}

.project-root:hover {
  background: #F0E8DC;
}

.root-icon {
  font-size: 16px;
  color: var(--color-primary);
}

.root-name {
  font-family: 'Consolas', 'Courier New', monospace;
}

.file-list {
  display: flex;
  flex-direction: column;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 12px 0 8px; /* 左侧padding由动态style控制 */
  cursor: pointer;
  transition: background 0.15s ease;
  user-select: none;
  position: relative;
}

.file-item:hover {
  background: #F0E8DC;
}

.file-item.active {
  background: linear-gradient(90deg,
    rgba(255, 107, 53, 0.08) 0%,
    rgba(255, 140, 66, 0.04) 100%);
  color: #24292E;
}

/* 目录样式 */
.file-item.is-folder {
  font-weight: 500;
}

/* 展开/收起箭头 */
.folder-arrow {
  font-size: 12px;
  color: #586069;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.file-icon {
  font-size: 16px;
  color: #586069;
  flex-shrink: 0;
}

/* 文件图标额外缩进（与有箭头的目录对齐）*/
.file-icon-indent {
  margin-left: 18px;
}

.file-name {
  flex: 1;
  font-size: 13px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: #24292E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-item.active .file-name {
  color: #24292E;
  font-weight: 500;
}

.file-badge {
  padding: 2px 6px;
  background: rgba(255, 107, 53, 0.2);
  color: var(--color-primary);
  font-size: 10px;
  border-radius: 3px;
  font-weight: 500;
}

.explorer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #6A6A6A;
}

.empty-icon-small {
  font-size: 24px;
  margin-bottom: 8px;
  opacity: 0.5;
}

.empty-text-small {
  font-size: 12px;
  color: var(--code-comment);
  margin: 0;
}

/* 竖向分隔条 */
.vertical-resizer {
  width: 4px;
  background: #3E3E42;
  cursor: col-resize;
  position: relative;
  transition: background 0.2s ease;
  flex-shrink: 0;
}

.vertical-resizer:hover {
  background: linear-gradient(180deg,
    rgba(255, 107, 53, 0.3) 0%,
    rgba(255, 140, 66, 0.3) 100%);
}

.resizer-handle {
  position: absolute;
  top: 0;
  left: -2px;
  right: -2px;
  bottom: 0;
}

/* 右侧：代码编辑器区域 */
.code-editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #FFFFFF;
}

/* 文件标签栏 */
.file-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F6F8FA;
  border-bottom: 1px solid #E1E4E8;
  overflow-x: auto;
  overflow-y: hidden;
  height: 35px;
  flex-shrink: 0;
  padding-right: 8px;
}

.file-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 35px;
  padding: 0 12px;
  background: #FFFFFF;
  color: #586069;
  border-right: 1px solid #E1E4E8;
  font-size: 13px;
  font-family: 'Consolas', 'Courier New', monospace;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  user-select: none;
  position: relative;
}

.file-tab:hover {
  background: #F6F8FA;
  color: #24292E;
}

.file-tab.active {
  background: var(--code-bg);
  color: var(--code-text);
  border-bottom: 2px solid var(--color-primary);
}

.tab-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.tab-name {
  font-size: 13px;
}

/* 复制按钮图标 */
.copy-btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  color: var(--code-comment);
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.copy-btn-icon:hover {
  background: var(--code-bg-highlight);
  color: var(--color-primary);
}

/* 历史版本按钮图标 */
.history-btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-left: auto;
  margin-right: 8px;
}

.history-btn-icon:hover {
  background: var(--code-bg-highlight);
}

.version-history-icon {
  width: 18px;
  height: 18px;
  display: block;
}

/* 编辑器主体 */
.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-width: max-content;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 16px;
  background: var(--code-bg-highlight);
  border-bottom: 1px solid #E1E4E8;
  flex-shrink: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-path {
  font-size: 12px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: var(--code-comment);
}

.file-stats {
  font-size: 11px;
  color: var(--code-comment);
  font-family: 'Consolas', 'Courier New', monospace;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: transparent;
  color: var(--code-comment);
  border: 1px solid #E1E4E8;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: var(--code-bg-highlight);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.code-content {
  flex: 1;
  overflow: visible;
  background: var(--code-bg);
  position: relative;
  min-width: max-content;
}

/* 状态栏 */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 22px;
  padding: 0 12px;
  background: #007ACC;
  font-size: 12px;
  color: #FFFFFF;
  flex-shrink: 0;
}

.status-left,
.status-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'Consolas', 'Courier New', monospace;
}

.status-icon {
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6A6A6A;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--code-comment);
}

.empty-hint {
  font-size: 14px;
  color: var(--code-comment);
}

/* 滚动条样式 */
.explorer-content::-webkit-scrollbar,
.file-tabs::-webkit-scrollbar,
.editor-main::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.explorer-content::-webkit-scrollbar-track,
.file-tabs::-webkit-scrollbar-track,
.editor-main::-webkit-scrollbar-track {
  background: var(--code-bg);
}

.explorer-content::-webkit-scrollbar-thumb,
.file-tabs::-webkit-scrollbar-thumb,
.editor-main::-webkit-scrollbar-thumb {
  background: #C8C8C8;
  border-radius: 5px;
}

.explorer-content::-webkit-scrollbar-thumb:hover,
.file-tabs::-webkit-scrollbar-thumb:hover,
.editor-main::-webkit-scrollbar-thumb:hover {
  background: #A8A8A8;
}

/* 预览容器：生成完成后填满右侧面板 */
.preview-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
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
  /* 手机端：左右面板改为上下布局 */
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

  /* 手机端：拖动条改为横向 */
  .resizer {
    width: 100%;
    height: 12px; /* 手机端改为横向拖动条 */
    cursor: row-resize;
    /* 纵向拖动 */
    touch-action: pan-y;
  }

  .resizer::before {
    width: 50px;
    height: 4px;
    background: linear-gradient(90deg,
      rgba(255, 107, 53, 0.4) 0%,
      rgba(255, 140, 66, 0.7) 50%,
      rgba(255, 107, 53, 0.4) 100%);
  }

  .resizer:hover::before,
  .resizer.is-resizing::before {
    width: 60px;
    height: 5px;
  }

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

  /* 版本管理样式 */
  .version-item {
    padding: 8px 0;
  }

  .version-header {
    margin-bottom: 12px;
  }

  .version-header h4 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }

  .version-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .version-info-item {
    margin: 0;
    color: #666;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .version-icon {
    font-size: 16px;
    color: #1890ff;
    flex-shrink: 0;
  }

  .version-actions {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    padding-left: 24px;
  }

  .version-dot {
    font-size: 12px;
    font-weight: bold;
    color: #1890ff;
  }

  .version-detail {
    padding: 16px 0;
  }

  /* —— 消息摘要栏样式（Lovable 风格）—— */
  :deep(.message-summary-bar) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 8px 10px 14px;
    margin: 0 0 16px 0;
    background: #f5f5f5;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    cursor: pointer;
    user-select: none;
    transition: all 0.2s ease;
  }

  :deep(.message-summary-bar:hover) {
    background: #ececec;
    border-color: #d0d0d0;
  }

  /* SVG图标 */
  :deep(.message-summary-bar .summary-icon) {
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    flex-shrink: 0;
    color: #666;
  }

  :deep(.message-summary-bar .summary-icon svg) {
    display: block;
  }

  /* 统计文本 */
  :deep(.message-summary-bar .summary-text) {
    flex: 1;
    color: #666;
    font-size: 13px;
    font-weight: 500;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  /* 按钮 - 米色主题，增强层次感 */
  :deep(.message-content .message-summary-bar .summary-toggle-btn) {
    padding: 7px 14px !important;
    background: linear-gradient(135deg, #f8f6f0 0%, #e8e3d8 100%) !important;
    color: #2d3748 !important;
    border: 1px solid #d4cfc4 !important;
    border-radius: 12px !important;
    font-size: 13px !important;
    font-weight: 600 !important;
    cursor: pointer !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
    outline: none !important;
    white-space: nowrap !important;
    flex-shrink: 0 !important;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12),
                0 1px 3px rgba(0, 0, 0, 0.08),
                inset 0 1px 0 rgba(255, 255, 255, 0.5) !important;
    letter-spacing: 0.3px !important;
  }

  :deep(.message-content .message-summary-bar .summary-toggle-btn:hover) {
    background: linear-gradient(135deg, #e8e3d8 0%, #d4cfc4 100%) !important;
    border-color: #c9c3b8 !important;
    color: #1a1a1a !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.16),
                0 2px 6px rgba(0, 0, 0, 0.12),
                inset 0 1px 0 rgba(255, 255, 255, 0.6) !important;
    transform: translateY(-2px) !important;
  }

  :deep(.message-content .message-summary-bar .summary-toggle-btn:active) {
    transform: translateY(0px) scale(0.97) !important;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12),
                0 1px 2px rgba(0, 0, 0, 0.08),
                inset 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  }
}

/* 刷新提示样式 - 米色主题，与背景一体化 */
.refresh-reminder {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f6f0 0%, #e8e3d8 100%);
  border: 2px solid #d4cfc4;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08);
  animation: gentle-pulse 2s ease-in-out infinite;
}

.refresh-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
  animation: rotate-refresh 3s linear infinite;
}

.refresh-text {
  font-size: 16px;
  font-weight: 700;
  color: #5d4e37;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.6);
  line-height: 1.5;
}

/* 脉冲动画 */
@keyframes gentle-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15), 0 3px 8px rgba(0, 0, 0, 0.1);
  }
}

/* 旋转动画 */
@keyframes rotate-refresh {
  0% {
    transform: rotate(0deg);
  }
  10% {
    transform: rotate(360deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
