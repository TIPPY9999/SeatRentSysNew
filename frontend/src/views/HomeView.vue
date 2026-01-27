<script setup>
import { ref } from 'vue';
import { RouterLink } from 'vue-router';

// --- 模擬資料 ---
const searchKeyword = ref('');

// 熱門標籤
const hotTags = ['台北車站', '信義區', '圖書館', '咖啡廳'];

// 流程步驟
const steps = [
  { icon: 'fas fa-map-marker-alt', title: '尋找座位', desc: '透過地圖快速找到附近的空位' },
  { icon: 'fas fa-qrcode', title: '掃碼入座', desc: '掃描桌上 QR Code 即可開始使用' },
  { icon: 'fas fa-coffee', title: '享受時光', desc: '專注工作或放鬆，按時計費' },
];

// 熱門點位資料
const hotSpots = ref([
  { id: 1, name: '信義誠品閱讀區', status: '營運中', seats: 12, image: 'https://images.unsplash.com/photo-1507842217121-9e962835d771?q=80&w=600&auto=format&fit=crop' },
  { id: 2, name: '大安森林公園共享亭', status: '營運中', seats: 5, image: 'https://images.unsplash.com/photo-1493934558415-9d19f0b2b4d2?q=80&w=600&auto=format&fit=crop' },
  { id: 3, name: '松山文創園區', status: '滿位中', seats: 0, image: 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?q=80&w=600&auto=format&fit=crop' },
  { id: 4, name: '內湖科技園區 Hub', status: '營運中', seats: 8, image: 'https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=600&auto=format&fit=crop' },
]);

const handleSearch = () => {
  // 這裡可以實作跳轉到搜尋頁並帶入參數
  console.log('Search:', searchKeyword.value);
};
</script>

<template>
  <div class="home-view-wrapper">
    <!-- 第一屏：Hero Section -->
    <div class="hero-container">
      <!-- 1. 背景圖片層 -->
      <div class="hero-bg"></div>

      <!-- 2. 漸層遮罩 -->
      <div class="hero-overlay"></div>

      <!-- 3. 置中內容 -->
      <div class="hero-content">
        <h1 class="hero-title animate-up">Take@Seat</h1>
        <p class="hero-subtitle animate-up delay-1">隨時隨地，輕鬆入座</p>
        
        <!-- 按鈕組 -->
        <div class="cta-buttons animate-up delay-2">
          <router-link to="/SearchSpot" class="cta-btn primary">
            開始尋找租借點 <i class="fas fa-arrow-right arrow-icon"></i>
          </router-link>
        </div>

        <!-- [新增] 快速搜尋卡 (懸浮於 Hero 底部) -->
        <div class="quick-search-card animate-up delay-3">
          <div class="search-input-wrapper">
            <i class="fas fa-search search-icon"></i>
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="輸入地點或關鍵字..." 
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜尋</button>
          </div>
          <div class="hot-tags">
            <span>熱門：</span>
            <span v-for="tag in hotTags" :key="tag" class="tag" @click="searchKeyword = tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 第二部分：內容區塊 (白色背景) -->
    <div class="content-section">
      
      <!-- [新增] 流程介紹 -->
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

      <!-- [新增] 熱門點位 -->
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
                <router-link to="/SearchSpot" class="card-link">預約 <i class="fas fa-chevron-right"></i></router-link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-view-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.hero-container {
  position: relative;
  height: 100vh; /* 佔滿第一屏 */
  width: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000; /* 預防圖片載入前的底色 */
}

/* 背景圖片層：獨立出來做縮放動畫 */
.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=1920&auto=format&fit=crop'); /* 範例圖片，請換成您的圖片路徑 */
  background-size: cover; /* 讓圖片填滿容器 */
  background-position: center; /* 圖片置中 */
  z-index: 0;
  animation: kenBurns 20s ease-in-out infinite alternate;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 改用漸層遮罩，更有層次感 */
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.6) 100%);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  color: #ffffff;
  text-align: center;
  padding: 20px;
}

.hero-title {
  font-size: 5rem; /* 稍微加大 */
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 15px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 1.5rem; /* 24px */
  font-weight: 300;
  margin-bottom: 2rem;
  text-shadow: 1px 1px 10px rgba(0, 0, 0, 0.5);
  letter-spacing: 1px;
  opacity: 0.9;
}

.cta-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 500;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid rgba(255, 255, 255, 0.5);
  min-width: 160px;
}

.cta-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}
.cta-btn.primary {
  background-color: #4CAF50; /* 使用一個醒目的綠色 */
  color: white; /* 白色文字 */
  font-size: 1.25rem; /* 稍大字體 */
  padding: 16px 40px; /* 更大的點擊區域 */
  border: none; /* 不需要邊框 */
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.4); /* 綠色陰影 */
}

.cta-btn.primary:hover {
  background-color: #45a049; /* 滑鼠懸停時加深顏色 */
  transform: translateY(-5px); /* 稍微放大 */
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.6);
}

.arrow-icon {
  margin-left: 10px;
  transition: transform 0.3s ease;
}

.cta-btn.primary:hover .arrow-icon {
  transform: translateX(5px);
}

/* --- 動畫定義 --- */

/* 背景緩慢縮放 (Ken Burns Effect) */
@keyframes kenBurns {
  0% { transform: scale(1); }
  100% { transform: scale(1.15); }
}

/* 文字向上浮現 */
.animate-up {
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 1s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.delay-1 { animation-delay: 0.2s; }
.delay-2 { animation-delay: 0.4s; }
.delay-3 { animation-delay: 0.6s; }

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* --- [新增] 快速搜尋卡樣式 --- */
.quick-search-card {
  margin-top: 40px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 20px 30px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  width: 100%;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 50px;
  padding: 5px 5px 5px 20px;
  margin-bottom: 12px;
}

.search-icon {
  color: #909399;
  margin-right: 10px;
}

.search-input-wrapper input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 1rem;
  color: #333;
}

.search-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 50px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #45a049;
}

.hot-tags {
  display: flex;
  gap: 10px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.9);
  justify-content: center;
  flex-wrap: wrap;
}

.tag {
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.2s;
}

.tag:hover {
  color: #fff;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
}

/* --- [新增] 內容區塊共用樣式 --- */
.content-section {
  background-color: #f9f9f9;
  padding: 60px 20px;
  display: flex;
  flex-direction: column;
  gap: 80px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
}

.section-header p {
  color: #666;
  font-size: 1.1rem;
}

/* --- 流程區塊 --- */
.steps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.step-item {
  padding: 20px;
}

.step-icon {
  width: 80px;
  height: 80px;
  background: #e8f5e9;
  color: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 20px;
  transition: transform 0.3s;
}

.step-item:hover .step-icon {
  transform: scale(1.1) rotate(5deg);
}

.step-item h3 {
  font-size: 1.5rem;
  margin-bottom: 10px;
  color: #333;
}

.step-item p {
  color: #666;
  line-height: 1.6;
}

/* --- 熱門點位區塊 --- */
.spots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.spot-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.spot-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.card-image {
  height: 200px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #4CAF50;
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.full {
  background: #f56c6c;
}

.card-body {
  padding: 20px;
}

.card-body h3 {
  margin: 0 0 10px;
  font-size: 1.2rem;
  color: #333;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 0.95rem;
}

.card-link {
  color: #4CAF50;
  text-decoration: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: gap 0.2s;
}

.card-link:hover {
  gap: 8px;
}
</style>
