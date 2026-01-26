<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useTicketConfig } from '@/composables/maintenance/useTicketConfig'
import TicketTimeline from '@/components/maintenance/TicketTimeline.vue'

const route = useRoute()
const router = useRouter()
const ticketId = computed(() => Number(route.params.id))
const isEdit = computed(() => !isNaN(ticketId.value) && ticketId.value > 0)

const formRef = ref(null)
const timelineRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const activeStep = ref(0)

// 維修目標類型：'spot' (機台) 或 'seat' (椅子)
const targetType = ref('spot')

const form = reactive({
  spotId: null,
  seatsId: null, // 新增：椅子 ID
  issueType: '',
  issueDesc: '',
  issuePriority: 'NORMAL',
  assignedStaffId: null,
})

const staffOptions = ref([])
const spotOptions = ref([])
const seatOptions = ref([]) // 新增：椅子選項

// ★ Bug3 修復：記錄編輯時原始的 assignedStaffId，用於判斷是否有變更
const originalAssignedStaffId = ref(null)

// ★ Bug3 修復：定義可編輯的狀態
const EDITABLE_STATUSES = ['REPORTED', 'ASSIGNED']

// 使用共用 composable 的問題類型配置
const { issueTypeOptions: sharedIssueTypes } = useTicketConfig()
const issueTypeOptions = sharedIssueTypes

// 優先級配置（擴展版，含描述）
const priorityConfig = {
  LOW: { color: '#909399', bgColor: '#f4f4f5', icon: '🔵', text: '低優先', desc: '可稍後處理' },
  NORMAL: { color: '#409eff', bgColor: '#ecf5ff', icon: '🟢', text: '普通', desc: '正常排程處理' },
  HIGH: { color: '#e6a23c', bgColor: '#fdf6ec', icon: '🟠', text: '高優先', desc: '優先安排處理' },
  URGENT: { color: '#f56c6c', bgColor: '#fef0f0', icon: '🔴', text: '緊急', desc: '立即處理' },
}

// 驗證規則
const rules = computed(() => ({
  spotId:
    targetType.value === 'spot'
      ? [{ required: true, message: '請選擇一個機台', trigger: 'change' }]
      : [],
  seatsId:
    targetType.value === 'seat'
      ? [{ required: true, message: '請選擇一張椅子', trigger: 'change' }]
      : [],
  issueType: [{ required: true, message: '請輸入或選擇問題類型', trigger: 'blur' }],
  issuePriority: [{ required: true, message: '請選擇優先級', trigger: 'change' }],
}))

// 計算表單完成度
const formProgress = computed(() => {
  let filled = 0
  if (targetType.value === 'spot' ? form.spotId : form.seatsId) filled++
  if (form.issueType) filled++
  if (form.issueDesc) filled++
  if (form.assignedStaffId) filled++
  return Math.round((filled / 4) * 100)
})

// 監聽表單變化，自動更新步驟指示
watch(
  () => (targetType.value === 'spot' ? form.spotId : form.seatsId),
  (val) => {
    if (val && activeStep.value === 0) activeStep.value = 1
  },
)
watch(
  () => form.issueType,
  (val) => {
    if (val && activeStep.value === 1) activeStep.value = 2
  },
)

// 切換維修類型時，清空已選擇的目標
watch(targetType, (newType) => {
  if (newType === 'spot') {
    form.seatsId = null
  } else {
    form.spotId = null
  }
})

