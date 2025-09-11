<template>
  <div class="about">
    <div class="container">
      <!-- Hero Section -->
      <section class="about-hero">
        <div class="hero-content">
          <h1>关于我</h1>
          <p class="hero-subtitle">
            一名充满激情的前端开发者，专注于创造卓越的数字体验
          </p>
        </div>
        <div class="hero-image">
          <img src="https://picsum.photos/400/400?random=13" alt="个人照片" />
        </div>
      </section>

      <!-- Intro Section -->
      <section class="intro section">
        <h2>我的故事</h2>
        <div class="intro-content">
          <p>
            我是一名拥有3年以上经验的前端开发工程师，专注于Vue.js和现代Web技术。
            我的职业生涯始于对设计和技术的热爱，这种热情驱使我不断学习和探索新的技术趋势。
          </p>
          <p>
            我相信好的设计不仅仅是外观漂亮，更重要的是用户体验和功能性。
            我致力于创建既美观又实用的数字产品，帮助客户实现他们的业务目标。
          </p>
        </div>
      </section>

      <!-- Skills Section -->
      <section class="skills section">
        <h2>技术技能</h2>
        <div class="skills-grid">
          <div class="skill-category">
            <h3>前端技术</h3>
            <div class="skill-list">
              <div class="skill-item">
                <span class="skill-name">Vue.js</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 95%"></div>
                </div>
                <span class="skill-percent">95%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">React</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 85%"></div>
                </div>
                <span class="skill-percent">85%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">JavaScript/TypeScript</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 90%"></div>
                </div>
                <span class="skill-percent">90%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">HTML/CSS</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 98%"></div>
                </div>
                <span class="skill-percent">98%</span>
              </div>
            </div>
          </div>

          <div class="skill-category">
            <h3>后端技术</h3>
            <div class="skill-list">
              <div class="skill-item">
                <span class="skill-name">Node.js</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 80%"></div>
                </div>
                <span class="skill-percent">80%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">Express</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 75%"></div>
                </div>
                <span class="skill-percent">75%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">MongoDB</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 70%"></div>
                </div>
                <span class="skill-percent">70%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">MySQL</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 65%"></div>
                </div>
                <span class="skill-percent">65%</span>
              </div>
            </div>
          </div>

          <div class="skill-category">
            <h3>设计工具</h3>
            <div class="skill-list">
              <div class="skill-item">
                <span class="skill-name">Figma</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 90%"></div>
                </div>
                <span class="skill-percent">90%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">Adobe XD</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 85%"></div>
                </div>
                <span class="skill-percent">85%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">Photoshop</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 75%"></div>
                </div>
                <span class="skill-percent">75%</span>
              </div>
              <div class="skill-item">
                <span class="skill-name">Illustrator</span>
                <div class="skill-bar">
                  <div class="skill-progress" style="width: 70%"></div>
                </div>
                <span class="skill-percent">70%</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Experience Section -->
      <section class="experience section">
        <h2>工作经历</h2>
        <div class="timeline">
          <div class="timeline-item">
            <div class="timeline-content">
              <h3>高级前端开发工程师</h3>
              <span class="company">某科技公司 · 2022年至今</span>
              <p>负责核心产品的前端架构设计和开发，带领团队完成多个大型项目</p>
              <ul>
                <li>主导电商平台重构项目，提升性能40%</li>
                <li>设计并实现组件库，提高团队开发效率</li>
                <li> mentoring 初级开发人员</li>
              </ul>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-content">
              <h3>前端开发工程师</h3>
              <span class="company">某互联网公司 · 2020-2022</span>
              <p>参与企业级应用开发，专注于用户界面和交互体验优化</p>
              <ul>
                <li>开发响应式Web应用，支持多设备访问</li>
                <li>优化网站性能，减少加载时间50%</li>
                <li>与设计团队紧密合作，实现设计到代码的完美转换</li>
              </ul>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-content">
              <h3>UI/UX设计师</h3>
              <span class="company">某设计工作室 · 2019-2020</span>
              <p>负责客户项目的用户界面设计和用户体验优化</p>
              <ul>
                <li>为多个品牌设计完整的视觉识别系统</li>
                <li>创建用户流程和交互原型</li>
                <li>进行用户研究和可用性测试</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- Education Section -->
      <section class="education section">
        <h2>教育背景</h2>
        <div class="education-grid">
          <div class="education-item">
            <h3>计算机科学与技术</h3>
            <span class="school">某大学 · 学士学位</span>
            <span class="year">2015-2019</span>
            <p>主修Web开发、算法设计和软件工程</p>
          </div>
          <div class="education-item">
            <h3>UI/UX设计认证</h3>
            <span class="school">某设计学院 · 专业认证</span>
            <span class="year">2019</span>
            <p>用户界面设计、用户体验研究和交互设计</p>
          </div>
        </div>
      </section>

      <!-- Call to Action -->
      <section class="cta section">
        <h2>准备好开始合作了吗？</h2>
        <p>如果您有项目需要开发或者想要了解更多信息，请随时联系我</p>
        <router-link to="/contact" class="btn">联系我</router-link>
      </section>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about {
  padding: 2rem 0;
}

