<template>
  <div class="seat-result container-fluid">
    <div class="card card-primary card-outline mt-3">
      <div class="card-header">
        <h3 class="card-title">Seat 查詢結果</h3>
      </div>

      <div class="card-body">
        <div v-if="seatList.length === 0" class="alert alert-warning text-center">
          沒有找到符合條件的資料。
        </div>

        <div v-else>
          <table class="table table-bordered table-hover table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>名稱</th>
                <th>類型</th>
                <th>狀態</th>
                <th>SpotId</th>
                <th>序號</th>
                <th>UpdatedAt</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in seatList" :key="s.seatsId">
                <td>{{ s.seatsId }}</td>
                <td>{{ s.seatsName }}</td>
                <td>{{ s.seatsType }}</td>
                <td>
                  <span :class="getStatusClass(s.seatsStatus)">
                    {{ s.seatsStatus }}
                  </span>
                </td>
                <td>{{ s.spotId }}</td>
                <td>{{ s.serialNumber }}</td>
                <td>{{ s.updatedAt?.replace('T', ' ').substring(0, 19) }}</td>
                <td>
                  <router-link
                    :to="`/admin/seat/view/${s.seatsId}`"
                    class="btn btn-sm btn-info mr-1"
                    >詳細</router-link
                  >
                  <router-link :to="`/admin/seat/edit/${s.seatsId}`" class="btn btn-sm btn-primary"
                    >修改</router-link
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer">
        <router-link to="/admin/seat/search" class="btn btn-secondary mr-2">回查詢</router-link>
        <router-link to="/admin/seat/list" class="btn btn-default">回清單</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const seatList = ref([])

// 明確定義元件名稱，方便識別與除錯
defineOptions({
  name: 'SeatResult'
})

onMounted(async () => {
  const query = route.query
  console.log('Search query:', query)

  try {
    // 改用 RESTful API: GET /api/seats/search (搭配查詢參數)
    const response = await axios.get('/api/seats/search', { params: query })
    seatList.value = response.data
  } catch (error) {
    console.error('Error fetching seats:', error)
  }
})

// [新增] 簡單的狀態顏色輔助函式
const getStatusClass = (status) => {
  if (status === '可用') return 'badge badge-success'
  if (status === '維修' || status === '故障') return 'badge badge-warning'
  if (status === '停用') return 'badge badge-danger'
  return 'badge badge-secondary'
}
</script>

<style scoped>
/* 微調按鈕間距 */
.mr-1 {
  margin-right: 5px;
}
.mr-2 {
  margin-right: 10px;
}
</style>
