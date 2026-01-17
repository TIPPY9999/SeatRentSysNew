<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useTicketConfig } from '@/composables/maintenance/useTicketConfig'
import { usePagination } from '@/composables/maintenance/usePagination'
import TicketCharts from '@/components/maintenance/TicketCharts.vue'

const props = defineProps({ historyMode: Boolean })
const tickets = ref([])
const filters = reactive({ keyword: '', priority: '', status: '' })
const loading = ref(false)
const pageVisible = ref(false)

// 控制結案彈窗
const showResolveDialog = ref(false)
const resolveForm = reactive({ ticketId: 0, resultType: 'FIXED', resolveNote: '' })

// 使用共用 composables
const { priorityConfig, statusConfig, getPriorityTag, getStatusTag } = useTicketConfig()

// 向後相容：保留原有變數名稱 (供模板使用)
const priorityText = Object.fromEntries(Object.entries(priorityConfig).map(([k, v]) => [k, v.text]))
const priorityIcon = Object.fromEntries(Object.entries(priorityConfig).map(([k, v]) => [k, v.icon]))
const statusText = Object.fromEntries(Object.entries(statusConfig).map(([k, v]) => [k, v.text]))
const statusIcon = Object.fromEntries(Object.entries(statusConfig).map(([k, v]) => [k, v.icon]))

