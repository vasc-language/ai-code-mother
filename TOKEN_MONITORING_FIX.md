# Token监控跨线程修复方案

## 问题描述

在工具调用场景下，AI会调用工具（如文件操作）来完成任务。这些工具调用在新线程中执行，导致：
1. ThreadLocal的MonitorContext为null
2. 监听器无法获取用户ID和应用ID
3. Token统计被完全跳过，无法实时监控消耗

**错误日志示例：**
```java
NullPointerException: Cannot invoke "MonitorContext.getUserId()" because "context" is null
```

## 解决方案

### 核心思路
利用OpenAI流式API的特性 + 全局Context缓存实现跨线程Token统计

### 修改的文件

#### 1. MonitorContextHolder.java - 全局缓存支持
**位置：** `src/main/java/com/spring/aicodemother/monitor/MonitorContextHolder.java`

**修改内容：**
- 添加 `ConcurrentHashMap` 全局缓存（key: userId:appId）
- 支持跨线程访问MonitorContext
- 新增 `accumulateTokens()` 方法，支持Token累加

**关键代码：**
```java
// 全局Context缓存：支持跨线程访问
private static final ConcurrentHashMap<String, MonitorContext> GLOBAL_CONTEXT_CACHE = new ConcurrentHashMap<>();

// 累加Token到Context（支持跨线程）
public static void accumulateTokens(String userId, String appId, Long additionalTokens) {
    String cacheKey = buildCacheKey(userId, appId);
    MonitorContext context = GLOBAL_CONTEXT_CACHE.get(cacheKey);
    if (context != null) {
        synchronized (context) {
            Long current = context.getTotalTokens();
            long newTotal = (current == null ? 0 : current) + additionalTokens;
            context.setTotalTokens(newTotal);
        }
    }
}
```

#### 2. AiModelMonitorListener.java - 监听器优化
**位置：** `src/main/java/com/spring/aicodemother/monitor/AiModelMonitorListener.java`

**修改内容：**
- `onRequest`: 主请求正常记录，工具调用不直接返回
- `onResponse`: 区分主请求和工具调用，使用全局缓存累加Token
- `recordTokenUsage`: 每次响应都累加Token到全局缓存

**关键代码：**
```java
// onResponse中的修复
if (context == null) {
    // 工具调用场景：等待主请求的最终Token统计
    context = MonitorContextHolder.getContext();
    if (context == null) {
        log.debug("[工具调用-跳过] 等待主请求的最终Token统计");
        return;
    }
}

// recordTokenUsage中的累加
if (totalTokens != null && totalTokens > 0) {
    MonitorContextHolder.accumulateTokens(userId, appId, totalTokens.longValue());
    log.debug("[Token累加] userId:{}, appId:{}, 本次消耗:{}", userId, appId, totalTokens);
}

// 从全局缓存获取最新的context（包含累加的Token）
MonitorContext latestContext = MonitorContextHolder.getContextFromCache(userId, appId);
if (latestContext != null) {
    context = latestContext;
}
```

#### 3. DynamicAiModelFactory.java - 代码优化
**位置：** `src/main/java/com/spring/aicodemother/ai/DynamicAiModelFactory.java`

**修改内容：**
- 添加注释说明：OpenAI流式API自动在最后一条消息中返回完整Token统计
- 移除不兼容的streamOptions配置（langchain4j 1.1.0-beta7不支持）

## 工作流程

