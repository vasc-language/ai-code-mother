<template>
  <div class="code-panel-lovable">
    <!-- ========== 预览模式：全屏独立页面 ========== -->
    <div v-if="currentView === 'preview'" class="preview-mode">
      <div v-if="showPreview && previewUrl" class="preview-container">
        <iframe
          :src="previewUrl"
          class="preview-iframe"
          frameborder="0"
          @load="$emit('preview-loaded')"
        />
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">
          <EyeOutlined />
        </div>
        <p class="empty-text">暂无预览内容</p>
        <p class="empty-hint">开始对话以生成代码</p>
      </div>
    </div>

    <!-- ========== 代码模式：VSCode风格左右分栏 ========== -->
    <div v-else-if="currentView === 'code'" class="code-mode">
      <!-- 左侧：文件树 -->
      <div class="file-explorer" :style="{ width: explorerWidth + 'px' }">
        <!-- 文件树头部 -->
        <div class="explorer-header">
          <div class="header-title">
            <FolderOutlined class="title-icon" />
            <span>资源管理器</span>
          </div>
        </div>

        <!-- 文件树内容 -->
        <div class="explorer-content">
          <!-- 项目名称 -->
          <div v-if="hasFiles" class="project-root">
            <FolderOpenOutlined class="root-icon" />
            <span class="root-name">{{ projectName }}</span>
          </div>

          <!-- 文件列表 -->
          <div class="file-list">
            <!-- HTML 单文件 -->
            <div
              v-if="codeGenType === 'HTML' && simpleCodeFile"
              class="file-item"
              :class="{ active: true }"
              @click="handleFileClick(simpleCodeFile.id)"
            >
              <FileTextOutlined class="file-icon" />
              <span class="file-name">{{ simpleCodeFile.name || 'index.html' }}</span>
            </div>

            <!-- 多文件项目 -->
            <div
              v-for="file in multiFiles"
              :key="file.id"
              class="file-item"
              :class="{ active: activeFileKey === file.id }"
              @click="handleFileClick(file.id)"
            >
              <component :is="getFileIcon(file.name)" class="file-icon" />
              <span class="file-name">{{ file.name }}</span>
            </div>

            <!-- 空状态 -->
            <div v-if="!hasFiles" class="explorer-empty">
              <CodeOutlined class="empty-icon-small" />
              <p class="empty-text-small">等待文件生成</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 竖向分隔条 -->
      <div
        class="vertical-resizer"
        @mousedown="startResize"
        @dblclick="resetSize"
      >
        <div class="resizer-handle"></div>
      </div>

      <!-- 右侧：代码编辑器 -->
      <div class="code-editor-area">
        <!-- 文件标签栏 -->
        <div v-if="hasFiles" class="file-tabs">
          <!-- HTML 单文件 -->
          <div
            v-if="codeGenType === 'HTML' && simpleCodeFile"
            class="file-tab active"
          >
            <FileTextOutlined class="tab-icon" />
            <span class="tab-name">{{ simpleCodeFile.name || 'index.html' }}</span>
          </div>

          <!-- 多文件项目 -->
          <div
            v-for="file in multiFiles"
            :key="file.id"
            class="file-tab"
            :class="{ active: activeFileKey === file.id }"
            @click="$emit('update:activeFileKey', file.id)"
          >
            <component :is="getFileIcon(file.name)" class="tab-icon" />
            <span class="tab-name">{{ file.name }}</span>
            <CloseOutlined class="tab-close" />
          </div>
        </div>

        <!-- 代码编辑器主体 -->
        <div class="editor-main">
          <!-- HTML 单文件 -->
          <div v-if="codeGenType === 'HTML' && simpleCodeFile" class="editor-container">
            <!-- 工具栏 -->
            <div class="editor-toolbar">
              <div class="file-info">
                <span class="file-path">{{ simpleCodeFile.name || 'index.html' }}</span>
                <span class="file-stats">
                  {{ getLineCount(simpleCodeFile.content) }} 行 ·
                  {{ getFileSize(simpleCodeFile.content) }}
                </span>
              </div>
              <button class="copy-btn" @click="copyCode(simpleCodeFile.content)" title="复制代码">
                <CopyOutlined />
                <span>复制</span>
              </button>
            </div>

            <!-- 代码内容 -->
            <div class="code-content">
              <CodeHighlight
                :code="simpleCodeFile.content"
                :language="simpleCodeFile.language || 'html'"
              />
            </div>
          </div>

          <!-- 多文件项目 -->
          <div v-else-if="codeGenType === 'MULTI_FILE' && currentFile" class="editor-container">
            <!-- 工具栏 -->
            <div class="editor-toolbar">
              <div class="file-info">
                <span class="file-path">{{ currentFile.name }}</span>
                <span class="file-stats">
                  {{ getLineCount(currentFile.content) }} 行 ·
                  {{ getFileSize(currentFile.content) }}
                </span>
              </div>
              <button class="copy-btn" @click="copyCode(currentFile.content)" title="复制代码">
                <CopyOutlined />
                <span>复制</span>
              </button>
            </div>

            <!-- 代码内容 -->
            <div class="code-content">
              <CodeHighlight :code="currentFile.content" :language="currentFile.language" />
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">
              <CodeOutlined />
            </div>
            <p class="empty-text">等待代码生成</p>
            <p class="empty-hint">AI 正在为你创建精彩的代码</p>
          </div>
        </div>

        <!-- 状态栏 -->
        <div v-if="hasFiles" class="status-bar">
          <div class="status-left">
            <span class="status-item">
              <BranchesOutlined class="status-icon" />
              main
            </span>
          </div>
          <div class="status-right">
            <span class="status-item" v-if="currentFile || simpleCodeFile">
              LF
            </span>
            <span class="status-item" v-if="currentFile || simpleCodeFile">
              {{ (currentFile || simpleCodeFile)?.language || 'plaintext' }}
            </span>
            <span class="status-item" v-if="currentFile || simpleCodeFile">
              UTF-8
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  CodeOutlined,
  CopyOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  EyeOutlined,
  CloseOutlined,
  BranchesOutlined,
} from '@ant-design/icons-vue'
import CodeHighlight from '@/components/CodeHighlight.vue'

