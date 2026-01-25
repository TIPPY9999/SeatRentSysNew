<script setup>
import { ref, onMounted, nextTick,computed } from "vue";
import { LocationFilled } from "@element-plus/icons-vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { useMemberAuthStore } from "@/stores/memberAuth";
import { useAdminAuthStore } from "@/stores/adminAuth";

// --- Computed properties from store ---
// --- Props ---
const props = defineProps({
  spotId: {
    type: String,
    required: true,
  },
});

// --- Pinia Store ---
const memberAuthStore = useMemberAuthStore();
const adminAuthStore = useAdminAuthStore();

// --- Computed properties from store ---
const isLoggedIn = computed(() => memberAuthStore.isLogin && !!memberAuthStore.member?.memId);
const memberId = computed(() => memberAuthStore.member?.memId);
const memberName = computed(() => memberAuthStore.member?.memName || "訪客");

// --- 路由與狀態管理 ---
const router = useRouter();

// --- 1. 組態設定 ---
const center = ref({ lat: 23.973875, lng: 120.982025 });
const zoom = ref(8);
const backendApiUrl = "http://localhost:8080/spot/list";
// 地圖選項設定 (啟用 Google Maps 的完整 UI 控制項)
const mapOptions = {
  zoomControl: true,
  mapTypeControl: true,
  streetViewControl: true,
  fullscreenControl: true,
  rotateControl: true,
};

// --- 2. 狀態定義 ---
const spots = ref([]);
const error = ref(null);
// 用於存放搜尋結果的地圖標記位置
const searchResultMarker = ref(null);
// 用於雙向綁定搜尋框的輸入文字
const searchQuery = ref("");
const infoWindow = ref({
  position: null,
  opened: false,
  isSearchResult: false,
  rentCount:0,
  returnCount:0,
  spot: null,
  title: "",
});

// --- 3. 核心邏輯 ---

// 根據站點狀態生成帶有顏色的地圖圖示
const getMarkerIcon = (status) => {
  
  const color = status === "營運中" ? "green" : "gray";
  const svg = `
<svg xmlns="http://www.w3.org/2000/svg" width="8" height="8" fill="${color}" class="bi bi-lightbulb-fill" viewBox="0 0 16 16">
  <path d="M2 6a6 6 0 1 1 10.174 4.31c-.203.196-.359.4-.453.619l-.762 1.769A.5.5 0 0 1 10.5 13h-5a.5.5 0 0 1-.46-.302l-.761-1.77a2 2 0 0 0-.453-.618A5.98 5.98 0 0 1 2 6m3 8.5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1l-.224.447a1 1 0 0 1-.894.553H6.618a1 1 0 0 1-.894-.553L5.5 15a.5.5 0 0 1-.5-.5"/>
</svg>
 `;
  return {
    url: `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`,
    scaledSize: { width: 32, height: 32 },
  };
};

// 統一的地圖更新函式，用於聚焦與縮放
const updateMapLocation = (location) => {
  if (!location) return;
  center.value = {
    lat: location.lat(),
    lng: location.lng(),
  };
  zoom.value = 15;
};

// 初始化地圖中心點，優先使用使用者地理位置
const initializeMapCenter = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        center.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };
        zoom.value = 12;
      },
      (error) => {
        console.warn(`無法獲取地理位置: ${error.message}。將使用預設中心點。`);
      }
    );
  } else {
    console.warn("此瀏覽器不支援地理位置功能。將使用預設中心點。");
  }
};

// 處理從 GMapAutocomplete 選擇一個地點
const onPlaceChanged = (place) => {
  if (place && place.geometry && place.geometry.location) {
    const location = place.geometry.location;
    updateMapLocation(location);
    searchResultMarker.value = location.toJSON();
    searchQuery.value = place.formatted_address; // 將選擇的地點地址同步回輸入框

    infoWindow.value.opened = false;
    nextTick(() => {
      infoWindow.value = {
        position: location.toJSON(),
        opened: true,
        isSearchResult: true,
        spot: null,
        title: place.formatted_address,
      };
    });
  }
};

// 執行地理編碼搜尋 (將地址文字轉換為座標)
const performSearch = () => {
  const query = searchQuery.value; // 直接從狀態獲取查詢字串
  if (!query) return;

  const geocoder = new google.maps.Geocoder();
  geocoder.geocode({ address: query }, (results, status) => {
    if (status === "OK" && results[0]) {
      const place = results[0];
      const location = place.geometry.location;
      updateMapLocation(location);
      searchResultMarker.value = location.toJSON();

      infoWindow.value.opened = false;
      nextTick(() => {
        infoWindow.value = {
          position: location.toJSON(),
          opened: true,
          isSearchResult: true,
          spot: null,
          title: place.formatted_address,
        };
      });
    } else {
      console.warn(`Geocode 失敗，原因: ${status}`);
      alert("找不到指定的地點，請嘗試輸入更詳細的地址。");
    }
  });
};

