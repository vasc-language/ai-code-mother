<template>
  <div class="contact">
    <div class="container">
      <!-- Header -->
      <div class="contact-header">
        <h1>联系我</h1>
        <p>如果您有任何问题或想要讨论项目合作，请随时与我联系</p>
      </div>

      <div class="contact-content">
        <!-- Contact Info -->
        <div class="contact-info">
          <h2>联系方式</h2>
          <div class="info-item">
            <div class="info-icon">📧</div>
            <div class="info-content">
              <h3>邮箱</h3>
              <p>contact@portfolio.com</p>
              <span>工作日24小时内回复</span>
            </div>
          </div>
          
          <div class="info-item">
            <div class="info-icon">📱</div>
            <div class="info-content">
              <h3>电话</h3>
              <p>+86 138 0000 0000</p>
              <span>周一至周五 9:00-18:00</span>
            </div>
          </div>
          
          <div class="info-item">
            <div class="info-icon">📍</div>
            <div class="info-content">
              <h3>地址</h3>
              <p>北京市朝阳区某科技园区</p>
              <span>欢迎预约面谈</span>
            </div>
          </div>
          
          <div class="social-links">
            <h3>关注我</h3>
            <div class="social-grid">
              <a href="#" class="social-item">
                <span class="social-icon">📘</span>
                <span>微博</span>
              </a>
              <a href="#" class="social-item">
                <span class="social-icon">💼</span>
                <span>知乎</span>
              </a>
              <a href="#" class="social-item">
                <span class="social-icon">🐱</span>
                <span>GitHub</span>
              </a>
              <a href="#" class="social-item">
                <span class="social-icon">👔</span>
                <span>LinkedIn</span>
              </a>
            </div>
          </div>
        </div>

        <!-- Contact Form -->
        <div class="contact-form">
          <h2>发送消息</h2>
          <form @submit.prevent="submitForm">
            <div class="form-group">
              <label for="name">姓名 *</label>
              <input 
                type="text" 
                id="name" 
                v-model="form.name" 
                required 
                placeholder="请输入您的姓名"
              >
            </div>
            
            <div class="form-group">
              <label for="email">邮箱 *</label>
              <input 
                type="email" 
                id="email" 
                v-model="form.email" 
                required 
                placeholder="请输入您的邮箱"
              >
            </div>
            
            <div class="form-group">
              <label for="subject">主题 *</label>
              <input 
                type="text" 
                id="subject" 
                v-model="form.subject" 
                required 
                placeholder="请输入消息主题"
              >
            </div>
            
            <div class="form-group">
              <label for="message">消息内容 *</label>
              <textarea 
                id="message" 
                v-model="form.message" 
                required 
                rows="5" 
                placeholder="请详细描述您的需求或问题"
              ></textarea>
            </div>
            
            <button type="submit" class="btn" :disabled="isSubmitting">
              {{ isSubmitting ? '发送中...' : '发送消息' }}
            </button>
            
            <div v-if="submitStatus" class="submit-status" :class="{ success: submitStatus === 'success', error: submitStatus === 'error' }">
              {{ submitStatus === 'success' ? '消息发送成功！' : '发送失败，请重试。' }}
            </div>
          </form>
        </div>
      </div>

      <!-- Map -->
      <div class="map-section">
        <h2>办公地点</h2>
        <div class="map-placeholder">
          <div class="map-content">
            <div class="map-icon">🗺️</div>
            <p>北京市朝阳区某科技园区</p>
            <span>点击查看详细地图</span>
          </div>
        </div>
      </div>

      <!-- FAQ -->
      <div class="faq-section">
        <h2>常见问题</h2>
        <div class="faq-grid">
          <div class="faq-item">
            <h3>项目开发周期是多久？</h3>
            <p>根据项目复杂程度，一般网站开发需要2-4周，复杂应用可能需要1-3个月。</p>
          </div>
          <div class="faq-item">
            <h3>如何开始合作？</h3>
            <p>请通过表单联系我，我们会安排一次免费咨询，讨论项目需求和细节。</p>
          </div>
          <div class="faq-item">
            <h3>支持远程协作吗？</h3>
            <p>是的，我支持完全远程协作，通过在线会议和项目管理工具保持沟通。</p>
          </div>
          <div class="faq-item">
            <h3>提供售后服务吗？</h3>
            <p>所有项目都包含3个月的免费维护期，之后可以选择续订维护服务。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const isSubmitting = ref(false)
const submitStatus = ref('') // 'success', 'error', ''

const submitForm = async () => {
  isSubmitting.value = true
  submitStatus.value = ''
  
  // 模拟表单提交
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 随机模拟成功或失败
    const isSuccess = Math.random() > 0.3
    submitStatus.value = isSuccess ? 'success' : 'error'
    
    if (isSuccess) {
      // 清空表单
      form.value = {
        name: '',
        email: '',
        subject: '',
        message: ''
      }
    }
  } catch (error) {
    submitStatus.value = 'error'
  } finally {
    isSubmitting.value = false
    
    // 5秒后清除状态提示
    setTimeout(() => {
      submitStatus.value = ''
    }, 5000)
  }
}
</script>

<style scoped>
.contact {
  padding: 2rem 0;
}

.contact-header {
  text-align: center;
  margin-bottom: 3rem;
}

.contact-header h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #333;
}

.contact-header p {
  font-size: 1.1rem;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  margin-bottom: 4rem;
}

.contact-info h2,
.contact-form h2 {
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #333;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.info-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.info-content h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.info-content p {
  color: #667eea;
  font-weight: 500;
  margin-bottom: 0.3rem;
}

.info-content span {
  color: #999;
  font-size: 0.9rem;
}

.social-links h3 {
  margin-bottom: 1rem;
  color: #333;
}

.social-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.social-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.social-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  color: #667eea;
}

.social-icon {
  font-size: 1.2rem;
}

.contact-form {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.8rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

button[type="submit"] {
  width: 100%;
  margin-top: 1rem;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-status {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.submit-status.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.submit-status.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.map-section {
  margin-bottom: 4rem;
}

.map-section h2 {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #333;
}

.map-placeholder {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 300px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.map-placeholder:hover {
  transform: scale(1.02);
}

.map-content {
  text-align: center;
}

.map-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.map-content p {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.map-content span {
  opacity: 0.9;
}

.faq-section h2 {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #333;
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.faq-item {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.faq-item h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.2rem;
}

.faq-item p {
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .contact-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .info-item {
    flex-direction: column;
    text-align: center;
  }
  
  .social-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-form {
    padding: 1.5rem;
  }
  
  .map-placeholder {
    height: 200px;
  }
  
  .map-icon {
    font-size: 3rem;
  }
  
  .faq-grid {
    grid-template-columns: 1fr;
  }
}
</style>