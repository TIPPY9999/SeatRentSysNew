<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'
import { usePagination } from '@/composables/maintenance/usePagination'
import { InfoFilled, Right, Delete, Check } from '@element-plus/icons-vue'

const router = useRouter()
const staffList = ref([])
const searchText = ref('')
const loading = ref(true)
const pageVisible = ref(false)
const sortConfig = ref({ prop: 'staffId', order: 'ascending' })

// ====== 轉移工單 Dialog 狀態 ======
const showTransferDialog = ref(false)
const transferForm = ref({
  deleteStaffId: null,
  deleteStaffName: '',
  targetStaffId: null,
})
const transferLoading = ref(false)

// 計算可選的接手人員 (排除要刪除的人)
const availableTargetStaff = computed(() => {
  return staffList.value.filter((s) => s.staffId !== transferForm.value.deleteStaffId)
})

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

// ====== 刪除人員（含防呆轉移邏輯）======
const handleDelete = async (row) => {
  const result = await Swal.fire({
    title: '確定要停用此人員嗎？',
    html: `
      <div style="text-align: center;">
        <div style="width: 80px; height: 80px; margin: 0 auto 16px; background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
          <i class="fas fa-user-slash" style="font-size: 36px; color: white;"></i>
        </div>
        <p style="font-size: 16px; margin-bottom: 8px;">即將停用 <b style="color: #f56c6c;">${row.staffName}</b></p>
        <p style="color: #909399; font-size: 13px;">刪除的資料可在歷史紀錄中查看並恢復</p>
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
    didRender: (popup) => {
      // Task 1 & 2: 為統計卡片添加點擊事件
      const cards = popup.querySelectorAll('[data-card-type]')
      cards.forEach(card => {
        card.addEventListener('click', () => {
          const cardType = card.getAttribute('data-card-type')
          showHistoryModal(row.staffId, cardType, row.staffName)
        })
      })
    }
  })

  if (result.isConfirmed) {
    try {
      await maintenanceApi.deleteStaff(row.staffId)
      fetchStaff()
      await Swal.fire({
        icon: 'success',
        title: '停用成功',
        html: `<span><b>${row.staffName}</b> 已移至歷史紀錄</span>`,
        timer: 1000,
        timerProgressBar: true,
        showConfirmButton: false,
        showClass: { popup: 'animate__animated animate__bounceIn' },
      })
    } catch (error) {
      // 檢查是否有未完成工單（根據後端錯誤訊息）
      const errorMsg = error?.response?.data?.message || error?.message || ''
      if (
        errorMsg.includes('未完成') ||
        errorMsg.includes('工單') ||
        error?.response?.status === 400
      ) {
        // 開啟轉移 Dialog
        transferForm.value = {
          deleteStaffId: row.staffId,
          deleteStaffName: row.staffName,
          targetStaffId: null,
        }
        showTransferDialog.value = true
      }
      // 其他錯誤已由 http.js 攔截器處理
    }
  }
}

// ====== Task 2: 歷史工單查詢 Modal ======
const showHistoryModal = async (staffId, cardType, staffName) => {
  try {
    // 根據卡片類型決定查詢的狀態
    let statuses = []
    let title = ''
    let iconClass = ''
    let colorClass = ''
    
    switch (cardType) {
      case 'repair-pending':
        statuses = ['REPORTED', 'ASSIGNED', 'UNDER_MAINTENANCE']
        title = '待修任務'
        iconClass = 'fas fa-wrench'
        colorClass = 'text-warning'
        break
      case 'maintenance-pending':
        statuses = ['SCHEDULED'] // 根據你的系統調整
        title = '待保養任務'
        iconClass = 'fas fa-clipboard-list'
        colorClass = 'text-info'
        break
      case 'repair-completed':
        statuses = ['RESOLVED']
        title = '已完成維修'
        iconClass = 'fas fa-check-circle'
        colorClass = 'text-success'
        break
      case 'maintenance-completed':
        statuses = ['COMPLETED'] // 根據你的系統調整
        title = '已完成保養'
        iconClass = 'fas fa-flag-checkered'
        colorClass = 'text-secondary'
        break
    }

    // 呼叫 API 查詢工單
    const response = await maintenanceApi.getTicketsByStaff(staffId, statuses)
    const tickets = response.data || []

    // 限制顯示最近 10 筆避免過多資料
    const displayTickets = tickets.slice(0, 10)

    let tableHtml = ''
    if (displayTickets.length === 0) {
      tableHtml = `
        <div style="text-align: center; padding: 40px; color: #909399;">
          <i class="fas fa-inbox" style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;"></i>
          <p>暫無${title}記錄</p>
        </div>
      `
    } else {
      tableHtml = `
        <div style="max-height: 400px; overflow-y: auto;">
          <table class="table table-hover table-sm">
            <thead class="thead-light">
              <tr>
                <th style="width: 15%;"><i class="fas fa-ticket-alt mr-1"></i>工單編號</th>
                <th style="width: 20%;"><i class="fas fa-calendar mr-1"></i>日期</th>
                <th style="width: 25%;"><i class="fas fa-map-marker-alt mr-1"></i>維修目標</th>
                <th style="width: 25%;"><i class="fas fa-exclamation-triangle mr-1"></i>問題描述</th>
                <th style="width: 15%;"><i class="fas fa-clipboard-check mr-1"></i>處理結果</th>
              </tr>
            </thead>
            <tbody>
      `
      
      displayTickets.forEach(ticket => {
        const date = ticket.reportedAt ? new Date(ticket.reportedAt).toLocaleDateString('zh-TW') : '-'
        const target = ticket.spotId ? `機台 #${ticket.spotId}${ticket.seatsId ? ` - 座位 #${ticket.seatsId}` : ''}` : '未指定'
        const description = ticket.issueDesc || '無描述'
        const result = ticket.resolveNote || (ticket.issueStatus === 'RESOLVED' ? '已完成' : '處理中')
        
        tableHtml += `
          <tr>
            <td><span class="badge badge-secondary">#${ticket.ticketId}</span></td>
            <td><small>${date}</small></td>
            <td><small>${target}</small></td>
            <td><small title="${description}">${description.length > 20 ? description.substring(0, 20) + '...' : description}</small></td>
            <td><small>${result.length > 15 ? result.substring(0, 15) + '...' : result}</small></td>
          </tr>
        `
      })
      
      tableHtml += `
            </tbody>
          </table>
        </div>
      `
      
      if (tickets.length > 10) {
        tableHtml += `
          <div style="text-align: center; margin-top: 12px; color: #909399; font-size: 12px;">
            <i class="fas fa-info-circle mr-1"></i>
            顯示最近 10 筆，共 ${tickets.length} 筆記錄
          </div>
        `
      }
    }

    await Swal.fire({
      title: `<div style="display: flex; align-items: center; gap: 12px; justify-content: center;">
        <i class="${iconClass} ${colorClass}" style="font-size: 24px;"></i>
        <span>${staffName} - ${title}</span>
      </div>`,
      html: tableHtml,
      width: '90%',
      maxWidth: '1000px',
      showConfirmButton: true,
      confirmButtonText: '<i class="fas fa-times mr-1"></i>關閉',
      customClass: {
        popup: 'custom-swal-popup',
        confirmButton: 'btn btn-secondary'
      },
      showClass: { popup: 'animate__animated animate__fadeInUp animate__faster' },
      hideClass: { popup: 'animate__animated animate__fadeOutDown animate__faster' }
    })
  } catch (error) {
    console.error('查詢工單歷史失敗:', error)
    await Swal.fire({
      icon: 'error',
      title: '查詢失敗',
      text: '無法載入工單歷史記錄',
      timer: 2000,
      showConfirmButton: false
    })
  }
}

