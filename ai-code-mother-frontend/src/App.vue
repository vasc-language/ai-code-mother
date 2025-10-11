<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

const route = useRoute()

// 判断是否是文档页面
const isDocsPage = computed(() => route.path.startsWith('/docs'))

// 判断是否是应用生成页面(需要全屏显示,不需要GlobalHeader)
const isAppChatPage = computed(() => route.path.startsWith('/app/chat/'))
</script>

<template>
  <!-- 文档页面和应用生成页面直接显示内容,不使用BasicLayout -->
  <div v-if="isDocsPage || isAppChatPage">
    <router-view />
  </div>
  <!-- 其他页面使用BasicLayout -->
  <BasicLayout v-else />
</template>

<style>
/* 导入全局样式 */
@import './styles/globals.css';
</style>
