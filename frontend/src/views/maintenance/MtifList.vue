<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue'
import axios from 'axios'

const props = defineProps({ historyMode: Boolean })
const tickets = ref<any[]>([])
const filters = reactive({ keyword: '', priority: '', status: '' })
const loading = ref(false)
const showResolveModal = ref(false)
const resolveForm = reactive({ ticketId: 0, resultType: 'FIXED', resolveNote: '' })

// 定義優先級中文對照表
const priorityMap: Record<string, string> = {
  LOW: '低',
  NORMAL: '普通',
  HIGH: '高',
  URGENT: '緊急',
}

// 定義狀態中文對照表
const statusMap: Record<string, string> = {
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
  } catch (e) {
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

const startTicket = async (id: number) => {
  try {
    await axios.post(`http://localhost:8080/api/maintenance/tickets/${id}/start`)
    fetchTickets()
  } catch (e) {
    alert('操作失敗：請確認已指派人員且狀態正確')
  }
}

const cancelTicket = async (id: number) => {
  if (!confirm('確認取消此工單?')) return
  const reason = prompt('請輸入取消原因 (選填):') || ''
  await axios.post(`http://localhost:8080/api/maintenance/tickets/${id}/cancel`, { reason })
  fetchTickets()
}

const openResolveModal = (id: number) => {
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
  } catch (e) {
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
        <div class="row">
          <div class="col-sm-6">
            <h1>{{ historyMode ? '維修工單歷史紀錄' : '維修工單列表' }}</h1>
          </div>
          <div class="col-sm-6 text-right">
            <router-link v-if="!historyMode" to="/mtif-history" class="btn btn-outline-info btn-sm"
              >查看歷史</router-link
            >
            <router-link v-if="historyMode" to="/mtif-list" class="btn btn-outline-info btn-sm"
              >返回列表</router-link
            >
            <router-link v-if="!historyMode" to="/mtif-form" class="btn btn-success btn-sm ml-2"
              >新增工單</router-link
            >
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

        <div class="card">
          <div class="card-header bg-light d-flex align-items-center">
            <input
              v-model="filters.keyword"
              class="form-control form-control-sm mr-2 w-25"
              placeholder="搜尋ID / 描述 / 類型"
            />
            <select v-model="filters.priority" class="form-control form-control-sm mr-2 w-auto">
              <option value="">全部優先級</option>
              <option value="LOW">低</option>
              <option value="NORMAL">普通</option>
              <option value="HIGH">高</option>
              <option value="URGENT">緊急</option>
            </select>
            <select v-model="filters.status" class="form-control form-control-sm w-auto">
              <option value="">全部狀態</option>
              <template v-if="!historyMode">
                <option value="REPORTED">已通報</option>
                <option value="ASSIGNED">已指派</option>
                <option value="UNDER_MAINTENANCE">維修中</option>
              </template>
              <template v-else
                ><option value="RESOLVED">已完成</option>
                <option value="CANCELLED">已取消</option></template
              >
            </select>
          </div>
          <div class="card-body p-0">
            <table class="table table-sm jy-table table-hover mb-0">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>場地</th>
                  <th>類型</th>
                  <th>描述</th>
                  <th>優先級</th>
                  <th>狀態</th>
                  <th>指派ID</th>
                  <th v-if="!historyMode">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in filteredTickets" :key="t.ticketId">
                  <td>{{ t.ticketId }}</td>
                  <td>{{ t.spotId }}</td>
                  <td>{{ t.issueType }}</td>
                  <td>
                    <div class="issue-desc">{{ t.issueDesc }}</div>
                  </td>

                  <td>
                    <span :class="['badge', `priority-${t.issuePriority.toLowerCase()}`]">
                      {{ priorityMap[t.issuePriority] || t.issuePriority }}
                    </span>
                  </td>

                  <td>
                    <span :class="['badge', `status-${t.issueStatus.toLowerCase()}`]">
                      {{ statusMap[t.issueStatus] || t.issueStatus }}
                    </span>
                  </td>

                  <td>{{ t.assignedStaffId || '-' }}</td>

                  <td v-if="!historyMode">
                    <router-link
                      :to="`/mtif-form/${t.ticketId}`"
                      class="btn btn-outline-primary btn-sm mr-1"
                      >編輯</router-link
                    >

                    <button
                      v-if="
                        (t.issueStatus === 'REPORTED' || t.issueStatus === 'ASSIGNED') &&
                        t.assignedStaffId
                      "
                      @click="startTicket(t.ticketId)"
                      class="btn btn-outline-secondary btn-sm mr-1"
                    >
                      開始
                    </button>

                    <button
                      v-if="t.issueStatus === 'UNDER_MAINTENANCE'"
                      @click="openResolveModal(t.ticketId)"
                      class="btn btn-outline-success btn-sm mr-1"
                    >
                      結案
                    </button>

                    <button
                      v-if="t.issueStatus !== 'RESOLVED' && t.issueStatus !== 'CANCELLED'"
                      @click="cancelTicket(t.ticketId)"
                      class="btn btn-outline-danger btn-sm"
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
    </section>

    <div v-if="showResolveModal" class="modal d-block" style="background: rgba(0, 0, 0, 0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5>工單結案</h5>
            <button @click="showResolveModal = false" class="close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>結果</label>
              <select v-model="resolveForm.resultType" class="form-control">
                <option value="FIXED">維修成功</option>
                <option value="NOT_FIXABLE">無法修復</option>
              </select>
            </div>
            <div class="form-group">
              <label>備註</label
              ><textarea v-model="resolveForm.resolveNote" class="form-control" rows="3"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="submitResolve" class="btn btn-primary">確定</button
            ><button @click="showResolveModal = false" class="btn btn-secondary">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
