#!/bin/bash
##############################################################################
# AICodeHub 环境检查脚本
# 在部署前运行此脚本，检查服务器环境是否满足要求
# 使用方法: bash check_env.sh
##############################################################################

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0

check_pass() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASS_COUNT++))
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
    ((FAIL_COUNT++))
}

check_warn() {
    echo -e "${YELLOW}!${NC} $1"
    ((WARN_COUNT++))
}

echo "=========================================="
echo "AICodeHub 环境检查"
echo "=========================================="
echo

# 1. 操作系统检查
echo "1. 操作系统检查"
echo "----------------------------------------"
if [ -f /etc/os-release ]; then
    source /etc/os-release
    echo "操作系统: $PRETTY_NAME"
    if [[ "$ID" == "ubuntu" ]]; then
        check_pass "Ubuntu 系统"
    else
        check_warn "推荐使用 Ubuntu 20.04 或 22.04"
    fi
else
    check_fail "无法识别操作系统"
fi
echo

# 2. Java 环境检查
echo "2. Java 环境检查"
echo "----------------------------------------"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Java 版本: $JAVA_VERSION"
    if [[ "$JAVA_VERSION" == "21."* ]]; then
        check_pass "Java 21 已安装"
    else
        check_fail "需要 Java 21，当前版本: $JAVA_VERSION"
    fi
else
    check_fail "Java 未安装"
    echo "安装命令: sudo apt install -y openjdk-21-jdk"
fi
echo

# 3. MySQL 检查
echo "3. MySQL 检查"
echo "----------------------------------------"
if command -v mysql &> /dev/null; then
    MYSQL_VERSION=$(mysql --version | awk '{print $3}')
    echo "MySQL 版本: $MYSQL_VERSION"
    check_pass "MySQL 已安装"

    # 检查 MySQL 服务
    if systemctl is-active --quiet mysql; then
        check_pass "MySQL 服务运行中"
    else
        check_warn "MySQL 服务未运行"
        echo "启动命令: sudo systemctl start mysql"
    fi
else
    check_fail "MySQL 未安装"
    echo "安装命令: sudo apt install -y mysql-server"
fi
echo

# 4. Redis 检查
echo "4. Redis 检查"
echo "----------------------------------------"
if command -v redis-cli &> /dev/null; then
    REDIS_VERSION=$(redis-cli --version | awk '{print $2}')
    echo "Redis 版本: $REDIS_VERSION"
    check_pass "Redis 已安装"

    # 检查 Redis 服务
    if redis-cli ping &> /dev/null; then
        check_pass "Redis 服务运行中"
    else
        check_warn "Redis 服务未运行"
        echo "启动命令: sudo systemctl start redis"
    fi
else
    check_fail "Redis 未安装"
    echo "安装命令: sudo apt install -y redis-server"
fi
echo

# 5. Nginx 检查
echo "5. Nginx 检查"
echo "----------------------------------------"
if command -v nginx &> /dev/null; then
    NGINX_VERSION=$(nginx -v 2>&1 | awk -F'/' '{print $2}')
    echo "Nginx 版本: $NGINX_VERSION"
    check_pass "Nginx 已安装"

    # 检查 Nginx 服务
    if systemctl is-active --quiet nginx; then
        check_pass "Nginx 服务运行中"
    else
        check_warn "Nginx 服务未运行"
    fi
else
    check_fail "Nginx 未安装"
    echo "安装命令: sudo apt install -y nginx"
fi
echo

# 6. Chrome/Chromium 检查（用于截图功能）
echo "6. Chrome/Chromium 检查"
echo "----------------------------------------"
if command -v chromium-browser &> /dev/null || command -v google-chrome &> /dev/null; then
    check_pass "Chrome/Chromium 已安装"
else
    check_warn "Chrome/Chromium 未安装（网页截图功能需要）"
    echo "安装命令: sudo apt install -y chromium-browser chromium-chromedriver"
fi
echo

# 7. 端口检查
echo "7. 端口检查"
echo "----------------------------------------"
check_port() {
    if netstat -tuln 2>/dev/null | grep -q ":$1 "; then
        check_warn "端口 $1 已被占用"
        netstat -tuln | grep ":$1 "
    else
        check_pass "端口 $1 可用"
    fi
}

