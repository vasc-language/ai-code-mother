import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import ChatManagePage from '@/pages/admin/ChatManagePage.vue'
import SignInPage from '@/pages/points/SignInPage.vue'
import InvitePage from '@/pages/points/InvitePage.vue'
import PointsDetailPage from '@/pages/points/PointsDetailPage.vue'
import DocsLayout from '@/layouts/DocsLayout.vue'
import QuickStartDoc from '@/pages/docs/QuickStartDoc.vue'
import AIGenerationDoc from '@/pages/docs/features/AIGenerationDoc.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/docs',
      component: DocsLayout,
      children: [
        {
          path: '',
          redirect: '/docs/quick-start',
        },
        {
          path: 'quick-start',
          name: '快速开始',
          component: QuickStartDoc,
        },
        {
          path: 'features/ai-generation',
          name: 'AI 智能生成',
          component: AIGenerationDoc,
        },
        {
          path: 'features/points-system',
          name: '积分系统',
          component: () => import('@/pages/docs/features/PointsSystemDoc.vue'),
        },
        {
          path: 'features/online-editor',
          name: '在线编辑器',
          component: () => import('@/pages/docs/features/OnlineEditorDoc.vue'),
        },
        {
          path: 'features/version-control',
          name: '版本管理',
          component: () => import('@/pages/docs/features/VersionControlDoc.vue'),
        },
        {
          path: 'tutorial/create-html',
          name: '生成 HTML 页面教程',
          component: () => import('@/pages/docs/tutorial/CreateHTMLDoc.vue'),
        },
        {
          path: 'tutorial/create-vue',
          name: '生成 Vue 应用教程',
          component: () => import('@/pages/docs/tutorial/CreateVueDoc.vue'),
        },
        {
          path: 'tutorial/create-multifile',
          name: '生成多文件项目教程',
          component: () => import('@/pages/docs/tutorial/CreateMultifileDoc.vue'),
        },
        {
          path: 'tutorial/edit-code',
          name: '在线编辑代码教程',
          component: () => import('@/pages/docs/tutorial/EditCodeDoc.vue'),
        },
        {
          path: 'faq',
          name: '常见问题',
          component: () => import('@/pages/docs/FAQDoc.vue'),
        },
        {
          path: 'api',
          name: 'API 文档',
          component: () => import('@/pages/docs/APIDoc.vue'),
        },
      ],
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/user/profile/:userId?',
      name: '个人主页',
      component: UserProfilePage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: ChatManagePage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
      meta: { keepAlive: true },
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: AppEditPage,
    },
    {
      path: '/points/sign-in',
      name: '每日签到',
      component: SignInPage,
    },
    {
      path: '/points/invite',
      name: '邀请好友',
      component: InvitePage,
    },
    {
      path: '/points/detail',
      name: '积分明细',
      component: PointsDetailPage,
    },
  ],
})

export default router