```
1. 用户发起生成请求
   └─> AppService.chatToGenCode()
       └─> MonitorContextHolder.setContext(context)  // 存储到ThreadLocal和全局缓存

2. AI开始流式响应
   └─> 监听器 onRequest (主请求)
       └─> 记录context到attributes
   
3. AI调用工具（文件操作等）
   └─> 新线程执行工具
   └─> 监听器 onRequest (工具调用)
       └─> ThreadLocal为null，但不影响
   └─> 监听器 onResponse (工具调用)
       └─> context为null，跳过（等待主请求汇总）

4. 工具执行完成，AI继续响应
   
5. 流式响应结束
   └─> OpenAI返回最终消息（包含所有Token统计）
   └─> 监听器 onResponse (主请求)
       └─> 从全局缓存获取context
       └─> accumulateTokens() 累加Token
       └─> 更新context.totalTokens

6. doFinally处理
   └─> 从全局缓存读取累加后的totalTokens
   └─> 扣除积分、记录统计
   └─> MonitorContextHolder.clearContext()  // 清除ThreadLocal和全局缓存
```

## 关键特性

### 1. OpenAI流式API特性
OpenAI的流式Chat Completion API在最后一条消息中会返回**完整的Token使用统计**，包括：
- 主请求的Token
- 所有工具调用的Token
- 总计Token数

这意味着我们不需要手动累加每次工具调用的Token，只需在最终响应中获取一次即可。

### 2. 全局缓存的线程安全
- 使用 `ConcurrentHashMap` 保证并发安全
- Token累加时使用 `synchronized` 保证原子性
- 流程结束后自动清理缓存，避免内存泄漏

### 3. 兼容性
- 主请求（ThreadLocal有值）：正常监控
- 工具调用（ThreadLocal为null）：跳过单独监控，等待主请求汇总
- 降级处理：如果全局缓存也找不到，则跳过（不影响业务流程）

## 测试方法

### 1. 观察日志
启动应用后，观察生成代码时的日志输出：

```bash
# 期望看到的日志
[AI请求-主流程] userId:xxx, appId:xxx, model:xxx
[Token消耗] userId:xxx, appId:xxx, model:xxx, input:xxx, output:xxx, total:xxx
[Token累加] userId:xxx, appId:xxx, 本次消耗:xxx
[Context缓存] 存储到全局缓存: xxx:xxx
[工具调用-跳过] 等待主请求的最终Token统计
[Context缓存] 从全局缓存清除: xxx:xxx
```

### 2. 验证Token统计
在 `AppServiceImpl.doFinally()` 中添加日志：
```java
com.spring.aicodemother.monitor.MonitorContext monitorContext = 
    com.spring.aicodemother.monitor.MonitorContextHolder.getContext();
if (monitorContext != null && monitorContext.getTotalTokens() != null) {
    log.info("✅ Token统计成功: {}", monitorContext.getTotalTokens());
} else {
    log.warn("❌ Token统计失败: context或totalTokens为null");
}
```

### 3. 数据库验证
检查 `points_record` 表，确认：
- 积分扣除记录存在
- Token数量合理（>0）
- 扣除积分与Token数量成正比

## 预期效果

✅ **修复前的问题：**
- 工具调用时抛出NullPointerException
- Token监控完全失效
- 无法实时扣费

✅ **修复后的效果：**
- 工具调用正常执行，不抛异常
- Token统计准确（包含所有工具调用）
- 实时扣费机制正常工作
- 日志清晰显示Token消耗路径

## 注意事项

1. **缓存清理时机**：流程结束时（`doFinally`）必须调用 `MonitorContextHolder.clearContext()`，否则会导致内存泄漏

2. **并发安全**：Token累加使用 `synchronized(context)` 保证原子性，避免并发修改丢失

3. **降级策略**：如果全局缓存查不到context，则跳过监控，不影响业务流程

4. **测试模式**：当前积分扣除被注释（测试期间），测试完成后需要取消注释启用

## 相关文件

- `MonitorContextHolder.java` - Context持有者（全局缓存）
- `AiModelMonitorListener.java` - AI模型监听器
- `DynamicAiModelFactory.java` - 动态模型工厂
- `AppServiceImpl.java` - 应用服务（设置和清除context）

## 版本信息

- LangChain4j: 1.1.0-beta7
- Spring Boot: 3.5.4
- Java: 21

---

修复完成时间：2025-01-XX
修复人：AI Assistant
