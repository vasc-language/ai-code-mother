# AI代码生成器 (AI Code Mother)

一个功能强大的全栈AI代码生成平台，基于Spring Boot 3.5.4和Vue 3构建，集成DeepSeek AI模型，提供智能化的HTML、多文件项目和Vue应用生成服务。

## 🚀 项目概述

AI Code Mother 是一个现代化的AI驱动代码生成平台，旨在通过人工智能技术简化和加速软件开发流程。系统支持多种代码生成场景，从简单的HTML页面到复杂的多文件项目结构，为开发者提供高效、智能的代码生成解决方案。

### ✨ 核心特性

- **🤖 AI驱动生成**: 集成DeepSeek Reasoner和Qwen Turbo模型，提供高质量代码生成
- **🔄 实时流式输出**: 基于SSE(Server-Sent Events)的实时代码生成进度展示
- **📱 全栈架构**: Spring Boot后端 + Vue 3前端的现代化全栈解决方案
- **🎯 多场景支持**: HTML页面、多文件项目、Vue应用等多种生成类型
- **📊 监控系统**: 集成Prometheus指标监控和Grafana可视化
- **☁️ 云端集成**: 腾讯云COS存储支持，支持生成文件的云端存储
- **🔧 智能工具**: 网页截图、项目下载、代码预览等实用功能
- **📧 邮箱认证**: 支持邮箱注册登录，邮件验证码验证，提升账户安全性

## 🛠️ 技术架构

### 后端技术栈 (Spring Boot)

- **核心框架**: Spring Boot 3.5.4 + Java 21
- **AI集成**: LangChain4j 1.1.0 + DeepSeek API + 阿里云DashScope
- **流式处理**: Reactor响应式编程支持Server-Sent Events
- **数据层**: MyBatis-Flex 1.11.0 ORM + MySQL + HikariCP连接池
- **缓存系统**: Redis会话管理 + Caffeine本地缓存
- **API文档**: Knife4j (Swagger UI) 自动生成API文档
- **云存储**: 腾讯云COS对象存储集成
- **监控系统**: Spring Boot Actuator + Prometheus指标收集
- **工具库**: Hutool工具集 + Lombok代码简化
- **Web自动化**: Selenium网页截图功能

### 前端技术栈 (Vue 3)

- **核心框架**: Vue 3.5.17 + TypeScript + Vite 7.0.0
- **UI组件库**: Ant Design Vue 4.2.6 完整组件系统
- **状态管理**: Pinia 3.0.3 响应式状态管理
- **路由系统**: Vue Router 4.5.1 单页面应用路由
- **HTTP客户端**: Axios 1.11.0 + 请求/响应拦截器
- **API集成**: OpenAPI自动生成TypeScript客户端
- **开发工具**: Vue DevTools + ESLint + Prettier
- **内容渲染**: Markdown-it + highlight.js 代码高亮

### 数据库设计

- **用户系统**: 完整的用户注册、登录、会话管理，支持邮箱验证码注册登录
- **邮箱验证**: 邮件验证码生成、存储和验证机制
- **应用管理**: 支持多种生成类型的应用CRUD操作
- **聊天历史**: AI对话历史记录和管理
- **分布式ID**: 雪花算法ID生成策略
- **软删除**: 基于isDelete标志的逻辑删除

## 🎯 核心功能模块

### 1. AI代码生成引擎

- **多模型支持**: DeepSeek Reasoner用于复杂推理，Qwen Turbo用于快速分类
- **流式生成**: 实时显示代码生成进度，提升用户体验
- **自定义提示**: 支持不同场景的专业化提示词模板
- **结构化输出**: JSON格式响应，严格的结构验证

### 2. 项目管理系统

- **应用CRUD**: 完整的应用创建、读取、更新、删除操作
- **生成类型**: 支持HTML、多文件项目、Vue应用等多种类型
- **版本控制**: 支持应用版本管理和历史记录
- **批量操作**: 支持批量导入导出和管理

### 3. 实时通信系统

- **SSE流式传输**: 基于Server-Sent Events的实时数据推送
- **WebSocket支持**: 双向实时通信能力
- **断线重连**: 自动重连机制确保连接稳定性

### 4. 云存储集成

- **腾讯云COS**: 生成文件的云端存储和管理
- **自动上传**: 生成完成后自动上传到云存储
- **文件管理**: 支持文件的查看、下载和删除
- **CDN加速**: 通过CDN加速文件访问

### 5. 监控与运维

- **Prometheus指标**: 详细的应用性能指标收集
- **Grafana仪表盘**: 可视化的监控面板
- **健康检查**: 多维度的应用健康状态检查
- **日志管理**: 结构化日志记录和分析

## 📦 项目结构

