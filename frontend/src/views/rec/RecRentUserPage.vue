<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useAuthStore } from "@/stores/auth"; // 保留，用於傳遞 memId
import axios from "axios";

// --- Component Imports ---
import RecRentUserOrder from "@/components/rec/RecRentUserOrder.vue";
import RecRentUserComplete from "@/components/rec/RecRentUserComplete.vue";
// From MgnPage
import RecRentSearch from "@/components/rec/RecRentSearch.vue";
import RecRentAdd from "@/components/rec/RecRentAdd.vue";
import RecRentEdit from "@/components/rec/RecRentEdit.vue";

const route = useRoute();
const authStore = useAuthStore(); // 保留 auth store 實例

// --- 1. 狀態定義 (State Definitions) ---
const activeView = ref("order"); // Default view
const editingRent = ref(null);
const searchComponent = ref(null);
const API_URL = "http://localhost:8080/api/rec-rents";

// --- 2. 核心邏輯 (Core Logic from MgnPage) ---

// --- 3. 視圖切換邏輯 (View Switching Logic) ---

// Combined logic
const setActiveView = (view) => {
  activeView.value = view;
  editingRent.value = null; // Reset editing data on view change
};

// For MgnPage functions
const handleEditRent = (rent) => {
  editingRent.value = { ...rent };
  activeView.value = "edit";
  setTimeout(() => {
    const mainContent = document.querySelector(".main-content");
    if (mainContent) mainContent.scrollTo({ top: 0, behavior: "smooth" });
  }, 50);
};

const goToAddView = () => {
  setActiveView("add");
};

const backToList = () => {
  setActiveView("list");
};
const handlePayment = async (recId) => {
  try {
    // 呼叫您的後端 API
    // 注意：因為後端回傳的是 HTML 表單字串，所以不能用一般的 json 處理
    const response = await axios.post(`/api/payment/checkout?recId=${recId}`);
    
    // 將後端傳回的自動跳轉表單插入頁面並執行
    const div = document.createElement('div');
    div.innerHTML = response.data; // 這是 EcpayUtils 產生的 <form>...<script>
    document.body.appendChild(div);
    document.forms[0].submit(); // 觸發跳轉至綠界
    
  } catch (error) {
    console.error("付款發起失敗", error);
  }
};

// --- 4. 路由監聽 (Route Listener from UserPage) ---
onMounted(() => {
  // Set initial view based on route param or default to 'list'
  const initialAction = route.params.action;
  if (initialAction === "order" || initialAction === "complete") {
    setActiveView(initialAction);
  } else {
    // 如果沒有 action，預設到 order view
    setActiveView("order");
  }
});

watch(
  () => route.params.action,
  (newAction) => {
    if (newAction === "order" || newAction === "complete") {
      setActiveView(newAction);
    }
  }
);
</script>

<template>
  <div class="top-nav">
    <!-- 還原為 router-link，用於頁面內部切換 -->
    <router-link
      :to="{ name: 'rec-rent-user', params: { action: 'order' } }"
      custom
      v-slot="{ navigate }"
    >
      <button
        @click="navigate"
        :class="{ active: activeView === 'order' }"
        :disabled="activeView === 'order'"
      >
        租借＠Seat
      </button>
    </router-link>
    <router-link
      :to="{ name: 'rec-rent-user', params: { action: 'complete' } }"
      custom
      v-slot="{ navigate }"
    >
      <button
        @click="navigate"
        :class="{ active: activeView === 'complete' }"
        :disabled="activeView === 'complete'"
      >
        歸還＠Seat
      </button>
    </router-link>
  </div>

  <div class="rec-rent-container">
    <div class="main-content">
      <h1>＠Seat 租借服務</h1>

      <!-- Views from UserPage -->
      <div v-if="activeView === 'order'" class="view-section">
        <!-- 傳遞會員 ID 給子元件，並確保 user 物件存在 -->
        <rec-rent-user-order v-if="authStore.user" :mem-id="authStore.user.id" />
        <div v-else class="alert alert-warning">
          無法獲取會員資訊，請返回首頁並重新登入。
        </div>
      </div>
      <div v-if="activeView === 'complete'" class="view-section">
        <rec-rent-user-complete />
      </div>

      <!-- Views from MgnPage (如果需要管理功能的話) -->
      <div v-if="activeView === 'list'" class="view-section">
        <rec-rent-search ref="searchComponent" @edit-rent="handleEditRent" />
      </div>
      <div v-if="activeView === 'add'" class="view-section">
        <rec-rent-add @cancel="backToList" />
      </div>
      <div v-if="activeView === 'edit'" class="view-section">
        <rec-rent-edit :initial-data="editingRent" @cancel="backToList" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* General container and navigation styles */
.rec-rent-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  font-family: "Microsoft JhengHei", Arial, sans-serif;
  background-color: #f9f9f9;
}

.top-nav {
  width: 100%;
  background-color: #acacac;
  color: white;
  display: flex;
  padding-top: 0px;
  flex-shrink: 0;
}

.top-nav button {
  background-color: #01e68e;
  color: #2b2b2b;
  font-size: 25px;
  font-weight: 500;
  display: flex;
  margin: 10px;
  border: none;
  cursor: pointer;
}

.top-nav button:disabled,
.top-nav button.active {
  background-color: #00ff9d;
  color: black;
  font-weight: bold;
  cursor: not-allowed;
}

.top-nav button:not(:disabled):not(.active):hover {
  background-color: #5dffc4;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.view-section {
  display: block;
}
</style>
