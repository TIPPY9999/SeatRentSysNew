<script setup>
/**
 * AdminEditView.vue：編輯管理員
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const admin = ref(null)
const newPassword = ref('')
const errorMsg = ref('')

const fetchAdmin = () => {
  const id = route.params.id
  axios
    .get(`http://localhost:8080/admins/find?admId=${id}`)
    .then((res) => {
      admin.value = res.data
    })
    .catch(() => {
      errorMsg.value = '載入管理員資料失敗'
    })
}

const submitEdit = () => {
  const payload = {
    ...admin.value,
    admPassword: newPassword.value || '',
  }

  axios
    .post('http://localhost:8080/admins/update', payload)
    .then(() => {
      alert('管理員修改成功')
      router.push('/admin/admins')
    })
    .catch((err) => {
      console.error(err)
      errorMsg.value = '修改失敗'
    })
}

const goBack = () => {
  router.push('/admin/admins')
}

onMounted(() => {
  fetchAdmin()
})
</script>

<template>
  <div class="container">
    <h2>修改管理員資料</h2>

    <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
    <div v-if="!admin">資料載入中...</div>

    <form v-else @submit.prevent="submitEdit">
      <input type="hidden" v-model="admin.admId" />

      <label>帳號</label>
      <input type="text" v-model="admin.admUsername" required />

      <label>密碼</label>
      <input
        type="password"
        v-model="newPassword"
        placeholder="不修改請留空"
      />

      <label>姓名</label>
      <input type="text" v-model="admin.admName" required />

      <label>信箱</label>
      <input type="email" v-model="admin.admEmail" required />

      <label>角色</label>
      <select v-model="admin.admRole">
        <option :value="1">一般管理員</option>
        <option :value="9">超級管理員</option>
      </select>

      <button type="submit" class="primary-btn">確認修改</button>
      <a class="back-link" @click.prevent="goBack">回管理員列表</a>
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

input,
select {
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