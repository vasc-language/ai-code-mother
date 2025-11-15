-- ======================================================
-- 修复 user_points 表字段名称不匹配问题
-- 问题: Java代码使用 userId，数据库可能只有 id 字段
-- 日期: 2025-10-21
-- ======================================================

USE ai_code_mother;

-- 检查当前表结构
SELECT '当前 user_points 表结构：' AS '步骤1';
DESC user_points;

-- 备份数据（如果表存在且有数据）
CREATE TABLE IF NOT EXISTS user_points_backup AS 
SELECT * FROM user_points WHERE 1=0;  -- 只复制结构

-- 如果表中有数据，先备份
INSERT INTO user_points_backup 
SELECT * FROM user_points;

SELECT CONCAT('已备份 ', COUNT(*), ' 条记录') AS '步骤2' FROM user_points_backup;

-- 删除旧表
DROP TABLE IF EXISTS user_points;

-- 重新创建正确的表结构
CREATE TABLE `user_points`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID（雪花算法）',
    `userId`           bigint      NOT NULL COMMENT '用户ID',
    `totalPoints`      int         DEFAULT 0 COMMENT '累计获得积分',
    `availablePoints`  int         DEFAULT 0 COMMENT '当前可用积分',
    `frozenPoints`     int         DEFAULT 0 COMMENT '冻结积分（预留，暂不使用）',
    `createTime`       datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`       datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`         tinyint     DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId` (`userId`),
    INDEX `idx_availablePoints` (`availablePoints`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

SELECT '✅ 表结构已重新创建，字段名为 userId' AS '步骤3';

-- 如果需要恢复数据（根据实际情况调整）
-- INSERT INTO user_points (userId, totalPoints, availablePoints, frozenPoints, createTime, updateTime, isDelete)
-- SELECT id AS userId, totalPoints, availablePoints, frozenPoints, createTime, updateTime, isDelete
-- FROM user_points_backup;

-- 验证新表结构
SELECT '新的 user_points 表结构：' AS '步骤4';
DESC user_points;

SELECT '=' AS '============================';
SELECT '✅ 修复完成！' AS '结果';
SELECT 'Java代码中的 userId 字段现在可以正常使用了' AS '说明';
SELECT '备份表 user_points_backup 保留以防需要' AS '提示';
SELECT '=' AS '============================';
