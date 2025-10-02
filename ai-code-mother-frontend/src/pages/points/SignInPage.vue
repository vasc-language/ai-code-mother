<template>
  <div class="sign-in-page">
    <a-card class="sign-in-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <CalendarOutlined class="title-icon" />
          <span>每日签到</span>
        </div>
      </template>

      <!-- 签到区域 -->
      <div class="sign-in-section">
        <!-- 签到按钮 -->
        <div class="sign-in-button-wrapper">
          <a-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="hasSignedInToday"
            @click="handleSignIn"
            class="sign-in-button"
          >
            <template #icon>
              <CheckCircleOutlined v-if="hasSignedInToday" />
              <GiftOutlined v-else />
            </template>
            {{ hasSignedInToday ? '今日已签到' : '立即签到' }}
          </a-button>
        </div>

        <!-- 签到状态 -->
        <div class="sign-in-status">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-statistic
                title="连续签到"
                :value="continuousDays"
                suffix="天"
                :value-style="{ color: '#3f8600' }"
              >
                <template #prefix>
                  <FireOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="12">
              <a-statistic
                title="今日可得"
                :value="todayPoints"
                suffix="积分"
                :value-style="{ color: '#cf1322' }"
              >
                <template #prefix>
                  <GoldOutlined />
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </div>

        <!-- 签到奖励说明 -->
        <a-divider>签到奖励规则</a-divider>
        <div class="reward-rules">
          <a-timeline>
            <a-timeline-item color="blue">
              <template #dot>
                <GoldOutlined style="font-size: 16px" />
              </template>
              每日签到：<span class="points-highlight">+5积分</span>
            </a-timeline-item>
            <a-timeline-item color="green">
              <template #dot>
                <TrophyOutlined style="font-size: 16px" />
              </template>
              连续3天：额外<span class="points-highlight">+3积分</span>（总8积分）
            </a-timeline-item>
            <a-timeline-item color="orange">
              <template #dot>
                <CrownOutlined style="font-size: 16px" />
              </template>
              连续7天：额外<span class="points-highlight">+10积分</span>（总15积分）
            </a-timeline-item>
            <a-timeline-item color="red">
              <template #dot>
                <StarOutlined style="font-size: 16px" />
              </template>
              连续30天：额外<span class="points-highlight">+50积分</span>（总55积分）
            </a-timeline-item>
          </a-timeline>
        </div>

        <!-- 签到日历 -->
        <a-divider>本月签到记录</a-divider>
        <div class="calendar-section">
          <a-calendar
            :fullscreen="false"
            @panelChange="onPanelChange"
          >
            <template #dateCellRender="{ current }">
              <div class="calendar-cell">
                <CheckCircleOutlined
                  v-if="isSignedDate(current)"
                  class="signed-icon"
                />
              </div>
            </template>
          </a-calendar>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  CalendarOutlined,
  CheckCircleOutlined,
  GiftOutlined,
  FireOutlined,
  GoldOutlined,
  TrophyOutlined,
  CrownOutlined,
  StarOutlined
} from '@ant-design/icons-vue'
import type { Dayjs } from 'dayjs'
import { dailySignIn, getSignInStatus } from '@/api/qiedaoguanli'
import { getUserLogin } from '@/api/userController'

const router = useRouter()
const loading = ref(false)
const hasSignedInToday = ref(false)
const continuousDays = ref(0)
const signedDates = ref<string[]>([])

// 检查登录状态
const checkLoginStatus = async () => {
  try {
    const res = await getUserLogin()
    if (res.data.code !== 0 || !res.data.data) {
      message.warning('请先登录进行签到')
      router.push({
        path: '/user/login',
        query: { redirect: '/points/sign-in' }
      })
      return false
    }
    return true
  } catch (error) {
    message.error('登录状态检查失败，请重新登录')
    router.push({
      path: '/user/login',
      query: { redirect: '/points/sign-in' }
    })
    return false
  }
}

// 计算今日可得积分
const todayPoints = computed(() => {
  if (hasSignedInToday.value) {
    return 0
  }
  const days = continuousDays.value + 1
  if (days >= 30) return 55
  if (days >= 7) return 15
  if (days >= 3) return 8
  return 5
})

// 检查是否为签到日期
const isSignedDate = (date: Dayjs) => {
  const dateStr = date.format('YYYY-MM-DD')
  return signedDates.value.includes(dateStr)
}

// 获取签到状态
const fetchSignInStatus = async () => {
  try {
    const res = await getSignInStatus()
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      hasSignedInToday.value = data.hasSignedInToday || false
      continuousDays.value = data.continuousDays || 0
      signedDates.value = data.signedDates || []
    }
  } catch (error) {
    console.error('获取签到状态失败:', error)
  }
}

