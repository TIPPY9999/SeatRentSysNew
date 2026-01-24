<script setup>
/**
 * AdminListView.vue：管理員列表
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const admins = ref([])
const errorMsg = ref('')
const keyword = ref('')

// 統計數據
const totalAdmins = computed(() => admins.value.length)
const superAdmins = computed(() => admins.value.filter(a => a.admRole === 9).length)
const normalAdmins = computed(() => admins.value.filter(a => a.admRole === 1).length)

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
  Swal.fire({
    title: '確定要刪除嗎？',
    html: `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg><br><br>此操作無法復原`,
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    cancelButtonColor: '#909399',
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消'
  }).then((result) => {
    if (result.isConfirmed) {
      axios
        .get('http://localhost:8080/admins/delete', {
          params: { admId },
        })
        .then(() => {
          Swal.fire({
            title: '刪除成功',
            html: `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="16 12 12 8 8 12"></polyline><line x1="12" y1="16" x2="12" y2="8"></line></svg>`,
            confirmButtonColor: '#409eff'
          })
          fetchAdmins()
        })
        .catch(() => {
          Swal.fire({
            title: '刪除失敗',
            html: `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`,
            confirmButtonColor: '#409eff'
          })
        })
    }
  })
}

const formatDateTime = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 19)
}

const getRoleType = (role) => {
  return role === 9 ? 'danger' : 'primary'
}

const getRoleText = (role) => {
  return role === 9 ? '超級管理員' : '一般管理員'
}

onMounted(() => {
  fetchAdmins()
})
</script>

<template>
  <div class="admin-list-page">
    <!-- 頁面標題 -->
    <div class="page-title-box">
      <div class="title-icon">
        <i class="fas fa-user-shield"></i>
      </div>
      <div class="title-content">
        <h2>管理員列表</h2>
        <p>管理系統管理員帳號與權限</p>
      </div>
    </div>

    <!-- 統計卡片 -->
    <div class="stat-cards">
      <div class="stat-card total">
        <div class="stat-icon">
          <i class="fas fa-users"></i>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ totalAdmins }}</span>
          <span class="stat-label">管理員總數</span>
        </div>
      </div>
      <div class="stat-card super">
        <div class="stat-icon">
          <i class="fas fa-crown"></i>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ superAdmins }}</span>
          <span class="stat-label">超級管理員</span>
        </div>
      </div>
      <div class="stat-card normal">
        <div class="stat-icon">
          <i class="fas fa-user-tie"></i>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ normalAdmins }}</span>
          <span class="stat-label">一般管理員</span>
        </div>
      </div>
    </div>

    <!-- 搜尋與操作列 -->
    <div class="filter-bar">
      <div class="search-section">
        <div class="search-input-wrapper">
          <i class="fas fa-search search-icon"></i>
          <el-input
            v-model="keyword"
            placeholder="搜尋帳號 / 姓名 / Email"
            clearable
            @keyup.enter="searchAdmins"
            class="search-input"
          />
        </div>
        <el-button type="primary" @click="searchAdmins">
          <i class="fas fa-search mr-1"></i> 搜尋
        </el-button>
        <el-button @click="fetchAdmins">
          <i class="fas fa-list mr-1"></i> 顯示全部
        </el-button>
      </div>
      <el-button type="success" @click="router.push('/admin/admins/create')">
        <i class="fas fa-plus mr-1"></i> 新增管理員
      </el-button>
    </div>

    <!-- 錯誤訊息 -->
    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" class="error-alert" />

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table :data="admins" stripe style="width: 100%" empty-text="目前沒有管理員資料">
        <el-table-column prop="admId" label="ID" width="80" align="center" />
        <el-table-column prop="admUsername" label="帳號" min-width="120" />
        <el-table-column prop="admName" label="姓名" min-width="100" />
        <el-table-column prop="admEmail" label="Email" min-width="180" />
        <el-table-column label="權限" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.admRole)" effect="dark">
              {{ getRoleText(row.admRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建立時間" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新時間" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="router.push(`/admin/admins/edit/${row.admId}`)">
              <i class="fas fa-edit"></i>
            </el-button>
            <el-button type="danger" size="small" @click="deleteAdmin(row.admId)">
              <i class="fas fa-trash-alt"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
/* ========== 頁面容器 ========== */
.admin-list-page {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: 100vh;
}

/* ========== 頁面標題區塊 ========== */
.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.title-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.title-content h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
}

.title-content p {
  margin: 4px 0 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

/* ========== 統計卡片 ========== */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border-left: 4px solid;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-card.total { border-left-color: #409eff; }
.stat-card.super { border-left-color: #f56c6c; }
.stat-card.normal { border-left-color: #67c23a; }

.stat-card.total .stat-icon { background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); }
.stat-card.super .stat-icon { background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%); }
.stat-card.normal .stat-icon { background: linear-gradient(135deg, #67c23a 0%, #b3e19d 100%); }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 4px;
}

/* ========== 搜尋與操作列 ========== */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.search-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #909399;
  z-index: 1;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: 8px;
}

.search-input {
  width: 280px;
}

/* ========== 錯誤提示 ========== */
.error-alert {
  margin-bottom: 20px;
  border-radius: 12px;
}

/* ========== 表格卡片 ========== */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.table-card :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.table-card :deep(.el-table th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%) !important;
  font-weight: 600;
  color: #303133;
}

.table-card :deep(.el-table td),
.table-card :deep(.el-table th) {
  padding: 14px 0;
}

.table-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafbfc;
}

.table-card :deep(.el-table__row:hover > td) {
  background: #f0f7ff !important;
}

/* ========== 間距工具類 ========== */
.mr-1 { margin-right: 6px; }

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .admin-list-page {
    padding: 16px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
}
</style>