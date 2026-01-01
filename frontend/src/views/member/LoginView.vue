<script setup>
/**
 * LoginView.vue：登入頁面
 * 已轉換為純 JS，並移除所有會干擾執行的標記。
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const memUsername = ref('')
const memPassword = ref('')
const errorMsg = ref('')
const successMsg = ref('')

const login = () => {
  errorMsg.value = ''
  successMsg.value = ''

  axios
    .post('http://localhost:8080/login/member', {
      memUsername: memUsername.value,
      memPassword: memPassword.value,
    })
    .then(() => {
      successMsg.value = '登入成功'
      router.push('/admin')
    })
    .catch((err) => {
      // 這裡有使用到 err 變數，所以保留它
      if (err.response && err.response.data) {
        errorMsg.value = err.response.data
      } else {
        errorMsg.value = '伺服器連線失敗'
      }
    })
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
          <form @submit.prevent="login">
            <div class="input-group mb-3">
              <input
                v-model="memUsername"
                type="text"
                class="form-control"
                placeholder="帳號"
                required
              />
            </div>
            <div class="input-group mb-3">
              <input
                v-model="memPassword"
                type="password"
                class="form-control"
                placeholder="密碼"
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
</style>
