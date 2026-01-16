<script setup>
import { ref, onMounted, computed, reactive, watch, nextTick, onBeforeUnmount } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import ApexCharts from 'apexcharts' // 引入圖表庫

const props = defineProps({ historyMode: Boolean })
const tickets = ref([])
const filters = reactive({ keyword: '', priority: '', status: '' })
const loading = ref(false)

// 控制結案彈窗
const showResolveDialog = ref(false)
const resolveForm = reactive({ ticketId: 0, resultType: 'FIXED', resolveNote: '' })

// 圖表實體參照 (用來更新數據或銷毀圖表)
let statusChart = null
let priorityChart = null
let typeChart = null

// --- 顏色與文字定義區 ---
const getPriorityTag = (p) => {
  const map = { LOW: 'info', NORMAL: '', HIGH: 'warning', URGENT: 'danger' }
  return map[p] || 'info'
}
const getStatusTag = (s) => {
  const map = {
    REPORTED: 'info',
    ASSIGNED: 'primary',
    UNDER_MAINTENANCE: 'warning',
    RESOLVED: 'success',
    CANCELLED: 'info',
  }
  return map[s] || ''
}
const priorityText = { LOW: '低', NORMAL: '普通', HIGH: '高', URGENT: '緊急' }
const statusText = {
  REPORTED: '已通報',
  ASSIGNED: '已指派',
  UNDER_MAINTENANCE: '維修中',
  RESOLVED: '已完成',
  CANCELLED: '已取消',
}

