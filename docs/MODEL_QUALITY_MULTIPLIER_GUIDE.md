# AI模型质量倍率系统优化指南

## 问题背景

用户提出："不同模型的生成代码质量不同，所以得弄一个模型的倍率出来，不然积分不完善"

## 现状分析

✅ **系统已经实现了基础倍率机制**
- `ai_model_config.points_per_k_token` 字段就是倍率
- 已按 SIMPLE/MEDIUM/HARD/EXPERT 分级
- 倍率范围：1-18 积分/1K tokens

❌ **存在的问题**
- 倍率设置过于简单，未考虑代码质量差异
- 同等级模型倍率相同，无法体现质量差异
- 缺少用户反馈机制来动态调整倍率

## 优化方案

### 方案1：质量系数加权（推荐）

```
最终积分 = 基础积分 × 质量系数

基础积分 = ceil(token数 / 1000) × points_per_k_token
质量系数 = 0.5 ~ 2.0（根据代码质量评估）
```

**实际案例：**

| 模型 | Token消耗 | 基础倍率 | 质量系数 | 实际扣费 | 质量表现 |
|------|----------|---------|---------|---------|----------|
| Qwen Turbo | 3000 | 1积分/1K | 0.7 | 3×1×0.7=**2.1积分** | 代码能跑但质量一般 |
| Qwen3 235B Free | 3000 | 3积分/1K | 1.0 | 3×3×1.0=**9积分** | 标准质量，性价比高 |
| DeepSeek R1 | 3000 | 8积分/1K | 1.4 | 3×8×1.4=**33.6积分** | 架构优秀，代码规范 |
| GPT-5 Codex | 3000 | 15积分/1K | 1.8 | 3×15×1.8=**81积分** | 顶级质量，文档完整 |

### 方案2：基于用户评价的动态调整

**实现机制：**
1. 用户完成生成后可以评分（1-5星）
2. 系统收集评分数据，定期调整 `quality_score`
3. 评分高的模型倍率降低（性价比提升）
4. 评分低的模型倍率提高（惩罚机制）

**数据表设计：**
```sql
-- 1. ai_model_config 增加质量评分字段
ALTER TABLE ai_model_config 
ADD COLUMN quality_score DECIMAL(3,2) DEFAULT 1.00;
ADD COLUMN user_rating DECIMAL(3,2) DEFAULT 5.00;

-- 2. 用户评价表
CREATE TABLE user_model_rating (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    model_key VARCHAR(50) NOT NULL,
    app_id BIGINT NOT NULL,
    rating INT NOT NULL COMMENT '评分1-5星',
    code_quality INT COMMENT '代码质量1-5',
    response_speed INT COMMENT '响应速度1-5',
    comment TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. 模型质量统计表
CREATE TABLE ai_model_quality_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_key VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    total_generations INT DEFAULT 0,
    success_count INT DEFAULT 0,
    avg_user_rating DECIMAL(3,2) DEFAULT 0,
    avg_tokens INT DEFAULT 0,
    UNIQUE KEY uk_model_date (model_key, date)
);
```

### 方案3：成功率影响倍率

**逻辑：**
- 生成失败率高的模型，降低倍率（补偿用户）
- 生成成功率高的模型，保持原倍率

```java
// 示例：如果模型成功率低于80%，自动降低倍率
if (modelStats.getSuccessRate() < 80.0) {
    double penalty = (80.0 - modelStats.getSuccessRate()) / 100.0;
    finalMultiplier = baseMultiplier * (1 - penalty);
}
```

## 推荐的模型倍率配置

基于代码质量和实际测试，推荐以下倍率：

### SIMPLE级别（适合新手练习）
| 模型 | 倍率 | 质量系数 | 说明 |
|------|-----|---------|------|
| Qwen Turbo | 1 | 0.70 | 快速但质量一般 |
| Codex Mini | 2 | 0.80 | 基础编程能力 |

### MEDIUM级别（日常开发）
| 模型 | 倍率 | 质量系数 | 说明 |
|------|-----|---------|------|
| Qwen3 235B Free | 3 | 1.00 | **推荐**，性价比最高 |
| DeepSeek V3.1 Free | 3 | 1.00 | 均衡性能 |
| Qwen Coder 32B | 4 | 1.10 | 专业编程增强 |

### HARD级别（复杂项目）
| 模型 | 倍率 | 质量系数 | 说明 |
|------|-----|---------|------|
| DeepSeek R1 | 8 | 1.40 | **当前默认**，架构优秀 |
| Qwen3 Coder Plus | 10 | 1.50 | 企业级代码质量 |
| DeepSeek V3.2 | 10 | 1.50 | 最新版增强 |

### EXPERT级别（顶级质量）
| 模型 | 倍率 | 质量系数 | 说明 |
|------|-----|---------|------|
| GPT-5 Codex High | 15 | 1.80 | 顶级代码生成 |
| Qwen3 Max Preview | 15 | 1.80 | 复杂架构设计 |
| Qwen3 235B Thinking | 18 | 2.00 | **最强**，思维链推理 |

