<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view v-slot="{ Component, route }">
        <keep-alive include="AppChatPage">
          <component v-if="route.meta?.keepAlive" :is="Component" />
        </keep-alive>
        <component v-if="!route.meta?.keepAlive" :is="Component" />
      </router-view>
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: var(--bg-gradient);
  background-attachment: fixed;
}

.main-content {
  width: 100%;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: transparent;
  margin: 0;
  flex: 1;
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--spacing-md) var(--spacing-lg);
  }
}
</style>
