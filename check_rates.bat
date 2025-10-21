@echo off
chcp 65001 >nul
echo 正在查询模型倍率配置...
echo.

mysql -u root -p -e "USE ai_code_mother; SELECT model_key AS '模型标识', tier AS '等级', points_per_k_token AS '倍率', quality_score AS '质量系数', ROUND(points_per_k_token * IFNULL(quality_score, 1.0), 2) AS '实际费率' FROM ai_model_config WHERE is_enabled = 1 AND is_delete = 0 ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT'), points_per_k_token;"

echo.
pause
