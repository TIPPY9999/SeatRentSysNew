<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

const form = reactive({
  memName: '',
  memUsername: '',
  memEmail: '',
  memPhone: '',
  memPassword: '',
})

const confirmPassword = ref('')

const submit = async () => {
  // 1️⃣ 空值檢查
  if (
    !form.memName ||
    !form.memUsername ||
    !form.memEmail ||
    !form.memPhone ||
    !form.memPassword ||
    !confirmPassword.value
  ) {
    Swal.fire('錯誤', '請填寫所有欄位', 'warning')
    return
  }

  // 2️⃣ Email 格式驗證
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.memEmail)) {
    Swal.fire('錯誤', '電子郵件格式不正確', 'error')
    return
  }

  // 3️⃣ 手機號碼驗證（只允許數字，8~15 碼你可調）
  const phoneRegex = /^\d{8,15}$/
  if (!phoneRegex.test(form.memPhone)) {
    Swal.fire('錯誤', '手機號碼格式不正確', 'error')
    return
  }

  // 4️⃣ 密碼一致檢查
  if (form.memPassword !== confirmPassword.value) {
    Swal.fire('錯誤', '兩次密碼輸入不一致', 'error')
    return
  }

  // 5️⃣ 送後端
  try {
    await axios.post('http://localhost:8080/members', form)
    await Swal.fire('成功', '註冊成功，請登入', 'success')
    router.push('/login')
  } catch (err) {
    Swal.fire('註冊失敗', err.response?.data || '錯誤', 'error')
  }
}

const goLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-card">
    <h2 class="page-title">電子郵件註冊</h2>
    
    <form @submit.prevent="submit" novalidate class="register-form">
      <div class="form-group">
        <label>姓名</label>
        <input v-model="form.memName" type="text" placeholder="請輸入您的姓名" />
      </div>

      <div class="form-group">
        <label>帳號</label>
        <input v-model="form.memUsername" type="text" placeholder="請輸入帳號" />
      </div>

      <div class="form-group">
        <label>電子郵件</label>
        <input v-model="form.memEmail" type="text" placeholder="請輸入您的 E-MAIL" />
      </div>

      <div class="form-group">
        <label>手機</label>
        <input v-model="form.memPhone" type="text" placeholder="請輸入您的手機" />
      </div>

      <div class="form-group">
        <label>密碼</label>
        <input v-model="form.memPassword" type="password" placeholder="請輸入您的密碼" />
      </div>

      <div class="form-group">
        <label>再次輸入密碼</label>
        <input v-model="confirmPassword" type="password" placeholder="再輸入一次密碼" />
      </div>

      <button type="submit" class="submit-btn">加入會員</button>
    </form>

    <div class="back-login-area">
      <div class="back-login" @click="goLogin">
        👤 我已經有會員帳號了？ <span>回登入頁面</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 核心白底卡片容器 */
.register-card {
  background-color: #ffffff;
  width: 100%;
  max-width: 650px;
  margin: 0 auto;
  padding: 50px 80px;
  border: 1px solid #e8e8e8;
  border-radius: 4px; /* 輕微圓角增加質感 */
}

/* 標題樣式 */
.page-title {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 35px;
}

.register-form {
  display: flex;
  flex-direction: column;
}

/* 欄位群組 */
.form-group {
  margin-bottom: 22px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: bold;
  color: #444;
  margin-bottom: 8px;
}

/* 輸入框樣式：仿照圖片寬高與色調 */
.form-group input {
  width: 100%;
  height: 45px;
  padding: 0 15px;
  border: 1px solid #dcdcdc;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: all 0.2s ease-in-out;
}

.form-group input:focus {
  border-color: #ff7a66;
  box-shadow: 0 0 4px rgba(255, 122, 102, 0.2);
}

/* 按鈕樣式：珊瑚橘滿版設計 */
.submit-btn {
  width: 100%;
  height: 50px;
  background-color: #ff7a66;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 15px;
  transition: background 0.3s;
}

.submit-btn:hover {
  background-color: #ff6650;
}

/* 底部連結區塊 */
.back-login-area {
  margin-top: 30px;
  text-align: center;
}

.back-login {
  font-size: 14px;
  color: #555;
  cursor: pointer;
  display: inline-block;
}

.back-login span {
  color: #0066cc;
  margin-left: 5px;
}

.back-login:hover span {
  text-decoration: underline;
}

/* 針對行動裝置的調整 */
@media (max-width: 600px) {
  .register-card {
    padding: 30px 20px;
    border: none;
  }
}
</style>