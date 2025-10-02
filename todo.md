# 用户积分系统设计与实现

## 核心目标
通过积分系统限制大模型API费用过度消耗（防刷），激发用户使用平台的积极性，建立健康的用户增长机制。

---

## 检查任务 - 用户积分系统实现状态
- [x] 梳理后端积分系统实现，记录核心逻辑与已覆盖需求
- [x] 核查前端积分相关功能及接口，评估覆盖情况
- [x] 对照设计找出缺口或风险，形成结论
- [x] 汇总检查结果并准备后续 review 记录

---

## 一、积分系统核心逻辑

### 实际消耗数据参考
- **真实数据**：10次生成请求消耗6万tokens，平均每次6000 tokens
- **成本估算**：DeepSeek API约0.001元/1000tokens，每次生成约0.006元

### 1. 积分与Token消耗直接挂钩
- **兑换比例**：1积分 = 1000 Token（简化计算，降低积分膨胀）
- **消耗机制**：生成应用时，统一按实际Token消耗扣减积分
  - 平均每次生成：预估6积分（对应6000 tokens）
  - 简单应用（HTML）：4-5积分
  - 中等应用（多文件）：6-8积分
  - 复杂应用（Vue）：8-10积分
- **扣减时机**：生成前预扣6积分，生成后根据实际消耗多退少补（误差±2积分内不退补）

### 2. 积分获取方式

#### 2.1 每日签到
- **基础奖励**：每日签到获得5积分（可生成1次应用）
- **连续签到奖励递增**：
  - 连续3天：额外获得3积分（当日共8积分）
  - 连续7天：额外获得10积分（当日共15积分）
  - 连续30天：额外获得50积分（当日共55积分）
- **规则**：
  - 每日首次签到有效，当天重复签到不计
  - 断签后重新计算连续天数

#### 2.2 注册新用户
- **新用户注册奖励**：30积分（可体验5次生成）
- **首次生成奖励**：新用户首次成功生成应用后，额外奖励30积分（鼓励完成首次体验）

#### 2.3 邀请新用户
- **邀请规则**：
  - 被邀请用户完成注册并首次登录：邀请人和新用户各获得20积分
  - 被邀请用户首次成功生成应用：邀请人额外获得30积分，新用户额外获得10积分
- **防刷机制**：
  - 同一IP/设备7天内注册的多个账号不计入邀请奖励
  - 邀请人单日最多获得3次邀请奖励（防止批量注册刷积分）
  - 被邀请用户7天内无任何操作，回收邀请奖励积分

### 3. 积分有效期
- **有效期**：积分有效期为获得后90天
- **提醒机制**：积分到期前7天通过站内信提醒用户
- **过期处理**：过期积分自动清零，不影响账户其他积分

---

## 二、防刷规则

### 1. Token消耗限制
- **单用户单日Token上限**：12万Token（相当于120积分，可生成20次应用）
- **单用户单日生成次数上限**：30次（防止恶意刷量）
- **达到上限后的处理**：当日无法继续生成应用，次日0点重置
- **超额监控**：通过`AiModelMetricsCollector`的`ai_model_tokens_total`指标监控异常消耗

### 2. 无效生成的惩罚
- **无效判定标准**：
  - 生成内容为空或少于100字符
  - 24小时内重复生成相同需求（通过需求文本MD5检测）
  - 触发安全过滤（如色情、暴力、政治敏感内容）
- **惩罚措施**：
  - 扣除的积分不返还
  - 额外扣减10积分
  - 记录警告次数，累计3次当日禁止生成

### 3. 限流规则
- 复用`RateLimitAspect`限流能力：
  - 单用户每分钟最多发起5次生成请求
  - 单IP每分钟最多发起10次生成请求
- 超出限流后返回429错误，前端提示用户稍后重试

---

## 三、数据库表设计

### 3.1 用户积分表（user_points）
```sql
CREATE TABLE user_points (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points INT DEFAULT 0 COMMENT '累计获得积分',
    available_points INT DEFAULT 0 COMMENT '当前可用积分',
    frozen_points INT DEFAULT 0 COMMENT '冻结积分（预留，暂不使用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_available_points (available_points)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';
```

