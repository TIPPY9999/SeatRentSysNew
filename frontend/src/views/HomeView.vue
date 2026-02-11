<script setup>
<<<<<<< Updated upstream
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
=======
import { ref, onMounted, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
>>>>>>> Stashed changes
import axios from 'axios';

const router = useRouter();

// --- 元件狀態 ---
const searchKeyword = ref('');
const isMapVisible = ref(false); // 控制地圖 Modal 顯示
const selectedSpotForMap = ref(null); // 目前點選要導覽的點位
const mapCenter = ref({ lat: 25.033964, lng: 121.564472 }); // 預設台北 101
const mapZoom = ref(12);
const allSpots = ref([]); // 全台所有據點
const isSearchTriggered = ref(false); // 標記是否為搜尋框觸發的彈窗
const isSuggestionsVisible = ref(false); // 管理建議清單顯示

// 熱門標籤
const hotTags = ['台北車站', '信義區', '圖書館', '咖啡廳'];

// 流程步驟
const steps = [
  { icon: 'fas fa-map-marker-alt', title: '尋找座位', desc: '透過地圖快速找到附近的空位' },
  { icon: 'fas fa-qrcode', title: '掃碼入座', desc: '掃描桌上 QR Code 即可開始使用' },
  { icon: 'fas fa-coffee', title: '享受時光', desc: '專注工作或放鬆，按時計費' },
];

<<<<<<< Updated upstream
// 熱門點位資料
=======
/**
 * -------------------------------------------
 * 1. 資料獲取邏輯 (Data Fetching)
 * -------------------------------------------
 */

// 熱門點位資料 (由 API 獲取)
>>>>>>> Stashed changes
const hotSpots = ref([]);

const fetchHotSpots = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/analyze/hot-spots');
<<<<<<< Updated upstream
    // 後端回傳的資料結構包含: spotId, spotName, spotStatus, availableSeats, spotImage, orderCount
    // 我們需要將其對映到前端使用的欄位名
=======
>>>>>>> Stashed changes
    hotSpots.value = response.data.map(spot => ({
      id: spot.spotId,
      name: spot.spotName,
      status: spot.spotStatus,
      seats: spot.availableSeats,
<<<<<<< Updated upstream
      // 如果後端沒有圖片則給予預設圖
      image: spot.spotImage ? (spot.spotImage.startsWith('http') ? spot.spotImage : `http://localhost:8080/${spot.spotImage}`) : 'https://images.unsplash.com/photo-1517502884422-41eaead166d4?q=80&w=600&auto=format&fit=crop'
    }));
  } catch (error) {
    console.error('無法取得熱門點位資料:', error);
=======
      lat: spot.latitude,
      lng: spot.longitude,
      // 圖片處理邏輯
      image: spot.spotImage 
        ? (spot.spotImage.startsWith('http') ? spot.spotImage : `http://localhost:8080/${spot.spotImage}`) 
        : `https://images.unsplash.com/photo-1517502884422-41eaead166d4?q=80&w=600&auto=format&fit=crop`
    }));
  } catch (error) {
    console.error('無法取得熱門點位資料:', error);
    // 回退到模擬資料以確保介面不空白
    hotSpots.value = [
      { id: 1, name: '信義誠品閱讀區', status: '營運中', seats: 12, lat: 25.0392, lng: 121.5654, image: 'https://images.unsplash.com/photo-1507842217121-9e962835d771?q=80&w=600&auto=format&fit=crop' },
      { id: 2, name: '大安森林公園共享亭', status: '營運中', seats: 5, lat: 25.0297, lng: 121.5364, image: 'https://images.unsplash.com/photo-1493934558415-9d19f0b2b4d2?q=80&w=600&auto=format&fit=crop' }
    ];
  }
};

const fetchAllSpots = async () => {
  try {
    const response = await axios.get('http://localhost:8080/spot/list');
    allSpots.value = response.data.map(spot => ({
      id: spot.spotId,
      name: spot.spotName,
      status: spot.spotStatus,
      lat: parseFloat(spot.latitude),
      lng: parseFloat(spot.longitude),
      seats: spot.availableSeats || 0
    }));
  } catch (error) {
    console.error('無法取得所有據點資料:', error);
>>>>>>> Stashed changes
  }
};

onMounted(() => {
  fetchHotSpots();
<<<<<<< Updated upstream
=======
  fetchAllSpots();
});

/**
 * -------------------------------------------
 * 2. 搜尋與建議列表邏輯 (Search & Suggestions)
 * -------------------------------------------
 */

