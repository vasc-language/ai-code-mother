-- ======================================================
-- 修正AI模型配置定价和质量系数 v2
-- 日期: 2025-10-18
-- 描述: 修正不合理的等级划分、价格设置和质量系数
-- 说明: 本脚本假设 quality_score 等字段已存在，仅修正数据
-- ======================================================

USE ai_code_mother;

-- ======================================================
-- 0. 检查字段是否存在（仅查询验证）
-- ======================================================
SELECT 'quality_score字段检查' AS 检查项,
       IF(COUNT(*) > 0, '✅ 已存在', '❌ 不存在') AS 状态
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ai_code_mother'
  AND TABLE_NAME = 'ai_model_config'
  AND COLUMN_NAME = 'quality_score';

-- ======================================================
-- 1. 修正 SIMPLE 级别（1-2积分/1K，质量系数0.7-0.8）
-- ======================================================

-- Codex Mini - 保持不变，配置合理
UPDATE ai_model_config 
SET quality_score = 0.80,
    description = '【经济型】轻量级编程模型，适合单文件简单应用'
WHERE model_key = 'codex-mini-latest';

-- GPT-5 低配版 - 从MEDIUM降级到SIMPLE
UPDATE ai_model_config 
SET tier = 'SIMPLE',
    points_per_k_token = 2,
    quality_score = 0.75,
    description = '【入门级】GPT-5 低配版，适合快速原型和简单页面'
WHERE model_key = 'gpt-5-low';

-- GPT-5 精简版 - 从HARD降级到SIMPLE
UPDATE ai_model_config 
SET tier = 'SIMPLE',
    points_per_k_token = 2,
    quality_score = 0.80,
    description = '【轻量级】GPT-5 精简版，性能优化，适合标准HTML'
WHERE model_key = 'gpt-5-minimal';

-- ======================================================
-- 2. 修正 MEDIUM 级别（3-5积分/1K，质量系数0.9-1.2）
-- ======================================================

-- GPT-5 标准版 - 从EXPERT降级到MEDIUM
UPDATE ai_model_config 
SET tier = 'MEDIUM',
    points_per_k_token = 3,
    quality_score = 1.00,
    description = '【标准版】GPT-5 标准版，均衡性能，适合常规Web应用'
WHERE model_key = 'gpt-5';

-- GPT-5 中配版 - 保持MEDIUM，但调整质量系数
UPDATE ai_model_config 
SET tier = 'MEDIUM',
    points_per_k_token = 5,
    quality_score = 1.15,
    description = '【进阶版】GPT-5 中配版，代码质量优秀，适合多文件项目'
WHERE model_key = 'gpt-5-medium';

-- Qwen3 Coder - 调整为MEDIUM推荐选项
UPDATE ai_model_config 
SET tier = 'MEDIUM',
    points_per_k_token = 3,
    quality_score = 1.00,
    description = '【推荐】Qwen3 Coder 编程模型，性价比高'
WHERE model_key = 'qwen3-coder';

-- Qwen Coder 32B Free - 调整质量系数
UPDATE ai_model_config 
SET quality_score = 1.10,
    description = '【专业级】Qwen Coder 32B 免费版，专业编程能力强'
WHERE model_key = 'qwen-coder-32b-free';

-- ======================================================
-- 3. 修正 HARD 级别（8-12积分/1K，质量系数1.3-1.6）
-- ======================================================

-- GPT-5 高配版 - 从EXPERT降级到HARD
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 10,
    quality_score = 1.50,
    description = '【高级版】GPT-5 高配版，卓越性能，适合复杂业务系统'
WHERE model_key = 'gpt-5-high';

-- Qwen3 Coder Plus - 调整质量系数
UPDATE ai_model_config 
SET quality_score = 1.40,
    description = '【企业级】Qwen3 Coder Plus 增强编程，完整错误处理'
WHERE model_key = 'qwen3-coder-plus';

-- DeepSeek R1 - 设置为HARD推荐
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 10,
    quality_score = 1.50,
    description = '【推荐】DeepSeek R1 深度推理模型，架构设计优秀'
WHERE model_key = 'deepseek-r1';

-- DeepSeek V3.1 - 调整为HARD
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 10,
    quality_score = 1.40,
    description = '【专业级】DeepSeek V3.1 标准版，全栈开发能力'
WHERE model_key = 'deepseek-v3.1';

-- DeepSeek V3.2 - 调整为HARD
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 12,
    quality_score = 1.50,
    description = '【最新版】DeepSeek V3.2 最新增强，代码质量提升'
WHERE model_key = 'deepseek-v3.2';

