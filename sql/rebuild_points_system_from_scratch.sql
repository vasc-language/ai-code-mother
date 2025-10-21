-- ======================================================
-- 从零开始重建积分系统
-- 日期: 2025-10-18
-- 描述: 删除旧表，创建全新的积分系统表结构和基础数据
-- ======================================================

USE ai_code_mother;

SET FOREIGN_KEY_CHECKS = 0;

-- ======================================================
-- 1. 删除旧表
-- ======================================================
DROP TABLE IF EXISTS `ai_model_quality_stats`;
DROP TABLE IF EXISTS `user_model_rating`;
DROP TABLE IF EXISTS `ai_model_config`;
DROP TABLE IF EXISTS `points_record`;
DROP TABLE IF EXISTS `user_points`;
DROP TABLE IF EXISTS `sign_in_record`;
DROP TABLE IF EXISTS `invite_record`;

SELECT '✅ 旧表已删除' AS '步骤1';

-- ======================================================
-- 2. 创建用户积分账户表
-- ======================================================
CREATE TABLE `user_points` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID（关联user表）',
    `totalPoints` INT NOT NULL DEFAULT 0 COMMENT '累计获得积分（只增不减）',
    `availablePoints` INT NOT NULL DEFAULT 0 COMMENT '可用积分（可增可减）',
    `frozenPoints` INT NOT NULL DEFAULT 0 COMMENT '冻结积分（预留）',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_availablePoints` (`availablePoints`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户表';

-- ======================================================
-- 3. 创建积分明细表
-- ======================================================
CREATE TABLE `points_record` (
    `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `points` INT NOT NULL COMMENT '积分变动数量（正数增加，负数扣减）',
    `balance` INT NOT NULL COMMENT '变动后余额',
    `type` VARCHAR(50) NOT NULL COMMENT '积分类型（SIGN_IN:签到, REGISTER:注册, INVITE:邀请, GENERATE:生成, AI_GENERATE:AI生成消耗）',
    `reason` VARCHAR(255) COMMENT '变动原因描述',
    `relatedId` BIGINT COMMENT '关联ID（如应用ID、邀请记录ID）',
    `model_key` VARCHAR(50) COMMENT '使用的AI模型key（用于AI生成）',
    `token_count` INT COMMENT '消耗的token数量（用于AI生成）',
    `expireTime` DATETIME COMMENT '积分过期时间',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `isDelete` TINYINT DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    INDEX `idx_userId` (`userId`),
    INDEX `idx_type` (`type`),
    INDEX `idx_createTime` (`createTime`),
    INDEX `idx_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细表';

-- ======================================================
-- 4. 创建签到记录表
-- ======================================================
CREATE TABLE `sign_in_record` (
    `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `userId` BIGINT NOT NULL COMMENT '用户ID',
    `signInDate` DATE NOT NULL COMMENT '签到日期',
    `consecutiveDays` INT DEFAULT 1 COMMENT '连续签到天数',
    `pointsEarned` INT NOT NULL COMMENT '本次签到获得积分',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_date` (`userId`, `signInDate`),
    INDEX `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ======================================================