// 模糊查詢過濾建議
const filteredSuggestions = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase();
  if (!kw) return [];
  return allSpots.value
    .filter(s => s.name.toLowerCase().includes(kw))
    .slice(0, 5);
>>>>>>> Stashed changes
});

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return;
  const exactMatch = allSpots.value.find(s => s.name === searchKeyword.value.trim());
  if (exactMatch) {
    handleReserveClick(exactMatch);
  } else {
    // 若非精確匹配，可執行一般搜尋
    console.log('Search:', searchKeyword.value);
  }
};

const handleSelectSuggestion = (spot) => {
  searchKeyword.value = spot.name;
  isSuggestionsVisible.value = false;
  handleReserveClick(spot);
};

const hideSuggestions = () => {
  setTimeout(() => {
    isSuggestionsVisible.value = false;
  }, 200);
};

// 處理 Google Autocomplete 變化
const onPlaceChanged = (place) => {
  if (place && place.geometry && place.geometry.location) {
    const location = place.geometry.location;
    handleSearchLocation(location.lat(), location.lng());
  }
};

const handleSearchLocation = (lat, lng) => {
  isSearchTriggered.value = true;
  selectedSpotForMap.value = null;
  isMapVisible.value = true;
  mapCenter.value = { lat, lng };
  mapZoom.value = 13;
  setTimeout(() => { mapZoom.value = 15; }, 300);
};

/**
 * -------------------------------------------
 * 3. 地圖地圖與定位邏輯 (Map Modal & Zoom)
 * -------------------------------------------
 */

const handleReserveClick = (spot) => {
  isSearchTriggered.value = false;
  selectedSpotForMap.value = spot;
  isMapVisible.value = true;
  
  // 1. 設定該點位中心
  mapCenter.value = { lat: spot.lat, lng: spot.lng };
  mapZoom.value = 13;

  // 2. [WOW 效果] 平滑縮放至該點位
  setTimeout(() => {
    mapZoom.value = 17;
  }, 300);
};

const handleMarkerClick = (spot) => {
  selectedSpotForMap.value = spot;
};

// 前往租借操作頁面 (Rec 模組)
const confirmAndRent = () => {
  if (!selectedSpotForMap.value) return;
  isMapVisible.value = false;
  router.push({ 
    name: 'rec-rent-user', 
    params: { action: 'order' }, 
    query: { spotId: selectedSpotForMap.value.id } 
  });
};
</script>

