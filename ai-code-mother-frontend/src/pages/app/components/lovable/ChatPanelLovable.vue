<template>
  <div class="chat-panel-lovable">
    <!-- 消息容器 -->
    <div class="messages-wrapper" ref="messagesWrapperRef">
      <!-- 加载更多历史消息 -->
      <div v-if="hasMoreHistory" class="load-more-section">
        <button class="load-more-btn" @click="$emit('load-more')" :disabled="loadingHistory">
          <span v-if="loadingHistory" class="loading-spinner"></span>
          <span>{{ loadingHistory ? '加载中...' : '加载更多历史消息' }}</span>
        </button>
      </div>

      <!-- 消息列表 -->
      <div class="messages-list">
        <div v-for="(message, index) in messages" :key="index" class="message-wrapper">
          <!-- 用户消息 -->
          <div v-if="message.type === 'user'" class="message message-user">
            <div class="message-content-wrapper">
              <div class="message-bubble user-bubble">
                <p class="message-text">{{ message.content }}</p>
              </div>
            </div>
          </div>

          <!-- AI 消息 -->
          <div v-else class="message message-ai">
            <div class="message-avatar">
              <img :src="aiAvatar" alt="AI" class="avatar-img ai-avatar" />
            </div>
            <div class="message-content-wrapper">
              <div class="message-bubble ai-bubble">
                <!-- 加载状态 -->
                <div v-if="message.loading && !message.content" class="message-loading">
                  <div class="loading-dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                  </div>
                  <span class="loading-text">Thinking</span>
                </div>
                <!-- 思考完成标签（显示在消息内容上方） -->
                <div v-else-if="message.thinkingDuration && message.content" class="thinking-complete">
                  <img src="@/assets/thinking.png" alt="Thinking" class="thinking-icon" />
                  <span class="thinking-duration">Thought for {{ message.thinkingDuration }} seconds</span>
                </div>
                <!-- Markdown 内容 -->
                <div v-if="message.content" class="message-markdown">
                  <MarkdownRenderer :content="message.content" />
                </div>
                <!-- 空消息 -->
                <div v-else-if="!message.loading" class="message-empty">等待 AI 响应...</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中元素信息 (编辑模式) -->
    <transition name="slide-up">
      <div v-if="isEditMode && selectedElementInfo" class="selected-element-panel">
        <div class="element-info-header">
          <span class="element-badge">
            {{ selectedElementInfo.tagName?.toLowerCase() || 'element' }}
          </span>
          <span v-if="selectedElementInfo.id" class="element-id">#{{ selectedElementInfo.id }}</span>
          <span v-if="selectedElementInfo.className" class="element-class">
            .{{ selectedElementInfo.className.split(' ')[0] }}
          </span>
          <button class="close-btn" @click="$emit('clear-selected-element')" title="关闭">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M1 1L13 13M1 13L13 1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div v-if="selectedElementInfo.textContent" class="element-text">
          {{ selectedElementInfo.textContent.substring(0, 100) }}
          {{ selectedElementInfo.textContent.length > 100 ? '...' : '' }}
        </div>
        <div class="element-selector">
          <code>{{ selectedElementInfo.selector }}</code>
        </div>
      </div>
    </transition>

    <!-- 输入区域 - 简洁版 -->
    <div class="input-section">
      <!-- AI模型选择器弹出层 -->
      <div
        class="model-selector-popup"
        v-if="showModelSelector"
        @mouseenter="handlePlusButtonHover"
        @mouseleave="handleSelectorAreaLeave"
      >
        <AiModelSelector
          ref="modelSelectorRef"
          :defaultModelKey="selectedModelKey"
          :disabled="isGenerating"
          @change="handleModelChange"
        />
      </div>

      <div class="input-wrapper">
        <!-- 左侧+号按钮 -->
        <button
          class="action-btn plus-btn"
          @click="toggleModelSelector"
          @mouseenter="handlePlusButtonHover"
          @mouseleave="handlePlusButtonLeave"
          :title="showModelSelector ? '收起模型选择' : '展开模型选择'"
          :disabled="!isOwner || isGenerating"
        >
          <!-- 如果有选中的模型图标，显示图标；否则显示+号 -->
          <img
            v-if="getCurrentModelIcon()"
            :src="getCurrentModelIcon()!"
            alt="模型图标"
            class="model-icon-img"
          />
          <span v-else class="plus-icon" :class="{ rotated: showModelSelector }">+</span>
        </button>

        <textarea
          v-model="inputValue"
          :placeholder="inputPlaceholder"
          :disabled="!isOwner || isGenerating"
          class="message-input"
          rows="1"
          @keydown="handleKeydown"
          @input="autoResize"
          ref="textareaRef"
        ></textarea>

        <button
          class="send-btn"
          :class="{ 'send-btn-active': canSend, 'send-btn-generating': isGenerating }"
          :disabled="primaryActionDisabled"
          @click="$emit('primary-action')"
          :title="primaryActionTitle"
        >
          <!-- 停止按钮 -->
          <svg v-if="isGenerating" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <rect x="5" y="5" width="10" height="10" rx="1"/>
          </svg>
          <!-- 发送按钮 -->
          <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path d="M2 10L18 2L10 18L8 12L2 10Z"/>
          </svg>
        </button>
      </div>

      <!-- 提示文本 -->
      <div v-if="!isOwner" class="input-hint">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor">
          <path d="M7 0C3.13 0 0 3.13 0 7s3.13 7 7 7 7-3.13 7-7-3.13-7-7-7zm0 10c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm1-3H6V3h2v4z"/>
        </svg>
        <span>无法在别人的作品下对话</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AiModelSelector from '@/components/AiModelSelector.vue'
