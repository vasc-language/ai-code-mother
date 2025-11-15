-- ======================================================
-- 删除 Kimi K2 系列模型
-- 日期: 2025-01-18
-- 描述: 删除 kimi-k2 和 kimi-k2-0903 模型（如果存在）
-- ======================================================

USE ai_code_mother;

-- 查看即将删除的模型（执行前检查）
SELECT id, model_key, model_name, provider, is_enabled, create_time
FROM ai_model_config 
WHERE model_key IN (
    'kimi-k2',
    'kimi-k2-0903',
    'kimi-k2-0905'
)
ORDER BY model_key;

-- 删除指定的 Kimi 模型
DELETE FROM ai_model_config 
WHERE model_key IN (
    'kimi-k2',         -- Kimi K2 标准版
    'kimi-k2-0903',    -- Kimi K2 0903版（如果存在）
    'kimi-k2-0905'     -- Kimi K2 0905版
);

-- 查看删除结果
SELECT CONCAT('已删除 ', ROW_COUNT(), ' 个 Kimi 模型') AS result;

-- 查看剩余的所有启用模型数量
SELECT 
    COUNT(*) as total_enabled_models,
    COUNT(CASE WHEN provider = 'iflow' AND model_key LIKE 'kimi%' THEN 1 END) as remaining_kimi_models
FROM ai_model_config 
WHERE is_enabled = 1 AND is_delete = 0;

-- 查看所有剩余的 Kimi 模型（如果还有）
SELECT id, model_key, model_name, provider, is_enabled, sort_order
FROM ai_model_config 
WHERE model_key LIKE 'kimi%' AND is_delete = 0
ORDER BY sort_order;
