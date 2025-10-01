// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /app/version/get/${param0} */
export async function getVersionVo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getVersionVOParams,
  options?: { [key: string]: any }
) {
  const { versionId: param0, ...queryParams } = params
  return request<API.BaseResponseAppVersionVO>(`/app/version/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/version/list/${param0} */
export async function listVersionsByAppId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listVersionsByAppIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseListAppVersionVO>(`/app/version/list/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/version/list/page */
export async function listVersionsByPage(
  body: API.AppVersionQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVersionVO>('/app/version/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/version/rollback */
export async function rollbackToVersion(
  body: API.AppVersionRollbackRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/version/rollback', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
