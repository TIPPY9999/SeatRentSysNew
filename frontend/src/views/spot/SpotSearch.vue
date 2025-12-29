<template>
  <div class="spot-search">
    <h2>景點查詢</h2>
    <form @submit.prevent="handleSearch">
      <div>
        <label for="spotName">景點名稱：</label>
        <input type="text" id="spotName" v-model="searchCriteria.spotName">
      </div>
      
      <div>
        <label for="spotCode">景點代碼：</label>
        <input type="text" id="spotCode" v-model="searchCriteria.spotCode">
      </div>
      
      <div>
        <label for="spotStatus">狀態：</label>
        <input type="text" id="spotStatus" v-model="searchCriteria.spotStatus" placeholder="啟用/停用">
      </div>

      <div>
        <label for="merchantId">商家ID：</label>
        <input type="number" id="merchantId" v-model="searchCriteria.merchantId">
      </div>

      <div class="actions">
        <button type="submit">查詢</button>
        <router-link to="/spot/add" class="btn-link">新增景點</router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchCriteria = ref({
  spotName: '',
  spotCode: '',
  spotStatus: '',
  merchantId: ''
});

const handleSearch = () => {
  // 將查詢條件帶入 URL query 參數，跳轉至結果頁
  router.push({ 
    path: '/spot/result', 
    query: { ...searchCriteria.value } 
  });
};
</script>

<style scoped>
.spot-search {
  padding: 20px;
}
.actions {
  margin-top: 15px;
}
.btn-link {
  margin-left: 10px;
  text-decoration: none;
  color: blue;
}
</style>