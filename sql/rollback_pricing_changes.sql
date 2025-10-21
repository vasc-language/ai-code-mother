-- ======================================================
-- 回滚所有定价修改
-- 日期: 2025-10-18
-- 描述: 恢复到修改前的原始配置
-- ======================================================

USE ai_code_mother;

-- 回滚说明
SELECT '⚠️ 正在回滚所有模型配置修改...' AS '提示';

-- 1. 回滚 gpt-5-codex-low（恢复原始命名和等级）
UPDATE ai_model_config
SET 
    model_name = 'GPT-5 Codex 低配版',
    tier = 'HARD',
    points_per_k_token = 15,
    quality_score = 1.00,
    description = '【专业级】GPT-5 Codex 低配版'
WHERE model_key = 'gpt-5-codex-low';

-- 2. 回滚 gpt-5-low（恢复到MEDIUM）
UPDATE ai_model_config
SET 
    tier = 'MEDIUM',
    points_per_k_token = 2,
    quality_score = 1.00,
    description = 'GPT-5 低配版，经济实惠'
WHERE model_key = 'gpt-5-low';

-- 3. 回滚 gpt-5-minimal（恢复到HARD）
UPDATE ai_model_config
SET 
    tier = 'HARD',
    points_per_k_token = 2,
    quality_score = 1.00,
    description = 'GPT-5 精简版，性能优化'
WHERE model_key = 'gpt-5-minimal';

-- 4. 回滚 gpt-5（恢复到EXPERT）
UPDATE ai_model_config
SET 
    tier = 'EXPERT',
    points_per_k_token = 3,
    quality_score = 1.00,
    description = 'OpenAI GPT-5 标准版，顶级通用模型'
WHERE model_key = 'gpt-5';

-- 5. 回滚 gpt-5-medium（保持MEDIUM但恢复质量系数）
UPDATE ai_model_config
SET 
    quality_score = 1.00,
    description = 'GPT-5 中配版，平衡性能'
WHERE model_key = 'gpt-5-medium';

-- 6. 回滚 gpt-5-high（恢复到EXPERT）
UPDATE ai_model_config
SET 
    tier = 'EXPERT',
    quality_score = 1.00,
    description = 'GPT-5 高配版，卓越性能'
WHERE model_key = 'gpt-5-high';

-- 7. 回滚 gpt-5-codex（保持EXPERT但恢复质量系数）
UPDATE ai_model_config
SET 
    quality_score = 1.00,
    description = 'GPT-5 Codex 编程版'
WHERE model_key = 'gpt-5-codex';

-- 8. 回滚 gpt-5-codex-medium（恢复到HARD）
UPDATE ai_model_config
SET 
    tier = 'HARD',
    quality_score = 1.00,
    description = 'GPT-5 Codex 中配版'
WHERE model_key = 'gpt-5-codex-medium';

-- 9. 回滚 gpt-5-codex-high（恢复到EXPERT）
UPDATE ai_model_config
SET 
    quality_score = 1.00,
    description = 'GPT-5 Codex 高配版'
WHERE model_key = 'gpt-5-codex-high';

-- 10. 回滚 qwen 系列（恢复质量系数）
UPDATE ai_model_config
SET 
    quality_score = 1.00
WHERE model_key IN ('qwen3-coder', 'qwen3-coder-plus', 'qwen-coder-32b-free');

-- 11. 回滚 DeepSeek 系列（恢复等级和质量系数）
UPDATE ai_model_config
SET 
    tier = 'HARD',
    quality_score = 1.00
WHERE model_key = 'deepseek-r1';

UPDATE ai_model_config
SET 
    quality_score = 1.00
WHERE model_key IN ('deepseek-v3.1', 'deepseek-v3.2', 'deepseek-r1-0528-free');

-- 12. 回滚所有免费模型的质量系数
UPDATE ai_model_config
SET 
    quality_score = 1.00
WHERE model_key LIKE '%-free';

-- 查看回滚后的配置
SELECT 
    '✅ 回滚完成，当前配置如下：' AS '提示';

SELECT 
    model_key AS '模型标识',
    model_name AS '模型名称',
    tier AS '等级',
    points_per_k_token AS '倍率',
    quality_score AS '质量系数',
    description AS '说明'
FROM ai_model_config
WHERE is_enabled = 1 AND is_delete = 0
ORDER BY id;