interface GeneratedFile {
  id: string
  name: string
  content: string
  language: string
  modified?: boolean
}

interface Props {
  currentView: 'preview' | 'code'
  codeGenType?: string
  showPreview?: boolean
  previewUrl?: string
  simpleCodeFile?: GeneratedFile | null
  multiFiles?: GeneratedFile[]
  activeFileKey?: string
  generationFinished?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentView: 'preview',
  showPreview: false,
  multiFiles: () => [],
  generationFinished: false,
})

const emit = defineEmits<{
  'preview-loaded': []
  'update:activeFileKey': [key: string]
}>()

// ========== 左侧文件树宽度调节 ==========
const explorerWidth = ref(248) // 默认宽度 248px (参考VSCode)
const isResizing = ref(false)
const startX = ref(0)
const startWidth = ref(0)

const startResize = (e: MouseEvent) => {
  isResizing.value = true
  startX.value = e.clientX
  startWidth.value = explorerWidth.value

  const onMouseMove = (e: MouseEvent) => {
    if (!isResizing.value) return
    const delta = e.clientX - startX.value
    const newWidth = Math.max(180, Math.min(500, startWidth.value + delta))
    explorerWidth.value = newWidth
  }

  const onMouseUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const resetSize = () => {
  explorerWidth.value = 248
}

// ========== 文件处理 ==========
const currentFile = computed(() => {
  return props.multiFiles.find((f) => f.id === props.activeFileKey)
})

const hasFiles = computed(() => {
  return (props.codeGenType === 'HTML' && props.simpleCodeFile) ||
         (props.codeGenType === 'MULTI_FILE' && props.multiFiles.length > 0)
})

const projectName = computed(() => {
  if (props.codeGenType === 'HTML') return 'html-project'
  if (props.codeGenType === 'MULTI_FILE') return 'multi-file-project'
  return 'project'
})

const handleFileClick = (fileId: string) => {
  emit('update:activeFileKey', fileId)
}

// 获取文件图标
const getFileIcon = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  const iconMap: Record<string, any> = {
    html: FileTextOutlined,
    css: FileTextOutlined,
    js: CodeOutlined,
    ts: CodeOutlined,
    vue: FileTextOutlined,
    json: CodeOutlined,
  }
  return iconMap[ext || ''] || FileTextOutlined
}

// 获取文件行数
const getLineCount = (content: string) => {
  return content.split('\n').length
}

// 获取文件大小
const getFileSize = (content: string) => {
  const bytes = new Blob([content]).size
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// 复制代码
const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    message.success('代码已复制到剪贴板')
  } catch (err) {
    message.error('复制失败')
  }
}
</script>

