<template>
  <div class="doc-page">
    <h1 class="doc-title">在线编辑</h1>
    <p class="doc-intro">
      无需下载代码,在浏览器中直接对生成的网站进行可视化修改,点击页面元素即可让 AI 帮你完成调整。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <EditOutlined class="title-icon" />
        核心能力
      </h2>

      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <CodeOutlined /> 可视化选中
            </template>
            <p>直接对网页元素进行定位与选择</p>
            <ul>
              <li>鼠标悬停即高亮轮廓</li>
              <li>点击元素自动锁定节点信息</li>
              <li>支持多层嵌套组件逐层选中</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <ThunderboltOutlined /> 对话式修改
            </template>
            <p>用自然语言描述想要的改动</p>
            <ul>
              <li>支持文本、样式与布局调整</li>
              <li>AI 解读意图并生成修改方案</li>
              <li>连续对话即可迭代多次修改</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="8">
          <a-card class="feature-card" hoverable>
            <template #title>
              <EyeOutlined /> 实时预览
            </template>
            <p>修改结果即时体现在页面上</p>
            <ul>
              <li>无需刷新即可查看效果</li>
              <li>保持当前页面状态与交互</li>
              <li>适合即时验证多次改动</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        常用操作
      </h2>

      <a-descriptions bordered :column="1">
        <a-descriptions-item label="启用编辑模式">
          在应用对话页面点击“启用编辑模式”按钮,开启可视化修改流程
        </a-descriptions-item>
        <a-descriptions-item label="选择元素">
          将鼠标移动到预览区域,点击需要调整的元素,系统会高亮并展示节点信息
        </a-descriptions-item>
        <a-descriptions-item label="描述需求">
          在对话框中用自然语言说明改动,例如“把按钮文字改为立即体验,背景色改成#1a73e8”
        </a-descriptions-item>
        <a-descriptions-item label="应用并预览">
          AI 会生成修改结果并自动刷新预览区域,无需手动刷新或复制代码
        </a-descriptions-item>
        <a-descriptions-item label="保存成果">
          确认改动后,建议下载最新代码包或同步部署结果,以便长期保留修改
        </a-descriptions-item>
      </a-descriptions>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <FolderOpenOutlined class="title-icon" />
        适用场景
      </h2>

      <p class="section-content">
        在线编辑适合在生成结果基础上快速迭代以下内容:
      </p>
      <ul class="content-list">
        <li>网站文案、标题、按钮等文本内容</li>
        <li>局部样式与品牌色彩微调</li>
        <li>布局对齐、间距及组件显隐</li>
        <li>图片链接、图标等静态资源替换</li>
      </ul>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        使用技巧
      </h2>

      <a-collapse :bordered="false" default-active-key="['1']">
        <a-collapse-panel key="1" header="如何快速选准元素?">
          <p>参考以下技巧提升选中效率:</p>
          <ul>
            <li><strong>悬停观察:</strong> 鼠标悬停即出现高亮边框,确认后再点击</li>
            <li><strong>辅助信息:</strong> 选中后可查看标签、类名等提示,避免误改</li>
            <li><strong>重复尝试:</strong> 嵌套结构较深时可多次点击,逐层定位目标元素</li>
            <li><strong>退出重选:</strong> 若选错可关闭元素信息框重新选择</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="修改会自动保存吗?">
          <p>不会自动保存。确认修改后,请下载最新代码或发布新版本,以免刷新页面后丢失结果。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="适合处理哪些需求?">
          <p>在线编辑适用于快速验证和微调,大规模的架构调整依旧建议回到本地开发环境完成。</p>
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
