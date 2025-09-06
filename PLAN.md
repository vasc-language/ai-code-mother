# AppChatPage.vue 界面改造计划

## 项目概述
基于现有的AI代码生成聊天页面，参考设计图片实现一个更专业的代码生成界面，具有清晰的步骤展示和流式代码输出功能。

## 界面布局重新设计

### 当前界面分析 (AppChatPage.vue:40-192)
- **左侧对话区域** (chat-section): 占 flex: 2，包含消息容器和输入框
- **右侧预览区域** (preview-section): 占 flex: 3，显示生成的网页iframe
- **顶部栏**: 应用信息、操作按钮(详情、下载、部署)

### 目标界面设计 (基于参考图片分析)
**两栏布局重新规划:**
1. **左侧工具步骤区域** (改造): 显示AI操作步骤和工具调用过程，取代原有对话区域
2. **右侧代码生成区域** (改造): 流式输出代码文件，带文件标签，取代原有预览区域

**注意**: 移除原有的网页预览功能，专注于展示代码生成过程。用户可通过其他方式查看生成结果。

## 详细改造方案

### 1. 左侧工具步骤显示区域
```vue
<div class="tools-section">
  <div class="steps-container">
    <div class="step-item" v-for="step in generationSteps" :key="step.id">
      <div class="step-header">
        <span class="step-number">STEP {{ step.number }}</span>
        <span class="step-title">{{ step.title }}</span>
        <a-badge :status="getStepStatus(step)" />
      </div>
      
      <div class="tool-calls" v-if="step.toolCalls">
        <div class="tool-call-item" v-for="call in step.toolCalls" :key="call.id">
          <div class="tool-selection">
            <a-tag color="blue">{{ call.toolType }}</a-tag>
          </div>
          <div class="tool-execution">
            <span class="tool-action">{{ call.action }}</span>
            <span class="file-path">{{ call.filePath }}</span>
            <p class="operation-desc">{{ call.description }}</p>
          </div>
        </div>
      </div>
      
      <!-- 文件卡片显示 -->
      <div class="file-cards" v-if="step.generatedFiles">
        <a-card 
          size="small" 
          v-for="file in step.generatedFiles" 
          :key="file.name"
          class="file-card"
        >
          <div class="file-info">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-path">{{ file.path }}</span>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</div>
```

### 2. 右侧代码生成区域改造
```vue
<div class="code-generation-section">
  <div class="code-output-container">
    <!-- 当前生成的文件 -->
    <div class="current-file" v-if="currentGeneratingFile">
      <div class="file-header">
        <div class="file-tab">
          <FileOutlined class="file-icon" />
          <span class="file-name">{{ currentGeneratingFile.name }}</span>
          <a-button 
            type="link" 
            size="small" 
            @click="minimizeCurrentFile"
            v-if="currentGeneratingFile.completed"
          >
            <MinusOutlined />
          </a-button>
        </div>
      </div>
      <div class="code-content">
        <pre class="code-stream"><code>{{ currentGeneratingFile.content }}</code></pre>
        <div class="typing-cursor" v-if="!currentGeneratingFile.completed">|</div>
      </div>
    </div>

    <!-- 已完成的文件列表 -->
    <div class="completed-files">
      <a-collapse v-model:activeKey="activeFileKeys">
        <a-collapse-panel 
          v-for="file in completedFiles" 
          :key="file.id"
          :header="file.name"
        >
          <div class="file-path">{{ file.path }}</div>
          <pre class="code-content"><code>{{ file.content }}</code></pre>
        </a-collapse-panel>
      </a-collapse>
    </div>
  </div>

  <!-- 用户输入区域移动到这里 -->
  <div class="input-container">
    <!-- 现有的输入框代码保持不变 -->
  </div>
</div>
```

### 3. 数据结构设计
```typescript
// 步骤数据结构
interface GenerationStep {
  id: string
  number: number
  title: string
  status: 'pending' | 'running' | 'completed' | 'error'
  toolCalls?: ToolCall[]
  generatedFiles?: GeneratedFile[]
}

interface ToolCall {
  id: string
  toolType: '写入文件' | '删除文件' | '读取目录' | '修改文件' | '读取文件'
  action: string // 例如: "写入文件"
  filePath: string
  description: string
  status: 'pending' | 'running' | 'completed'
}

interface GeneratedFile {
  id: string
  name: string
  path: string
  content: string
  completed: boolean
  generatedAt: string
}
```

### 4. 核心功能实现

#### 4.1 流式代码生成处理 (改造 generateCode 方法)
```typescript
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  // ... 现有的 EventSource 设置代码 ...

  let currentFile: GeneratedFile | null = null
  let currentStep: GenerationStep | null = null

  eventSource.onmessage = function (event) {
    try {
      const parsed = JSON.parse(event.data)
      const content = parsed.d

      // 解析是否是新文件开始
      if (isNewFileStart(content)) {
        // 完成当前文件
        if (currentFile) {
          currentFile.completed = true
          completedFiles.value.push(currentFile)
        }
        
        // 开始新文件
        currentFile = createNewFile(content)
        currentGeneratingFile.value = currentFile
      } else if (isStepInfo(content)) {
        // 更新步骤信息
        currentStep = updateStep(content)
        generationSteps.value.push(currentStep)
      } else {
        // 追加代码内容
        if (currentFile) {
          currentFile.content += content
        }
      }

      scrollToBottom()
    } catch (error) {
      console.error('解析消息失败:', error)
    }
  }
}
```

#### 4.2 步骤状态管理
```typescript
const generationSteps = ref<GenerationStep[]>([])
const currentGeneratingFile = ref<GeneratedFile | null>(null)
const completedFiles = ref<GeneratedFile[]>([])
const activeFileKeys = ref<string[]>([])

const updateStep = (content: string): GenerationStep => {
  // 根据AI返回的内容解析步骤信息
  const stepMatch = content.match(/STEP (\d+)[:：]\s*(.+)/)
  if (stepMatch) {
    return {
      id: Date.now().toString(),
      number: parseInt(stepMatch[1]),
      title: stepMatch[2],
      status: 'running',
      toolCalls: parseToolCalls(content),
      generatedFiles: []
    }
  }
  return null
}

const parseToolCalls = (content: string): ToolCall[] => {
  // 解析工具调用信息，例如：
  // [选择工具] 写入文件
  // [工具调用] 写入文件 package.json
  const toolCalls = []
  const toolMatches = content.matchAll(/\[选择工具\]\s*(.+)\n\[工具调用\]\s*(.+)/g)
  
  for (const match of toolMatches) {
    toolCalls.push({
      id: Date.now().toString() + Math.random(),
      toolType: match[1],
      action: match[2].split(' ')[0],
      filePath: match[2].split(' ').slice(1).join(' '),
      description: '',
      status: 'running'
    })
  }
  return toolCalls
}
```

