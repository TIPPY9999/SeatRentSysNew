import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// ✅ 只載入我們自己寫的客製化修正，不載入 main.css
import './assets/custom.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
