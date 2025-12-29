// src/main.ts

// [修正] 移除這行！因為 index.html 已經從 public 引入了 CSS
// 留著它會導致 "找不到檔案" 或是 "樣式重複" 的問題
// import './assets/vendor/adminlte/dist/css/adminlte.min.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// [保留] 這是你自己寫的客製化樣式，放在 src/assets 是正確的
import './assets/custom.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
