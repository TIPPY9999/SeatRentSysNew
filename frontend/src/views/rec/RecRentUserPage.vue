<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// --- Component Imports ---
import RecRentUserOrder from '@/components/rec/RecRentUserOrder.vue'
import RecRentUserComplete from '@/components/rec/RecRentUserComplete.vue'

const route = useRoute()
const authStore = useAuthStore()

// --- 1. 狀態定義 (State Definitions) ---
const activeView = ref('order') // Default view

// --- 2. 視圖切換邏輯 (View Switching Logic) ---
const setActiveView = (view) => {
  activeView.value = view
}

// --- 3. 路由監聽 (Route Listener from UserPage) ---
onMounted(() => {
  // Set initial view based on route param or default to 'list'
  const initialAction = route.params.action
  if (initialAction === 'order' || initialAction === 'complete') {
    setActiveView(initialAction)
  } else {
    // 如果沒有 action，預設到 order view
    setActiveView('order')
  }
})

watch(
  () => route.params.action,
  (newAction) => {
    if (newAction === 'order' || newAction === 'complete') {
      setActiveView(newAction)
    }
  },
)

// --- 4. Computed for router-link ---
const orderRoute = computed(() => {
  const r = { name: 'rec-rent-user', params: { action: 'order' } }
  if (route.query.spotId) {
    r.query = { spotId: route.query.spotId }
  }
  return r
})
</script>

<template>
  <div class="top-nav">
    <!-- 還原為 router-link，用於頁面內部切換 -->
    <router-link :to="orderRoute" custom v-slot="{ navigate }">
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
        <!-- 傳遞 spotId 給子元件，會員資訊由子元件自行從 store 獲取 -->
        <rec-rent-user-order
          v-if="authStore.user && route.query.spotId"
          :spot-id="String(route.query.spotId)"
        />
        <div v-else-if="!route.query.spotId" class="alert alert-info">
          請先至「站點地圖」選擇一個租借站點。
        </div>
        <div v-else class="alert alert-warning">無法獲取會員資訊，請返回首頁並重新登入。</div>
      </div>
      <div v-if="activeView === 'complete'" class="view-section">
        <rec-rent-user-complete />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* General container and navigation styles */
.rec-rent-container {
  display: flex;
  flex-direction: column;
  /* height: 100%; */ /* 移除，讓內容自然撐高 */
  width: 100%;
  font-family: 'Microsoft JhengHei', Arial, sans-serif;
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
  /* overflow-y: auto; */ /* 移除，交由 MainLayout 滾動 */
}

.view-section {
  display: block;
}
</style>