// 從後端 API 獲取所有站點資料
const fetchSpots = async () => {
  try {
    const response = await axios.get(backendApiUrl);
    // 為了顯示每個站點的座位數，我們使用 Promise.all 來平行發送請求
    const spotsData = await Promise.all(
      response.data.map(async (spot) => {
        let seatCount = 0; // 預設座位數為 0
        try {
          // 呼叫後端 API 獲取該站點的座位數量
          const countResponse = await axios.get(`http://localhost:8080/seats/count-by-spot?spotId=${spot.spotId}`);
          seatCount = countResponse.data;
        } catch (countErr) {
          // 如果獲取座位數失敗，在控制台印出錯誤，但整個流程不中斷
          console.error(`無法獲取站點 ${spot.spotId} 的座位數:`, countErr);
        }
        
        // 組合最終的站點物件，包含座位資訊
        return {
          id: spot.spotId,
          name: spot.spotName,
          status: spot.spotStatus,
          position: {
            lat: parseFloat(spot.latitude),
            lng: parseFloat(spot.longitude),
          },
          seatCount: seatCount,
          returnCount: 20 - seatCount, // 根據需求計算可歸還數量
        };
      })
    );
    spots.value = spotsData;
  } catch (err) {
    console.error("無法獲取租借站點資料:", err);
    error.value = "無法載入租借站點資料，請稍後再試。";
  }
};

// 開啟一個站點的資訊視窗
const openInfoWindowForSpot = (spot) => {
  searchResultMarker.value = null; // 清除搜尋結果的標記
  infoWindow.value.opened = false;
  nextTick(() => {
    infoWindow.value = {
      position: spot.position,
      opened: true,
      isSearchResult: false,
      spot: spot,
      title: spot.name,
    };
  });
};

// 關閉資訊視窗
const closeInfoWindow = () => {
  infoWindow.value.opened = false;
  searchResultMarker.value = null; // 同時清除搜尋結果的標記
};

// 處理導航至租借或歸還頁面
const handleNavigation = (action) => {
  const spotId = infoWindow.value.spot?.id
  let routeParams = { name: 'rec-rent-user', params: { action } }

  if (action === 'order' && spotId) {
    routeParams.query = { spotId }
  }

  // --- [新增] 將站點 ID 寫入 Pinia，確保切換頁面時能讀取 ---
  if (spotId) {
    memberAuthStore.setSpotId(spotId);
  }

  // --- 統一導航邏輯 ---
  // 1. 檢查是否處於「假登入」狀態 (isLogin=true 但 memId 遺失)
  if (memberAuthStore.isLogin && !memberAuthStore.member?.memId ) {
    console.warn("偵測到登入狀態異常(無會員ID)，執行強制登出並重導向");
    memberAuthStore.clearMemberLogin();
    const redirectPath = router.resolve(routeParams).path;
    router.push({ name: 'login', query: { redirect: redirectPath } });
    return;
  }

  // 2. 正常狀態判斷
  if (memberAuthStore.isLogin||adminAuthStore.isLogin ) {
    console.log("準備導向 Store 中的會員ID:", memberAuthStore.member?.memId);
    router.push(routeParams);
  } else {
    const redirectPath = router.resolve(routeParams).path;
    router.push({ name: 'login', query: { redirect: redirectPath } });
  }
}

// Vue 組件掛載時執行的初始化
onMounted(() => {
  initializeMapCenter();
  fetchSpots();
});
</script>

