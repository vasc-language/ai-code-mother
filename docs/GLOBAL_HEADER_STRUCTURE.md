# 主页页眉（GlobalHeader）层次结构

## 📐 完整层次结构

```
<a-layout-header class="header">
│
└─ <div class="header-content">
    │
    ├─ <div class="header-left">                    ← 左侧区域
    │   │
    │   └─ <RouterLink to="/">
    │       │
    │       └─ <div class="logo-section">
    │           │
    │           ├─ <img class="logo" src="/favicon.ico" alt="Logo" />
    │           │
    │           └─ <h1 class="site-title">AICodeHub</h1>
    │
    │
    ├─ <div class="header-center">                  ← 中间区域
    │   │
    │   └─ <div class="custom-menu">
    │       │
    │       └─ <div class="menu-item" v-for="item in menuItems">  ← 循环渲染
    │           │
    │           ├─ <component :is="item.icon" class="menu-icon" />
    │           │
    │           └─ <span class="menu-label">{{ getMenuLabel(item) }}</span>
    │
    │
    └─ <div class="header-right">                   ← 右侧区域
        │
        └─ <div class="user-login-status">
            │
            ├─ [已登录] <div v-if="loginUserStore.loginUser.id">
            │   │
            │   └─ <a-dropdown>
            │       │
            │       ├─ <a-space>                     ← 触发器
            │       │   │
            │       │   ├─ <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            │       │   │
            │       │   └─ {{ loginUserStore.loginUser.userName ?? '无名' }}
            │       │
            │       └─ <template #overlay>          ← 下拉菜单
            │           │
            │           └─ <a-menu>
            │               │
            │               ├─ <a-menu-item @click="goToProfile">
            │               │   ├─ <UserOutlined />
            │               │   └─ 个人主页
            │               │
            │               ├─ <a-menu-divider />
            │               │
            │               ├─ <a-menu-item @click="goToPointsDetail">
            │               │   ├─ <WalletOutlined />
            │               │   └─ 积分明细
            │               │
            │               ├─ <a-menu-item @click="goToSignIn">
            │               │   ├─ <CalendarOutlined />
            │               │   └─ 每日签到
            │               │
            │               ├─ <a-menu-item @click="goToInvite">
            │               │   ├─ <TeamOutlined />
            │               │   └─ 邀请好友
            │               │
            │               ├─ <a-menu-divider />
            │               │
            │               └─ <a-menu-item @click="doLogout">
            │                   ├─ <LogoutOutlined />
            │                   └─ 退出登录
            │
            └─ [未登录] <div v-else>
                │
                └─ <a-button type="primary" href="/user/login">登录</a-button>
```

---

## 🎯 三大区域详解

### 1. 左侧区域 (`header-left`)

**功能**: Logo + 网站标题

```html
<div class="header-left">
  <RouterLink to="/">
    <div class="logo-section">
      <img class="logo" src="/favicon.ico" alt="Logo" />
      <h1 class="site-title">AICodeHub</h1>
    </div>
  </RouterLink>
</div>
```

**特点**:
- ✅ 点击整个区域可跳转到主页
- ✅ Logo 图标：`favicon.ico`
- ✅ 标题：渐变色文字 "AICodeHub"

---

### 2. 中间区域 (`header-center`)

**功能**: 导航菜单

```html
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
```

**菜单项**:
1. 🏠 **主页** (`/`)
2. 📖 **使用文档** (`/docs`)
3. 👤 **用户管理** (`/admin/userManage`) - 仅管理员可见
4. 📦 **应用管理** (`/admin/appManage`) - 仅管理员可见
5. 🔗 **项目仓库** (外部链接到 GitHub)

**特点**:
- ✅ 当前页面高亮显示（`menu-item-selected`）
- ✅ 动态过滤（管理员才能看到管理菜单）
- ✅ 圆角胶囊设计
- ✅ hover 效果：上移 + 阴影

---

### 3. 右侧区域 (`header-right`)

**功能**: 用户信息 / 登录按钮

#### 3.1 已登录状态

```html
<div v-if="loginUserStore.loginUser.id">
  <a-dropdown>
    <!-- 触发器 -->
    <a-space>
      <a-avatar :src="loginUserStore.loginUser.userAvatar" />
      {{ loginUserStore.loginUser.userName ?? '无名' }}
    </a-space>
    
    <!-- 下拉菜单 -->
    <template #overlay>
      <a-menu>
        <a-menu-item @click="goToProfile">个人主页</a-menu-item>
        <a-menu-divider />
        <a-menu-item @click="goToPointsDetail">积分明细</a-menu-item>
        <a-menu-item @click="goToSignIn">每日签到</a-menu-item>
        <a-menu-item @click="goToInvite">邀请好友</a-menu-item>
        <a-menu-divider />
        <a-menu-item @click="doLogout">退出登录</a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</div>
```

**下拉菜单项**:
1. 👤 个人主页
2. 💰 积分明细
3. 📅 每日签到
4. 👥 邀请好友
5. 🚪 退出登录

#### 3.2 未登录状态

```html
<div v-else>
  <a-button type="primary" href="/user/login">登录</a-button>
</div>
```

---

## 📊 CSS 类名层级

### 外层容器
```css
.header                    /* Ant Design Layout Header */
  └─ .header-content       /* 弹性布局容器 */
```

### 左侧区域
```css
.header-left
  └─ .logo-section
      ├─ .logo             /* 图标 */
      └─ .site-title       /* 标题 */
```

### 中间区域
```css
.header-center
  └─ .custom-menu
      └─ .menu-item
          ├─ .menu-item-selected  /* 选中状态 */
          ├─ .menu-icon           /* 图标 */
          └─ .menu-label          /* 文本 */
```

