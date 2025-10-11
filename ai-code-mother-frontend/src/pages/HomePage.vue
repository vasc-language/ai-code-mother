<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, getAppVoById, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'
import AiModelSelector from '@/components/AiModelSelector.vue'

// 导入模型SVG图标
import deepseekIcon from '@/assets/deepseek-color.svg'
import qwenIcon from '@/assets/qwen-color.svg'
import openaiIcon from '@/assets/openai.svg'
import kimiIcon from '@/assets/kimi-color.svg'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// AI模型选择相关
const modelSelectorRef = ref()
const selectedModelKey = ref('codex-mini-latest')
const showModelSelector = ref(false) // 控制模型选择器显示
const popupStyle = ref({}) // 弹出框动态样式
let hoverTimeout: number | null = null

// 计算弹出框位置（fixed定位，确保始终在视口内可见）
const calculatePopupPosition = () => {
  const inputWrapper = document.querySelector('.input-wrapper') as HTMLElement
  if (!inputWrapper) return {}

  const rect = inputWrapper.getBoundingClientRect()
  const viewportHeight = window.innerHeight
  const viewportWidth = window.innerWidth

  // 弹出框的尺寸
  const popupHeight = 480
  const popupWidth = 360
  const gap = 8 // 与输入框的间距

  // 计算上方和下方的可用空间
  const spaceAbove = rect.top
  const spaceBelow = viewportHeight - rect.bottom

  let style: any = {
    position: 'fixed',
    left: `${rect.left}px`,
    minWidth: `${popupWidth}px`,
    maxHeight: `${popupHeight}px`,
  }

  // 决定显示在上方还是下方
  if (spaceAbove >= popupHeight || spaceAbove > spaceBelow) {
    // 显示在上方
    style.bottom = `${viewportHeight - rect.top + gap}px`
    style.top = 'auto'
  } else {
    // 显示在下方
    style.top = `${rect.bottom + gap}px`
    style.bottom = 'auto'
    // 如果下方空间也不够，限制最大高度
    if (spaceBelow < popupHeight) {
      style.maxHeight = `${spaceBelow - gap - 20}px`
    }
  }

  // 确保不会超出视口左右边界
  if (rect.left + popupWidth > viewportWidth) {
    style.left = `${viewportWidth - popupWidth - 20}px`
  }
  if (rect.left < 0) {
    style.left = '20px'
  }

  return style
}

