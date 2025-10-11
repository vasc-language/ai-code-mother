<template>
  <a-layout-header class="header">
    <div class="header-content">
      <!-- 左侧：Logo和标题 -->
      <div class="header-left">
        <RouterLink to="/">
          <div class="logo-section">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">AICodeHub</h1>
          </div>
        </RouterLink>
      </div>
      <!-- 中间：导航菜单 -->
      <div class="header-center">
        <div class="custom-menu">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="menu-item"
            :class="{ 'menu-item-selected': selectedKeys.includes(item.key) }"
            @click="handleMenuItemClick(item)"
          >
            <component :is="item.icon" v-if="item.icon" class="menu-icon" />
            <span class="menu-label">{{ getMenuLabel(item) }}</span>
          </div>
        </div>
      </div>
      <!-- 右侧：用户操作区域 -->
      <div class="header-right">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="goToProfile">
                    <UserOutlined />
                    个人主页
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="goToPointsDetail">
                    <WalletOutlined />
                    积分明细
                  </a-menu-item>
                  <a-menu-item @click="goToSignIn">
                    <CalendarOutlined />
                    每日签到
                  </a-menu-item>
                  <a-menu-item @click="goToInvite">
                    <TeamOutlined />
                    邀请好友
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, UserOutlined, AppstoreOutlined, GithubOutlined, WalletOutlined, CalendarOutlined, TeamOutlined, BookOutlined } from '@ant-design/icons-vue'
import PointsDisplay from './PointsDisplay.vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/docs',
    icon: () => h(BookOutlined),
    label: '使用文档',
    title: '使用文档',
  },
  {
    key: '/admin/userManage',
    icon: () => h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    icon: () => h(AppstoreOutlined),
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: 'others',
    icon: () => h(GithubOutlined),
    label: h('a', { href: 'https://github.com/vasc-language/ai-code-mother', target: '_blank' }, '项目仓库'),
    title: '项目仓库',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 处理自定义菜单项点击
const handleMenuItemClick = (item: any) => {
  const key = item.key as string
  selectedKeys.value = [key]

  if (key === 'others') {
    // 项目仓库是外部链接
    window.open('https://github.com/vasc-language/ai-code-mother', '_blank')
  } else if (key.startsWith('/')) {
    // 内部路由跳转
    router.push(key)
  }
}

// 获取菜单标签文本
const getMenuLabel = (item: any) => {
  if (typeof item.label === 'string') {
    return item.label
  }
  return item.title || ''
}

// 跳转到个人主页
const goToProfile = () => {
  router.push('/user/profile')
}

const goToPointsDetail = () => {
  router.push('/points/detail')
}

const goToSignIn = () => {
  router.push('/points/sign-in')
}

const goToInvite = () => {
  router.push('/points/invite')
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
  box-shadow: var(--shadow-lg);
  margin: 0 var(--spacing-lg) var(--spacing-lg) var(--spacing-lg);
  padding: 0 var(--spacing-xl);
  transition: var(--transition-normal);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header:hover {
  box-shadow: var(--shadow-xl);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 64px;
}

.header-left {
  flex-shrink: 0;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo {
  height: 40px;
  width: 40px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-fast);
}

.logo:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.site-title {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  background: var(--button-gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: var(--transition-fast);
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 var(--spacing-xl);
  min-width: 400px;
  overflow: visible;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-login-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* 自定义菜单样式 - 优化版本 */
.custom-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  flex-wrap: nowrap;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  height: 44px;
  border-radius: 22px;
  font-weight: var(--font-weight-medium);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 56, 255, 0.1) 0%, rgba(0, 209, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.menu-item:hover::before {
  opacity: 1;
}

.menu-item:hover {
  background: transparent;
  color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 56, 255, 0.15);
}

.menu-item-selected {
  background: linear-gradient(135deg, rgba(0, 56, 255, 0.15) 0%, rgba(0, 209, 255, 0.15) 100%);
  color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(0, 56, 255, 0.2);
  font-weight: 600;
}

.menu-item-selected::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: var(--button-gradient-primary);
  border-radius: 2px;
}

.menu-icon {
  font-size: 18px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.menu-label {
  font-size: 15px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

/* 用户下拉菜单样式 */
:deep(.ant-dropdown-menu) {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-sm);
}

:deep(.ant-dropdown-menu-item) {
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

:deep(.ant-dropdown-menu-item:hover) {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary-color);
}

/* 头像样式 */
:deep(.ant-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-fast);
}

:deep(.ant-avatar:hover) {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

/* 登录按钮样式 - 蓝色渐变 */
:deep(.ant-btn-primary) {
  background: var(--button-gradient-primary);
  border: none;
  border-radius: var(--radius-full);
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: var(--shadow-md);
  transition: var(--transition-normal);
}

:deep(.ant-btn-primary:hover) {
  background: var(--button-gradient-secondary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    margin: 0 var(--spacing-sm) var(--spacing-md) var(--spacing-sm);
    padding: 0 var(--spacing-lg);
    border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  }

  .header-content {
    height: 56px;
  }

  .site-title {
    font-size: var(--font-size-base);
  }

  .logo {
    height: 32px;
    width: 32px;
  }

  .header-center {
    margin: 0 var(--spacing-xs);
    flex: 1;
    display: flex;
    justify-content: center;
    overflow: visible;
  }

  .custom-menu {
    gap: var(--spacing-xs);
    flex-wrap: nowrap;
    overflow: visible;
  }

  .menu-item {
    height: 32px;
    padding: 0 8px;
    font-size: var(--font-size-sm);
  }

  .menu-icon {
    font-size: 14px;
  }

  .menu-label {
    font-size: 12px;
  }
}
</style>
