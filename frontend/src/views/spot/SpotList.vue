<template>
  <div class="spot-list container">
    <!-- [Proposal 2] 迷你儀表板 -->
    <div class="row mb-4">
      <div class="col-md-3 col-sm-6">
        <div class="small-box bg-info">
          <div class="inner">
            <h3>{{ statsSummary.totalSpots || 0 }}</h3>
            <p>總據點數</p>
          </div>
          <div class="icon">
            <i class="fas fa-map-marker-alt"></i>
          </div>
          <a href="#" class="small-box-footer" @click.prevent="goToAnalyze">
            更多資訊 <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="small-box bg-success">
          <div class="inner">
            <h3>{{ statsSummary.activeSeats || 0 }}</h3>
            <p>營運座位</p>
          </div>
          <div class="icon">
            <i class="fas fa-chair"></i>
          </div>
          <a href="#" class="small-box-footer" @click.prevent="goToAnalyze">
            更多資訊 <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="small-box bg-warning">
          <div class="inner">
            <h3>{{ statsSummary.todayRents || 0 }}</h3>
            <p>今日租借</p>
          </div>
          <div class="icon">
            <i class="fas fa-file-invoice dollar"></i>
          </div>
          <a href="#" class="small-box-footer" @click.prevent="goToAnalyze">
            更多資訊 <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="small-box bg-danger">
          <div class="inner">
            <h3>{{ statsSummary.maintenance || 0 }}</h3>
            <p>維修中</p>
          </div>
          <div class="icon">
            <i class="fas fa-tools"></i>
          </div>
          <a href="#" class="small-box-footer" @click.prevent="goToAnalyze">
            更多資訊 <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
    </div>

    <!-- 列表模式內容 -->
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="header">
          <h2>據點管理列表</h2>
          <!-- 新增搜尋框 -->
          <div class="search-bar">
            <input type="text" v-model="searchKeyword" placeholder="搜尋名稱、代碼或地址" class="search-input" />
            <input type="number" v-model.number="searchMerchantId" placeholder="搜尋ID" class="search-input-mid" min="1"
              step="1" @input="sanitizeMerchantId" />
            <select v-model="searchStatus" class="search-select">
              <option value="">全部狀態</option>
              <option value="營運中">營運中</option>
              <option value="停用">停用</option>
              <option value="維修中">維修中</option>
            </select>
          </div>
          <div>
            <button class="btn-monitor" @click="goToMonitor">
              <i class="fas fa-desktop"></i> 調度監控
            </button>
            <button class="btn-add" @click="goToAdd">新增據點</button>
          </div>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>代碼</th>
              <th>名稱</th>
              <th>地址</th>
              <th>Merchant ID</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="spot in filteredSpots" :key="spot.spotId">
              <td>{{ spot.spotId }}</td>
              <td>{{ spot.spotCode }}</td>
              <td>{{ spot.spotName }}</td>
              <td>{{ spot.spotAddress }}</td>
              <td>{{ spot.merchantName || spot.merchantId }}</td>
              <td>
                <span
                  :class="['badge', (spot.spotStatus === '啟用' || spot.spotStatus === 1) ? 'bg-success' : 'bg-secondary']">
                  {{ spot.spotStatus }}
                </span>
              </td>
              <td>
                <button class="btn btn-info btn-sm me-1 text-white" @click="goToView(spot.spotId)">
                  詳細
                </button>
                <button class="btn-edit" @click="goToEdit(spot.spotId)">編輯</button>
                <button class="btn-delete" @click="deleteSpot(spot.spotId, spot.spotName)">刪除</button>
              </td>
            </tr>
            <tr v-if="filteredSpots.length === 0">
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

defineOptions({
  name: 'SpotList'
})

const router = useRouter()
const spots = ref([])
const searchKeyword = ref('')
const searchMerchantId = ref('')
const searchStatus = ref('')
const statsSummary = ref({ totalSpots: 0, activeSeats: 0, todayRents: 0, maintenance: 0 })

const sanitizeMerchantId = () => {
  const v = searchMerchantId.value
  if (v === '' || v === null || v === undefined) return
  if (!Number.isFinite(v) || v < 1) {
    searchMerchantId.value = 1
  }
}

