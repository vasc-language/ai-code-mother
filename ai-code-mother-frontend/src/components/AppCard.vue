<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span>🤖</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button v-if="isOwner" type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()
const loginUserStore = useLoginUserStore()

// 判断当前用户是否是应用的创建者
const isOwner = computed(() => {
  return loginUserStore.loginUser.id &&
         props.app.userId === loginUserStore.loginUser.id
})

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: var(--card-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-normal);
  cursor: pointer;
  position: relative;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  opacity: 0;
  transition: var(--transition-normal);
}

.app-card:hover::before {
  opacity: 1;
}

.app-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-2xl);
}

.app-card--featured {
  background: linear-gradient(135deg, 
    rgba(99, 102, 241, 0.05) 0%, 
    rgba(236, 72, 153, 0.05) 50%, 
    rgba(255, 255, 255, 0.9) 100%);
  border: 2px solid rgba(99, 102, 241, 0.2);
}

.app-card--featured::before {
  opacity: 1;
  background: linear-gradient(90deg, 
    var(--primary-color) 0%, 
    var(--secondary-color) 50%, 
    var(--success-color) 100%);
}

.app-preview {
  height: 200px;
  background: var(--gray-100);
  background-image: 
    linear-gradient(45deg, transparent 40%, rgba(99, 102, 241, 0.05) 50%, transparent 60%),
    radial-gradient(circle at center, rgba(236, 72, 153, 0.05) 0%, transparent 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition-normal);
}

.app-card:hover .app-preview img {
  transform: scale(1.1);
}

.app-placeholder {
  font-size: var(--font-size-4xl);
  color: var(--gray-300);
  font-weight: var(--font-weight-light);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(0, 0, 0, 0.6) 0%, 
    rgba(99, 102, 241, 0.4) 100%);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: var(--transition-normal);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-info {
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  font-family: var(--font-family-primary);
  margin: 0 0 var(--spacing-xs);
  color: var(--gray-800);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: var(--transition-fast);
}

.app-card:hover .app-title {
  color: var(--primary-color);
}

.app-author {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--gray-500);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Ant Design 组件样式覆盖 */
:deep(.ant-btn) {
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-fast);
  backdrop-filter: blur(10px);
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border: none;
  box-shadow: var(--shadow-sm);
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary-color) 100%);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

:deep(.ant-btn-default) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: var(--gray-600);
  box-shadow: var(--shadow-sm);
}

:deep(.ant-btn-default:hover) {
  background: rgba(255, 255, 255, 1);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

:deep(.ant-avatar) {
  background: var(--primary-color);
  color: var(--white);
  font-weight: var(--font-weight-medium);
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-fast);
}

.app-card:hover :deep(.ant-avatar) {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: scale(1.1);
}

/* 动画效果 */
@keyframes cardShimmer {
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: 200px 0;
  }
}

.app-card--featured::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(99, 102, 241, 0.1), 
    transparent);
  background-size: 200px 100%;
  animation: cardShimmer 2s infinite;
  pointer-events: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-preview {
    height: 160px;
  }
  
  .app-info {
    padding: var(--spacing-md);
  }
  
  .app-title {
    font-size: var(--font-size-base);
  }
  
  .app-author {
    font-size: var(--font-size-xs);
  }
}
</style>
