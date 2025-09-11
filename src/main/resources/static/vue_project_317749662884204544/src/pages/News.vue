<template>
  <div class="news">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>新闻资讯</h1>
        <p>了解最新的行业动态、技术趋势和公司新闻</p>
      </div>

      <!-- 新闻分类 -->
      <div class="news-categories">
        <button 
          v-for="category in categories" 
          :key="category.id"
          :class="['category-btn', { active: activeCategory === category.id }]"
          @click="activeCategory = category.id"
        >
          {{ category.name }}
        </button>
      </div>

      <!-- 新闻列表 -->
      <div class="news-grid">
        <div 
          v-for="item in filteredNews" 
          :key="item.id"
          class="news-card"
        >
          <div class="news-image">
            <div class="image-placeholder">
              <span>{{ item.category }}</span>
            </div>
          </div>
          <div class="news-content">
            <div class="news-meta">
              <span class="news-date">{{ item.date }}</span>
              <span class="news-category">{{ item.category }}</span>
            </div>
            <h3 class="news-title">{{ item.title }}</h3>
            <p class="news-excerpt">{{ item.excerpt }}</p>
            <div class="news-footer">
              <span class="read-time">{{ item.readTime }} 阅读</span>
              <button class="read-more-btn">阅读全文</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more">
        <button class="btn btn-secondary" @click="loadMore">
          加载更多
        </button>
      </div>

      <!-- 热门文章 -->
      <section class="popular-articles">
        <h2 class="section-title">热门文章</h2>
        <div class="popular-grid">
          <div 
            v-for="article in popularArticles" 
            :key="article.id"
            class="popular-card"
          >
            <div class="popular-rank">
              {{ article.rank }}
            </div>
            <div class="popular-content">
              <h4>{{ article.title }}</h4>
              <div class="popular-meta">
                <span>{{ article.date }}</span>
                <span>·</span>
                <span>{{ article.views }} 阅读</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 订阅区域 -->
      <section class="subscribe-section">
        <div class="subscribe-content">
          <h2>订阅我们的新闻资讯</h2>
          <p>第一时间获取最新的技术动态和行业洞察</p>
          <div class="subscribe-form">
            <input 
              type="email" 
              placeholder="请输入您的邮箱地址"
              class="email-input"
            >
            <button class="btn btn-primary">立即订阅</button>
          </div>
          <p class="subscribe-note">我们尊重您的隐私，绝不会分享您的邮箱信息</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const categories = [
  { id: 'all', name: '全部' },
  { id: 'company', name: '公司动态' },
  { id: 'industry', name: '行业资讯' },
  { id: 'technology', name: '技术文章' },
  { id: 'event', name: '活动预告' }
]

const newsItems = [
  {
    id: 1,
    title: '科技公司成功签约某大型制造企业数字化转型项目',
    category: 'company',
    date: '2024-01-15',
    excerpt: '近日，科技公司与某知名制造企业达成战略合作，为其提供全面的数字化转型解决方案，助力企业实现智能化升级。',
    readTime: '5分钟',
    image: 'https://picsum.photos/400/200?random=1'
  },
  {
    id: 2,
    title: '2024年企业数字化转型的五大趋势预测',
    category: 'industry',
    date: '2024-01-12',
    excerpt: '随着技术的不断发展，企业数字化转型将呈现智能化、云端化、数据驱动等新趋势，为企业带来新的发展机遇。',
    readTime: '8分钟',
    image: 'https://picsum.photos/400/200?random=2'
  },
  {
    id: 3,
    title: '微服务架构在企业级应用中的最佳实践',
    category: 'technology',
    date: '2024-01-10',
    excerpt: '本文详细介绍了微服务架构的设计原则、实施步骤和常见问题解决方案，帮助企业更好地构建分布式系统。',
    readTime: '12分钟',
    image: 'https://picsum.photos/400/200?random=3'
  },
  {
    id: 4,
    title: '科技公司荣获"年度最佳数字化转型服务商"奖项',
    category: 'company',
    date: '2024-01-08',
    excerpt: '在近日举办的数字经济发展峰会上，科技公司凭借卓越的服务质量和创新技术，荣获年度最佳数字化转型服务商称号。',
    readTime: '3分钟',
    image: 'https://picsum.photos/400/200?random=4'
  },
  {
    id: 5,
    title: '人工智能在数据分析中的应用与挑战',
    category: 'technology',
    date: '2024-01-05',
    excerpt: 'AI技术正在重塑数据分析领域，本文将探讨机器学习、深度学习在数据分析中的具体应用场景和面临的挑战。',
    readTime: '10分钟',
    image: 'https://picsum.photos/400/200?random=5'
  },
  {
    id: 6,
    title: '云端安全：企业上云必须注意的关键问题',
    category: 'industry',
    date: '2024-01-03',
    excerpt: '随着企业加速上云进程，云端安全问题日益突出。本文梳理了企业上云过程中需要重点关注的安全风险和防范措施。',
    readTime: '7分钟',
    image: 'https://picsum.photos/400/200?random=6'
  }
]

