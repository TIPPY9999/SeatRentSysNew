<template>
  <div class="spot-form">
    <h2>{{ isEdit ? '編輯' : '新增' }}景點</h2>
    <form @submit.prevent="saveSpot">
      <div>
        <label>代碼 (Code):</label>
        <input v-model="formData.spotCode" type="text" required :disabled="isEdit">
      </div>
      <div>
        <label>名稱 (Name):</label>
        <input v-model="formData.spotName" type="text" required>
      </div>
      <div>
        <label>地址 (Address):</label>
        <input v-model="formData.spotAddress" type="text" required>
      </div>
      <div>
        <label>狀態 (Status):</label>
        <input v-model="formData.spotStatus" type="text" required>
      </div>
      <div>
        <label>Merchant ID:</label>
        <input v-model="formData.merchantId" type="number" required>
      </div>
      <div>
        <label>緯度 (Latitude):</label>
        <input v-model="formData.latitude" type="number" step="any">
      </div>
      <div>
        <label>經度 (Longitude):</label>
        <input v-model="formData.longitude" type="number" step="any">
      </div>

      <div class="actions">
        <button type="submit">儲存</button>
        <button type="button" @click="$router.back()">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const formData = ref({
  spotCode: '',
  spotName: '',
  spotAddress: '',
  spotStatus: '',
  merchantId: null,
  latitude: null,
  longitude: null
});

// 判斷是否為編輯模式 (假設路由參數有 id)
const isEdit = computed(() => !!route.params.id);

onMounted(async () => {
  if (isEdit.value) {
    try {
      // [AXIOS GET 請求原理]
      // 1. 目的：向後端要資料來填滿表單。
      // 2. 動作：axios.get('/spot/update', ...) 會發送一個 HTTP GET 請求到後端。
      // 3. 參數：{ params: { spotId: ... } } 會被轉換成 URL 查詢字串，例如：/spot/update?spotId=123
      // 4. 等待：await 會暫停程式執行，直到後端回傳結果。
      const response = await axios.get('/spot/update', { params: { spotId: route.params.id } });
      // 5. 接收：response.data 就是後端回傳的 JSON 物件 (RentalSpot，即租借據點)，我們把它存入 formData，畫面就會自動更新。
      formData.value = response.data;
    } catch (error) {
      console.error('Error fetching spot data:', error);
    }
  }
});

const saveSpot = async () => {
  // [資料打包]
  // 因為後端目前是用 req.getParameter() 接收資料 (傳統表單格式 application/x-www-form-urlencoded)，
  // 所以我們不能直接傳 JSON 物件，必須用 URLSearchParams 把資料包裝成 key=value&key2=value2 的字串格式。
  const params = new URLSearchParams();
  for (const key in formData.value) {
    if (formData.value[key] !== null && formData.value[key] !== undefined) {
      params.append(key, formData.value[key]);
    }
  }
  if (isEdit.value) {
    params.append('spotId', route.params.id);
  }

  try {
    if (isEdit.value) {
      // [AXIOS POST 請求原理]
      // 1. 目的：將打包好的表單資料 (params) 送給後端進行更新。
      // 2. 動作：發送 HTTP POST 請求。
      await axios.post('/spot/update', params);
    } else {
      // 同上，只是路徑改為新增
      await axios.post('/spot/insert', params);
    }
    // 3. 後續：如果沒有報錯 (catch)，代表後端處理成功 (HTTP 200)，我們就跳轉回列表頁。
    router.push('/spot/result');
  } catch (error) {
    console.error('Save failed:', error);
  }
};
</script>

<style scoped>
.spot-form {
  padding: 20px;
}
label {
  display: inline-block;
  width: 120px;
}
div {
  margin-bottom: 10px;
}
.actions {
  margin-top: 20px;
}
button {
  margin-right: 10px;
}
</style>