package com.spring.aicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.mapper.AiModelConfigMapper;
import com.spring.aicodemother.model.entity.AiModelConfig;
import com.spring.aicodemother.service.AiModelConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.spring.aicodemother.model.entity.table.AiModelConfigTableDef.AI_MODEL_CONFIG;

/**
 * AI模型配置 服务实现层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Service
public class AiModelConfigServiceImpl extends ServiceImpl<AiModelConfigMapper, AiModelConfig> implements AiModelConfigService {

    @Override
    public AiModelConfig getByModelKey(String modelKey) {
        return this.getOne(
            QueryWrapper.create()
                .where(AI_MODEL_CONFIG.MODEL_KEY.eq(modelKey))
                .and(AI_MODEL_CONFIG.IS_ENABLED.eq(1))
        );
    }

    @Override
    public List<AiModelConfig> listEnabledModels() {
        return this.list(
            QueryWrapper.create()
                .where(AI_MODEL_CONFIG.IS_ENABLED.eq(1))
                .orderBy(AI_MODEL_CONFIG.SORT_ORDER.asc())
                .orderBy(AI_MODEL_CONFIG.TIER.asc())
        );
    }

    @Override
    public List<AiModelConfig> listByTier(String tier) {
        return this.list(
            QueryWrapper.create()
                .where(AI_MODEL_CONFIG.TIER.eq(tier))
                .and(AI_MODEL_CONFIG.IS_ENABLED.eq(1))
                .orderBy(AI_MODEL_CONFIG.SORT_ORDER.asc())
        );
    }

    @Override
    public Integer calculatePoints(String modelKey, Integer tokenCount) {
        AiModelConfig config = getByModelKey(modelKey);
        if (config == null) {
            throw new IllegalArgumentException("模型配置不存在: " + modelKey);
        }

        // 计算积分: (token数量 / 1000) * 每1K token的积分
        // 向上取整，确保即使不足1K token也会扣除积分
        int kTokens = (int) Math.ceil(tokenCount / 1000.0);
        return kTokens * config.getPointsPerKToken();
    }

}