// 鼠标悬停在+号上，延迟显示模型列表
const handlePlusButtonHover = () => {
  if (hoverTimeout) {
    clearTimeout(hoverTimeout)
  }
  hoverTimeout = window.setTimeout(() => {
    popupStyle.value = calculatePopupPosition() // 计算位置
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
  if (!showModelSelector.value) {
    // 打开时计算位置
    popupStyle.value = calculatePopupPosition()
  }
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

// 自动调整textarea高度
const autoResizeTextarea = (e: Event) => {
  const textarea = e.target as HTMLTextAreaElement
  textarea.style.height = 'auto'
  textarea.style.height = textarea.scrollHeight + 'px'
}

// 在输入框中：Enter 发送，Shift+Enter 换行
const onPromptKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (!creating.value) {
      createApp()
    }
  }
  // 自动调整高度
  setTimeout(() => autoResizeTextarea(e), 0)
}

// 处理模型选择变化
const handleModelChange = (modelKey: string, model: API.AiModelConfig) => {
  selectedModelKey.value = modelKey
  // 选择模型后自动关闭列表
  showModelSelector.value = false
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

  // 默认返回null，显示+号
  return null
}

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  let loadingMessage = null

  try {
    console.log('开始创建应用，用户输入:', userPrompt.value.trim())
    console.log('选择的模型:', selectedModelKey.value)

    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
      modelKey: selectedModelKey.value,  // 传递用户选择的模型
    })

    console.log('应用创建API响应:', res.data)

    if (res.data.code === 0 && res.data.data) {
      const appId = String(res.data.data)
      console.log('应用创建成功，ID:', appId)

      message.success('应用创建成功，正在准备环境...')

      // 显示加载提示
      loadingMessage = message.loading('正在验证应用状态，请稍候...', 0)

      // 改进的重试机制
      const maxRetries = 5  // 增加重试次数
      let retryCount = 0
      let navigateSuccess = false

      while (retryCount < maxRetries && !navigateSuccess) {
        try {
          // 动态调整等待时间：第一次立即检查，后续逐渐增加等待时间
          const waitTime = retryCount === 0 ? 100 : Math.min(1000 + retryCount * 800, 3000)
          console.log(`第 ${retryCount + 1} 次验证，等待时间: ${waitTime}ms`)

          await new Promise(resolve => setTimeout(resolve, waitTime))

          // 验证应用是否存在 - 修复：保持字符串格式传递ID
          const checkRes = await getAppVoById({ id: appId })
          console.log(`第 ${retryCount + 1} 次验证结果:`, checkRes.data)

          if (checkRes.data.code === 0 && checkRes.data.data) {
            console.log('应用验证成功，准备跳转')
            // 关闭加载提示
            if (loadingMessage) {
              loadingMessage()
              loadingMessage = null
            }

            message.success('应用准备完成，正在跳转...')
            // 如果应用存在，跳转到聊天页面
            await router.push(`/app/chat/${appId}`)
            navigateSuccess = true
            // 清空输入框
            userPrompt.value = ''
            break
          } else {
            throw new Error(`应用验证失败: ${checkRes.data.message || '应用尚未就绪'}`)
          }
        } catch (error) {
          retryCount++
          const errorMsg = error.response?.data?.message || error.message || '未知错误'
          console.warn(`第 ${retryCount} 次验证应用失败:`, errorMsg)

          if (retryCount >= maxRetries) {
            console.error('重试次数已达上限，验证失败')

            // 关闭加载提示
            if (loadingMessage) {
              loadingMessage()
              loadingMessage = null
            }

            // 根据错误类型给出不同的提示
            if (errorMsg.includes('不存在') || errorMsg.includes('NOT_FOUND')) {
              message.warning('应用创建成功，但需要更多时间准备。请稍后从"我的应用"中访问。', 5)
            } else {
              message.error(`应用访问失败: ${errorMsg}。请从"我的应用"中重新尝试。`, 4)
            }

            // 刷新我的应用列表
            await loadMyApps()
            // 清空输入框
            userPrompt.value = ''
            break
          }

          // 更新加载提示
          if (loadingMessage && retryCount < maxRetries) {
            loadingMessage()
            loadingMessage = message.loading(`正在准备应用环境 (${retryCount}/${maxRetries})...`, 0)
          }
        }
      }
    } else {
      const errorMsg = res.data.message || '未知错误'
      console.error('应用创建API返回错误:', errorMsg)
      message.error('创建失败：' + errorMsg)
    }
  } catch (error) {
    console.error('创建应用异常：', error)
    const errorMsg = error.response?.data?.message || error.message || '网络错误'
    message.error('创建失败：' + errorMsg)
  } finally {
    // 确保关闭所有加载提示
    if (loadingMessage) {
      loadingMessage()
    }
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  // 窗口滚动或resize时更新弹出框位置
  const updatePopupPosition = () => {
    if (showModelSelector.value) {
      popupStyle.value = calculatePopupPosition()
    }
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', updatePopupPosition, true) // 捕获阶段监听所有滚动
  window.addEventListener('resize', updatePopupPosition)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('click', handleClickOutside)
    window.removeEventListener('scroll', updatePopupPosition, true)
    window.removeEventListener('resize', updatePopupPosition)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <h1 class="hero-title">Your Majestic AI Creation Concierge</h1>
        <p class="hero-description">一句话轻松创建网站应用</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <!-- AI模型选择器 - fixed定位，确保始终在视口内可见 -->
        <div
          class="model-selector-popup"
          :style="popupStyle"
          v-if="showModelSelector"
          @mouseenter="handlePlusButtonHover"
          @mouseleave="handleSelectorAreaLeave"
        >
          <AiModelSelector
            ref="modelSelectorRef"
            :disabled="creating"
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

          <!-- 主输入框 -->
          <textarea
            v-model="userPrompt"
            placeholder="描述你想创建的应用..."
            :maxlength="1000"
            class="main-input"
            @keydown="onPromptKeydown"
            @input="autoResizeTextarea"
            rows="1"
          ></textarea>

          <!-- 右侧发送按钮 -->
          <button
            class="action-btn send-btn"
            @click="createApp"
            :disabled="!userPrompt.trim() || creating"
            :class="{ loading: creating }"
          >
            <span v-if="!creating" class="send-icon">↑</span>
            <span v-else class="loading-spinner">⟳</span>
          </button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个简洁优雅的个人介绍博客，使用原生多文件模式，包含响应式设计和基本交互功能。使用语义化HTML结构，和现代CSS Grid和Flexbox布局，添加平滑滚动和主题切换功能。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个功能完整的待办事项列表单文件应用，包含添加任务、标记完成、删除任务和本地存储功能。',
            )
          "
        >待办事项列表</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* 科技感网格背景 - 蓝色主题 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(0, 56, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 56, 255, 0.03) 1px, transparent 1px),
    linear-gradient(rgba(0, 209, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 209, 255, 0.02) 1px, transparent 1px);
  background-size:
    100px 100px,
    100px 100px,
    20px 20px,
    20px 20px;
  pointer-events: none;
  animation: gridFloat 20s ease-in-out infinite;
}

/* 动态光效 - 蓝色渐变 */
#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(
      600px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(0, 56, 255, 0.08) 0%,
      rgba(0, 209, 255, 0.06) 40%,
      transparent 80%
    ),
    linear-gradient(45deg, transparent 30%, rgba(0, 56, 255, 0.04) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(0, 209, 255, 0.04) 50%, transparent 70%);
  pointer-events: none;
  animation: lightPulse 8s ease-in-out infinite alternate;
}

@keyframes gridFloat {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(5px, 5px);
  }
}

