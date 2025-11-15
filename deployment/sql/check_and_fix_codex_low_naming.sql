-- 检查并修正 gpt-5-codex-low 的命名问题
USE ai_code_mother;

-- 1. 查看当前配置
SELECT 
    '当前配置' AS '检查项',
    model_key AS '模型标识',
    model_name AS '模型名称',
    tier AS '等级',
    points_per_k_token AS '倍率'
FROM ai_model_config
WHERE model_key = 'gpt-5-codex-low';

-- 2. 如果命名还是"低配版"，则修正
UPDATE ai_model_config
SET 
    model_name = 'GPT-5 Codex 高配版',
    description = '【企业级】GPT-5 Codex 高配版，企业级编程，架构完整'
WHERE model_key = 'gpt-5-codex-low'
  AND model_name LIKE '%低配%';

-- 3. 确认修改后的结果
SELECT 
    '修正后配置' AS '检查项',
    model_key AS '模型标识',
    model_name AS '模型名称',
    tier AS '等级',
    points_per_k_token AS '倍率',
    description AS '说明'
FROM ai_model_config
WHERE model_key = 'gpt-5-codex-low';

-- 4. 查看所有EXPERT级别的模型（确保命名一致）
SELECT 
    '所有EXPERT模型' AS '类别',
    model_key AS '模型标识',
    model_name AS '模型名称',
    points_per_k_token AS '倍率'
FROM ai_model_config
WHERE tier = 'EXPERT' 
  AND is_enabled = 1 
  AND is_delete = 0
ORDER BY points_per_k_token;
