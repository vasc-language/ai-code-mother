<template>
  <div class="doc-page">
    <h1 class="doc-title">生成多文件项目</h1>
    <p class="doc-intro">
      通过 AI 对话生成包含多个文件的完整项目,实时查看生成过程,支持在线预览和下载。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        完整操作流程
      </h2>

      <a-steps direction="vertical" :current="5" style="margin-bottom: 24px;">
        <a-step title="1. 创建应用">
          <template #description>
            在首页输入框中描述你的项目需求,点击"开始生成"创建应用
          </template>
        </a-step>

        <a-step title="2. 进入对话页面">
          <template #description>
            系统自动跳转到应用对话页面,开始与 AI 对话
          </template>
        </a-step>

        <a-step title="3. AI 分析需求">
          <template #description>
            AI 会分析你的需求,确定生成类型(多文件项目),并给出项目规划
          </template>
        </a-step>

        <a-step title="4. 实时生成代码">
          <template #description>
            AI 逐个文件生成代码,右侧实时显示当前生成的文件内容
          </template>
        </a-step>

        <a-step title="5. 预览和下载">
          <template #description>
            生成完成后,可以查看所有文件,预览网站效果,或下载整个项目
          </template>
        </a-step>
      </a-steps>

      <a-alert
        message="💡 提示"
        description="多文件项目生成消耗 9 积分,生成时间约 15-20 分钟,请确保积分充足"
        type="info"
        show-icon
      />
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <FolderOpenOutlined class="title-icon" />
        什么是多文件项目?
      </h2>

      <p class="section-content">
        多文件项目是指包含多个代码文件的完整项目,适用于:
      </p>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-card size="small" hoverable class="feature-card">
            <div class="card-content">
              <CheckCircleOutlined class="card-icon" />
              <div>
                <h4>组件化项目</h4>
                <p>多个组件文件组成的项目</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small" hoverable class="feature-card">
            <div class="card-content">
              <CheckCircleOutlined class="card-icon" />
              <div>
                <h4>工具库项目</h4>
                <p>包含多个工具函数文件</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small" hoverable class="feature-card">
            <div class="card-content">
              <CheckCircleOutlined class="card-icon" />
              <div>
                <h4>多页面网站</h4>
                <p>包含多个 HTML/CSS/JS 文件</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small" hoverable class="feature-card">
            <div class="card-content">
              <CheckCircleOutlined class="card-icon" />
              <div>
                <h4>配置文件项目</h4>
                <p>需要多种配置文件的项目</p>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        如何描述需求?
      </h2>

      <p class="section-content">
        为了让 AI 生成符合预期的多文件项目,建议在需求中包含:
      </p>

      <a-card title="✅ 推荐的需求描述结构" style="margin-bottom: 16px;">
        <ol class="guide-list">
          <li>
            <strong>项目类型:</strong> 说明要做什么类型的项目<br />
            <span class="example-text">例: "创建一个个人博客网站"</span>
          </li>
          <li>
            <strong>核心功能:</strong> 列出主要功能模块<br />
            <span class="example-text">例: "包含文章列表、文章详情、分类筛选、搜索功能"</span>
          </li>
          <li>
            <strong>技术要求:</strong> 指定技术栈(可选)<br />
            <span class="example-text">例: "使用原生 HTML/CSS/JavaScript,不使用框架"</span>
          </li>
          <li>
            <strong>设计风格:</strong> 描述界面风格(可选)<br />
            <span class="example-text">例: "采用现代简约风格,响应式布局"</span>
          </li>
        </ol>
      </a-card>

      <a-card title="📝 完整需求示例">
        <blockquote class="example-quote">
创建一个在线待办事项管理网站,包含以下功能:

1. 添加、编辑、删除待办事项
2. 标记任务完成状态
3. 按优先级和分类筛选
4. 数据保存到本地存储

技术要求:
- 使用原生 HTML + CSS + JavaScript
- 响应式设计,支持移动端
- 简洁现代的界面风格

