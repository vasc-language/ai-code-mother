<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-main">
        <div class="footer-brand">
          <h3 class="brand-title">AI Code Mother</h3>
          <p class="brand-desc">让 AI 为你生成专业代码</p>
        </div>

        <div class="footer-links">
          <div class="link-group">
            <h4 class="group-title">快速链接</h4>
            <a href="/" class="footer-link">首页</a>
            <a href="/docs" class="footer-link">文档</a>
          </div>

          <div class="link-group">
            <h4 class="group-title">开发者</h4>
            <a
              href="https://github.com/vasc-language/ai-code-mother"
              target="_blank"
              rel="noopener noreferrer"
              class="footer-link"
            >
              GitHub 仓库
            </a>
            <a href="/docs/api" class="footer-link">API 文档</a>
          </div>

          <div class="link-group">
            <h4 class="group-title">联系我们</h4>
            <a href="mailto:zrt3ljnygz@163.com" class="footer-link">发送邮件</a>
            <span class="footer-link-text">微信: Join2049</span>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="copyright">
          © 2025 AI Code Mother. All rights reserved.
        </div>
        <div class="author">
          Made with ❤️ by
          <a
            href="https://github.com/vasc-language"
            target="_blank"
            rel="noopener noreferrer"
            class="author-link"
          >
            vasc-language
          </a>
        </div>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: linear-gradient(180deg, rgba(249, 250, 251, 0.8) 0%, rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(236, 72, 153, 0.1);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.04);
  padding: var(--spacing-xl) var(--spacing-lg) var(--spacing-lg);
  margin-top: var(--spacing-xl);
  position: relative;
  overflow: hidden;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ec4899 0%, #f97316 50%, #ec4899 100%);
  background-size: 200% 100%;
  animation: gradientMove 3s ease infinite;
}

@keyframes gradientMove {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: var(--spacing-xl);
  padding-bottom: var(--spacing-xl);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: var(--spacing-lg);
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.brand-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  margin: 0;
  color: var(--gray-600);
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-lg);
}

.link-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.group-title {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--gray-800);
}

.footer-link {
  color: var(--gray-600);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s ease;
  width: fit-content;
  position: relative;
  padding: 2px 0;
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #ec4899 0%, #f97316 100%);
  transition: width 0.3s ease;
}

.footer-link:hover {
  color: var(--primary-color);
}

.footer-link:hover::after {
  width: 100%;
}

.footer-link-text {
  color: var(--gray-600);
  font-size: 14px;
  cursor: default;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.copyright {
  color: var(--gray-500);
  font-size: 13px;
}

.author {
  color: var(--gray-500);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s ease;
  position: relative;
}

.author-link:hover {
  color: var(--primary-dark);
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .footer-links {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .footer {
    padding: var(--spacing-lg) var(--spacing-md);
  }

  .footer-links {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .footer-bottom {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--spacing-xs);
  }
}

/* 深度覆盖 Ant Design 样式 */
:deep(.ant-layout-footer) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
}
</style>