if command -v netstat &> /dev/null; then
    check_port 80
    check_port 443
    check_port 8123
    check_port 3306
    check_port 6379
else
    check_warn "netstat 未安装，无法检查端口"
    echo "安装命令: sudo apt install -y net-tools"
fi
echo

# 8. 磁盘空间检查
echo "8. 磁盘空间检查"
echo "----------------------------------------"
DISK_AVAIL=$(df -h / | awk 'NR==2 {print $4}')
DISK_AVAIL_GB=$(df -BG / | awk 'NR==2 {print $4}' | sed 's/G//')
echo "可用空间: $DISK_AVAIL"
if [ "$DISK_AVAIL_GB" -gt 20 ]; then
    check_pass "磁盘空间充足（建议 >20GB）"
else
    check_warn "磁盘空间不足（建议 >20GB）"
fi
echo

# 9. 内存检查
echo "9. 内存检查"
echo "----------------------------------------"
TOTAL_MEM=$(free -h | awk '/^Mem:/ {print $2}')
TOTAL_MEM_GB=$(free -m | awk '/^Mem:/ {print $2}')
TOTAL_MEM_GB=$((TOTAL_MEM_GB / 1024))
echo "总内存: $TOTAL_MEM"
if [ "$TOTAL_MEM_GB" -ge 4 ]; then
    check_pass "内存充足（建议 >=4GB）"
else
    check_warn "内存不足（建议 >=4GB）"
fi
echo

# 10. 防火墙检查
echo "10. 防火墙检查"
echo "----------------------------------------"
if command -v ufw &> /dev/null; then
    UFW_STATUS=$(ufw status | head -1)
    echo "$UFW_STATUS"
    if ufw status | grep -q "Status: active"; then
        check_pass "防火墙已启用"
        echo "确保以下端口已开放："
        echo "  - 80/tcp (HTTP)"
        echo "  - 443/tcp (HTTPS)"
        echo "开放命令: sudo ufw allow 80/tcp && sudo ufw allow 443/tcp"
    else
        check_warn "防火墙未启用"
    fi
else
    check_warn "UFW 未安装"
fi
echo

# 11. 系统权限检查
echo "11. 系统权限检查"
echo "----------------------------------------"
if [ "$EUID" -eq 0 ]; then
    check_pass "当前以 root 权限运行"
else
    check_warn "非 root 权限，部署时需要 sudo"
fi
echo

# 12. 必要目录检查
echo "12. 必要目录检查"
echo "----------------------------------------"
if [ -d "/var/www" ]; then
    check_pass "/var/www 目录存在"
else
    check_warn "/var/www 目录不存在"
    echo "创建命令: sudo mkdir -p /var/www"
fi

if [ -d "/var/app" ]; then
    check_pass "/var/app 目录存在"
else
    check_warn "/var/app 目录不存在"
    echo "创建命令: sudo mkdir -p /var/app"
fi
echo

# 13. 网络连接检查
echo "13. 网络连接检查"
echo "----------------------------------------"
echo "测试外部 API 连接..."

# DeepSeek API
if curl -s --connect-timeout 5 https://api.deepseek.com &> /dev/null; then
    check_pass "DeepSeek API 可访问"
else
    check_warn "DeepSeek API 无法访问"
fi

# GitHub (测试网络)
if curl -s --connect-timeout 5 https://github.com &> /dev/null; then
    check_pass "GitHub 可访问（网络正常）"
else
    check_warn "GitHub 无法访问（网络可能受限）"
fi
echo

# 总结
echo "=========================================="
echo "检查总结"
echo "=========================================="
echo -e "${GREEN}通过: $PASS_COUNT${NC}"
echo -e "${YELLOW}警告: $WARN_COUNT${NC}"
echo -e "${RED}失败: $FAIL_COUNT${NC}"
echo

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ 环境检查通过，可以继续部署${NC}"
    exit 0
else
    echo -e "${RED}✗ 存在必须解决的问题，请先修复后再部署${NC}"
    exit 1
fi