import { initMessageCollapse } from '@/utils/messageCollapse'

// 导入模型SVG图标
import deepseekIcon from '@/assets/deepseek-color.svg'
import qwenIcon from '@/assets/qwen-color.svg'
import openaiIcon from '@/assets/openai.svg'
import kimiIcon from '@/assets/kimi-color.svg'

interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  thinkingStartTime?: number
  thinkingDuration?: number
}

interface Props {
  messages: Message[]
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
  defaultModelKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  isEditMode: false,
  isGenerating: false,
  primaryActionDisabled: false,
  primaryActionTitle: '发送',
  defaultModelKey: 'deepseek-chat',
})

const emit = defineEmits<{
  'load-more': []
  'clear-selected-element': []
  'primary-action': []
  'update:userInput': [value: string]
  'keydown': [e: KeyboardEvent]
  'model-change': [modelKey: string, model: API.AiModelConfig]
}>()

const messagesWrapperRef = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const inputValue = ref(props.userInput)

// AI模型选择相关
const modelSelectorRef = ref()
const selectedModelKey = ref(props.defaultModelKey)
const showModelSelector = ref(false)
let hoverTimeout: number | null = null

// 鼠标悬停在+号上，延迟显示模型列表
const handlePlusButtonHover = () => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
  }
  hoverTimeout = window.setTimeout(() => {
    showModelSelector.value = true
  }, 200) // 200ms延迟，避免误触
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

// 点击外部区域关闭模型选择器
const handleClickOutside = (event: MouseEvent) => {
  if (!showModelSelector.value) return

  const target = event.target as HTMLElement
  const popup = document.querySelector('.model-selector-popup')
  const plusBtn = document.querySelector('.plus-btn')

  // 如果点击的不是弹出层内部或+号按钮，则关闭
  if (popup && !popup.contains(target) && plusBtn && !plusBtn.contains(target)) {
    showModelSelector.value = false
  }
}

// 处理模型选择变化
const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  // 选择模型后自动关闭列表
  showModelSelector.value = false
  // 向父组件传递模型变化事件
  emit('model-change', modelKey, model)
  // console.log('切换模型:', modelKey)  // 生产环境可注释
}

// 获取当前选中模型的图标
const getCurrentModelIcon = () => {
  const modelKey = selectedModelKey.value?.toLowerCase() || ''

  // 根据modelKey匹配对应的图标
  if (modelKey.includes('deepseek')) {
    return deepseekIcon
  } else if (modelKey.includes('qwen') || modelKey.includes('coder')) {
    return qwenIcon
  } else if (modelKey.includes('gpt') || modelKey.includes('codex') || modelKey.includes('o3') || modelKey.includes('o4')) {
    return openaiIcon
  } else if (modelKey.includes('kimi')) {
    return kimiIcon
  }

  // 默认返回null，显示+号
  return null
}

