/**
 * HTTP 请求工具类
 * 基于 Axios 封装，提供统一的请求和响应处理
 */
import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建 Axios 实例，配置基础请求参数
const myAxios = axios.create({
  baseURL: 'http://localhost:8123/api', // API 基础地址
  timeout: 60000, // 请求超时时间
  withCredentials: true, // 携带 Cookie
})

// 全局请求拦截器 - 在请求发送前进行统一处理
myAxios.interceptors.request.use(
  function (config) {
    // 可在此添加请求头、token 等
    return config
  },
  function (error) {
    // 请求错误处理
    return Promise.reject(error)
  },
)

// 全局响应拦截器 - 统一处理响应结果和错误
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 处理未登录状态 (40100)
    if (data.code === 40100) {
      // 非登录相关请求且不在登录页面时，跳转到登录页
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 响应错误处理
    return Promise.reject(error)
  },
)

export default myAxios
