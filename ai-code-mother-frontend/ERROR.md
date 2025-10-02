"D:\Program Files\nodejs\npm.cmd" run dev

> yu-ai-code-mother-frontend@0.0.0 dev
> vite



VITE v7.0.4  ready in 598 ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
➜  Vue DevTools: Open http://localhost:5173/__devtools__/ as a separate window
➜  Vue DevTools: Press Alt(⌥)+Shift(⇧)+D in App to toggle the Vue DevTools
➜  press h + enter to show help
20:31:46 [vite] (client) Pre-transform error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/SignInPage.vue". Does the file exist?
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/SignInPage.vue:130:37
29 |  } from "@ant-design/icons-vue";
30 |  import { dailySignIn, getSignInStatus } from "@/api/qiedaoguanli";
31 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
32 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
33 |    __name: "SignInPage",
20:31:46 [vite] (client) Pre-transform error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/InvitePage.vue". Does the file exist?
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/InvitePage.vue:182:37
27 |  import dayjs from "dayjs";
28 |  import { getInviteCode, getInviteRecords } from "@/api/yaoqingguanli";
29 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
30 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
31 |    __name: "InvitePage",
20:31:46 [vite] (client) Pre-transform error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/PointsDetailPage.vue". Does the file exist?
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/PointsDetailPage.vue:146:37
33 |  import "dayjs/locale/zh-cn";
34 |  import { getPointsOverview, getPointsRecords } from "@/api/jifenguanli";
35 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
36 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
37 |    __name: "PointsDetailPage",

20:31:46 [vite] Internal server error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/SignInPage.vue". Does the file exist?   
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/SignInPage.vue:130:37
29 |  } from "@ant-design/icons-vue";
30 |  import { dailySignIn, getSignInStatus } from "@/api/qiedaoguanli";
31 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
32 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
33 |    __name: "SignInPage",
at TransformPluginContext._formatLog (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31435:43)
at TransformPluginContext.error (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31432:14)
at normalizeUrl (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:29978:18)  
at process.processTicksAndRejections (node:internal/process/task_queues:105:5)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30036:32
at async Promise.all (index 7)
at async TransformPluginContext.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30004:4)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite-plugin-vue-devtools/node_modules/vite-plugin-inspect/dist/index.mjs:1128:17
at async EnvironmentPluginContainer.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31249:14)
at async loadAndTransform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:26419:26)

20:31:46 [vite] Internal server error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/InvitePage.vue". Does the file exist?   
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/InvitePage.vue:182:37
27 |  import dayjs from "dayjs";
28 |  import { getInviteCode, getInviteRecords } from "@/api/yaoqingguanli";
29 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
30 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
31 |    __name: "InvitePage",
at TransformPluginContext._formatLog (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31435:43)
at TransformPluginContext.error (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31432:14)
at normalizeUrl (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:29978:18)  
at process.processTicksAndRejections (node:internal/process/task_queues:105:5)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30036:32
at async Promise.all (index 8)
at async TransformPluginContext.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30004:4)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite-plugin-vue-devtools/node_modules/vite-plugin-inspect/dist/index.mjs:1128:17
at async EnvironmentPluginContainer.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31249:14)
at async loadAndTransform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:26419:26)

20:31:47 [vite] Internal server error: Failed to resolve import "@/api/yonghuguanli" from "src/pages/points/PointsDetailPage.vue". Does the file exist?
Plugin: vite:import-analysis
File: D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/src/pages/points/PointsDetailPage.vue:146:37
33 |  import "dayjs/locale/zh-cn";
34 |  import { getPointsOverview, getPointsRecords } from "@/api/jifenguanli";
35 |  import { getLoginUserUsingGet } from "@/api/yonghuguanli";
|                                        ^
36 |  const _sfc_main = /* @__PURE__ */ _defineComponent({
37 |    __name: "PointsDetailPage",
at TransformPluginContext._formatLog (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31435:43)
at TransformPluginContext.error (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31432:14)
at normalizeUrl (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:29978:18)  
at process.processTicksAndRejections (node:internal/process/task_queues:105:5)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30036:32
at async Promise.all (index 10)
at async TransformPluginContext.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:30004:4)
at async file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite-plugin-vue-devtools/node_modules/vite-plugin-inspect/dist/index.mjs:1128:17
at async EnvironmentPluginContainer.transform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:31249:14)
at async loadAndTransform (file:///D:/Java/ai-code/ai-code-mother/ai-code-mother-frontend/node_modules/vite/dist/node/chunks/dep-DZ2tZksn.js:26419:26)


