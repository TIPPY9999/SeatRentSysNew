<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import axios from 'axios'

const props = defineProps({ historyMode: Boolean })
const tickets = ref([])
const filters = reactive({ keyword: '', priority: '', status: '' })
const loading = ref(false)
const showResolveModal = ref(false)
const resolveForm = reactive({ ticketId: 0, resultType: 'FIXED', resolveNote: '' })

// 移除 TypeScript 型別標註
const priorityMap = {
  LOW: '低',
  NORMAL: '普通',
  HIGH: '高',
  URGENT: '緊急',
}

const statusMap = {
  REPORTED: '已通報',
  ASSIGNED: '已指派',
  UNDER_MAINTENANCE: '維修中',
  RESOLVED: '已完成',
  CANCELLED: '已取消',
}

const fetchTickets = async () => {
  const endpoint = props.historyMode ? 'history' : 'active'
  try {
    loading.value = true
    const res = await axios.get(`http://localhost:8080/api/maintenance/tickets/${endpoint}`)
    tickets.value = res.data
  } catch {
    // 已修正：移除未使用的 e 變數
    alert('無法取得工單資料')
  } finally {
    loading.value = false
  }
}

const stats = computed(() => ({
  total: tickets.value.length,
  under: tickets.value.filter((t) => t.issueStatus === 'UNDER_MAINTENANCE').length,
  done: tickets.value.filter((t) => t.issueStatus === 'RESOLVED').length,
}))

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
    await axios.post(`http://localhost:8080/api/maintenance/tickets/${id}/start`)
    fetchTickets()
  } catch {
    alert('操作失敗：請確認已指派人員且狀態正確')
  }
}

// ✅ 修正警告：此函式現在已正確綁定到下方模板的按鈕上
const cancelTicket = async (id) => {
  if (!confirm('確認取消此工單?')) return
  const reason = prompt('請輸入取消原因 (選填):') || ''
  try {
    await axios.post(`http://localhost:8080/api/maintenance/tickets/${id}/cancel`, { reason })
    fetchTickets()
  } catch {
    alert('取消失敗')
  }
}

const openResolveModal = (id) => {
  resolveForm.ticketId = id
  resolveForm.resultType = 'FIXED'
  resolveForm.resolveNote = ''
  showResolveModal.value = true
}

const submitResolve = async () => {
  try {
    await axios.post(
      `http://localhost:8080/api/maintenance/tickets/${resolveForm.ticketId}/resolve`,
      resolveForm,
    )
    showResolveModal.value = false
    fetchTickets()
  } catch {
    alert('結案失敗')
  }
}

watch(
  () => props.historyMode,
  () => fetchTickets(),
)

