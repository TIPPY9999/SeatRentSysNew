
<script setup>
import { ref, onMounted } from "vue";
// 不知為啥不能使用
// import { GmpxApiLoader, GmpxPlacePicker } from "@googlemaps/extended-component-library";
import '@googlemaps/extended-component-library/api_loader.js';//上方套件的替代
import '@googlemaps/extended-component-library/place_picker.js';//上方套件的替代
import axios from "axios";

// 1. ---版面狀態---
const isSidebarCollapsed = ref(false); // 控制側邊欄是否收合

/**
 * 切換側邊欄的收合狀態
 */
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

// 2. ---組態設定---
// 請將你的 API 金鑰存放在 .env 檔案中，然後使用 import.meta.env.VITE_GOOGLE_MAPS_API_KEY 來讀取
const apiKey = "AIzaSyCu6YRYdgvvOg2aLI6K5L3R0GtnyyfRe_M"; // 記得替換成你自己的金鑰或使用環境變數
// 地圖中心點，預設為台灣
const center = ref({ lat: 23.973875, lng: 120.982025 });
// 後端 API 的 URL
const backendApiUrl = "http://localhost:8080/spot/list";

// 3. ---狀態定義---
const spots = ref([]); // 儲存從後端獲取的 SPOT 點
const error = ref(null); // API 呼叫的錯誤訊息

// InfoWindow 的狀態管理
const infoWindow = ref({
  position: null,
  spot: null,
  opened: false,
});

// 4. ---核心邏輯---
/**
 * 當使用者在搜尋框中選擇一個地點後觸發
 * @param {CustomEvent} event - gmpx-place-picker 發出的 gmp-placechange 事件
 */
const onPlaceChanged = (event) => {
  const place = event.detail.place;
  if (place && place.geometry && place.geometry.location) {
    center.value = {
      lat: place.geometry.location.lat(),
      lng: place.geometry.location.lng(),
    };
  }
};

/**
 * 從後端取得所有 SPOT 點的資料
 */
const fetchSpots = async () => {
  try {
    const response = await axios.get(backendApiUrl);
    // [修正] 將後端回傳的資料格式轉換為地圖標記所需的格式
    spots.value = response.data.map((spot) => ({
      id: spot.spotId,
      name: spot.spotName,
      status: spot.spotStatus,
      position: {
        lat: parseFloat(spot.latitude), // 對應後端 'latitude' 欄位
        lng: parseFloat(spot.longitude), // 對應後端 'longitude' 欄位
      },
    }));
  } catch (err) {
    console.error("無法獲取 SPOT 點資料:", err);
    error.value = "無法載入 SPOT 點資料，請稍後再試。";
  }
};

/**
 * 當使用者點擊地圖上的標記時觸發
 * @param {object} spot - 被點擊的 SPOT 物件
 */
const openInfoWindow = (spot) => {
  infoWindow.value = {
    position: spot.position,
    spot: spot,
    opened: true,
  };
};

/**
 * 當 InfoWindow 被使用者關閉時觸發
 */
const closeInfoWindow = () => {
  infoWindow.value.opened = false;
};

/**
 * 處理租借按鈕點擊事件
 * @param {number} spotId - SPOT 的 ID
 */
const handleRent = (spotId) => {
  console.log(`準備租借 Spot ID: ${spotId}`);
  alert(`您點擊了租借 Spot ID: ${spotId}`);
};

/**
 * 處理歸還按鈕點擊事件
 * @param {number} spotId - SPOT 的 ID
 */
const handleReturn = (spotId) => {
  console.log(`準備歸還 Spot ID: ${spotId}`);
  alert(`您點擊了歸還 Spot ID: ${spotId}`);
};

// 在組件掛載完成後，自動獲取 SPOT 點資料
onMounted(fetchSpots);
</script>

