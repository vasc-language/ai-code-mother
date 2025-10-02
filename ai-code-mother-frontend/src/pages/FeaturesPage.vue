<template>
  <div class="features-page">
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">AI Code Mother</h1>
        <p class="hero-subtitle">智能代码生成平台 · 让编程变得简单</p>
        <p class="hero-description">
          无需深厚的编程背景,只需用自然语言描述你的需求,AI 就能为你生成完整的网站、应用程序代码
        </p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goToCreate">
            <template #icon><RocketOutlined /></template>
            立即开始
          </a-button>
          <a-button size="large" @click="scrollToFeatures">
            <template #icon><DownOutlined /></template>
            了解更多
          </a-button>
        </div>
      </div>
    </div>

    <!-- 核心特性 -->
    <div class="features-section" ref="featuresSection">
      <div class="section-header">
        <h2 class="section-title">核心特性</h2>
        <p class="section-subtitle">强大的功能,让代码生成更高效</p>
      </div>

      <div class="features-grid">
        <!-- AI 智能生成 -->
        <div class="feature-card">
          <div class="feature-icon ai-icon">
            <RobotOutlined />
          </div>
          <h3 class="feature-title">AI 智能生成</h3>
          <p class="feature-desc">基于 DeepSeek AI 模型,理解自然语言需求,生成高质量代码</p>
          <ul class="feature-list">
            <li>支持 HTML、Vue、多文件项目</li>
            <li>实时流式输出,即时查看进度</li>
            <li>符合最佳实践的代码结构</li>
          </ul>
        </div>

        <!-- 积分系统 -->
        <div class="feature-card">
          <div class="feature-icon points-icon">
            <WalletOutlined />
          </div>
          <h3 class="feature-title">积分系统</h3>
          <p class="feature-desc">完善的积分机制,多种方式获取免费积分</p>
          <ul class="feature-list">
            <li>新用户注册即送 100 积分</li>
            <li>每日签到获得 10 积分</li>
            <li>邀请好友双方各得 100 积分</li>
          </ul>
        </div>

        <!-- 在线编辑 -->
        <div class="feature-card">
          <div class="feature-icon edit-icon">
            <EditOutlined />
          </div>
          <h3 class="feature-title">在线编辑</h3>
          <p class="feature-desc">强大的在线代码编辑器,随时修改优化</p>
          <ul class="feature-list">
            <li>语法高亮和代码补全</li>
            <li>实时预览生成效果</li>
            <li>支持多文件编辑</li>
          </ul>
        </div>

        <!-- 版本管理 -->
        <div class="feature-card">
          <div class="feature-icon version-icon">
            <HistoryOutlined />
          </div>
          <h3 class="feature-title">版本管理</h3>
          <p class="feature-desc">完整的版本控制,保护每一次修改</p>
          <ul class="feature-list">
            <li>自动记录每次保存</li>
            <li>版本对比查看差异</li>
            <li>一键回滚到任意版本</li>
          </ul>
        </div>

        <!-- 快速部署 -->
        <div class="feature-card">
          <div class="feature-icon deploy-icon">
            <CloudUploadOutlined />
          </div>
          <h3 class="feature-title">快速部署</h3>
          <p class="feature-desc">一键部署到云端,即刻分享你的作品</p>
          <ul class="feature-list">
            <li>一键部署到云服务器</li>
            <li>自动配置 CDN 加速</li>
            <li>支持自定义域名</li>
          </ul>
          <a-tag color="blue">即将上线</a-tag>
        </div>

        <!-- 个人主页 -->
        <div class="feature-card">
          <div class="feature-icon profile-icon">
            <UserOutlined />
          </div>
          <h3 class="feature-title">个人主页</h3>
          <p class="feature-desc">展示你的作品集,分享创作成果</p>
          <ul class="feature-list">
            <li>精选作品展示</li>
            <li>版本历史记录</li>
            <li>访问数据统计</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 生成类型 -->
    <div class="types-section">
      <div class="section-header">
        <h2 class="section-title">支持的生成类型</h2>
        <p class="section-subtitle">根据不同需求选择合适的生成类型</p>
      </div>

      <div class="types-grid">
        <div class="type-card">
          <div class="type-header">
            <FileTextOutlined class="type-icon" />
            <h3 class="type-title">HTML 页面</h3>
            <a-tag color="green">10 积分</a-tag>
          </div>
          <p class="type-desc">生成单个 HTML 文件,适合简单页面和落地页</p>
          <div class="type-features">
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>单文件输出</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>开箱即用</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>约 10 秒生成</span>
            </div>
          </div>
        </div>

        <div class="type-card popular">
          <div class="popular-badge">最受欢迎</div>
          <div class="type-header">
            <AppstoreOutlined class="type-icon" />
            <h3 class="type-title">Vue 应用</h3>
            <a-tag color="blue">30 积分</a-tag>
          </div>
          <p class="type-desc">生成完整 Vue 项目,适合单页应用和管理后台</p>
          <div class="type-features">
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>Vue 3 + TypeScript</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>Ant Design Vue</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>约 20 秒生成</span>
            </div>
          </div>
        </div>

        <div class="type-card">
          <div class="type-header">
            <FolderOpenOutlined class="type-icon" />
            <h3 class="type-title">多文件项目</h3>
            <a-tag color="purple">50 积分</a-tag>
          </div>
          <p class="type-desc">生成多文件代码结构,适合复杂应用开发</p>
          <div class="type-features">
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>多文件多目录</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>可自定义技术栈</span>
            </div>
            <div class="type-feature">
              <CheckCircleOutlined />
              <span>约 30 秒生成</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用流程 -->
    <div class="workflow-section">
      <div class="section-header">
        <h2 class="section-title">使用流程</h2>
        <p class="section-subtitle">简单四步,快速生成你的应用</p>
      </div>

      <div class="workflow-steps">
        <div class="workflow-step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3 class="step-title">注册账号</h3>
            <p class="step-desc">填写用户信息完成注册,即可获得 100 初始积分</p>
          </div>
        </div>

        <div class="workflow-arrow">→</div>

        <div class="workflow-step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3 class="step-title">描述需求</h3>
            <p class="step-desc">用自然语言详细描述你想要的应用功能和样式</p>
          </div>
        </div>

        <div class="workflow-arrow">→</div>

        <div class="workflow-step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3 class="step-title">AI 生成</h3>
            <p class="step-desc">AI 实时生成代码,10-30 秒即可完成</p>
          </div>
        </div>

        <div class="workflow-arrow">→</div>

        <div class="workflow-step">
          <div class="step-number">4</div>
          <div class="step-content">
            <h3 class="step-title">下载使用</h3>
            <p class="step-desc">下载代码或在线编辑,部署分享给他人</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 技术栈 -->
    <div class="tech-section">
      <div class="section-header">
        <h2 class="section-title">技术架构</h2>
        <p class="section-subtitle">基于先进技术构建的可靠平台</p>
      </div>

      <div class="tech-grid">
        <div class="tech-category">
          <h3 class="tech-category-title">
            <CodeOutlined />
            前端技术
          </h3>
          <div class="tech-tags">
            <a-tag color="blue">Vue 3</a-tag>
            <a-tag color="blue">TypeScript</a-tag>
            <a-tag color="blue">Vite</a-tag>
            <a-tag color="blue">Ant Design Vue</a-tag>
            <a-tag color="blue">Pinia</a-tag>
          </div>
        </div>

        <div class="tech-category">
          <h3 class="tech-category-title">
            <ApiOutlined />
            后端技术
          </h3>
          <div class="tech-tags">
            <a-tag color="green">Spring Boot 3.5</a-tag>
            <a-tag color="green">Java 21</a-tag>
            <a-tag color="green">MyBatis-Flex</a-tag>
            <a-tag color="green">MySQL</a-tag>
            <a-tag color="green">Redis</a-tag>
          </div>
        </div>

        <div class="tech-category">
          <h3 class="tech-category-title">
            <ThunderboltOutlined />
            AI 引擎
          </h3>
          <div class="tech-tags">
            <a-tag color="purple">DeepSeek AI</a-tag>
            <a-tag color="purple">LangChain4j</a-tag>
            <a-tag color="purple">Qwen Turbo</a-tag>
            <a-tag color="purple">DashScope</a-tag>
          </div>
        </div>

        <div class="tech-category">
          <h3 class="tech-category-title">
            <CloudOutlined />
            云服务
          </h3>
          <div class="tech-tags">
            <a-tag color="orange">Tencent COS</a-tag>
            <a-tag color="orange">Prometheus</a-tag>
            <a-tag color="orange">Grafana</a-tag>
            <a-tag color="orange">Caffeine</a-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- CTA区域 -->
    <div class="cta-section">
      <div class="cta-content">
        <h2 class="cta-title">准备好开始了吗?</h2>
        <p class="cta-desc">立即注册,免费获得 100 积分,开启你的 AI 编程之旅!</p>
        <div class="cta-actions">
          <a-button type="primary" size="large" @click="goToRegister">
            <template #icon><UserAddOutlined /></template>
            免费注册
          </a-button>
          <a-button size="large" @click="goToDocs">
            <template #icon><BookOutlined /></template>
            查看文档
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  RocketOutlined,
  DownOutlined,
  RobotOutlined,
  WalletOutlined,
  EditOutlined,
  HistoryOutlined,
  CloudUploadOutlined,
  UserOutlined,
  FileTextOutlined,
  AppstoreOutlined,
  FolderOpenOutlined,
  CheckCircleOutlined,
  CodeOutlined,
  ApiOutlined,
  ThunderboltOutlined,
  CloudOutlined,
  UserAddOutlined,
  BookOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const featuresSection = ref<HTMLElement>()

