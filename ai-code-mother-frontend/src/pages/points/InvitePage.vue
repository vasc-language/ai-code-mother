<template>
  <div class="invite-page">
    <a-row :gutter="16">
      <!-- 左侧：邀请卡片 -->
      <a-col :xs="24" :lg="12">
        <a-card class="invite-card" :bordered="false">
          <template #title>
            <div class="card-title">
              <TeamOutlined class="title-icon" />
              <span>邀请好友</span>
            </div>
          </template>

          <!-- 邀请码显示 -->
          <div class="invite-code-section">
            <h3>我的邀请码</h3>
            <div class="invite-code-display">
              <a-input
                :value="inviteCode"
                readonly
                size="large"
                class="invite-code-input"
              >
                <template #addonAfter>
                  <a-button
                    type="link"
                    @click="copyInviteCode"
                    :loading="copyLoading"
                  >
                    <CopyOutlined />
                    复制
                  </a-button>
                </template>
              </a-input>
            </div>
          </div>

          <!-- 邀请链接 -->
          <div class="invite-link-section">
            <h3>邀请链接</h3>
            <div class="invite-link-display">
              <a-input
                :value="inviteLink"
                readonly
                size="large"
                class="invite-link-input"
              >
                <template #addonAfter>
                  <a-button
                    type="link"
                    @click="copyInviteLink"
                    :loading="copyLinkLoading"
                  >
                    <LinkOutlined />
                    复制链接
                  </a-button>
                </template>
              </a-input>
            </div>
          </div>

          <!-- 奖励说明 -->
          <a-divider>邀请奖励</a-divider>
          <div class="reward-info">
            <a-alert
              message="邀请奖励规则"
              type="info"
              show-icon
            >
              <template #description>
                <ul class="reward-list">
                  <li>好友注册：你和好友各得 <span class="points-highlight">50积分</span></li>
                  <li>好友首次生成：你得 <span class="points-highlight">50积分</span>，好友得 <span class="points-highlight">30积分</span></li>
                  <li>每日最多获得 <span class="points-highlight">3次</span> 邀请奖励</li>
                </ul>
              </template>
            </a-alert>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：邀请记录 -->
      <a-col :xs="24" :lg="12">
        <a-card class="records-card" :bordered="false">
          <template #title>
            <div class="card-title">
              <HistoryOutlined class="title-icon" />
              <span>邀请记录</span>
            </div>
          </template>

          <!-- 统计信息 -->
          <div class="stats-section">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-statistic
                  title="累计邀请"
                  :value="totalInvites"
                  suffix="人"
                  :value-style="{ color: '#3f8600', fontSize: '24px' }"
                />
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="成功注册"
                  :value="registeredCount"
                  suffix="人"
                  :value-style="{ color: '#1890ff', fontSize: '24px' }"
                />
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="累计奖励"
                  :value="totalRewards"
                  suffix="积分"
                  :value-style="{ color: '#cf1322', fontSize: '24px' }"
                />
              </a-col>
            </a-row>
          </div>

          <a-divider />

          <!-- 邀请记录列表 -->
          <div class="records-list">
            <a-list
              :data-source="inviteRecords"
              :loading="loading"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #avatar>
                      <a-avatar :style="{ backgroundColor: getStatusColor(item.status) }">
                        <UserOutlined />
                      </a-avatar>
                    </template>
                    <template #title>
                      <span>{{ item.inviteeId ? `用户 ${item.inviteeId}` : '新用户' }}</span>
                      <a-tag :color="getStatusTagColor(item.status)" style="margin-left: 8px">
                        {{ getStatusText(item.status) }}
                      </a-tag>
                    </template>
                    <template #description>
                      <div>
                        <div>注册时间：{{ formatDate(item.registerTime) }}</div>
                        <div v-if="item.inviterPoints && item.inviterPoints > 0" style="color: #f57c00; font-weight: bold;">
                          已获得 {{ item.inviterPoints }} 积分
                        </div>
                      </div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>

            <!-- 空状态 -->
            <a-empty
              v-if="inviteRecords.length === 0 && !loading"
              description="暂无邀请记录"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  TeamOutlined,
  HistoryOutlined,
  CopyOutlined,
  LinkOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getInviteCode, getInviteRecords } from '@/api/yaoqingguanli'
import { getUserLogin } from '@/api/userController'
import { APP_DOMAIN } from '@/config/env'

const router = useRouter()

interface InviteRecord {
  id: number
  inviteeId?: number
  status: string
  registerTime: string
  inviterPoints?: number
  inviteePoints?: number
  rewardTime?: string
}

const inviteCode = ref('')
const inviteLink = computed(() => {
  return `${APP_DOMAIN}/user/register?inviteCode=${inviteCode.value}`
})
const copyLoading = ref(false)
const copyLinkLoading = ref(false)
const loading = ref(false)
const inviteRecords = ref<InviteRecord[]>([])

