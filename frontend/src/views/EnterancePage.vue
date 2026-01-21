<script setup>
import { ref, onMounted, nextTick } from "vue";
import "@googlemaps/extended-component-library/api_loader.js";
import "@googlemaps/extended-component-library/place_picker.js";
import axios from "axios";
import MainLayout from "@/layouts/MainLayout.vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

// --- Router and Store ---
const router = useRouter();
const authStore = useAuthStore();

// 1. ---組態設定---
const center = ref({ lat: 23.973875, lng: 120.982025 });
const backendApiUrl = "http://localhost:8080/spot/list";

// 2. ---狀態定義---
const spots = ref([]);
const error = ref(null);
const infoWindow = ref({
  position: null,
  spot: null,
  opened: false,
});

// 3. ---核心邏輯---
const initializeMapCenter = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        center.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };
      },
      (error) => {
        console.warn(`無法獲取地理位置: ${error.message}。將使用預設中心點。`);
      }
    );
  } else {
    console.warn("此瀏覽器不支援地理位置功能。將使用預設中心點。");
  }
};

const onPlaceChanged = (event) => {
  const place = event.detail.place;
  if (place && place.geometry && place.geometry.location) {
    center.value = {
      lat: place.geometry.location.lat(),
      lng: place.geometry.location.lng(),
    };
  }
};

const fetchSpots = async () => {
  try {
    const response = await axios.get(backendApiUrl);
    spots.value = response.data.map((spot) => ({
      id: spot.spotId,
      name: spot.spotName,
      status: spot.spotStatus,
      position: {
        lat: parseFloat(spot.latitude),
        lng: parseFloat(spot.longitude),
      },
    }));
  } catch (err) {
    console.error("無法獲取租借站點資料:", err);
    error.value = "無法載入租借站點資料，請稍後再試。";
  }
};

const openInfoWindow = (spot) => {
  // 1. 先強制將狀態設為關閉，確保任何情況下都能從乾淨的狀態開始
  infoWindow.value.opened = false;

  // 2. 使用 nextTick 等待 UI 更新週期完成
  nextTick(() => {
    // 3. 在下一個更新週期，用新資料賦予一個全新的狀態物件來打開視窗
    infoWindow.value = {
      position: spot.position,
      spot: spot,
      opened: true,
    };
  });
};

const closeInfoWindow = () => {
  // 採用「物件替換」模式，徹底重設狀態，確保響應性被觸發
  infoWindow.value = {
    position: null,
    spot: null,
    opened: false,
  };
};

// --- 新增的導航邏輯 ---
const handleNavigation = (action) => {
  if (authStore.isLogin) {
    // 已登入，直接導航
    router.push({ name: 'rec-rent-user', params: { action } });
  } else {
    // 未登入，導向登入頁並帶上重定向參數
    const redirectPath = router.resolve({ name: 'rec-rent-user', params: { action } }).path;
    router.push({ name: 'login', query: { redirect: redirectPath } });
  }
};

onMounted(() => {
  initializeMapCenter();
  fetchSpots();
});
</script>

<template>
  <MainLayout>
    <div class="map-container-wrapper">
      <div v-if="error" class="error-message">{{ error }}</div>
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
          
                  >
          <div v-if="infoWindow.spot" class="info-window-content">
            <h4>{{ infoWindow.spot.name }}</h4>
            <p><strong>ID:</strong> {{ infoWindow.spot.id }}</p>
            <p><strong>狀態:</strong> {{ infoWindow.spot.status }}</p>
            <div class="button-group">
              <button @click="handleNavigation('order')" class="btn btn-success">
                租借
              </button>
              <button @click="handleNavigation('complete')" class="btn btn-primary">
                歸還
              </button>
            </div>
          </div>
        </GMapInfoWindow>
      </GMapMap>
    </div>
  </MainLayout>
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
