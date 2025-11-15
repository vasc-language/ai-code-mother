#!/bin/bash
##############################################################################
# AICodeHub 回滚脚本
# 功能：快速回滚到上一个稳定版本
# 使用方法: bash rollback.sh [backup_directory]
##############################################################################

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 配置
SERVICE_NAME="aicodehub"
APP_DIR="/var/app/aicodehub"
FRONTEND_DIR="/var/www/aicodehub"
BACKUP_ROOT="/backup/aicodehub"

# 显示可用备份
show_available_backups() {
    log_info "可用的备份列表:"
    echo
    echo "=========================================="
    echo "编号  备份时间              类型     大小"
    echo "=========================================="

    INDEX=1
    for TYPE in manual weekly daily; do
        if [ -d "${BACKUP_ROOT}/${TYPE}" ]; then
            for DIR in $(ls -t "${BACKUP_ROOT}/${TYPE}" 2>/dev/null); do
                SIZE=$(du -sh "${BACKUP_ROOT}/${TYPE}/${DIR}" 2>/dev/null | cut -f1)
                printf "%-6s%-20s%-10s%-10s\n" "$INDEX" "$DIR" "$TYPE" "$SIZE"
                BACKUP_DIRS[$INDEX]="${BACKUP_ROOT}/${TYPE}/${DIR}"
                ((INDEX++))
            done
        fi
    done
    echo "=========================================="
}

# 选择备份
select_backup() {
    if [ -n "$1" ]; then
        SELECTED_BACKUP="$1"
        log_info "使用指定的备份目录: $SELECTED_BACKUP"
    else
        declare -A BACKUP_DIRS
        show_available_backups

        echo
        read -p "请选择要回滚的备份编号 (或输入完整路径): " CHOICE

        if [[ "$CHOICE" =~ ^[0-9]+$ ]]; then
            SELECTED_BACKUP="${BACKUP_DIRS[$CHOICE]}"
        else
            SELECTED_BACKUP="$CHOICE"
        fi
    fi

    if [ ! -d "$SELECTED_BACKUP" ]; then
        log_error "备份目录不存在: $SELECTED_BACKUP"
        exit 1
    fi

    log_info "选择的备份: $SELECTED_BACKUP"
}

# 确认回滚
confirm_rollback() {
    echo
    log_warn "=== 警告 ==="
    echo "即将回滚到: $SELECTED_BACKUP"
    echo "当前应用将被替换！"
    echo
    read -p "确认要继续吗？(yes/no): " CONFIRM

    if [ "$CONFIRM" != "yes" ]; then
        log_info "回滚已取消"
        exit 0
    fi
}

# 停止服务
stop_service() {
    log_info "停止服务..."
    systemctl stop $SERVICE_NAME
    sleep 2
    log_info "服务已停止"
}

# 备份当前状态
backup_current() {
    log_info "备份当前状态（以防回滚失败）..."
    CURRENT_BACKUP="${BACKUP_ROOT}/rollback_$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$CURRENT_BACKUP"

    # 备份当前 JAR
    if [ -f "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar" ]; then
        cp "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar" "$CURRENT_BACKUP/"
    fi

    # 备份当前配置
    if [ -f "/etc/nginx/sites-available/aicodehub" ]; then
        cp "/etc/nginx/sites-available/aicodehub" "$CURRENT_BACKUP/nginx.conf"
    fi

    log_info "当前状态已备份到: $CURRENT_BACKUP"
}

# 恢复数据库
restore_database() {
    log_info "恢复数据库..."

    DB_BACKUP=$(find "$SELECTED_BACKUP/database" -name "*.sql.gz" | head -1)

    if [ -z "$DB_BACKUP" ]; then
        log_warn "未找到数据库备份文件，跳过数据库恢复"
        return
    fi

    read -p "确认要恢复数据库吗？当前数据将被覆盖！(yes/no): " CONFIRM_DB
    if [ "$CONFIRM_DB" != "yes" ]; then
        log_warn "跳过数据库恢复"
        return
    fi

    read -sp "请输入 MySQL root 密码: " DB_PASS
    echo

    # 恢复数据库
    gunzip < "$DB_BACKUP" | mysql -uroot -p$DB_PASS ai_code_mother

    if [ $? -eq 0 ]; then
        log_info "数据库恢复成功"
    else
        log_error "数据库恢复失败"
        exit 1
    fi
}

