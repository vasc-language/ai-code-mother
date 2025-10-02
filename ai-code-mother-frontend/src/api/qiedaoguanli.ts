// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 每日签到 用户每日签到，获取积分奖励 POST /signin/daily */
export async function dailySignIn(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringObject>('/signin/daily', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 获取签到状态 查询用户今日签到状态和连续签到天数 GET /signin/status */
export async function getSignInStatus(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringObject>('/signin/status', {
    method: 'GET',
    ...(options || {}),
  })
}
