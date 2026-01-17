<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'
import { usePagination } from '@/composables/maintenance/usePagination'

const router = useRouter()
const staffList = ref([])
const searchText = ref('')
const loading = ref(true)
const pageVisible = ref(false)
const sortConfig = ref({ prop: 'staffId', order: 'ascending' })

const fetchStaff = async () => {
  try {
    loading.value = true
    const res = await maintenanceApi.getAllStaff()
    staffList.value = res.data
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  const result = await Swal.fire({
    title: '確定要停用此人員嗎？',
    html: `
      <div style="text-align: center;">
        <div style="width: 80px; height: 80px; margin: 0 auto 16px; background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
          <i class="fas fa-user-slash" style="font-size: 36px; color: white;"></i>
        </div>
        <p style="font-size: 16px; margin-bottom: 8px;">即將停用 <b style="color: #f56c6c;">${row.staffName}</b></p>
        <p style="color: #909399; font-size: 13px;">此操作為軟刪除，可在歷史紀錄中查看並恢復</p>
      </div>
    `,
    icon: null,
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    cancelButtonColor: '#909399',
    confirmButtonText: '<i class="fas fa-user-slash mr-1"></i> 確認停用',
    cancelButtonText: '取消',
    showClass: { popup: 'animate__animated animate__fadeInDown animate__faster' },
    hideClass: { popup: 'animate__animated animate__fadeOutUp animate__faster' },
    customClass: { popup: 'custom-swal-popup' },
  })

  if (result.isConfirmed) {
    try {
      await maintenanceApi.deleteStaff(row.staffId)
      await Swal.fire({
        icon: 'success',
        title: '停用成功',
        html: `<span><b>${row.staffName}</b> 已移至歷史紀錄</span>`,
        timer: 1800,
        timerProgressBar: true,
        showConfirmButton: false,
        showClass: { popup: 'animate__animated animate__bounceIn' },
      })
      fetchStaff()
    } catch {
      // 錯誤已由 http.js 攔截器處理
    }
  }
}

const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  if (!key) return staffList.value
  return staffList.value.filter(
    (s) =>
      (s.staffName || '').toLowerCase().includes(key) ||
      (s.staffCompany || '').toLowerCase().includes(key) ||
      (s.staffPhone || '').includes(key),
  )
})

// 使用 usePagination composable
const {
  currentPage,
  pageSize,
  paginatedList,
  total: paginationTotal,
  showPagination,
  resetPagination,
} = usePagination(filteredList, { defaultPageSize: 10 })

// 搜尋時重置分頁
watch(searchText, () => {
  resetPagination()
})

const formatDate = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return new Date(cellValue).toLocaleDateString('zh-TW')
}