// 执行签到
const handleSignIn = async () => {
  loading.value = true
  try {
    const res = await dailySignIn()
    if (res.data.code === 0 && res.data.data) {
      const points = res.data.data.points || todayPoints.value
      message.success(`签到成功！获得 ${points} 积分`)
      await fetchSignInStatus()
    } else {
      message.error(res.data.message || '签到失败')
    }
  } catch (error: any) {
    console.error('签到失败:', error)
    message.error(error.message || '签到失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 日历面板切换
const onPanelChange = (value: Dayjs) => {
  console.log('Calendar panel changed:', value.format('YYYY-MM'))
}

onMounted(async () => {
  // 先检查登录状态
  const isLoggedIn = await checkLoginStatus()
  if (!isLoggedIn) {
    return
  }

  // 已登录，加载签到状态
  fetchSignInStatus()
})
</script>

<style scoped>
.sign-in-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.sign-in-card {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
}

:deep(.ant-card-head) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
}

.title-icon {
  font-size: 24px;
}

.sign-in-section {
  padding: var(--spacing-lg) 0;
}

.sign-in-button-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-xl);
}

.sign-in-button {
  height: 56px;
  padding: 0 var(--spacing-xl);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border: none;
  box-shadow: var(--shadow-md);
  transition: var(--transition-normal);
}

.sign-in-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.sign-in-button:disabled {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3) 0%, rgba(139, 92, 246, 0.3) 100%);
  cursor: not-allowed;
}

.sign-in-status {
  margin: var(--spacing-xl) 0;
  padding: var(--spacing-lg);
  background: rgba(99, 102, 241, 0.05);
  border-radius: var(--radius-lg);
}

:deep(.ant-statistic) {
  text-align: center;
}

:deep(.ant-statistic-title) {
  font-size: var(--font-size-sm);
  color: rgba(0, 0, 0, 0.65);
}

:deep(.ant-statistic-content) {
  font-size: 32px;
  font-weight: var(--font-weight-bold);
}

.reward-rules {
  padding: var(--spacing-md) 0;
}

:deep(.ant-timeline-item-content) {
  font-size: var(--font-size-base);
  color: rgba(0, 0, 0, 0.85);
}

.points-highlight {
  font-weight: var(--font-weight-bold);
  color: #f57c00;
  font-size: var(--font-size-lg);
}

.calendar-section {
  margin-top: var(--spacing-xl);
  padding: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-picker-calendar) {
  background: transparent;
  font-size: 16px;
}

:deep(.ant-picker-calendar-header) {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

:deep(.ant-picker-calendar-header .ant-select) {
  display: inline-flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

:deep(.ant-picker-calendar-header .ant-select-selector) {
  display: flex;
  align-items: center;
  padding: 8px 16px !important;
  border-radius: var(--radius-md);
  border-color: rgba(99, 102, 241, 0.2);
  background: white;
}

:deep(.ant-picker-calendar-header .ant-select:hover .ant-select-selector) {
  border-color: rgba(99, 102, 241, 0.5);
}

:deep(.ant-picker-panel),
:deep(.ant-picker-calendar-mini .ant-picker-panel) {
  width: 100%;
}

/* 表头样式 */
:deep(.ant-picker-content thead th) {
  text-align: center;
  padding: 16px 8px;
  font-size: 15px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  background: rgba(99, 102, 241, 0.05);
}

/* 日期单元格样式 */
:deep(.ant-picker-cell) {
  padding: 4px;
}

:deep(.ant-picker-cell .ant-picker-cell-inner) {
  min-height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  position: relative;
  font-size: 16px;
  font-weight: 500;
}

/* 日期单元格hover效果 */
:deep(.ant-picker-cell:hover .ant-picker-cell-inner) {
  background: rgba(99, 102, 241, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

/* 当前月份日期 */
:deep(.ant-picker-cell-in-view .ant-picker-cell-inner) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

/* 非当前月份日期 */
:deep(.ant-picker-cell-disabled .ant-picker-cell-inner) {
  background: transparent;
  opacity: 0.3;
}

/* 今天日期样式 */
:deep(.ant-picker-cell-today .ant-picker-cell-inner) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.12) 100%);
  border: 2px solid rgba(99, 102, 241, 0.5);
  font-weight: 700;
  color: var(--primary-color);
}

/* 选中日期样式 */
:deep(.ant-picker-cell-selected .ant-picker-cell-inner) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  font-weight: 700;
}

:deep(.ant-picker-calendar-date-content) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 0;
  left: 0;
}

.calendar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.signed-icon {
  color: #52c41a;
  font-size: 28px;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 50%;
  padding: 8px;
  animation: signedPulse 2s ease-in-out infinite;
  box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
}

@keyframes signedPulse {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(82, 196, 26, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sign-in-page {
    padding: var(--spacing-md);
  }

  .sign-in-button {
    width: 100%;
  }

  :deep(.ant-statistic-content) {
    font-size: 24px;
  }
}
</style>
