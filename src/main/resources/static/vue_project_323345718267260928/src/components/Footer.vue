<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <h3>作品集</h3>
          <p>展示我的创意作品和专业项目</p>
        </div>
        
        <div class="footer-section">
          <h4>快速链接</h4>
          <router-link to="/" class="footer-link">首页</router-link>
          <router-link to="/portfolio" class="footer-link">作品集</router-link>
          <router-link to="/about" class="footer-link">关于我</router-link>
          <router-link to="/contact" class="footer-link">联系我</router-link>
        </div>
        
        <div class="footer-section">
          <h4>联系方式</h4>
          <p>邮箱: contact@portfolio.com</p>
          <p>电话: +86 138 0000 0000</p>
        </div>
        
        <div class="footer-section">
          <h4>关注我</h4>
          <div class="social-links">
            <a href="#" class="social-link">微博</a>
            <a href="#" class="social-link">知乎</a>
            <a href="#" class="social-link">GitHub</a>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>&copy; 2024 作品展示网站. 保留所有权利.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  padding: 3rem 0 1rem;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.footer-section h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-section h4 {
  margin-bottom: 1rem;
  color: #ecf0f1;
}

.footer-link {
  display: block;
  color: #bdc3c7;
  text-decoration: none;
  margin-bottom: 0.5rem;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: #667eea;
}

.social-links {
  display: flex;
  gap: 1rem;
}

.social-link {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s ease;
}

.social-link:hover {
  color: #667eea;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding-top: 1rem;
  text-align: center;
  color: #95a5a6;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .social-links {
    justify-content: center;
  }
  
  .footer {
    padding: 2rem 0 1rem;
  }
}
</style>