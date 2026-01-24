<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useRouter, useRoute } from 'vue-router'
import { useMemberAuthStore } from '@/stores/memberAuth'

// --- Pinia Store ---
const memberAuthStore = useMemberAuthStore()
const router = useRouter()
const route = useRoute()

// --- Computed properties from store ---
const isLoggedIn = computed(() => memberAuthStore.isLogin && !!memberAuthStore.member?.memId)
const memberId = computed(() => memberAuthStore.member?.memId)
const memberName = computed(() => memberAuthStore.member?.memName || '訪客')

// --- 狀態定義 ---
const seats = ref([])
const selectedSpot = ref(null)
const activeRent = ref(null) // [新增] 儲存目前進行中的訂單
const selectedSeat = ref(null)
const isLoading = ref({
  spot: false,
  seats: false,
  rent: false,
})
const errorMessage = ref('')

// 對話框狀態
const showTermsModal = ref(false)
const agreedToTerms = ref(false)

// --- API 呼叫 ---
const loadSpotInfo = async (spotId) => {
  if (!spotId) return
  isLoading.value.spot = true
  try {
    const response = await axios.get(`http://localhost:8080/spot/${spotId}`)
    selectedSpot.value = response.data
  } catch (error) {
    console.error(`載入站點 ${spotId} 失敗:`, error)
    errorMessage.value = '無法載入站點資料。'
  } finally {
    isLoading.value.spot = false
  }
}

const loadSeats = async (spotId) => {
  if (!spotId) return
  isLoading.value.seats = true
  seats.value = []
  selectedSeat.value = null
  try {
    const response = await axios.get(`http://localhost:8080/seats/search?spotId=${spotId}`)
    // 歸還邏輯可能不同，此處暫時沿用租借邏輯
    seats.value = response.data.filter((seat) => seat.seatsStatus === '空閒')
  } catch (error) {
    console.error(`載入 ${spotId} 的座位失敗:`, error)
    errorMessage.value = '無法載入該站點的座位資訊。'
  } finally {
    isLoading.value.seats = false
  }
}

// --- 核心邏輯 ---
const goToSearchSpot = () => {
  router.push('/SearchSpot')
}

const openTermsModal = () => {
  if (isReadyToRent.value) {
    showTermsModal.value = true
  }
}
const closeModal = () => {
  showTermsModal.value = false
  agreedToTerms.value = false // 關閉時重置勾選
}


// --- 問題回報處理 ---
const handleReport = () => {
  const rent = activeRent.value
  // 準備攜帶至回報頁面的資料
  const reportData = {
    orderId: rent?.recSeqId, // 訂單 ID
    memberId: memberAuthStore.member?.memId, // 使用者 ID
    spotId: selectedSpot.value?.spotId || rent?.spotIdRent, // 站點 ID
  }
  // TODO: REPORT - 暫時以 Alert 代替實際路由跳轉
  alert(
    `TODO: REPORT\n準備前往問題回報頁面\n訂單ID: ${reportData.orderId}\n會員ID: ${reportData.memberId}\n站點ID: ${reportData.spotId}`,
  )
}

