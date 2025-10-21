package com.spring.aicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI模型配置 实体类。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_model_config")
public class AiModelConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增）
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 模型唯一标识（如 qwen-turbo）
     */
    @Column("model_key")
    private String modelKey;

    /**
     * 模型显示名称
     */
    @Column("model_name")
    private String modelName;

    /**
     * 提供商（如 OpenAI, DashScope）
     */
    @Column("provider")
    private String provider;

    /**
     * API基础URL
     */
    @Column("base_url")
    private String baseUrl;

    /**
     * 模型等级（SIMPLE, MEDIUM, HARD, EXPERT）
     */
    @Column("tier")
    private String tier;

    /**
     * 每1K token消耗的积分
     */
    @Column("points_per_k_token")
    private Integer pointsPerKToken;

    /**
     * 质量评分（0.5-2.0，用于动态调整倍率）
     */
    @Column("quality_score")
    private java.math.BigDecimal qualityScore;

    /**
     * 生成成功率（%）
     */
    @Column("success_rate")
    private java.math.BigDecimal successRate;

    /**
     * 平均token消耗
     */
    @Column("avg_token_usage")
    private Integer avgTokenUsage;

    /**
     * 用户评分（1-5分）
     */
    @Column("user_rating")
    private java.math.BigDecimal userRating;

    /**
     * 模型描述
     */
    @Column("description")
    private String description;

    /**
     * 是否启用（1=启用 0=禁用）
     */
    @Column("is_enabled")
    private Integer isEnabled;

    /**
     * 排序顺序（数字越小越靠前）
     */
    @Column("sort_order")
    private Integer sortOrder;

}
