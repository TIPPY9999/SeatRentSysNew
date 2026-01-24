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
    .get('http://localhost:8080/members')
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
    .get('http://localhost:8080/members/search', {
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
    .get('http://localhost:8080/members/delete', {
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
            <button class="btn-edit" @click="router.push(`/admin/members/edit/${m.memId}`)">
              編輯
            </button>
          </td>
          <td>
            <button class="btn-delete" @click="deleteMember(m.memId)">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* ========== 整個頁面 - 淺色背景 ========== */
.member-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f5fa 0%, #e8eef5 100%);
  padding: 20px 1rem;
  padding-bottom: 40px;
}

/* ========== 標題區塊 ========== */
.title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}

/* ========== 工具列 - 淺色卡片 ========== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.toolbar:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

/* ========== 搜尋列 ========== */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-bar input {
  width: 280px;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s ease;
}

.search-bar input:focus {
  border-color: #c0c4cc;
  background: white;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.08);
  outline: none;
}

.search-bar input::placeholder {
  color: #c0c4cc;
}

.search-bar button {
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: #409eff;
  color: #ffffff;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-bar button:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

/* ========== 新增會員按鈕 ========== */
.create-bar {
  display: flex;
  justify-content: flex-end;
}

.btn-create {
  padding: 10px 20px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-create:hover {
  background: #85ce61;
  transform: translateY(-1px);
}

/* ========== 表格 ========== */
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* ========== 表格欄位 ========== */
th,
td {
  border-bottom: 1px solid #ebeef5;
  padding: 14px 12px;
  text-align: center;
  white-space: nowrap;
}

/* ========== 表頭 - 淺灰色 ========== */
th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

/* ========== 表格內容行 ========== */
tbody tr {
  transition: background-color 0.2s ease;
}

tbody tr:hover {
  background-color: #f5f7fa;
}

/* 奇偶行交替色 */
tbody tr:nth-child(even) {
  background-color: #fafbfc;
}

tbody tr:nth-child(even):hover {
  background-color: #f5f7fa;
}

/* ========== 沒資料提示 ========== */
tbody tr td[colspan] {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

/* ========== 錯誤訊息 ========== */
.error {
  color: #f56c6c;
  text-align: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fef0f0;
  border-radius: 8px;
  font-weight: 500;
  border: 1px solid #fde2e2;
}

/* ========== 編輯按鈕 - 圓形藍色 ========== */
.btn-edit {
  width: 32px;
  height: 32px;
  padding: 0;
  cursor: pointer;
  border: none;
  background: #409eff;
  color: #ffffff;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-edit:hover {
  background: #66b1ff;
  transform: scale(1.1);
}

/* ========== 刪除按鈕 - 圓形紅色 ========== */
.btn-delete {
  width: 32px;
  height: 32px;
  padding: 0;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-delete:hover {
  background: #f89898;
  transform: scale(1.1);
}

/* ========== ID 欄位 ========== */
td:first-child {
  color: #606266;
  font-size: 13px;
}
</style>
