-- 查看当前所有启用模型的倍率配置
USE ai_code_mother;

SELECT 
    tier AS '等级',
    model_key AS '模型标识',
    model_name AS '模型名称',
    points_per_k_token AS '倍率',
    quality_score AS '质量系数',
    ROUND(points_per_k_token * IFNULL(quality_score, 1.0), 2) AS '实际费率',
    description AS '说明'
FROM ai_model_config 
WHERE is_enabled = 1 AND is_delete = 0
ORDER BY 
    FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT'),
    points_per_k_token;
