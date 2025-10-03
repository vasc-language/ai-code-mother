<template>
  <div class="markdown-content" v-html="renderedMarkdown"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 引入代码高亮样式
import 'highlight.js/styles/github.css'

interface Props {
  content: string
}

const props = defineProps<Props>()

// 配置 markdown-it 实例
const md: MarkdownIt = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // 忽略错误，使用默认处理
      }
    }

    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

// 计算渲染后的 Markdown
const renderedMarkdown = computed(() => {
  return md.render(props.content)
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.6;
  color: #333;
  word-wrap: break-word;
}

/* 全局样式，影响 v-html 内容 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1.5em 0 0.5em 0;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.3em;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h3) {
  font-size: 1.1em;
}

.markdown-content :deep(p) {
  margin: 0.8em 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.8em 0;
  padding-left: 1.5em;
}

.markdown-content :deep(li) {
  margin: 0.3em 0;
}

.markdown-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 4px solid #ddd;
  background-color: #f9f9f9;
  color: #666;
}

.markdown-content :deep(code) {
  background-color: #f1f1f1;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background-color: #f8f8f8;
  border: 1px solid #e1e1e1;
  border-radius: 6px;
  padding: 1em;
  overflow-x: auto;
  margin: 1em 0;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
  font-size: 0.9em;
  line-height: 1.4;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 1em 0;
  width: 100%;
}

.markdown-content :deep(table th),
.markdown-content :deep(table td) {
  border: 1px solid #ddd;
  padding: 0.5em 0.8em;
  text-align: left;
}

.markdown-content :deep(table th) {
  background-color: #f5f5f5;
  font-weight: 600;
}

.markdown-content :deep(table tr:nth-child(even)) {
  background-color: #f9f9f9;
}

.markdown-content :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #eee;
  margin: 1.5em 0;
}

/* 代码高亮样式优化 */
.markdown-content :deep(.hljs) {
  background-color: #f8f8f8 !important;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
  line-height: 1.4;
}

/* 特定语言的代码块样式 */
.markdown-content :deep(.hljs-keyword) {
  color: #d73a49;
  font-weight: 600;
}

.markdown-content :deep(.hljs-string) {
  color: #032f62;
}

.markdown-content :deep(.hljs-comment) {
  color: #6a737d;
  font-style: italic;
}

.markdown-content :deep(.hljs-number) {
  color: #005cc5;
}

.markdown-content :deep(.hljs-function) {
  color: #6f42c1;
}

.markdown-content :deep(.hljs-tag) {
  color: #22863a;
}

.markdown-content :deep(.hljs-attr) {
  color: #6f42c1;
}

.markdown-content :deep(.hljs-title) {
  color: #6f42c1;
  font-weight: 600;
}

/* —— 工具调用信息样式 —— */
/* 为包含加粗文本的段落添加特殊样式 */
.markdown-content :deep(p:has(strong)) {
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-left: 4px solid transparent;
  border-image: linear-gradient(109deg, rgb(0, 56, 255) 9.43%, rgb(0, 209, 255) 96.31%) 1;
  border-radius: 8px;
  margin: 14px 0;
  box-shadow: 0 2px 8px rgba(0, 56, 255, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.markdown-content :deep(p:has(strong)):hover {
  box-shadow: 0 4px 16px rgba(0, 56, 255, 0.15);
  transform: translateX(3px);
  background: linear-gradient(135deg, #f0f4ff 0%, #e8edff 100%);
}

/* 工具调用标签样式 - 使用 AI Code Mother 同款渐变 */
.markdown-content :deep(p > strong:first-child) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(109deg, rgb(0, 56, 255) 9.43%, rgb(0, 209, 255) 96.31%);
  color: white;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-right: 10px;
  box-shadow: 0 3px 10px rgba(0, 56, 255, 0.35);
  letter-spacing: 0.3px;
}

/* 工具调用内容文本 */
.markdown-content :deep(p:has(strong)) {
  color: #2c3e50;
  font-size: 14px;
  line-height: 1.7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 修复普通段落样式不受影响 */
.markdown-content :deep(p:not(:has(strong))) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

/* —— 修改文件 Diff 渲染样式（上下布局优化版）—— */
.markdown-content :deep(.diff-container) {
  border: 1px solid #e1e4e8;
  border-radius: 12px;
  background: white;
  margin: 16px 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.markdown-content :deep(.diff-container):hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 文件头部 */
.markdown-content :deep(.diff-container .diff-header) {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  border-bottom: 1px solid #e8eaed;
  font-size: 13px;
  color: #1a1a1a;
}

.markdown-content :deep(.diff-container .diff-header .tool) {
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  color: #d46b08;
  border: 1px solid #ffd591;
  border-radius: 6px;
  padding: 4px 10px;
  margin-right: 10px;
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.markdown-content :deep(.diff-container .diff-header .file-path) {
  color: #666;
  font-family: 'Monaco', 'Menlo', 'Cascadia Code', monospace;
  font-size: 12px;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 上下布局的diff区域 */
.markdown-content :deep(.diff-container.vertical) {
  display: flex;
  flex-direction: column;
}

/* Diff 区块 */
.markdown-content :deep(.diff-container .diff-section) {
  background: #fafbfc;
  padding: 12px 16px;
}

/* 标题样式 */
.markdown-content :deep(.diff-container .diff-title) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  letter-spacing: 0.3px;
}

.markdown-content :deep(.diff-container .diff-title .title-icon) {
  font-size: 16px;
}

.markdown-content :deep(.diff-container .diff-title .title-text) {
  flex: 1;
}

/* 替换前标题 - 红色系 */
.markdown-content :deep(.diff-container .before-title) {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  color: #cf1322;
  border-left: 4px solid #ff4d4f;
}

/* 替换后标题 - 绿色系 */
.markdown-content :deep(.diff-container .after-title) {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #389e0d;
  border-left: 4px solid #52c41a;
}

/* 分隔线 */
.markdown-content :deep(.diff-container .diff-divider) {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  background: linear-gradient(to right, transparent, #e8eaed 20%, #e8eaed 80%, transparent);
  position: relative;
}

.markdown-content :deep(.diff-container .diff-divider .divider-icon) {
  font-size: 20px;
  background: white;
  padding: 4px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 代码区域 */
.markdown-content :deep(.diff-container .diff-code) {
  background: white;
  border: 1px solid #e8eaed;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Cascadia Code', monospace;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  padding: 10px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  max-height: 400px;
  overflow-y: auto;
}

/* 代码行样式 */
.markdown-content :deep(.diff-container .diff-code .line) {
  display: flex;
  padding: 2px 12px;
  transition: background-color 0.2s ease;
}

.markdown-content :deep(.diff-container .diff-code .line .gutter) {
  width: 3.5em;
  color: #999;
  text-align: right;
  padding-right: 10px;
  user-select: none;
  font-weight: 500;
}

.markdown-content :deep(.diff-container .diff-code .line .content) {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 新增行 - 绿色背景 */
.markdown-content :deep(.diff-container .line.added) {
  background: linear-gradient(90deg, #e6ffed 0%, #f0fff4 100%);
  border-left: 3px solid #52c41a;
}

/* 删除行 - 红色背景 */
.markdown-content :deep(.diff-container .line.removed) {
  background: linear-gradient(90deg, #ffeef0 0%, #fff5f5 100%);
  border-left: 3px solid #ff4d4f;
}

/* 未修改行 */
.markdown-content :deep(.diff-container .line.unchanged) {
  background: transparent;
}

/* 空行 */
.markdown-content :deep(.diff-container .line.empty .content) {
  color: #bbb;
  font-style: italic;
}
</style>
