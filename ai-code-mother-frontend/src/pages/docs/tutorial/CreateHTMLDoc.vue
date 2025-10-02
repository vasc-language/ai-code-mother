<template>
  <div class="doc-page">
    <h1 class="doc-title">生成 HTML 页面</h1>
    <p class="doc-intro">
      学习如何使用 AI 快速生成单页 HTML 页面,适合简单的展示页面和落地页。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <FileTextOutlined class="title-icon" />
        适用场景
      </h2>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 个人简历页面
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 产品介绍页
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 活动落地页
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card size="small">
            <CheckCircleOutlined style="color: #52c41a;" /> 简单表单页
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        操作步骤
      </h2>

      <a-steps direction="vertical" :current="5">
        <a-step title="登录系统">
          <template #description>
            <p>登录账号,确保有足够积分(至少 10 积分)</p>
          </template>
        </a-step>

        <a-step title="选择生成类型">
          <template #description>
            <p>在主页选择"HTML 页面"生成类型</p>
          </template>
        </a-step>

        <a-step title="描述需求">
          <template #description>
            <p>在输入框中描述你的需求,例如:</p>
            <blockquote class="example-quote">
              "生成一个个人简历页面,包含顶部导航栏、个人信息区(姓名、照片、联系方式)、
              工作经历、教育背景、技能列表和底部版权信息。使用蓝色主题色,简洁现代的设计风格。"
            </blockquote>
          </template>
        </a-step>

        <a-step title="开始生成">
          <template #description>
            <p>点击"开始生成"按钮,等待 10-15 秒</p>
          </template>
        </a-step>

        <a-step title="查看结果">
          <template #description>
            <p>生成完成后可以:</p>
            <ul>
              <li>实时预览页面效果</li>
              <li>下载 HTML 文件</li>
              <li>在线编辑修改代码</li>
            </ul>
          </template>
        </a-step>
      </a-steps>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        需求描述示例
      </h2>

      <a-tabs>
        <a-tab-pane key="1" tab="基础示例">
          <blockquote class="example-quote">
            "创建一个产品介绍页面,顶部是导航栏和 Banner,中间展示产品特性(3-4个卡片),
            底部是联系表单和公司信息。"
          </blockquote>
        </a-tab-pane>

        <a-tab-pane key="2" tab="详细示例">
          <blockquote class="example-quote">
            "生成一个活动报名页面,包含:
            <br>1. 顶部导航栏(Logo + 菜单)
            <br>2. 主视觉区(活动标题、时间、地点、大图)
            <br>3. 活动介绍区(富文本内容)
            <br>4. 报名表单(姓名、电话、邮箱、备注)
            <br>5. 底部版权信息
            <br>设计要求:使用橙色主题,活泼明快的风格,适配移动端"
          </blockquote>
        </a-tab-pane>
      </a-tabs>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        进阶技巧
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何让页面更美观?">
          <ul>
            <li>明确设计风格:现代简洁 / 商务正式 / 活泼可爱</li>
            <li>指定主题色:如"使用蓝色作为主色调"</li>
            <li>提及布局方式:响应式、居中、全屏等</li>
            <li>参考优秀网站:可以提供参考链接</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="如何添加交互效果?">
          <p>在需求中明确说明需要的交互:</p>
          <ul>
            <li>"导航栏滚动时固定在顶部"</li>
            <li>"点击按钮有悬浮动画效果"</li>
            <li>"表单提交前验证必填字段"</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="生成后如何使用?">
          <p>生成的 HTML 文件可以:</p>
          <ol>
            <li>直接双击打开在浏览器中查看</li>
            <li>上传到服务器作为静态页面访问</li>
            <li>嵌入到其他网站中使用</li>
            <li>作为模板进一步开发</li>
          </ol>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  FileTextOutlined,
  RocketOutlined,
  BulbOutlined,
  ToolOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import '../features/doc-common.css';

.example-quote {
  background: #f5f5f5;
  border-left: 4px solid #1890ff;
  padding: 12px 16px;
  margin: 12px 0;
  font-style: italic;
  color: #555;
  line-height: 1.8;
}
</style>
