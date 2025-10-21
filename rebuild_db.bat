@echo off
chcp 65001 >nul
echo ==========================================
echo 开始重建积分系统数据库
echo ==========================================
echo.
echo ⚠️ 警告：此操作将删除以下表的所有数据：
echo   - ai_model_config
echo   - user_points
echo   - points_record
echo   - sign_in_record
echo   - invite_record
echo   - ai_model_quality_stats
echo   - user_model_rating
echo.
echo 请确认是否继续？
pause
echo.
echo 正在执行SQL脚本...
mysql -u root -p ai_code_mother < sql\rebuild_points_system_from_scratch.sql
echo.
echo ==========================================
echo 执行完成！
echo ==========================================
pause