<template>
  <div class="page-wrapper" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- 左側邊欄 -->
    <aside class="sidebar">
      <!-- 常駐會員資訊區塊 -->
      <div class="member-profile">
        <span class="icon-wrapper">
          <el-icon><Avatar /></el-icon>
        </span>
        <span class="member-name">會員登入</span>
      </div>

      <!-- 可收合的功能選單 -->
      <ul class="menu-list">
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Search /></el-icon>
          </span>
          <span class="menu-text">站點查詢</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Ticket /></el-icon>
          </span>
          <span class="menu-text">商家優惠</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><MapLocation /></el-icon>
          </span>
          <span class="menu-text">猜你喜歡</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Comment /></el-icon>
          </span>
          <span class="menu-text">分享討論</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Phone /></el-icon>
          </span>
          <span class="menu-text">客服支援</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><StarFilled /></el-icon>
          </span>
          <span class="menu-text">支持我們</span>
        </li>
      </ul>

      <!-- 收合按鈕 -->
      <div class="sidebar-footer">
        <button
          @click="toggleSidebar"
          class="toggle-btn"
          :title="isSidebarCollapsed ? '展開' : '收合'"
        >
          <el-icon
            ><DArrowLeft v-if="!isSidebarCollapsed" /><DArrowRight
              v-if="isSidebarCollapsed"
          /></el-icon>
        </button>
      </div>
    </aside>

    <!-- 右側地圖容器 -->
    <main class="map-container">
      <div v-if="error" class="error-message">{{ error }}</div>
           <!-- GMAP 搜尋 (官方元件) -->
      <gmpx-api-loader :key="apiKey"></gmpx-api-loader>
      <div class="place-picker-container">
        <gmpx-place-picker
          placeholder="請輸入地址或地點"
          @gmp-placechange="onPlaceChanged"
        ></gmpx-place-picker>
      </div>

      <GMapMap
        v-if="!error"
        :center="center"
        :zoom="8"
        class="map"
        map-id="d2fc83863651fe9c90d73c8a"
      >
        <!-- Markers and InfoWindow... -->
        <GMapMarker
          v-for="spot in spots"
          :key="spot.id"
          :position="spot.position"
          :title="spot.name"
          :clickable="true"
          @click="openInfoWindow(spot)"
        />
        <GMapInfoWindow
          :opened="infoWindow.opened"
          :position="infoWindow.position"
          :options="{ pixelOffset: { width: 0, height: -35 } }"
          @closeclick="closeInfoWindow"
        >
          <div v-if="infoWindow.spot" class="info-window-content">
            <h4>{{ infoWindow.spot.name }}</h4>
            <p><strong>ID:</strong> {{ infoWindow.spot.id }}</p>
            <p><strong>狀態:</strong> {{ infoWindow.spot.status }}</p>
            <div class="button-group">
              <button @click="handleRent(infoWindow.spot.id)" class="btn btn-success">
                租借
              </button>
              <button @click="handleReturn(infoWindow.spot.id)" class="btn btn-primary">
                歸還
              </button>
            </div>
          </div>
        </GMapInfoWindow>
      </GMapMap>
    </main>
  </div>
</template>

<style scoped>
/* --- 1. CSS 變數 --- */
:root {
  --sidebar-width-expanded: 250px;
  --sidebar-width-collapsed: 80px;
}

/* --- 2. 主佈局 --- */
.page-wrapper {
  display: flex;
  height: 90vh;
  width: 100%;
  background-color: #f4f6f9;
}

/* --- 3. 側邊欄 --- */
.sidebar {
  width: var(--sidebar-width-expanded);
  background-color: #99ff99;
  border-right: 1px solid #dee2e6;
  transition: width 0.3s ease;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-wrapper.sidebar-collapsed .sidebar {
  width: var(--sidebar-width-collapsed);
}

/* 通用圖示容器樣式 */
.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  font-size: 30px; /* for SVG size */
  color: #484848;
  flex-shrink: 0;
}

/* 會員資訊 (常駐) */
.member-profile {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 20px;
  border-bottom: 1px solid #e9ecef;
  cursor: pointer;
  transition: background-color 0.2s;
}
.member-profile:hover {
  background-color: #f5f7fa;
}

.member-name {
  font-weight: bold;

  font-size: 20px;
  font-family: fantasy;
  opacity: 1;
  transition: opacity 0.2s ease, width 0.2s ease;
  white-space: nowrap;
}

.page-wrapper.sidebar-collapsed .member-name {
  opacity: 0;
  width: 0;
}

/* 功能選單 (可隱藏) */
.menu-list {
  list-style: none;
  padding: 0;
  margin: 10px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  gap: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-text {
  font-size: 20px;
  font-family: fantasy;
  opacity: 1;
  transition: opacity 0.2s ease, width 0.2s ease;
}

.page-wrapper.sidebar-collapsed .menu-text {
  opacity: 0;
  width: 0;
}

/* 側邊欄頁腳 (收合按鈕) */
.sidebar-footer {
  padding: 5px;
  margin-top: auto; /* 將按鈕推到底部 */
  border-top: 1px solid #e9ecef;
}

.toggle-btn {
  width: 100%;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  padding: 5px;
  font-size: 28px;
  line-height: 1;
  color: #606266;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.2s, color 0.2s;
}

.toggle-btn:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

/* --- 4. 地圖容器 --- */
.map-container {
  flex-grow: 1;
  width: 100%;
  height: 100%;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

/* --- 5. 其他樣式 (錯誤訊息、彈出視窗等) --- */
.error-message {
  color: red;
  padding: 20px;
  text-align: center;
}
.info-window-content {
  padding: 5px;
  min-width: 200px;
}
.info-window-content h4,
.info-window-content p {
  margin: 5px 0;
}
.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: space-around;
}
.btn {
  display: inline-block;
  font-weight: 400;
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  user-select: none;
  border: 1px solid transparent;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  cursor: pointer;
}
.btn-success {
  color: #fff;
  background-color: #28a745;
  border-color: #28a745;
}
.btn-primary {
  color: #fff;
  background-color: #007bff;
  border-color: #007bff;
}

/* --- 6. Google Maps Place Picker --- */
.place-picker-container {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  z-index: 10;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

gmpx-place-picker {
  width: 100%;
}
</style>
