-- ======================================================
-- 删除指定的 DeepSeek 和 Kimi 免费模型
-- 日期: 2025-01-XX
-- 描述: 删除 deepseek-v3.1-free, deepseek-v3.1, deepseek-v3.2, kimi-k2-free 模型
-- ======================================================

USE ai_code_mother;

-- 查看即将删除的模型（执行前检查）
SELECT id, model_key, model_name, provider, is_enabled 
FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);

-- 删除指定的模型
DELETE FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',  -- DeepSeek V3.1 免费版
    'deepseek-v3.1',       -- DeepSeek V3.1 标准版
    'deepseek-v3.2',       -- DeepSeek V3.2 最新版
    'kimi-k2-free'         -- Kimi K2 免费版
);

-- 查看删除后的结果
SELECT COUNT(*) as remaining_models FROM ai_model_config WHERE is_enabled = 1;

-- 查看所有启用的模型列表
SELECT id, model_key, model_name, provider, tier, is_enabled, sort_order 
FROM ai_model_config 
WHERE is_enabled = 1 
ORDER BY sort_order;
