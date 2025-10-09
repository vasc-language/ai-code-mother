// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 根据模型key获取配置 获取指定模型的详细配置信息 GET /ai-model/get/${param0} */
export async function getModelByKey(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getModelByKeyParams,
  options?: { [key: string]: any }
) {
  const { modelKey: param0, ...queryParams } = params
  return request<API.BaseResponseAiModelConfig>(`/ai-model/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取所有启用的AI模型列表 返回按等级和排序顺序排列的所有可用模型 GET /ai-model/list */
export async function listEnabledModels(options?: { [key: string]: any }) {
  return request<API.BaseResponseListAiModelConfig>('/ai-model/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 根据等级获取AI模型 获取指定等级的所有可用模型 GET /ai-model/list/tier/${param0} */
export async function listModelsByTier(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listModelsByTierParams,
  options?: { [key: string]: any }
) {
  const { tier: param0, ...queryParams } = params
  return request<API.BaseResponseListAiModelConfig>(`/ai-model/list/tier/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