### 3.2 积分明细表（points_record）
```sql
CREATE TABLE points_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动数量（正数为增加，负数为扣减）',
    balance INT NOT NULL COMMENT '变动后余额',
    type VARCHAR(20) NOT NULL COMMENT '积分类型（SIGN_IN:签到, REGISTER:注册, INVITE:邀请, GENERATE:生成应用, EXPIRE:过期）',
    reason VARCHAR(200) COMMENT '变动原因描述',
    related_id BIGINT COMMENT '关联ID（如应用ID、邀请记录ID）',
    expire_time DATETIME COMMENT '积分过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细表';
```

### 3.3 签到记录表（sign_in_record）
```sql
CREATE TABLE sign_in_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sign_in_date DATE NOT NULL COMMENT '签到日期',
    continuous_days INT DEFAULT 1 COMMENT '连续签到天数',
    points_earned INT NOT NULL COMMENT '本次签到获得积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_user_date (user_id, sign_in_date),
    INDEX idx_user_id (user_id),
    INDEX idx_sign_in_date (sign_in_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';
```

### 3.4 邀请关系表（invite_record）
```sql
CREATE TABLE invite_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    invitee_id BIGINT NOT NULL COMMENT '被邀请人ID',
    invite_code VARCHAR(32) NOT NULL COMMENT '邀请码',
    register_ip VARCHAR(50) COMMENT '注册IP',
    device_id VARCHAR(100) COMMENT '设备ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态（PENDING:待确认, REGISTERED:已注册, REWARDED:已奖励）',
    inviter_points INT DEFAULT 0 COMMENT '邀请人获得积分',
    invitee_points INT DEFAULT 0 COMMENT '被邀请人获得积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    register_time DATETIME COMMENT '注册时间',
    reward_time DATETIME COMMENT '奖励发放时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_invite_code (invite_code),
    INDEX idx_inviter_id (inviter_id),
    INDEX idx_invitee_id (invitee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请关系表';
```

---

## 四、具体实现任务列表

### 阶段一：数据库和基础设施（1-2天）
- [ ] 创建积分相关的4张数据库表（user_points, points_record, sign_in_record, invite_record）
- [ ] 使用MyBatis-Flex代码生成器生成实体类、Mapper和基础Service
- [ ] 编写积分系统单元测试基类，配置测试数据库

### 阶段二：核心积分服务（2-3天）
- [ ] 实现`PointsService`核心服务：
  - [ ] 积分增加方法（addPoints）：支持签到、注册、邀请等类型
  - [ ] 积分扣减方法（deductPoints）：生成应用时扣减积分
  - [ ] 积分查询方法（getUserPoints）：查询用户当前可用积分
  - [ ] 积分明细查询（getPointsRecords）：分页查询积分变动历史
- [ ] 实现积分过期定时任务（每日0点执行）：
  - [ ] 扫描expire_time小于当前时间的积分记录
  - [ ] 扣减过期积分，记录过期明细

### 阶段三：签到功能（1天）
- [ ] 实现`SignInService`签到服务：
  - [ ] 每日签到方法（dailySignIn）：校验是否已签到，发放积分
  - [ ] 计算连续签到天数逻辑
  - [ ] 连续签到额外奖励发放（3天、7天、30天）
- [ ] 签到Controller和API接口：
  - [ ] `POST /api/points/sign-in`：每日签到
  - [ ] `GET /api/points/sign-in/status`：查询今日签到状态和连续天数

### 阶段四：邀请功能（2天）
- [ ] 实现`InviteService`邀请服务：
  - [ ] 生成邀请码方法（generateInviteCode）：为用户生成唯一邀请码
  - [ ] 注册时绑定邀请关系（bindInvite）：新用户注册时记录邀请人
  - [ ] 发放邀请奖励（rewardInvite）：新用户首次登录/生成应用时触发
  - [ ] 防刷检测（checkInviteAbuse）：同IP/设备检测，单日邀请次数限制
- [ ] 邀请Controller和API接口：
  - [ ] `GET /api/invite/code`：获取当前用户的邀请码
  - [ ] `GET /api/invite/records`：查询邀请记录和奖励明细
- [ ] 修改用户注册接口，支持邀请码参数

