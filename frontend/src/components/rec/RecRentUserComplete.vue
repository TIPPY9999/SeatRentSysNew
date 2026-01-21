<script setup>
import { ref, onMounted, computed, watch } from "vue";
import axios from "axios";
import { useAuthStore } from "@/stores/auth";

// --- Pinia Store ---
const authStore = useAuthStore();

// --- Computed properties from store ---
const isLoggedIn = computed(() => authStore.isLogin && authStore.user);
const memberId = computed(() => authStore.user?.member?.memId);
const memberName = computed(() => authStore.user?.member?.memName || '訪客');

// --- 狀態定義 ---
const spots = ref([]);
const seats = ref([]);
const selectedSpot = ref(null);
const selectedSeat =ref(null);
const selectedPaymentMethod = ref(null);
const isLoading = ref({
  spots: false,
  seats: false,
  rent: false,
});
const errorMessage = ref("");

// 付款方式選項
const paymentMethods = [
  { value: "CREDIT_CARD", text: "信用卡" },
  { value: "LINE_PAY", text: "LINE Pay" },
  { value: "APPLE_PAY", text: "Apple Pay" },
];

// 對話框狀態
const showTermsModal = ref(false);
const agreedToTerms = ref(false);

// --- API 呼叫 ---

const loadSpots = async () => {
  isLoading.value.spots = true;
  try {
    const response = await axios.get("http://localhost:8080/spot/list");
    spots.value = response.data;
  } catch (error) {
    console.error("載入站點失敗:", error);
    errorMessage.value = "無法載入站點資料。";
  } finally {
    isLoading.value.spots = false;
  }
};

const loadSeats = async (spotId) => {
  if (!spotId) return;
  isLoading.value.seats = true;
  seats.value = [];
  selectedSeat.value = null;
  try {
    const response = await axios.get(
      `http://localhost:8080/seats/search?spotId=${spotId}`
    );
    // 歸還邏輯可能不同，此處暫時沿用租借邏輯
    seats.value = response.data.filter((seat) => seat.seatsStatus === "空閒");
  } catch (error) {
    console.error(`載入 ${spotId} 的座位失敗:`, error);
    errorMessage.value = "無法載入該站點的座位資訊。";
  } finally {
    isLoading.value.seats = false;
  }
};



// --- 核心邏輯 ---
const openTermsModal = () => {
  if (isReadyToRent.value) {
    showTermsModal.value = true;
  }
};
const closeModal = () => {
  showTermsModal.value = false;
  agreedToTerms.value = false; // 關閉時重置勾選
};

const proceedWithRent = async () => {
  if (!memberId.value) {
    errorMessage.value = "無法獲取您的會員資訊，請重新登入。";
    closeModal();
    return;
  }

  // 這裡的 API endpoint 和 data 應該是歸還的邏輯
  // 例如: axios.put(`/api/rec-rents/${rentId}/complete`, rentalData)
  // 此處暫時保留原邏輯作為示意
  const rentalData = {
    memId: memberId.value, // 從 Pinia Store 取得會員 ID
    spotIdRent: selectedSpot.value.spotId,
    // seatsId: selectedSeat.value.seatsId, // 歸還可能需要的是租借紀錄ID
    paymentMethod: selectedPaymentMethod.value,
  };
  isLoading.value.rent = true;
  try {
    // 假設這是歸還的 API
    const response = await axios.post(`http://localhost:8080/api/rec-rents/complete`, rentalData);
    if (response.status === 200) {
      alert(
        `歸還成功！\n站點：${selectedSpot.value.spotName}\n感謝您的使用。`
      );
      // 重置流程
      selectedSpot.value = null;
    } else {
      errorMessage.value = "歸還失敗，請稍後再試。";
    }
  } catch (error) {
    console.error("歸還請求失敗:", error);
    errorMessage.value = `歸還失敗: ${error.response?.data?.message || error.message}`;
  } finally {
    isLoading.value.rent = false;
    closeModal();
  }
};

// --- Computed ---

const isStep1Completed = computed(() => !!selectedSpot.value);
const isStep2Completed = computed(() => !!selectedPaymentMethod.value);