```
ai-code-mother/
├── src/main/java/com/spring/aicodemother/    # 后端源码
│   ├── ai/                                   # AI模型集成和服务
│   ├── controller/                           # REST API控制器
│   ├── service/                              # 业务逻辑层
│   ├── mapper/                              # 数据访问层
│   ├── model/                               # 数据模型和DTO
│   ├── core/                                # 核心业务逻辑
│   ├── config/                              # 配置类
│   ├── utils/                               # 工具类
│   └── manager/                             # 云存储管理
├── ai-code-mother-frontend/                 # 前端源码
│   ├── src/
│   │   ├── components/                      # Vue组件
│   │   ├── pages/                          # 页面组件
│   │   ├── stores/                         # Pinia状态管理
│   │   ├── router/                         # Vue路由配置
│   │   └── api/                            # API客户端
├── sql/                                     # 数据库脚本
├── tmp/                                     # 生成文件临时目录
├── docs/                                    # 项目文档
└── prometheus.yml                           # Prometheus配置
```

## 🔧 环境要求

### 必需环境

- **Java 21**: Spring Boot 3.5.4的运行要求
- **Node.js 18+**: 前端开发和构建环境
- **MySQL 8.0+**: 主数据库，需要创建`ai_code_mother`数据库
- **Redis 6.0+**: 会话存储和缓存，默认端口6379
- **Chrome/Chromium**: Selenium网页截图功能依赖

### 可选组件

- **Docker**: 容器化部署支持
- **Nginx**: 反向代理和静态文件服务
- **Prometheus**: 指标收集和监控
- **Grafana**: 监控数据可视化

## 🚀 快速开始

### 1. 环境准备

```bash
# 检查Java版本
java -version  # 需要Java 21

# 检查Node.js版本
node -version  # 需要18.0+

# 启动MySQL服务
mysql -u root -p
CREATE DATABASE ai_code_mother;

# 启动Redis服务
redis-server
```

### 2. 后端启动

```bash
# 克隆项目
git clone [repository-url]
cd ai-code-mother

# 配置数据库连接 (修改 application.yml)
# spring.datasource.url=jdbc:mysql://localhost:3306/ai_code_mother
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# 配置Redis连接
# spring.data.redis.host=localhost
# spring.data.redis.port=6379

# 编译项目
./mvnw clean compile

# 运行测试
./mvnw test

# 启动应用
./mvnw spring-boot:run
```

### 3. 前端启动

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

### 4. 访问应用

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8123/api
- **API文档**: http://localhost:8123/api/doc.html
- **健康检查**: http://localhost:8123/api/health
- **Prometheus指标**: http://localhost:8123/api/actuator/prometheus

## 📋 开发指南

### 后端开发

```bash
# 编译项目
./mvnw clean compile

# 运行特定测试
./mvnw test -Dtest=ClassName#methodName

# 生成数据库代码
./mvnw compile exec:java -Dexec.mainClass="com.spring.aicodemother.generator.MyBatisCodeGenerator"

# 打包应用
./mvnw clean package
```

### 前端开发

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

### API开发工作流

1. 修改后端API接口
2. 重启后端服务
3. 重新生成前端API客户端：`npm run openapi2ts`
4. 在前端使用新的API接口

## 🔑 配置说明

### 后端配置

主要配置文件：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_code_mother
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

# 配置AI模型API密钥
ai:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:your-api-key}
    base-url: https://api.deepseek.com
  dashscope:
    api-key: ${DASHSCOPE_API_KEY:your-api-key}

# 配置邮件服务 (用于发送验证码)
spring:
  mail:
    host: ${MAIL_HOST:smtp.example.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@example.com}
    password: ${MAIL_PASSWORD:your-password}

# 腾讯云COS配置
cos:
  secret-id: ${COS_SECRET_ID:your-secret-id}
  secret-key: ${COS_SECRET_KEY:your-secret-key}
  region: ${COS_REGION:ap-beijing}
  bucket: ${COS_BUCKET:your-bucket}