<template>
  <div class="home-view-wrapper">
    <!-- 第一屏：Hero Section -->
    <div class="hero-container">
      <div class="hero-bg"></div>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title animate-up">Take@Seat</h1>
        <p class="hero-subtitle animate-up delay-1">隨時隨地，輕鬆入座</p>
        
        <div class="cta-buttons animate-up delay-2">
          <router-link to="/SearchSpot" class="cta-btn primary">
            開始尋找租借點 <i class="fas fa-arrow-right arrow-icon"></i>
          </router-link>
        </div>

        <div class="quick-search-card animate-up delay-3">
          <div class="search-input-wrapper-container">
            <div class="search-input-wrapper">
              <i class="fas fa-search search-icon"></i>
              <GMapAutocomplete
                @place_changed="onPlaceChanged"
                :options="{
                  fields: ['geometry', 'formatted_address', 'name'],
                  componentRestrictions: { country: 'tw' },
                }"
                style="flex: 1"
              >
                <input 
                  v-model="searchKeyword" 
                  type="text" 
                  placeholder="輸入據點名稱或地點..." 
                  @keyup.enter="handleSearch"
                  @focus="isSuggestionsVisible = true"
                  @blur="hideSuggestions"
                />
              </GMapAutocomplete>
              <button class="search-btn" @click="handleSearch">搜尋</button>
            </div>
            
            <!-- 自定義建議列表 -->
            <ul v-if="isSuggestionsVisible && filteredSuggestions.length > 0" class="search-suggestions">
              <li 
                v-for="spot in filteredSuggestions" 
                :key="spot.id"
                @mousedown="handleSelectSuggestion(spot)"
              >
                <i class="fas fa-chair"></i>
                <span class="spot-name">{{ spot.name }}</span>
                <span class="spot-status">({{ spot.status }})</span>
              </li>
            </ul>
          </div>
          <div class="hot-tags">
            <span>熱門：</span>
            <span v-for="tag in hotTags" :key="tag" class="tag" @click="searchKeyword = tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 第二部分：內容區塊 -->
    <div class="content-section">
      <!-- 簡單三步驟 -->
      <section class="how-it-works">
        <div class="section-header">
          <h2>簡單三步驟</h2>
          <p>輕鬆開啟您的共享空間體驗</p>
        </div>
        <div class="steps-grid">
          <div v-for="(step, index) in steps" :key="index" class="step-item">
            <div class="step-icon">
              <i :class="step.icon"></i>
            </div>
            <h3>{{ step.title }}</h3>
            <p>{{ step.desc }}</p>
          </div>
        </div>
      </section>

      <!-- 熱門點位 -->
      <section class="spot-section">
        <div class="section-header">
          <h2>熱門租借點</h2>
          <p>探索城市中最受歡迎的角落</p>
        </div>
        <div class="spots-grid">
          <div v-for="spot in hotSpots" :key="spot.id" class="spot-card">
            <div class="card-image" :style="{ backgroundImage: `url(${spot.image})` }">
              <span class="status-badge" :class="{ 'full': spot.seats === 0 }">{{ spot.status }}</span>
            </div>
            <div class="card-body">
              <h3>{{ spot.name }}</h3>
              <div class="card-meta">
                <span><i class="fas fa-chair"></i> 剩餘 {{ spot.seats }} 位</span>
                <!-- [修改] 觸發地圖預覽定位邏輯 -->
                <a href="javascript:void(0)" @click="handleReserveClick(spot)" class="card-link">
                  預約 <i class="fas fa-chevron-right"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- [新設計] 地圖預覽與跳轉確認彈窗 -->
    <el-dialog
      v-model="isMapVisible"
      title="確認租借位址"
      width="800px"
      destroy-on-close
      class="map-preview-dialog"
    >
      <div class="map-preview-body">
        <div v-if="selectedSpotForMap" class="spot-info-mini">
          <h4>{{ selectedSpotForMap.name }}</h4>
          <p><i class="fas fa-map-marker-alt"></i> 即將為您跳轉至此點位的租借頁面</p>
        </div>
        <div v-else-if="isSearchTriggered" class="spot-info-mini">
          <h4>請選擇附近的站點</h4>
          <p><i class="fas fa-info-circle"></i> 點擊地圖上的標記以選擇租借點</p>
        </div>
        <div class="map-iframe-container">
          <GMapMap
            :center="mapCenter"
            :zoom="mapZoom"
            :options="{ disableDefaultUI: true, zoomControl: true }"
            style="width: 100%; height: 400px; border-radius: 12px;"
          >
            <GMapMarker
              v-for="spot in allSpots"
              :key="spot.id"
              :position="{ lat: spot.lat, lng: spot.lng }"
              :clickable="true"
              @click="handleMarkerClick(spot)"
              :icon="selectedSpotForMap && selectedSpotForMap.id === spot.id ? 'http://maps.google.com/mapfiles/ms/icons/green-dot.png' : 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'"
            />
          </GMapMap>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="isMapVisible = false">返回</el-button>
          <el-button type="success" @click="confirmAndRent" class="confirm-btn">
            確認並開始租借 <i class="fas fa-check"></i>
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.home-view-wrapper { width: 100%; display: flex; flex-direction: column; }
.hero-container { position: relative; height: 100vh; width: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: #000; }
.hero-bg { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-image: url('https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=1920&auto=format&fit=crop'); background-size: cover; background-position: center; z-index: 0; animation: kenBurns 20s ease-in-out infinite alternate; }
.hero-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.6) 100%); z-index: 1; }
.hero-content { position: relative; z-index: 2; color: #ffffff; text-align: center; padding: 20px; }
.hero-title { font-size: 5rem; font-weight: 700; margin-bottom: 1rem; text-shadow: 2px 2px 15px rgba(0, 0, 0, 0.5); letter-spacing: 2px; }
.hero-subtitle { font-size: 1.5rem; font-weight: 300; margin-bottom: 2rem; text-shadow: 1px 1px 10px rgba(0, 0, 0, 0.5); letter-spacing: 1px; opacity: 0.9; }
.cta-buttons { display: flex; flex-wrap: wrap; gap: 1rem; justify-content: center; }
.cta-btn { display: inline-flex; align-items: center; justify-content: center; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-size: 1.1rem; font-weight: 500; transition: transform 0.2s ease, box-shadow 0.2s ease; border: 1px solid rgba(255, 255, 255, 0.5); min-width: 160px; }
.cta-btn:hover { transform: translateY(-3px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2); }
.cta-btn.primary { background-color: #4CAF50; color: white; font-size: 1.25rem; padding: 16px 40px; border: none; font-weight: 600; box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4); }
.cta-btn.primary:hover { background-color: #45a049; transform: translateY(-5px); box-shadow: 0 8px 25px rgba(76, 175, 80, 0.6); }
.quick-search-card { margin-top: 40px; background: rgba(255, 255, 255, 0.2); backdrop-filter: blur(10px); padding: 20px 30px; border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.3); width: 100%; max-width: 600px; margin-left: auto; margin-right: auto; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); }

.search-input-wrapper-container { position: relative; width: 100%; }
.search-input-wrapper { display: flex; align-items: center; background: white; border-radius: 50px; padding: 5px 5px 5px 20px; margin-bottom: 12px; }
.search-icon { color: #909399; margin-right: 10px; }
.search-input-wrapper input { border: none; outline: none; flex: 1; font-size: 1rem; color: #333; }
.search-btn { background: #4CAF50; color: white; border: none; padding: 10px 24px; border-radius: 50px; cursor: pointer; font-weight: 600; transition: background 0.2s; }
.search-btn:hover { background: #45a049; }

/* 搜尋建議列表 */
.search-suggestions { position: absolute; top: 105%; left: 0; right: 0; background: white; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); list-style: none; padding: 8px 0; margin: 0; z-index: 1000; border: 1px solid #eee; text-align: left; }
.search-suggestions li { padding: 12px 20px; cursor: pointer; display: flex; align-items: center; gap: 12px; transition: background 0.2s; color: #333; }
.search-suggestions li:hover { background: #f5f7fa; }
.search-suggestions li i { color: #4CAF50; font-size: 0.9rem; }
.search-suggestions .spot-name { font-weight: 600; flex: 1; }
.search-suggestions .spot-status { font-size: 0.8rem; color: #909399; }

.hot-tags { display: flex; gap: 10px; font-size: 0.9rem; color: rgba(255, 255, 255, 0.9); justify-content: center; flex-wrap: wrap; }
.tag { cursor: pointer; text-decoration: underline; transition: color 0.2s; }
.tag:hover { color: #fff; text-shadow: 0 0 5px rgba(255, 255, 255, 0.5); }

.content-section { background-color: #f9f9f9; padding: 60px 20px; display: flex; flex-direction: column; gap: 80px; }
.section-header { text-align: center; margin-bottom: 40px; }
.section-header h2 { font-size: 2.5rem; color: #333; margin-bottom: 10px; font-weight: 700; }
.section-header p { color: #666; font-size: 1.1rem; }

.steps-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 30px; max-width: 1200px; margin: 0 auto; text-align: center; }
.step-item { padding: 20px; }
.step-icon { width: 80px; height: 80px; background: #e8f5e9; color: #4CAF50; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; margin: 0 auto 20px; transition: transform 0.3s; }
.step-item:hover .step-icon { transform: scale(1.1) rotate(5deg); }
.step-item h3 { font-size: 1.5rem; margin-bottom: 10px; color: #333; }
.step-item p { color: #666; line-height: 1.6; }

.spots-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 30px; max-width: 1200px; margin: 0 auto; width: 100%; }
.spot-card { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05); transition: transform 0.3s, box-shadow 0.3s; }
.spot-card:hover { transform: translateY(-5px); box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1); }
.card-image { height: 200px; background-size: cover; background-position: center; position: relative; }
.status-badge { position: absolute; top: 10px; right: 10px; background: #4CAF50; color: white; padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: 600; }
.status-badge.full { background: #f56c6c; }
.card-body { padding: 20px; }
.card-body h3 { margin: 0 0 10px; font-size: 1.2rem; color: #333; }
.card-meta { display: flex; justify-content: space-between; align-items: center; color: #666; font-size: 0.95rem; }
.card-link { color: #4CAF50; text-decoration: none; font-weight: 600; display: flex; align-items: center; gap: 5px; transition: gap 0.2s; }
.card-link:hover { gap: 8px; }

/* 地圖預覽彈窗 */
.spot-info-mini h4 { margin: 0 0 5px; font-size: 1.25rem; color: #2c3e50; }
.spot-info-mini p { margin: 0 0 15px; color: #7f8c8d; font-size: 0.9rem; }
.confirm-btn { padding: 12px 30px; font-size: 1.1rem; font-weight: 600; }

@keyframes kenBurns { 0% { transform: scale(1); } 100% { transform: scale(1.15); } }
.animate-up { opacity: 0; transform: translateY(30px); animation: fadeInUp 1s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; }
.delay-1 { animation-delay: 0.2s; }
.delay-2 { animation-delay: 0.4s; }
.delay-3 { animation-delay: 0.6s; }
@keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }
</style>
