@echo off
chcp 65001 > nul
echo ================================================
echo 修复所有表结构字段不匹配问题
echo ================================================
echo.
echo 本脚本将修复以下问题：
echo 1. sign_in_record: consecutiveDays → continuousDays
echo 2. sign_in_record: 添加 isDelete 字段
echo 3. invite_record: 完全重建表结构
echo.
echo ⚠️ 警告：此操作会修改数据库表结构！
echo ⚠️ 数据将被备份到 invite_record_backup_20251021
echo.
pause

echo.
echo 正在执行修复...
echo.

mysql -u root -p212409 ai_code_mother < "%~dp0sql\fix_all_field_mismatches.sql"

if %errorlevel% equ 0 (
    echo.
    echo ================================================
    echo ✅ 所有表结构修复成功！
    echo ================================================
    echo.
    echo 修复内容：
    echo   - sign_in_record.consecutiveDays → continuousDays
    echo   - sign_in_record 添加 isDelete 字段
    echo   - invite_record 表完全重建
    echo   - inviterReward → inviterPoints
    echo   - inviteeReward → inviteePoints
    echo   - 添加 registerIp, deviceId, registerTime, rewardTime
    echo.
    echo 备份表：
    echo   - invite_record_backup_20251021
    echo.
    echo 下一步：
    echo   1. 重启Spring Boot应用
    echo   2. 测试签到功能
    echo   3. 测试邀请功能
    echo.
) else (
    echo.
    echo ================================================
    echo ❌ 修复失败，请检查错误信息
    echo ================================================
    echo.
    echo 可能的原因：
    echo   1. MySQL服务未启动
    echo   2. 密码错误
    echo   3. 数据库不存在
    echo.
    echo 请手动执行SQL脚本：
    echo   mysql -u root -p212409 ai_code_mother ^< sql\fix_all_field_mismatches.sql
    echo.
)

echo.
pause