### 5. 样式设计 (CSS)
```scss
.main-content {
  display: flex;
  gap: 16px;
  
  // 左侧工具步骤区域
  .tools-section {
    flex: 1;
    min-width: 320px;
    max-width: 400px;
    display: flex;
    flex-direction: column;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    overflow: hidden;
    
    .section-header {
      padding: 16px;
      border-bottom: 1px solid #e8e8e8;
      background: #fafafa;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
      }
    }
    
    .steps-container {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
      
      .step-item {
        margin-bottom: 16px;
        padding: 12px;
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .step-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          
          .step-number {
            font-weight: bold;
            color: #1890ff;
            font-size: 14px;
          }
          
          .step-title {
            flex: 1;
            font-size: 13px;
            color: #333;
          }
        }
        
        .tool-call-item {
          margin: 8px 0;
          padding: 8px;
          background: white;
          border-radius: 4px;
          border: 1px solid #e1e4e8;
          
          .tool-selection {
            margin-bottom: 4px;
          }
          
          .tool-execution {
            .file-path {
              color: #666;
              font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
              font-size: 12px;
              word-break: break-all;
            }
            
            .operation-desc {
              margin: 4px 0 0 0;
              font-size: 12px;
              color: #666;
            }
          }
        }
        
        .file-cards {
          margin-top: 8px;
          
          .file-card {
            margin-bottom: 4px;
            cursor: pointer;
            transition: all 0.2s ease;
            
            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 6px rgba(0,0,0,0.1);
            }
            
            .file-info {
              display: flex;
              align-items: center;
              gap: 6px;
              
              .file-icon {
                color: #1890ff;
                font-size: 12px;
              }
              
              .file-name {
                font-size: 12px;
                font-weight: 500;
                color: #333;
              }
              
              .file-path {
                font-size: 11px;
                color: #999;
                margin-left: auto;
              }
            }
          }
        }
      }
    }
    
    // 底部用户输入区域
    .input-container {
      border-top: 1px solid #e8e8e8;
      padding: 16px;
      background: white;
      
      .input-wrapper {
        position: relative;
        
        .ant-input {
          padding-right: 50px;
          resize: vertical;
        }
        
        .input-actions {
          position: absolute;
          bottom: 8px;
          right: 8px;
        }
      }
    }
  }
  
  // 右侧代码生成区域
  .code-generation-section {
    flex: 2;
    display: flex;
    flex-direction: column;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    overflow: hidden;
    
    .section-header {
      padding: 16px;
      border-bottom: 1px solid #e8e8e8;
      background: #fafafa;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
      }
      
      .header-actions {
        display: flex;
        gap: 8px;
      }
    }
    
    .code-output-container {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      
      .current-file {
        background: white;
        border-bottom: 1px solid #e8e8e8;
        
        .file-header {
          padding: 12px 16px;
          border-bottom: 1px solid #f0f0f0;
          background: #f8f9fa;
          
          .file-tab {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .file-icon {
              color: #1890ff;
              font-size: 14px;
            }
            
            .file-name {
              font-weight: 500;
              color: #333;
            }
          }
        }
        
        .code-content {
          position: relative;
          padding: 16px;
          background: #fafbfc;
          max-height: 400px;
          overflow-y: auto;
          
          .code-stream {
            font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
            font-size: 13px;
            line-height: 1.5;
            color: #333;
            margin: 0;
            
            code {
              background: transparent;
              padding: 0;
              font-family: inherit;
            }
          }
          
          .typing-cursor {
            animation: blink 1s infinite;
            display: inline-block;
            color: #1890ff;
            font-weight: bold;
          }
        }
      }
      
      .completed-files {
        flex: 1;
        overflow-y: auto;
        
        .ant-collapse {
          border: none;
          background: transparent;
          
          .ant-collapse-item {
            border-bottom: 1px solid #f0f0f0;
            
            .ant-collapse-header {
              padding: 12px 16px !important;
              
              .file-panel-header {
                display: flex;
                align-items: center;
                gap: 8px;
                
                .file-icon {
                  color: #52c41a;
                }
                
                .file-name {
                  font-weight: 500;
                  color: #333;
                }
                
                .file-path {
                  margin-left: auto;
                  font-size: 12px;
                  color: #999;
                }
              }
            }
            
            .ant-collapse-content {
              .ant-collapse-content-box {
                padding: 16px;
                background: #fafbfc;
                
                .code-content {
                  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
                  font-size: 13px;
                  line-height: 1.5;
                  color: #333;
                  margin: 0;
                  max-height: 300px;
                  overflow-y: auto;
                  
                  code {
                    background: transparent;
                    padding: 0;
                    font-family: inherit;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
```

## 性能优化与响应式设计

### 1. 性能优化策略

#### 1.1 虚拟滚动优化
**大量代码内容的渲染优化：**
```typescript
// 使用虚拟滚动处理大量文件和代码内容
import { RecycleScroller } from 'vue-virtual-scroller'

// 步骤列表虚拟滚动
const VirtualStepList = defineComponent({
  components: { RecycleScroller },
  setup() {
    const stepItems = computed(() => 
      generationSteps.value.map(step => ({
        id: step.id,
        height: calculateStepHeight(step), // 动态计算高度
        data: step
      }))
    )
    
    return { stepItems }
  },
  template: `
    <RecycleScroller
      class="steps-scroller"
      :items="stepItems"
      :item-size="80"
      key-field="id"
      v-slot="{ item }"
    >
      <StepItem :step="item.data" />
    </RecycleScroller>
  `
})

// 代码内容虚拟滚动
const VirtualCodeViewer = defineComponent({
  setup(props: { content: string }) {
    const lines = computed(() => 
      props.content.split('\n').map((line, index) => ({
        id: index,
        content: line,
        height: 24 // 固定行高
      }))
    )
    
    return { lines }
  },
  template: `
    <RecycleScroller
      :items="lines"
      :item-size="24"
      key-field="id"
      class="code-scroller"
    >
      <template #default="{ item }">
        <div class="code-line">{{ item.content }}</div>
      </template>
    </RecycleScroller>
  `
})
```

#### 1.2 代码高亮优化
**按需加载和WebWorker处理：**
```typescript
// 使用Web Worker进行代码高亮处理
class CodeHighlighter {
  private worker: Worker | null = null
  private cache: Map<string, string> = new Map()
  
  constructor() {
    // 延迟加载Web Worker
    if (typeof Worker !== 'undefined') {
      this.worker = new Worker(new URL('./highlight-worker.ts', import.meta.url))
    }
  }
  
  async highlight(code: string, language: string): Promise<string> {
    const cacheKey = `${language}:${this.hashCode(code)}`
    
    // 检查缓存
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!
    }
    
    // 使用Web Worker处理
    if (this.worker) {
      return new Promise((resolve, reject) => {
        const timeoutId = setTimeout(() => {
          reject(new Error('Highlight timeout'))
        }, 5000)
        
        this.worker!.onmessage = (event) => {
          clearTimeout(timeoutId)
          const highlightedCode = event.data.result
          this.cache.set(cacheKey, highlightedCode)
          
          // 限制缓存大小
          if (this.cache.size > 100) {
            const firstKey = this.cache.keys().next().value
            this.cache.delete(firstKey)
          }
          
          resolve(highlightedCode)
        }
        
        this.worker!.onerror = () => {
          clearTimeout(timeoutId)
          reject(new Error('Worker error'))
        }
        
        this.worker!.postMessage({ code, language })
      })
    }
    
    // 降级到主线程处理（小代码块）
    if (code.length < 5000) {
      const highlighted = await this.highlightInMainThread(code, language)
      this.cache.set(cacheKey, highlighted)
      return highlighted
    }
    
    // 超大代码块不进行高亮
    return code
  }
  
  private async highlightInMainThread(code: string, language: string): Promise<string> {
    // 动态导入高亮库（按需加载）
    const { highlight, languages } = await import('prismjs/components/prism-core')
    
    // 按需加载语言支持
    if (!languages[language]) {
      try {
        await import(`prismjs/components/prism-${language}`)
      } catch (error) {
        console.warn(`Language ${language} not supported`)
        return code
      }
    }
    
    return highlight(code, languages[language], language)
  }
  
  private hashCode(str: string): string {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i)
      hash = ((hash << 5) - hash) + char
      hash = hash & hash // Convert to 32bit integer
    }
    return hash.toString()
  }
}

// highlight-worker.ts
self.onmessage = async function(event) {
  const { code, language } = event.data
  
  // 在Worker中导入必要的库
  const { highlight, languages } = await import('prismjs/components/prism-core')
  
  try {
    // 加载语言支持
    if (!languages[language]) {
      await import(`prismjs/components/prism-${language}`)
    }
    
    const result = highlight(code, languages[language], language)
    self.postMessage({ result })
  } catch (error) {
    self.postMessage({ result: code }) // 降级返回原始代码
  }
}
```

#### 1.3 内存管理优化
**智能内容清理和内存监控：**
```typescript
class MemoryManager {
  private memoryThreshold = 100 * 1024 * 1024 // 100MB
  private contentCache: Map<string, any> = new Map()
  private cleanupTimer: NodeJS.Timeout | null = null
  
  constructor() {
    this.startMemoryMonitoring()
  }
  
  private startMemoryMonitoring() {
    if ('memory' in performance) {
      setInterval(() => {
        const memInfo = (performance as any).memory
        if (memInfo.usedJSHeapSize > this.memoryThreshold) {
          this.triggerCleanup()
        }
      }, 30000) // 每30秒检查一次
    }
  }
  
  private triggerCleanup() {
    console.log('Triggering memory cleanup')
    
    // 清理长时间未使用的缓存
    const now = Date.now()
    for (const [key, value] of this.contentCache.entries()) {
      if (now - value.lastAccessed > 300000) { // 5分钟未访问
        this.contentCache.delete(key)
      }
    }
    
    // 清理完成的文件内容（保留最近的20个）
    if (completedFiles.value.length > 20) {
      const filesToRemove = completedFiles.value.splice(0, completedFiles.value.length - 20)
      filesToRemove.forEach(file => {
        // 清理大文件内容，保留基本信息
        if (file.content.length > 50000) {
          file.content = file.content.substring(0, 1000) + '\n\n... [内容已清理以释放内存] ...\n\n' + 
                        file.content.substring(file.content.length - 1000)
          file.truncated = true
        }
      })
    }
    
    // 强制垃圾回收（如果支持）
    if ('gc' in window) {
      (window as any).gc()
    }
  }
  
  public cacheContent(key: string, content: any) {
    this.contentCache.set(key, {
      content,
      lastAccessed: Date.now()
    })
  }
  
  public getCachedContent(key: string) {
    const cached = this.contentCache.get(key)
    if (cached) {
      cached.lastAccessed = Date.now()
      return cached.content
    }
    return null
  }
}
```