// ====== 執行轉移並刪除 ======
const handleTransferAndDelete = async () => {
  if (!transferForm.value.targetStaffId) {
    await Swal.fire({
      icon: 'warning',
      title: '請選擇接手人員',
      text: '必須指定一位接手人員來接收未完成的工單',
      confirmButtonColor: '#409eff',
    })
    return
  }

  transferLoading.value = true
  try {
    await maintenanceApi.transferAndDelete(
      transferForm.value.targetStaffId,
      transferForm.value.deleteStaffId,
    )

    showTransferDialog.value = false
    fetchStaff()

    const targetStaff = staffList.value.find((s) => s.staffId === transferForm.value.targetStaffId)

    await Swal.fire({
      icon: 'success',
      title: '轉移成功！',
      html: `
        <div style="text-align: center;">
          <p>工單已轉移給 <b style="color: #67c23a;">${targetStaff?.staffName || '接手人員'}</b></p>
          <p style="color: #909399; font-size: 13px;"><b>${transferForm.value.deleteStaffName}</b> 已停用</p>
        </div>
      `,
      timer: 2000,
      timerProgressBar: true,
      showConfirmButton: false,
      showClass: { popup: 'animate__animated animate__bounceIn' },
    })
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    transferLoading.value = false
  }
}

// 關閉 Dialog
const closeTransferDialog = () => {
  showTransferDialog.value = false
  transferForm.value = {
    deleteStaffId: null,
    deleteStaffName: '',
    targetStaffId: null,
  }
}

