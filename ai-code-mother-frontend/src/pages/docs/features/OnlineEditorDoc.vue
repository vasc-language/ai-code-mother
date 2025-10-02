<template>
  <div class="doc-page">
    <h1 class="doc-title">在线编辑器</h1>
    <p class="doc-intro">
      强大的在线代码编辑器,支持语法高亮、代码补全,随时修改和优化生成的代码。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <EditOutlined class="title-icon" />
        核心功能
      </h2>

      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <CodeOutlined /> 语法高亮
            </template>
            <p>支持多种编程语言的语法高亮显示</p>
            <ul>
              <li>HTML / CSS / JavaScript</li>
              <li>Vue / TypeScript</li>
              <li>JSON / Markdown</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <ThunderboltOutlined /> 智能补全
            </template>
            <p>智能代码补全和提示</p>
            <ul>
              <li>自动补全标签</li>
              <li>语法提示</li>
              <li>错误检测</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <EyeOutlined /> 实时预览
            </template>
            <p>实时查看代码效果</p>
            <ul>
              <li>即时渲染</li>
              <li>热更新</li>
              <li>响应式预览</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        编辑器操作
      </h2>

      <a-descriptions bordered :column="1">
        <a-descriptions-item label="保存修改">
          点击"保存"按钮或使用快捷键 Ctrl+S (Mac: Cmd+S)
        </a-descriptions-item>
        <a-descriptions-item label="撤销/重做">
          Ctrl+Z 撤销,Ctrl+Y 重做 (Mac: Cmd+Z / Cmd+Shift+Z)
        </a-descriptions-item>
        <a-descriptions-item label="查找替换">
          Ctrl+F 查找,Ctrl+H 替换 (Mac: Cmd+F / Cmd+Option+F)
        </a-descriptions-item>
        <a-descriptions-item label="代码格式化">
          点击"格式化"按钮或使用 Shift+Alt+F (Mac: Shift+Option+F)
        </a-descriptions-item>
        <a-descriptions-item label="全屏编辑">
          点击全屏按钮进入沉浸式编辑模式
        </a-descriptions-item>
      </a-descriptions>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <FolderOpenOutlined class="title-icon" />
        多文件编辑
      </h2>

      <p class="section-content">
        对于 Vue 应用和多文件项目,编辑器支持:
      </p>
      <ul class="content-list">
        <li>文件树导航,快速切换文件</li>
        <li>多标签页同时打开多个文件</li>
        <li>文件搜索定位</li>
        <li>新建、删除、重命名文件</li>
      </ul>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        使用技巧
      </h2>

      <a-collapse :bordered="false" default-active-key="['1']">
        <a-collapse-panel key="1" header="如何快速定位代码?">
          <p>使用以下方式快速定位:</p>
          <ul>
            <li><strong>Ctrl+P:</strong> 快速打开文件</li>
            <li><strong>Ctrl+G:</strong> 跳转到指定行</li>
            <li><strong>Ctrl+F:</strong> 在当前文件中查找</li>
            <li><strong>Ctrl+Shift+F:</strong> 全局搜索</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="代码修改会自动保存吗?">
          <p>不会自动保存。你需要手动点击"保存"按钮或使用快捷键保存修改。未保存的修改会有标记提示。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="编辑器支持哪些主题?">
          <p>目前支持浅色和深色两种主题,可在编辑器设置中切换。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  EditOutlined,
  CodeOutlined,
  ThunderboltOutlined,
  EyeOutlined,
  ToolOutlined,
  FolderOpenOutlined,
  BulbOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import './doc-common.css';

.feature-card {
  height: 100%;
  border-left: 3px solid #1890ff;
}

.feature-card ul {
  margin: 12px 0 0 0;
  padding-left: 20px;
  color: #666;
}

.feature-card li {
  margin: 6px 0;
}
</style>
