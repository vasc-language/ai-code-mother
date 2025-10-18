<template>
  <div class="code-highlight-container">
    <div class="code-content-wrapper">
      <!-- 行号列 -->
      <div class="line-numbers" aria-hidden="true">
        <span
          v-for="(line, index) in codeLines"
          :key="index"
          class="line-number"
        >{{ index + 1 }}</span>
      </div>
      <!-- 代码内容 -->
      <div class="code-content">
        <pre><code :class="`hljs language-${language}`" v-html="highlightedCode"></code></pre>
      </div>
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

// 计算代码行数组
const codeLines = computed(() => {
  if (!props.code) return []
  return props.code.split('\n')
})

// 计算高亮后的代码
const highlightedCode = computed(() => {
  if (!props.code) return ''

  console.log('[CodeHighlight] 接收到的props:', {
    language: props.language,
    fileName: props.fileName,
    codeLength: props.code.length,
    hasLanguageSupport: props.language ? hljs.getLanguage(props.language) : false
  })

  if (props.language && hljs.getLanguage(props.language)) {
    try {
      const result = hljs.highlight(props.code, {
        language: props.language,
        ignoreIllegals: true
      }).value
      console.log('[CodeHighlight] 高亮成功，使用语言:', props.language)
      return result
    } catch (error) {
      console.warn('[CodeHighlight] 代码高亮失败:', error)
    }
  } else {
    console.warn('[CodeHighlight] 未找到语言或语言不支持:', props.language)
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

.code-content-wrapper {
  display: flex;
  position: relative;
  height: auto;
  overflow: visible;
  background: transparent;
  min-width: max-content;
}

/* 行号样式 - GitHub Light 风格 */
.line-numbers {
  display: flex;
  flex-direction: column;
  background: var(--code-bg, #FFFFFF);
  padding: 16px 0;
  margin: 0;
  user-select: none;
  text-align: right;
  min-width: 50px;
  border-right: 1px solid #E1E4E8;
  flex-shrink: 0;
}

.line-number {
  display: block;
  padding: 0 12px 0 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Cascadia Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #6A737D;
  height: 19.5px; /* 13px * 1.5 = 19.5px */
}

.line-number::before {
  content: attr(data-line);
}

.code-content {
  position: relative;
  height: auto;
  overflow: visible;
  background: transparent;
  flex: 1;
  min-width: 0;
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
  color: var(--code-text, #24292E);
  background: transparent;
  display: inline-block;
  min-width: 100%;
}

/* 自定义滚动条样式 - GitHub Light 风格 */
.code-content pre::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.code-content pre::-webkit-scrollbar-track {
  background: var(--code-bg, #FFFFFF);
}

.code-content pre::-webkit-scrollbar-thumb {
  background: #C8C8C8;
  border-radius: 5px;
}

.code-content pre::-webkit-scrollbar-thumb:hover {
  background: #A8A8A8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .line-numbers {
    min-width: 40px;
    padding: 12px 0;
  }

  .line-number {
    padding: 0 8px 0 12px;
    font-size: 12px;
  }

  .code-content pre {
    padding: 12px;
  }

  .code-content code {
    font-size: 12px;
  }
}
</style>
