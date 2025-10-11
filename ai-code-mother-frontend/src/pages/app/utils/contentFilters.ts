/**
 * 内容过滤工具函数
 * 用于过滤不同生成类型的AI响应内容，让聊天面板只显示描述性文字
 */

/**
 * HTML模式专用：移除代码块，只保留AI的文本描述
 */
export const filterHtmlContent = (content: string): string => {
  if (!content) return ''

  // 移除完整代码块（```language code ```）及其前后内容
  let filteredContent = content.replace(/(\n\s*)?```[\w-]*\n[\s\S]*?```(\n\s*)?/g, '')

  // 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/(\n\s*)?```[\w-]*\n[\s\S]*$/g, '')

  // 移除HTML代码流式输出的特殊标记及其周围的代码内容
  filteredContent = filteredContent.replace(
    /\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][\s\S]*?(?=\n\n|$)/g,
    ''
  )

  // 移除内联代码标记
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 移除任何包含代码标记的行
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*`.*$/gm, '')

  // 清理多余的空行
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  filteredContent = filteredContent.replace(/^\n+/, '') // 移除开头的空行
  filteredContent = filteredContent.replace(/\n\s*$/, '') // 移除结尾的空行

  return filteredContent.trim()
}

/**
 * MULTI_FILE模式专用：完全移除代码片段，只保留AI的文本描述
 */
export const filterOutCodeBlocks = (content: string): string => {
  if (!content) return ''

  // 移除完整代码块（```language code ```）及其前后内容
  let filteredContent = content.replace(/(\n\s*)?```[\w-]*\n[\s\S]*?```(\n\s*)?/g, '')

  // 移除不完整的代码块（```开头但没有结束的）及其后面的所有内容
  filteredContent = filteredContent.replace(/(\n\s*)?```[\w-]*\n[\s\S]*$/g, '')

  // 移除所有MULTI_FILE相关标记及其周围的内容
  filteredContent = filteredContent.replace(/\[MULTI_FILE_START:[^\]]+\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(
    /\[MULTI_FILE_CONTENT:[^\]]+\][\s\S]*?(?=\n\n|$)/g,
    ''
  )
  filteredContent = filteredContent.replace(/\[MULTI_FILE_END:[^\]]+\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除特殊标记及其周围内容
  filteredContent = filteredContent.replace(
    /\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][\s\S]*?(?=\n\n|$)/g,
    ''
  )

  // 移除内联代码标记
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 移除工具调用信息（完全移除，不显示在左边框）
  filteredContent = filteredContent.replace(/\[选择工具\][\s\S]*?(?=\n\n|$)/g, '')
  filteredContent = filteredContent.replace(/\[工具调用\][\s\S]*?(?=\n\n|$)/g, '')

  // 移除步骤信息
  filteredContent = filteredContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 移除任何包含代码标记的行
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*`.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[MULTI_FILE_.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[工具调用\].*$/gm, '')
  filteredContent = filteredContent.replace(/^.*\[选择工具\].*$/gm, '')

  // 清理多余的空行
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  filteredContent = filteredContent.replace(/^\n+/, '')
  filteredContent = filteredContent.replace(/\n\s*$/, '')

  return filteredContent.trim()
}

/**
 * VUE_PROJECT模式专用：格式化工具调用信息并移除代码块
 * 简化版本 - 暂不包含 diff 对比功能
 */
export const formatVueProjectContent = (content: string): string => {
  if (!content) return ''

  let formattedContent = content

  // 移除完整/不完整代码块
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 移除特殊标记与 MULTI_FILE 标记
  formattedContent = formattedContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_START:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_CONTENT:[^\]]+\]/g, '')
  formattedContent = formattedContent.replace(/\[MULTI_FILE_END:[^\]]+\]/g, '')

  // 移除步骤信息
  formattedContent = formattedContent.replace(/STEP\s+\d+:[\s\S]*?(?=\n\n|$)/g, '')

  // 移除单行代码反引号
  formattedContent = formattedContent.replace(/`([^`\n]+)`/g, '$1')

  // 清理特定残留行
  formattedContent = formattedContent.replace(/^.*\[MULTI_FILE_CONTENT:.*$/gm, '')

  // 工具标记格式化
  formattedContent = formattedContent.replace(/(\[选择工具\])/g, '\n$1')
  formattedContent = formattedContent.replace(/(\[工具调用\])/g, '\n$1')
  formattedContent = formattedContent.replace(/\[选择工具\]\s*([^\[\n\r]*)/g, (match, toolName) => {
    return `**[选择工具]** ${toolName.trim()}\n\n`
  })
  formattedContent = formattedContent.replace(/\[工具调用\]\s*([^\[\n\r]*)/g, (match, info) => {
    return `**[工具调用]** ${info.trim()}\n\n`
  })

  // 清理多余空行
  formattedContent = formattedContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  formattedContent = formattedContent.replace(/^\n+/, '')

  return formattedContent.trim()
}