### 阶段五：生成应用积分扣减（1-2天）
- [ ] 修改`AppServiceImpl.chatToGenCode`方法：
  - [ ] 生成前校验用户积分是否充足
  - [ ] 根据应用类型（HTML/多文件/Vue）预估Token消耗
  - [ ] 扣减对应积分，记录明细
- [ ] 实现无效生成检测和惩罚：
  - [ ] 生成结果为空或过短时记录警告
  - [ ] 24小时内重复生成检测（MD5）
  - [ ] 累计3次警告当日禁止生成

### 阶段六：防刷和监控（1天）
- [ ] 实现Token消耗单日上限检测：
  - [ ] 在Redis中记录用户当日Token消耗总量
  - [ ] 达到50万Token上限后拒绝生成请求
- [ ] 集成限流规则到生成应用接口：
  - [ ] 单用户每分钟5次
  - [ ] 单IP每分钟10次
- [ ] Prometheus监控指标：
  - [ ] 积分发放总量（按类型统计）
  - [ ] 积分消耗总量
  - [ ] 无效生成次数

### 阶段七：前端集成（2-3天）
- [ ] 积分展示组件：
  - [ ] 头部导航栏显示当前可用积分
  - [ ] 积分不足时生成按钮置灰并提示
- [ ] 签到功能页面：
  - [ ] 签到按钮和动画效果
  - [ ] 连续签到天数展示
  - [ ] 签到日历（可选）
- [ ] 邀请功能页面：
  - [ ] 邀请码展示和复制
  - [ ] 邀请链接生成和分享
  - [ ] 邀请记录列表
- [ ] 积分明细页面：
  - [ ] 积分收支明细列表
  - [ ] 按类型筛选（签到/邀请/消耗）
  - [ ] 积分到期提醒
- [ ] 修改生成应用页面：
  - [ ] 显示预估消耗积分
  - [ ] 积分不足时显示获取积分引导

### 阶段八：测试和优化（1-2天）
- [ ] 单元测试：
  - [ ] PointsService所有方法覆盖率>80%
  - [ ] SignInService连续签到逻辑测试
  - [ ] InviteService防刷逻辑测试
- [ ] 集成测试：
  - [ ] 完整签到流程测试
  - [ ] 完整邀请流程测试
  - [ ] 生成应用扣减积分流程测试
- [ ] 性能测试：
  - [ ] 积分并发扣减测试（防止超扣）
  - [ ] 签到高并发测试
- [ ] 数据修复脚本：
  - [ ] 为现有用户初始化积分账户
  - [ ] 赠送老用户初始积分（如100积分）

---

## 五、用户激励机制

### 1. 新手引导
- 新用户注册后自动弹窗介绍积分系统（获得30积分）
- 引导用户完成首次签到（获得5积分，共35积分）
- 引导用户完成首次生成（使用注册赠送的30积分，完成后再奖励30积分）

### 2. 每日任务
- 每日签到：5积分（连续签到有额外奖励）
- 每日首次生成应用：3积分
- 每日分享应用到社交媒体：2积分（可选）

### 3. 成就系统（可选，后期扩展）
- 连续签到30天：勋章+100积分
- 生成100个应用：勋章+200积分
- 成功邀请10个用户：勋章+500积分

### 4. 积分商城（可选，后期扩展）
- 积分兑换会员特权（如更高的Token上限）
- 积分兑换高级模型使用权（如GPT-4）
- 积分兑换虚拟礼品或实物奖品

---

## 六、监控和告警

### 1. Prometheus监控指标
```
# 积分发放总量（按类型）
ai_code_points_granted_total{type="sign_in|register|invite"}

# 积分消耗总量
ai_code_points_consumed_total

# 积分过期总量
ai_code_points_expired_total

# 无效生成次数
ai_code_invalid_generation_total{reason="empty|duplicate|security"}

# 单日Token消耗超限用户数
ai_code_token_limit_exceeded_users
```

### 2. Grafana告警规则
- 单用户单小时积分消耗超过500积分 → 可能存在刷量
- 单IP单小时注册超过5个账号 → 可能存在批量注册
- 无效生成次数超过总生成次数的30% → 需要优化生成质量

---

## 七、实现可行性分析

### 1. 复用现有能力
- **Token统计**：直接使用`AiModelMetricsCollector.recordTokenUsage`获取真实Token消耗
- **限流**：复用`RateLimitAspect`的限流能力
- **数据库**：复用MyBatis-Flex和MySQL
- **缓存**：复用Redis和Caffeine
- **监控**：复用Prometheus和Grafana

