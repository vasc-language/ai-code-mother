# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在该代码库中工作时提供指导。

## 项目概述

这是一个基于 Vue 3 + Vite + TypeScript + Ant Design Vue 构建的前端应用程序，作为 "ai-code-mother" 项目的前端界面，提供AI应用生成的Web界面。

- **框架**: Vue 3.5.17 (Composition API + `<script setup>` 语法)
- **UI组件库**: Ant Design Vue 4.2.6 完整组件系统
- **构建工具**: Vite 7.0.0 + Vue DevTools 集成
- **状态管理**: Pinia 3.0.3 响应式状态管理
- **路由管理**: Vue Router 4.5.1 (history模式)
- **HTTP客户端**: Axios 1.11.0 (含请求/响应拦截器)
- **开发语言**: TypeScript 严格类型检查
- **API集成**: OpenAPI-to-TypeScript 自动生成后端API类型

## 核心开发命令

### 基础开发
```bash
# 安装依赖
npm install

# 启动开发服务器 (通常运行在端口 5173)
npm run dev

# 生产环境构建 (包含类型检查)
npm run build

# 仅构建 (不进行类型检查)
npm run build-only

# 本地预览生产构建
npm run preview
```

### 代码质量检查
```bash
# Vue TSC 类型检查
npm run type-check

# ESLint 检查并自动修复
npm run lint

# Prettier 代码格式化
npm run format
```

### API集成
```bash
# 从后端 OpenAPI 规范生成 TypeScript 类型 (推荐)
npm run openapi2ts

# 备用命令
npx @umijs/openapi
```

## 架构概览

### 项目结构特点
```
src/
├── main.ts              # 应用入口，集成 Ant Design Vue
├── App.vue              # 根组件，包含 BasicLayout
├── request.ts           # Axios 实例，含拦截器和认证处理
├── layouts/             # 布局组件
│   └── BasicLayout.vue  # 主布局 (头部+内容+底部)
├── components/          # 可复用UI组件
│   ├── GlobalHeader.vue # 导航头部，含菜单和用户认证
│   └── GlobalFooter.vue # 网站底部
├── page/                # 页面级组件 (注意：使用'page'不是'views')
│   └── HomeView.vue     # 首页组件
├── router/              # Vue Router 配置
├── stores/              # Pinia 状态存储
├── assets/              # 静态资源 (logo, 样式)
└── api/                 # OpenAPI 生成的API客户端
```

### 关键架构模式

**应用启动** (`src/main.ts`):
- Vue 3 + Pinia + Vue Router + Ant Design Vue 集成
- 导入 Ant Design 重置CSS确保样式一致性
- 全局可用的完整 Ant Design 组件库

**HTTP客户端配置** (`src/request.ts`):
- Axios 实例配置后端地址 `http://localhost:8123/api`
- 请求/响应拦截器处理认证和错误
- 未登录状态(40100)自动跳转登录页面
- 60秒超时，支持携带凭证

**布局系统** (`src/layouts/BasicLayout.vue`):
- 三层布局：GlobalHeader + 主内容 + GlobalFooter
- 响应式设计，最大宽度1200px的居中容器
- Router view 集成用于页面内容渲染

**导航系统** (`src/components/GlobalHeader.vue`):
- Ant Design Menu 组件，水平布局
- 基于路由的菜单选中状态同步
- 支持外部链接(编程导航)
- 用户认证区域(登录按钮占位符)

**路由配置** (`src/router/index.ts`):
- 简单路由配置，当前仅有首页路由
- 使用基于环境的 BASE_URL 配置
- 设计用于扩展更多路由

### 后端集成

**API配置** (`openapi2ts.config.ts`):
- 连接Spring Boot后端 `http://localhost:8123/api/v3/api-docs`
- 从OpenAPI规范生成TypeScript类型和请求函数
- 导入自定义请求实例 `@/request`

**认证流程** (`src/request.ts:28-42`):
- 拦截40100错误码(后端NOT_LOGIN_ERROR)
- 自动重定向到`/user/login`，携带返回URL
- 处理应用程序级别的登录状态

## 开发注意事项

### UI框架集成
- Ant Design Vue 提供完整组件库
- 组件使用`a-`前缀 (如 `a-layout`, `a-menu`, `a-button`)
- 全局应用重置CSS确保样式一致性
- Message 组件可用于通知提示

### TypeScript模式
- 全程使用`<script setup>`语法，更好的TypeScript集成
- 项目启用严格类型检查
- 从后端OpenAPI规范自动生成API类型

### 状态管理
- Pinia stores 使用 Composition API 模式
- 提供 counter store 示例作为模板
- 准备扩展用户认证和应用状态

### 构建流程
- 开发服务器运行Vite热重载
- 生产构建包含自动类型检查
- 开发调试启用Vue DevTools

## 重要实现细节

### 后端通信
- 后端运行在端口8123，上下文路径为`/api`
- 前端开发服务器通常运行在端口5173
- 通过Axios拦截器处理CORS和认证

### 认证系统
- 登录重定向保留返回URL，提升用户体验
- 错误码40100触发自动登录流程
- 请求中包含凭证用于会话管理

### 代码生成工作流
- 从`http://localhost:8123/api/v3/api-docs`生成OpenAPI类型
- 生成的文件放置在`./src`目录
- 所有API调用使用自定义请求实例

## 常见问题及解决方案

### OpenAPI代码生成失败
**问题**: `npm run openapi2ts`失败，出现`ECONNREFUSED`或代理错误
**解决方案**: 运行前清除代理环境变量:
```bash
set HTTP_PROXY= && set HTTPS_PROXY= && npm run openapi2ts
```

**问题**: 缺少API文件导致导入错误，如`Cannot find module '@/api/healthController.ts'`
**解决方案**: 
1. 确保后端服务运行在端口8123
2. 生成API客户端：`npm run openapi2ts`
3. 验证文件已创建在`src/api/`目录

### 路径解析问题
**问题**: 组件导入错误，如`Cannot find module '@/pages/HomeView.vue'`
**解决方案**: 检查实际目录结构 - 本项目使用`src/page/`(单数)而非`src/pages/`(复数)

## 全栈开发工作流

### 完整开发环境启动
```bash
# 1. 启动后端服务 (在项目根目录)
cd ..
./mvnw spring-boot:run

# 2. 返回前端目录并生成API客户端
cd ai-code-mother-frontend
npm run openapi2ts

# 3. 启动前端开发服务器
npm run dev
```

### 后端服务地址
- **后端API**: `http://localhost:8123/api`
- **健康检查**: `http://localhost:8123/api/health/`
- **API文档**: `http://localhost:8123/api/doc.html`
- **OpenAPI规范**: `http://localhost:8123/api/v3/api-docs`

## 关键配置文件

### Vite配置 (`vite.config.ts`)
- 路径别名: `@/` 映射到 `./src`
- Vue插件 + Vue DevTools集成
- 开发服务器热重载配置

### TypeScript配置
- `tsconfig.app.json`: 应用代码配置
- `tsconfig.node.json`: 构建工具配置
- 严格模式启用，完整类型检查

### OpenAPI配置 (`openapi2ts.config.ts`)
```typescript
export default {
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:8123/api/v3/api-docs',
  serversPath: './src',
}
```