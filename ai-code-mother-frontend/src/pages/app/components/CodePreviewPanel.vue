<template>
  <div class="code-preview-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <h3 class="panel-title">{{ panelTitle }}</h3>
      <div class="panel-actions">
        <a-button v-if="showEditButton" type="link" size="small" @click="$emit('toggle-edit')">
          {{ isEditMode ? '退出编辑' : '编辑页面' }}
        </a-button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="panel-content">
      <!-- 预览模式 -->
      <div v-if="showPreview" class="preview-container">
        <iframe
          v-if="previewUrl"
          :src="previewUrl"
          class="preview-iframe"
          :class="{ 'edit-mode': isEditMode }"
          @load="$emit('preview-loaded')"
        />
        <div v-else class="empty-preview">
          <a-empty description="暂无预览内容" />
        </div>
      </div>

      <!-- 代码模式 - HTML -->
      <div v-else-if="codeGenType === 'HTML' && simpleCodeFile" class="code-container">
        <CodeHighlight :code="simpleCodeFile.content" :language="simpleCodeFile.language" />
      </div>

      <!-- 代码模式 - 多文件 -->
      <div v-else-if="codeGenType === 'MULTI_FILE' && multiFiles.length > 0" class="code-container">
        <a-tabs v-model:activeKey="activeFileKey" type="card" class="file-tabs">
          <a-tab-pane v-for="file in multiFiles" :key="file.id" :tab="file.name">
            <CodeHighlight :code="file.content" :language="file.language" />
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <a-empty description="开始对话以生成代码" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CodeHighlight from '@/components/CodeHighlight.vue'

interface GeneratedFile {
  id: string
  name: string
  content: string
  language: string
}

interface Props {
  codeGenType?: string
  showPreview?: boolean
  previewUrl?: string
  isEditMode?: boolean
  simpleCodeFile?: GeneratedFile | null
  multiFiles?: GeneratedFile[]
  activeFileKey?: string
  showEditButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  multiFiles: () => [],
  showEditButton: true,
})

defineEmits<{
  'toggle-edit': []
  'preview-loaded': []
  'update:activeFileKey': [key: string]
}>()

const panelTitle = computed(() => {
  if (props.showPreview) {
    return '预览'
  }
  if (props.codeGenType === 'HTML') {
    return 'HTML代码'
  }
  if (props.codeGenType === 'MULTI_FILE') {
    return '生成的文件'
  }
  return '代码输出'
})
</script>

<style scoped>
.code-preview-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.panel-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.preview-container,
.code-container,
.empty-state {
  height: 100%;
  width: 100%;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-iframe.edit-mode {
  pointer-events: auto;
  cursor: crosshair;
}

.empty-preview,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.code-container {
  overflow: auto;
  height: 100%;
}

.file-tabs {
  height: 100%;
}

:deep(.file-tabs .ant-tabs-content) {
  height: calc(100% - 46px);
  overflow: auto;
}
</style>
