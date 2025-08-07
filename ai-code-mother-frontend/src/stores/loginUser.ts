import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getUserLogin } from '@/api/userController.ts'

/**
 * 登录用户状态管理 Store
 * 使用 Pinia 管理用户登录状态和用户信息
 */
export const useLoginUserStore  = defineStore('loginUser', () => {
  /**
   * 当前登录用户信息
   * 默认值为未登录状态
   */
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  /**
   * 从后端获取当前登录用户信息
   * 调用后端 API 获取用户数据并更新本地状态
   * @returns Promise<void>
   */
  async function fetchLoginUser() {
    const res = await getUserLogin()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  /**
   * 更新本地登录用户信息
   * 直接设置用户信息到本地状态，不调用后端 API
   * @param newLoginUser - 新的用户信息对象
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
