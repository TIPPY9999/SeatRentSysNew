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
.container {
  width: 380px;
  margin: 30px auto;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #3366cc;
}

label {
  font-weight: bold;
  margin-top: 10px;
  display: block;
}

input {
  width: 100%;
  padding: 6px;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.error {
  color: red;
  text-align: center;
  margin-bottom: 10px;
}

.primary-btn {
  width: 100%;
  padding: 10px;
  background-color: #4d88ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

.primary-btn:hover {
  background-color: #3366cc;
}

/* 回列表文字連結 */
.back-link {
  display: block;
  margin-top: 14px;
  text-align: center;
  color: #3366cc;
  font-size: 15px;
  cursor: pointer;
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
