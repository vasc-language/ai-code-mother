<template>
  <div class="ai-model-selector">
    <a-select
      v-model:value="selectedModelKey"
      :loading="loading"
      :disabled="disabled"
      placeholder="选择AI模型"
      style="width: 200px"
      @change="handleModelChange"
    >
      <a-select-option
        v-for="model in models"
        :key="model.modelKey"
        :value="model.modelKey"
      >
        <div class="model-option">
          <span class="model-name">{{ model.modelName }}</span>
          <a-tag :color="getTierColor(model.tier)" size="small" class="tier-tag">
            {{ getTierLabel(model.tier) }}
          </a-tag>
          <span class="model-points">{{ model.pointsPerKToken }}积分/1K</span>
        </div>
      </a-select-option>
    </a-select>

    <!-- 模型详情卡片 -->
    <a-card v-if="currentModel" size="small" class="model-info-card">
      <div class="model-details">
        <div class="detail-row">
          <span class="label">模型:</span>
          <span class="value">{{ currentModel.modelName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">等级:</span>
          <a-tag :color="getTierColor(currentModel.tier)">
            {{ getTierLabel(currentModel.tier) }}
          </a-tag>
        </div>
        <div class="detail-row">
          <span class="label">费用:</span>
          <span class="value">{{ currentModel.pointsPerKToken }} 积分/1K tokens</span>
        </div>
        <div v-if="currentModel.description" class="detail-row">
          <span class="label">说明:</span>
          <span class="value">{{ currentModel.description }}</span>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { listEnabledModels } from '@/api/aImoxingpeizhi'

// Props
interface Props {
  defaultModelKey?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  defaultModelKey: 'deepseek-reasoner',
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
const loadModels = async () => {
  loading.value = true
  try {
    const res = await listEnabledModels()
    if (res.code === 0 && res.data) {
      models.value = res.data
      // 如果默认模型不在列表中,选择第一个
      const hasDefault = models.value.some(m => m.modelKey === selectedModelKey.value)
      if (!hasDefault && models.value.length > 0) {
        selectedModelKey.value = models.value[0].modelKey || ''
      }
    } else {
      message.error('加载模型列表失败: ' + res.message)
    }
  } catch (error: any) {
    message.error('加载模型列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleModelChange = (value: string) => {
  const model = models.value.find(m => m.modelKey === value)
  if (model) {
    emit('change', value, model)
  }
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
.ai-model-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-name {
  flex: 1;
  font-weight: 500;
}

.tier-tag {
  margin: 0;
}

.model-points {
  font-size: 12px;
  color: #666;
}

.model-info-card {
  margin-top: 8px;
}

.model-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.detail-row .label {
  color: #666;
  min-width: 50px;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
}
</style>
