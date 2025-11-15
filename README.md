# AI代码生成器 (AI Code Mother)

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5.17-blue" alt="Vue">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
  <img src="https://img.shields.io/badge/Version-1.1.0-blue" alt="Version">
</p>

<p align="center">
  <a href="https://joinoai.cloud/user/register?inviteCode=INVNJZKYSJH">🌐 在线预览</a> •
  <a href="https://github.com/vasc-language/ai-code-mother">📦 GitHub</a> •
  <a href="https://gitee.com/vasc-language/ai-code-mother">📦 Gitee</a> •
  <a href="deployment/docs/DEPLOYMENT.md">📖 部署文档</a>
</p>

一个功能强大的全栈 AI 代码生成平台，基于 Spring Boot 3.5.4 和 Vue 3 构建，集成 DeepSeek、Qwen 等多种 AI 模型，专注于 Vue 3 项目的智能化生成服务。支持 AI 模型动态选择、两阶段代码生成、代码自动验证等先进功能。

## 🚀 项目概述

AI Code Mother 是一个现代化的 AI 驱动代码生成平台，旨在通过人工智能技术简化和加速 Vue 3 应用开发流程。系统专注于 Vue 3 项目的智能化生成，通过动态模型选择、两阶段代码生成、自动代码验证等先进功能，为开发者提供高效、智能、可靠的代码生成解决方案。

### ✨ 核心特性

#### AI 智能生成
- **🤖 动态模型选择**: 支持 DeepSeek、Qwen、OpenAI 等 24+ 个 AI 模型，根据任务复杂度智能选择
- **📋 两阶段生成**: 计划生成 → 用户确认 → 代码生成，避免无效生成节省成本
- **✅ 代码自动验证**: 集成 ESLint 代码质量检查，自动生成代码审查报告
- **🎯 Vue 3 专注**: 专为 Vue 3 项目优化，支持 Composition API、TypeScript、Pinia 等现代特性
- **🔄 实时流式输出**: 基于 SSE (Server-Sent Events) 的实时代码生成进度展示

#### 完善的用户体系
- **📧 邮箱认证**: 支持邮箱注册登录，邮件验证码双因素认证，提升账户安全性
- **🎁 积分系统**: 完善的积分获取、消耗、过期机制（已修复 8 个 P0-P3 级别缺陷）
- **👥 邀请机制**: 邀请码系统，邀请双方均可获得积分奖励，配套防刷机制
- **📦 版本管理**: 完整的应用版本历史记录、版本对比、一键回滚功能

#### 企业级运维
- **🚀 一键部署**: 规范的 deployment/ 目录，支持 Ubuntu 服务器一键部署
- **🔄 备份回滚**: 自动备份数据库、应用、配置，支持交互式回滚
- **📊 监控系统**: 集成 Prometheus 指标监控，支持 Grafana 可视化
- **🛡️ 安全加固**: 支持 OpenResty 限流、WAF 防护、日志轮转

#### 云端集成
- **☁️ 腾讯云 COS**: 生成文件自动上传云端，CDN 加速访问
- **🔧 智能工具**: 网页截图、项目下载、代码预览、差异对比等实用功能
- **📱 全栈架构**: Spring Boot 后端 + Vue 3 前端的现代化全栈解决方案

## 📑 目录