### 2. 技术风险
- **积分并发扣减**：使用Redis分布式锁或数据库乐观锁防止超扣
- **邀请防刷**：结合IP、设备ID、用户行为多维度检测
- **积分过期**：使用定时任务扫描，考虑大数据量下的性能优化（分批处理）

### 3. 开发工时估算
- 总工时：约12-15个工作日
- 核心功能（阶段一~五）：8-10天
- 前端集成（阶段七）：2-3天
- 测试和优化（阶段八）：2天

---

## Review

### 本次修改内容
1. **简化设计**：
   - 去除"多退少补"机制，改为预扣固定积分（按应用类型）
   - 去除邀请奖励的阶梯机制，改为一次性奖励+首次生成奖励
   - 去除阶梯式积分成本，使用统一兑换比例（1积分=100Token）

2. **增强可执行性**：
   - 补充完整的数据库表设计（4张表，包含索引和注释）
   - 细化开发任务列表（8个阶段，共30+子任务）
   - 明确每个阶段的工时估算

3. **强化防刷机制**：
   - 邀请功能增加IP/设备检测和单日次数限制
   - 无效生成增加惩罚机制（扣分+警告+禁止）
   - Token消耗设置单日上限（50万Token）

4. **优化用户体验**：
   - 积分有效期从30天延长到90天，并增加到期提醒
   - 新用户注册赠送50积分，可体验1-3次生成
   - 连续签到奖励递增，增强用户粘性

### 核心改进点
- **逻辑更简单**：去除复杂的多退少补和阶梯机制，降低实现难度
- **防刷更有效**：多维度检测（IP、设备、行为），多层次限制（限流、上限、惩罚）
- **激励更明确**：签到奖励递增、邀请奖励分两次发放，引导用户持续使用
- **可执行性强**：详细的任务列表和数据库设计，可直接进入开发

### 待确认事项
1. ✅ **已根据实际数据调整**：积分兑换比例从1:100调整为1:1000，降低积分膨胀
2. ✅ **已调整**：单日Token上限从50万降低到12万（约20次生成），更符合正常使用场景
3. ✅ **已优化**：邀请奖励调整为注册20积分+首次生成30/10积分，分阶段激励
4. **待定**：是否需要成就系统和积分商城（可作为V2.0功能）

### 新积分体系核心数据
| 场景 | 积分变化 | 可生成次数 | 备注 |
|------|---------|-----------|------|
| 新用户注册 | +30积分 | 5次 | 鼓励体验 |
| 首次生成成功 | +30积分 | 5次 | 完成新手任务 |
| 每日签到 | +5积分 | 1次 | 基础奖励 |
| 连续签到3天 | +8积分 | 1.3次 | 递增奖励 |
| 连续签到7天 | +15积分 | 2.5次 | 递增奖励 |
| 连续签到30天 | +55积分 | 9次 | 高额奖励 |
| 邀请用户注册 | +20积分 | 3次 | 邀请人和新用户各得 |
| 邀请用户首次生成 | 邀请人+30，新用户+10 | 5次/1.6次 | 完成邀请任务 |
| 生成应用（平均） | -6积分 | - | 实际扣减 |
| 单日Token上限 | 120积分 | 20次 | 防刷限制 |

### 用户获取积分路径分析
- **纯签到用户**：每月最低150积分（30天×5积分），可生成25次，满足轻度用户需求
- **活跃用户**：连续签到30天+每日生成，每月约210积分（150基础+60连续奖励），可生成35次
- **邀请用户**：邀请3个用户并完成首次生成，额外获得150积分，可生成25次
- **新用户**：注册30+首次生成30=60积分，可体验10次生成，足够了解产品