onMounted(async () => {
  setTimeout(() => (formVisible.value = true), 100)

  loading.value = true
  try {
    // ★ Bug3 修復：先讀取工單資料，檢查狀態是否可編輯
    if (isEdit.value) {
      const ticketRes = await maintenanceApi.getTicketById(ticketId.value)
      const ticketData = ticketRes.data

      // ★ 問題A修復：使用正確的欄位名稱 issueStatus
      if (!EDITABLE_STATUSES.includes(ticketData.issueStatus)) {
        await Swal.fire({
          icon: 'warning', // ★ (2B) 改為 warning，不是系統錯誤
          title: '無法編輯',
          html: `
            <p style="color: #909399;">此工單狀態為「<b>${ticketData.issueStatus}</b>」，不允許編輯</p>
            <p style="color: #f56c6c; font-size: 13px; margin-top: 10px;">可編輯狀態：REPORTED, ASSIGNED</p>
          `,
          confirmButtonText: '返回列表',
        })
        router.push({ name: 'mtif-list' })
        return
      }

      // ★ Bug3 修復：記錄原始 assignedStaffId
      originalAssignedStaffId.value = ticketData.assignedStaffId

      // 載入其他資料
      const [spotRes, staffRes, seatRes] = await Promise.all([
        maintenanceApi.getAllSpots().catch(() => ({ data: [] })),
        maintenanceApi.getAllStaff().catch(() => ({ data: [] })), // 編輯時用 getAllStaff
        maintenanceApi.getAllSeats().catch(() => ({ data: [] })),
      ])

      spotOptions.value = Array.isArray(spotRes.data) ? spotRes.data : []
      staffOptions.value = staffRes.data || []
      seatOptions.value = seatRes.data || []

      // ★ 如果原始人員已停用，要保留並顯示為 disabled
      if (ticketData.assignedStaffId) {
        const assignedStaff = staffOptions.value.find(s => s.staffId === ticketData.assignedStaffId)
        if (assignedStaff && !assignedStaff.isActive) {
          assignedStaff.staffName = assignedStaff.staffName + ' (已停用)'
          assignedStaff.disabled = true
        }
      }

      // 賦值表單
      Object.assign(form, ticketData)
      // 根據資料判斷維修類型
      if (ticketData.seatsId) {
        targetType.value = 'seat'
      } else {
        targetType.value = 'spot'
      }
      activeStep.value = 3
    } else {
      // ★ Bug2 修復：建立時只載入啟用人員
      const [spotRes, staffRes, seatRes] = await Promise.all([
        maintenanceApi.getAllSpots().catch(() => ({ data: [] })),
        maintenanceApi.getActiveStaff().catch(() => ({ data: [] })), // ★ 改用 getActiveStaff
        maintenanceApi.getAllSeats().catch(() => ({ data: [] })),
      ])

      spotOptions.value = Array.isArray(spotRes.data) ? spotRes.data : []
      staffOptions.value = staffRes.data || []
      seatOptions.value = seatRes.data || []

      if (spotOptions.value.length > 0) form.spotId = spotOptions.value[0].spotId
    }
  } catch (error) {
    console.error('Failed to load form data:', error)
    Swal.fire('錯誤', '載入失敗，請稍後再試', 'error')
    router.push({ name: 'mtif-list' })
  } finally {
    loading.value = false
  }
})

const selectIssueType = (type) => {
  form.issueType = type.value
}

