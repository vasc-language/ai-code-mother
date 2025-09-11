<template>
  <div id="app">
    <Navigation />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Navigation from '@/components/Navigation.vue'
import Footer from '@/components/Footer.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  line-height: 1.6;
  color: #1e293b;
  background: #ffffff;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 80px; /* 导航栏高度 */
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .container {
    padding: 0 1.5rem;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }
  
  .btn {
    padding: 0.625rem 1.25rem;
    font-size: 0.875rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 0.75rem;
  }
  
  .main-content {
    padding-top: 70px;
  }
}

/* 工具类 */
.text-center {
  text-align: center;
}

.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 0.75rem; }
.mt-4 { margin-top: 1rem; }
.mt-6 { margin-top: 1.5rem; }
.mt-8 { margin-top: 2rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 1.5rem; }
.mb-8 { margin-bottom: 2rem; }

.p-4 { padding: 1rem; }
.p-6 { padding: 1.5rem; }
.p-8 { padding: 2rem; }
</style>