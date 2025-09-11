<template>
  <div class="products">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>产品服务</h1>
        <p>我们提供全方位的数字化解决方案，助力企业实现业务创新和效率提升</p>
      </div>

      <!-- 产品分类 -->
      <div class="product-categories">
        <button 
          v-for="category in categories" 
          :key="category.id"
          :class="['category-btn', { active: activeCategory === category.id }]"
          @click="activeCategory = category.id"
        >
          {{ category.name }}
        </button>
      </div>

      <!-- 产品列表 -->
      <div class="products-grid">
        <div 
          v-for="product in filteredProducts" 
          :key="product.id"
          class="product-card"
        >
          <div class="product-image">
            <div class="image-placeholder">
              <span>{{ product.category }}</span>
            </div>
          </div>
          <div class="product-content">
            <h3>{{ product.name }}</h3>
            <p class="product-description">{{ product.description }}</p>
            <div class="product-features">
              <span 
                v-for="feature in product.features" 
                :key="feature"
                class="feature-tag"
              >
                {{ feature }}
              </span>
            </div>
            <div class="product-footer">
              <span class="product-price">{{ product.price }}</span>
              <button class="btn btn-primary">了解详情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 服务流程 -->
      <section class="service-process">
        <h2 class="section-title">服务流程</h2>
        <div class="process-steps">
          <div class="process-step">
            <div class="step-number">1</div>
            <h3>需求分析</h3>
            <p>深入了解企业业务需求，制定个性化解决方案</p>
          </div>
          <div class="process-step">
            <div class="step-number">2</div>
            <h3>方案设计</h3>
            <p>设计系统架构和功能模块，确保技术可行性</p>
          </div>
          <div class="process-step">
            <div class="step-number">3</div>
            <h3>开发实施</h3>
            <p>采用敏捷开发模式，分阶段交付功能模块</p>
          </div>
          <div class="process-step">
            <div class="step-number">4</div>
            <h3>测试上线</h3>
            <p>严格测试确保质量，平稳上线并提供培训</p>
          </div>
          <div class="process-step">
            <div class="step-number">5</div>
            <h3>运维支持</h3>
            <p>提供持续的技术支持和系统优化服务</p>
          </div>
        </div>
      </section>

      <!-- 技术优势 -->
      <section class="tech-advantages">
        <h2 class="section-title">技术优势</h2>
        <div class="advantages-grid">
          <div class="advantage-item">
            <div class="advantage-icon">⚡</div>
            <h3>高性能架构</h3>
            <p>采用微服务架构，支持高并发访问，确保系统稳定运行</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">🔒</div>
            <h3>安全保障</h3>
            <p>多重安全防护机制，数据加密传输，符合行业安全标准</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">📱</div>
            <h3>多端适配</h3>
            <p>支持PC、移动端、平板等多种设备，提供一致的用户体验</p>
          </div>
          <div class="advantage-item">
            <div class="advantage-icon">🔄</div>
            <h3>弹性扩展</h3>
            <p>云端部署支持弹性扩容，满足业务快速增长需求</p>
          </div>
        </div>
      </section>

      <!-- CTA区域 -->
      <section class="cta-section">
        <div class="cta-content">
          <h2>需要定制化解决方案？</h2>
          <p>我们的专业团队将为您提供个性化的产品服务</p>
          <router-link to="/contact" class="btn btn-primary">获取咨询</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const categories = [
  { id: 'all', name: '全部产品' },
  { id: 'software', name: '软件开发' },
  { id: 'cloud', name: '云端服务' },
  { id: 'data', name: '数据分析' },
  { id: 'consulting', name: '咨询培训' }
]

const products = [
  {
    id: 1,
    name: '企业资源管理系统',
    category: 'software',
    description: '集成财务管理、人力资源、供应链管理等模块的一体化企业管理系统',
    features: ['财务管理', '人事管理', '供应链', '报表分析'],
    price: '定制报价'
  },
  {
    id: 2,
    name: '客户关系管理平台',
    category: 'software',
    description: '帮助企业建立完整的客户信息库，提升客户服务和销售效率',
    features: ['客户管理', '销售跟踪', '营销自动化', '数据分析'],
    price: '定制报价'
  },
  {
    id: 3,
    name: '云端部署服务',
    category: 'cloud',
    description: '提供安全可靠的云端部署方案，支持弹性扩展和高可用性',
    features: ['AWS/Azure', '负载均衡', '自动备份', '监控告警'],
    price: '按需计费'
  },
  {
    id: 4,
    name: '大数据分析平台',
    category: 'data',
    description: '基于先进的数据分析技术，帮助企业挖掘数据价值，支持智能决策',
    features: ['数据采集', '数据清洗', '机器学习', '可视化'],
    price: '定制报价'
  },
  {
    id: 5,
    name: '移动应用开发',
    category: 'software',
    description: '为企业定制开发iOS和Android移动应用，提升业务移动化能力',
    features: ['原生开发', '混合开发', 'UI/UX设计', '应用商店发布'],
    price: '定制报价'
  },
  {
    id: 6,
    name: '数字化转型咨询',
    category: 'consulting',
    description: '为企业提供数字化转型战略规划和技术咨询服务',
    features: ['战略规划', '技术选型', '流程优化', '培训指导'],
    price: '项目咨询'
  }
]

const activeCategory = ref('all')

const filteredProducts = computed(() => {
  if (activeCategory.value === 'all') {
    return products
  }
  return products.filter(product => product.category === activeCategory.value)
})
</script>

<style scoped>
.products {
  padding: 2rem 0 4rem;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.page-header p {
  font-size: 1.125rem;
  color: #64748b;
  max-width: 600px;
  margin: 0 auto;
}

.product-categories {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 3rem;
  flex-wrap: wrap;
}

.category-btn {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
  color: #64748b;
}

.category-btn:hover,
.category-btn.active {
  border-color: #2563eb;
  background: #2563eb;
  color: white;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 4rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-image {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 1.25rem;
}

.product-content {
  padding: 1.5rem;
}

.product-content h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.product-description {
  color: #64748b;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.product-features {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.feature-tag {
  padding: 0.25rem 0.75rem;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 0.875rem;
  color: #475569;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-weight: 600;
  color: #2563eb;
  font-size: 1.125rem;
}

.service-process {
  margin: 4rem 0;
  padding: 3rem 0;
  background: #f8fafc;
  border-radius: 12px;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 3rem;
  color: #1e293b;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.process-step {
  text-align: center;
}

.step-number {
  width: 60px;
  height: 60px;
  background: #2563eb;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0 auto 1rem;
}

.process-step h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #1e293b;
}

.process-step p {
  color: #64748b;
  line-height: 1.6;
}

.tech-advantages {
  margin: 4rem 0;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.advantage-item {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.advantage-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.advantage-item h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.advantage-item p {
  color: #64748b;
  line-height: 1.6;
}

.cta-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 3rem 2rem;
  border-radius: 12px;
  text-align: center;
  margin-top: 3rem;
}

.cta-content h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.cta-content p {
  font-size: 1.125rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .product-categories {
    gap: 0.5rem;
  }
  
  .category-btn {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }
  
  .process-steps {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .advantages-grid {
    grid-template-columns: 1fr;
  }
  
  .cta-content h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .product-card {
    margin: 0 0.5rem;
  }
  
  .product-content {
    padding: 1rem;
  }
  
  .product-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .service-process {
    margin: 2rem 0;
    padding: 2rem 1rem;
  }
}
</style>