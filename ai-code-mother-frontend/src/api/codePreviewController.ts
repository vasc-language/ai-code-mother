// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /code/download/${param0} */
export async function downloadCodeFiles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.downloadCodeFilesParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<any>(`/code/download/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /code/file/${param0} */
export async function getFileContent(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getFileContentParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseMapStringObject>(`/code/file/${param0}`, {
    method: 'GET',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /code/list/${param0} */
export async function listCodeFiles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listCodeFilesParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponseMapStringObject>(`/code/list/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
