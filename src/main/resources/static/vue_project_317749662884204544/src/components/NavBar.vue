<template>
  <nav class="navbar">
    <div class="container">
      <div class="nav-brand">
        <router-link to="/" class="logo">
          <span class="logo-text">科技公司</span>
        </router-link>
      </div>
      
      <div :class="['nav-menu', { active: isMenuOpen }]">
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          class="nav-link"
          @click="closeMenu"
        >
          {{ item.name }}
        </router-link>
      </div>

      <button class="nav-toggle" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

const navItems = [
  { path: '/', name: '首页' },
  { path: '/products', name: '产品服务' },
  { path: '/news', name: '新闻资讯' },
  { path: '/about', name: '关于我们' },
  { path: '/contact', name: '联系我们' }
]

const isMenuOpen = ref(false)

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}
</script>

<style scoped>
.navbar {
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
}

.logo {
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: bold;
  color: #2563eb;
}

.logo-text {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  padding: 0.5rem 0;
  position: relative;
  transition: color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #2563eb;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #2563eb;
}

.nav-toggle {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.nav-toggle span {
  width: 25px;
  height: 3px;
  background: #64748b;
  margin: 3px 0;
  transition: 0.3s;
}

@media (max-width: 768px) {
  .nav-toggle {
    display: flex;
  }

  .nav-menu {
    position: fixed;
    top: 70px;
    left: -100%;
    width: 100%;
    height: calc(100vh - 70px);
    background: white;
    flex-direction: column;
    gap: 0;
    transition: left 0.3s;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  }

  .nav-menu.active {
    left: 0;
  }

  .nav-link {
    width: 100%;
    padding: 1rem 2rem;
    border-bottom: 1px solid #e2e8f0;
  }

  .nav-link.router-link-active::after {
    display: none;
  }

  .nav-link.router-link-active {
    background: #f1f5f9;
  }
}
</style>