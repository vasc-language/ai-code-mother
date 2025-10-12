import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd, { message } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 导入 Lovable 深色主题样式
import '@/styles/lovable-theme.css'

import '@/access'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// 配置 message 组件挂载到 body,确保不被其他元素遮挡
message.config({
  getContainer: () => document.body,
  top: '24px',
  maxCount: 3
})

app.mount('#app')
