<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <div class="search-container">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="账号" class="search-item">
          <a-input
            v-model:value="searchParams.userAccount"
            placeholder="输入账号"
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户名" class="search-item">
          <a-input
            v-model:value="searchParams.userName"
            placeholder="输入用户名"
            class="search-input"
            allow-clear
          />
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

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

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
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

/* Tag 标签优化 */
:deep(.ant-tag) {
  border-radius: var(--radius-full) !important;
  padding: var(--spacing-xs) var(--spacing-md) !important;
  font-weight: var(--font-weight-medium) !important;
  border: none !important;
}

:deep(.ant-tag-green) {
  background: linear-gradient(135deg, var(--success-color) 0%, var(--success-dark) 100%) !important;
  color: var(--white) !important;
}

:deep(.ant-tag-blue) {
  background: var(--button-gradient-primary) !important;
  color: var(--white) !important;
}

/* 删除按钮优化 */
:deep(.ant-btn-dangerous) {
  background: linear-gradient(135deg, var(--error-color) 0%, var(--error-dark) 100%) !important;
  border: none !important;
  border-radius: var(--radius-full) !important;
  color: var(--white) !important;
  font-weight: var(--font-weight-medium) !important;
  padding: var(--spacing-sm) var(--spacing-xl) !important;
  box-shadow: var(--shadow-sm) !important;
  transition: var(--transition-normal) !important;
}

:deep(.ant-btn-dangerous:hover) {
  background: linear-gradient(135deg, var(--error-dark) 0%, var(--error-color) 100%) !important;
  transform: translateY(-2px) !important;
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
  #userManagePage {
    padding: var(--spacing-lg);
  }

  :deep(.ant-form),
  :deep(.ant-table-wrapper) {
    padding: var(--spacing-lg);
  }
}
</style>
