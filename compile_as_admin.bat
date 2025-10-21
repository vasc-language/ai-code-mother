@echo off
chcp 65001 >nul
echo ============================================
echo    以管理员权限编译项目
echo ============================================
echo.

cd /d "%~dp0"

echo [1/2] 清理旧的编译文件...
call mvnw.cmd clean -q

echo.
echo [2/2] 编译项目（跳过测试）...
call mvnw.cmd compile -DskipTests

echo.
echo ============================================
echo    编译完成！
echo ============================================
pause
