<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useTicketConfig } from '@/composables/maintenance/useTicketConfig'

const route = useRoute()
const router = useRouter()
const ticketId = Number(route.params.id)
const isEdit = computed(() => !isNaN(ticketId) && ticketId > 0)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const activeStep = ref(0)

const form = reactive({
  spotId: null,
  issueType: '',
  issueDesc: '',
  issuePriority: 'NORMAL',
  assignedStaffId: null,
})

const staffOptions = ref([])
const spotOptions = ref([])

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
const rules = {
  spotId: [{ required: true, message: '請選擇一個場地', trigger: 'change' }],
  issueType: [{ required: true, message: '請輸入或選擇問題類型', trigger: 'blur' }],
  issuePriority: [{ required: true, message: '請選擇優先級', trigger: 'change' }],
}

// 計算表單完成度
const formProgress = computed(() => {
  let filled = 0
  if (form.spotId) filled++
  if (form.issueType) filled++
  if (form.issueDesc) filled++
  if (form.assignedStaffId) filled++
  return Math.round((filled / 4) * 100)
})

// 監聽表單變化，自動更新步驟指示
watch(
  () => form.spotId,
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

onMounted(async () => {
  setTimeout(() => (formVisible.value = true), 100)

  loading.value = true
  try {
    const [spotRes, staffRes] = await Promise.all([
      maintenanceApi.getAllSpots().catch(() => ({ data: [] })),
      maintenanceApi.getAllStaff().catch(() => ({ data: [] })),
    ])

    spotOptions.value = Array.isArray(spotRes.data) ? spotRes.data : []
    staffOptions.value = staffRes.data || []

    if (isEdit.value) {
      const res = await maintenanceApi.getTicketById(ticketId)
      Object.assign(form, res.data)
      activeStep.value = 3
    } else {
      if (spotOptions.value.length > 0) form.spotId = spotOptions.value[0].spotId
    }
  } catch {
    router.push('/admin/mtif-list')
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
      // 確認彈窗
      const selectedStaff = staffOptions.value.find((s) => s.staffId === form.assignedStaffId)
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
        showClass: { popup: 'animate__animated animate__fadeInUp animate__faster' },
        hideClass: { popup: 'animate__animated animate__fadeOutDown animate__faster' },
      })

      if (!confirmResult.isConfirmed) return

      submitting.value = true
      try {
        if (isEdit.value) {
          await maintenanceApi.updateTicket(ticketId, form)
          await maintenanceApi.assignStaff(ticketId, form.assignedStaffId)
          await Swal.fire({
            icon: 'success',
            title: '更新成功！',
            html: '<span class="text-success">工單資料已成功更新</span>',
            timer: 2000,
            timerProgressBar: true,
            showConfirmButton: false,
            showClass: { popup: 'animate__animated animate__bounceIn' },
          })
        } else {
          await maintenanceApi.createTicket(form)
          await Swal.fire({
            icon: 'success',
            title: '🎉 工單建立成功！',
            html: `
              <div style="text-align: center;">
                <div style="font-size: 48px; margin-bottom: 12px;">📋</div>
                <p>新工單已成功建立並進入待處理佇列</p>
              </div>
            `,
            timer: 2500,
            timerProgressBar: true,
            showConfirmButton: false,
            showClass: { popup: 'animate__animated animate__tada' },
          })
        }
        router.push('/admin/mtif-list')
      } catch {
        // 錯誤已由 http.js 攔截器處理
      } finally {
        submitting.value = false
      }
    } else {
      Swal.fire({
        icon: 'warning',
        title: '表單驗證失敗',
        text: '請確認所有必填欄位都已正確填寫',
        confirmButtonText: '我知道了',
        showClass: { popup: 'animate__animated animate__shakeX' },
      })
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
              <!-- 場地選擇 -->
              <el-form-item label="場地選擇" prop="spotId" class="form-item-animated">
                <template #label>
                  <span class="custom-label">
                    <i class="fas fa-map-marker-alt label-icon"></i> 維修場地
                    <span class="required-star">*</span>
                  </span>
                </template>
                <el-select
                  v-model="form.spotId"
                  placeholder="請選擇或搜尋場地..."
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
                    :label="`${s.spotCode || s.spotId} - ${s.spotName}`"
                    :value="s.spotId"
                  >
                    <div class="spot-option">
                      <span class="spot-code">{{ s.spotCode || s.spotId }}</span>
                      <span class="spot-name">{{ s.spotName }}</span>
                    </div>
                  </el-option>
                </el-select>
                <small v-if="spotOptions.length === 0" class="text-warning">
                  <i class="fas fa-exclamation-triangle mr-1"></i> 無可用場地資料
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
                  <el-option
                    v-for="s in staffOptions"
                    :key="s.staffId"
                    :label="`${s.staffName} (${s.staffCompany || '外部'})`"
                    :value="s.staffId"
                  >
                    <div class="staff-option">
                      <div class="staff-avatar">{{ s.staffName?.charAt(0) }}</div>
                      <div class="staff-info">
                        <span class="staff-name">{{ s.staffName }}</span>
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
