@echo off
chcp 65001 > nul
echo ================================================
echo 修复 user_points 表结构
echo ================================================
echo.
echo 请确保MySQL服务正在运行...
echo.

set /p mysql_password="请输入MySQL root密码: "

echo.
echo 正在删除旧表并重新创建...
echo.

mysql -u root -p%mysql_password% -D ai_code_mother < "%~dp0sql\create_table.sql"

if %errorlevel% equ 0 (
    echo.
    echo ✅ 表结构修复成功！
    echo.
    echo 验证表结构...
    mysql -u root -p%mysql_password% -D ai_code_mother -e "DESC user_points;"
) else (
    echo.
    echo ❌ 修复失败，请检查错误信息
)

echo.
pause