<template>
  <div class="map-container-wrapper">
      <!-- 地點搜尋列 -->
      <div class="search-bar-container">
        <GMapAutocomplete
          @place_changed="onPlaceChanged"
          :options="{
            fields: ['geometry', 'formatted_address', 'name'],
            componentRestrictions: { country: 'tw' },
          }"
        >
          <input
            type="text"
            class="search-input"
            placeholder="搜尋地點..."
            v-model="searchQuery"
            @keyup.enter="performSearch"
          />
        </GMapAutocomplete>
        <button class="search-button" @click="performSearch" title="搜尋">
          <el-icon :size="20"><LocationFilled /></el-icon>
        </button>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <GMapMap
        v-if="!error"
        :center="center"
        :zoom="zoom"
        :options="mapOptions"
        class="map"
        map-id="d2fc83863651fe9c90d73c8a"
      >
        <!-- 渲染站點標記 -->
        <GMapMarker
          v-for="spot in spots"
          :key="spot.id"
          :position="spot.position"
          :title="spot.name"
          :clickable="true"
          :icon="getMarkerIcon(spot.status)"
          @click="openInfoWindowForSpot(spot)"
        />
        <!-- 渲染搜尋結果標記 -->
        <GMapMarker
          v-if="searchResultMarker"
          :key="'search-result'"
          :position="searchResultMarker"
        />

        <GMapInfoWindow
          :opened="infoWindow.opened"
          :position="infoWindow.position"
          :options="{ pixelOffset: { width: 0, height: -32 } }"
          @closeclick="closeInfoWindow"
        >
          <div class="info-window-content">
            <!-- 顯示站點資訊 -->
            <div v-if="infoWindow.spot">
              <h5>{{ infoWindow.title }}</h5>
              <p><strong>ID:</strong> {{ infoWindow.spot.id }}<strong>   |   狀態:</strong> {{ infoWindow.spot.status }}</p>
              <p><strong>目前座位數量:</strong> {{ infoWindow.spot.seatCount }}</p>
              <p><strong>可歸還位置:</strong> {{ infoWindow.spot.returnCount }}</p>
              <div class="button-group">
                <button
                  @click="handleNavigation('order')"
                  class="btn btn-success"
                  :disabled="infoWindow.spot.status !== '營運中' || infoWindow.spot.seatCount <= 0"
                  :class="{ 'btn-disabled': infoWindow.spot.status !== '營運中' || infoWindow.spot.seatCount <= 0 }"
                >
                  租借
                </button>
                <button
                  @click="handleNavigation('complete')"
                  class="btn btn-primary"
                  :disabled="infoWindow.spot.status !== '營運中' || infoWindow.spot.returnCount <= 0"
                  :class="{ 'btn-disabled': infoWindow.spot.status !== '營運中' || infoWindow.spot.returnCount <= 0 }"
                >
                  歸還
                </button>
                <button @click="" class="btn btn-issue">
                  回報 <br />
                  問題
                </button>
              </div>
            </div>
            <!-- 顯示搜尋結果 -->
            <div v-else-if="infoWindow.isSearchResult">
              <h4>{{ infoWindow.title }}</h4>
              <a
                v-if="infoWindow.position"
                :href="`https://www.google.com/maps/search/?api=1&query=${infoWindow.position.lat},${infoWindow.position.lng}`"
                target="_blank"
                rel="noopener noreferrer"
                class="map-link"
              >
                在 Google 地圖中查看
              </a>
            </div>
          </div>
        </GMapInfoWindow>
      </GMapMap>
    </div>
</template>

<style scoped>
.map-container-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}
.map {
  width: 100%;
  height: 100%;
}
.error-message {
  color: red;
  padding: 20px;
  text-align: center;
}
.info-window-content {
  padding: 0px;
  min-width: 200px;
}
.info-window-content h4,
.info-window-content p {
  margin: 1px 0;
}

/* 在 Google 地圖中查看的連結樣式 */
.info-window-content .map-link {
  display: block;
  margin-top: 10px;
  font-weight: 500;
  text-decoration: none;
  color: #007bff;
}
.info-window-content .map-link:hover {
  text-decoration: underline;
}
.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: space-around;
}
.btn {
  display: inline-block;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  user-select: none;
  border: 1px solid transparent;
  margin: 0.3rem;
  padding: 0.15rem 0.45rem;
  font-size: 1.5rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  cursor: pointer;
  text-decoration: none; /* Add this to make router-link look like a button */
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
.btn-issue {
  font-size: medium;
  color: #fff;
  background-color: #cf820e;
  border-color: #cf820e;
}

/* 當按鈕被禁用時的樣式 */
.btn-disabled {
  background-color: #6b6b6b;
  border-color: #6b6b6b;
  cursor: not-allowed;
}

/* --- 新增/修改的搜尋列樣式 --- */
.search-bar-container {
  position: absolute;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  height: 48px;
  z-index: 10;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.search-bar-container :deep(.search-input) {
  flex-grow: 12;
  width: 100%;
  height: 58px;
  border: none;
  outline: none;
  padding: 0 13px;
  font-size: 2.2rem;
  background-color: transparent;
}

/* 這是 GMapAutocomplete 元件的包裝器，我們讓它填滿空間 */
:deep(.pac-container) {
  z-index: 1051 !important; /* 確保建議清單顯示在其他元素之上 */
}

.search-button {
  height: 30px;
  margin-left: 8px;
  padding: 8px;
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover {
  background-color: #0056b3;
}
</style>
