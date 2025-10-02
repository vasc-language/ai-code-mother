// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 获取邀请码 获取当前用户的邀请码 GET /invite/code */
export async function getInviteCode(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringString>('/invite/code', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取邀请记录 查询当前用户的邀请记录列表 GET /invite/records */
export async function getInviteRecords(options?: { [key: string]: any }) {
  return request<API.BaseResponseListInviteRecord>('/invite/records', {
    method: 'GET',
    ...(options || {}),
  })
}
