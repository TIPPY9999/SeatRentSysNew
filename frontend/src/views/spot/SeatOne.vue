<template>
  <div class="seat-one">
    <h2>Seat 詳細資料</h2>

    <div v-if="loading">
      <p>載入中...</p>
    </div>
    <div v-else-if="!seat">
      <p>找不到資料</p>
    </div>
    <div v-else>
      <table>
        <tr>
          <th>ID</th>
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
      </table>
    </div>

    <br>
    <router-link to="/seat/list">回清單</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const seat = ref(null);
const loading = ref(true);

onMounted(async () => {
  // 假設路由定義為 /seat/view/:id
  const seatId = route.params.id;
  if (seatId) {
    try {
      const response = await axios.get('/seat/one', { params: { seatsId: seatId } });
      seat.value = response.data;
    } catch (error) {
      console.error('Error fetching seat:', error);
    }
  }
  loading.value = false;
});
</script>

<style scoped>
table {
  width: 60%;
  border-collapse: collapse;
  margin-top: 12px;
}

th, td {
  border: 1px solid #aaa;
  padding: 8px;
}

th {
  background: #eee;
  width: 160px;
}
</style>