<script setup>
/**
 * MemberEditView.vue：編輯會員
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const member = ref(null)
const newPassword = ref('')
const errorMsg = ref('')

const fetchMember = () => {
  const id = route.params.id
  axios
    .get(`http://localhost:8080/members/find?memId=${id}`)
    .then((res) => {
      member.value = res.data
    })
    .catch(() => {
      errorMsg.value = '載入會員資料失敗'
    })
}

const submitEdit = () => {
  const payload = {
    ...member.value,
    memPassword: newPassword.value || '',
  }

  axios
    .post('http://localhost:8080/members/update', payload)
    .then(() => {
      alert('會員修改成功')
      router.push('/admin/members')
    })
    .catch((err) => {
      console.error(err)
      errorMsg.value = '修改失敗'
    })
}

const goBack = () => {
  router.push('/admin/members')
}

onMounted(() => {
  fetchMember()
})
</script>

<template>
  <div class="container">
    <h2>修改會員資料</h2>
    <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
    <div v-if="!member">資料載入中...</div>
    <form v-else @submit.prevent="submitEdit">
      <input type="hidden" v-model="member.memId" />
      <label>帳號</label>
      <input type="text" v-model="member.memUsername" required />
      <label>密碼</label>
      <input type="password" v-model="newPassword" placeholder="不修改請留空" />
      <label>姓名</label>
      <input type="text" v-model="member.memName" required />
      <label>信箱</label>
      <input type="text" v-model="member.memEmail" required />
      <label>電話</label>
      <input type="text" v-model="member.memPhone" required />
      <label>狀態 (1正常 / 0停權)</label>
      <input type="number" v-model="member.memStatus" />
      <label>總積分</label>
      <input type="number" v-model="member.memPoints" />
      <label>違規次數</label>
      <input type="number" v-model="member.memViolation" />
      <label>會員等級</label>
      <input type="number" v-model="member.memLevel" />
      <label>發票載具</label>
      <input type="text" v-model="member.memInvoice" placeholder="未提供" />
      <button type="submit" class="primary-btn">確認修改</button>
      <a class="back-link" @click.prevent="goBack">回會員列表</a>
    </form>
  </div>
</template>

<style scoped>
/* ========== 主容器：Glassmorphism 風格 ========== */
.container {
  width: 420px;
  margin: 30px auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* ========== 標題 ========== */
h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #1e3a5f;
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
  width: 80px;
  height: 3px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 2px;
}

/* ========== 表單元素 ========== */
label {
  font-weight: 600;
  margin-top: 12px;
  display: block;
  color: #334155;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px 14px;
  margin-bottom: 8px;
  margin-top: 6px;
  box-sizing: border-box;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  background: #f8fafc;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
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

/* ========== 錯誤訊息 ========== */
.error {
  color: #dc2626;
  text-align: center;
  margin-bottom: 16px;
  padding: 10px;
  background: rgba(220, 38, 38, 0.1);
  border-radius: 8px;
  font-weight: 500;
}

/* ========== 主要按鈕 ========== */
.primary-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 16px;
  transition: box-shadow 0.2s ease, transform 0.15s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.primary-btn:hover {
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.primary-btn:active {
  transform: translateY(1px);
}

/* ========== 返回連結 ========== */
.back-link {
  display: block;
  margin-top: 20px;
  text-align: center;
  color: #3b82f6;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}
</style>