-- DeepSeek R1 0528 - 修正命名和等级
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 12,
    quality_score = 1.60,
    model_name = 'DeepSeek R1 0528',
    description = '【增强版】DeepSeek R1 0528版本，推理能力强化'
WHERE model_key = 'deepseek-r1-0528-free';

-- ======================================================
-- 4. 修正 EXPERT 级别（15-20积分/1K，质量系数1.7-2.0）
-- ======================================================

-- GPT-5 Codex 编程版 - 保持EXPERT
UPDATE ai_model_config 
SET points_per_k_token = 15,
    quality_score = 1.80,
    description = '【顶级】GPT-5 Codex 编程专用，代码质量巅峰，完整文档'
WHERE model_key = 'gpt-5-codex';

-- GPT-5 Codex 低配版 - 重命名为高配版
UPDATE ai_model_config 
SET model_name = 'GPT-5 Codex 高配版',
    tier = 'EXPERT',
    points_per_k_token = 15,
    quality_score = 1.70,
    description = '【企业级】GPT-5 Codex 高配版，企业级编程，架构完整'
WHERE model_key = 'gpt-5-codex-low';

-- GPT-5 Codex 中配版 - 降级到HARD
UPDATE ai_model_config 
SET tier = 'HARD',
    points_per_k_token = 10,
    quality_score = 1.50,
    description = '【高级】GPT-5 Codex 中配版，标准企业编程'
WHERE model_key = 'gpt-5-codex-medium';

-- GPT-5 Codex 高配版（原名） - 重命名为旗舰版，保持EXPERT最高级
UPDATE ai_model_config 
SET model_name = 'GPT-5 Codex 旗舰版',
    points_per_k_token = 18,
    quality_score = 1.90,
    description = '【旗舰级】GPT-5 Codex 旗舰版，最高代码质量，包含测试用例'
WHERE model_key = 'gpt-5-codex-high';

-- Qwen3 Max Preview - 调整为EXPERT
UPDATE ai_model_config 
SET points_per_k_token = 15,
    quality_score = 1.75,
    description = '【顶级】Qwen3 Max 预览版，复杂架构设计能力'
WHERE model_key = 'qwen3-max-preview';

-- Qwen3 235B Thinking - 设置为最高级别
UPDATE ai_model_config 
SET points_per_k_token = 20,
    quality_score = 2.00,
    description = '【巅峰】Qwen3 235B 思维推理版，代码艺术级，顶级架构'
WHERE model_key = 'qwen3-235b-a22b-thinking-2507';

-- ======================================================
-- 5. 调整免费模型的质量系数（MEDIUM级别为主）
-- ======================================================

UPDATE ai_model_config 
SET quality_score = 1.00
WHERE model_key IN (
    'qwen3-235b-free',
    'deepseek-v3.1-free',
    'deepseek-r1-free',
    'llama-3.3-70b-free',
    'qwen-2.5-72b-free'
) AND tier = 'MEDIUM';

UPDATE ai_model_config 
SET quality_score = 1.05
WHERE model_key IN (
    'qwen3-coder-free',
    'devstral-free'
) AND tier = 'MEDIUM';

-- ======================================================
-- 6. 验证查询 - 查看修正后的配置
-- ======================================================

SELECT 
    '=' AS '======================',
    '修正后的模型配置' AS '标题',
    '=' AS '======================'
UNION ALL
SELECT '', '', '';

SELECT 
    tier AS '等级',
    model_name AS '模型名称',
    points_per_k_token AS '积分倍率/1K',
    quality_score AS '质量系数',
    ROUND(points_per_k_token * quality_score, 2) AS '实际价值',
    LEFT(description, 30) AS '说明'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
ORDER BY 
    FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT'),
    (points_per_k_token * quality_score) ASC;

-- ======================================================
-- 7. 按等级统计修正后的分布
-- ======================================================

SELECT '' AS '';
SELECT '=' AS '======================',
       '等级统计' AS '标题',
       '=' AS '======================';

SELECT 
    tier AS '等级',
    COUNT(*) AS '模型数量',
    MIN(points_per_k_token) AS '最低倍率',
    ROUND(AVG(points_per_k_token), 1) AS '平均倍率',
    MAX(points_per_k_token) AS '最高倍率',
    ROUND(MIN(points_per_k_token * quality_score), 2) AS '最低实际价值',
    ROUND(AVG(points_per_k_token * quality_score), 2) AS '平均实际价值',
    ROUND(MAX(points_per_k_token * quality_score), 2) AS '最高实际价值'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
GROUP BY tier
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT');

-- ======================================================
-- 8. 检查异常配置
-- ======================================================

SELECT '' AS '';
SELECT '=' AS '======================',
       '异常检查' AS '标题',
       '=' AS '======================';

