<template>
  <div class="seat-update container-fluid">
    <div class="card card-primary card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">修改 Seat</h3>
      </div>

      <div v-if="loading" class="card-body">
        <p>載入中...</p>
      </div>
      <div v-else-if="!formData" class="card-body">
        <p class="text-danger">找不到資料</p>
      </div>

      <form v-else @submit.prevent="handleUpdate">
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
            <label class="col-sm-2 col-form-label">SpotId</label>
            <div class="col-sm-10">
              <input type="number" v-model="formData.spotId" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">序號</label>
            <div class="col-sm-10">
              <input type="text" v-model="formData.serialNumber" class="form-control" />
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button type="submit" class="btn btn-primary mr-2">更新</button>
          <router-link to="/admin/seat/list" class="btn btn-secondary">回清單</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const formData = ref(null)

onMounted(async () => {
  const id = route.params.id
  if (id) {
    try {
      // [修正] 加上 /api
      const res = await axios.get('/api/seat/update', { params: { seatsId: id } })
      formData.value = res.data
    } catch (error) {
      console.error('Error fetching seat:', error)
    }
  }
  loading.value = false
})

const handleUpdate = async () => {
  const params = new URLSearchParams()
  for (const key in formData.value) {
    if (formData.value[key] !== null) {
      params.append(key, formData.value[key])
    }
  }
  // 確保 ID 被包含
  params.append('seatsId', formData.value.seatsId)

  // [修正] 加上 /api
  await axios.post('/api/seat/update', params)

  // [修正] 加上 /admin
  router.push('/admin/seat/list')
}
</script>
