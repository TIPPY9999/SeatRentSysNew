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
    // 管理員登入 (最簡化修正版)
    // =========================
    const res = await axios.post(
      'http://localhost:8080/login/admin',
      {
        admUsername: account.value,
        admPassword: password.value,
      },
      {
        withCredentials: true,
      },
    )

    // 1. 只清掉「會員」的資料，不要用 clear()
    localStorage.removeItem('member_user');
    memberAuthStore.clearMemberLogin();

    // 2. 存入管理員 Token 與資料
    const token = res.data?.token || 'admin-login-ok'
    localStorage.setItem('token', token)

    const adminData = {
      username: res.data?.admUsername,
      name: res.data?.admName,
      role: res.data?.admRole,
    }

    // 3. 同步寫入 Pinia 和 LocalStorage
    adminAuthStore.setAdmin(adminData)
    localStorage.setItem('admin', JSON.stringify(adminData))

    // 4. 成功提示
    await Swal.fire({
      icon: 'success',
      title: '管理員登入成功',
      text: '歡迎回來！',
      showConfirmButton: false,
      timer: 1000,
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

const loginWithGoogle = () => {
  // 加上時間戳記 t=${Date.now()} 確保每次請求都是新的，不被瀏覽器緩存網址
  const googleLoginUrl = `http://localhost:8080/oauth2/authorization/google?prompt=select_account&t=${Date.now()}`;
  window.location.href = googleLoginUrl;
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

            <div
              v-if="loginType === 'member'"
              class="register-link"
            >
              <span @click="goRegister">註冊會員</span>
            </div>

            <div class="google-login-section">
              <div class="divider">
                <span>或使用第三方登入</span>
              </div>
              
              <button type="button" class="btn-google" @click="loginWithGoogle">
                <img src="https://developers.google.com/static/identity/images/g-logo.png" alt="Google" />
                使用 Google 帳號登入
              </button>
            </div>

            <button type="submit" class="btn btn-primary btn-block">登入</button>
            <div class="back-to-home" @click="router.push('/')">
               <el-icon><ArrowLeft /></el-icon>
               <span>返回首頁</span>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 粒子背景：最底層 */
.particles-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

/* 登入框：浮在上面 */
.login-box {
  width: 360px;
  position: relative;
  z-index: 10;
}

.login-switch {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.login-switch button {
  padding: 6px 16px;
  border: 1px solid #ccc;
  background-color: #f3f4f6;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
}

.login-switch button.active {
  background-color: #007bff;
  color: #fff;
  border-color: #007bff;
}

.register-link {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.register-link span {
  font-size: 13px;
  color: #007bff;
  cursor: pointer;
}

.register-link span:hover {
  text-decoration: underline;
}

.back-to-home {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 15px; /* 與登入按鈕拉開距離 */
  padding-top: 10px;
  border-top: 1px inset #f0f0f0; /* 加一條淡淡的分隔線 */
  color: #6c757d; /* 灰色，不搶走登入按鈕的焦點 */
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
  gap: 5px;
}

.back-to-home:hover {
  color: #007bff; /* 懸浮時變回藍色 */
  text-decoration: underline;
}

/* 確保 el-icon 在文字中間 */
.back-to-home .el-icon {
  font-size: 12px;
}

.google-login-section {
  margin-top: 20px;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  color: #888;
  font-size: 12px;
}

.divider::before, .divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: #eee;
}

.divider span {
  padding: 0 10px;
}

.btn-google {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #ffffff;
  border: 1px solid #dadce0;
  border-radius: 4px;
  color: #3c4043;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-google img {
  width: 18px;
  height: 18px;
}

.btn-google:hover {
  background-color: #f8f9fa;
  box-shadow: 0 1px 2px 0 rgba(60,64,67,.30), 0 1px 3px 1px rgba(60,64,67,.15);
}
</style>
