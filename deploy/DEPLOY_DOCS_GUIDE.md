# 部署文档说明

本目录包含两个部署文档：

## 📄 文档说明

### 1. README.md（生产环境专用 - 已排除）
- **用途**: 包含真实生产环境配置的完整部署文档
- **特点**: 包含实际的服务器IP、数据库密码、API密钥等敏感信息
- **位置**: `deploy/README.md`
- **状态**: 已在 `.gitignore` 中排除，不会被推送到 GitHub

### 2. README_PUBLIC.md（公开版本 - 已包含）
- **用途**: 用于 GitHub 公开仓库的脱敏部署文档
- **特点**: 所有敏感信息已替换为占位符（如 `your-*`）
- **位置**: `deploy/README_PUBLIC.md`
- **状态**: 可以安全推送到 GitHub 公开仓库

## 🔒 安全提示

### 敏感信息已脱敏：

| 类型 | 生产环境（保密） | 公开版本（占位符） |
|------|----------------|------------------|
| 服务器IP | `xxx.xxx.xxx.xxx`（已隐藏） | `<your-server-ip>` |
| 域名 | `example.com`（已隐藏） | `your-domain.com` |
| 数据库密码 | `********`（已隐藏） | `your_database_password` |
| Redis密码 | `********`（已隐藏） | `your_redis_password` |
| API密钥 | `sk-****`（已隐藏） | `sk-your-api-key` |
| COS配置 | `****`（已隐藏） | `your-*` 占位符 |
| 邮箱密码 | `********`（已隐藏） | `your_email_password` |

## 📋 使用指南

### 对于开发者（内部使用）
使用 `README.md`（包含真实配置）：
```bash
# 查看生产环境部署文档
cat deploy/README.md
```

### 对于开源贡献者（公开使用）
使用 `README_PUBLIC.md`（脱敏版本）：
```bash
# 查看公开部署文档
cat deploy/README_PUBLIC.md
```

## 🚀 推送到 GitHub

### 确认文件状态
```bash
# 检查哪些文件会被提交
git status

# 验证敏感文件已被忽略
git check-ignore deploy/README.md
# 应输出: deploy/README.md（表示已被忽略）

# 验证公开文件会被提交
git check-ignore deploy/README_PUBLIC.md
# 无输出（表示会被提交）
```

### 安全推送
```bash
# 添加文件
git add .

# 提交（公开版本）
git commit -m "docs: 添加公开版本的部署文档"

# 推送到 GitHub
git push origin main
```

## ⚠️ 重要警告

### 🚫 切勿执行以下操作：

1. **不要修改 .gitignore**
   - 不要移除 `deploy/README.md` 的排除规则
   - 不要移除 `application-prod.yml` 的排除规则

2. **不要强制添加敏感文件**
   ```bash
   # 危险！不要执行
   git add -f deploy/README.md
   git add -f src/main/resources/application-prod.yml
   ```

3. **不要在公开版本中填写真实信息**
   - `README_PUBLIC.md` 应始终使用占位符
   - 真实信息只应在 `README.md` 中

4. **定期检查泄露**
   ```bash
   # 检查是否有敏感信息即将提交
   git diff --cached | grep -E "(password|api-key|secret)"
   ```

## 🔄 更新流程

### 更新生产文档（README.md）
```bash
# 1. 编辑生产文档
vim deploy/README.md

# 2. 不会被 git 追踪，无需提交
git status  # 不会显示 README.md
```

### 更新公开文档（README_PUBLIC.md）
```bash
# 1. 编辑公开文档（使用占位符）
vim deploy/README_PUBLIC.md

# 2. 提交到 GitHub
git add deploy/README_PUBLIC.md
git commit -m "docs: 更新部署文档"
git push
```

## 📝 配置文件清单

### ✅ 可以推送到 GitHub（已包含）
- `README.md`（项目根目录）
- `deploy/README_PUBLIC.md`（公开部署文档）
- `CLAUDE.md`（项目说明）
- `deploy/init_database.sql`（数据库结构，不含敏感数据）
- `deploy/ai-code-mother.conf`（OpenResty配置模板，域名已占位符化）
- `deploy/ai-code-mother.service`（systemd服务模板）
- `deploy/deploy.sh`（需脱敏后推送）
- `deploy/backup.sh`（需脱敏后推送）

### 🚫 禁止推送（已排除）
- `deploy/README.md`（包含真实配置的部署文档）
- `tasks/todo.md`（可能包含敏感信息的任务清单）
- `src/main/resources/application-prod.yml`（生产配置）
- `ai-code-mother-frontend/.env.production`（前端生产配置）
- `tmp/`（临时文件和生成的代码）

## 🛡️ 安全最佳实践

1. **双重检查**
   - 提交前运行：`git diff --cached`
   - 推送前运行：`git log -p -1`

2. **使用环境变量**
   - 生产环境使用环境变量管理密钥
   - 避免硬编码敏感信息

3. **密钥管理**
   - 使用密钥管理系统（如 HashiCorp Vault）
   - 定期轮换密钥和密码

4. **访问控制**
   - 限制对生产配置文件的访问
   - 使用 SSH 密钥认证

---

**最后检查**: 推送前务必确认 `git status` 中没有任何包含敏感信息的文件！
