package com.spring.aicodemother.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * AI模型配置 表定义层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public class AiModelConfigTableDef extends TableDef {

    /**
     * AI模型配置表
     */
    public static final AiModelConfigTableDef AI_MODEL_CONFIG = new AiModelConfigTableDef();

    /**
     * 主键ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 模型唯一标识
     */
    public final QueryColumn MODEL_KEY = new QueryColumn(this, "model_key");

    /**
     * 模型显示名称
     */
    public final QueryColumn MODEL_NAME = new QueryColumn(this, "model_name");

    /**
     * 提供商
     */
    public final QueryColumn PROVIDER = new QueryColumn(this, "provider");

    /**
     * API基础URL
     */
    public final QueryColumn BASE_URL = new QueryColumn(this, "base_url");

    /**
     * 模型等级
     */
    public final QueryColumn TIER = new QueryColumn(this, "tier");

    /**
     * 每1K token消耗的积分
     */
    public final QueryColumn POINTS_PER_K_TOKEN = new QueryColumn(this, "points_per_k_token");

    /**
     * 模型描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 是否启用
     */
    public final QueryColumn IS_ENABLED = new QueryColumn(this, "is_enabled");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段,不包含逻辑删除或大字段等
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
        ID, MODEL_KEY, MODEL_NAME, PROVIDER, BASE_URL,
        TIER, POINTS_PER_K_TOKEN, DESCRIPTION, IS_ENABLED, SORT_ORDER
    };

    public AiModelConfigTableDef() {
        super("", "ai_model_config");
    }

}
