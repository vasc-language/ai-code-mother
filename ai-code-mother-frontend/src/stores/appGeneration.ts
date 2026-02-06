import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 消息接口
 */
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
}

/**
 * 文件接口
 */
interface FileInfo {
  id: string
  name: string
  path: string
  content: string
  language: string
  completed: boolean
  generatedAt: string
  lastUpdated?: string
}

/**
 * 应用生成全局状态管理
 * 用于在页面切换时保持生成状态，避免中断生成过程
 */
export const useAppGenerationStore = defineStore('appGeneration', () => {
  // 当前正在生成的应用ID
  const generatingAppId = ref<string | null>(null)

  // EventSource 连接实例（全局单例）
  const eventSource = ref<EventSource | null>(null)

  // 当前运行ID
  const currentRunId = ref<string | null>(null)

  // 是否正在生成
  const isGenerating = ref(false)

  // UI状态：消息列表
  const messages = ref<Message[]>([])

  // UI状态：当前AI消息索引
  const currentAiMessageIndex = ref<number | null>(null)

  // UI状态：最后一条用户消息
  const lastUserMessage = ref('')

  // UI状态：已完成的文件列表
  const completedFiles = ref<FileInfo[]>([])

  // UI状态：当前正在生成的文件
  const currentGeneratingFile = ref<FileInfo | null>(null)

  // UI状态：简单代码文件（HTML类型）
  const simpleCodeFile = ref<FileInfo | null>(null)

  // UI状态：多文件列表
  const multiFiles = ref<FileInfo[]>([])

  // UI状态：当前多文件名称
  const currentMultiFile = ref<string | null>(null)

  // UI状态：多文件内容映射
  const multiFileContents = ref<Record<string, string>>({})

  // UI状态：生成是否完成
  const generationFinished = ref(false)

  /**
   * 开始生成
   */
  function startGeneration(appId: string, runId: string, source: EventSource) {
    generatingAppId.value = appId
    currentRunId.value = runId
    eventSource.value = source
    isGenerating.value = true
  }

  /**
   * 完成生成
   */
  function finishGeneration() {
    // 生成完成后必须主动关闭 EventSource，避免浏览器自动重连造成重复请求
    if (eventSource.value) {
      try {
        eventSource.value.close()
      } catch (e) {
        console.error('关闭 EventSource 失败:', e)
      }
      eventSource.value = null
    }

    isGenerating.value = false
    generationFinished.value = true
    generatingAppId.value = null
    currentRunId.value = null
  }

  /**
   * 停止生成（用户主动停止或出错）
   */
  function stopGeneration() {
    if (eventSource.value) {
      try {
        eventSource.value.close()
      } catch (e) {
        console.error('关闭 EventSource 失败:', e)
      }
      eventSource.value = null
    }

    isGenerating.value = false
    generatingAppId.value = null
    currentRunId.value = null
  }

  /**
   * 更新UI状态
   */
  function updateUIState(state: {
    messages?: Message[]
    currentAiMessageIndex?: number | null
    lastUserMessage?: string
    completedFiles?: FileInfo[]
    currentGeneratingFile?: FileInfo | null
    simpleCodeFile?: FileInfo | null
    multiFiles?: FileInfo[]
    currentMultiFile?: string | null
    multiFileContents?: Record<string, string>
    generationFinished?: boolean
  }) {
    if (state.messages !== undefined) messages.value = state.messages
    if (state.currentAiMessageIndex !== undefined)
      currentAiMessageIndex.value = state.currentAiMessageIndex
    if (state.lastUserMessage !== undefined) lastUserMessage.value = state.lastUserMessage
    if (state.completedFiles !== undefined) completedFiles.value = state.completedFiles
    if (state.currentGeneratingFile !== undefined)
      currentGeneratingFile.value = state.currentGeneratingFile
    if (state.simpleCodeFile !== undefined) simpleCodeFile.value = state.simpleCodeFile
    if (state.multiFiles !== undefined) multiFiles.value = state.multiFiles
    if (state.currentMultiFile !== undefined) currentMultiFile.value = state.currentMultiFile
    if (state.multiFileContents !== undefined) multiFileContents.value = state.multiFileContents
    if (state.generationFinished !== undefined)
      generationFinished.value = state.generationFinished
  }

  /**
   * 清理UI状态
   */
  function clearUIState() {
    messages.value = []
    currentAiMessageIndex.value = null
    lastUserMessage.value = ''
    completedFiles.value = []
    currentGeneratingFile.value = null
    simpleCodeFile.value = null
    multiFiles.value = []
    currentMultiFile.value = null
    multiFileContents.value = {}
    generationFinished.value = false
  }

  /**
   * 清理资源（仅在真正需要停止时调用）
   */
  function cleanup() {
    stopGeneration()
    clearUIState()
  }

  /**
   * 检查是否正在为指定应用生成
   */
  function isGeneratingForApp(appId: string): boolean {
    return isGenerating.value && generatingAppId.value === appId
  }

  return {
    // 状态
    generatingAppId,
    eventSource,
    currentRunId,
    isGenerating,
    messages,
    currentAiMessageIndex,
    lastUserMessage,
    completedFiles,
    currentGeneratingFile,
    simpleCodeFile,
    multiFiles,
    currentMultiFile,
    multiFileContents,
    generationFinished,

    // 方法
    startGeneration,
    finishGeneration,
    stopGeneration,
    updateUIState,
    clearUIState,
    cleanup,
    isGeneratingForApp,
  }
})
