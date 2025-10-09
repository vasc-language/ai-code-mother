# AI模型等级选择系统 - 开发进度报告

## 项目概述
实现一个基于积分的AI模型分级选择系统,用户可以根据项目复杂度选择不同等级的AI模型,不同模型按token消耗扣除相应积分。

---

## ✅ 已完成工作

### 阶段一：数据库设计
1. **ai_model_config表** - 存储11个AI模型配置
   - 字段: id, model_key, model_name, provider, base_url, tier, points_per_k_token, description, is_enabled, sort_order
   - 4个等级: SIMPLE(1分), MEDIUM(3分), HARD(8分), EXPERT(15分)

2. **points_record表修改** - 添加模型追踪字段
   - 新增字段: model_key (VARCHAR 50), token_count (INT)
   - 添加索引: idx_model_key

3. **SQL迁移脚本** - v1.1.0_ai_model_tier_system.sql
   - 已执行成功,数据已验证

### 阶段二：后端核心实体和服务
1. **实体类**
   - `AiModelConfig.java` - 模型配置实体
   - `AiModelConfigTableDef.java` - MyBatis Flex表定义
   - `PointsRecord.java` - 添加 modelKey 和 tokenCount 字段

2. **数据访问层**
   - `AiModelConfigMapper.java`

3. **枚举类**
   - `AiModelTierEnum.java` - 4级模型等级枚举
   - `PointsTypeEnum.java` - 添加 AI_GENERATE 类型

4. **服务层**
   - `AiModelConfigService.java` + 实现类
     - getByModelKey() - 根据key查询模型
     - listEnabledModels() - 获取所有启用模型
     - listByTier() - 按等级查询
     - calculatePoints() - 计算积分消耗

   - `UserPointsServiceImpl.deductPointsWithModel()` - 新方法
     - 支持记录 modelKey 和 tokenCount 参数

### 阶段三：积分扣除逻辑
- 新增 `deductPointsWithModel()` 方法
- 扣除积分时记录模型key和token数量

### 阶段四：API接口
- **AiModelController.java** - 3个接口
  - GET `/api/ai-model/list` - 获取所有启用模型
  - GET `/api/ai-model/list/tier/{tier}` - 按等级查询模型
  - GET `/api/ai-model/get/{modelKey}` - 查询单个模型配置

### 阶段五：集成(部分完成)
- ✅ **AppController.chatToGenCode()** - 已添加 `modelKey` 参数
  - 默认值: "deepseek-reasoner"
  - 参数可选,前端可传入指定模型

- ✅ **AppService接口** - 已添加新方法签名
  ```java
  Flux<String> chatToGenCode(Long appId, String message, User loginUser,
                             GenerationControl control, String modelKey);
  ```

---

## ⏳ 剩余工作

### 1. AppService实现类 - 添加modelKey支持
**文件**: `AppServiceImpl.java` (Line 115)

**需要做的修改**:
1. 添加新方法重载:
   ```java
   @Override
   public Flux<String> chatToGenCode(Long appId, String message, User loginUser,
                                      GenerationControl control, String modelKey) {
       // 1. 查询模型配置
       AiModelConfig modelConfig = aiModelConfigService.getByModelKey(modelKey);
       ThrowUtils.throwIf(modelConfig == null, ErrorCode.PARAMS_ERROR, "不支持的模型: " + modelKey);

       // 2. 存储modelKey到MonitorContext,供后续监听器使用
       MonitorContext context = MonitorContext.builder()
           .userId(loginUser.getId().toString())
           .appId(appId.toString())
           .modelKey(modelKey)
           .build();
       MonitorContextHolder.setContext(context);

       // 3. 调用现有的生成逻辑...
       // ... (复用现有代码)
   }
   ```

2. 修改现有方法,默认调用新方法:
   ```java
   public Flux<String> chatToGenCode(Long appId, String message, User loginUser,
                                      GenerationControl control) {
       return chatToGenCode(appId, message, loginUser, control, "deepseek-reasoner");
   }
   ```

### 2. MonitorContext - 添加modelKey字段
**文件**: `MonitorContext.java`

