<script setup>
/**
 * MemberListView.vue：會員列表
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const members = ref([])
const errorMsg = ref('')

const fetchMembers = () => {
  errorMsg.value = ''
  axios
    .get('http://localhost:8080/members')
    .then((res) => {
      members.value = res.data
    })
    .catch(() => {
      errorMsg.value = '取得會員資料失敗'
    })
}

const deleteMember = (memId) => {
  if (!confirm('確定要刪除這個會員嗎？')) return
  axios
    .get('http://localhost:8080/members/delete', {
      params: { memId },
    })
    .then(() => {
      alert('會員刪除成功')
      fetchMembers()
    })
    .catch((err) => {
      console.error(err)
      alert('刪除失敗')
    })
}

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ')
}

onMounted(() => {
  fetchMembers()
})
</script>

<template>
  <div class="member-page">
    <h2 class="title">會員列表</h2>
    <div class="search-bar">
      <input type="text" placeholder="搜尋帳號 / 姓名 / Email / 電話" />
      <button>搜尋</button>
      <button @click="fetchMembers">顯示全部</button>
    </div>
    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>帳號</th>
          <th>姓名</th>
          <th>Email</th>
          <th>手機</th>
          <th>狀態</th>
          <th>等級</th>
          <th>總積分</th>
          <th>違規次數</th>
          <th>發票載具</th>
          <th>建立時間</th>
          <th>更新時間</th>
          <th>修改</th>
          <th>刪除</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="members.length === 0">
          <td colspan="14">目前沒有會員資料</td>
        </tr>
        <tr v-for="m in members" :key="m.memId">
          <td>{{ m.memId }}</td>
          <td>{{ m.memUsername }}</td>
          <td>{{ m.memName }}</td>
          <td>{{ m.memEmail }}</td>
          <td>{{ m.memPhone }}</td>
          <td>{{ m.memStatus }}</td>
          <td>{{ m.memLevel }}</td>
          <td>{{ m.memPoints }}</td>
          <td>{{ m.memViolation }}</td>
          <td>{{ m.memInvoice || '未提供' }}</td>
          <td>{{ formatDateTime(m.createdAt) }}</td>
          <td>{{ formatDateTime(m.updatedAt) }}</td>
          <td>
            <button class="btn-edit" @click="router.push(`/admin/members/edit/${m.memId}`)">
              修改
            </button>
          </td>
          <td><button class="btn-delete" @click="deleteMember(m.memId)">刪除</button></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.member-page {
  padding: 20px;
  background-color: #fdf5e6;
}
.title {
  text-align: center;
  margin-bottom: 20px;
}
.search-bar {
  text-align: center;
  margin-bottom: 15px;
}
.search-bar input {
  width: 260px;
  padding: 6px;
  margin-right: 6px;
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
th,
td {
  border: 1px solid #999;
  padding: 6px;
  text-align: center;
}
th {
  background-color: #a8fefa;
}
.error {
  color: red;
  text-align: center;
  margin-bottom: 10px;
}
.btn-delete {
  padding: 4px 8px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-edit {
  cursor: pointer;
}
</style>
