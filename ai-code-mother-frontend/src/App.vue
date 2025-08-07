<!--
  主应用组件
  作为应用的根组件，负责加载基础布局和执行初始化操作
-->
<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
/**
 * 全局初始化：在应用加载时获取当前登录用户信息。
 * App.vue 作为根组件，在此处调用可以确保应用启动时立即恢复用户的登录状态，
 * 以便其他组件（如路由守卫、页面）可以同步获取和使用这些信息。
 */
loginUserStore.fetchLoginUser()
</script>

<template>
  <BasicLayout />
</template>

<style></style>

