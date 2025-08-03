"D:\Program Files\nodejs\npm.cmd" run openapi2ts

> ai-code-mother-frontend@0.0.0 openapi2ts
> openapi2ts

fetch openapi error: FetchError: request to http://localhost:8123/api/v3/api-docs failed, reason:
at ClientRequest.<anonymous> (D:\Java\ai-code\ai-code-mother\ai-code-mother-frontend\node_modules\node-fetch\lib\index.js:1501:11)
at ClientRequest.emit (node:events:518:28)
at emitErrorEvent (node:_http_client:104:11)
at Socket.socketErrorListener (node:_http_client:518:5)
at Socket.emit (node:events:518:28)
at emitErrorNT (node:internal/streams/destroy:170:8)
at emitErrorCloseNT (node:internal/streams/destroy:129:3)
at process.processTicksAndRejections (node:internal/process/task_queues:90:21) {
type: 'system',
errno: 'ECONNREFUSED',
code: 'ECONNREFUSED'
}
TypeError: Cannot destructure property 'info' of 'this.openAPIData' as it is null.

Process finished with exit code 0


jiajiang