<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useMemberAuthStore } from '@/stores/memberAuth'

// --- Pinia Store ---
const memberAuthStore = useMemberAuthStore()

// --- 狀態定義 ---
const rents = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

// --- 核心邏輯 ---
const loadUserRents = async () => {
  const memId = memberAuthStore.member?.memId

  // 防呆：未登入不執行查詢
  if (!memId) {
    errorMessage.value = '請先登入會員以查看紀錄'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // 呼叫後端 API 查詢該會員的訂單 (假設後端支援 memId 參數過濾)
    const response = await axios.get(`http://localhost:8080/rec-rent?memId=${memId}`)
    let data = response.data

    // --- 排序邏輯 ---
    // 1. recStatus = "租借中" 的紀錄排在最上面
    // 2. 其餘紀錄依照租借時間 (recRentDT2) 倒序排列 (新的在前)
    data.sort((a, b) => {
      const isActiveA = a.recStatus === '租借中'
      const isActiveB = b.recStatus === '租借中'

      if (isActiveA && !isActiveB) return -1 // a 排前
      if (!isActiveA && isActiveB) return 1 // b 排前

      // 若狀態權重相同，比較時間 (倒序)
      const dateA = new Date(a.recRentDT2).getTime()
      const dateB = new Date(b.recRentDT2).getTime()
      return dateB - dateA
    })

    rents.value = data
  } catch (error) {
    console.error('載入租借紀錄失敗:', error)
    errorMessage.value = '無法載入租借紀錄，請確認網路連線或稍後再試。'
  } finally {
    isLoading.value = false
  }
}

// --- 問題回報處理 ---
const handleReport = (rent) => {
  // 準備攜帶至回報頁面的資料
  const reportData = {
    orderId: rent.recSeqId, // 訂單 ID
    memberId: memberAuthStore.member?.memId, // 使用者 ID
    spotId: rent.spotIdRent, // 站點 ID
  }
  // TODO: REPORT - 暫時以 Alert 代替實際路由跳轉
  alert(
    `TODO: REPORT\n準備前往問題回報頁面\n訂單ID: ${reportData.orderId}\n會員ID: ${reportData.memberId}\n站點ID: ${reportData.spotId}`,
  )
}

// --- Lifecycle ---
onMounted(() => {
  if (memberAuthStore.isLogin) {
    loadUserRents()
  }
})
</script>

<template>
  <div class="record-container">
    <h2 class="section-title"><i class="fas fa-history"></i> 我的租借紀錄</h2>

    <!-- 載入中 -->
    <div v-if="isLoading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">載入中...</p>
    </div>

    <!-- 錯誤訊息 -->
    <div v-else-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <!-- 無資料 -->
    <div v-else-if="rents.length === 0" class="alert alert-info">目前沒有租借紀錄。</div>

    <!-- 列表顯示 -->
    <div v-else class="list-group">
      <div
        v-for="rent in rents"
        :key="rent.recSeqId"
        class="list-group-item mb-3 shadow-sm"
        :class="{ 'border-active': rent.recStatus === '租借中' }"
      >
        <div class="d-flex w-100 justify-content-between align-items-center header-row">
          <h5 class="mb-1">
            <span
              class="badge"
              :class="rent.recStatus === '租借中' ? 'bg-success' : 'bg-secondary'"
            >
              {{ rent.recStatus }}
            </span>
            <span class="ms-2 title-text">訂單編號: {{ rent.recId || rent.recSeqId }}</span>
          </h5>
          <span class="ms-2 title-text">座位編號: {{ rent.seatsId }}</span>
        </div>

        <div class="row mt-2">
          <div class="col-md-6">
            <p class="mb-1">
              <strong>租借站點:</strong> {{ rent.rentSpotName || rent.spotIdRent }}
            </p>
            <p class="mb-1">
              <strong>歸還站點:</strong> {{ rent.returnSpotName || rent.spotIdReturn || '-' }}
            </p>
          </div>
          <div class="col-md-6">
            <p class="mb-1">
              <strong>租借時間:</strong> {{ rent.recRentDT2?.replace('T', ' ') || '-' }}
            </p>
            <p class="mb-1">
              <strong>歸還時間:</strong> {{ rent.recReturnDT2?.replace('T', ' ') || '-' }}
            </p>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-2 pt-2 border-top">
          <span class="text-muted small">付款方式: {{ rent.recPayBy || '-' }}</span>

          <h5 class="mb-0 text-primary fw-bold">
            <button class="btn btn-sm btn-outline-warning" @click="handleReport(rent)">
              <i class="fas fa-exclamation-circle"></i> 問題回報
            </button>
          </h5>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.record-container {
  padding: 10px;
  max-width: 800px;
  margin: 0 auto;
}
.section-title {
  color: #333;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.list-group-item {
  border: 1px solid #ddd;
  border-radius: 8px !important;
  transition: transform 0.2s;
}
.list-group-item:hover {
  transform: translateY(-2px);
}
/* 租借中狀態的特殊樣式 (左側綠色邊條) */
.border-active {
  border-left: 5px solid #28a745 !important;
  background-color: #f9fff9;
}
.badge {
  padding: 0.5em 0.7em;
  border-radius: 0.25rem;
  color: white;
}
.bg-success {
  background-color: #28a745;
}
.bg-secondary {
  background-color: #6c757d;
}
.text-primary {
  color: #007bff !important;
}
.text-muted {
  color: #6c757d !important;
}
</style>
