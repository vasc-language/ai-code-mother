# GEMINI.md

## 项目概述

这是一个全栈 Web 应用项目，采用前后端分离的架构。

*   **后端**: 使用 Java (JDK 21) 和 Spring Boot 3 构建。
    *   **构建工具**: Maven
    *   **数据库**: MySQL
    *   **ORM**: MyBatis-Flex
    *   **API 文档**: Knife4j
    *   **主要功能**: 提供用户管理（注册、登录、增删改查）的 API 接口。

*   **前端**: 使用 Vue.js 3 和 TypeScript 构建。
    *   **构建工具**: Vite
    *   **UI 框架**: Ant Design Vue
    *   **状态管理**: Pinia
    *   **路由**: Vue Router
    *   **HTTP 请求**: Axios

*   **架构**:
    *   后端服务运行在 `http://localhost:8123`，并统一设置了API根路径 `/api`。
    *   前端直接通过 `/api` 前缀请求后端接口，无需 Vite 代理。
    *   后端通过 `UserController` 提供 RESTful API。
    *   数据库表结构在 `sql/create_table.sql` 文件中定义。

## 主要 API 端点

所有接口均以 `/api` 为前缀。

*   `POST /user/register`: 用户注册
*   `POST /user/login`: 用户登录
*   `GET /user/get/login`: 获取当前登录用户信息
*   `POST /user/logout`: 用户注销
*   `POST /user/add`: 新增用户 (管理员权限)
*   `GET /user/get`: 根据 ID 获取用户 (管理员权限)
*   `GET /user/get/vo`: 根据 ID 获取脱敏后的用户信息
*   `POST /user/delete`: 删除用户 (管理员权限)
*   `POST /user/update`: 更新用户 (管理员权限)
*   `POST /user/list/page/vo`: 分页获取用户列表 (管理员权限)

## 构建和运行

### 环境准备

1.  安装 Java 21 或更高版本。
2.  安装 Maven。
3.  安装 Node.js 22 或更高版本。
4.  安装 MySQL 数据库。
5.  修改 `src/main/resources/application.yml` 中的数据库连接信息。
6.  在 MySQL 中创建数据库（例如 `ai_code_mother`），并执行 `sql/create_table.sql` 中的脚本创建 `user` 表。

### 数据库初始化

`sql/create_table.sql` 内容如下，请在您的 MySQL 数据库中执行：

```sql
-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  varchar(256) NOT NULL COMMENT '账号',
    `userPassword` varchar(512) NOT NULL COMMENT '密码',
    `userName`     varchar(256) NULL DEFAULT NULL COMMENT '用户昵称',
    `userAvatar`   varchar(1024) NULL DEFAULT NULL COMMENT '用户头像',
    `userProfile`  varchar(512) NULL DEFAULT NULL COMMENT '用户简介',
    `userRole`     varchar(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `createTime`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_userAccount`(`userAccount`)
) COMMENT '用户';
```

### 后端

在项目根目录下执行以下命令：

```bash
# 运行后端服务
./mvnw spring-boot:run
```

服务启动后，API 文档地址为 [http://localhost:8123/api/doc.html](http://localhost:8123/api/doc.html)。

### 前端

进入 `ai-code-mother-frontend` 目录执行以下命令：

```bash
# 进入前端项目目录
cd ai-code-mother-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 Vite 指定的端口上运行，通常是 `http://localhost:5173`。

## 开发约定

*   **API 请求**: 前端所有对后端的请求都应通过 `/api` 路径发起。
*   **代码生成**: 后端代码生成器位于 `src/main/java/com/spring/aicodemother/generator/MyBatisCodeGenerator.java`，可用于根据数据库表生成相应的实体类、Mapper 等。
*   **API 文档**: 后端代码变更后，应通过 Knife4j 检查 API 文档，确保其与实现一致。