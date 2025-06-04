// 导入 polyfill 文件，确保在其他导入之前执行
import './polyfills'

// 为 sockjs-client 添加 global polyfill
(window as any).global = window;

import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { loadConfig } from './config'


// 先加载配置，再初始化应用
async function initApp() {
  // 加载配置
  await loadConfig();

  const app = createApp(App)

  app.use(createPinia())
  app.use(router)

  app.mount('#app')
}

// 启动应用
initApp().catch(error => {
  console.error('应用初始化失败:', error);
})