# 恢复应用文件
restore_app() {
    log_info "恢复应用文件..."

    # 恢复 JAR 包
    JAR_FILE=$(find "$SELECTED_BACKUP/app" -name "*.jar" | head -1)
    if [ -f "$JAR_FILE" ]; then
        cp "$JAR_FILE" "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar"
        chown aicode:aicode "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar"
        log_info "JAR 包恢复成功"
    else
        log_warn "未找到 JAR 包备份"
    fi
}

# 恢复配置文件
restore_config() {
    log_info "恢复配置文件..."

    # 恢复 Nginx 配置
    if [ -f "$SELECTED_BACKUP/config/nginx.conf" ]; then
        cp "$SELECTED_BACKUP/config/nginx.conf" /etc/nginx/sites-available/aicodehub
        nginx -t
        if [ $? -eq 0 ]; then
            systemctl reload nginx
            log_info "Nginx 配置恢复成功"
        else
            log_error "Nginx 配置测试失败"
        fi
    fi

    # 恢复 Systemd 服务配置
    if [ -f "$SELECTED_BACKUP/config/aicodehub.service" ]; then
        cp "$SELECTED_BACKUP/config/aicodehub.service" /etc/systemd/system/
        systemctl daemon-reload
        log_info "Systemd 配置恢复成功"
    fi

    # 恢复环境变量
    if [ -f "$SELECTED_BACKUP/config/.env.prod" ]; then
        cp "$SELECTED_BACKUP/config/.env.prod" "${APP_DIR}/"
        log_info "环境变量恢复成功"
    fi
}

# 恢复前端文件（可选）
restore_frontend() {
    if [ -f "$SELECTED_BACKUP/frontend/dist_*.tar.gz" ]; then
        read -p "是否恢复前端文件？(yes/no): " CONFIRM_FRONTEND
        if [ "$CONFIRM_FRONTEND" == "yes" ]; then
            log_info "恢复前端文件..."
            FRONTEND_BACKUP=$(find "$SELECTED_BACKUP/frontend" -name "dist_*.tar.gz" | head -1)
            rm -rf "${FRONTEND_DIR:?}"/*
            tar -xzf "$FRONTEND_BACKUP" -C "$FRONTEND_DIR"
            chown -R www-data:www-data "$FRONTEND_DIR"
            log_info "前端文件恢复成功"
        fi
    fi
}

# 启动服务
start_service() {
    log_info "启动服务..."
    systemctl start $SERVICE_NAME
    sleep 5

    # 检查服务状态
    if systemctl is-active --quiet $SERVICE_NAME; then
        log_info "服务启动成功"
    else
        log_error "服务启动失败"
        log_error "请查看日志: journalctl -u $SERVICE_NAME -n 50"
        exit 1
    fi
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    sleep 3

    # 检查后端
    if curl -f -s http://localhost:8123/api/health/ > /dev/null; then
        log_info "✓ 后端健康检查通过"
    else
        log_error "✗ 后端健康检查失败"
        return 1
    fi

    # 检查前端
    if curl -f -s http://localhost/ > /dev/null; then
        log_info "✓ 前端访问正常"
    else
        log_warn "✗ 前端访问失败"
    fi
}

# 显示回滚报告
show_report() {
    log_info "=========================================="
    log_info "回滚完成！"
    log_info "=========================================="
    echo
    echo "回滚信息:"
    echo "  - 备份来源: $SELECTED_BACKUP"
    echo "  - 当前备份: $CURRENT_BACKUP"
    echo "  - 回滚时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo
    echo "服务状态:"
    systemctl status $SERVICE_NAME --no-pager -l | head -10
    echo
    log_info "如果回滚失败，可以从以下位置恢复:"
    echo "  $CURRENT_BACKUP"
}

# 主函数
main() {
    log_info "=========================================="
    log_info "AICodeHub 回滚脚本"
    log_info "=========================================="
    echo

    # 检查权限
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 sudo 运行此脚本"
        exit 1
    fi

    select_backup "$1"
    confirm_rollback
    backup_current
    stop_service
    restore_database
    restore_app
    restore_config
    restore_frontend
    start_service
    health_check
    show_report
}

# 执行主函数
main "$@"
