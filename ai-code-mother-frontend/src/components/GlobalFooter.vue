<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://github.com/vasc-language/ai-code-mother"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          项目地址 by vasc-language
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  box-shadow: var(--shadow-lg);
  text-align: center;
  padding: var(--spacing-xl) var(--spacing-lg);
  margin: var(--spacing-xl) var(--spacing-lg) 0 var(--spacing-lg);
  position: relative;
  transition: var(--transition-normal);
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: var(--radius-full);
  opacity: 0.7;
}

.footer:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-lg);
}

.copyright {
  margin: 0;
  color: var(--gray-600);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-primary);
  font-weight: var(--font-weight-medium);
  line-height: var(--line-height-relaxed);
}

.author-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
  transition: var(--transition-fast);
  position: relative;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-md);
}

.author-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  opacity: 0;
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
  z-index: -1;
}

.author-link:hover {
  color: var(--white);
  text-decoration: none;
  transform: translateY(-1px);
}

.author-link:hover::before {
  opacity: 1;
}

/* 移除多余的装饰内容 */

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    margin: var(--spacing-lg) var(--spacing-sm) 0 var(--spacing-sm);
    padding: var(--spacing-lg) var(--spacing-md);
    border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  }
  
  .copyright {
    font-size: var(--font-size-xs);
  }
  
  .author-link {
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}

/* 深度覆盖 Ant Design 样式 */
:deep(.ant-layout-footer) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
}
</style>
