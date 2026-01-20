<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router' // 引入 useRoute
import axios from 'axios'

//  SweetAlert2：統一用彈窗顯示成功/失敗訊息（取代 errorMsg/successMsg）
import Swal from 'sweetalert2'

//  Pinia：引入兩種 store
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'

const adminAuthStore = useAdminAuthStore()
const authStore = useAuthStore() // 引入會員 store
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

      //  [+] 將登入狀態存入 Pinia，讓整個 App 保持同步
      authStore.login(res.data, 'member')

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
        router.push('/member/profile')
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

    adminAuthStore.setAdmin(adminData)
    localStorage.setItem('admin', JSON.stringify(adminData))

    // 成功提示
    await Swal.fire({
      icon: 'success',
      title: '管理員登入成功',
      text: '正在進入後台...',
      showConfirmButton: false,
      timer: 1200,
    })

    //  進後台（replace 避免回上一頁又回到登入頁）
    router.replace('/admin')
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

            <div
              v-if="loginType === 'member'"
              class="register-link"
            >
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
</style>
