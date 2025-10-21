# 数据库表结构与Java实体类字段对比分析

## 检查日期：2025-10-21

---

## 📊 总体结论

### ✅ 完全匹配的表（5个）
- `user_points` 
- `points_record`
- `sign_in_record`
- `ai_model_config`

### ⚠️ 有差异的表（1个）
- `invite_record` - **字段不匹配**

---

## 详细对比分析

### 1. ✅ user_points 表

| 数据库字段 | Java实体字段 | 类型匹配 | 状态 |
|-----------|-------------|---------|------|
| id | id | ✅ bigint → Long | 完全匹配 |
| userId | userId | ✅ bigint → Long | 完全匹配 |
| totalPoints | totalPoints | ✅ int → Integer | 完全匹配 |
| availablePoints | availablePoints | ✅ int → Integer | 完全匹配 |
| frozenPoints | frozenPoints | ✅ int → Integer | 完全匹配 |
| createTime | createTime | ✅ datetime → LocalDateTime | 完全匹配 |
| updateTime | updateTime | ✅ datetime → LocalDateTime | 完全匹配 |
| isDelete | isDelete | ✅ tinyint → Integer | 完全匹配 |

**结论：✅ 所有字段完全匹配，无需修改**

---

### 2. ✅ points_record 表

| 数据库字段 | Java实体字段 | 类型匹配 | 状态 |
|-----------|-------------|---------|------|
| id | id | ✅ bigint → Long | 完全匹配 |
| userId | userId | ✅ bigint → Long | 完全匹配 |
| points | points | ✅ int → Integer | 完全匹配 |
| balance | balance | ✅ int → Integer | 完全匹配 |
| type | type | ✅ varchar(50) → String | 完全匹配 |
| status | status | ✅ varchar(20) → String | 完全匹配 |
| reason | reason | ✅ varchar(255) → String | 完全匹配 |
| relatedId | relatedId | ✅ bigint → Long | 完全匹配 |
| model_key | modelKey | ✅ varchar(50) → String | 完全匹配 |
| token_count | tokenCount | ✅ int → Integer | 完全匹配 |
| expireTime | expireTime | ✅ datetime → LocalDateTime | 完全匹配 |
| expired_amount | expiredAmount | ✅ int → Integer | 完全匹配 |
| remaining_points | remainingPoints | ✅ int → Integer | 完全匹配 |
| actual_expire_time | actualExpireTime | ✅ datetime → LocalDateTime | 完全匹配 |
| createTime | createTime | ✅ datetime → LocalDateTime | 完全匹配 |
| isDelete | isDelete | ✅ tinyint → Integer | 完全匹配 |

**结论：✅ 所有字段完全匹配，包括新增的status、expired_amount、remaining_points、actual_expire_time字段**

---

### 3. ⚠️ sign_in_record 表

| 数据库字段 | Java实体字段 | 类型匹配 | 状态 |
|-----------|-------------|---------|------|
| id | id | ✅ bigint → Long | 完全匹配 |
| userId | userId | ✅ bigint → Long | 完全匹配 |
| signInDate | signInDate | ✅ date → LocalDate | 完全匹配 |
| **consecutiveDays** | **continuousDays** | ✅ int → Integer | ⚠️ **字段名不同** |
| pointsEarned | pointsEarned | ✅ int → Integer | 完全匹配 |
| createTime | createTime | ✅ datetime → LocalDateTime | 完全匹配 |
| - | isDelete | ❌ 数据库缺失 | ⚠️ **数据库缺失字段** |

**发现的问题：**
1. 数据库使用 `consecutiveDays`，Java使用 `continuousDays`
2. 数据库缺少 `isDelete` 字段

---

### 4. ❌ invite_record 表 - **严重不匹配**

| 数据库字段 | Java实体字段 | 类型匹配 | 状态 |
|-----------|-------------|---------|------|
| id | id | ✅ bigint → Long | 完全匹配 |
| inviterId | inviterId | ✅ bigint → Long | 完全匹配 |
| inviteeId | inviteeId | ✅ bigint → Long | 完全匹配 |
| inviteCode | inviteCode | ✅ varchar(50) → String | 完全匹配 |
| **inviterReward** | **inviterPoints** | ✅ int → Integer | ❌ **字段名不同** |
| **inviteeReward** | **inviteePoints** | ✅ int → Integer | ❌ **字段名不同** |
| status | status | ✅ varchar(20) → String | 完全匹配 |
| **firstGenerateTime** | - | - | ❌ **Java缺失** |
| createTime | createTime | ✅ datetime → LocalDateTime | 完全匹配 |
| updateTime | - | - | ❌ **Java缺失** |
| - | **registerIp** | - | ❌ **数据库缺失** |
| - | **deviceId** | - | ❌ **数据库缺失** |
| - | **registerTime** | - | ❌ **数据库缺失** |
| - | **rewardTime** | - | ❌ **数据库缺失** |
| - | **isDelete** | - | ❌ **数据库缺失** |

