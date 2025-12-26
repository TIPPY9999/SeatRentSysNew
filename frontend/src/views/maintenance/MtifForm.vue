<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const ticketId = Number(route.params.id)
const isEdit = computed(() => !isNaN(ticketId) && ticketId > 0)

const form = reactive({
  spotId: null,
  issueType: '',
  issueDesc: '',
  issuePriority: 'NORMAL',
  assignedStaffId: null,
})

const staffOptions = ref<any[]>([])

onMounted(async () => {
  // 載入維修人員列表供下拉選單使用
  try {
    const sRes = await axios.get('http://localhost:8080/api/maintenance/staff')
    staffOptions.value = sRes.data
  } catch (e) {
    console.error('無法載入人員列表')
  }

  // 如果是編輯模式，載入現有工單資料
  if (isEdit.value) {
    try {
      const res = await axios.get(`http://localhost:8080/api/maintenance/tickets/${ticketId}`)
      const d = res.data
      form.spotId = d.spotId
      form.issueType = d.issueType
      form.issueDesc = d.issueDesc
      form.issuePriority = d.issuePriority
      form.assignedStaffId = d.assignedStaffId
    } catch (e) {
      alert('找不到該工單資料')
      // 修正：補上 /admin 前綴
      router.push('/admin/mtif-list')
    }
  }
})

const submit = async () => {
  try {
    if (isEdit.value) {
      // 執行更新
      await axios.put(`http://localhost:8080/api/maintenance/tickets/${ticketId}`, form)
      // 若有指派人員，同時執行指派 API
      await axios.post(`http://localhost:8080/api/maintenance/tickets/${ticketId}/assign`, {
        staffId: form.assignedStaffId,
      })
      alert('更新成功')
    } else {
      // 執行新增
      await axios.post('http://localhost:8080/api/maintenance/tickets', form)
      alert('新增成功')
    }
    // 修正：補上 /admin 前綴
    router.push('/admin/mtif-list')
  } catch (e) {
    alert('儲存失敗，請確認欄位內容與後端連線')
  }
}
</script>

<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <h1>{{ isEdit ? '編輯' : '新增' }}維修工單</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid row justify-content-center">
        <div class="col-lg-8">
          <div class="card card-primary shadow-sm">
            <div class="card-header">
              <h3 class="card-title">詳細資訊</h3>
            </div>

            <form @submit.prevent="submit">
              <div class="card-body">
                <div class="form-group row">
                  <label class="col-sm-3 col-form-label"
                    >場地編號 <span class="text-danger">*</span></label
                  >
                  <div class="col-sm-9">
                    <input
                      v-model.number="form.spotId"
                      type="number"
                      class="form-control"
                      placeholder="請輸入座位或場地編號"
                      required
                    />
                  </div>
                </div>

                <div class="form-group row">
                  <label class="col-sm-3 col-form-label"
                    >問題類型 <span class="text-danger">*</span></label
                  >
                  <div class="col-sm-9">
                    <input
                      v-model="form.issueType"
                      class="form-control"
                      placeholder="例如：硬體故障、網路問題"
                      required
                    />
                  </div>
                </div>

                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">詳細描述</label>
                  <div class="col-sm-9">
                    <textarea
                      v-model="form.issueDesc"
                      class="form-control"
                      rows="4"
                      placeholder="請詳細說明故障情況..."
                    ></textarea>
                  </div>
                </div>

                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">優先級</label>
                  <div class="col-sm-9">
                    <select v-model="form.issuePriority" class="form-control">
                      <option value="LOW">低 (Low)</option>
                      <option value="NORMAL">普通 (Normal)</option>
                      <option value="HIGH">高 (High)</option>
                      <option value="URGENT">緊急 (Urgent)</option>
                    </select>
                  </div>
                </div>

                <div class="form-group row">
                  <label class="col-sm-3 col-form-label">指派維修員</label>
                  <div class="col-sm-9">
                    <select v-model="form.assignedStaffId" class="form-control">
                      <option :value="null">-- 暫不指派 --</option>
                      <option v-for="s in staffOptions" :key="s.staffId" :value="s.staffId">
                        {{ s.staffName }} ({{ s.staffCompany || '外部廠商' }})
                      </option>
                    </select>
                  </div>
                </div>
              </div>

              <div class="card-footer text-right">
                <button type="submit" class="btn btn-primary">
                  <i class="fas fa-paper-plane mr-1"></i> 儲存並送出
                </button>
                <button
                  type="button"
                  class="btn btn-secondary ml-2"
                  @click="router.push('/admin/mtif-list')"
                >
                  取消
                </button>
              </div>
            </form>
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
.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}
</style>
