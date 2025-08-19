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
('admin', '12345678', '管理员', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是管理员', 'admin'),
('admin2', '12345678', '管理员2', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是管理员2', 'admin'),
('user1', '12345678', '用户1', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户1', 'user'),
('user2', '12345678', '用户2', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户2', 'user'),
('user3', '12345678', '用户3', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户3', 'user'),
('user4', '12345678', '用户4', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户4', 'user'),
('user5', '12345678', '用户5', 'https.gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户5', 'user'),
('user6', '12345678', '用户6', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户6', 'user'),
('user7', '12345678', '用户7', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户7', 'user'),
('user8', '12345678', '用户8', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我是用户8', 'user');


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

