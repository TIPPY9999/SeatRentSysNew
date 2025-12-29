<template>
  <div class="seat-search">
    <h2>Seat 條件查詢</h2>

    <form @submit.prevent="handleSearch">
      <div>
        <label>名稱(模糊)：</label>
        <input type="text" v-model="searchCriteria.seatsName">
      </div>
      <br>
      <div>
        <label>類型(模糊)：</label>
        <input type="text" v-model="searchCriteria.seatsType">
      </div>
      <br>
      <div>
        <label>狀態：</label>
        <select v-model="searchCriteria.seatsStatus">
          <option value="">(不限制)</option>
          <option value="可用">可用</option>
          <option value="維修">維修</option>
          <option value="停用">停用</option>
        </select>
      </div>
      <br>
      <div>
        <label>SpotId(精準)：</label>
        <input type="number" v-model="searchCriteria.spotId">
      </div>
      <br>
      <div>
        <label>序號(模糊)：</label>
        <input type="text" v-model="searchCriteria.serialNumber">
      </div>
      <br>

      <button type="submit">查詢</button>
    </form>

    <br>
    <router-link to="/seat/list">回清單</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchCriteria = ref({
  seatsName: '',
  seatsType: '',
  seatsStatus: '',
  spotId: '',
  serialNumber: ''
});

const handleSearch = () => {
  // 將查詢條件帶入 URL query 參數，跳轉至結果頁
  // 過濾掉空字串可視後端需求決定，這裡直接傳遞
  router.push({ path: '/seat/result', query: { ...searchCriteria.value } });
};
</script>