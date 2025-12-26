<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// 取得路由參數中的 ID
const staffId = Number(route.params.id)
// 判斷是「編輯」還是「新增」
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
      // ✅ 手動挑選欄位，確保不傳回多餘欄位
      form.staffName = d.staffName || ''
      form.staffCompany = d.staffCompany || ''
      form.staffPhone = d.staffPhone || ''
      form.staffEmail = d.staffEmail || ''
      form.staffNote = d.staffNote || ''
    } catch (error) {
      alert('找不到該人員資料')
      // 修正：跳轉回列表時需補上 /admin
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
    // 修正：儲存成功後跳轉回列表需補上 /admin
    router.push('/admin/staff-list')
  } catch (error) {
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
/* 這裡可以依需求調整間距 */
.content-header {
  padding: 15px 0.5rem;
}
.card-teal:not(.card-outline) > .card-header {
  background-color: #20c997;
  color: #fff;
}
</style>
