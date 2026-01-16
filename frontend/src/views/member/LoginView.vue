<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAdminAuthStore } from '@/stores/adminAuth'

const adminAuthStore = useAdminAuthStore()

const router = useRouter()

// 登入類型
const loginType = ref('member') // member | admin

// 共用輸入欄位（重點）
const account = ref('')
const password = ref('')

const errorMsg = ref('')
const successMsg = ref('')

// 切換登入身分時，清空輸入
watch(loginType, () => {
  account.value = ''
  password.value = ''
  errorMsg.value = ''
})

const login = () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (loginType.value === 'member') {
    axios
      .post('http://localhost:8080/login/member', {
        memUsername: account.value,
        memPassword: password.value,
      },
      {
        withCredentials: true,
      }
    )
      .then(() => {
        router.push('/member/profile') // 之後要做的會員頁
      })
      .catch((err) => {
        errorMsg.value = err.response?.data || '登入失敗'
      })
  } else {
    axios
      .post('http://localhost:8080/login/admin', {
        admUsername: account.value,
        admPassword: password.value,
      }, {
        withCredentials: true, // 🔥 一定要有（Session）
      })
      .then((res) => {
        // 🔥 直接用後端回傳的 admin 物件
        const adminData = {
          username: res.data.admUsername,
          name: res.data.admName,
          role: res.data.admRole,
        }

        // 存 Pinia
        adminAuthStore.setAdmin(adminData)

        // 存 localStorage（給重新整理用）
        localStorage.setItem('admin', JSON.stringify(adminData))

        // 進後台
        router.replace('/admin')
      })
      .catch((err) => {
        errorMsg.value = err.response?.data || '登入失敗'
      })
  }
}

// Particles 背景設定（只影響視覺）
const particlesOptions = {
  background: {
    color: { value: "#e9ecef" } 
  },
  particles: {
    color: { value: "#6c757d" },
    links: {
      enable: true,
      color: "#6c757d",
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
        mode: "grab",
      },
    },
  },
}
</script>

<template>
  <div class="login-page">

    <!-- 粒子背景 -->
    <vue-particles
      id="tsparticles"
      :options="particlesOptions"
      class="particles-bg"
    />

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
            <div v-if="errorMsg" class="text-danger small mb-2">{{ errorMsg }}</div>
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
</style>
