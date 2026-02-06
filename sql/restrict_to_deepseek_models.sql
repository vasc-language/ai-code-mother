-- 目标：将模型池收敛为 DeepSeek 官方两模型
-- 影响表：ai_model_config、app
-- 执行前请先备份数据库

START TRANSACTION;

-- 1) 保证 deepseek-chat / deepseek-reasoner 存在且启用
INSERT INTO ai_model_config (
    model_key,
    model_name,
    provider,
    base_url,
    tier,
    points_per_k_token,
    description,
    is_enabled,
    is_delete,
    sort_order,
    create_time,
    update_time
)
VALUES
    ('deepseek-chat', 'DeepSeek Chat', 'deepseek', 'https://api.deepseek.com', 'MEDIUM', 5, 'DeepSeek 官方聊天模型', 1, 0, 10, NOW(), NOW()),
    ('deepseek-reasoner', 'DeepSeek Reasoner', 'deepseek', 'https://api.deepseek.com', 'HARD', 8, 'DeepSeek 官方推理模型', 1, 0, 11, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    model_name = VALUES(model_name),
    provider = VALUES(provider),
    base_url = VALUES(base_url),
    tier = VALUES(tier),
    points_per_k_token = VALUES(points_per_k_token),
    description = VALUES(description),
    is_enabled = 1,
    is_delete = 0,
    update_time = NOW();

-- 2) 禁用其他模型，仅保留两个 DeepSeek 模型
UPDATE ai_model_config
SET is_enabled = CASE
        WHEN model_key IN ('deepseek-chat', 'deepseek-reasoner') THEN 1
        ELSE 0
    END,
    provider = CASE
        WHEN model_key IN ('deepseek-chat', 'deepseek-reasoner') THEN 'deepseek'
        ELSE provider
    END,
    base_url = CASE
        WHEN model_key IN ('deepseek-chat', 'deepseek-reasoner') THEN 'https://api.deepseek.com'
        ELSE base_url
    END,
    update_time = NOW();

-- 3) 修正历史应用默认模型，避免旧无效 modelKey 被继续使用
UPDATE app
SET modelKey = 'deepseek-chat'
WHERE modelKey IS NULL
   OR modelKey = ''
   OR modelKey NOT IN ('deepseek-chat', 'deepseek-reasoner');

COMMIT;

-- 4) 校验结果
SELECT id, model_key, model_name, provider, base_url, tier, is_enabled, is_delete, sort_order
FROM ai_model_config
WHERE model_key IN ('deepseek-chat', 'deepseek-reasoner')
ORDER BY sort_order;

SELECT COUNT(*) AS enabled_model_count
FROM ai_model_config
WHERE is_enabled = 1;

