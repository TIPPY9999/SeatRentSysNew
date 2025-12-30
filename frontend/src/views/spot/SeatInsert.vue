<template>
  <div class="seat-insert">
    <h2>新增 Seat</h2>

    <form @submit.prevent="handleSubmit">
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

      <button type="submit">新增</button>
    </form>

    <br>
    <router-link to="/seat/list">回清單</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const formData = ref({
  seatsName: '',
  seatsType: '',
  seatsStatus: '可用',
  spotId: null,
  serialNumber: ''
});

const handleSubmit = async () => {
  const params = new URLSearchParams();
  for (const key in formData.value) {
    if (formData.value[key] !== null) {
      params.append(key, formData.value[key]);
    }
  }
  await axios.post('/seat/insert', params);
  
  router.push('/seat/list');
};
</script>