#!/bin/bash
##############################################################################
# AICodeHub 完整部署脚本
# 适用于 Ubuntu 20.04/22.04 服务器
# 使用方法: sudo bash deploy.sh
##############################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量（请根据实际情况修改）
APP_NAME="aicodehub"
APP_USER="aicode"
APP_DIR="/var/app/${APP_NAME}"
FRONTEND_DIR="/var/www/${APP_NAME}"
JAR_NAME="ai-code-mother-0.0.1-SNAPSHOT.jar"

# 数据库配置（根据实际修改）
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="ai_code_mother"
DB_USER="root"
DB_PASS=""  # 执行时会提示输入

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 sudo 运行此脚本"
        exit 1
    fi
}

# 步骤1: 系统环境检查
check_system() {
    log_info "步骤1: 检查系统环境..."

    # 检查操作系统
    if [ ! -f /etc/os-release ]; then
        log_error "无法识别的操作系统"
        exit 1
    fi

    source /etc/os-release
    log_info "操作系统: $PRETTY_NAME"

    # 检查必要命令
    for cmd in apt systemctl nginx mysql java; do
        if ! command -v $cmd &> /dev/null; then
            log_warn "$cmd 未安装，将在后续步骤中安装"
        else
            log_info "$cmd 已安装"
        fi
    done
}

# 步骤2: 安装必要软件
install_dependencies() {
    log_info "步骤2: 安装必要软件..."

    apt update

    # 询问使用 Nginx 还是 OpenResty
    read -p "使用 Nginx 还是 OpenResty？(nginx/openresty) [nginx]: " WEB_SERVER
    WEB_SERVER=${WEB_SERVER:-nginx}

    # 安装 Java 21
    if ! java -version 2>&1 | grep -q "21"; then
        log_info "安装 OpenJDK 21..."
        apt install -y openjdk-21-jdk
    fi

    # 安装 MySQL（如果未安装）
    if ! command -v mysql &> /dev/null; then
        log_info "安装 MySQL..."
        apt install -y mysql-server
        systemctl start mysql
        systemctl enable mysql
    fi

    # 安装 Redis
    if ! command -v redis-cli &> /dev/null; then
        log_info "安装 Redis..."
        apt install -y redis-server
        systemctl start redis
        systemctl enable redis
    fi

    # 安装 Nginx 或 OpenResty
    if [ "$WEB_SERVER" == "openresty" ]; then
        if ! command -v openresty &> /dev/null; then
            log_info "安装 OpenResty..."
            # 添加 OpenResty 仓库
            apt install -y wget gnupg ca-certificates
            wget -qO - https://openresty.org/package/pubkey.gpg | apt-key add -
            echo "deb http://openresty.org/package/ubuntu $(lsb_release -sc) main" \
                | tee /etc/apt/sources.list.d/openresty.list
            apt update
            apt install -y openresty
        fi
    else
        if ! command -v nginx &> /dev/null; then
            log_info "安装 Nginx..."
            apt install -y nginx
        fi
    fi

    # 安装 Chromium（用于网页截图）
    if ! command -v chromium-browser &> /dev/null; then
        log_info "安装 Chromium..."
        apt install -y chromium-browser chromium-chromedriver
    fi

    log_info "所有依赖安装完成"
}

# 步骤3: 创建应用用户
create_user() {
    log_info "步骤3: 创建应用用户..."

    if id "$APP_USER" &>/dev/null; then
        log_warn "用户 $APP_USER 已存在"
    else
        useradd -r -m -s /bin/bash $APP_USER
        log_info "用户 $APP_USER 创建成功"
    fi
}

# 步骤4: 创建目录结构
create_directories() {
    log_info "步骤4: 创建目录结构..."

    mkdir -p $APP_DIR/{tmp/code_output,tmp/screenshots,logs}
    mkdir -p $FRONTEND_DIR

    chown -R $APP_USER:$APP_USER $APP_DIR
    chown -R www-data:www-data $FRONTEND_DIR

    log_info "目录创建完成"
}

# 步骤5: 初始化数据库
init_database() {
    log_info "步骤5: 初始化数据库..."

    read -sp "请输入 MySQL root 密码: " DB_PASS
    echo

    # 检查数据库是否存在
    if mysql -u$DB_USER -p$DB_PASS -e "USE $DB_NAME" 2>/dev/null; then
        log_warn "数据库 $DB_NAME 已存在，跳过初始化"
        read -p "是否重新初始化数据库？这将删除所有数据！(yes/no): " confirm
        if [ "$confirm" != "yes" ]; then
            log_info "跳过数据库初始化"
            return
        fi
        mysql -u$DB_USER -p$DB_PASS -e "DROP DATABASE $DB_NAME"
    fi

    # 创建数据库
    mysql -u$DB_USER -p$DB_PASS -e "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

    # 执行SQL脚本
    log_info "执行 SQL 初始化脚本..."
    mysql -u$DB_USER -p$DB_PASS $DB_NAME < ../sql/create_table.sql
    mysql -u$DB_USER -p$DB_PASS $DB_NAME < ../sql/ai_model_config.sql
    mysql -u$DB_USER -p$DB_PASS $DB_NAME < ../sql/v1.1.0_ai_model_tier_system.sql
    mysql -u$DB_USER -p$DB_PASS $DB_NAME < ../sql/migration_email_login.sql

    log_info "数据库初始化完成"
}

