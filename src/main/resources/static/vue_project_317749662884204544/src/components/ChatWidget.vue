<template>
  <div class="chat-widget" :class="{ active: isOpen }" @click="toggleChat">
    <div class="chat-icon">💬</div>
    <div class="chat-content" v-if="isOpen">
      <h4>在线客服</h4>
      <p>您好！有什么可以帮助您的？</p>
      <input placeholder="输入您的问题..." />
      <button class="btn btn-primary">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const isOpen = ref(false)
const toggleChat = () => {
  isOpen.value = !isOpen.value
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  padding: 1rem;
  cursor: pointer;
  z-index: 1000;
}
.chat-icon {
  font-size: 1.5rem;
}
.chat-content {
  margin-top: 1rem;
}
.chat-content h4 {
  margin-bottom: 0.5rem;
}
.chat-content input {
  width: 100%;
  padding: 8px;
  margin: 0.5rem 0;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>