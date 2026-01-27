<script setup>
/**
 * MemberCreateView.vue：新增會員
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

const member = reactive({
  memUsername: '',
  memPassword: '',
  memName: '',
  memEmail: '',
  memPhone: '',
  memInvoice: '',
  memStatus: 1,
  memLevel: 1,
})

const errorMsg = ref('')
const successMsg = ref('')

const submitCreate = async () => {
  try {
    //  endpoint 統一 /api/members
    const res = await axios.post('http://localhost:8080/api/members', {
      //  正確展開 member
      ...member,
      memStatus: 1,
      memLevel: 1,
    })

    await Swal.fire({
      icon: 'success',
      title: '新增成功',
      text: res.data?.message || '會員已新增',
      confirmButtonText: '確定',
      confirmButtonColor: '#409eff',
    })

    router.push('/admin/members')
  } catch (err) {
    const msg = err.response?.data?.message || '新增失敗'
    await Swal.fire({
      icon: 'error',
      title: '新增失敗',
      text: msg,
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c',
    })
  }
}

const goBack = () => {
  router.push('/admin/members')
}
</script>

<template>
  <div class="container">
    <h2>新增會員</h2>
    <p v-if="errorMsg" style="color: red; font-weight: bold; text-align: center">{{ errorMsg }}</p>
    <p v-if="successMsg" class="msg">{{ successMsg }}</p>

    <form @submit.prevent="submitCreate" autocomplete="off">
      <label>帳號</label>
      <input type="text" v-model="member.memUsername" autocomplete="new-username" required />
      <label>密碼</label>
      <input type="password" v-model="member.memPassword" autocomplete="new-password" required />
      <label>姓名</label>
      <input type="text" v-model="member.memName" required />
      <label>信箱</label>
      <input type="text" v-model="member.memEmail" required />
      <label>手機</label>
      <input type="text" v-model="member.memPhone" required />
      <label>發票載具</label>
      <input type="text" v-model="member.memInvoice" />
      <button type="submit">確認新增</button>
    </form>
    <a class="home-btn" @click.prevent="goBack">回會員列表</a>
  </div>
</template>

<style scoped>
/* ========== 主容器：Glassmorphism 風格 ========== */
.container {
  width: 420px;
  margin: 40px auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 32px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
}

/* ========== 標題 ========== */
h2 {
  color: #1e3a5f;
  margin-bottom: 24px;
  font-size: 1.5rem;
  font-weight: 700;
  position: relative;
  padding-bottom: 12px;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 2px;
}

/* ========== 訊息提示 ========== */
.msg {
  color: #059669;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
  padding: 10px;
  background: rgba(5, 150, 105, 0.1);
  border-radius: 8px;
}

/* ========== 表單元素 ========== */
label {
  display: block;
  text-align: left;
  margin-bottom: 6px;
  font-weight: 600;
  font-size: 14px;
  color: #334155;
}

input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  height: 42px;
  box-sizing: border-box;
  margin-bottom: 16px;
  font-size: 14px;
  background: #f8fafc;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
  outline: none;
  background: #ffffff;
}

input::placeholder {
  color: #94a3b8;
}

/* ========== 提交按鈕 ========== */
button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition:
    box-shadow 0.2s ease,
    transform 0.15s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

button:hover {
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

button:active {
  transform: translateY(1px);
}

/* ========== 返回連結 ========== */
.home-btn {
  display: inline-block;
  margin-top: 20px;
  text-decoration: none;
  color: #3b82f6;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
}

.home-btn:hover {
  color: #1d4ed8;
  text-decoration: underline;
}
</style>
