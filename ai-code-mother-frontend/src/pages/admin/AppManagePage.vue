<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <div class="search-container">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="应用名称" class="search-item">
          <a-input
            v-model:value="searchParams.appName"
            placeholder="输入应用名称"
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="创建者" class="search-item">
          <a-input
            v-model:value="searchParams.userId"
            placeholder="输入用户ID"
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="生成类型" class="search-item">
          <a-select
            v-model:value="searchParams.codeGenType"
            placeholder="选择生成类型"
            class="search-select"
            allow-clear
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option
              v-for="option in CODE_GEN_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="search-button-item">
          <a-button type="primary" html-type="submit" class="search-button" size="large">
            🔍 搜索
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" :width="80" :height="60" />
          <div v-else class="no-cover">无封面</div>
        </template>
        <template v-else-if="column.dataIndex === 'initPrompt'">
          <a-tooltip :title="record.initPrompt">
            <div class="prompt-text">{{ record.initPrompt }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ formatCodeGenType(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <UserInfo :user="record.user" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
            <a-button
              type="default"
              size="small"
              @click="toggleFeatured(record)"
              :class="{ 'featured-btn': record.priority === 99 }"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.id)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建者',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#appManagePage {
  padding: var(--spacing-xl);
  background: transparent;
  margin-top: 0;
}

/* 搜索容器 - 现代化设计 */
.search-container {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%
  );
  backdrop-filter: blur(20px);
  border: 2px solid rgba(0, 56, 255, 0.1);
  border-radius: 24px;
  padding: 32px 40px;
  box-shadow: 0 8px 32px rgba(0, 56, 255, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: var(--spacing-xl);
  transition: all 0.3s ease;
}

.search-container:hover {
  box-shadow: 0 12px 40px rgba(0, 56, 255, 0.12), 0 4px 12px rgba(0, 0, 0, 0.06);
  border-color: rgba(0, 56, 255, 0.15);
}

