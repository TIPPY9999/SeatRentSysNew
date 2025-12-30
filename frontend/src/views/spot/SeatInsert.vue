<template>
  <div class="seat-insert container-fluid">
    <div class="card card-success card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">新增 Seat</h3>
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="card-body">
          <div class="form-group row">
            <label class="col-sm-2 col-form-label">名稱</label>
            <div class="col-sm-10">
              <input type="text" v-model="formData.seatsName" class="form-control" required />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">類型</label>
            <div class="col-sm-10">
              <input type="text" v-model="formData.seatsType" class="form-control" required />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">狀態</label>
            <div class="col-sm-10">
              <select v-model="formData.seatsStatus" class="form-control" required>
                <option value="可用">可用</option>
                <option value="維修">維修</option>
                <option value="停用">停用</option>
              </select>
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">SpotId (可空)</label>
            <div class="col-sm-10">
              <input type="number" v-model="formData.spotId" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">序號 (可空)</label>
            <div class="col-sm-10">
              <input type="text" v-model="formData.serialNumber" class="form-control" />
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button type="submit" class="btn btn-success mr-2">新增</button>
          <router-link to="/admin/seat/list" class="btn btn-secondary">回清單</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formData = ref({
  seatsName: '',
  seatsType: '',
  seatsStatus: '可用',
  spotId: null,
  serialNumber: '',
})

const handleSubmit = async () => {
  const params = new URLSearchParams()
  for (const key in formData.value) {
    if (formData.value[key] !== null) {
      params.append(key, formData.value[key])
    }
  }

  // [修正] 加上 /api
  await axios.post('/api/seat/insert', params)

  // [修正] 加上 /admin
  router.push('/admin/seat/list')
}
</script>
