<template>
  <div class="doc-page">
    <h1 class="doc-title">AI 智能生成</h1>
    <p class="doc-intro">
      基于先进的 DeepSeek AI 模型,理解你的自然语言需求,自动生成高质量、可运行的代码。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <RobotOutlined class="title-icon" />
        核心能力
      </h2>

      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :md="12">
          <a-card class="feature-card" hoverable>
            <template #title>
              <MessageOutlined /> 自然语言理解
            </template>
            <p>无需编写代码,只需用日常语言描述你的想法</p>
            <ul>
              <li>智能理解需求意图</li>
              <li>自动补全缺失细节</li>
              <li>支持中英文混合输入</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="feature-card" hoverable>
            <template #title>
              <CodeOutlined /> 代码生成
            </template>
            <p>生成符合最佳实践的完整代码</p>
            <ul>
              <li>标准化代码结构</li>
              <li>优化的性能表现</li>
              <li>完善的注释说明</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="feature-card" hoverable>
            <template #title>
              <ThunderboltOutlined /> 实时流式输出
            </template>
            <p>实时查看生成过程,即时预览效果</p>
            <ul>
              <li>逐行代码输出</li>
              <li>实时语法高亮</li>
              <li>随时暂停/继续</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="feature-card" hoverable>
            <template #title>
              <AppstoreOutlined /> 多种生成类型
            </template>
            <p>支持不同场景的代码生成需求</p>
            <ul>
              <li>单页 HTML</li>
              <li>Vue 3 应用</li>
              <li>多文件项目</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <SettingOutlined class="title-icon" />
        技术架构
      </h2>

      <a-descriptions bordered :column="{ xs: 1, sm: 2 }">
        <a-descriptions-item label="AI 模型">
          <a-tag color="purple">DeepSeek Reasoner</a-tag>
          <p class="desc-text">用于复杂推理和代码生成</p>
        </a-descriptions-item>

        <a-descriptions-item label="路由模型">
          <a-tag color="blue">Qwen Turbo</a-tag>
          <p class="desc-text">用于简单分类和快速响应</p>
        </a-descriptions-item>

        <a-descriptions-item label="集成框架">
          <a-tag color="green">LangChain4j 1.1.0</a-tag>
          <p class="desc-text">提供流式输出和会话管理</p>
        </a-descriptions-item>

        <a-descriptions-item label="最大Token">
          <a-tag color="orange">32,768</a-tag>
          <p class="desc-text">支持生成大型项目代码</p>
        </a-descriptions-item>
      </a-descriptions>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        使用技巧
      </h2>

      <a-collapse :bordered="false" default-active-key="['1']">
        <a-collapse-panel key="1" header="如何写出清晰的需求描述?">
          <h4>✅ 推荐做法:</h4>
          <ul>
            <li>明确页面结构: "需要顶部导航栏、中间内容区、底部版权信息"</li>
            <li>列举核心功能: "支持用户登录、数据展示、表单提交"</li>
            <li>描述视觉风格: "采用扁平化设计,主色调为蓝色"</li>
            <li>指定技术栈: "使用 Vue 3 + TypeScript + Ant Design"</li>
          </ul>

          <h4>❌ 避免:</h4>
          <ul>
            <li>过于简单: "做个网站"</li>
            <li>过于冗长: 超过500字的描述</li>
            <li>含糊不清: "做个好看的页面"</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="如何提高生成成功率?">
          <ul>
            <li><strong>分步描述:</strong> 将复杂需求拆分成多个简单步骤</li>
            <li><strong>提供示例:</strong> 参考类似网站或应用的链接</li>
            <li><strong>明确优先级:</strong> 说明哪些功能是必需的,哪些是可选的</li>
            <li><strong>检查余额:</strong> 确保积分充足,避免中途中断</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成时间需要多久?">
          <a-table :dataSource="timeData" :columns="timeColumns" :pagination="false" size="small" bordered>
          </a-table>
          <p style="margin-top: 12px; color: #666;">
            <InfoCircleOutlined /> 实际时间可能因网络状况和服务器负载而有所波动
          </p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="生成的代码质量如何?">
          <p>我们的 AI 模型经过大量代码训练,生成的代码具有以下特点:</p>
          <a-list
            :data-source="qualityFeatures"
            :split="false"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <CheckCircleOutlined style="color: #52c41a; margin-right: 8px;" />
                {{ item }}
              </a-list-item>
            </template>
          </a-list>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ExperimentOutlined class="title-icon" />
        示例展示
      </h2>

      <a-tabs>
        <a-tab-pane key="1" tab="简单示例">
          <div class="example-content">
            <h4>需求描述:</h4>
            <blockquote class="example-quote">
              "生成一个个人博客首页,包含文章列表、分类标签和搜索功能"
            </blockquote>

            <h4>AI 理解:</h4>
            <a-alert message="需求解析" type="info" show-icon style="margin: 12px 0;">
              <template #description>
                <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                  <li>页面类型: 博客首页</li>
                  <li>核心组件: 文章列表、分类标签、搜索框</li>
                  <li>推荐技术: HTML + CSS + JavaScript</li>
                </ul>
              </template>
            </a-alert>

            <h4>生成结果:</h4>
            <a-tag color="green">成功</a-tag>
            <a-tag color="blue">耗时 12 秒</a-tag>
            <a-tag color="purple">消耗 10 积分</a-tag>
          </div>
        </a-tab-pane>

        <a-tab-pane key="2" tab="复杂示例">
          <div class="example-content">
            <h4>需求描述:</h4>
            <blockquote class="example-quote">
              "创建一个在线待办事项管理应用,支持添加、编辑、删除任务,可以标记完成状态,
              按优先级排序,数据持久化到本地存储。使用 Vue 3、TypeScript 和 Ant Design Vue"
            </blockquote>

            <h4>AI 理解:</h4>
            <a-alert message="需求解析" type="info" show-icon style="margin: 12px 0;">
              <template #description>
                <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                  <li>应用类型: Vue 3 SPA</li>
                  <li>核心功能: CRUD 操作、状态管理、排序、本地存储</li>
                  <li>技术栈: Vue 3 + TypeScript + Ant Design Vue + localStorage</li>
                  <li>预估文件: 5-8 个组件文件</li>
                </ul>
              </template>
            </a-alert>

            <h4>生成结果:</h4>
            <a-tag color="green">成功</a-tag>
            <a-tag color="blue">耗时 24 秒</a-tag>
            <a-tag color="purple">消耗 30 积分</a-tag>
          </div>
        </a-tab-pane>
      </a-tabs>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  RobotOutlined,
  MessageOutlined,
  CodeOutlined,
  ThunderboltOutlined,
  AppstoreOutlined,
  SettingOutlined,
  BulbOutlined,
  ExperimentOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'

