<template>
  <a-layout-header class="header">
    <div class="header-content">
      <!-- 左侧：Logo和标题 -->
      <div class="header-left">
        <RouterLink to="/">
          <div class="logo-section">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">零代码应用生成</h1>
          </div>
        </RouterLink>
      </div>
      <!-- 中间：导航菜单 -->
      <div class="header-center">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
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
import { LogoutOutlined, HomeOutlined, UserOutlined, AppstoreOutlined, GithubOutlined } from '@ant-design/icons-vue'

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
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
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
}

.header-right {
  flex-shrink: 0;
}

.user-login-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* Ant Design Menu 样式覆盖 */
:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
  background: transparent !important;
  line-height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-menu-item) {
  border-radius: var(--radius-md);
  margin: 0 var(--spacing-sm);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-fast);
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
}

:deep(.ant-menu-item .anticon) {
  margin-right: 6px;
  font-size: 16px;
}

:deep(.ant-menu-item:hover) {
  background: rgba(99, 102, 241, 0.1) !important;
  color: var(--primary-color) !important;
}

:deep(.ant-menu-item-selected) {
  background: rgba(99, 102, 241, 0.15) !important;
  color: var(--primary-color) !important;
}

:deep(.ant-menu-item-selected::after) {
  display: none;
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

/* 登录按钮样式 */
:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border: none;
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-medium);
  box-shadow: var(--shadow-sm);
  transition: var(--transition-fast);
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary-color) 100%);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
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
    margin: 0 var(--spacing-md);
  }
  
  :deep(.ant-menu-horizontal) {
    line-height: 56px;
  }
  
  :deep(.ant-menu-item) {
    height: 32px;
    margin: 0 var(--spacing-xs);
    font-size: var(--font-size-sm);
  }
}
</style>
