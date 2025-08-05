<!--
  主应用组件
  作为应用的根组件，负责加载基础布局和执行初始化操作
-->
<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthCheck } from '@/api/healthController'

// 执行健康检查，确认后端服务可用性
healthCheck().then((res) => {
  console.log('健康检查结果:', res)
})
</script>

<template>
  <BasicLayout />
</template>

<style></style>