const submit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 1. 取得選中的人員名稱 (為了顯示確認窗)
      const selectedStaff = staffOptions.value.find((s) => s.staffId === form.assignedStaffId)

      // 2. 顯示確認視窗
      const confirmResult = await Swal.fire({
        title: isEdit.value ? '確認更新工單？' : '確認建立工單？',
        html: `
          <div style="text-align: left; padding: 10px 0;">
            <div style="display: grid; gap: 12px;">
              <div style="padding: 12px; background: #f5f7fa; border-radius: 10px;">
                <p style="margin: 0 0 8px; color: #909399; font-size: 12px;">問題類型</p>
                <p style="margin: 0; font-size: 16px; font-weight: 600;">${form.issueType}</p>
              </div>
              <div style="padding: 12px; background: ${priorityConfig[form.issuePriority].bgColor}; border-radius: 10px; border-left: 4px solid ${priorityConfig[form.issuePriority].color};">
                <p style="margin: 0 0 8px; color: #909399; font-size: 12px;">優先級</p>
                <p style="margin: 0; font-size: 16px; font-weight: 600; color: ${priorityConfig[form.issuePriority].color};">
                   ${priorityConfig[form.issuePriority].icon} ${priorityConfig[form.issuePriority].text}
                </p>
              </div>
              ${
                selectedStaff
                  ? `
              <div style="padding: 12px; background: #f0f9eb; border-radius: 10px;">
                <p style="margin: 0 0 8px; color: #909399; font-size: 12px;">指派人員</p>
                <p style="margin: 0; font-size: 16px; font-weight: 600; color: #67c23a;">
                  <i class="fas fa-user-check mr-1"></i> ${selectedStaff.staffName}
                </p>
              </div>
              `
                  : ''
              }
            </div>
          </div>
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409eff',
        cancelButtonColor: '#909399',
        confirmButtonText: '<i class="fas fa-paper-plane mr-1"></i> 確認送出',
        cancelButtonText: '再檢查一下',
      })

      if (!confirmResult.isConfirmed) return

      submitting.value = true
      try {
        // ★ 關鍵修正：資料清洗（完整版）
        // 建立一個乾淨的物件，只包含後端需要的欄位
        const submitData = {
          spotId: form.spotId,
          seatsId: form.seatsId,
          issueType: form.issueType,
          issueDesc: form.issueDesc,
          issuePriority: form.issuePriority,
          assignedStaffId: form.assignedStaffId
        }
        
        // 根據維修類型清除不需要的欄位
        if (targetType.value === 'spot') {
          submitData.seatsId = null
        } else if (targetType.value === 'seat') {
          submitData.spotId = null
        }

        if (isEdit.value) {
          // === [更新模式] ===
          // ★ 問題3修復：後端 updateTicket 已經處理 assignedStaffId 的更新與 Log 紀錄
          // 不需要額外呼叫 assignStaff API，完全依賴 updateTicket 即可
          await maintenanceApi.updateTicket(ticketId.value, submitData)

          await Swal.fire({
            icon: 'success',
            title: '更新成功！',
            text: '工單資料已更新',
            timer: 1200,
            showConfirmButton: false,
          })
        } else {
          // === [新增模式] ===
          await maintenanceApi.createTicket(submitData)

          await Swal.fire({
            icon: 'success',
            title: '建立成功！',
            text: '新工單已建立',
            timer: 1500,
            showConfirmButton: false,
          })
        }

        // ★ 關鍵修正 2：無論新增或修改，統一跳回列表頁
        router.push({ name: 'mtif-list' })
      } catch (error) {
        console.error('Submit failed:', error)
        // ★ Bug3 修復：顯示後端回傳的錯誤訊息
        const errorMsg = error?.response?.data?.message || '操作失敗，請稍後再試'
        Swal.fire('錯誤', errorMsg, 'error')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleCancel = async () => {
  if (form.issueType || form.issueDesc) {
    const result = await Swal.fire({
      title: '確定要離開嗎？',
      text: '您填寫的工單資料將不會被保存',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#e6a23c',
      cancelButtonColor: '#909399',
      confirmButtonText: '離開',
      cancelButtonText: '繼續編輯',
      showClass: { popup: 'animate__animated animate__fadeIn animate__faster' },
    })
    if (!result.isConfirmed) return
  }
  router.push('/admin/mtif-list')
}
</script>

<template>
  <div class="ticket-form-container">
    <!-- 頁面標題區 -->
    <section class="content-header">
      <div class="container-fluid">
        <transition name="slide-fade" appear>
          <div class="page-title-box">
            <div class="title-icon" :class="isEdit ? 'edit-mode' : 'add-mode'">
              <i :class="isEdit ? 'fas fa-ticket-alt' : 'fas fa-plus-circle'"></i>
            </div>
            <div class="title-content">
              <h1>{{ isEdit ? '編輯維修工單' : '建立新工單' }}</h1>
              <p class="subtitle">
                {{ isEdit ? '修改現有工單資訊' : '填寫問題詳情以建立維修工單' }}
              </p>
            </div>
            <div class="title-progress" v-if="!isEdit">
              <div class="progress-ring">
                <el-progress
                  type="circle"
                  :percentage="formProgress"
                  :width="60"
                  :stroke-width="6"
                  :color="formProgress === 100 ? '#67c23a' : '#409eff'"
                />
              </div>
              <span class="progress-text">完成度</span>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <!-- 表單主體 -->
    <section class="content">
      <div class="container-fluid d-flex justify-content-center">
        <transition name="zoom-fade" appear>
          <el-card
            v-show="formVisible"
            shadow="hover"
            class="form-card"
            v-loading="loading"
            element-loading-text="載入中..."
          >
            <template #header>
              <div class="card-header-content">
                <div class="header-left">
                  <span class="header-icon">
                    <i class="fas fa-clipboard-list"></i>
                  </span>
                  <span class="header-text">工單資訊</span>
                  <el-tag v-if="isEdit" type="warning" effect="plain" size="small" class="ml-2">
                    #{{ ticketId }}
                  </el-tag>
                </div>
                <el-button class="cancel-btn" text type="info" @click="handleCancel">
                  <i class="fas fa-times mr-1"></i> 取消
                </el-button>
              </div>
            </template>

            <!-- 步驟指示器 -->
            <div class="steps-indicator" v-if="!isEdit">
              <el-steps :active="activeStep" finish-status="success" simple>
                <el-step title="選擇場地" icon="Location" />
                <el-step title="問題描述" icon="Edit" />
                <el-step title="設定優先級" icon="Flag" />
                <el-step title="指派人員" icon="User" />
              </el-steps>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              status-icon
              class="ticket-form"
            >
              <!-- 維修目標類型切換 -->
              <el-form-item class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-wrench label-icon"></i> 維修目標
                    <span class="required-star">*</span>
                  </span>
                </template>
                <div class="target-type-switch">
                  <div
                    class="target-type-option"
                    :class="{ active: targetType === 'spot' }"
                    @click="targetType = 'spot'"
                  >
                    <i class="fas fa-desktop"></i>
                    <span>機台</span>
                  </div>
                  <div
                    class="target-type-option"
                    :class="{ active: targetType === 'seat' }"
                    @click="targetType = 'seat'"
                  >
                    <i class="fas fa-chair"></i>
                    <span>椅子</span>
                  </div>
                </div>
              </el-form-item>

              <!-- 機台選擇 (當 targetType === 'spot') -->
              <el-form-item
                v-if="targetType === 'spot'"
                label="場地選擇"
                prop="spotId"
                class="form-item-animated"
              >
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-desktop label-icon"></i> 選擇機台
                    <span class="required-star">*</span>
                  </span>
                </template>
                <el-select
                  v-model="form.spotId"
                  placeholder="請選擇或搜尋機台..."
                  class="w-100"
                  filterable
                  :disabled="isEdit"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-search"></i>
                  </template>
                  <el-option
                    v-for="s in spotOptions"
                    :key="s.spotId"
                    :label="`${s.spotCode || s.spotId} - ${s.spotName} (${s.spotStatus})`"
                    :value="s.spotId"
                    :disabled="s.spotStatus !== '營運中'"
                  >
                    <div class="spot-option">
                      <span class="spot-code">{{ s.spotCode || s.spotId }}</span>
                      <span class="spot-name">{{ s.spotName }}</span>
                      <span class="spot-status" :style="{ color: s.spotStatus === '營運中' ? '#67c23a' : '#909399' }">
                        ({{ s.spotStatus }})
                      </span>
                    </div>
                  </el-option>
                </el-select>
                <small v-if="spotOptions.length === 0" class="text-warning">
                  <i class="fas fa-exclamation-triangle mr-1"></i> 無可用機台資料
                </small>
              </el-form-item>

              <!-- 椅子選擇 (當 targetType === 'seat') -->
              <el-form-item
                v-if="targetType === 'seat'"
                label="椅子選擇"
                prop="seatsId"
                class="form-item-animated"
              >
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-chair label-icon"></i> 選擇椅子
                    <span class="required-star">*</span>
                  </span>
                </template>
                <el-select
                  v-model="form.seatsId"
                  placeholder="請選擇或搜尋椅子..."
                  class="w-100"
                  filterable
                  :disabled="isEdit"
                  size="large"
                >
                  <template #prefix>
                    <i class="fas fa-search"></i>
                  </template>
                  <el-option
                    v-for="seat in seatOptions"
                    :key="seat.seatsId"
                    :label="`${seat.seatsName || seat.seatsId} (${seat.seatsType || '一般'})`"
                    :value="seat.seatsId"
                  >
                    <div class="seat-option">
                      <span class="seat-icon">🪑</span>
                      <div class="seat-info">
                        <span class="seat-name">{{
                          seat.seatsName || `椅子 #${seat.seatsId}`
                        }}</span>
                        <span class="seat-type"
                          >{{ seat.seatsType || '一般座椅' }} ·
                          {{ seat.seatsStatus || '正常' }}</span
                        >
                      </div>
                    </div>
                  </el-option>
                </el-select>
                <small v-if="seatOptions.length === 0" class="text-warning">
                  <i class="fas fa-exclamation-triangle mr-1"></i> 無可用椅子資料
                </small>
              </el-form-item>

              <!-- 問題類型 -->
              <el-form-item label="問題類型" prop="issueType" class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-exclamation-circle label-icon"></i> 問題類型
                    <span class="required-star">*</span>
                  </span>
                </template>

                <!-- 快速選擇區 -->
                <div class="quick-select-grid">
                  <div
                    v-for="type in issueTypeOptions"
                    :key="type.value"
                    class="quick-select-item"
                    :class="{ active: form.issueType === type.value }"
                    @click="selectIssueType(type)"
                  >
                    <span class="item-icon">{{ type.icon }}</span>
                    <span class="item-text">{{ type.value }}</span>
                  </div>
                </div>

                <el-input
                  v-model="form.issueType"
                  placeholder="或自行輸入問題類型..."
                  size="large"
                  class="mt-2"
                  clearable
                />
              </el-form-item>

              <!-- 詳細描述 -->
              <el-form-item label="詳細描述" prop="issueDesc" class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-align-left label-icon"></i> 詳細描述
                  </span>
                </template>
                <el-input
                  v-model="form.issueDesc"
                  type="textarea"
                  :rows="4"
                  placeholder="請詳細描述問題狀況，例如：故障位置、發生時間、嚴重程度等..."
                  show-word-limit
                  maxlength="1000"
                  class="custom-textarea"
                />
              </el-form-item>

              <!-- 優先級選擇 -->
              <el-form-item label="優先級" prop="issuePriority" class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-flag label-icon"></i> 優先級
                    <span class="required-star">*</span>
                  </span>
                </template>
                <div class="priority-cards">
                  <div
                    v-for="(config, key) in priorityConfig"
                    :key="key"
                    class="priority-card"
                    :class="{ active: form.issuePriority === key }"
                    :style="{
                      '--card-color': config.color,
                      '--card-bg': config.bgColor,
                    }"
                    @click="form.issuePriority = key"
                  >
                    <span class="priority-icon">{{ config.icon }}</span>
                    <span class="priority-text">{{ config.text }}</span>
                    <span class="priority-desc">{{ config.desc }}</span>
                  </div>
                </div>
              </el-form-item>

              <!-- 指派維修員 -->
              <el-form-item label="指派維修員" prop="assignedStaffId" class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-user-cog label-icon"></i> 指派維修員
                    <el-tag type="info" size="small" class="ml-2">選填</el-tag>
                  </span>
                </template>
                <el-select
                  v-model="form.assignedStaffId"
                  placeholder="暫不指派，稍後可編輯"
                  class="w-100"
                  filterable
                  clearable
                  size="large"
                >
                  <!-- ★ 問題2修復：過濾只顯示啟用人員，或當前工單已指派的人員（即使已停用） -->
                  <el-option
                    v-for="s in staffOptions.filter(staff => staff.isActive === true || staff.staffId === originalAssignedStaffId)"
                    :key="s.staffId"
                    :label="`${s.staffName}${s.isActive === false ? ' (已停用)' : ''} (${s.staffCompany || '外部'})`"
                    :value="s.staffId"
                    :disabled="s.isActive === false && s.staffId !== form.assignedStaffId"
                  >
                    <div class="staff-option">
                      <div class="staff-avatar" :style="{ opacity: s.isActive === false ? 0.5 : 1 }">{{ s.staffName?.charAt(0) }}</div>
                      <div class="staff-info">
                        <span class="staff-name" :style="{ color: s.isActive === false ? '#909399' : '' }">
                          {{ s.staffName }}
                          <el-tag v-if="s.isActive === false" type="info" size="small" style="margin-left: 4px;">已停用</el-tag>
                        </span>
                        <span class="staff-company">{{ s.staffCompany || '外部人員' }}</span>
                      </div>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>

              <!-- 分隔線 -->
              <el-divider>
                <i class="fas fa-paper-plane"></i>
              </el-divider>

              <!-- 按鈕區 -->
              <el-form-item class="form-actions">
                <el-button
                  type="primary"
                  @click="submit"
                  :loading="submitting"
                  size="large"
                  class="submit-btn"
                >
                  <i class="fas fa-paper-plane mr-2" v-if="!submitting"></i>
                  <span>{{ submitting ? '處理中...' : isEdit ? '更新工單' : '建立工單' }}</span>
                </el-button>
                <el-button @click="handleCancel" size="large" class="back-btn">
                  <i class="fas fa-arrow-left mr-2"></i> 返回列表
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </transition>
      </div>
    </section>
  </div>
  <!-- 維修歷程紀錄 -->
  <div class="page-container">
    <el-card v-if="ticketId" class="mt-4" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>維修歷程紀錄</span>
        </div>
      </template>
      <TicketTimeline ref="timelineRef" :ticketId="ticketId" />
    </el-card>
  </div>
