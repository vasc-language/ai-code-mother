<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthCheck } from '@/api/healthController'

healthCheck().then((res) => {
  console.log('健康检查结果:', res)
})
</script>

<template>
  <BasicLayout />
</template>

<style></style>
