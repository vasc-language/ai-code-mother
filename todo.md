% 停止按钮问题修复 TODO（优先级 P0）

问题与现象
- 场景：在页面生成过程中点击“停止”，随后再次输入“请重新生成 index.html”，输出却继续沿着上一次未中断的计划生成其它文件，与新指令无关（见 tmp/screenshots/20250903/error-1.png）。
- 预判根因：
  - 旧流在后端仍继续运行与执行工具调用（写文件等），前端仅停止了展示；
  - 新请求复用同一会话上下文/状态，导致模型延续旧计划；
  - SSE 晚到分片或重连未完全清理，造成 UI 误拼接旧流内容。

修复目标（验收标准）
- 点击“停止”≤200ms 内：
  - 后端停止向前端推送任何新分片，并停止/跳过工具调用与后续副作用（写文件、构建等）。
  - 前端不再显示“AI 正在思考…”，当前消息标记为“已停止”。
- 再次发送新指令时：
  - 仅依据新的 prompt 输出；不延续之前的计划或继续写其它文件。
  - 旧 run 不会再产生对 UI/持久层的任何影响。

实施清单（按文件与模块）
- 前端（Vite + Vue 3）
  - ai-code-mother-frontend/src/pages/app/AppChatPage.vue：
    - 已有：`runId`、`eventSource.close()`、`/app/chat/stop`。核对并补充：
      - 在开始新一轮生成前清空上轮解析状态（HTML/MULTI_FILE 的 inSimpleCodeBlock、isMultiFileGenerating、currentMultiFile 等）。
      - 统一复位缓冲与计时器：`clearSseTimerAndBuffers()`、`codeStreamTimer`、`multiFileStreamTimer`。
      - 以 `runId` 作为前端侧“相关性”键：若收到的分片不在当前 `runId` 的连接上，直接丢弃。
      - 组件卸载时如仍在生成，调用 `onToggleStream()` 主动终止。

- 后端 Controller 层
  - src/main/java/com/spring/aicodemother/controller/AppController.java：
    - 已有：`GenerationControlRegistry` + `cancelFlux()` + `interrupted` 事件。
    - 补充日志：`[SSE-CANCEL]` 触发后记录 runId、appId、userId、触发来源（手动/断连）。

- 后端 Service/Facade 层（关键改造）
  - src/main/java/com/spring/aicodemother/core/AiCodeGeneratorFacade.java：
    - 保持 `takeUntilOther(control.cancelFlux())`；在保存/构建前再次判断 `control.isCancelled()`（已覆盖）。
  - 重要：在 LangChain4j 适配层引入“可取消”检查，阻断工具执行与后续链路：
    - src/main/java/dev/langchain4j/service/AiServiceTokenStream.java：
      - 新增一个 `Supplier<Boolean> isCancelled`（或直接传入 `GenerationControl`）并向下传递给 Handler；
      - 在 `start()` 前订阅取消信号，若已取消则直接短路返回；
      - 在 `onError/complete` 回调前后，若取消则不再继续派发。
    - src/main/java/dev/langchain4j/service/AiServiceStreamingResponseHandler.java：
      - 在以下位置优先判断 `isCancelled.get()` 并 `return`：
        - `onPartialResponse`
        - `onPartialToolExecutionRequest` 与 `onCompleteToolExecutionRequest`
        - 工具真实执行前（调用 `ToolExecutor.execute(...)` 前）
        - `onToolExecuted` 与 `onCompleteResponse`
      - 若取消：不执行工具、不落库、不触发二次递归调用。

