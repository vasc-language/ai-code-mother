<template>
  <section class="features">
    <div class="container">
      <h2>我们的优势</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🚀</div>
          <h3>技术创新</h3>
          <p>采用最新技术栈，确保产品性能卓越</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">💼</div>
          <h3>专业团队</h3>
          <p>资深工程师团队，丰富的项目经验</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🛡️</div>
          <h3>安全保障</h3>
          <p>多重安全机制，保障数据安全</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📈</div>
          <h3>持续优化</h3>
          <p>持续迭代更新，满足不断变化的需求</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.features {
  padding: 4rem 0;
  background: #f8fafc;
}
.features h2 {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 3rem;
  color: #1e293b;
}
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}
.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}
.feature-card:hover {
  transform: translateY(-4px);
}
.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}
.feature-card h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
}
.feature-card p {
  color: #64748b;
}
</style>