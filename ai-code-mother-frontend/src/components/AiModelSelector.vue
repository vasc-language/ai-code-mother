<template>
  <div class="ai-model-selector">
    <!-- 标题栏 -->
    <div class="selector-header">
      <span class="header-label">AICodeHub</span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <a-spin size="small" />
      <span>加载中...</span>
    </div>

    <!-- 模型列表 - 直接在组件内滚动，无额外嵌套 -->
    <div
      v-for="model in models"
      :key="model.modelKey"
      class="model-item"
      :class="{ selected: selectedModelKey === model.modelKey }"
      @click="handleModelSelect(model)"
      v-else
    >
      <!-- 左侧：SVG图标 -->
      <div class="model-icon">
        <img
          :src="getModelIcon(model)"
          :alt="model.provider"
          class="icon-img"
        />
      </div>

      <!-- 中间：模型名称 -->
      <div class="model-info">
        <span class="model-name">{{ model.modelKey }}</span>
      </div>

      <!-- 右侧：能力图标组 + Token -->
      <div class="model-meta">
        <!-- 能力图标组 -->
        <div class="capability-icons">
          <img
            v-for="(iconData, index) in getCapabilityIcons(model)"
            :key="index"
            :src="iconData.icon"
            :alt="iconData.title"
            :title="iconData.title"
            class="capability-icon"
            :class="iconData.type"
          />
        </div>

        <!-- 质量系数 -->
        <div class="token-count">
          {{ formatTokenCount(model) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { listEnabledModels } from '@/api/aImoxingpeizhi'

// 导入SVG图标
import deepseekIcon from '@/assets/deepseek-color.svg'
import qwenIcon from '@/assets/qwen-color.svg'
import openaiIcon from '@/assets/openai.svg'
import kimiIcon from '@/assets/kimi-color.svg'

// 导入功能SVG图标
import viewIcon from '@/assets/view.svg'
import onlineSearchIcon from '@/assets/Online search.svg'
import thinkingIcon from '@/assets/thinking.svg'
import toolsCallIcon from '@/assets/ToolsCall.svg'

// Props
interface Props {
  defaultModelKey?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  defaultModelKey: 'codex-mini-latest',
  disabled: false
})

// Emits
const emit = defineEmits<{
  (e: 'change', modelKey: string, model: API.AiModelConfig): void
}>()

// State
const loading = ref(false)
const models = ref<API.AiModelConfig[]>([])
const selectedModelKey = ref<string>(props.defaultModelKey)

// Computed
const currentModel = computed(() => {
  return models.value.find(m => m.modelKey === selectedModelKey.value)
})

// Methods
const getModelIcon = (model: API.AiModelConfig) => {
  const provider = model.provider?.toLowerCase() || ''
  const modelKey = model.modelKey?.toLowerCase() || ''

  // 根据modelKey匹配图标（优先级更高）
  if (modelKey.includes('deepseek')) {
    return deepseekIcon
  } else if (modelKey.includes('qwen') || modelKey.includes('coder')) {
    return qwenIcon
  } else if (modelKey.includes('gpt') || modelKey.includes('codex') || modelKey.includes('o3') || modelKey.includes('o4')) {
    return openaiIcon
  } else if (modelKey.includes('kimi')) {
    return kimiIcon
  }

  // 根据provider匹配图标
  if (provider === 'openai' || provider === 'openrouter') {
    return openaiIcon
  } else if (provider === 'iflow') {
    // iflow提供多种模型，需要根据modelKey判断
    if (modelKey.includes('qwen')) return qwenIcon
    if (modelKey.includes('kimi')) return kimiIcon
    if (modelKey.includes('deepseek')) return deepseekIcon
  }

  // 默认返回OpenAI图标
  return openaiIcon
}

const loadModels = async () => {
  loading.value = true
  try {
    const response = await listEnabledModels()
    const res = response.data // axios响应需要访问.data
    if (res.code === 0 && res.data) {
      models.value = res.data
      // 如果默认模型不在列表中,选择第一个
      const hasDefault = models.value.some(m => m.modelKey === selectedModelKey.value)
      if (!hasDefault && models.value.length > 0) {
        selectedModelKey.value = models.value[0].modelKey || ''
      }
    } else {
      message.error('加载模型列表失败: ' + (res.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('加载模型列表异常:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '网络请求失败'
    message.error('加载模型列表失败: ' + errorMsg)
  } finally {
    loading.value = false
  }
}

const handleModelSelect = (model: API.AiModelConfig) => {
  if (props.disabled) return
  selectedModelKey.value = model.modelKey || ''
  emit('change', model.modelKey || '', model)
  console.log('选择模型:', model.modelName, model.modelKey)
}

const getTierColor = (tier?: string) => {
  switch (tier) {
    case 'SIMPLE':
      return 'green'
    case 'MEDIUM':
      return 'blue'
    case 'HARD':
      return 'orange'
    case 'EXPERT':
      return 'red'
    default:
      return 'default'
  }
}

const getTierLabel = (tier?: string) => {
  switch (tier) {
    case 'SIMPLE':
      return '简单'
    case 'MEDIUM':
      return '中等'
    case 'HARD':
      return '困难'
    case 'EXPERT':
      return '专家'
    default:
      return tier
  }
}

// 获取品牌类名（用于品牌配色）
const getBrandClass = (model: API.AiModelConfig) => {
  const provider = model.provider?.toLowerCase() || ''
  const modelKey = model.modelKey?.toLowerCase() || ''

  // 优先根据modelKey判断
  if (modelKey.includes('deepseek')) {
    return 'deepseek'
  } else if (modelKey.includes('qwen') || modelKey.includes('coder')) {
    return 'qwen'
  } else if (modelKey.includes('gpt') || modelKey.includes('codex') || modelKey.includes('o3') || modelKey.includes('o4')) {
    return 'openai'
  } else if (modelKey.includes('kimi')) {
    return 'kimi'
  } else if (modelKey.includes('gemini')) {
    return 'gemini'
  }

  // 根据provider判断
  if (provider === 'openai' || provider === 'openrouter') {
    return 'openai'
  } else if (provider === 'google') {
    return 'gemini'
  } else if (provider === 'iflow') {
    return 'default'
  }

  return 'default'
}

// 获取模型显示名称
const getModelDisplayName = (model: API.AiModelConfig) => {
  const modelKey = model.modelKey || ''

  // 提取品牌名称
  if (modelKey.toLowerCase().includes('deepseek')) {
    return 'DeepSeek'
  } else if (modelKey.toLowerCase().includes('qwen')) {
    return 'Qwen'
  } else if (modelKey.toLowerCase().includes('gpt')) {
    return 'GPT'
  } else if (modelKey.toLowerCase().includes('o3') || modelKey.toLowerCase().includes('o4')) {
    return 'OpenAI'
  } else if (modelKey.toLowerCase().includes('kimi')) {
    return 'Kimi'
  }

  return model.modelName || modelKey
}

// 获取能力图标组（返回图标数组）
const getCapabilityIcons = (model: API.AiModelConfig) => {
  const icons: Array<{ icon: string; type: string; title: string }> = []
  const modelKey = model.modelKey?.toLowerCase() || ''

  // GPT-5系列和Codex系列 - 全能型(view 🌐 ❄️ 🔧)
  const isGpt5 = modelKey.includes('gpt-5') || modelKey.includes('codex')

  // DeepSeek系列 - 推理+工具型(❄️ 🔧)
  const isDeepSeek = modelKey.includes('deepseek')

  // Qwen3 Coder、Kimi系列 - 工具专精型(🔧)
  const isToolOnly = modelKey.includes('qwen3-coder') || modelKey.includes('qwen') ||
                     modelKey.includes('kimi')

  if (isGpt5) {
    // 全能型模型 - GPT-5和Codex系列
    icons.push({ icon: viewIcon, type: 'vision', title: '视觉 - 支持图像识别和处理' })
    icons.push({ icon: onlineSearchIcon, type: 'web', title: '联网 - 支持实时网络搜索' })
    icons.push({ icon: thinkingIcon, type: 'reasoning', title: '推理 - 支持复杂推理能力' })
    icons.push({ icon: toolsCallIcon, type: 'tool', title: '工具 - 支持函数调用和工具使用' })
  } else if (isDeepSeek) {
    // 推理+工具型 - DeepSeek V3.1/V3.2
    icons.push({ icon: thinkingIcon, type: 'reasoning', title: '推理 - 支持复杂推理能力' })
    icons.push({ icon: toolsCallIcon, type: 'tool', title: '工具 - 支持函数调用和工具使用' })
  } else if (isToolOnly) {
    // 工具专精型 - Qwen3 Coder和Kimi K2系列
    icons.push({ icon: toolsCallIcon, type: 'tool', title: '工具 - 支持函数调用和工具使用' })
  } else {
    // 未分类模型,默认显示工具能力
    icons.push({ icon: toolsCallIcon, type: 'tool', title: '工具 - 支持函数调用和工具使用' })
  }

  return icons
}

// 格式化质量系数显示
const formatTokenCount = (model: API.AiModelConfig) => {
  // 显示质量系数，反映模型的积分消耗倍数
  if (model.qualityScore) {
    return `${model.qualityScore}×`
  }
  // 降级显示：如果没有质量系数，显示基础倍率
  return model.pointsPerKToken ? `×${model.pointsPerKToken}` : '-'
}

// Watch props changes
watch(() => props.defaultModelKey, (newValue) => {
  if (newValue) {
    selectedModelKey.value = newValue
  }
})

// Lifecycle
onMounted(() => {
  loadModels()
})

// Expose
defineExpose({
  selectedModelKey,
  currentModel,
  loadModels
})
</script>

<style scoped>
/* ========== 单层容器结构 ========== */
.ai-model-selector {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  max-height: 500px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.selector-header {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  border-bottom: 2px solid rgba(255, 107, 53, 0.1);
}

.header-label {
  font-weight: 700;
  font-size: 16px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.3px;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: #666;
  font-size: 14px;
}

/* ========== 模型列表项 - 直接在.ai-model-selector下 ========== */
.model-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 0 8px 6px 8px;
  border-radius: 16px;
  background: white;
  border: 2px solid rgba(255, 107, 53, 0.1);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  min-height: 56px;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.06);
}

.model-item:first-of-type {
  margin-top: 8px;
}

.model-item:hover {
  background: rgba(255, 245, 240, 0.6);
  border-color: rgba(255, 107, 53, 0.3);
  transform: translateX(4px) translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
}

.model-item.selected {
  background: linear-gradient(135deg, #fff5f0 0%, #ffe8dd 100%);
  border-color: #ff6b35;
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.2),
    0 4px 16px rgba(255, 107, 53, 0.2);
}

.model-item.selected:hover {
  background: linear-gradient(135deg, #ffe8dd 0%, #ffd9cc 100%);
  transform: translateX(4px) translateY(-2px);
}

/* ========== SVG图标 ========== */
.model-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

/* ========== 模型信息 ========== */
.model-info {
  flex: 1;
  min-width: 0;
}

.model-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  line-height: 1.5;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 右侧元数据 ========== */
.model-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* ========== 能力图标组 ========== */
.capability-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.capability-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  border-radius: 4px;
  padding: 2px;
  background: rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  cursor: help;
}

.capability-icon:hover {
  background: rgba(0, 0, 0, 0.08);
  transform: scale(1.15);
}

/* 视觉能力 - 绿色 */
.capability-icon.vision {
  background: rgba(16, 185, 129, 0.1);
}

/* 联网 - 青色 */
.capability-icon.web {
  background: rgba(6, 182, 212, 0.1);
}

/* 推理能力 - 蓝色 */
.capability-icon.reasoning {
  background: rgba(59, 130, 246, 0.1);
}

/* 工具能力 - 紫色 */
.capability-icon.tool {
  background: rgba(168, 85, 247, 0.1);
}

/* ========== 质量系数显示 ========== */
.token-count {
  min-width: 45px;
  text-align: right;
  font-size: 14px;
  font-weight: 700;
  color: #ff8c42;
  font-family: 'SF Mono', 'Consolas', 'Monaco', monospace;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.model-item.selected .token-count {
  color: #ff6b35;
}

/* ========== 滚动条样式 ========== */
.ai-model-selector::-webkit-scrollbar {
  width: 8px;
}

.ai-model-selector::-webkit-scrollbar-track {
  background: rgba(255, 107, 53, 0.05);
  border-radius: 4px;
}

.ai-model-selector::-webkit-scrollbar-thumb {
  background: rgba(255, 107, 53, 0.3);
  border-radius: 4px;
  transition: background 0.2s ease;
}

.ai-model-selector::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 107, 53, 0.5);
}

/* ========== 响应式 ========== */
@media (max-width: 480px) {
  .model-item {
    padding: 8px 10px;
    gap: 10px;
  }

  .model-icon {
    width: 28px;
    height: 28px;
  }

  .icon-img {
    width: 28px;
    height: 28px;
  }

  .model-name {
    font-size: 13px;
  }

  .capability-icon {
    width: 18px;
    height: 18px;
  }

  .token-count {
    font-size: 12px;
    min-width: 50px;
  }
}
</style>
