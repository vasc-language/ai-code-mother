<!--
  用户管理页面组件
  提供管理员查看、搜索和删除用户的功能
  包含用户列表展示、分页、搜索筛选等完整的用户管理功能
-->
<template>
  <div id="userManagePage">
    <!-- 搜索表单区域 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 用户数据表格区域 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <!-- 自定义表格单元格渲染 -->
      <template #bodyCell="{ column, record }">
        <!-- 头像列：显示用户头像图片 -->
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <!-- 用户角色列：根据角色显示不同颜色的标签 -->
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <!-- 创建时间列：格式化显示创建时间 -->
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!-- 操作列：提供删除用户功能 -->
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

/**
 * 表格列配置
 * 定义用户管理表格的列结构和显示内容
 */
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

/**
 * 表格展示的用户数据列表
 * 存储从后端获取的用户信息数组
 */
const data = ref<API.UserVO[]>([])

/**
 * 数据总条数
 * 用于分页组件显示总数量信息
 */
const total = ref(0)

/**
 * 搜索和分页参数
 * 包含搜索条件和分页信息，用于向后端请求数据
 */
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,      // 当前页码，从1开始
  pageSize: 10,    // 每页显示条数
  // userAccount: '',  // 用户账号搜索条件（可选）
  // userName: '',     // 用户名搜索条件（可选）
})

/**
 * 获取用户数据
 * 调用后端API获取用户列表，支持分页和搜索
 */
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    // 更新表格数据
    data.value = res.data.data.records ?? []
    // 更新总条数（注意：这里使用totalRow，可能需要根据后端实际返回字段调整）
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

/**
 * 分页配置
 * 计算属性，返回Ant Design表格分页组件所需的配置
 */
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,        // 当前页码
    pageSize: searchParams.pageSize ?? 10,     // 每页条数
    total: total.value,                        // 数据总条数
    showSizeChanger: true,                     // 显示页面大小选择器
    showTotal: (total: number) => `共 ${total} 条`,  // 显示总条数信息
  }
})

/**
 * 处理表格分页变化
 * 当用户切换页码或改变每页显示条数时触发
 * @param page - 分页参数对象，包含current和pageSize
 */
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current     // 更新当前页码
  searchParams.pageSize = page.pageSize   // 更新每页显示条数
  fetchData()                             // 重新请求数据
}

/**
 * 执行搜索操作
 * 当用户点击搜索按钮时触发，重置页码并请求数据
 */
const doSearch = () => {
  // 重置页码到第一页（搜索时应该从第一页开始显示结果）
  searchParams.pageNum = 1
  fetchData()
}

/**
 * 删除用户操作
 * 根据用户ID删除指定用户
 * @param id - 要删除的用户ID
 */
const doDelete = async (id: number) => {
  // 参数校验：检查ID是否为空
  if (!id) {
    return
  }
  
  // 调用删除API
  const res = await deleteUser({ id })
  
  if (res.data.code === 0) {
    // 删除成功
    message.success('删除成功')
    // 刷新数据列表以反映删除后的结果
    fetchData()
  } else {
    // 删除失败，显示错误信息
    message.error('删除失败，' + res.data.message)
  }
}

/**
 * 组件挂载时的初始化操作
 * 页面加载完成后自动请求一次数据
 */
onMounted(() => {
  fetchData()
})
</script>

<style>
#userManagePage {
}
</style>
