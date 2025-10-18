# 开发计划生成功能 - 实现文档

## 功能概述

实现了"方案1：前置规划API"，用户在生成代码前可以先查看AI生成的开发计划，确认后再执行代码生成。

## 实现的功能

### 核心特性

- ✅ 独立的计划生成API（不执行代码生成）
- ✅ 使用轻量级模型降低成本（qwen-turbo）
- ✅ 计划内容结构化解析（步骤数、文件数、技术栈）
- ✅ Redis缓存（24小时有效期）
- ✅ 积分管理（生成计划仅消耗5积分）
- ✅ 限流保护（用户级别，每分钟10次）
- ✅ 对话历史记录

## 文件清单

### 新增文件（5个）

1. **GeneratePlanRequest.java**
   - 路径: `src/main/java/com/spring/aicodemother/model/dto/app/`
   - 作用: 生成计划请求DTO

2. **DevelopmentPlanVO.java**
   - 路径: `src/main/java/com/spring/aicodemother/model/vo/`
   - 作用: 开发计划响应VO，包含planId、content、estimatedSteps等

3. **PlanCacheData.java**
   - 路径: `src/main/java/com/spring/aicodemother/model/dto/plan/`
   - 作用: 计划缓存数据类

4. **PlanCacheService.java**
   - 路径: `src/main/java/com/spring/aicodemother/service/`
   - 作用: 计划缓存服务（Redis存储，24小时过期）

5. **AiPlanningService.java**
   - 路径: `src/main/java/com/spring/aicodemother/service/`
   - 作用: AI规划服务，使用轻量级模型生成开发计划

### 修改文件（3个）

6. **AppService.java**
   - 新增: `generateDevelopmentPlan` 接口定义

7. **AppServiceImpl.java**
   - 新增: `generateDevelopmentPlan` 方法实现
   - 包含：参数校验、权限验证、积分检查、AI生成计划、缓存保存、积分扣除、对话历史记录

8. **AppController.java**
   - 新增: `POST /app/generatePlan/{appId}` 接口

## API接口

### 生成开发计划

**接口**: `POST /api/app/generatePlan/{appId}`

**请求参数**:
```json
{
  "message": "用户需求描述"
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "planId": "123_456_1234567890",
    "content": "## 1. 需求分析\n...\n## 2. 技术选型\n...",
    "estimatedSteps": 5,
    "estimatedFiles": 12,
    "techStack": ["Vue 3", "Pinia", "Ant Design Vue"],
    "createdAt": "2025-01-10T10:30:00"
  },
  "message": "ok"
}
```

## 使用流程

### 后端流程

1. **用户调用生成计划API**
   ```
   POST /api/app/generatePlan/123
   Body: { "message": "制作一个TODO应用" }
   ```

2. **后端处理**:
   - 参数校验
   - 权限验证
   - 积分检查（5积分）
   - 调用轻量级模型（qwen-turbo）生成计划
   - 解析计划内容，提取结构化信息
   - 保存到Redis缓存（24小时）
   - 扣除积分
   - 记录到对话历史

3. **返回计划给前端**

### 前端流程（需实现）

1. **展示计划**:
   - 显示计划内容（Markdown格式）
   - 显示预估信息（步骤数、文件数、技术栈）

2. **用户确认**:
   - 提供"确认开始开发"按钮
   - 提供"修改需求"选项

3. **确认后调用代码生成**:
   ```javascript
   GET /api/app/chat/gen/code?appId=123&message=原需求&modelKey=xxx
   ```

## 技术细节

### AI模型选择

- **默认模型**: qwen-turbo（轻量级，成本低）
- **备选模型**: codex-mini-latest
- **温度**: 0.7
- **最大Token**: 3000

### 计划内容结构

计划使用Markdown格式，包含以下部分：

