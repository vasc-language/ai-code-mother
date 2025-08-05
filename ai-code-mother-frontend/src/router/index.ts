/**
 * 路由配置
 * 定义应用的页面路由规则
 */
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/page/HomeView.vue'

const router = createRouter({
  // 使用 HTML5 History 模式
  history: createWebHistory(import.meta.env.BASE_URL),
  // 路由配置
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
  ],
})

export default router
