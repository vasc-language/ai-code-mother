-- 【说明】数据库中已存在 codeStorageUrl 字段，无需执行此脚本
-- 此脚本仅用于记录字段说明和历史

-- 现有字段说明：
-- codeContent: 代码内容（JSON格式）- 已废弃，大文件请使用 codeStorageUrl
-- codeStorageUrl: 代码内容存储URL（COS对象存储）- 用于解决大文件超过MySQL max_allowed_packet限制

-- 如果表中 codeStorageUrl 字段不存在，可执行以下SQL（通常已存在，无需执行）：
-- ALTER TABLE `app_version` 
-- ADD COLUMN `codeStorageUrl` VARCHAR(512) NULL COMMENT '代码内容存储URL（COS对象存储）' AFTER `codeContent`;

-- 查看表结构
DESC `app_version`;
