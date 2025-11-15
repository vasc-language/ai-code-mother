# AICodeHub 部署包

> Ubuntu 服务器一键部署方案

## 📦 包含内容

```
deployment/
├── backend/                     # 后端应用（143MB JAR包）
├── frontend/                    # 前端应用（Vue 3 构建产物）
├── config/                      # 配置文件
│   ├── nginx.conf              # Nginx 配置
│   └── aicodehub.service       # Systemd 服务配置
├── scripts/                     # 部署脚本
│   ├── deploy.sh               # 一键部署脚本
│   ├── check_env.sh            # 环境检查脚本
│   ├── init_database.sh        # 数据库初始化脚本
│   └── service_manager.sh      # 服务管理脚本
├── sql/                         # 数据库脚本
└── docs/                        # 文档
    ├── DEPLOYMENT.md           # 完整部署指南
    └── QUICK_REFERENCE.md      # 快速参考手册
```

## 🚀 快速开始

### 1. 上传部署包到服务器

```bash
scp -r deployment/ user@your-server:/tmp/
```

### 2. 连接到服务器

```bash
ssh user@your-server
```

### 3. 环境检查

```bash
cd /tmp/deployment/scripts
chmod +x *.sh
bash check_env.sh
```

### 4. 一键部署

```bash
sudo bash deploy.sh
```

部署完成后，访问 `http://你的服务器IP` 即可使用！

## 📋 环境要求

### 必需软件
- **操作系统**: Ubuntu 20.04/22.04 LTS
- **Java**: 21
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nginx**: 1.18+
- **Chrome/Chromium**: 用于截图功能

### 服务器配置
- **CPU**: 2核心+
- **内存**: 4GB+（推荐8GB）
- **磁盘**: 20GB+ 可用空间

### 外部服务（已配置）
- ✓ DeepSeek API
- ✓ 阿里云 DashScope
- ✓ 腾讯云 COS
- ✓ SMTP 邮件服务

## 📖 文档导航

### 新手部署
1. 阅读 [完整部署指南](docs/DEPLOYMENT.md)
2. 运行环境检查脚本
3. 执行一键部署脚本
4. 完成后续配置（域名、SSL等）

### 快速参考
- 常用命令: [快速参考手册](docs/QUICK_REFERENCE.md)
- 服务管理: `bash service_manager.sh [start|stop|restart|status|logs|health]`
- 故障排查: 见 [完整部署指南](docs/DEPLOYMENT.md#故障排查)

## 🔧 部署后配置

### 1. 修改域名
```bash
sudo nano /etc/nginx/sites-available/aicodehub
# 将 yourname.com 改为实际域名
sudo nginx -t && sudo systemctl reload nginx
```

### 2. 配置 SSL（可选但推荐）
```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourname.com -d www.yourname.com
```

### 3. 配置防火墙
```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 4. 修改默认密码
登录后台，使用默认账号 `admin/12345678`，立即修改密码！

## 🎯 服务管理

### 使用服务管理脚本（推荐）
```bash
cd /tmp/deployment/scripts

# 启动服务
bash service_manager.sh start

# 停止服务
bash service_manager.sh stop

# 重启服务
bash service_manager.sh restart

# 查看状态
bash service_manager.sh status

# 查看日志
bash service_manager.sh logs

# 健康检查
bash service_manager.sh health
```

### 使用 systemctl
```bash
sudo systemctl start aicodehub
sudo systemctl stop aicodehub
sudo systemctl restart aicodehub
sudo systemctl status aicodehub
```

## 🩺 健康检查

### 自动检查
```bash
bash scripts/service_manager.sh health
```

### 手动检查
```bash
# 后端健康
curl http://localhost:8080/api/health/

# 前端访问
curl http://localhost/

# MySQL
sudo systemctl status mysql

# Redis
redis-cli ping

# Nginx
sudo systemctl status nginx
```

## 📊 访问地址

部署成功后，通过以下地址访问：

- **前端首页**: http://你的IP 或 http://你的域名
- **API文档**: http://你的IP/api/doc.html
- **健康检查**: http://你的IP/api/health/

## 🔒 默认账号

**⚠️ 重要：首次登录后立即修改密码！**

- **管理员**: admin / 12345678
- **测试用户**: user1 / 12345678

## 🐛 常见问题

### 服务启动失败
```bash
# 查看错误日志
sudo journalctl -u aicodehub -n 100

# 检查端口占用
sudo netstat -tuln | grep 8080
```

### 502 Bad Gateway
```bash
# 检查后端是否运行
curl http://localhost:8080/api/health/

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 数据库连接失败
```bash
# 检查 MySQL 状态
sudo systemctl status mysql

# 测试数据库连接
mysql -u root -p ai_code_mother
```

完整故障排查指南请参考: [DEPLOYMENT.md](docs/DEPLOYMENT.md#故障排查)

## 📁 重要目录

| 目录 | 说明 |
|------|------|
| `/var/app/aicodehub/` | 应用主目录 |
| `/var/app/aicodehub/logs/` | 应用日志 |
| `/var/app/aicodehub/tmp/` | 生成的代码和截图 |
| `/var/www/aicodehub/` | 前端静态文件 |

## 🔄 更新应用

```bash
# 1. 停止服务
sudo systemctl stop aicodehub

# 2. 备份旧版本
sudo mv /var/app/aicodehub/ai-code-mother-0.0.1-SNAPSHOT.jar \
        /var/app/aicodehub/ai-code-mother.jar.bak

# 3. 上传新版本
sudo cp new-version.jar /var/app/aicodehub/ai-code-mother-0.0.1-SNAPSHOT.jar

# 4. 启动服务
sudo systemctl start aicodehub
```

## 📞 技术支持

- **完整文档**: [DEPLOYMENT.md](docs/DEPLOYMENT.md)
- **快速参考**: [QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md)
- **项目地址**: https://github.com/your-repo/ai-code-mother

## 📝 版本信息

- **应用版本**: v0.0.1-SNAPSHOT
- **部署包版本**: v1.0.0
- **发布日期**: 2025-11-14

---

**祝部署顺利！** 🎉

如有问题，请查看完整文档或提交 Issue。
