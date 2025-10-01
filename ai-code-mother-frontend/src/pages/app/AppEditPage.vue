<template>
  <div id="appEditPage">
    <div class="page-header">
      <h1>编辑应用信息</h1>
    </div>

    <div class="edit-container">
      <a-card title="基本信息" :loading="loading">
        <a-form
          :model="formData"
          :rules="rules"
          layout="vertical"
          @finish="handleSubmit"
          ref="formRef"
        >
          <a-form-item label="应用名称" name="appName">
            <a-input
              v-model:value="formData.appName"
              placeholder="请输入应用名称"
              :maxlength="50"
              show-count
            />
          </a-form-item>

          <a-form-item
            v-if="isAdmin"
            label="应用封面"
            name="cover"
            extra="支持图片链接，建议尺寸：400x300"
          >
            <a-input v-model:value="formData.cover" placeholder="请输入封面图片链接" />
            <div v-if="formData.cover" class="cover-preview">
              <a-image
                :src="formData.cover"
                :width="200"
                :height="150"
                fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
              />
            </div>
          </a-form-item>

          <a-form-item v-if="isAdmin" label="优先级" name="priority" extra="设置为99表示精选应用">
            <a-input-number
              v-model:value="formData.priority"
              :min="0"
              :max="99"
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item label="初始提示词" name="initPrompt">
            <a-textarea
              v-model:value="formData.initPrompt"
              placeholder="请输入初始提示词"
              :rows="4"
              :maxlength="1000"
              show-count
              disabled
            />
            <div class="form-tip">初始提示词不可修改</div>
          </a-form-item>

          <a-form-item label="生成类型" name="codeGenType">
            <a-input
              :value="formatCodeGenType(formData.codeGenType)"
              placeholder="生成类型"
              disabled
            />
            <div class="form-tip">生成类型不可修改</div>
          </a-form-item>

          <a-form-item v-if="formData.deployKey" label="部署密钥" name="deployKey">
            <a-input v-model:value="formData.deployKey" placeholder="部署密钥" disabled />
            <div class="form-tip">部署密钥不可修改</div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="submitting">
                保存修改
              </a-button>
              <a-button @click="resetForm">重置</a-button>
              <a-button type="link" @click="goToChat">进入对话</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 应用信息展示 -->
      <a-card title="应用信息" style="margin-top: 24px">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="应用ID">
            {{ appInfo?.id }}
          </a-descriptions-item>
          <a-descriptions-item label="创建者">
            <UserInfo :user="appInfo?.user" size="small" />
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatTime(appInfo?.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatTime(appInfo?.updateTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="部署时间">
            {{ appInfo?.deployedTime ? formatTime(appInfo.deployedTime) : '未部署' }}
          </a-descriptions-item>
          <a-descriptions-item label="访问链接">
            <a-button v-if="appInfo?.deployKey" type="link" @click="openPreview" size="small">
              查看预览
            </a-button>
            <span v-else>未部署</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 历史版本 -->
      <a-card title="历史版本" style="margin-top: 24px">
        <a-spin :spinning="loadingVersions">
          <a-timeline v-if="versionList.length > 0" mode="left">
            <a-timeline-item
              v-for="version in versionList"
              :key="version.id"
              :color="version.versionNum === currentVersionNum ? 'green' : 'blue'"
            >
              <template #dot>
                <span class="version-dot">{{ version.versionTag }}</span>
              </template>
              <div class="version-item">
                <div class="version-header">
                  <h4>
                    {{ version.versionTag }}
                    <a-tag v-if="version.versionNum === currentVersionNum" color="success">
                      当前版本
                    </a-tag>
                  </h4>
                </div>
                <div class="version-info">
                  <p class="version-info-item">
                    <ClockCircleOutlined class="version-icon" />
                    <span><strong>部署时间：</strong>{{ formatDateTime(version.deployedTime) }}</span>
                  </p>
                  <p v-if="version.user" class="version-info-item">
                    <UserOutlined class="version-icon" />
                    <span><strong>部署用户：</strong>{{ version.user.userName }}</span>
                  </p>
                  <p v-if="version.deployUrl" class="version-info-item">
                    <LinkOutlined class="version-icon" />
                    <span>
                      <strong>部署地址：</strong>
                      <a :href="version.deployUrl" target="_blank">{{ version.deployUrl }}</a>
                    </span>
                  </p>
                  <div class="version-actions">
                    <a-button type="link" size="small" @click="viewVersionDetail(version)">
                      查看详情
                    </a-button>
                    <a-popconfirm
                      v-if="version.versionNum !== currentVersionNum"
                      title="确定要回滚到此版本吗？回滚后将恢复该版本的所有代码文件。"
                      ok-text="确定"
                      cancel-text="取消"
                      @confirm="handleRollback(version)"
                    >
                      <a-button type="link" size="small" danger>回滚</a-button>
                    </a-popconfirm>
                  </div>
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无版本历史" />
        </a-spin>
      </a-card>
    </div>

    <!-- 版本详情弹窗 -->
    <a-modal
      v-model:open="versionDetailModalVisible"
      :title="`版本详情 - ${selectedVersion?.versionTag}`"
      :width="900"
      :footer="null"
    >
      <div v-if="selectedVersion" class="version-detail">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="版本号">
            {{ selectedVersion.versionTag }}
          </a-descriptions-item>
          <a-descriptions-item label="部署时间">
            {{ formatDateTime(selectedVersion.deployedTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="部署用户" v-if="selectedVersion.user">
            {{ selectedVersion.user.userName }}
          </a-descriptions-item>
          <a-descriptions-item label="部署地址" v-if="selectedVersion.deployUrl">
            <a :href="selectedVersion.deployUrl" target="_blank">{{ selectedVersion.deployUrl }}</a>
          </a-descriptions-item>
          <a-descriptions-item label="部署标识" v-if="selectedVersion.deployKey">
            {{ selectedVersion.deployKey }}
          </a-descriptions-item>
          <a-descriptions-item label="备注" v-if="selectedVersion.remark">
            {{ selectedVersion.remark }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import {
  listVersionsByAppId,
  rollbackToVersion as rollbackToVersionApi
} from '@/api/appVersionController'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import { getStaticPreviewUrl } from '@/config/env'
import type { FormInstance } from 'ant-design-vue'
import {
  ClockCircleOutlined,
  UserOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 版本管理
const versionList = ref<API.AppVersionVO[]>([])
const loadingVersions = ref(false)
const currentVersionNum = ref(0)
const versionDetailModalVisible = ref(false)
const selectedVersion = ref<API.AppVersionVO | null>(null)

// 表单数据
const formData = reactive({
  appName: '',
  cover: '',
  priority: 0,
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
})

// 是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在1-50个字符', trigger: 'blur' },
  ],
  cover: [{ type: 'url', message: '请输入有效的URL', trigger: 'blur' }],
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级范围0-99', trigger: 'blur' }],
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  
  // 添加重试机制
  const maxRetries = 3
  let retryCount = 0
  let loadSuccess = false

  while (retryCount < maxRetries && !loadSuccess) {
    try {
      await new Promise(resolve => setTimeout(resolve, retryCount * 300)) // 逐渐增加延迟
      
      const res = await getAppVoById({ id: id })
      if (res.data.code === 0 && res.data.data) {
        appInfo.value = res.data.data
        loadSuccess = true

        // 检查权限
        if (!isAdmin.value && appInfo.value.userId !== loginUserStore.loginUser.id) {
          message.error('您没有权限编辑此应用')
          router.push('/')
          return
        }

        // 填充表单数据
        formData.appName = appInfo.value.appName || ''
        formData.cover = appInfo.value.cover || ''
        formData.priority = appInfo.value.priority || 0
        formData.initPrompt = appInfo.value.initPrompt || ''
        formData.codeGenType = appInfo.value.codeGenType || ''
        formData.deployKey = appInfo.value.deployKey || ''
        break
      } else {
        throw new Error('应用不存在或已被删除')
      }
    } catch (error) {
      retryCount++
      console.warn(`第 ${retryCount} 次获取应用信息失败:`, error)
      
      if (retryCount >= maxRetries) {
        console.error('获取应用信息失败，已达最大重试次数')
        message.error('应用不存在或暂时无法访问，请稍后重试')
        router.push('/')
        break
      } else if (retryCount === 1) {
        message.loading('正在加载应用信息...', 1.5)
      }
    }
  }
  
  loading.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!appInfo.value?.id) return

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以修改更多字段
      res = await updateAppByAdmin({
        id: appInfo.value.id,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp({
        id: appInfo.value.id,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('修改成功')
      // 重新获取应用信息
      await fetchAppInfo()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改失败：', error)
    message.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (appInfo.value) {
    formData.appName = appInfo.value.appName || ''
    formData.cover = appInfo.value.cover || ''
    formData.priority = appInfo.value.priority || 0
  }
  formRef.value?.clearValidate()
}

// 进入对话页面
const goToChat = () => {
  if (appInfo.value?.id) {
    router.push(`/app/chat/${appInfo.value.id}`)
  }
}

// 打开预览
const openPreview = () => {
  if (appInfo.value?.codeGenType && appInfo.value?.id) {
    const url = getStaticPreviewUrl(appInfo.value.codeGenType, String(appInfo.value.id))
    window.open(url, '_blank')
  }
}

// 获取版本列表
const fetchVersionList = async () => {
  if (!appInfo.value?.id) return

  loadingVersions.value = true
  try {
    const res = await listVersionsByAppId({ appId: appInfo.value.id })
    if (res.data.code === 0 && res.data.data) {
      versionList.value = res.data.data || []
      // 获取当前版本号
      if (appInfo.value?.versionNum) {
        currentVersionNum.value = appInfo.value.versionNum
      }
    }
  } catch (error) {
    console.error('获取版本列表失败:', error)
  } finally {
    loadingVersions.value = false
  }
}

// 查看版本详情
const viewVersionDetail = (version: API.AppVersionVO) => {
  selectedVersion.value = version
  versionDetailModalVisible.value = true
}

// 回滚版本
const handleRollback = async (version: API.AppVersionVO) => {
  if (!appInfo.value?.id || !version.id) return

  try {
    const res = await rollbackToVersionApi({
      appId: appInfo.value.id,
      versionId: version.id,
    })

    if (res.data.code === 0) {
      message.success('回滚成功')
      // 刷新应用信息和版本列表
      await fetchAppInfo()
      await fetchVersionList()
    } else {
      message.error(res.data.message || '回滚失败')
    }
  } catch (error) {
    console.error('回滚失败:', error)
    message.error('回滚失败')
  }
}

// 格式化时间显示
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 页面加载时获取应用信息
onMounted(async () => {
  await fetchAppInfo()
  await fetchVersionList()
})
</script>

<style scoped>
#appEditPage {
  min-height: calc(100vh - 200px);
  padding: 24px;
  background: #f0f2f5;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 24px;
  padding: 0 4px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #262626;
}

.edit-container {
  max-width: 1200px;
  margin: 0 auto;
}

.edit-container > .ant-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02),
    0 2px 4px 0 rgba(0, 0, 0, 0.02);
}

.cover-preview {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: #fafafa;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

:deep(.ant-card) {
  transition: box-shadow 0.3s ease;
}

:deep(.ant-card:hover) {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08), 0 2px 6px -1px rgba(0, 0, 0, 0.05),
    0 4px 8px 0 rgba(0, 0, 0, 0.05);
}

:deep(.ant-card-head) {
  background: linear-gradient(to bottom, #fafafa, #ffffff);
  border-bottom: 2px solid #e8e8e8;
  font-weight: 600;
  font-size: 16px;
}

:deep(.ant-descriptions-item-label) {
  background: #fafafa;
  font-weight: 500;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #262626;
}

/* 版本管理样式 */
.version-item {
  padding: 8px 0;
}

.version-header {
  margin-bottom: 12px;
}

.version-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.version-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.version-info-item {
  margin: 0;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-icon {
  font-size: 16px;
  color: #1890ff;
  flex-shrink: 0;
}

.version-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-left: 24px;
}

.version-dot {
  font-size: 12px;
  font-weight: bold;
  color: #1890ff;
}

.version-detail {
  padding: 16px 0;
}
</style>
