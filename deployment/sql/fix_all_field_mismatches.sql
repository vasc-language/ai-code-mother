-- ======================================================
-- 修复所有表结构与Java实体类字段不匹配问题
-- 日期: 2025-10-21
-- 问题: sign_in_record 和 invite_record 表字段与Java代码不匹配
-- ======================================================

USE ai_code_mother;

SET FOREIGN_KEY_CHECKS = 0;

-- ======================================================
-- 问题1: 修复 sign_in_record 表
-- ======================================================

SELECT '开始修复 sign_in_record 表...' AS '步骤1';

-- 1.1 重命名 consecutiveDays 为 continuousDays
ALTER TABLE `sign_in_record` 
CHANGE COLUMN `consecutiveDays` `continuousDays` int DEFAULT 1 COMMENT '连续签到天数';

-- 1.2 添加缺失的 isDelete 字段
ALTER TABLE `sign_in_record` 
ADD COLUMN IF NOT EXISTS `isDelete` tinyint DEFAULT 0 COMMENT '逻辑删除';

SELECT '✅ sign_in_record 表修复完成' AS '步骤1结果';
SELECT '  - consecutiveDays → continuousDays' AS '  修改1';
SELECT '  - 添加 isDelete 字段' AS '  修改2';

-- 验证 sign_in_record
DESC sign_in_record;

-- ======================================================
-- 问题2: 重建 invite_record 表（严重不匹配）
-- ======================================================

SELECT '开始修复 invite_record 表...' AS '步骤2';

-- 2.1 备份现有数据
DROP TABLE IF EXISTS `invite_record_backup_20251021`;
CREATE TABLE `invite_record_backup_20251021` AS 
SELECT * FROM `invite_record`;

SELECT CONCAT('已备份 ', COUNT(*), ' 条记录到 invite_record_backup_20251021') AS '步骤2-备份' 
FROM `invite_record_backup_20251021`;

-- 2.2 删除旧表
DROP TABLE IF EXISTS `invite_record`;

-- 2.3 创建正确的表结构（与 InviteRecord.java 完全匹配）
CREATE TABLE `invite_record`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID（雪花算法）',
    `inviterId`      bigint       NOT NULL COMMENT '邀请人ID',
    `inviteeId`      bigint       NOT NULL COMMENT '被邀请人ID',
    `inviteCode`     varchar(32)  NOT NULL COMMENT '邀请码',
    `registerIp`     varchar(50)  NULL COMMENT '注册IP',
    `deviceId`       varchar(100) NULL COMMENT '设备ID',
    `status`         varchar(20)  DEFAULT 'PENDING' COMMENT '状态（PENDING:待确认, REGISTERED:已注册, REWARDED:已奖励）',
    `inviterPoints`  int          DEFAULT 0 COMMENT '邀请人获得积分',
    `inviteePoints`  int          DEFAULT 0 COMMENT '被邀请人获得积分',
    `createTime`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `registerTime`   datetime     NULL COMMENT '注册时间',
    `rewardTime`     datetime     NULL COMMENT '奖励发放时间',
    `isDelete`       tinyint      DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inviteCode` (`inviteCode`),
    INDEX `idx_inviterId` (`inviterId`),
    INDEX `idx_inviteeId` (`inviteeId`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请关系表';

SELECT '✅ invite_record 表已重建' AS '步骤2结果';

-- 2.4 迁移数据（从备份表）
INSERT INTO `invite_record` (
    `id`, `inviterId`, `inviteeId`, `inviteCode`, `status`, 
    `inviterPoints`, `inviteePoints`, `createTime`
)
SELECT 
    `id`, 
    `inviterId`, 
    `inviteeId`, 
    COALESCE(`inviteCode`, CONCAT('INV', id)) AS inviteCode,  -- 如果没有邀请码，生成一个
    `status`, 
    COALESCE(`inviterReward`, 20) AS inviterPoints,           -- 旧字段名 inviterReward
    COALESCE(`inviteeReward`, 10) AS inviteePoints,           -- 旧字段名 inviteeReward
    `createTime`
FROM `invite_record_backup_20251021`;

SELECT CONCAT('已迁移 ', COUNT(*), ' 条记录') AS '步骤2-迁移' 
FROM `invite_record`;

-- 验证 invite_record
DESC invite_record;

-- ======================================================
-- 验证所有修复
-- ======================================================

SELECT '=' AS '============================';
SELECT '验证表结构' AS '步骤3';
SELECT '=' AS '============================';

-- 3.1 验证 sign_in_record
SELECT '✅ sign_in_record 表结构：' AS '表1';
SHOW COLUMNS FROM sign_in_record LIKE 'continuousDays';
SHOW COLUMNS FROM sign_in_record LIKE 'isDelete';

-- 3.2 验证 invite_record
SELECT '✅ invite_record 表结构：' AS '表2';
SHOW COLUMNS FROM invite_record LIKE 'inviterPoints';
SHOW COLUMNS FROM invite_record LIKE 'inviteePoints';
SHOW COLUMNS FROM invite_record LIKE 'registerIp';
SHOW COLUMNS FROM invite_record LIKE 'deviceId';
SHOW COLUMNS FROM invite_record LIKE 'registerTime';
SHOW COLUMNS FROM invite_record LIKE 'rewardTime';
SHOW COLUMNS FROM invite_record LIKE 'isDelete';

-- 3.3 数据验证
SELECT '=' AS '============================';
SELECT '数据验证' AS '步骤4';
SELECT '=' AS '============================';

SELECT 
    COUNT(*) AS '签到记录总数',
    COUNT(DISTINCT userId) AS '签到用户数'
FROM sign_in_record;

SELECT 
    COUNT(*) AS '邀请记录总数',
    COUNT(CASE WHEN status='REWARDED' THEN 1 END) AS '已奖励记录数',
    SUM(inviterPoints) AS '邀请人总积分',
    SUM(inviteePoints) AS '被邀请人总积分'
FROM invite_record;

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================
-- 完成
-- ======================================================

SELECT '=' AS '============================';
SELECT '✅✅✅ 所有表结构修复完成！' AS '完成';
SELECT '=' AS '============================';
SELECT '修复内容：' AS '汇总';
SELECT '1. sign_in_record: consecutiveDays → continuousDays' AS '  -';
SELECT '2. sign_in_record: 添加 isDelete 字段' AS '  -';
SELECT '3. invite_record: 重建整个表结构' AS '  -';
SELECT '4. invite_record: inviterReward → inviterPoints' AS '  -';
SELECT '5. invite_record: inviteeReward → inviteePoints' AS '  -';
SELECT '6. invite_record: 添加 registerIp, deviceId, registerTime, rewardTime, isDelete' AS '  -';
SELECT '=' AS '============================';
SELECT '备份表：' AS '提示';
SELECT '  - invite_record_backup_20251021（已保留原始数据）' AS '  -';
SELECT '=' AS '============================';
SELECT '下一步：重启应用，测试所有功能' AS '建议';
SELECT '=' AS '============================';
