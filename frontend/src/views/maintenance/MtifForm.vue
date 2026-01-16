<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const ticketId = Number(route.params.id)
const isEdit = computed(() => !isNaN(ticketId) && ticketId > 0)

const formRef = ref(null)
const loading = ref(false) // 頁面資料讀取中
const submitting = ref(false) // 送出按鈕讀取中

const form = reactive({
  spotId: null,
  issueType: '',
  issueDesc: '',
  issuePriority: 'NORMAL',
  assignedStaffId: null,
})

const staffOptions = ref([])
const spotOptions = ref([])

// 驗證規則
const rules = {
  spotId: [{ required: true, message: '請選擇一個場地', trigger: 'change' }],
  issueType: [{ required: true, message: '請輸入問題類型', trigger: 'blur' }],
  issuePriority: [{ required: true, message: '請選擇優先級', trigger: 'change' }],
}

onMounted(async () => {
  loading.value = true
  try {
    // 平行載入所有需要的資料
    const [spotRes, staffRes] = await Promise.all([
      maintenanceApi.getAllSpots().catch(() => ({ data: [] })),
      maintenanceApi.getAllStaff().catch(() => ({ data: [] })),
    ])

    spotOptions.value = Array.isArray(spotRes.data) ? spotRes.data : []
    staffOptions.value = staffRes.data || []

    if (isEdit.value) {
      const res = await maintenanceApi.getTicketById(ticketId)
      Object.assign(form, res.data)
    } else {
      // 預設選取第一個場地，減少點擊
      if (spotOptions.value.length > 0) form.spotId = spotOptions.value[0].spotId
    }
  } catch {
    // 錯誤已由 http.js 攔截器處理
    router.push('/admin/mtif-list')
  } finally {
    loading.value = false
  }
})

const submit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await maintenanceApi.updateTicket(ticketId, form)
          // ★ 修復：無論 assignedStaffId 是有值還是 null，都要呼叫指派 API
          // 這樣使用者才能「取消指派」讓後端重置狀態
          await maintenanceApi.assignStaff(ticketId, form.assignedStaffId)
          Swal.fire('成功', '工單已更新', 'success')
        } else {
          await maintenanceApi.createTicket(form)
          Swal.fire('成功', '新工單已建立', 'success')
        }
        router.push('/admin/mtif-list')
      } catch {
        // 錯誤已由 http.js 攔截器處理
      } finally {
        submitting.value = false
      }
    } else {
      Swal.fire('提示', '請檢查必填欄位', 'warning')
    }
  })
}
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <h1>{{ isEdit ? '編輯工單' : '新增工單' }}</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid d-flex justify-content-center">
        <el-card shadow="always" style="width: 100%; max-width: 800px" v-loading="loading">
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <span><i class="fas fa-edit mr-1"></i> 工單資訊</span>
              <el-button link type="info" @click="router.push('/admin/mtif-list')">返回</el-button>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" status-icon>
            <el-form-item label="場地編號" prop="spotId">
              <el-select
                v-model="form.spotId"
                placeholder="搜尋場地..."
                class="w-100"
                filterable
                :disabled="isEdit"
              >
                <el-option
                  v-for="s in spotOptions"
                  :key="s.spotId"
                  :label="`${s.spotCode || s.spotId} - ${s.spotName}`"
                  :value="s.spotId"
                />
              </el-select>
              <small v-if="spotOptions.length === 0" class="text-muted">無可用場地資料</small>
            </el-form-item>

            <el-form-item label="問題類型" prop="issueType">
              <el-input v-model="form.issueType" placeholder="例如：螢幕故障、椅子損壞" />
            </el-form-item>

            <el-form-item label="詳細描述" prop="issueDesc">
              <el-input
                v-model="form.issueDesc"
                type="textarea"
                :rows="4"
                placeholder="請詳細描述狀況..."
              />
            </el-form-item>

            <el-form-item label="優先級" prop="issuePriority">
              <el-radio-group v-model="form.issuePriority">
                <el-radio-button label="LOW">低</el-radio-button>
                <el-radio-button label="NORMAL">普通</el-radio-button>
                <el-radio-button label="HIGH">高</el-radio-button>
                <el-radio-button label="URGENT">緊急</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="指派維修員" prop="assignedStaffId">
              <el-select
                v-model="form.assignedStaffId"
                placeholder="暫不指派"
                class="w-100"
                filterable
                clearable
              >
                <el-option
                  v-for="s in staffOptions"
                  :key="s.staffId"
                  :label="`${s.staffName} (${s.staffCompany || '外部'})`"
                  :value="s.staffId"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submit" :loading="submitting">
                <i class="fas fa-save mr-1"></i> 儲存並送出
              </el-button>
              <el-button @click="router.push('/admin/mtif-list')">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.content-header {
  padding: 15px 0.5rem;
}
.w-100 {
  width: 100%;
}
</style>
