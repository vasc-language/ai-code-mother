<template>
  <div class="portfolio">
    <div class="container">
      <!-- Header -->
      <div class="portfolio-header">
        <h1>作品集</h1>
        <p>这里展示了我近年来的精选作品，涵盖了网站开发、UI设计和移动应用等多个领域</p>
      </div>

      <!-- Filter -->
      <div class="filter">
        <button 
          v-for="category in categories" 
          :key="category" 
          :class="['filter-btn', { active: activeCategory === category }]"
          @click="setActiveCategory(category)"
        >
          {{ category }}
        </button>
      </div>

      <!-- Works Grid -->
      <div class="portfolio-grid">
        <div 
          v-for="(work, index) in filteredWorks" 
          :key="index" 
          class="portfolio-item"
        >
          <div class="portfolio-image">
            <img :src="work.image" :alt="work.title" />
            <div class="portfolio-overlay">
              <button class="view-btn" @click="viewWork(work)">查看详情</button>
            </div>
          </div>
          <div class="portfolio-info">
            <h3>{{ work.title }}</h3>
            <p>{{ work.description }}</p>
            <div class="portfolio-meta">
              <span class="category">{{ work.category }}</span>
              <span class="date">{{ work.date }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Load More -->
      <div class="load-more" v-if="hasMoreWorks">
        <button class="btn" @click="loadMore">加载更多</button>
      </div>
    </div>

    <!-- Modal -->
    <div class="modal" :class="{ active: selectedWork }" @click="closeModal">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeModal">×</button>
        <div class="modal-body" v-if="selectedWork">
          <img :src="selectedWork.image" :alt="selectedWork.title" />
          <div class="modal-info">
            <h2>{{ selectedWork.title }}</h2>
            <p class="modal-description">{{ selectedWork.description }}</p>
            <div class="modal-details">
              <div class="detail-item">
                <strong>类别:</strong> {{ selectedWork.category }}
              </div>
              <div class="detail-item">
                <strong>日期:</strong> {{ selectedWork.date }}
              </div>
              <div class="detail-item">
                <strong>技术栈:</strong> {{ selectedWork.tech }}
              </div>
            </div>
            <div class="modal-actions">
              <a :href="selectedWork.demoUrl" class="btn" target="_blank">查看演示</a>
              <a :href="selectedWork.githubUrl" class="btn btn-secondary" target="_blank">GitHub</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeCategory = ref('全部')
const selectedWork = ref(null)
const visibleCount = ref(6)

const categories = ['全部', '网站开发', 'UI设计', '移动应用', '品牌设计']

const works = [
  {
    title: '电商平台重构',
    description: '完整的电商网站重构项目，提升用户体验和性能',
    category: '网站开发',
    date: '2024-01-15',
    image: 'https://picsum.photos/600/400?random=5',
    tech: 'Vue3, Node.js, MongoDB',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '企业官网设计',
    description: '现代化企业官网设计，突出品牌形象',
    category: '网站开发',
    date: '2024-02-20',
    image: 'https://picsum.photos/600/400?random=6',
    tech: 'React, Tailwind CSS',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '移动银行应用',
    description: '金融类移动应用UI设计，注重安全性和易用性',
    category: '移动应用',
    date: '2024-03-10',
    image: 'https://picsum.photos/600/400?random=7',
    tech: 'Flutter, Firebase',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '品牌视觉系统',
    description: '为科技公司设计的完整品牌视觉识别系统',
    category: '品牌设计',
    date: '2024-04-05',
    image: 'https://picsum.photos/600/400?random=8',
    tech: 'Illustrator, Photoshop',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '教育平台UI',
    description: '在线教育平台用户界面设计，注重学习体验',
    category: 'UI设计',
    date: '2024-05-12',
    image: 'https://picsum.photos/600/400?random=9',
    tech: 'Figma, Sketch',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '社交媒体应用',
    description: '社交类移动应用开发，包含实时聊天功能',
    category: '移动应用',
    date: '2024-06-18',
    image: 'https://picsum.photos/600/400?random=10',
    tech: 'React Native, Socket.io',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '餐厅网站',
    description: '餐饮行业网站设计，突出美食展示和在线预订',
    category: '网站开发',
    date: '2024-07-22',
    image: 'https://picsum.photos/600/400?random=11',
    tech: 'Vue3, Express.js',
    demoUrl: '#',
    githubUrl: '#'
  },
  {
    title: '健康应用UI',
    description: '健康管理类应用界面设计，注重数据可视化',
    category: 'UI设计',
    date: '2024-08-30',
    image: 'https://picsum.photos/600/400?random=12',
    tech: 'Adobe XD, After Effects',
    demoUrl: '#',
    githubUrl: '#'
  }
]

const filteredWorks = computed(() => {
  let filtered = works
  if (activeCategory.value !== '全部') {
    filtered = works.filter(work => work.category === activeCategory.value)
  }
  return filtered.slice(0, visibleCount.value)
})

const hasMoreWorks = computed(() => {
  const total = activeCategory.value === '全部' ? works.length : 
    works.filter(work => work.category === activeCategory.value).length
  return visibleCount.value < total
})

const setActiveCategory = (category) => {
  activeCategory.value = category
  visibleCount.value = 6
}

const viewWork = (work) => {
  selectedWork.value = work
}

const closeModal = () => {
  selectedWork.value = null
}

const loadMore = () => {
  visibleCount.value += 6
}
</script>

<style scoped>
.portfolio {
  padding: 2rem 0;
}

.portfolio-header {
  text-align: center;
  margin-bottom: 3rem;
}

.portfolio-header h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #333;
}

