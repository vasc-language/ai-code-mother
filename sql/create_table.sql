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