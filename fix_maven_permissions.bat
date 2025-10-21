@echo off
chcp 65001 >nul
echo ============================================
echo    修复 Maven 文件夹权限
echo ============================================
echo.

set M2_DIR=%USERPROFILE%\.m2\wrapper

echo 检查目录: %M2_DIR%
echo.

if not exist "%M2_DIR%" (
    echo [创建] .m2\wrapper 目录...
    mkdir "%M2_DIR%"
    if errorlevel 1 (
        echo [错误] 无法创建目录，请以管理员身份运行此脚本
        pause
        exit /b 1
    )
    echo [成功] 目录创建成功
) else (
    echo [存在] 目录已存在
)

echo.
echo [修复] 设置完全控制权限...
icacls "%M2_DIR%" /grant %USERNAME%:(OI)(CI)F /T

if errorlevel 1 (
    echo [错误] 权限设置失败，请以管理员身份运行
) else (
    echo [成功] 权限设置完成
)

echo.
echo ============================================
echo    修复完成！
echo    现在可以尝试编译项目了
echo ============================================
pause
