<template>
  <div class="doc-page">
    <h1 class="doc-title">快速开始</h1>
    <p class="doc-intro">
      欢迎使用 AI Code Mother! 本指南将帮助你在几分钟内开始使用平台,无需任何编程经验即可生成你的第一个应用。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        第一步:注册账号
      </h2>
      <p class="section-content">
        访问平台主页,点击右上角的"登录"按钮,然后选择"注册新账号"。填写以下信息:
      </p>
      <ul class="content-list">
        <li>用户名:你的唯一标识</li>
        <li>密码:至少8位字符</li>
        <li>确认密码</li>
      </ul>
      <a-alert
        message="新用户福利"
        description="注册成功后,你将自动获得 50 积分,足够生成多个应用!"
        type="success"
        show-icon
        class="tip-alert"
      />
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <EditOutlined class="title-icon" />
        第二步:描述你的需求
      </h2>
      <p class="section-content">
        登录后,你将看到应用生成页面。在输入框中用自然语言描述你想要的应用:
      </p>
      <a-card class="example-card" title="示例需求描述">
        <p><strong>示例 1 - 简单页面:</strong></p>
        <blockquote class="example-quote">
          "帮我生成一个个人简历页面,包含头部导航栏、个人信息展示区、技能列表和联系方式"
        </blockquote>

        <p class="example-divider"><strong>示例 2 - Vue 应用:</strong></p>
        <blockquote class="example-quote">
          "创建一个待办事项管理应用,需要添加任务、标记完成、删除任务的功能,使用 Vue 3"
        </blockquote>

        <p class="example-divider"><strong>示例 3 - 多文件项目:</strong></p>
        <blockquote class="example-quote">
          "生成一个在线计算器项目,包含基本的加减乘除功能,分离 HTML、CSS 和 JavaScript 文件"
        </blockquote>
      </a-card>

      <a-alert
        message="提示:如何写出更好的需求描述"
        type="info"
        show-icon
        class="tip-alert"
      >
        <template #description>
          <ul class="tip-list">
            <li>清晰描述页面结构(头部、内容区、底部等)</li>
            <li>说明主要功能需求</li>
            <li>提及颜色、风格等视觉要求</li>
            <li>如果有具体技术要求,请明确指出</li>
          </ul>
        </template>
      </a-alert>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ThunderboltOutlined class="title-icon" />
        第三步:选择生成类型
      </h2>
      <p class="section-content">
        根据你的需求选择合适的生成类型:
      </p>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="8">
          <a-card class="type-card" hoverable>
            <template #cover>
              <div class="type-cover html-type">
                <FileTextOutlined class="type-icon" />
              </div>
            </template>
            <a-card-meta title="HTML 页面">
              <template #description>
                <p>单文件输出,开箱即用</p>
                <a-tag color="green">6 积分</a-tag>
                <p class="type-time">约 2 分钟生成</p>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="8">
          <a-card class="type-card popular-card" hoverable>
            <template #cover>
              <div class="type-cover vue-type">
                <AppstoreOutlined class="type-icon" />
                <div class="popular-badge">推荐</div>
              </div>
            </template>
            <a-card-meta title="Vue 应用">
              <template #description>
                <p>完整 Vue 3 项目结构</p>
                <a-tag color="blue">15 积分</a-tag>
                <p class="type-time">约 15 分钟生成</p>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="8">
          <a-card class="type-card" hoverable>
            <template #cover>
              <div class="type-cover multifile-type">
                <FolderOpenOutlined class="type-icon" />
              </div>
            </template>
            <a-card-meta title="多文件项目">
              <template #description>
                <p>自定义技术栈和结构</p>
                <a-tag color="purple">9 积分</a-tag>
                <p class="type-time">约 4 分钟生成</p>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <EyeOutlined class="title-icon" />
        第四步:实时查看生成过程
      </h2>
      <p class="section-content">
        点击"开始生成"后,AI 将实时输出代码。你可以:
      </p>
      <ul class="content-list">
        <li>看到代码逐行生成的过程</li>
        <li>实时预览生成的效果</li>
        <li>随时暂停或取消生成</li>
      </ul>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <DownloadOutlined class="title-icon" />
        第五步:下载或部署
      </h2>
      <p class="section-content">
        代码生成完成后,你有多种选择:
      </p>
      <a-space direction="vertical" size="middle" style="width: 100%">
        <a-card size="small" class="action-card">
          <template #title>
            <DownloadOutlined /> 下载代码
          </template>
          将生成的代码打包下载到本地,可以在本地环境运行或进一步开发
        </a-card>

        <a-card size="small" class="action-card">
          <template #title>
            <EditOutlined /> 在线编辑
          </template>
          使用内置编辑器直接修改代码,支持语法高亮和代码补全
        </a-card>

        <a-card size="small" class="action-card">
          <template #title>
            <CloudUploadOutlined /> 一键部署
          </template>
          将应用部署到云端,获得可访问的链接
        </a-card>
      </a-space>
    </section>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        常见问题
      </h2>
      <a-collapse :bordered="false" class="faq-collapse">
        <a-collapse-panel key="1" header="生成失败怎么办?">
          <p>生成失败可能有以下原因:</p>
          <ul>
            <li>积分不足:请查看积分余额,通过签到或邀请好友获取更多积分</li>
            <li>需求描述不清:尝试更详细地描述你的需求</li>
            <li>网络问题:检查网络连接,刷新页面重试</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="如何获得更多积分?">
          <ul>
            <li>每日签到:每天签到获得 5 积分</li>
            <li>邀请好友:每邀请一位好友注册,双方各得 20 积分</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成的代码可以商用吗?">
          <p>
            是的,生成的代码归你所有,可以自由使用、修改和商用。
            我们建议在实际使用前进行测试和优化。
          </p>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <a-divider />

    <div class="next-steps">
      <h2 class="section-title">下一步</h2>
      <p>现在你已经了解了基本使用流程,可以:</p>
      <a-space size="middle" wrap>
        <a-button type="primary" size="large" @click="goToHome">
          <template #icon><RocketOutlined /></template>
          立即开始生成
        </a-button>
        <a-button size="large" @click="goToFeatures">
          <template #icon><AppstoreOutlined /></template>
          探索更多功能
        </a-button>
        <a-button size="large" @click="goToTutorial">
          <template #icon><FileTextOutlined /></template>
          查看详细教程
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  RocketOutlined,
  EditOutlined,
  ThunderboltOutlined,
  EyeOutlined,
  DownloadOutlined,
  QuestionCircleOutlined,
  FileTextOutlined,
  AppstoreOutlined,
  FolderOpenOutlined,
  CloudUploadOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

