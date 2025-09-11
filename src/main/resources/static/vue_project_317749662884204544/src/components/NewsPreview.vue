<template>
  <section class="news-preview">
    <div class="container">
      <h2>最新资讯</h2>
      <div class="news-grid">
        <div class="news-card" v-for="item in news" :key="item.id">
          <img :src="item.image" :alt="item.title" />
          <div class="news-info">
            <h3>{{ item.title }}</h3>
            <p>{{ item.excerpt }}</p>
            <span class="date">{{ item.date }}</span>
          </div>
        </div>
      </div>
      <div class="view-all">
        <router-link to="/news" class="btn btn-primary">查看更多资讯</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
const news = [
  {
    id: 1,
    title: '公司荣获2024年度技术创新奖',
    excerpt: '我们很荣幸获得行业认可，将继续致力于技术创新...',
    date: '2024-01-15',
    image: 'https://picsum.photos/300/200?random=5'
  },
  {
    id: 2,
    title: '新产品发布会圆满成功',
    excerpt: '最新版本的企业管理系统正式发布，获得客户一致好评...',
    date: '2024-01-10',
    image: 'https://picsum.photos/300/200?random=6'
  },
  {
    id: 3,
    title: '合作伙伴签约仪式',
    excerpt: '与知名企业达成战略合作，共同推动行业发展...',
    date: '2024-01-05',
    image: 'https://picsum.photos/300/200?random=7'
  }
]
</script>

<style scoped>
.news-preview {
  padding: 4rem 0;
  background: #f8fafc;
}
.news-preview h2 {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 3rem;
  color: #1e293b;
}
.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}
.news-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}
.news-card:hover {
  transform: translateY(-4px);
}
.news-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.news-info {
  padding: 1.5rem;
}
.news-info h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #1e293b;
}
.news-info p {
  color: #64748b;
  margin-bottom: 1rem;
}
.date {
  color: #94a3b8;
  font-size: 0.875rem;
}
.view-all {
  text-align: center;
}
</style>