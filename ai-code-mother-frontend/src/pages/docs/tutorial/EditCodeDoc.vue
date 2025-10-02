<template>
  <div class="doc-page">
    <h1 class="doc-title">在线编辑代码</h1>
    <p class="doc-intro">
      通过 AI 对话直接修改已生成的网站,无需下载代码,点击元素即可编辑。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <EditOutlined class="title-icon" />
        编辑模式介绍
      </h2>

      <a-alert
        message="💡 核心特性"
        description="AI Code Mother 支持直接在浏览器中修改已生成的网站,无需任何代码编辑器"
        type="info"
        show-icon
        style="margin-bottom: 24px;"
      />

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="8">
          <a-card hoverable class="feature-card">
            <div class="feature-content">
              <div class="feature-icon">🎯</div>
              <h4>可视化选择</h4>
              <p>直接点击网站中的元素</p>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card hoverable class="feature-card">
            <div class="feature-content">
              <div class="feature-icon">💬</div>
              <h4>对话式编辑</h4>
              <p>用自然语言描述修改需求</p>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card hoverable class="feature-card">
            <div class="feature-content">
              <div class="feature-icon">⚡</div>
              <h4>实时预览</h4>
              <p>修改后立即查看效果</p>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <RocketOutlined class="title-icon" />
        完整操作流程
      </h2>

      <a-steps direction="vertical" :current="5">
        <a-step title="1. 生成网站">
          <template #description>
            首先通过 AI 生成一个网站(HTML、Vue 或多文件项目)
          </template>
        </a-step>

        <a-step title="2. 点击编辑模式">
          <template #description>
            在应用对话页面,点击"启用编辑模式"按钮
          </template>
        </a-step>

        <a-step title="3. 选择要修改的元素">
          <template #description>
            在网站预览区域点击任意元素,系统会高亮显示并提取元素信息
          </template>
        </a-step>

        <a-step title="4. 描述修改需求">
          <template #description>
            在输入框中用自然语言描述如何修改该元素
          </template>
        </a-step>

        <a-step title="5. 查看修改结果">
          <template #description>
            AI 会更新代码,预览区域会自动刷新显示新效果
          </template>
        </a-step>
      </a-steps>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        可以修改什么?
      </h2>

      <a-tabs>
        <a-tab-pane key="1" tab="文本内容">
          <div class="tab-content">
            <h4>修改网页中的文字</h4>
            <p class="example-title">✅ 推荐的修改描述:</p>
            <ul class="example-list">
              <li>"把这个标题改成'欢迎来到我的博客'"</li>
              <li>"将按钮文字改为'立即开始'"</li>
              <li>"修改这段描述文字为..."</li>
              <li>"把这里的数字改成1000"</li>
            </ul>
          </div>
        </a-tab-pane>

        <a-tab-pane key="2" tab="样式设计">
          <div class="tab-content">
            <h4>调整元素的外观</h4>
            <p class="example-title">✅ 推荐的修改描述:</p>
            <ul class="example-list">
              <li>"把这个按钮改成红色"</li>
              <li>"增大标题字体"</li>
              <li>"给这个区域加上圆角和阴影"</li>
              <li>"调整这个元素的边距和内边距"</li>
            </ul>
          </div>
        </a-tab-pane>

        <a-tab-pane key="3" tab="布局结构">
          <div class="tab-content">
            <h4>改变元素的位置和排列</h4>
            <p class="example-title">✅ 推荐的修改描述:</p>
            <ul class="example-list">
              <li>"把这个元素移到左边"</li>
              <li>"将这些按钮横向排列"</li>
              <li>"调整这个区域的宽度为50%"</li>
              <li>"让这个元素居中显示"</li>
            </ul>
          </div>
        </a-tab-pane>

        <a-tab-pane key="4" tab="添加删除">
          <div class="tab-content">
            <h4>添加新元素或删除现有元素</h4>
            <p class="example-title">✅ 推荐的修改描述:</p>
            <ul class="example-list">
              <li>"删除这个元素"</li>
              <li>"在这里添加一个搜索框"</li>
              <li>"在这个按钮旁边再加一个'取消'按钮"</li>
              <li>"移除这个元素的背景图片"</li>
            </ul>
          </div>
        </a-tab-pane>
      </a-tabs>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        使用技巧
      </h2>

      <a-card title="🎯 如何准确选择元素?" style="margin-bottom: 16px;">
        <ul class="tip-list">
          <li><strong>点击前先观察:</strong> 在预览区域将鼠标悬停在元素上,会显示高亮边框</li>
          <li><strong>查看元素信息:</strong> 选中后会在输入框上方显示元素的标签、ID、类名等信息</li>
          <li><strong>重新选择:</strong> 如果选错了,可以点击信息框右上角的关闭按钮,重新选择</li>
          <li><strong>复杂页面:</strong> 对于嵌套较深的元素,可能需要多点几次才能选中目标元素</li>
        </ul>
      </a-card>

      <a-card title="💬 如何描述修改需求?" style="margin-bottom: 16px;">
        <div class="comparison-container">
          <div class="comparison-section good">
            <h4>✅ 清晰明确的描述</h4>
            <ul>
              <li>"把标题颜色改成深蓝色 #1a73e8"</li>
              <li>"将按钮宽度设置为 200px"</li>
              <li>"修改文字为'立即购买',颜色改为红色"</li>
            </ul>
          </div>
          <div class="comparison-section bad">
            <h4>❌ 模糊不清的描述</h4>
            <ul>
              <li>"改一下颜色"(没说改什么颜色)</li>
              <li>"调整大小"(没说具体数值)</li>
              <li>"美化一下"(太抽象)</li>
            </ul>
          </div>
        </div>
      </a-card>

      <a-card title="⚡ 提高修改效率的方法">
        <ul class="tip-list">
          <li><strong>一次修改一个元素:</strong> 不要同时描述多个元素的修改,分步骤进行</li>
          <li><strong>先测试再批量:</strong> 对于重复性修改,先在一个元素上测试效果</li>
          <li><strong>保留成功案例:</strong> 下载修改后的代码,避免意外丢失</li>
          <li><strong>使用具体数值:</strong> 颜色、尺寸、位置等尽量给出具体数值</li>
        </ul>
      </a-card>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        常见问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="为什么点击元素没有反应?">
          <p>可能的原因:</p>
          <ol>
            <li><strong>未开启编辑模式:</strong> 检查是否点击了"启用编辑模式"按钮</li>
            <li><strong>元素不可点击:</strong> 某些元素(如 iframe 内容)可能无法直接选择</li>
            <li><strong>网站未生成完成:</strong> 等待代码生成完成后再开启编辑模式</li>
            <li><strong>浏览器兼容性:</strong> 建议使用 Chrome 或 Edge 浏览器</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="修改后效果不符合预期怎么办?">
          <p>解决方案:</p>
          <ul>
            <li><strong>重新描述需求:</strong> 更详细地说明你想要的效果</li>
            <li><strong>分步修改:</strong> 将复杂的修改拆分成多个简单步骤</li>
            <li><strong>提供参考:</strong> 描述时可以参考具体的颜色值、尺寸等</li>
            <li><strong>下载代码手动调整:</strong> 复杂的修改可以下载后在本地编辑器中精细调整</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="可以撤销修改吗?">
          <p>
            目前系统不支持直接撤销单次修改,但你可以:
          </p>
          <ul>
            <li>通过对话要求 AI "恢复到上一个版本"</li>
            <li>查看"历史版本"功能,选择之前的版本</li>
            <li>重新生成一个新的应用</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="可以修改 JavaScript 功能吗?">
          <p>
            目前编辑模式主要支持:
          </p>
          <ul>
            <li>✅ 文本内容修改</li>
            <li>✅ 样式(CSS)修改</li>
            <li>✅ 简单的 HTML 结构调整</li>
            <li>⚠️ JavaScript 功能修改支持有限,复杂的逻辑建议下载代码后手动修改</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="5" header="修改会消耗积分吗?">
          <p>
            是的,每次通过对话进行修改都会消耗积分:
          </p>
          <ul>
            <li>HTML 页面修改: 6 积分/次</li>
            <li>Vue 应用修改: 15 积分/次</li>
            <li>多文件项目修改: 9 积分/次</li>
          </ul>
          <a-alert
            message="建议一次性描述完整的修改需求,避免多次对话产生重复消耗"
            type="warning"
            show-icon
            style="margin-top: 12px;"
          />
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ThunderboltOutlined class="title-icon" />
        高级技巧
      </h2>

      <a-card title="🎨 批量修改相似元素" style="margin-bottom: 16px;">
        <p>如果页面中有多个相似的元素需要修改:</p>
        <ol>
          <li>选中其中一个元素</li>
          <li>在描述中说明"把所有类似的XX也改成..."</li>
          <li>AI 会尝试识别并修改所有相同类型的元素</li>
        </ol>
        <p class="example-text">
          例如: "把这个按钮改成绿色,并且把页面中所有的按钮都改成相同样式"
        </p>
      </a-card>

      <a-card title="🔄 组合修改" style="margin-bottom: 16px;">
        <p>对同一个元素进行多项修改:</p>
        <p class="example-text">
          例如: "把这个标题改成'最新优惠',颜色改成红色,字号调大,并且加粗显示"
        </p>
      </a-card>

      <a-card title="📱 响应式调整">
        <p>要求 AI 考虑移动端显示:</p>
        <p class="example-text">
          例如: "把这个菜单在手机上改成汉堡菜单样式"
        </p>
      </a-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  EditOutlined,
  RocketOutlined,
  ToolOutlined,
  ThunderboltOutlined,
  BulbOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import '../features/doc-common.css';