// 滚动到特性区域
const scrollToFeatures = () => {
  featuresSection.value?.scrollIntoView({ behavior: 'smooth' })
}

// 跳转到创建页面
const goToCreate = () => {
  router.push('/')
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/user/register')
}

// 跳转到文档页面
const goToDocs = () => {
  window.open('/docs/guide/index.html', '_blank')
}
</script>

<style scoped>
.features-page {
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* 科技感网格背景 - 与主页保持一致 */
.features-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(139, 92, 246, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(139, 92, 246, 0.04) 1px, transparent 1px);
  background-size:
    100px 100px,
    100px 100px,
    20px 20px,
    20px 20px;
  background-position:
    -1px -1px,
    -1px -1px,
    -1px -1px,
    -1px -1px;
  pointer-events: none;
  z-index: 0;
}

/* 确保所有内容在背景之上 */
.features-page > * {
  position: relative;
  z-index: 1;
}

/* 顶部横幅 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 100px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  margin-bottom: 20px;
  animation: fadeInUp 0.8s ease;
}

.hero-subtitle {
  font-size: 24px;
  margin-bottom: 20px;
  opacity: 0.95;
  animation: fadeInUp 0.8s ease 0.2s backwards;
}

.hero-description {
  font-size: 16px;
  line-height: 1.8;
  opacity: 0.9;
  margin-bottom: 40px;
  animation: fadeInUp 0.8s ease 0.4s backwards;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  animation: fadeInUp 0.8s ease 0.6s backwards;
}

/* 核心特性 */
.features-section {
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.section-subtitle {
  font-size: 16px;
  color: #666;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-bottom: 20px;
}

.ai-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.points-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.edit-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.version-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.deploy-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.profile-icon {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  color: white;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.feature-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  font-size: 14px;
  color: #888;
  padding: 6px 0;
  padding-left: 20px;
  position: relative;
}

.feature-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #52c41a;
  font-weight: 600;
}

