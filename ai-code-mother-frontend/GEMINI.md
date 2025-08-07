# GEMINI.md - 前端项目上下文

## 项目概述

本项目是 `ai-code-mother` 全栈应用的**前端**部分。它是一个使用 Vue.js 3 构建的现代化单页应用 (SPA)。

*   **核心技术栈**:
    *   **框架**: Vue.js 3
    *   **语言**: TypeScript
    *   **构建工具**: Vite
    *   **UI 框架**: Ant Design Vue 4.x
    *   **路由**: Vue Router
    *   **状态管理**: Pinia
    *   **HTTP 请求**: Axios

*   **项目架构**:
    *   采用标准的 Vue 项目结构，将代码按功能划分为 `pages` (页面), `components` (组件), `stores` (状态), `router` (路由), `api` (接口) 等目录。
    *   通过 `src/request.ts` 中配置的 Axios 实例与后端 API 进行通信。
    *   后端 API 的基础 URL 硬编码为 `http://localhost:8123/api`，前端直接请求该地址，**不使用 Vite 代理**。
    *   使用 Pinia (`src/stores/loginUser.ts`) 管理全局用户登录状态。
    *   路由配置在 `src/router/index.ts` 中，定义了应用的页面和访问路径。

## 构建与运行

在开始之前，请确保已根据**项目根目录的 `GEMINI.md`** 完成了后端服务的环境准备和启动。

1.  **安装依赖**:
    在当前前端项目目录 (`ai-code-mother-frontend`) 下执行：
    ```bash
    npm install
    ```

2.  **启动开发服务器**:
    ```bash
    npm run dev
    ```
    服务通常会启动在 `http://localhost:5173`。

3.  **编译生产版本**:
    ```bash
    npm run build
    ```
    构建产物将输出到 `dist` 目录。

## 开发约定

*   **API 请求**:
    *   所有与后端 `UserController` 的交互都应通过 `src/api/userController.ts` 中封装好的函数进行。
    *   这些 API 函数是基于 OpenAPI 规范自动生成的（通过 `npm run openapi2ts`），不应手动修改。
    *   所有请求都通过 `src/request.ts` 中的 Axios 实例发送，该实例统一处理了基础 URL、Cookie 凭证和响应拦截（如未登录跳转）。

*   **状态管理**:
    *   全局状态（如登录用户信息）由 Pinia 统一管理。需要时，应从 `src/stores` 中获取或定义新的 store。
    *   通过 `useLoginUserStore` 获取和更新当前登录用户的信息。

*   **路由与权限**:
    *   页面路由在 `src/router/index.ts` 中定义。
    *   前端权限控制逻辑位于 `src/access.ts` 文件中，它会在路由切换前进行检查。

*   **代码风格**:
    *   项目配置了 ESLint 和 Prettier 以保证代码风格统一。
    *   在提交代码前，可以运行以下命令进行检查和自动格式化：
        ```bash
        # 检查并修复 ESLint 问题
        npm run lint

        # 格式化代码
        npm run format
        ```
