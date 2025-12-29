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

  // 讓 /images 和 /api 都轉到後端 8080
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/images': 'http://localhost:8080',
    },
  },
})
