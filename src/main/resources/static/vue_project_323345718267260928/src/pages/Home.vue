<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">创意设计与专业开发</h1>
          <p class="hero-subtitle">
            专注于打造卓越的数字体验，将创意转化为现实
          </p>
          <div class="hero-buttons">
            <router-link to="/portfolio" class="btn btn-primary">查看作品</router-link>
            <router-link to="/contact" class="btn btn-secondary">联系我</router-link>
          </div>
        </div>
        <div class="hero-image">
          <img src="https://picsum.photos/600/400?random=1" alt="创意设计" />
        </div>
      </div>
    </section>

    <!-- Featured Works -->
    <section class="featured-works section">
      <div class="container">
        <h2 class="section-title">精选作品</h2>
        <div class="works-grid">
          <div 
            v-for="(work, index) in featuredWorks" 
            :key="index" 
            class="work-card"
          >
            <img :src="work.image" :alt="work.title" />
            <div class="work-info">
              <h3>{{ work.title }}</h3>
              <p>{{ work.description }}</p>
              <span class="work-category">{{ work.category }}</span>
            </div>
          </div>
        </div>
        <div class="text-center">
          <router-link to="/portfolio" class="btn">查看更多作品</router-link>
        </div>
      </div>
    </section>

    <!-- Services -->
    <section class="services section">
      <div class="container">
        <h2 class="section-title">服务范围</h2>
        <div class="services-grid">
          <div class="service-item">
            <div class="service-icon">💻</div>
            <h3>网站开发</h3>
            <p>响应式网站设计，现代化前端开发，用户体验优化</p>
          </div>
          <div class="service-item">
            <div class="service-icon">🎨</div>
            <h3>UI/UX设计</h3>
            <p>用户界面设计，交互体验优化，品牌视觉设计</p>
          </div>
          <div class="service-item">
            <div class="service-icon">📱</div>
            <h3>移动应用</h3>
            <p>跨平台应用开发，原生应用设计，移动端优化</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="stats section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">50+</div>
            <div class="stat-label">完成项目</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">3+</div>
            <div class="stat-label">年经验</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">100%</div>
            <div class="stat-label">客户满意度</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">24/7</div>
            <div class="stat-label">技术支持</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
const featuredWorks = [
  {
    title: '电商平台设计',
    description: '现代化的电商网站设计，注重用户体验和转化率',
    category: '网站开发',
    image: 'https://picsum.photos/400/300?random=2'
  },
  {
    title: '移动应用界面',
    description: '流畅的移动端应用设计，完美的交互体验',
    category: 'UI设计',
    image: 'https://picsum.photos/400/300?random=3'
  },
  {
    title: '品牌视觉设计',
    description: '完整的品牌形象设计，包括logo和视觉系统',
    category: '品牌设计',
    image: 'https://picsum.photos/400/300?random=4'
  }
]
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 100px 0;
  min-height: 80vh;
  display: flex;
  align-items: center;
}

.hero .container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: 1rem;
}

.btn-primary {
  background: white;
  color: #667eea;
}

.btn-secondary {
  background: transparent;
  border: 2px solid white;
  color: white;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
}

.hero-image img {
  width: 100%;
  height: auto;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 3rem;
  color: #333;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.work-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.work-card:hover {
  transform: translateY(-5px);
}

.work-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.work-info {
  padding: 1.5rem;
}

.work-info h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.work-info p {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.work-category {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 500;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.service-item {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.service-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.service-item h3 {
  margin-bottom: 1rem;
  color: #333;
}

.service-item p {
  color: #666;
  line-height: 1.6;
}

.stats {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 1.1rem;
  opacity: 0.9;
}

.text-center {
  text-align: center;
}

@media (max-width: 768px) {
  .hero .container {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .works-grid {
    grid-template-columns: 1fr;
  }
}
</style>