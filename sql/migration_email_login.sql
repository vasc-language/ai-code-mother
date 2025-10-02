-- 邮箱登录注册功能 - 数据库迁移脚本
-- 执行日期: 2025-10-02
-- 说明: 添加邮箱字段和验证码表支持邮箱登录注册

USE ai_code_mother;

-- 1. 修改user表,添加邮箱相关字段
ALTER TABLE `user`
ADD COLUMN `userEmail` varchar(256) NULL COMMENT '用户邮箱' AFTER `userAccount`,
ADD COLUMN `emailVerified` tinyint NOT NULL DEFAULT 0 COMMENT '邮箱是否验证:0-未验证,1-已验证' AFTER `userEmail`,
ADD UNIQUE INDEX `uni_userEmail` (`userEmail`);

-- 2. 创建邮箱验证码表
CREATE TABLE IF NOT EXISTS `email_verification_code`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email`      varchar(256) NOT NULL COMMENT '邮箱地址',
    `code`       varchar(10)  NOT NULL COMMENT '验证码',
    `type`       varchar(20)  NOT NULL COMMENT '验证码类型:REGISTER-注册, RESET_PASSWORD-重置密码, LOGIN-登录',
    `expireTime` datetime     NOT NULL COMMENT '过期时间',
    `verified`   tinyint      DEFAULT 0 COMMENT '是否已使用:0-未使用, 1-已使用',
    `createTime` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   tinyint      DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_email` (`email`),
    INDEX `idx_email_type` (`email`, `type`),
    INDEX `idx_expire` (`expireTime`)
) COMMENT='邮箱验证码表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 为现有用户添加默认邮箱(可选,仅用于兼容性)
-- UPDATE `user` SET `userEmail` = CONCAT(userAccount, '@example.com') WHERE `userEmail` IS NULL;