-- 检查SIMPLE级别中是否有高价模型（应该<=2积分）
SELECT '【SIMPLE异常】' AS 类型, model_key, model_name, tier, points_per_k_token, quality_score
FROM ai_model_config
WHERE tier = 'SIMPLE' AND points_per_k_token > 2 AND is_enabled = 1
UNION ALL
-- 检查MEDIUM级别中是否有超出范围的模型（应该3-5积分）
SELECT '【MEDIUM异常】' AS 类型, model_key, model_name, tier, points_per_k_token, quality_score
FROM ai_model_config
WHERE tier = 'MEDIUM' AND (points_per_k_token < 3 OR points_per_k_token > 5) AND is_enabled = 1
UNION ALL
-- 检查HARD级别中是否有超出范围的模型（应该8-12积分）
SELECT '【HARD异常】' AS 类型, model_key, model_name, tier, points_per_k_token, quality_score
FROM ai_model_config
WHERE tier = 'HARD' AND (points_per_k_token < 8 OR points_per_k_token > 12) AND is_enabled = 1
UNION ALL
-- 检查EXPERT级别中是否有低价模型（应该>=15积分）
SELECT '【EXPERT异常】' AS 类型, model_key, model_name, tier, points_per_k_token, quality_score
FROM ai_model_config
WHERE tier = 'EXPERT' AND points_per_k_token < 15 AND is_enabled = 1;

-- 如果没有异常，显示提示
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM (
            SELECT 1 FROM ai_model_config WHERE tier = 'SIMPLE' AND points_per_k_token > 2 AND is_enabled = 1
            UNION ALL
            SELECT 1 FROM ai_model_config WHERE tier = 'MEDIUM' AND (points_per_k_token < 3 OR points_per_k_token > 5) AND is_enabled = 1
            UNION ALL
            SELECT 1 FROM ai_model_config WHERE tier = 'HARD' AND (points_per_k_token < 8 OR points_per_k_token > 12) AND is_enabled = 1
            UNION ALL
            SELECT 1 FROM ai_model_config WHERE tier = 'EXPERT' AND points_per_k_token < 15 AND is_enabled = 1
        ) t) = 0 
        THEN '✅ 未发现异常配置'
        ELSE '⚠️ 发现异常配置，请检查上方列表'
    END AS '检查结果';

-- ======================================================
-- 9. 各等级推荐模型
-- ======================================================

SELECT '' AS '';
SELECT '=' AS '======================',
       '推荐模型' AS '标题',
       '=' AS '======================';

-- SIMPLE级别推荐（性价比最高）
(SELECT 
    'SIMPLE推荐' AS '等级',
    model_name AS '模型',
    points_per_k_token AS '倍率',
    quality_score AS '质量',
    ROUND(points_per_k_token * quality_score, 2) AS '实际价值',
    '经济实惠' AS '特点'
FROM ai_model_config
WHERE tier = 'SIMPLE' AND is_enabled = 1
ORDER BY quality_score DESC
LIMIT 1)

UNION ALL

-- MEDIUM级别推荐（质量/价格比最高）
(SELECT 
    'MEDIUM推荐' AS '等级',
    model_name AS '模型',
    points_per_k_token AS '倍率',
    quality_score AS '质量',
    ROUND(points_per_k_token * quality_score, 2) AS '实际价值',
    '性价比高' AS '特点'
FROM ai_model_config
WHERE tier = 'MEDIUM' AND is_enabled = 1
ORDER BY quality_score / points_per_k_token DESC
LIMIT 1)

UNION ALL

-- HARD级别推荐（质量最高）
(SELECT 
    'HARD推荐' AS '等级',
    model_name AS '模型',
    points_per_k_token AS '倍率',
    quality_score AS '质量',
    ROUND(points_per_k_token * quality_score, 2) AS '实际价值',
    '专业开发' AS '特点'
FROM ai_model_config
WHERE tier = 'HARD' AND is_enabled = 1
ORDER BY quality_score DESC
LIMIT 1)

UNION ALL

-- EXPERT级别推荐（顶级质量）
(SELECT 
    'EXPERT推荐' AS '等级',
    model_name AS '模型',
    points_per_k_token AS '倍率',
    quality_score AS '质量',
    ROUND(points_per_k_token * quality_score, 2) AS '实际价值',
    '巅峰品质' AS '特点'
FROM ai_model_config
WHERE tier = 'EXPERT' AND is_enabled = 1
ORDER BY quality_score DESC
LIMIT 1);

-- ======================================================
-- 执行完成提示
-- ======================================================
SELECT '' AS '';
SELECT '✅' AS '状态', 
       '模型配置修正完成！' AS '信息',
       '请查看上方验证查询结果，确认配置合理性。' AS '说明';