#### 1.4 网络请求优化
**请求去重和批处理：**
```typescript
class RequestOptimizer {
  private pendingRequests: Map<string, Promise<any>> = new Map()
  private requestQueue: Array<{ url: string, options: any, resolve: Function, reject: Function }> = []
  private processingQueue = false
  
  // 请求去重
  async dedupedRequest(url: string, options: any = {}): Promise<any> {
    const requestKey = this.generateRequestKey(url, options)
    
    // 如果相同请求正在进行中，返回相同的Promise
    if (this.pendingRequests.has(requestKey)) {
      return this.pendingRequests.get(requestKey)
    }
    
    const requestPromise = this.executeRequest(url, options)
      .finally(() => {
        this.pendingRequests.delete(requestKey)
      })
    
    this.pendingRequests.set(requestKey, requestPromise)
    return requestPromise
  }
  
  // 批量请求处理
  async batchRequest(requests: Array<{ url: string, options?: any }>): Promise<any[]> {
    return Promise.allSettled(
      requests.map(req => this.dedupedRequest(req.url, req.options))
    )
  }
  
  private async executeRequest(url: string, options: any): Promise<any> {
    // 实现请求重试机制
    let retries = 3
    while (retries > 0) {
      try {
        const response = await fetch(url, {
          ...options,
          signal: AbortSignal.timeout(10000) // 10秒超时
        })
        
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`)
        }
        
        return await response.json()
      } catch (error) {
        retries--
        if (retries === 0) throw error
        
        // 指数退避重试
        await new Promise(resolve => setTimeout(resolve, Math.pow(2, 3 - retries) * 1000))
      }
    }
  }
  
  private generateRequestKey(url: string, options: any): string {
    return `${url}:${JSON.stringify(options)}`
  }
}
```

### 2. 响应式设计优化

#### 2.1 自适应布局系统
**智能断点和布局调整：**
```scss
// 响应式断点定义
$breakpoints: (
  xs: 480px,
  sm: 768px,
  md: 1024px,
  lg: 1440px,
  xl: 1920px
);

@mixin respond-to($breakpoint) {
  @media (min-width: map-get($breakpoints, $breakpoint)) {
    @content;
  }
}

// 主要布局响应式
.main-content {
  display: flex;
  gap: 16px;
  padding: 8px;
  height: calc(100vh - 120px);
  
  // 移动端：垂直堆叠
  @media (max-width: 767px) {
    flex-direction: column;
    gap: 8px;
    padding: 4px;
    
    .tools-section {
      flex: none;
      height: 40vh;
      min-width: unset;
      max-width: unset;
      
      .steps-container {
        padding: 8px;
        
        .step-item {
          padding: 8px;
          margin-bottom: 8px;
          
          .step-header {
            .step-title {
              font-size: 12px;
            }
            
            .step-number {
              font-size: 12px;
            }
          }
          
          .tool-call-item {
            padding: 6px;
            margin: 4px 0;
            
            .file-path {
              font-size: 10px;
            }
          }
        }
      }
      
      .input-container {
        padding: 8px;
        
        .ant-input {
          font-size: 14px;
        }
      }
    }
    
    .code-generation-section {
      flex: 1;
      min-height: 50vh;
      
      .current-file {
        .file-header {
          padding: 8px 12px;
          
          .file-tab {
            gap: 4px;
            
            .file-name {
              font-size: 12px;
            }
          }
        }
        
        .code-content {
          padding: 8px;
          max-height: 300px;
          
          .code-stream {
            font-size: 11px;
            line-height: 1.4;
          }
        }
      }
    }
  }
  
  // 平板端：调整比例
  @media (min-width: 768px) and (max-width: 1023px) {
    .tools-section {
      flex: 0 0 280px;
      min-width: 280px;
      max-width: 320px;
    }
    
    .code-generation-section {
      flex: 1;
    }
  }
  
  // 桌面端：标准布局
  @media (min-width: 1024px) {
    .tools-section {
      flex: 0 0 320px;
      min-width: 320px;
      max-width: 400px;
    }
    
    .code-generation-section {
      flex: 1;
      min-width: 600px;
    }
  }
  
  // 大屏端：扩展布局
  @media (min-width: 1440px) {
    gap: 20px;
    
    .tools-section {
      flex: 0 0 380px;
      max-width: 450px;
    }
    
    .code-generation-section {
      .current-file {
        .code-content {
          max-height: 500px;
          
          .code-stream {
            font-size: 14px;
          }
        }
      }
    }
  }
}

// 触摸设备优化
@media (hover: none) and (pointer: coarse) {
  .file-card {
    &:hover {
      transform: none; // 移除hover效果
    }
    
    &:active {
      transform: scale(0.98);
      background-color: #f0f0f0;
    }
  }
  
  .tool-call-item {
    padding: 12px; // 增加触摸区域
    margin: 8px 0;
  }
  
  .input-container {
    .ant-input {
      font-size: 16px; // 防止iOS缩放
      min-height: 44px; // iOS触摸最小尺寸
    }
  }
}

// 高对比度和无障碍支持
@media (prefers-contrast: high) {
  .step-item {
    border: 2px solid #000;
    
    .step-number {
      color: #000;
      font-weight: 900;
    }
  }
  
  .tool-call-item {
    border: 1px solid #333;
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
  
  .typing-cursor {
    animation: none;
  }
}

// 深色模式支持
@media (prefers-color-scheme: dark) {
  .main-content {
    background: #1a1a1a;
    
    .tools-section,
    .code-generation-section {
      background: #2a2a2a;
      border: 1px solid #404040;
      
      .section-header {
        background: #333;
        border-bottom-color: #404040;
        
        h3 {
          color: #fff;
        }
      }
    }
    
    .step-item {
      background: #333;
      border-color: #404040;
      
      .step-title {
        color: #e0e0e0;
      }
      
      .tool-call-item {
        background: #2a2a2a;
        border-color: #404040;
        
        .file-path {
          color: #b0b0b0;
        }
      }
    }
    
    .code-content {
      background: #1e1e1e;
      
      .code-stream {
        color: #e0e0e0;
      }
    }
  }
}
```

#### 2.2 性能监控和指标
**实时性能监控系统：**
```typescript
class PerformanceMonitor {
  private metrics: Map<string, number[]> = new Map()
  private observer: PerformanceObserver | null = null
  
  constructor() {
    this.initializeObserver()
    this.startMonitoring()
  }
  
  private initializeObserver() {
    if ('PerformanceObserver' in window) {
      this.observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordMetric(entry.name, entry.duration)
          
          // 长任务警告
          if (entry.entryType === 'longtask' && entry.duration > 50) {
            console.warn(`Long task detected: ${entry.duration}ms`)
            this.notifySlowPerformance(entry.duration)
          }
        }
      })
      
      this.observer.observe({ entryTypes: ['longtask', 'measure', 'navigation'] })
    }
  }
  
  public startTiming(label: string) {
    performance.mark(`${label}-start`)
  }
  
  public endTiming(label: string) {
    performance.mark(`${label}-end`)
    performance.measure(label, `${label}-start`, `${label}-end`)
  }
  
  private recordMetric(name: string, value: number) {
    if (!this.metrics.has(name)) {
      this.metrics.set(name, [])
    }
    
    const values = this.metrics.get(name)!
    values.push(value)
    
    // 只保留最近100个记录
    if (values.length > 100) {
      values.shift()
    }
  }
  
  public getMetrics(): Record<string, any> {
    const result: Record<string, any> = {}
    
    for (const [name, values] of this.metrics.entries()) {
      if (values.length > 0) {
        const avg = values.reduce((sum, val) => sum + val, 0) / values.length
        const min = Math.min(...values)
        const max = Math.max(...values)
        
        result[name] = { avg, min, max, count: values.length }
      }
    }
    
    return result
  }
  
  private notifySlowPerformance(duration: number) {
    // 性能降级策略
    if (duration > 100) {
      // 禁用动画
      document.body.classList.add('reduced-motion')
      
      // 减少虚拟滚动缓冲区
      this.adjustVirtualScrollBuffer(10)
      
      // 降低代码高亮复杂度
      this.disableComplexHighlighting()
    }
  }
  
  private adjustVirtualScrollBuffer(size: number) {
    // 通知虚拟滚动组件调整缓冲区大小
    window.dispatchEvent(new CustomEvent('performance-adjust', { 
      detail: { bufferSize: size } 
    }))
  }
  
  private disableComplexHighlighting() {
    // 禁用复杂的语法高亮
    window.dispatchEvent(new CustomEvent('disable-highlighting'))
  }
}