const step1Class = computed(() =>
  isStep1Completed.value ? "status-completed" : "status-pending"
);
const step2Class = computed(() =>
  isStep2Completed.value ? "status-completed" : "status-pending"
);


const isReadyToRent = computed(() => isStep1Completed.value && isStep2Completed.value && isLoggedIn.value);

// --- Watchers ---
watch(selectedSpot, (newSpot) => {
  if (newSpot) {
    // 歸還時可能不需要重載座位，或者需要載入正在使用的座位
    // loadSeats(newSpot.spotId);
  } else {
    seats.value = [];
    selectedSeat.value = null;
  }
});

onMounted(() => {
    // 應用程式的狀態恢復邏輯已移至 App.vue
    // 此處僅需執行此組件自身的初始化任務
    loadSpots();
});
</script>

<template>
  <div class="user-order-container">
    <div class="card">
      <h1 class="card-header">歸還座位並結算</h1>
      
      <!-- 會員資訊顯示區塊 -->
      <div class="card-body user-info-section">
        <div v-if="isLoggedIn">
            <h5><i class="fas fa-user-check"></i> 會員資訊</h5>
            <p class="mb-0">使用者 <strong>{{ memberName }}</strong> (ID: {{ memberId }})，請確認您的歸還資訊。</p>
            
            <!-- 除錯用：顯示整個 user 物件結構 -->
            <details class="mt-2">
              <summary style="cursor: pointer; font-size: 0.8rem;">點此查看原始會員資料物件</summary>
              <pre style="background-color: #333; color: #fff; padding: 10px; border-radius: 4px; font-size: 0.8rem;">{{ JSON.stringify(authStore.user, null, 2) }}</pre>
            </details>

        </div>
        <div v-else class="alert alert-warning">
            <h5><i class="fas fa-exclamation-triangle"></i> 訪客你好</h5>
            <p class="mb-0">請先登入以進行歸還結算。</p>
        </div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger m-3">{{ errorMessage }}</div>

      <!-- 步驟一: 選擇歸還站點 -->
      <div class="card-body" :class="step1Class">
        <h2><i class="fas fa-store"></i> 步驟一：選擇歸還站點</h2>
        <fieldset :disabled="!isLoggedIn">
            <div class="form-group">
                <label for="spot-select">請選擇站點：</label>
                <select id="spot-select" class="form-control" v-model="selectedSpot">
                    <option :value="null" disabled>-- 請選擇一個站點 --</option>
                    <option v-for="spot in spots" :key="spot.spotId" :value="spot">
                    {{ spot.spotName }} ({{ spot.spotAddress }})
                    </option>
                </select>
            </div>
        </fieldset>
      </div>

      <!-- 步驟2: 確認費用與付款 -->
      <div class="card-body" :class="step2Class">
        <fieldset :disabled="!isStep1Completed || !isLoggedIn">
          <h2><i class="fas fa-credit-card"></i> 步驟二：確認使用資訊並結清費用</h2>
          <h3>租借時間: {{ "10:30" }}</h3>
          <h3>歸還時間: {{ "11:15" }}</h3>
          <h3>使用時間: {{ "45 分鐘" }}</h3>
          <h3>費率: 30 NTD / 30 min</h3>
          <hr>
          <h2>費用總計: {{ "50 NTD" }}</h2>

          <div class="form-group">
            <label for="payment-method">付款方式：</label>
            <select
              id="payment-method"
              class="form-control"
              v-model="selectedPaymentMethod"
            >
              <option :value="null" disabled>-- 請選擇一個付款方式 --</option>
              <option
                v-for="method in paymentMethods"
                :key="method.value"
                :value="method.value"
              >
                {{ method.text }}
              </option>
            </select>
          </div>
          <button
            @click="openTermsModal"
            class="btn btn-success btn-lg"
            :disabled="!isReadyToRent"
          >
            {{ isReadyToRent ? "確認歸還並付款" : "請完成歸還步驟" }}
          </button>
        </fieldset>
      </div>
    </div>
  </div>
</template>
<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css");

.user-order-container {
  padding: 20px;
  max-width: 800px;
  margin: auto;
  font-family: "Microsoft JhengHei", sans-serif;
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
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
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
