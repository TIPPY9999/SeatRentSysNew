<script setup>
import { ref, reactive } from "vue";
import axios from "axios";
import RecRentSearch from "@/components/rec/RecRentSearch.vue";
import RecRentAdd from "@/components/rec/RecRentAdd.vue";
import RecRentEdit from "@/components/rec/RecRentEdit.vue"; // 1. 引入 Edit 組件

// --- 1. 狀態定義 ---
const activeView = ref("list"); // 'list', 'add', 'edit'
const editingRent = ref(null); // Holds the data for the rent being edited
const searchComponent = ref(null); // Ref to access the search component instance
const API_URL = "http://localhost:8080/rec-rent";

// --- 2. 核心邏輯 ---

// 新增或更新 (Create / Update)
const handleSaveRent = async (formData) => {
  try {
    const id = formData.recSeqId;
    const method = id ? "put" : "post";
    const url = id ? `${API_URL}/${id}` : API_URL;
    const res = await axios[method](url, formData);
    if (res.status === 200 || res.status === 201) {
      alert(id ? "更新成功！" : "新增成功！");
      activeView.value = "list";
      // Use the ref to call the child's method
      if (searchComponent.value) {
        await searchComponent.value.loadRents();
      }
    } else {
      alert("儲存失敗，請檢查輸入資料。");
    }
  } catch (err) {
    console.error("儲存操作失敗:", err);
    alert("儲存失敗，請檢查輸入資料。");
  }
};

// 刪除 (Delete)
const handleDeleteRent = async (id) => {
  if (!confirm("確定要刪除這筆訂單嗎？(ID: " + id + ")")) return;
  try {
    const res = await axios.delete(`${API_URL}/${id}`);
    if (res.status === 200) {
      alert("刪除成功！");
      // Use the ref to call the child's method
      if (searchComponent.value) {
        await searchComponent.value.loadRents();
      }
    } else {
      alert("刪除失敗");
    }
  } catch (err) {
    console.error("刪除操作失敗:", err);
    alert("刪除失敗");
  }
};

// --- 3. 視圖切換邏輯 ---

// 準備編輯資料
const handleEditRent = (rent) => {
  
  editingRent.value = { ...rent };
  activeView.value = "edit"; // 切換到編輯視圖

  // Scroll to top for better user experience
  setTimeout(() => {
    const mainContent = document.querySelector(".main-content");
    if (mainContent) {
      mainContent.scrollTo({ top: 0, behavior: "smooth" });
    }
  }, 50);
};

// 切換到新增畫面
const goToAddView = () => {
  editingRent.value = null; // Clear any editing data
  activeView.value = "add"; // 切換到新增視圖
};

// 取消並返回列表
const backToList = () => {
  editingRent.value = null; // Clear any editing data
  activeView.value = "list";
};
</script>

<template>
  <div class="top-nav">
    <button
      @click="backToList"
      :class="{ active: activeView === 'list' }"
      :disabled="activeView === 'list'"
    >訂單查詢</button>

    <button
      @click="goToAddView"
      :class="{ active: activeView === 'add' }"
      :disabled="activeView === 'add'"
    >新增訂單</button>

  </div>


  <div class="rec-rent-container">
    <div class="main-content">
      <h1>訂單管理系統 (RecRent)</h1>

      <div v-if="activeView === 'add'" class="view-section">
        <rec-rent-add
          @save-rent="handleSaveRent"
          @cancel="backToList"
        />
      </div>

      <!--  組件的區塊 -->
      <div v-if="activeView === 'edit'" class="view-section">
        <rec-rent-edit
          :initial-data="editingRent"
          @save-rent="handleSaveRent"
          @cancel="backToList"
        />
      </div>

      <div v-if="activeView === 'list'" class="view-section">
        <rec-rent-search
          ref="searchComponent"
          @edit-rent="handleEditRent"
          @delete-rent="handleDeleteRent"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* General container and navigation styles remain in the parent */
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

.top-nav button:disabled {
  background-color: #00ff9d;
  color: black;
  font-weight: bold;
  cursor: not-allowed;
}

.top-nav button:not(:disabled):hover {
  background-color: #5dffc4;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.view-section {
  display: block; /* Make sections visible by default for v-show */
}
</style>
