<template>
  <div class="spot-list container">
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="header">
          <h2>據點管理列表</h2>
          <!-- 新增搜尋框 -->
          <div class="search-bar">
            <!-- [修復] 補上遺失的關鍵字搜尋框 -->
            <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜尋名稱、代碼或地址"
              class="search-input"
            />
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
              <option value="營運中">啟用</option>
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
              <!-- 建議後端 DTO 補上 merchantName -->
              <td>{{ spot.merchantName || spot.merchantId }}</td>
              <td>
                <span :class="['badge', (spot.spotStatus === '啟用' || spot.spotStatus === 1) ? 'bg-success' : 'bg-secondary']">
                  {{ spot.spotStatus }}
                </span>
              </td>
              <td>
                <!-- [修改] 改用 button 統一操作風格，並呼叫 goToView 函式 -->
                <button
                  class="btn btn-info btn-sm me-1 text-white"
                  @click="goToView(spot.spotId)"
                >
                  詳細
                </button>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 明確定義元件名稱
defineOptions({
  name: 'SpotList'
})

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
    // [修正] 改用 RESTful 風格路徑，對應 RentalSpotController 的 @GetMapping("/api/spots")
    // TODO: 若資料量大，建議將 searchKeyword, searchMerchantId 等參數傳給後端進行過濾，而非前端過濾
    const res = await axios.get('/api/spot/list', {
      // params: {
      //   keyword: searchKeyword.value,
      //   status: searchStatus.value
      // }
    })
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
  router.push({ name: 'spot-add' }) // 改用具名路由，對應 router/index.js 的 name: 'spot-add'
}

// [新增] 跳轉到詳細頁面
const goToView = (id) => {
  // 改用具名路由，對應 router/index.js 的 name: 'spot-view'
  router.push({ name: 'spot-view', params: { id } })
}

// 跳轉到編輯頁面
const goToEdit = (id) => {
  // 改用具名路由，對應 router/index.js 的 name: 'spot-edit'
  router.push({ name: 'spot-edit', params: { id } })
}

// 刪除功能
const deleteSpot = async (id, name) => {
  // 二次確認：顯示更詳細的資訊 (名稱 + ID)，並提示無法復原，增加安全性
  if (!confirm(`確定要刪除據點「${name}」(ID: ${id}) 嗎？\n此動作無法復原！`)) return

  try {
    // [修正] 改用 RESTful 風格：DELETE 方法 + 路徑參數 ID
    // 對應 RentalSpotController 的 @DeleteMapping("/{id}")
    await axios.delete(`/api/spot/${id}`)

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
/* 補上 search-input 樣式 */
.search-bar .search-input {
  width: 200px;
  margin-right: 10px;
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
