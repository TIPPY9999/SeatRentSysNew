import './assets/custom.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// --- [NEW] 1. 引入 Element Plus 主程式與樣式 ---
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// --- [NEW] 2. 引入 Element Plus 所有圖示 ---
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// --- [NEW] 3. 啟用 Element Plus ---
app.use(ElementPlus)

// --- [NEW] 4. 自動註冊所有圖示 (解決方塊亂碼問題) ---
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
