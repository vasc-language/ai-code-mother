<template>
  <nav class="navigation">
    <div class="container">
      <div class="nav-brand">
        <router-link to="/" class="brand-link">作品集</router-link>
      </div>
      
      <div class="nav-menu">
        <router-link 
          v-for="item in navItems" 
          :key="item.path" 
          :to="item.path" 
          class="nav-link"
          :class="{ active: $route.path === item.path }"
        >
          {{ item.label }}
        </router-link>
      </div>
      
      <button class="mobile-menu-btn" @click="toggleMobileMenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
    
    <div class="mobile-menu" :class="{ active: isMobileMenuOpen }">
      <router-link 
        v-for="item in navItems" 
        :key="item.path" 
        :to="item.path" 
        class="mobile-nav-link"
        @click="closeMobileMenu"
        :class="{ active: $route.path === item.path }"
      >
        {{ item.label }}
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isMobileMenuOpen = ref(false)

const navItems = [
  { path: '/', label: '首页' },
  { path: '/portfolio', label: '作品集' },
  { path: '/about', label: '关于我' },
  { path: '/contact', label: '联系我' }
]

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}
</script>

<style scoped>
.navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  z-index: 1000;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.navigation .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 20px;
}

.brand-link {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-decoration: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  position: relative;
}

.nav-link:hover,
.nav-link.active {
  color: #667eea;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.mobile-menu-btn {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
}

.mobile-menu-btn span {
  width: 25px;
  height: 3px;
  background: #333;
  margin: 3px 0;
  transition: 0.3s;
}

.mobile-menu {
  display: none;
  background: white;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.mobile-menu.active {
  display: block;
}

.mobile-nav-link {
  display: block;
  padding: 1rem 0;
  color: #666;
  text-decoration: none;
  border-bottom: 1px solid #eee;
  font-weight: 500;
}

.mobile-nav-link:last-child {
  border-bottom: none;
}

.mobile-nav-link.active {
  color: #667eea;
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .navigation .container {
    padding: 0.8rem 15px;
  }
}
</style>