# 错误日志

## ✅ 已解决的错误

### 1. LogoGeneratorToolTest SLF4J 冲突 (2025-08-28 已解决)
**问题**: `java.lang.IllegalStateException: LoggerFactory is not a Logback LoggerContext`
**原因**: `com.alibaba:dashscope-sdk-java` 引入了 `slf4j-simple` 与 Spring Boot 默认 Logback 冲突
**解决**: 在 pom.xml 中排除 `slf4j-simple` 传递依赖

### 2. UndrawIllustrationTool API 访问问题 (2025-08-28 已解决) 
**问题**: 尝试访问 unDraw 私有 API 导致连接失败
**解决**: 重构为使用 Pixabay API 作为插画搜索服务

---

当前项目状态：✅ 所有已知错误已修复，测试正常通过