const proceedWithRent = async () => {
  // 防呆檢查：確保會員 ID 存在
  if (!memberAuthStore.member?.memId) {
    console.warn('無法獲取會員資訊(memId遺失)，自動導向登入頁')
    memberAuthStore.clearMemberLogin()
    router.push({
      name: 'login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
    return
  }

  // 這裡的 API endpoint 和 data 應該是歸還的邏輯
  // 例如: axios.put(`/api/rec-rents/${rentId}/complete`, rentalData)
  // 此處暫時保留原邏輯作為示意
  const rentalData = {
    recSeqId: activeRent.value?.recSeqId, // [新增] 傳送訂單 ID 以便後端更新
    memId: memberAuthStore.member.memId, // 從 Pinia Store 取得會員 ID
    spotIdRent: selectedSpot.value.spotId,
    // seatsId: selectedSeat.value.seatsId, // 歸還可能需要的是租借紀錄ID
    recPayment: rentCalculation.value.totalFee, // [新增] 傳送計算後的費用
  }
  isLoading.value.rent = true
  try {
    // 假設這是歸還的 API
    const response = await axios.post(`http://localhost:8080/api/rec-rents/complete`, rentalData)
    if (response.status === 200) {
      // 歸還成功後，導向至付款頁面
      const recId = activeRent.value.recId || activeRent.value.recSeqId
      router.push(`/payment/${recId}`)
    } else {
      errorMessage.value = '歸還失敗，請稍後再試。'
    }
  } catch (error) {
    console.error('歸還請求失敗:', error)
    errorMessage.value = `歸還失敗: ${error.response?.data?.message || error.message}`
  } finally {
    isLoading.value.rent = false
    closeModal()
  }
}

// --- Computed ---

const isStep1Completed = computed(() => !!selectedSpot.value)

const step1Class = computed(() => (isStep1Completed.value ? 'status-completed' : 'status-pending'))

const isReadyToRent = computed(() => isStep1Completed.value && isLoggedIn.value)

// --- [新增] 費用與時間計算 ---
const rentCalculation = computed(() => {
  if (!activeRent.value) {
    return { rentTime: '-', returnTime: '-', duration: 0, totalFee: 0 }
  }

  // 1. 取得時間
  const rentDate = new Date(activeRent.value.recRentDT || activeRent.value.recRentDT2)
  const returnDate = new Date() // 當下時間

  // 2. 計算使用時間 (分鐘)
  const diffMs = returnDate - rentDate
  const durationMinutes = Math.floor(diffMs / (1000 * 60))

  // 3. 計算費用: 使用時間/30 取整後 + 20
  // 註：若您的費率是 "每30分鐘30元"，公式應為 Math.floor(durationMinutes / 30) * 30 + 20
  // 這裡依照您的指示 "使用時間/30 取整後+20" 實作
  const totalFee = Math.floor(durationMinutes / 30) * 30 + 20

  // 4. 格式化時間顯示 (YYYY/MM/DD HH:mm:ss)
  const formatTime = (date) => {
    return date.toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    })
  }

  return {
    rentTime: formatTime(rentDate),
    returnTime: formatTime(returnDate),
    duration: durationMinutes,
    totalFee: totalFee > 0 ? totalFee : 20, // 確保最低費用
  }
})
// 生成發票號碼 (格式: 兩位大寫英文 - 八位數字)
const generateInvoiceNumber = () => {
  // 產生兩個隨機大寫英文字母 (ASCII 65-90)
  const letters =
    String.fromCharCode(65 + Math.floor(Math.random() * 26)) +
    String.fromCharCode(65 + Math.floor(Math.random() * 26))
  // 產生八位隨機數字，不足補零
  const numbers = Math.floor(Math.random() * 100000000)
    .toString()
    .padStart(8, '0')
  return `${letters}-${numbers}`
}
// --- 測試功能：快速歸還 ---
const handleQuickReturnTest = async () => {
  if (!activeRent.value) return
  if (
    !confirm(
      `[測試功能] 確定要強制歸還訂單 ${activeRent.value.recSeqId || activeRent.value.recId} 嗎？`,
    )
  )
    return

  try {
    const testReturnData = {
      memId: memberId.value,
      seatsId: activeRent.value.seatsId,
      recSeqId: activeRent.value.recSeqId,
      spotIdRent: activeRent.value.spotIdRent,
      spotIdReturn: selectedSpot.value?.spotId, // 若未選站點則使用原站點
      recRentDT2: activeRent.value.recRentDT2 || activeRent.value.recRentDT,
      recReturnDT2: new Date().toISOString(), // 紀錄當下時間 (ISO 8601 格式)
      recUsageDT2: rentCalculation.value.duration,
      recStatus: '已完成',
      recPrice: rentCalculation.value.totalFee, // 價格或費率可由後端根據座位類型和站點決定
      recRequestPay: 0, // 因為無需確認付款，所以請求付款金額為 0
      recPayment: 0, // 同上，實際付款為 0
      recPayBy: '信用卡', //
      recInvoice: generateInvoiceNumber(), // 歸還時生成發票號碼
      recCarrier: memberAuthStore.member.memInvoice,
      recViolatInt: 0, //
    }

    //
    const updateId = activeRent.value.recSeqId;
    await axios.put(`http://localhost:8080/rec-rent/update/${updateId}`, testReturnData)
    alert('測試歸還成功，將導向紀錄頁面。')
    router.push({ name: 'rec-rent-user', params: { action: 'record' } })
  } catch (error) {
    console.error('測試歸還失敗:', error)
    alert('測試歸還失敗，請檢查後端或網路。')
  }
}
// --- Watchers ---
watch(selectedSpot, (newSpot) => {
  if (newSpot) {
    // 歸還時可能不需要重載座位，或者需要載入正在使用的座位
    // loadSeats(newSpot.spotId);
  } else {
    seats.value = []
    selectedSeat.value = null
  }
})