### 2024-12-12 用户积分系统检查记录
- **后端覆盖**：`UserPointsService`、`SignInRecordService`、`InviteRecordService`、`GenerationValidationService` 等已实现账户初始化、签到奖励、邀请奖励、首次生成奖励、预扣积分、日限额校验，并配套 `PointsExpireScheduler` 执行90天过期清理（src/main/java/com/spring/aicodemother/service/impl/UserPointsServiceImpl.java:34、SignInRecordServiceImpl.java:21、InviteRecordServiceImpl.java:31、GenerationValidationServiceImpl.java:19、schedule/PointsExpireScheduler.java:23）。对应数据表在 `sql/create_table.sql` 中就绪。
- **前端现状**：已提供签到、积分概览、邀请页及头部积分展示（ai-code-mother-frontend/src/pages/points/SignInPage.vue:1、PointsDetailPage.vue:1、InvitePage.vue:1、components/PointsDisplay.vue:1），但 `getSignInStatus` 仅返回是否签到与连续天数，页面期待的 `signedDates` 暂为空，签到日历无法高亮历史；暂无前端入口处理积分即将过期或奖励回收提示。
- **主要缺口**：
  1. 生成结果有效性校验未落地，`GenerationValidationService.validateGenerationResult` 未被调用，无法按“生成内容为空/不足100字符/安全违规”执行惩罚。
  2. 邀请回收策略缺失——未实现“被邀请用户7天内无操作回收积分”逻辑，对应定时任务或事件尚未编写。
  3. 积分到期前7天提醒缺位，当前仅有过期扣减任务。
  4. Token消耗统计采用固定6000值，未接入 `AiModelMetricsCollector` 实际 token 数据，多退少补也未实现。
  5. `PointsMetricsCollector.recordTokenLimitExceeded` 等监控指标未被触发，缺乏运营层可视化。
- **建议**：补充生成结果落地校验和邀请回收流程，完善积分即将过期通知及监控指标，视需求考虑按生成类型动态扣分与实际 token 结算。


## 积分系统缺口修复 TODO
- [x] 接入生成结果有效性校验，失败时触发惩罚与返还逻辑调整
- [x] 实现邀请奖励回收（被邀请人7天内无操作扣回积分）
- [x] 增加积分到期前提醒机制
- [x] 接入真实 Token 消耗数据并调整预扣/结算策略
- [x] 扩展签到状态接口返回历史签到日期，修复前端日历显示
- [x] 新增管理员批量补发积分能力（`POST /admin/data-repair/grant-admin-points`）
- [x] 个人主页新增邀请信息卡片，展示邀请码与邀请链接
- [x] 管理员账户积分视为无限，跳过扣减与余额校验

## 仓库托管 TODO
- [ ] 确认 Git 仓库当前状态并梳理需提交的改动
- [ ] 按照既定步骤提交所有本地改动
- [ ] 推送 `main` 分支至远程 `origin`
- [ ] 校验远程仓库分支同步情况
- [ ] 在 Review 部分记录本次上传操作


## 邀请注册积分 BUG 修复 TODO
- [x] 查明邀请链接注册未发放积分的原因（前端注册表单未携带邀请码，后端无法触发奖励）
- [x] 修复注册流程，确保邀请码随请求提交
- [x] 自检修复效果并记录验证思路（确认表单载入邀请参数并运行 `npm run type-check`，但受既有类型错误影响需后续单独修复）


## 邀请记录展示 BUG 修复 TODO
- [x] 过滤邀请占位记录，避免“用户0”出现在邀请记录与统计
- [x] 自检前端统计展示恢复正常（调用 `/invite/records` 应仅返回真实邀请记录，可在控制台确认 `inviteeId>0`）


## AGENTS.md 更新计划
- [ ] 重写 AGENTS.md，更新贡献者指南内容满足最新要求


### Review
- 更新 AGENTS.md，重写贡献者指南并补充关键命令与规范
- 检查单词数约391词，满足200-400词要求
- 接入积分系统缺口修复：生成结果校验、邀请回收、到期提醒、Token实耗结算、签到日历补全
- 修复邀请注册链接未携带邀请码，确保注册时触发奖励逻辑（ai-code-mother-frontend/src/pages/user/UserRegisterPage.vue:1、ai-code-mother-frontend/src/api/typings.d.ts:465）
- 自检执行 `npm run type-check` 暂未通过（项目原有类型错误待后续处理）
- 调整邀请积分发放链路，避免邀请码占位记录与邀请记录冲突（src/main/java/com/spring/aicodemother/service/impl/InviteRecordServiceImpl.java:48、src/main/java/com/spring/aicodemother/service/impl/InviteRecordServiceImpl.java:155）
