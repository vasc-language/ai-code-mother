<template>
  <div class="code-highlight-container">
    <div class="code-header">
      <div class="file-info">
        <FileOutlined class="file-icon" />
        <span class="file-name">{{ fileName }}</span>
        <span v-if="language" class="language-badge">{{ language.toUpperCase() }}</span>
      </div>
      <div class="code-actions">
        <a-button type="text" size="small" @click="copyCode" class="copy-btn">
          <template #icon>
            <CopyOutlined v-if="!copied" />
            <CheckOutlined v-else />
          </template>
          {{ copied ? '已复制' : '复制' }}
        </a-button>
      </div>
    </div>
    <div class="code-content">
      <pre><code :class="`hljs language-${language}`" v-html="highlightedCode"></code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import hljs from 'highlight.js'
import { FileOutlined, CopyOutlined, CheckOutlined } from '@ant-design/icons-vue'

interface Props {
  code: string
  language?: string
  fileName?: string
  theme?: 'github' | 'vs2015' | 'atom-one-dark' | 'monokai-sublime'
}

const props = defineProps<Props>()

const copied = ref(false)
const currentTheme = ref(props.theme || 'atom-one-dark')

// 计算高亮后的代码
const highlightedCode = computed(() => {
  if (!props.code) return ''
  
  if (props.language && hljs.getLanguage(props.language)) {
    try {
      return hljs.highlight(props.code, { 
        language: props.language, 
        ignoreIllegals: true 
      }).value
    } catch (error) {
      console.warn('代码高亮失败:', error)
    }
  }
  
  // 如果指定语言失败，尝试自动检测
  try {
    return hljs.highlightAuto(props.code).value
  } catch (error) {
    console.warn('自动检测语言失败:', error)
  }
  
  // 最后转义HTML返回
  return props.code
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
})

// 动态加载主题样式
const loadTheme = async (theme: string) => {
  try {
    // 移除旧的主题样式
    const oldTheme = document.querySelector('link[data-highlight-theme]')
    if (oldTheme) {
      oldTheme.remove()
    }
    
    // 加载新的主题样式
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = `https://cdn.jsdelivr.net/npm/highlight.js@11.11.1/styles/${theme}.min.css`
    link.setAttribute('data-highlight-theme', 'true')
    document.head.appendChild(link)
  } catch (error) {
    console.error('加载主题失败:', error)
  }
}

// 复制代码到剪贴板
const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(props.code)
    copied.value = true
    message.success('代码已复制到剪贴板')
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (error) {
    message.error('复制失败，请手动选择复制')
  }
}

// 监听主题变化
watch(() => props.theme, (newTheme) => {
  if (newTheme && newTheme !== currentTheme.value) {
    currentTheme.value = newTheme
    loadTheme(newTheme)
  }
})

onMounted(() => {
  // 初始化主题
  if (props.theme) {
    loadTheme(props.theme)
  }
})
</script>

<style scoped>
.code-highlight-container {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  margin: 8px 0;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #1890ff;
  font-size: 14px;
}

.file-name {
  font-weight: 500;
  color: #333;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.language-badge {
  background: #1890ff;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  text-transform: uppercase;
}

.code-actions {
  display: flex;
  gap: 4px;
}

.copy-btn {
  font-size: 12px;
  padding: 4px 8px;
  height: auto;
}

.code-content {
  position: relative;
  /* 使用透明背景以继承父容器（页面为浅色风格，更协调） */
  background: transparent;
}

.code-content pre {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
  background: transparent;
}

.code-content code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Cascadia Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #abb2bf;
  background: transparent;
}

/* 自定义滚动条样式 */
.code-content pre::-webkit-scrollbar {
  height: 8px;
}

.code-content pre::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.code-content pre::-webkit-scrollbar-thumb {
  background: #4a4a4a;
  border-radius: 4px;
}

.code-content pre::-webkit-scrollbar-thumb:hover {
  background: #5a5a5a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .code-header {
    padding: 8px 12px;
  }
  
  .file-name {
    font-size: 12px;
  }
  
  .language-badge {
    font-size: 9px;
    padding: 1px 4px;
  }
  
  .copy-btn {
    font-size: 11px;
    padding: 3px 6px;
  }
  
  .code-content pre {
    padding: 12px;
  }
  
  .code-content code {
    font-size: 12px;
  }
}
</style>