</template>

<style scoped>
.ticket-form-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
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
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.title-icon {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: white;
  transition: transform 0.3s ease;
}

.title-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.title-icon.add-mode {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}

.title-icon.edit-mode {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
}

.title-content {
  flex: 1;
}

.title-content h1 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: #303133;
}

.title-content .subtitle {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

.title-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

/* 表單卡片 */
.form-card {
  width: 100%;
  max-width: 800px;
  border-radius: 16px;
  overflow: hidden;
  border: none;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
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
  width: 36px;
  height: 36px;
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

/* 步驟指示器 */
.steps-indicator {
  padding: 16px 0 24px;
  border-bottom: 1px dashed #ebeef5;
  margin-bottom: 20px;
}

/* 表單樣式 */
.ticket-form {
  padding: 10px 20px;
}

.custom-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #606266;
}

.label-icon {
  color: #409eff;
  font-size: 14px;
}

.required-star {
  color: #f56c6c;
  margin-left: 2px;
}

.form-item-animated {
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
}

.form-item-animated:nth-child(1) {
  animation-delay: 0.1s;
}
.form-item-animated:nth-child(2) {
  animation-delay: 0.15s;
}
.form-item-animated:nth-child(3) {
  animation-delay: 0.2s;
}
.form-item-animated:nth-child(4) {
  animation-delay: 0.25s;
}
.form-item-animated:nth-child(5) {
  animation-delay: 0.3s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 場地選項樣式 */
.spot-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.spot-code {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.spot-name {
  color: #606266;
}

/* 維修目標類型切換 */
.target-type-switch {
  display: flex;
  gap: 16px;
  width: 100%;
}

.target-type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.target-type-option:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.target-type-option.active {
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f4ff 100%);
  border-color: #409eff;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.2);
}

.target-type-option i {
  font-size: 28px;
  color: #909399;
  transition: all 0.3s ease;
}

.target-type-option.active i {
  color: #409eff;
  transform: scale(1.1);
}

.target-type-option span {
  font-weight: 600;
  font-size: 14px;
  color: #606266;
}

.target-type-option.active span {
  color: #409eff;
}

/* 椅子選項樣式 */
.seat-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.seat-icon {
  font-size: 20px;
}

.seat-info {
  display: flex;
  flex-direction: column;
}

.seat-name {
  font-weight: 500;
  color: #303133;
}

.seat-type {
  font-size: 12px;
  color: #909399;
}

/* 快速選擇區 - 橫排設計 */
.quick-select-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-select-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #f5f7fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.quick-select-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.quick-select-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.item-icon {
  font-size: 22px;
}

.item-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

/* 優先級卡片 */
.priority-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

@media (max-width: 768px) {
  .priority-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

.priority-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: var(--card-bg);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 2px solid transparent;
}

.priority-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.priority-card.active {
  border-color: var(--card-color);
  transform: scale(1.05);
}

.priority-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.priority-text {
  font-weight: 600;
  color: var(--card-color);
  margin-bottom: 4px;
}

.priority-desc {
  font-size: 11px;
  color: #909399;
  text-align: center;
}

/* 維修員選項 */
.staff-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.staff-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.staff-info {
  display: flex;
  flex-direction: column;
}

.staff-name {
  font-weight: 500;
  color: #303133;
}

.staff-company {
  font-size: 12px;
  color: #909399;
}

/* 按鈕區 */
.form-actions {
  margin-top: 20px;
}

.submit-btn {
  min-width: 160px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border: none;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.back-btn {
  min-width: 120px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-3px);
}

/* 過渡動畫 */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.3s ease-in;
}
.slide-fade-enter-from {
  transform: translateX(-20px);
  opacity: 0;
}
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

.zoom-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.zoom-fade-leave-active {
  transition: all 0.3s ease-in;
}
.zoom-fade-enter-from {
  transform: scale(0.9);
  opacity: 0;
}
.zoom-fade-leave-to {
  transform: scale(0.95);
  opacity: 0;
}

/* 輔助類 */
.w-100 {
  width: 100%;
}
.mt-2 {
  margin-top: 8px;
}
.ml-2 {
  margin-left: 8px;
}
.mr-1 {
  margin-right: 4px;
}
.mr-2 {
  margin-right: 8px;
}
.text-warning {
  color: #e6a23c;
}

:deep(.el-divider__text) {
  background: white;
  color: #c0c4cc;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
}
</style>
