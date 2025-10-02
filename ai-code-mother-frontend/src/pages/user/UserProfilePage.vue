<template>
  <div class="user-profile-page">
    <a-spin :spinning="loading" tip="加载中...">
      <div class="profile-container">
        <!-- 用户信息卡片 -->
        <UserInfoCard
          v-if="userInfo"
          :user="userInfo"
          :editable="isOwnProfile"
          @edit-success="handleEditSuccess"
        />

        <!-- 统计数据面板 -->
        <StatisticsPanel v-if="statistics" :statistics="statistics" />


        <!-- Tab标签页 -->
        <a-card class="apps-card" :bordered="false">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="apps" tab="我的应用">
              <!-- 应用列表 -->
              <UserAppsList
                v-if="userInfo"
                :user-id="userInfo.id"
                @refresh="refreshProfile"
              />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import UserInfoCard from '@/components/user/UserInfoCard.vue'
import StatisticsPanel from '@/components/user/StatisticsPanel.vue'
import UserAppsList from '@/components/user/UserAppsList.vue'
import request from '@/request'

const route = useRoute()
const loading = ref(false)
const activeTab = ref('apps')

// 用户信息
const userInfo = ref<any>(null)
const statistics = ref<any>(null)
const currentUserId = ref<number | null>(null)

// 判断是否是自己的主页
const isOwnProfile = computed(() => {
  if (!currentUserId.value || !userInfo.value) return false
  return currentUserId.value === userInfo.value.id
})

// 获取当前登录用户
const getCurrentUser = async () => {
  try {
    const res = await request.get('/user/get/login')
    if (res.data.code === 0) {
      currentUserId.value = res.data.data.id
    }
  } catch (error) {
    console.error('获取当前用户失败:', error)
  }
}

// 获取用户主页信息
const getUserProfile = async () => {
  loading.value = true
  try {
    const userId = route.params.userId as string
    const params = userId ? { userId: Number(userId) } : {}

    const res = await request.get('/user/profile', { params })
    if (res.data.code === 0) {
      userInfo.value = res.data.data.user
      statistics.value = res.data.data.statistics
      // 邀请板块已移除
    } else {
      message.error(res.data.message || '获取用户信息失败')
    }
  } catch (error: any) {
    console.error('获取用户主页失败:', error)
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 编辑成功回调
const handleEditSuccess = () => {
  message.success('更新成功')
  getUserProfile()
}

// 刷新主页
const refreshProfile = () => {
  getUserProfile()
}

// 获取邀请信息

onMounted(async () => {
  await getCurrentUser()
  await getUserProfile()
})
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 200px);
  padding: 24px;
  background: #f0f2f5;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.apps-card {
  margin-top: 24px;
  border-radius: 8px;
}

:deep(.ant-card-body) {
  padding: 0;
}

:deep(.ant-tabs-nav) {
  padding: 0 24px;
  margin: 0;
}

:deep(.ant-tabs-content) {
  padding: 24px;
}
</style>
