<template>
  <section class="hero">
    <div class="container">
      <div class="hero-content">
        <h1>创新科技，引领未来</h1>
        <p>我们致力于为客户提供最优质的产品和服务，助力企业数字化转型</p>
        <div class="hero-buttons">
          <button class="btn btn-primary">了解详情</button>
          <button class="btn" style="background: #f1f5f9; color: #333;">联系我们</button>
        </div>
      </div>
      <div class="hero-image">
        <img :src="`https://picsum.photos/600/400?random=1`" alt="企业形象" />
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  padding: 4rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
.hero .container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}
.hero-content h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
}
.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}
.hero-buttons {
  display: flex;
  gap: 1rem;
}
.hero-image img {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}
</style>