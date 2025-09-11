<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <!-- 公司信息 -->
        <div class="footer-section">
          <div class="footer-brand">
            <span class="brand-icon">🚀</span>
            <span class="brand-text">科技公司</span>
          </div>
          <p class="footer-description">
            专注于企业数字化转型，提供全方位的数字化解决方案
          </p>
          <div class="footer-contact">
            <p>📞 400-123-4567</p>
            <p>📧 info@techcompany.com</p>
            <p>📍 北京市海淀区中关村科技园区</p>
          </div>
        </div>

        <!-- 产品服务 -->
        <div class="footer-section">
          <h3>产品服务</h3>
          <ul class="footer-links">
            <li><a href="/products#software">软件开发</a></li>
            <li><a href="/products#cloud">云端服务</a></li>
            <li><a href="/products#data">数据分析</a></li>
            <li><a href="/products#consulting">咨询培训</a></li>
          </ul>
        </div>

        <!-- 快速链接 -->
        <div class="footer-section">
          <h3>快速链接</h3>
          <ul class="footer-links">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/products">产品服务</router-link></li>
            <li><router-link to="/news">新闻资讯</router-link></li>
            <li><router-link to="/about">关于我们</router-link></li>
            <li><router-link to="/contact">联系我们</router-link></li>
          </ul>
        </div>

        <!-- 社交媒体 -->
        <div class="footer-section">
          <h3>关注我们</h3>
          <div class="social-links">
            <a href="#" class="social-link" aria-label="微信公众号">
              <span class="social-icon">📘</span>
              <span>微信公众号</span>
            </a>
            <a href="#" class="social-link" aria-label="微博">
              <span class="social-icon">🐦</span>
              <span>微博</span>
            </a>
            <a href="#" class="social-link" aria-label="LinkedIn">
              <span class="social-icon">💼</span>
              <span>LinkedIn</span>
            </a>
            <a href="#" class="social-link" aria-label="知乎">
              <span class="social-icon">📷</span>
              <span>知乎</span>
            </a>
          </div>
          
          <!-- 订阅表单 -->
          <div class="newsletter">
            <h4>订阅资讯</h4>
            <div class="subscribe-form">
              <input 
                type="email" 
                placeholder="请输入邮箱"
                class="email-input"
              >
              <button class="subscribe-btn">订阅</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <p>&copy; 2024 科技公司. 保留所有权利.</p>
          <div class="footer-legal">
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
            <a href="#">法律声明</a>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
  padding: 3rem 0 1rem;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 3rem;
  margin-bottom: 2rem;
}

.footer-section h3 {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
  color: #e2e8f0;
}

.footer-section h4 {
  font-size: 1rem;
  margin-bottom: 1rem;
  color: #e2e8f0;
}

.footer-brand {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  font-size: 1.5rem;
  font-weight: bold;
}

.brand-icon {
  font-size: 2rem;
  margin-right: 0.5rem;
}

.brand-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-description {
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.footer-contact p {
  color: #cbd5e1;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-links {
  list-style: none;
}

.footer-links li {
  margin-bottom: 0.75rem;
}

.footer-links a {
  color: #cbd5e1;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #667eea;
}

.social-links {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.social-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #cbd5e1;
  text-decoration: none;
  transition: color 0.3s;
  padding: 0.5rem 0;
}

.social-link:hover {
  color: #667eea;
}

.social-icon {
  font-size: 1.25rem;
}

.newsletter {
  margin-top: 1.5rem;
}

.subscribe-form {
  display: flex;
  gap: 0.5rem;
}

.email-input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #334155;
  color: white;
  font-size: 0.875rem;
}

.email-input::placeholder {
  color: #94a3b8;
}

.email-input:focus {
  outline: none;
  border-color: #667eea;
}

.subscribe-btn {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: opacity 0.3s;
}

.subscribe-btn:hover {
  opacity: 0.9;
}

.footer-bottom {
  border-top: 1px solid #475569;
  padding-top: 1.5rem;
}

.footer-bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-bottom p {
  color: #94a3b8;
  font-size: 0.875rem;
}

.footer-legal {
  display: flex;
  gap: 1.5rem;
}

.footer-legal a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.3s;
}

.footer-legal a:hover {
  color: #667eea;
}

@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }
}

@media (max-width: 768px) {
  .footer {
    padding: 2rem 0 1rem;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .footer-brand {
    justify-content: center;
  }
  
  .social-links {
    justify-content: center;
  }
  
  .subscribe-form {
    justify-content: center;
  }
  
  .footer-bottom-content {
    flex-direction: column;
    text-align: center;
  }
  
  .footer-legal {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .footer {
    padding: 1.5rem 0 1rem;
  }
  
  .footer-section {
    padding: 0 0.5rem;
  }
  
  .footer-brand {
    font-size: 1.25rem;
  }
  
  .brand-icon {
    font-size: 1.5rem;
  }
  
  .subscribe-form {
    flex-direction: column;
  }
  
  .footer-legal {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>