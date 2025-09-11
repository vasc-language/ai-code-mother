% 手动停止 / 恢复 AI 生成（SSE 流）实现说明（P0 软中断）

本说明记录“蓝色按钮随开随关”功能的最终实现方案与关键设计点，覆盖前后端协议约定、后端取消链路、前端交互与状态管理，以及已知限制与后续建议。

附：本次 Bug 修复记录（停止后仍继续旧计划输出）
- 现象（见 tmp/screenshots/20250903/error-1.png）：停止后重新下达“请重新生成 index.html 文件”，页面仍沿用上一轮未中断的计划继续输出其它文件。
- 根因归纳：
  1) 后端仅在顶层使用 takeUntilOther(cancelFlux) 截断推送，但底层 TokenStream 的工具执行与递归 follow-up chat 未感知取消；
  2) 前端在新一轮生成时，偶发接收/渲染上一个 run 的残余分片（SSE 定时批次或网络延迟到达），造成“继续旧计划”的观感；
  3) 继续操作对同一 AI 消息行未清空旧文案，呈现为“在原内容上追加”。
- 修复思路与落地：
  后端（硬化取消传播）
  - dev/langchain4j/service/AiServiceTokenStream.java：新增 withCancellation(Supplier<Boolean>)，把外部取消信号透传到流式 Handler；
  - dev/langchain4j/service/AiServiceStreamingResponseHandler.java：在 onPartialResponse/onPartialToolExecutionRequest/onToolExecuted/onCompleteResponse/onError 前均判断取消，已取消则直接 return，阻断工具执行与后续递归调用；
  - core/AiCodeGeneratorFacade.java：若 TokenStream 为 AiServiceTokenStream，则注入取消判断 stream.withCancellation(() -> control != null && control.isCancelled())；保留 takeUntilOther(cancelFlux) 与 sink.onCancel/onDispose → control.cancel()；
  - 以上确保“停止”后既不再向前端推送，也不再执行写文件/构建等副作用，更不会把旧流的后续步骤带入下一次会话。
  前端（源头隔离与 UI 正确性）
  - pages/app/AppChatPage.vue：为每次 generate() 捕获 myRunId，在 onmessage/done/interrupted/onerror/flushToUi 中若 currentRunId !== myRunId 则丢弃事件，彻底消除旧 run 残留分片对 UI 的影响；
  - onToggleStream 改进：
    - Continue 模式会将同一条 AI 消息重置为 loading 状态并清空旧内容，再以新 runId 重新发起，避免“接着旧内容往下写”；
    - Idle 模式下点蓝色按钮等同“开始生成”快捷入口（若输入为空则给予提示）。
  - 继续保留微批渲染（≈40ms）与定时器清理，避免频繁重绘及内存泄漏。
- 验收要点：
  - 停止≤200ms：前端停止追加、后端不再推送/不触发工具；
  - 新指令仅按新 prompt 输出；旧 run 的任何事件/工具结果不会污染 UI 或落库；
  - 继续/开始操作的文案与 loading 表现正确，无“在旧内容上续写”的错觉。

一、目标回顾
- 生成中点击“停止”：
  - 前端立即停止追加展示，按钮状态立刻切换；
  - 后端当前 SSE 流被中断，不再继续输出，也不落库被截断的 AI 回复，不触发构建等副作用；
  - 保持页面不冻结，用户可立即“继续”（本质为重新发起一次新流）。
- 继续生成：以新的 runId 携带相同上下文，重新拉起一条新 SSE 流，前端继续显示新一轮输出。

二、前后端协议
- GET /app/chat/gen/code?appId&message&runId（SSE）：
  - data 事件：负载统一包装为 JSON：{"d": "...chunk..."}
  - done 事件：一次流自然完成时发送。
  - interrupted 事件：当用户主动停止时，服务端立即发送该事件并终止 SSE。
- POST /app/chat/stop：{ runId }
  - 成功后触发服务端取消，后续相应 runId 的 SSE 流立刻结束。

三、后端实现
1) 运行控制注册表 GenerationControlRegistry（软中断核心）
- 位置：src/main/java/com/spring/aicodemother/core/control/GenerationControlRegistry.java
- 结构：ConcurrentHashMap<String, GenerationControl>
  - GenerationControl：
    - cancelled: AtomicBoolean 记录取消状态；
    - cancelSink: Sinks.Many<Void>（replay().latest）→ 暴露 cancelFlux() 供 takeUntilOther/merge 使用；
    - cancel(): 置位 cancelled 并向 sink 发出事件 + complete，确保所有订阅者能“即时感知”。

