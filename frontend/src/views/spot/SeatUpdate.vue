<template>
  <div class="seat-update">
    <h2>修改 Seat</h2>

    <div v-if="loading">
      <p>載入中...</p>
    </div>
    <div v-else-if="!formData">
      <p>找不到資料</p>
    </div>
    <div v-else>
      <form @submit.prevent="handleUpdate">
        <div>
          <label>名稱：</label>
          <input type="text" v-model="formData.seatsName" required>
        </div>
        <br>
        <div>
          <label>類型：</label>
          <input type="text" v-model="formData.seatsType" required>
        </div>
        <br>
        <div>
          <label>狀態：</label>
          <select v-model="formData.seatsStatus" required>
            <option value="可用">可用</option>
            <option value="維修">維修</option>
            <option value="停用">停用</option>
          </select>
        </div>
        <br>
        <div>
          <label>SpotId（可空）：</label>
          <input type="number" v-model="formData.spotId">
        </div>
        <br>
        <div>
          <label>序號（可空）：</label>
          <input type="text" v-model="formData.serialNumber">
        </div>
        <br>

        <button type="submit">更新</button>
      </form>
    </div>

    <br>
    <router-link to="/seat/list">回清單</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const formData = ref(null);

onMounted(async () => {
  const id = route.params.id; // 假設路由為 /seat/edit/:id
  if (id) {
    try {
      const res = await axios.get('/seat/update', { params: { seatsId: id } });
      formData.value = res.data;
    } catch (error) {
      console.error('Error fetching seat:', error);
    }
  }
  loading.value = false;
});

const handleUpdate = async () => {
  const params = new URLSearchParams();
  for (const key in formData.value) {
    if (formData.value[key] !== null) {
      params.append(key, formData.value[key]);
    }
  }
  // 確保 ID 被包含
  params.append('seatsId', formData.value.seatsId);

  await axios.post('/seat/update', params);
  
  router.push('/seat/list');
};
</script>