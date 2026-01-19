<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const staffId = Number(route.params.id)
const isEdit = computed(() => !isNaN(staffId) && staffId > 0)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  staffName: '',
  staffCompany: '',
  staffPhone: '',
  staffEmail: '',
  staffNote: '',
  isActive: true,
})

const rules = {
  staffName: [{ required: true, message: '請輸入姓名', trigger: 'blur' }],
  staffEmail: [{ type: 'email', message: 'Email 格式不正確', trigger: 'blur' }],
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await maintenanceApi.getStaffById(staffId)
      Object.assign(form, res.data)
    } catch {
      // 錯誤已由 http.js 攔截器處理
      router.push('/admin/staff-list')
    } finally {
      loading.value = false
    }
  }
})

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await maintenanceApi.updateStaff(staffId, form)
          Swal.fire('成功', '資料已更新', 'success')
        } else {
          await maintenanceApi.createStaff(form)
          Swal.fire('成功', '人員新增成功', 'success')
        }
        router.push('/admin/staff-list')
      } catch {
        // 錯誤已由 http.js 攔截器處理
      } finally {
        submitting.value = false
      }
    } else {
      Swal.fire('提示', '請檢查格式', 'warning')
    }
  })
}
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <h1>{{ isEdit ? '編輯人員' : '新增人員' }}</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid d-flex justify-content-center">
        <el-card shadow="always" style="width: 100%; max-width: 600px" v-loading="loading">
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <span><i class="fas fa-user-circle mr-1"></i> 基本資料</span>
              <el-button link type="info" @click="router.push('/admin/staff-list')">取消</el-button>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" status-icon>
            <el-form-item label="姓名" prop="staffName">
              <el-input v-model="form.staffName" placeholder="請輸入姓名" />
            </el-form-item>
            <el-form-item label="公司" prop="staffCompany">
              <el-input v-model="form.staffCompany" placeholder="請輸入公司名稱" />
            </el-form-item>
            <el-form-item label="電話" prop="staffPhone">
              <el-input v-model="form.staffPhone" placeholder="09xx-xxx-xxx" />
            </el-form-item>
            <el-form-item label="Email" prop="staffEmail">
              <el-input v-model="form.staffEmail" placeholder="example@mail.com" />
            </el-form-item>
            <el-form-item label="備註" prop="staffNote">
              <el-input v-model="form.staffNote" type="textarea" :rows="3" />
            </el-form-item>

            <el-form-item v-if="isEdit" label="狀態" prop="isActive">
              <el-switch
                v-model="form.isActive"
                active-text="在職"
                inactive-text="停用"
                active-color="#13ce66"
                inactive-color="#ff4949"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="submitting">
                <i class="fas fa-save mr-1"></i> 儲存
              </el-button>
              <el-button @click="router.push('/admin/staff-list')">返回</el-button>
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
</style>
