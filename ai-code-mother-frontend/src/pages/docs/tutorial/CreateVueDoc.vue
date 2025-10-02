<template>
  <div class="doc-page">
    <h1 class="doc-title">生成 Vue 应用</h1>
    <p class="doc-intro">
      学习如何生成完整的 Vue 3 单页应用,适合管理后台、工具应用等复杂场景。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <AppstoreOutlined class="title-icon" />
        适用场景
      </h2>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 管理后台系统
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 工具类应用
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 数据展示应用
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 交互式网站
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <CodeOutlined class="title-icon" />
        技术栈
      </h2>

      <a-descriptions bordered :column="{ xs: 1, sm: 2 }">
        <a-descriptions-item label="框架">
          <a-tag color="green">Vue 3</a-tag>
          <p class="desc-text">使用最新的 Composition API</p>
        </a-descriptions-item>

        <a-descriptions-item label="语言">
          <a-tag color="blue">TypeScript</a-tag>
          <p class="desc-text">提供完整的类型支持</p>
        </a-descriptions-item>

        <a-descriptions-item label="UI 组件">
          <a-tag color="purple">Ant Design Vue</a-tag>
          <p class="desc-text">企业级 UI 组件库</p>
        </a-descriptions-item>

        <a-descriptions-item label="构建工具">
          <a-tag color="orange">Vite</a-tag>
          <p class="desc-text">快速的开发构建工具</p>
        </a-descriptions-item>
      </a-descriptions>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        操作步骤
      </h2>

      <a-steps direction="vertical" :current="5">
        <a-step title="准备积分">
          <template #description>
            <p>确保账户有至少 15 积分</p>
          </template>
        </a-step>

        <a-step title="选择类型">
          <template #description>
            <p>选择"Vue 应用"生成类型</p>
          </template>
        </a-step>

        <a-step title="详细描述">
          <template #description>
            <p>描述应用功能和页面结构,例如:</p>
            <blockquote class="example-quote">
              "创建一个待办事项管理应用,包含任务列表、添加任务、编辑任务、删除任务、
              标记完成功能。使用 Ant Design Vue 组件,数据保存到 localStorage。"
            </blockquote>
          </template>
        </a-step>

        <a-step title="生成项目">
          <template #description>
            <p>等待 15-20 分钟生成完整项目</p>
          </template>
        </a-step>

        <a-step title="下载运行">
          <template #description>
            <p>下载项目后,在本地运行:</p>
            <pre class="code-block">
npm install
npm run dev
</pre>
          </template>
        </a-step>
      </a-steps>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <FileTextOutlined class="title-icon" />
        项目结构
      </h2>

      <pre class="code-block">
my-vue-app/
├── src/
│   ├── components/     # 组件目录
│   ├── views/          # 页面目录
│   ├── router/         # 路由配置
│   ├── stores/         # 状态管理
│   ├── assets/         # 静态资源
│   ├── App.vue         # 根组件
│   └── main.ts         # 入口文件
├── package.json        # 依赖配置
├── vite.config.ts      # Vite 配置
└── tsconfig.json       # TS 配置
</pre>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        最佳实践
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何描述复杂功能?">
          <p>将功能拆分成模块描述:</p>
          <ul>
            <li>页面结构:首页、列表页、详情页等</li>
            <li>核心功能:增删改查操作</li>
            <li>数据流转:从哪里获取数据,如何存储</li>
            <li>用户交互:点击、输入、提交等操作</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成后如何二次开发?">
          <ol>
            <li>先运行项目,了解整体结构</li>
            <li>查看 src/router 了解路由配置</li>
            <li>在 src/components 添加新组件</li>
            <li>在 src/views 添加新页面</li>
            <li>使用 Pinia 管理全局状态</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="如何添加新的依赖?">
          <pre class="code-block">
# 安装新依赖
npm install package-name

# 在组件中使用
import { something } from 'package-name'
</pre>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  CodeOutlined,
  RocketOutlined,
  FileTextOutlined,
  BulbOutlined,
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

.code-block {
  background: linear-gradient(135deg, #1e1e2e 0%, #2d2d44 100%);
  color: #e0e0e0;
  padding: 24px;
  border-radius: 12px;
  overflow-x: auto;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  margin: 16px 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  transition: all 0.3s ease;
}

.code-block:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px);
}

.code-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 32px;
  background: linear-gradient(90deg,
    rgba(255, 95, 109, 0.3) 0%,
    rgba(255, 195, 113, 0.3) 25%,
    rgba(100, 221, 23, 0.3) 50%,
    rgba(0, 0, 0, 0) 100%);
  border-radius: 12px 12px 0 0;
  opacity: 0.6;
}

.desc-text {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #888;
}

@media (max-width: 768px) {
  .code-block {
    font-size: 12px;
    padding: 16px;
  }
}
</style>
