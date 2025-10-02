<template>
  <a-card class="user-info-card" :bordered="false">
    <div class="user-info-content">
      <!-- 左侧：头像 -->
      <div class="avatar-section">
        <a-avatar :size="120" :src="user.userAvatar">
          {{ user.userName?.charAt(0) }}
        </a-avatar>
      </div>

      <!-- 右侧：用户信息 -->
      <div class="info-section">
        <template v-if="!isEditing">
          <!-- 查看模式 -->
          <div class="info-header">
            <div class="name-wrapper">
              <h2 class="user-name">{{ user.userName || '未设置昵称' }}</h2>
              <a-tag v-if="user.userRole === 'admin'" color="blue">管理员</a-tag>
              <a-tag v-else color="default">普通用户</a-tag>
            </div>
            <a-button
              v-if="editable"
              type="primary"
              @click="startEdit"
            >
              <template #icon><EditOutlined /></template>
              编辑资料
            </a-button>
          </div>

          <div class="info-details">
            <p class="user-account">邮箱：{{ user.userEmail }}</p>
            <p class="user-id">ID：{{ user.id }}</p>
            <p class="user-profile">
              {{ user.userProfile || '这个人很懒，还没有填写个人简介' }}
            </p>
            <p class="join-time">
              加入于 {{ formatJoinTime(user.createTime) }}
            </p>
          </div>
        </template>

        <template v-else>
          <!-- 编辑模式 -->
          <a-form
            :model="editForm"
            :rules="rules"
            layout="vertical"
            @finish="handleSave"
          >
            <a-form-item label="用户昵称" name="userName">
              <a-input
                v-model:value="editForm.userName"
                placeholder="请输入昵称（2-20字符）"
                :maxlength="20"
                show-count
              />
            </a-form-item>

            <a-form-item label="头像URL" name="userAvatar">
              <a-input
                v-model:value="editForm.userAvatar"
                placeholder="请输入头像图片URL"
              />
            </a-form-item>

            <a-form-item label="个人简介" name="userProfile">
              <a-textarea
                v-model:value="editForm.userProfile"
                placeholder="请输入个人简介（最多200字符）"
                :rows="4"
                :maxlength="200"
                show-count
              />
            </a-form-item>

            <a-form-item>
              <a-space>
                <a-button type="primary" html-type="submit" :loading="saving">
                  保存修改
                </a-button>
                <a-button @click="cancelEdit">取消</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </template>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { EditOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import request from '@/request'

interface Props {
  user: any
  editable: boolean
}

interface Emits {
  (e: 'edit-success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const loginUserStore = useLoginUserStore()

const isEditing = ref(false)
const saving = ref(false)

const editForm = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

// 表单验证规则
const rules = {
  userName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度必须在2-20字符之间', trigger: 'blur' },
  ],
  userAvatar: [
    {
      type: 'url',
      message: '请输入有效的URL',
      trigger: 'blur',
    },
  ],
  userProfile: [
    { max: 200, message: '个人简介不能超过200字符', trigger: 'blur' },
  ],
}

// 开始编辑
const startEdit = () => {
  editForm.userName = props.user.userName || ''
  editForm.userAvatar = props.user.userAvatar || ''
  editForm.userProfile = props.user.userProfile || ''
  isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
}

// 保存修改
const handleSave = async () => {
  saving.value = true
  try {
    const res = await request.post('/user/profile/update', editForm)
    if (res.data.code === 0) {
      message.success('更新成功')
      isEditing.value = false

      // 刷新全局登录用户信息（更新右上角显示）
      await loginUserStore.fetchLoginUser()

      // 通知父组件刷新页面数据
      emit('edit-success')
    } else {
      message.error(res.data.message || '更新失败')
    }
  } catch (error: any) {
    console.error('更新用户信息失败:', error)
    message.error(error.message || '更新失败')
  } finally {
    saving.value = false
  }
}

// 格式化加入时间
const formatJoinTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
}
</script>

<style scoped>
.user-info-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-info-content {
  display: flex;
  gap: 32px;
}

.avatar-section {
  flex-shrink: 0;
}

.info-section {
  flex: 1;
  min-width: 0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.name-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #262626;
}

.info-details {
  color: #595959;
}

.info-details p {
  margin: 8px 0;
  line-height: 1.6;
}

.user-account,
.user-id {
  font-size: 14px;
  color: #8c8c8c;
}

.user-profile {
  margin: 16px 0 !important;
  font-size: 15px;
  color: #595959;
  line-height: 1.8;
}

.join-time {
  font-size: 13px;
  color: #8c8c8c;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .user-info-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .avatar-section {
    margin-bottom: 16px;
  }

  .info-header {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .name-wrapper {
    flex-direction: column;
  }
}
</style>
