<template>
  <a-modal v-model:open="visible" title="部署中" :footer="null" :closable="false" width="600px">
    <div class="deploying-container">
      <!-- 部署动效 -->
      <div class="deploying-animation">
        <div class="rocket-container">
          <div class="rocket">🚀</div>
          <div class="cloud cloud-1">☁️</div>
          <div class="cloud cloud-2">☁️</div>
          <div class="cloud cloud-3">☁️</div>
        </div>
      </div>

      <!-- 部署状态文字 -->
      <h3>项目部署中...</h3>

      <!-- 雾化文案 -->
      <div class="quote-container">
        <p class="quote-text frosted">Good things come to those who wait.</p>
      </div>

      <!-- 加载进度条 -->
      <div class="progress-bar">
        <div class="progress-fill"></div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  open: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})
</script>

<style scoped>
.deploying-container {
  text-align: center;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 火箭动画容器 */
.deploying-animation {
  position: relative;
  width: 150px;
  height: 150px;
  margin-bottom: 24px;
}

.rocket-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.rocket {
  font-size: 48px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%) rotate(-45deg);
  animation: rocket-fly 2s ease-in-out infinite;
}

/* 云朵动画 */
.cloud {
  font-size: 24px;
  position: absolute;
  opacity: 0.6;
  animation: cloud-float 3s ease-in-out infinite;
}

.cloud-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.cloud-2 {
  top: 50%;
  right: 15%;
  animation-delay: 1s;
}

.cloud-3 {
  bottom: 25%;
  left: 20%;
  animation-delay: 2s;
}

/* 标题样式 */
h3 {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 0.5px;
}

/* 引用文案容器 */
.quote-container {
  margin-bottom: 24px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #f8f6f0 0%, #e8e3d8 100%);
  border-radius: 12px;
  border: 2px solid #d4cfc4;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

/* 雾化文字效果 */
.quote-text {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  font-style: italic;
  color: #5d4e37;
  letter-spacing: 1px;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.quote-text.frosted {
  background: linear-gradient(
    90deg,
    rgba(93, 78, 55, 0.3) 0%,
    rgba(93, 78, 55, 1) 20%,
    rgba(93, 78, 55, 1) 80%,
    rgba(93, 78, 55, 0.3) 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: text-shimmer 3s ease-in-out infinite;
}

/* 背景雾化效果 */
.quote-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.3) 0%,
    rgba(255, 255, 255, 0) 70%
  );
  animation: fog-move 6s linear infinite;
}

/* 进度条 */
.progress-bar {
  width: 100%;
  height: 6px;
  background: #e8e3d8;
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d4cfc4 0%, #b8b0a0 50%, #d4cfc4 100%);
  background-size: 200% 100%;
  border-radius: 3px;
  animation: progress-loading 2s ease-in-out infinite;
}

/* 火箭飞行动画 */
@keyframes rocket-fly {
  0%, 100% {
    transform: translate(-50%, -50%) rotate(-45deg) translateY(0);
  }
  50% {
    transform: translate(-50%, -50%) rotate(-45deg) translateY(-20px);
  }
}

/* 云朵漂浮动画 */
@keyframes cloud-float {
  0%, 100% {
    transform: translateX(0) translateY(0);
    opacity: 0.4;
  }
  50% {
    transform: translateX(15px) translateY(-10px);
    opacity: 0.8;
  }
}

/* 文字闪烁动画 */
@keyframes text-shimmer {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* 雾气移动动画 */
@keyframes fog-move {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(-10%, 10%) rotate(180deg);
  }
  100% {
    transform: translate(0, 0) rotate(360deg);
  }
}

/* 进度条加载动画 */
@keyframes progress-loading {
  0% {
    width: 0%;
    background-position: 0% 0%;
  }
  50% {
    width: 70%;
    background-position: 100% 0%;
  }
  100% {
    width: 100%;
    background-position: 200% 0%;
  }
}
</style>