.portfolio-header p {
  font-size: 1.1rem;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.filter {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 3rem;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 0.8rem 1.5rem;
  border: 2px solid #667eea;
  background: transparent;
  color: #667eea;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.filter-btn.active,
.filter-btn:hover {
  background: #667eea;
  color: white;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.portfolio-item {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.portfolio-item:hover {
  transform: translateY(-5px);
}

.portfolio-image {
  position: relative;
  overflow: hidden;
}

.portfolio-image img {
  width: 100%;
  height: 250px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.portfolio-item:hover .portfolio-image img {
  transform: scale(1.1);
}

.portfolio-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.portfolio-item:hover .portfolio-overlay {
  opacity: 1;
}

.view-btn {
  padding: 0.8rem 1.5rem;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: transform 0.3s ease;
}

.view-btn:hover {
  transform: scale(1.05);
}

.portfolio-info {
  padding: 1.5rem;
}

.portfolio-info h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.portfolio-info p {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.portfolio-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 500;
}

.date {
  color: #999;
  font-size: 0.9rem;
}

.load-more {
  text-align: center;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 2000;
}

.modal.active {
  opacity: 1;
  visibility: visible;
}

.modal-content {
  background: white;
  border-radius: 15px;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #ff4757;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.5rem;
  z-index: 10;
}

.modal-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.modal-body img {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 10px 0 0 10px;
}

.modal-info {
  padding: 2rem;
}

.modal-info h2 {
  margin-bottom: 1rem;
  color: #333;
}

.modal-description {
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.modal-details {
  margin-bottom: 2rem;
}

.detail-item {
  margin-bottom: 0.5rem;
  color: #666;
}

.detail-item strong {
  color: #333;
}

.modal-actions {
  display: flex;
  gap: 1rem;
}

.btn-secondary {
  background: #95a5a6;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

@media (max-width: 768px) {
  .portfolio-grid {
    grid-template-columns: 1fr;
  }
  
  .filter {
    gap: 0.5rem;
  }
  
  .filter-btn {
    padding: 0.6rem 1rem;
    font-size: 0.9rem;
  }
  
  .modal-body {
    grid-template-columns: 1fr;
  }
  
  .modal-body img {
    border-radius: 10px 10px 0 0;
    height: 200px;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>