/* 搜索表单布局 */
.search-form {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

/* 搜索项 */
.search-item {
  margin-bottom: 0 !important;
  flex: 0 0 auto;
}

.search-button-item {
  margin-bottom: 0 !important;
  flex: 0 0 auto;
}

/* 表单标签样式 - 更大更清晰 */
:deep(.ant-form-item-label) {
  padding-bottom: 8px !important;
}

:deep(.ant-form-item-label > label) {
  font-weight: 600 !important;
  color: var(--gray-800) !important;
  font-size: 15px !important;
  letter-spacing: 0.3px;
}

/* 搜索输入框 - 大气设计 */
.search-input {
  width: 220px !important;
  height: 46px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  border: 2px solid rgba(0, 56, 255, 0.15) !important;
  border-radius: 16px !important;
  font-size: 15px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.search-input:hover {
  border-color: rgba(0, 56, 255, 0.3) !important;
  box-shadow: 0 4px 16px rgba(0, 56, 255, 0.08) !important;
  transform: translateY(-1px);
}

.search-input:focus,
.search-input:focus-within {
  background: var(--white) !important;
  border-color: var(--primary-color) !important;
  box-shadow: 0 6px 24px rgba(0, 56, 255, 0.15) !important;
  transform: translateY(-2px);
}

/* 输入框图标 */
.search-icon {
  font-size: 18px;
  margin-right: 4px;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.search-input:hover .search-icon,
.search-input:focus .search-icon {
  opacity: 1;
  transform: scale(1.1);
}

/* 输入框内部样式 */
:deep(.search-input .ant-input) {
  font-size: 15px !important;
  font-weight: 500 !important;
  color: var(--gray-800) !important;
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 !important;
  height: auto !important;
}

:deep(.search-input .ant-input:focus),
:deep(.search-input .ant-input-focused) {
  border: none !important;
  box-shadow: none !important;
}

:deep(.search-input .ant-input::placeholder) {
  color: var(--gray-400) !important;
  font-weight: 400 !important;
}

/* 清除按钮优化 */
:deep(.search-input .ant-input-clear-icon) {
  font-size: 14px;
  color: var(--gray-400);
  transition: all 0.2s ease;
}

:deep(.search-input .ant-input-clear-icon:hover) {
  color: var(--error-color);
  transform: scale(1.2);
}

/* 下拉选择框 - 大气设计 */
.search-select {
  width: 220px !important;
}

:deep(.search-select .ant-select-selector) {
  height: 46px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  border: 2px solid rgba(0, 56, 255, 0.15) !important;
  border-radius: 16px !important;
  padding: 0 16px !important;
  display: flex !important;
  align-items: center !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

:deep(.search-select .ant-select-selector:hover) {
  border-color: rgba(0, 56, 255, 0.3) !important;
  box-shadow: 0 4px 16px rgba(0, 56, 255, 0.08) !important;
  transform: translateY(-1px);
}

:deep(.search-select.ant-select-focused .ant-select-selector) {
  background: var(--white) !important;
  border-color: var(--primary-color) !important;
  box-shadow: 0 6px 24px rgba(0, 56, 255, 0.15) !important;
  transform: translateY(-2px);
}

:deep(.search-select .ant-select-selection-placeholder) {
  color: var(--gray-400) !important;
  font-size: 15px !important;
  font-weight: 400 !important;
  line-height: 46px !important;
}

:deep(.search-select .ant-select-selection-item) {
  font-size: 15px !important;
  font-weight: 500 !important;
  color: var(--gray-800) !important;
  line-height: 46px !important;
}

:deep(.search-select .ant-select-arrow) {
  color: var(--gray-500);
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.search-select:hover .ant-select-arrow),
:deep(.search-select.ant-select-focused .ant-select-arrow) {
  color: var(--primary-color);
  transform: scale(1.1);
}

/* 搜索按钮 - 现代化渐变设计 */
.search-button {
  height: 46px !important;
  padding: 0 32px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 16px !important;
  border: none !important;
  background: linear-gradient(135deg, #0038ff 0%, #00d1ff 100%) !important;
  box-shadow: 0 4px 16px rgba(0, 56, 255, 0.25) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  letter-spacing: 0.5px;
}

.search-button:hover {
  background: linear-gradient(135deg, #0030e0 0%, #00b8e6 100%) !important;
  box-shadow: 0 6px 24px rgba(0, 56, 255, 0.35) !important;
  transform: translateY(-2px) scale(1.02);
}

.search-button:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 56, 255, 0.2) !important;
}

/* 分割线优化 */
:deep(.ant-divider) {
  margin: 0 0 var(--spacing-xl) 0;
  border-color: transparent;
}

/* 表格容器 - 玻璃态效果 */
:deep(.ant-table-wrapper) {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

/* 表格样式优化 */
:deep(.ant-table) {
  background: transparent !important;
  border-radius: var(--radius-xl);
  overflow: hidden;
}

/* 表头样式 */
:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, rgba(0, 56, 255, 0.08) 0%, rgba(0, 209, 255, 0.08) 100%) !important;
  color: var(--gray-800) !important;
  font-weight: var(--font-weight-bold) !important;
  font-size: var(--font-size-base) !important;
  border-bottom: 2px solid rgba(0, 56, 255, 0.1) !important;
  padding: var(--spacing-lg) var(--spacing-md) !important;
  text-align: center;
}

/* 表格行样式 */
:deep(.ant-table-tbody > tr) {
  background: rgba(255, 255, 255, 0.6) !important;
  transition: var(--transition-fast) !important;
}

:deep(.ant-table-tbody > tr:hover) {
  background: rgba(0, 56, 255, 0.05) !important;
  box-shadow: 0 2px 8px rgba(0, 56, 255, 0.1) !important;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
  border-bottom: 1px solid rgba(0, 56, 255, 0.05) !important;
  padding: var(--spacing-lg) var(--spacing-md) !important;
  color: var(--gray-700) !important;
  text-align: center;
}

/* 无封面样式 */
.no-cover {
  width: 80px;
  height: 60px;
  background: linear-gradient(135deg, rgba(0, 56, 255, 0.05) 0%, rgba(0, 209, 255, 0.05) 100%);
  border: 1px solid rgba(0, 56, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-400);
  font-size: var(--font-size-xs);
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-medium);
}

/* 提示词文本 */
.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--gray-600);
}

/* 灰色文字 */
.text-gray {
  color: var(--gray-400);
  font-style: italic;
}

/* Tag 标签优化 */
:deep(.ant-tag) {
  border-radius: var(--radius-full) !important;
  padding: var(--spacing-xs) var(--spacing-md) !important;
  font-weight: var(--font-weight-medium) !important;
  border: none !important;
}

:deep(.ant-tag-gold) {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%) !important;
  color: var(--white) !important;
  box-shadow: 0 2px 8px rgba(250, 173, 20, 0.3) !important;
}

/* 精选按钮优化 */
.featured-btn {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%) !important;
  border: none !important;
  color: white !important;
  box-shadow: var(--shadow-sm) !important;
}

