# 数据库初始化
# @author <a href="https://github.com/vasc-language/ai-code-mother">Join2049</a>

-- 创建库
create database if not exists ai_code_mother;

-- 切换库
use ai_code_mother;


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

-- 插入数据
INSERT INTO `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userProfile`, `userRole`) VALUES
('admin', '45682a7088aad280cec116864abbdc93', '管理员', 'https://material-center.meitudata.com/material/image/62bc0346cf1202921.png', '我是管理员', 'admin'),
('admin2', '45682a7088aad280cec116864abbdc93', '管理员2', 'https://material-center.meitudata.com/material/image/62bc0346cf1202921.png', '我是管理员2', 'admin'),
('user1', '45682a7088aad280cec116864abbdc93', '用户1', 'https://material-center.meitudata.com/material/image/62bc0346cf1202921.png', '我是用户1', 'user'),
('user2', '45682a7088aad280cec116864abbdc93', '用户2', 'https://material-center.meitudata.com/material/image/62bc0346cf1202921.png', '我是用户2', 'user');

-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 应用版本历史表
create table app_version
(
    id           bigint auto_increment comment '版本记录id' primary key,
    appId        bigint                             not null comment '关联的应用id',
    versionNum   int                                not null comment '版本号（1, 2, 3...）',
    versionTag   varchar(32)                        not null comment '版本标签（v1, v2, v3...）',
    codeContent  longtext                           null comment '代码内容（JSON格式，包含所有文件）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployUrl    varchar(512)                       null comment '部署URL',
    deployedTime datetime                           null comment '部署时间',
    userId       bigint                             not null comment '部署操作用户id',
    remark       varchar(512)                       null comment '版本备注说明',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                            -- 提升基于应用的查询性能
    INDEX idx_appId_versionNum (appId, versionNum),     -- 组合索引加速版本查询
    UNIQUE KEY uk_appId_versionNum (appId, versionNum)  -- 确保同一应用的版本号唯一
) comment '应用版本历史' collate = utf8mb4_unicode_ci;

