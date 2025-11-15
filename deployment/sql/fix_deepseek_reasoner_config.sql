-- ======================================================
-- 修复 deepseek-reasoner 模型配置缺失问题
-- 日期: 2025-10-10
-- 描述: 添加 deepseek-reasoner 模型配置到 ai_model_config 表
-- ======================================================

USE ai_code_mother;

-- 检查并插入 deepseek-reasoner 模型配置
-- 注意：使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `ai_model_config`
(
    `model_key`,
    `model_name`,
    `provider`,
    `base_url`,
    `tier`,
    `points_per_k_token`,
    `description`,
    `is_enabled`,
    `sort_order`
)
VALUES
(
    'deepseek-reasoner',                           -- model_key
    'DeepSeek Reasoner',                           -- model_name
    'deepseek',                                    -- provider
    'https://api.deepseek.com',                    -- base_url
    'HARD',                                        -- tier (推理模型，定为 HARD 等级)
    8,                                             -- points_per_k_token (与其他 HARD 模型一致)
    'DeepSeek推理模型，适合复杂逻辑推理和代码生成任务，支持最大32K tokens', -- description
    1,                                             -- is_enabled (启用)
    19                                             -- sort_order (在 HARD 等级中排第一位)
);

-- 验证插入结果
SELECT
    model_key,
    model_name,
    provider,
    tier,
    points_per_k_token AS '积分/1K',
    is_enabled AS '启用状态',
    description
FROM ai_model_config
WHERE model_key = 'deepseek-reasoner';

-- ======================================================
-- 脚本执行完成
-- ======================================================
