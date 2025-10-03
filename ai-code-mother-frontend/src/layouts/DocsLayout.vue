<template>
  <div class="docs-layout">
    <a-layout>
      <!-- 左侧导航栏 -->
      <a-layout-sider
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        width="280"
        class="docs-sider"
        :breakpoint="'lg'"
        @breakpoint="onBreakpoint"
      >
        <!-- Logo和标题区域 -->
        <div class="docs-sider-header">
          <div class="logo-container" @click="goHome">
            <img v-if="!collapsed" class="logo-img" src="@/assets/logo.png" alt="Logo" />
            <BookOutlined v-else class="docs-icon" />
            <span v-if="!collapsed" class="docs-title">AI Code Mother</span>
          </div>
          <a-button
            v-if="!collapsed"
            class="back-btn"
            size="small"
            @click="goHome"
          >
            <template #icon><HomeOutlined /></template>
            返回首页
          </a-button>
        </div>

        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          :theme="'light'"
          class="docs-menu"
        >
          <a-menu-item key="/docs/quick-start" @click="navigateTo('/docs/quick-start')">
            <template #icon><RocketOutlined /></template>
            快速开始
          </a-menu-item>

          <a-sub-menu key="features">
            <template #icon><AppstoreOutlined /></template>
            <template #title>功能详解</template>
            <a-menu-item key="/docs/features/ai-generation" @click="navigateTo('/docs/features/ai-generation')">
              AI 智能生成
            </a-menu-item>
            <a-menu-item key="/docs/features/points-system" @click="navigateTo('/docs/features/points-system')">
              积分系统
            </a-menu-item>
            <a-menu-item key="/docs/features/online-editor" @click="navigateTo('/docs/features/online-editor')">
              在线编辑
            </a-menu-item>
            <a-menu-item key="/docs/features/version-control" @click="navigateTo('/docs/features/version-control')">
              版本管理
            </a-menu-item>
          </a-sub-menu>

          <a-sub-menu key="tutorial">
            <template #icon><FileTextOutlined /></template>
            <template #title>使用教程</template>
            <a-menu-item key="/docs/tutorial/create-html" @click="navigateTo('/docs/tutorial/create-html')">
              生成 HTML 页面
            </a-menu-item>
            <a-menu-item key="/docs/tutorial/create-vue" @click="navigateTo('/docs/tutorial/create-vue')">
              生成 Vue 应用
            </a-menu-item>
            <a-menu-item key="/docs/tutorial/create-multifile" @click="navigateTo('/docs/tutorial/create-multifile')">
              生成多文件项目
            </a-menu-item>
            <a-menu-item key="/docs/tutorial/edit-code" @click="navigateTo('/docs/tutorial/edit-code')">
              在线编辑代码
            </a-menu-item>
          </a-sub-menu>

          <a-menu-item key="/docs/faq" @click="navigateTo('/docs/faq')">
            <template #icon><QuestionCircleOutlined /></template>
            常见问题
          </a-menu-item>

          <a-menu-item key="/docs/api" @click="navigateTo('/docs/api')">
            <template #icon><ApiOutlined /></template>
            API 文档
          </a-menu-item>
        </a-menu>
      </a-layout-sider>

      <!-- 右侧内容区域 -->
      <a-layout class="docs-main">
        <!-- 简化的顶部工具栏 -->
        <div class="docs-toolbar">
          <MenuUnfoldOutlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <MenuFoldOutlined
            v-else
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />

          <a-breadcrumb class="docs-breadcrumb">
            <a-breadcrumb-item>
              <HomeOutlined @click="goHome" style="cursor: pointer;" />
            </a-breadcrumb-item>
            <a-breadcrumb-item>文档</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentDocTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <a-layout-content class="docs-content">
          <div class="docs-content-wrapper">
            <RouterView />
          </div>
        </a-layout-content>

        <!-- 底部版权 -->
        <div class="docs-footer">
          <p>© 2025 AI Code Mother. All rights reserved.</p>
        </div>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  BookOutlined,
  RocketOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  QuestionCircleOutlined,
  ApiOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  HomeOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)
const selectedKeys = ref<string[]>([route.path])
const openKeys = ref<string[]>(['features', 'tutorial'])

