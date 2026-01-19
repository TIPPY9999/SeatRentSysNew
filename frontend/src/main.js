import './assets/custom.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// --- [ADD] 引入 vue-google-maps ---
import VueGoogleMaps from '@fawmi/vue-google-maps'
// Particles.js（Vue 3 版）
import Particles from '@tsparticles/vue3'
import { loadSlim } from '@tsparticles/slim'

import App from './App.vue'
import router from './router'

import { ElMessageBox } from 'element-plus'

// Axios request interceptor
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

let isAuthExpiredDialogShowing = false

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''

    // 只處理「非登入 API」＋「已登入狀態」的 401
    if (
      status === 401 &&
      !url.includes('/login') && // 排除登入請求
      localStorage.getItem('admin') && // 確定曾經登入
      !isAuthExpiredDialogShowing
    ) {
      isAuthExpiredDialogShowing = true

      ElMessageBox.alert('登入已過期，請重新登入', '登入狀態失效', {
        confirmButtonText: '確認',
        type: 'warning',
        showClose: false,
        closeOnClickModal: false,
      }).then(() => {
        isAuthExpiredDialogShowing = false
        localStorage.removeItem('token')
        localStorage.removeItem('admin')
        router.push('/login')
      })
    }

    return Promise.reject(error)
  },
)

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Element Plus
app.use(ElementPlus)

// --- 啟用 vue-google-maps 並設定 API 金鑰 ---
app.use(VueGoogleMaps, {
  load: {
<<<<<<< HEAD
    key: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
=======
    key: 'AIzaSyCu6YRYdgvvOg2aLI6K5L3R0GtnyyfRe_M', //BY zax
>>>>>>> origin/merchantAndCoupon2
    libraries: ['marker'], // 載入 marker 函式庫
    v: 'quarterly', // 指定載入穩定的 API 版本
  },
})

// --- [NEW] 4. 自動註冊所有圖示 (解決方塊亂碼問題) ---
// Element Plus Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化 Particles（只做一次）
app.use(Particles, {
  init: async (engine) => {
    await loadSlim(engine)
  },
})

app.mount('#app')