onMounted(async () => {
  // Debug: 確認進入頁面時的狀態
  console.log('歸還頁面載入，目前會員ID:', memberId.value, '登入狀態:', memberAuthStore.isLogin)

  // 如果進入頁面時發現是登入狀態但沒有 ID，導回登入頁
  if (memberAuthStore.isLogin && !memberId.value) {
    console.warn('歸還頁面偵測到資料遺失，自動導向登入頁')
    memberAuthStore.clearMemberLogin() // 確保清除異常狀態
    router.push({
      name: 'login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
    return
  }

  // 檢查是否有進行中的訂單 (recStatus === '租借中')
  if (memberAuthStore.isLogin && memberId.value) {
    try {
      const res = await axios.get(`http://localhost:8080/rec-rent?memId=${memberId.value}`)
      // [修改] 找到該筆訂單並存入 activeRent
      const foundRent = res.data.find((rent) => rent.recStatus === '租借中')

      if (!foundRent) {
        alert('您目前沒有租借中的訂單，無法進行歸還。\n將為您導向至租借紀錄頁面。')
        router.push({ name: 'rec-rent-user', params: { action: 'record' } })
        return
      } else {
        activeRent.value = foundRent
      }
    } catch (error) {
      console.error('檢查租借狀態失敗:', error)
    }
  }

  // 載入指定的站點資訊 (從 Pinia 或 URL 參數)
  const spotId = memberAuthStore.selectedSpotId || route.query.spotId
  if (spotId) {
    loadSpotInfo(spotId)
  }
})
</script>

<template>
  <div class="user-order-container">
    <div class="card">
      <h2 class="card-header">歸還座位並結算</h2>

      <!-- 會員資訊顯示區塊 -->
      <div class="card-body user-info-section">
        <div v-if="isLoggedIn">
          <h5 class="mb-0">
            您好: <strong>{{ memberName }}</strong> (ID: {{ memberId }})，請確認您的歸還資訊。
          </h5>

          <!-- 除錯用：顯示整個 user 物件結構 -->
          <!-- <details class="mt-2">
              <summary style="cursor: pointer; font-size: 0.8rem;">點此查看原始會員資料物件</summary>
              <pre style="background-color: #333; color: #fff; padding: 10px; border-radius: 4px; font-size: 0.8rem;">{{ JSON.stringify(memberAuthStore.member, null, 2) }}</pre>
            </details> -->
        </div>
        <div v-else class="alert alert-warning">
          <h5><i class="fas fa-exclamation-triangle"></i> 訪客你好</h5>
          <p class="mb-0">請先登入以進行歸還結算。</p>
        </div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger m-3">{{ errorMessage }}</div>

      <!-- 步驟一: 選擇歸還站點 -->
      <div class="card-body" :class="step1Class">
        <h2><i class="fas fa-store"></i> 步驟一：確認歸還站點</h2>
        <fieldset :disabled="!isLoggedIn">
          <div v-if="isLoading.spot" class="text-center">
            <span class="spinner-border spinner-border-sm"></span> 正在載入站點資訊...
          </div>
          <div v-else-if="selectedSpot" class="d-flex justify-content-between align-items-center">
            <div>
              <h4 class="mb-1">{{ selectedSpot.spotName }}</h4>
              <p class="text-muted mb-0">
                <i class="fas fa-map-marker-alt"></i> {{ selectedSpot.spotAddress }}
              </p>
            </div>
            <button class="btn btn-primary fw-bold" @click="goToSearchSpot">
              <i class="fas fa-exchange-alt"></i> 重新選擇站點
            </button>
          </div>
          <div v-else class="alert alert-warning d-flex justify-content-between align-items-center">
            <span>尚未選擇歸還站點，請先至地圖選擇。</span>
            <button class="btn btn-primary" @click="goToSearchSpot">前往地圖</button>
          </div>
        </fieldset>
      </div>

      <!-- 步驟2: 確認費用與付款 -->
      <div class="card-body">
        <fieldset :disabled="!isStep1Completed || !isLoggedIn">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="mb-0"><i class="fas fa-credit-card"></i> 步驟二：確認使用資訊</h2>
          </div>
          <h5>租借時間: {{ rentCalculation.rentTime }}</h5>
          <h5>歸還時間: {{ rentCalculation.returnTime }}</h5>
          <h5>使用時間: {{ rentCalculation.duration }} 分鐘</h5>
          <h5>費率: 20 NTD (基本) + 30 NTD 每30分鐘</h5>
          <hr />
          <h3>費用總計: {{ rentCalculation.totalFee }} NTD</h3>
          <div class="d-flex justify-content-between align-items-center">
            <button
              @click="openTermsModal"
              class="btn btn-success btn-lg"
              :disabled="!isReadyToRent"
            >
              {{ isReadyToRent ? '前往付款' : '請完成歸還步驟' }}
            </button>
            <div>
              <button class="btn btn-outline-danger fw-bold me-2" @click="handleQuickReturnTest">
                <i class="fas fa-bug"></i> 測試歸還
              </button>
              <button class="btn btn-warning fw-bold" @click="handleReport">
                <i class="fas fa-exclamation-circle"></i> 我有訂單問題!
              </button>
            </div>
          </div>
        </fieldset>
      </div>
    </div>
  </div>
</template>
<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

.user-order-container {
  padding: 20px;
  max-width: 800px;
  margin: auto;
  font-family: 'Microsoft JhengHei', sans-serif;
}
.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: background-color 0.5s ease;
}
.card-header {
  background-color: #28a745;
  color: white;
  padding: 15px 20px;
  margin: 0;
  text-align: center;
}
.card-body {
  padding: 20px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.3s ease-in-out;
}
.user-info-section {
  background-color: #e9ecef;
}
.card-body:last-child {
  border-bottom: none;
}

.status-pending {
  color: black;
  background-color: #ffffff;
}
.status-completed {
  background-color: #c1ffbe;
}

fieldset:disabled {
  opacity: 0.5;
  pointer-events: none;
}
h2 {
  color: #333;
  border-bottom: 2px solid #28a745;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 1rem;
}
.form-control {
  display: block;
  width: 100%;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  transition:
    border-color 0.15s ease-in-out,
    box-shadow 0.15s ease-in-out;
}
.btn {
  font-size: 1rem;
  padding: 10px 15px;
  border-radius: 5px;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  border: none;
}
.btn-primary {
  color: #fff;
  background-color: #007bff;
}
.btn-success {
  color: #fff;
  background-color: #28a745;
}
.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.btn-lg {
  padding: 15px 25px;
  font-size: 1.25rem;
}
.alert {
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid transparent;
  border-radius: 4px;
}
.alert-danger {
  color: #721c24;
  background-color: #f8d7da;
  border-color: #f5c6cb;
}
.alert-info {
  color: #0c5460;
  background-color: #d1ecf1;
  border-color: #bee5eb;
}
.alert-warning {
  color: #856404;
  background-color: #fff3cd;
  border-color: #ffeeba;
}
.m-3 {
  margin: 1rem;
}
.mt-3 {
  margin-top: 1rem;
}
.text-muted {
  color: #6c757d !important;
}
.text-center {
  text-align: center;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.2em;
}
.spinner-border {
  display: inline-block;
  width: 2rem;
  height: 2rem;
  vertical-align: text-bottom;
  border: 0.25em solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  -webkit-animation: spinner-border 0.75s linear infinite;
  animation: spinner-border 0.75s linear infinite;
}
@keyframes spinner-border {
  to {
    transform: rotate(360deg);
  }
}
/* Modal styles */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1050;
  width: 100vw;
  height: 100vh;
  background-color: #000;
  opacity: 0.5;
}
.modal.show {
  display: block;
}
.modal {
  z-index: 1055;
}
</style>
