import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  listVersionsByAppId,
  rollbackToVersion as rollbackToVersionApi,
} from '@/api/appVersionController'

/**
 * 应用版本管理Composable
 * 负责版本列表查看、版本回滚等功能
 */
export function useVersionManagement() {
  // 版本列表状态
  const versionModalVisible = ref(false)
  const versionDetailModalVisible = ref(false)
  const loadingVersions = ref(false)
  const versionList = ref<API.AppVersionVO[]>([])
  const selectedVersion = ref<API.AppVersionVO | null>(null)
  const currentVersionNum = ref<number>(0)

  /**
   * 显示版本历史
   */
  const showVersionHistory = async (appId: any) => {
    versionModalVisible.value = true
    await fetchVersionList(appId)
  }

  /**
   * 获取版本列表
   */
  const fetchVersionList = async (appId: any) => {
    if (!appId) {
      message.error('应用ID不存在')
      return
    }

    loadingVersions.value = true
    try {
      const res = await listVersionsByAppId({
        appId: appId as unknown as number,
      })

      if (res.data.code === 0 && res.data.data) {
        versionList.value = res.data.data
        // 当前版本号为列表中最大的版本号
        if (versionList.value.length > 0) {
          currentVersionNum.value = Math.max(...versionList.value.map((v) => v.versionNum || 0))
        }
      } else {
        message.error('获取版本列表失败:' + res.data.message)
      }
    } catch (error) {
      console.error('获取版本列表失败:', error)
      message.error('获取版本列表失败,请重试')
    } finally {
      loadingVersions.value = false
    }
  }

  /**
   * 关闭版本历史弹窗
   */
  const closeVersionModal = () => {
    versionModalVisible.value = false
  }

  /**
   * 查看版本详情
   */
  const viewVersionDetail = (version: API.AppVersionVO) => {
    selectedVersion.value = version
    versionDetailModalVisible.value = true
  }

  /**
   * 处理版本回滚
   */
  const handleRollback = async (appId: any, version: API.AppVersionVO) => {
    if (!appId || !version.id) {
      message.error('参数错误')
      return
    }

    try {
      const res = await rollbackToVersionApi({
        appId: appId as unknown as number,
        versionId: version.id as unknown as number,
      })

      if (res.data.code === 0) {
        message.success('版本回滚成功')
        // 关闭模态框
        versionModalVisible.value = false
        // 刷新页面以显示回滚后的代码
        window.location.reload()
      } else {
        message.error('版本回滚失败:' + res.data.message)
      }
    } catch (error) {
      console.error('版本回滚失败:', error)
      message.error('版本回滚失败,请重试')
    }
  }

  /**
   * 格式化日期时间
   */
  const formatDateTime = (dateTime: string | undefined) => {
    if (!dateTime) return '-'
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  }

  return {
    versionModalVisible,
    versionDetailModalVisible,
    loadingVersions,
    versionList,
    selectedVersion,
    currentVersionNum,
    showVersionHistory,
    fetchVersionList,
    closeVersionModal,
    viewVersionDetail,
    handleRollback,
    formatDateTime,
  }
}
