<template>
  <div class="doc-page">
    <h1 class="doc-title">生成多文件项目</h1>
    <p class="doc-intro">
      生成包含多个文件和目录的完整项目,支持自定义技术栈和复杂结构。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <FolderOpenOutlined class="title-icon" />
        适用场景
      </h2>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 前后端分离项目
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 组件库项目
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 工具类项目
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 自定义框架项目
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        需求描述要点
      </h2>

      <p class="section-content">
        描述多文件项目需求时,建议包含以下信息:
      </p>

      <a-card title="1. 技术栈选择" style="margin-bottom: 16px;">
        <p>明确使用的技术和工具:</p>
        <blockquote class="example-quote">
          "使用 React + TypeScript + Webpack,配置 ESLint 和 Prettier"
        </blockquote>
      </a-card>

      <a-card title="2. 目录结构" style="margin-bottom: 16px;">
        <p>说明项目的文件组织方式:</p>
        <blockquote class="example-quote">
          "src 目录下分为 components、utils、services 三个子目录,
          public 目录存放静态资源"
        </blockquote>
      </a-card>

      <a-card title="3. 核心功能" style="margin-bottom: 16px;">
        <p>描述各个模块的功能:</p>
        <blockquote class="example-quote">
          "components 目录包含 Header、Sidebar、Footer 等组件,
          utils 提供日期格式化、数据验证等工具函数"
        </blockquote>
      </a-card>

      <a-card title="4. 配置文件">
        <p>说明需要的配置文件:</p>
        <blockquote class="example-quote">
          "包含 package.json、tsconfig.json、webpack.config.js 等配置文件"
        </blockquote>
      </a-card>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <FileTextOutlined class="title-icon" />
        完整示例
      </h2>

      <a-card title="需求描述示例">
        <blockquote class="example-quote" style="white-space: pre-line;">
生成一个 React 组件库项目:

技术栈:
- React 18 + TypeScript
- Rollup 打包
- Storybook 文档

目录结构:
- src/components: 组件源码(Button、Input、Modal等)
- src/utils: 工具函数
- src/styles: 全局样式
- stories: Storybook 文档
- dist: 打包输出

配置文件:
- package.json: 包含开发和打包脚本
- rollup.config.js: Rollup 配置
- tsconfig.json: TypeScript 配置
- .storybook: Storybook 配置

核心功能:
- 提供常用 UI 组件
- 支持主题定制
- 完整的 TypeScript 类型定义
        </blockquote>
      </a-card>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        使用技巧
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何确保生成的项目结构合理?">
          <ul>
            <li>参考流行开源项目的目录结构</li>
            <li>明确说明各目录的职责</li>
            <li>遵循技术栈的最佳实践</li>
            <li>考虑项目的可扩展性</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成后如何运行项目?">
          <ol>
            <li>下载并解压项目文件</li>
            <li>查看 README.md 了解项目信息</li>
            <li>运行 npm install 安装依赖</li>
            <li>根据 package.json 中的 scripts 运行项目</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="如何自定义配置?">
          <p>生成后可以修改配置文件:</p>
          <ul>
            <li>package.json: 修改项目信息和依赖</li>
            <li>配置文件: 调整构建和开发配置</li>
            <li>环境变量: 添加 .env 文件</li>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  FolderOpenOutlined,
  RocketOutlined,
  FileTextOutlined,
  ToolOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import '../features/doc-common.css';

.example-quote {
  background: #f5f5f5;
  border-left: 4px solid #1890ff;
  padding: 12px 16px;
  margin: 12px 0;
  font-style: italic;
  color: #555;
  line-height: 1.8;
}
</style>
