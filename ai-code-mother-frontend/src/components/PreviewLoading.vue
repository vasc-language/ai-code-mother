<template>
  <div class="preview-loading-container">
    <div class="loading-content">
      <!-- Favicon 图标和标题 -->
      <div class="icon-section">
        <img src="/favicon.ico" alt="Loading" class="favicon-icon" />
        <h2 class="loading-title">Spinning up preview</h2>
      </div>

      <!-- 循环滚动的提示文字 -->
      <div class="scrolling-tips">
        <div
          class="tips-wrapper"
          :class="{ 'no-transition': !enableTransition }"
          :style="{ transform: `translateY(-${currentIndex * 60}px)` }"
        >
          <div
            v-for="(tip, index) in displayTips"
            :key="index"
            class="tip-item"
            :class="{ 'active': index === currentIndex }"
          >
            <img :src="tip.icon" :alt="tip.text" class="tip-icon" />
            <span class="tip-text">{{ tip.text }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import chatIcon from '@/assets/chat.svg'
import selectIcon from '@/assets/select.svg'
import uploadIcon from '@/assets/Upload .svg'
import previewIcon from '@/assets/preview .svg'
import editIcon from '@/assets/edit.svg'
import deployIcon from '@/assets/Deploy.svg'

interface Tip {
  icon: string
  text: string
}

const tips: Tip[] = [
  { icon: chatIcon, text: 'Chat with AI in the sidebar' },
  { icon: selectIcon, text: 'Select specific elements to modify' },
  { icon: uploadIcon, text: 'Upload images as a reference' },
  { icon: previewIcon, text: 'Instantly preview your changes' },
  { icon: editIcon, text: 'Set custom knowledge for every edit' },
  { icon: deployIcon, text: 'Deploy when you\'re ready' }
]

// 创建一个扩展的数组用于无缝循环
const displayTips = ref<Tip[]>([...tips, tips[0]])
const currentIndex = ref(0)
const enableTransition = ref(true)
let intervalId: number | null = null

onMounted(() => {
  // 每3秒切换一个提示
  intervalId = window.setInterval(() => {
    currentIndex.value++

    // 当到达最后一个重复项时，等待动画完成后重置到开始位置
    if (currentIndex.value >= tips.length) {
      // 等待过渡动画完成（600ms）
      setTimeout(() => {
        // 禁用过渡效果
        enableTransition.value = false
        // 立即重置到起始位置
        currentIndex.value = 0

        // 在下一帧重新启用过渡效果
        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            enableTransition.value = true
          })
        })
      }, 600)
    }
  }, 3000)
})

onUnmounted(() => {
  if (intervalId !== null) {
    clearInterval(intervalId)
  }
})
</script>

<style scoped>
.preview-loading-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: #FFFBF7;
  overflow: hidden;
}

.loading-content {
  width: 100%;
  max-width: 500px;
  padding-top: calc(33.33vh - 150px); /* 页面上方 1/3 处 */
  text-align: center;
}

.icon-section {
  margin-bottom: 60px;
  animation: fadeInDown 0.8s ease-out;
}

.favicon-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 24px;
  animation: pulse 2s ease-in-out infinite;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.1));
}

.loading-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  letter-spacing: -0.5px;
}

.scrolling-tips {
  height: 60px;
  overflow: hidden;
  position: relative;
  background: transparent;
  padding: 0;
}

/* 添加上下渐变雾化遮罩 */
.scrolling-tips::before,
.scrolling-tips::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 30px;
  pointer-events: none;
  z-index: 2;
}

.scrolling-tips::before {
  top: 0;
  background: linear-gradient(to bottom,
    rgba(255, 251, 247, 0.9) 0%,
    rgba(255, 251, 247, 0.3) 50%,
    transparent 100%);
}

.scrolling-tips::after {
  bottom: 0;
  background: linear-gradient(to top,
    rgba(255, 251, 247, 0.9) 0%,
    rgba(255, 251, 247, 0.3) 50%,
    transparent 100%);
}

.tips-wrapper {
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.tips-wrapper.no-transition {
  transition: none;
}

.tip-item {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0.3;
  filter: blur(1px);
  transition: all 0.6s ease;
}

.tip-item.active {
  opacity: 1;
  filter: blur(0px);
}

.tip-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  object-fit: contain;
}

.tip-text {
  font-size: 16px;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

/* 动画效果 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-content {
    max-width: 90%;
    padding-top: 25vh;
  }

  .favicon-icon {
    width: 60px;
    height: 60px;
  }

  .loading-title {
    font-size: 22px;
  }

  .tip-text {
    font-size: 14px;
  }

  .tip-icon {
    width: 20px;
    height: 20px;
  }
}
</style>
