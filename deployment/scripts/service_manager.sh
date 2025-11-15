#!/bin/bash
##############################################################################
# AICodeHub 服务管理脚本
# 提供启动、停止、重启、状态查看等功能
# 使用方法: bash service_manager.sh [start|stop|restart|status|logs]
##############################################################################

SERVICE_NAME="aicodehub"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

show_usage() {
    echo "用法: $0 {start|stop|restart|status|logs|health}"
    echo
    echo "命令:"
    echo "  start   - 启动服务"
    echo "  stop    - 停止服务"
    echo "  restart - 重启服务"
    echo "  status  - 查看服务状态"
    echo "  logs    - 查看实时日志"
    echo "  health  - 健康检查"
    exit 1
}

start_service() {
    echo -e "${GREEN}启动服务...${NC}"
    sudo systemctl start $SERVICE_NAME
    sleep 3
    if sudo systemctl is-active --quiet $SERVICE_NAME; then
        echo -e "${GREEN}✓ 服务启动成功${NC}"
        show_status
    else
        echo -e "${RED}✗ 服务启动失败${NC}"
        echo "查看日志: sudo journalctl -u $SERVICE_NAME -n 50"
        exit 1
    fi
}

stop_service() {
    echo -e "${YELLOW}停止服务...${NC}"
    sudo systemctl stop $SERVICE_NAME
    sleep 2
    if sudo systemctl is-active --quiet $SERVICE_NAME; then
        echo -e "${RED}✗ 服务停止失败${NC}"
        exit 1
    else
        echo -e "${GREEN}✓ 服务已停止${NC}"
    fi
}

restart_service() {
    echo -e "${YELLOW}重启服务...${NC}"
    sudo systemctl restart $SERVICE_NAME
    sleep 3
    if sudo systemctl is-active --quiet $SERVICE_NAME; then
        echo -e "${GREEN}✓ 服务重启成功${NC}"
        show_status
    else
        echo -e "${RED}✗ 服务重启失败${NC}"
        echo "查看日志: sudo journalctl -u $SERVICE_NAME -n 50"
        exit 1
    fi
}

show_status() {
    echo
    echo "=========================================="
    echo "服务状态"
    echo "=========================================="
    sudo systemctl status $SERVICE_NAME --no-pager -l
}

show_logs() {
    echo -e "${GREEN}查看实时日志 (Ctrl+C 退出)${NC}"
    sudo journalctl -u $SERVICE_NAME -f
}

health_check() {
    echo "=========================================="
    echo "健康检查"
    echo "=========================================="
    echo

    # 检查服务状态
    if sudo systemctl is-active --quiet $SERVICE_NAME; then
        echo -e "${GREEN}✓${NC} 服务运行中"
    else
        echo -e "${RED}✗${NC} 服务未运行"
        return 1
    fi

    # 检查后端 API
    echo -n "检查后端 API... "
    if curl -f -s http://localhost:8123/api/health/ > /dev/null; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi

    # 检查前端
    echo -n "检查前端... "
    if curl -f -s http://localhost/ > /dev/null; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi

    # 检查 MySQL
    echo -n "检查 MySQL... "
    if sudo systemctl is-active --quiet mysql; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi

    # 检查 Redis
    echo -n "检查 Redis... "
    if redis-cli ping &> /dev/null; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi

    # 检查 Nginx
    echo -n "检查 Nginx... "
    if sudo systemctl is-active --quiet nginx; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${RED}✗${NC}"
    fi

    echo
    echo "资源使用情况:"
    echo "----------------------------------------"

    # CPU 和内存
    if command -v ps &> /dev/null; then
        PID=$(sudo systemctl show -p MainPID $SERVICE_NAME | cut -d= -f2)
        if [ "$PID" != "0" ]; then
            ps -p $PID -o %cpu,%mem,rss,vsz,cmd --no-headers
        fi
    fi

    echo
    echo "端口监听:"
    echo "----------------------------------------"
    sudo netstat -tuln | grep -E ":8123|:80|:443|:3306|:6379"
}

# 主逻辑
case "$1" in
    start)
        start_service
        ;;
    stop)
        stop_service
        ;;
    restart)
        restart_service
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    health)
        health_check
        ;;
    *)
        show_usage
        ;;
esac
