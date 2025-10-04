#!/bin/bash

# =============================================
# AI Code Mother 快速启动脚本
# 用于本地开发或快速部署
# 作者: Join2049
# 日期: 2025-10-04
# =============================================

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 检查 Redis
if ! redis-cli ping &> /dev/null; then
    print_warn "Redis 未运行，正在启动..."
    redis-server --daemonize yes
fi

# 检查 MySQL
if ! pgrep -x mysqld > /dev/null; then
    print_warn "MySQL 未运行，请手动启动 MySQL"
fi

# 启动后端
print_info "启动后端服务..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod &

# 等待后端启动
sleep 10

# 启动前端
print_info "启动前端服务..."
cd ai-code-mother-frontend
npm run dev &

print_info "服务启动完成！"
echo "前端地址: http://localhost:5173"
echo "后端地址: http://localhost:8123/api"
