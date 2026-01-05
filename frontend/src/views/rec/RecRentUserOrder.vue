<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

// --- 狀態定義 ---
const spots = ref([]);
const userLocation = ref(null);
const nearestSpot = ref(null);
const selectedSpot = ref(null);
const selectedPaymentMethod = ref('CREDIT_CARD');
const isLoading = ref(false);
const errorMessage = ref('');

const API_BASE_URL = 'http://localhost:8080/api';

// 付款方式選項
const paymentMethods = [
  { value: 'CREDIT_CARD', text: '信用卡' },
  { value: 'LINE_PAY', text: 'LINE Pay' },
  { value: 'APPLE_PAY', text: 'Apple Pay' },
];

// --- API 呼叫 ---

// 載入所有站點
const loadSpots = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/spots`);
    spots.value = response.data.map(spot => ({
      ...spot,
      distance: null
    }));
  } catch (error) {
    console.error('載入站點失敗:', error);
    errorMessage.value = '無法載入站點資料。';
  }
};

// --- 核心邏輯 ---

// 1. 取得使用者目前位置
const getUserLocation = () => {
  isLoading.value = true;
  errorMessage.value = '';
  nearestSpot.value = null;

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        userLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };
        findNearestSpot();
        isLoading.value = false;
      },
      (error) => {
        console.error('地理位置錯誤:', error);
        errorMessage.value = '無法取得您的目前位置。請確認已授權瀏覽器存取位置資訊。';
        isLoading.value = false;
      }
    );
  } else {
    errorMessage.value = '您的瀏覽器不支援地理位置功能。';
    isLoading.value = false;
  }
};

// 2. 計算兩點之間距離 (Haversine 公式)
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // 地球半徑 (公里)
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
    Math.cos(lat2 * (Math.PI / 180)) *
    Math.sin(dLon / 2) *
    Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c; // 回傳公里
};

// 3. 尋找最近的站點
const findNearestSpot = () => {
  if (!userLocation.value || spots.value.length === 0) return;

  let minDistance = Infinity;
  let closestSpot = null;

  spots.value.forEach((spot) => {
    // 假設 spot 物件有 spotLat 和 spotLng 欄位
    const distance = calculateDistance(
      userLocation.value.lat,
      userLocation.value.lng,
      spot.spotLat,
      spot.spotLng
    );
    spot.distance = distance; // 更新每個站點的距離
    if (distance < minDistance) {
      minDistance = distance;
      closestSpot = spot;
    }
  });

  nearestSpot.value = closestSpot;
  if (closestSpot) {
    selectedSpot.value = closestSpot; // 自動選取最近的站點
  }
};

// 租借服務
const rentService = async () => {
    if (!selectedSpot.value) {
        alert('請先選擇一個站點。');
        return;
    }

    // 這裡的 memId 和 seatsId 需要根據實際應用邏輯取得
    // 例如，memId 可能來自登入狀態，seatsId 來自於選擇座位的介面
    const rentalData = {
        memId: 1, // 假設的會員 ID
        spotIdRent: selectedSpot.value.spotId,
        seatsId: 'S001', // 假設的座位 ID, 應改為從可選座位中動態選擇
        paymentMethod: selectedPaymentMethod.value, // 付款方式
    };

    try {
        isLoading.value = true;
        const response = await axios.post(`${API_BASE_URL}/rec-rents`, rentalData);
        if (response.status === 201 || response.status === 200) {
            alert(`租借成功！\n站點：${selectedSpot.value.spotName}\n付款方式：${paymentMethods.find(p => p.value === selectedPaymentMethod.value).text}`);
            // 可選擇導向到訂單歷史頁面或重置介面
        } else {
            errorMessage.value = '租借失敗，請稍後再試。';
        }
    } catch (error) {
        console.error('租借請求失敗:', error);
        errorMessage.value = `租借失敗: ${error.response?.data?.message || error.message}`;
    } finally {
        isLoading.value = false;
    }
};


// --- Computed ---
// 根據距離排序的站點列表
const sortedSpots = computed(() => {
  if (!userLocation.value) {
    return spots.value;
  }
  return [...spots.value].sort((a, b) => a.distance - b.distance);
});


// --- Lifecycle Hooks ---
onMounted(() => {
  loadSpots();
});

</script>

<template>
  <div class="user-order-container">
    <div class="card">
      <h1 class="card-header">座位租借服務</h1>

      <!-- 步驟一: 定位 -->
      <div class="card-body">
        <h2><i class="fas fa-map-marker-alt"></i> 步驟一：尋找最近的站點</h2>
        <button @click="getUserLocation" class="btn btn-primary" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isLoading ? '定位中...' : '開始定位' }}
        </button>
        <div v-if="errorMessage" class="alert alert-danger mt-3">{{ errorMessage }}</div>
      </div>

      <!-- 步驟二: 選擇站點 -->
      <div v-if="userLocation" class="card-body">
        <h2><i class="fas fa-store"></i> 步驟二：選擇租借站點</h2>
        <p>您的位置：緯度 {{ userLocation.lat.toFixed(4) }}, 經度 {{ userLocation.lng.toFixed(4) }}</p>

        <div v-if="nearestSpot" class="alert alert-info">
            距離您最近的站點是：<strong>{{ nearestSpot.spotName }}</strong> (距離約 {{ nearestSpot.distance.toFixed(2) }} 公里)
        </div>

        <div class="form-group">
            <label for="spot-select">請選擇站點：</label>
            <select id="spot-select" class="form-control" v-model="selectedSpot">
                <option v-for="spot in sortedSpots" :key="spot.spotId" :value="spot">
                    {{ spot.spotName }} ({{ spot.spotAddress }}) - 約 {{ spot.distance ? spot.distance.toFixed(2) + '公里' : 'N/A' }}
                </option>
            </select>
        </div>
         <div v-if="selectedSpot" class="spot-details">
            <h4>已選站點資訊</h4>
            <p><strong>站點名稱：</strong> {{ selectedSpot.spotName }}</p>
            <p><strong>地址：</strong> {{ selectedSpot.spotAddress }}</p>
            <!-- 在此處可以加上查詢該站點可用座位的邏輯 -->
        </div>
      </div>


      <!-- 步驟三: 付款與租借 -->
      <div v-if="selectedSpot" class="card-body">
        <h2><i class="fas fa-credit-card"></i> 步驟三：選擇付款方式並租借</h2>
        <div class="form-group">
          <label for="payment-method">付款方式：</label>
          <select id="payment-method" class="form-control" v-model="selectedPaymentMethod">
            <option v-for="method in paymentMethods" :key="method.value" :value="method.value">
              {{ method.text }}
            </option>
          </select>
        </div>

        <button @click="rentService" class="btn btn-success btn-lg" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
          立即租借
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

.user-order-container {
  padding: 20px;
  max-width: 800px;
  margin: auto;
  font-family: 'Microsoft JhengHei', sans-serif;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  background-color: #007bff;
  color: white;
  padding: 15px 20px;
  margin: 0;
  text-align: center;
}

.card-body {
  padding: 20px;
  border-bottom: 1px solid #eee;
}
.card-body:last-child {
  border-bottom: none;
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
    padding: .375rem .75rem;
    font-size: 1rem;
    line-height: 1.5;
    color: #495057;
    background-color: #fff;
    background-clip: padding-box;
    border: 1px solid #ced4da;
    border-radius: .25rem;
    transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
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
.spot-details {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 5px;
    margin-top: 15px;
}
.spinner-border-sm {
    width: 1rem;
    height: 1rem;
    border-width: .2em;
}
.spinner-border {
    display: inline-block;
    width: 2rem;
    height: 2rem;
    vertical-align: text-bottom;
    border: .25em solid currentColor;
    border-right-color: transparent;
    border-radius: 50%;
    -webkit-animation: spinner-border .75s linear infinite;
    animation: spinner-border .75s linear infinite;
}
</style>
