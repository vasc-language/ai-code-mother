@echo off
chcp 65001 >nul
echo ========================================
echo   删除指定AI模型 - 执行脚本
echo ========================================
echo.
echo 将要删除以下模型:
echo   - deepseek-v3.1-free
echo   - deepseek-v3.1
echo   - deepseek-v3.2
echo   - kimi-k2-free
echo.
echo ========================================
echo.

set /p mysql_password=请输入MySQL root密码: 

echo.
echo 正在执行SQL删除操作...
echo.

mysql -u root -p%mysql_password% ai_code_mother < "%~dp0remove_specific_models.sql"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   ✓ 删除成功！
    echo ========================================
    echo.
    echo 请执行以下步骤验证:
    echo 1. 重启后端服务
    echo 2. 刷新前端页面
    echo 3. 检查模型列表
    echo.
) else (
    echo.
    echo ========================================
    echo   ✗ 删除失败！
    echo ========================================
    echo.
    echo 请检查:
    echo 1. MySQL密码是否正确
    echo 2. MySQL服务是否运行
    echo 3. 数据库 ai_code_mother 是否存在
    echo.
)

pause
