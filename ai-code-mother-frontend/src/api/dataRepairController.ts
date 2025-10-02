// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /admin/data-repair/grant-admin-points */
export async function grantPointsForAdmins(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.grantPointsForAdminsParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseMapStringObject>('/admin/data-repair/grant-admin-points', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /admin/data-repair/init-points */
export async function initializePointsForAllUsers(options?: { [key: string]: any }) {
  return request<API.BaseResponseMapStringObject>('/admin/data-repair/init-points', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /admin/data-repair/init-points/${param0} */
export async function initializePointsForUser(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.initializePointsForUserParams,
  options?: { [key: string]: any }
) {
  const { userId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/admin/data-repair/init-points/${param0}`, {
    method: 'POST',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}