// --- API 資料讀取 ---
const fetchTickets = async () => {
  try {
    loading.value = true
    const res = props.historyMode 
      ? await maintenanceApi.getHistoryTickets()
      : await maintenanceApi.getActiveTickets()
    tickets.value = res.data

    // 資料載入後，等待 DOM 更新完畢再繪製圖表
    nextTick(() => {
      renderCharts()
    })
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

// --- 數據統計邏輯 (前端計算) ---
const chartData = computed(() => {
  const data = filteredTickets.value // 使用篩選後的資料來畫圖，互動感更強

  // 1. 狀態統計 (Status)
  const statusCount = {}
  data.forEach((t) => {
    statusCount[t.issueStatus] = (statusCount[t.issueStatus] || 0) + 1
  })

  // 2. 優先級統計 (Priority)
  const priorityCount = { LOW: 0, NORMAL: 0, HIGH: 0, URGENT: 0 }
  data.forEach((t) => {
    if (priorityCount[t.issuePriority] !== undefined) priorityCount[t.issuePriority]++
  })

  // 3. 類型統計 (Type - 取前 5 名)
  const typeCount = {}
  data.forEach((t) => {
    typeCount[t.issueType] = (typeCount[t.issueType] || 0) + 1
  })
  // 轉成陣列並排序
  const sortedTypes = Object.entries(typeCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)

  return { statusCount, priorityCount, sortedTypes }
})

// --- 圖表繪製與更新 ---
const renderCharts = () => {
  // 如果資料是空的，就不畫圖，避免報錯
  if (tickets.value.length === 0) return

  const { statusCount, priorityCount, sortedTypes } = chartData.value

  // 1. 狀態圓餅圖 (Donut)
  const statusOptions = {
    series: Object.values(statusCount),
    labels: Object.keys(statusCount).map((k) => statusText[k] || k),
    chart: { type: 'donut', height: 250, toolbar: { show: false } }, // 隱藏工具列
    colors: ['#17a2b8', '#007bff', '#ffc107', '#28a745', '#6c757d'], // 對應 AdminLTE 顏色
    dataLabels: { enabled: false }, // 簡潔一點
    legend: { position: 'bottom' },
  }

  // 2. 優先級圓餅圖 (Pie)
  const priorityOptions = {
    series: [priorityCount.LOW, priorityCount.NORMAL, priorityCount.HIGH, priorityCount.URGENT],
    labels: ['低', '普通', '高', '緊急'],
    chart: { type: 'pie', height: 250, toolbar: { show: false } },
    colors: ['#17a2b8', '#6c757d', '#ffc107', '#dc3545'],
    legend: { position: 'bottom' },
  }

  // 3. 類型長條圖 (Bar)
  const typeOptions = {
    series: [{ name: '件數', data: sortedTypes.map((i) => i[1]) }],
    chart: { type: 'bar', height: 250, toolbar: { show: false } },
    xaxis: { categories: sortedTypes.map((i) => i[0]) },
    plotOptions: { bar: { borderRadius: 4, horizontal: true } }, // 橫向長條圖
    colors: ['#007bff'],
  }

  // 判斷是「更新」還是「新建」
  if (statusChart) {
    statusChart.updateOptions(statusOptions)
    priorityChart.updateOptions(priorityOptions)
    typeChart.updateOptions(typeOptions)
  } else {
    // 第一次建立，需要檢查 DOM 元素是否存在
    const el1 = document.querySelector('#status-chart')
    const el2 = document.querySelector('#priority-chart')
    const el3 = document.querySelector('#type-chart')

    if (el1) {
      statusChart = new ApexCharts(el1, statusOptions)
      statusChart.render()
    }
    if (el2) {
      priorityChart = new ApexCharts(el2, priorityOptions)
      priorityChart.render()
    }
    if (el3) {
      typeChart = new ApexCharts(el3, typeOptions)
      typeChart.render()
    }
  }
}

// 監聽篩選條件變化，自動更新圖表
watch(
  filters,
  () => {
    nextTick(() => renderCharts())
  },
  { deep: true },
)

// --- 其他業務邏輯 (開始、取消、結案) ---
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

const startTicket = async (id) => {
  try {
    await maintenanceApi.startTicket(id)
    Swal.fire({ icon: 'success', title: '開始維修', timer: 1000, showConfirmButton: false })
    fetchTickets()
  } catch {
    // 錯誤已由 http.js 攔截器處理
  }
}

const cancelTicket = async (id) => {
  const { value: reason } = await Swal.fire({
    title: '確認取消工單?',
    input: 'text',
    inputPlaceholder: '請輸入原因...',
    showCancelButton: true,
  })
  if (reason !== undefined) {
    try {
      await maintenanceApi.cancelTicket(id, reason)
      Swal.fire('已取消', '', 'success')
      fetchTickets()
    } catch {
      // 錯誤已由 http.js 攔截器處理
    }
  }
}

const openResolveDialog = (id) => {
  resolveForm.ticketId = id
  resolveForm.resultType = 'FIXED'
  resolveForm.resolveNote = ''
  showResolveDialog.value = true
}

const submitResolve = async () => {
  try {
    await maintenanceApi.resolveTicket(
      resolveForm.ticketId,
      resolveForm.resultType,
      resolveForm.resolveNote
    )
    showResolveDialog.value = false
    Swal.fire('已結案', '', 'success')
    fetchTickets()
  } catch {
    // 錯誤已由 http.js 攔截器處理
  }
}

// 切換模式時重新抓資料
watch(
  () => props.historyMode,
  () => {
    // 切換時銷毀舊圖表，避免 ID 衝突或殘留
    if (statusChart) {
      statusChart.destroy()
      statusChart = null
    }
    if (priorityChart) {
      priorityChart.destroy()
      priorityChart = null
    }
    if (typeChart) {
      typeChart.destroy()
      typeChart = null
    }
    fetchTickets()
  },
)

onMounted(() => fetchTickets())
onBeforeUnmount(() => {
  // 組件卸載前清理圖表，防止記憶體洩漏
  if (statusChart) statusChart.destroy()
  if (priorityChart) priorityChart.destroy()
  if (typeChart) typeChart.destroy()
})
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>
              <i class="fas fa-chart-line mr-2"></i
              >{{ historyMode ? '維修歷史檔案' : '維修工單管理' }}
            </h1>
          </div>
          <div class="col-sm-6 text-right">
            <el-button-group>
              <router-link to="/admin/mtif-list" v-if="historyMode">
                <el-button type="info" plain icon="ArrowLeft">返回列表</el-button>
              </router-link>
              <router-link to="/admin/mtif-history" v-if="!historyMode">
                <el-button type="info" plain icon="Clock">歷史紀錄</el-button>
              </router-link>
              <router-link to="/admin/mtif-form" v-if="!historyMode">
                <el-button type="success" icon="Plus">新增工單</el-button>
              </router-link>
            </el-button-group>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <el-row :gutter="20" class="mb-4" v-if="tickets.length > 0">
          <el-col :md="8" :sm="24">
            <el-card shadow="hover" class="chart-card">
              <template #header
                ><b><i class="fas fa-chart-pie mr-1"></i> 狀態分佈</b></template
              >
              <div id="status-chart"></div>
            </el-card>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-card shadow="hover" class="chart-card">
              <template #header
                ><b><i class="fas fa-exclamation-triangle mr-1"></i> 優先級佔比</b></template
              >
              <div id="priority-chart"></div>
            </el-card>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-card shadow="hover" class="chart-card">
              <template #header
                ><b><i class="fas fa-tools mr-1"></i> 前五大故障類型</b></template
              >
              <div id="type-chart"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never">
          <div class="d-flex flex-wrap mb-3 gap-2">
            <el-input
              v-model="filters.keyword"
              placeholder="搜尋 ID、描述、類型"
              style="width: 250px"
              prefix-icon="Search"
              clearable
            />
            <el-select
              v-model="filters.priority"
              placeholder="優先級"
              clearable
              style="width: 120px"
            >
              <el-option label="低" value="LOW" /><el-option
                label="普通"
                value="NORMAL"
              /><el-option label="高" value="HIGH" /><el-option label="緊急" value="URGENT" />
            </el-select>
            <el-select v-model="filters.status" placeholder="狀態" clearable style="width: 120px">
              <el-option v-for="(val, key) in statusText" :key="key" :label="val" :value="key" />
            </el-select>
          </div>

          <el-table
            :data="filteredTickets"
            v-loading="loading"
            stripe
            style="width: 100%"
            height="500"
          >
            <el-table-column prop="ticketId" label="ID" width="70" sortable fixed />
            <el-table-column prop="spotId" label="場地" width="80" align="center" />
            <el-table-column prop="issueType" label="類型" width="120" show-overflow-tooltip />
            <el-table-column prop="issueDesc" label="描述" min-width="200" show-overflow-tooltip />

            <el-table-column prop="issuePriority" label="優先級" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityTag(row.issuePriority)" effect="light" round>{{
                  priorityText[row.issuePriority]
                }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="issueStatus" label="狀態" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.issueStatus)" effect="dark">{{
                  statusText[row.issueStatus]
                }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column
              v-if="!historyMode"
              label="操作"
              width="180"
              align="center"
              fixed="right"
            >
              <template #default="{ row }">
                <el-button-group>
                  <el-button
                    circle
                    size="small"
                    icon="Edit"
                    @click="$router.push(`/admin/mtif-form/${row.ticketId}`)"
                    title="編輯"
                  />

                  <el-button
                    v-if="row.issueStatus === 'ASSIGNED'"
                    type="primary"
                    size="small"
                    @click="startTicket(row.ticketId)"
                    title="開始維修"
                  >
                    <i class="fas fa-play"></i>
                  </el-button>

                  <el-button
                    v-if="row.issueStatus === 'UNDER_MAINTENANCE'"
                    type="success"
                    size="small"
                    @click="openResolveDialog(row.ticketId)"
                    title="結案"
                  >
                    <i class="fas fa-check"></i>
                  </el-button>

                  <el-button
                    v-if="!['RESOLVED', 'CANCELLED'].includes(row.issueStatus)"
                    type="danger"
                    size="small"
                    circle
                    icon="Close"
                    @click="cancelTicket(row.ticketId)"
                    title="取消"
                  />
                </el-button-group>
              </template>
            </el-table-column>

            <template #empty>
              <el-empty description="目前沒有相關工單資料" />
            </template>
          </el-table>
        </el-card>
      </div>
    </section>

    <el-dialog
      v-model="showResolveDialog"
      title="工單結案確認"
      width="500px"
      center
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="維修結果"
          ><el-select v-model="resolveForm.resultType" class="w-100"
            ><el-option label="維修成功 (Fixed)" value="FIXED" /><el-option
              label="無法修復"
              value="NOT_FIXABLE" /><el-option label="無問題" value="NO_ISSUE" /><el-option
              label="其他"
              value="OTHER" /></el-select
        ></el-form-item>
        <el-form-item label="維修備註"
          ><el-input
            v-model="resolveForm.resolveNote"
            type="textarea"
            :rows="3"
            placeholder="請填寫維修說明..."
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="showResolveDialog = false">取消</el-button
        ><el-button type="primary" @click="submitResolve">確認結案</el-button></template
      >
    </el-dialog>
  </div>
</template>

<style scoped>
.content-header {
  padding: 15px 0.5rem;
}
.w-100 {
  width: 100%;
}
.gap-2 {
  gap: 0.5rem;
}
/* 讓圖表卡片高度一致 */
.chart-card {
  min-height: 320px;
}
</style>
