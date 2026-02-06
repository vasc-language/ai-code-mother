# 数据库表全量梳理（2026-02-06）

## 1. 结论先说

- 你现在不是“几十套不同数据库”，而是 **同一套 SQL 被复制了两份目录**（`sql/` 与 `deployment/sql/`）。
- 这两份目录下的同名脚本当前 **内容完全一致**（逐文件 `sha256` 对比为 `SAME`）。
- 真正的混乱来自：**基线脚本 + 多个历史补丁脚本叠加**，其中有些补丁只适用于旧库，放到新库会报错或重复改表。

## 2. 证据范围

- 基线建表：`sql/create_table.sql`
- 迁移/补丁：`sql/migration_email_login.sql`、`sql/add_modelKey_to_app.sql`、`sql/v1.1.0_ai_model_tier_system.sql`、`sql/add_points_record_status.sql`、`sql/optimize_model_quality_multiplier.sql`、`sql/fix_user_points_field_mismatch.sql`、`sql/fix_all_field_mismatches.sql`
- 代码实体：`src/main/java/com/spring/aicodemother/model/entity/*.java`

## 3. 当前应保留的业务表（12 张）

以下是“按现有代码 + 迁移脚本收敛后”的目标表清单。

### 3.1 主业务表（10 张，代码实体直接使用）

1. `user`
列：`id`, `userAccount`, `userEmail`, `emailVerified`, `userPassword`, `userName`, `userAvatar`, `userProfile`, `userRole`, `createTime`, `updateTime`, `isDelete`

2. `app`
列：`id`, `appName`, `cover`, `initPrompt`, `codeGenType`, `modelKey`, `deployKey`, `deployedTime`, `priority`, `userId`, `editTime`, `createTime`, `updateTime`, `isDelete`

3. `chat_history`
列：`id`, `message`, `messageType`, `appId`, `userId`, `createTime`, `updateTime`, `isDelete`

4. `app_version`
列：`id`, `appId`, `versionNum`, `versionTag`, `codeContent`, `codeStorageUrl`, `deployKey`, `deployUrl`, `deployedTime`, `userId`, `remark`, `createTime`, `updateTime`, `isDelete`

5. `user_points`
列：`id`, `userId`, `totalPoints`, `availablePoints`, `frozenPoints`, `createTime`, `updateTime`, `isDelete`

6. `points_record`
列：`id`, `userId`, `points`, `balance`, `type`, `status`, `reason`, `relatedId`, `model_key`, `token_count`, `expireTime`, `expired_amount`, `remaining_points`, `actual_expire_time`, `createTime`, `isDelete`

7. `sign_in_record`
列：`id`, `userId`, `signInDate`, `continuousDays`, `pointsEarned`, `createTime`, `isDelete`

8. `invite_record`
列：`id`, `inviterId`, `inviteeId`, `inviteCode`, `registerIp`, `deviceId`, `status`, `inviterPoints`, `inviteePoints`, `createTime`, `registerTime`, `rewardTime`, `isDelete`

9. `email_verification_code`
列：`id`, `email`, `code`, `type`, `expireTime`, `verified`, `createTime`, `updateTime`, `isDelete`

10. `ai_model_config`
列：`id`, `model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `quality_score`, `success_rate`, `avg_token_usage`, `user_rating`, `description`, `is_enabled`, `is_delete`, `sort_order`, `create_time`, `update_time`

### 3.2 扩展统计表（2 张，脚本创建，当前代码未见实体直接映射）

11. `ai_model_quality_stats`
12. `user_model_rating`

## 4. 历史/临时/备份表（不应作为长期主表）

1. `invite_record_backup_20251021`（由 `sql/fix_all_field_mismatches.sql` 创建）
2. `user_points_backup`（由 `sql/fix_user_points_field_mismatch.sql` 创建）

说明：这两个表属于修复脚本副产物，应归档或清理，不建议在业务代码中依赖。

## 5. 明确冲突点（你现在觉得“乱”的核心来源）

1. 目录重复：
`sql/` 与 `deployment/sql/` 同名脚本重复维护（虽当前一致，但未来极易分叉）。

2. 基线与迁移重复：
`sql/create_table.sql` 已包含 `userEmail` / `emailVerified` / `email_verification_code`，而 `sql/migration_email_login.sql` 又重复 `ALTER TABLE user ADD COLUMN ...`。

3. `app.modelKey` 不是基线字段：
`sql/create_table.sql` 没有 `modelKey`，要靠 `sql/add_modelKey_to_app.sql` 补；但代码实体 `App` 已直接使用 `modelKey`。

4. 旧库修复脚本不可直接用于新库：
`sql/fix_all_field_mismatches.sql` 包含 `consecutiveDays -> continuousDays` 的改名、重建 `invite_record`；新库若已是新结构，直接执行会冲突。

5. `user` 表字段存在代码/脚本不一致风险：
实体 `User` 有 `editTime`，但 `sql/create_table.sql` 的 `user` 表定义未见该列。

6. 无效模板脚本混入：
`sql/ai_model_config.sql` 使用占位符 ``<table_name>``，不是可直接执行脚本。

7. 命名风格混用：
同库混用 camelCase（如 `createTime`）与 snake_case（如 `model_key`），迁移与 ORM 映射易出错。

## 6. 建议的“单一真相”执行顺序（新库初始化）

建议只保留一个目录为权威（推荐 `deployment/sql/`），并按下面顺序执行：

1. `create_table.sql`（建议先修订为最终版，直接包含 `app.modelKey`）
2. `v1.1.0_ai_model_tier_system.sql`
3. `add_points_record_status.sql`
4. `optimize_model_quality_multiplier.sql`（如果你要保留模型质量统计）
5. `restrict_to_deepseek_models.sql`（如果你要只保留 DeepSeek 两模型）

仅旧库修复时再按需执行（不要纳入新库基线）：

1. `migration_email_login.sql`
2. `add_modelKey_to_app.sql`
3. `fix_user_points_field_mismatch.sql`
4. `fix_all_field_mismatches.sql`
5. `add_codeContentUrl_to_app_version.sql`（当前是说明脚本，默认不执行）

## 7. 下一步治理建议（可直接落地）

1. 选定一个 SQL 目录为唯一来源，另一份删除或只保留只读镜像。
2. 新建一个“最终基线脚本”（例如 `V1__baseline.sql`），把必须字段一次性建全。
3. 旧库修复脚本统一归档到 `sql/legacy-patches/`，避免和新库初始化脚本混用。
4. 为每个迁移脚本加前置检查（`IF NOT EXISTS` / 信息架构检查），提升幂等性。
