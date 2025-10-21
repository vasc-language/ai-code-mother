# ERROR.md 问题修复指南

## 🔴 问题描述

**错误信息：**
```
java.sql.SQLSyntaxErrorException: Unknown column 'userId' in 'field list'
```

**触发场景：**
- 用户首次生成应用后，系统尝试发放奖励积分
- 查询 `user_points` 表时找不到 `userId` 字段

## 🔍 根本原因

数据库表结构与 Java 实体类不匹配：

| 位置 | 字段定义 | 问题 |
|------|---------|------|
| **Java 实体类** | `@Column("userId")` | ✅ 使用 `userId` |
| **数据库表** | 可能只有 `id` 字段 | ❌ 缺少 `userId` 字段 |

**可能原因：**
1. 使用了 `rebuild_points_system_from_scratch.sql`（该脚本用 `id` 直接作为用户ID）
2. 数据库表未正确初始化
3. 使用了旧版本的表结构

## ✅ 解决方案

### 方案 1：执行修复脚本（推荐）

```bash
# 1. 进入项目 SQL 目录
cd D:\Java\ai-code\ai-code-mother\sql

# 2. 执行修复脚本
mysql -u root -p ai_code_mother < fix_user_points_field_mismatch.sql
```

### 方案 2：使用批处理文件（Windows）

双击运行：
```
D:\Java\ai-code\ai-code-mother\fix_user_points_table.bat
```

### 方案 3：手动执行 SQL

```sql
USE ai_code_mother;

-- 1. 备份现有数据（如果有）
CREATE TABLE user_points_backup AS SELECT * FROM user_points;

-- 2. 删除旧表
DROP TABLE IF EXISTS user_points;

-- 3. 创建正确的表结构
CREATE TABLE `user_points`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `userId`           bigint      NOT NULL COMMENT '用户ID',
    `totalPoints`      int         DEFAULT 0 COMMENT '累计获得积分',
    `availablePoints`  int         DEFAULT 0 COMMENT '当前可用积分',
    `frozenPoints`     int         DEFAULT 0 COMMENT '冻结积分',
    `createTime`       datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`       datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`         tinyint     DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId` (`userId`),
    INDEX `idx_availablePoints` (`availablePoints`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';
```

## 📋 验证步骤

### 1. 检查表结构
```sql
USE ai_code_mother;
DESC user_points;
```

**期望输出：**
```
+------------------+----------+------+-----+-------------------+
| Field            | Type     | Null | Key | Default           |
+------------------+----------+------+-----+-------------------+
| id               | bigint   | NO   | PRI | NULL              |
| userId           | bigint   | NO   | UNI | NULL              | ✅ 必须有这个字段
| totalPoints      | int      | YES  |     | 0                 |
| availablePoints  | int      | YES  | MUL | 0                 |
| frozenPoints     | int      | YES  |     | 0                 |
| createTime       | datetime | YES  |     | CURRENT_TIMESTAMP |
| updateTime       | datetime | YES  |     | CURRENT_TIMESTAMP |
| isDelete         | tinyint  | YES  |     | 0                 |
+------------------+----------+------+-----+-------------------+
```

### 2. 测试查询
```sql
-- 应该能正常执行
SELECT id, userId, totalPoints, availablePoints FROM user_points LIMIT 1;
```

### 3. 重启应用
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. 测试首次生成
1. 注册新用户
2. 创建应用
3. 首次生成代码
4. 检查日志，应该看到：
```
用户 XXX 增加了 30 积分，类型：FIRST_GENERATE，原因：首次生成应用奖励
```

## 🔧 其他需要修复的表

如果遇到类似错误，可能还需要检查以下表：

### points_record 表
```sql
DESC points_record;
-- 必须有 userId 字段
```

### sign_in_record 表
```sql
DESC sign_in_record;
-- 必须有 userId 字段
```

### invite_record 表
```sql
DESC invite_record;
-- 必须有 inviterId 和 inviteeId 字段
```

## ⚠️ 注意事项

1. **数据备份：** 修复前务必备份数据
2. **应用停机：** 建议停止应用后再执行修复
3. **字段命名：** 确保使用驼峰命名 `userId`，不是下划线 `user_id`
4. **唯一索引：** `userId` 字段必须有唯一索引
5. **恢复数据：** 如果有旧数据，记得从备份表恢复

## 🚫 不要使用的脚本

**❌ 不要使用 `rebuild_points_system_from_scratch.sql`**

该脚本的表结构设计不正确：
```sql
-- 错误的设计
CREATE TABLE `user_points` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID',  -- ❌ 直接用 id 作为用户ID
    ...
```

**✅ 应该使用 `create_table.sql` 或 `fix_user_points_field_mismatch.sql`**

## 📖 相关文档

- **错误日志：** `ERROR.md`
- **建表脚本：** `sql/create_table.sql`
- **修复脚本：** `sql/fix_user_points_field_mismatch.sql`
- **积分系统设计：** `用户积分系统设计与实现.md`

## 🆘 如果问题仍未解决

### 检查 MyBatis-Flex 配置

确保配置了驼峰命名转换：
```yaml
# application.yml
mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
```

### 检查实体类注解

```java
@Table("user_points")
public class UserPoints {
    @Id
    private Long id;
    
    @Column("userId")  // ✅ 明确指定列名
    private Long userId;
    
    // ...
}
```

### 查看完整错误堆栈

```bash
tail -f logs/application.log | grep -A 20 "Unknown column"
```

---

**修复完成时间：** 2025-10-21  
**问题状态：** ✅ 已提供解决方案
