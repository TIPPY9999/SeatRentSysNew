<template>
  <div class="seat-one container-fluid">
    <div class="card card-warning card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">Seat 詳細資料</h3>
      </div>

      <div class="card-body">
        <div v-if="loading">
          <p>載入中...</p>
        </div>
        <div v-else-if="!seat">
          <p class="text-danger">找不到資料</p>
        </div>
        <div v-else>
          <table class="table table-bordered table-striped">
            <tbody>
              <tr>
                <th style="width: 200px">ID</th>
                <td>{{ seat.seatsId }}</td>
              </tr>
              <tr>
                <th>名稱</th>
                <td>{{ seat.seatsName }}</td>
              </tr>
              <tr>
                <th>類型</th>
                <td>{{ seat.seatsType }}</td>
              </tr>
              <tr>
                <th>狀態</th>
                <td>{{ seat.seatsStatus }}</td>
              </tr>
              <tr>
                <th>SpotId</th>
                <td>{{ seat.spotId }}</td>
              </tr>
              <tr>
                <th>序號</th>
                <td>{{ seat.serialNumber }}</td>
              </tr>
              <tr>
                <th>CreatedAt</th>
                <td>{{ seat.createdAt }}</td>
              </tr>
              <tr>
                <th>UpdatedAt</th>
                <td>{{ seat.updatedAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer">
        <router-link to="/admin/seat/list" class="btn btn-secondary">回清單</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'


// 明確定義元件名稱，方便識別與除錯
defineOptions({
  name: 'SeatOne'
})
const route = useRoute()
const seat = ref(null)
const loading = ref(true)

onMounted(async () => {
  const seatId = route.params.id
  if (seatId) {
    try {
      // [修正] 改用 RESTful 風格: GET /api/seats/{id}
      const response = await axios.get(`/api/seats/${seatId}`)
      seat.value = response.data
    } catch (error) {
      console.error('Error fetching seat:', error)
    }
  }
  loading.value = false
})
</script>
