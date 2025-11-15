# AICodeHub 部署包文件清单

> 自动生成时间: 2025-11-14

## 📦 包总览

- **总大小**: 147 MB
- **文件数量**: 50+ 文件
- **目录数量**: 6 个主目录

## 📂 目录结构

```
deployment/ (147 MB)
├── backend/ (143 MB)              # 后端应用
├── frontend/ (3.3 MB)             # 前端应用
├── config/ (8 KB)                 # 配置文件
├── scripts/ (32 KB)               # 部署脚本
├── sql/ (143 KB)                  # 数据库脚本
├── docs/ (32 KB)                  # 文档
└── README.md (8 KB)               # 部署包说明
```

---

## 📋 详细清单

### 1. 后端应用 (backend/) - 143 MB

| 文件名 | 大小 | 说明 |
|--------|------|------|
| ai-code-mother-0.0.1-SNAPSHOT.jar | 143 MB | Spring Boot 可执行 JAR 包 |

**包含内容**:
- Spring Boot 3.5.4 框架
- LangChain4j 1.1.0 AI 集成
- MyBatis-Flex 1.11.0 数据层
- 所有依赖库（Fat JAR）
- application-prod.yml 生产配置
- 系统提示词文件
- 静态资源

**运行要求**:
- Java 21
- 最小内存: 2GB
- 推荐内存: 4GB

---

### 2. 前端应用 (frontend/) - 3.3 MB

```
frontend/dist/
├── index.html (450 B)              # 入口文件
├── favicon.ico (7 KB)              # 网站图标
├── test-sse.html (6 KB)            # SSE 测试页面
└── assets/ (3.2 MB)                # 静态资源
    ├── JavaScript 文件 (2.9 MB)
    ├── CSS 文件 (200 KB)
    └── 图片/SVG (100 KB)
```

**技术栈**:
- Vue 3.5.17
- Ant Design Vue 4.2.6
- Vite 7.0.0 构建
- TypeScript

**主要文件**:
- `index-BNexabYU.js` (2.8 MB) - 主应用包
- `index-D05xbrQx.css` (187 KB) - 主样式
- 文档页面组件 (APIDoc, QuickStartDoc, FAQDoc等)

---

### 3. 配置文件 (config/) - 8 KB

| 文件名 | 大小 | 说明 |
|--------|------|------|
| nginx.conf | 4 KB | Nginx 反向代理配置 |
| aicodehub.service | 1 KB | Systemd 服务配置 |

**nginx.conf 关键配置**:
- 静态文件服务
- API 反向代理（/api/ → localhost:8080）
- SSE 流式传输支持
- HTTPS 配置模板
- 缓存策略

**aicodehub.service 关键配置**:
- 用户: aicode
- 工作目录: /var/app/aicodehub
- JVM 参数: -Xms2G -Xmx4G
- 自动重启策略

---

### 4. 部署脚本 (scripts/) - 32 KB

| 文件名 | 大小 | 权限 | 说明 |
|--------|------|------|------|
| deploy.sh | 8.2 KB | 755 | 一键部署脚本 |
| check_env.sh | 7.4 KB | 755 | 环境检查脚本 |
| init_database.sh | 3.6 KB | 755 | 数据库初始化脚本 |
| service_manager.sh | 4.3 KB | 755 | 服务管理脚本 |

**脚本功能**:

1. **deploy.sh**
   - 自动安装依赖（Java, MySQL, Redis, Nginx）
   - 创建用户和目录
   - 初始化数据库
   - 部署前后端
   - 配置 Nginx
   - 启动服务
   - 健康检查

2. **check_env.sh**
   - 操作系统检查
   - 软件依赖检查
   - 端口检查
   - 磁盘空间检查
   - 网络连接检查

3. **init_database.sh**
   - 创建数据库
   - 执行 SQL 脚本
   - 验证表结构
   - 显示初始账号

4. **service_manager.sh**
   - 启动/停止/重启服务
   - 查看服务状态
   - 实时日志查看
   - 健康检查

---

### 5. 数据库脚本 (sql/) - 143 KB

| 文件名 | 大小 | 说明 |
|--------|------|------|
| create_table.sql | 18 KB | 核心表结构（12张表） |
| ai_model_config.sql | 45 KB | AI模型配置数据 |
| v1.1.0_ai_model_tier_system.sql | 2 KB | 模型分级系统 |
| migration_email_login.sql | 1 KB | 邮箱登录支持 |
| add_24_models.sql | 30 KB | 额外模型配置 |
| add_all_available_models.sql | 40 KB | 完整模型列表 |
| 其他迁移脚本 | 7 KB | 数据库升级脚本 |

