/**
 * 应用程序主入口文件
 * 负责初始化 Vue 应用并配置全局插件
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 创建 Vue 应用实例
const app = createApp(App)

// 配置状态管理
app.use(createPinia())
// 配置路由
app.use(router)
// 配置 Ant Design Vue 组件库
app.use(Antd)

// 挂载应用到 DOM
app.mount('#app')
