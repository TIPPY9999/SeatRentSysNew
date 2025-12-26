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
  try {
    const sRes = await axios.get('http://localhost:8080/api/maintenance/staff')
    staffOptions.value = sRes.data
  } catch (e) {
    console.error('無法載入人員列表')
  }

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
      router.push('/mtif-list')
    }
  }
})

const submit = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`http://localhost:8080/api/maintenance/tickets/${ticketId}`, form)
      await axios.post(`http://localhost:8080/api/maintenance/tickets/${ticketId}/assign`, {
        staffId: form.assignedStaffId,
      })
      alert('更新成功')
    } else {
      await axios.post('http://localhost:8080/api/maintenance/tickets', form)
      alert('新增成功')
    }
    router.push('/mtif-list')
  } catch (e) {
    alert('儲存失敗')
  }
}
</script>

<template>
  <div>
    <section class="content-header">
      <h1>{{ isEdit ? '編輯' : '新增' }}維修工單</h1>
    </section>
    <section class="content">
      <div class="container-fluid row justify-content-center">
        <div class="col-lg-8">
          <div class="card card-primary">
            <div class="card-header"><h3 class="card-title">詳細資訊</h3></div>
            <form @submit.prevent="submit">
              <div class="card-body">
                <div class="form-group row">
                  <label class="col-sm-3">場地編號*</label>
                  <div class="col-sm-9">
                    <input
                      v-model.number="form.spotId"
                      type="number"
                      class="form-control"
                      required
                    />
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3">問題類型*</label>
                  <div class="col-sm-9">
                    <input v-model="form.issueType" class="form-control" required />
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3">描述</label>
                  <div class="col-sm-9">
                    <textarea v-model="form.issueDesc" class="form-control" rows="4"></textarea>
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3">優先級</label>
                  <div class="col-sm-9">
                    <select v-model="form.issuePriority" class="form-control">
                      <option value="LOW">低</option>
                      <option value="NORMAL">普通</option>
                      <option value="HIGH">高</option>
                      <option value="URGENT">緊急</option>
                    </select>
                  </div>
                </div>
                <div class="form-group row">
                  <label class="col-sm-3">指派維修員</label>
                  <div class="col-sm-9">
                    <select v-model="form.assignedStaffId" class="form-control">
                      <option :value="null">暫不指派</option>
                      <option v-for="s in staffOptions" :key="s.staffId" :value="s.staffId">
                        {{ s.staffName }} - {{ s.staffCompany }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>
              <div class="card-footer text-right">
                <button type="submit" class="btn btn-primary">送出</button>
                <button
                  type="button"
                  class="btn btn-secondary ml-2"
                  @click="router.push('/mtif-list')"
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