const loadSpots = async () => {
  try {
    // 1. 平行呼叫兩個 API：列表 + 統計數據
    const [resList, resStats] = await Promise.all([
      axios.get('/api/spot/list'),
      axios.get('/api/analyze/stats')
    ])

    // 2. 更新列表
    spots.value = resList.data

    // 3. 計算並更新統計卡片
    calculateStats(resList.data, resStats.data)

  } catch (err) {
    console.error('讀取失敗:', err)
    alert('讀取資料失敗，請檢查後端連線')
  }
}

const calculateStats = (listData, statsData) => {
  // A. 從列表計算的基本數據
  statsSummary.value.totalSpots = listData.length
  statsSummary.value.maintenance = listData.filter(s => s.spotStatus === '維修中').length

  // B. 從統計 API 取得的進階數據 (spotMonitor 陣列)
  const monitorList = statsData?.spotMonitor || []

  // 用 reduce 加總所有站點的數據
  statsSummary.value.activeSeats = monitorList.reduce((sum, item) => sum + (item.totalSeats || 0), 0)
  statsSummary.value.todayRents = monitorList.reduce((sum, item) => sum + (item.rentedCount || 0), 0)
}

const filteredSpots = computed(() => {
  let results = spots.value
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    results = results.filter(
      (spot) =>
        spot.spotName?.toLowerCase().includes(keyword) ||
        spot.spotCode?.toLowerCase().includes(keyword) ||
        spot.spotAddress?.toLowerCase().includes(keyword),
    )
  }
  if (searchMerchantId.value !== '' && searchMerchantId.value !== null) {
    const mId = Number(searchMerchantId.value)
    results = results.filter((spot) => Number(spot.merchantId) === mId)
  }
  if (searchStatus.value) {
    results = results.filter((spot) => spot.spotStatus === searchStatus.value)
  }
  return results
})

const goToAdd = () => {
  router.push({ name: 'spot-add' })
}

const goToMonitor = () => {
  // 請確保 router/index.js 中已設定 name: 'dispatch-monitor' 的路由
  router.push({ name: 'dispatch-monitor' })
}

const goToAnalyze = () => {
  router.push({ name: 'spot-analyze' })
}

const goToView = (id) => {
  router.push({ name: 'spot-view', params: { id } })
}

const goToEdit = (id) => {
  router.push({ name: 'spot-edit', params: { id } })
}

const deleteSpot = async (id, name) => {
  if (!confirm(`確定要刪除據點「${name}」(ID: ${id}) 嗎？\n此動作無法復原！`)) return
  try {
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

/* Small Box Styles */
.small-box {
  border-radius: 0.25rem;
  box-shadow: 0 0 1px rgba(0,0,0,.125), 0 1px 3px rgba(0,0,0,.2);
  display: block;
  margin-bottom: 20px;
  position: relative;
  background: #fff;
  color: #fff;
  overflow: hidden;
}
.small-box > .inner {
  padding: 10px;
}
.small-box h3 {
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0 0 10px 0;
  white-space: nowrap;
  padding: 0;
}
.small-box p {
  font-size: 1rem;
}
.small-box .icon {
  color: rgba(0,0,0,.15);
  z-index: 0;
}
.small-box .icon > i {
  font-size: 90px;
  position: absolute;
  right: 15px;
  top: 15px;
  transition: all .3s linear;
}
.small-box:hover .icon > i {
  transform: scale(1.1);
}
.small-box > .small-box-footer {
  background: rgba(0,0,0,.1);
  color: #fff;
  display: block;
  padding: 3px 0;
  position: relative;
  text-align: center;
  text-decoration: none;
  z-index: 10;
}
.small-box > .small-box-footer:hover {
  background: rgba(0,0,0,.15);
}
.bg-info { background-color: #17a2b8 !important; }
.bg-success { background-color: #28a745 !important; }
.bg-warning { background-color: #ffc107 !important; color: #1f2d3d !important; }
.bg-danger { background-color: #dc3545 !important; }



/* Original Styles */
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

.search-bar input {
  padding: 8px;
  width: 250px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-bar .search-input {
  width: 200px;
  margin-right: 10px;
}

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

.btn-monitor {
  background-color: #17a2b8; /* Info color (藍綠色) 區隔於新增按鈕 */
  font-size: 1.1em;
  padding: 8px 16px;
  margin-right: 10px;
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
