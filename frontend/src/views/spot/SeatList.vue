<template>
  <div class="seat-list">
    <h2>Seat 清單</h2>

    <div class="actions">
      <router-link to="/seat/insert">新增 Seat</router-link>
      <router-link to="/seat/search">條件查詢</router-link>
    </div>

    <table>
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
          <td colspan="8" style="text-align: center;">目前沒有資料。</td>
        </tr>
        <tr v-for="s in seatList" :key="s.seatsId">
          <td>{{ s.seatsId }}</td>
          <td>{{ s.seatsName }}</td>
          <td>{{ s.seatsType }}</td>
          <td>{{ s.seatsStatus }}</td>
          <td>{{ s.spotId }}</td>
          <td>{{ s.serialNumber }}</td>
          <td>{{ s.updatedAt }}</td>
          <td>
            <router-link :to="`/seat/view/${s.seatsId}`">詳細</router-link>
            <router-link :to="`/seat/edit/${s.seatsId}`">修改</router-link>
            <button @click="deleteSeat(s.seatsId)">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const seatList = ref([]);

onMounted(async () => {
  fetchSeats();
});

const fetchSeats = async () => {
  try {
    const response = await axios.get('/seat/list');
    seatList.value = response.data;
  } catch (error) {
    console.error('Error fetching seats:', error);
  }
};

const deleteSeat = async (id) => {
  if (!confirm('確定刪除?')) return;
  
  try {
    const params = new URLSearchParams();
    params.append('seatsId', id);
    await axios.post('/seat/delete', params);
    // 刪除成功後重新整理列表
    fetchSeats();
  } catch (error) {
    console.error('Delete failed:', error);
  }
};
</script>

<style scoped>
table {
  width: 95%;
  border-collapse: collapse;
  margin-top: 12px;
}

th, td {
  border: 1px solid #aaa;
  padding: 8px;
}

th {
  background: #eee;
}

a, button {
  margin-right: 10px;
}

.actions {
  margin-bottom: 15px;
}

.actions a {
  margin-right: 15px;
  text-decoration: none;
  color: blue;
}
</style>