const popularArticles = [
  {
    id: 1,
    rank: 1,
    title: '数字化转型的成功案例分享',
    date: '2023-12-20',
    views: '2.5k'
  },
  {
    id: 2,
    rank: 2,
    title: '2024年软件开发技术趋势',
    date: '2023-12-15',
    views: '1.8k'
  },
  {
    id: 3,
    rank: 3,
    title: '企业如何选择合适的云服务商',
    date: '2023-12-10',
    views: '1.5k'
  },
  {
    id: 4,
    rank: 4,
    title: '微服务架构实战指南',
    date: '2023-12-05',
    views: '1.2k'
  },
  {
    id: 5,
    rank: 5,
    title: '数据分析驱动的业务增长',
    date: '2023-11-28',
    views: '980'
  }
]

const activeCategory = ref('all')
const displayedItems = ref(6)

const filteredNews = computed(() => {
  let filtered = newsItems
  
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(item => item.category === activeCategory.value)
  }
  
  return filtered.slice(0, displayedItems.value)
})

const loadMore = () => {
  displayedItems.value += 3
}
</script>

<style scoped>
.news {
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

.news-categories {
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

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.news-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.news-card:hover {
  transform: translateY(-4px);
}

.news-image {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 1.25rem;
}

.news-content {
  padding: 1.5rem;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: #64748b;
}

.news-category {
  padding: 0.25rem 0.75rem;
  background: #f1f5f9;
  border-radius: 12px;
  color: #475569;
}

.news-title {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
  line-height: 1.4;
}

.news-excerpt {
  color: #64748b;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.read-time {
  color: #94a3b8;
  font-size: 0.875rem;
}

.read-more-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.3s;
}

.read-more-btn:hover {
  color: #1d4ed8;
}

.load-more {
  text-align: center;
  margin-bottom: 4rem;
}

.popular-articles {
  margin: 4rem 0;
  padding: 3rem 2rem;
  background: #f8fafc;
  border-radius: 12px;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 3rem;
  color: #1e293b;
}

.popular-grid {
  display: grid;
  gap: 1rem;
  max-width: 600px;
  margin: 0 auto;
}

.popular-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  transition: transform 0.3s;
}

.popular-card:hover {
  transform: translateX(4px);
}

.popular-rank {
  width: 32px;
  height: 32px;
  background: #2563eb;
  color: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.875rem;
}

.popular-content h4 {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  color: #1e293b;
}

.popular-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #64748b;
}

.subscribe-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 3rem 2rem;
  border-radius: 12px;
  text-align: center;
}

.subscribe-content h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.subscribe-content p {
  font-size: 1.125rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.subscribe-form {
  display: flex;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto 1rem;
}

.email-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
}

.subscribe-note {
  font-size: 0.875rem;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .news-grid {
    grid-template-columns: 1fr;
  }
  
  .news-categories {
    gap: 0.5rem;
  }
  
  .category-btn {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }
  
  .subscribe-form {
    flex-direction: column;
  }
  
  .subscribe-content h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .news-card {
    margin: 0 0.5rem;
  }
  
  .news-content {
    padding: 1rem;
  }
  
  .news-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .news-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .popular-articles {
    margin: 2rem 0;
    padding: 2rem 1rem;
  }
}
</style>