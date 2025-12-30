<template>
  <div class="spot-list container">
    <div class="header">
      <h2>據點管理列表</h2>
      <!-- 新增搜尋框 -->
      <div class="search-bar">
        <!-- Merchant ID 搜尋框 ，說明：min="0" 讓點擊上下箭頭時不會變負數；oninput 則是防止使用者直接用鍵盤打 - 號。-->
        <input
          type="number"
          v-model.number="searchMerchantId"
          placeholder="搜尋ID"
          class="search-input-mid"
          min="1"
          step="1"
          @input="sanitizeMerchantId"
        />
        <!-- 狀態下拉選單 -->
        <select v-model="searchStatus" class="search-select">
          <option value="">全部狀態</option>
          <option value="啟用">啟用</option>
          <option value="停用">停用</option>
        </select>
      </div>
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
          <!-- 這裡新增了 Merchant ID 的標題欄位 -->
          <th>Merchant ID</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <!-- 修改：改為遍歷過濾後的 filteredSpots -->
        <tr v-for="spot in filteredSpots" :key="spot.spotId">
          <td>{{ spot.spotId }}</td>
          <td>{{ spot.spotCode }}</td>
          <td>{{ spot.spotName }}</td>
          <td>{{ spot.spotAddress }}</td>
          <!-- 這裡對應顯示每一筆資料的 merchantId 數值 -->
          <td>{{ spot.merchantId }}</td>
          <td>{{ spot.spotStatus }}</td>
          <td>
            <!-- 點擊編輯，跳轉到 SpotForm (帶 ID) -->
            <button class="btn-edit" @click="goToEdit(spot.spotId)">編輯</button>
            <button class="btn-delete" @click="deleteSpot(spot.spotId, spot.spotName)">刪除</button>
          </td>
        </tr>
        <tr v-if="filteredSpots.length === 0">
          <!-- 因為我們新增了 Merchant ID，現在表格總共有 7 個欄位，所以這裡 colspan (跨欄) 要改成 7，讓這行字能置中佔滿整列 -->
          <td colspan="7" style="text-align: center">目前沒有資料</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const spots = ref([])
const searchKeyword = ref('') // 搜尋關鍵字狀態
const searchMerchantId = ref('') // Merchant ID 搜尋狀態
const searchStatus = ref('') // 狀態搜尋狀態

// 防止輸入負數
const sanitizeMerchantId = () => {
  const v = searchMerchantId.value
  // 1. 允許使用者清空欄位 (不套用搜尋條件)
  if (v === '' || v === null || v === undefined) return

  // 2. 如果是非數字或是小於 1，強制變成 1
  if (!Number.isFinite(v) || v < 1) {
    searchMerchantId.value = 1
  }
}

// 載入資料
const loadSpots = async () => {
  try {
    // 呼叫後端 API (對應 RentalSpotController 的 list 方法)
    // 修改：加上 /api 前綴，觸發 Vite 代理
    const res = await axios.get('/api/spot/list')
    spots.value = res.data
  } catch (err) {
    console.error('讀取失敗:', err)
    alert('讀取資料失敗，請檢查後端連線')
  }
}

// 計算屬性：即時過濾資料
const filteredSpots = computed(() => {
  let results = spots.value

  // 1. 關鍵字過濾 (名稱、代碼、地址)
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    results = results.filter(
      (spot) =>
        spot.spotName?.toLowerCase().includes(keyword) ||
        spot.spotCode?.toLowerCase().includes(keyword) ||
        spot.spotAddress?.toLowerCase().includes(keyword),
    )
  }

  // 2. Merchant ID 過濾 (若有輸入才過濾)
  if (searchMerchantId.value !== '' && searchMerchantId.value !== null) {
    const mId = Number(searchMerchantId.value)
    results = results.filter((spot) => Number(spot.merchantId) === mId)
  }

  // 3. 狀態過濾 (若有選擇才過濾)
  if (searchStatus.value) {
    results = results.filter((spot) => spot.spotStatus === searchStatus.value)
  }

  return results
})

// 跳轉到新增頁面
const goToAdd = () => {
  router.push('/admin/spot/add') // 請確認您的 router 設定中，此路徑對應到 SpotForm.vue
}

// 跳轉到編輯頁面
const goToEdit = (id) => {
  router.push(`/admin/spot/edit/${id}`) // 請確認 router 設定包含動態參數，如 path: '/admin/spot/edit/:id'
}

// 刪除功能
const deleteSpot = async (id, name) => {
  // 二次確認：顯示更詳細的資訊 (名稱 + ID)，並提示無法復原，增加安全性
  if (!confirm(`確定要刪除據點「${name}」(ID: ${id}) 嗎？\n此動作無法復原！`)) return

  try {
    // 呼叫後端 API
    // 改用 JSON 傳遞 { spotId: id }
    const params = new URLSearchParams()
    params.append('spotId', String(id))

    await axios.post('/api/spot/delete', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })

    alert('刪除成功')
    await loadSpots()
  } catch (err) {
    console.error('刪除失敗:', err)
    alert('刪除失敗')
  }
}

onMounted(() => {
  loadSpots()
})
</script>

<style scoped>
.spot-list {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
.table th,
.table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
.table th {
  background-color: #f4f4f4;
}

/* 搜尋框樣式 */
.search-bar input {
  padding: 8px;
  width: 250px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-bar .search-input-mid {
  width: 150px; /* 數字欄位可以窄一點 */
  margin-left: 10px;
}

.search-bar .search-select {
  padding: 8px;
  margin-left: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

/* 按鈕樣式 */
button {
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 5px;
  border: none;
  border-radius: 4px;
  color: white;
}
.btn-add {
  background-color: #28a745;
  font-size: 1.1em;
  padding: 8px 16px;
}
.btn-edit {
  background-color: #007bff;
}
.btn-delete {
  background-color: #dc3545;
}
button:hover {
  opacity: 0.9;
}
</style>
