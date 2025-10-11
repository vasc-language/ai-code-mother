<template>
  <div class="code-highlight-container">
    <div class="code-content">
      <pre><code :class="`hljs language-${language}`" v-html="highlightedCode"></code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import hljs from 'highlight.js'

interface Props {
  code: string
  language?: string
  fileName?: string
  theme?: 'github' | 'vs2015' | 'atom-one-dark' | 'monokai-sublime'
}

const props = defineProps<Props>()

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
  height: auto;
  overflow: visible;
  background: transparent;
  min-width: max-content;
}

.code-content {
  position: relative;
  height: auto;
  overflow: visible;
  background: transparent;
  min-width: max-content;
}

.code-content pre {
  margin: 0;
  padding: 16px;
  overflow: visible;
  background: transparent;
  height: auto;
  min-width: max-content;
}

.code-content code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Cascadia Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #abb2bf;
  background: transparent;
  display: inline-block;
  min-width: 100%;
}

/* 自定义滚动条样式 */
.code-content pre::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.code-content pre::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.code-content pre::-webkit-scrollbar-thumb {
  background: #424242;
  border-radius: 5px;
}

.code-content pre::-webkit-scrollbar-thumb:hover {
  background: #4E4E4E;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .code-content pre {
    padding: 12px;
  }

  .code-content code {
    font-size: 12px;
  }
}
</style>