/* 生成类型 */
.types-section {
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 20px;
}

.types-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 30px;
}

.type-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  transition: all 0.3s ease;
}

.type-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.type-card.popular {
  border: 2px solid #1890ff;
}

.popular-badge {
  position: absolute;
  top: -12px;
  right: 20px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  padding: 4px 16px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.type-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.type-icon {
  font-size: 32px;
  color: #1890ff;
}

.type-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.type-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.type-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-feature {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #888;
}

.type-feature :deep(.anticon) {
  color: #52c41a;
  font-size: 16px;
}

/* 使用流程 */
.workflow-section {
  max-width: 1200px;
  margin: 80px auto;
  padding: 60px 20px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.workflow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
}

.workflow-step {
  display: flex;
  gap: 16px;
  max-width: 200px;
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.step-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin: 0;
}

.workflow-arrow {
  font-size: 24px;
  color: #d9d9d9;
  font-weight: 700;
}

/* 技术栈 */
.tech-section {
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 20px;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.tech-category {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.tech-category-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tech-category-title :deep(.anticon) {
  font-size: 20px;
  color: #1890ff;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* CTA区域 */
.cta-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px;
  text-align: center;
  margin-top: 80px;
}

.cta-content {
  max-width: 800px;
  margin: 0 auto;
}

.cta-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin-bottom: 16px;
}

.cta-desc {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 32px;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .section-title {
    font-size: 28px;
  }

  .features-grid,
  .types-grid,
  .tech-grid {
    grid-template-columns: 1fr;
  }

  .workflow-steps {
    flex-direction: column;
  }

  .workflow-arrow {
    transform: rotate(90deg);
  }

  .hero-actions,
  .cta-actions {
    flex-direction: column;
  }
}
</style>
