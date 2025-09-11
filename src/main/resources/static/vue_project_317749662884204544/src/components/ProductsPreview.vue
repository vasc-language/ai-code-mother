<template>
  <section class="products-preview">
    <div class="container">
      <h2>热门产品</h2>
      <div class="products-grid">
        <div class="product-card" v-for="product in products" :key="product.id">
          <img :src="product.image" :alt="product.name" />
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p>{{ product.description }}</p>
            <span class="price">{{ product.price }}</span>
          </div>
        </div>
      </div>
      <div class="view-all">
        <router-link to="/products" class="btn btn-primary">查看所有产品</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
const products = [
  {
    id: 1,
    name: '企业管理系统',
    description: '一体化企业管理解决方案',
    price: '¥29,800',
    image: 'https://picsum.photos/300/200?random=2'
  },
  {
    id: 2,
    name: '移动应用开发',
    description: '跨平台移动应用定制开发',
    price: '¥45,000',
    image: 'https://picsum.photos/300/200?random=3'
  },
  {
    id: 3,
    name: '云端部署服务',
    description: '安全可靠的云端部署方案',
    price: '¥12,500/年',
    image: 'https://picsum.photos/300/200?random=4'
  }
]
</script>

<style scoped>
.products-preview {
  padding: 4rem 0;
}
.products-preview h2 {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 3rem;
  color: #1e293b;
}
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}
.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}
.product-card:hover {
  transform: translateY(-4px);
}
.product-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.product-info {
  padding: 1.5rem;
}
.product-info h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #1e293b;
}
.product-info p {
  color: #64748b;
  margin-bottom: 1rem;
}
.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2563eb;
}
.view-all {
  text-align: center;
}
</style>