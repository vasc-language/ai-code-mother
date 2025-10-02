<template>
  <div class="doc-page">
    <h1 class="doc-title">常见问题</h1>
    <p class="doc-intro">
      收集了用户最常遇到的问题和解决方案,帮助你快速解决使用中的疑问。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        账户相关
      </h2>

      <a-collapse :bordered="false" default-active-key="['1']">
        <a-collapse-panel key="1" header="如何注册账号?">
          <p>点击页面右上角的"注册"按钮，使用邮箱注册账号：</p>
          <ul>
            <li>填写邮箱地址，点击"发送验证码"</li>
            <li>输入收到的6位数字验证码（有效期5分钟）</li>
            <li>设置密码（确认两次密码一致）</li>
            <li>可选填写邀请码（如有）</li>
          </ul>
          <p>注册成功后会自动登录，可在个人主页查看账户信息。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="忘记密码怎么办?">
          <p>目前系统支持邮箱验证码登录，无需记住密码：</p>
          <ul>
            <li>在登录页面选择"邮箱验证码登录"</li>
            <li>输入注册邮箱并获取验证码</li>
            <li>输入验证码即可登录</li>
          </ul>
          <p>如需重置密码功能，请联系管理员或等待后续版本更新。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="可以修改个人资料吗?">
          <p>可以在个人主页编辑以下信息：</p>
          <ul>
            <li><strong>用户昵称：</strong>2-20个字符</li>
            <li><strong>头像URL：</strong>支持网络图片链接</li>
            <li><strong>个人简介：</strong>最多200字符</li>
          </ul>
          <p>注意：邮箱（账号）注册后不可修改。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="账户角色有什么区别?">
          <p>系统支持两种角色：</p>
          <ul>
            <li><strong>普通用户（user）：</strong>可使用所有代码生成功能</li>
            <li><strong>管理员（admin）：</strong>拥有用户管理、应用管理等额外权限</li>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <WalletOutlined class="title-icon" />
        积分相关
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="积分系统是什么?">
          <p>AI Code Mother 使用积分系统来管理代码生成次数：</p>
          <ul>
            <li>不同类型的应用消耗不同积分</li>
            <li>积分不足时无法生成代码</li>
            <li>可通过多种方式获取积分</li>
            <li>积分记录可在个人中心查看</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成代码消耗多少积分?">
          <p>根据生成类型和复杂度消耗不同积分：</p>
          <ul>
            <li><strong>简单HTML页面：</strong>消耗较少积分</li>
            <li><strong>Vue3应用：</strong>消耗中等积分</li>
            <li><strong>多文件项目：</strong>消耗较多积分</li>
          </ul>
          <p>具体积分消耗在创建应用时会显示。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成失败积分会退还吗?">
          <p>是的。如果生成失败（如AI服务异常、网络错误等），系统会自动退还已扣除的积分。</p>
          <p>你可以在积分记录中查看退还详情。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="积分会过期吗?">
          <p>目前积分永久有效，不会过期。</p>
          <p>建议合理使用积分，优先生成真正需要的应用。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <CodeOutlined class="title-icon" />
        生成相关
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="支持哪些生成类型?">
          <p>目前支持三种代码生成类型：</p>
          <ul>
            <li><strong>HTML页面：</strong>单个HTML文件，包含CSS和JavaScript</li>
            <li><strong>Vue3应用：</strong>完整的Vue3项目，支持组件化开发</li>
            <li><strong>多文件项目：</strong>包含多个文件的完整项目结构</li>
          </ul>
          <p>在创建应用时可以选择生成类型。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="使用什么AI模型生成?">
          <p>系统集成了先进的AI模型：</p>
          <ul>
            <li><strong>主要模型：</strong>DeepSeek Reasoner（深度推理）</li>
            <li><strong>辅助模型：</strong>Qwen Turbo（快速分类）</li>
            <li>支持实时流式生成，可查看生成过程</li>
            <li>最大支持32,768个token</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成的代码质量如何?">
          <p>AI生成的代码具有以下特点：</p>
          <ul>
            <li>遵循业界最佳实践和编码规范</li>
            <li>清晰的代码结构和命名</li>
            <li>完整的注释说明</li>
            <li>基本的错误处理</li>
          </ul>
          <p>建议根据实际需求进行调整和优化，代码仅供参考和学习。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何提高生成成功率?">
          <p>编写清晰的需求描述很重要：</p>
          <ul>
            <li>详细描述页面功能和布局</li>
            <li>说明技术栈要求（如Vue3、TypeScript）</li>
            <li>指定设计风格和UI组件库</li>
            <li>拆分复杂需求为多个简单应用</li>
            <li>确保积分充足</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="5" header="生成失败怎么办?">
          <p>生成失败的常见原因和解决方法：</p>
          <ul>
            <li><strong>积分不足：</strong>检查积分余额</li>
            <li><strong>需求不清晰：</strong>重新描述得更详细</li>
            <li><strong>AI服务异常：</strong>稍后重试</li>
            <li><strong>网络问题：</strong>检查网络连接</li>
          </ul>
          <p>失败后积分会自动退还，可以修改需求重新生成。</p>
        </a-collapse-panel>

        <a-collapse-panel key="6" header="支持实时查看生成过程吗?">
          <p>是的！系统支持SSE（Server-Sent Events）流式传输：</p>
          <ul>
            <li>实时显示AI生成的代码</li>
            <li>可以看到生成进度</li>
            <li>支持中断生成过程</li>
            <li>生成完成后自动保存</li>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        技术问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="生成的代码如何在本地运行?">
          <p>根据项目类型有不同的运行方式：</p>
          <ul>
            <li><strong>HTML页面：</strong>直接用浏览器打开HTML文件</li>
            <li><strong>Vue3应用：</strong>
              <ol>
                <li>解压下载的项目文件</li>
                <li>在项目目录执行 <code>npm install</code></li>
                <li>执行 <code>npm run dev</code> 启动开发服务器</li>
              </ol>
            </li>
            <li><strong>多文件项目：</strong>查看项目中的 README.md 文件说明</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成的代码保存在哪里?">
          <p>代码文件的存储位置：</p>
          <ul>
            <li><strong>服务器端：</strong>保存在 <code>tmp/code_output/</code> 目录（带时间戳）</li>
            <li><strong>云存储：</strong>可上传至腾讯云COS</li>
            <li><strong>下载：</strong>可通过应用详情页下载到本地</li>
          </ul>
          <p>建议及时下载保存，避免服务器清理导致丢失。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="如何部署生成的应用?">
          <p>常见的部署方式：</p>
          <ul>
            <li><strong>静态页面：</strong>上传到服务器或使用GitHub Pages</li>
            <li><strong>Vue应用：</strong>
              <ol>
                <li>执行 <code>npm run build</code> 打包</li>
                <li>将 <code>dist</code> 目录上传到服务器</li>
              </ol>
            </li>
            <li><strong>云服务：</strong>使用Vercel、Netlify等平台一键部署</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="生成的代码报错怎么办?">
          <p>遇到错误时的排查步骤：</p>
          <ol>
            <li>仔细阅读错误提示信息</li>
            <li>检查是否已安装依赖（<code>npm install</code>）</li>
            <li>确认Node.js版本符合要求</li>
            <li>查看项目文档和README</li>
            <li>可在线编辑修改代码</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="5" header="支持哪些技术栈?">
          <p>当前支持的主要技术：</p>
          <ul>
            <li><strong>前端框架：</strong>Vue 3, HTML5</li>
            <li><strong>构建工具：</strong>Vite</li>
            <li><strong>语言：</strong>JavaScript, TypeScript</li>
            <li><strong>UI库：</strong>可指定Ant Design Vue等</li>
          </ul>
          <p>后续会支持更多技术栈，敬请期待。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ContactsOutlined class="title-icon" />
        其他问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="平台完全免费吗?">
          <p>AI Code Mother 是一个开源项目，代码完全开放：</p>
          <ul>
            <li>GitHub仓库：<a href="https://github.com/vasc-language/ai-code-mother" target="_blank">vasc-language/ai-code-mother</a></li>
            <li>可以自行部署使用</li>
            <li>使用积分系统管理资源</li>
          </ul>
          <p>注意：AI模型调用可能产生费用（如使用DeepSeek API）。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="数据安全吗?">
          <p>我们重视用户数据安全：</p>
          <ul>
            <li><strong>密码加密：</strong>使用MD5+盐值加密存储</li>
            <li><strong>会话管理：</strong>基于Redis的分布式会话，30天有效期</li>
            <li><strong>数据隔离：</strong>每个用户的应用和代码独立存储</li>
            <li><strong>软删除：</strong>支持逻辑删除，数据可恢复</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="技术架构是什么?">
          <p>项目采用前后端分离架构：</p>
          <ul>
            <li><strong>后端：</strong>Spring Boot 3.5.4 + Java 21 + MyBatis-Flex</li>
            <li><strong>前端：</strong>Vue 3 + TypeScript + Vite + Ant Design Vue</li>
            <li><strong>数据库：</strong>MySQL 8.0</li>
            <li><strong>缓存：</strong>Redis + Caffeine本地缓存</li>
            <li><strong>AI集成：</strong>LangChain4j + DeepSeek + DashScope</li>
            <li><strong>监控：</strong>Prometheus + Spring Boot Actuator</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何联系开发者?">
          <p>欢迎通过以下方式联系：</p>
          <ul>
            <li><strong>GitHub Issues：</strong><a href="https://github.com/vasc-language/ai-code-mother/issues" target="_blank">提交问题反馈</a></li>
            <li><strong>邮箱：</strong>zrt3ljnygz@163.com</li>
            <li><strong>官网：</strong><a href="https://www.joinoai.cloud/" target="_blank">www.joinoai.cloud</a></li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="5" header="如何参与项目开发?">
          <p>欢迎贡献代码和建议：</p>
          <ol>
            <li>Fork GitHub仓库</li>
            <li>阅读 CLAUDE.md 了解项目结构</li>
            <li>提交Pull Request</li>
            <li>加入社区讨论</li>
          </ol>
          <p>项目使用MIT协议，欢迎自由使用和改进。</p>
        </a-collapse-panel>

        <a-collapse-panel key="6" header="后续有什么规划?">
          <p>项目持续更新中，规划功能包括：</p>
          <ul>
            <li>支持更多AI模型（Claude、GPT等）</li>
            <li>一键部署到云平台</li>
            <li>代码模板市场</li>
            <li>团队协作功能</li>
            <li>代码质量分析</li>
            <li>更多生成类型（Spring Boot、React等）</li>
          </ul>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <a-divider />

    <div style="text-align: center; padding: 24px; background: #f5f5f5; border-radius: 8px;">
      <h3>没有找到答案?</h3>
      <p>如果以上内容没有解决你的问题,请访问 GitHub 仓库提 Issue 或联系客服</p>
      <a-space>
        <a-button type="primary" href="https://github.com/vasc-language/ai-code-mother" target="_blank">
          <GithubOutlined /> GitHub 仓库
        </a-button>
        <a-button href="mailto:zrt3ljnygz@163.com">
          <MailOutlined /> 发送邮件
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  QuestionCircleOutlined,
  WalletOutlined,
  CodeOutlined,
  ToolOutlined,
  ContactsOutlined,
  GithubOutlined,
  MailOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import './features/doc-common.css';

