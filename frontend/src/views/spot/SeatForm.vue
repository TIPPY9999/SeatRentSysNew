<template>
  <div class="seat-form container">
    <div class="header">
      <h2>{{ isEditMode ? '修改座位' : '新增座位' }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-content">
      <!-- 所屬據點 (下拉選單) -->
      <div class="form-group">
        <label for="spotId">所屬據點 <span class="required">*</span></label>
        <select id="spotId" v-model="formData.spotId" required>
          <option value="" disabled>請選擇據點</option>
          <option v-for="spot in spotList" :key="spot.spotId" :value="spot.spotId">
            {{ spot.spotName }} (ID: {{ spot.spotId }})
          </option>
        </select>
      </div>

      <!-- 座位名稱 -->
      <div class="form-group">
        <label for="seatsName">座位名稱 <span class="required">*</span></label>
        <input
          id="seatsName"
          type="text"
          v-model="formData.seatsName"
          placeholder="例如：A-01"
          required
        />
      </div>

      <!-- 序號 -->
      <div class="form-group">
        <label for="serialNumber">序號</label>
        <input
          id="serialNumber"
          type="text"
          v-model="formData.serialNumber"
          placeholder="例如：SN-2023001"
        />
      </div>

      <!-- 類型 -->
      <div class="form-group">
        <label for="seatsType">類型</label>
        <input
          id="seatsType"
          type="text"
          v-model="formData.seatsType"
          placeholder="例如:高腳椅、塑膠椅..."
        />
      </div>

      <!-- 狀態 -->
      <div class="form-group">
        <label for="seatsStatus">狀態</label>
        <select id="seatsStatus" v-model="formData.seatsStatus">
          <option value="啟用">啟用</option>
          <option value="停用">停用</option>
          <option value="維修中">維修中</option>
        </select>
      </div>

      <div class="actions">
        <button type="button" class="btn-cancel" @click="goBack">取消</button>
        <button type="submit" class="btn-save" :disabled="isSubmitting">{{ isSubmitting ? '儲存中...' : '儲存' }}</button>
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

// 明確定義元件名稱，方便識別與除錯
defineOptions({
  name: 'SeatForm'
})

// 判斷是否為編輯模式 (網址有 id 參數)
const isEditMode = computed(() => !!route.params.id)

// 表單資料
const formData = ref({
  seatsName: '',
  seatsType: '',
  seatsStatus: '啟用',
  spotId: '',
  serialNumber: '',
})

// 防止重複提交的狀態
const isSubmitting = ref(false)

// 據點列表 (供下拉選單使用)
const spotList = ref([])

// 初始化
onMounted(async () => {
  await fetchSpots()
  
  if (isEditMode.value) {
    await fetchSeatData(route.params.id)
  }
})

// 取得所有據點 (用於下拉選單)
const fetchSpots = async () => {
  try {
    const res = await axios.get('/api/spot/list')
    spotList.value = res.data
  } catch (err) {
    console.error('無法取得據點列表:', err)
    alert('無法載入據點列表，請檢查後端連線')
  }
}

// 取得單一座位資料 (編輯模式用)
const fetchSeatData = async (id) => {
  if (!id) {
    console.warn('未提供座位ID，無法取得資料')
    return
  }
  try {
    const res = await axios.get(`/api/seats/${id}`)
    // 將後端資料填入表單
    formData.value = {
      seatsId: res.data.seatsId, // 雖然表單不顯示，但更新時可能需要
      seatsName: res.data.seatsName,
      seatsType: res.data.seatsType,
      seatsStatus: res.data.seatsStatus,
      spotId: res.data.spotId,
      serialNumber: res.data.serialNumber,
    }
  } catch (err) {
    console.error('無法取得座位資料:', err)
    alert('讀取資料失敗')
    router.push('/admin/seat/list')
  }
}

// 送出表單
const handleSubmit = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    if (isEditMode.value) {
      // 更新 (PUT)
      const seatId = route.params.id
      if (!seatId) {
        throw new Error('無法取得座位ID')
      }
      await axios.put(`/api/seats/${seatId}`, formData.value)
      alert('更新成功')
    } else {
      // 新增 (POST)
      await axios.post('/api/seats', formData.value)
      alert('新增成功')
    }
    // 成功後返回列表
    router.push('/admin/seat/list')
  } catch (err) {
    console.error('儲存失敗:', err)
    alert('儲存失敗，請檢查輸入資料')
  } finally {
    isSubmitting.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.seat-form {
  padding: 20px;
  max-width: 600px; /* 限制表單寬度，比較美觀 */
  margin: 0 auto;   /* 置中 */
}

.header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.required {
  color: red;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* 確保 padding 不會撐大寬度 */
}

.actions {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-save {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.btn-save:disabled {
  background-color: #94d3a2;
  cursor: not-allowed;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.btn-save:hover {
  background-color: #218838;
}

.btn-cancel:hover {
  background-color: #5a6268;
}
</style>