## 实施步骤

### 第一阶段：静态质量系数（立即可实施）

1. **执行SQL脚本**
```bash
mysql -u root -p ai_code_mother < sql/optimize_model_quality_multiplier.sql
```

2. **修改积分计算逻辑**
```java
// AiModelConfigServiceImpl.java
@Override
public Integer calculatePoints(String modelKey, Integer tokenCount) {
    AiModelConfig config = getByModelKey(modelKey);
    if (config == null) {
        throw new IllegalArgumentException("模型配置不存在: " + modelKey);
    }
    
    // 基础积分
    int kTokens = (int) Math.ceil(tokenCount / 1000.0);
    int basePoints = kTokens * config.getPointsPerKToken();
    
    // 应用质量系数
    BigDecimal qualityScore = config.getQualityScore();
    if (qualityScore != null && qualityScore.compareTo(BigDecimal.ONE) != 0) {
        return BigDecimal.valueOf(basePoints)
            .multiply(qualityScore)
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }
    
    return basePoints;
}
```

### 第二阶段：用户评价系统（后续实施）

1. **创建评价接口**
```java
// ModelRatingController.java
@PostMapping("/model/rate")
public BaseResponse<Void> rateModel(@RequestBody ModelRatingRequest request) {
    // 用户对模型进行评分
    modelRatingService.rateModel(
        request.getUserId(),
        request.getModelKey(),
        request.getAppId(),
        request.getRating(),
        request.getComment()
    );
    return ResultUtils.success(null);
}
```

2. **前端评价界面**
```vue
<!-- 生成完成后弹出评价框 -->
<a-modal v-model:visible="showRatingModal" title="给AI模型评分">
  <a-rate v-model:value="rating" :count="5" />
  <a-textarea v-model:value="comment" placeholder="您对代码质量的评价..." />
  <template #footer>
    <a-button @click="submitRating">提交评分</a-button>
  </template>
</a-modal>
```

3. **定时任务更新质量系数**
```java
// ModelQualityUpdateScheduler.java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
public void updateQualityScores() {
    // 统计过去30天的用户评分
    List<ModelQualityStats> stats = calculateQualityStats();
    
    for (ModelQualityStats stat : stats) {
        // 根据评分调整质量系数
        double newQualityScore = calculateQualityScore(
            stat.getAvgUserRating(),
            stat.getSuccessRate(),
            stat.getAvgTokens()
        );
        
        // 更新配置
        aiModelConfigService.updateQualityScore(
            stat.getModelKey(), 
            newQualityScore
        );
    }
}
```

### 第三阶段：智能推荐（高级功能）

根据用户历史使用情况，推荐最合适的模型：

```java
// ModelRecommendationService.java
public AiModelConfig recommendModel(Long userId, String codeGenType) {
    // 1. 获取用户积分余额
    UserPoints userPoints = userPointsService.getByUserId(userId);
    
    // 2. 分析用户历史偏好
    List<String> usedModels = getUserHistoryModels(userId);
    
    // 3. 根据积分推荐合适等级的模型
    if (userPoints.getAvailablePoints() < 50) {
        // 积分不足，推荐SIMPLE/MEDIUM
        return aiModelConfigService.getByModelKey("qwen3-235b-free");
    } else if (userPoints.getAvailablePoints() < 200) {
        // 中等积分，推荐HARD
        return aiModelConfigService.getByModelKey("deepseek-r1");
    } else {
        // 积分充足，推荐EXPERT
        return aiModelConfigService.getByModelKey("gpt-5-codex-high");
    }
}
```

## 效果对比

### 优化前（固定倍率）
```
用户生成一个复杂Vue应用：
- 使用DeepSeek R1，消耗8000 tokens
- 扣费 = 8 × 8 = 64积分
- 问题：无论代码质量如何，都是固定64积分
```

### 优化后（质量系数加权）
```
场景1：代码质量优秀（quality_score = 1.4）
- 扣费 = 8 × 8 × 1.4 = 89.6 ≈ 90积分
- 用户反馈：代码规范、可维护性强，物有所值

场景2：代码质量一般（quality_score = 0.8）
- 扣费 = 8 × 8 × 0.8 = 51.2 ≈ 51积分
- 用户反馈：代码能用但不完美，自动降价补偿
```

## 总结

✅ **你的担心是对的**，不同模型质量差异应该反映在积分上

✅ **好消息**，系统已经有 `points_per_k_token` 倍率机制

✅ **优化方案**，增加 `quality_score` 质量系数实现动态定价

✅ **立即行动**，执行 SQL 脚本并修改计算逻辑即可上线

📊 **预期收益**
- 用户感知更公平（高质量贵，低质量便宜）
- 鼓励使用高质量模型（物有所值）
- 自动淘汰低质量模型（低评分=高倍率=无人用）
- 建立模型质量数据库（指导后续采购决策）
