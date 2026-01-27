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
const keyword = ref('')

const fetchMembers = () => {
  errorMsg.value = ''
  axios
    .get('http://localhost:8080/api/members')
    .then((res) => {
      members.value = res.data
    })
    .catch(() => {
      errorMsg.value = '取得會員資料失敗'
    })
}

const searchMembers = () => {
  if (!keyword.value.trim()) {
    fetchMembers()
    return
  }

  axios
    .get('http://localhost:8080/api/members/search', {
      params: { keyword: keyword.value },
    })
    .then((res) => {
      members.value = res.data
    })
    .catch(() => {
      errorMsg.value = '搜尋失敗'
    })
}

const deleteMember = (memId) => {
  if (!confirm('確定要刪除這個會員嗎？')) return
  axios
    .get('http://localhost:8080/api/members/delete', {
      params: { memId },
    })
    .then(() => {
      alert('會員刪除成功')
      fetchMembers()
    })
    .catch(() => {
      alert('刪除失敗')
    })
}

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  fetchMembers()
})
</script>

<template>
  <div class="member-page">
    <h2 class="title">會員列表</h2>

    <div class="toolbar">
  <!-- 左：搜尋 -->
  <div class="search-bar">
    <input
      v-model="keyword"
      type="text"
      placeholder="搜尋帳號 / 姓名 / Email / 電話"
      @keyup.enter="searchMembers"
    />
    <button @click="searchMembers">搜尋</button>
    <button @click="fetchMembers">顯示全部</button>
  </div>

  <!-- 右：新增會員 -->
  <div class="create-bar">
    <button class="btn-create" @click="router.push('/admin/members/create')">
      ＋ 新增會員
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
          <th>手機</th>
          <th>狀態</th>
          <th>等級</th>
          <th>總積分</th>
          <th>違規次數</th>
          <th>發票載具</th>
          <th>建立時間</th>
          <th>更新時間</th>
          <th>編輯</th>
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
            <button
              class="btn-edit"
              @click="router.push(`/admin/members/edit/${m.memId}`)"
            >
              編輯
            </button>
          </td>
          <td>
            <button class="btn-delete" @click="deleteMember(m.memId)">
              刪除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* 整個頁面 */
.member-page {
  padding: 20px;
}

/* 標題 */
.title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 22px;
  font-weight: bold;
}

/* 上方工具列：搜尋（左）＋新增（右） */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 搜尋列 */
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
  font-size: 14px;
  cursor: pointer;
  border: 1px solid #333;
  background-color: #f3f4f6;
  color: #333 !important;
}

.search-bar button:hover {
  background-color: #e5e7eb;
  color: #111 !important;
}

/* 新增會員區 */
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

/* 表格 */
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

/* 表格欄位 */
th,
td {
  border: 1px solid #999;
  padding: 6px;
  text-align: center;
  white-space: nowrap;
}

/* 表頭 */
th {
  background-color: #a8fefa;
  font-weight: bold;
}

/* 沒資料提示 */
tbody tr td[colspan] {
  text-align: center;
  padding: 12px;
}

/* 錯誤訊息 */
.error {
  color: red;
  text-align: center;
  margin-bottom: 10px;
}

/* 修改按鈕 */
.btn-edit {
  padding: 4px 8px;
  cursor: pointer;
  border: 1px solid #333;
  background-color: #f3f4f6;
  color: #333 !important;
}

.btn-edit:hover {
  background-color: #e5e7eb;
  color: #111 !important;
}

/* 刪除按鈕 */
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