#!/bin/bash
##############################################################################
# AICodeHub 数据库初始化脚本
# 自动执行所有 SQL 脚本
# 使用方法: bash init_database.sh
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

# 数据库配置
DB_NAME="ai_code_mother"
DB_HOST="localhost"
DB_PORT="3306"

echo "=========================================="
echo "AICodeHub 数据库初始化"
echo "=========================================="
echo

# 获取数据库凭据
read -p "MySQL 用户名 [root]: " DB_USER
DB_USER=${DB_USER:-root}

read -sp "MySQL 密码: " DB_PASS
echo
echo

# 测试数据库连接
log_info "测试数据库连接..."
if mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT 1" &>/dev/null; then
    log_info "数据库连接成功"
else
    log_error "数据库连接失败，请检查用户名和密码"
    exit 1
fi

# 检查数据库是否存在
if mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "USE $DB_NAME" 2>/dev/null; then
    log_warn "数据库 $DB_NAME 已存在"
    echo
    read -p "是否删除并重新创建数据库？这将删除所有现有数据！(yes/no): " confirm
    if [ "$confirm" == "yes" ]; then
        log_warn "删除现有数据库..."
        mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "DROP DATABASE $DB_NAME"
    else
        log_info "保留现有数据库，仅执行必要的更新脚本"
        KEEP_EXISTING=true
    fi
fi

# 创建数据库
if [ "$KEEP_EXISTING" != true ]; then
    log_info "创建数据库 $DB_NAME..."
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS << EOF
CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
    log_info "数据库创建成功"
fi

# SQL 脚本执行顺序
SQL_SCRIPTS=(
    "create_table.sql"
    "ai_model_config.sql"
    "v1.1.0_ai_model_tier_system.sql"
    "migration_email_login.sql"
)

# 执行 SQL 脚本
echo
log_info "开始执行 SQL 脚本..."
cd ../sql

for script in "${SQL_SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        log_info "执行: $script"
        if mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < $script; then
            echo -e "${GREEN}  ✓${NC} $script 执行成功"
        else
            echo -e "${RED}  ✗${NC} $script 执行失败"
            exit 1
        fi
    else
        log_warn "脚本不存在: $script"
    fi
done

# 验证数据库
echo
log_info "验证数据库..."
TABLE_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -sse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME'")
log_info "共创建 $TABLE_COUNT 张表"

# 显示表列表
echo
log_info "数据表列表:"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES"

# 显示初始用户
echo
log_info "初始用户账号:"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -e "SELECT id, userName, userRole FROM user"

echo
log_info "=========================================="
log_info "数据库初始化完成！"
log_info "=========================================="
echo
log_info "数据库信息:"
echo "  - 数据库名: $DB_NAME"
echo "  - 字符集: utf8mb4"
echo "  - 表数量: $TABLE_COUNT"
echo
log_info "默认管理员账号:"
echo "  - 用户名: admin"
echo "  - 密码: 12345678"
echo
log_warn "请登录后立即修改默认密码！"
echo
