<!--
  全局底部组件
  显示版权信息和作者链接
-->
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
          Github 项目仓库
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 静态组件，无需响应式数据
</script>

<style scoped>
.footer {
  background: #efefef;
  text-align: center;
  padding: 20px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