# 步骤6: 部署后端
deploy_backend() {
    log_info "步骤6: 部署后端应用..."

    # 复制 JAR 文件
    cp ../backend/$JAR_NAME $APP_DIR/
    chown $APP_USER:$APP_USER $APP_DIR/$JAR_NAME

    # 安装 systemd 服务
    cp ../config/aicodehub.service /etc/systemd/system/
    systemctl daemon-reload

    log_info "后端部署完成"
}

# 步骤7: 部署前端
deploy_frontend() {
    log_info "步骤7: 部署前端应用..."

    # 复制前端文件
    cp -r ../frontend/dist/* $FRONTEND_DIR/
    chown -R www-data:www-data $FRONTEND_DIR

    log_info "前端部署完成"
}

# 步骤8: 配置 Nginx/OpenResty
configure_nginx() {
    log_info "步骤8: 配置 Web 服务器..."

    if [ "$WEB_SERVER" == "openresty" ]; then
        log_info "配置 OpenResty..."

        # 创建配置目录
        mkdir -p /usr/local/openresty/nginx/conf/vhost

        # 复制 OpenResty 配置
        cp ../config/openresty.conf /usr/local/openresty/nginx/conf/vhost/aicodehub.conf

        # 在主配置中引入 vhost 目录
        if ! grep -q "include.*vhost/\*.conf" /usr/local/openresty/nginx/conf/nginx.conf; then
            sed -i '/http {/a \    include vhost/*.conf;' /usr/local/openresty/nginx/conf/nginx.conf
        fi

        # 测试配置
        /usr/local/openresty/bin/openresty -t

        # 启动或重启 OpenResty
        if systemctl is-active --quiet openresty; then
            systemctl reload openresty
        else
            systemctl start openresty
            systemctl enable openresty
        fi

        log_info "OpenResty 配置完成"
    else
        log_info "配置 Nginx..."

        # 复制 Nginx 配置
        cp ../config/nginx.conf /etc/nginx/sites-available/$APP_NAME

        # 创建软链接
        if [ ! -L /etc/nginx/sites-enabled/$APP_NAME ]; then
            ln -s /etc/nginx/sites-available/$APP_NAME /etc/nginx/sites-enabled/$APP_NAME
        fi

        # 测试配置
        nginx -t

        # 重启 Nginx
        systemctl restart nginx
        systemctl enable nginx

        log_info "Nginx 配置完成"
    fi

    # 安装日志轮转配置
    log_info "配置日志轮转..."
    cp ../config/logrotate-aicodehub /etc/logrotate.d/aicodehub
    chmod 644 /etc/logrotate.d/aicodehub
    log_info "日志轮转配置完成"
}

# 步骤9: 启动服务
start_services() {
    log_info "步骤9: 启动服务..."

    # 启动后端服务
    systemctl start $APP_NAME
    systemctl enable $APP_NAME

    # 等待服务启动
    sleep 10

    # 检查服务状态
    if systemctl is-active --quiet $APP_NAME; then
        log_info "后端服务启动成功"
    else
        log_error "后端服务启动失败，请查看日志: journalctl -u $APP_NAME -n 50"
        exit 1
    fi
}

# 步骤10: 健康检查
health_check() {
    log_info "步骤10: 执行健康检查..."

    sleep 5

    # 检查后端健康
    if curl -f http://localhost:8080/api/health/ &>/dev/null; then
        log_info "✓ 后端健康检查通过"
    else
        log_warn "× 后端健康检查失败"
    fi

    # 检查前端
    if curl -f http://localhost/ &>/dev/null; then
        log_info "✓ 前端访问正常"
    else
        log_warn "× 前端访问失败"
    fi

    # 检查 MySQL
    if systemctl is-active --quiet mysql; then
        log_info "✓ MySQL 运行正常"
    else
        log_warn "× MySQL 未运行"
    fi

    # 检查 Redis
    if redis-cli ping &>/dev/null; then
        log_info "✓ Redis 运行正常"
    else
        log_warn "× Redis 未运行"
    fi
}

# 显示部署信息
show_info() {
    log_info "=========================================="
    log_info "部署完成！"
    log_info "=========================================="
    echo
    log_info "应用信息:"
    echo "  - 应用目录: $APP_DIR"
    echo "  - 前端目录: $FRONTEND_DIR"
    echo "  - 日志目录: $APP_DIR/logs"
    echo
    log_info "服务管理:"
    echo "  - 启动: systemctl start $APP_NAME"
    echo "  - 停止: systemctl stop $APP_NAME"
    echo "  - 重启: systemctl restart $APP_NAME"
    echo "  - 状态: systemctl status $APP_NAME"
    echo "  - 日志: journalctl -u $APP_NAME -f"
    echo
    log_info "访问地址:"
    echo "  - 前端: http://你的服务器IP"
    echo "  - API: http://你的服务器IP/api/"
    echo "  - 健康检查: http://你的服务器IP/api/health/"
    echo
    log_info "下一步:"
    echo "  1. 配置域名解析"
    echo "  2. 修改 /etc/nginx/sites-available/$APP_NAME 中的域名"
    echo "  3. 安装 SSL 证书: certbot --nginx -d 你的域名"
    echo "  4. 配置防火墙: ufw allow 80/tcp && ufw allow 443/tcp"
    echo
}

# 主函数
main() {
    log_info "开始部署 AICodeHub..."
    echo

    check_root
    check_system
    install_dependencies
    create_user
    create_directories
    init_database
    deploy_backend
    deploy_frontend
    configure_nginx
    start_services
    health_check
    show_info
}

# 执行主函数
main
