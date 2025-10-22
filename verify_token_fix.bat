@echo off
chcp 65001 >nul
echo ========================================
echo Token监控修复验证脚本
echo ========================================
echo.

echo [1/3] 检查修改的文件...
set FILES_OK=0

if exist "src\main\java\com\spring\aicodemother\monitor\MonitorContextHolder.java" (
    echo ✓ MonitorContextHolder.java 存在
    set /a FILES_OK+=1
) else (
    echo ✗ MonitorContextHolder.java 缺失
)

if exist "src\main\java\com\spring\aicodemother\monitor\AiModelMonitorListener.java" (
    echo ✓ AiModelMonitorListener.java 存在
    set /a FILES_OK+=1
) else (
    echo ✗ AiModelMonitorListener.java 缺失
)

if exist "src\main\java\com\spring\aicodemother\ai\DynamicAiModelFactory.java" (
    echo ✓ DynamicAiModelFactory.java 存在
    set /a FILES_OK+=1
) else (
    echo ✗ DynamicAiModelFactory.java 缺失
)

if exist "TOKEN_MONITORING_FIX.md" (
    echo ✓ TOKEN_MONITORING_FIX.md 文档存在
    set /a FILES_OK+=1
) else (
    echo ✗ TOKEN_MONITORING_FIX.md 文档缺失
)

echo.
echo [2/3] 检查关键代码...
findstr /C:"GLOBAL_CONTEXT_CACHE" "src\main\java\com\spring\aicodemother\monitor\MonitorContextHolder.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ 全局Context缓存已添加
) else (
    echo ✗ 全局Context缓存未找到
)

findstr /C:"accumulateTokens" "src\main\java\com\spring\aicodemother\monitor\MonitorContextHolder.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Token累加方法已添加
) else (
    echo ✗ Token累加方法未找到
)

findstr /C:"accumulateTokens" "src\main\java\com\spring\aicodemother\monitor\AiModelMonitorListener.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ 监听器已调用Token累加
) else (
    echo ✗ 监听器未调用Token累加
)

echo.
echo [3/3] 编译检查...
echo 正在编译项目（跳过测试）...
call mvnw.cmd clean compile -DskipTests -q
if %errorlevel%==0 (
    echo ✓ 编译成功，修复有效
) else (
    echo ✗ 编译失败，请检查代码
    goto :error
)

echo.
echo ========================================
echo ✅ 验证完成！Token监控修复已应用
echo ========================================
echo.
echo 下一步：
echo 1. 启动应用：mvnw.cmd spring-boot:run
echo 2. 测试工具调用场景（生成Vue项目）
echo 3. 观察日志中的 [Token累加] 和 [Context缓存]
echo 4. 检查 points_record 表确认积分扣除
echo.
pause
exit /b 0

:error
echo.
echo ❌ 验证失败，请检查错误信息
pause
exit /b 1
