<template>
  <div class="points-display">
    <a-tooltip placement="bottom">
      <template #title>
        <div class="points-tooltip">
          <p>当前可用积分：{{ availablePoints }}</p>
          <p class="tip">每次生成消耗约6积分</p>
        </div>
      </template>
      <div class="points-badge" @click="goToPointsDetail">
        <GoldOutlined class="points-icon" />
        <span class="points-value">{{ availablePoints }}</span>
      </div>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { GoldOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { getCurrentPoints } from '@/api/jifenguanli'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const availablePoints = ref<number>(0)
const loading = ref<boolean>(false)

// 是否已登录
const isLoggedIn = computed(() => {
  return loginUserStore.loginUser && loginUserStore.loginUser.id
})

// 获取用户积分
const fetchUserPoints = async () => {
  if (!isLoggedIn.value) {
    availablePoints.value = 0
    return
  }

  loading.value = true
  try {
    const res = await getCurrentPoints()
    if (res.data.code === 0 && res.data.data) {
      availablePoints.value = res.data.data.availablePoints || 0
    }
  } catch (error) {
    console.error('获取积分失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到积分明细页面
const goToPointsDetail = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  router.push('/points/detail')
}

// 组件挂载时获取积分
onMounted(() => {
  if (isLoggedIn.value) {
    fetchUserPoints()
  }
})

// 暴露刷新方法，供父组件调用
defineExpose({
  refresh: fetchUserPoints
})
</script>

<style scoped>
.points-display {
  display: flex;
  align-items: center;
  margin-right: var(--spacing-md);
}

.points-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(255, 152, 0, 0.1) 100%);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: var(--transition-fast);
  user-select: none;
}

.points-badge:hover {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.2) 0%, rgba(255, 152, 0, 0.2) 100%);
  border-color: rgba(255, 193, 7, 0.5);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.2);
}

.points-badge:active {
  transform: translateY(0);
}

.points-icon {
  font-size: 18px;
  color: #ffa726;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.points-value {
  font-size: 16px;
  font-weight: var(--font-weight-bold);
  color: #f57c00;
  min-width: 30px;
  text-align: right;
}

.points-tooltip {
  text-align: center;
}

.points-tooltip p {
  margin: 4px 0;
}

.points-tooltip .tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .points-badge {
    padding: 4px 8px;
  }

  .points-icon {
    font-size: 16px;
  }

  .points-value {
    font-size: 14px;
    min-width: 24px;
  }
}
</style>
