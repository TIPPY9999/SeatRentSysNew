<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { usePagination } from '@/composables/maintenance/usePagination'
import { useScheduleConfig } from '@/composables/maintenance/useScheduleConfig'
import ScheduleCalendar from '@/components/maintenance/ScheduleCalendar.vue'
import { Calendar, List, Clock, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const {
  scheduleTypeConfig,
  dayOfWeekConfig,
  formatDateTime,
  formatScheduleDetail,
  getRelativeTime,
  getPriorityTag,
} = useScheduleConfig()

// ====== 資料狀態 ======
const schedules = ref([])
const loading = ref(true)
const pageVisible = ref(false)
const searchText = ref('')
const viewMode = ref('table') // 'table' | 'calendar'

// ====== 彈窗控制 ======
const showHistoryDialog = ref(false)
const showDeletedDialog = ref(false)
const historyTickets = ref([])
const deletedSchedules = ref([])
const dialogLoading = ref(false)

// ====== API 資料讀取 ======
const fetchSchedules = async () => {
  try {
    loading.value = true
    const res = await maintenanceApi.getAllSchedules()
    schedules.value = res.data
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
    setTimeout(() => (pageVisible.value = true), 100)
  }
}

// ====== 統計數據 ======
const stats = computed(() => {
  const active = schedules.value.filter((s) => s.isActive)
  const daily = active.filter((s) => s.scheduleType === 'DAILY').length
  const weekly = active.filter((s) => s.scheduleType === 'WEEKLY').length
  const monthly = active.filter((s) => s.scheduleType === 'MONTHLY').length

  // 即將執行（24小時內）
  const now = new Date()
  const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000)
  const upcoming = active.filter((s) => {
    const next = new Date(s.nextExecuteAt)
    return next >= now && next <= tomorrow
  }).length

  return { total: active.length, daily, weekly, monthly, upcoming }
})

// ====== 即將執行的排程 (Timeline 用) ======
const upcomingSchedules = computed(() => {
  const now = new Date()
  return schedules.value
    .filter((s) => s.isActive && new Date(s.nextExecuteAt) >= now)
    .sort((a, b) => new Date(a.nextExecuteAt) - new Date(b.nextExecuteAt))
    .slice(0, 5)
})

// ====== 篩選邏輯 ======
const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()
  let list = schedules.value.filter((s) => s.isActive === true)
  if (!key) return list
  return list.filter(
    (s) =>
      s.title?.toLowerCase().includes(key) ||
      s.issueType?.toLowerCase().includes(key) ||
      String(s.scheduleId).includes(key),
  )
})

// ====== 分頁 ======
const { currentPage, pageSize, paginatedList, total, resetPagination } = usePagination(
  filteredList,
  10,
)

watch(searchText, () => resetPagination())

// ====== 業務邏輯 ======
const handleCreate = () => router.push('/admin/maintenance/schedule/create')
const handleEdit = (row) => router.push(`/admin/maintenance/schedule/edit/${row.scheduleId}`)

// ★ Bug1 修復：改為 before-change 模式，確認成功才切換
const handleToggleConfirm = async (row) => {
  const action = row.isActive ? '停用' : '啟用'
  
  const result = await Swal.fire({
    title: `確定要${action}此排程嗎？`,
    html: `
      <div style="text-align: center;">
        <p>排程：<b style="color: ${row.isActive ? '#f56c6c' : '#67c23a'}">${row.title}</b></p>
        <p style="color: #909399; font-size: 13px;">${row.isActive ? '停用後將不會自動執行' : '啟用後將恢復自動執行'}</p>
      </div>
    `,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: row.isActive ? '#f56c6c' : '#67c23a',
    confirmButtonText: `確認${action}`,
    cancelButtonText: '取消',
  })

  if (!result.isConfirmed) {
    return false // 返回 false，switch 不會切換
  }

  try {
    await maintenanceApi.toggleSchedule(row.scheduleId)
    
    // ★ 成功後重新載入資料
    await fetchSchedules()
    
    // ★ 如果是停用，提示已移到停用清單
    if (row.isActive) {
      await Swal.fire({
        icon: 'success',
        title: `已${action}`,
        html: '<p style="color: #909399; font-size: 13px;">排程已移至「已停用」清單</p>',
        timer: 1500,
        showConfirmButton: false
      })
    } else {
      await Swal.fire({ icon: 'success', title: `已${action}`, timer: 1000, showConfirmButton: false })
    }
    
    return true // 返回 true，讓 switch 切換
  } catch (error) {
    console.error('Toggle failed:', error)
    const errorMsg = error?.response?.data?.message || `${action}失敗，請稍後再試`
    Swal.fire('錯誤', errorMsg, 'error')
    return false // 失敗不切換
  }
}

