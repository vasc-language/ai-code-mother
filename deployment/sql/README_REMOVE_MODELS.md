# 删除指定模型 - 执行指南

## 📋 需要删除的模型

以下模型将从数据库中删除：
- `deepseek-v3.1-free` - DeepSeek V3.1 免费版
- `deepseek-v3.1` - DeepSeek V3.1 标准版
- `deepseek-v3.2` - DeepSeek V3.2 最新版
- `kimi-k2-free` - Kimi K2 免费版

## 🔍 检查结果

### 前端检查
✅ **主页 (HomePage.vue)**: 使用 `AiModelSelector` 组件，动态从后端获取模型列表  
✅ **应用生成页 (AppChatPage.old-optimized.vue)**: 使用 `AiModelSelector` 组件，动态从后端获取模型列表  
✅ **模型选择器 (AiModelSelector.vue)**: 通过 `listEnabledModels()` API 动态加载模型

**结论**: 前端无硬编码模型列表，删除数据库记录后自动生效

### 后端检查
✅ **Java代码**: 未发现对这4个模型的硬编码引用  
⚠️ **测试文件 (SimpleOpenAiApiTest.java)**: 引用了 `deepseek-v3`（不是我们要删除的模型，无影响）

**结论**: 后端无需修改代码

## 🚀 执行步骤

### 方法1: 使用MySQL命令行

```bash
# 1. 连接到MySQL
mysql -u root -p

# 2. 切换到数据库
USE ai_code_mother;

# 3. 查看即将删除的模型（确认）
SELECT id, model_key, model_name, provider, is_enabled 
FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);

# 4. 执行删除
DELETE FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);

# 5. 验证删除结果
SELECT COUNT(*) as deleted_count 
FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);
-- 应该返回 0

# 6. 查看剩余的启用模型
SELECT id, model_key, model_name, provider, is_enabled 
FROM ai_model_config 
WHERE is_enabled = 1 
ORDER BY sort_order
LIMIT 20;
```

### 方法2: 执行SQL脚本文件

```bash
# Windows
mysql -u root -p ai_code_mother < "D:\Java\ai-code\ai-code-mother\sql\remove_specific_models.sql"

# Linux/Mac
mysql -u root -p ai_code_mother < /path/to/sql/remove_specific_models.sql
```

### 方法3: 使用数据库管理工具

1. 打开 **Navicat** / **MySQL Workbench** / **DBeaver** 等工具
2. 连接到 `ai_code_mother` 数据库
3. 打开 `sql/remove_specific_models.sql` 文件
4. 执行整个脚本

## ✅ 验证删除成功

### 1. 数据库验证

```sql
-- 应该返回 0 条记录
SELECT * FROM ai_model_config 
WHERE model_key IN (
    'deepseek-v3.1-free',
    'deepseek-v3.1', 
    'deepseek-v3.2',
    'kimi-k2-free'
);
```

### 2. 前端验证

1. 重启后端服务（如果在运行）
   ```bash
   # 停止后端
   # 重新启动
   mvnw.cmd spring-boot:run  # Windows
   # 或
   ./mvnw spring-boot:run     # Linux/Mac
   ```

2. 刷新前端页面
   - 打开主页 `http://localhost:5173`
   - 点击输入框的 **+** 按钮
   - 查看模型列表，确认以下模型已消失：
     - ❌ DeepSeek V3.1 免费版
     - ❌ DeepSeek V3.1
     - ❌ DeepSeek V3.2
     - ❌ Kimi K2 免费版

3. 打开应用生成页面
   - 进入任意应用的生成页面
   - 点击左下角的模型选择器
   - 再次确认这4个模型不在列表中

## 📊 预期结果

### 删除前
```
总启用模型数: ~50-60 个
包含: deepseek-v3.1-free, deepseek-v3.1, deepseek-v3.2, kimi-k2-free
```

### 删除后
```
总启用模型数: ~46-56 个
不包含: deepseek-v3.1-free, deepseek-v3.1, deepseek-v3.2, kimi-k2-free
```

## ⚠️ 注意事项

1. **备份数据库**（可选）
   ```bash
   mysqldump -u root -p ai_code_mother > backup_before_delete.sql
   ```

2. **影响范围**
   - ✅ 不影响已生成的应用
   - ✅ 不影响历史聊天记录
   - ✅ 只影响新的模型选择

3. **恢复方法**（如果误删）
   - 重新运行 `sql/add_all_available_models.sql` 脚本
   - 或从备份恢复

## 📝 相关文件

- `sql/remove_specific_models.sql` - 删除SQL脚本
- `sql/add_all_available_models.sql` - 原始模型配置（如需恢复）
- `src/components/AiModelSelector.vue` - 前端模型选择器组件

## 🎯 完成检查清单

- [ ] 执行SQL删除语句
- [ ] 验证数据库中模型已删除
- [ ] 重启后端服务
- [ ] 刷新前端页面
- [ ] 确认主页模型列表已更新
- [ ] 确认应用生成页模型列表已更新
- [ ] 测试选择其他模型能否正常使用

---

**执行时间**: 建议在系统维护时间执行  
**预计耗时**: 1-2 分钟  
**风险评估**: 低风险（可恢复）