.feature-card {
  height: 100%;
  text-align: center;
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.feature-icon {
  font-size: 48px;
  line-height: 1;
}

.feature-content h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.feature-content p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.tab-content {
  padding: 16px;
}

.tab-content h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
}

.example-title {
  margin: 12px 0 8px 0;
  font-weight: 600;
  color: #333;
}

.example-list {
  margin: 0;
  padding-left: 24px;
  line-height: 2;
  color: #666;
}

.example-list li {
  margin: 8px 0;
  font-style: italic;
}

.tip-list {
  margin: 0;
  padding-left: 0;
  list-style: none;
}

.tip-list li {
  margin: 12px 0;
  padding-left: 24px;
  position: relative;
  line-height: 1.8;
  color: #555;
}

.tip-list li::before {
  content: '▸';
  position: absolute;
  left: 8px;
  color: #1890ff;
  font-weight: bold;
}

.comparison-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}

.comparison-section {
  padding: 16px;
  border-radius: 8px;
  border: 2px solid;
}

.comparison-section.good {
  background: #f6ffed;
  border-color: #52c41a;
}

.comparison-section.bad {
  background: #fff2e8;
  border-color: #fa8c16;
}

.comparison-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
}

.comparison-section ul {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.comparison-section li {
  margin: 8px 0;
  font-size: 13px;
}

.example-text {
  color: #666;
  font-size: 14px;
  font-style: italic;
  display: block;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-left: 3px solid #1890ff;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .comparison-container {
    grid-template-columns: 1fr;
  }
}
</style>
