// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 获取当前积分 查询用户当前可用积分 GET /points/current */
export async function getCurrentPoints(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringInteger>('/points/current', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取积分概览 查询用户的积分统计信息 GET /points/overview */
export async function getPointsOverview(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringObject>('/points/overview', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取积分明细 查询用户的积分变动记录 GET /points/records */
export async function getPointsRecords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPointsRecordsParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListPointsRecord>('/points/records', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
