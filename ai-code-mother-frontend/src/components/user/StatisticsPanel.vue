<template>
  <a-card class="statistics-panel" :bordered="false">
    <a-row :gutter="[16, 16]">
      <a-col :xs="12" :sm="12" :md="6">
        <a-statistic
          title="创建应用"
          :value="statistics.appCount"
          :value-style="{ color: '#1890ff' }"
        >
          <template #suffix>个</template>
          <template #prefix>
            <AppstoreOutlined />
          </template>
        </a-statistic>
      </a-col>

      <a-col :xs="12" :sm="12" :md="6">
        <a-statistic
          title="生成次数"
          :value="statistics.generateCount"
          :value-style="{ color: '#52c41a' }"
        >
          <template #suffix>次</template>
          <template #prefix>
            <ThunderboltOutlined />
          </template>
        </a-statistic>
      </a-col>

      <a-col :xs="12" :sm="12" :md="6">
        <a-statistic
          title="累计使用"
          :value="statistics.joinDays"
          :value-style="{ color: '#fa8c16' }"
        >
          <template #suffix>天</template>
          <template #prefix>
            <CalendarOutlined />
          </template>
        </a-statistic>
      </a-col>

      <a-col :xs="12" :sm="12" :md="6">
        <div class="last-active">
          <div class="statistic-title">最近活跃</div>
          <div class="statistic-value">
            <ClockCircleOutlined class="icon" />
            {{ formatLastActive(statistics.lastActiveTime) }}
          </div>
        </div>
      </a-col>
    </a-row>
  </a-card>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  ThunderboltOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'

interface Props {
  statistics: {
    appCount: number
    generateCount: number
    joinDays: number
    lastActiveTime: string
  }
}

defineProps<Props>()

// 格式化最近活跃时间
const formatLastActive = (time: string) => {
  if (!time) return '暂无'

  const now = new Date().getTime()
  const lastActive = new Date(time).getTime()
  const diff = now - lastActive

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}
</script>

<style scoped>
.statistics-panel {
  margin-top: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.ant-statistic-title) {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

:deep(.ant-statistic-content) {
  font-size: 24px;
  font-weight: 600;
}

.last-active {
  text-align: center;
}

.statistic-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.statistic-value {
  font-size: 16px;
  color: #595959;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.statistic-value .icon {
  color: #722ed1;
  font-size: 18px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.ant-statistic-content) {
    font-size: 20px;
  }

  .statistic-value {
    font-size: 14px;
  }
}
</style>