const handleDelete = async (row) => {
  const result = await Swal.fire({
    title: '確定要刪除？',
    html: `<p>排程：<b style="color:#f56c6c">${row.title}</b></p>`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    confirmButtonText: '<i class="fas fa-trash-alt"></i> 刪除',
    cancelButtonText: '取消',
  })

  if (result.isConfirmed) {
    try {
      await maintenanceApi.deleteSchedule(row.scheduleId)
      fetchSchedules()
      Swal.fire({ icon: 'success', title: '已刪除', timer: 1000, showConfirmButton: false })
    } catch {
      /* handled */
    }
  }
}

// ====== 彈窗邏輯 ======
const openHistoryDialog = async () => {
  showHistoryDialog.value = true
  dialogLoading.value = true
  try {
    const res = await maintenanceApi.getAllTickets()
    historyTickets.value = res.data.filter(
      (t) => t.issueDesc && t.issueDesc.includes('【排程自動保養】'),
    )
  } catch (e) {
    console.error(e)
  } finally {
    dialogLoading.value = false
  }
}

const openDeletedDialog = async () => {
  showDeletedDialog.value = true
  dialogLoading.value = true
  try {
    const res = await maintenanceApi.getAllSchedules()
    deletedSchedules.value = res.data.filter((s) => !s.isActive)
  } catch (e) {
    console.error(e)
  } finally {
    dialogLoading.value = false
  }
}

const handleRestore = async (row) => {
  try {
    await maintenanceApi.toggleSchedule(row.scheduleId)
    Swal.fire({ icon: 'success', title: '排程已復原', showConfirmButton: false, timer: 1000 })
    await fetchSchedules()
    openDeletedDialog()
  } catch {
    Swal.fire('錯誤', '復原失敗', 'error')
  }
}

// ====== 日曆點擊事件 ======
const handleCalendarScheduleClick = (schedule) => {
  handleEdit(schedule)
}

onMounted(fetchSchedules)
</script>

