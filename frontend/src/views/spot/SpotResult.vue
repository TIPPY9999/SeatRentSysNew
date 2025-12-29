<template>
  <div class="spot-result">
    <h2>查詢結果列表</h2>

    <div v-if="spotList.length === 0">
      <p>沒有找到符合條件的資料。</p>
    </div>

    <div v-else>
      <table border="1" cellpadding="5">
        <thead>
          <tr>
            <th>ID</th>
            <th>代碼</th>
            <th>名稱</th>
            <th>地址</th>
            <th>狀態</th>
            <th>Merchant ID</th>
            <th>緯度</th>
            <th>經度</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="spot in spotList" :key="spot.spotId">
            <td>{{ spot.spotId }}</td>
            <td>{{ spot.spotCode }}</td>
            <td>{{ spot.spotName }}</td>
            <td>{{ spot.spotAddress }}</td>
            <td>{{ spot.spotStatus }}</td>
            <td>{{ spot.merchantId }}</td>
            <td>{{ spot.latitude }}</td>
            <td>{{ spot.longitude }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <br>
    <router-link to="/spot/search">返回搜尋頁</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const spotList = ref([]);

onMounted(async () => {
  const query = route.query;
  try {
    // [AXIOS GET 請求原理]
    // 1. 動作：發送 GET 請求到 /spot/condition，並帶上查詢參數 (query)。
    const response = await axios.get('/spot/condition', { params: query });
    // 2. 接收：後端回傳的是一個 JSON 陣列 (List<RentalSpot>，即租借據點列表)。
    // 3. 更新：將資料存入 spotList，Vue 的 v-for 就會自動把表格畫出來。
    spotList.value = response.data;
  } catch (error) {
    console.error('Error fetching spots:', error);
  }
});
</script>

<style scoped>
/* 可根據需要添加樣式 */
</style>