<style scoped>
.code-panel-lovable {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* ========== 预览模式：全屏独立页面 ========== */
.preview-mode {
  width: 100%;
  height: 100%;
  background: white;
}

.preview-container {
  width: 100%;
  height: 100%;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* ========== 代码模式：VSCode风格左右分栏 ========== */
.code-mode {
  display: flex;
  height: 100%;
  background: #1E1E1E;
  color: #D4D4D4;
}

/* ========== 左侧：文件树 ========== */
.file-explorer {
  display: flex;
  flex-direction: column;
  background: #252526;
  border-right: 1px solid #3E3E42;
  overflow: hidden;
  flex-shrink: 0;
}

.explorer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 35px;
  padding: 0 12px;
  background: #252526;
  border-bottom: 1px solid #3E3E42;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  color: #CCCCCC;
  letter-spacing: 0.5px;
}

.title-icon {
  font-size: 14px;
  color: var(--color-primary);
}

.explorer-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.project-root {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 12px;
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #CCCCCC;
  cursor: pointer;
  user-select: none;
}

.project-root:hover {
  background: #2A2D2E;
}

.root-icon {
  font-size: 16px;
  color: var(--color-primary);
}

.root-name {
  font-family: 'Consolas', 'Courier New', monospace;
}

.file-list {
  display: flex;
  flex-direction: column;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 28px;
  padding: 0 12px 0 28px;
  cursor: pointer;
  transition: background 0.15s ease;
  user-select: none;
}

.file-item:hover {
  background: #2A2D2E;
}

.file-item.active {
  background: linear-gradient(90deg,
    rgba(255, 107, 53, 0.15) 0%,
    rgba(255, 140, 66, 0.08) 100%);
  color: #FFFFFF;
}

.file-icon {
  font-size: 16px;
  color: #CCCCCC;
  flex-shrink: 0;
}

.file-name {
  flex: 1;
  font-size: 13px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: #CCCCCC;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-item.active .file-name {
  color: #FFFFFF;
}

.explorer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #6A6A6A;
}

.empty-icon-small {
  font-size: 24px;
  margin-bottom: 8px;
  opacity: 0.5;
}

.empty-text-small {
  font-size: 12px;
  color: #858585;
}

/* ========== 竖向分隔条 ========== */
.vertical-resizer {
  width: 4px;
  background: #3E3E42;
  cursor: col-resize;
  position: relative;
  transition: background 0.2s ease;
  flex-shrink: 0;
}

.vertical-resizer:hover {
  background: linear-gradient(180deg,
    rgba(255, 107, 53, 0.3) 0%,
    rgba(255, 140, 66, 0.3) 100%);
}

.resizer-handle {
  position: absolute;
  top: 0;
  left: -2px;
  right: -2px;
  bottom: 0;
}

/* ========== 右侧：代码编辑器区域 ========== */
.code-editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #1E1E1E;
}

/* 文件标签栏 */
.file-tabs {
  display: flex;
  background: #252526;
  border-bottom: 1px solid #3E3E42;
  overflow-x: auto;
  overflow-y: hidden;
  height: 35px;
  flex-shrink: 0;
}

.file-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 35px;
  padding: 0 12px;
  background: #2D2D30;
  color: #969696;
  border-right: 1px solid #3E3E42;
  font-size: 13px;
  font-family: 'Consolas', 'Courier New', monospace;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  user-select: none;
  position: relative;
}

.file-tab:hover {
  background: #1E1E1E;
  color: #FFFFFF;
}

.file-tab.active {
  background: #1E1E1E;
  color: #FFFFFF;
  border-bottom: 2px solid var(--color-primary);
}

.tab-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.tab-name {
  font-size: 13px;
}

.tab-close {
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.file-tab:hover .tab-close {
  opacity: 0.7;
}

.tab-close:hover {
  opacity: 1 !important;
}

/* 编辑器主体 */
.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 16px;
  background: #252526;
  border-bottom: 1px solid #3E3E42;
  flex-shrink: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-path {
  font-size: 12px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: #CCCCCC;
}

.file-stats {
  font-size: 11px;
  color: #858585;
  font-family: 'Consolas', 'Courier New', monospace;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: transparent;
  color: #858585;
  border: 1px solid #3E3E42;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: #2D2D30;
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.code-content {
  flex: 1;
  overflow: auto;
  background: #1E1E1E;
}

/* 状态栏 */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 22px;
  padding: 0 12px;
  background: #007ACC;
  font-size: 12px;
  color: #FFFFFF;
  flex-shrink: 0;
}

.status-left,
.status-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'Consolas', 'Courier New', monospace;
}

.status-icon {
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6A6A6A;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #858585;
}

.empty-hint {
  font-size: 14px;
  color: #6A6A6A;
}

/* 滚动条样式 */
.explorer-content::-webkit-scrollbar,
.file-tabs::-webkit-scrollbar,
.code-content::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.explorer-content::-webkit-scrollbar-track,
.file-tabs::-webkit-scrollbar-track,
.code-content::-webkit-scrollbar-track {
  background: #1E1E1E;
}

.explorer-content::-webkit-scrollbar-thumb,
.file-tabs::-webkit-scrollbar-thumb,
.code-content::-webkit-scrollbar-thumb {
  background: #424242;
  border-radius: 5px;
}

.explorer-content::-webkit-scrollbar-thumb:hover,
.file-tabs::-webkit-scrollbar-thumb:hover,
.code-content::-webkit-scrollbar-thumb:hover {
  background: #4E4E4E;
}
</style>
