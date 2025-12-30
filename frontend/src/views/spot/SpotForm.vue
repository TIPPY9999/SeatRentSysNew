<template>
  <div class="spot-form">
    <h2>{{ isEdit ? '編輯' : '新增' }}景點</h2>
    <form @submit.prevent="saveSpot">
      <div>
        <label>代碼 (Code):</label>
        <input v-model="formData.spotCode" type="text" required :disabled="isEdit" />
      </div>
      <div>
        <label>名稱 (Name):</label>
        <input v-model="formData.spotName" type="text" required />
      </div>
      <div>
        <label>地址 (Address):</label>
        <input v-model="formData.spotAddress" type="text" required />
      </div>
      <div>
        <label>狀態 (Status):</label>
        <select v-model="formData.spotStatus" required>
          <option value="" disabled>請選擇狀態</option>
          <option value="啟用">啟用</option>
          <option value="停用">停用</option>
        </select>
      </div>
      <div>
        <label>Merchant ID:</label>
        <input v-model="formData.merchantId" type="number" required />
      </div>
      <div>
        <label>緯度 (Latitude):</label>
        <input v-model="formData.latitude" type="number" step="any" />
      </div>
      <div>
        <label>經度 (Longitude):</label>
        <input v-model="formData.longitude" type="number" step="any" />
      </div>

      <div class="actions">
        <button type="submit">儲存</button>
        <button type="button" @click="$router.back()">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const formData = ref({
  spotCode: '',
  spotName: '',
  spotAddress: '',
  spotStatus: '',
  merchantId: null,
  latitude: null,
  longitude: null,
})

// 判斷是否為編輯模式 (假設路由參數有 id)
const isEdit = computed(() => !!route.params.id)

onMounted(async () => {
  if (isEdit.value) {
    try {
      // [AXIOS GET 請求原理]
      // 1. 目的：向後端要資料來填滿表單。
      // 2. 動作：axios.get('/spot/update', ...) 會發送一個 HTTP GET 請求到後端。
      // 3. 參數：{ params: { spotId: ... } } 會被轉換成 URL 查詢字串，例如：/spot/update?spotId=123
      // 4. 等待：await 會暫停程式執行，直到後端回傳結果。
      const response = await axios.get('/api/spot/update', { params: { spotId: route.params.id } })
      // 5. 接收：response.data 就是後端回傳的 JSON 物件 (RentalSpot，即租借據點)，我們把它存入 formData，畫面就會自動更新。
      formData.value = response.data
    } catch (error) {
      console.error('Error fetching spot data:', error)
    }
  }
})

const saveSpot = async () => {
  // [優化邏輯] 改用 JSON 傳輸
  // 直接使用 formData 物件，Axios 會自動將其序列化為 JSON

  // [新增] 前端驗證邏輯：確保必填欄位不為空，且不能只輸入空白鍵
  if (
    !formData.value.spotCode?.trim() ||
    !formData.value.spotName?.trim() ||
    !formData.value.spotAddress?.trim() ||
    !formData.value.spotStatus?.trim() ||
    !formData.value.merchantId
  ) {
    alert('請檢查所有必填欄位 (代碼、名稱、地址、狀態、Merchant ID) 是否皆已填寫！')
    return // 驗證失敗，中斷執行，不發送請求
  }

  if (isEdit.value) {
    // 確保 ID 包含在資料中
    formData.value.spotId = route.params.id
  }

  try {
    if (isEdit.value) {
      // 發送 JSON 資料
      await axios.post('/api/spot/update', formData.value)
    } else {
      // 同上，只是路徑改為新增
      await axios.post('/api/spot/insert', formData.value)
    }
    // 3. 後續：如果沒有報錯 (catch)，代表後端處理成功 (HTTP 200)，我們就跳轉回列表頁。
    router.push('/admin/spot/list')
  } catch (error) {
    console.error('Save failed:', error)
  }
}
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
