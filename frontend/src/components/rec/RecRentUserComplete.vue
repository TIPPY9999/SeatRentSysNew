<script setup>
import { ref, onMounted, computed, watch } from "vue";
import axios from "axios";

// --- 狀態定義 ---
const spots = ref([]);
const seats = ref([]);
const selectedSpot = ref(null);
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
      `http://localhost:8080/seat/listBySoptId?spotId=${spotId}`
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
  const rentalData = {
    memId: 1, // 假設的會員 ID，應從登入狀態取得
    spotIdRent: selectedSpot.value.spotId,
    seatsId: selectedSeat.value.seatsId,
    paymentMethod: selectedPaymentMethod.value,
  };
  isLoading.value.rent = true;
  try {
    const response = await axios.post(`http://localhost:8080/api/rec-rents`, rentalData);
    if (response.status === 201 || response.status === 200) {
      alert(
        `歸還成功！\n站點：${selectedSpot.value.spotName}\n座椅：${
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
const isStep2Completed = computed(() => !!selectedPaymentMethod.value);

const step1Class = computed(() =>
  isStep1Completed.value ? "status-completed" : "status-pending"
);
const step2Class = computed(() =>
  isStep2Completed.value ? "status-completed" : "status-pending"
);


const isReadyToRent = computed(() => isStep1Completed.value && isStep2Completed.value);

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
      <div v-if="errorMessage" class="alert alert-danger m-3">{{ errorMessage }}</div>

      <!-- 步驟一: 選擇站點 -->
      <div class="card-body" :class="step1Class">
        <h2><i class="fas fa-store"></i> 步驟一：選擇歸還站點</h2>
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

      <!-- 步驟2: 付款與租借 -->
      <div class="card-body" :class="step2Class">
        <fieldset :disabled="!isStep1Completed">
          <h2><i class="fas fa-credit-card"></i> 步驟二：確認使用資訊結清費用</h2>
          <h3>租借時間:{{}}</h3>
          <h3>歸還時間:{{}}</h3>
          <h3>使用時間:{{}}</h3>
          <h3>費率:30 NTD / 30 min</h3>
          <hr>
          <h2>費用總計:{{}}</h2>

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
            {{ isReadyToRent ? "確認歸還" : "請完成各步驟" }}
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
