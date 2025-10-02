<template>
  <div class="doc-page">
    <h1 class="doc-title">在线编辑代码</h1>
    <p class="doc-intro">
      使用内置编辑器修改和优化生成的代码,实时查看效果。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <EditOutlined class="title-icon" />
        进入编辑器
      </h2>

      <a-steps direction="vertical" :current="3">
        <a-step title="生成代码">
          <template #description>
            完成代码生成后,会自动显示预览和代码
          </template>
        </a-step>

        <a-step title="打开编辑器">
          <template #description>
            点击"编辑代码"按钮进入在线编辑器
          </template>
        </a-step>

        <a-step title="开始编辑">
          <template #description>
            在编辑器中修改代码,实时查看效果
          </template>
        </a-step>
      </a-steps>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        编辑器功能
      </h2>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :md="12">
          <a-card title="代码编辑" size="small">
            <ul>
              <li>语法高亮</li>
              <li>代码补全</li>
              <li>错误提示</li>
              <li>代码折叠</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card title="快捷操作" size="small">
            <ul>
              <li>撤销/重做</li>
              <li>查找替换</li>
              <li>代码格式化</li>
              <li>全屏模式</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card title="文件管理" size="small">
            <ul>
              <li>文件树导航</li>
              <li>多标签页</li>
              <li>新建/删除文件</li>
              <li>文件重命名</li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card title="实时预览" size="small">
            <ul>
              <li>自动刷新</li>
              <li>控制台输出</li>
              <li>响应式预览</li>
              <li>错误显示</li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ThunderboltOutlined class="title-icon" />
        快捷键列表
      </h2>

      <a-table :dataSource="shortcuts" :columns="shortcutColumns" :pagination="false" size="small" bordered>
      </a-table>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <BulbOutlined class="title-icon" />
        编辑技巧
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="如何快速定位代码?">
          <ul>
            <li>使用 Ctrl+P 快速打开文件</li>
            <li>使用 Ctrl+G 跳转到指定行</li>
            <li>使用 Ctrl+F 在当前文件中查找</li>
            <li>双击变量名高亮所有相同内容</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="如何保证代码质量?">
          <ul>
            <li>使用代码格式化功能保持一致风格</li>
            <li>注意编辑器的错误提示</li>
            <li>修改后及时预览效果</li>
            <li>定期保存避免丢失修改</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="修改未生效怎么办?">
          <p>如果修改后预览未更新:</p>
          <ol>
            <li>确认已保存修改(Ctrl+S)</li>
            <li>手动刷新预览窗口</li>
            <li>检查是否有语法错误</li>
            <li>清除浏览器缓存后重试</li>
          </ol>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="如何导出修改后的代码?">
          <ol>
            <li>点击"保存"按钮保存所有修改</li>
            <li>点击"下载"按钮导出代码</li>
            <li>选择导出格式(单文件/压缩包)</li>
            <li>下载到本地使用</li>
          </ol>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  EditOutlined,
  ToolOutlined,
  ThunderboltOutlined,
  BulbOutlined
} from '@ant-design/icons-vue'

const shortcuts = [
  { action: '保存', windows: 'Ctrl + S', mac: 'Cmd + S' },
  { action: '撤销', windows: 'Ctrl + Z', mac: 'Cmd + Z' },
  { action: '重做', windows: 'Ctrl + Y', mac: 'Cmd + Shift + Z' },
  { action: '查找', windows: 'Ctrl + F', mac: 'Cmd + F' },
  { action: '替换', windows: 'Ctrl + H', mac: 'Cmd + Option + F' },
  { action: '格式化', windows: 'Shift + Alt + F', mac: 'Shift + Option + F' },
  { action: '快速打开', windows: 'Ctrl + P', mac: 'Cmd + P' },
  { action: '跳转到行', windows: 'Ctrl + G', mac: 'Cmd + G' },
]

const shortcutColumns = [
  { title: '操作', dataIndex: 'action', key: 'action' },
  { title: 'Windows', dataIndex: 'windows', key: 'windows' },
  { title: 'Mac', dataIndex: 'mac', key: 'mac' },
]
</script>

<style scoped>
@import '../features/doc-common.css';

/* 快捷键表格样式优化 */
:deep(.ant-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  border: none;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f7ff;
}
</style>