// --- API 資料讀取 ---
const fetchTickets = async () => {
  try {
    loading.value = true
    const res = props.historyMode
      ? await maintenanceApi.getHistoryTickets()
      : await maintenanceApi.getActiveTickets()
    tickets.value = res.data
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

// 統計卡片數據
const statsCards = computed(() => {
  const total = tickets.value.length
  const urgent = tickets.value.filter((t) => t.issuePriority === 'URGENT').length
  const inProgress = tickets.value.filter((t) => t.issueStatus === 'UNDER_MAINTENANCE').length
  const resolved = tickets.value.filter((t) => t.issueStatus === 'RESOLVED').length
  return { total, urgent, inProgress, resolved }
})

// 監聽篩選條件變更，重置分頁
watch(
  filters,
  () => {
    resetPagination()
  },
  { deep: true },
)

// --- 業務邏輯 ---
const filteredTickets = computed(() => {
  return tickets.value.filter((t) => {
    const k = filters.keyword.toLowerCase()
    const textMatch =
      !k ||
      String(t.ticketId).includes(k) ||
      (t.issueDesc || '').toLowerCase().includes(k) ||
      (t.issueType || '').toLowerCase().includes(k)
    const pMatch = !filters.priority || t.issuePriority === filters.priority
    const sMatch = !filters.status || t.issueStatus === filters.status
    return textMatch && pMatch && sMatch
  })
})

// 使用 usePagination composable
const {
  currentPage,
  pageSize,
  paginatedList: paginatedTickets,
  total: paginationTotal,
  showPagination,
  resetPagination,
} = usePagination(filteredTickets, { defaultPageSize: 10 })

// 開始維修
const startTicket = async (row) => {
  const result = await Swal.fire({
    title: '開始維修？',
    html: `
      <div style="text-align: center; padding: 10px 0;">
        <div style="font-size: 48px; margin-bottom: 12px;">🔧</div>
        <p>工單 <b>#${row.ticketId}</b> 即將進入維修狀態</p>
        <p style="color: #909399; font-size: 13px;">問題類型：${row.issueType}</p>
      </div>
    `,
    icon: null,
    showCancelButton: true,
    confirmButtonColor: '#409eff',
    cancelButtonColor: '#909399',
    confirmButtonText: '<i class="fas fa-play mr-1"></i> 開始維修',
    cancelButtonText: '稍後再說',
    showClass: { popup: 'animate__animated animate__bounceIn' },
  })

  if (result.isConfirmed) {
    try {
      await maintenanceApi.startTicket(row.ticketId)
      await Swal.fire({
        icon: 'success',
        title: '維修開始！',
        html: '<span class="text-primary">工單狀態已更新為「維修中」</span>',
        timer: 1500,
        timerProgressBar: true,
        showConfirmButton: false,
        showClass: { popup: 'animate__animated animate__fadeInUp animate__faster' },
      })
      fetchTickets()
    } catch {
      // 錯誤已由攔截器處理
    }
  }
}

// 取消工單
const cancelTicket = async (row) => {
  const { value: reason } = await Swal.fire({
    title: '取消工單',
    html: `
      <div style="text-align: center; padding: 10px 0;">
        <div style="font-size: 48px; margin-bottom: 12px;">⚠️</div>
        <p style="margin-bottom: 16px;">工單 <b>#${row.ticketId}</b> - ${row.issueType}</p>
      </div>
    `,
    input: 'textarea',
    inputPlaceholder: '請輸入取消原因...',
    inputAttributes: { rows: 3 },
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    cancelButtonColor: '#909399',
    confirmButtonText: '<i class="fas fa-times mr-1"></i> 確認取消',
    cancelButtonText: '返回',
    showClass: { popup: 'animate__animated animate__fadeInDown animate__faster' },
    inputValidator: (value) => {
      if (!value) return '請輸入取消原因！'
    },
  })

  if (reason) {
    try {
      await maintenanceApi.cancelTicket(row.ticketId, reason)
      await Swal.fire({
        icon: 'success',
        title: '工單已取消',
        timer: 1500,
        timerProgressBar: true,
        showConfirmButton: false,
        showClass: { popup: 'animate__animated animate__bounceIn' },
      })
      fetchTickets()
    } catch {
      // 錯誤已由攔截器處理
    }
  }
}

// 開啟結案彈窗
const openResolveDialog = (id) => {
  resolveForm.ticketId = id
  resolveForm.resultType = 'FIXED'
  resolveForm.resolveNote = ''
  showResolveDialog.value = true
}

// 送出結案
const submitResolve = async () => {
  const resultConfig = {
    FIXED: { icon: '✅', text: '維修成功', color: '#67c23a' },
    NOT_FIXABLE: { icon: '❌', text: '無法修復', color: '#f56c6c' },
    NO_ISSUE: { icon: '✔️', text: '無問題', color: '#909399' },
    OTHER: { icon: '📋', text: '其他', color: '#e6a23c' },
  }

  try {
    await maintenanceApi.resolveTicket(
      resolveForm.ticketId,
      resolveForm.resultType,
      resolveForm.resolveNote,
    )
    showResolveDialog.value = false

    const config = resultConfig[resolveForm.resultType]
    await Swal.fire({
      icon: 'success',
      title: '🎉 工單結案成功！',
      html: `
        <div style="text-align: center;">
          <div style="font-size: 48px; margin-bottom: 12px;">${config.icon}</div>
          <p>結案結果：<b style="color: ${config.color};">${config.text}</b></p>
        </div>
      `,
      timer: 2000,
      timerProgressBar: true,
      showConfirmButton: false,
      showClass: { popup: 'animate__animated animate__tada' },
    })
    fetchTickets()
  } catch {
    // 錯誤已由攔截器處理
  }
}

// 查看工單詳情
const viewTicketDetail = (row) => {
  Swal.fire({
    title: `<span style="font-size: 18px;">工單 #${row.ticketId}</span>`,
    html: `
      <div style="text-align: left; padding: 16px 0;">
        <div style="display: grid; gap: 12px;">
          <div style="padding: 14px; background: #f5f7fa; border-radius: 10px;">
            <p style="margin: 0 0 4px; color: #909399; font-size: 12px;">問題類型</p>
            <p style="margin: 0; font-size: 16px; font-weight: 600;">${row.issueType}</p>
          </div>
          <div style="padding: 14px; background: #fef0f0; border-radius: 10px; border-left: 4px solid #f56c6c;">
            <p style="margin: 0 0 4px; color: #909399; font-size: 12px;">問題描述</p>
            <p style="margin: 0; font-size: 14px; color: #606266;">${row.issueDesc || '無詳細描述'}</p>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div style="padding: 14px; background: #ecf5ff; border-radius: 10px; text-align: center;">
              <p style="margin: 0 0 4px; color: #909399; font-size: 12px;">優先級</p>
              <p style="margin: 0; font-size: 20px;">${priorityIcon[row.issuePriority]} ${priorityText[row.issuePriority]}</p>
            </div>
            <div style="padding: 14px; background: #f0f9eb; border-radius: 10px; text-align: center;">
              <p style="margin: 0 0 4px; color: #909399; font-size: 12px;">狀態</p>
              <p style="margin: 0; font-size: 20px;">${statusIcon[row.issueStatus]} ${statusText[row.issueStatus]}</p>
            </div>
          </div>
        </div>
      </div>
    `,
    confirmButtonText: '關閉',
    confirmButtonColor: '#909399',
    showClass: { popup: 'animate__animated animate__zoomIn animate__faster' },
    hideClass: { popup: 'animate__animated animate__zoomOut animate__faster' },
    width: 480,
  })
}

// 切換模式時重新抓資料
// [修正] 移除舊的 destroy 邏輯，只負責重抓資料
watch(
  () => props.historyMode,
  () => {
    fetchTickets()
  },
)

onMounted(() => {
  fetchTickets()
  setTimeout(() => (pageVisible.value = true), 100)
})

// [修正] 移除 onBeforeUnmount，因為圖表銷毀已交由 TicketCharts 元件處理
</script>

<template>
  <div class="ticket-list-container">
    <section class="content-header">
      <div class="container-fluid">
        <transition name="slide-down" appear>
          <div class="page-title-box">
            <div class="title-icon" :class="historyMode ? 'history-mode' : 'active-mode'">
              <i :class="historyMode ? 'fas fa-archive' : 'fas fa-clipboard-list'"></i>
            </div>
            <div class="title-content">
              <h1>{{ historyMode ? '維修歷史檔案' : '維修工單管理' }}</h1>
              <p class="subtitle">
                {{ historyMode ? '查看已完成或取消的工單紀錄' : '管理與追蹤所有維修工單' }}
              </p>
            </div>
            <div class="title-actions">
              <el-button-group>
                <router-link v-if="historyMode" to="/admin/mtif-list">
                  <el-button type="primary" plain class="action-btn">
                    <i class="fas fa-arrow-left mr-2"></i> 返回列表
                  </el-button>
                </router-link>
                <router-link v-if="!historyMode" to="/admin/mtif-history">
                  <el-button type="info" plain class="action-btn">
                    <i class="fas fa-history mr-2"></i> 歷史紀錄
                  </el-button>
                </router-link>
                <router-link v-if="!historyMode" to="/admin/mtif-form">
                  <el-button type="success" class="action-btn add-btn">
                    <i class="fas fa-plus mr-2"></i> 新增工單
                  </el-button>
                </router-link>
              </el-button-group>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <transition name="zoom-fade" appear>
          <div v-show="pageVisible">
            <el-row :gutter="16" class="mb-4" v-if="!historyMode">
              <el-col :xs="12" :sm="6" :md="6">
                <div class="stat-card total-card">
                  <div class="stat-icon">
                    <i class="fas fa-clipboard-list"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ statsCards.total }}</h3>
                    <span>全部工單</span>
                  </div>
                  <div class="stat-wave"></div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="6" :md="6">
                <div class="stat-card urgent-card">
                  <div class="stat-icon pulse">
                    <i class="fas fa-exclamation-triangle"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ statsCards.urgent }}</h3>
                    <span>緊急工單</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="6" :md="6">
                <div class="stat-card progress-card">
                  <div class="stat-icon">
                    <i class="fas fa-tools"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ statsCards.inProgress }}</h3>
                    <span>維修中</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="6" :md="6">
                <div class="stat-card resolved-card">
                  <div class="stat-icon">
                    <i class="fas fa-check-circle"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ statsCards.resolved }}</h3>
                    <span>已完成</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <TicketCharts :tickets="filteredTickets" class="mb-4" />

            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="card-header-content">
                  <div class="header-left">
                    <span class="header-icon">
                      <i class="fas fa-table"></i>
                    </span>
                    <span class="header-text">工單列表</span>
                    <el-tag type="primary" effect="light" size="small" class="ml-2" round>
                      {{ filteredTickets.length }} 筆
                    </el-tag>
                  </div>
                </div>
              </template>

              <div class="filter-bar">
                <el-input
                  v-model="filters.keyword"
                  placeholder="搜尋 ID、描述、類型..."
                  prefix-icon="Search"
                  clearable
                  class="filter-input"
                />
                <el-select
                  v-model="filters.priority"
                  placeholder="優先級"
                  clearable
                  class="filter-select"
                >
                  <el-option label="🔵 低" value="LOW" />
                  <el-option label="🟢 普通" value="NORMAL" />
                  <el-option label="🟠 高" value="HIGH" />
                  <el-option label="🔴 緊急" value="URGENT" />
                </el-select>
                <el-select
                  v-model="filters.status"
                  placeholder="狀態"
                  clearable
                  class="filter-select"
                >
                  <el-option
                    v-for="(val, key) in statusText"
                    :key="key"
                    :label="`${statusIcon[key]} ${val}`"
                    :value="key"
                  />
                </el-select>
                <el-button type="info" plain @click="fetchTickets" class="refresh-btn">
                  <i class="fas fa-sync-alt"></i>
                </el-button>
              </div>

              <el-skeleton :rows="8" animated v-if="loading" />

              <el-table
                v-else
                :data="paginatedTickets"
                stripe
                highlight-current-row
                style="width: 100%"
                class="custom-table"
                @row-dblclick="viewTicketDetail"
              >
                <el-table-column prop="ticketId" label="ID" width="80" sortable fixed>
                  <template #default="{ row }">
                    <el-tag effect="plain" size="small" class="id-tag">#{{ row.ticketId }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="spotId" label="場地" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag type="info" effect="light" size="small" round>
                      {{ row.spotId }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="issueType" label="問題類型" width="140">
                  <template #default="{ row }">
                    <div class="type-cell" @click="viewTicketDetail(row)">
                      <i class="fas fa-exclamation-circle type-icon"></i>
                      <span>{{ row.issueType }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="issueDesc"
                  label="描述"
                  min-width="200"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span class="desc-cell">{{ row.issueDesc || '-' }}</span>
                  </template>
                </el-table-column>

                <el-table-column prop="issuePriority" label="優先級" width="110" align="center">
                  <template #default="{ row }">
                    <el-tag
                      :type="getPriorityTag(row.issuePriority)"
                      effect="dark"
                      round
                      class="priority-tag"
                    >
                      {{ priorityIcon[row.issuePriority] }} {{ priorityText[row.issuePriority] }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="issueStatus" label="狀態" width="130" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTag(row.issueStatus)" effect="light" class="status-tag">
                      {{ statusIcon[row.issueStatus] }} {{ statusText[row.issueStatus] }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column
                  v-if="!historyMode"
                  label="操作"
                  width="200"
                  align="center"
                  fixed="right"
                >
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-tooltip content="查看詳情" placement="top">
                        <el-button
                          type="info"
                          size="small"
                          circle
                          @click="viewTicketDetail(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-eye"></i>
                        </el-button>
                      </el-tooltip>

                      <el-tooltip content="編輯工單" placement="top">
                        <el-button
                          size="small"
                          circle
                          @click="$router.push(`/admin/mtif-form/${row.ticketId}`)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-edit"></i>
                        </el-button>
                      </el-tooltip>

                      <el-tooltip
                        v-if="row.issueStatus === 'ASSIGNED'"
                        content="開始維修"
                        placement="top"
                      >
                        <el-button
                          type="primary"
                          size="small"
                          circle
                          @click="startTicket(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-play"></i>
                        </el-button>
                      </el-tooltip>

                      <el-tooltip
                        v-if="row.issueStatus === 'UNDER_MAINTENANCE'"
                        content="結案"
                        placement="top"
                      >
                        <el-button
                          type="success"
                          size="small"
                          circle
                          @click="openResolveDialog(row.ticketId)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-check"></i>
                        </el-button>
                      </el-tooltip>

                      <el-tooltip
                        v-if="!['RESOLVED', 'CANCELLED'].includes(row.issueStatus)"
                        content="取消工單"
                        placement="top"
                      >
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click="cancelTicket(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-times"></i>
                        </el-button>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>

                <template #empty>
                  <el-empty description="目前沒有相關工單資料">
                    <template #image>
                      <div class="empty-icon">
                        <i class="fas fa-clipboard"></i>
                      </div>
                    </template>
                    <router-link to="/admin/mtif-form" v-if="!historyMode">
                      <el-button type="primary">
                        <i class="fas fa-plus mr-1"></i> 建立第一張工單
                      </el-button>
                    </router-link>
                  </el-empty>
                </template>
              </el-table>

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

            <div class="tips-bar mt-3">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>💡 小提示：雙擊表格列可快速查看工單詳情 | 緊急工單會優先顯示紅色標記</span>
                </template>
              </el-alert>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <el-dialog
      v-model="showResolveDialog"
      title=""
      width="500px"
      center
      destroy-on-close
      class="resolve-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-icon">✅</span>
          <span class="dialog-title">工單結案確認</span>
        </div>
      </template>

      <el-form label-position="top" class="resolve-form">
        <el-form-item label="維修結果">
          <div class="result-cards">
            <div
              v-for="(config, key) in {
                FIXED: { icon: '✅', text: '維修成功', color: '#67c23a' },
                NOT_FIXABLE: { icon: '❌', text: '無法修復', color: '#f56c6c' },
                NO_ISSUE: { icon: '✔️', text: '無問題', color: '#909399' },
                OTHER: { icon: '📋', text: '其他', color: '#e6a23c' },
              }"
              :key="key"
              class="result-card"
              :class="{ active: resolveForm.resultType === key }"
              :style="{ '--card-color': config.color }"
              @click="resolveForm.resultType = key"
            >
              <span class="result-icon">{{ config.icon }}</span>
              <span class="result-text">{{ config.text }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="維修備註">
          <el-input
            v-model="resolveForm.resolveNote"
            type="textarea"
            :rows="4"
            placeholder="請填寫維修過程說明、更換零件等資訊..."
            show-word-limit
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showResolveDialog = false" size="large">取消</el-button>
        <el-button type="primary" @click="submitResolve" size="large" class="confirm-btn">
          <i class="fas fa-check mr-1"></i> 確認結案
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.ticket-list-container {
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
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  transition: all 0.4s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.title-icon:hover {
  transform: scale(1.1) rotate(10deg);
}

.title-icon.active-mode {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}

.title-icon.history-mode {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
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
  gap: 14px;
  padding: 18px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  z-index: 1;
}

.stat-icon.pulse {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}
.urgent-card .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}
.progress-card .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
}
.resolved-card .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.stat-info h3 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #303133;
}

.stat-info span {
  font-size: 0.85rem;
  color: #909399;
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

/* 篩選區 */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.filter-input {
  width: 280px;
}

.filter-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.filter-select {
  width: 140px;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.refresh-btn {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: rotate(180deg);
}

/* 表格樣式 */
.custom-table {
  --el-table-header-bg-color: #f8f9fa;
}

.id-tag {
  font-weight: 600;
}

.type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.type-cell:hover {
  background: #ecf5ff;
}

.type-cell .type-icon {
  color: #e6a23c;
}

.desc-cell {
  color: #606266;
  font-size: 13px;
}

.priority-tag,
.status-tag {
  font-weight: 500;
}

/* 操作按鈕 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
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

/* 分頁 */
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

/* 結案彈窗 */
.resolve-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.dialog-icon {
  font-size: 28px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.resolve-form {
  padding: 10px 0;
}

.result-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.result-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 10px;
  background: #f5f7fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.result-card:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.result-card.active {
  border-color: var(--card-color);
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.result-text {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.confirm-btn {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border: none;
  border-radius: 10px;
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
.w-100 {
  width: 100%;
}
</style>
