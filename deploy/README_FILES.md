# 部署文件说明

## 📁 文件清单

### 🔒 私有文件（不推送到 GitHub）

| 文件 | 说明 | 原因 |
|------|------|------|
| `README.md` | 包含真实配置的完整部署文档 | 包含服务器IP、数据库密码等敏感信息 |
| `deploy.sh` | 一键部署脚本 | 包含真实的数据库密码和服务器配置 |
| `backup.sh` | 备份脚本 | 包含真实的数据库密码 |

### ✅ 公开文件（可以推送到 GitHub）

| 文件 | 说明 | 状态 |
|------|------|------|
| `README_PUBLIC.md` | 脱敏的公开部署文档 | ✅ 所有敏感信息已替换为占位符 |
| `init_database.sql` | 数据库初始化脚本 | ✅ 仅包含表结构，无敏感数据 |
| `ai-code-mother.conf` | OpenResty 配置模板 | ⚠️ 域名已占位符化 |
| `ai-code-mother.service` | systemd 服务配置 | ✅ 通用配置，无敏感信息 |
| `start.sh` | 快速启动脚本 | ✅ 仅用于本地开发 |
| `DEPLOY_DOCS_GUIDE.md` | 部署文档使用指南 | ✅ 安全说明文档 |
| `README_FILES.md` | 本文件 | ✅ 文件清单说明 |

## 🔄 Git 推送前检查

### 1. 确认 .gitignore 配置

```bash
# 检查 .gitignore 是否正确配置
cat .gitignore | grep -A 3 "### CUSTOM ###"
```

应包含以下排除规则：
```
tasks/*.md
deploy/README.md
application-prod.yml
*.env
*.key
```

### 2. 验证敏感文件被忽略

```bash
# 应输出文件路径（表示被忽略）
git check-ignore deploy/README.md
git check-ignore deploy/deploy.sh  # ⚠️ 如果未在 .gitignore 中需手动添加
git check-ignore deploy/backup.sh  # ⚠️ 如果未在 .gitignore 中需手动添加
```

### 3. 检查暂存区

```bash
# 查看即将提交的文件
git status

# 检查是否包含敏感信息
git diff --cached | grep -E "(password|api-key|secret|<your-server-ip-pattern>|<your-password-pattern>)"
```

## 📝 推送步骤

### 步骤 1: 添加文件到 .gitignore

如果 `deploy.sh` 和 `backup.sh` 尚未被忽略，需要添加：

```bash
# 编辑 .gitignore
echo "# 部署脚本包含敏感配置" >> .gitignore
echo "deploy/deploy.sh" >> .gitignore
echo "deploy/backup.sh" >> .gitignore
```

### 步骤 2: 移除已追踪的敏感文件

如果这些文件之前已被 git 追踪：

```bash
# 从 git 追踪中移除（但保留本地文件）
git rm --cached deploy/README.md
git rm --cached deploy/deploy.sh
git rm --cached deploy/backup.sh

# 提交移除操作
git commit -m "chore: 移除包含敏感信息的部署文件"
```

### 步骤 3: 提交公开文件

```bash
# 添加公开文件
git add deploy/README_PUBLIC.md
git add deploy/DEPLOY_DOCS_GUIDE.md
git add deploy/README_FILES.md
git add deploy/init_database.sql
git add deploy/ai-code-mother.service
git add deploy/start.sh

# 提交
git commit -m "docs: 添加脱敏的公开部署文档"

# 推送
git push origin main
```

## ⚠️ 安全警告

### 已知敏感信息类型

以下**类型**的信息存在于私有文件中，**禁止推送**：

1. **服务器信息**
   - IP: `xxx.xxx.xxx.xxx`（示例格式）
   - 域名: `your-domain.com`

2. **数据库配置**
   - 主机: `<server-ip>:3306`
   - 用户: `<db-user>`
   - 密码: `<db-password>`

3. **Redis 配置**
   - 主机: `<server-ip>:6379`
   - 密码: `<redis-password>`

4. **API 密钥**
   - DeepSeek: `sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`（示例格式）
   - DashScope: `sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`（示例格式）

5. **COS 配置**
   - SecretId: `AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxx`（示例格式）
   - SecretKey: `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`（示例格式）

6. **邮箱配置**
   - 账号: `your-email@example.com`
   - 密码: `<email-password>`

⚠️ **注意**：以上仅为格式示例，真实配置信息存储在本地私有文件中，已被 .gitignore 排除。

### 如果意外推送了敏感信息

1. **立即删除远程提交**
   ```bash
   # 回退到之前的提交
   git reset --hard HEAD~1
   git push --force origin main
   ```

2. **清理历史记录**
   ```bash
   # 使用 git filter-branch 或 BFG Repo-Cleaner
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch deploy/README.md" \
     --prune-empty --tag-name-filter cat -- --all
   ```

3. **轮换所有密钥**
   - 更改数据库密码
   - 重新生成 API 密钥
   - 更新所有相关配置

## 📋 最终检查清单

推送前必须确认：

- [ ] `.gitignore` 包含所有敏感文件的排除规则
- [ ] `git status` 中没有敏感文件
- [ ] `git diff --cached` 中没有密码或密钥
- [ ] 所有占位符使用 `your-*` 格式
- [ ] 公开文档中不包含真实的 IP 地址
- [ ] 公开文档中不包含真实的域名
- [ ] 已经测试过公开文档的可用性

## 🔐 推荐做法

1. **使用环境变量**
   ```bash
   # 在服务器上设置环境变量
   export DB_PASSWORD="your_password"
   export REDIS_PASSWORD="your_password"
   ```

2. **使用密钥管理工具**
   - HashiCorp Vault
   - AWS Secrets Manager
   - Azure Key Vault

3. **配置文件加密**
   ```bash
   # 使用 git-crypt 加密敏感文件
   git-crypt init
   echo "deploy/README.md filter=git-crypt diff=git-crypt" >> .gitattributes
   ```

---

**记住**: 一旦敏感信息被推送到 GitHub，即使删除也可能被缓存。预防胜于补救！
