# AICodeHub Ubuntu 服务器部署指南

## 📋 目录

1. [部署前准备](#部署前准备)
2. [环境要求](#环境要求)
3. [快速部署](#快速部署)
4. [手动部署](#手动部署)
5. [配置说明](#配置说明)
6. [服务管理](#服务管理)
7. [故障排查](#故障排查)
8. [安全加固](#安全加固)
9. [性能优化](#性能优化)

---

## 部署前准备

### 1. 服务器要求

- **操作系统**: Ubuntu 20.04 LTS 或 22.04 LTS
- **CPU**: 2核心以上
- **内存**: 4GB 以上（推荐 8GB）
- **磁盘**: 20GB 可用空间以上
- **网络**: 公网 IP，80/443 端口可访问

### 2. 必需的外部服务

确保以下服务可用：

- **MySQL 8.0+**: 数据库服务
- **Redis 6.0+**: 缓存和会话存储
- **DeepSeek API**: AI 模型服务（需要 API Key）
- **阿里云 DashScope**: AI 模型服务（需要 API Key）
- **腾讯云 COS**: 对象存储服务（需要 SecretId/SecretKey）
- **SMTP 邮件服务**: 邮件发送（已配置QQ邮箱）

### 3. 环境检查

运行环境检查脚本：

```bash
cd deployment/scripts
bash check_env.sh
```

确保所有检查项通过后再继续部署。

---

## 环境要求

### 软件依赖清单

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| Java | 21 | 运行 Spring Boot 应用 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 会话存储和缓存 |
| Nginx | 1.18+ | 反向代理和静态文件服务 |
| Chrome/Chromium | 最新版 | 网页截图功能 |

---

## 快速部署

### 方式一：一键自动部署（推荐）

```bash
# 1. 上传部署包到服务器
scp -r deployment/ user@server:/tmp/

# 2. 连接到服务器
ssh user@server

# 3. 进入部署目录
cd /tmp/deployment/scripts

# 4. 赋予执行权限
chmod +x *.sh

# 5. 运行部署脚本
sudo bash deploy.sh
```

部署脚本会自动完成：
- ✓ 安装所有依赖软件
- ✓ 创建应用用户和目录
- ✓ 初始化数据库
- ✓ 部署后端和前端
- ✓ 配置 Nginx
- ✓ 启动服务
- ✓ 执行健康检查

### 方式二：分步部署

如果需要更细粒度的控制，可以分步执行：

```bash
# 1. 环境检查
bash check_env.sh

# 2. 数据库初始化
bash init_database.sh

# 3. 部署应用
sudo bash deploy.sh

# 4. 启动服务
bash service_manager.sh start
```

---

## 手动部署

如果自动部署失败，可以按以下步骤手动部署：

### 1. 安装依赖

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 21
sudo apt install -y openjdk-21-jdk

# 安装 MySQL
sudo apt install -y mysql-server
sudo mysql_secure_installation

# 安装 Redis
sudo apt install -y redis-server
sudo systemctl start redis
sudo systemctl enable redis

# 安装 Nginx
sudo apt install -y nginx

# 安装 Chromium
sudo apt install -y chromium-browser chromium-chromedriver
```

### 2. 创建应用用户

```bash
sudo useradd -r -m -s /bin/bash aicode
```

### 3. 创建目录

```bash
# 应用目录
sudo mkdir -p /var/app/aicodehub/{tmp/code_output,tmp/screenshots,logs}
sudo chown -R aicode:aicode /var/app/aicodehub

# 前端目录
sudo mkdir -p /var/www/aicodehub
sudo chown -R www-data:www-data /var/www/aicodehub
```

### 4. 初始化数据库

```bash
# 登录 MySQL
sudo mysql -u root -p

# 创建数据库
CREATE DATABASE ai_code_mother CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入表结构
USE ai_code_mother;
SOURCE /tmp/deployment/sql/create_table.sql;
SOURCE /tmp/deployment/sql/ai_model_config.sql;
SOURCE /tmp/deployment/sql/v1.1.0_ai_model_tier_system.sql;
SOURCE /tmp/deployment/sql/migration_email_login.sql;

# 退出
EXIT;
```

### 5. 部署后端

```bash
# 复制 JAR 文件
sudo cp deployment/backend/ai-code-mother-0.0.1-SNAPSHOT.jar /var/app/aicodehub/

# 安装 systemd 服务
sudo cp deployment/config/aicodehub.service /etc/systemd/system/
sudo systemctl daemon-reload
```

### 6. 部署前端

```bash
# 复制前端文件
sudo cp -r deployment/frontend/dist/* /var/www/aicodehub/
```

### 7. 配置 Nginx

```bash
# 复制配置文件
sudo cp deployment/config/nginx.conf /etc/nginx/sites-available/aicodehub

# 修改域名（重要！）
sudo nano /etc/nginx/sites-available/aicodehub
# 将 yourname.com 替换为你的实际域名

# 创建软链接
sudo ln -s /etc/nginx/sites-available/aicodehub /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 8. 启动服务

```bash
# 启动后端
sudo systemctl start aicodehub
sudo systemctl enable aicodehub

# 检查状态
sudo systemctl status aicodehub
```

### 9. 验证部署

```bash
# 检查后端健康
curl http://localhost:8080/api/health/

# 检查前端
curl http://localhost/

# 完整健康检查
cd /tmp/deployment/scripts
bash service_manager.sh health
```

---

## 配置说明

### application-prod.yml 配置项

后端配置文件位于 JAR 包内，关键配置项包括：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_code_mother
    username: root
    password: 你的密码  # 需要修改

  redis:
    host: localhost
    port: 6379
    password: 你的密码  # 如果有的话

# DeepSeek API
deepseek:
  api-key: sk-xxx  # 需要配置

# 阿里云 DashScope
dashscope:
  api-key: sk-xxx  # 需要配置

# 腾讯云 COS
cos:
  secret-id: xxx   # 需要配置
  secret-key: xxx  # 需要配置
```

### Nginx 配置修改

编辑 `/etc/nginx/sites-available/aicodehub`：

```nginx
# 修改域名
server_name yourname.com www.yourname.com;

# 修改前端目录（如果不同）
root /var/www/aicodehub/dist;

# 修改后端代理地址（如果不同）
proxy_pass http://localhost:8080/api/;
```

### Systemd 服务配置

编辑 `/etc/systemd/system/aicodehub.service`：

```ini
# 修改用户（如果不同）
User=aicode
Group=aicode

# 修改工作目录（如果不同）
WorkingDirectory=/var/app/aicodehub

# 修改 Java 路径（如果不同）
Environment="JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"

# 调整 JVM 内存（根据服务器配置）
Environment="JAVA_OPTS=-Xms2G -Xmx4G ..."
```

修改后重新加载：

```bash
sudo systemctl daemon-reload
sudo systemctl restart aicodehub
```

---

## 服务管理

### 使用服务管理脚本

```bash
cd deployment/scripts

# 启动服务
bash service_manager.sh start

# 停止服务
bash service_manager.sh stop

# 重启服务
bash service_manager.sh restart

# 查看状态
bash service_manager.sh status

# 查看实时日志
bash service_manager.sh logs

# 健康检查
bash service_manager.sh health
```

### 使用 systemctl 命令

```bash
# 启动
sudo systemctl start aicodehub

# 停止
sudo systemctl stop aicodehub

# 重启
sudo systemctl restart aicodehub

# 状态
sudo systemctl status aicodehub

# 开机自启
sudo systemctl enable aicodehub

# 禁用自启
sudo systemctl disable aicodehub
```

### 查看日志

```bash
# 实时日志
sudo journalctl -u aicodehub -f

# 最近 100 行
sudo journalctl -u aicodehub -n 100

# 今天的日志
sudo journalctl -u aicodehub --since today

# 指定时间范围
sudo journalctl -u aicodehub --since "2025-01-01" --until "2025-01-02"

# 应用日志文件
tail -f /var/app/aicodehub/logs/ai-code-mother.log
```

---

## 故障排查

### 常见问题

#### 1. 服务无法启动

```bash
# 查看错误日志
sudo journalctl -u aicodehub -n 100

# 检查端口占用
sudo netstat -tuln | grep 8080

# 检查 Java 版本
java -version

# 检查 JAR 文件权限
ls -l /var/app/aicodehub/ai-code-mother-0.0.1-SNAPSHOT.jar
```

#### 2. 数据库连接失败

```bash
# 检查 MySQL 状态
sudo systemctl status mysql

# 测试连接
mysql -h localhost -u root -p ai_code_mother

# 检查防火墙
sudo ufw status
```

#### 3. Redis 连接失败

```bash
# 检查 Redis 状态
sudo systemctl status redis

# 测试连接
redis-cli ping

# 查看 Redis 配置
sudo cat /etc/redis/redis.conf | grep bind
```

#### 4. Nginx 502 错误

```bash
# 检查后端是否运行
curl http://localhost:8080/api/health/

# 检查 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log

# 检查 Nginx 配置
sudo nginx -t
```

#### 5. 前端页面无法访问

```bash
# 检查 Nginx 状态
sudo systemctl status nginx

# 检查文件权限
ls -la /var/www/aicodehub/

# 检查 Nginx 访问日志
sudo tail -f /var/log/nginx/access.log
```

### 健康检查清单

| 检查项 | 命令 | 期望结果 |
|--------|------|----------|
| 后端服务 | `systemctl status aicodehub` | active (running) |
| 后端健康 | `curl http://localhost:8080/api/health/` | HTTP 200 |
| MySQL | `systemctl status mysql` | active (running) |
| Redis | `redis-cli ping` | PONG |
| Nginx | `systemctl status nginx` | active (running) |
| 前端访问 | `curl http://localhost/` | HTTP 200 |

---

## 安全加固

### 1. 配置防火墙

```bash
# 启用 UFW
sudo ufw enable

# 开放必要端口
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS

# 限制 8080 仅本地访问（后端端口）
sudo ufw deny 8080/tcp

# 查看规则
sudo ufw status
```

### 2. 配置 SSL/HTTPS

使用 Let's Encrypt 免费证书：

```bash
# 安装 Certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取证书（替换为你的域名）
sudo certbot --nginx -d yourname.com -d www.yourname.com

# 自动续期测试
sudo certbot renew --dry-run
```

### 3. 限制 API 文档访问

编辑 `/etc/nginx/sites-available/aicodehub`，添加：

```nginx
# 仅允许特定 IP 访问 API 文档
location /api/doc.html {
    allow 你的IP地址;
    deny all;
    proxy_pass http://localhost:8080/api/doc.html;
}
```

### 4. 配置 fail2ban（防止暴力破解）

```bash
# 安装 fail2ban
sudo apt install -y fail2ban

# 启用并启动
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

---

## 性能优化

### 1. JVM 调优

编辑 `/etc/systemd/system/aicodehub.service`：

```ini
# 根据服务器内存调整（示例：8GB内存服务器）
Environment="JAVA_OPTS=-Xms4G -Xmx4G \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/app/aicodehub/logs/heap_dump.hprof"
```

### 2. MySQL 优化

编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`：

```ini
[mysqld]
# 根据服务器内存调整
innodb_buffer_pool_size = 2G
max_connections = 200
query_cache_size = 64M
```

### 3. Redis 优化

编辑 `/etc/redis/redis.conf`：

```conf
# 设置最大内存
maxmemory 1gb
maxmemory-policy allkeys-lru

# 启用持久化
appendonly yes
```

### 4. Nginx 优化

编辑 `/etc/nginx/nginx.conf`：

```nginx
# 工作进程数（等于CPU核心数）
worker_processes auto;

# 每个进程的最大连接数
events {
    worker_connections 2048;
}

# 启用 gzip 压缩
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css application/json application/javascript;
```

---

## 附录

### 目录结构

```
deployment/
├── backend/                    # 后端文件
│   └── ai-code-mother-0.0.1-SNAPSHOT.jar
├── frontend/                   # 前端文件
│   └── dist/
├── config/                     # 配置文件
│   ├── nginx.conf
│   └── aicodehub.service
├── scripts/                    # 脚本文件
│   ├── deploy.sh              # 一键部署脚本
│   ├── check_env.sh           # 环境检查脚本
│   ├── init_database.sh       # 数据库初始化脚本
│   └── service_manager.sh     # 服务管理脚本
├── sql/                        # SQL 脚本
│   ├── create_table.sql
│   ├── ai_model_config.sql
│   ├── v1.1.0_ai_model_tier_system.sql
│   └── migration_email_login.sql
└── docs/                       # 文档
    └── DEPLOYMENT.md           # 本文档
```

### 默认账号

**管理员账号**：
- 用户名: `admin`
- 密码: `12345678`
- 角色: 管理员

**测试账号**：
- 用户名: `user1`
- 密码: `12345678`
- 角色: 普通用户

**⚠️ 重要**: 首次登录后请立即修改密码！

### 端口清单

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | HTTP |
| 443 | Nginx | HTTPS |
| 8080 | Spring Boot | 后端 API（仅内网） |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |

### 有用的链接

- **项目文档**: https://github.com/your-repo/ai-code-mother
- **Nginx 文档**: https://nginx.org/en/docs/
- **Spring Boot 文档**: https://spring.io/projects/spring-boot
- **Let's Encrypt**: https://letsencrypt.org/

---

## 技术支持

如遇到问题，请：

1. 查看本文档的"故障排查"章节
2. 查看应用日志: `sudo journalctl -u aicodehub -n 100`
3. 检查系统健康: `bash service_manager.sh health`
4. 提交 Issue 到 GitHub 仓库

---

**文档版本**: v1.0.0
**最后更新**: 2025-11-14
**适用版本**: AICodeHub v0.0.1-SNAPSHOT
