<template>
  <div class="contact">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>联系我们</h1>
        <p>我们期待与您合作，共同创造价值</p>
      </div>

      <div class="contact-content">
        <!-- 联系信息 -->
        <div class="contact-info">
          <h2>联系信息</h2>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-icon">📍</div>
              <div class="info-content">
                <h3>地址</h3>
                <p>北京市海淀区中关村科技园区<br>创新大厦A座15层</p>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">📞</div>
              <div class="info-content">
                <h3>电话</h3>
                <p>400-123-4567</p>
                <p>+86 10 8888 8888</p>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">📧</div>
              <div class="info-content">
                <h3>邮箱</h3>
                <p>info@techcompany.com</p>
                <p>sales@techcompany.com</p>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">🕒</div>
              <div class="info-content">
                <h3>工作时间</h3>
                <p>周一至周五: 9:00 - 18:00</p>
                <p>周六: 9:00 - 12:00</p>
              </div>
            </div>
          </div>

          <!-- 社交媒体 -->
          <div class="social-media">
            <h3>关注我们</h3>
            <div class="social-links">
              <a href="#" class="social-link">
                <span class="social-icon">📘</span>
                <span>微信公众号</span>
              </a>
              <a href="#" class="social-link">
                <span class="social-icon">🐦</span>
                <span>微博</span>
              </a>
              <a href="#" class="social-link">
                <span class="social-icon">💼</span>
                <span>LinkedIn</span>
              </a>
              <a href="#" class="social-link">
                <span class="social-icon">📷</span>
                <span>知乎</span>
              </a>
            </div>
          </div>
        </div>

        <!-- 联系表单 -->
        <div class="contact-form">
          <h2>发送消息</h2>
          <form @submit.prevent="submitForm">
            <div class="form-group">
              <label for="name">姓名 *</label>
              <input 
                type="text" 
                id="name" 
                v-model="formData.name"
                required
                placeholder="请输入您的姓名"
              >
            </div>
            <div class="form-group">
              <label for="email">邮箱 *</label>
              <input 
                type="email" 
                id="email" 
                v-model="formData.email"
                required
                placeholder="请输入您的邮箱地址"
              >
            </div>
            <div class="form-group">
              <label for="phone">电话</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="formData.phone"
                placeholder="请输入您的联系电话"
              >
            </div>
            <div class="form-group">
              <label for="company">公司名称</label>
              <input 
                type="text" 
                id="company" 
                v-model="formData.company"
                placeholder="请输入您的公司名称"
              >
            </div>
            <div class="form-group">
              <label for="subject">主题 *</label>
              <select 
                id="subject" 
                v-model="formData.subject"
                required
              >
                <option value="">请选择咨询主题</option>
                <option value="software">软件开发</option>
                <option value="cloud">云端服务</option>
                <option value="data">数据分析</option>
                <option value="consulting">咨询培训</option>
                <option value="other">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label for="message">消息内容 *</label>
              <textarea 
                id="message" 
                v-model="formData.message"
                required
                rows="5"
                placeholder="请详细描述您的需求或问题"
              ></textarea>
            </div>
            <button type="submit" class="btn btn-primary">发送消息</button>
          </form>
        </div>
      </div>

      <!-- 地图位置 -->
      <section class="map-section">
        <h2 class="section-title">我们的位置</h2>
        <div class="map-container">
          <div class="map-placeholder">
            <div class="placeholder-content">
              <div class="map-icon">🗺️</div>
              <h3>地图位置</h3>
              <p>北京市海淀区中关村科技园区创新大厦A座</p>
              <div class="map-actions">
                <button class="btn btn-secondary">查看详细路线</button>
                <button class="btn btn-secondary">导航前往</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 常见问题 -->
      <section class="faq-section">
        <h2 class="section-title">常见问题</h2>
        <div class="faq-grid">
          <div class="faq-item">
            <h3>如何开始合作？</h3>
            <p>您可以通过联系表单提交需求，我们的顾问将在24小时内与您联系，详细了解您的需求并提供初步方案。</p>
          </div>
          <div class="faq-item">
            <h3>项目开发周期是多久？</h3>
            <p>项目周期根据具体需求而定，一般中小型项目需要2-3个月，大型项目可能需要6个月以上。</p>
          </div>
          <div class="faq-item">
            <h3>是否提供售后服务？</h3>
            <p>是的，我们提供完善的售后服务，包括系统维护、技术支持和功能升级等服务。</p>
          </div>
          <div class="faq-item">
            <h3>支持定制开发吗？</h3>
            <p>我们专注于定制化开发，可以根据企业的特定需求提供量身定制的解决方案。</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const formData = ref({
  name: '',
  email: '',
  phone: '',
  company: '',
  subject: '',
  message: ''
})

const submitForm = () => {
  // 这里可以添加表单提交逻辑
  console.log('表单提交:', formData.value)
  alert('感谢您的留言！我们将尽快与您联系。')
  
  // 重置表单
  formData.value = {
    name: '',
    email: '',
    phone: '',
    company: '',
    subject: '',
    message: ''
  }
}
</script>

<style scoped>
.contact {
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
}

.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  margin-bottom: 4rem;
}

.contact-info h2,
.contact-form h2 {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #1e293b;
}

.info-grid {
  display: grid;
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 8px;
}

.info-icon {
  font-size: 1.5rem;
  min-width: 40px;
}

.info-content h3 {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
  color: #1e293b;
}

.info-content p {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 0.25rem;
}

.social-media h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.social-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.social-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  text-decoration: none;
  color: #64748b;
  transition: all 0.3s;
}

.social-link:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.social-icon {
  font-size: 1.25rem;
}

.contact-form {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #2563eb;
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.map-section {
  margin: 4rem 0;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 3rem;
  color: #1e293b;
}

.map-container {
  background: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  height: 400px;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.placeholder-content {
  text-align: center;
}

.map-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.placeholder-content h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.placeholder-content p {
  margin-bottom: 2rem;
  opacity: 0.9;
}

.map-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.faq-section {
  margin: 4rem 0;
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.faq-item {
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.faq-item h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.faq-item p {
  color: #64748b;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .contact-content {
    grid-template-columns: 1fr;
    gap: 3rem;
  }
  
  .social-links {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .info-item {
    flex-direction: column;
    text-align: center;
  }
  
  .social-links {
    grid-template-columns: 1fr;
  }
  
  .faq-grid {
    grid-template-columns: 1fr;
  }
  
  .map-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .contact-form {
    padding: 1.5rem;
  }
  
  .info-item,
  .faq-item {
    padding: 1.5rem;
  }
  
  .map-container {
    height: 300px;
  }
  
  .map-icon {
    font-size: 3rem;
  }
}
</style>