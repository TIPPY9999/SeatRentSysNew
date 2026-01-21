<script setup>
import { ref, onMounted, computed, watch } from "vue";
import axios from "axios";

// --- Props ---
const props = defineProps({
  memId: {
    type: Number,
    required: true,
  },
});

// --- 狀態定義 ---
const spots = ref([]);
const seats = ref([]);
const selectedSpot = ref(null);
const selectedSeat = ref(null);
const selectedPaymentMethod = ref(null);
const isLoading = ref({
  spots: false,
  seats: false,
  rent: false,
});
const errorMessage = ref("");

// 對話框狀態
const showTermsModal = ref(false);
const agreedToTerms = ref(false);

// 付款方式選項
const paymentMethods = [
  { value: "CREDIT_CARD", text: "信用卡" },
  { value: "LINE_PAY", text: "LINE Pay" },
  { value: "APPLE_PAY", text: "Apple Pay" },
];

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
    // 根據需求，只顯示狀態為"空閒"的座位
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
  if (!props.memId) {
    errorMessage.value = "無法獲取會員資訊，請重新登入。";
    closeModal();
    return;
  }
  if (!isReadyToRent.value || !agreedToTerms.value) {
    alert("請完成所有租借步驟並同意使用條款。");
    return;
  }
  const rentalData = {
    memId: props.memId, // 使用 props 傳入的會員 ID
    spotIdRent: selectedSpot.value.spotId,
    seatsId: selectedSeat.value.seatsId,
    paymentMethod: selectedPaymentMethod.value,
  };
  isLoading.value.rent = true;
  try {
    const response = await axios.post(`http://localhost:8080/api/rec-rents`, rentalData);
    if (response.status === 201 || response.status === 200) {
      alert(
        `租借成功！\n站點：${selectedSpot.value.spotName}\n座位：${
          selectedSeat.value.seatsId
        }\n付款方式：${
          paymentMethods.find((p) => p.value === selectedPaymentMethod.value).text
        }`
      );
      // 重置流程
      selectedSpot.value = null;
    } else {
      errorMessage.value = "租借失敗，請稍後再試。";
    }
  } catch (error) {
    console.error("租借請求失敗:", error);
    errorMessage.value = `租借失敗: ${error.response?.data?.message || error.message}`;
  } finally {
    isLoading.value.rent = false;
    closeModal(); // 無論成功失敗都關閉對話框
  }
};

// --- Computed ---

const isStep1Completed = computed(() => !!selectedSpot.value);
const isStep2Completed = computed(() => !!selectedSeat.value);
const isStep3Completed = computed(() => !!selectedPaymentMethod.value);

const step1Class = computed(() =>
  isStep1Completed.value ? "status-completed" : "status-pending"
);
const step2Class = computed(() =>
  isStep2Completed.value ? "status-completed" : "status-pending"
);
const step3Class = computed(() =>
  isStep3Completed.value ? "status-completed" : "status-pending"
);

// 新增 computed：檢查會員 ID 是否有效
const isMemIdValid = computed(() => props.memId && props.memId > 0);

const isReadyToRent = computed(() => isStep1Completed.value && isStep2Completed.value && isStep3Completed.value && isMemIdValid.value);

// --- Watchers ---
watch(selectedSpot, (newSpot) => {
  if (newSpot) {
    loadSeats(newSpot.spotId);
  } else {
    seats.value = [];
    selectedSeat.value = null;
  }
});

onMounted(loadSpots);
</script>

<template>
  <div class="user-order-container">
    <div class="card">
      <h1 class="card-header">即時座位租借</h1>
      <div v-if="!isMemIdValid" class="alert alert-danger m-3">無法獲取您的會員資訊，請確保您已正確登入。</div>
      <div v-if="errorMessage" class="alert alert-danger m-3">{{ errorMessage }}</div>

      <!-- 步驟一: 選擇站點 -->
      <div class="card-body" :class="step1Class">
        <h2><i class="fas fa-store"></i> 步驟一：選擇租借站點</h2>
        <div class="form-group">
          <label for="spot-select">請選擇站點：</label>
          <select id="spot-select" class="form-control" v-model="selectedSpot">
            <option :value="null" disabled>-- 請選擇一個站點 --</option>
            <option v-for="spot in spots" :key="spot.spotId" :value="spot">
              {{ spot.spotName }} ({{ spot.spotAddress }})
            </option>
          </select>
        </div>
      </div>

      <!-- 步驟二: 選擇座位 -->
      <div class="card-body" :class="step2Class">
        <fieldset :disabled="!isStep1Completed">
          <h2><i class="fas fa-chair"></i> 步驟二：選擇座椅類型</h2>
          <div v-if="isLoading.seats" class="text-center">
            <span class="spinner-border spinner-border-sm"></span> 正在載入座位...
          </div>
          <div v-else-if="seats.length > 0" class="form-group">
            <label for="seat-select">請選擇座椅類型：</label>
            <select id="seat-select" class="form-control" v-model="selectedSeat">
              <option :value="null" disabled>-- 請選擇一個類型 --</option>
              <option v-for="seat in seats" :key="seat.seatsId" :value="seat">
                ID: {{ seat.seatsId }} | 類型: {{ seat.seatsType }}
              </option>
            </select>
          </div>
          <div v-else-if="selectedSpot" class="alert alert-warning">
            此站點目前無可用座位。
          </div>
          <div v-else class="text-muted">請先選擇站點以載入座位。</div>
        </fieldset>
      </div>

      <!-- 步驟三: 付款與租借 -->
      <div class="card-body" :class="step3Class">
        <fieldset :disabled="!isStep2Completed">
          <h2><i class="fas fa-credit-card"></i> 步驟三：確認付款資訊並租借</h2>
          
          <h3>基本費用:前三十分鐘 20 NTD</h3>
          <h3> 30 min</h3>
          <h3>費率:30 NTD / 30 min</h3>
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
            {{ isReadyToRent ? "確認租借，前往付款" : "請完成各步驟或檢查登入狀態" }}
          </button>
        </fieldset>
      </div>
    </div>

    <!-- 使用條款 Modal -->
    <div v-if="showTermsModal" class="modal-backdrop fade show"></div>
    <div
      class="modal fade"
      :class="{ show: showTermsModal }"
      :style="{ display: showTermsModal ? 'block' : 'none' }"
      tabindex="-1"
      role="dialog"
    >
      <div class="modal-dialog modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">使用條款</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <p>開始使用前，請詳閱使用條款...</p>
            <!-- 在此處添加您的條款詳細內容 -->
            <hr />
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                id="terms-agree"
                v-model="agreedToTerms"
              />
              <label class="form-check-label" for="terms-agree">
                我已閱讀並同意以上使用條款。
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">
              取消
            </button>
            <button
              type="button"
              class="btn btn-primary"
              :disabled="!agreedToTerms || isLoading.rent"
              @click="proceedWithRent"
            >
              <span v-if="isLoading.rent" class="spinner-border spinner-border-sm"></span>
              確認租借
            </button>
          </div>
        </div>
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
  background-color: #007bff;
  color: rgb(0, 0, 0);
  padding: 15px 20px;
  margin: 0;
  text-align: center;
}
.card-body {
  padding: 20px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.3s ease-in-out;
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
  border-bottom: 2px solid #007bff;
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
