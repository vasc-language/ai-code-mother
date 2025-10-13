# 删除指定AI模型 - 完整总结

## 📋 任务目标

从系统中删除以下 DeepSeek 和 Kimi 模型：
- ❌ `deepseek-v3.1-free` - DeepSeek V3.1 免费版
- ❌ `deepseek-v3.1` - DeepSeek V3.1 标准版
- ❌ `deepseek-v3.2` - DeepSeek V3.2 最新版
- ❌ `kimi-k2-free` - Kimi K2 免费版

## ✅ 已完成的检查

### 1. 前端代码检查 ✅

**检查位置**:
- ✅ `src/components/AiModelSelector.vue` - 模型选择器组件
- ✅ `src/pages/HomePage.vue` - 主页
- ✅ `src/pages/app/AppChatPage.old-optimized.vue` - 应用生成页面

**检查结果**:
```
✓ 所有页面都使用 <AiModelSelector> 组件
✓ 模型列表通过 API 动态加载: listEnabledModels()
✓ 无硬编码的模型列表
✓ 删除数据库记录后自动生效，无需修改前端代码
```

**图标匹配逻辑**（保留）:
```typescript
// 这些是动态判断逻辑，不是硬编码模型
if (modelKey.includes('deepseek')) { ... }
else if (modelKey.includes('kimi')) { ... }
```
→ 删除模型后，这些逻辑仍然有效，用于动态匹配其他 deepseek/kimi 模型

### 2. 后端代码检查 ✅

**检查位置**:
- ✅ 所有 Java 源文件
- ✅ 配置文件
- ✅ 测试文件

**检查结果**:
```
✓ 未发现对这4个模型的硬编码引用
✓ 唯一发现: SimpleOpenAiApiTest.java 引用 "deepseek-v3"
  → 不是我们要删除的模型，无影响
✓ 无需修改后端代码
```

### 3. 数据库配置检查 ✅

**位置**: `sql/add_all_available_models.sql`

**找到的记录**:
```sql
-- Line 48
('deepseek-v3.1-free', 'DeepSeek V3.1 免费版', 'openrouter', ..., 1, 301),

-- Line 49
('deepseek-v3.1', 'DeepSeek V3.1', 'iflow', ..., 1, 302),

-- Line 50
('deepseek-v3.2', 'DeepSeek V3.2', 'iflow', ..., 1, 303),

-- Line 58
('kimi-k2-free', 'Kimi K2 免费版', 'openrouter', ..., 1, 402);
```

## 📦 已创建的文件

### 1. SQL 删除脚本
**文件**: `sql/remove_specific_models.sql`
```sql
-- 包含查询、删除、验证三步操作
-- 可安全执行，支持回滚
```

### 2. 执行指南
**文件**: `sql/README_REMOVE_MODELS.md`
```markdown
- 详细的执行步骤
- 多种执行方法
- 验证检查清单
- 恢复方法
```

### 3. 自动执行脚本
**文件**: `sql/execute_remove_models.bat`
```batch
- 双击运行
- 输入MySQL密码
- 自动执行删除
- 显示执行结果
```

## 🚀 执行方法（3选1）

### 方法 1: 双击批处理脚本 ⭐ 推荐

```bash
# Windows 用户推荐
1. 双击: sql/execute_remove_models.bat
2. 输入 MySQL 密码
3. 等待执行完成
```

### 方法 2: MySQL 命令行

```bash
# 1. 连接MySQL
mysql -u root -p

# 2. 切换数据库
USE ai_code_mother;

# 3. 执行删除
DELETE FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);

# 4. 确认结果
SELECT COUNT(*) FROM ai_model_config 
WHERE model_key IN ('deepseek-v3.1-free', 'deepseek-v3.1', 'deepseek-v3.2', 'kimi-k2-free');
-- 应该返回: 0
```

### 方法 3: 数据库管理工具

```
1. 打开 Navicat / MySQL Workbench / DBeaver
2. 连接到 ai_code_mother 数据库
3. 打开 sql/remove_specific_models.sql
4. 执行整个脚本
```

## ✅ 验证步骤

### 1. 数据库验证

```sql
-- 查询应该返回 0 条记录
SELECT * FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);
```

### 2. 后端验证

```bash
# 重启后端服务（如果在运行）
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 3. 前端验证

1. 刷新浏览器: `http://localhost:5173`

2. **主页测试**:
   ```
   → 点击输入框
   → 点击左侧 + 按钮
   → 查看模型列表
   → 确认4个模型已消失
   ```

3. **应用生成页测试**:
   ```
   → 进入任意应用
   → 点击左下角模型选择器
   → 确认4个模型已消失
   ```

## 📊 预期结果

### Before (删除前)
```
总启用模型: ~55 个
包含:
  ✓ deepseek-v3.1-free
  ✓ deepseek-v3.1
  ✓ deepseek-v3.2
  ✓ kimi-k2-free
```

### After (删除后)
```
总启用模型: ~51 个
不包含:
  ✗ deepseek-v3.1-free
  ✗ deepseek-v3.1
  ✗ deepseek-v3.2
  ✗ kimi-k2-free
```

## 🎯 检查清单

- [ ] **Step 1**: 执行SQL删除（3种方法选1）
- [ ] **Step 2**: 验证数据库记录已删除
- [ ] **Step 3**: 重启后端服务
- [ ] **Step 4**: 刷新前端页面
- [ ] **Step 5**: 测试主页模型列表
- [ ] **Step 6**: 测试应用生成页模型列表
- [ ] **Step 7**: 选择其他模型测试功能

## ⚠️ 注意事项

### 安全性
- ✅ 操作不影响已生成的应用
- ✅ 操作不影响历史聊天记录
- ✅ 操作仅删除模型配置记录
- ✅ 可通过重新运行 `add_all_available_models.sql` 恢复

### 建议
1. **备份数据库**（可选）:
   ```bash
   mysqldump -u root -p ai_code_mother > backup_$(date +%Y%m%d).sql
   ```

2. **维护窗口执行**: 建议在系统维护时间执行

3. **团队通知**: 执行前通知团队成员

## 📁 相关文件

```
sql/
├── remove_specific_models.sql      # SQL删除脚本
├── execute_remove_models.bat       # Windows自动执行脚本
├── README_REMOVE_MODELS.md         # 详细执行指南
└── add_all_available_models.sql    # 原始配置（恢复用）

ai-code-mother-frontend/src/
├── components/AiModelSelector.vue  # 模型选择器（已检查✓）
├── pages/HomePage.vue              # 主页（已检查✓）
└── pages/app/AppChatPage.old-optimized.vue  # 应用页（已检查✓）
```

## 🔄 恢复方法（如果需要）

如果误删或需要恢复：

```bash
# 方法1: 重新运行原始SQL
mysql -u root -p ai_code_mother < sql/add_all_available_models.sql

# 方法2: 单独插入（需要复制INSERT语句）
# 从 add_all_available_models.sql 中复制对应的 INSERT 语句
```

## 📞 支持

如有问题，请查看：
1. `sql/README_REMOVE_MODELS.md` - 详细指南
2. 本文档 - 完整总结
3. 错误日志 - 后端日志

---

**创建时间**: 2025-01-XX  
**执行时间**: 待执行  
**预计耗时**: 1-2 分钟  
**风险评估**: 🟢 低风险（可恢复）