// 统计数据
const totalInvites = ref(0)
const registeredCount = ref(0)
const totalRewards = ref(0)

// 检查登录状态
const checkLoginStatus = async () => {
  try {
    const res = await getUserLogin()
    if (res.data.code !== 0 || !res.data.data) {
      message.warning('请先登录查看邀请信息')
      router.push({
        path: '/user/login',
        query: { redirect: '/points/invite' }
      })
      return false
    }
    return true
  } catch (error) {
    message.error('登录状态检查失败，请重新登录')
    router.push({
      path: '/user/login',
      query: { redirect: '/points/invite' }
    })
    return false
  }
}


// 复制邀请码
const copyInviteCode = async () => {
  copyLoading.value = true
  try {
    await navigator.clipboard.writeText(inviteCode.value)
    message.success('邀请码已复制到剪贴板')
  } catch (error) {
    message.error('复制失败，请手动复制')
  } finally {
    copyLoading.value = false
  }
}

// 复制邀请链接
const copyInviteLink = async () => {
  copyLinkLoading.value = true
  try {
    await navigator.clipboard.writeText(inviteLink.value)
    message.success('邀请链接已复制到剪贴板')
  } catch (error) {
    message.error('复制失败，请手动复制')
  } finally {
    copyLinkLoading.value = false
  }
}

// 获取邀请码和记录
const fetchInviteData = async () => {
  loading.value = true
  try {
    // 获取邀请码
    const codeRes = await getInviteCode()
    if (codeRes.data.code === 0 && codeRes.data.data) {
      inviteCode.value = codeRes.data.data.inviteCode || ''
    }

    // 获取邀请记录
    const recordsRes = await getInviteRecords()
    if (recordsRes.data.code === 0 && recordsRes.data.data) {
      inviteRecords.value = recordsRes.data.data

      // 计算统计数据
      totalInvites.value = inviteRecords.value.length
      registeredCount.value = inviteRecords.value.filter(r =>
        r.status === 'REGISTERED' || r.status === 'REWARDED'
      ).length
      totalRewards.value = inviteRecords.value.reduce((sum, r) =>
        sum + (r.inviterPoints || 0), 0
      )
    }
  } catch (error) {
    console.error('获取邀请数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '待确认',
    REGISTERED: '已注册',
    REWARDED: '已奖励',
    REVOKED: '已回收'
  }
  return statusMap[status] || status
}

// 状态标签颜色
const getStatusTagColor = (status: string) => {
  const colorMap: Record<string, string> = {
    PENDING: 'default',
    REGISTERED: 'blue',
    REWARDED: 'success',
    REVOKED: 'warning'
  }
  return colorMap[status] || 'default'
}

// 状态头像颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    PENDING: '#d9d9d9',
    REGISTERED: '#1890ff',
    REWARDED: '#52c41a',
    REVOKED: '#faad14'
  }
  return colorMap[status] || '#d9d9d9'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dateStr ? dayjs(dateStr).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(async () => {
  // 先检查登录状态
  const isLoggedIn = await checkLoginStatus()
  if (!isLoggedIn) {
    return
  }

  // 已登录，加载邀请数据
  fetchInviteData()
})
</script>

<style scoped>
.invite-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.invite-card,
.records-card {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  margin-bottom: var(--spacing-lg);
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

.invite-code-section,
.invite-link-section {
  margin-bottom: var(--spacing-xl);
}

.invite-code-section h3,
.invite-link-section h3 {
  margin-bottom: var(--spacing-md);
  color: rgba(0, 0, 0, 0.85);
}

.invite-code-display,
.invite-link-display {
  padding: var(--spacing-md);
  background: rgba(99, 102, 241, 0.05);
  border-radius: var(--radius-lg);
}

:deep(.ant-input-group-addon) {
  background: transparent;
  border: none;
}

:deep(.ant-btn-link) {
  color: var(--primary-color);
  font-weight: var(--font-weight-medium);
}

.reward-info {
  margin-top: var(--spacing-lg);
}

.reward-list {
  margin: var(--spacing-sm) 0 0 0;
  padding-left: var(--spacing-lg);
}

.reward-list li {
  margin-bottom: var(--spacing-xs);
  color: rgba(0, 0, 0, 0.85);
}

.points-highlight {
  font-weight: var(--font-weight-bold);
  color: #f57c00;
  font-size: var(--font-size-base);
}

.stats-section {
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

.records-list {
  max-height: 500px;
  overflow-y: auto;
}

:deep(.ant-list-item) {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

:deep(.ant-list-item:hover) {
  background: rgba(99, 102, 241, 0.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .invite-page {
    padding: var(--spacing-md);
  }

  :deep(.ant-statistic-content) {
    font-size: 18px !important;
  }
}
</style>
