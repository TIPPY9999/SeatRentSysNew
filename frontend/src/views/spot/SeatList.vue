<template>
  <div class="seat-list container">
    <div class="header">
      <h2>Seat 清單</h2>
      <div class="actions">
        <router-link to="/admin/seat/search" class="btn-search">條件查詢</router-link>
        <button class="btn-add" @click="$router.push('/admin/seat/insert')">新增 Seat</button>
      </div>
    </div>

    <table class="table">
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
        <tr v-if="seatList.length === 0">
          <td colspan="8" style="text-align: center">目前沒有資料。</td>
        </tr>
        <tr v-for="s in seatList" :key="s.seatsId">
          <td>{{ s.seatsId }}</td>
          <td>{{ s.seatsName }}</td>
          <td>{{ s.seatsType }}</td>
          <td>{{ s.seatsStatus }}</td>
          <td>{{ s.spotId }}</td>
          <td>{{ s.serialNumber }}</td>
          <td>{{ s.updatedAt?.replace('T', ' ').substring(0, 19) }}</td>
          <td>
            <router-link :to="`/admin/seat/view/${s.seatsId}`" class="btn-detail">詳細</router-link>
            <router-link :to="`/admin/seat/edit/${s.seatsId}`" class="btn-edit">修改</router-link>
            <button class="btn-delete" @click="deleteSeat(s.seatsId, s.seatsName)">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const seatList = ref([])

onMounted(async () => {
  fetchSeats()
})

const fetchSeats = async () => {
  try {
    // [修正 API 路徑] 加上 /api
    const response = await axios.get('/api/seat/list')
    seatList.value = response.data
  } catch (error) {
    console.error('Error fetching seats:', error)
  }
}

const deleteSeat = async (id, name) => {
  if (!confirm(`確定刪除座位 ${name} (ID: ${id})?`)) return
  try {
    // [修正 API 路徑與參數] 使用 URLSearchParams 確保後端讀取正確
    const params = new URLSearchParams()
    params.append('seatsId', String(id))

    await axios.post('/api/seat/delete', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })

    fetchSeats() // 重新整理列表
    alert('刪除成功')
  } catch (error) {
    console.error('Delete failed:', error)
    alert('刪除失敗')
  }
}
</script>

<style scoped>
/* [UI 優化] 移植 SpotList 的 CSS 樣式 */
.seat-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.table th,
.table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
.table th {
  background-color: #f4f4f4;
}

/* 按鈕樣式 */
button,
.btn-detail,
.btn-edit,
.btn-search {
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 5px;
  border: none;
  border-radius: 4px;
  color: white;
  text-decoration: none; /* 移除超連結底線 */
  display: inline-block;
  font-size: 14px;
}

.btn-add {
  background-color: #28a745;
  font-size: 1.1em;
  padding: 8px 16px;
}
.btn-search {
  background-color: #17a2b8;
  font-size: 1.1em;
  padding: 8px 16px;
  margin-right: 10px;
}
.btn-detail {
  background-color: #17a2b8;
}
.btn-edit {
  background-color: #007bff;
}
.btn-delete {
  background-color: #dc3545;
}

button:hover,
a:hover {
  opacity: 0.9;
}
</style>