**严重问题：**
1. 字段命名不一致：`inviterReward` vs `inviterPoints`
2. 字段命名不一致：`inviteeReward` vs `inviteePoints`  
3. 数据库多了：`firstGenerateTime`, `updateTime`
4. Java多了：`registerIp`, `deviceId`, `registerTime`, `rewardTime`, `isDelete`

---

### 5. ✅ ai_model_config 表

| 数据库字段 | Java实体字段 | 类型匹配 | 状态 |
|-----------|-------------|---------|------|
| id | id | ✅ bigint → Long | 完全匹配 |
| model_key | modelKey | ✅ varchar(50) → String | 完全匹配 |
| model_name | modelName | ✅ varchar(100) → String | 完全匹配 |
| provider | provider | ✅ varchar(50) → String | 完全匹配 |
| base_url | baseUrl | ✅ varchar(255) → String | 完全匹配 |
| tier | tier | ✅ varchar(20) → String | 完全匹配 |
| points_per_k_token | pointsPerKToken | ✅ int → Integer | 完全匹配 |
| quality_score | qualityScore | ✅ decimal(3,2) → BigDecimal | 完全匹配 |
| success_rate | successRate | ✅ decimal(5,2) → BigDecimal | 完全匹配 |
| avg_token_usage | avgTokenUsage | ✅ int → Integer | 完全匹配 |
| user_rating | userRating | ✅ decimal(3,2) → BigDecimal | 完全匹配 |
| description | description | ✅ text → String | 完全匹配 |
| is_enabled | isEnabled | ✅ tinyint → Integer | 完全匹配 |
| sort_order | sortOrder | ✅ int → Integer | 完全匹配 |
| create_time | - | - | ⚠️ Java未声明 |
| update_time | - | - | ⚠️ Java未声明 |
| is_delete | - | - | ⚠️ Java未声明 |

**小问题：** Java实体类缺少 `createTime`, `updateTime`, `isDelete` 字段（但这些可能被基类继承处理）

---

## 🔴 严重问题总结

### 问题1：sign_in_record 字段名不一致

**数据库：**
```sql
consecutiveDays int
```

**Java：**
```java
@Column("continuousDays")
private Integer continuousDays;
```

**影响：** 可能导致查询错误

### 问题2：invite_record 表结构完全不匹配

**这是最严重的问题！** 数据库使用的是 `rebuild_points_system_from_scratch.sql` 的简化版本，而Java代码使用的是 `create_table.sql` 的完整版本。

---

## 🛠️ 修复方案

### 方案1：修改 sign_in_record 表（推荐）

```sql
-- 修复 sign_in_record
ALTER TABLE sign_in_record 
CHANGE COLUMN consecutiveDays continuousDays int DEFAULT 1 COMMENT '连续签到天数';

-- 添加缺失的 isDelete 字段
ALTER TABLE sign_in_record 
ADD COLUMN isDelete tinyint DEFAULT 0 COMMENT '逻辑删除';
```

### 方案2：重建 invite_record 表（必须执行）

```sql
-- 备份现有数据
CREATE TABLE invite_record_backup AS SELECT * FROM invite_record;

-- 删除旧表
DROP TABLE invite_record;

-- 创建正确的表结构（与Java实体类匹配）
CREATE TABLE `invite_record`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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

-- 迁移数据（如果需要）
INSERT INTO invite_record (id, inviterId, inviteeId, inviteCode, status, inviterPoints, inviteePoints, createTime)
SELECT id, inviterId, inviteeId, inviteCode, status, inviterReward, inviteeReward, createTime
FROM invite_record_backup;
```

---

## ✅ 验证步骤

执行修复后，运行以下SQL验证：

```sql
-- 1. 验证 sign_in_record
DESC sign_in_record;
-- 应该看到 continuousDays 和 isDelete

-- 2. 验证 invite_record
DESC invite_record;
-- 应该看到 inviterPoints, inviteePoints, registerIp, deviceId, registerTime, rewardTime, isDelete

-- 3. 测试查询
SELECT * FROM sign_in_record LIMIT 1;
SELECT * FROM invite_record LIMIT 1;
```

---

## 📋 修复优先级

1. **P0（立即修复）：** `invite_record` 表 - 完全不匹配，会导致邀请功能崩溃
2. **P1（高优先级）：** `sign_in_record.continuousDays` - 字段名不一致
3. **P2（建议修复）：** `sign_in_record.isDelete` - 缺失软删除字段

---

**生成时间：** 2025-10-21  
**数据库：** ai_code_mother  
**MySQL密码：** 212409
