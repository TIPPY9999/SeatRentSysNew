<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const admins = ref([])
const loading = ref(false)
const keyword = ref('')

// 分頁控制：一頁 5 筆
const currentPage = ref(1)
const pageSize = 5 

// 統計數據邏輯
const totalAdmins = computed(() => admins.value.length)
const superAdmins = computed(() => admins.value.filter(a => a.admRole === 9).length)
const normalAdmins = computed(() => admins.value.filter(a => a.admRole === 1).length)

// 取得所有資料
const fetchAdmins = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/admins')
    admins.value = res.data
    currentPage.value = 1 
  } catch (error) {
    Swal.fire({ 
      icon: 'error', 
      title: '載入失敗', 
      text: '取得管理員資料失敗' 
    })
  } finally {
    loading.value = false
  }
}

// 模糊搜尋
const searchAdmins = async () => {
  if (!keyword.value.trim()) {
    fetchAdmins()
    return
  }
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/admins/search', {
      params: { keyword: keyword.value.trim() }
    })
    
    if (res.data.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: '找不到資料',
        text: `找不到與「${keyword.value}」相關的管理員`,
        confirmButtonColor: '#f39c12'
      })
    } else {
      admins.value = res.data
      currentPage.value = 1
      Swal.fire({
        icon: 'success',
        title: '搜尋成功',
        text: `找到 ${res.data.length} 筆符合的管理員資料`,
        timer: 1500,
        showConfirmButton: false
      })
    }
  } catch (error) {
    Swal.fire({ 
      icon: 'error', 
      title: '搜尋發生錯誤' 
    })
  } finally {
    loading.value = false
  }
}

// 分頁邏輯
const paginatedAdmins = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return admins.value.slice(start, start + pageSize)
})

const totalPages = computed(() => {
  return Math.ceil(admins.value.length / pageSize) || 1
})

// 停職操作
const deleteAdmin = async (admId, admName) => {
  const result = await Swal.fire({
    title: '確定要將此管理員停職嗎？',
    html: `管理員：<strong>${admName}</strong> (ID: ${admId})`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ff4d4f',
    cancelButtonColor: '#8c8c8c',
    confirmButtonText: '確定停職',
    cancelButtonText: '取消'
  })
  
  if (result.isConfirmed) {
    try {
      await axios.get('http://localhost:8080/admins/delete', { params: { admId } })
      Swal.fire({ 
        icon: 'success', 
        title: '已執行停職', 
        timer: 1500, 
        showConfirmButton: false 
      })
      fetchAdmins()
    } catch (error) {
      Swal.fire({ 
        icon: 'error', 
        title: '操作失敗' 
      })
    }
  }
}

const formatDate = (dt) => {
  if (!dt) return ''
  return dt.substring(0, 10).replace(/-/g, '/')
}

// 根據 ID 補零並獲取圖片路徑
const getAvatar = (id) => {
  if (id >= 1 && id <= 10) {
    const fileName = String(id).padStart(2, '0')
    return `/admin/${fileName}.jpg`
  }
  return '/admin/default.png'
}

// 權限樣式與文字
const getRoleClass = (role) => {
  return role === 9 ? 'role-super' : 'role-normal'
}

const getRoleText = (role) => {
  return role === 9 ? '超級管理員' : '一般管理員'
}

onMounted(fetchAdmins)
</script>

<template>
  <div class="admin-main-container">
    <div class="header-info-card">
      <div class="header-icon">
        <i class="fas fa-user-shield"></i>
      </div>
      <div class="header-content">
        <h2>管理員列表</h2>
        <p>管理系統管理員帳號與權限</p>
      </div>
    </div>

    <div class="statistics-row">
      <div class="stat-item blue-left">
        <div class="stat-icon-box bg-blue-icon">
          <i class="fas fa-users"></i>
        </div>
        <div class="stat-data">
          <div class="stat-value">{{ totalAdmins }}</div>
          <div class="stat-label">管理員總數</div>
        </div>
      </div>
      <div class="stat-item red-left">
        <div class="stat-icon-box bg-red-icon">
          <i class="fas fa-crown"></i>
        </div>
        <div class="stat-data">
          <div class="stat-value">{{ superAdmins }}</div>
          <div class="stat-label">超級管理員</div>
        </div>
      </div>
      <div class="stat-item green-left">
        <div class="stat-icon-box bg-green-icon">
          <i class="fas fa-user-tie"></i>
        </div>
        <div class="stat-data">
          <div class="stat-value">{{ normalAdmins }}</div>
          <div class="stat-label">一般管理員</div>
        </div>
      </div>
    </div>

    <div class="search-filter-bar">
      <div class="search-input-group">
        <div class="icon-input-wrap">
          <i class="fas fa-search"></i>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜尋帳號 / 姓名 / Email"
            @keyup.enter="searchAdmins"
          />
        </div>
        <button class="btn-action-search" @click="searchAdmins">搜尋</button>
        <button class="btn-action-all" @click="fetchAdmins">顯示全部</button>
      </div>
      <button class="btn-add-admin" @click="router.push('/admin/admins/create')">
        <i class="fas fa-plus"></i> 新增管理員
      </button>
    </div>

    <div class="table-wrapper-card">
      <table class="data-list-table">
        <thead>
          <tr>
            <th class="col-w-id">ID</th>
            <th class="col-w-info">管理員資訊</th>
            <th class="col-w-email">Email</th>
            <th class="col-w-role">權限</th>
            <th class="col-w-date">到職日</th>
            <th class="col-w-date">更新時間</th>
            <th class="col-w-btn">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in paginatedAdmins" :key="a.admId">
            <td>{{ a.admId }}</td>
            <td class="align-left">
              <div class="user-info-flex">
                <div class="avatar-box">
                  <img :src="getAvatar(a.admId)" alt="管理員頭像" />
                </div>
                <div class="name-account-stack">
                  <span class="full-name">{{ a.admName }}</span>
                  <span class="account-tag">@{{ a.admUsername }}</span>
                </div>
              </div>
            </td>
            <td class="align-left">{{ a.admEmail }}</td>
            <td>
              <span class="role-badge" :class="getRoleClass(a.admRole)">
                {{ getRoleText(a.admRole) }}
              </span>
            </td>
            <td>{{ formatDate(a.createdAt) }}</td>
            <td>{{ formatDate(a.updatedAt) }}</td>
            <td>
              <div class="op-button-group">
                <button class="btn-op-edit" @click="router.push(`/admin/admins/edit/${a.admId}`)">修改</button>
                <button class="btn-op-stop" @click="deleteAdmin(a.admId, a.admName)">停職</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="page-control-footer" v-if="admins.length > pageSize">
      <button class="page-nav-btn" :disabled="currentPage === 1" @click="currentPage--">上一頁</button>
      <span class="page-current-info">第 {{ currentPage }} / {{ totalPages }} 頁</span>
      <button class="page-nav-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一頁</button>
    </div>
  </div>
