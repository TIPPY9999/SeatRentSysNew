<script setup>
// [修正] 移除了 ref，因為下面沒用到
import { onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const staffId = Number(route.params.id)
const isEdit = computed(() => !isNaN(staffId) && staffId > 0)

const form = reactive({
  staffName: '',
  staffCompany: '',
  staffPhone: '',
  staffEmail: '',
  staffNote: '',
})

onMounted(async () => {
  if (isEdit.value) {
    try {
      const res = await axios.get(`http://localhost:8080/api/maintenance/staff/${staffId}`)
      const d = res.data
      form.staffName = d.staffName || ''
      form.staffCompany = d.staffCompany || ''
      form.staffPhone = d.staffPhone || ''
      form.staffEmail = d.staffEmail || ''
      form.staffNote = d.staffNote || ''
    } catch {
      // [修正] 移除了 (error)，因為我們只 alert，沒用到 error 變數
      alert('找不到該人員資料')
      router.push('/admin/staff-list')
    }
  }
})

const submitForm = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`http://localhost:8080/api/maintenance/staff/${staffId}`, form)
      alert('更新成功')
    } else {
      await axios.post('http://localhost:8080/api/maintenance/staff', form)
      alert('新增成功')
    }
    router.push('/admin/staff-list')
  } catch {
    // [修正] 移除了 (error)
    alert('儲存失敗，請檢查網路或後端服務')
  }
}
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <h1>{{ isEdit ? '編輯' : '新增' }}維護人員</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row justify-content-center">
          <div class="col-lg-6">
            <div class="card card-teal">
              <div class="card-header">
                <h3 class="card-title">基本資料</h3>
              </div>

              <form @submit.prevent="submitForm">
                <div class="card-body">
                  <div class="form-group">
                    <label>姓名 <span class="text-danger">*</span></label>
                    <input
                      v-model="form.staffName"
                      type="text"
                      class="form-control"
                      placeholder="請輸入姓名"
                      required
                    />
                  </div>
                  <div class="form-group">
                    <label>公司</label>
                    <input
                      v-model="form.staffCompany"
                      type="text"
                      class="form-control"
                      placeholder="請輸入公司名稱"
                    />
                  </div>
                  <div class="form-group">
                    <label>電話</label>
                    <input
                      v-model="form.staffPhone"
                      type="text"
                      class="form-control"
                      placeholder="請輸入聯絡電話"
                    />
                  </div>
                  <div class="form-group">
                    <label>Email</label>
                    <input
                      v-model="form.staffEmail"
                      type="email"
                      class="form-control"
                      placeholder="example@mail.com"
                    />
                  </div>
                  <div class="form-group">
                    <label>備註</label>
                    <textarea
                      v-model="form.staffNote"
                      class="form-control"
                      rows="3"
                      placeholder="其他說明..."
                    ></textarea>
                  </div>
                </div>

                <div class="card-footer text-right">
                  <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save mr-1"></i> 送出
                  </button>
                  <button
                    type="button"
                    class="btn btn-secondary ml-2"
                    @click="router.push('/admin/staff-list')"
                  >
                    取消
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.content-header {
  padding: 15px 0.5rem;
}
.card-teal:not(.card-outline) > .card-header {
  background-color: #20c997;
  color: #fff;
}
</style>