@keyframes lightPulse {
  0% {
    opacity: 0.3;
  }
  100% {
    opacity: 0.7;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 移除居中光束效果 */

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: var(--spacing-4xl) 0 var(--spacing-3xl);
  margin-bottom: var(--spacing-xl);
  color: var(--gray-800);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse 800px 400px at center, rgba(0, 56, 255, 0.12) 0%, transparent 70%),
    linear-gradient(45deg, transparent 30%, rgba(0, 209, 255, 0.05) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(204, 48, 134, 0.04) 50%, transparent 70%);
  animation: heroGlow 10s ease-in-out infinite alternate;
}

@keyframes heroGlow {
  0% {
    opacity: 0.6;
    transform: scale(1);
  }
  100% {
    opacity: 1;
    transform: scale(1.02);
  }
}

@keyframes rotate {
  0% {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  100% {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

.hero-title {
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--spacing-lg);
  line-height: var(--line-height-tight);
  background: var(--button-gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.025em;
  position: relative;
  z-index: 2;
  animation: titleShimmer 3s ease-in-out infinite;
  background-size: 200% 200%;
}

@keyframes titleShimmer {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.hero-description {
  font-size: var(--font-size-xl);
  margin: 0;
  opacity: 0.8;
  color: var(--gray-600);
  font-weight: var(--font-weight-medium);
  position: relative;
  z-index: 2;
}

/* ChatGPT风格输入框 */
.input-section {
  position: relative;
  margin: 0 auto var(--spacing-2xl);
  max-width: 900px;
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

/* 主输入框 */
.main-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  font-family: var(--font-family-primary);
  color: var(--gray-800);
  resize: none;
  line-height: 26px;
  min-height: 26px;
  height: auto;
  max-height: none;
  overflow: hidden;
  padding: 6px 0;
  display: block;
}

.main-input::placeholder {
  color: rgba(255, 107, 53, 0.4);
}

.main-input::-webkit-scrollbar {
  width: 6px;
}

.main-input::-webkit-scrollbar-thumb {
  background: rgba(255, 107, 53, 0.3);
  border-radius: 10px;
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

.plus-btn:hover {
  background: rgba(255, 107, 53, 0.15);
  border-color: rgba(255, 107, 53, 0.3);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.25);
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

/* 模型选择器弹出层 - Lovable 风格 */
.model-selector-popup {
  /* position, top, bottom, left 由JavaScript动态设置 */
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
}

@keyframes popupFadeIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(-8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 发送按钮 */
.send-btn {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.4);
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

.send-icon {
  font-size: 20px;
  font-weight: bold;
  line-height: 1;
}

.loading-spinner {
  font-size: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  margin-bottom: var(--spacing-3xl);
  margin-top: var(--spacing-sm);
  flex-wrap: wrap;
  padding: var(--spacing-lg) 0;
}

.quick-actions .ant-btn {
  border-radius: var(--radius-full);
  padding: var(--spacing-sm) var(--spacing-lg);
  height: auto;
  background: var(--white);
  border: 1px solid var(--gray-300);
  color: var(--gray-700);
  font-weight: var(--font-weight-medium);
  font-family: var(--font-family-primary);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.quick-actions .ant-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transition: left 0.5s;
}

.quick-actions .ant-btn:hover::before {
  left: 100%;
}

.quick-actions .ant-btn:hover {
  background: var(--gray-50);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* 区域标题 */
.section {
  margin-bottom: var(--spacing-3xl);
  background: var(--card-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-xl);
  transition: var(--transition-normal);
}

.section:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.section-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--spacing-xl);
  color: var(--gray-800);
  font-family: var(--font-family-primary);
  text-align: center;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: var(--spacing-lg);
  transition: var(--transition-normal);
}

/* Ant Design 组件样式覆盖 */
:deep(.ant-textarea) {
  transition: var(--transition-normal);
}

:deep(.ant-btn) {
  transition: var(--transition-fast);
}

:deep(.ant-btn-primary) {
  background: var(--button-gradient-primary);
  border: none;
  border-radius: var(--radius-full);
  font-weight: var(--font-weight-medium);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-fast);
  padding: var(--spacing-sm) var(--spacing-xl);
}

:deep(.ant-btn-primary:hover) {
  background: var(--button-gradient-secondary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

:deep(.ant-pagination) {
  font-family: var(--font-family-primary);
}

:deep(.ant-pagination-item) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

:deep(.ant-pagination-item:hover) {
  background: rgba(99, 102, 241, 0.1);
  border-color: var(--primary-color);
}

:deep(.ant-pagination-item-active) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: var(--spacing-lg);
  }

  .hero-section {
    padding: var(--spacing-3xl) 0 var(--spacing-2xl);
    margin-bottom: var(--spacing-lg);
  }

  .hero-title {
    font-size: var(--font-size-3xl);
  }

  .hero-description {
    font-size: var(--font-size-lg);
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .quick-actions {
    justify-content: center;
    padding: var(--spacing-lg);
  }

  .section {
    padding: var(--spacing-lg);
  }

  .section-title {
    font-size: var(--font-size-2xl);
  }
}
</style>
