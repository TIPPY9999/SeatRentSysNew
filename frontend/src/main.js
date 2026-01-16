import './assets/custom.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// --- [NEW] 1. 引入 Element Plus 主程式與樣式 ---
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// --- [NEW] 2. 引入 Element Plus 所有圖示 ---
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// --- [ADD] 引入 vue-google-maps ---
import VueGoogleMaps from '@fawmi/vue-google-maps'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// --- [NEW] 3. 啟用 Element Plus ---
app.use(ElementPlus)

// --- 啟用 vue-google-maps 並設定 API 金鑰 ---
app.use(VueGoogleMaps, {
  load: {
    key: 'AIzaSyCu6YRYdgvvOg2aLI6K5L3R0GtnyyfRe_M',//BY zax
    libraries: ['marker'], // 載入 marker 函式庫
    v: 'quarterly', // 指定載入穩定的 API 版本
  },
})

// --- [NEW] 4. 自動註冊所有圖示 (解決方塊亂碼問題) ---
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
