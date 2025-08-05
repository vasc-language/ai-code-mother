-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  varchar(256) NOT NULL COMMENT '账号',
    `userPassword` varchar(512) NOT NULL COMMENT '密码',
    `userName`     varchar(256) NULL DEFAULT NULL COMMENT '用户昵称',
    `userAvatar`   varchar(1024) NULL DEFAULT NULL COMMENT '用户头像',
    `userProfile`  varchar(512) NULL DEFAULT NULL COMMENT '用户简介',
    `userRole`     varchar(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `createTime`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_userAccount`(`userAccount`)
) COMMENT '用户';
