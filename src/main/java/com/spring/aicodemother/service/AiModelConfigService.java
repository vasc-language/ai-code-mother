package com.spring.aicodemother.service;

import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.entity.AiModelConfig;

import java.util.List;

/**
 * AI模型配置 服务层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public interface AiModelConfigService extends IService<AiModelConfig> {

    /**
     * 根据模型key获取配置
     *
     * @param modelKey 模型key
     * @return 模型配置
     */
    AiModelConfig getByModelKey(String modelKey);

    /**
     * 获取所有启用的模型配置（按排序顺序）
     *
     * @return 启用的模型配置列表
     */
    List<AiModelConfig> listEnabledModels();

    /**
     * 根据等级获取模型配置列表
     *
     * @param tier 模型等级
     * @return 模型配置列表
     */
    List<AiModelConfig> listByTier(String tier);

    /**
     * 计算消耗的积分
     *
     * @param modelKey 模型key
     * @param tokenCount token数量
     * @return 消耗的积分
     */
    Integer calculatePoints(String modelKey, Integer tokenCount);

}
