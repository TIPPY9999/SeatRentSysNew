<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router' // 引入 useRoute
import axios from 'axios'

//  SweetAlert2：統一用彈窗顯示成功/失敗訊息（取代 errorMsg/successMsg）
import Swal from 'sweetalert2'

//  Pinia：引入兩種 store
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useMemberAuthStore } from '@/stores/memberAuth'

const adminAuthStore = useAdminAuthStore()
const memberAuthStore = useMemberAuthStore()
const router = useRouter()
const route = useRoute() // 建立 route 實例

/**
 * 登入類型：
 * - member：會員登入（導向 /member/profile）
 * - admin：管理員登入（導向 /admin）
 */
const loginType = ref('member') // member | admin

// 共用輸入欄位（會員/管理員都用同一組欄位）
const account = ref('')
const password = ref('')

// 切換登入身分時，清空輸入，避免誤送上一種身分的帳密
watch(loginType, () => {
  account.value = ''
  password.value = ''
})

/**
 * 登入主流程（async/await）
 * 目的：
 * 1) 避免 Promise then/catch 的順序 race condition
 * 2) 確保「先寫入 localStorage」再導頁，路由守衛才會放行
 */
const login = async () => {
  try {
    // =========================
    // 會員登入
    // =========================
    if (loginType.value === 'member') {
      const res = await axios.post(
        'http://localhost:8080/login/member',
        {
          memUsername: account.value,
          memPassword: password.value,
        },
        {
          withCredentials: true,
        },
      )

      /**
       *  token 儲存規則（統一 localStorage）
       * - main.js 的 axios interceptor 從 localStorage 取 token
       * - 所以這裡也必須存 localStorage
       * - 若後端沒回 token，暫用 'login-ok'（你原本 HEAD 的策略）避免流程卡死
       */
      const token = res.data?.token || 'login-ok'
      localStorage.setItem('token', token)

      adminAuthStore.clearAdmin()
      localStorage.removeItem('admin')

      //  [+] 將登入狀態存入 Pinia，讓整個 App 保持同步
      memberAuthStore.setMemberLogin({
        memId: res.data.memId,
        memUsername: res.data.memUsername,
        memName: res.data.memName,
        memPoints: res.data.memPoints,
        memInvoice: res.data.memInvoice,
      })

      //  [+] 將會員資料存入 localStorage，實現持久化登入
      localStorage.setItem('member_user', JSON.stringify(res.data))

      // 成功提示
      await Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: '歡迎回來！',
        showConfirmButton: false,
        timer: 1200,
      })

      //  檢查是否有重定向路徑，若有則跳轉，否則導向會員頁
      if (route.query.redirect) {
        router.push(route.query.redirect)
      } else {
        router.push('/')
      }
      return
    }

    // =========================
    // 管理員登入
    // =========================
    const res = await axios.post(
      'http://localhost:8080/login/admin',
      {
        admUsername: account.value,
        admPassword: password.value,
      },
      {
        withCredentials: true, // 如果後端有用 Session/Cookie 驗證，這行保留最安全
      },
    )

    /**
     *  token 儲存（統一 localStorage）
     * - 若後端有回 token 就用，沒有就給臨時值
     */
    const token = res.data?.token || 'admin-login-ok'
    localStorage.setItem('token', token)

    /**
     *  admin 儲存（統一 localStorage + Pinia）
     * - router/index.js 的後台守衛會檢查 localStorage.admin 是否存在
     * - AdminLayout 會顯示 adminAuthStore.admin.name/username
     * 所以：Pinia + localStorage 兩邊都要存
     */
    const adminData = {
      username: res.data?.admUsername,
      name: res.data?.admName,
      role: res.data?.admRole,
    }

    memberAuthStore.clearMemberLogin()
    localStorage.removeItem('member_user')

    adminAuthStore.setAdmin(adminData)
    localStorage.setItem('admin', JSON.stringify(adminData))

    // 成功提示
    await Swal.fire({
      icon: 'success',
      title: '管理員登入成功',
      text: '歡迎回來！',
      showConfirmButton: false,
      timer: 1200,
    })

    //  進後台（replace 避免回上一頁又回到登入頁）
    router.push('/')
  } catch (err) {
    console.error(err)

    // 錯誤訊息整理：後端可能回字串或物件
    let errorText = '登入失敗'
    if (err.response && err.response.data) {
      const msg = err.response.data
      errorText = typeof msg === 'object' ? JSON.stringify(msg) : String(msg)
    }

    // 失敗提示
    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: errorText,
      confirmButtonText: '確認',
    })
  }
}