1. **需求分析**: 核心功能点、使用场景、技术难点
2. **技术选型**: 框架、库、工具及版本
3. **目录结构**: 完整的项目目录树
4. **核心模块**: 主要功能模块及职责
5. **开发步骤**: 详细的分步实施计划
6. **预估工作量**: 文件数量、代码行数、时长

### 结构化信息提取

使用正则表达式从计划内容中提取：

- **步骤数**: 匹配"步骤1:"、"步骤2:"等模式
- **文件数**: 匹配"预计X个文件"等模式
- **技术栈**: 从技术选型部分提取关键词

### Redis缓存

- **Key格式**: `dev_plan:{appId}_{userId}_{timestamp}`
- **过期时间**: 24小时
- **存储内容**: PlanCacheData（包含appId、userId、requirement、plan、createdAt）

### 积分管理

- **生成计划**: 消耗5积分
- **代码生成**: 正常消耗（按模型计费）
- **失败返还**: 如果生成失败，积分自动返还

## 优势

1. **用户体验**:
   - 提前了解AI的开发计划
   - 避免"只输出计划就开始构建"的问题
   - 可以在生成前修改需求

2. **成本控制**:
   - 使用轻量级模型生成计划（成本低）
   - 仅5积分，用户可以多次尝试
   - 避免无效的代码生成浪费积分

3. **技术优势**:
   - 完全可控的两阶段流程
   - 不依赖AI是否遵守"等待"指令
   - 可以保存和复用计划

## 下一步

### 前端实现

需要在前端实现：

1. **计划展示组件**:
   - Markdown渲染
   - 结构化信息展示
   - 确认/修改按钮

2. **流程改造**:
   - 在代码生成前先调用计划生成
   - 用户确认后再调用代码生成
   - 处理取消和修改场景

### 可选优化

1. **计划历史**: 保存用户的历史计划
2. **计划对比**: 对比不同需求的计划差异
3. **计划评分**: 用户对计划的满意度反馈
4. **智能推荐**: 根据历史数据推荐技术栈

## 测试

### 接口测试

```bash
# 生成开发计划
curl -X POST "http://localhost:8123/api/app/generatePlan/123" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxx" \
  -d '{"message": "制作一个待办事项管理应用"}'
```

### 预期响应

```json
{
  "code": 0,
  "data": {
    "planId": "123_456_1735898765432",
    "content": "## 1. 需求分析\n\n### 核心功能点\n- 添加待办事项\n- 标记完成状态...",
    "estimatedSteps": 6,
    "estimatedFiles": 15,
    "techStack": ["Vue 3", "Pinia", "TypeScript", "Ant Design Vue"],
    "createdAt": "2025-01-10T10:30:00"
  },
  "message": "ok"
}
```

## 常见问题

### Q1: 为什么使用qwen-turbo？

**A**: qwen-turbo是轻量级模型，成本低，响应快，适合生成计划这种不需要编写代码的任务。

### Q2: planId有什么用？

**A**: planId用于关联计划和后续的代码生成，可以追踪哪些计划最终被执行，用于数据分析。

### Q3: 计划生成失败怎么办？

**A**: 
1. 检查qwen-turbo模型是否启用
2. 检查用户积分是否充足
3. 查看日志中的详细错误信息
4. 如果模型不可用，会自动回退到codex-mini-latest

### Q4: 如何调整计划的详细程度？

**A**: 修改 `AiPlanningService.buildPlanningSystemPrompt()` 方法中的System Prompt。

## 维护

### 配置项

- `langchain4j.open-ai.streaming-chat-model.api-key`: AI模型API密钥
- Redis连接配置（计划缓存）

### 监控

- 生成计划的调用次数
- 平均生成时长
- 积分消耗统计
- 计划确认率（需前端支持）

## 总结

本次实现完成了开发计划生成功能的完整后端逻辑，采用"前置规划API"方案，为用户提供了更好的代码生成体验。前端只需实现计划展示和确认交互即可完成整个功能的闭环。

---

**实现时间**: 2025年1月  
**实现人员**: Factory AI Droid  
**版本**: v1.0
