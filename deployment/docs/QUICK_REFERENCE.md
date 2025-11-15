# AICodeHub 快速参考手册

## 🚀 一行命令

### 部署
```bash
sudo bash deployment/scripts/deploy.sh
```

### 服务管理
```bash
# 启动
sudo systemctl start aicodehub

# 停止
sudo systemctl stop aicodehub

# 重启
sudo systemctl restart aicodehub

# 状态
sudo systemctl status aicodehub

# 日志
sudo journalctl -u aicodehub -f
```

### 健康检查
```bash
# 后端
curl http://localhost:8080/api/health/

# 前端
curl http://localhost/

# 完整检查
bash deployment/scripts/service_manager.sh health
```

---

## 📂 重要目录

| 路径 | 用途 |
|------|------|
| `/var/app/aicodehub/` | 应用主目录 |
| `/var/app/aicodehub/logs/` | 应用日志 |
| `/var/app/aicodehub/tmp/` | 临时文件和生成代码 |
| `/var/www/aicodehub/` | 前端静态文件 |
| `/etc/nginx/sites-available/aicodehub` | Nginx 配置 |
| `/etc/systemd/system/aicodehub.service` | Systemd 服务配置 |

---

## 🔧 常用命令

### Nginx
```bash
# 测试配置
sudo nginx -t

# 重载配置
sudo nginx -s reload

# 重启
sudo systemctl restart nginx

# 访问日志
sudo tail -f /var/log/nginx/access.log

# 错误日志
sudo tail -f /var/log/nginx/error.log
```

### MySQL
```bash
# 登录
sudo mysql -u root -p ai_code_mother

# 备份
mysqldump -u root -p ai_code_mother > backup.sql

# 恢复
mysql -u root -p ai_code_mother < backup.sql

# 查看连接
mysql -u root -p -e "SHOW PROCESSLIST;"
```

### Redis
```bash
# 测试连接
redis-cli ping

# 查看信息
redis-cli info

# 清空缓存
redis-cli FLUSHALL

# 监控
redis-cli monitor
```

---

## 📊 监控命令

### 系统资源
```bash
# CPU 和内存
top

# 磁盘使用
df -h

# 目录大小
du -sh /var/app/aicodehub/*

# 网络连接
netstat -tuln
```

### 应用状态
```bash
# Java 进程
ps aux | grep java

# 端口监听
sudo netstat -tuln | grep -E ':8080|:80|:443'

# 内存使用
sudo systemctl show -p MainPID aicodehub | cut -d= -f2 | xargs ps -o pid,%cpu,%mem,rss,cmd -p
```

---

## 🔒 安全检查

### 防火墙
```bash
# 状态
sudo ufw status

# 开放端口
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 禁止端口
sudo ufw deny 8080/tcp
```

### SSL 证书
```bash
# 获取证书
sudo certbot --nginx -d yourname.com

# 续期
sudo certbot renew

# 测试续期
sudo certbot renew --dry-run
```

---

## 🐛 快速故障排查

### 服务启动失败
```bash
# 1. 查看错误日志
sudo journalctl -u aicodehub -n 50

# 2. 检查 Java 版本
java -version

# 3. 检查端口占用
sudo netstat -tuln | grep 8080

# 4. 检查文件权限
ls -l /var/app/aicodehub/
```

### 502 Bad Gateway
```bash
# 1. 检查后端是否运行
curl http://localhost:8080/api/health/

# 2. 检查 Nginx 配置
sudo nginx -t

# 3. 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 数据库连接失败
```bash
# 1. 检查 MySQL 状态
sudo systemctl status mysql

# 2. 测试连接
mysql -h localhost -u root -p ai_code_mother

# 3. 查看错误日志
sudo tail -f /var/log/mysql/error.log
```

### Redis 连接失败
```bash
# 1. 检查 Redis 状态
sudo systemctl status redis

# 2. 测试连接
redis-cli ping

# 3. 查看配置
cat /etc/redis/redis.conf | grep bind
```

---

## 📝 配置文件快速修改

### 修改 Nginx 域名
```bash
sudo nano /etc/nginx/sites-available/aicodehub
# 找到 server_name 并修改
sudo nginx -t && sudo systemctl reload nginx
```

### 修改 JVM 内存
```bash
sudo nano /etc/systemd/system/aicodehub.service
# 找到 JAVA_OPTS 并修改 -Xms -Xmx
sudo systemctl daemon-reload
sudo systemctl restart aicodehub
```

### 修改数据库密码
```bash
# 1. 修改 MySQL 密码
sudo mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';

# 2. 更新应用配置（需要重新打包JAR）
```

---

## 🔄 常见运维操作

### 更新应用
```bash
# 1. 停止服务
sudo systemctl stop aicodehub

# 2. 备份旧版本
mv /var/app/aicodehub/ai-code-mother-0.0.1-SNAPSHOT.jar /var/app/aicodehub/ai-code-mother-old.jar

# 3. 上传新版本
scp ai-code-mother-new.jar user@server:/var/app/aicodehub/ai-code-mother-0.0.1-SNAPSHOT.jar

# 4. 启动服务
sudo systemctl start aicodehub

# 5. 检查状态
sudo systemctl status aicodehub
```

### 清理临时文件
```bash
# 清理生成的代码文件（30天前的）
find /var/app/aicodehub/tmp/code_output -type f -mtime +30 -delete

# 清理截图文件（30天前的）
find /var/app/aicodehub/tmp/screenshots -type f -mtime +30 -delete

# 清理日志文件（根据配置自动轮转）
```

### 数据库备份
```bash
# 手动备份
mysqldump -u root -p ai_code_mother > /backup/ai_code_mother_$(date +%Y%m%d).sql

# 自动备份（添加到 crontab）
0 2 * * * mysqldump -u root -pYOUR_PASSWORD ai_code_mother > /backup/ai_code_mother_$(date +\%Y\%m\%d).sql
```

---

## 📞 默认配置

### 应用配置
- **后端端口**: 8080
- **上下文路径**: /api
- **环境**: prod

### 数据库
- **主机**: localhost
- **端口**: 3306
- **数据库**: ai_code_mother
- **用户**: root

### Redis
- **主机**: localhost
- **端口**: 6379

### 默认账号
- **管理员**: admin / 12345678
- **用户**: user1 / 12345678

---

## 🌐 访问地址

### 生产环境
- **前端**: http://你的域名
- **API**: http://你的域名/api/
- **API文档**: http://你的域名/api/doc.html
- **健康检查**: http://你的域名/api/health/
- **Prometheus**: http://你的域名/api/actuator/prometheus

### 本地调试
- **后端直接访问**: http://localhost:8080/api/
- **Nginx 代理访问**: http://localhost/api/

---

## 💡 性能调优参考

### JVM 参数（8GB 内存服务器）
```ini
-Xms4G -Xmx4G
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

### MySQL 参数
```ini
innodb_buffer_pool_size = 2G
max_connections = 200
```

### Redis 参数
```conf
maxmemory 1gb
maxmemory-policy allkeys-lru
```

---

**快速帮助**: 查看完整文档 `deployment/docs/DEPLOYMENT.md`
