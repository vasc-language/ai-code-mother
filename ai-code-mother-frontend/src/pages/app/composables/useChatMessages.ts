import { ref, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { CodeGenTypeEnum } from '@/utils/codeGenTypes'

// 消息类型定义
export interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
  thinkingStartTime?: number // 思考开始时间戳
  thinkingDuration?: number // 思考持续时间（秒）
}

/**
 * 聊天消息管理Composable
 * 负责消息列表的维护、历史记录加载和消息滚动
 */
export function useChatMessages() {
  // 消息列表
  const messages = ref<Message[]>([])
  const userInput = ref('')
  const messagesContainer = ref<HTMLElement>()

  // 历史加载状态
  const loadingHistory = ref(false)
  const hasMoreHistory = ref(false)
  const lastCreateTime = ref<string>()
  const historyLoaded = ref(false)

  /**
   * 加载聊天历史
   */
  const loadChatHistory = async (
    appId: any,
    codeGenType: string | undefined,
    isLoadMore = false,
    filterFunctions: {
      filterHtmlContent: (content: string) => string
      filterOutCodeBlocks: (content: string) => string
      formatVueProjectContent: (content: string) => string
    }
  ) => {
    if (!appId || loadingHistory.value) return

    loadingHistory.value = true
    try {
      const params: API.listAppChatHistoryParams = {
        appId: appId,
        pageSize: 10,
      }

      // 如果是加载更多,传递最后一条消息的创建时间作为游标
      if (isLoadMore && lastCreateTime.value) {
        params.lastCreateTime = lastCreateTime.value
      }

      const res = await listAppChatHistory(params)
      if (res.data.code === 0 && res.data.data) {
        const chatHistories = res.data.data.records || []

        if (chatHistories.length > 0) {
          // 将对话历史转换为消息格式,并按时间正序排列
          const historyMessages: Message[] = chatHistories
            .map((chat) => {
              let content = chat.message || ''
              // 如果是AI消息,根据项目类型过滤掉代码相关信息
              if (chat.messageType === 'ai') {
                if (codeGenType === CodeGenTypeEnum.HTML) {
                  content = filterFunctions.filterHtmlContent(content)
                } else if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
                  content = filterFunctions.filterOutCodeBlocks(content)
                } else if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
                  content = filterFunctions.formatVueProjectContent(content)
                }
              }
              return {
                type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
                content: content,
                createTime: chat.createTime,
              }
            })
            .reverse() // 反转数组,让老消息在前

          if (isLoadMore) {
            // 加载更多时,将历史消息添加到开头
            messages.value.unshift(...historyMessages)
          } else {
            // 初始加载,直接设置消息列表
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
      console.error('加载对话历史失败:', error)
      message.error('加载对话历史失败')
    } finally {
      loadingHistory.value = false
    }
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  }

  /**
   * 添加用户消息
   */
  const addUserMessage = (content: string) => {
    messages.value.push({
      type: 'user',
      content,
    })
    scrollToBottom()
  }

  /**
   * 添加AI消息占位符
   */
  const addAiMessagePlaceholder = () => {
    const aiMessageIndex = messages.value.length
    messages.value.push({
      type: 'ai',
      content: '',
      loading: true,
      thinkingStartTime: Date.now(), // 记录开始时间
    })
    scrollToBottom()
    return aiMessageIndex
  }

  /**
   * 更新AI消息内容
   */
  const updateAiMessage = (index: number, content: string, loading = false) => {
    if (messages.value[index]) {
      messages.value[index].content = content
      messages.value[index].loading = loading

      // 如果思考结束（loading变为false），计算思考持续时间
      if (!loading && messages.value[index].thinkingStartTime) {
        const endTime = Date.now()
        const startTime = messages.value[index].thinkingStartTime!
        const durationMs = endTime - startTime
        messages.value[index].thinkingDuration = Math.round(durationMs / 1000) // 转换为秒并四舍五入
      }
    }
  }

  return {
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
  }
}
