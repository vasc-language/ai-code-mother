-- ======================================================
-- AI模型分级选择系统 - 数据库迁移脚本 v1.1.0
-- 日期: 2025-10-09
-- 描述: 实现基于积分的AI模型分级选择系统
-- ======================================================

USE ai_code_mother;

-- ======================================================
-- 1. 创建 AI 模型配置表
-- ======================================================
CREATE TABLE IF NOT EXISTS `ai_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `model_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '模型唯一标识（如：qwen-turbo）',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型显示名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供商：openai/openrouter/iflow/dashscope',
    `base_url` VARCHAR(255) NOT NULL COMMENT 'API Base URL',
    `tier` VARCHAR(20) NOT NULL COMMENT '等级：SIMPLE/MEDIUM/HARD/EXPERT',
    `points_per_k_token` INT NOT NULL COMMENT '每1000 tokens消耗积分',
    `description` TEXT COMMENT '模型描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序（越小越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT DEFAULT 0 COMMENT '是否删除（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_tier` (`tier`),
    KEY `idx_provider` (`provider`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

-- ======================================================
-- 2. 修改积分记录表，新增模型和Token字段
-- ======================================================
ALTER TABLE `points_record`
ADD COLUMN `model_key` VARCHAR(50) NULL COMMENT '使用的模型标识' AFTER `relatedId`,
ADD COLUMN `token_count` INT NULL COMMENT '消耗的Token数量' AFTER `model_key`,
ADD INDEX `idx_model_key` (`model_key`);

-- ======================================================
-- 3. 插入初始模型配置数据（方案二：激进定价）
-- ======================================================

-- 3.1 Simple tier models (1 point per 1K tokens)
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `sort_order`) VALUES
('qwen-turbo', 'Qwen Turbo', 'dashscope', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'SIMPLE', 1, 'Fast speed, suitable for simple tasks and quick prototypes', 1),
('llama-3.1-8b-free', 'Llama 3.1 8B', 'openrouter', 'https://openrouter.ai/api/v1', 'SIMPLE', 1, 'Free lightweight model, suitable for simple page generation', 2);

-- 3.2 Medium tier models (3 points per 1K tokens)
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `sort_order`) VALUES
('qwen3-235b-free', 'Qwen3 235B Free', 'openrouter', 'https://openrouter.ai/api/v1', 'MEDIUM', 3, 'Free large parameter model, balanced performance (Recommended)', 10),
('deepseek-v3.1-free', 'DeepSeek V3.1 Free', 'openrouter', 'https://openrouter.ai/api/v1', 'MEDIUM', 3, 'DeepSeek V3.1 free version, suitable for standard web apps', 11),
('qwen-coder-32b-free', 'Qwen Coder 32B Free', 'openrouter', 'https://openrouter.ai/api/v1', 'MEDIUM', 3, 'Programming-specific free model, suitable for code generation', 12);

-- 3.3 Hard tier models (8 points per 1K tokens)
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `sort_order`) VALUES
('deepseek-r1', 'DeepSeek R1', 'iflow', 'https://api.deepseek.com', 'HARD', 8, 'Current default reasoning model, high-quality complex projects', 20),
('qwen3-coder-plus', 'Qwen3 Coder Plus', 'iflow', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'HARD', 8, 'Programming-specific enhanced version for complex business logic', 21),
('deepseek-v3.1', 'DeepSeek V3.1', 'iflow', 'https://api.deepseek.com', 'HARD', 8, 'DeepSeek V3.1 paid version, full system development', 22);

-- 3.4 Expert tier models (15 points per 1K tokens)
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `sort_order`) VALUES
('gpt-5-codex-high', 'GPT-5 Codex High', 'openai', 'https://api.openai.com/v1', 'EXPERT', 15, 'GPT-5 Codex high-end version, top quality enterprise projects', 30),
('deepseek-r1-0528-free', 'DeepSeek R1 0528 Free', 'openrouter', 'https://openrouter.ai/api/v1', 'EXPERT', 15, 'DeepSeek R1 latest version, extremely high quality requirements', 31),
('qwen3-max-preview', 'Qwen3 Max Preview', 'iflow', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'EXPERT', 15, 'Qwen3 Max preview version, complex architecture design', 32);

-- ======================================================
-- 4. 更新积分类型枚举（如果需要）
-- ======================================================
-- 注意：PointsTypeEnum 已经有 GENERATE 类型，无需修改
-- 但我们会在后端代码中明确使用 GENERATE 类型记录AI生成消耗

-- ======================================================
-- 5. 数据验证查询
-- ======================================================
-- 查看所有模型配置
SELECT
    model_key,
    model_name,
    tier,
    points_per_k_token AS '积分/1K',
    provider,
    is_enabled AS '启用',
    description
FROM ai_model_config
WHERE is_delete = 0
ORDER BY sort_order;

-- 按等级统计模型数量
SELECT
    tier AS '等级',
    COUNT(*) AS '模型数量',
    GROUP_CONCAT(model_name SEPARATOR ', ') AS '模型列表'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
GROUP BY tier
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT');

-- ======================================================
-- 回滚脚本（如需要回滚，执行以下语句）
-- ======================================================
-- DROP TABLE IF EXISTS `ai_model_config`;
-- ALTER TABLE `points_record` DROP COLUMN `model_key`;
-- ALTER TABLE `points_record` DROP COLUMN `token_count`;
-- ALTER TABLE `points_record` DROP INDEX `idx_model_key`;

-- ======================================================
-- 脚本执行完成
-- ======================================================