// 同步输入值
watch(() => props.userInput, (val) => {
  inputValue.value = val
})

watch(inputValue, (val) => {
  emit('update:userInput', val)
})

// 同步 defaultModelKey 的变化
watch(() => props.defaultModelKey, (newModelKey) => {
  if (newModelKey) {
    selectedModelKey.value = newModelKey
  }
})

// 计算是否可以发送
const canSend = computed(() => {
  return props.isOwner && !props.isGenerating && inputValue.value.trim().length > 0
})

// 输入框占位符
const inputPlaceholder = computed(() => {
  if (!props.isOwner) return '无法在别人的作品下对话'
  if (props.isEditMode) return '描述你想要修改的内容...'
  return '输入消息，Shift+Enter 换行，Enter 发送'
})

// 键盘事件处理
const handleKeydown = (e: KeyboardEvent) => {
  emit('keydown', e)
}

// 自动调整输入框高度
const autoResize = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  textarea.style.height = 'auto'
  const scrollHeight = textarea.scrollHeight
  const maxHeight = 160 // 最大高度约 5 行
  textarea.style.height = Math.min(scrollHeight, maxHeight) + 'px'
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const wrapper = messagesWrapperRef.value
    if (wrapper) {
      wrapper.scrollTop = wrapper.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(() => props.messages.length, () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
  document.addEventListener('click', handleClickOutside)

  // ✅ 初始化消息折叠功能
  // 参数：容器选择器, 自动收缩=true, 只处理已完成的消息=true
  initMessageCollapse('.messages-wrapper', true, true)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

defineExpose({
  scrollToBottom,
  messagesWrapperRef,
})
</script>

<style scoped>
.chat-panel-lovable {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

/* ========== 消息容器 ========== */
.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--spacing-lg) var(--spacing-lg) var(--spacing-md);
  scroll-behavior: smooth;
}

/* 加载更多 - Lovable 风格 */
.load-more-section {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
}

.load-more-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: white;
  color: #ff6b35;
  border: 2px solid rgba(255, 107, 53, 0.2);
  border-radius: 30px;
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.1);
}

.load-more-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  color: #ff6b35;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.2);
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--border-primary);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 消息列表 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.message-wrapper {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 消息布局 */
.message {
  display: flex;
  gap: var(--spacing-md);
  max-width: 85%;
}

.message-user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-ai {
  flex-direction: row;
  margin-right: auto;
}

/* 头像 */
.message-avatar {
  flex-shrink: 0;
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.ai-avatar {
  background: var(--bg-elevated);
  padding: 2px;
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--text-sm);
}

/* 消息气泡 - Lovable 风格 */
.message-content-wrapper {
  flex: 1;
  min-width: 0;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 24px;
  word-wrap: break-word;
  overflow-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.message-bubble:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.user-bubble {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
}

.ai-bubble {
  background: white;
  color: var(--text-primary);
  border-bottom-left-radius: 6px;
  border: 2px solid rgba(255, 107, 53, 0.1);
  box-shadow: 0 2px 12px rgba(255, 107, 53, 0.08);
}

.message-text {
  margin: 0;
  line-height: var(--leading-relaxed);
  white-space: pre-wrap;
}

/* AI 加载状态 */
.message-loading {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-tertiary);
  animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.loading-text {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  font-weight: 600;
}

/* 思考完成标签 */
.thinking-complete {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid rgba(59, 130, 246, 0.2);
  border-radius: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
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
  filter: drop-shadow(0 1px 2px rgba(59, 130, 246, 0.3));
}

.thinking-duration {
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.message-markdown {
  color: var(--text-primary);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
}

.message-empty {
  color: var(--text-tertiary);
  font-style: italic;
}

/* ========== 选中元素面板 - Lovable 风格 ========== */
.selected-element-panel {
  margin: 0 var(--spacing-lg) var(--spacing-md);
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  border: 2px solid rgba(255, 107, 53, 0.15);
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.12);
}

.element-info-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.element-badge {
  padding: 6px 14px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  border-radius: 20px;
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  box-shadow: 0 3px 10px rgba(255, 107, 53, 0.4);
  letter-spacing: 0.5px;
}

.element-id {
  color: #ff6b35;
  font-size: var(--text-sm);
  font-family: var(--font-mono);
  font-weight: 600;
}

.element-class {
  color: #ff8c42;
  font-size: var(--text-sm);
  font-family: var(--font-mono);
  font-weight: 600;
}

.close-btn {
  margin-left: auto;
  padding: 8px;
  background: white;
  color: #ff6b35;
  border: 2px solid rgba(255, 107, 53, 0.2);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #ff6b35;
  color: white;
  transform: rotate(90deg) scale(1.1);
}

.element-text {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  margin-bottom: var(--spacing-sm);
  line-height: var(--leading-normal);
}

.element-selector {
  padding: var(--spacing-md);
  background: white;
  border-radius: 16px;
  overflow-x: auto;
  border: 2px solid rgba(255, 107, 53, 0.1);
}

.element-selector code {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--text-primary);
}

/* 过渡动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ========== 输入区域 - Lovable 风格 ========== */
.input-section {
  position: relative;
  padding: var(--spacing-lg) var(--spacing-xl) var(--spacing-xl);
  background: var(--bg-primary);
}

/* 模型选择器弹出层 - 固定在输入框上方 */
.model-selector-popup {
  position: absolute;
  bottom: 100%;
  left: var(--spacing-xl);
  margin-bottom: 8px;
  z-index: 1000;
  background: white;
  border-radius: 24px;
  border: 3px solid rgba(255, 107, 53, 0.15);
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

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 32px;
  padding: 14px 20px;
  max-height: 220px;
  overflow-y: auto;
  border: 3px solid rgba(255, 107, 53, 0.15);
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.08),
    0 4px 16px rgba(255, 107, 53, 0.12),
    0 12px 32px rgba(255, 107, 53, 0.08);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.input-wrapper:hover {
  border-color: rgba(255, 107, 53, 0.25);
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.12),
    0 6px 20px rgba(255, 107, 53, 0.15),
    0 16px 40px rgba(255, 107, 53, 0.1);
  transform: translateY(-2px);
}

