<template>
  <div class="seat-result">
    <h2>Seat 查詢結果</h2>

    <div class="actions">
      <router-link to="/seat/search">回查詢</router-link>
      <router-link to="/seat/list">回清單</router-link>
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
          <td colspan="8" style="text-align: center;">沒有找到符合條件的資料。</td>
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
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const seatList = ref([]);

onMounted(async () => {
  // 取得 URL 查詢參數
  const query = route.query;
  console.log('Search query:', query);
  
  try {
    const response = await axios.get('/seat/condition', { params: query });
    seatList.value = response.data;
  } catch (error) {
    console.error('Error fetching seats:', error);
  }
});
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

.actions {
  margin-bottom: 15px;
}

.actions a {
  margin-right: 15px;
  text-decoration: none;
  color: blue;
}
</style>