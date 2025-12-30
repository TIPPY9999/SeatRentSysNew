<template>
  <div class="seat-search container-fluid">
    <div class="card card-info card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">Seat 條件查詢</h3>
      </div>

      <form @submit.prevent="handleSearch">
        <div class="card-body">
          <div class="form-group row">
            <label class="col-sm-2 col-form-label">名稱 (模糊)</label>
            <div class="col-sm-10">
              <input type="text" v-model="searchCriteria.seatsName" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">類型 (模糊)</label>
            <div class="col-sm-10">
              <input type="text" v-model="searchCriteria.seatsType" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">狀態</label>
            <div class="col-sm-10">
              <select v-model="searchCriteria.seatsStatus" class="form-control">
                <option value="">(不限制)</option>
                <option value="可用">可用</option>
                <option value="維修">維修</option>
                <option value="停用">停用</option>
              </select>
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">SpotId (精準)</label>
            <div class="col-sm-10">
              <input type="number" v-model="searchCriteria.spotId" class="form-control" />
            </div>
          </div>

          <div class="form-group row mt-3">
            <label class="col-sm-2 col-form-label">序號 (模糊)</label>
            <div class="col-sm-10">
              <input type="text" v-model="searchCriteria.serialNumber" class="form-control" />
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button type="submit" class="btn btn-info mr-2">查詢</button>
          <router-link to="/admin/seat/list" class="btn btn-secondary">回清單</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchCriteria = ref({
  seatsName: '',
  seatsType: '',
  seatsStatus: '',
  spotId: '',
  serialNumber: '',
})

const handleSearch = () => {
  // [修正] 加上 /admin
  router.push({ path: '/admin/seat/result', query: { ...searchCriteria.value } })
}
</script>