/**
 * Particles 背景設定（只影響視覺，不影響登入邏輯）
 * 注意：main.js 需已 app.use(Particles) 且已載入 loadSlim
 */
const particlesOptions = {
  background: {
    color: { value: '#e9ecef' },
  },
  particles: {
    color: { value: '#6c757d' },
    links: {
      enable: true,
      color: '#6c757d',
      distance: 150,
      opacity: 0.3,
      width: 1,
    },
    move: {
      enable: true,
      speed: 1.2,
    },
    number: {
      value: 60,
    },
    size: {
      value: { min: 1, max: 3 },
    },
  },
  interactivity: {
    events: {
      onHover: {
        enable: true,
        mode: 'grab',
      },
    },
  },
}

const goRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <!-- 粒子背景 -->
    <vue-particles id="tsparticles" :options="particlesOptions" class="particles-bg" />

    <div class="login-box">
      <div class="card card-outline card-primary">
        <div class="card-header text-center">
          <h1 class="h1"><b>SeatRentSys</b></h1>
        </div>
        <div class="card-body">
          <div class="login-switch">
            <button
              :class="{ active: loginType === 'member' }"
              @click="loginType = 'member'"
              type="button"
            >
              會員登入
            </button>

            <button
              :class="{ active: loginType === 'admin' }"
              @click="loginType = 'admin'"
              type="button"
            >
              管理員登入
            </button>
          </div>

          <form @submit.prevent="login">
            <div class="input-group mb-3">
              <input
                v-model="account"
                type="text"
                class="form-control"
                :placeholder="loginType === 'member' ? '會員帳號' : '管理員帳號'"
                required
              />
            </div>
            <div class="input-group mb-3">
              <input
                v-model="password"
                type="password"
                class="form-control"
                :placeholder="loginType === 'member' ? '會員密碼' : '管理員密碼'"
                required
              />
            </div>

            <div v-if="loginType === 'member'" class="register-link">
              <span @click="goRegister">註冊會員</span>
            </div>

            <button type="submit" class="btn btn-primary btn-block">登入</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 登入頁面主容器 ========== */
.login-page {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #9db8d4 0%, #8aa5c1 100%);
}

/* 粒子背景：最底層 */
.particles-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

/* ========== 登入框：Glassmorphism 風格 ========== */
.login-box {
  width: 400px;
  position: relative;
  z-index: 10;
}

.login-box .card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.login-box .card-header {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  padding: 24px;
  border-bottom: none;
}

.login-box .card-header h1 {
  color: #ffffff;
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.login-box .card-body {
  padding: 28px;
}

/* ========== 登入切換按鈕 ========== */
.login-switch {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.login-switch button {
  padding: 10px 24px;
  border: 2px solid #e2e8f0;
  background-color: #f8fafc;
  color: #475569;
  cursor: pointer;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.login-switch button:hover {
  background-color: #e2e8f0;
  border-color: #cbd5e1;
}

.login-switch button.active {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* ========== 輸入欄位 ========== */
.login-box .input-group {
  margin-bottom: 16px;
}

.login-box .form-control {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
  background: #f8fafc;
}

.login-box .form-control:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
  outline: none;
  background: #ffffff;
}

.login-box .form-control::placeholder {
  color: #94a3b8;
}

/* ========== 註冊連結 ========== */
.register-link {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.register-link span {
  font-size: 14px;
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}

.register-link span:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* ========== 登入按鈕 ========== */
.login-box .btn-primary {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.login-box .btn-primary:hover {
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.login-box .btn-primary:active {
  transform: translateY(1px);
}
</style>
