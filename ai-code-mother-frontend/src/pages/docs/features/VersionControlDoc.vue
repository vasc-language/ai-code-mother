<template>
  <div class="doc-page">
    <h1 class="doc-title">版本管理</h1>
    <p class="doc-intro">
      完整的版本控制系统,自动记录每次修改,支持版本对比和一键回滚。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <HistoryOutlined class="title-icon" />
        版本记录
      </h2>

      <p class="section-content">
        系统会在以下情况自动创建版本记录:
      </p>
      <a-timeline>
        <a-timeline-item color="green">
          <strong>生成完成时</strong><br>
          AI 生成代码完成后自动保存为初始版本
        </a-timeline-item>
        <a-timeline-item color="blue">
          <strong>手动保存时</strong><br>
          在编辑器中修改代码并保存
        </a-timeline-item>
        <a-timeline-item color="purple">
          <strong>部署时</strong><br>
          部署到云端时会创建部署版本快照
        </a-timeline-item>
      </a-timeline>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <DiffOutlined class="title-icon" />
        版本对比
      </h2>

      <p class="section-content">
        查看不同版本之间的差异:
      </p>
      <ul class="content-list">
        <li>并排对比两个版本的代码</li>
        <li>高亮显示新增、删除和修改的内容</li>
        <li>支持逐行对比和全文对比</li>
        <li>查看版本创建时间和备注信息</li>
      </ul>

      <a-card title="版本对比示例" style="margin-top: 16px;">
        <div style="display: flex; gap: 16px;">
          <div style="flex: 1;">
            <a-tag color="red">删除行</a-tag>
            <pre style="background: #fff1f0; padding: 8px; margin-top: 8px;">
&lt;div class="old-content"&gt;
  旧的内容
&lt;/div&gt;</pre>
          </div>
          <div style="flex: 1;">
            <a-tag color="green">新增行</a-tag>
            <pre style="background: #f6ffed; padding: 8px; margin-top: 8px;">
&lt;div class="new-content"&gt;
  新的内容
&lt;/div&gt;</pre>
          </div>
        </div>
      </a-card>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <RollbackOutlined class="title-icon" />
        版本回滚
      </h2>

      <a-steps direction="vertical" :current="3">
        <a-step title="选择版本">
          <template #description>
            在版本历史列表中选择要回滚的目标版本
          </template>
        </a-step>
        <a-step title="预览对比">
          <template #description>
            查看当前版本与目标版本的差异
          </template>
        </a-step>
        <a-step title="确认回滚">
          <template #description>
            点击"回滚到此版本"按钮
          </template>
        </a-step>
        <a-step title="完成">
          <template #description>
            代码已恢复到选定版本,当前操作也会被记录为新版本
          </template>
        </a-step>
      </a-steps>

      <a-alert
        message="重要提示"
        type="warning"
        show-icon
        style="margin-top: 16px;"
      >
        <template #description>
          回滚操作会创建一个新版本,原有版本不会被删除,可以随时再次回滚
        </template>
      </a-alert>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        常见问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="最多保存多少个版本?">
          <p>每个项目最多保存 50 个历史版本。超过后会自动删除最早的版本(初始版本除外)。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="版本可以添加备注吗?">
          <p>可以。保存时可以添加版本备注,方便日后查找和识别。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="可以删除某个版本吗?">
          <p>可以删除非当前版本和非初始版本的历史记录。删除后无法恢复,请谨慎操作。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="版本数据会占用空间吗?">
          <p>是的。版本历史会占用存储空间,但我们使用了增量存储技术,只保存版本间的差异,大大减少了空间占用。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  HistoryOutlined,
  DiffOutlined,
  RollbackOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
@import './doc-common.css';
</style>
