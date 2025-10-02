<template>
  <div class="points-detail-page">
    <a-card class="points-card" :bordered="false">
      <template #title>
        <div class="card-title">
          <WalletOutlined class="title-icon" />
          <span>积分明细</span>
        </div>
      </template>

      <!-- 积分概览 -->
      <div class="points-overview">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic
              title="当前可用积分"
              :value="availablePoints"
              suffix="积分"
              :value-style="{ color: '#3f8600', fontSize: '32px' }"
            >
              <template #prefix>
                <GoldOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="8">
            <a-statistic
              title="累计获得"
              :value="totalEarned"
              suffix="积分"
              :value-style="{ color: '#1890ff', fontSize: '32px' }"
            >
              <template #prefix>
                <RiseOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="8">
            <a-statistic
              title="累计消耗"
              :value="totalConsumed"
              suffix="积分"
              :value-style="{ color: '#cf1322', fontSize: '32px' }"
            >
              <template #prefix>
                <FallOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <a-divider />

      <!-- 筛选器 -->
      <div class="filter-section">
        <a-space wrap>
          <span>筛选类型：</span>
          <a-radio-group v-model:value="filterType" button-style="solid" @change="handleFilterChange">
            <a-radio-button value="ALL">全部</a-radio-button>
            <a-radio-button value="SIGN_IN">签到</a-radio-button>
            <a-radio-button value="REGISTER">注册</a-radio-button>
            <a-radio-button value="INVITE">邀请</a-radio-button>
            <a-radio-button value="GENERATE">生成消耗</a-radio-button>
            <a-radio-button value="FIRST_GENERATE">首次生成</a-radio-button>
          </a-radio-group>
        </a-space>
      </div>

      <!-- 积分明细列表 -->
      <div class="points-list">
        <a-timeline>
          <a-timeline-item
            v-for="record in pointsRecords"
            :key="record.id"
            :color="getTimelineColor(record.points)"
          >
            <template #dot>
              <component
                :is="getTypeIcon(record.type)"
                style="font-size: 16px"
              />
            </template>
            <div class="record-item">
              <div class="record-header">
                <span class="record-type">{{ getTypeText(record.type) }}</span>
                <span
                  class="record-points"
                  :class="{ 'points-earned': record.points > 0, 'points-consumed': record.points < 0 }"
                >
                  {{ record.points > 0 ? '+' : '' }}{{ record.points }}
                </span>
              </div>
              <div class="record-reason">{{ record.reason }}</div>
              <div class="record-footer">
                <span class="record-time">{{ formatDate(record.createTime) }}</span>
                <span class="record-balance">余额：{{ record.balance }}</span>
                <span v-if="record.expireTime" class="record-expire">
                  {{ formatExpireTime(record.expireTime) }}
                </span>
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <a-button
            type="link"
            :loading="loading"
            @click="loadMore"
          >
            加载更多
          </a-button>
        </div>

        <!-- 空状态 -->
        <a-empty
          v-if="pointsRecords.length === 0 && !loading"
          description="暂无积分记录"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  WalletOutlined,
  GoldOutlined,
  RiseOutlined,
  FallOutlined,
  GiftOutlined,
  UserAddOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  TrophyOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getPointsOverview, getPointsRecords } from '@/api/jifenguanli'
import { getUserLogin } from '@/api/userController'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

interface PointsRecord {
  id: number
  points: number
  balance: number
  type: string
  reason: string
  createTime: string
  expireTime?: string
}

const availablePoints = ref(0)
const totalEarned = ref(0)
const totalConsumed = ref(0)
const filterType = ref('ALL')
const pointsRecords = ref<PointsRecord[]>([])
const loading = ref(false)
const hasMore = ref(false)
const isCheckingLogin = ref(true)

// 检查登录状态
const checkLoginStatus = async () => {
  try {
    const res = await getUserLogin()
    if (res.data.code !== 0 || !res.data.data) {
      message.warning('请先登录查看积分明细')
      router.push({
        path: '/user/login',
        query: { redirect: '/points/detail' }
      })
      return false
    }
    return true
  } catch (error) {
    message.error('登录状态检查失败，请重新登录')
    router.push({
      path: '/user/login',
      query: { redirect: '/points/detail' }
    })
    return false
  } finally {
    isCheckingLogin.value = false
  }
}