// 快速檢視人員詳情
const viewDetail = (row) => {
  Swal.fire({
    title: `<div style="display: flex; align-items: center; gap: 12px;">
      <div style="width: 50px; height: 50px; background: linear-gradient(135deg, #67c23a 0%, #95d475 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 22px; font-weight: bold;">
        ${row.staffName?.charAt(0) || '?'}
      </div>
      <span>${row.staffName}</span>
    </div>`,
    html: `
      <div style="text-align: left; margin-top: 20px;">
        <div style="display: grid; gap: 12px;">
          <div style="padding: 14px; background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%); border-radius: 10px; border-left: 4px solid #67c23a;">
            <p style="margin: 0; font-size: 12px; color: #909399;">所屬公司</p>
            <p style="margin: 4px 0 0; font-size: 15px; color: #303133; font-weight: 500;">
              <i class="fas fa-building mr-2" style="color: #67c23a;"></i>${row.staffCompany || '未填寫'}
            </p>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div style="padding: 14px; background: #f5f7fa; border-radius: 10px;">
              <p style="margin: 0; font-size: 12px; color: #909399;">聯絡電話</p>
              <p style="margin: 4px 0 0; font-size: 14px; color: #303133;">
                <i class="fas fa-phone mr-2" style="color: #409eff;"></i>${row.staffPhone || '-'}
              </p>
            </div>
            <div style="padding: 14px; background: #f5f7fa; border-radius: 10px;">
              <p style="margin: 0; font-size: 12px; color: #909399;">電子郵件</p>
              <p style="margin: 4px 0 0; font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis;">
                <i class="fas fa-envelope mr-2" style="color: #e6a23c;"></i>${row.staffEmail || '-'}
              </p>
            </div>
          </div>
          <div style="padding: 14px; background: #fdf6ec; border-radius: 10px; border-left: 4px solid #e6a23c;">
            <p style="margin: 0; font-size: 12px; color: #909399;">備註說明</p>
            <p style="margin: 4px 0 0; font-size: 14px; color: #606266;">
              ${row.staffNote || '無備註'}
            </p>
          </div>
        </div>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: '<i class="fas fa-edit mr-1"></i> 編輯資料',
    cancelButtonText: '關閉',
    confirmButtonColor: '#409eff',
    showClass: { popup: 'animate__animated animate__zoomIn animate__faster' },
    hideClass: { popup: 'animate__animated animate__zoomOut animate__faster' },
    width: 480,
  }).then((result) => {
    if (result.isConfirmed) {
      router.push(`/admin/staff-form/${row.staffId}`)
    }
  })
}

const handleAddNew = () => {
  Swal.fire({
    title: '新增維護人員',
    text: '即將前往新增人員表單',
    icon: 'info',
    timer: 800,
    timerProgressBar: true,
    showConfirmButton: false,
    showClass: { popup: 'animate__animated animate__fadeInRight animate__faster' },
  }).then(() => {
    router.push('/admin/staff-form')
  })
}

onMounted(() => {
  fetchStaff()
  setTimeout(() => (pageVisible.value = true), 100)
})
</script>

<template>
  <div class="staff-list-container">
    <!-- 頁面標題區 -->
    <section class="content-header">
      <div class="container-fluid">
        <transition name="slide-down" appear>
          <div class="page-title-box">
            <div class="title-icon">
              <i class="fas fa-users-cog"></i>
            </div>
            <div class="title-content">
              <h1>維護人員管理</h1>
              <p class="subtitle">管理系統內的維護人員資料</p>
            </div>
            <div class="title-actions">
              <el-button-group>
                <el-button type="success" @click="handleAddNew" class="action-btn add-btn">
                  <i class="fas fa-user-plus mr-2"></i> 新增人員
                </el-button>
                <el-button
                  type="info"
                  plain
                  @click="router.push('/admin/staff-history')"
                  class="action-btn"
                >
                  <i class="fas fa-history mr-2"></i> 歷史紀錄
                </el-button>
              </el-button-group>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <!-- 主內容區 -->
    <section class="content">
      <div class="container-fluid">
        <transition name="zoom-fade" appear>
          <div v-show="pageVisible">
            <!-- 統計卡片列 -->
            <el-row :gutter="20" class="mb-4">
              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card active-card" @click="searchText = ''">
                  <div class="stat-icon pulse-animation">
                    <i class="fas fa-user-check"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ staffList.length }}</h3>
                    <span>在職人員</span>
                  </div>
                  <div class="stat-bg-icon">
                    <i class="fas fa-users"></i>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card">
                  <div class="stat-icon">
                    <i class="fas fa-search"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ filteredList.length }}</h3>
                    <span>搜尋結果</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- 資料表格卡片 -->
            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="card-header-content">
                  <div class="header-left">
                    <span class="header-icon">
                      <i class="fas fa-list-ul"></i>
                    </span>
                    <span class="header-text">人員名單</span>
                    <el-tag type="success" effect="light" size="small" class="ml-2" round>
                      <i class="fas fa-circle" style="font-size: 6px; margin-right: 4px"></i>
                      {{ filteredList.length }} 位
                    </el-tag>
                  </div>
                  <div class="header-right">
                    <el-input
                      v-model="searchText"
                      placeholder="搜尋姓名、公司、電話..."
                      prefix-icon="Search"
                      clearable
                      class="search-input"
                    >
                      <template #append>
                        <el-button icon="Refresh" @click="fetchStaff" />
                      </template>
                    </el-input>
                  </div>
                </div>
              </template>

              <!-- 骨架屏載入 -->
              <div v-if="loading" class="loading-skeleton">
                <el-skeleton :rows="6" animated />
              </div>

              <!-- 資料表格 -->
              <el-table
                v-else
                :data="paginatedList"
                stripe
                highlight-current-row
                style="width: 100%"
                class="custom-table"
                @row-dblclick="viewDetail"
              >
                <el-table-column prop="staffId" label="ID" width="70" align="center" sortable>
                  <template #default="{ row }">
                    <el-tag effect="plain" size="small">#{{ row.staffId }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="staffName" label="姓名" width="150" sortable>
                  <template #default="{ row }">
                    <div class="name-cell" @click="viewDetail(row)">
                      <div class="name-avatar">
                        {{ row.staffName?.charAt(0) || '?' }}
                      </div>
                      <div class="name-info">
                        <span class="name-text">{{ row.staffName }}</span>
                        <span class="status-dot active"></span>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column prop="staffCompany" label="所屬公司" min-width="160">
                  <template #default="{ row }">
                    <div v-if="row.staffCompany" class="company-cell">
                      <i class="fas fa-building company-icon"></i>
                      <span>{{ row.staffCompany }}</span>
                    </div>
                    <span v-else class="text-muted">未填寫</span>
                  </template>
                </el-table-column>

                <el-table-column prop="staffPhone" label="聯絡電話" width="150">
                  <template #default="{ row }">
                    <div v-if="row.staffPhone" class="phone-cell">
                      <i class="fas fa-phone phone-icon"></i>
                      <span>{{ row.staffPhone }}</span>
                    </div>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column prop="staffEmail" label="Email" min-width="200">
                  <template #default="{ row }">
                    <el-tooltip v-if="row.staffEmail" :content="row.staffEmail" placement="top">
                      <div class="email-cell">
                        <i class="fas fa-envelope email-icon"></i>
                        <span>{{ row.staffEmail }}</span>
                      </div>
                    </el-tooltip>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="staffNote"
                  label="備註"
                  min-width="120"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span v-if="row.staffNote" class="note-cell">{{ row.staffNote }}</span>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="createdAt"
                  label="建立日期"
                  width="120"
                  :formatter="formatDate"
                />

                <el-table-column label="操作" width="180" align="center" fixed="right">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-tooltip content="查看詳情" placement="top">
                        <el-button
                          type="info"
                          size="small"
                          circle
                          @click="viewDetail(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-eye"></i>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="編輯資料" placement="top">
                        <el-button
                          type="primary"
                          size="small"
                          circle
                          @click="router.push(`/admin/staff-form/${row.staffId}`)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-edit"></i>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="停用人員" placement="top">
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click="handleDelete(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-user-slash"></i>
                        </el-button>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>

                <!-- 空狀態 -->
                <template #empty>
                  <el-empty description="目前沒有人員資料">
                    <template #image>
                      <div class="empty-icon">
                        <i class="fas fa-users-slash"></i>
                      </div>
                    </template>
                    <el-button type="primary" @click="handleAddNew">
                      <i class="fas fa-plus mr-1"></i> 新增第一位人員
                    </el-button>
                  </el-empty>
                </template>
              </el-table>

              <!-- 分頁器 -->
              <div class="pagination-wrapper" v-if="showPagination">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="paginationTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                />
              </div>
            </el-card>

            <!-- 快速操作提示 -->
            <div class="tips-bar mt-3">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>💡 小提示：雙擊表格列可快速查看人員詳情</span>
                </template>
              </el-alert>
            </div>
          </div>
        </transition>
      </div>
    </section>
  </div>
</template>

<style scoped>
.staff-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding-bottom: 40px;
}

.content-header {
  padding: 20px 1rem;
}

.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.title-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  transition: all 0.4s ease;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.title-icon:hover {
  transform: scale(1.1) rotate(10deg);
}

.title-content {
  flex: 1;
  min-width: 200px;
}

.title-content h1 {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 700;
  color: #303133;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-content .subtitle {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

.title-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-btn {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

/* 統計卡片 */
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  z-index: 1;
}

.active-card .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.filter-card .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}

.stat-info {
  z-index: 1;
}

.stat-info h3 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #303133;
}

.stat-info span {
  font-size: 0.85rem;
  color: #909399;
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 80px;
  color: rgba(0, 0, 0, 0.03);
}

.pulse-animation {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.header-text {
  font-weight: 600;
  font-size: 1.1rem;
  color: #303133;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 10px 10px 0;
}

/* 表格樣式 */
.custom-table {
  --el-table-header-bg-color: #f8f9fa;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.name-cell:hover {
  background: #f0f9eb;
}

.name-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.name-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name-text {
  font-weight: 500;
  color: #303133;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: #67c23a;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
  animation: blink 2s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.company-cell,
.phone-cell,
.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.company-icon {
  color: #409eff;
}
.phone-icon {
  color: #67c23a;
}
.email-icon {
  color: #e6a23c;
}

.email-cell {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-cell {
  color: #606266;
  font-size: 13px;
}

/* 操作按鈕 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-btn-item {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn-item:hover {
  transform: scale(1.2);
}

/* 空狀態 */
.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

/* 分頁器 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

/* 提示欄 */
.tips-bar :deep(.el-alert) {
  border-radius: 12px;
}

/* 過渡動畫 */
.slide-down-enter-active {
  transition: all 0.5s ease-out;
}
.slide-down-leave-active {
  transition: all 0.3s ease-in;
}
.slide-down-enter-from {
  transform: translateY(-30px);
  opacity: 0;
}
.slide-down-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.zoom-fade-enter-active {
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.zoom-fade-leave-active {
  transition: all 0.3s ease-in;
}
.zoom-fade-enter-from {
  transform: scale(0.95);
  opacity: 0;
}
.zoom-fade-leave-to {
  transform: scale(0.98);
  opacity: 0;
}

/* 輔助類 */
.text-muted {
  color: #c0c4cc;
}
.mr-1 {
  margin-right: 4px;
}
.mr-2 {
  margin-right: 8px;
}
.ml-2 {
  margin-left: 8px;
}
.mb-4 {
  margin-bottom: 1.5rem;
}
.mt-3 {
  margin-top: 1rem;
}
</style>
