<script setup>
/**
 * AdminCreateView.vue：新增管理員
 */
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const admin = reactive({
  admUsername: '',
  admPassword: '',
  admName: '',
  admEmail: '',
  admRole: 1,
})

const submitCreate = () => {
  axios
    .post('http://localhost:8080/admins', {
      ...admin,
    })
    .then((res) => {
      alert(res.data || '新增管理員成功')
      router.push('/admin/admins')
    })
    .catch((err) => {
      alert(err.response?.data || '新增失敗')
    })
}

const goBack = () => {
  router.push('/admin/admins')
}
</script>

<template>
  <div class="container">
    <h2>新增管理員</h2>

    <form @submit.prevent="submitCreate" autocomplete="off">
      <label>帳號</label>
      <input
        type="text"
        v-model="admin.admUsername"
        autocomplete="new-username"
        required
      />

      <label>密碼</label>
      <input
        type="password"
        v-model="admin.admPassword"
        autocomplete="new-password"
        required
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

      <button type="submit">確認新增</button>
    </form>

    <a class="home-btn" @click.prevent="goBack">回管理員列表</a>
  </div>
</template>

<style scoped>
.container {
  width: 380px;
  margin: 40px auto;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.15);
  text-align: center;
}

h2 {
  color: #3366cc;
  margin-bottom: 10px;
}

label {
  display: block;
  text-align: left;
  margin-bottom: 4px;
  font-weight: bold;
  font-size: 14px;
}

input,
select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  height: 32px;
  box-sizing: border-box;
  margin-bottom: 12px;
  font-size: 14px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #4d88ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 5px;
}

button:hover {
  background-color: #3366cc;
}

.home-btn {
  display: block;
  margin-top: 15px;
  text-decoration: none;
  color: #3366cc;
  font-size: 15px;
  cursor: pointer;
}

.home-btn:hover {
  text-decoration: underline;
}
</style>