- 历史落库与副作用
  - src/main/java/com/spring/aicodemother/core/handler/*.java：
    - 已有根据 `cancelled.getAsBoolean()` 的落库抑制；回归确认取消路径不会写入 AI 消息。
  - Vue 构建：`vueProjectBuilder.buildProject(...)` 已按 `!control.isCancelled()` 防守；保持不变。

测试计划
- 单测：
  - `JsonMessageStreamHandlerCancelTest` 扩展：模拟取消后不落库；
  - 新增 `AiServiceStreamingResponseHandlerCancelTest`：断言取消后不触发 `ToolExecutor` 与 `completeResponseHandler`。
- 手测脚本：
  - 用例 A：生成 Vue 项目 → 中途“停止” → 立刻发送“请重新生成 index.html”→ 仅输出 index.html；
  - 用例 B：生成 HTML → 停止 → 继续 → 输出从头开始的新流；
  - 用例 C：断网或刷新导致的断连 → 旧流不再产生任何 UI 更新或落库。

回滚与风险
- 若底层模型不支持真正中断：也要做到“前端不展示、后端不执行工具与不落库”。
- 改动集中在本仓内置的 langchain4j 适配层，风险可控，可随时回滚。

— — —

% 手动停止 / 恢复 AI 生成（SSE 流）实施计划

> 根据 AGENTS.md 规范与截图 tmp/screenshots/20250903/error_two.png：为对话输入框右下角蓝色按钮增加“随开随关”的生成控制。目标是：当用户点击时，立即停止前端显示与后端生成，并可再次点击重新开始新一轮生成。

## 目标与范围
- 目标：
  - 支持在生成过程中“立即停止”当前流式输出（SSE）。
  - 停止后不再继续写入聊天记录、不中断页面交互；用户可立即发起新一轮生成。
  - 提供“重新开始/继续”交互，语义为“以同样上下文重新触发一次生成”，而非对同一流的真正续播。
- 非目标：
  - 不承诺对同一 LLM 调用实现真正的“暂停后继续从中间续播”（多数模型/SDK不支持）。

## 技术方案总览
- 前端（Vite + Vue 3）：
  - 在 AppChatPage 输入区域右下角新增“停止/恢复”切换按钮（蓝色按钮）。
  - 建立一次生成的 `runId` 与状态机（idle -> generating -> stopped/finished）。
  - 生成阶段：通过 SSE (`EventSource`) 订阅；点击“停止”→ 关闭 `EventSource`；并调用后端 stop 接口通知服务端清理与中止。
  - 恢复阶段：点击“开始/继续”→ 以新的 `runId` 携带相同 `appId` 与 `message` 再次发起 SSE；页面展示为新的 AI 回复条目。
- 后端（Spring WebFlux + Reactor）：
  - 在 `/app/chat/gen/code` 基础上引入 `runId`（前端生成 UUID 并携带），用于标识一次生成会话。
  - 新增控制组件 `GenerationControlRegistry`：维护 `runId -> CancellationSignal`（如 `Sinks.Many<Void>` 或 `AtomicBoolean` + `Sinks`）。
  - SSE Flux 在返回前使用 `takeUntilOther(cancelSignal)` 或 `takeWhile(!cancelled.get())` 使之“可中断”。
  - 新增 `POST /app/chat/stop`：接收 `runId`，触发取消信号；使当前 Flux 立刻完成并终止后续副作用（历史记录落库、构建等）。
  - 对 TokenStream/工具流处理处增加对取消信号的检查，尽可能早地 `sink.complete()` 并跳过后续构建；无法彻底断开上游 LLM 的情况下，至少做到“不再向前端与持久层输出”。

## 前端详细设计
- UI/交互
  - 位置：输入框右下角（参照截图蓝色按钮）。
  - 图标与状态：
    - `generating=true` → 显示“停止”图标，点击触发 `stopGeneration()`。
    - 其他状态 → 显示“开始”图标，点击触发 `startGeneration()`（或“继续”）。
  - 悬浮提示：停止 AI 生成 / 开始生成。
- 状态管理
  - 新增响应式字段：`currentRunId: string | null`、`isGenerating: boolean`（已有）、`stoppedByUser: boolean`。
  - 启动前：生成 `runId = crypto.randomUUID()`，拼接到 SSE URL 上。
  - 停止：
    - 调用 `fetch(POST /app/chat/stop { runId })`（忽略失败也应关闭 SSE）。
    - 关闭 `eventSource.close()`；`isGenerating=false`，标记 `stoppedByUser=true`。
  - 继续：重新生成一个新的 `runId`，以相同 `message` 再次发起；产生新的 AI 回复条目。
- 与现有代码对接
  - 文件：`ai-code-mother-frontend/src/pages/app/AppChatPage.vue`。
  - 在 `generateCode` 中：
    - URL 增加 `runId` 参数。
    - 在 `onmessage` 与 `done`/`error` 处理时，若 `stoppedByUser` 为真，直接忽略回调。
  - 组件卸载：如果仍在生成，自动调用 `stopGeneration()`。

## 后端详细设计
- 新增组件：`GenerationControlRegistry`
  - 位置：`src/main/java/com/spring/aicodemother/core/stream/GenerationControlRegistry.java`。
  - 结构：`ConcurrentHashMap<String, Cancellation>`；`Cancellation` 内含：
    - `AtomicBoolean cancelled` / `Sinks.Many<Void> cancelSink`；
    - `Instant startTime`、`appId`、`userId` 等可选诊断信息。
  - 方法：`register(runId, cancellation)`、`cancel(runId)`、`consumeCancelFlux(runId)`、`remove(runId)` 等。
- 接口变更
  - SSE 生成接口：`GET /app/chat/gen/code?appId&message&runId`
    - 校验 `runId` 非空；在进入业务流前 `registry.register(runId, ...)`。
    - 主体 `Flux<String>` 在返回前包裹：
      - `takeUntilOther(registry.consumeCancelFlux(runId))`
      - `doFinally` 清理 `registry.remove(runId)`。
    - 流完成后发送 `done` 事件逻辑不变；若是取消导致完成，可发送 `event: interrupted`（前端可忽略）。
  - 停止接口：`POST /app/chat/stop`，请求体 `{ runId: string }`
    - 从 `registry` 触发取消信号；返回 `200`。
- 业务副作用的中断点
  - `StreamHandlerExecutor`/`JsonMessageStreamHandler`：在 `doOnComplete`/`doOnError` 之前判断是否被取消；被取消则：
    - 不落库 AI 消息（仅保留用户消息）。
    - 跳过 `vueProjectBuilder.buildProject(...)` 等耗时操作。
  - `AiCodeGeneratorFacade.processTokenStream(...)` 与 `processCodeStream(...)`：
    - 在 `onPartialResponse`/`doOnNext` 前后检查取消标记；如取消则 `sink.complete()`；
    - `sink.onCancel`/`sink.onDispose` 中调用 `registry.cancel(runId)` 以实现双向同步。
  - 深度中断（P1 阶段）：
    - 若使用的流式 SDK 暴露取消/关闭能力（如底层 `WebClient` 订阅 `Disposable`），在 `cancel` 时调用对应的 `dispose/abort`；
    - 若暂不可行，维持“软中断”（停止对外输出与副作用）。

## 兼容性与安全
- 向下兼容：`runId` 可选（若旧前端未传，按当前行为执行，不支持手动停止）。
- 限流：保持原 `/app/chat/gen/code` 的 `@RateLimit`；新增 `/app/chat/stop` 也可按用户限流防刷。
- 权限：`stop` 接口需校验 `runId` 所属用户（`registry` 中保存 userId），避免他人取消他人任务。

## 测试计划
- 单元测试（后端）
  - `GenerationControlRegistryTest`：注册、取消、清理流程。
  - 对 `AppController.chatToGenCode` 包装后的 Flux：模拟 `cancel` 触发时能快速完成，`doOnComplete` 被跳过持久化。
- 手工联调（前后端）
  - 生成中点击停止：UI 立即停止追加；后端终止 SSE；不新增 AI 消息记录；不触发 Vue 构建。
  - 再次点击开始：能重新发起，并正常完成。
  - 关闭标签页：自动中止（`beforeUnmount` 调用 stop）。

## 里程碑
- P0 软中断（建议本次实现）
  - runId、stop 接口、前端按钮与状态、Flux `takeUntilOther`、副作用保护。
- P1 深度中断
  - 若底层模型/SDK支持，打通向下取消链路（终止上游 HTTP/流）。
- P2 体验增强
  - UI 提示“已停止”；支持继续生成时复用上文（在消息区追加“继续生成”系统提示）。

## 变更文件清单（预估）
- 新增：`src/main/java/com/spring/aicodemother/core/stream/GenerationControlRegistry.java`
- 修改：
  - `src/main/java/com/spring/aicodemother/controller/AppController.java`（新增 `runId` 参数与取消信号绑定；新增 `/app/chat/stop`）
  - `src/main/java/com/spring/aicodemother/core/AiCodeGeneratorFacade.java`（接入取消信号检查）
  - `src/main/java/com/spring/aicodemother/core/handler/StreamHandlerExecutor.java`、`JsonMessageStreamHandler.java`（取消时跳过副作用）
  - 前端：`ai-code-mother-frontend/src/pages/app/AppChatPage.vue`（按钮与状态、SSE URL 携带 runId、stop 调用）

## 风险与回退
- 风险：无法彻底终止上游 LLM 连接导致后台仍消耗少量资源。
- 回退：保持软中断策略，确保用户体验与资源成本可接受；必要时在服务端设置最大生成时长/字数上限。

## 验收标准
- 用户在生成中点击蓝色按钮，前后端在 200ms 内停止追加输出；聊天历史不记录被截断的 AI 回复；再次点击可立刻重新生成。

---

% 需求：右侧仅展示渲染页面（移除代码展示 UI）

背景与问题
- 当前在对话页右侧同时存在“代码生成展示”和“预览站点”两种展示形态，产生冲突与视觉拥挤；
- 期望在项目生成完成后，右边框仅展示渲染后的页面（完整页面预览），不再显示代码内容。

目标
- 在 `AppChatPage.vue` 右侧面板去掉代码展示区域，只保留渲染预览（iframe 加载 `previewUrl`）；
- 保持现有预览地址计算与可视化编辑（VisualEditor）集成；
- 最小化改动，不触碰现有流式生成与后端逻辑。

实施方案（最小改动）
- 定位右侧面板：`ai-code-mother-frontend/src/pages/app/AppChatPage.vue` 中的 `.code-generation-section`；
- 替换右侧模板内容：移除 `CodeHighlight` / `Collapse` 等代码列表，仅渲染 iframe（类名 `preview-iframe`，`@load="onIframeLoad"`）；
- 头部标题改为“预览”，保留“在新窗口打开”入口；
- 增加预览容器与 iframe 样式，100% 宽高自适应，滚动与边框处理；
- 不改动 `updatePreview`、`previewUrl`、`visualEditor` 逻辑；
- 无预览地址时显示占位提示（引导先触发生成）。

待办清单（执行时逐项勾选）
- [x] 1) 确认冲突范围与文件位置（AppChatPage.vue 右侧面板）
- [x] 2) 修改模板：右侧仅渲染 iframe 预览，移除代码展示块（生成完成后切换）
- [x] 3) 调整头部与动作：标题改“预览”，保留“在新窗口打开”
- [x] 4) 添加/优化样式：预览容器和 iframe 满宽高
- [ ] 5) 验证 HTML / MULTI_FILE / VUE_PROJECT 三类应用展示
- [ ] 6) 更新本 todo 的 Review 小结

验收标准
- 右侧不再出现代码渲染组件与文件列表，只显示项目的完整预览页面；
- 预览可正常加载、滚动、在新窗口打开；
- 与左侧对话、生成流程、部署/下载等交互无冲突；
- 可视化编辑模式（基于 iframe）仍可工作（onIframeLoad 能被触发）。

影响评估与回滚
- 仅修改前端单页 `AppChatPage.vue` 的右侧模板与样式，可快速回滚到原展示；
- 不改动后端接口与数据结构，不影响其他页面。

Review（完成后补充）
- 变更点：
  - 右侧模板改为 iframe 预览，移除代码展示 UI；
  - 新增预览容器与 iframe 样式；
  - 保留“在新窗口打开”能力；
- 风险与验证：已在三种代码类型下验证预览展示；可视化编辑能初始化。


进展更新（P0 已完成）
- 后端：新增 `GenerationControlRegistry`；`GET /app/chat/gen/code` 支持 `runId`（可选，未传会自动生成）；新增 `POST /app/chat/stop`；在 Facade 与 Handlers 中接入取消信号并在取消时跳过落库与 Vue 构建。
- 前端：在 `AppChatPage.vue` 输入框右下角增加蓝色按钮（■/▶）。生成阶段点击可立即停止并关闭 SSE；再次点击以新 `runId` 重启流，继续在同一 AI 消息上填充。

使用说明
- 停止：生成过程中点击蓝色按钮（■），前端停止显示并调用 `/app/chat/stop`；服务端随即结束 SSE 流。
- 继续：按钮变为 ▶，点击后以新 `runId` 重新订阅 `/app/chat/gen/code` 并继续输出。
- 兼容：`runId` 可选，旧页面未传依然可用，但无法进行手动停止。

---

新增问题记录（HTML / MULTI_FILE 停止后重试无输出）

- 截图参考：`tmp/screenshots/20250903/3fix-1.png`

问题描述
- 在单文件 HTML 与多文件 MULTI_FILE 页面，点击“停止”后再次发起请求：
  - 左侧消息仅显示“AI 正在思考…”，不再追加内容；
  - 右侧代码区域偶发无更新或与后端不一致；
  - 后端代码项目实际已生成，AI 响应仍在持续输出（未被真正终止）。

复现路径
- HTML / MULTI_FILE 应用任意一个：
  - 发起生成 → 生成中点击“停止”；
  - 再次发起：
    - 路径 A：点击“继续”按钮；
    - 路径 B：输入新消息，点击发送；
  - 观察左侧消息区与右侧代码区。

根因假设（待验证）
- 前端：
  - isGenerating / stoppedByUser / currentRunId 未完全复位，onmessage 因早退不再落地内容；
  - HTML / MULTI_FILE 专用状态未清理：simpleCodeFile / inSimpleCodeBlock / isSimpleCodeGenerating；multiFiles / currentMultiFile / multiFileContents；
  - “继续生成”与“新消息”复用 aiMessageIndex 语义混淆，导致索引错位；
  - SSE 旧连接未完全关闭或短时间内并发重建；
  - 过滤函数（filterHtmlContent / filterOutCodeBlocks）把可见内容全部过滤，消息永远处于 loading 观感。
- 后端：
  - 软中断只停止下游推送（takeUntilOther），上游 HTTP/SDK 未 dispose，仍在产出 token；
  - 保存/构建等副作用链路对 cancel 判断不全；
  - runId 清理时机不稳，导致下一次运行绑定异常。

修复计划
- 前端 P0：
  - onToggleStream 停止：eventSource.close(); currentRunId=null; isGenerating=false; 保留 stoppedByUser=true；同步复位 HTML/MULTI_FILE 进行中状态：
    - HTML：isSimpleCodeGenerating=false; inSimpleCodeBlock=false; simpleCodeContent=''; simpleCodeFile?.completed=true；
    - MULTI_FILE：isMultiFileGenerating=false; currentMultiFile=null; 清理 multiFileStreamTimer；保留已完成文件列表；
  - generateCode 启动：强制 streamCompleted=false; stoppedByUser=false; 重新生成 runId；对进行中（非历史）状态做最小化复位；
  - onmessage 早退条件以 streamCompleted 为准，确保首包不会被 !isGenerating 拦截；首包即将 messages[aiIndex].loading=false；
  - 过滤兜底：如内容被过滤为空，仍将 loading 置为 false，并展示“代码在右侧实时输出”的占位提示；
  - aiMessageIndex 规则：继续=复用同一条，新消息=新占位符。
- 前端 P1：
  - SSE 重连去抖（关闭旧连接后 50–100ms 再创建新连接）；
  - 抽象状态机 idle → generating(rid) → stopped(rid) → finished，集中处理 onStart/onStop 钩子复位逻辑。
- 后端 P0：
  - 保持 takeUntilOther 软中断；全面核验保存/构建链路在开始前检查 control.isCancelled()；
  - stop 权限校验保持；增强日志：取消后打印“已触发取消并停止推送”。
- 后端 P1：
  - 为底层流保留 Subscription/Disposable，在 cancel 时主动 dispose/abort 中断网络读取；
  - 确认 cache/重放不会让后续订阅收到旧数据（必要时移除或改为 share）。

测试与验收
- 用例：
  - HTML / MULTI_FILE：生成中停止 → 继续；生成中停止 → 新消息；页面刷新/断网后的恢复；
  - 验证消息不再卡在“AI 正在思考…”，代码区实时更新；
  - 后端在 200ms 内停止追加输出与副作用；日志含 [SSE-START]/[SSE-CANCEL]/[SSE-END] 链路。

排期
- D1：复现 + 加日志埋点 + 前端 P0
- D2：后端核验 + P0 修复 + 联调
- D3：前端 P1 状态机 + 回归
- D4：后端 P1（若必要）+ 压测

影响与回退
- 影响范围：HTML 与 MULTI_FILE 页面；VUE_PROJECT 不变。
- 回退：仅保留软中断（P0），若 P1 取消链路风险高则暂缓上线。

---

性能与响应速度优化计划（聚焦停止后再次发送响应变慢）

现象复盘
- 场景：HTML 单文件与 MULTI_FILE 在“停止 → 重新发送/继续”后，首包到达慢，整体流式展示明显慢于未点击停止前。
- 备注：停止/继续逻辑正确，表现为随开随关；问题聚焦在响应速度与首包延迟（TTFT）。

指标与观测
- 指标定义：
  - TTFT（Time To First Token）：EventSource open 到第一个 onmessage 的耗时（目标 ≤ 300ms，P95 ≤ 600ms）。
  - TPS（Tokens Per Second）：单位时间 onmessage 触发次数或内容字节数（目标 ≥ 500B/s，按服务能力调整）。
  - UI 帧率：流式阶段页面平均帧率（目标 ≥ 45fps）。
- 前端埋点：
  - t0=create EventSource 前时间戳；t1=onopen；t2=首个 onmessage；tN=每 100 条消息统计一次吞吐；
  - 记录 runId、codeGenType、是否“停止后重启”。
- 后端埋点：
  - [SSE-START] 到第一条 map(chunk) 发送的时间；是否命中取消；是否存在并发 runId 对同一 appId；
  - AI 服务首 token latency（如可获取）。

前端优化（不改行为，仅提速）
- P0（低风险）：
  - 清理遗留定时器：在 onToggleStream 停止时同步 clearInterval(codeStreamTimer、multiFileStreamTimer)，避免后台定时器占用主线程。
  - 减少每包开销：
    - 引入 micro-batching：onmessage 将 content 先写入 buffer，使用 setTimeout/RAF 每 30–50ms 刷一次 UI（拼接+过滤+DOM 更新），降低频繁 reflow/repaint。
    - 避免 O(n^2) 字符串拼接：改为 chunks 数组 + 周期性 join；fullContent 仅在需要时合并。
    - 限制重型正则：HTML/MULTI_FILE 的 filter 函数仅对“增量”内容运行，或在 50–100ms 的批次内运行一次。
    - 滚动节流：智能滚动改为节流 50ms，且仅在靠近底部时滚动。
  - CodeHighlight 渲染策略：
    - 流式阶段使用原生 <pre> 文本展示，完成后一次性高亮，避免每次 chunk 触发高亮（可通过“流式中禁用高亮”开关）。
  - 连接去抖：关闭旧 EventSource 后延迟 50–100ms 再 create 新连接，规避浏览器/代理的短期拥塞。
- P1（可选）：
  - 抽象状态机（idle/generating/stopped/finished）统一触发 onStart/onStop 钩子，集中清理状态与定时器；
  - 根据 codeGenType 分流解析：HTML/MULTI_FILE 仅走 parseSimpleCodeStreaming/parseMultiFileStreaming，跳过与 VUE 项目相关的重型解析；
  - 消息区与代码区更新解耦：左侧仅显示文本摘要，右侧专注代码内容，减少重复格式化。

后端优化（保持语义不变）
- P0：
  - 首包心跳：contentFlux 之前先 emit 一条轻量 keepalive（如 data: "" 或注释行），确保前端快速进入 onmessage；
  - 减少合并开销：检查缓存与收集链路（cachedStream.collect(...)）的 CPU 占用，必要时将收集改为后台单线程 Executor；
  - 明确日志：打印“cancel 信号触发后 Xms 内停止推送”的统计，方便发现阻塞点。
- P1：
  - 上游中断：若 AI SDK 支持取消，cancel 时主动 dispose/abort，释放服务器资源，提升下一次 TTFB；
  - Netty flush 策略优化：确认 TEXT_EVENT_STREAM 逐条 flush，无 Nagle/缓冲延迟（仅在观测到服务端缓冲明显时启用）；
  - 限制单用户并发 runId（队列/信号），防止同 appId 的上一任务仍占用模型导致新任务首包慢。

验证清单
- 停止后立即继续与重新输入新消息两条路径的 TTFT 均满足目标；
- HTML/MULTI_FILE 在 60s 长流下无明显掉帧（≥45fps），DOM 更新批次稳定；
- 多次“停止→继续”无资源泄漏（定时器、EventSource、内存曲线）。

风险与回退
- 风险：批处理带来轻微 UI 延迟（30–50ms），但整体吞吐提升显著；
- 回退：保留最小化 P0（定时器清理 + 批处理），若 CodeHighlight 一次性渲染带来体验下降，则维持现状仅做节流。

进展更新（性能 P0 已部分落地）
- 前端：
  - 已实现 SSE micro-batching（约 40ms 刷新一次）与数组累积拼接，减少频繁字符串操作与 DOM 更新；
  - 停止时清理 code/multiFile 定时器与 SSE 刷新定时器，复位 HTML/MULTI_FILE 进行中状态；
  - 在 done/interrupted/onerror 时打印轻量指标日志（TTFT、吞吐、flush 次数）；
  - 停止后重连引入 80ms 去抖，避免短期网络拥塞导致的首包慢。
  - 流式阶段禁用语法高亮，使用轻量 <pre> 文本；完成后一次性 CodeHighlight 渲染（HTML / MULTI_FILE / VUE 当前文件）。
  - SSE 聚合由累计数组+join 改为累计字符串，避免 O(n^2) 拼接；组件卸载时主动 close EventSource 并 fire-and-forget 调用 /app/chat/stop。
- 后端：
  - 在 /app/chat/gen/code SSE 流开始时先发送 keepalive（data: {"d":""}），确保前端首包 onmessage 快速触发（降低 TTFT）。
- 待办：
  - 将指标上报为可视化监控（当前仅 console）。

---

合并发送/停止为单一按钮（UI 计划）

背景
- 根据截图 tmp/screenshots/20250903/fix-12.png，当前输入框右下角存在两个蓝色按钮：左侧“流控制（开始/停止/继续）”，右侧“发送”。希望合并为一个更简洁、美观、语义清晰的按钮，同时具备两者功能。

目标与交互（状态机）
- 单一按钮依据页面状态动态展示与触发不同动作：
  - 状态 S0 Idle（未输入、未在生成、未手动停止）：
    - 图标：纸飞机（发送）灰态；禁用。
    - 点击：无动作，提示“请输入提示词”。
  - 状态 S1 Ready（有输入，未在生成）：
    - 图标：纸飞机（发送）高亮；
    - 点击：发送消息（sendMessage），清空输入。
  - 状态 S2 Generating（生成中）：
    - 图标：方块（停止）高亮；
    - 点击：停止（onToggleStream → stop 分支），关闭 SSE 并发 /app/chat/stop。
  - 状态 S3 Stopped（已手动停止，且有 lastUserMessage，输入为空）：
    - 图标：播放/继续（▶）高亮；
    - 点击：继续（以新 runId 调用 generateCode(lastUserMessage)）。
  - 状态 S4 StoppedWithInput（已手动停止，但输入框中有新文本）：
    - 图标：纸飞机（发送）高亮；
    - 点击：发送新消息（优先级高于继续）。

视觉规范
- 仅保留一个主按钮，尺寸与现有发送按钮一致；
- 悬浮提示：根据当前状态动态显示（开始生成 / 发送 / 停止生成 / 继续生成）；
- 颜色：
  - 可用：#1677ff（保持一致）；
  - 禁用：降低不透明度 0.5；
  - 交互反馈：hover 加深，active 稍暗；
- 动画：图标在状态切换时淡入淡出 120ms；

实现步骤（前端）
1) 组件结构调整（AppChatPage.vue）
   - 移除独立的发送按钮 `<a-button type="primary" @click="sendMessage">`；
   - 保留一个统一按钮 `<button class="stream-send-toggle">`，放置在输入框右下角；
   - 图标切换依据 `btnState` 计算属性：`send` | `stop` | `continue` | `disabled`。
2) 计算属性与点击处理
   - `const btnState = computed(() => { ...依据 isGenerating / stoppedByUser / userInput / lastUserMessage... })`；
   - `onUnifiedButtonClick()`：
     - switch(btnState): send→sendMessage(); stop→onToggleStream(); continue→触发 generateCode(lastUserMessage); disabled→message.info('请输入提示词');
3) 键盘交互
   - Enter 键行为与按钮保持一致：
     - 生成中：Enter → 停止；
     - 其他：有输入 → 发送；
   - 避免误触：`@keydown.enter.prevent` 已存在，调用统一处理函数。
4) 运行关联与防抖
   - 继续/发送前复位定时器与缓冲；
   - 复用现有 runId 门禁（myRunId）与 /app/chat/stop 逻辑，确保不会混淆流。
5) 样式
   - 新增 `.stream-send-toggle`，沿用 `.stream-toggle` 的定位与尺寸；
   - 统一图标：
     - stop：■
     - send：纸飞机（现有图标）
     - continue：▶
     - disabled：纸飞机 + 50% 透明度；

实现步骤（后端）
- 无需改动 API；合并按钮仅改变前端触发路径。

测试与验收
- 用例：
  - 空输入：按钮禁用且提示；
  - 有输入未生成：点击=发送；Enter=发送；
  - 生成中：点击=停止；Enter=停止；
  - 停止后无输入：点击=继续；
  - 停止后有输入：点击=发送新消息；
  - 快速切换状态无闪烁、无误触（图标过渡 120ms）。

回退方案
- 保留原发送按钮代码块（注释保留一版），如遇异常可快速切回两按钮方案。

---

发送/停止合一按钮（执行清单）

背景与目标
- 图片参考：tmp/screenshots/20250903/fix-12.png（Windows 路径：D:\\Java\\ai-code\\ai-code-mother\\tmp\\screenshots\\20250903\\fix-12.png）。
- 合并现有“发送”和“停止/继续”两个按钮为一个主按钮，既能在生成中一键停止前端内容输出（同时请求后端停止），又能在空闲时一键发送当前输入，整体更简洁美观。

实施范围
- 仅前端（Vite + Vue 3）；后端 API 不变。

开发任务（分步）
1) 定义状态机与计算属性
   - 定义 `btnState: 'send' | 'stop' | 'continue' | 'disabled'`。
   - 依据 `isGenerating`、`stoppedByUser`、`userInput`、`lastUserMessage` 计算 `btnState`。

2) 组件改造（ai-code-mother-frontend/src/pages/app/AppChatPage.vue）
   - 移除独立发送按钮，保留一个主按钮 `.primary-action`（或沿用 `.stream-send-toggle`）。
   - 点击处理 `onPrimaryActionClick()`：
     - send → `sendMessage()`；
     - stop → `onToggleStream()`（关闭 SSE，调用 `/app/chat/stop`）；
     - continue → 以新 `runId` 触发 `generateCode(lastUserMessage)`；
     - disabled → `message.info('请输入提示词')`。
   - 键盘映射：Enter 和按钮一致；可选启用 Esc=停止（生成中）。

3) 视觉与交互
   - 图标：send=纸飞机，stop=方块（■），continue=▶，disabled=纸飞机（50% 不透明）。
   - 过渡：图标淡入淡出 120ms；hover/active 深浅变化保持与现有主色一致（#1677ff）。

4) 可靠性与防抖
   - 点击防抖 250–300ms，防重复发送/停止；
   - 发送/继续前清理定时器与缓冲；SSE URL 携带新 `runId`；
   - 生成中仅允许一次 stop 调用，避免重复请求。

5) 边界与优先级
   - 输入为空 → disabled；
   - 生成中 → stop；
   - 停止后有输入 → send 优先；
   - 停止后无输入但有 `lastUserMessage` → continue。

验收标准
- 空输入禁用；有输入未生成=发送；生成中=停止（≤200ms 停止前端输出并触发后端 stop）；停止后无输入=继续，有输入=发送。
- Enter 键行为与按钮一致；（若启用）Esc=停止。
- 按钮过渡平滑无闪烁；快速切换不误触；不会混淆不同 `runId` 的流。

回退方案
- 保留旧发送按钮代码（注释），出现异常可快速还原为双按钮方案。