.input-wrapper:focus-within {
  border-color: #ff6b35;
  box-shadow:
    0 0 0 3px rgba(255, 107, 53, 0.2),
    0 8px 24px rgba(255, 107, 53, 0.2),
    0 20px 48px rgba(255, 107, 53, 0.15);
  transform: translateY(-2px);
}

.message-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  font-family: inherit;
  color: var(--text-primary);
  resize: none;
  line-height: 26px;
  min-height: 26px;
  height: auto;
  max-height: none;
  overflow: hidden;
  padding: 6px 0;
  display: block;
}

.message-input::placeholder {
  color: rgba(255, 107, 53, 0.4);
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message-input::-webkit-scrollbar {
  width: 6px;
}

.message-input::-webkit-scrollbar-thumb {
  background: rgba(255, 107, 53, 0.3);
  border-radius: 10px;
}

.send-btn {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.4);
  align-self: flex-end;
}

.send-btn:not(:disabled):hover {
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b35 100%);
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.5);
}

.send-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.5;
}

.send-btn-generating {
  background: #ef4444 !important;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(0.95);
  }
}

/* 操作按钮通用样式 */
.action-btn {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  background: transparent;
  align-self: flex-end;
}

/* +号按钮 - Lovable 风格 */
.plus-btn {
  color: #ff6b35;
  background: rgba(255, 107, 53, 0.1);
  border: 2px solid rgba(255, 107, 53, 0.2);
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.15);
}

.plus-btn:hover:not(:disabled) {
  background: rgba(255, 107, 53, 0.15);
  border-color: rgba(255, 107, 53, 0.3);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.25);
}

.plus-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.plus-icon {
  font-size: 24px;
  font-weight: 400;
  line-height: 1;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
}

.plus-icon.rotated {
  transform: rotate(45deg);
}

/* 模型图标 */
.model-icon-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  filter: drop-shadow(0 2px 4px rgba(255, 107, 53, 0.2));
}

.plus-btn:hover .model-icon-img {
  transform: scale(1.1);
}

.input-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: var(--spacing-sm);
  padding: 10px 16px;
  background: rgba(255, 107, 53, 0.08);
  border-radius: 16px;
  color: #ff6b35;
  font-size: var(--text-xs);
  font-weight: 600;
}
</style>
