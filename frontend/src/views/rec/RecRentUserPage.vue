<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute } from 'vue-router';
import axios from "axios";

// --- Component Imports ---
// From original UserPage
import RecRentUserOrder from "@/components/rec/RecRentUserOrder.vue";
import RecRentUserComplete from "@/components/rec/RecRentUserComplete.vue";
// From MgnPage
import RecRentSearch from "@/components/rec/RecRentSearch.vue";
import RecRentAdd from "@/components/rec/RecRentAdd.vue";
import RecRentEdit from "@/components/rec/RecRentEdit.vue";

const route = useRoute();

// --- 1. 狀態定義 (State Definitions) ---
// Combined states from both files
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


// --- 4. 路由監聽 (Route Listener from UserPage) ---
onMounted(() => {
  // Set initial view based on route param or default to 'list'
  const initialAction = route.params.action;
  if (initialAction === 'order' || initialAction === 'complete') {
    setActiveView(initialAction);
  } else {
    setActiveView('list'); // Default to list view if no/invalid action
  }
});

watch(() => route.params.action, (newAction) => {
  if (newAction === 'order' || newAction === 'complete') {
    setActiveView(newAction);
  }
});

</script>

<template>
  <div class="top-nav">
    <!-- Buttons from UserPage (using router-link) -->
    <router-link :to="{ name: 'rec-rent-user', params: { action: 'order' } }" custom v-slot="{ navigate }">
      <button @click="navigate" :class="{ active: activeView === 'order' }" :disabled="activeView === 'order'">租借＠Seat</button>
    </router-link>
    <router-link :to="{ name: 'rec-rent-user', params: { action: 'complete' } }" custom v-slot="{ navigate }">
      <button @click="navigate" :class="{ active: activeView === 'complete' }" :disabled="activeView === 'complete'">歸還＠Seat</button>
    </router-link>

    <!-- Buttons from MgnPage (using click handlers) -->
    <button @click="backToList" :class="{ active: activeView === 'list' }" :disabled="activeView === 'list'">
      訂單查詢
    </button>
    <button @click="goToAddView" :class="{ active: activeView === 'add' }" :disabled="activeView === 'add'">
      新增訂單
    </button>
  </div>

  <div class="rec-rent-container">
    <div class="main-content">
      <h1>＠Seat 租借服務</h1>

      <!-- Views from UserPage -->
      <div v-if="activeView === 'order'" class="view-section">
        <rec-rent-user-order />
      </div>
      <div v-if="activeView === 'complete'" class="view-section">
        <rec-rent-user-complete />
      </div>

      <!-- Views from MgnPage -->
      <div v-if="activeView === 'list'" class="view-section">
        <rec-rent-search ref="searchComponent" @edit-rent="handleEditRent" @delete-rent="handleDeleteRent" />
      </div>
      <div v-if="activeView === 'add'" class="view-section">
        <rec-rent-add @save-rent="handleSaveRent" @cancel="backToList" />
      </div>
      <div v-if="activeView === 'edit'" class="view-section">
        <rec-rent-edit :initial-data="editingRent" @save-rent="handleSaveRent" @cancel="backToList" />
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