-- 5. 创建邀请记录表
-- ======================================================
CREATE TABLE `invite_record` (
    `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `inviterId` BIGINT NOT NULL COMMENT '邀请人ID',
    `inviteeId` BIGINT NOT NULL COMMENT '被邀请人ID',
    `inviteCode` VARCHAR(50) COMMENT '邀请码',
    `inviterReward` INT DEFAULT 20 COMMENT '邀请人奖励积分',
    `inviteeReward` INT DEFAULT 10 COMMENT '被邀请人奖励积分',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态（PENDING:待首次生成, COMPLETED:已完成, EXPIRED:已过期）',
    `firstGenerateTime` DATETIME COMMENT '被邀请人首次生成时间',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_inviterId` (`inviterId`),
    INDEX `idx_inviteeId` (`inviteeId`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请记录表';

-- ======================================================
-- 6. 创建AI模型配置表
-- ======================================================
CREATE TABLE `ai_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `model_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '模型唯一标识',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型显示名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供商',
    `base_url` VARCHAR(255) NOT NULL COMMENT 'API Base URL',
    `tier` VARCHAR(20) NOT NULL COMMENT '等级（SIMPLE/MEDIUM/HARD/EXPERT）',
    `points_per_k_token` INT NOT NULL COMMENT '每1K tokens消耗积分（倍率）',
    `quality_score` DECIMAL(3,2) DEFAULT 1.00 COMMENT '质量系数（0.5-2.0）',
    `success_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '成功率（%）',
    `avg_token_usage` INT DEFAULT 0 COMMENT '平均token消耗',
    `user_rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '用户评分（1-5）',
    `description` TEXT COMMENT '模型描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_tier` (`tier`),
    INDEX `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- ======================================================
-- 7. 创建模型质量统计表
-- ======================================================
CREATE TABLE `ai_model_quality_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    UNIQUE KEY `uk_model_date` (`model_key`, `date`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型质量统计表';

-- ======================================================
-- 8. 创建用户模型评价表
-- ======================================================
CREATE TABLE `user_model_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `model_key` VARCHAR(50) NOT NULL COMMENT '模型标识',
    `app_id` BIGINT NOT NULL COMMENT '应用ID',
    `rating` INT NOT NULL COMMENT '评分（1-5星）',
    `comment` TEXT COMMENT '评价内容',
    `code_quality` INT COMMENT '代码质量评分（1-5）',
    `response_speed` INT COMMENT '响应速度评分（1-5）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_model_rating` (`model_key`, `rating`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户模型评价表';

SELECT '✅ 所有表结构创建完成' AS '步骤2';

-- ======================================================
-- 9. 插入基础模型配置（保持简单，倍率合理）
-- ======================================================

-- SIMPLE 级别（入门经济）- 倍率 1-2
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `quality_score`, `description`, `sort_order`) VALUES
('codex-mini-latest', 'Codex Mini 最新版', 'openai', 'https://204992.xyz/v1', 'SIMPLE', 2, 1.00, '轻量级编程模型，适合简单应用', 1),
('gpt-5-minimal', 'GPT-5 精简版', 'openai', 'https://204992.xyz/v1', 'SIMPLE', 2, 1.00, 'GPT-5 精简版，性能优化', 2),
('gpt-5-low', 'GPT-5 低配版', 'openai', 'https://204992.xyz/v1', 'SIMPLE', 2, 1.00, 'GPT-5 低配版，经济实惠', 3);

-- MEDIUM 级别（日常开发）- 倍率 3-5
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `quality_score`, `description`, `sort_order`) VALUES
('qwen3-coder', 'Qwen3 Coder', 'iflow', 'https://204992.xyz/v1', 'MEDIUM', 3, 1.00, 'Qwen3 Coder 编程模型，性价比高', 10),
('gpt-5', 'GPT-5 标准版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 3, 1.00, 'GPT-5 标准版，顶级通用模型', 11),
('gpt-5-codex-low', 'GPT-5 Codex 低配版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 3, 1.00, 'GPT-5 Codex 低配版，编程入门', 12),
('gpt-5-medium', 'GPT-5 中配版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 5, 1.00, 'GPT-5 中配版，平衡性能', 13);

-- HARD 级别（企业专业）- 倍率 8-15
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `quality_score`, `description`, `sort_order`) VALUES
('qwen3-coder-plus', 'Qwen3 Coder Plus', 'iflow', 'https://204992.xyz/v1', 'HARD', 10, 1.00, 'Qwen3 Coder Plus 增强编程', 20),
('gpt-5-codex-medium', 'GPT-5 Codex 中配版', 'openai', 'https://204992.xyz/v1', 'HARD', 12, 1.00, 'GPT-5 Codex 中配版', 21),
('gpt-5-codex-high', 'GPT-5 Codex 高配版', 'openai', 'https://204992.xyz/v1', 'HARD', 15, 1.00, 'GPT-5 Codex 高配版', 22);

-- EXPERT 级别（顶级巅峰）- 倍率 15+
INSERT INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `quality_score`, `description`, `sort_order`) VALUES
('gpt-5-codex', 'GPT-5 Codex 编程版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15, 1.00, 'GPT-5 Codex 编程专用，顶级代码生成', 30);

SELECT '✅ 基础模型配置插入完成' AS '步骤3';

-- ======================================================
-- 10. 验证结果
-- ======================================================
SELECT '=' AS '============================';
SELECT '查看创建的表' AS '验证步骤';
SELECT '=' AS '============================';

SHOW TABLES LIKE '%points%';
SHOW TABLES LIKE '%model%';
SHOW TABLES LIKE '%invite%';
SHOW TABLES LIKE '%sign%';

SELECT '=' AS '============================';
SELECT '查看模型配置' AS '验证步骤';
SELECT '=' AS '============================';

SELECT 
    tier AS '等级',
    model_key AS '模型标识',
    model_name AS '模型名称',
    points_per_k_token AS '倍率',
    quality_score AS '质量系数',
    description AS '说明'
FROM ai_model_config
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT'), sort_order;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅✅✅ 积分系统重建完成！' AS '完成';
SELECT '所有表已删除并重新创建' AS '说明1';
SELECT '基础模型配置已插入（倍率简单合理）' AS '说明2';
SELECT '质量系数全部设为1.0（暂不启用质量加权）' AS '说明3';