// 性能优化的代码组件
const PerformantCodeViewer = defineComponent({
  props: {
    content: String,
    language: String
  },
  setup(props) {
    const monitor = new PerformanceMonitor()
    const isHighlightingEnabled = ref(true)
    const chunkSize = ref(1000) // 每次渲染的行数
    
    // 监听性能事件
    onMounted(() => {
      window.addEventListener('disable-highlighting', () => {
        isHighlightingEnabled.value = false
      })
      
      window.addEventListener('performance-adjust', (event: CustomEvent) => {
        chunkSize.value = event.detail.bufferSize
      })
    })
    
    const processedContent = computed(() => {
      monitor.startTiming('content-processing')
      
      const lines = props.content.split('\n')
      const chunks = []
      
      for (let i = 0; i < lines.length; i += chunkSize.value) {
        chunks.push(lines.slice(i, i + chunkSize.value))
      }
      
      monitor.endTiming('content-processing')
      return chunks
    })
    
    return {
      processedContent,
      isHighlightingEnabled
    }
  }
})
```

### 3. 用户体验增强

#### 3.1 加载状态和反馈
**智能加载指示和用户反馈：**
```vue
<template>
  <div class="loading-aware-component">
    <!-- 骨架屏加载 -->
    <div v-if="isInitialLoading" class="skeleton-container">
      <div class="skeleton-step" v-for="i in 3" :key="i">
        <div class="skeleton-header"></div>
        <div class="skeleton-content"></div>
      </div>
    </div>
    
    <!-- 内容加载完成 -->
    <div v-else class="content-container">
      <!-- 实际内容 -->
    </div>
    
    <!-- 全局加载指示器 -->
    <Teleport to="body">
      <div v-if="isGenerating" class="global-loading-overlay">
        <div class="loading-content">
          <div class="spinner"></div>
          <div class="loading-text">{{ loadingMessage }}</div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
          </div>
          <a-button v-if="canCancel" @click="cancelOperation" type="text" danger>
            取消生成
          </a-button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.skeleton-container {
  .skeleton-step {
    margin-bottom: 16px;
    
    .skeleton-header {
      height: 20px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
      border-radius: 4px;
      margin-bottom: 8px;
    }
    
    .skeleton-content {
      height: 60px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
      border-radius: 4px;
      animation-delay: 0.2s;
    }
  }
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.global-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  
  .loading-content {
    background: white;
    padding: 32px;
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    text-align: center;
    min-width: 300px;
    
    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #1890ff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 16px;
    }
    
    .loading-text {
      font-size: 16px;
      color: #333;
      margin-bottom: 16px;
    }
    
    .progress-bar {
      width: 100%;
      height: 4px;
      background: #f0f0f0;
      border-radius: 2px;
      overflow: hidden;
      margin-bottom: 16px;
      
      .progress-fill {
        height: 100%;
        background: #1890ff;
        border-radius: 2px;
        transition: width 0.3s ease;
      }
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
```

## 总结与实施建议

### 实施路径建议

1. **第一阶段（MVP）**：
   - 实现基础两栏布局
   - 基本的工具步骤解析和显示
   - 简单的代码流式输出

2. **第二阶段（增强）**：
   - 添加健壮性错误处理
   - 实现性能优化措施
   - 完善响应式设计

3. **第三阶段（完善）**：
   - 添加高级功能（搜索、筛选等）
   - 完整的无障碍支持
   - 深度性能优化

### 风险控制

1. **技术风险**：
   - 渐进式改造，确保每个阶段都可回退
   - 充分的测试覆盖，特别是边界情况
   - 性能基准测试，防止性能退化

2. **用户体验风险**：
   - A/B测试验证新界面效果
   - 保留用户反馈通道
   - 提供界面切换选项（如需要）

3. **维护风险**：
   - 代码模块化，便于后续维护
   - 完整的文档和注释
   - 自动化测试保证代码质量

通过这套完整的改造方案，可以将AppChatPage.vue从简单的对话界面升级为专业的AI代码生成工作台，大幅提升用户的使用体验和工作效率。

## 实现优先级

### Phase 1: 基础布局改造 ⭐⭐⭐
- 修改主布局为两栏结构（左侧工具步骤 + 右侧代码输出）
- 改造左侧区域为步骤显示区域，移除原有对话功能
- 改造右侧区域为代码生成区域，移除原有预览功能

### Phase 2: 流式输出改进 ⭐⭐⭐
- 改造 generateCode 方法支持分文件显示
- 实现打字机效果的代码流式输出
- 添加文件完成后的收起/展开功能

### Phase 3: 步骤跟踪系统 ⭐⭐
- 实现 AI 操作步骤的解析和显示
- 添加工具调用状态跟踪
- 完善文件卡片展示

### Phase 4: 交互优化 ⭐
- 添加步骤跳转功能
- 实现文件搜索和筛选
- 优化移动端响应式设计

## 后端工具返回值分析

基于对后端工具类的分析，每个工具都有两种返回格式：

### 工具选择阶段返回值 (BaseTool:31-33)
```java
// 显示给用户，表示AI选择了什么工具
public String generateToolRequestResponse() {
    return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
}
```

### 工具执行结果返回值 (各工具的generateToolExecutedResult方法)

**1. 写入文件工具 (FileWriteTool:73-86)**
```java
// 返回格式：[工具调用] 写入文件 文件路径 + 代码块
return String.format("""
        [工具调用] %s %s
        ```%s
        %s
        ```
        """, getDisplayName(), relativeFilePath, suffix, content);
```

**2. 读取文件工具 (FileReadTool:59-62)**
```java
// 返回格式：[工具调用] 读取文件 文件路径
return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
```

**3. 修改文件工具 (FileModifyTool:74-92)**
```java
// 返回格式：[工具调用] 修改文件 文件路径 + 对比代码块
return String.format("""
        [工具调用] %s %s
        
        替换前：
        ```
        %s
        ```
        
        替换后：
        ```
        %s
        ```
        """, getDisplayName(), relativeFilePath, oldContent, newContent);
```

**4. 删除文件工具 (FileDeleteTool:87-90)**
```java
// 返回格式：[工具调用] 删除文件 文件路径
return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
```

**5. 读取目录工具 (FileDirReadTool:122-128)**
```java
// 返回格式：[工具调用] 读取目录 目录路径
return String.format("[工具调用] %s %s", getDisplayName(), relativeDirPath);
```

### 前端解析规则设计

基于后端返回值格式，前端需要解析以下模式：

```typescript
// 工具选择解析
const isToolSelection = (content: string) => {
  return content.includes('[选择工具]')
}

// 工具调用解析  
const isToolExecution = (content: string) => {
  return content.includes('[工具调用]')
}

// 文件生成检测（写入文件工具特有的代码块）
const isFileGeneration = (content: string) => {
  return content.includes('[工具调用] 写入文件') && content.includes('```')
}

// 解析工具调用信息
const parseToolCall = (content: string): ToolCall => {
  const toolMatch = content.match(/\[工具调用\]\s+(.+?)\s+(.+?)(?:\n|$)/)
  if (toolMatch) {
    return {
      toolType: toolMatch[1], // 如："写入文件"、"读取文件"等
      filePath: toolMatch[2],  // 文件路径
      content: content        // 完整内容，包含代码块
    }
  }
}
```

## 前后端工具返回值与界面展示统一性设计（增强版）

### 1. 界面展示与后端返回值的对应关系

**左侧步骤区域显示映射（健壮性增强）：**
```typescript
// 后端返回：[选择工具] 写入文件
// 前端显示：步骤标题 + 工具标签
const renderToolSelection = (content: string) => {
  try {
    // 支持中英文括号和多种空白符
    const toolMatch = content.match(/\[(?:选择工具|tool-selection)\]\s*(.+?)(?:\n|$)/i)
    if (toolMatch) {
      const toolName = toolMatch[1].trim()
      return {
        stepType: 'tool-selection',
        toolName: toolName,
        displayText: `选择工具：${toolName}`,
        tagColor: getToolColor(toolName),
        timestamp: new Date().toISOString(),
        confidence: 'high' // 解析信心度
      }
    }
    return null
  } catch (error) {
    console.warn('解析工具选择失败:', error, content)
    return {
      stepType: 'tool-selection',
      toolName: '未知工具',
      displayText: '解析失败',
      tagColor: 'default',
      error: true
    }
  }
}

// 工具颜色映射（支持多语言和别名）
const getToolColor = (toolName: string) => {
  const normalizedName = toolName.toLowerCase().trim()
  const colorMap = {
    // 中文工具名
    '写入文件': 'blue',
    '读取文件': 'green', 
    '修改文件': 'orange',
    '删除文件': 'red',
    '读取目录': 'purple',
    // 英文工具名
    'writefile': 'blue',
    'readfile': 'green',
    'modifyfile': 'orange', 
    'deletefile': 'red',
    'readdirectory': 'purple',
    // 别名
    '文件写入': 'blue',
    '文件读取': 'green',
    '文件修改': 'orange',
    '文件删除': 'red',
    '目录读取': 'purple'
  }
  return colorMap[normalizedName] || colorMap[toolName] || 'default'
}

**中间代码区域显示映射：**
```typescript
// 后端返回：[工具调用] 写入文件 package.json + 代码块
// 前端显示：文件标签页 + 流式代码输出
const renderToolExecution = (content: string) => {
  // 解析文件信息
  const toolMatch = content.match(/\[工具调用\]\s+(.+?)\s+(.+?)(?:\n|$)/)
  const codeMatch = content.match(/```(\w+)?\n([\s\S]*?)```/)
  
  if (toolMatch && codeMatch) {
    return {
      stepType: 'file-generation',
      toolName: toolMatch[1],
      filePath: toolMatch[2],
      fileName: extractFileName(toolMatch[2]),
      language: codeMatch[1] || 'text',
      code: codeMatch[2],
      displayInCodeArea: true // 显示在中间代码区域
    }
  }
  
  // 非文件生成的工具调用（如删除、读取）
  if (toolMatch) {
    return {
      stepType: 'tool-execution',
      toolName: toolMatch[1],
      filePath: toolMatch[2],
      description: `${toolMatch[1]} ${toolMatch[2]}`,
      displayInCodeArea: false // 只显示在左侧步骤区
    }
  }
}
```

### 2. 统一的数据流处理（增强健壮性）

**流式内容解析器（健壮性增强版）：**
```typescript
interface ParseResult {
  type: 'step-start' | 'tool-selection' | 'tool-execution' | 'file-start' | 'code-stream' | 'text-stream' | 'error'
  data: any
  confidence?: 'high' | 'medium' | 'low'
  error?: boolean
  rawContent?: string
}

class ContentParser {
  private currentStep: GenerationStep | null = null
  private currentFile: GeneratedFile | null = null
  private buffer: string = '' // 处理分片内容
  private parseErrors: string[] = [] // 错误收集
  
  // 主要解析方法，增强错误处理
  parseStreamContent(content: string): ParseResult {
    if (!content || typeof content !== 'string') {
      return { type: 'error', data: { message: 'Invalid content', content }, error: true }
    }
    
    // 将新内容加入缓冲区
    this.buffer += content
    
    try {
      // 1. 检测新步骤开始 (STEP N: ...) - 支持多种格式
      const stepResult = this.tryParseStepStart(this.buffer)
      if (stepResult.success) {
        this.currentStep = stepResult.step
        this.clearProcessedContent(stepResult.matchLength)
        return { type: 'step-start', data: this.currentStep, confidence: 'high' }
      }
      
      // 2. 检测工具选择 ([选择工具] ...) - 增强模式匹配
      const toolSelectionResult = this.tryParseToolSelection(this.buffer)
      if (toolSelectionResult.success) {
        if (this.currentStep) {
          this.currentStep.toolCalls = this.currentStep.toolCalls || []
          this.currentStep.toolCalls.push(toolSelectionResult.toolCall)
        }
        this.clearProcessedContent(toolSelectionResult.matchLength)
        return { 
          type: 'tool-selection', 
          data: toolSelectionResult.toolCall, 
          confidence: toolSelectionResult.confidence 
        }
      }
      
      // 3. 检测工具执行 ([工具调用] ...) - 增强文件解析
      const toolExecutionResult = this.tryParseToolExecution(this.buffer)
      if (toolExecutionResult.success) {
        const toolExecution = toolExecutionResult.toolExecution
        
        // 3.1 如果是文件生成类工具，创建新文件
        if (this.isFileGenerationTool(toolExecution)) {
          if (this.currentFile && !this.currentFile.completed) {
            this.currentFile.completed = true
            this.currentFile.completedAt = new Date().toISOString()
          }
          
          try {
            this.currentFile = this.createFileFromTool(toolExecution)
            this.clearProcessedContent(toolExecutionResult.matchLength)
            return { 
              type: 'file-start', 
              data: this.currentFile, 
              confidence: toolExecutionResult.confidence 
            }
          } catch (error) {
            this.parseErrors.push(`创建文件失败: ${error}`)
            return { type: 'error', data: { error, toolExecution }, error: true }
          }
        } else {
          // 3.2 其他工具调用，只更新步骤状态
          if (this.currentStep) {
            this.currentStep.toolCalls = this.currentStep.toolCalls || []
            this.currentStep.toolCalls.push(toolExecution)
          }
          this.clearProcessedContent(toolExecutionResult.matchLength)
          return { 
            type: 'tool-execution', 
            data: toolExecution, 
            confidence: toolExecutionResult.confidence 
          }
        }
      }
      
      // 4. 普通内容流（代码内容）- 增强状态检查
      if (this.currentFile && !this.currentFile.completed) {
        // 处理代码块结束标记
        if (this.buffer.includes('```') && this.currentFile.content.includes('```')) {
          const endIndex = this.buffer.indexOf('```', 3) + 3
          if (endIndex > 3) {
            const codeContent = this.buffer.substring(0, endIndex)
            this.currentFile.content += codeContent
            this.currentFile.completed = true
            this.currentFile.completedAt = new Date().toISOString()
            this.clearProcessedContent(endIndex)
            return { 
              type: 'code-stream', 
              data: { file: this.currentFile, chunk: codeContent, completed: true },
              confidence: 'high'
            }
          }
        }
        
        // 常规代码内容追加
        this.currentFile.content += content
        this.currentFile.lastUpdated = new Date().toISOString()
        return { 
          type: 'code-stream', 
          data: { file: this.currentFile, chunk: content },
          confidence: 'medium'
        }
      }
      
      // 5. 其他内容（AI描述文本等）
      return { type: 'text-stream', data: { content }, confidence: 'low', rawContent: this.buffer }
      
    } catch (error) {
      this.parseErrors.push(`解析错误: ${error}`)
      console.error('Content parsing error:', error, 'Buffer:', this.buffer)
      return { type: 'error', data: { error, content: this.buffer }, error: true }
    }
  }
  
  // 增强的步骤解析
  private tryParseStepStart(buffer: string): { success: boolean, step?: GenerationStep, matchLength?: number } {
    try {
      // 支持多种步骤格式
      const patterns = [
        /(?:^|\n)(STEP\s*(\d+)[:：]\s*(.+?)(?:\n|$))/i,
        /(?:^|\n)(步骤\s*(\d+)[:：]\s*(.+?)(?:\n|$))/i,
        /(?:^|\n)(第\s*(\d+)\s*步[:：]\s*(.+?)(?:\n|$))/i
      ]
      
      for (const pattern of patterns) {
        const match = buffer.match(pattern)
        if (match) {
          return {
            success: true,
            step: {
              id: Date.now().toString(),
              number: parseInt(match[2]),
              title: match[3].trim(),
              status: 'running',
              toolCalls: [],
              generatedFiles: [],
              startTime: new Date().toISOString()
            },
            matchLength: match[1].length
          }
        }
      }
      return { success: false }
    } catch (error) {
      console.error('Step parsing error:', error)
      return { success: false }
    }
  }
  
  // 增强的工具选择解析
  private tryParseToolSelection(buffer: string): { 
    success: boolean, 
    toolCall?: ToolCall, 
    matchLength?: number,
    confidence?: 'high' | 'medium' | 'low'
  } {
    try {
      const patterns = [
        /\[(?:选择工具|tool-selection)\]\s*(.+?)(?:\n|$)/gi,
        /选择工具[:：]\s*(.+?)(?:\n|$)/gi,
        /工具选择[:：]\s*(.+?)(?:\n|$)/gi
      ]
      
      for (const pattern of patterns) {
        const match = buffer.match(pattern)
        if (match) {
          const toolName = match[1].trim()
          return {
            success: true,
            toolCall: {
              id: Date.now().toString() + Math.random(),
              toolType: toolName,
              action: '选择',
              filePath: '',
              description: `选择了工具: ${toolName}`,
              status: 'completed',
              timestamp: new Date().toISOString()
            },
            matchLength: match[0].length,
            confidence: 'high'
          }
        }
      }
      return { success: false }
    } catch (error) {
      console.error('Tool selection parsing error:', error)
      return { success: false }
    }
  }
  
  // 增强的工具执行解析
  private tryParseToolExecution(buffer: string): { 
    success: boolean, 
    toolExecution?: ToolCall, 
    matchLength?: number,
    confidence?: 'high' | 'medium' | 'low'
  } {
    try {
      // 匹配工具调用格式，支持代码块
      const toolCallMatch = buffer.match(/\[工具调用\]\s+(.+?)\s+(.+?)(?:\n|```)/s)
      if (toolCallMatch) {
        const toolName = toolCallMatch[1].trim()
        const filePath = toolCallMatch[2].trim()
        
        // 检查是否有代码块
        const codeBlockMatch = buffer.match(/```(\w+)?\n([\s\S]*?)```/)
        
        const toolExecution: ToolCall = {
          id: Date.now().toString() + Math.random(),
          toolType: toolName,
          action: this.mapToolNameToAction(toolName),
          filePath: filePath,
          description: `${toolName} ${filePath}`,
          status: 'running',
          timestamp: new Date().toISOString()
        }
        
        if (codeBlockMatch) {
          toolExecution.language = codeBlockMatch[1] || this.detectLanguage(filePath)
          toolExecution.code = codeBlockMatch[2]
          toolExecution.hasCode = true
        }
        
        return {
          success: true,
          toolExecution,
          matchLength: toolCallMatch[0].length,
          confidence: 'high'
        }
      }
      return { success: false }
    } catch (error) {
      console.error('Tool execution parsing error:', error)
      return { success: false }
    }
  }
  
  // 工具名到操作的映射
  private mapToolNameToAction(toolName: string): string {
    const actionMap: { [key: string]: string } = {
      '写入文件': '写入',
      '读取文件': '读取',
      '修改文件': '修改',
      '删除文件': '删除',
      '读取目录': '读取'
    }
    return actionMap[toolName] || '执行'
  }
  
  // 语言检测
  private detectLanguage(filePath: string): string {
    const ext = filePath.split('.').pop()?.toLowerCase()
    const languageMap: { [key: string]: string } = {
      'js': 'javascript',
      'ts': 'typescript',
      'vue': 'vue',
      'html': 'html',
      'css': 'css',
      'json': 'json',
      'md': 'markdown',
      'py': 'python',
      'java': 'java'
    }
    return languageMap[ext || ''] || 'text'
  }
  
  // 清理已处理的内容
  private clearProcessedContent(length: number) {
    this.buffer = this.buffer.substring(length)
  }
  
  // 重置解析器状态
  public reset() {
    this.currentStep = null
    this.currentFile = null
    this.buffer = ''
    this.parseErrors = []
  }
  
  // 获取解析错误
  public getErrors(): string[] {
    return [...this.parseErrors]
  }
  
  private isFileGenerationTool(toolExecution: ToolCall): boolean {
    return toolExecution.toolType === '写入文件' && Boolean(toolExecution.code)
  }
  
  private createFileFromTool(toolExecution: ToolCall): GeneratedFile {
    if (!toolExecution.filePath) {
      throw new Error('文件路径不能为空')
    }
    
    return {
      id: Date.now().toString() + Math.random(),
      name: this.extractFileName(toolExecution.filePath),
      path: toolExecution.filePath,
      content: toolExecution.code || '',
      language: toolExecution.language || this.detectLanguage(toolExecution.filePath),
      completed: false,
      generatedAt: new Date().toISOString(),
      lastUpdated: new Date().toISOString(),
      size: (toolExecution.code || '').length
    }
  }
  
  private extractFileName(filePath: string): string {
    if (!filePath) return '未知文件'
    return filePath.split(/[/\\]/).pop() || filePath
  }
}
```

### 3. 界面组件的统一渲染

**左侧步骤组件：**
```vue
<template>
  <div class="step-item" v-for="step in generationSteps" :key="step.id">
    <div class="step-header">
      <span class="step-number">STEP {{ step.number }}</span>
      <span class="step-title">{{ step.title }}</span>
      <a-badge :status="getStepStatus(step)" />
    </div>
    
    <!-- 工具调用列表 -->
    <div class="tool-calls">
      <div 
        class="tool-call-item" 
        v-for="call in step.toolCalls" 
        :key="call.id"
        :class="{ 'file-generation': isFileGenerationCall(call) }"
      >
        <!-- 工具选择显示 -->
        <div class="tool-selection">
          <a-tag :color="getToolColor(call.toolName)">
            {{ call.toolName }}
          </a-tag>
        </div>
        
        <!-- 工具执行显示 -->
        <div class="tool-execution">
          <span class="file-path">{{ call.filePath }}</span>
          
          <!-- 文件生成特殊显示 -->
          <div v-if="isFileGenerationCall(call)" class="file-preview">
            <div class="language-tag">{{ call.language }}</div>
            <div class="code-preview">{{ getCodePreview(call.code) }}</div>
          </div>
          
          <!-- 普通工具调用显示 -->
          <div v-else class="operation-desc">
            {{ getOperationDescription(call) }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 生成的文件卡片 -->
    <div class="generated-files" v-if="step.generatedFiles?.length">
      <a-card 
        size="small" 
        v-for="file in step.generatedFiles" 
        :key="file.id"
        class="file-card"
        @click="jumpToFile(file)"
      >
        <div class="file-info">
          <FileOutlined class="file-icon" />
          <span class="file-name">{{ file.name }}</span>
          <span class="file-size">{{ formatFileSize(file.content) }}</span>
        </div>
      </a-card>
    </div>
  </div>
</template>
```

**中间代码生成组件：**
```vue
<template>
  <div class="code-generation-section">
    <!-- 当前正在生成的文件 -->
    <div class="current-file" v-if="currentGeneratingFile">
      <div class="file-header">
        <div class="file-tab">
          <FileOutlined class="file-icon" />
          <span class="file-name">{{ currentGeneratingFile.name }}</span>
          <a-tag size="small" :color="getLanguageColor(currentGeneratingFile.language)">
            {{ currentGeneratingFile.language }}
          </a-tag>
          <a-button 
            type="link" 
            size="small" 
            @click="minimizeCurrentFile"
            v-if="currentGeneratingFile.completed"
          >
            <MinusOutlined />
          </a-button>
        </div>
      </div>
      
      <div class="code-content">
        <pre class="code-stream" :class="`language-${currentGeneratingFile.language}`">
          <code>{{ currentGeneratingFile.content }}</code>
        </pre>
        <div 
          class="typing-cursor" 
          v-if="!currentGeneratingFile.completed"
        >|</div>
      </div>
    </div>
    
    <!-- 已完成的文件折叠面板 -->
    <div class="completed-files" v-if="completedFiles.length">
      <a-collapse v-model:activeKey="activeFileKeys" ghost>
        <a-collapse-panel 
          v-for="file in completedFiles" 
          :key="file.id"
        >
          <template #header>
            <div class="file-panel-header">
              <FileOutlined class="file-icon" />
              <span class="file-name">{{ file.name }}</span>
              <a-tag size="small" :color="getLanguageColor(file.language)">
                {{ file.language }}
              </a-tag>
              <span class="file-path">{{ file.path }}</span>
            </div>
          </template>
          
          <pre class="code-content" :class="`language-${file.language}`">
            <code>{{ file.content }}</code>
          </pre>
        </a-collapse-panel>
      </a-collapse>
    </div>
  </div>
</template>
```

### 4. 样式统一性

**工具标签颜色统一：**
```scss
.tool-tag {
  &.write-file { background-color: #1890ff; }
  &.read-file { background-color: #52c41a; }
  &.modify-file { background-color: #fa8c16; }
  &.delete-file { background-color: #ff4d4f; }
  &.read-dir { background-color: #722ed1; }
}

.language-tag {
  &.javascript, &.js { background-color: #f7df1e; color: #000; }
  &.typescript, &.ts { background-color: #3178c6; color: #fff; }
  &.vue { background-color: #4fc08d; color: #fff; }
  &.html { background-color: #e34c26; color: #fff; }
  &.css { background-color: #1572b6; color: #fff; }
  &.json { background-color: #292929; color: #fff; }
}
```

这样设计确保了前后端数据的完美对应，界面展示与后端工具返回值保持高度一致性。

## 技术实现要点

1. **保持现有功能**: 不影响当前的AI对话、预览、部署等核心功能
2. **渐进式改造**: 可分步实施，每个阶段都保持系统可用性
3. **组件化设计**: 新功能尽量抽象为独立组件便于维护
4. **性能优化**: 大量代码内容需要虚拟滚动等性能优化手段
5. **状态管理**: 合理管理生成过程中的各种状态数据
6. **内容解析**: 准确解析后端工具返回的不同格式内容

## 风险评估

- **复杂度**: 中等，主要是界面重构和数据流改造
- **兼容性**: 需要保持与现有后端API的兼容
- **性能**: 大量DOM操作需要优化，特别是代码高亮部分
- **用户体验**: 需要平滑的过渡动画和加载状态

通过以上改造，可以实现一个更直观、专业的AI代码生成界面，用户能够清晰地看到每个步骤的进展和生成的文件内容。

## 健壮性增强：错误处理与边界情况

### 1. 全局错误处理机制

**错误类型分类与处理策略：**
```typescript
enum ErrorType {
  PARSE_ERROR = 'parse_error',           // 解析错误
  NETWORK_ERROR = 'network_error',       // 网络连接错误
  SSE_ERROR = 'sse_error',              // 服务器推送错误
  CONTENT_ERROR = 'content_error',       // 内容错误
  STATE_ERROR = 'state_error',          // 状态错误
  TIMEOUT_ERROR = 'timeout_error'        // 超时错误
}

interface AppError {
  type: ErrorType
  message: string
  details?: any
  timestamp: string
  recoverable: boolean
  userMessage: string // 用户友好的错误描述
  suggestions?: string[] // 恢复建议
}

class ErrorHandler {
  private errorQueue: AppError[] = []
  private maxErrorHistory = 100
  
  handleError(error: AppError) {
    // 记录错误
    this.errorQueue.push(error)
    if (this.errorQueue.length > this.maxErrorHistory) {
      this.errorQueue.shift()
    }
    
    // 根据错误类型决定处理方式
    switch (error.type) {
      case ErrorType.PARSE_ERROR:
        this.handleParseError(error)
        break
      case ErrorType.NETWORK_ERROR:
        this.handleNetworkError(error)
        break
      case ErrorType.SSE_ERROR:
        this.handleSSEError(error)
        break
      default:
        this.handleGenericError(error)
    }
  }
  
  private handleParseError(error: AppError) {
    // 解析错误通常是可恢复的，显示警告即可
    message.warning(`解析失败: ${error.userMessage}`)
    console.warn('Parse error:', error)
  }
  
  private handleNetworkError(error: AppError) {
    // 网络错误需要用户关注，提供重试选项
    message.error(error.userMessage, 10)
    this.showRetryDialog(error)
  }
  
  private handleSSEError(error: AppError) {
    // SSE错误可能需要重新连接
    message.error(`连接失败: ${error.userMessage}`)
    if (error.recoverable) {
      this.scheduleReconnection()
    }
  }
  
  private handleGenericError(error: AppError) {
    message.error(error.userMessage)
    console.error('Generic error:', error)
  }
  
  private showRetryDialog(error: AppError) {
    // 显示重试对话框的逻辑
    Modal.confirm({
      title: '连接失败',
      content: error.userMessage + '\n是否重试？',
      onOk: () => this.retry(error),
      okText: '重试',
      cancelText: '取消'
    })
  }
  
  private retry(error: AppError) {
    // 重试逻辑
    console.log('Retrying after error:', error)
  }
  
  private scheduleReconnection() {
    // 自动重连逻辑
    setTimeout(() => {
      console.log('Attempting to reconnect...')
    }, 3000)
  }
  
  getErrorHistory(): AppError[] {
    return [...this.errorQueue]
  }
}
```

### 2. 流式内容处理的健壮性增强

**智能内容缓冲与分片处理：**
```typescript
class RobustStreamProcessor {
  private contentBuffer: string = ''
  private processingLock: boolean = false
  private heartbeatTimer: NodeJS.Timeout | null = null
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5
  private errorHandler: ErrorHandler
  
  constructor() {
    this.errorHandler = new ErrorHandler()
  }
  
  async processStreamChunk(chunk: string): Promise<void> {
    // 防重入处理
    if (this.processingLock) {
      await this.waitForUnlock()
    }
    
    this.processingLock = true
    
    try {
      // 输入验证
      if (!this.validateChunk(chunk)) {
        throw new Error('Invalid chunk received')
      }
      
      // 添加到缓冲区
      this.contentBuffer += chunk
      
      // 尝试解析完整的消息
      await this.parseCompleteMessages()
      
      // 重置心跳
      this.resetHeartbeat()
      
    } catch (error) {
      this.errorHandler.handleError({
        type: ErrorType.CONTENT_ERROR,
        message: `Stream processing failed: ${error}`,
        details: { chunk, buffer: this.contentBuffer },
        timestamp: new Date().toISOString(),
        recoverable: true,
        userMessage: '内容处理失败，正在尝试恢复'
      })
    } finally {
      this.processingLock = false
    }
  }
  
  private validateChunk(chunk: string): boolean {
    // 验证块的有效性
    if (typeof chunk !== 'string') {
      return false
    }
    
    if (chunk.length > 10000) { // 单个块过大
      this.errorHandler.handleError({
        type: ErrorType.CONTENT_ERROR,
        message: 'Chunk too large',
        timestamp: new Date().toISOString(),
        recoverable: true,
        userMessage: '接收的数据块过大',
        suggestions: ['检查网络连接', '重新开始生成']
      })
      return false
    }
    
    return true
  }
  
  private async parseCompleteMessages(): Promise<void> {
    // 查找完整的消息边界
    const messagePatterns = [
      /\[选择工具\][^\[]*(?=\[|$)/g,
      /\[工具调用\][^\[]*(?=\[|$)/g,
      /STEP\s*\d+[:：][^\[]*(?=\[|$)/g,
      /```[\w]*\n[\s\S]*?\n```/g
    ]
    
    for (const pattern of messagePatterns) {
      const matches = this.contentBuffer.match(pattern)
      if (matches) {
        for (const match of matches) {
          await this.processCompleteMessage(match)
          // 从缓冲区移除已处理的内容
          this.contentBuffer = this.contentBuffer.replace(match, '')
        }
      }
    }
  }
  
  private async processCompleteMessage(message: string): Promise<void> {
    try {
      const parser = new ContentParser()
      const result = parser.parseStreamContent(message)
      
      if (result.error) {
        throw new Error(`Parse result error: ${result.data?.message}`)
      }
      
      // 触发相应的事件处理
      await this.handleParseResult(result)
      
    } catch (error) {
      this.errorHandler.handleError({
        type: ErrorType.PARSE_ERROR,
        message: `Message parsing failed: ${error}`,
        details: { message },
        timestamp: new Date().toISOString(),
        recoverable: true,
        userMessage: '消息解析失败'
      })
    }
  }
  
  private async handleParseResult(result: ParseResult): Promise<void> {
    // 根据解析结果类型进行处理
    switch (result.type) {
      case 'step-start':
        await this.handleStepStart(result.data)
        break
      case 'tool-selection':
        await this.handleToolSelection(result.data)
        break
      case 'file-start':
        await this.handleFileStart(result.data)
        break
      case 'code-stream':
        await this.handleCodeStream(result.data)
        break
      default:
        console.log('Unhandled parse result:', result)
    }
  }
  
  private resetHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearTimeout(this.heartbeatTimer)
    }
    
    // 30秒无数据则认为连接可能有问题
    this.heartbeatTimer = setTimeout(() => {
      this.errorHandler.handleError({
        type: ErrorType.TIMEOUT_ERROR,
        message: 'Stream timeout - no data received',
        timestamp: new Date().toISOString(),
        recoverable: true,
        userMessage: '长时间无响应，连接可能已断开'
      })
    }, 30000)
  }
  
  private async waitForUnlock(): Promise<void> {
    return new Promise((resolve) => {
      const checkLock = () => {
        if (!this.processingLock) {
          resolve()
        } else {
          setTimeout(checkLock, 50)
        }
      }
      checkLock()
    })
  }
}
```

### 3. 状态管理的健壮性

**状态一致性保障机制：**
```typescript
interface StateSnapshot {
  generationSteps: GenerationStep[]
  currentGeneratingFile: GeneratedFile | null
  completedFiles: GeneratedFile[]
  timestamp: string
  version: number
}

class RobustStateManager {
  private stateHistory: StateSnapshot[] = []
  private maxHistorySize: number = 10
  private currentVersion: number = 0
  private subscribers: Array<(state: StateSnapshot) => void> = []
  
  saveStateSnapshot(state: {
    generationSteps: GenerationStep[],
    currentGeneratingFile: GeneratedFile | null,
    completedFiles: GeneratedFile[]
  }): void {
    const snapshot: StateSnapshot = {
      ...state,
      timestamp: new Date().toISOString(),
      version: ++this.currentVersion
    }
    
    // 深拷贝防止引用问题
    const clonedSnapshot = JSON.parse(JSON.stringify(snapshot))
    
    this.stateHistory.push(clonedSnapshot)
    
    if (this.stateHistory.length > this.maxHistorySize) {
      this.stateHistory.shift()
    }
    
    // 通知订阅者
    this.notifySubscribers(clonedSnapshot)
  }
  
  restoreLastValidState(): StateSnapshot | null {
    // 找到最近的有效状态
    for (let i = this.stateHistory.length - 1; i >= 0; i--) {
      const state = this.stateHistory[i]
      if (this.validateState(state)) {
        return state
      }
    }
    return null
  }
  
  private validateState(state: StateSnapshot): boolean {
    try {
      // 验证状态的完整性
      if (!Array.isArray(state.generationSteps)) return false
      
      // 验证步骤编号的连续性
      for (let i = 0; i < state.generationSteps.length; i++) {
        const step = state.generationSteps[i]
        if (!step.id || !step.title || typeof step.number !== 'number') {
          return false
        }
      }
      
      // 验证文件对象的完整性
      if (state.currentGeneratingFile) {
        if (!state.currentGeneratingFile.id || !state.currentGeneratingFile.name) {
          return false
        }
      }
      
      for (const file of state.completedFiles) {
        if (!file.id || !file.name || !file.path) {
          return false
        }
      }
      
      return true
    } catch (error) {
      console.error('State validation error:', error)
      return false
    }
  }
  
  rollbackToValidState(): boolean {
    const validState = this.restoreLastValidState()
    if (validState) {
      // 这里需要触发 Vue 的状态更新
      this.restoreVueState(validState)
      return true
    }
    return false
  }
  
  private restoreVueState(state: StateSnapshot): void {
    // 这个方法需要在 Vue 组件中实现
    // 用于恢复 ref 值到快照状态
    console.log('Restoring state to:', state)
  }
  
  subscribe(callback: (state: StateSnapshot) => void): void {
    this.subscribers.push(callback)
  }
  
  private notifySubscribers(state: StateSnapshot): void {
    this.subscribers.forEach(callback => {
      try {
        callback(state)
      } catch (error) {
        console.error('Subscriber notification error:', error)
      }
    })
  }
  
  clearHistory(): void {
    this.stateHistory = []
    this.currentVersion = 0
  }
  
  getStateHistory(): StateSnapshot[] {
    return [...this.stateHistory]
  }
}
```

### 4. 网络连接健壮性

**自动重连与降级策略：**
```typescript
class RobustSSEManager {
  private eventSource: EventSource | null = null
  private reconnectTimer: NodeJS.Timeout | null = null
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5
  private backoffMultiplier: number = 1.5
  private baseReconnectDelay: number = 1000
  private connectionState: 'disconnected' | 'connecting' | 'connected' | 'error' = 'disconnected'
  private messageQueue: Array<{ timestamp: string, content: string }> = []
  private heartbeatInterval: NodeJS.Timeout | null = null
  private lastMessageTime: number = 0
  
  async connect(url: string, onMessage: (data: string) => void): Promise<boolean> {
    try {
      this.connectionState = 'connecting'
      this.cleanup() // 清理之前的连接
      
      this.eventSource = new EventSource(url, {
        withCredentials: true
      })
      
      // 连接成功
      this.eventSource.onopen = () => {
        console.log('SSE connection established')
        this.connectionState = 'connected'
        this.reconnectAttempts = 0
        this.startHeartbeat()
      }
      
      // 消息处理
      this.eventSource.onmessage = (event) => {
        this.lastMessageTime = Date.now()
        this.messageQueue.push({
          timestamp: new Date().toISOString(),
          content: event.data
        })
        
        try {
          onMessage(event.data)
        } catch (error) {
          console.error('Message handler error:', error)
        }
      }
      
      // 错误处理
      this.eventSource.onerror = (error) => {
        console.error('SSE connection error:', error)
        this.connectionState = 'error'
        this.handleConnectionError()
      }
      
      // 自定义事件处理
      this.eventSource.addEventListener('done', () => {
        console.log('Generation completed')
        this.cleanup()
      })
      
      return true
      
    } catch (error) {
      console.error('Failed to establish SSE connection:', error)
      this.connectionState = 'error'
      return false
    }
  }
  
  private handleConnectionError(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      const delay = this.calculateBackoffDelay()
      console.log(`Attempting reconnection in ${delay}ms (attempt ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts})`)
      
      this.reconnectTimer = setTimeout(() => {
        this.reconnectAttempts++
        this.attemptReconnection()
      }, delay)
    } else {
      console.error('Max reconnection attempts reached, giving up')
      this.connectionState = 'disconnected'
      this.showConnectionFailedMessage()
    }
  }
  
  private calculateBackoffDelay(): number {
    return Math.min(
      this.baseReconnectDelay * Math.pow(this.backoffMultiplier, this.reconnectAttempts),
      30000 // 最大延迟30秒
    )
  }
  
  private async attemptReconnection(): Promise<void> {
    console.log(`Reconnection attempt ${this.reconnectAttempts}`)
    // 这里需要重新获取URL和消息处理器
    // 实际实现需要在组件中保存这些参数
  }
  
  private startHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
    }
    
    this.heartbeatInterval = setInterval(() => {
      const timeSinceLastMessage = Date.now() - this.lastMessageTime
      
      if (timeSinceLastMessage > 60000) { // 60秒无消息
        console.warn('No messages received for 60 seconds, connection may be stale')
        if (this.connectionState === 'connected') {
          this.connectionState = 'error'
          this.handleConnectionError()
        }
      }
    }, 30000) // 每30秒检查一次
  }
  
  private showConnectionFailedMessage(): void {
    Modal.error({
      title: '连接失败',
      content: '无法连接到服务器，请检查网络连接后重试。',
      onOk: () => {
        // 可以提供手动重试选项
        this.resetConnection()
      }
    })
  }
  
  resetConnection(): void {
    this.cleanup()
    this.reconnectAttempts = 0
    this.connectionState = 'disconnected'
  }
  
  private cleanup(): void {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
    
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }
  
  getConnectionState(): string {
    return this.connectionState
  }
  
  getMessageHistory(): Array<{ timestamp: string, content: string }> {
    return [...this.messageQueue]
  }
  
  disconnect(): void {
    this.cleanup()
    this.connectionState = 'disconnected'
  }
}
```

### 5. 用户体验优化的容错机制

**界面状态管理与降级显示：**
```typescript
interface UIState {
  isLoading: boolean
  error: AppError | null
  connectionStatus: 'connected' | 'disconnected' | 'reconnecting'
  generationProgress: number
  lastUpdateTime: string
  retryAvailable: boolean
}

class UIStateManager {
  private uiState: Ref<UIState>
  private fallbackTimer: NodeJS.Timeout | null = null
  
  constructor() {
    this.uiState = ref({
      isLoading: false,
      error: null,
      connectionStatus: 'disconnected',
      generationProgress: 0,
      lastUpdateTime: '',
      retryAvailable: true
    })
  }
  
  updateLoadingState(loading: boolean): void {
    this.uiState.value.isLoading = loading
    
    if (loading) {
      // 设置超时保护
      this.fallbackTimer = setTimeout(() => {
        if (this.uiState.value.isLoading) {
          this.showTimeoutWarning()
        }
      }, 120000) // 2分钟超时警告
    } else {
      this.clearFallbackTimer()
    }
  }
  
  updateConnectionStatus(status: 'connected' | 'disconnected' | 'reconnecting'): void {
    this.uiState.value.connectionStatus = status
    this.uiState.value.lastUpdateTime = new Date().toISOString()
  }
  
  updateProgress(progress: number): void {
    this.uiState.value.generationProgress = Math.max(0, Math.min(100, progress))
  }
  
  setError(error: AppError | null): void {
    this.uiState.value.error = error
    this.uiState.value.retryAvailable = error?.recoverable ?? false
  }
  
  private showTimeoutWarning(): void {
    message.warning('生成过程耗时较长，请耐心等待...', 10)
    
    // 提供取消选项
    Modal.confirm({
      title: '生成超时',
      content: '当前生成过程耗时较长，是否继续等待？',
      onOk: () => {
        // 继续等待，重置计时器
        this.resetFallbackTimer()
      },
      onCancel: () => {
        // 用户选择取消
        this.cancelGeneration()
      },
      okText: '继续等待',
      cancelText: '取消生成'
    })
  }
  
  private resetFallbackTimer(): void {
    this.clearFallbackTimer()
    this.fallbackTimer = setTimeout(() => {
      this.showTimeoutWarning()
    }, 120000)
  }
  
  private cancelGeneration(): void {
    this.uiState.value.isLoading = false
    this.clearFallbackTimer()
    // 触发取消事件
    this.emitCancelEvent()
  }
  
  private clearFallbackTimer(): void {
    if (this.fallbackTimer) {
      clearTimeout(this.fallbackTimer)
      this.fallbackTimer = null
    }
  }
  
  private emitCancelEvent(): void {
    // 在实际组件中需要实现取消逻辑
    console.log('Generation cancelled by user')
  }
  
  getState(): UIState {
    return this.uiState.value
  }
  
  reset(): void {
    this.clearFallbackTimer()
    this.uiState.value = {
      isLoading: false,
      error: null,
      connectionStatus: 'disconnected',
      generationProgress: 0,
      lastUpdateTime: '',
      retryAvailable: true
    }
  }
}
```







