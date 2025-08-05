/**
 * 健康检查 API 控制器
 * 提供后端服务健康状态检查接口
 */
// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 
 * 健康检查接口
 * 用于检测后端服务是否正常运行
 */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}