</template>

<style scoped>
.admin-main-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #ffffff;
  padding: 20px 24px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background-color: #e8f4ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #1890ff;
}

.header-content h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.header-content p {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 14px;
}

/* 統計卡片 */
.statistics-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-item {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left-width: 5px;
  border-left-style: solid;
}

.blue-left {
  border-left-color: #1890ff;
}

.red-left {
  border-left-color: #ff4d4f;
}

.green-left {
  border-left-color: #52c41a;
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 20px;
}

.bg-blue-icon {
  background-color: #1890ff; 
}

.bg-red-icon { 
  background-color: #ff4d4f; 
}

.bg-green-icon { 
  background-color: #52c41a; 
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

/* 按鈕選取與動態效果 */
.btn-action-search, 
.btn-action-all, 
.btn-add-admin, 
.btn-op-edit, 
.btn-op-stop, 
.page-nav-btn {
  transition: all 0.2s ease;
  cursor: pointer;
  user-select: none;
  outline: none;
}

.btn-action-search:hover, 
.btn-action-all:hover, 
.btn-add-admin:hover, 
.btn-op-edit:hover, 
.btn-op-stop:hover, 
.page-nav-btn:hover:not(:disabled) {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn-action-search:active, 
.btn-action-all:active, 
.btn-add-admin:active, 
.btn-op-edit:active, 
.btn-op-stop:active, 
.page-nav-btn:active:not(:disabled) {
  transform: scale(0.95);
}

/* 工具列樣式 */
.search-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-input-group {
  display: flex;
  gap: 12px;
}

.icon-input-wrap {
  position: relative;
}

.icon-input-wrap i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
}

.icon-input-wrap input {
  padding: 10px 12px 10px 36px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  width: 300px;
  transition: border-color 0.2s;
}

.icon-input-wrap input:focus {
  border-color: #1890ff;
  outline: none;
}

.btn-action-search {
  background-color: #1890ff;
  color: #ffffff;
  border: none;
  padding: 0 20px;
  border-radius: 8px;
}

.btn-action-all {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  padding: 0 20px;
  border-radius: 8px;
}

.btn-add-admin {
  background-color: #52c41a;
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
}

/* 表格樣式 */
.table-wrapper-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.data-list-table {
  width: 100%;
  border-collapse: collapse;
}

.data-list-table th {
  background-color: #f8f9fb;
  padding: 16px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.data-list-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

/* 針對 ID 欄位給予特定的左邊距 */
.data-list-table td:first-child, 
.data-list-table th:first-child {
  padding-left: 24px;
}

.align-left {
  text-align: left !important;
}

.user-info-flex {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
}

.col-w-info {
  text-align: left !important;
  padding-left: 16px !important;
}

.avatar-box img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.name-account-stack {
  display: flex;
  flex-direction: column;
}

.full-name {
  font-weight: 600;
}

.account-tag {
  font-size: 12px;
  color: #909399;
}

/* 權限標籤 (Badge) */
.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  display: inline-block;
}

.role-super {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8e8e 100%);
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

.role-normal {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

/* 操作按鈕顏色 */
.op-button-group {
  display: flex;
  justify-content: flex-start;
  gap: 8px;
}

.btn-op-edit {
  background-color: #1890ff;
  color: #ffffff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
}

.btn-op-stop {
  background-color: #ff4d4f;
  color: #ffffff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
}

/* 分頁 */
.page-control-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 0 20px 40px 0;
}

.page-nav-btn {
  padding: 8px 16px;
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.page-nav-btn:disabled {
  color: #c0c4cc;
  background-color: #f5f7fa;
  cursor: not-allowed;
}
</style>