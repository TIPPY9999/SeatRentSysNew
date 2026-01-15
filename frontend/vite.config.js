import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 讓前端開發伺服器 (Port 5173) 幫忙轉送請求到後端 (Port 8080)
    proxy: {
      // 1. 處理登入請求 (對應 LoginView.vue)
      '/login': {
        target: 'http://localhost:8080', //
      },

      // 2. 處理座位管理 (對應 SeatList.vue 等，路徑為 /seat/...)
      '/seat': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },

      // 3. 處理景點管理 (對應 SpotList.vue 等，組員程式碼有用 /api 開頭)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 因為組員前端呼叫 '/api/spot/list'，但後端通常是 '/spot/list'
        // 所以這裡用 rewrite 把 '/api' 去掉再送給後端
        rewrite: (path) => path.replace(/^\/api/, ''),
      },

      // 4. 處理圖片路徑
      '/images': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  //Vue 跨域環境設定
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080/SeatRentSysNew',
        changeOrigin: true,
      },
    },
  },
})