文件结构:
- index.html: 主页面
- style.css: 样式文件
- app.js: 核心逻辑
- storage.js: 本地存储管理
        </blockquote>
      </a-card>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <CodeOutlined class="title-icon" />
        生成过程详解
      </h2>

      <a-tabs>
        <a-tab-pane key="1" tab="代码生成">
          <div class="tab-content">
            <h4>实时查看生成过程</h4>
            <ul>
              <li>AI 会逐个文件生成代码</li>
              <li>右侧区域实时显示当前正在生成的文件</li>
              <li>文件生成完成后会收起,显示文件名标签</li>
              <li>所有文件生成完成后自动显示预览</li>
            </ul>
            <a-alert
              message="生成过程中可以随时点击停止按钮暂停生成"
              type="warning"
              show-icon
              style="margin-top: 12px;"
            />
          </div>
        </a-tab-pane>

        <a-tab-pane key="2" tab="文件管理">
          <div class="tab-content">
            <h4>查看和管理生成的文件</h4>
            <ul>
              <li>生成过程中,点击文件标签可以查看已完成的文件</li>
              <li>生成完成后,所有文件会以标签页形式展示</li>
              <li>支持清空所有文件,重新生成</li>
              <li>可以下载整个项目的压缩包</li>
            </ul>
          </div>
        </a-tab-pane>

        <a-tab-pane key="3" tab="预览和部署">
          <div class="tab-content">
            <h4>在线预览和部署</h4>
            <ul>
              <li>生成完成后自动在右侧显示网站预览</li>
              <li>点击"预览网站"按钮在新标签页打开</li>
              <li>点击"部署"按钮将项目部署到云端</li>
              <li>点击"下载代码"按钮下载项目文件</li>
            </ul>
          </div>
        </a-tab-pane>
      </a-tabs>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        常见问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="生成的文件数量有限制吗?">
          <p>
            理论上没有严格限制,但建议单次生成控制在 10-15 个文件以内,
            以确保生成质量和速度。如需更多文件,可以分步骤生成。
          </p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成失败怎么办?">
          <p>如果生成过程中断或失败:</p>
          <ol>
            <li>检查积分是否充足(需要 9 积分)</li>
            <li>点击"继续"按钮尝试继续生成</li>
            <li>如果多次失败,尝试简化需求描述</li>
            <li>或者将复杂项目拆分成多个简单项目</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="如何下载和使用生成的项目?">
          <ol>
            <li>点击顶部"下载代码"按钮</li>
            <li>会下载一个 ZIP 压缩包</li>
            <li>解压后可以直接在浏览器打开 HTML 文件</li>
            <li>如果是 Node.js 项目,需要先运行 npm install 安装依赖</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="生成后可以修改代码吗?">
          <p>
            可以!生成完成后,你可以:
          </p>
          <ul>
            <li>下载代码到本地,使用编辑器修改</li>
            <li>继续与 AI 对话,要求修改某个文件或功能</li>
            <li>如果是简单的文本修改,AI 可以帮你直接修改</li>
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
  CodeOutlined,
  BulbOutlined,
  CheckCircleOutlined,
  QuestionCircleOutlined
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
  white-space: pre-line;
}

.feature-card {
  height: 100%;
}

.card-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.card-icon {
  font-size: 24px;
  color: #52c41a;
  flex-shrink: 0;
  margin-top: 4px;
}

.card-content h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #333;
}

.card-content p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.guide-list {
  margin: 0;
  padding-left: 24px;
}

.guide-list li {
  margin: 16px 0;
  line-height: 1.8;
}

.example-text {
  color: #666;
  font-size: 13px;
  font-style: italic;
}

.tab-content {
  padding: 16px;
}

.tab-content h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 15px;
}

.tab-content ul,
.tab-content ol {
  margin: 0;
  padding-left: 24px;
  color: #666;
  line-height: 1.8;
}

.tab-content li {
  margin: 8px 0;
}
</style>
