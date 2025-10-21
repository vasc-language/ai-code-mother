package com.spring.aicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.mapper.AiModelConfigMapper;
import com.spring.aicodemother.model.entity.AiModelConfig;
import com.spring.aicodemother.service.AiModelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.spring.aicodemother.model.entity.table.AiModelConfigTableDef.AI_MODEL_CONFIG;

/**
 * AI模型配置 服务实现层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
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

        // 计算基础积分: (token数量 / 1000) * 每1K token的积分
        // 向上取整，确保即使不足1K token也会扣除积分
        int kTokens = (int) Math.ceil(tokenCount / 1000.0);
        int basePoints = kTokens * config.getPointsPerKToken();

        // 应用质量系数（如果存在）
        BigDecimal qualityScore = config.getQualityScore();
        if (qualityScore != null && qualityScore.compareTo(BigDecimal.ONE) != 0) {
            // 质量系数加权计算
            BigDecimal finalPoints = BigDecimal.valueOf(basePoints)
                    .multiply(qualityScore)
                    .setScale(0, RoundingMode.HALF_UP); // 四舍五入到整数

            int result = finalPoints.intValue();
            
            log.debug("模型 {} Token消耗计算: {}tokens -> {}K tokens, 基础积分: {}×{}={}, 质量系数: {}, 最终积分: {}",
                    modelKey, tokenCount, kTokens, kTokens, config.getPointsPerKToken(), 
                    basePoints, qualityScore, result);
            
            return result;
        }

        // 无质量系数或质量系数为1.0，返回基础积分
        log.debug("模型 {} Token消耗计算: {}tokens -> {}K tokens, 积分: {}×{}={} (无质量系数调整)",
                modelKey, tokenCount, kTokens, kTokens, config.getPointsPerKToken(), basePoints);
        
        return basePoints;
    }

    @Override
    public boolean updateQualityScore(String modelKey, BigDecimal qualityScore) {
        if (qualityScore == null || qualityScore.compareTo(BigDecimal.valueOf(0.5)) < 0 
                || qualityScore.compareTo(BigDecimal.valueOf(2.0)) > 0) {
            log.warn("质量系数超出范围(0.5-2.0): {}", qualityScore);
            return false;
        }

        AiModelConfig config = getByModelKey(modelKey);
        if (config == null) {
            log.warn("模型配置不存在: {}", modelKey);
            return false;
        }

        config.setQualityScore(qualityScore);
        boolean updated = this.updateById(config);
        
        if (updated) {
            log.info("模型 {} 质量系数已更新: {}", modelKey, qualityScore);
        } else {
            log.error("模型 {} 质量系数更新失败", modelKey);
        }
        
        return updated;
    }

    @Override
    public boolean updateModelStats(String modelKey, BigDecimal successRate, 
                                    Integer avgTokenUsage, BigDecimal userRating) {
        AiModelConfig config = getByModelKey(modelKey);
        if (config == null) {
            log.warn("模型配置不存在: {}", modelKey);
            return false;
        }

        if (successRate != null) {
            config.setSuccessRate(successRate);
        }
        if (avgTokenUsage != null) {
            config.setAvgTokenUsage(avgTokenUsage);
        }
        if (userRating != null) {
            config.setUserRating(userRating);
        }

        boolean updated = this.updateById(config);
        
        if (updated) {
            log.info("模型 {} 统计信息已更新: 成功率={}, 平均Token={}, 用户评分={}", 
                    modelKey, successRate, avgTokenUsage, userRating);
        } else {
            log.error("模型 {} 统计信息更新失败", modelKey);
        }
        
        return updated;
    }

}
