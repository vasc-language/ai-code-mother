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

  // ✅ 格式化工具标记（显示为卡片样式）
  filteredContent = filteredContent.replace(/\[选择工具\]\s*([^\[\n]*)/g, (match, toolName) => {
    const name = toolName.trim()
    return name ? `**[选择工具]** ${name}\n\n` : ''
  })
  filteredContent = filteredContent.replace(/\[工具调用\]\s*([^\[\n]*)/g, (match, info) => {
    const infoText = info.trim()
    return infoText ? `**[工具调用]** ${infoText}\n\n` : ''
  })
  // ✅ 执行结束标记格式化（显示为卡片样式）
  filteredContent = filteredContent.replace(/\[执行结束\]/g, '**[执行结束]**\n\n')

  // ✅ 关键标题加粗（让"实现的功能："和"What's next?"更显眼）
  filteredContent = filteredContent.replace(/^(实现的功能：)/gm, '**$1**')
  filteredContent = filteredContent.replace(/^(What's next\?)/gm, '**$1**')

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

  // ✅ 移除完整的 MULTI_FILE 块（从 START 到 END，包括中间的所有代码）
  let filteredContent = content.replace(
    /\[MULTI_FILE_START:[^\]]+\][\s\S]*?\[MULTI_FILE_END:[^\]]+\]/g,
    ''
  )

  // ✅ 移除独立的 MULTI_FILE_CONTENT 块（可能没有配对的 START/END）
  filteredContent = filteredContent.replace(
    /\[MULTI_FILE_CONTENT:[^\]]+\][\s\S]*?(?=\[MULTI_FILE_|$)/g,
    ''
  )

  // 移除工具调用块（包括代码）
  filteredContent = filteredContent.replace(/\[工具调用\][^\n]*\n```[\w-]*\n[\s\S]*?```/g, '')

  // 移除完整代码块（```language code ```）
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')

  // 移除不完整的代码块（```开头但没有结束的）
  filteredContent = filteredContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 移除特殊标记及其周围内容
  filteredContent = filteredContent.replace(
    /\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\][\s\S]*?(?=\n\n|$)/g,
    ''
  )

  // 移除内联代码标记
  filteredContent = filteredContent.replace(/`[^`\n]*`/g, '')

  // 移除步骤信息
  filteredContent = filteredContent.replace(/STEP\s+\d+:[^\n]*/g, '')

  // 移除任何残留的标记行
  filteredContent = filteredContent.replace(/^.*\[MULTI_FILE_.*$/gm, '')
  filteredContent = filteredContent.replace(/^.*```.*$/gm, '')

  // ✅ 格式化工具标记（显示为卡片样式）
  filteredContent = filteredContent.replace(/\[选择工具\]\s*([^\[\n]*)/g, (match, toolName) => {
    const name = toolName.trim()
    return name ? `**[选择工具]** ${name}\n\n` : ''
  })
  filteredContent = filteredContent.replace(/\[工具调用\]\s*([^\[\n]*)/g, (match, info) => {
    const infoText = info.trim()
    return infoText ? `**[工具调用]** ${infoText}\n\n` : ''
  })
  // ✅ 执行结束标记格式化（显示为卡片样式）
  filteredContent = filteredContent.replace(/\[执行结束\]/g, '**[执行结束]**\n\n')

  // ✅ 关键标题加粗（让"实现的功能："和"What's next?"更显眼）
  filteredContent = filteredContent.replace(/^(实现的功能：)/gm, '**$1**')
  filteredContent = filteredContent.replace(/^(What's next\?)/gm, '**$1**')

  // 清理多余的空行
  filteredContent = filteredContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  filteredContent = filteredContent.replace(/^\n+/, '')
  filteredContent = filteredContent.replace(/\n\s*$/, '')

  return filteredContent.trim()
}

/**
 * VUE_PROJECT模式专用：格式化工具调用信息并移除代码块
 * 只保留AI的文字描述，移除所有代码内容
 */
export const formatVueProjectContent = (content: string): string => {
  if (!content) return ''

  let formattedContent = content

  // ✅ 移除完整的 MULTI_FILE 块（从 START 到 END，包括中间的所有代码）
  formattedContent = formattedContent.replace(
    /\[MULTI_FILE_START:[^\]]+\][\s\S]*?\[MULTI_FILE_END:[^\]]+\]/g,
    ''
  )

  // ✅ 移除独立的 MULTI_FILE_CONTENT 块
  formattedContent = formattedContent.replace(
    /\[MULTI_FILE_CONTENT:[^\]]+\][\s\S]*?(?=\[MULTI_FILE_|$)/g,
    ''
  )

  // 移除工具调用块（包括代码）
  formattedContent = formattedContent.replace(/\[工具调用\][^\n]*\n```[\w-]*\n[\s\S]*?```/g, '')

  // 移除完整代码块
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*?```/g, '')

  // 移除不完整的代码块
  formattedContent = formattedContent.replace(/```[\w-]*\n[\s\S]*$/g, '')

  // 移除特殊标记
  formattedContent = formattedContent.replace(/\[(CODE_BLOCK_START|CODE_STREAM|CODE_BLOCK_END)\]/g, '')

  // 移除步骤信息
  formattedContent = formattedContent.replace(/STEP\s+\d+:[^\n]*/g, '')

  // 移除单行代码反引号
  formattedContent = formattedContent.replace(/`[^`\n]*`/g, '')

  // 移除残留的标记行
  formattedContent = formattedContent.replace(/^.*\[MULTI_FILE_.*$/gm, '')
  formattedContent = formattedContent.replace(/^.*```.*$/gm, '')

  // ✅ 工具标记格式化（保留工具调用信息，但移除代码）
  formattedContent = formattedContent.replace(/\[选择工具\]\s*([^\[\n]*)/g, (match, toolName) => {
    const name = toolName.trim()
    return name ? `**[选择工具]** ${name}\n\n` : ''
  })
  formattedContent = formattedContent.replace(/\[工具调用\]\s*([^\[\n]*)/g, (match, info) => {
    const infoText = info.trim()
    return infoText ? `**[工具调用]** ${infoText}\n\n` : ''
  })
  // ✅ 执行结束标记格式化（显示为卡片样式）
  formattedContent = formattedContent.replace(/\[执行结束\]/g, '**[执行结束]**\n\n')

  // ✅ 关键标题加粗（让"实现的功能："和"What's next?"更显眼）
  formattedContent = formattedContent.replace(/^(实现的功能：)/gm, '**$1**')
  formattedContent = formattedContent.replace(/^(What's next\?)/gm, '**$1**')

  // 清理多余空行
  formattedContent = formattedContent.replace(/\n\s*\n\s*\n/g, '\n\n')
  formattedContent = formattedContent.replace(/^\n+/, '')
  formattedContent = formattedContent.replace(/\n\s*$/, '')

  return formattedContent.trim()
}
