<template>
  <div class="spot-list container">
    <div class="header">
      <h2>據點管理列表</h2>
      <!-- 點擊新增，跳轉到 SpotForm (無 ID) -->
      <button class="btn-add" @click="goToAdd">新增據點</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>代碼</th>
          <th>名稱</th>
          <th>地址</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="spot in spots" :key="spot.spotId">
          <td>{{ spot.spotId }}</td>
          <td>{{ spot.spotCode }}</td>
          <td>{{ spot.spotName }}</td>
          <td>{{ spot.spotAddress }}</td>
          <td>{{ spot.spotStatus }}</td>
          <td>
            <!-- 點擊編輯，跳轉到 SpotForm (帶 ID) -->
            <button class="btn-edit" @click="goToEdit(spot.spotId)">編輯</button>
            <button class="btn-delete" @click="deleteSpot(spot.spotId)">刪除</button>
          </td>
        </tr>
        <tr v-if="spots.length === 0">
          <td colspan="6" style="text-align: center;">目前沒有資料</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const spots = ref([]);

// 載入資料
const loadSpots = async () => {
  try {
    // 呼叫後端 API (對應 RentalSpotController 的 list 方法)
    const res = await axios.get('/spot/list');
    spots.value = res.data;
  } catch (err) {
    console.error('讀取失敗:', err);
    alert('讀取資料失敗，請檢查後端連線');
  }
};

// 跳轉到新增頁面
const goToAdd = () => {
  router.push('/spot/add'); // 請確認您的 router 設定中，此路徑對應到 SpotForm.vue
};

// 跳轉到編輯頁面
const goToEdit = (id) => {
  router.push(`/spot/edit/${id}`); // 請確認 router 設定包含動態參數，如 path: '/spot/edit/:id'
};

// 刪除功能
const deleteSpot = async (id) => {
  if (!confirm('確定要刪除這筆資料嗎？')) return;
  
  try {
    const params = new URLSearchParams();
    params.append('spotId', id);
    // 呼叫後端 API
    await axios.post('/spot/delete', params);
    alert('刪除成功');
    loadSpots(); // 重新整理列表
  } catch (err) {
    console.error('刪除失敗:', err);
    alert('刪除失敗');
  }
};

onMounted(() => {
  loadSpots();
});
</script>

<style scoped>
.spot-list { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.table th, .table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
.table th { background-color: #f4f4f4; }

/* 按鈕樣式 */
button { cursor: pointer; padding: 5px 10px; margin-right: 5px; border: none; border-radius: 4px; color: white; }
.btn-add { background-color: #28a745; font-size: 1.1em; padding: 8px 16px; }
.btn-edit { background-color: #007bff; }
.btn-delete { background-color: #dc3545; }
button:hover { opacity: 0.9; }
</style>