2) SSE 控制器 AppController
- chatToGenCode：
  - 接入 runId（前端传或服务端生成），在 registry 中 register(runId, userId, appId)；
  - 业务流 contentFlux = appService.chatToGenCode(..., control)；
  - 将 contentFlux.takeUntilOther(control.cancelFlux()) 映射为 data 事件；
  - 另起 interruptEvent = control.cancelFlux().map(→ event:"interrupted")；
  - Flux.merge(dataEvents, interruptEvent).takeUntil(sse.event()=="interrupted") 保证一旦取消就立刻终止 SSE；
  - doFinally 清理 registry 并记录日志。
- stopChat：
  - 校验 runId 归属用户，调用 registry.cancel(runId) 触发取消。

3) 业务流与工具流绑定取消信号（AiCodeGeneratorFacade）
- 位置：src/main/java/com/spring/aicodemother/core/AiCodeGeneratorFacade.java
- 流式 HTML/MULTI_FILE：processCodeStream(Flux<String>):
  - 使用 codeStream.takeUntilOther(control.cancelFlux()) 截断；
  - 异步保存前检查 control.isCancelled()，被取消则跳过解析与保存。
- TokenStream（VUE_PROJECT）：processTokenStream(TokenStream):
  - 创建 Flux 时绑定 sink.onCancel/onDispose → control.cancel()，打通“下游关闭→上游知晓”；
  - onPartialResponse/onPartialToolExecutionRequest/onToolExecuted 内部先判定取消，已取消则直接 return；
  - .takeUntilOther(control.cancelFlux()) 截断；
  - onCompleteResponse 中仅在未取消时才进行 vueProjectBuilder.buildProject(...);
  - onError 做错误恢复，确保流不因单次异常崩溃。

4) 流处理与落库抑制（StreamHandlerExecutor / JsonMessageStreamHandler）
- 位置：src/main/java/com/spring/aicodemother/core/handler
- 通过 BooleanSupplier cancelled 将取消状态注入 handler：
  - doOnComplete/doOnError 前检查 cancelled，若为真则不落库 AI 消息、不触发构建等副作用。

四、前端实现（Vite + Vue 3）
- 位置：ai-code-mother-frontend/src/pages/app/AppChatPage.vue
- 关键状态：
  - eventSource: 当前 SSE 连接；
  - currentRunId: string | null；
  - isGenerating: boolean；stoppedByUser: boolean；
  - lastUserMessage: string；currentAiMessageIndex: number | null。
- 启动：
  - generateCode() 前生成 runId 并拼接到 URL；
  - 订阅 onmessage 解析 {"d": chunk} 并渲染；
  - 监听 done 事件做收尾；
  - 新增监听 interrupted 事件：一旦收到，立即关闭 SSE、isGenerating=false，并保留 stoppedByUser=true 以便“继续”。
- 停止：
  - onToggleStream() 在 isGenerating=true 时：
    - stoppedByUser=true、isGenerating=false；
    - 关闭 eventSource；
    - POST /app/chat/stop { runId }（容错处理，失败也不影响关闭前端展示）；
  - 继续：stoppedByUser=true 且存在 lastUserMessage → 以新 runId 再次 generateCode()。

五、即时停止的关键点（体验优化）
1) 取消信号广播与合流：
   - 由 Sinks.Many.replay().latest() 提供“最新值重放”，确保 late subscriber 也能立即收到取消通知；
   - 服务端把取消流合并为单独的 interrupted 事件，避免等待上游再吐出下一 token 才结束。
2) 下游主动关闭的反向通知：
   - processTokenStream 中使用 sink.onCancel/onDispose → control.cancel()，实现“前端先关，后端立停”。

六、兼容性与安全
- runId 可选：旧前端未传 runId 仍可用，但无法手动停止；
- 限流：/app/chat/gen/code 与 /app/chat/stop 均保留 @RateLimit；
- 权限：stop 接口校验 runId 所属用户，防止越权停止他人任务；
- CORS：保留允许凭证与跨域；SSE 通过 withCredentials 建立。

