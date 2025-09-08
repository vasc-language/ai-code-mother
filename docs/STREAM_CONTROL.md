Stream Control: Stop/Resume SSE Generation

Overview
- Adds manual control to immediately stop an in-progress AI generation and resume with a fresh run.
- Implemented via a per-run `runId` coordinated between frontend and backend.

Endpoints
- `GET /app/chat/gen/code?appId&message&runId` (SSE)
  - `runId` optional; when missing, server generates one.
  - Stream terminates early when the matching run is cancelled.
- `POST /app/chat/stop` JSON `{ "runId": "..." }`
  - Cancels the active run. Only the owner of the run can stop it.

Server Internals
- `GenerationControlRegistry` keeps `runId -> GenerationControl` with a cancel signal and owner metadata.
- Streaming `Flux` pipelines apply `takeUntilOther(cancelSignal)` to end immediately upon cancel.
- Side-effects (chat history writes, Vue build, file saving) are skipped when a run is cancelled.

Frontend Behavior (AppChatPage.vue)
- A blue button in the input area toggles stop/resume.
  - Generating: shows `■` to stop.
  - Stopped/idle: shows `▶` to start/continue.
- On start: generates a `runId` and opens an EventSource to `/app/chat/gen/code` with that `runId`.
- On stop: closes EventSource and POSTs `/app/chat/stop` with the same `runId`.
- On continue: starts a new `runId` and resumes streaming into the same AI message block.

Compatibility
- The `runId` parameter is optional, so existing clients continue to work without manual stop.