```

### 前端配置

开发环境配置：`.env.development`

```
VITE_API_BASE_URL=http://localhost:8123/api
VITE_APP_TITLE=AI代码生成器
```

生产环境配置：`.env.production`

```
VITE_API_BASE_URL=/api
VITE_APP_TITLE=AI代码生成器
```

## 📊 API文档

### 核心API端点

#### 应用管理
- `GET /api/app/` - 获取应用列表
- `POST /api/app/` - 创建新应用
- `PUT /api/app/{id}` - 更新应用
- `DELETE /api/app/{id}` - 删除应用

#### AI代码生成
- `POST /api/app/generate/{id}` - 非流式代码生成
- `GET /api/app/generate/sse/{id}` - 流式代码生成 (SSE)

#### 用户管理
- `POST /api/user/register` - 用户邮箱注册
- `POST /api/user/login` - 用户邮箱登录
- `POST /api/user/logout` - 用户登出
- `GET /api/user/current` - 获取当前用户信息
- `POST /api/user/email/send` - 发送邮箱验证码

#### 系统监控
- `GET /api/health/` - 健康检查
- `GET /api/actuator/prometheus` - Prometheus指标

### 响应格式

所有API遵循统一的响应格式：

```json
{
  "code": 0,          // 0=成功，其他值表示错误
  "data": {},         // 响应数据
  "message": "ok"     // 状态消息
}
```

### 错误代码

- `0`: 成功
- `40000`: 请求参数错误
- `40100`: 未登录错误
- `40101`: 无权限错误
- `40400`: 资源不存在
- `40300`: 禁止访问
- `50000`: 系统错误
- `50001`: 操作失败

## 🎨 特色功能

### 1. 智能代码生成

- **多模型协作**: 结合DeepSeek和Qwen模型的优势，实现精准的代码生成
- **上下文理解**: 深度理解用户需求，生成符合业务逻辑的代码
- **代码优化**: 自动优化生成的代码结构和性能

### 2. 实时生成体验

- **流式输出**: 实时展示代码生成过程，用户可以看到AI的思考过程
- **进度跟踪**: 详细的生成进度指示，让用户了解当前状态
- **即时预览**: 生成完成后立即提供代码预览和下载

### 3. 项目智能化管理

- **模板系统**: 内置多种项目模板，支持快速启动
- **版本管理**: 支持项目版本控制和回滚
- **批量处理**: 支持批量生成和管理多个项目

### 4. 云端无缝集成

- **自动部署**: 生成的项目可自动部署到云端
- **CDN加速**: 通过CDN网络加速文件访问
- **备份恢复**: 自动备份重要文件，支持一键恢复

## 🚢 部署指南

### Docker部署

```bash
# 构建后端镜像
docker build -t ai-code-mother-backend .

# 构建前端镜像
cd ai-code-mother-frontend
docker build -t ai-code-mother-frontend .

# 使用Docker Compose启动
docker-compose up -d
```

### 传统部署

```bash
# 后端部署
./mvnw clean package -DskipTests
java -jar target/ai-code-mother-0.0.1-SNAPSHOT.jar

# 前端部署
cd ai-code-mother-frontend
npm run build
# 将dist目录部署到Web服务器
```

### 生产环境配置

1. **数据库优化**: 配置连接池、索引优化
2. **Redis集群**: 配置Redis高可用集群
3. **负载均衡**: 使用Nginx进行负载均衡
4. **SSL证书**: 配置HTTPS安全访问
5. **监控报警**: 配置Prometheus + Grafana监控

## 🔍 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库连接配置
   - 确认数据库权限设置

2. **Redis连接失败**
   - 检查Redis服务状态
   - 验证Redis配置参数
   - 检查防火墙设置

3. **前端API调用失败**
   - 确认后端服务正常运行
   - 检查跨域配置
   - 验证API客户端版本

4. **AI模型调用失败**
   - 检查API密钥配置
   - 验证网络连接
   - 查看模型服务状态

### 性能优化

1. **数据库优化**
   - 添加合适的索引
   - 优化查询语句
   - 配置连接池参数

2. **缓存策略**
   - 启用Redis缓存
   - 配置本地缓存
   - 实施缓存失效策略

3. **前端优化**
   - 启用代码分割
   - 优化图片资源
   - 配置CDN加速

## 🤝 贡献指南

我们欢迎所有形式的贡献！请遵循以下步骤：

1. Fork 项目仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 开发规范

- **代码风格**: 遵循项目的ESLint和Prettier配置
- **提交消息**: 使用清晰、描述性的提交消息
- **测试覆盖**: 为新功能添加相应的测试用例
- **文档更新**: 更新相关的API文档和使用说明

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目和服务：

- [Spring Boot](https://spring.io/projects/spring-boot) - 强大的Java应用框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Ant Design Vue](https://antdv.com/) - 优秀的Vue UI组件库
- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI应用框架
- [MyBatis-Flex](https://mybatis-flex.com/) - 灵活的MyBatis增强框架
- [DeepSeek](https://www.deepseek.com/) - 先进的AI模型服务
- [Vite](https://vitejs.dev/) - 快速的前端构建工具

## 📞 联系我们

如果您有任何问题或建议，请随时联系我们：

- 提交 [Issue](https://github.com/vasc-language/ai-code-mother/issues)
- 发送邮件至: zrt3ljnygz@163.com
- 微信联系: Join2049

---

<p align="center">
  <b>让AI为您的代码生成插上翅膀！</b>
</p>