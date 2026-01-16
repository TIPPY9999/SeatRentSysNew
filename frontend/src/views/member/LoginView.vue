<script setup>
/**
 * LoginView.vue：登入頁面
 * 已轉換為純 JS，並移除所有會干擾執行的標記。
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

const memUsername = ref('')
const memPassword = ref('')
// errorMsg 與 successMsg 已經用 SweetAlert 取代，這裡不需要了

// 這是新的版本：使用 async/await 確保順序正確
const login = async () => {
  try {
    // 發送請求 (等待後端回應)
    const res = await axios.post('http://localhost:8080/login/member', {
      memUsername: memUsername.value,
      memPassword: memPassword.value,
    })

    // 3. 成功後執行這裡
    console.log('後端回應:', res.data) // 打開 F12 看結果

    // 取得 token (因為後端現在回傳 Map 物件，所以這裡拿 res.data.token)
    // 如果後端還沒改好回傳格式，這裡預設給一個 'login-ok' 當作臨時憑證
    const token = res.data.token || 'login-ok'

    // 關鍵步驟：一定要先存 Token，才能跳轉頁面
    sessionStorage.setItem('token', token)

    // 跳出成功視窗
    Swal.fire({
      icon: 'success',
      title: '登入成功',
      text: '歡迎回來！正在進入系統...',
      showConfirmButton: false, // 不顯示按鈕
      timer: 1500, // 1.5秒後自動關閉
    }).then(() => {
      // 4. 最後才跳轉 (這時候路由守衛檢查 sessionStorage 已經有東西了，就會放行)
      router.push('/admin')
    })
  } catch (err) {
    // 5. 失敗執行這裡 (這一段全部要在 catch 裡面)
    console.error(err)

    let errorText = '登入失敗'

    if (err.response && err.response.data) {
      // 處理後端回傳的錯誤訊息 (如果是物件就轉字串，如果是字串直接顯示)
      const msg = err.response.data
      errorText = typeof msg === 'object' ? JSON.stringify(msg) : msg
    }

    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: errorText,
      confirmButtonText: '確認',
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
