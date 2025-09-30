<template>
  <div class="user-apps-list">
    <!-- 筛选和排序工具栏 -->
    <div class="toolbar">
      <a-space :size="12">
        <a-select
          v-model:value="filters.genType"
          style="width: 140px"
          @change="handleFilterChange"
          placeholder="选择类型"
        >
          <a-select-option value="all">全部类型</a-select-option>
          <a-select-option value="HTML">HTML</a-select-option>
          <a-select-option value="多文件项目">多文件项目</a-select-option>
          <a-select-option value="Vue应用">Vue应用</a-select-option>
        </a-select>

        <a-select
          v-model:value="filters.sortBy"
          style="width: 140px"
          @change="handleFilterChange"
          placeholder="排序字段"
        >
          <a-select-option value="createTime">创建时间</a-select-option>
          <a-select-option value="updateTime">更新时间</a-select-option>
          <a-select-option value="appName">应用名称</a-select-option>
        </a-select>

        <a-select
          v-model:value="filters.sortOrder"
          style="width: 100px"
          @change="handleFilterChange"
          placeholder="排序方式"
        >
          <a-select-option value="descend">降序</a-select-option>
          <a-select-option value="ascend">升序</a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 应用列表 -->
    <a-spin :spinning="loading">
      <div v-if="appsList.length > 0" class="apps-grid">
        <a-card
          v-for="app in appsList"
          :key="app.id"
          class="app-card"
          hoverable
          :bordered="false"
        >
          <template #cover>
            <div class="app-cover">
              <img
                v-if="app.cover"
                :src="app.cover"
                :alt="app.appName"
              />
              <div v-else class="default-cover">
                <AppstoreOutlined style="font-size: 48px; color: #d9d9d9" />
              </div>
            </div>
          </template>

          <div class="app-info">
            <div class="app-header">
              <div class="app-title">{{ app.appName }}</div>
              <a-tag color="blue" class="app-type-tag">{{ app.codeGenType }}</a-tag>
            </div>
            <div class="app-time">
              <ClockCircleOutlined class="time-icon" />
              {{ formatTime(app.createTime) }}
            </div>
          </div>

          <template #actions>
            <a-tooltip title="使用应用">
              <PlayCircleOutlined @click="handleUseApp(app.id)" />
            </a-tooltip>
            <a-tooltip title="编辑应用">
              <EditOutlined @click="handleEditApp(app.id)" />
            </a-tooltip>
            <a-popconfirm
              title="确定要删除这个应用吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteApp(app.id)"
            >
              <a-tooltip title="删除应用">
                <DeleteOutlined />
              </a-tooltip>
            </a-popconfirm>
          </template>
        </a-card>
      </div>

      <!-- 空状态 -->
      <a-empty
        v-else
        description="暂无应用"
        style="margin: 60px 0"
      >
        <a-button type="primary" @click="handleCreateApp">
          创建应用
        </a-button>
      </a-empty>
    </a-spin>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <a-pagination
        v-model:current="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        show-size-changer
        :page-size-options="['10', '20', '30', '50']"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  AppstoreOutlined,
  PlayCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import request from '@/request'

interface Props {
  userId: number
}

interface Emits {
  (e: 'refresh'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

const loading = ref(false)
const appsList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filters = reactive({
  genType: 'all',
  sortBy: 'createTime',
  sortOrder: 'descend',
})

// 获取应用列表
const fetchApps = async () => {
  loading.value = true
  try {
    const res = await request.post('/user/apps', {
      userId: props.userId,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      genType: filters.genType,
      sortBy: filters.sortBy,
      sortOrder: filters.sortOrder,
    })

    if (res.data.code === 0) {
      appsList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    } else {
      message.error(res.data.message || '获取应用列表失败')
    }
  } catch (error: any) {
    console.error('获取应用列表失败:', error)
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 筛选/排序改变
const handleFilterChange = () => {
  currentPage.value = 1
  fetchApps()
}

// 分页改变
const handlePageChange = () => {
  fetchApps()
}

// 使用应用
const handleUseApp = (id: number) => {
  router.push(`/app/chat/${id}`)
}

// 编辑应用
const handleEditApp = (id: number) => {
  router.push(`/app/edit/${id}`)
}

// 删除应用
const handleDeleteApp = async (id: number) => {
  try {
    const res = await request.post('/app/delete', { id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchApps()
      emit('refresh')
    } else {
      message.error(res.data.message || '删除失败')
    }
  } catch (error: any) {
    console.error('删除应用失败:', error)
    message.error(error.message || '删除失败')
  }
}

// 创建应用
const handleCreateApp = () => {
  router.push('/')
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchApps()
})
</script>

<style scoped>
.user-apps-list {
  min-height: 400px;
}

.toolbar {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

/* 确保下拉选择器内容对齐 */
:deep(.ant-select) {
  text-align: left;
}

:deep(.ant-select-selector) {
  display: flex;
  align-items: center;
  padding: 0 11px !important;
}

:deep(.ant-select-selection-item) {
  display: flex;
  align-items: center;
  line-height: 32px;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.app-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.app-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.app-cover {
  height: 180px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.app-info {
  padding: 16px;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.app-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 24px;
}

.app-type-tag {
  flex-shrink: 0;
  margin: 0;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.app-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8c8c8c;
  line-height: 20px;
}

.time-icon {
  font-size: 14px;
  color: #bfbfbf;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

:deep(.ant-card-actions) {
  background: #fafafa;
}

:deep(.ant-card-actions > li) {
  margin: 8px 0;
}

:deep(.ant-card-actions > li > span) {
  font-size: 18px;
  color: #595959;
  cursor: pointer;
  transition: color 0.3s;
}

:deep(.ant-card-actions > li > span:hover) {
  color: #1890ff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .apps-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
  }
}
</style>