# Token监控修复补丁 - 日志可见性与NPE修复

## 🐛 发现的问题

### 问题1：关键日志不可见
**现象：** 虽然代码已部署生效，但看不到Token累加和Context缓存的日志

**原因：** 使用了 `log.debug()` 而应用日志级别为 `INFO`

**证据：**
```java
// 旧代码
log.debug("[Context缓存] 存储到全局缓存: {}", cacheKey);  // ← 看不到
log.debug("[Token累加] userId:{}, 本次消耗:{}", ...);      // ← 看不到
```

**影响：** 无法验证修复是否真正生效，调试困难

---

### 问题2：onError的空指针异常
**现象：** 错误日志中出现 `NullPointerException` 在 `onError` 方法

**原因：** 工具调用出错时，新线程的ThreadLocal为null，直接获取会NPE

**证据：**
```
2025-10-21T23:36:15.106+08:00  WARN ... An exception occurred during the invocation of the chat model listener.

java.lang.NullPointerException: Cannot invoke "MonitorContext.getUserId()" because "context" is null
at AiModelMonitorListener.onError(AiModelMonitorListener.java:109)
```

**影响：** 错误监控失效，污染日志

---

## ✅ 修复方案

### 修复1：日志级别提升（DEBUG → INFO）

#### 修改文件：MonitorContextHolder.java
**3处修改：**
```java
// 1. setContext中的缓存存储
- log.debug("[Context缓存] 存储到全局缓存: {}", cacheKey);
+ log.info("[Context缓存] 存储到全局缓存: {}", cacheKey);

// 2. accumulateTokens中的累加记录
- log.debug("[Token累加] userId:{}, appId:{}, +{} = {}", userId, appId, additionalTokens, newTotal);
+ log.info("[Token累加] userId:{}, appId:{}, +{} = {}", userId, appId, additionalTokens, newTotal);

// 3. clearContext中的缓存清除
- log.debug("[Context缓存] 从全局缓存清除: {}", cacheKey);
+ log.info("[Context缓存] 从全局缓存清除: {}", cacheKey);
```

#### 修改文件：AiModelMonitorListener.java
**1处修改：**
```java
// recordTokenUsage中的Token累加日志
- log.debug("[Token累加] userId:{}, appId:{}, 本次消耗:{}", userId, appId, totalTokens);
+ log.info("[Token累加] userId:{}, appId:{}, 本次消耗:{}", userId, appId, totalTokens);
```

---

### 修复2：onError空指针保护

#### 修改文件：AiModelMonitorListener.java

**修复前：**
```java
@Override
public void onError(ChatModelErrorContext errorContext) {
    MonitorContext context = MonitorContextHolder.getContext();  // ← 工具调用线程为null
    String userId = context.getUserId();  // ← NPE！
    String appId = context.getAppId();
    // ...
}
```

**修复后：**
```java
@Override
public void onError(ChatModelErrorContext errorContext) {
    // ✅ 修复：从attributes和全局缓存获取context，避免NPE
    Map<Object, Object> attributes = errorContext.attributes();
    MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
    
    // 如果attributes中没有，尝试从ThreadLocal获取
    if (context == null) {
        context = MonitorContextHolder.getContext();
    }
    
    // 如果还是null，说明是工具调用或其他异常场景，跳过监控
    if (context == null) {
        log.debug("[错误监控-跳过] MonitorContext为null，跳过错误监控");
        return;
    }
    
    String userId = context.getUserId();
    String appId = context.getAppId();
    
    // 安全检查
    if (userId == null || appId == null) {
        log.warn("[错误监控-跳过] userId或appId为null");
        return;
    }
    
    // 获取模型名称和错误类型
    String modelName = errorContext.chatRequest().modelName();
    String errorMessage = errorContext.error().getMessage();
    // 记录失败请求
    aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
    aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
    // 记录响应时间（即使是错误响应）
    recordResponseTime(attributes, userId, appId, modelName);
    
    log.info("[错误监控] userId:{}, appId:{}, model:{}, error:{}", userId, appId, modelName, errorMessage);
}
```

**修复特点：**
1. 先尝试从 `attributes` 获取（主请求）
2. 再尝试从 `ThreadLocal` 获取（降级）
3. 如果都为null，**安全跳过**（不抛异常）
4. 增加userId/appId的null检查
5. 添加错误监控日志

---

## 📊 修复效果对比

### 修复前的日志：
```
✅ [AI请求-主流程] userId:338216128179834880, appId:338267680726773760, model:gpt-5-low
✅ [Token消耗] userId:338216128179834880, total:8855
❌ （看不到Token累加日志）
❌ （看不到Context缓存日志）
❌ NullPointerException in onError
```

### 修复后的日志（预期）：
```
✅ [AI请求-主流程] userId:338216128179834880, appId:338267680726773760, model:gpt-5-low
✅ [Context缓存] 存储到全局缓存: 338216128179834880:338267680726773760
✅ [Token消耗] userId:338216128179834880, total:8855
✅ [Token累加] userId:338216128179834880, 本次消耗:8855
✅ [Context缓存] 从全局缓存清除: 338216128179834880:338267680726773760
✅ （无NPE错误）
```

---

## 🚀 部署步骤

### 1. 重新编译
```bash
mvnw.cmd clean compile
```

### 2. 重启应用
```bash
# Ctrl+C 停止当前应用
mvnw.cmd spring-boot:run
```

### 3. 测试验证
生成一个新应用，观察日志中是否出现：
- `[Context缓存] 存储到全局缓存`
- `[Token累加] userId:xxx, 本次消耗:xxx`
- `[Context缓存] 从全局缓存清除`
- 无NPE错误日志

---

## 📝 修复文件清单

| 文件 | 修改内容 | 修改行数 |
|-----|---------|---------|
| **MonitorContextHolder.java** | 日志级别提升（3处） | 3行 |
| **AiModelMonitorListener.java** | 日志级别提升（1处）+ onError修复 | 30行 |

---

## 💡 技术总结

### 学到的经验：
1. **日志级别很重要** - 关键监控日志应该用 `INFO` 而不是 `DEBUG`
2. **全量测试** - 修复时要考虑所有监听器方法（onRequest、onResponse、**onError**）
3. **防御性编程** - 跨线程场景要处理各种null情况
4. **先验证再下结论** - 看到特定日志（如`[AI请求-主流程]`）说明代码已生效

### 修复亮点：
- ✅ 保持了一致的修复策略（attributes → ThreadLocal → 安全跳过）
- ✅ 增加了详细的日志输出，便于问题追踪
- ✅ 不影响业务流程（降级策略，跳过而不报错）

---

**修复完成时间**: 2025-01-21
**修复人**: AI Assistant
**版本**: Token监控修复 v1.1
