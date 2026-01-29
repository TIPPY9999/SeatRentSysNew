<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const members = ref([])
const keyword = ref('')
const loading = ref(false)

// --- 分頁控制 ---
const currentPage = ref(1)
const pageSize = 6 

// --- 彈窗控制 ---
const showModal = ref(false)
const selectedMember = ref(null)

// 取得會員 (保留原 SweetAlert 邏輯)
const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/members')
    members.value = res.data
    currentPage.value = 1 
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
      text: error.response?.data?.message || '取得會員資料失敗',
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c'
    })
  } finally {
    loading.value = false
  }
}

const paginatedMembers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return members.value.slice(start, start + pageSize)
})

const totalPages = computed(() => {
  return Math.ceil(members.value.length / pageSize) || 1
})

const openMemberDetail = (member) => {
  selectedMember.value = member
  showModal.value = true
}

// 模糊搜尋 (完全保留原邏輯與樣式)
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
    currentPage.value = 1 // 搜尋後回到第一頁

    // --- 完整還原：顯示搜尋結果提示 ---
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
    // --- 完整還原：搜尋失敗提示 ---
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

// 停權邏輯 (保留原刪除 API 與提示)
const deleteMember = async (memId, memName) => {
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
  try {
    await axios.get('http://localhost:8080/api/members/delete', { params: { memId } })
    Swal.fire({ icon: 'success', title: '刪除成功', timer: 2000, showConfirmButton: false })
    fetchMembers()
  } catch (error) {
    Swal.fire({ icon: 'error', title: '刪除失敗' })
  }
}

const formatDateOnly = (dt) => {
  if (!dt) return ''
  return dt.substring(0, 10).replace(/-/g, '/')
}

const clearSearch = () => {
  keyword.value = ''
  fetchMembers()
}

onMounted(fetchMembers)
</script>