<template>
  <div class="schedule-list-container">
    <!-- ========== Header ========== -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <h1 class="page-title">
              <i class="fas fa-calendar-check mr-2" style="color: #409eff"></i>
              定期維護排程
            </h1>
          </div>
          <div class="col-sm-6 text-right">
            <el-button-group class="mr-2">
              <el-button type="info" plain @click="openHistoryDialog">
                <i class="fas fa-history mr-1"></i> 保養紀錄
              </el-button>
              <el-button type="warning" plain @click="openDeletedDialog">
                <i class="fas fa-trash-alt mr-1"></i> 已停用
              </el-button>
            </el-button-group>
            <el-button type="primary" @click="handleCreate">
              <i class="fas fa-plus mr-1"></i> 新增排程
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <transition name="fade-slide" appear>
          <div v-if="pageVisible">
            <!-- ========== 統計卡片 ========== -->
            <el-row :gutter="16" class="mb-4">
              <el-col :xs="12" :sm="6" :md="4">
                <el-card shadow="hover" class="stat-card stat-total">
                  <el-statistic title="啟用中排程" :value="stats.total">
                    <template #prefix><i class="fas fa-calendar-check"></i></template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-card shadow="hover" class="stat-card stat-upcoming">
                  <el-statistic title="24小時內執行" :value="stats.upcoming">
                    <template #prefix><i class="fas fa-clock"></i></template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-card shadow="hover" class="stat-card stat-daily">
                  <el-statistic title="每日排程" :value="stats.daily">
                    <template #prefix><i class="fas fa-sun"></i></template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-card shadow="hover" class="stat-card stat-weekly">
                  <el-statistic title="每週排程" :value="stats.weekly">
                    <template #prefix><i class="fas fa-calendar-week"></i></template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-card shadow="hover" class="stat-card stat-monthly">
                  <el-statistic title="每月排程" :value="stats.monthly">
                    <template #prefix><i class="fas fa-calendar-alt"></i></template>
                  </el-statistic>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <!-- ========== 主內容區 ========== -->
              <el-col :xs="24" :lg="17">
                <el-card shadow="hover" class="main-card">
                  <!-- 工具列 -->
                  <div class="toolbar">
                    <el-input
                      v-model="searchText"
                      placeholder="搜尋標題、問題類型..."
                      clearable
                      prefix-icon="Search"
                      style="width: 280px"
                    />
                    <div class="toolbar-right">
                      <el-radio-group v-model="viewMode" size="small">
                        <el-radio-button value="table">
                          <el-icon><List /></el-icon> 列表
                        </el-radio-button>
                        <el-radio-button value="calendar">
                          <el-icon><Calendar /></el-icon> 日曆
                        </el-radio-button>
                      </el-radio-group>
                      <el-button @click="fetchSchedules" :loading="loading" class="ml-2">
                        <i class="fas fa-sync-alt"></i>
                      </el-button>
                    </div>
                  </div>

                  <!-- 列表視圖 -->
                  <div v-show="viewMode === 'table'">
                    <el-table
                      :data="paginatedList"
                      v-loading="loading"
                      stripe
                      style="width: 100%"
                      :header-cell-style="{ background: '#f5f7fa', fontWeight: 'bold' }"
                    >
                      <el-table-column prop="scheduleId" label="ID" width="60" align="center" />

                      <el-table-column prop="title" label="排程標題" min-width="160">
                        <template #default="{ row }">
                          <div class="title-cell">
                            <i
                              :class="scheduleTypeConfig[row.scheduleType]?.icon"
                              :style="{ color: scheduleTypeConfig[row.scheduleType]?.color }"
                              class="mr-2"
                            ></i>
                            <span>{{ row.title }}</span>
                          </div>
                        </template>
                      </el-table-column>

                      <el-table-column label="頻率" width="140" align="center">
                        <template #default="{ row }">
                          <el-tag
                            :type="scheduleTypeConfig[row.scheduleType]?.tagType"
                            size="small"
                            effect="plain"
                          >
                            {{ formatScheduleDetail(row) }}
                          </el-tag>
                        </template>
                      </el-table-column>

                      <el-table-column label="目標" width="80" align="center">
                        <template #default="{ row }">
                          <el-tag
                            :type="row.targetType === 'SPOT' ? 'primary' : 'warning'"
                            size="small"
                            effect="light"
                          >
                            {{ row.targetType === 'SPOT' ? '機台' : '椅子' }}
                          </el-tag>
                        </template>
                      </el-table-column>

                      <el-table-column label="負責人員" width="100" align="center">
                        <template #default="{ row }">
                          <el-tag v-if="row.assignedStaffId" type="success" size="small" effect="plain">
                            <i class="fas fa-user mr-1"></i>
                            {{ row.assignedStaffName || `#${row.assignedStaffId}` }}
                          </el-tag>
                          <span v-else style="color: #909399; font-size: 12px;">未指派</span>
                        </template>
                      </el-table-column>

                      <el-table-column label="下次執行" width="150">
                        <template #default="{ row }">
                          <div class="next-exec-cell">
                            <span class="datetime">{{ formatDateTime(row.nextExecuteAt) }}</span>
                            <el-tag
                              :type="getRelativeTime(row.nextExecuteAt).isOverdue ? 'danger' : getRelativeTime(row.nextExecuteAt).isSoon ? 'warning' : 'info'"
                              size="small"
                              effect="plain"
                              class="relative-tag"
                            >
                              {{ getRelativeTime(row.nextExecuteAt).text }}
                            </el-tag>
                          </div>
                        </template>
                      </el-table-column>

                      <el-table-column label="狀態" width="80" align="center">
                        <template #default="{ row }">
                          <el-switch
                            :model-value="row.isActive"
                            active-color="#67c23a"
                            inactive-color="#dcdfe6"
                            :before-change="() => handleToggleConfirm(row)"
                          />
                        </template>
                      </el-table-column>

                      <el-table-column label="操作" width="100" align="center">
                        <template #default="{ row }">
                          <el-button-group>
                            <el-button size="small" type="primary" @click="handleEdit(row)">
                              <i class="fas fa-edit"></i>
                            </el-button>
                            <el-button size="small" type="danger" @click="handleDelete(row)">
                              <i class="fas fa-trash-alt"></i>
                            </el-button>
                          </el-button-group>
                        </template>
                      </el-table-column>
                    </el-table>

                    <div class="pagination-bar">
                      <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :page-sizes="[10, 20, 50]"
                        :total="total"
                        layout="total, sizes, prev, pager, next"
                        background
                        small
                      />
                    </div>
                  </div>

                  <!-- 日曆視圖 -->
                  <div v-show="viewMode === 'calendar'">
                    <ScheduleCalendar
                      :schedules="schedules"
                      @click-schedule="handleCalendarScheduleClick"
                    />
                  </div>
                </el-card>
              </el-col>

              <!-- ========== 側邊欄：即將執行 ========== -->
              <el-col :xs="24" :lg="7">
                <el-card shadow="hover" class="timeline-card">
                  <template #header>
                    <div class="card-header">
                      <el-icon><Clock /></el-icon>
                      <span>即將執行</span>
                      <el-badge :value="upcomingSchedules.length" :max="9" class="ml-auto" />
                    </div>
                  </template>

                  <el-timeline v-if="upcomingSchedules.length > 0">
                    <el-timeline-item
                      v-for="schedule in upcomingSchedules"
                      :key="schedule.scheduleId"
                      :color="scheduleTypeConfig[schedule.scheduleType]?.color"
                      :hollow="true"
                    >
                      <div class="timeline-content" @click="handleEdit(schedule)">
                        <div class="timeline-title">{{ schedule.title }}</div>
                        <div class="timeline-meta">
                          <el-tag size="small" effect="plain">
                            {{ schedule.targetType === 'SPOT' ? '機台' : '椅子' }}
                          </el-tag>
                          <span class="timeline-time">
                            {{ formatDateTime(schedule.nextExecuteAt) }}
                          </span>
                        </div>
                      </div>
                    </el-timeline-item>
                  </el-timeline>

                  <el-empty v-else description="暫無即將執行的排程" :image-size="80" />
                </el-card>
              </el-col>
            </el-row>
          </div>
        </transition>
      </div>

      <!-- ========== 保養紀錄 Dialog ========== -->
      <el-dialog v-model="showHistoryDialog" title="📜 系統自動保養紀錄" width="70%" draggable>
        <el-table :data="historyTickets" v-loading="dialogLoading" max-height="400" stripe>
          <el-table-column prop="ticketId" label="ID" width="70" />
          <el-table-column prop="spotId" label="機台" width="80" />
          <el-table-column prop="issueDesc" label="描述" show-overflow-tooltip />
          <el-table-column prop="reportedAt" label="執行時間" width="160">
            <template #default="{ row }">{{ formatDateTime(row.reportedAt) }}</template>
          </el-table-column>
          <el-table-column prop="issueStatus" label="狀態" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.issueStatus }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- ========== 已停用 Dialog ========== -->
      <el-dialog v-model="showDeletedDialog" title="🗑️ 已停用排程" width="60%" draggable>
        <el-table :data="deletedSchedules" v-loading="dialogLoading" max-height="400" stripe>
          <el-table-column prop="scheduleId" label="ID" width="70" />
          <el-table-column prop="title" label="標題" />
          <el-table-column label="類型" width="100">
            <template #default="{ row }">
              <el-tag :type="scheduleTypeConfig[row.scheduleType]?.tagType" size="small">
                {{ scheduleTypeConfig[row.scheduleType]?.text }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="handleRestore(row)">
                <i class="fas fa-undo mr-1"></i> 復原
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </section>
  </div>
</template>

<style scoped>
.schedule-list-container {
  padding: 20px;
}

.page-title {
  font-size: 1.6rem;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  margin: 0;
}

/* ====== 統計卡片 ====== */
.stat-card {
  border-radius: 10px;
  text-align: center;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-3px);
}
.stat-total :deep(.el-statistic__head) { color: #409eff; }
.stat-upcoming :deep(.el-statistic__head) { color: #f56c6c; }
.stat-daily :deep(.el-statistic__head) { color: #67c23a; }
.stat-weekly :deep(.el-statistic__head) { color: #409eff; }
.stat-monthly :deep(.el-statistic__head) { color: #e6a23c; }

.stat-card :deep(.el-statistic__content) {
  font-size: 1.8rem;
  font-weight: 700;
}

/* ====== 主卡片 ====== */
.main-card {
  border-radius: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.title-cell {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.next-exec-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.next-exec-cell .datetime {
  font-size: 12px;
  color: #606266;
}

.next-exec-cell .relative-tag {
  width: fit-content;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* ====== Timeline 卡片 ====== */
.timeline-card {
  border-radius: 12px;
}

.timeline-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.timeline-content {
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.timeline-content:hover {
  background: #f5f7fa;
}

.timeline-title {
  font-weight: 500;
  margin-bottom: 6px;
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

/* ====== 工具類 ====== */
.mr-1 { margin-right: 4px; }
.mr-2 { margin-right: 8px; }
.ml-2 { margin-left: 8px; }
.ml-auto { margin-left: auto; }
.mb-4 { margin-bottom: 1rem; }

/* ====== 動畫 ====== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
</style>

<style>
.swal2-container { z-index: 20000 !important; }
</style>