**核心数据表**:
1. user - 用户表（4个初始用户）
2. app - 应用表
3. chat_history - 对话历史
4. app_version - 版本管理
5. user_points - 积分系统
6. ai_model_config - AI模型配置
7. invite_record - 邀请记录
8. points_record - 积分记录
9. sign_in_record - 签到记录
10. generation_history - 生成历史
11. app_stats - 应用统计
12. system_config - 系统配置

---

### 6. 文档 (docs/) - 32 KB

| 文件名 | 大小 | 说明 |
|--------|------|------|
| DEPLOYMENT.md | 18 KB | 完整部署指南 |
| QUICK_REFERENCE.md | 7 KB | 快速参考手册 |
| DEPLOYMENT_CHECKLIST.md | 7 KB | 部署检查清单 |

**文档内容**:

1. **DEPLOYMENT.md** (完整部署指南)
   - 部署前准备
   - 环境要求
   - 快速部署
   - 手动部署
   - 配置说明
   - 服务管理
   - 故障排查
   - 安全加固
   - 性能优化

2. **QUICK_REFERENCE.md** (快速参考)
   - 一行命令
   - 重要目录
   - 常用命令
   - 监控命令
   - 安全检查
   - 故障排查
   - 配置修改
   - 运维操作

3. **DEPLOYMENT_CHECKLIST.md** (检查清单)
   - 部署前准备
   - 部署过程
   - 部署后验证
   - 安全加固
   - 性能优化
   - 监控和备份
   - 文档交接

---

### 7. 根目录文件

| 文件名 | 大小 | 说明 |
|--------|------|------|
| README.md | 8 KB | 部署包说明和快速开始 |

---

## ✅ 完整性检查

### 必需文件检查

- [x] 后端 JAR 包 (143 MB)
- [x] 前端 dist 目录 (3.3 MB)
- [x] Nginx 配置文件
- [x] Systemd 服务配置
- [x] 部署脚本 (4个)
- [x] SQL 脚本 (7个)
- [x] 文档文件 (3个)
- [x] README 文件

### 权限检查

所有脚本文件权限: **755 (rwxr-xr-x)**

```bash
-rwxr-xr-x  check_env.sh
-rwxr-xr-x  deploy.sh
-rwxr-xr-x  init_database.sh
-rwxr-xr-x  service_manager.sh
```

---

## 📊 磁盘空间需求

### 部署包本身
- **部署包**: 147 MB

### 服务器空间需求
- **应用目录**: 150 MB (JAR + 日志)
- **前端目录**: 5 MB (静态文件)
- **MySQL 数据**: 100 MB (初始)
- **Redis 数据**: 50 MB (初始)
- **临时文件**: 500 MB (代码生成)
- **日志文件**: 300 MB (轮转前)
- **系统软件**: 2 GB (Java, MySQL, Redis等)

**建议最小磁盘**: 20 GB
**推荐磁盘空间**: 50 GB+

---

## 🔐 敏感信息

**注意**: 以下配置包含敏感信息，已经在生产配置中硬编码：

1. **数据库密码** (application-prod.yml)
2. **Redis 密码** (如果有)
3. **DeepSeek API Key**
4. **DashScope API Key**
5. **腾讯云 COS 密钥** (SecretId/SecretKey)
6. **SMTP 密码**

**安全建议**: 部署后立即修改默认密码和密钥！

---

## 📦 打包建议

### 压缩命令
```bash
# 打包为 tar.gz (推荐)
tar -czf aicodehub-deployment-v1.0.tar.gz deployment/

# 查看压缩后大小
ls -lh aicodehub-deployment-v1.0.tar.gz
```

### 传输命令
```bash
# SCP 传输
scp aicodehub-deployment-v1.0.tar.gz user@server:/tmp/

# 服务器解压
tar -xzf aicodehub-deployment-v1.0.tar.gz
```

---

## 🎯 部署优先级

### P0 (必需)
1. ✅ 后端 JAR 包
2. ✅ 前端 dist 目录
3. ✅ SQL 初始化脚本
4. ✅ 部署脚本

### P1 (重要)
5. ✅ Nginx 配置
6. ✅ Systemd 配置
7. ✅ 部署文档

### P2 (可选)
8. ✅ 快速参考手册
9. ✅ 检查清单
10. ✅ 额外 SQL 脚本

---

## 📞 使用说明

1. **查看 README**: `cat deployment/README.md`
2. **环境检查**: `bash deployment/scripts/check_env.sh`
3. **一键部署**: `sudo bash deployment/scripts/deploy.sh`
4. **查看文档**: `deployment/docs/DEPLOYMENT.md`

---

**清单生成时间**: 2025-11-15
**最后构建时间**: 2025-11-15 12:50
**包版本**: v1.0.0
**应用版本**: v0.0.1-SNAPSHOT
