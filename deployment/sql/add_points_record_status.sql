-- 为积分记录表添加状态字段，支持FIFO积分管理
-- 执行时间：2025-01-XX
-- 作者：AI Assistant

USE ai_code_mother;

-- 1. 添加积分状态字段
ALTER TABLE points_record
ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '积分状态：ACTIVE=有效, EXPIRED=已过期, CONSUMED=已消费, PARTIAL_CONSUMED=部分消费' AFTER type;

-- 2. 添加实际过期金额字段（可能小于原始积分）
ALTER TABLE points_record
ADD COLUMN expired_amount INT DEFAULT 0 COMMENT '实际过期金额（可能小于points）' AFTER expireTime;

-- 3. 添加实际过期时间字段
ALTER TABLE points_record
ADD COLUMN actual_expire_time DATETIME COMMENT '实际过期时间' AFTER expired_amount;

-- 4. 添加剩余积分字段（用于FIFO消费追踪）
ALTER TABLE points_record
ADD COLUMN remaining_points INT COMMENT '剩余积分（仅对增加类型有效）' AFTER expired_amount;

-- 5. 添加索引优化查询性能
ALTER TABLE points_record
ADD INDEX idx_status_expire (status, expireTime);

ALTER TABLE points_record
ADD INDEX idx_user_status_expire (userId, status, expireTime);

-- 6. 初始化现有数据的status字段
UPDATE points_record 
SET status = CASE 
    WHEN type = 'EXPIRE' THEN 'EXPIRED'
    WHEN points > 0 AND (expireTime IS NULL OR expireTime > NOW()) THEN 'ACTIVE'
    WHEN points > 0 AND expireTime <= NOW() THEN 'EXPIRED'
    ELSE 'CONSUMED'
END
WHERE status = 'ACTIVE'; -- 只更新默认值的记录

-- 7. 初始化remaining_points字段（对于增加积分的记录）
UPDATE points_record 
SET remaining_points = points
WHERE points > 0 
  AND status = 'ACTIVE'
  AND remaining_points IS NULL;

-- 8. 验证SQL执行结果
SELECT '积分记录表字段添加完成' AS result;
SELECT 
    COUNT(*) AS total_records,
    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN status = 'EXPIRED' THEN 1 ELSE 0 END) AS expired_count,
    SUM(CASE WHEN status = 'CONSUMED' THEN 1 ELSE 0 END) AS consumed_count
FROM points_record;

-- 9. 查看表结构
DESC points_record;