const timeData = [
  { type: 'HTML 页面', time: '10-15 秒', points: '10 积分' },
  { type: 'Vue 应用', time: '20-30 秒', points: '30 积分' },
  { type: '多文件项目', time: '30-45 秒', points: '50 积分' },
]

const timeColumns = [
  { title: '生成类型', dataIndex: 'type', key: 'type' },
  { title: '平均时间', dataIndex: 'time', key: 'time' },
  { title: '消耗积分', dataIndex: 'points', key: 'points' },
]

const qualityFeatures = [
  '遵循业界最佳实践和编码规范',
  '清晰的代码结构和命名规范',
  '完整的注释和文档说明',
  '响应式设计,支持多端适配',
  '良好的性能优化',
  '基本的错误处理机制',
]
</script>

<style scoped>
@import './doc-common.css';

.feature-card {
  height: 100%;
  border-left: 3px solid #667eea;
}

.feature-card ul {
  margin: 12px 0 0 0;
  padding-left: 20px;
  color: #666;
}

.feature-card li {
  margin: 6px 0;
}

.desc-text {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #888;
}

.example-content {
  padding: 16px;
}

.example-content h4 {
  color: #333;
  margin: 16px 0 8px 0;
}

.example-quote {
  background: #f5f5f5;
  border-left: 4px solid #667eea;
  padding: 12px 16px;
  margin: 8px 0;
  font-style: italic;
  color: #555;
}
</style>
