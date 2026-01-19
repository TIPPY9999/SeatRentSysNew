<template>
  <div class="seat-list container">
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="header">
          <h2>座位管理列表</h2>
          <!-- 搜尋區塊 -->
          <!-- [修改] 參考 SpotList.vue 的搜尋介面 -->
          <div class="search-bar">
            <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜尋名稱、類型或序號"
              class="search-input"
            />
            <input
              type="number"
              v-model.number="searchSpotId"
              placeholder="搜尋據點ID"
              class="search-input-mid"
              min="1"
              step="1"
              @input="sanitizeSpotId"
            />
            <select v-model="searchStatus" class="search-select">
              <option value="">全部狀態</option>
              <option value="啟用">啟用</option>
              <option value="停用">停用</option>
              <option value="維修中">維修中</option>
            </select>
          </div>
          <button class="btn-add" @click="goToAdd">新增座位</button>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名稱</th>
              <th>類型</th>
              <th>序號</th>
              <th>所屬據點 ID</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="seat in filteredSeats" :key="seat.seatsId">
              <td>{{ seat.seatsId }}</td>
              <td>{{ seat.seatsName }}</td>
              <td>{{ seat.seatsType }}</td>
              <td>{{ seat.serialNumber }}</td>
              <!-- 建議後端 DTO 回傳 spotName，若無則顯示 ID -->
              <td>{{ seat.spotName || seat.spotId }}</td>
              <td>
                <!-- 增加對後端可能回傳 1/0 或 ACTIVE/INACTIVE 的相容性判斷 -->
                <span
                  :class="['badge', (seat.seatsStatus === '啟用' || seat.seatsStatus === 1 || seat.seatsStatus === 'ACTIVE') ? 'bg-success' : 'bg-danger']">
                  {{ seat.seatsStatus }}
                </span>
              </td>
              <td>
                <button class="btn btn-info btn-sm me-1 text-white" 
                @click="goToView(seat.seatsId)">
                  詳細
                </button>
                
                <button class="btn-edit" @click="goToEdit(seat.seatsId)">
                  編輯
                </button>
                <button class="btn-delete" @click="deleteSeat(seat.seatsId)">
                  刪除
                </button>
              </td>
            </tr>
            <tr v-if="filteredSeats.length === 0">
              <td colspan="7" style="text-align: center">
                {{ loading ? '載入中...' : '查無資料' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router' // [新增] 引入 useRouter
import axios from 'axios'
import Swal from 'sweetalert2' // 假設您有安裝，若無可改用 alert

// 明確定義元件名稱
defineOptions({
  name: 'SeatList'
})

const router = useRouter() // [新增] 初始化 router
const seats = ref([])
const loading = ref(false)

// [新增] 參考 SpotList.vue 的搜尋狀態
const searchKeyword = ref('')
const searchSpotId = ref('')
const searchStatus = ref('')

// [新增] 參考 SpotList.vue 的數字輸入處理函式，並調整變數名稱
const sanitizeSpotId = () => {
  const v = searchSpotId.value
  if (v === '' || v === null || v === undefined) return

  if (!Number.isFinite(v) || v < 1) {
    searchSpotId.value = 1
  }
}

// [新增] 跳轉到詳細頁 (使用具名路由，更安全)
const goToView = (id) => {
  router.push({ name: 'seat-view', params: { id } })
}

// [新增] 跳轉到編輯頁
const goToEdit = (id) => {
  router.push({ name: 'seat-edit', params: { id } })
}

// [新增] 跳轉到新增頁
const goToAdd = () => {
  router.push({ name: 'seat-insert' })
}

// 查詢座位
const fetchSeats = async () => {
  loading.value = true
  try {
    // [修改] 為了實現前端過濾，移除請求參數，一次性獲取所有資料
    const res = await axios.get('/api/seats/search')
    seats.value = res.data
  } catch (err) {
    console.error('載入失敗:', err)
    Swal.fire('錯誤', '無法載入座位列表', 'error')
  } finally {
    loading.value = false
  }
}

// [新增] 參考 SpotList.vue 的計算屬性，並根據 Seat 的欄位調整過濾邏輯
const filteredSeats = computed(() => {
  let results = seats.value

  // 1. 關鍵字過濾 (對應 Seat 的名稱、類型、序號)
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    results = results.filter(
      (seat) =>
        seat.seatsName?.toLowerCase().includes(keyword) ||
        seat.seatsType?.toLowerCase().includes(keyword) ||
        seat.serialNumber?.toLowerCase().includes(keyword)
    )
  }

  // 2. 據點 ID 過濾 (對應 Seat 的 spotId)
  if (searchSpotId.value !== '' && searchSpotId.value !== null) {
    const sId = Number(searchSpotId.value)
    results = results.filter((seat) => Number(seat.spotId) === sId)
  }

  // 3. 狀態過濾 (對應 Seat 的 seatsStatus)
  if (searchStatus.value) {
    results = results.filter((seat) => seat.seatsStatus === searchStatus.value)
  }

  return results
})

// 刪除座位
const deleteSeat = async (id) => {
  const result = await Swal.fire({
    title: '確定刪除?',
    text: "刪除後將無法復原！",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '是的，刪除'
  })

  if (result.isConfirmed) {
    try {
      await axios.delete(`/api/seats/${id}`)
      Swal.fire('已刪除', '該座位已被移除', 'success')
      fetchSeats() // 重新整理列表
    } catch (err) {
      Swal.fire('失敗', '刪除失敗，可能尚有相關聯的訂單', 'error')
    }
  }
}

onMounted(() => {
  fetchSeats()
})
</script>

<style scoped>
.seat-list {
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
.search-bar .search-input {
  width: 200px; /* 調整寬度以容納更多欄位 */
  margin-right: 10px;
}

/* [新增] 參考 SpotList.vue 的樣式 */
.search-bar .search-input-mid {
  width: 150px;
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