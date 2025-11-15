-- ======================================================
-- 优化AI模型质量倍率系统
-- 日期: 2025-10-18
-- 描述: 基于实际代码生成质量优化倍率配置
-- ======================================================

USE ai_code_mother;

-- ======================================================
-- 1. 为ai_model_config增加质量评分字段
-- ======================================================
ALTER TABLE `ai_model_config`
ADD COLUMN `quality_score` DECIMAL(3,2) DEFAULT 1.00 COMMENT '质量评分（0.5-2.0，用于动态调整倍率）' AFTER `points_per_k_token`,
ADD COLUMN `success_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '生成成功率（%）' AFTER `quality_score`,
ADD COLUMN `avg_token_usage` INT DEFAULT 0 COMMENT '平均token消耗' AFTER `success_rate`,
ADD COLUMN `user_rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '用户评分（1-5分）' AFTER `avg_token_usage`;

-- ======================================================
-- 2. 基于实际测试数据更新模型倍率（优化建议）
-- ======================================================

-- 2.1 SIMPLE级别 - 轻量快速模型（适合简单HTML）
UPDATE `ai_model_config` SET 
    `points_per_k_token` = 1,
    `quality_score` = 0.70,
    `description` = '【经济型】快速生成简单页面，代码质量一般，适合快速原型'
WHERE `model_key` = 'qwen-turbo';

UPDATE `ai_model_config` SET 
    `points_per_k_token` = 2,
    `quality_score` = 0.80,
    `description` = '【轻量级】免费编程模型，适合单文件简单应用'
WHERE `model_key` = 'codex-mini-latest';

-- 2.2 MEDIUM级别 - 均衡性能模型（适合常规应用）
UPDATE `ai_model_config` SET 
    `points_per_k_token` = 3,
    `quality_score` = 1.00,
    `description` = '【推荐】免费大参数模型，性能均衡，代码质量优秀'
WHERE `model_key` IN ('qwen3-235b-free', 'deepseek-v3.1-free', 'qwen-coder-32b-free');

UPDATE `ai_model_config` SET 
    `points_per_k_token` = 4,
    `quality_score` = 1.10,
    `description` = '【优质】专业编程模型，代码规范性强，适合多文件项目'
WHERE `model_key` LIKE '%coder%' AND `tier` = 'MEDIUM';

-- 2.3 HARD级别 - 高性能模型（适合复杂业务）
UPDATE `ai_model_config` SET 
    `points_per_k_token` = 8,
    `quality_score` = 1.40,
    `description` = '【专业级】深度推理模型，架构设计优秀，代码可维护性强'
WHERE `model_key` = 'deepseek-r1';

UPDATE `ai_model_config` SET 
    `points_per_k_token` = 10,
    `quality_score` = 1.50,
    `description` = '【高级】企业级编程模型，完整错误处理和测试用例'
WHERE `model_key` IN ('qwen3-coder-plus', 'deepseek-v3.1', 'deepseek-v3.2');

-- 2.4 EXPERT级别 - 顶级模型（适合企业架构）
UPDATE `ai_model_config` SET 
    `points_per_k_token` = 15,
    `quality_score` = 1.80,
    `description` = '【顶级】GPT-5 Codex系列，代码质量最高，完整文档和注释'
WHERE `model_key` LIKE 'gpt-5-codex%';

UPDATE `ai_model_config` SET 
    `points_per_k_token` = 18,
    `quality_score` = 2.00,
    `description` = '【巅峰】Qwen3思维推理版，顶级架构设计能力，代码艺术级'
WHERE `model_key` = 'qwen3-235b-a22b-thinking-2507';

-- ======================================================
-- 3. 创建模型质量统计表（用于动态调整倍率）
-- ======================================================
CREATE TABLE IF NOT EXISTS `ai_model_quality_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `model_key` VARCHAR(50) NOT NULL COMMENT '模型标识',
    `date` DATE NOT NULL COMMENT '统计日期',
    `total_generations` INT DEFAULT 0 COMMENT '总生成次数',
    `success_count` INT DEFAULT 0 COMMENT '成功次数',
    `failure_count` INT DEFAULT 0 COMMENT '失败次数',
    `avg_tokens` INT DEFAULT 0 COMMENT '平均token消耗',
    `avg_user_rating` DECIMAL(3,2) DEFAULT 0 COMMENT '平均用户评分',
    `total_points_consumed` BIGINT DEFAULT 0 COMMENT '总积分消耗',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_date` (`model_key`, `date`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型质量统计表';

-- ======================================================
-- 4. 创建用户模型评价表
-- ======================================================
CREATE TABLE IF NOT EXISTS `user_model_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `model_key` VARCHAR(50) NOT NULL COMMENT '模型标识',
    `app_id` BIGINT NOT NULL COMMENT '应用ID',
    `rating` INT NOT NULL COMMENT '评分（1-5星）',
    `comment` TEXT COMMENT '评价内容',
    `code_quality` INT COMMENT '代码质量评分（1-5）',
    `response_speed` INT COMMENT '响应速度评分（1-5）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_model_rating` (`model_key`, `rating`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户模型评价表';

-- ======================================================
-- 5. 查看优化后的模型倍率配置
-- ======================================================
SELECT 
    tier AS '等级',
    model_name AS '模型名称',
    points_per_k_token AS '积分倍率/1K',
    quality_score AS '质量系数',
    (points_per_k_token * quality_score) AS '实际价值',
    description AS '说明'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
ORDER BY 
    FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT'),
    points_per_k_token ASC;

-- ======================================================
-- 6. 按等级统计倍率分布
-- ======================================================
SELECT 
    tier AS '等级',
    COUNT(*) AS '模型数量',
    MIN(points_per_k_token) AS '最低倍率',
    AVG(points_per_k_token) AS '平均倍率',
    MAX(points_per_k_token) AS '最高倍率',
    AVG(quality_score) AS '平均质量'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
GROUP BY tier
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT');

-- ======================================================
-- 7. 推荐不同场景的模型
-- ======================================================
-- 新手入门推荐（低成本）
SELECT model_name, points_per_k_token, description
FROM ai_model_config
WHERE tier IN ('SIMPLE', 'MEDIUM') 
  AND is_enabled = 1 
  AND is_delete = 0
ORDER BY points_per_k_token ASC
LIMIT 3;

-- 专业开发推荐（性价比）
SELECT model_name, points_per_k_token, quality_score, description
FROM ai_model_config
WHERE tier = 'HARD' 
  AND is_enabled = 1 
  AND is_delete = 0
ORDER BY (points_per_k_token / quality_score) ASC
LIMIT 3;

-- 企业级推荐（最高质量）
SELECT model_name, points_per_k_token, quality_score, description
FROM ai_model_config
WHERE tier = 'EXPERT' 
  AND is_enabled = 1 
  AND is_delete = 0
ORDER BY quality_score DESC
LIMIT 3;

-- ======================================================
-- 执行完成
-- ======================================================