.featured-btn:hover {
  background: linear-gradient(135deg, #d48806 0%, #faad14 100%) !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-md) !important;
}

/* 小按钮样式优化 */
:deep(.ant-btn-sm) {
  border-radius: var(--radius-full) !important;
  padding: 2px var(--spacing-md) !important;
  font-weight: var(--font-weight-medium) !important;
}

/* 编辑按钮 */
:deep(.ant-btn-primary.ant-btn-sm) {
  background: var(--button-gradient-primary) !important;
  border: none !important;
  box-shadow: var(--shadow-sm) !important;
}

:deep(.ant-btn-primary.ant-btn-sm:hover) {
  background: var(--button-gradient-secondary) !important;
  transform: translateY(-1px) !important;
  box-shadow: var(--shadow-md) !important;
}

/* 默认按钮 */
:deep(.ant-btn-default.ant-btn-sm) {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(0, 56, 255, 0.2) !important;
  color: var(--gray-700) !important;
}

:deep(.ant-btn-default.ant-btn-sm:hover) {
  background: rgba(0, 56, 255, 0.05) !important;
  border-color: var(--primary-color) !important;
  color: var(--primary-color) !important;
  transform: translateY(-1px) !important;
}

/* 删除按钮优化 */
:deep(.ant-btn-dangerous.ant-btn-sm) {
  background: linear-gradient(135deg, var(--error-color) 0%, var(--error-dark) 100%) !important;
  border: none !important;
  color: var(--white) !important;
  box-shadow: var(--shadow-sm) !important;
}

:deep(.ant-btn-dangerous.ant-btn-sm:hover) {
  background: linear-gradient(135deg, var(--error-dark) 0%, var(--error-color) 100%) !important;
  transform: translateY(-1px) !important;
  box-shadow: var(--shadow-md) !important;
}

/* 图片优化 */
:deep(.ant-image) {
  border-radius: var(--radius-lg) !important;
  overflow: hidden;
  box-shadow: var(--shadow-sm) !important;
  transition: var(--transition-fast) !important;
}

:deep(.ant-image:hover) {
  box-shadow: var(--shadow-md) !important;
  transform: scale(1.05) !important;
}

/* 气泡确认框优化 */
:deep(.ant-popconfirm) {
  z-index: 1060;
}

/* 分页组件优化 */
:deep(.ant-pagination) {
  margin-top: var(--spacing-xl) !important;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.ant-pagination-item) {
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(0, 56, 255, 0.2) !important;
  border-radius: var(--radius-lg) !important;
  transition: var(--transition-fast) !important;
}

:deep(.ant-pagination-item:hover) {
  background: rgba(0, 56, 255, 0.1) !important;
  border-color: var(--primary-color) !important;
  transform: translateY(-1px) !important;
}

:deep(.ant-pagination-item-active) {
  background: var(--button-gradient-primary) !important;
  border-color: transparent !important;
  color: var(--white) !important;
  font-weight: var(--font-weight-semibold) !important;
}

/* 修复分页组件的文字对齐 */
:deep(.ant-pagination-options-size-changer) {
  display: flex !important;
  align-items: center !important;
  height: 32px !important;
}

:deep(.ant-pagination-options-size-changer .ant-select) {
  height: 32px !important;
}

:deep(.ant-pagination-options-size-changer .ant-select-selector) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 32px !important;
  padding: 0 8px !important;
  background: rgba(255, 255, 255, 0.8) !important;
  border: 1px solid rgba(0, 56, 255, 0.2) !important;
  border-radius: var(--radius-lg) !important;
  transition: var(--transition-fast) !important;
}

:deep(.ant-pagination-options-size-changer .ant-select-selector:hover) {
  border-color: var(--primary-color) !important;
  box-shadow: 0 2px 8px rgba(0, 56, 255, 0.1) !important;
}

:deep(.ant-pagination-options-size-changer .ant-select-selection-item) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 100% !important;
  height: 30px !important;
  line-height: 30px !important;
  text-align: center !important;
  font-family: var(--font-family-primary) !important;
  font-size: var(--font-size-sm) !important;
  font-weight: var(--font-weight-medium) !important;
  color: var(--gray-700) !important;
}

/* 分页总数文字 */
:deep(.ant-pagination-total-text) {
  font-family: var(--font-family-primary) !important;
  font-weight: var(--font-weight-medium) !important;
  color: var(--gray-600) !important;
}

/* 响应式优化 */
@media (max-width: 768px) {
  #appManagePage {
    padding: var(--spacing-lg);
  }

  :deep(.ant-form),
  :deep(.ant-table-wrapper) {
    padding: var(--spacing-lg);
  }
}
</style>
