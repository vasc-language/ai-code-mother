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

进展更新（P0 已完成）
- 后端：新增 `GenerationControlRegistry`；`GET /app/chat/gen/code` 支持 `runId`（可选，未传会自动生成）；新增 `POST /app/chat/stop`；在 Facade 与 Handlers 中接入取消信号并在取消时跳过落库与 Vue 构建。
- 前端：在 `AppChatPage.vue` 输入框右下角增加蓝色按钮（■/▶）。生成阶段点击可立即停止并关闭 SSE；再次点击以新 `runId` 重启流，继续在同一 AI 消息上填充。

使用说明
- 停止：生成过程中点击蓝色按钮（■），前端停止显示并调用 `/app/chat/stop`；服务端随即结束 SSE 流。
- 继续：按钮变为 ▶，点击后以新 `runId` 重新订阅 `/app/chat/gen/code` 并继续输出。
- 兼容：`runId` 可选，旧页面未传依然可用，但无法进行手动停止。