const goToHome = () => {
  router.push('/')
}

const goToFeatures = () => {
  router.push('/docs/features/ai-generation')
}

const goToTutorial = () => {
  router.push('/docs/tutorial/create-html')
}
</script>

<style scoped>
.doc-page {
  color: #333;
  line-height: 1.8;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.doc-title {
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
  letter-spacing: -1px;
}

.doc-intro {
  font-size: 17px;
  color: #666;
  margin-bottom: 24px;
  line-height: 2;
  padding: 20px 24px;
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
  border-left: 4px solid #ec4899;
  border-radius: 8px;
}

.doc-section {
  margin: 48px 0;
  padding: 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.doc-section:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 3px solid #f0f0f0;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 80px;
  height: 3px;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  border-radius: 2px;
}

.title-icon {
  color: #ec4899;
  font-size: 32px;
  padding: 8px;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1) 0%, rgba(249, 115, 22, 0.1) 100%);
  border-radius: 12px;
}

.section-content {
  font-size: 16px;
  color: #555;
  margin-bottom: 20px;
  line-height: 1.9;
}

.content-list {
  font-size: 16px;
  color: #555;
  padding-left: 24px;
  margin: 20px 0;
}

.content-list li {
  margin: 12px 0;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #ec4899;
  transition: all 0.2s ease;
}

