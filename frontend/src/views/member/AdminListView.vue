<script setup>
/**
 * AdminListView.vue：管理員列表
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const admins = ref([])
const errorMsg = ref('')
const keyword = ref('')

const fetchAdmins = () => {
  errorMsg.value = ''
  axios
    .get('http://localhost:8080/admins')
    .then((res) => {
      admins.value = res.data
    })
    .catch(() => {
      errorMsg.value = '取得管理員資料失敗'
    })
}

const searchAdmins = () => {
  errorMsg.value = ''
  axios
    .get('http://localhost:8080/admins/search', {
      params: { keyword: keyword.value },
    })
    .then((res) => {
      admins.value = res.data
    })
    .catch(() => {
      errorMsg.value = '搜尋失敗'
    })
}

const deleteAdmin = (admId) => {
  if (!confirm('確定要刪除這個管理員嗎？')) return
  axios
    .get('http://localhost:8080/admins/delete', {
      params: { admId },
    })
    .then(() => {
      alert('管理員刪除成功')
      fetchAdmins()
    })
    .catch(() => {
      alert('刪除失敗')
    })
}

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ')
}

onMounted(() => {
  fetchAdmins()
})
</script>

<template>
  <div class="member-page">
    <h2 class="title">管理員列表</h2>

    <!-- 工具列：搜尋 + 新增 -->
    <div class="toolbar">
      <!-- 左：搜尋 -->
      <div class="search-bar">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜尋帳號 / 姓名 / Email"
          @keyup.enter="searchAdmins"
        />
        <button @click="searchAdmins">搜尋</button>
        <button @click="fetchAdmins">顯示全部</button>
      </div>

      <!-- 右：新增管理員 -->
      <div class="create-bar">
        <button
          class="btn-create"
          @click="router.push('/admin/admins/create')"
        >
          ＋ 新增管理員
        </button>
      </div>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>帳號</th>
          <th>姓名</th>
          <th>Email</th>
          <th>權限</th>
          <th>建立時間</th>
          <th>更新時間</th>
          <th>修改</th>
          <th>刪除</th>
        </tr>
      </thead>

      <tbody>
        <tr v-if="admins.length === 0">
          <td colspan="9">目前沒有管理員資料</td>
        </tr>

        <tr v-for="a in admins" :key="a.admId">
          <td>{{ a.admId }}</td>
          <td>{{ a.admUsername }}</td>
          <td>{{ a.admName }}</td>
          <td>{{ a.admEmail }}</td>
          <td>{{ a.admRole }}</td>
          <td>{{ formatDateTime(a.createdAt) }}</td>
          <td>{{ formatDateTime(a.updatedAt) }}</td>
          <td>
            <button
              class="btn-edit"
              @click="router.push(`/admin/admins/edit/${a.admId}`)"
            >
              修改
            </button>
          </td>
          <td>
            <button class="btn-delete" @click="deleteAdmin(a.admId)">
              刪除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
button {
  cursor: pointer;
  transition: all 0.2s ease;
}

.member-page {
  padding: 20px;
}

.title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 22px;
  font-weight: bold;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-bar input {
  width: 260px;
  padding: 6px 8px;
  font-size: 14px;
  border: 1px solid #999;
  border-radius: 4px;
}

.search-bar button {
  padding: 6px 12px;
  margin-left: 6px;
  border: 1px solid #333;
  background-color: #f3f4f6;
  color: #333 !important;
  border-radius: 4px;
}

.search-bar button:hover {
  background-color: #e5e7eb;
  color: #111 !important;
}

.create-bar {
  display: flex;
  justify-content: flex-end;
}

.btn-create {
  padding: 8px 18px;
  background-color: #28a745;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
}

.btn-create:hover {
  background-color: #218838;
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
  white-space: nowrap;
}

th {
  background-color: #a8fefa;
  font-weight: bold;
}

tbody tr td[colspan] {
  text-align: center;
  padding: 12px;
}

.error {
  color: red;
  text-align: center;
  margin-bottom: 10px;
}

.btn-edit {
  padding: 4px 10px;
  background-color: #f3f4f6;
  color: #333 !important;
  border: 1px solid #333;
  border-radius: 4px;
}

.btn-edit:hover {
  background-color: #e5e7eb;
  color: #111 !important;
}

.btn-delete {
  padding: 4px 8px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete:hover {
  background-color: #d9363e;
}
</style>