const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  // ★ 修正：只顯示啟用中的人員 (isActive = true)
  const activeStaff = staffList.value.filter((s) => s.isActive === true)

  if (!key) return activeStaff
  return activeStaff.filter(
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

// ★ 任務3：快速檢視人員詳情（增強版 - 區分維修與保養）
const viewDetail = async (row) => {
  // ★ 任務3：拆分為維修與保養統計
  let repairCurrent = 0    // 維修 + 未完成
  let maintainCurrent = 0  // 保養 + 未完成
  let repairDone = 0       // 維修 + 已完成
  let maintainDone = 0     // 保養 + 已完成

  // ★ 任務3：判斷是否為保養任務
  const isMaintenance = (issueType) => {
    if (!issueType) return false
    const keywords = ['保養', '例行', '檢查']
    return keywords.some(keyword => issueType.includes(keyword))
  }

  // ★ 任務3：判斷是否已完成
  const isCompleted = (status) => {
    return ['RESOLVED', 'CLOSED', 'CANCELLED'].includes(status)
  }

  try {
    const res = await maintenanceApi.getAllTickets()
    const allTickets = res.data || []

    // ★ 任務3：分類統計該人員的工單
    const staffTickets = allTickets.filter(t => t.assignedStaffId === row.staffId)
    
    staffTickets.forEach(ticket => {
      const isMaintenanceTask = isMaintenance(ticket.issueType)
      const isDone = isCompleted(ticket.issueStatus)
      
      if (isMaintenanceTask) {
        // 保養類任務
        if (isDone) {
          maintainDone++
        } else {
          maintainCurrent++
        }
      } else {
        // 維修類任務
        if (isDone) {
          repairDone++
        } else {
          repairCurrent++
        }
      }
    })
  } catch {
    // 統計失敗時保持為 0
  }

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
          <!-- ★ 任務3：2x2 Grid 統計卡片 -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 8px;">
            <!-- 左上：待修任務 -->
            <div data-card-type="repair-pending" style="padding: 16px; background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%); border-radius: 10px; text-align: center; color: white; cursor: pointer; transition: all 0.3s ease;" 
                 onmouseover="this.style.transform='scale(1.02)'; this.style.boxShadow='0 8px 25px rgba(230, 162, 60, 0.3)';" 
                 onmouseout="this.style.transform='scale(1)'; this.style.boxShadow='none';">
              <p style="margin: 0; font-size: 12px; opacity: 0.9;"><i class="fas fa-wrench mr-1"></i>待修任務</p>
              <p style="margin: 4px 0 0; font-size: 24px; font-weight: bold;">${repairCurrent}</p>
            </div>
            <!-- 右上：待保養 -->
            <div data-card-type="maintenance-pending" style="padding: 16px; background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); border-radius: 10px; text-align: center; color: white; cursor: pointer; transition: all 0.3s ease;"
                 onmouseover="this.style.transform='scale(1.02)'; this.style.boxShadow='0 8px 25px rgba(64, 158, 255, 0.3)';" 
                 onmouseout="this.style.transform='scale(1)'; this.style.boxShadow='none';">
              <p style="margin: 0; font-size: 12px; opacity: 0.9;"><i class="fas fa-clipboard-list mr-1"></i>待保養</p>
              <p style="margin: 4px 0 0; font-size: 24px; font-weight: bold;">${maintainCurrent}</p>
            </div>
            <!-- 左下：維修完成 -->
            <div data-card-type="repair-completed" style="padding: 16px; background: linear-gradient(135deg, #67c23a 0%, #95d475 100%); border-radius: 10px; text-align: center; color: white; cursor: pointer; transition: all 0.3s ease;"
                 onmouseover="this.style.transform='scale(1.02)'; this.style.boxShadow='0 8px 25px rgba(103, 194, 58, 0.3)';" 
                 onmouseout="this.style.transform='scale(1)'; this.style.boxShadow='none';">
              <p style="margin: 0; font-size: 12px; opacity: 0.9;"><i class="fas fa-check-circle mr-1"></i>維修完成</p>
              <p style="margin: 4px 0 0; font-size: 24px; font-weight: bold;">${repairDone}</p>
            </div>
            <!-- 右下：保養完成 -->
            <div data-card-type="maintenance-completed" style="padding: 16px; background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%); border-radius: 10px; text-align: center; color: white; cursor: pointer; transition: all 0.3s ease;"
                 onmouseover="this.style.transform='scale(1.02)'; this.style.boxShadow='0 8px 25px rgba(144, 147, 153, 0.3)';" 
                 onmouseout="this.style.transform='scale(1)'; this.style.boxShadow='none';">
              <p style="margin: 0; font-size: 12px; opacity: 0.9;"><i class="fas fa-flag-checkered mr-1"></i>保養完成</p>
              <p style="margin: 4px 0 0; font-size: 24px; font-weight: bold;">${maintainDone}</p>
            </div>
          </div>
          
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
    width: 520,
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
    timer: 600,
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
                    <h3>{{ staffList.filter((s) => s.isActive).length }}</h3>
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

    <!-- ========== 轉移工單並刪除 Dialog ========== -->
    <el-dialog
      v-model="showTransferDialog"
      title="轉移工單並刪除人員"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="!transferLoading"
      @close="closeTransferDialog"
    >
      <div class="transfer-dialog-content">
        <el-alert type="warning" :closable="false" show-icon class="mb-4">
          <template #title>
            <strong>{{ transferForm.deleteStaffName }}</strong> 有未完成的工單
          </template>
          <template #default> 請選擇要將工單轉移給哪位人員後，才能刪除此人員。 </template>
        </el-alert>

        <el-form label-position="top">
          <el-form-item label="選擇接手人員" required>
            <el-select
              v-model="transferForm.targetStaffId"
              placeholder="請選擇接手人員"
              filterable
              style="width: 100%"
              :disabled="transferLoading"
            >
              <el-option
                v-for="staff in availableTargetStaff"
                :key="staff.staffId"
                :label="`${staff.staffName} (${staff.specialization || '未設定專長'})`"
                :value="staff.staffId"
              >
                <div class="transfer-option">
                  <span class="name">{{ staff.staffName }}</span>
                  <el-tag size="small" type="info">{{
                    staff.specialization || '未設定專長'
                  }}</el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <div class="transfer-preview" v-if="transferForm.targetStaffId">
          <el-divider content-position="left">
            <el-icon><InfoFilled /></el-icon> 操作預覽
          </el-divider>
          <div class="preview-content">
            <p>
              <el-icon color="#E6A23C"><Right /></el-icon>
              <strong>{{ transferForm.deleteStaffName }}</strong> 的所有未完成工單 將轉移給
              <strong>{{
                availableTargetStaff.find((s) => s.staffId === transferForm.targetStaffId)
                  ?.staffName
              }}</strong>
            </p>
            <p>
              <el-icon color="#F56C6C"><Delete /></el-icon>
              然後刪除人員 <strong>{{ transferForm.deleteStaffName }}</strong>
            </p>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeTransferDialog" :disabled="transferLoading"> 取消 </el-button>
        <el-button
          type="danger"
          :loading="transferLoading"
          :disabled="!transferForm.targetStaffId"
          @click="handleTransferAndDelete"
        >
          <el-icon v-if="!transferLoading"><Check /></el-icon>
          確認轉移並刪除
        </el-button>
      </template>
    </el-dialog>
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

/* ========== 轉移工單 Dialog 樣式 ========== */
.transfer-dialog-content {
  padding: 0 10px;
}

.transfer-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.transfer-option .name {
  font-weight: 500;
}

.transfer-preview {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef3 100%);
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 10px;
}

.transfer-preview .preview-content {
  font-size: 14px;
}

.transfer-preview .preview-content p {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 8px 0;
  color: #606266;
}

.transfer-preview .preview-content p strong {
  color: #303133;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}
</style>
