<template>
  <div class="chat-panel">
    <!-- 消息列表区域 -->
    <div class="messages-container" ref="messagesContainerRef">
      <!-- 加载更多按钮 -->
      <div v-if="hasMoreHistory" class="load-more-container">
        <a-button type="link" @click="$emit('load-more')" :loading="loadingHistory" size="small">
          加载更多历史消息
        </a-button>
      </div>

      <!-- 消息列表 -->
      <div v-for="(message, index) in messages" :key="index" class="message-item">
        <!-- 用户消息 -->
        <div v-if="message.type === 'user'" class="user-message">
          <div class="message-content">{{ message.content }}</div>
          <div class="message-avatar">
            <a-avatar :src="userAvatar" />
          </div>
        </div>

        <!-- AI消息 -->
        <div v-else class="ai-message">
          <div class="message-avatar">
            <img :src="aiAvatar" alt="AI" class="ai-avatar-img" />
          </div>
          <div class="message-content">
            <!-- 加载中 -->
            <div v-if="message.loading && !message.content" class="loading-indicator">
              <a-spin size="small" />
              <span>AI 正在思考...</span>
            </div>
            <!-- Markdown内容 -->
            <MarkdownRenderer v-else-if="message.content" :content="message.content" />
            <!-- 空消息 -->
            <div v-else class="empty-message">等待AI响应...</div>
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
      @close="$emit('clear-selected-element')"
    >
      <template #message>
        <div class="selected-element-info">
          <div class="element-header">
            <span class="element-tag">
              选中元素:{{ selectedElementInfo.tagName.toLowerCase() }}
            </span>
            <span v-if="selectedElementInfo.id" class="element-id">
              #{{ selectedElementInfo.id }}
            </span>
            <span v-if="selectedElementInfo.className" class="element-class">
              .{{ selectedElementInfo.className }}
            </span>
          </div>
          <div v-if="selectedElementInfo.textContent" class="element-text">
            内容: {{ selectedElementInfo.textContent.substring(0, 50) }}...
          </div>
        </div>
      </template>
    </a-alert>

    <!-- 输入区域 -->
    <div class="input-container">
      <a-textarea
        v-model:value="inputValue"
        :placeholder="inputPlaceholder"
        :auto-size="{ minRows: 1, maxRows: 4 }"
        class="message-input"
        @keydown="handleKeydown"
        :disabled="!isOwner"
      />
      <a-button
        type="primary"
        :disabled="primaryActionDisabled"
        :loading="isGenerating"
        @click="$emit('primary-action')"
        class="send-button"
      >
        <template #icon>
          <SendOutlined v-if="!isGenerating" />
        </template>
        {{ primaryActionTitle }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { SendOutlined } from '@ant-design/icons-vue'
import type { Message } from '../composables/useChatMessages'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

interface Props {
  messages: Message[]
  messagesContainer?: HTMLElement
  hasMoreHistory: boolean
  loadingHistory: boolean
  userAvatar?: string
  aiAvatar: string
  isEditMode?: boolean
  selectedElementInfo?: any
  userInput: string
  isOwner: boolean
  isGenerating?: boolean
  primaryActionDisabled?: boolean
  primaryActionTitle?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'load-more': []
  'clear-selected-element': []
  'primary-action': []
  'update:userInput': [value: string]
  keydown: [e: KeyboardEvent]
}>()

const messagesContainerRef = ref<HTMLElement>()
const inputValue = ref(props.userInput)

// 同步输入值
watch(
  () => props.userInput,
  (newVal) => {
    inputValue.value = newVal
  }
)

watch(inputValue, (newVal) => {
  emit('update:userInput', newVal)
})

const inputPlaceholder = props.isOwner
  ? '输入消息内容，Enter 发送，Shift+Enter 换行'
  : '无法在别人的作品下对话哦~'

const handleKeydown = (e: KeyboardEvent) => {
  emit('keydown', e)
}

defineExpose({
  messagesContainerRef,
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-more-container {
  text-align: center;
  padding: 10px 0;
}

.message-item {
  display: flex;
  flex-direction: column;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: flex-start;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
  padding: 12px 16px;
  border-radius: 12px;
  max-width: 70%;
  word-break: break-word;
  white-space: pre-wrap;
}

.ai-message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.ai-message .message-content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  max-width: 70%;
  word-break: break-word;
}

.ai-avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: contain;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.empty-message {
  color: #999;
}

.selected-element-alert {
  margin: 0 20px;
}

.selected-element-info {
  font-size: 13px;
}

.element-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}

.element-tag {
  background: #1890ff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.element-id,
.element-class {
  color: #666;
  font-size: 12px;
}

.element-text {
  color: #666;
  font-size: 12px;
  margin-top: 4px;
}

.input-container {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e8e8e8;
}

.message-input {
  flex: 1;
}

.send-button {
  align-self: flex-end;
}
</style>