onMounted(() => fetchTickets())
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>{{ historyMode ? '維修工單歷史紀錄' : '維修工單列表' }}</h1>
          </div>
          <div class="col-sm-6 text-right">
            <router-link
              v-if="!historyMode"
              to="/admin/mtif-history"
              class="btn btn-outline-info btn-sm"
            >
              <i class="fas fa-history mr-1"></i> 查看歷史
            </router-link>
            <router-link
              v-if="historyMode"
              to="/admin/mtif-list"
              class="btn btn-outline-secondary btn-sm"
            >
              <i class="fas fa-arrow-left mr-1"></i> 返回列表
            </router-link>
            <router-link
              v-if="!historyMode"
              to="/admin/mtif-form"
              class="btn btn-success btn-sm ml-2"
            >
              <i class="fas fa-plus mr-1"></i> 新增工單
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row mb-3">
          <div class="col-md-4">
            <div class="small-box bg-info">
              <div class="inner">
                <h3>{{ stats.total }}</h3>
                <p>工單總數</p>
              </div>
              <div class="icon"><i class="fas fa-clipboard-list"></i></div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="small-box bg-warning">
              <div class="inner">
                <h3>{{ stats.under }}</h3>
                <p>維修中</p>
              </div>
              <div class="icon"><i class="fas fa-tools"></i></div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="small-box bg-success">
              <div class="inner">
                <h3>{{ stats.done }}</h3>
                <p>已完成</p>
              </div>
              <div class="icon"><i class="fas fa-check"></i></div>
            </div>
          </div>
        </div>

        <div class="card shadow-sm">
          <div class="card-header bg-light d-flex align-items-center">
            <input
              v-model="filters.keyword"
              class="form-control form-control-sm mr-2 w-25"
              placeholder="搜尋ID / 描述"
            />
            <select v-model="filters.priority" class="form-control form-control-sm mr-2 w-auto">
              <option value="">全部優先級</option>
              <option value="LOW">低</option>
              <option value="NORMAL">普通</option>
              <option value="HIGH">高</option>
              <option value="URGENT">緊急</option>
            </select>
          </div>

          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-sm table-hover mb-0">
                <thead class="thead-light">
                  <tr>
                    <th>ID</th>
                    <th>場地</th>
                    <th>描述</th>
                    <th>優先級</th>
                    <th>狀態</th>
                    <th v-if="!historyMode">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in filteredTickets" :key="t.ticketId">
                    <td class="font-weight-bold">{{ t.ticketId }}</td>
                    <td>{{ t.spotId }}</td>
                    <td>{{ t.issueDesc }}</td>
                    <td>
                      <span :class="['badge', `priority-${(t.issuePriority || '').toLowerCase()}`]">
                        {{ priorityMap[t.issuePriority] || t.issuePriority }}
                      </span>
                    </td>
                    <td>
                      <span :class="['badge', `status-${(t.issueStatus || '').toLowerCase()}`]">
                        {{ statusMap[t.issueStatus] || t.issueStatus }}
                      </span>
                    </td>
                    <td v-if="!historyMode">
                      <router-link
                        :to="`/admin/mtif-form/${t.ticketId}`"
                        class="btn btn-outline-primary btn-xs mr-1"
                        >編輯</router-link
                      >
                      <button
                        v-if="t.issueStatus === 'ASSIGNED'"
                        @click="startTicket(t.ticketId)"
                        class="btn btn-outline-secondary btn-xs mr-1"
                      >
                        開始
                      </button>
                      <button
                        v-if="t.issueStatus === 'UNDER_MAINTENANCE'"
                        @click="openResolveModal(t.ticketId)"
                        class="btn btn-outline-success btn-xs mr-1"
                      >
                        結案
                      </button>
                      <button
                        v-if="t.issueStatus !== 'RESOLVED' && t.issueStatus !== 'CANCELLED'"
                        @click="cancelTicket(t.ticketId)"
                        class="btn btn-outline-danger btn-xs"
                      >
                        取消
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div
      v-if="showResolveModal"
      class="modal d-block"
      style="background: rgba(0, 0, 0, 0.5); z-index: 1050"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content shadow">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">工單結案確認</h5>
            <button @click="showResolveModal = false" class="close text-white">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>維修結果</label>
              <select v-model="resolveForm.resultType" class="form-control">
                <option value="FIXED">維修成功</option>
                <option value="NOT_FIXABLE">無法修復</option>
              </select>
            </div>
            <div class="form-group">
              <label>備註</label>
              <textarea v-model="resolveForm.resolveNote" class="form-control" rows="3"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="submitResolve" class="btn btn-success">確定結案</button>
            <button @click="showResolveModal = false" class="btn btn-secondary">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.priority-low {
  background-color: #f8f9fa;
  color: #6c757d;
}
.priority-normal {
  background-color: #d1ecf1;
  color: #0c5460;
}
.priority-high {
  background-color: #fff3cd;
  color: #856404;
}
.priority-urgent {
  background-color: #f8d7da;
  color: #721c24;
}
.btn-xs {
  padding: 1px 5px;
  font-size: 12px;
}
</style>
