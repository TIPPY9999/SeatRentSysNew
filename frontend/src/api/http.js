/**
 * HTTP Client 統一封裝
 * - 使用 axios.create 建立實體
 * - 統一錯誤處理 (使用 SweetAlert2)
 * - 自動加上 /api 前綴
 */
import axios from 'axios'
import Swal from 'sweetalert2'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const TUNNEL_API = window.APP_CONFIG?.API_URL; // 從設定檔抓 Tunnel 網址
const LOCAL_API = "http://localhost:8080";

let currentBaseURL = LOCAL_API; // 預設用本機
if (window.location.hostname.includes("trycloudflare.com")) { 
// 而且設定檔裡面有填 Tunnel 網址 
if (TUNNEL_API) { currentBaseURL = TUNNEL_API; console.log("🌐 偵測到外部連線，切換至 Tunnel API");
 } else { console.log("⚠️ 雖然在 Tunnel，但設定檔未填 API_URL，仍使用 Localhost"); } 
} else { console.log("🏠 偵測到本機連線，使用 Localhost API");}

// 建立 axios 實體
const http = axios.create({
  baseURL: '/api', // Vite Proxy 會將 /api 轉發到 http://localhost:8080
  timeout: 10000, // 10 秒逾時
  headers: {
    'Content-Type': 'application/json',
  },
})

// ============ Request 攔截器 ============
http.interceptors.request.use(
  (config) => {
    // 可在此加入 Token 等驗證資訊
    // const token = localStorage.getItem('token')
    // if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ============ Response 攔截器 ============
http.interceptors.response.use(
  (response) => {
    // 成功回應，直接回傳 data
    return response
  },
  (error) => {
    // 錯誤處理：使用 SweetAlert2 顯示錯誤訊息
    const status = error.response?.status
    const message = error.response?.data?.message 
      || error.response?.data?.error 
      || error.response?.data 
      || '發生未知錯誤，請稍後再試'

    // 根據狀態碼顯示不同訊息
    if (status === 400) {
      Swal.fire('請求錯誤', message, 'warning')
    } else if (status === 401) {
      Swal.fire('未授權', '請重新登入', 'error')
      // 可在此導向登入頁
      // window.location.href = '/login'
    } else if (status === 403) {
      Swal.fire('權限不足', '您沒有權限執行此操作', 'error')
    } else if (status === 404) {
      Swal.fire('找不到資源', message, 'error')
    } else if (status === 500) {
      Swal.fire('伺服器錯誤', message, 'error')
    } else if (error.code === 'ECONNABORTED') {
      Swal.fire('連線逾時', '請檢查網路連線', 'error')
    } else if (!error.response) {
      Swal.fire('網路錯誤', '無法連線到伺服器', 'error')
    } else {
      Swal.fire('錯誤', message, 'error')
    }

    return Promise.reject(error)
  }
)

export default http
