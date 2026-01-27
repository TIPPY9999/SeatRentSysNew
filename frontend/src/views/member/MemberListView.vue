<script setup>
/**
 * MemberListView.vue：會員列表（完整模糊查詢 + SweetAlert2）
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const members = ref([])
const keyword = ref('')
const loading = ref(false)

/**
 * 取得所有會員
 */
const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/members')
    members.value = res.data

    // 如果沒有資料，顯示提示
    if (res.data.length === 0) {
      Swal.fire({
        icon: 'info',
        title: '查無會員資料',
        text: '目前系統中沒有任何會員',
        confirmButtonText: '確定',
        confirmButtonColor: '#409eff'
      })
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: '載入失敗',
      text: error.response?.data?.message || '取得會員資料失敗，請稍後再試',
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 模糊搜尋會員（搜尋欄位：帳號、姓名、Email、手機）
 */
const searchMembers = async () => {
  // 如果關鍵字為空，顯示全部資料
  if (!keyword.value.trim()) {
    fetchMembers()
    return
  }

  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/members/search', {
      params: { keyword: keyword.value.trim() }
    })

    members.value = res.data

    // 顯示搜尋結果提示
    if (res.data.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '查無結果',
        text: `找不到包含「${keyword.value.trim()}」的會員資料`,
        confirmButtonText: '確定',
        confirmButtonColor: '#e6a23c'
      })
    } else {
      Swal.fire({
        icon: 'success',
        title: '搜尋成功',
        text: `找到 ${res.data.length} 筆符合的會員資料`,
        timer: 1500,
        showConfirmButton: false
      })
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: '搜尋失敗',
      text: error.response?.data?.message || '搜尋過程發生錯誤，請稍後再試',
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 刪除會員（使用 SweetAlert2 確認對話框）
 */
const deleteMember = async (memId, memName) => {
  // 使用 SweetAlert2 確認對話框
  const result = await Swal.fire({
    title: '確定要刪除這個會員嗎？',
    html: `會員：<strong>${memName}</strong> (ID: ${memId})<br>此操作無法復原！`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    cancelButtonColor: '#909399',
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消'
  })

  if (!result.isConfirmed) return

  // 顯示載入中
  Swal.fire({
    title: '刪除中...',
    text: '請稍候',
    allowOutsideClick: false,
    didOpen: () => {
      Swal.showLoading()
    }
  })

  try {
    await axios.get('http://localhost:8080/api/members/delete', {
      params: { memId }
    })

    // 成功提示
    Swal.fire({
      icon: 'success',
      title: '刪除成功',
      text: `會員「${memName}」已成功刪除`,
      timer: 2000,
      showConfirmButton: false
    })

    // 重新載入會員列表
    fetchMembers()
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: '刪除失敗',
      text: error.response?.data?.message || '刪除過程發生錯誤，請稍後再試',
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c'
    })
  }
}

/**
 * 格式化日期時間
 */
const formatDateTime = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 19)
}

/**
 * 清空搜尋條件
 */
const clearSearch = () => {
  keyword.value = ''
  fetchMembers()
}

onMounted(() => {
  fetchMembers()
})
</script>

