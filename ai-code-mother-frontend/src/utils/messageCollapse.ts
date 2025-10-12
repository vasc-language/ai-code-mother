/**
 * 消息折叠工具
 * 用于折叠AI消息中的工具调用和修改文件内容
 */

export interface MessageStats {
  toolCallCount: number      // 工具调用次数
  fileEditCount: number       // 文件修改次数
  totalCount: number          // 总计
}

/**
 * 统计消息容器中的工具调用和修改文件数量
 */
export function countMessageActions(messageElement: Element): MessageStats {
  // 统计工具调用：查找所有包含 <strong> 且以 [工具调用] 开头的段落
  const toolCallElements = messageElement.querySelectorAll('p > strong:first-child')
  let toolCallCount = 0

  toolCallElements.forEach((el) => {
    const text = el.textContent?.trim() || ''
    if (text.startsWith('[工具调用]')) {
      toolCallCount++
    }
  })

  // 统计修改文件：查找所有 .diff-container
  const fileEditCount = messageElement.querySelectorAll('.diff-container').length

  return {
    toolCallCount,
    fileEditCount,
    totalCount: toolCallCount + fileEditCount
  }
}

/**
 * 生成摘要栏HTML
 */
export function generateSummaryBarHtml(stats: MessageStats, containerId: string): string {
  const { toolCallCount, fileEditCount, totalCount } = stats

  if (totalCount === 0) {
    return '' // 没有工具调用或文件编辑，不显示摘要栏
  }

  // 生成统计文本
  let statsText = ''
  if (fileEditCount > 0 && toolCallCount > 0) {
    statsText = `${fileEditCount} edits made, ${toolCallCount} tool calls`
  } else if (fileEditCount > 0) {
    statsText = `${fileEditCount} edits made`
  } else {
    statsText = `${toolCallCount} tool calls`
  }

  return `
    <div class="message-summary-bar" data-container-id="${containerId}">
      <span class="summary-icon">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
          <path d="M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z"/>
        </svg>
      </span>
      <span class="summary-text">${statsText}</span>
      <button class="summary-toggle-btn">Show all</button>
    </div>
  `
}

/**
 * 切换消息内容的折叠状态
 */
export function toggleMessageCollapse(messageElement: Element, isCollapsed: boolean): void {
  const summaryBar = messageElement.querySelector('.message-summary-bar')
  const toggleBtn = summaryBar?.querySelector('.summary-toggle-btn') as HTMLButtonElement

  // 找到所有需要折叠的元素
  const toolCalls = messageElement.querySelectorAll('p:has(strong)')
  const diffContainers = messageElement.querySelectorAll('.diff-container')
  const allElements = [...Array.from(toolCalls), ...Array.from(diffContainers)] as HTMLElement[]

  if (isCollapsed) {
    // 当前已收缩，点击后展开
    allElements.forEach((el) => {
      el.style.display = ''
      el.style.maxHeight = 'none'
      el.style.opacity = '1'
    })

    if (toggleBtn) toggleBtn.textContent = 'Hide all'
    summaryBar?.classList.remove('collapsed')
  } else {
    // 当前未收缩，点击后收缩
    allElements.forEach((el) => {
      el.style.maxHeight = el.scrollHeight + 'px'
      void el.offsetHeight // 强制重绘
      el.style.maxHeight = '0px'
      el.style.opacity = '0'

      // 动画结束后隐藏
      setTimeout(() => {
        if (summaryBar?.classList.contains('collapsed')) {
          el.style.display = 'none'
        }
      }, 300)
    })

    if (toggleBtn) toggleBtn.textContent = 'Show all'
    summaryBar?.classList.add('collapsed')
  }
}

/**
 * 为消息容器添加摘要栏和折叠功能
 * @param autoCollapse 是否自动收缩（默认为true）
 */
function processMessageContainer(messageElement: Element, autoCollapse: boolean = true): void {
  // 检查是否已经处理过
  if (messageElement.hasAttribute('data-summary-initialized')) {
    return
  }
  messageElement.setAttribute('data-summary-initialized', 'true')

  // 统计工具调用和文件编辑
  const stats = countMessageActions(messageElement)

  if (stats.totalCount === 0) {
    return // 没有需要折叠的内容
  }

  // 生成唯一ID
  const containerId = `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

  // 生成并插入摘要栏
  const summaryHtml = generateSummaryBarHtml(stats, containerId)
  // 查找多个可能的消息内容容器
  const messageContent =
    messageElement.querySelector('.message-content') ||
    messageElement.querySelector('.message-markdown') ||
    messageElement.querySelector('.markdown-content')

  if (messageContent) {
    // 在消息内容的开头插入摘要栏
    messageContent.insertAdjacentHTML('afterbegin', summaryHtml)

    // 添加点击事件
    const summaryBar = messageContent.querySelector('.message-summary-bar')
    if (summaryBar) {
      summaryBar.addEventListener('click', (e) => {
        e.stopPropagation()
        const isCollapsed = summaryBar.classList.contains('collapsed')
        toggleMessageCollapse(messageElement, isCollapsed)
      })

      // ✅ 如果需要自动收缩，立即执行收缩
      if (autoCollapse) {
        // 使用 setTimeout 确保DOM已经渲染完成
        setTimeout(() => {
          toggleMessageCollapse(messageElement, false)
        }, 100)
      }
    }
  }
}

/**
 * 初始化消息折叠功能
 * @param containerSelector 消息容器的选择器，默认为 '.messages-container'
 * @param autoCollapse 是否自动收缩（默认为true）
 * @param onlyCompleted 是否只处理已完成的消息（默认为true，只处理非loading状态的消息）
 */
export function initMessageCollapse(
  containerSelector: string = '.messages-container',
  autoCollapse: boolean = true,
  onlyCompleted: boolean = true
): void {
  // 处理所有AI消息
  const processAllMessages = () => {
    const messagesContainer = document.querySelector(containerSelector)
    if (!messagesContainer) return

    const aiMessages = messagesContainer.querySelectorAll('.ai-message, .message-ai')
    aiMessages.forEach((messageEl) => {
      // ✅ 如果启用 onlyCompleted，只处理非loading状态的消息
      if (onlyCompleted) {
        // 检查消息是否有loading类或loading状态
        const messageBubble = messageEl.querySelector('.message-bubble, .ai-bubble')
        const hasLoadingIndicator = messageBubble?.querySelector('.message-loading, .loading-dots')

        // 如果有loading指示器，跳过处理
        if (hasLoadingIndicator) {
          return
        }
      }

      processMessageContainer(messageEl, autoCollapse)
    })
  }

  // 初始化已存在的消息
  processAllMessages()

  // 监听DOM变化，自动处理新消息
  const observer = new MutationObserver(() => {
    processAllMessages()
  })

  const messagesContainer = document.querySelector(containerSelector)
  if (messagesContainer) {
    observer.observe(messagesContainer, {
      childList: true,
      subtree: true,
      attributes: true, // ✅ 监听属性变化，以便捕获loading状态改变
      attributeFilter: ['class'] // 只监听class属性变化
    })
  }
}