.about-hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  margin-bottom: 4rem;
}

.hero-content h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #333;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: #666;
  line-height: 1.6;
}

.hero-image img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.section {
  margin-bottom: 4rem;
}

.section h2 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
  color: #333;
  text-align: center;
}

.intro-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
}

.intro-content p {
  font-size: 1.1rem;
  color: #666;
  line-height: 1.8;
  margin-bottom: 1.5rem;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.skill-category {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.skill-category h3 {
  margin-bottom: 1.5rem;
  color: #333;
  text-align: center;
}

.skill-item {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.skill-name {
  min-width: 120px;
  color: #333;
  font-weight: 500;
}

.skill-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.skill-progress {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 1s ease;
}

.skill-percent {
  min-width: 40px;
  text-align: right;
  color: #666;
  font-weight: 500;
}

.timeline {
  max-width: 800px;
  margin: 0 auto;
}

.timeline-item {
  position: relative;
  padding-left: 2rem;
  margin-bottom: 3rem;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 2px;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.timeline-item::after {
  content: '';
  position: absolute;
  left: -4px;
  top: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #667eea;
}

.timeline-content {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.timeline-content h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.company {
  color: #667eea;
  font-weight: 500;
  margin-bottom: 1rem;
  display: block;
}

.timeline-content p {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.timeline-content ul {
  color: #666;
  padding-left: 1.5rem;
}

.timeline-content li {
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.education-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.education-item {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.education-item h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.school {
  color: #667eea;
  font-weight: 500;
  display: block;
  margin-bottom: 0.5rem;
}

.year {
  color: #999;
  display: block;
  margin-bottom: 1rem;
}

.education-item p {
  color: #666;
  line-height: 1.6;
}

.cta {
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4rem 2rem;
  border-radius: 20px;
}

.cta h2 {
  color: white;
  margin-bottom: 1rem;
}

.cta p {
  margin-bottom: 2rem;
  opacity: 0.9;
  font-size: 1.1rem;
}

.cta .btn {
  background: white;
  color: #667eea;
}

.cta .btn:hover {
  background: #f8f9fa;
}

@media (max-width: 768px) {
  .about-hero {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .hero-content h1 {
    font-size: 2rem;
  }
  
  .skills-grid {
    grid-template-columns: 1fr;
  }
  
  .skill-item {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }
  
  .skill-name {
    min-width: auto;
  }
  
  .timeline-item {
    padding-left: 1.5rem;
  }
  
  .education-grid {
    grid-template-columns: 1fr;
  }
  
  .cta {
    padding: 3rem 1rem;
  }
}
</style>