// 获取积分概览
const fetchPointsOverview = async () => {
  try {
    const res = await getPointsOverview()
    if (res.data.code === 0 && res.data.data) {
      availablePoints.value = res.data.data.availablePoints || 0
      totalEarned.value = res.data.data.totalEarned || 0
      totalConsumed.value = res.data.data.totalConsumed || 0
    }
  } catch (error) {
    console.error('获取积分概览失败:', error)
  }
}

// 获取积分明细
const fetchPointsRecords = async (reset = false) => {
  if (reset) {
    pointsRecords.value = []
  }

  loading.value = true
  try {
    const res = await getPointsRecords({
      type: filterType.value === 'ALL' ? undefined : filterType.value,
    })

    if (res.data.code === 0 && res.data.data) {
      pointsRecords.value = res.data.data
      hasMore.value = false // 当前API返回全部数据，不支持分页
    }
  } catch (error) {
    console.error('获取积分明细失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多（当前不支持）
const loadMore = () => {
  // 暂不支持分页
}

// 筛选变化
const handleFilterChange = () => {
  fetchPointsRecords(true)
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    SIGN_IN: '签到',
    REGISTER: '注册',
    FIRST_GENERATE: '首次生成',
    INVITE: '邀请',
    GENERATE: '生成消耗',
    EXPIRE: '过期'
  }
  return typeMap[type] || type
}

// 获取类型图标
const getTypeIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    SIGN_IN: GiftOutlined,
    REGISTER: UserAddOutlined,
    FIRST_GENERATE: TrophyOutlined,
    INVITE: TeamOutlined,
    GENERATE: ThunderboltOutlined
  }
  return iconMap[type] || GoldOutlined
}

// 获取时间线颜色
const getTimelineColor = (points: number) => {
  if (points > 0) {
    return 'green'
  }
  if (points < 0) {
    return 'red'
  }
  return 'blue'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化过期时间
const formatExpireTime = (dateStr: string) => {
  const now = dayjs()
  const expireDate = dayjs(dateStr)
  const daysLeft = expireDate.diff(now, 'day')

  if (daysLeft < 0) {
    return '已过期'
  } else if (daysLeft <= 7) {
    return `${daysLeft}天后过期`
  } else {
    return `${expireDate.format('YYYY-MM-DD')}过期`
  }
}

onMounted(async () => {
  // 先检查登录状态
  const isLoggedIn = await checkLoginStatus()
  if (!isLoggedIn) {
    return
  }

  // 已登录，加载数据
  fetchPointsOverview()
  fetchPointsRecords(true)
})
</script>

<style scoped>
.points-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.points-card {
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

.points-overview {
  padding: var(--spacing-lg);
  background: rgba(99, 102, 241, 0.05);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-md);
}

:deep(.ant-statistic) {
  text-align: center;
}

:deep(.ant-statistic-title) {
  font-size: var(--font-size-sm);
  color: rgba(0, 0, 0, 0.65);
}

.filter-section {
  margin: var(--spacing-lg) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.points-list {
  margin-top: var(--spacing-lg);
  max-height: 600px;
  overflow-y: auto;
  padding-right: var(--spacing-sm);
}

.record-item {
  padding: var(--spacing-md);
  background: rgba(99, 102, 241, 0.03);
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
  display: grid;
  row-gap: var(--spacing-sm);
}

.record-item:hover {
  background: rgba(99, 102, 241, 0.08);
}

.record-header {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  column-gap: var(--spacing-lg);
}

.record-type {
  font-weight: var(--font-weight-medium);
  color: rgba(0, 0, 0, 0.85);
  font-size: var(--font-size-base);
}

.record-points {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  justify-self: end;
}

.points-earned {
  color: #52c41a;
}

.points-consumed {
  color: #ff4d4f;
}

.record-reason {
  color: rgba(0, 0, 0, 0.65);
}

.record-footer {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) minmax(120px, auto) minmax(120px, auto);
  column-gap: var(--spacing-lg);
  row-gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: rgba(0, 0, 0, 0.45);
}

.record-footer span {
  display: block;
}

.record-time {
  justify-self: start;
}

.record-balance {
  justify-self: start;
}

.record-expire {
  justify-self: start;
  color: #faad14;
  font-weight: var(--font-weight-medium);
}

.load-more {
  text-align: center;
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-lg);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .points-detail-page {
    padding: var(--spacing-md);
  }

  .points-overview {
    padding: var(--spacing-md);
  }

  :deep(.ant-statistic-content) {
    font-size: 24px !important;
  }

  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .record-footer {
    grid-template-columns: 1fr;
    gap: var(--spacing-xs);
  }
}
</style>