.content-list li:hover {
  background: #fef2f2;
  transform: translateX(4px);
}

.tip-alert {
  margin-top: 20px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tip-list {
  margin: 12px 0 0 0;
  padding-left: 20px;
}

.tip-list li {
  margin: 8px 0;
  line-height: 1.8;
}

.example-card {
  margin: 24px 0;
  background: linear-gradient(135deg, #f8f9ff 0%, #fff7ed 100%);
  border: 2px solid #e8ecff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(236, 72, 153, 0.08);
  transition: all 0.3s ease;
}

.example-card:hover {
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.15);
  transform: translateY(-4px);
}

:deep(.example-card .ant-card-head) {
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  color: white;
  border-radius: 14px 14px 0 0;
  border: none;
  font-weight: 600;
}

:deep(.example-card .ant-card-head-title) {
  color: white;
}

.example-quote {
  background: white;
  border-left: 5px solid #ec4899;
  padding: 16px 20px;
  margin: 12px 0;
  font-style: italic;
  color: #555;
  border-radius: 0 8px 8px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  line-height: 1.9;
  transition: all 0.2s ease;
}

.example-quote:hover {
  background: #fafafa;
  border-left-width: 6px;
  padding-left: 19px;
}

.example-divider {
  margin-top: 28px;
  font-weight: 600;
  color: #333;
}

.type-card {
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.type-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  border-color: rgba(236, 72, 153, 0.3);
}

.popular-card {
  border: 2px solid #1890ff;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2);
}

.popular-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #1890ff;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
  z-index: 1;
}

:deep(.popular-card .ant-ribbon) {
  font-weight: 600;
}

.type-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  color: white;
  position: relative;
  overflow: hidden;
}

.type-cover::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(45deg);
  transition: all 0.5s ease;
}

.type-card:hover .type-cover::before {
  top: 100%;
  right: 100%;
}

.html-type {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.vue-type {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.multifile-type {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.type-icon {
  font-size: 56px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.type-time {
  color: #999;
  font-size: 13px;
  margin-top: 8px;
}

:deep(.type-card .ant-card-meta-title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

:deep(.type-card .ant-card-meta-description) {
  color: #666;
}

.action-card {
  border-left: 4px solid #ec4899;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
}

.action-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.15);
}

:deep(.action-card .ant-card-head) {
  background: transparent;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  color: #ec4899;
}

.faq-collapse {
  background: #fafafa;
  border-radius: 12px;
}

:deep(.ant-collapse-header) {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

:deep(.ant-collapse-item) {
  border-radius: 8px;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
  background: white;
}

:deep(.ant-collapse-item:hover) {
  border-color: #ec4899;
}

.next-steps {
  text-align: center;
  padding: 48px 32px;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(236, 72, 153, 0.3);
  color: white;
  position: relative;
  overflow: hidden;
}

.next-steps::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(10%, 10%);
  }
}

.next-steps .section-title {
  justify-content: center;
  margin-bottom: 24px;
  color: white;
  border-bottom: none;
}

.next-steps .section-title::after {
  display: none;
}

.next-steps .section-title .title-icon {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.next-steps h2 {
  color: white;
  font-size: 32px;
}

.next-steps p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin-bottom: 32px;
}

:deep(.next-steps .ant-btn) {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  border: 2px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

:deep(.next-steps .ant-btn-primary) {
  background: white;
  color: #ec4899;
}

:deep(.next-steps .ant-btn-primary:hover) {
  background: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

:deep(.next-steps .ant-btn:not(.ant-btn-primary)) {
  background: transparent;
  color: white;
}

:deep(.next-steps .ant-btn:not(.ant-btn-primary):hover) {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .doc-title {
    font-size: 32px;
  }

  .doc-section {
    padding: 24px 16px;
  }

  .section-title {
    font-size: 22px;
  }

  .next-steps {
    padding: 32px 20px;
  }
}
</style>
