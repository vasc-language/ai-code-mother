<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero">
      <div class="hero-content">
        <div class="container">
          <div class="hero-text">
            <h1>引领企业数字化转型</h1>
            <p class="hero-description">
              我们提供全方位的数字化解决方案，助力企业实现智能化升级，
              提升运营效率，创造更大商业价值
            </p>
            <div class="hero-actions">
              <router-link to="/products" class="btn btn-primary">
                了解产品
              </router-link>
              <router-link to="/contact" class="btn btn-secondary">
                联系我们
              </router-link>
            </div>
          </div>
          <div class="hero-visual">
            <div class="visual-placeholder">
              <div class="tech-icons">
                <span class="icon">💻</span>
                <span class="icon">☁️</span>
                <span class="icon">📊</span>
                <span class="icon">🤖</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-background"></div>
    </section>

    <!-- 服务特色 -->
    <section class="features">
      <div class="container">
        <div class="section-header">
          <h2>塑造智能商业未来</h2>
          <p>为企业提供全方位的数字化服务，助力业务增长</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🚀</div>
            <h3>高效开发</h3>
            <p>采用敏捷开发流程，快速响应需求变化，确保项目按时交付</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>安全可靠</h3>
            <p>严格的安全标准和数据保护措施，确保系统稳定运行</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📈</div>
            <h3>智能分析</h3>
            <p>基于大数据和AI技术，提供深度业务洞察和决策支持</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🌐</div>
            <h3>云端部署</h3>
            <p>支持多云部署，弹性扩展，降低IT成本，提高灵活性</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 产品服务 -->
    <section class="services">
      <div class="container">
        <div class="section-header">
          <h2>产品与服务</h2>
          <p>全面的数字化解决方案，满足不同行业需求</p>
        </div>
        <div class="services-grid">
          <div class="service-card">
            <div class="service-image">
              <img src="https://picsum.photos/400/300?random=1" alt="软件开发" />
            </div>
            <div class="service-content">
              <h3>定制软件开发</h3>
              <p>为企业量身定制各类管理系统、移动应用和Web平台</p>
              <ul>
                <li>企业资源规划系统</li>
                <li>客户关系管理系统</li>
                <li>移动应用开发</li>
                <li>电商平台建设</li>
              </ul>
              <router-link to="/products" class="service-link">
                了解更多 →
              </router-link>
            </div>
          </div>
          <div class="service-card">
            <div class="service-image">
              <img src="https://picsum.photos/400/300?random=2" alt="云端服务" />
            </div>
            <div class="service-content">
              <h3>云端服务部署</h3>
              <p>提供专业的云架构设计和部署服务，确保系统高可用性</p>
              <ul>
                <li>云服务器托管</li>
                <li>数据库管理</li>
                <li>负载均衡配置</li>
                <li>灾难恢复方案</li>
              </ul>
              <router-link to="/products" class="service-link">
                了解更多 →
              </router-link>
            </div>
          </div>
          <div class="service-card">
            <div class="service-image">
              <img src="https://picsum.photos/400/300?random=3" alt="数据分析" />
            </div>
            <div class="service-content">
              <h3>数据分析洞察</h3>
              <p>利用先进的数据分析技术，挖掘业务价值，驱动决策</p>
              <ul>
                <li>业务数据可视化</li>
                <li>预测分析模型</li>
                <li>用户行为分析</li>
                <li>实时数据监控</li>
              </ul>
              <router-link to="/products" class="service-link">
                了解更多 →
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 成功案例 -->
    <section class="case-studies">
      <div class="container">
        <div class="section-header">
          <h2>成功案例</h2>
          <p>我们为各行业客户提供专业的数字化解决方案</p>
        </div>
        <div class="cases-grid">
          <div class="case-card">
            <div class="case-image">
              <img src="https://picsum.photos/400/300?random=4" alt="金融行业案例" />
            </div>
            <div class="case-content">
              <span class="case-category">金融行业</span>
              <h3>银行智能风控系统</h3>
              <p>为某大型银行构建智能风控平台，实现实时交易监控和风险预警</p>
              <div class="case-stats">
                <div class="stat">
                  <span class="stat-number">85%</span>
                  <span class="stat-label">风险识别率提升</span>
                </div>
                <div class="stat">
                  <span class="stat-number">60%</span>
                  <span class="stat-label">人工审核减少</span>
                </div>
              </div>
            </div>
          </div>
          <div class="case-card">
            <div class="case-image">
              <img src="https://picsum.photos/400/300?random=5" alt="零售行业案例" />
            </div>
            <div class="case-content">
              <span class="case-category">零售行业</span>
              <h3>连锁超市数字化升级</h3>
              <p>帮助连锁超市实现全渠道数字化，提升顾客体验和运营效率</p>
              <div class="case-stats">
                <div class="stat">
                  <span class="stat-number">40%</span>
                  <span class="stat-label">线上销售增长</span>
                </div>
                <div class="stat">
                  <span class="stat-number">30%</span>
                  <span class="stat-label">库存周转提升</span>
                </div>
              </div>
            </div>
          </div>
          <div class="case-card">
            <div class="case-image">
              <img src="https://picsum.photos/400/300?random=6" alt="制造业案例" />
            </div>
            <div class="case-content">
              <span class="case-category">制造业</span>
              <h3>智能工厂管理系统</h3>
              <p>为制造企业打造智能工厂平台，实现生产全流程数字化管理</p>
              <div class="case-stats">
                <div class="stat">
                  <span class="stat-number">25%</span>
                  <span class="stat-label">生产效率提升</span>
                </div>
                <div class="stat">
                  <span class="stat-number">15%</span>
                  <span class="stat-label">成本降低</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 客户评价 -->
    <section class="testimonials">
      <div class="container">
        <div class="section-header">
          <h2>客户评价</h2>
          <p>听听我们的客户怎么说</p>
        </div>
        <div class="testimonials-grid">
          <div class="testimonial-card">
            <div class="testimonial-content">
              <p>"科技公司的团队专业且高效，他们为我们开发的系统极大地提升了工作效率，售后服务也非常到位。"</p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <img src="https://picsum.photos/80/80?random=7" alt="张总" />
              </div>
              <div class="author-info">
                <h4>张总</h4>
                <p>某金融科技公司 CEO</p>
              </div>
            </div>
          </div>
          <div class="testimonial-card">
            <div class="testimonial-content">
              <p>"数字化转型过程中，科技公司提供了全方位的支持，从咨询到实施都非常专业，效果超出预期。"</p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <img src="https://picsum.photos/80/80?random=8" alt="李经理" />
              </div>
              <div class="author-info">
                <h4>李经理</h4>
                <p>某零售企业 IT总监</p>
              </div>
            </div>
          </div>
          <div class="testimonial-card">
            <div class="testimonial-content">
              <p>"合作非常愉快，技术团队响应迅速，解决问题的能力很强，是我们值得信赖的长期合作伙伴。"</p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <img src="https://picsum.photos/80/80?random=9" alt="王厂长" />
              </div>
              <div class="author-info">
                <h4>王厂长</h4>
                <p>某制造企业 生产厂长</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 行动召唤 -->
    <section class="cta">
      <div class="container">
        <div class="cta-content">
          <h2>准备好开始数字化转型了吗？</h2>
          <p>让我们帮助您实现业务增长和技术创新</p>
          <div class="cta-actions">
            <router-link to="/contact" class="btn btn-primary">
              立即咨询
            </router-link>
            <router-link to="/products" class="btn btn-secondary">
              查看服务
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  overflow-x: hidden;
}

