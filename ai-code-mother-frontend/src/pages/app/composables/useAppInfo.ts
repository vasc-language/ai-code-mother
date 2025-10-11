import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById } from '@/api/appController'

// 导入模型SVG图标
import deepseekIcon from '@/assets/deepseek-color.svg'
import qwenIcon from '@/assets/qwen-color.svg'
import openaiIcon from '@/assets/openai.svg'
import kimiIcon from '@/assets/kimi-color.svg'
import aiAvatar from '@/assets/aiAvatar.png'

/**
 * 应用信息管理Composable
 * 负责应用数据的获取、权限判断和AI头像显示
 */
export function useAppInfo() {
  const route = useRoute()
  const router = useRouter()
  const loginUserStore = useLoginUserStore()

  // 应用信息
  const appInfo = ref<API.AppVO>()
  const appId = ref<any>()

  // 权限判断
  const isOwner = computed(() => {
    return appInfo.value?.userId === loginUserStore.loginUser.id
  })

  const isAdmin = computed(() => {
    return loginUserStore.loginUser.userRole === 'admin'
  })

  // 根据应用的modelKey动态获取AI头像
  const currentAiAvatar = computed(() => {
    const modelKey = appInfo.value?.modelKey?.toLowerCase() || ''

    // 根据modelKey匹配对应的图标
    if (modelKey.includes('deepseek')) {
      return deepseekIcon
    } else if (modelKey.includes('qwen')) {
      return qwenIcon
    } else if (modelKey.includes('gpt') || modelKey.includes('o3') || modelKey.includes('o4')) {
      return openaiIcon
    } else if (modelKey.includes('kimi')) {
      return kimiIcon
    }

    // 默认返回通用AI头像
    return aiAvatar
  })

  // 获取应用信息
  const fetchAppInfo = async () => {
    const id = route.params.id as string
    if (!id) {
      message.error('应用ID不存在')
      router.push('/')
      return null
    }

    appId.value = id

    try {
      const res = await getAppVoById({ id: id as unknown as number })
      if (res.data.code === 0 && res.data.data) {
        appInfo.value = res.data.data
        return appInfo.value
      } else {
        message.error('获取应用信息失败')
        router.push('/')
        return null
      }
    } catch (error) {
      console.error('获取应用信息失败:', error)
      message.error('获取应用信息失败')
      router.push('/')
      return null
    }
  }

  return {
    appInfo,
    appId,
    isOwner,
    isAdmin,
    currentAiAvatar,
    fetchAppInfo,
  }
}
