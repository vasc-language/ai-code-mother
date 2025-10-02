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
          <p>点击页面右上角的"登录"按钮,然后选择"注册新账号",填写用户名和密码即可完成注册。</p>
          <p>注册成功后会自动获得 100 初始积分。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="忘记密码怎么办?">
          <p>目前暂不支持找回密码功能。建议记住密码或使用密码管理工具保存。</p>
          <p>如需重置密码,请联系管理员。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="可以修改用户名吗?">
          <p>用户名一旦注册不可修改。请在注册时慎重选择用户名。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何删除账号?">
          <p>如需删除账号,请联系管理员处理。删除后积分和历史记录将永久丢失。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <WalletOutlined class="title-icon" />
        积分相关
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何获得积分?">
          <p>主要有以下几种方式:</p>
          <ul>
            <li>新用户注册:100 积分(一次性)</li>
            <li>每日签到:10 积分</li>
            <li>邀请好友:100 积分/人(不限次数)</li>
            <li>参与活动:根据活动规则获得</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="积分会过期吗?">
          <p>目前积分永久有效,不会过期。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="积分不够用怎么办?">
          <p>可以通过以下方式快速获取积分:</p>
          <ul>
            <li>每天坚持签到获得积分</li>
            <li>邀请好友注册(双方各得 100 积分)</li>
            <li>参与平台活动</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="生成失败积分会退还吗?">
          <p>是的。如果生成失败(如网络错误、服务器异常等),已扣除的积分会自动退还到账户。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <CodeOutlined class="title-icon" />
        生成相关
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="生成失败怎么办?">
          <p>生成失败可能的原因和解决方法:</p>
          <ul>
            <li><strong>积分不足:</strong> 检查积分余额是否充足</li>
            <li><strong>需求不清晰:</strong> 尝试更详细地描述需求</li>
            <li><strong>网络问题:</strong> 检查网络连接,刷新页面重试</li>
            <li><strong>服务器繁忙:</strong> 稍后再试</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成的代码质量如何?">
          <p>AI 生成的代码具有以下特点:</p>
          <ul>
            <li>遵循业界最佳实践和编码规范</li>
            <li>清晰的代码结构和命名</li>
            <li>完整的注释说明</li>
            <li>基本的错误处理</li>
          </ul>
          <p>建议根据实际需求进行调整和优化。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成的代码可以商用吗?">
          <p>可以。生成的代码归你所有,可以自由使用、修改和商用。</p>
          <p>我们建议在实际使用前进行测试和优化。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何提高生成成功率?">
          <ul>
            <li>清晰详细地描述需求</li>
            <li>说明页面结构和功能要点</li>
            <li>指定技术栈和设计风格</li>
            <li>避免过于复杂的需求</li>
            <li>确保积分充足</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="5" header="生成时间为什么不一样?">
          <p>生成时间受以下因素影响:</p>
          <ul>
            <li>生成类型:HTML &lt; Vue &lt; 多文件</li>
            <li>项目复杂度:功能越多越慢</li>
            <li>服务器负载:高峰期可能较慢</li>
            <li>网络状况:网络越好越快</li>
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
          <p>根据项目类型:</p>
          <ul>
            <li><strong>HTML 页面:</strong> 直接双击打开</li>
            <li><strong>Vue 应用:</strong> npm install && npm run dev</li>
            <li><strong>多文件项目:</strong> 查看 README.md 说明</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="如何部署生成的应用?">
          <p>部署方法:</p>
          <ul>
            <li>静态页面:上传到服务器或使用 GitHub Pages</li>
            <li>Vue 应用:打包后部署(npm run build)</li>
            <li>使用云服务:Vercel、Netlify 等</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成的代码报错怎么办?">
          <ol>
            <li>查看错误提示,了解问题所在</li>
            <li>检查是否缺少依赖(npm install)</li>
            <li>使用在线编辑器修改代码</li>
            <li>参考文档或搜索解决方案</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何二次开发生成的项目?">
          <ol>
            <li>先运行项目,了解整体结构</li>
            <li>阅读代码注释,理解实现逻辑</li>
            <li>根据需求修改或添加功能</li>
            <li>遵循项目的代码风格</li>
          </ol>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ContactsOutlined class="title-icon" />
        其他问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="平台是否免费?">
          <p>平台完全免费使用。通过积分系统,用户可以免费生成应用。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="数据安全吗?">
          <p>我们重视用户数据安全:</p>
          <ul>
            <li>密码经过加密存储</li>
            <li>生成的代码只有你可以访问</li>
            <li>定期备份数据</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="如何联系客服?">
          <p>如有问题,可以通过以下方式联系:</p>
          <ul>
            <li>访问 GitHub 仓库提 Issue</li>
            <li>发送邮件到 support@example.com</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="未来会有新功能吗?">
          <p>我们会持续优化和更新平台,计划中的功能包括:</p>
          <ul>
            <li>一键部署到云端</li>
            <li>更多生成类型</li>
            <li>团队协作功能</li>
            <li>代码模板库</li>
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
        <a-button href="mailto:support@example.com">
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