### 右侧区域
```css
.header-right
  └─ .user-login-status
      ├─ <a-dropdown>            /* 已登录 */
      │   ├─ <a-space>
      │   │   ├─ <a-avatar>
      │   │   └─ userName
      │   └─ <a-menu>
      │       └─ <a-menu-item>
      │
      └─ <a-button>              /* 未登录 */
```

---

## 🎨 布局方式

### Flexbox 布局

```css
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  /* 三栏布局 */
  .header-left   { flex-shrink: 0; }       /* 左侧：固定宽度 */
  .header-center { flex: 1; }              /* 中间：自适应 */
  .header-right  { flex-shrink: 0; }       /* 右侧：固定宽度 */
}
```

**布局示意**:
```
┌───────────────────────────────────────────────────────────────┐
│ [Logo AICodeHub]  [主页] [文档] [用户] [应用] [GitHub]  [👤 用户] │
│                                                                 │
│     header-left          header-center           header-right  │
│     (固定)                (自适应)                  (固定)       │
└───────────────────────────────────────────────────────────────┘
```

---

## 🔄 响应式断点

### 桌面端 (> 768px)
```
[Logo + 标题]  [完整菜单]  [用户信息]
```

### 移动端 (≤ 768px)
```css
.header {
  margin: 0 var(--spacing-sm);
  padding: 0 var(--spacing-lg);
}

.site-title {
  font-size: var(--font-size-base);  /* 缩小标题 */
}

.logo {
  height: 32px;  /* 缩小图标 */
}

.menu-item {
  padding: 0 8px;       /* 缩小菜单项 */
  font-size: 12px;      /* 缩小文字 */
}
```

---

## 📦 组件依赖

### Ant Design Vue 组件
- `<a-layout-header>` - 布局头部
- `<a-dropdown>` - 下拉菜单
- `<a-space>` - 间距容器
- `<a-avatar>` - 头像
- `<a-menu>` - 菜单容器
- `<a-menu-item>` - 菜单项
- `<a-menu-divider>` - 分割线
- `<a-button>` - 按钮

### Vue Router
- `<RouterLink>` - 路由链接

### 图标
- `HomeOutlined` - 主页图标
- `BookOutlined` - 文档图标
- `UserOutlined` - 用户图标
- `AppstoreOutlined` - 应用图标
- `GithubOutlined` - GitHub图标
- `WalletOutlined` - 积分图标
- `CalendarOutlined` - 签到图标
- `TeamOutlined` - 邀请图标
- `LogoutOutlined` - 退出图标

---

## 🎯 交互逻辑

### 菜单点击处理
```typescript
const handleMenuItemClick = (item: any) => {
  const key = item.key as string
  selectedKeys.value = [key]

  if (key === 'others') {
    // 外部链接
    window.open('https://github.com/vasc-language/ai-code-mother', '_blank')
  } else if (key.startsWith('/')) {
    // 内部路由
    router.push(key)
  }
}
```

### 权限过滤
```typescript
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      // 只有管理员才能看到管理菜单
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}
```

### 用户操作
```typescript
// 跳转到个人主页
const goToProfile = () => router.push('/user/profile')

// 跳转到积分明细
const goToPointsDetail = () => router.push('/points/detail')

// 跳转到签到页面
const goToSignIn = () => router.push('/points/sign-in')

// 跳转到邀请页面
const goToInvite = () => router.push('/points/invite')

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/user/login')
  }
}
```

---

## 🎨 视觉特效

### 玻璃态效果
```css
.header {
  background: var(--glass-gradient);
  backdrop-filter: var(--glass-backdrop);
  border: var(--glass-border);
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
  box-shadow: var(--shadow-lg);
}
```

### Logo hover 动画
```css
.logo:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
  transition: var(--transition-fast);
}
```

### 菜单项 hover 动画
```css
.menu-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 56, 255, 0.15);
}
```

### 选中菜单项样式
```css
.menu-item-selected {
  background: linear-gradient(135deg, rgba(0, 56, 255, 0.15) 0%, rgba(0, 209, 255, 0.15) 100%);
  color: var(--primary-color);
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
```

---

## 📏 尺寸规格

| 元素 | 桌面端 | 移动端 |
|------|--------|--------|
| **页眉高度** | 64px | 56px |
| **Logo 尺寸** | 40x40px | 32x32px |
| **标题字号** | 24px (xl) | 16px (base) |
| **菜单项高度** | 44px | 32px |
| **菜单项内边距** | 0 18px | 0 8px |
| **菜单项字号** | 15px | 12px |
| **图标大小** | 18px | 14px |

---

## 🔍 Z-Index 层级

```css
.header {
  position: sticky;
  top: 0;
  z-index: 1000;         /* 页眉固定在顶部 */
}

.menu-item::before {
  z-index: 0;            /* 背景层 */
}

.menu-icon,
.menu-label {
  z-index: 1;            /* 内容层 */
}
```

---

## 📝 文件信息

**文件路径**: `src/components/GlobalHeader.vue`  
**组件名称**: GlobalHeader  
**总行数**: 473 行  
**类型**: Vue 3 SFC (Single File Component)

---

## 🎯 关键特性

- ✅ **三栏布局**: 左侧品牌 + 中间导航 + 右侧用户
- ✅ **响应式设计**: 桌面和移动端适配
- ✅ **权限控制**: 管理员菜单动态显示
- ✅ **玻璃态效果**: 现代化视觉设计
- ✅ **Sticky 定位**: 滚动时固定在顶部
- ✅ **路由高亮**: 当前页面菜单自动高亮
- ✅ **下拉菜单**: 用户操作集中管理

---

**创建时间**: 2025-01-XX  
**用途**: 主页页眉组件结构说明
