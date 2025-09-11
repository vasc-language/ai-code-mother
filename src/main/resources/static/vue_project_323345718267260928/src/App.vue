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

body {
  font-family: 'Arial', sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f8f9fa;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 80px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section {
  padding: 60px 0;
}

.btn {
  display: inline-block;
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.2);
}

@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }
  
  .section {
    padding: 40px 0;
  }
  
  .main-content {
    padding-top: 70px;
  }
}
</style>