- [技术架构](#-技术架构)
- [核心功能](#-核心功能)
- [项目结构](#-项目结构)
- [环境要求](#-环境要求)
- [快速开始](#-快速开始)
- [用户指南](#-用户指南)
- [开发指南](#-开发指南)
- [API文档](#-api文档)
- [部署指南](#-部署指南)
- [常见问题](#-常见问题)
- [贡献指南](#-贡献指南)

## 🛠️ 技术架构

### 后端技术栈 (Spring Boot)

**核心框架**
- **Spring Boot 3.5.4**: 基于Java 21的现代化Spring应用框架
- **Spring Web**: RESTful API开发
- **Spring AOP**: 切面编程支持，用于权限校验和日志记录
- **Spring Session**: 分布式会话管理，支持Redis存储

**AI集成**
- **LangChain4j 1.1.0**: Java AI 应用开发框架，支持工具调用和流式输出
- **LangGraph4j 1.6.0**: AI 工作流编排引擎，支持复杂任务编排
- **动态模型工厂**: 支持 24+ 个 AI 模型的动态选择和配置
  - **DeepSeek Reasoner**: 高级推理模型，用于复杂代码生成
  - **DeepSeek Chat**: 标准编码模型，快速响应
  - **Qwen Turbo**: 快速分类和简单任务处理
  - **OpenAI GPT-4/3.5**: 备选模型支持
- **AI 模型等级系统**: 基础编码、标准编码、高级编码、基础推理、高级推理 5 个等级
- **Reactor**: 响应式编程支持 Server-Sent Events 流式输出

**数据层**
- **MyBatis-Flex 1.11.0**: 灵活的MyBatis增强ORM框架
- **MySQL**: 主数据库，存储用户、应用、版本、积分等数据
- **HikariCP**: 高性能数据库连接池
- **雪花算法**: 分布式唯一ID生成策略

**缓存与会话**
- **Redis 6.0+**: 会话存储、邮箱验证码缓存
- **Caffeine**: 高性能本地缓存，优化热点数据访问
- **Redisson 3.50.0**: 分布式锁、高级Redis客户端

**监控与运维**
- **Spring Boot Actuator**: 应用监控和管理端点
- **Micrometer + Prometheus**: 指标收集和监控
- **Grafana**: 监控数据可视化(可选)
- **定时任务**: 积分过期、邀请奖励自动发放

**文档与工具**
- **Knife4j 4.4.0**: Swagger UI增强版API文档
- **Hutool 5.8.38**: Java工具类库
- **Lombok 1.18.36**: 代码简化注解

**云服务集成**
- **腾讯云COS 5.6.227**: 对象存储服务，用于文件存储和CDN加速
- **Spring Mail**: 邮件发送服务，用于验证码和通知

**Web自动化**
- **Selenium 4.33.0**: 网页自动化和截图
- **WebDriverManager 6.1.0**: 浏览器驱动自动管理

### 前端技术栈 (Vue 3)

**核心框架**
- **Vue 3.5.17**: 渐进式JavaScript框架，使用Composition API
- **TypeScript 5.8.0**: 类型安全的JavaScript超集
- **Vite 7.0.0**: 下一代前端构建工具，极速热重载

**UI与组件**
- **Ant Design Vue 4.2.6**: 企业级UI组件库
- **Vue Router 4.5.1**: 官方路由管理器
- **Pinia 3.0.3**: 新一代状态管理库

**HTTP与API**
- **Axios 1.11.0**: Promise based HTTP客户端
- **OpenAPI Generator**: 从后端OpenAPI规范自动生成TypeScript客户端

**内容渲染**
- **Markdown-it 14.1.0**: Markdown解析和渲染引擎
- **highlight.js 11.11.1**: 语法高亮显示库，支持200+编程语言

**开发工具**
- **ESLint**: JavaScript/TypeScript代码检查
- **Prettier**: 代码格式化工具
- **Vue DevTools**: Vue调试工具
- **Vue TSC**: Vue TypeScript类型检查

### 数据库设计

系统包含 10 个核心数据表：

- **user**: 用户信息表，支持邮箱注册、邮箱验证
- **user_points**: 用户积分表，记录累计积分和可用积分
- **points_record**: 积分记录表，记录积分获取和消耗明细（新增 status 字段支持积分状态管理）
- **invite_record**: 邀请关系表，记录邀请人、被邀请人及奖励状态
- **sign_in_record**: 签到记录表，记录用户签到历史
- **app**: 应用信息表，存储用户创建的应用配置（新增 modelKey 字段支持动态模型选择）
- **app_version**: 应用版本表，记录每次应用修改的版本历史（新增 codeContentUrl 字段）
- **chat_history**: 聊天记录表，存储用户与 AI 的对话历史
- **email_verification_code**: 邮箱验证码表，临时存储验证码用于注册登录
- **ai_model_config**: AI 模型配置表，存储所有可用 AI 模型的配置信息（🆕 v1.1.0）

**技术特性**
- 雪花算法ID生成策略
- 软删除支持(逻辑删除)
- 时间戳自动管理
- 索引优化查询性能

## 🎯 核心功能

### 1. AI 代码生成引擎

**🆕 动态模型选择系统**
- **24+ AI 模型支持**: DeepSeek、Qwen、OpenAI 等主流模型
- **5 级模型分类**: 基础编码、标准编码、高级编码、基础推理、高级推理
- **智能模型推荐**: 根据项目复杂度自动推荐合适的模型
- **质量系数显示**: 前端显示模型质量系数（替代倍率），帮助用户选择
- **实时成本预估**: 根据选择的模型预估积分消耗

**🆕 两阶段代码生成流程**
1. **阶段一：计划生成**
   - AI 分析需求，生成详细的开发计划
   - 包含文件列表、功能模块、技术选型等
   - 用户可预览和修改计划
2. **阶段二：代码生成**
   - 用户确认计划后开始生成代码
   - 支持基于计划的增量生成
   - 避免无效生成，节省 Token 成本

**🆕 代码自动验证**
- **ESLint 集成**: 自动执行 ESLint 代码质量检查
- **Vue 项目结构验证**: 验证项目结构是否符合 Vue 3 规范
- **自动生成审查报告**: 包含代码质量评分、错误列表、修复建议
- **构建前验证**: 在项目构建前发现潜在问题

**生成类型支持**
- **Vue 3 项目生成**: 专注于 Vue 3 完整前端应用（已移除 HTML 和多文件项目支持）
  - 支持 Composition API、TypeScript、Pinia
  - 自动配置路由、状态管理、UI 组件库
  - 优化的项目结构和代码组织

**实时流式输出**
- 基于 Server-Sent Events (SSE) 的实时推送
- 实时展示 AI 思考过程和代码生成进度
- 支持生成过程中的取消操作
- 断线自动重连机制
- 🆕 优化 ChatMemory 管理，避免上下文过长（性能提升 50%+）

**代码优化**
- 自动代码格式化
- 语法检查和修正
- 最佳实践建议
- 性能优化提示
- 🆕 重复写入检测和警告

### 2. 版本管理系统

**版本历史记录**
- 自动记录每次应用修改的版本快照
- 保存完整的应用配置、生成内容、文件列表
- 记录版本创建时间和操作人
- 支持版本列表分页查询

**版本回滚功能**
- 一键回滚到任意历史版本
- 回滚操作会创建新版本记录
- 保持完整的版本变更链
- 仅应用创建者可执行回滚

**版本对比**
- 查看不同版本之间的差异
- 对比应用配置变化
- 追溯功能变更历史

**权限控制**
- 仅应用创建者可查看版本历史
- 仅应用创建者可执行版本回滚
- 版本数据逻辑删除，可恢复

### 3. 积分系统（已修复 8 个 P0-P3 级别缺陷）

**积分获取机制**
- **注册奖励**: 新用户注册赠送初始积分
- **邀请奖励**: 成功邀请好友注册，邀请双方均获得积分
- **每日签到**: 每日首次登录获得签到积分（可扩展）
- **活动奖励**: 参与平台活动获得额外积分（可扩展）

**积分消耗场景**
- **AI 代码生成**: 根据选择的 AI 模型消耗相应积分（支持质量系数计费）
- **高级功能**: 使用高级功能（如大规模项目生成）消耗更多积分
- **项目部署**: 部署生成的项目到云端消耗积分

**🆕 积分状态管理**
- **PENDING**: 待处理 - 积分操作已创建但未确认
- **COMPLETED**: 已完成 - 积分成功增加或扣除
- **FAILED**: 已失败 - 积分操作失败，需要回滚
- **REFUNDED**: 已退款 - 失败任务的积分已退还

**🆕 积分一致性保障**
- **一致性检查器**: 定期检查积分记录与用户积分余额是否一致
- **自动修复机制**: 发现不一致时自动修复或报警
- **防重复扣费**: 使用分布式锁防止并发扣费
- **事务保障**: 积分操作使用数据库事务确保原子性

**积分管理**
- **累计积分**: 记录用户历史获得的总积分
- **可用积分**: 当前可使用的积分余额
- **冻结积分**: 预留字段，支持积分冻结功能
- **积分明细**: 完整的积分获取和消耗记录

**积分过期机制**
- 定时任务自动检查过期积分
- 支持设置积分有效期（如 180 天）
- 过期积分自动扣除
- 过期通知提醒（可扩展）

**🆕 积分监控优化**
- Prometheus 指标收集（已修复跨线程问题）
- 积分获取/消耗趋势统计
- 用户积分分布分析
- 异常积分变动预警
- Token 使用量监控

### 4. 邀请机制

**邀请码系统**
- 每个用户拥有唯一邀请码
- 邀请码基于用户ID生成，永久有效
- 支持邀请链接一键分享
- 邀请码区分大小写

**邀请奖励**
- **邀请人奖励**: 成功邀请好友注册，获得积分奖励
- **被邀请人奖励**: 通过邀请码注册，同样获得积分奖励
- **双向激励**: 促进用户主动分享和推广
- **自动发放**: 定时任务自动检测并发放奖励

**防刷机制**
- **IP检测**: 记录注册IP，同一IP多次注册预警
- **设备ID检测**: 通过浏览器指纹识别设备
- **时间窗口**: 短时间内大量邀请触发人工审核
- **人工审核**: 可疑邀请关系需人工确认后发放奖励

**邀请状态管理**
- **PENDING**: 待确认 - 邀请链接已创建
- **REGISTERED**: 已注册 - 被邀请人完成注册
- **REWARDED**: 已奖励 - 积分已成功发放
- **状态流转**: 自动化状态更新和通知

**邀请统计**
- 邀请人数统计
- 成功注册人数
- 奖励积分总额
- 邀请转化率分析

### 5. 用户认证系统

**邮箱注册登录**
- 邮箱验证码注册
- 邮箱验证码登录
- 邮箱唯一性验证
- 密码MD5加密(加盐: Join2049)

**会话管理**
- 基于Redis的分布式会话
- 30天会话有效期
- 自动登录状态维护
- 安全退出登录

**权限控制**
- 基于角色的访问控制(user/admin)
- `@AuthCheck`注解权限校验
- 仅创建者可修改应用
- 管理员拥有全部权限

### 6. 项目管理系统

**应用CRUD**
- 创建新应用(配置名称、描述、生成类型)
- 查询应用列表(分页、搜索、过滤)
- 更新应用配置
- 删除应用(逻辑删除)

**生成类型**
- **Vue 3 项目生成**: 专注于 Vue 3 完整前端应用（v1.1.0+ 已聚焦 Vue 项目）
  - 支持 Composition API、TypeScript、Pinia
  - 自动配置路由、状态管理、UI 组件库
  - 优化的项目结构和代码组织

**应用部署**
- 生成的项目自动上传到腾讯云COS
- 生成唯一的部署访问链接
- 支持自定义域名访问
- CDN加速文件访问

**批量操作**
- 批量删除应用
- 批量导出应用配置
- 批量部署项目

### 7. 实时通信系统

**SSE流式传输**
- 基于Server-Sent Events的单向推送
- 实时显示AI代码生成进度
- 自动处理连接中断和重连
- 支持多客户端同时接收

**聊天历史**
- 完整的AI对话历史记录
- 支持历史会话查询
- 上下文关联显示
- 聊天记录导出(可扩展)

**实时通知**
- 积分变动实时通知
- 邀请成功实时通知
- 系统消息推送

### 8. 云存储集成

**腾讯云COS**
- 生成文件自动上传云端
- 支持大文件分片上传
- 自动生成访问URL
- CDN加速文件分发

**文件管理**
- 文件列表查看
- 文件下载
- 文件删除
- 存储空间统计

**截图功能**
- 基于Selenium的网页截图
- 支持全页面截图
- 自动压缩和优化
- 截图结果云端存储

### 9. 监控与运维

**Prometheus指标**
- HTTP请求统计
- AI模型调用次数
- 数据库查询性能
- 缓存命中率
- 积分系统指标
- 邀请系统指标

**健康检查**
- 应用健康状态检查
- 数据库连接检查
- Redis连接检查
- 外部API可用性检查

**日志管理**
- 结构化日志记录
- 按级别分类(DEBUG/INFO/WARN/ERROR)
- 异常堆栈追踪
- 请求日志记录

## 📦 项目结构

```
ai-code-mother/
├── src/main/java/com/spring/aicodemother/    # 后端源码
│   ├── ai/                                   # AI 模型集成和服务
│   │   ├── AiManager.java                    # AI 模型管理器
│   │   ├── DynamicAiModelFactory.java        # 🆕 动态模型工厂
│   │   ├── AiCodeGeneratorServiceFactory.java # 🆕 AI 服务工厂
│   │   ├── MessageStreamHandler.java         # 流式消息处理
│   │   ├── model/                            # AI 请求响应模型
│   │   └── tools/                            # 🆕 AI 工具调用
│   │       ├── CodeValidationTool.java       # 🆕 代码验证工具
│   │       └── VueProjectStructureValidationTool.java  # 🆕 结构验证工具
│   ├── controller/                           # REST API 控制器
│   │   ├── AppController.java                # 应用管理
│   │   ├── AppVersionController.java         # 版本管理
│   │   ├── UserController.java               # 用户管理
│   │   ├── PointsController.java             # 积分管理
│   │   ├── InviteController.java             # 邀请管理
│   │   ├── AiModelController.java            # 🆕 AI 模型管理
│   │   └── HealthController.java             # 健康检查
│   ├── service/                              # 业务逻辑层
│   │   ├── AppService.java                   # 应用服务
│   │   ├── AppVersionService.java            # 版本服务
│   │   ├── UserService.java                  # 用户服务
│   │   ├── UserPointsService.java            # 积分服务
│   │   ├── PointsRecordService.java          # 积分记录服务
│   │   ├── InviteRecordService.java          # 邀请服务
│   │   ├── ChatService.java                  # 聊天服务
│   │   ├── AiModelConfigService.java         # 🆕 AI 模型配置服务
│   │   ├── AiPlanningService.java            # 🆕 AI 计划生成服务
│   │   ├── PlanCacheService.java             # 🆕 计划缓存服务
│   │   └── GenerationValidationService.java  # 🆕 生成验证服务
│   ├── mapper/                               # 数据访问层
│   │   └── *.java                            # MyBatis-Flex Mapper
│   ├── model/                                # 数据模型
│   │   ├── entity/                           # 实体类
│   │   ├── dto/                              # 数据传输对象
│   │   ├── vo/                               # 视图对象
│   │   └── enums/                            # 枚举类型
│   ├── core/                                 # 核心业务逻辑
│   │   ├── CodeGenerator.java                # 代码生成核心
│   │   ├── FileParser.java                   # 文件解析器
│   │   └── FileSaver.java                    # 文件保存器
│   ├── config/                               # 配置类
│   │   ├── CorsConfig.java                   # 跨域配置
│   │   ├── RedisConfig.java                  # Redis配置
│   │   └── CosClientConfig.java              # COS配置
│   ├── utils/                                # 工具类
│   │   └── ScreenshotUtils.java              # 截图工具
│   ├── manager/                              # 第三方服务管理
│   │   └── CosManager.java                   # COS管理器
│   ├── schedule/                             # 定时任务
│   │   ├── PointsExpireScheduler.java        # 积分过期任务
│   │   ├── PointsConsistencyChecker.java     # 🆕 积分一致性检查
│   │   └── InviteRewardScheduler.java        # 邀请奖励任务
│   ├── monitor/                              # 监控指标
│   │   ├── PointsMetricsCollector.java       # 积分指标收集
│   │   ├── AiModelMetricsCollector.java      # 🆕 AI 模型监控
│   │   ├── MonitorContext.java               # 🆕 监控上下文
│   │   └── MonitorContextHolder.java         # 🆕 上下文持有者（修复跨线程问题）
│   ├── exception/                            # 异常处理
│   │   ├── GlobalExceptionHandler.java       # 全局异常处理器
│   │   ├── BusinessException.java            # 业务异常
│   │   └── ErrorCode.java                    # 错误码定义
│   ├── common/                               # 公共类
│   │   ├── BaseResponse.java                 # 统一响应格式
│   │   └── ResultUtils.java                  # 响应工具类
│   └── annotation/                           # 自定义注解
│       └── AuthCheck.java                    # 权限校验注解
│
├── src/main/resources/                       # 资源文件
│   ├── application.yml                       # 应用配置
│   ├── application-dev.yml                   # 开发环境配置
│   ├── application-prod.yml                  # 生产环境配置
│   └── prompt/                               # AI提示词模板
│       ├── html_generator.txt                # HTML生成提示词
│       ├── multifile_generator.txt           # 多文件生成提示词
│       └── vue_generator.txt                 # Vue生成提示词
│
├── ai-code-mother-frontend/                  # 前端源码
│   ├── src/
│   │   ├── main.ts                           # 应用入口
│   │   ├── App.vue                           # 根组件
│   │   ├── request.ts                        # Axios配置
│   │   ├── layouts/                          # 布局组件
│   │   │   └── BasicLayout.vue               # 基础布局
│   │   ├── components/                       # 可复用组件
│   │   │   ├── GlobalHeader.vue              # 全局头部
│   │   │   ├── GlobalFooter.vue              # 全局底部
│   │   │   ├── AiModelSelector.vue           # 🆕 AI 模型选择器（544 行）
│   │   │   ├── DeployingModal.vue            # 🆕 部署中弹窗
│   │   │   ├── DeploySuccessModal.vue        # 部署成功弹窗
│   │   │   ├── PreviewLoading.vue            # 🆕 预览加载组件
│   │   │   ├── DiffViewer.vue                # 🆕 差异查看器
│   │   │   ├── CodeHighlight.vue             # 代码高亮组件
│   │   │   └── MarkdownRenderer.vue          # Markdown 渲染器
│   │   ├── pages/                            # 页面组件
│   │   │   ├── HomePage.vue                  # 首页
│   │   │   ├── app/                          # 应用管理页面
│   │   │   │   ├── AppChatPage.vue           # 🆕 应用聊天页（重构为模块化）
│   │   │   │   ├── components/               # 🆕 应用组件
│   │   │   │   │   ├── AppHeaderBar.vue      # 应用头部
│   │   │   │   │   ├── ChatPanel.vue         # 聊天面板
│   │   │   │   │   └── CodePreviewPanel.vue  # 代码预览面板
│   │   │   │   ├── composables/              # 🆕 组合式函数
│   │   │   │   │   ├── useAppDeployment.ts   # 部署逻辑
│   │   │   │   │   ├── useAppInfo.ts         # 应用信息
│   │   │   │   │   ├── useChatMessages.ts    # 消息管理
│   │   │   │   │   ├── useCodeGeneration.ts  # 代码生成（605 行）
│   │   │   │   │   ├── useVersionManagement.ts # 版本管理
│   │   │   │   │   └── useVisualEditor.ts    # 可视化编辑
│   │   │   │   └── utils/                    # 工具函数
│   │   │   │       └── contentFilters.ts     # 🆕 内容过滤器
│   │   │   ├── user/                         # 用户相关页面
│   │   │   │   ├── LoginPage.vue             # 登录页
│   │   │   │   ├── RegisterPage.vue          # 注册页
│   │   │   │   └── UserManagePage.vue        # 用户管理
│   │   │   ├── points/                       # 积分相关页面
│   │   │   │   ├── PointsPage.vue            # 积分中心
│   │   │   │   └── InvitePage.vue            # 邀请页面
│   │   │   └── docs/                         # 文档页面
│   │   │       ├── QuickStartDoc.vue         # 快速开始
│   │   │       ├── APIDoc.vue                # API 文档
│   │   │       ├── FAQDoc.vue                # 常见问题
│   │   │       ├── features/                 # 功能文档
│   │   │       │   ├── AIGenerationDoc.vue   # AI 生成文档
│   │   │       │   └── PointsSystemDoc.vue   # 积分系统文档
│   │   │       └── tutorial/                 # 教程目录
│   │   ├── router/                           # 路由配置
│   │   │   └── index.ts                      # 路由定义
│   │   ├── stores/                           # Pinia 状态管理
│   │   │   ├── user.ts                       # 用户状态
│   │   │   └── app.ts                        # 应用状态
│   │   ├── api/                              # API 客户端（自动生成）
│   │   │   ├── aImoxingpeizhi.ts            # 🆕 AI 模型配置 API
│   │   │   └── appController.ts              # 应用控制器 API
│   │   ├── assets/                           # 静态资源
│   │   │   ├── Deploy.svg                    # 🆕 部署图标
│   │   │   ├── Online search.svg             # 🆕 在线搜索图标
│   │   │   ├── ToolsCall.svg                 # 🆕 工具调用图标
│   │   │   ├── refresh.svg                   # 🆕 刷新图标
│   │   │   └── (共 18 个 SVG 图标)           # Deploy、ToolsCall、thinking 等
│   │   └── styles/                           # 🆕 样式文件
│   │       ├── lovable-theme.css             # Lovable 主题
│   │       └── lovable-theme-override.css    # 主题覆盖
│   ├── public/                               # 公共资源
│   ├── .env.development                      # 开发环境变量
│   ├── .env.production                       # 生产环境变量
│   └── package.json                          # 依赖配置
│
├── deployment/                               # 🆕 部署包目录
│   ├── backend/                              # 后端 JAR 包
│   ├── frontend/                             # 前端构建产物
│   ├── config/                               # 配置文件
│   │   ├── nginx.conf                        # Nginx 配置
│   │   ├── openresty.conf                    # OpenResty 配置
│   │   ├── aicodehub.service                 # Systemd 服务
│   │   └── logrotate-aicodehub               # 日志轮转
│   ├── scripts/                              # 部署脚本
│   │   ├── deploy.sh                         # 一键部署
│   │   ├── check_env.sh                      # 环境检查
│   │   ├── backup.sh                         # 备份脚本
│   │   ├── rollback.sh                       # 回滚脚本
│   │   └── service_manager.sh                # 服务管理
│   ├── sql/                                  # 数据库迁移脚本
│   ├── docs/                                 # 部署文档
│   ├── .env.prod.example                     # 环境变量模板
│   └── README.md                             # 部署说明
├── sql/                                      # 数据库脚本
│   ├── create_table.sql                      # 建表脚本
│   ├── add_24_models.sql                     # 🆕 新增 24 个模型
│   ├── fix_model_config_pricing_v2.sql       # 🆕 修复模型定价
│   └── v1.1.0_ai_model_tier_system.sql       # 🆕 模型等级系统
├── tmp/                                      # 临时文件目录
│   ├── code_output/                          # 生成代码输出
│   └── screenshots/                          # 截图输出
├── tasks/                                    # 🆕 任务和计划
│   └── todo.md                               # 任务清单
├── prometheus.yml                            # Prometheus 配置
├── pom.xml                                   # Maven 配置
├── mvnw / mvnw.cmd                          # Maven Wrapper
└── README.md                                 # 项目说明文档
```

## 🔧 环境要求

### 必需环境

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **Java** | 21+ | Spring Boot 3.5.4运行要求 |
| **Node.js** | 18+ | 前端开发和构建环境 |
| **MySQL** | 8.0+ | 主数据库 |
| **Redis** | 6.0+ | 会话存储和缓存 |
| **Chrome/Chromium** | 最新版 | Selenium截图功能依赖 |

### 可选组件

| 组件 | 用途 |
|------|------|
| **Docker** | 容器化部署 |
| **Nginx** | 反向代理和静态文件服务 |
| **Prometheus** | 指标收集和监控 |
| **Grafana** | 监控数据可视化 |

### 开发工具推荐

- **IDE**: IntelliJ IDEA 2023+ / VS Code
- **API测试**: Postman / Apifox
- **数据库工具**: Navicat / DataGrip
- **Git客户端**: SourceTree / GitKraken

## 🚀 快速开始

### 1. 环境准备

```bash
# 检查Java版本
java -version  # 需要Java 21

# 检查Node.js版本
node -version  # 需要18.0+

# 启动MySQL服务
mysql -u root -p
CREATE DATABASE ai_code_mother CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 启动Redis服务
redis-server
```

### 2. 数据库初始化

```bash
# 执行建表脚本
mysql -u root -p ai_code_mother < sql/create_table.sql
```

### 3. 后端启动

```bash
# 克隆项目
git clone [repository-url]
cd ai-code-mother

# 配置数据库连接 (修改 src/main/resources/application-dev.yml)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_code_mother
    username: your_username
    password: your_password

# 配置Redis连接
spring:
  data:
    redis:
      host: localhost
      port: 6379

# 配置AI API密钥
ai:
  deepseek:
    api-key: your-deepseek-api-key
  dashscope:
    api-key: your-dashscope-api-key

# 配置邮件服务
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-email-password

# 编译项目
./mvnw clean compile     # Linux/Mac
mvnw.cmd clean compile   # Windows

# 运行测试
./mvnw test             # Linux/Mac
mvnw.cmd test           # Windows

# 启动应用
./mvnw spring-boot:run     # Linux/Mac
mvnw.cmd spring-boot:run   # Windows
```

### 4. 前端启动

```bash
# 进入前端目录
cd ai-code-mother-frontend

# 安装依赖
npm install

# 生成API客户端
npm run openapi2ts

# 启动开发服务器
npm run dev
```

### 5. 访问应用

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8123/api
- **API文档**: http://localhost:8123/api/doc.html
- **健康检查**: http://localhost:8123/api/health
- **Prometheus指标**: http://localhost:8123/api/actuator/prometheus

## 📖 用户指南

### 注册与登录

**1. 邮箱注册**
1. 访问注册页面
2. 输入邮箱地址
3. 点击"发送验证码"，系统将发送6位数字验证码到邮箱
4. 输入验证码、设置密码、填写用户名
5. 可选：输入邀请码获得额外积分奖励
6. 点击注册，系统自动创建账户并赠送初始积分

**2. 邮箱登录**
1. 访问登录页面
2. 输入注册的邮箱地址
3. 点击"发送验证码"
4. 输入收到的验证码
5. 点击登录即可

**3. 会话管理**
- 登录状态保持30天
- 可随时退出登录
- 多设备登录互不影响

### AI代码生成使用

**1. 创建应用**
1. 登录后进入"应用管理"页面
2. 点击"创建应用"按钮
3. 填写应用信息:
   - 应用名称
   - 应用描述
   - 选择生成类型(HTML/多文件项目/Vue应用)
4. 点击创建

**2. 生成代码**
1. 在应用列表中找到创建的应用
2. 点击"生成代码"按钮
3. 输入详细的需求描述
4. 系统实时显示AI生成过程:
   - AI思考过程
   - 代码生成进度
   - 文件列表
5. 生成完成后可以:
   - 在线预览代码
   - 下载完整项目
   - 部署到云端

**3. 代码预览与下载**
- 支持在线查看生成的代码
- 代码高亮显示
- 支持复制代码
- 一键下载完整项目压缩包

**4. 项目部署**
1. 生成完成后点击"部署"按钮
2. 系统自动将项目上传到腾讯云COS
3. 生成唯一的访问链接
4. 通过链接即可访问部署的项目
5. 支持分享链接给他人访问

### 版本管理使用

**1. 查看版本历史**
1. 进入应用详情页面
2. 点击"版本历史"标签
3. 查看所有历史版本列表，包括:
   - 版本号
   - 创建时间
   - 版本说明
   - 操作人

**2. 版本回滚**
1. 在版本历史列表中找到目标版本
2. 点击"回滚"按钮
3. 确认回滚操作
4. 系统自动恢复应用到该版本状态
5. 回滚操作会创建新的版本记录

**3. 版本对比**
1. 选择两个不同的版本
2. 点击"对比"按钮
3. 查看版本之间的差异:
   - 配置变化
   - 代码变化
   - 功能变更

### 积分系统使用

**1. 查看积分余额**
- 登录后在用户中心查看当前积分
- 累计积分:历史获得的总积分
- 可用积分:当前可使用的积分

**2. 获取积分**
- **注册奖励**: 新用户注册赠送100积分
- **邀请奖励**: 成功邀请好友注册，双方各得50积分
- **每日签到**: 每日首次登录获得10积分(可扩展)
- **完成任务**: 参与平台活动获得额外积分

**3. 消耗积分**
- **AI代码生成**:
  - HTML页面生成: 10积分/次
  - 多文件项目: 30积分/次
  - Vue应用: 50积分/次
- **项目部署**: 20积分/次
- **高级功能**: 根据功能复杂度消耗相应积分

**4. 积分明细**
1. 进入"积分中心"
2. 查看积分获取和消耗记录
3. 每条记录包含:
   - 积分类型(获取/消耗)
   - 积分数量
   - 操作说明
   - 操作时间

**5. 积分有效期**
- 积分有效期为180天
- 即将过期的积分会提前通知
- 过期积分自动扣除

### 邀请好友获得奖励

**1. 获取邀请码**
1. 登录后进入"邀请中心"
2. 系统自动为每个用户生成唯一邀请码
3. 复制邀请码或邀请链接

**2. 分享邀请**
- **方式一**: 分享邀请码，好友注册时输入
- **方式二**: 分享邀请链接，好友点击直接跳转注册页
- **方式三**: 生成邀请海报，分享到社交媒体

**3. 获得奖励**
- 好友通过邀请码/链接完成注册
- 系统自动检测邀请关系
- 邀请人和被邀请人各获得50积分
- 奖励在好友注册后24小时内自动发放

**4. 邀请统计**
1. 进入"邀请中心"
2. 查看邀请数据:
   - 邀请人数
   - 成功注册人数
   - 获得积分总额
   - 邀请排行榜

**5. 防刷规则**
- 同一IP短时间内多次注册会被限制
- 异常邀请行为需人工审核
- 作弊行为将扣除全部奖励积分

## 💻 开发指南

### 后端开发

**编译和构建**
```bash
# 编译项目
./mvnw clean compile

# 打包应用
./mvnw clean package

# 跳过测试打包
./mvnw clean package -DskipTests
```

**运行测试**
```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=UserServiceTest

# 运行特定测试方法
./mvnw test -Dtest=UserServiceTest#testRegister
```

**代码生成**
```bash
# 生成MyBatis-Flex代码
./mvnw compile exec:java -Dexec.mainClass="com.spring.aicodemother.generator.MyBatisCodeGenerator"
```

**热部署**
- 项目使用Spring Boot DevTools支持热部署
- 修改代码后自动重启应用
- 静态资源修改无需重启

### 前端开发

**开发命令**
```bash
# 启动开发服务器
npm run dev

# 类型检查
npm run type-check

# 代码检查和自动修复
npm run lint

# 代码格式化
npm run format

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

**API客户端生成**
```bash
# 从后端OpenAPI规范生成TypeScript类型
npm run openapi2ts

# 注意: 需要后端服务先启动
```

**组件开发规范**
- 使用Composition API
- 使用`<script setup>`语法
- TypeScript严格类型检查
- 组件名使用PascalCase
- Props和Emits明确定义类型

### API开发工作流

**1. 后端API开发**
```java
@RestController
@RequestMapping("/example")
public class ExampleController {

    @PostMapping("/")
    public BaseResponse<ExampleVO> createExample(@RequestBody ExampleDTO dto) {
        // 实现业务逻辑
        return ResultUtils.success(result);
    }
}
```

**2. 更新OpenAPI文档**
- 重启后端服务
- 访问 http://localhost:8123/api/v3/api-docs 确认API已更新

**3. 生成前端API客户端**
```bash
cd ai-code-mother-frontend
npm run openapi2ts
```

**4. 在前端使用API**
```typescript
import { exampleControllerCreateExample } from '@/api/exampleController';

const result = await exampleControllerCreateExample(params);
```

### 数据库开发规范

**1. 实体类定义**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("table_name")
public class Entity implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("column_name")
    private String columnName;

    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}
```

**2. 命名规范**
- 表名: 小写下划线分隔 (user_points)
- 列名: 驼峰命名 (userId, createTime)
- 索引名: idx_表名_列名
- 外键名: fk_表名_关联表名

**3. 必需字段**
- id: 雪花算法主键
- createTime: 创建时间
- updateTime: 更新时间
- isDelete: 逻辑删除标志

### 错误处理规范

**1. 使用统一异常**
```java
// 参数错误
ThrowUtils.throwIf(condition, ErrorCode.PARAMS_ERROR, "错误信息");

// 业务异常
throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败");

// 权限异常
throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
```

**2. 错误码定义**
- 0: 成功
- 40000: 参数错误
- 40100: 未登录
- 40101: 无权限
- 40400: 资源不存在
- 50000: 系统错误
- 50001: 操作失败

**3. 统一响应格式**
```json
{
  "code": 0,
  "data": {},
  "message": "ok"
}
```

### 代码规范

**后端规范**
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 方法注释使用JavaDoc格式
- 异常必须处理或向上抛出
- 避免使用魔法值

**前端规范**
- 遵循Vue 3官方风格指南
- 使用ESLint和Prettier格式化
- 组件Props必须定义类型
- 避免在模板中使用复杂表达式
- API调用统一错误处理

## 📚 API文档

### 核心API端点

#### 应用管理

```
GET    /api/app/list/page          # 分页查询应用列表
POST   /api/app/                   # 创建新应用
GET    /api/app/{id}               # 获取应用详情
PUT    /api/app/                   # 更新应用
DELETE /api/app/{id}               # 删除应用
POST   /api/app/generate/{id}      # 非流式代码生成
GET    /api/app/generate/sse/{id}  # 流式代码生成(SSE)
```

#### 版本管理

```
POST   /api/app/version/list/page  # 分页查询版本列表
GET    /api/app/version/list/{appId} # 查询应用所有版本
GET    /api/app/version/get/{versionId} # 获取版本详情
POST   /api/app/version/rollback   # 版本回滚
```

#### 用户管理

```
POST   /api/user/register          # 用户注册
POST   /api/user/login             # 用户登录
POST   /api/user/logout            # 用户登出
GET    /api/user/current           # 获取当前用户信息
PUT    /api/user/                  # 更新用户信息
POST   /api/user/email/send        # 发送邮箱验证码
```

#### 积分管理

```
GET    /api/points/                # 获取用户积分
GET    /api/points/records         # 查询积分记录
POST   /api/points/add             # 增加积分
POST   /api/points/deduct          # 扣除积分
```

#### 邀请管理

```
GET    /api/invite/code            # 获取邀请码
GET    /api/invite/records         # 查询邀请记录
GET    /api/invite/stats           # 邀请统计
```

#### 系统监控

```
GET    /api/health/                # 健康检查
GET    /api/actuator/prometheus    # Prometheus指标
GET    /api/actuator/health        # 详细健康状态
```

### API调用示例

**1. 用户注册**
```bash
curl -X POST "http://localhost:8123/api/user/register" \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "test@example.com",
    "emailCode": "123456",
    "userPassword": "password123",
    "userName": "测试用户",
    "inviteCode": "INVITE123"
  }'
```

**2. 创建应用**
```bash
curl -X POST "http://localhost:8123/api/app/" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxx" \
  -d '{
    "appName": "我的应用",
    "appDesc": "这是一个测试应用",
    "appType": "html"
  }'
```

**3. AI代码生成(流式)**
```javascript
const eventSource = new EventSource(
  'http://localhost:8123/api/app/generate/sse/123'
);

eventSource.onmessage = (event) => {
  console.log('收到消息:', event.data);
};

eventSource.onerror = (error) => {
  console.error('连接错误:', error);
  eventSource.close();
};
```

**4. 查询积分记录**
```bash
curl -X GET "http://localhost:8123/api/points/records" \
  -H "Cookie: JSESSIONID=xxx"
```

### 错误码说明

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 0 | 成功 | 正常处理响应数据 |
| 40000 | 请求参数错误 | 检查请求参数格式和必填项 |
| 40100 | 未登录错误 | 跳转登录页面 |
| 40101 | 无权限错误 | 提示用户权限不足 |
| 40400 | 资源不存在 | 提示资源已被删除或不存在 |
| 40300 | 禁止访问 | 检查访问权限 |
| 50000 | 系统错误 | 联系管理员或稍后重试 |
| 50001 | 操作失败 | 查看错误消息并重试 |

## 🚢 部署指南

### 🆕 一键部署（推荐）

项目提供完整的生产环境部署方案，支持 Ubuntu 20.04/22.04 LTS 服务器。

**1. 部署包结构**
```
deployment/
├── backend/                     # 后端应用（143MB JAR包）
├── frontend/                    # 前端应用（Vue 3 构建产物）
├── config/                      # 配置文件
│   ├── nginx.conf              # Nginx 配置
│   ├── openresty.conf          # OpenResty 配置（支持限流、WAF）
│   ├── aicodehub.service       # Systemd 服务配置
│   └── logrotate-aicodehub     # 日志轮转配置
├── scripts/                     # 部署脚本
│   ├── deploy.sh               # 一键部署脚本
│   ├── check_env.sh            # 环境检查脚本
│   ├── init_database.sh        # 数据库初始化脚本
│   ├── service_manager.sh      # 服务管理脚本
│   ├── backup.sh               # 备份脚本
│   └── rollback.sh             # 回滚脚本
├── sql/                         # 数据库脚本
├── docs/                        # 完整文档
│   ├── DEPLOYMENT.md           # 完整部署指南（677 行）
│   ├── DEPLOYMENT_CHECKLIST.md # 部署检查清单
│   └── QUICK_REFERENCE.md      # 快速参考手册
├── .env.prod.example           # 环境变量模板（40+ 配置项）
├── README.md                   # 部署包说明
└── REVIEW_REPORT.md            # 部署包审查报告
```

**2. 快速开始**
```bash
# 上传部署包到服务器
scp -r deployment/ user@your-server:/tmp/

# 连接到服务器
ssh user@your-server

# 环境检查
cd /tmp/deployment/scripts
chmod +x *.sh
bash check_env.sh

# 一键部署
sudo bash deploy.sh
```

**3. 部署成功**
部署完成后，访问 `http://你的服务器IP` 即可使用！

详细部署文档请查看：`deployment/docs/DEPLOYMENT.md`

**4. 服务管理**
```bash
# 使用服务管理脚本
cd /tmp/deployment/scripts

# 启动/停止/重启服务
bash service_manager.sh start|stop|restart

# 查看状态和日志
bash service_manager.sh status|logs

# 健康检查
bash service_manager.sh health

# 备份和回滚
bash backup.sh daily          # 每日备份
bash rollback.sh              # 交互式回滚
```

### Docker 部署

**1. 构建镜像**
```bash
# 构建后端镜像
docker build -t ai-code-mother-backend:latest .

# 构建前端镜像
cd ai-code-mother-frontend
docker build -t ai-code-mother-frontend:latest .
```

**2. 使用Docker Compose**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: ai_code_mother
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/create_table.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:6.0
    ports:
      - "6379:6379"

  backend:
    image: ai-code-mother-backend:latest
    ports:
      - "8123:8123"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ai_code_mother
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: your_password
      SPRING_REDIS_HOST: redis
      DEEPSEEK_API_KEY: your-api-key
    depends_on:
      - mysql
      - redis

  frontend:
    image: ai-code-mother-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

**3. 启动服务**
```bash
docker-compose up -d
```

### 传统部署

**1. 后端部署**
```bash
# 打包
./mvnw clean package -DskipTests

# 运行
java -jar target/ai-code-mother-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8123
```

**2. 前端部署**
```bash
# 构建
cd ai-code-mother-frontend
npm run build

# 部署dist目录到Web服务器(Nginx)
cp -r dist/* /var/www/html/
```

**3. Nginx配置**
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8123;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # SSE支持
        proxy_buffering off;
        proxy_cache off;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
    }
}
```

### 生产环境配置

**1. 环境变量配置**
```bash
# 数据库配置
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=ai_code_mother
export DB_USERNAME=your-username
export DB_PASSWORD=your-password

# Redis配置
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379

# AI API密钥
export DEEPSEEK_API_KEY=your-deepseek-api-key
export DASHSCOPE_API_KEY=your-dashscope-api-key

# 邮件配置
export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@example.com
export MAIL_PASSWORD=your-email-password

# COS配置
export COS_SECRET_ID=your-secret-id
export COS_SECRET_KEY=your-secret-key
export COS_REGION=ap-beijing
export COS_BUCKET=your-bucket
```

**2. 性能调优**
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  data:
    redis:
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2

# JVM参数
java -Xms2g -Xmx2g -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -jar app.jar
```

**3. 监控配置**
```yaml
# Prometheus配置 (prometheus.yml)
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ai-code-mother'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8123']
```

**4. 日志配置**
```yaml
logging:
  level:
    root: INFO
    com.spring.aicodemother: DEBUG
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**5. 安全配置**
- 使用HTTPS(SSL证书)
- 配置防火墙规则
- 定期备份数据库
- 设置强密码策略
- 启用访问日志
- 配置限流和防刷

**6. 备份策略**
```bash
# 数据库备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
mysqldump -u root -p ai_code_mother > $BACKUP_DIR/ai_code_mother_$DATE.sql
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

## ❓ 常见问题

### 开发环境问题

**Q: 后端启动失败，提示数据库连接错误**
A:
1. 检查MySQL服务是否启动
2. 验证数据库连接配置(地址、端口、用户名、密码)
3. 确认数据库`ai_code_mother`已创建
4. 检查防火墙是否阻止连接

**Q: 前端启动后无法调用后端API**
A:
1. 确认后端服务已启动(http://localhost:8123/api/health)
2. 检查跨域配置是否正确
3. 清除浏览器缓存
4. 检查浏览器控制台的错误信息

**Q: OpenAPI代码生成失败**
A:
```bash
# Windows清除代理环境变量后重试
set HTTP_PROXY= && set HTTPS_PROXY= && npm run openapi2ts

# Linux/Mac
unset HTTP_PROXY && unset HTTPS_PROXY && npm run openapi2ts
```

**Q: Redis连接失败**
A:
1. 检查Redis服务是否启动: `redis-cli ping`
2. 验证Redis配置(host、port)
3. 检查Redis密码配置
4. 测试环境需要Redis运行才能通过测试

### 功能使用问题

**Q: 邮箱验证码收不到**
A:
1. 检查邮箱地址是否正确
2. 查看垃圾邮件文件夹
3. 验证邮件服务配置(SMTP服务器、端口、凭证)
4. 检查后端日志是否有发送失败记录

**Q: AI代码生成失败**
A:
1. 检查AI API密钥是否配置正确
2. 验证API密钥是否有效和有余额
3. 检查网络连接是否正常
4. 查看后端日志的详细错误信息
5. 确认积分是否足够

**Q: 积分扣除了但代码生成失败**
A:
1. 系统会自动退还失败任务的积分
2. 如未退还,联系管理员人工处理
3. 查看积分记录确认扣除和退还情况

**Q: 邀请好友后没有获得积分**
A:
1. 确认好友已完成注册
2. 邀请奖励在好友注册后24小时内发放
3. 检查是否触发了防刷机制
4. 查看邀请记录的状态

**Q: 版本回滚后数据丢失**
A:
- 版本回滚只恢复应用配置,不影响其他数据
- 回滚操作会创建新版本记录
- 可以再次回滚到之前的版本
- 所有版本数据都保留

### 部署相关问题

**Q: 生产环境启动慢**
A:
1. 调整JVM堆内存参数: `-Xms2g -Xmx2g`
2. 使用G1垃圾回收器: `-XX:+UseG1GC`
3. 优化数据库连接池配置
4. 启用Caffeine本地缓存

**Q: SSE连接频繁断开**
A:
1. 检查Nginx配置是否支持长连接
2. 禁用Nginx缓冲: `proxy_buffering off`
3. 设置合适的超时时间
4. 实现客户端自动重连机制

**Q: 文件上传到COS失败**
A:
1. 检查COS配置(SecretId、SecretKey、Region、Bucket)
2. 验证COS桶权限设置
3. 检查网络连接
4. 查看文件大小是否超限

**Q: Prometheus指标无法收集**
A:
1. 确认Actuator端点已启用
2. 检查Prometheus配置文件
3. 验证网络连接和端口
4. 查看应用日志是否有错误

### 性能优化问题

**Q: 数据库查询慢**
A:
1. 添加合适的索引
2. 优化SQL查询语句
3. 使用分页查询避免全表扫描
4. 启用Redis缓存热点数据
5. 使用Caffeine缓存频繁访问的数据

**Q: 并发量大时系统卡顿**
A:
1. 增加数据库连接池大小
2. 增加Redis连接池大小
3. 调整JVM内存参数
4. 使用负载均衡
5. 优化代码中的同步锁

**Q: 前端首次加载慢**
A:
1. 启用代码分割和懒加载
2. 压缩和优化图片资源
3. 启用Gzip压缩
4. 使用CDN加速静态资源
5. 优化打包配置

### 错误排查技巧

**1. 查看后端日志**
```bash
# 实时查看日志
tail -f logs/application.log

# 搜索错误日志
grep ERROR logs/application.log

# 查看最近100行
tail -n 100 logs/application.log
```

**2. 查看数据库日志**
```bash
# MySQL错误日志
tail -f /var/log/mysql/error.log

# 慢查询日志
tail -f /var/log/mysql/slow.log
```

**3. 检查系统资源**
```bash
# 查看内存使用
free -h

# 查看CPU使用
top

# 查看磁盘使用
df -h

# 查看端口占用
netstat -tulpn | grep 8123
```

**4. 测试网络连接**
```bash
# 测试数据库连接
mysql -h localhost -u root -p

# 测试Redis连接
redis-cli ping

# 测试HTTP接口
curl http://localhost:8123/api/health
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork项目仓库**
2. **创建特性分支**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **提交更改**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **推送到分支**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **开启Pull Request**

### 开发规范

**代码风格**
- 后端遵循阿里巴巴Java开发手册
- 前端遵循Vue 3官方风格指南
- 使用ESLint和Prettier保持代码风格统一

**提交消息规范**
```
<type>(<scope>): <subject>

<body>

<footer>
```

类型(type):
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具链更新

示例:
```
feat(points): 添加积分过期自动扣除功能

- 实现定时任务自动检查过期积分
- 添加积分过期通知功能
- 更新积分记录表结构

Closes #123
```

**测试要求**
- 新功能必须添加单元测试
- 确保所有测试通过
- 测试覆盖率不低于70%

**文档更新**
- 更新相关API文档
- 更新用户使用文档
- 添加代码注释

### Issue反馈

提交Issue时请包含:
- 问题描述
- 复现步骤
- 期望行为
- 实际行为
- 环境信息(OS、Java版本、Node版本等)
- 错误日志或截图

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目和服务:

- [Spring Boot](https://spring.io/projects/spring-boot) - 强大的Java应用框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Ant Design Vue](https://antdv.com/) - 优秀的Vue UI组件库
- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI应用框架
- [MyBatis-Flex](https://mybatis-flex.com/) - 灵活的MyBatis增强框架
- [DeepSeek](https://www.deepseek.com/) - 先进的AI模型服务
- [Vite](https://vitejs.dev/) - 快速的前端构建工具
- [Redis](https://redis.io/) - 高性能内存数据库
- [Prometheus](https://prometheus.io/) - 开源监控系统
- [Redisson](https://redisson.org/) - Redis Java客户端
- [Hutool](https://hutool.cn/) - Java工具类库

## 📞 联系我们

<div align="center">

如果您有任何问题或建议，请随时联系我们：

**📧 邮箱**: zrt3ljnygz@163.com
**💬 微信**: Join2049
**🐛 问题反馈**: [提交Issue](https://github.com/vasc-language/ai-code-mother/issues)

<br>

**扫码添加微信好友**

<img src="https://raw.githubusercontent.com/vasc-language/ai-code-mother/main/WeChatToFirend.png" alt="微信加好友" width="200">

</div>

---

## 📋 更新日志

### 🆕 v1.1.0 (2025-11-15)

#### 重大更新
- **动态 AI 模型选择系统**: 支持 24+ 个 AI 模型，5 级模型分类
- **两阶段代码生成流程**: 计划生成 → 用户确认 → 代码生成
- **代码自动验证**: ESLint 集成，自动生成代码审查报告
- **企业级部署方案**: 完整的 deployment/ 目录，支持一键部署

#### 功能增强
- 修复积分系统 8 个 P0-P3 级别缺陷
- 新增积分状态管理和一致性检查器
- 新增备份和回滚机制
- 新增 OpenResty 支持（限流、WAF）
- 新增日志轮转配置

#### 性能优化
- 优化 ChatMemory 管理，Vue 项目生成速度提升 50%+
- 修复 Token 监控跨线程问题
- 增加流式 AI 模型超时时间到 5 分钟

#### UI/UX 优化
- 集成 Lovable 主题系统
- 新增 AI 模型选择器组件（544 行）
- 新增部署中弹窗、预览加载、差异查看器等组件
- 重构聊天页面为模块化组件
- 新增 18 个 SVG 图标（Deploy、ToolsCall、Online search、refresh、thinking 等）

#### 代码重构
- 前端聊天页面模块化（从 5844 行重构为多个组件）
- 新增 composables 目录，提取复用逻辑
- 优化项目结构和代码组织

#### 文档完善
- 新增 677 行完整部署文档
- 新增部署检查清单和快速参考手册
- 新增环境变量模板（40+ 配置项）

### v1.0.1 (2025-10-04)

#### 性能优化
- **修复 ChatMemory 无限增长问题**: 限制临时记忆最大消息数为 30
- Vue 项目生成中后期速度显著提升（从 30-40 秒/文件优化到 10-15 秒/文件）

#### 新增功能
- 邮箱认证体系
- 版本管理系统
- 积分激励系统
- 邀请裂变机制
- 内置模板库

---

<div align="center">

### ⭐ Star History ⭐

如果这个项目对你有帮助，请给我们一个 Star！

[![Star History Chart](https://api.star-history.com/svg?repos=vasc-language/ai-code-mother&type=Date)](https://star-history.com/#vasc-language/ai-code-mother&Date)

</div>

---

<div align="center">

### 🎯 项目愿景

**让 AI 为您的代码生成插上翅膀！**

我们致力于打造最易用、最智能的 AI 代码生成平台
专注于 Vue 3 项目的智能化生成，为开发者提供高效、优质的代码生成服务

<br>

**💡 创新 · 🚀 高效 · 🎨 优雅 · 🔒 安全**

<br>

Made with ❤️ by [Join2049](https://github.com/vasc-language)

Copyright © 2025 AI Code Mother. All rights reserved.

</div>
