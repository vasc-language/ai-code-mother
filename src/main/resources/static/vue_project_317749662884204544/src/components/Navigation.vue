<template>
  <nav class="navigation" :class="{ 'scrolled': isScrolled }">
    <div class="container">
      <div class="nav-brand">
        <router-link to="/" class="brand-link">
          <span class="brand-icon">🚀</span>
          <span class="brand-text">科技公司</span>
        </router-link>
      </div>

      <div class="nav-menu" :class="{ 'active': isMenuOpen }">
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

      <div class="nav-actions">
        <router-link to="/contact" class="btn btn-primary">
          联系我们
        </router-link>
      </div>

      <button class="mobile-toggle" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const navItems = [
  { path: '/', name: '首页' },
  { path: '/products', name: '产品服务' },
  { path: '/news', name: '新闻资讯' },
  { path: '/about', name: '关于我们' },
  { path: '/contact', name: '联系我们' }
]

const isScrolled = ref(false)
const isMenuOpen = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.navigation.scrolled {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.navigation .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
}

.nav-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #1e293b;
  font-weight: bold;
  font-size: 1.25rem;
}

.brand-icon {
  font-size: 1.5rem;
  margin-right: 0.5rem;
}

.brand-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
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
  right: 0;
  height: 2px;
  background: #2563eb;
  border-radius: 1px;
}

.nav-actions {
  display: flex;
  align-items: center;
}

.mobile-toggle {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.mobile-toggle span {
  width: 25px;
  height: 3px;
  background: #1e293b;
  margin: 3px 0;
  transition: 0.3s;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .navigation .container {
    padding: 1rem;
  }
  
  .nav-menu {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    flex-direction: column;
    padding: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transform: translateY(-10px);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s;
  }
  
  .nav-menu.active {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }
  
  .nav-link {
    padding: 1rem 0;
    width: 100%;
    text-align: center;
  }
  
  .nav-link.router-link-active::after {
    left: 25%;
    right: 25%;
  }
  
  .mobile-toggle {
    display: flex;
  }
  
  .nav-actions {
    display: none;
  }
  
  .nav-menu.active + .nav-actions {
    display: flex;
    position: absolute;
    top: calc(100% + 200px);
    left: 0;
    right: 0;
    padding: 1rem;
    background: white;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .brand-text {
    font-size: 1.125rem;
  }
  
  .brand-icon {
    font-size: 1.25rem;
  }
  
  .navigation .container {
    padding: 0.75rem 1rem;
  }
}
</style>