**修改**:
```java
@Data
@Builder
public class MonitorContext {
    private String userId;
    private String appId;
    private String modelKey;  // 新增
    private Long totalTokens;
}
```

### 3. AiModelMonitorListener - 集成积分扣除
**文件**: `AiModelMonitorListener.java`

**需要做的修改**:
```java
@Resource
private UserPointsService userPointsService;

@Resource
private AiModelConfigService aiModelConfigService;

private void recordTokenUsage(...) {
    TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
    if (tokenUsage != null && context != null) {
        Integer totalTokens = tokenUsage.totalTokenCount();
        context.setTotalTokens(totalTokens.longValue());

        // 获取modelKey
        String modelKey = context.getModelKey();
        if (StrUtil.isNotBlank(modelKey)) {
            // 计算并扣除积分
            Integer points = aiModelConfigService.calculatePoints(modelKey, totalTokens);

            try {
                userPointsService.deductPointsWithModel(
                    Long.valueOf(userId),
                    points,
                    PointsTypeEnum.AI_GENERATE.getValue(),
                    String.format("AI生成消耗(%s, %d tokens)", modelKey, totalTokens),
                    Long.valueOf(appId),
                    modelKey,
                    totalTokens
                );
                log.info("AI生成积分扣除: userId={}, modelKey={}, tokens={}, points={}",
                         userId, modelKey, totalTokens, points);
            } catch (Exception e) {
                log.error("AI生成积分扣除失败: {}", e.getMessage(), e);
            }
        }
    }
}
```

---

## 📊 数据库配置汇总

### 模型等级分布
| 等级 | 积分/1K tokens | 模型数量 | 代表模型 |
|------|---------------|---------|---------|
| SIMPLE | 1 | 2 | qwen-turbo, llama-3.1-8b-free |
| MEDIUM | 3 | 3 | qwen3-235b-free, deepseek-v3.1-free |
| HARD | 8 | 3 | deepseek-r1, qwen3-coder-plus |
| EXPERT | 15 | 3 | gpt-5-codex-high, deepseek-r1-0528-free |

### Token消耗测试结果
- 简单Vue项目(300行): **2,101 tokens**
- 预计消耗: SIMPLE模型约3积分, EXPERT模型约32积分

---

## 🚀 下次对话起点

1. **编译验证** - 检查当前代码是否编译通过
2. **完成AppServiceImpl** - 添加modelKey参数支持
3. **修改MonitorContext** - 添加modelKey字段
4. **修改AiModelMonitorListener** - 集成积分扣除逻辑
5. **测试验证** - API测试和积分扣除验证

---

## 📁 已创建/修改的文件列表

### 新增文件 (10个)
```
sql/v1.1.0_ai_model_tier_system.sql
src/main/java/com/spring/aicodemother/
├── model/
│   ├── entity/AiModelConfig.java
│   ├── entity/table/AiModelConfigTableDef.java
│   └── enums/AiModelTierEnum.java
├── mapper/AiModelConfigMapper.java
├── service/AiModelConfigService.java
├── service/impl/AiModelConfigServiceImpl.java
└── controller/AiModelController.java
```

### 修改文件 (5个)
```
src/main/java/com/spring/aicodemother/
├── model/entity/PointsRecord.java (添加字段)
├── model/enums/PointsTypeEnum.java (添加枚举)
├── service/UserPointsService.java (新增方法)
├── service/impl/UserPointsServiceImpl.java (新增实现)
├── service/AppService.java (新增方法签名)
└── controller/AppController.java (添加参数)
```

---

## 💡 关键技术点

1. **动态模型选择**: 前端传入modelKey,后端根据配置动态选择AI模型
2. **后付费模式**: 先生成代码,完成后根据实际token消耗扣除积分
3. **监控上下文**: 使用ThreadLocal传递监控信息(userId, appId, modelKey)
4. **积分计算**: 公式 = ceil(tokens/1000) × pointsPerKToken
5. **数据追踪**: 记录每次生成使用的模型和token数量

---

**开发进度**: 约80%完成
**预计剩余时间**: 1-2小时
**Token使用**: 121k/200k (60%)

---
生成时间: 2025-10-09 21:15