<template>
  <div class="member-page">
    <h2 class="title">
      <i class="fas fa-users"></i> 會員列表
    </h2>

    <div class="toolbar">
      <!-- 左：搜尋區 -->
      <div class="search-bar">
        <div class="search-input-wrapper">
          <i class="fas fa-search search-icon"></i>
          <input
            v-model="keyword"
            type="text"
            placeholder="模糊搜尋：帳號 / 姓名 / Email / 手機"
            @keyup.enter="searchMembers"
            :disabled="loading"
          />
          <button 
            v-if="keyword" 
            class="clear-btn" 
            @click="clearSearch"
            title="清除搜尋"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        <button 
          class="btn-search" 
          @click="searchMembers"
          :disabled="loading"
        >
          <i class="fas fa-search"></i> 搜尋
        </button>
        <button 
          class="btn-refresh" 
          @click="fetchMembers"
          :disabled="loading"
        >
          <i class="fas fa-sync-alt"></i> 顯示全部
        </button>
      </div>

      <!-- 右：新增會員 -->
      <div class="create-bar">
        <button class="btn-create" @click="router.push('/admin/members/create')">
          <i class="fas fa-user-plus"></i> 新增會員
        </button>
      </div>
    </div>

    <!-- 載入中提示 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>載入中...</p>
    </div>

    <!-- 會員列表表格 -->
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
        <tr v-if="members.length === 0 && !loading">
          <td colspan="14" class="no-data">
            <i class="fas fa-inbox"></i>
            <p>目前沒有會員資料</p>
          </td>
        </tr>
        <tr v-for="m in members" :key="m.memId">
          <td>{{ m.memId }}</td>
          <td>
            <span class="username">{{ m.memUsername }}</span>
          </td>
          <td>
            <span class="member-name">{{ m.memName }}</span>
          </td>
          <td>{{ m.memEmail }}</td>
          <td>{{ m.memPhone }}</td>
          <td>
            <span 
              class="status-badge" 
              :class="m.memStatus === 1 ? 'status-active' : 'status-inactive'"
            >
              {{ m.memStatus === 1 ? '啟用' : '停用' }}
            </span>
          </td>
          <td>{{ m.memLevel }}</td>
          <td class="points">{{ m.memPoints }}</td>
          <td class="violation">{{ m.memViolation }}</td>
          <td>{{ m.memInvoice || '未提供' }}</td>
          <td>{{ formatDateTime(m.createdAt) }}</td>
          <td>{{ formatDateTime(m.updatedAt) }}</td>
          <td>
            <button 
              class="btn-edit" 
              @click="router.push(`/admin/members/edit/${m.memId}`)"
              title="編輯會員"
            >
              <i class="fas fa-edit"></i>
            </button>
          </td>
          <td>
            <button 
              class="btn-delete" 
              @click="deleteMember(m.memId, m.memName)"
              title="刪除會員"
            >
              <i class="fas fa-trash-alt"></i>
            </button>
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
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}

.title i {
  color: #409eff;
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
  flex: 1;
}

.search-input-wrapper {
  position: relative;
  width: 350px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  font-size: 14px;
  pointer-events: none;
}

.search-bar input {
  width: 100%;
  padding: 10px 40px 10px 40px;
  font-size: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s ease;
}

.search-bar input:focus {
  border-color: #409eff;
  background: white;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  outline: none;
}

.search-bar input::placeholder {
  color: #c0c4cc;
}

.search-bar input:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.clear-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: #909399;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  color: #f56c6c;
  background: #fef0f0;
}

.btn-search,
.btn-refresh {
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  color: #ffffff;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.btn-search {
  background: #409eff;
}

.btn-search:hover:not(:disabled) {
  background: #66b1ff;
  transform: translateY(-1px);
}

.btn-refresh {
  background: #67c23a;
}

.btn-refresh:hover:not(:disabled) {
  background: #85ce61;
  transform: translateY(-1px);
}

.btn-search:disabled,
.btn-refresh:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.btn-create:hover {
  background: #85ce61;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* ========== 載入中樣式 ========== */
.loading-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay p {
  margin-top: 12px;
  color: #606266;
  font-size: 14px;
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
.no-data {
  text-align: center;
  padding: 60px 40px !important;
  color: #909399;
  font-size: 14px;
}

.no-data i {
  font-size: 48px;
  color: #dcdfe6;
  margin-bottom: 12px;
  display: block;
}

.no-data p {
  margin: 0;
  font-size: 14px;
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
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-edit:hover {
  background: #66b1ff;
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
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
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-delete:hover {
  background: #f89898;
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

/* ========== 特殊欄位樣式 ========== */
td:first-child {
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.username {
  font-weight: 500;
  color: #409eff;
}

.member-name {
  font-weight: 500;
  color: #303133;
}

.points {
  color: #67c23a;
  font-weight: 600;
}

.violation {
  color: #f56c6c;
  font-weight: 600;
}

/* ========== 狀態標籤 ========== */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #f0f9ff;
  color: #409eff;
  border: 1px solid #b3d8ff;
}

.status-inactive {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}
</style>