/* 优化折叠面板样式 */
:deep(.ant-collapse) {
  background: transparent;
  border: none;
}

:deep(.ant-collapse-item) {
  background: white;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

:deep(.ant-collapse-item:hover) {
  border-color: #ec4899;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.12);
  transform: translateY(-2px);
}

:deep(.ant-collapse-item-active) {
  border-color: #ec4899;
  box-shadow: 0 4px 16px rgba(236, 72, 153, 0.15);
}

:deep(.ant-collapse-header) {
  padding: 16px 20px !important;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  transition: all 0.3s ease;
}

:deep(.ant-collapse-item:hover .ant-collapse-header) {
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
  color: #ec4899;
}

:deep(.ant-collapse-item-active .ant-collapse-header) {
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  color: white;
}

:deep(.ant-collapse-expand-icon) {
  padding-inline-end: 12px !important;
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.ant-collapse-item-active .ant-collapse-expand-icon) {
  color: white;
}

:deep(.ant-collapse-content) {
  border-top: 2px solid #f0f0f0;
  background: white;
}

:deep(.ant-collapse-content-box) {
  padding: 20px 24px !important;
  line-height: 1.9;
  color: #555;
}

:deep(.ant-collapse-content-box p) {
  margin: 12px 0;
  font-size: 15px;
  line-height: 1.9;
}

:deep(.ant-collapse-content-box ul),
:deep(.ant-collapse-content-box ol) {
  margin: 16px 0;
  padding-left: 28px;
}

:deep(.ant-collapse-content-box li) {
  margin: 10px 0;
  padding: 6px 10px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #ec4899;
  transition: all 0.2s ease;
}

:deep(.ant-collapse-content-box li:hover) {
  background: #fef2f2;
  transform: translateX(4px);
}

:deep(.ant-collapse-content-box strong) {
  color: #ec4899;
  font-weight: 600;
}

/* 底部联系卡片优化 */
.doc-page > div:last-child {
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%) !important;
  border: 2px solid #e8ecff !important;
  border-radius: 16px !important;
  padding: 32px 24px !important;
  box-shadow: 0 4px 16px rgba(236, 72, 153, 0.1);
}

.doc-page > div:last-child h3 {
  color: #1a1a1a;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 12px;
}

.doc-page > div:last-child p {
  color: #666;
  font-size: 15px;
  margin-bottom: 24px;
}

:deep(.doc-page .ant-btn-primary) {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 22px;
  background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
  transition: all 0.3s ease;
}

:deep(.doc-page .ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(236, 72, 153, 0.4);
}

:deep(.doc-page .ant-btn:not(.ant-btn-primary)) {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 22px;
  border: 2px solid #ec4899;
  color: #ec4899;
  transition: all 0.3s ease;
}

:deep(.doc-page .ant-btn:not(.ant-btn-primary):hover) {
  background: #ec4899;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
}
</style>
