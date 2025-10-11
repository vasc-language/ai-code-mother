import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { deployApp as deployAppApi } from '@/api/appController'
import request from '@/request'

/**
 * 应用部署管理Composable
 * 负责代码下载和应用部署功能
 */
export function useAppDeployment() {
  // 下载状态
  const downloading = ref(false)

  // 部署状态
  const deploying = ref(false)
  const deployModalVisible = ref(false)
  const deployUrl = ref('')

  /**
   * 下载代码
   */
  const downloadCode = async (appId: any) => {
    if (!appId) {
      message.error('应用ID不存在')
      return
    }

    downloading.value = true
    try {
      const API_BASE_URL = request.defaults.baseURL || ''
      const url = `${API_BASE_URL}/app/download/${appId}`
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
      })

      if (!response.ok) {
        throw new Error(`下载失败: ${response.status}`)
      }

      // 获取文件名
      const contentDisposition = response.headers.get('Content-Disposition')
      const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId}.zip`

      // 下载文件
      const blob = await response.blob()
      const downloadUrl = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = fileName
      link.click()

      // 清理
      URL.revokeObjectURL(downloadUrl)
      message.success('代码下载成功')
    } catch (error) {
      console.error('下载失败:', error)
      message.error('下载失败,请重试')
    } finally {
      downloading.value = false
    }
  }

  /**
   * 部署应用
   */
  const deployApp = async (appId: any, onSuccess?: () => void) => {
    if (!appId) {
      message.error('应用ID不存在')
      return
    }

    deploying.value = true
    try {
      const res = await deployAppApi({
        appId: appId as unknown as number,
      })

      if (res.data.code === 0 && res.data.data) {
        deployUrl.value = res.data.data
        deployModalVisible.value = true
        message.success('部署成功')
        onSuccess?.()
      } else {
        message.error('部署失败:' + res.data.message)
      }
    } catch (error) {
      console.error('部署失败:', error)
      message.error('部署失败,请重试')
    } finally {
      deploying.value = false
    }
  }

  return {
    downloading,
    deploying,
    deployModalVisible,
    deployUrl,
    downloadCode,
    deployApp,
  }
}
