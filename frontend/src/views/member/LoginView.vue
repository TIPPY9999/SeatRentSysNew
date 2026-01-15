<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

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
      })
      .then(() => {
        router.push('/admin')
      })
      .catch((err) => {
        errorMsg.value = err.response?.data || '登入失敗'
      })
  }
}
</script>

<template>
  <div class="login-page">
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
  background-color: #e9ecef;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 360px;
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