/* 英雄区域样式 */
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 4rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.hero-content {
  position: relative;
  z-index: 2;
  width: 100%;
}

.hero-content .container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.hero-text h1 {
  font-size: 3.5rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  line-height: 1.2;
}

.hero-description {
  font-size: 1.25rem;
  margin-bottom: 2.5rem;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

.visual-placeholder {
  width: 400px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tech-icons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.tech-icons .icon {
  font-size: 3rem;
  text-align: center;
  animation: float 3s ease-in-out infinite;
}

.tech-icons .icon:nth-child(2) { animation-delay: 0.5s; }
.tech-icons .icon:nth-child(3) { animation-delay: 1s; }
.tech-icons .icon:nth-child(4) { animation-delay: 1.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://picsum.photos/1920/1080?random=10') center/cover;
  opacity: 0.1;
}

/* 通用部分样式 */
.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-header h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.section-header p {
  font-size: 1.125rem;
  color: #64748b;
}

/* 特色区域样式 */
.features {
  padding: 6rem 0;
  background: #f8fafc;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2.5rem 2rem;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.feature-card p {
  color: #64748b;
  line-height: 1.6;
}

/* 服务区域样式 */
.services {
  padding: 6rem 0;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.service-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.service-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.service-card:hover .service-image img {
  transform: scale(1.05);
}

.service-content {
  padding: 2rem;
}

.service-content h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.service-content p {
  color: #64748b;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.service-content ul {
  list-style: none;
  margin-bottom: 1.5rem;
}

.service-content li {
  color: #64748b;
  margin-bottom: 0.5rem;
  padding-left: 1.5rem;
  position: relative;
}

.service-content li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #10b981;
  font-weight: bold;
}

.service-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.service-link:hover {
  color: #5a67d8;
}

/* 案例区域样式 */
.case-studies {
  padding: 6rem 0;
  background: #f8fafc;
}

.cases-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.case-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.case-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.case-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-content {
  padding: 2rem;
}

.case-category {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 1rem;
}

.case-content h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #1e293b;
}

.case-content p {
  color: #64748b;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.case-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.stat {
  text-align: center;
  padding: 1rem;
  background: #f1f5f9;
  border-radius: 8px;
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
}

/* 评价区域样式 */
.testimonials {
  padding: 6rem 0;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.testimonial-card {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.testimonial-content p {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  font-style: italic;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.author-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info h4 {
  font-size: 1.125rem;
  margin-bottom: 0.25rem;
  color: #1e293b;
}

.author-info p {
  color: #64748b;
  font-size: 0.875rem;
}

/* CTA区域样式 */
.cta {
  padding: 6rem 0;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
  text-align: center;
}

.cta-content h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.cta-content p {
  font-size: 1.125rem;
  margin-bottom: 2.5rem;
  opacity: 0.9;
}

.cta-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero-content .container {
    grid-template-columns: 1fr;
    gap: 3rem;
    text-align: center;
  }
  
  .hero-text h1 {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .visual-placeholder {
    width: 300px;
    height: 250px;
  }
}

@media (max-width: 768px) {
  .hero {
    min-height: 80vh;
    padding: 2rem 0;
  }
  
  .hero-text h1 {
    font-size: 2rem;
  }
  
  .hero-description {
    font-size: 1.125rem;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .features,
  .services,
  .case-studies,
  .testimonials,
  .cta {
    padding: 4rem 0;
  }
  
  .services-grid,
  .cases-grid {
    grid-template-columns: 1fr;
  }
  
  .cta-content h2 {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .hero-text h1 {
    font-size: 1.75rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .hero-actions .btn {
    width: 200px;
  }
  
  .visual-placeholder {
    width: 250px;
    height: 200px;
  }
  
  .tech-icons {
    gap: 1rem;
  }
  
  .tech-icons .icon {
    font-size: 2rem;
  }
  
  .feature-card,
  .service-content,
  .case-content,
  .testimonial-card {
    padding: 1.5rem;
  }
  
  .cta-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .cta-actions .btn {
    width: 200px;
  }
}
</style>