七、已知限制（软中断边界）
- 上游 LLM SDK（OpenAI 兼容流）未提供直接 abort/dispose 钩子时，只能做到软中断（不再向前端/持久层输出，尽力跳过副作用）；
- 若未来 SDK 暴露可取消的请求句柄，可在 control.cancel() 时同步调用底层 HTTP 取消以进一步降低资源消耗（P1 深度中断）。

八、验证方法（建议）
1) 本地启动后端（需要 JDK21、MySQL/Redis 可选）：
   - ./mvnw spring-boot:run 或 ./mvnw -DskipTests package && java -jar target/*.jar
2) 启动前端并登录，进入应用聊天页：
   - 输入较长 prompt 触发生成 → 立即点击“停止”：
     - UI 立刻停止追加，按钮从■切换为▶；
     - 网络面板可见 SSE 被服务端推送的 interrupted 事件终止；
     - 数据库无新增被截断的 AI 消息；Vue 构建未触发；
   - 点击“继续”：以新 runId 重新生成，SSE 正常工作。

九、关联变更清单（核心文件）
- 后端
  - core/control/GenerationControlRegistry.java（新增/改造：cancelFlux、Sinks.Many）
  - controller/AppController.java（合并 interrupted 事件、takeUntilOther(cancelFlux)）
  - core/AiCodeGeneratorFacade.java（onCancel/onDispose、工具回调处取消检查、takeUntilOther(cancelFlux)）
  - core/handler/StreamHandlerExecutor.java、JsonMessageStreamHandler.java（取消时跳过落库）
- 前端
  - pages/app/AppChatPage.vue（蓝色按钮、runId 透传、停止->POST /app/chat/stop、监听 interrupted 事件）

十、验收标准对照
- 点击蓝色按钮“停止”后：
  - 前端 ≤200ms 停止追加并关闭 SSE；
  - 服务端同时停止当前 runId 的输出，不落库、不构建；
  - “继续”操作可立即重新发起并正常收到流式输出。

（完）

---

% 发送/停止合一按钮（UI 实现完成）

目标
- 将输入框右下角的两个蓝色按钮（发送、停止/继续）合并为一个更简洁、美观的主按钮，同时具备：
  - 生成中一键停止（前端立即停更，后端 POST /app/chat/stop）。
  - 空闲时一键发送当前输入。
  - 手动停止后无输入时，支持“继续生成”。
- 非作品拥有者也能看到该按钮，但处于禁用态并展示提示。

实现要点（前端）
- 文件：ai-code-mother-frontend/src/pages/app/AppChatPage.vue
- 状态机（computed）：
  - btnState: 'send' | 'stop' | 'continue' | 'disabled'
  - 依据 isGenerating、stoppedByUser、userInput、lastUserMessage 与 isOwner 计算。
- 模板调整：
  - 移除独立的发送按钮，仅保留一个 `.stream-toggle` 主按钮；
  - 非拥有者：按钮可见但禁用，外层包裹 a-tooltip 提示“无法在别人的作品下对话哦~”；
  - 图标切换：stop=■、continue=▶、send=SendOutlined（禁用时 50% 透明）。
- 事件处理：
  - 新增 onPrimaryActionClick()：
    - stop/continue → 复用 onToggleStream()（停止或以新 runId 继续）。
    - send → 调用 sendMessage()。
    - disabled → message.info('请输入提示词')。
  - onPrimaryActionClick 内部首先校验 isOwner，非拥有者弹提示并返回。
- 全局快捷键：
  - 新增 window keydown 监听：生成中按 Enter 触发停止（owners only）。
  - onMounted 绑定、onUnmounted 移除，避免泄漏；与原 textarea 的 @keydown.enter.prevent 共存（生成中 textarea 已禁用）。
- 样式：
  - `.stream-toggle` 位置从 `right: 56px` 调整为 `right: 8px` 以适配单按钮布局；
  - 新增禁用态样式 `.stream-toggle[disabled] { opacity: .5; cursor: not-allowed; }`。

交互逻辑（摘要）
- 生成中：按钮=■ 停止；点击或按 Enter 立即停止（关闭 SSE 并调用 /app/chat/stop）。
- 停止后：
  - 有新输入：按钮=发送，点击发送新请求。
  - 无输入但存在 lastUserMessage：按钮=▶ 继续，点击以新 runId 重新触发同一语义请求。
- 空闲且无输入：按钮禁用并提示“请输入提示词”。
- 非拥有者：按钮始终禁用，展示“无法在别人的作品下对话哦~”。

影响面与兼容性
- 后端 API 与 SSE 协议不变；仅改变前端交互触发路径。
- 继续沿用 runId 相关性与 onToggleStream 的定时器/缓冲清理逻辑，避免旧流残留影响。

验收用例
- 空输入未生成：按钮禁用；输入文本后变为“发送”，点击后开始生成。
- 生成中：按钮变为“停止”，点击或按 Enter ≤200ms 内停更并触发 /app/chat/stop。
- 停止后：
  - 输入框为空 → 按钮为“继续”，点击后以新 runId 正常续生成；
  - 输入框有内容 → 按钮为“发送”，点击后按新输入生成。
- 非拥有者：按钮可见但禁用，悬浮与点击均提示无权限对话。

备注
- 若后续需要新增 Esc=停止 的快捷键，可在同一全局 keydown 中匹配 e.key==='Escape' 并复用 onToggleStream() 实现。

---

% 预置模板缓存与快速拷贝（Vue 工程，命中 HomePage 快捷提示词）

目标
- 在每次调用 AI 之前，先基于用户提示词匹配预置 Vue 模板；命中则把模板工程复制到 `tmp/code_output/vue_project_{appId}`，并把“模板使用说明”仅附加到本轮提示词，指导 AI 用读/写/改工具在模板上做最小变更。非命中或非 `VUE_PROJECT` 类型保持原流程。

实现位置与关键改动（后端）
- 文件：`src/main/java/com/spring/aicodemother/service/impl/AppServiceImpl.java`
  - 方法：`chatToGenCode(Long appId, String message, User loginUser, GenerationControl control)`
  - 新增逻辑：
    - `matchPresetTemplate(message)`: 精确匹配两条中文提示词（与 HomePage 快捷按钮一致）。
    - `ensurePresetCopied(appId, presetKey)`: 若目标目录不存在或为空，复制模板到 `vue_project_{appId}`。
    - `buildTemplateAwareMessage(original, presetKey, copied)`: 构造仅用于本轮调用的增强提示词（不落库），提醒 AI 先读取目录/文件，再进行最小修改，完成后调用退出工具。
    - 使用 `finalMessage` 代替 `message` 进入 `aiCodeGeneratorFacade.generateAndSaveCodeStream(...)`。
  - 日志：
    - 命中并处理：`[TEMPLATE-CACHED] appId=..., preset='portfolio|enterprise', copied=true|false`
    - 未命中：`[TEMPLATE-NO-HIT] appId=...`
    - 异常不中断主流程：`[TEMPLATE-ERROR] ...`

预置模板目录与触发规则
- 模板1（作品展示）：`tmp/code_output/vue_project_323345718267260928`
  - 触发文案（精确匹配）：制作一个精美的作品展示网站……（与首页“作品展示网站”快捷按钮一致）
- 模板2（企业官网）：`tmp/code_output/vue_project_317749662884204544`
  - 触发文案（精确匹配）：设计一个专业的企业官网……（与首页“企业官网”快捷按钮一致）

安全与边界
- 仅在 `codeGenType == VUE_PROJECT` 时生效。
- 首拷贝判定：仅当 `tmp/code_output/vue_project_{appId}` 目录不存在、非目录或为空时复制；避免覆盖用户已有成果。
- 模板不可用或复制失败不影响主流程（降级继续按原逻辑生成）。

验收要点（简）
- 日志出现：`[TEMPLATE-CACHED] ... copied=true`，随后再次生成为 `copied=false`。
- 目标目录存在模板文件：`ls -la tmp/code_output/vue_project_{appId}`。
- 聊天区可见工具调用顺序：先 `readDir/readFile`，再 `writeFile/modify`，非从零搭脚手架。
- 非命中/非 Vue 工程：日志 `[TEMPLATE-NO-HIT]`，流程不受影响。

影响面与回退
- 仅触达后端 `AppServiceImpl` 生成前置逻辑；对 HTML/MULTI_FILE 流程零影响。
- 出现异常自动降级（不复制、不增强提示词），无需回滚即可继续使用。

后续建议
- 支持“近似匹配/关键词映射”，并将模板映射抽取为可配置项（如 `application.yaml`）。
- 引入模板校验（必要文件存在性、`package.json`/`vite.config.*` 基础合法性）并在日志中提示。