<template>
  <div class="member-page">
    <h2 class="title"><i class="fas fa-users"></i> 會員列表</h2>

    <div class="toolbar">
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
          <button v-if="keyword" class="clear-btn" @click="clearSearch">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <button class="btn-search" @click="searchMembers" :disabled="loading">
          <i class="fas fa-search"></i> 搜尋
        </button>
        <button class="btn-refresh" @click="fetchMembers" :disabled="loading">
          <i class="fas fa-sync-alt"></i> 顯示全部
        </button>
      </div>
      <div class="create-bar">
        <button class="btn-create" @click="router.push('/admin/members/create')">
          <i class="fas fa-user-plus"></i> 新增會員
        </button>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-info">會員資訊</th>
            <th class="col-points">點數</th>
            <th class="col-date">註冊日期</th>
            <th class="col-status">狀態</th>
            <th class="col-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in paginatedMembers" :key="m.memId">
            <td class="col-id id-link-cell" @click="openMemberDetail(m)">{{ m.memId }}</td>
            <td class="col-info">
              <div class="info-cell">
                <div class="list-avatar-wrap">
                  <img :src="m.memImage ? `/members/${m.memImage}` : '/members/default.png'" />
                </div>
                <div class="user-text">
                  <span class="user-name">{{ m.memName }}</span>
                  <span class="user-phone">{{ m.memPhone }}</span>
                </div>
              </div>
            </td>
            <td class="col-points"><span class="points-val">{{ m.memPoints }}</span></td>
            <td class="col-date">{{ formatDateOnly(m.createdAt) }}</td>
            <td class="col-status">
              <span class="status-badge" :class="m.memStatus === 1 ? 'status-active' : 'status-inactive'">
                {{ m.memStatus === 1 ? '啟用' : '停用' }}
              </span>
            </td>
            <td class="col-action">
              <div class="action-btns">
                <button class="btn-box-edit" @click="router.push(`/admin/members/edit/${m.memId}`)">修改</button>
                <button class="btn-box-del" @click="deleteMember(m.memId, m.memName)">停權</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-bar" v-if="members.length > pageSize">
      <button class="btn-page" :disabled="currentPage === 1" @click="currentPage--">上一頁</button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 頁</span>
      <button class="btn-page" :disabled="currentPage === totalPages" @click="currentPage++">下一頁</button>
    </div>

    <div v-if="showModal && selectedMember" class="modal-overlay" @click.self="showModal = false">
      <div class="compact-card">
        <div class="card-header">
          <h3><i class="fas fa-id-card"></i> 會員詳細資訊</h3>
          <button class="close-x" @click="showModal = false">&times;</button>
        </div>
        <div class="card-body">
          <div class="compact-sidebar">
            <div class="mini-avatar">
              <img :src="selectedMember.memImage ? `/members/${selectedMember.memImage}` : '/members/default.png'" />
            </div>
            <h4 class="sidebar-name">{{ selectedMember.memName }}</h4>
            <span class="sidebar-user">@{{ selectedMember.memUsername }}</span>
          </div>
          <div class="compact-main">
            <div class="data-section">
              <h5 class="section-title">個人資訊</h5>
              <div class="mini-grid">
                <div class="grid-item">
                  <label>會員 ID</label>
                  <span>{{ selectedMember.memId }}</span>
                </div>
                <div class="grid-item">
                  <label>聯絡電話</label>
                  <span>{{ selectedMember.memPhone || '未提供' }}</span>
                </div>
                <div class="grid-item">
                  <label>電子信箱</label>
                  <span>{{ selectedMember.memEmail }}</span>
                </div>
                <div class="grid-item">
                  <label>發票載具</label>
                  <span class="text-blue">{{ selectedMember.memInvoice || '未提供' }}</span>
                </div>
                <div class="grid-item">
                  <label>目前點數</label>
                  <span class="text-green">{{ selectedMember.memPoints }}</span>
                </div>
              </div>
            </div>
            <div class="data-section">
              <h5 class="section-title">系統資訊</h5>
              <div class="mini-grid">
                <div class="grid-item">
                  <label>註冊日期</label>
                  <span>{{ formatDateOnly(selectedMember.createdAt) }}</span>
                </div>
                <div class="grid-item">
                  <label>帳號狀態</label>
                  <span :class="selectedMember.memStatus === 1 ? 'status-text-active' : 'status-text-inactive'">
                    {{ selectedMember.memStatus === 1 ? '啟用' : '停用' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="card-footer"><button class="btn-close-modal" @click="showModal = false">關閉視窗</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.member-page {
  min-height: 100vh;
  background-color: #f0f5fa;
  padding: 20px;
}

.title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
}

/* 工具列與搜尋框樣式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  gap: 10px;
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
}

.search-input-wrapper input {
  width: 100%;
  padding: 10px 40px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f5f7fa;
  transition: border-color 0.3s;
}

.search-input-wrapper input:focus {
  border-color: #409eff;
  outline: none;
}

.clear-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #909399;
  cursor: pointer;
}

/* 按鈕懸浮效果強化 */
.btn-search {
  padding: 10px 18px;
  background: #409eff;
  color: white;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-search:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
}

.btn-refresh {
  padding: 10px 18px;
  background: #67c23a;
  color: white;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-refresh:hover {
  background: #85ce61;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(103, 194, 58, 0.3);
}

.btn-create {
  padding: 10px 20px;
  background: #67c23a;
  color: white;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-create:hover {
  background: #85ce61;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(103, 194, 58, 0.3);
}

/* 表格對齊與樣式 */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th, td {
  padding: 16px 12px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

/* 固定寬度 */
.col-id {
  width: 70px;
}

.col-info {
  width: 280px;
  text-align: left !important;
  padding-left: 20px;
}

.col-points {
  width: 100px;
}

.col-date {
  width: 150px;
}

.col-status {
  width: 100px;
}

.col-action {
  width: 140px;
}

/* 會員資訊內容樣式 */
.info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-avatar-wrap img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover; /* 關鍵：確保照片不會被壓扁 */
  border: 1px solid #eee;
}

.user-text {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #303133;
}

.user-phone {
  font-size: 12px;
  color: #909399;
}

/* 其他小組件樣式 */
.id-link-cell {
  color: #409eff;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}

.id-link-cell:hover {
  color: #66b1ff;
}

.points-val {
  color: #f59e0b;
  font-weight: bold;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
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

/* 操作按鈕修改：藍色/紅色背景方塊 */
.action-btns {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.btn-box-edit {
  background-color: #409eff;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-box-edit:hover {
  background-color: #66b1ff;
  transform: scale(1.05);
}

.btn-box-del {
  background-color: #f56c6c;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-box-del:hover {
  background-color: #f78989;
  transform: scale(1.05);
}

/* 分頁條 */
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 25px;
}

.btn-page {
  padding: 8px 16px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-page:not(:disabled):hover {
  border-color: #409eff;
  color: #409eff;
}

/* 彈窗樣式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.compact-card {
  background: white;
  width: 600px;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

.card-body {
  display: flex;
}

.compact-sidebar {
  width: 180px;
  background: #f9f9f9;
  padding: 25px 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid #eee;
}

.mini-avatar img {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover; /* 關鍵：確保照片不會被壓扁 */
  border: 2px solid #409eff; /* 加個藍色邊框更有質感 */
}

.compact-main {
  flex: 1;
  padding: 20px;
}

.data-section {
  margin-bottom: 20px;
}

.section-title {
  border-left: 3px solid #409eff;
  padding-left: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
}

.mini-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.grid-item {
  display: flex;
  flex-direction: column;
}

.grid-item label {
  font-size: 11px;
  color: #bbb;
}

.grid-item span {
  font-size: 14px;
  font-weight: 500;
}

.text-green {
  color: #67c23a;
  font-weight: bold;
}

.text-blue {
  color: #409eff;
  font-weight: bold;
}

.status-text-active {
  color: #409eff;
  font-weight: bold;
}

.status-text-inactive {
  color: #f56c6c;
  font-weight: bold;
}

.card-footer {
  padding: 15px 20px;
  text-align: right;
  border-top: 1px solid #eee;
}

.btn-close-modal {
  padding: 8px 20px;
  background: #909399;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-close-modal:hover {
  background: #a6a9ad;
}

.close-x {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  color: #ccc;
}

.close-x:hover {
  color: #f56c6c;
}
</style>