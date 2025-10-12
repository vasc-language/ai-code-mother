import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { CodeGenTypeEnum } from '@/utils/codeGenTypes'
import {
  filterHtmlContent,
  filterOutCodeBlocks,
  formatVueProjectContent,
} from '../utils/contentFilters'

// 生成的文件接口
export interface GeneratedFile {
  id: string
  name: string
  content: string
  language: string
  path?: string
}

/**
 * 代码生成管理Composable
 * 负责SSE流式代码生成、文件解析和状态管理
 */
export function useCodeGeneration() {
  // 生成状态
  const isGenerating = ref(false)
  const generationFinished = ref(false)
  const abortController = ref<AbortController | null>(null)

  // 文件管理
  const simpleCodeFile = ref<GeneratedFile | null>(null)
  const multiFiles = ref<GeneratedFile[]>([])
  const activeMultiFileKey = ref('')

  // 预览相关
  const previewUrl = ref('')
  const previewReady = ref(false)

  // ✅ 性能监控
  interface SSEMetrics {
    runId: string | null
    codeGenType: string
    t0: number // 开始时间
    t1?: number // 连接打开时间
    t2?: number // 首个消息到达时间
    totalBytes: number
    totalChunks: number
  }
  const sseMetrics: SSEMetrics = {
    runId: null,
    codeGenType: '',
    t0: 0,
    totalBytes: 0,
    totalChunks: 0,
  }

  /**
   * 去除代码块标记（用于还原原始代码）
   */
  const stripCodeMarkers = (content: string): string => {
    if (!content) return ''
    return content
      .replace(/\r/g, '')
      .replace(/\[CODE_BLOCK_START\]/g, '')
      .replace(/\[CODE_STREAM\]/g, '')
      .replace(/\[CODE_BLOCK_END\]/g, '')
  }

  /**
   * 清理单个代码片段，移除围栏与标记
   */
  const cleanupCodeChunk = (chunk: string): string => {
    if (!chunk) return ''
    return stripCodeMarkers(chunk)
      .replace(/```[\w-]*\n?/g, '')
      .replace(/```/g, '')
  }

  /**
   * 从流式内容提取 HTML 代码
   */
  const extractHtmlFromStream = (content: string): string => {
    const cleaned = stripCodeMarkers(content)
    if (!cleaned) return ''

    const fencedMatch = cleaned.match(/```[a-zA-Z-]*\n?([\s\S]*?)```/)
    if (fencedMatch && fencedMatch[1]) {
      return fencedMatch[1].trim()
    }

    if (cleaned.includes('<html') || cleaned.includes('<!DOCTYPE') || cleaned.includes('<body')) {
      return cleaned.trim()
    }

    return ''
  }

  /**
   * 开始生成代码（SSE流式）
   */
  const startCodeGeneration = async (
    appId: any,
    userMessage: string,
    codeGenType: string | undefined,
    onMessageUpdate: (content: string) => void,
    onComplete: () => void,
    modelKey?: string
  ) => {
    if (!appId || !userMessage) return

    isGenerating.value = true
    generationFinished.value = false
    previewReady.value = false

    // 创建新的AbortController
    abortController.value = new AbortController()

    let accumulatedContent = ''

    try {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

      // 生成 runId (用于后端追踪)
      const runId = (crypto as any)?.randomUUID?.() || `run_${Date.now()}_${Math.random().toString(36).slice(2)}`

      // 构建URL参数
      const params = new URLSearchParams({
        appId: String(appId),
        message: userMessage,
        runId: runId
      })

      // 添加 modelKey (可选)
      if (modelKey) {
        params.append('modelKey', modelKey)
      }

      const url = `${baseUrl}/app/chat/gen/code?${params}`
      console.log('[代码生成] 开始连接:', url)

      await fetchEventSource(url, {
        method: 'GET',
        headers: {
          Accept: 'text/event-stream',
        },
        signal: abortController.value.signal,
        credentials: 'include', // 携带 Cookie (重要!)

        onopen: async (response) => {
          if (response.ok) {
            // ✅ 记录连接打开时间
            sseMetrics.t1 = performance.now()
            console.log('[代码生成] SSE连接已建立')
          } else {
            const errorText = await response.text()
            console.error('[代码生成] SSE连接失败:', response.status, errorText)
            throw new Error(`SSE连接失败: ${response.status} - ${errorText}`)
          }
        },

        onmessage: (event) => {
          // 处理 done 事件
          if (event.event === 'done') {
            console.log('[代码生成] 收到done事件，开始解析内容')

            // ✅ 打印性能指标
            printSSEMetrics('done')

            // 解析生成的内容
            parseGeneratedContent(accumulatedContent, codeGenType).then(() => {
              generationFinished.value = true
              onComplete()
              console.log('[代码生成] 内容解析完成')
            })
            return
          }

          // 处理 interrupted 事件
          if (event.event === 'interrupted') {
            console.warn('[代码生成] 生成被中断')

            // ✅ 打印性能指标
            printSSEMetrics('interrupted')

            message.warning('生成已被后台中断，请重试')
            isGenerating.value = false
            return
          }

          // 处理数据消息
          if (!event.data) {
            return
          }

          try {
            const payload = JSON.parse(event.data)
            const chunk = payload?.d || ''
            if (!chunk) {
              return
            }

            // ✅ 记录首个消息到达时间
            if (!sseMetrics.t2) {
              sseMetrics.t2 = performance.now()
            }

            // ✅ 更新性能指标
            sseMetrics.totalBytes += chunk.length
            sseMetrics.totalChunks += 1

            accumulatedContent += chunk

            // ❌ 禁用流式文件解析，避免解析不完整的内容
            // 只在 done 事件时通过 parseGeneratedContent 一次性解析完整内容
            // parseStreamingContent(chunk, accumulatedContent, codeGenType)

            // ✅ 根据 codeGenType 过滤内容后更新聊天面板（只过滤显示，不解析文件）
            let filteredContent = accumulatedContent
            if (codeGenType === CodeGenTypeEnum.HTML) {
              filteredContent = filterHtmlContent(accumulatedContent)
            } else if (codeGenType === CodeGenTypeEnum.MULTI_FILE) {
              filteredContent = filterOutCodeBlocks(accumulatedContent)
            } else if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
              filteredContent = formatVueProjectContent(accumulatedContent)
            }
            onMessageUpdate(filteredContent)
          } catch (error) {
            console.error('[代码生成] 解析SSE消息失败:', error, event.data)
          }
        },

        onerror: (error) => {
          console.error('SSE连接错误:', error)
          throw error
        },

        onclose: () => {
          console.log('[代码生成] SSE连接已关闭')
        },
      })

      // fetchEventSource 会在连接关闭后自动结束
      // 所有清理工作都在 done 事件中处理

    } catch (error: any) {
      if (error.name === 'AbortError') {
        console.log('代码生成已停止')
        message.info('已停止生成')
      } else {
        console.error('代码生成失败:', error)
        message.error('代码生成失败')
      }
    } finally {
      isGenerating.value = false
      abortController.value = null
    }
  }

  /**
   * 停止代码生成
   */
  const stopGeneration = () => {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
      isGenerating.value = false
      message.info('已停止生成')
    }
  }

  /**
   * 解析生成的内容
   */
  const parseGeneratedContent = async (content: string, codeGenType: string | undefined) => {
    if (!content || !codeGenType) return

    try {
      // ✅ 调试：打印原始内容的前500和后500字符
      console.log('[调试] 原始内容长度:', content.length)
      console.log('[调试] 原始内容开头500字符:', content.substring(0, 500))
      console.log('[调试] 原始内容结尾500字符:', content.substring(content.length - 500))

      if (codeGenType === CodeGenTypeEnum.HTML) {
        const htmlContent = extractHtmlFromStream(content)
        if (htmlContent) {
          simpleCodeFile.value = {
            id: 'html-file',
            name: 'index.html',
            content: htmlContent,
            language: 'html',
          }
          createPreviewUrl(htmlContent)
        }
      } else if (codeGenType === CodeGenTypeEnum.MULTI_FILE || codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
        const files = parseMultiFileContent(content)
        multiFiles.value = files

        // ✅ 调试：打印解析后的文件信息
        console.log('[调试] 解析后的文件数量:', files.length)
        files.forEach((file, index) => {
          console.log(`[调试] 文件${index + 1}: ${file.name}, 内容长度: ${file.content.length}`)
          console.log(`[调试] 文件${index + 1}的前200字符:`, file.content.substring(0, 200))
        })

        if (files.length > 0) {
          activeMultiFileKey.value = files[0].id

          const indexFile = files.find(f => f.name === 'index.html')
          if (indexFile) {
            createPreviewUrl(indexFile.content)
          }
        }
      }
    } catch (error) {
      console.error('解析生成内容失败:', error)
      message.error('解析代码失败')
    }
  }

  /**
   * 解析多文件内容
   */
  const parseMultiFileContent = (content: string): GeneratedFile[] => {
    const files: GeneratedFile[] = []

    const fileBuffers = new Map<string, string[]>()
    let currentFile: string | null = null
    let cursor = 0
    const markerRegex = /\[MULTI_FILE_(START|CONTENT|END):([^\]]+)\]/g

    const appendToCurrentFile = (snippet: string) => {
      if (!currentFile) return
      const cleaned = cleanupCodeChunk(snippet)
      console.log(`[调试] appendToCurrentFile - 文件: ${currentFile}, 原始长度: ${snippet.length}, 清理后长度: ${cleaned.length}`)
      if (!cleaned.trim()) return
      if (!fileBuffers.has(currentFile)) {
        fileBuffers.set(currentFile, [])
      }
      fileBuffers.get(currentFile)!.push(cleaned)
    }

    let match
    while ((match = markerRegex.exec(content)) !== null) {
      const [, markerType, rawFilename] = match
      const filename = rawFilename.trim()

      console.log(`[调试] 匹配到标记: [MULTI_FILE_${markerType}:${filename}], 位置: ${match.index}`)

      const preceding = content.slice(cursor, match.index)
      if (preceding) {
        console.log(`[调试] 提取preceding内容, 长度: ${preceding.length}, 内容前100字符:`, preceding.substring(0, 100))
        appendToCurrentFile(preceding)
      }

      if (markerType === 'START' || markerType === 'CONTENT') {
        currentFile = filename
        if (!fileBuffers.has(filename)) {
          fileBuffers.set(filename, [])
        }
      } else if (markerType === 'END') {
        appendToCurrentFile('')
        currentFile = null
      }

      cursor = markerRegex.lastIndex
    }

    const tail = content.slice(cursor)
    if (tail) {
      console.log(`[调试] 处理尾部内容, 长度: ${tail.length}`)
      appendToCurrentFile(tail)
    }

    console.log(`[调试] fileBuffers数量: ${fileBuffers.size}`)
    fileBuffers.forEach((chunks, filename) => {
      console.log(`[调试] 文件 ${filename} 有 ${chunks.length} 个chunk`)
      const code = chunks.join('').trim()
      console.log(`[调试] 文件 ${filename} 合并后长度: ${code.length}`)
      if (!code) return
      files.push({
        id: `file-${files.length}`,
        name: filename,
        path: filename,
        content: code,
        language: getLanguageFromFilename(filename),
      })
    })

    if (files.length === 0) {
      // 解析 AI 工具输出格式：[工具调用] 写入文件 xxx ```lang ...```
      // 使用更robust的解析策略
      console.log('[调试] 尝试解析工具调用格式')
      const toolBuffers = new Map<string, { chunks: string[]; language?: string }>()

      // 方法1：优先使用标准的工具调用格式
      const toolRegex = /\[工具调用\]\s+[^\s]+\s+([^\s]+)\s*```([\w-]*)?\n([\s\S]*?)```/g
      let toolMatch: RegExpExecArray | null
      let matchCount = 0

      while ((toolMatch = toolRegex.exec(content)) !== null) {
        matchCount++
        const [fullMatch, rawFilename, languageHint, rawCode] = toolMatch
        const filename = rawFilename.trim()
        console.log(`[调试] 工具调用匹配${matchCount}: 文件=${filename}, 语言=${languageHint || '无'}, 代码长度=${rawCode.length}`)

        if (!filename) {
          console.warn('[调试] 文件名为空，跳过')
          continue
        }
        if (!toolBuffers.has(filename)) {
          toolBuffers.set(filename, { chunks: [], language: languageHint || undefined })
        }
        const entry = toolBuffers.get(filename)!
        entry.chunks.push(rawCode.replace(/\r/g, ''))
        if (languageHint) {
          entry.language = languageHint
        }
      }

      console.log(`[调试] 工具调用格式匹配到 ${matchCount} 个文件`)

      if (toolBuffers.size > 0) {
        toolBuffers.forEach((value, filename) => {
          const code = value.chunks.join('\n').trim()
          console.log(`[调试] 合并文件 ${filename}, 最终长度: ${code.length}`)
          if (!code) {
            console.warn(`[调试] 文件 ${filename} 内容为空，跳过`)
            return
          }
          files.push({
            id: `file-${files.length}`,
            name: filename,
            path: filename,
            content: code,
            language: value.language || getLanguageFromFilename(filename),
          })
        })
      }
    }

    if (files.length === 0) {
      // 回退到旧格式解析，兼容历史回复
      console.log('[调试] 尝试旧格式解析（### 文件名）')
      const fallbackFiles = [] as GeneratedFile[]
      const fileRegex = /###\s+([\w\/.]+)\s*\n```(\w+)?\n([\s\S]*?)```/g
      let fallbackMatch
      while ((fallbackMatch = fileRegex.exec(content)) !== null) {
        const [, filename, language, code] = fallbackMatch
        console.log(`[调试] 旧格式匹配: 文件=${filename}, 语言=${language || '无'}, 代码长度=${code.length}`)
        fallbackFiles.push({
          id: `file-${fallbackFiles.length}`,
          name: filename.trim(),
          content: code.trim(),
          language: language || getLanguageFromFilename(filename),
          path: filename.trim(),
        })
      }

      if (fallbackFiles.length > 0) {
        console.log(`[调试] 旧格式解析成功，找到 ${fallbackFiles.length} 个文件`)
        return fallbackFiles
      }

      // 最后的兜底方案：提取所有代码块
      console.warn('[调试] 所有格式解析失败，尝试提取所有代码块')
      const emergencyFiles = [] as GeneratedFile[]
      const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g
      let blockMatch
      let blockIndex = 0
      while ((blockMatch = codeBlockRegex.exec(content)) !== null) {
        blockIndex++
        const [, language, code] = blockMatch
        if (code.trim()) {
          const filename = `file_${blockIndex}.${language || 'txt'}`
          console.log(`[调试] 提取代码块${blockIndex}: 文件=${filename}, 代码长度=${code.length}`)
          emergencyFiles.push({
            id: `emergency-${blockIndex}`,
            name: filename,
            content: code.trim(),
            language: language || 'plaintext',
            path: filename,
          })
        }
      }

      if (emergencyFiles.length > 0) {
        console.warn(`[调试] 兜底方案提取了 ${emergencyFiles.length} 个代码块`)
        return emergencyFiles
      }

      console.error('[调试] 无法解析任何文件！原始内容长度:', content.length)
      return fallbackFiles
    }

    return files
  }

  /**
   * 根据文件名推断语言
   */
  const getLanguageFromFilename = (filename: string): string => {
    const ext = filename.split('.').pop()?.toLowerCase()
    const languageMap: Record<string, string> = {
      html: 'html',
      css: 'css',
      js: 'javascript',
      ts: 'typescript',
      vue: 'vue',
      json: 'json',
      md: 'markdown',
      py: 'python',
      java: 'java',
    }
    return languageMap[ext || ''] || 'plaintext'
  }

  /**
   * 创建预览URL（Blob URL）
   */
  const createPreviewUrl = (htmlContent: string) => {
    try {
      // 清理旧的Blob URL
      if (previewUrl.value) {
        URL.revokeObjectURL(previewUrl.value)
      }

      // 创建新的Blob URL
      const blob = new Blob([htmlContent], { type: 'text/html' })
      previewUrl.value = URL.createObjectURL(blob)
      previewReady.value = true
    } catch (error) {
      console.error('创建预览URL失败:', error)
    }
  }

  /**
   * ✅ 打印SSE性能指标
   */
  const printSSEMetrics = (eventType: string) => {
    try {
      const now = performance.now()
      const t0 = sseMetrics.t0
      const t1 = sseMetrics.t1 || now
      const t2 = sseMetrics.t2 || now

      const ttftOpen = Math.round(t1 - t0) // Time To First Token (连接)
      const ttftMsg = Math.round(t2 - t0) // Time To First Token (消息)
      const duration = Math.max(1, (now - t0) / 1000) // 总持续时间 (秒)
      const avgBytesPerSec = Math.round((sseMetrics.totalBytes / duration) * 100) / 100

      console.info(`[SSE-METRICS][${eventType}]`, {
        runId: sseMetrics.runId,
        codeGenType: sseMetrics.codeGenType,
        ttftOpenMs: ttftOpen,
        ttftFirstMsgMs: ttftMsg,
        totalDurationMs: Math.round(now - t0),
        totalBytes: sseMetrics.totalBytes,
        totalChunks: sseMetrics.totalChunks,
        avgBytesPerSec,
      })
    } catch (error) {
      console.warn('[SSE-METRICS] 打印性能指标失败:', error)
    }
  }

  /**
   * 清理资源
   */
  const cleanup = () => {
    stopGeneration()

    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
    }

    simpleCodeFile.value = null
    multiFiles.value = []
    activeMultiFileKey.value = ''
    generationFinished.value = false
    previewReady.value = false
  }

  return {
    // 状态
    isGenerating,
    generationFinished,

    // 文件
    simpleCodeFile,
    multiFiles,
    activeMultiFileKey,

    // 预览
    previewUrl,
    previewReady,

    // 方法
    startCodeGeneration,
    stopGeneration,
    parseGeneratedContent,
    createPreviewUrl,
    cleanup,
  }
}
