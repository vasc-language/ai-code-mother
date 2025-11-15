#!/bin/bash
##############################################################################
# AICodeHub 数据备份脚本
# 功能：备份数据库、应用数据、配置文件到本地和云存储
# 使用方法: bash backup.sh [daily|weekly|manual]
##############################################################################

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

# 配置变量
BACKUP_TYPE=${1:-manual}  # daily, weekly, manual
BACKUP_ROOT="/backup/aicodehub"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="${BACKUP_ROOT}/${BACKUP_TYPE}/${TIMESTAMP}"

# 数据库配置
DB_NAME="ai_code_mother"
DB_USER="root"
DB_PASS=""  # 从环境变量或配置文件读取

# 应用目录
APP_DIR="/var/app/aicodehub"
FRONTEND_DIR="/var/www/aicodehub"

# 保留策略
DAILY_KEEP=7     # 保留最近 7 天的每日备份
WEEKLY_KEEP=4    # 保留最近 4 周的每周备份
MANUAL_KEEP=10   # 保留最近 10 个手动备份

# 创建备份目录
create_backup_dir() {
    log_info "创建备份目录..."
    mkdir -p "${BACKUP_DIR}"/{database,app,config,frontend}
}

# 备份数据库
backup_database() {
    log_info "备份数据库 $DB_NAME..."

    # 提示输入密码（如果未设置）
    if [ -z "$DB_PASS" ]; then
        read -sp "请输入 MySQL root 密码: " DB_PASS
        echo
    fi

    # 备份数据库
    mysqldump -u$DB_USER -p$DB_PASS \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        $DB_NAME | gzip > "${BACKUP_DIR}/database/${DB_NAME}_${TIMESTAMP}.sql.gz"

    if [ $? -eq 0 ]; then
        log_info "数据库备份成功: ${DB_NAME}_${TIMESTAMP}.sql.gz"

        # 获取备份文件大小
        BACKUP_SIZE=$(du -sh "${BACKUP_DIR}/database/${DB_NAME}_${TIMESTAMP}.sql.gz" | cut -f1)
        log_info "备份文件大小: $BACKUP_SIZE"
    else
        log_error "数据库备份失败"
        return 1
    fi
}

# 备份应用文件
backup_app_files() {
    log_info "备份应用文件..."

    # 备份 JAR 包
    if [ -f "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar" ]; then
        cp "${APP_DIR}/ai-code-mother-0.0.1-SNAPSHOT.jar" "${BACKUP_DIR}/app/"
        log_info "JAR 包备份成功"
    fi

    # 备份生成的代码（仅最近 7 天的）
    if [ -d "${APP_DIR}/tmp/code_output" ]; then
        find "${APP_DIR}/tmp/code_output" -type f -mtime -7 | \
            tar -czf "${BACKUP_DIR}/app/code_output_${TIMESTAMP}.tar.gz" -T -
        log_info "生成代码备份成功"
    fi

    # 备份日志（最近 3 天）
    if [ -d "${APP_DIR}/logs" ]; then
        find "${APP_DIR}/logs" -type f -mtime -3 | \
            tar -czf "${BACKUP_DIR}/app/logs_${TIMESTAMP}.tar.gz" -T -
        log_info "日志备份成功"
    fi
}

# 备份配置文件
backup_config() {
    log_info "备份配置文件..."

    # Nginx/OpenResty 配置
    if [ -f "/etc/nginx/sites-available/aicodehub" ]; then
        cp "/etc/nginx/sites-available/aicodehub" "${BACKUP_DIR}/config/nginx.conf"
    fi

    if [ -f "/usr/local/openresty/nginx/conf/vhost/aicodehub.conf" ]; then
        cp "/usr/local/openresty/nginx/conf/vhost/aicodehub.conf" "${BACKUP_DIR}/config/openresty.conf"
    fi

    # Systemd 服务
    if [ -f "/etc/systemd/system/aicodehub.service" ]; then
        cp "/etc/systemd/system/aicodehub.service" "${BACKUP_DIR}/config/"
    fi

    # 环境变量（如果有）
    if [ -f "${APP_DIR}/.env.prod" ]; then
        cp "${APP_DIR}/.env.prod" "${BACKUP_DIR}/config/"
        log_warn "已备份 .env.prod 文件（包含敏感信息），请妥善保管"
    fi

    log_info "配置文件备份成功"
}

# 备份前端文件（可选）
backup_frontend() {
    if [ "$BACKUP_TYPE" == "weekly" ] || [ "$BACKUP_TYPE" == "manual" ]; then
        log_info "备份前端文件..."
        tar -czf "${BACKUP_DIR}/frontend/dist_${TIMESTAMP}.tar.gz" -C "$FRONTEND_DIR" .
        log_info "前端文件备份成功"
    fi
}

# 上传到云存储（可选）
upload_to_cloud() {
    log_info "上传备份到云存储..."

    # 腾讯云 COS 示例（需要安装 coscmd）
    # coscmd upload -r "${BACKUP_DIR}" cos://your-bucket/backups/

    # 阿里云 OSS 示例（需要安装 ossutil）
    # ossutil cp -r "${BACKUP_DIR}" oss://your-bucket/backups/

    log_warn "云存储上传功能未配置，跳过"
}

# 清理旧备份
cleanup_old_backups() {
    log_info "清理旧备份..."

    case $BACKUP_TYPE in
        daily)
            find "${BACKUP_ROOT}/daily" -maxdepth 1 -type d -mtime +${DAILY_KEEP} -exec rm -rf {} \;
            log_info "已清理 ${DAILY_KEEP} 天前的每日备份"
            ;;
        weekly)
            find "${BACKUP_ROOT}/weekly" -maxdepth 1 -type d -mtime +$((WEEKLY_KEEP * 7)) -exec rm -rf {} \;
            log_info "已清理 ${WEEKLY_KEEP} 周前的每周备份"
            ;;
        manual)
            # 保留最近 N 个手动备份
            cd "${BACKUP_ROOT}/manual"
            ls -t | tail -n +$((MANUAL_KEEP + 1)) | xargs -r rm -rf
            log_info "已清理超过 ${MANUAL_KEEP} 个的手动备份"
            ;;
    esac
}

# 生成备份报告
generate_report() {
    log_info "生成备份报告..."

    REPORT_FILE="${BACKUP_DIR}/backup_report.txt"

    cat > "$REPORT_FILE" << EOF
AICodeHub 备份报告
====================
备份时间: $(date '+%Y-%m-%d %H:%M:%S')
备份类型: ${BACKUP_TYPE}
备份目录: ${BACKUP_DIR}

备份内容:
$(ls -lh "${BACKUP_DIR}"/*/)

磁盘使用:
$(du -sh "${BACKUP_DIR}")

数据库信息:
- 数据库名: ${DB_NAME}
- 表数量: $(mysql -u$DB_USER -p$DB_PASS -sse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME'")

备份状态: 成功 ✓
EOF

    cat "$REPORT_FILE"
}

# 主函数
main() {
    log_info "=========================================="
    log_info "开始 ${BACKUP_TYPE} 备份"
    log_info "=========================================="

    create_backup_dir
    backup_database
    backup_app_files
    backup_config
    backup_frontend
    # upload_to_cloud  # 取消注释以启用云存储
    cleanup_old_backups
    generate_report

    log_info "=========================================="
    log_info "备份完成！"
    log_info "备份位置: ${BACKUP_DIR}"
    log_info "=========================================="
}

# 执行主函数
main
