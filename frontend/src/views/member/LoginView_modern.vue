<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'

const adminAuthStore = useAdminAuthStore()
const authStore = useAuthStore()
const router = useRouter()

const loginType = ref('member')
const account = ref('')
const password = ref('')
const isLoading = ref(false)

const loginTitle = computed(() => {
  return loginType.value === 'member' ? '會員登入' : '管理員登入'
})

const login = async () => {
  if (!account.value || !password.value) {
    Swal.fire({
      icon: 'warning',
      title: '請填寫帳號密碼',
      showConfirmButton: false,
      timer: 1500,
    })
    return
  }

  isLoading.value = true

  try {
    if (loginType.value === 'member') {
      const res = await axios.post('http://localhost:8080/login/member', {
        memUsername: account.value,
        memPassword: password.value,
      })

      const { token, member } = res.data

      localStorage.setItem('token', token)
      localStorage.setItem('member', JSON.stringify(member))
      authStore.login()

      await Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: `歡迎回來，${member.memName || '會員'}！`,
        showConfirmButton: false,
        timer: 1500,
      })

      router.push('/member/profile')
    } else {
      const res = await axios.post('http://localhost:8080/login/admin', {
        adminUsername: account.value,
        adminPassword: password.value,
      })

      const { token, admin } = res.data

      localStorage.setItem('token', token)
      localStorage.setItem('admin', JSON.stringify(admin))
      adminAuthStore.setAdmin(admin)

      await Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: `歡迎，${admin.adminName || '管理員'}！`,
        showConfirmButton: false,
        timer: 1500,
      })

      router.push('/admin')
    }
  } catch (error) {
    console.error('登入錯誤:', error)
    let errorText = '帳號或密碼錯誤，請重新輸入'
    if (error.response?.data?.message) {
      errorText = error.response.data.message
    }

    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: errorText,
      confirmButtonText: '確認',
    })
  } finally {
    isLoading.value = false
  }
}

const goRegister = () => {
  router.push('/register')
}

const particlesOptions = {
  background: {
    color: { value: '#f0f2f5' },
  },
  particles: {
    color: { value: '#4285f4' },
    links: {
      enable: true,
      color: '#4285f4',
      distance: 120,
      opacity: 0.15,
      width: 1,
    },
    move: {
      enable: true,
      speed: 0.8,
    },
    number: {
      value: 40,
    },
    size: {
      value: { min: 1, max: 2 },
    },
    opacity: {
      value: 0.3,
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
</script>

<template>
  <div class="modern-login-page">
    <vue-particles id="tsparticles" :options="particlesOptions" class="particles-bg" />

    <div class="login-container">
      <div 
        class="login-card"
        v-motion
        :initial="{ opacity: 0, y: 50 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 600, ease: 'easeOut' } }"
      >
        <!-- Logo & Title -->
        <div class="logo-section">
          <div class="logo-icon">
            <i class="fas fa-chair"></i>
          </div>
          <h1 class="brand-title">SeatRentSys</h1>
          <p class="brand-subtitle">座位租賃管理系統</p>
        </div>

        <!-- Login Type Toggle -->
        <div class="login-type-toggle">
          <button
            :class="['toggle-btn', { active: loginType === 'member' }]"
            @click="loginType = 'member'"
            type="button"
            v-motion
            :initial="{ scale: 1 }"
            :tap="{ scale: 0.95 }"
          >
            <i class="fas fa-user"></i>
            <span>會員登入</span>
          </button>
          <button
            :class="['toggle-btn', { active: loginType === 'admin' }]"
            @click="loginType = 'admin'"
            type="button"
            v-motion
            :initial="{ scale: 1 }"
            :tap="{ scale: 0.95 }"
          >
            <i class="fas fa-user-shield"></i>
            <span>管理員登入</span>
          </button>
        </div>

        <!-- Login Form -->
        <form @submit.prevent="login" class="login-form">
          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-user-circle"></i>
              帳號
            </label>
            <input
              v-model="account"
              type="text"
              class="form-input"
              :placeholder="loginType === 'member' ? '請輸入會員帳號' : '請輸入管理員帳號'"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label">
              <i class="fas fa-lock"></i>
              密碼
            </label>
            <input
              v-model="password"
              type="password"
              class="form-input"
              :placeholder="loginType === 'member' ? '請輸入會員密碼' : '請輸入管理員密碼'"
              required
            />
          </div>

          <div v-if="loginType === 'member'" class="form-footer">
            <span class="register-link" @click="goRegister">
              <i class="fas fa-user-plus"></i>
              註冊新會員
            </span>
          </div>

          <button 
            type="submit" 
            class="login-btn"
            :disabled="isLoading"
            v-motion
            :initial="{ scale: 1 }"
            :hover="{ scale: 1.02 }"
            :tap="{ scale: 0.98 }"
          >
            <span v-if="!isLoading">
              <i class="fas fa-sign-in-alt"></i>
              {{ loginTitle }}
            </span>
            <span v-else>
              <i class="fas fa-spinner fa-spin"></i>
              登入中...
            </span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modern-login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.particles-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15),
              0 8px 16px rgba(0, 0, 0, 0.1);
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.login-type-toggle {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 32px;
  padding: 6px;
  background: #f3f4f6;
  border-radius: 12px;
}

.toggle-btn {
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: #6b7280;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.toggle-btn i {
  font-size: 16px;
}

.toggle-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-label i {
  color: #667eea;
  font-size: 16px;
}

.form-input {
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: -8px;
}

.register-link {
  font-size: 14px;
  color: #667eea;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.register-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-btn {
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  transform: translateY(-1px);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-btn i {
  font-size: 18px;
}

/* 響應式設計 */
@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 28px;
  }

  .logo-icon {
    width: 64px;
    height: 64px;
    font-size: 32px;
  }
}
</style>
