import { ref, onUnmounted } from 'vue'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'

/**
 * 可视化编辑器 Composable
 * 管理元素选择、编辑模式和 iframe 交互
 */
export function useVisualEditor() {
  // 状态
  const isEditMode = ref(false)
  const selectedElementInfo = ref<ElementInfo | null>(null)
  const iframeRef = ref<HTMLIFrameElement | null>(null)

  // 创建 VisualEditor 实例
  const visualEditor = new VisualEditor({
    onElementSelected: (elementInfo: ElementInfo) => {
      selectedElementInfo.value = elementInfo
      console.log('元素已选中:', elementInfo)
    },
    onElementHover: (elementInfo: ElementInfo) => {
      // 可以添加悬浮时的处理逻辑
      console.log('元素悬浮:', elementInfo)
    },
  })

  /**
   * 初始化 iframe
   */
  const initIframe = (iframe: HTMLIFrameElement) => {
    iframeRef.value = iframe
    visualEditor.init(iframe)
  }

  /**
   * 切换编辑模式
   */
  const toggleEditMode = () => {
    const newEditMode = visualEditor.toggleEditMode()
    isEditMode.value = newEditMode

    // 退出编辑模式时，清除选中元素
    if (!newEditMode && selectedElementInfo.value) {
      clearSelectedElement()
    }

    return newEditMode
  }

  /**
   * 开启编辑模式
   */
  const enableEditMode = () => {
    visualEditor.enableEditMode()
    isEditMode.value = true
  }

  /**
   * 关闭编辑模式
   */
  const disableEditMode = () => {
    visualEditor.disableEditMode()
    isEditMode.value = false

    // 清除选中元素
    if (selectedElementInfo.value) {
      clearSelectedElement()
    }
  }

  /**
   * 清除选中的元素
   */
  const clearSelectedElement = () => {
    selectedElementInfo.value = null
    visualEditor.clearSelection()
  }

  /**
   * iframe 加载完成时调用
   */
  const onIframeLoad = () => {
    visualEditor.onIframeLoad()
  }

  /**
   * 处理来自 iframe 的消息
   */
  const handleIframeMessage = (event: MessageEvent) => {
    visualEditor.handleIframeMessage(event)
  }

  /**
   * 同步状态
   */
  const syncState = () => {
    visualEditor.syncState()
  }

  /**
   * 获取带元素上下文的输入内容
   * 用于在发送消息时附加选中元素的信息
   */
  const getInputWithElementContext = (userInput: string): string => {
    if (!selectedElementInfo.value) {
      return userInput
    }

    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}`
    elementContext += `\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }

    return userInput + elementContext
  }

  /**
   * 获取输入框占位符文本
   */
  const getInputPlaceholder = (): string => {
    if (selectedElementInfo.value) {
      return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改...`
    }
    if (isEditMode.value) {
      return '点击选择要编辑的元素，或直接输入修改描述...'
    }
    return '请描述你想生成的内容，越详细效果越好哦'
  }

  /**
   * 在消息发送后清理状态
   */
  const afterMessageSent = () => {
    if (selectedElementInfo.value) {
      clearSelectedElement()
      if (isEditMode.value) {
        disableEditMode()
      }
    }
  }

  // 设置全局消息监听器
  const messageListener = (event: MessageEvent) => {
    handleIframeMessage(event)
  }
  window.addEventListener('message', messageListener)

  // 清理函数
  onUnmounted(() => {
    window.removeEventListener('message', messageListener)
    if (isEditMode.value) {
      disableEditMode()
    }
  })

  return {
    // 状态
    isEditMode,
    selectedElementInfo,
    iframeRef,

    // 方法
    initIframe,
    toggleEditMode,
    enableEditMode,
    disableEditMode,
    clearSelectedElement,
    onIframeLoad,
    handleIframeMessage,
    syncState,
    getInputWithElementContext,
    getInputPlaceholder,
    afterMessageSent,
  }
}
