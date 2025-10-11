<template>
  <div class="diff-container vertical">
    <div class="diff-header">
      <span class="tool">修改文件</span>
      <span class="file-path">{{ filePath }}</span>
    </div>

    <!-- 替换前区域 -->
    <div class="diff-section">
      <div
        class="diff-title before-title"
        @click="toggleBefore"
        :class="{ collapsed: isBeforeCollapsed }"
      >
        <span class="collapse-icon">{{ isBeforeCollapsed ? '▶' : '▼' }}</span>
        <span class="title-text">替换前</span>
        <span class="line-count">{{ beforeLines.length }} 行</span>
      </div>
      <transition name="collapse">
        <div v-show="!isBeforeCollapsed" class="diff-code before-code">
          <div
            v-for="(line, index) in beforeLines"
            :key="`before-${index}`"
            :class="['line', line.type]"
          >
            <span class="gutter">{{ line.lineNo }}</span>
            <span class="content">{{ line.content }}</span>
          </div>
        </div>
      </transition>
    </div>

    <!-- 替换后区域 -->
    <div class="diff-section">
      <div
        class="diff-title after-title"
        @click="toggleAfter"
        :class="{ collapsed: isAfterCollapsed }"
      >
        <span class="collapse-icon">{{ isAfterCollapsed ? '▶' : '▼' }}</span>
        <span class="title-text">替换后</span>
        <span class="line-count">{{ afterLines.length }} 行</span>
      </div>
      <transition name="collapse">
        <div v-show="!isAfterCollapsed" class="diff-code after-code">
          <div
            v-for="(line, index) in afterLines"
            :key="`after-${index}`"
            :class="['line', line.type]"
          >
            <span class="gutter">{{ line.lineNo }}</span>
            <span class="content">{{ line.content }}</span>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'

interface DiffLine {
  lineNo: number
  content: string
  type: 'unchanged' | 'removed' | 'added'
}

interface Props {
  filePath: string
  beforeContent: string
  afterContent: string
  autoCollapseDelay?: number  // 自动折叠延迟时间（毫秒），0表示不自动折叠
}

const props = withDefaults(defineProps<Props>(), {
  autoCollapseDelay: 5000  // 默认5秒后自动折叠
})

const isBeforeCollapsed = ref(false)
const isAfterCollapsed = ref(false)

// 简单的diff算法：逐行对比
const diffLines = (oldText: string, newText: string) => {
  const oldLines = oldText.split('\n')
  const newLines = newText.split('\n')

  const before: DiffLine[] = []
  const after: DiffLine[] = []

  const maxLen = Math.max(oldLines.length, newLines.length)
  let beforeLineNo = 1
  let afterLineNo = 1

  for (let i = 0; i < maxLen; i++) {
    const oldLine = i < oldLines.length ? oldLines[i] : null
    const newLine = i < newLines.length ? newLines[i] : null

    if (oldLine !== null && newLine !== null) {
      if (oldLine === newLine) {
        // 未修改行
        before.push({ lineNo: beforeLineNo++, content: oldLine, type: 'unchanged' })
        after.push({ lineNo: afterLineNo++, content: newLine, type: 'unchanged' })
      } else {
        // 修改的行
        before.push({ lineNo: beforeLineNo++, content: oldLine, type: 'removed' })
        after.push({ lineNo: afterLineNo++, content: newLine, type: 'added' })
      }
    } else if (oldLine !== null) {
      // 只在旧版本中存在（删除）
      before.push({ lineNo: beforeLineNo++, content: oldLine, type: 'removed' })
    } else if (newLine !== null) {
      // 只在新版本中存在（新增）
      after.push({ lineNo: afterLineNo++, content: newLine, type: 'added' })
    }
  }

  return { before, after }
}

const { before: beforeLines, after: afterLines } = diffLines(
  props.beforeContent,
  props.afterContent
)

// 切换折叠状态
const toggleBefore = () => {
  isBeforeCollapsed.value = !isBeforeCollapsed.value
}

const toggleAfter = () => {
  isAfterCollapsed.value = !isAfterCollapsed.value
}

// 自动折叠
onMounted(() => {
  if (props.autoCollapseDelay > 0) {
    setTimeout(() => {
      isBeforeCollapsed.value = true
      isAfterCollapsed.value = true
    }, props.autoCollapseDelay)
  }
})
</script>

<style scoped>
/* 折叠动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
}

/* 折叠图标 */
.collapse-icon {
  display: inline-block;
  font-size: 12px;
  margin-right: 8px;
  transition: transform 0.3s ease;
}

.diff-title {
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

.diff-title:hover {
  opacity: 0.8;
}

.diff-title.collapsed .collapse-icon {
  transform: rotate(0deg);
}

.line-count {
  margin-left: auto;
  font-size: 12px;
  opacity: 0.7;
  font-weight: normal;
}

/* 继承父级样式 */
.diff-container {
  /* 样式由 MarkdownRenderer.vue 提供 */
}
</style>