// 监听路由变化更新选中菜单
watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

// 响应式断点
const onBreakpoint = (broken: boolean) => {
  collapsed.value = broken
}

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 当前文档标题
const currentDocTitle = computed(() => {
  const path = route.path
  const titleMap: Record<string, string> = {
    '/docs/quick-start': '快速开始',
    '/docs/features/ai-generation': 'AI 智能生成',
    '/docs/features/points-system': '积分系统',
    '/docs/features/online-editor': '在线编辑',
    '/docs/features/version-control': '版本管理',
    '/docs/tutorial/create-html': '生成 HTML 页面',
    '/docs/tutorial/create-vue': '生成 Vue 应用',
    '/docs/tutorial/create-multifile': '生成多文件项目',
    '/docs/tutorial/edit-code': '在线编辑代码',
    '/docs/faq': '常见问题',
    '/docs/api': 'API 文档',
  }
  return titleMap[path] || '文档'
})
</script>

<style scoped>
.docs-layout {
  min-height: 100vh;
  background: var(--bg-gradient);
  background-attachment: fixed;
}

/* 侧边栏样式 - 优化宽度和阴影 */
.docs-sider {
  background: white;
  box-shadow: 2px 0 16px rgba(236, 72, 153, 0.12);
  overflow: hidden;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.docs-sider-header {
  padding: 24px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: sticky;
  top: 0;
  z-index: 10;
  flex-shrink: 0;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logo-container:hover {
  transform: scale(1.05);
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.docs-icon {
  font-size: 28px;
  color: white;
}

.docs-title {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.back-btn {
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: white;
  color: white;
}

.docs-menu {
  border-right: none;
  padding: 16px 0;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 140px);
}

:deep(.ant-menu-item) {
  margin: 4px 12px;
  border-radius: 10px;
  height: 44px;
  line-height: 44px;
  transition: all 0.2s ease;
}

:deep(.ant-menu-submenu-title) {
  margin: 4px 12px;
  border-radius: 10px;
  height: 44px;
  line-height: 44px;
  transition: all 0.2s ease;
}

:deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15) 0%, rgba(249, 115, 22, 0.15) 100%);
  color: #ec4899;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(236, 72, 153, 0.15);
}

:deep(.ant-menu-item:hover) {
  background: rgba(236, 72, 153, 0.08);
  color: #ec4899;
  transform: translateX(4px);
}

/* 主内容区域 */
.docs-main {
  margin-left: 280px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 工具栏样式 */
.docs-toolbar {
  background: white;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
}

.trigger {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
  color: #ec4899;
}

.trigger:hover {
  color: #f97316;
}

.docs-breadcrumb {
  flex: 1;
}

:deep(.docs-breadcrumb .ant-breadcrumb-link) {
  color: #666;
}

:deep(.docs-breadcrumb .ant-breadcrumb-link:hover) {
  color: #ec4899;
}

/* 内容区域样式 */
.docs-content {
  flex: 1;
  padding: 32px 24px;
}

.docs-content-wrapper {
  background: white;
  padding: 48px 64px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 200px);
}

/* 底部版权 */
.docs-footer {
  background: white;
  padding: 24px;
  text-align: center;
  color: #999;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
}

/* 响应式设计 - 优化移动端体验 */
@media (max-width: 992px) {
  .docs-sider {
    position: fixed !important;
    z-index: 100;
  }

  .docs-main {
    margin-left: 0;
  }

  .docs-sider:not(.ant-layout-sider-collapsed) {
    width: 280px !important;
  }
}

@media (max-width: 768px) {
  .docs-main {
    margin-left: 0;
  }

  .docs-content-wrapper {
    padding: 24px 16px;
  }

  .docs-sider {
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .docs-sider:not(.ant-layout-sider-collapsed) {
    transform: translateX(0);
    box-shadow: 4px 0 24px rgba(0, 0, 0, 0.2);
  }

  /* 移动端遮罩 */
  .docs-sider:not(.ant-layout-sider-collapsed)::after {
    content: '';
    position: fixed;
    top: 0;
    left: 280px;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: -1;
  }
}
</style>
