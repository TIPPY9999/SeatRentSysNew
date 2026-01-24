<template>
  <div class="mall-page">
    <!-- ========== 頂部導航 ========== -->
    <nav class="mall-navbar">
      <div class="navbar-container">
        <router-link class="navbar-brand" to="/mall" @click="scrollToTop">
          <div class="brand-icon">
            <i class="fas fa-store"></i>
          </div>
          <span class="brand-text">點數商城</span>
        </router-link>
        <div class="navbar-right">
          <div class="points-display">
            <div class="points-icon">
              <i class="fas fa-coins"></i>
            </div>
            <div class="points-info">
              <span class="points-label">可用點數</span>
              <span class="points-value">{{ currentPoints }}</span>
            </div>
          </div>
          <router-link to="/snake" class="game-btn">
            <i class="fas fa-gamepad mr-1"></i> 玩遊戲賺點數
          </router-link>
          <router-link to="/" class="home-btn">
            <i class="fas fa-home mr-1"></i> 返回首頁
          </router-link>
        </div>
      </div>
    </nav>

    <!-- ========== 主要內容 ========== -->
    <div class="mall-content">
      <!-- 標題區域 -->
      <div class="mall-header">
        <div class="header-content">
          <div class="header-icon">
            <i class="fas fa-ticket-alt"></i>
          </div>
          <div class="header-text">
            <h1>專屬優惠兌換</h1>
            <p>在店內結帳時出示此頁面，輸入店家核銷碼即可享折扣</p>
          </div>
        </div>
        
        <!-- 搜尋區域 -->
        <div class="search-section">
          <div class="search-box">
            <i class="fas fa-search search-icon"></i>
            <input 
              v-model="searchKeyword" 
              @input="handleSearch"
              type="text" 
              class="search-input" 
              placeholder="搜尋優惠券名稱、描述或商家名稱..."
            />
            <button v-if="searchKeyword" @click="clearSearch" class="clear-btn">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 載入中 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
        <p>正在載入最新優惠...</p>
      </div>

      <!-- 優惠券列表 -->
      <div v-else class="coupon-section">
        <transition-group name="coupon-list" tag="div" class="coupon-grid">
          <div v-for="coupon in pagedCoupons" :key="coupon.couponId" class="coupon-card-wrapper">
            <div class="coupon-card">
              <!-- 圖片區 -->
              <div class="card-image">
                <img 
                  :src="coupon.couponImg ? `http://localhost:8080/images/${coupon.couponImg}` : 'https://placehold.co/600x400?text=Discount'" 
                  :alt="coupon.couponName"
                />
                <div class="merchant-badge">
                  <i class="fas fa-store mr-1"></i>
                  {{ coupon.merchantName || (coupon.merchant ? coupon.merchant.merchantName : '合作商家') }}
                </div>
              </div>

              <!-- 內容區 -->
              <div class="card-content">
                <h3 class="coupon-title">{{ coupon.couponName }}</h3>
                <p class="coupon-description">{{ coupon.couponDescription }}</p>
                
                <div class="card-footer">
                  <div class="coupon-info">
                    <div class="points-required">
                      <span class="points-number">{{ coupon.pointsRequired }}</span>
                      <span class="points-unit">Pts</span>
                    </div>
                    <div class="expire-date">
                      <i class="fas fa-calendar-alt mr-1"></i>
                      有效期至 {{ coupon.endDate }}
                    </div>
                  </div>
                  <button 
                    @click="handleRedeem(coupon)"
                    class="redeem-btn"
                    :class="{ 'disabled': !canAfford(coupon.pointsRequired) }"
                    :disabled="!canAfford(coupon.pointsRequired)"
                  >
                    <i :class="canAfford(coupon.pointsRequired) ? 'fas fa-shopping-cart' : 'fas fa-lock'" class="mr-1"></i>
                    {{ canAfford(coupon.pointsRequired) ? '現場核銷' : '點數不足' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </transition-group>

        <!-- 無資料 -->
        <div v-if="filteredCoupons.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-ticket-alt"></i>
          </div>
          <h3>找不到符合的優惠券</h3>
          <p>試試其他關鍵字或瀏覽全部優惠</p>
        </div>

        <!-- 分頁 -->
        <div v-if="totalPages > 1" class="pagination-wrapper">
          <div class="pagination">
            <button 
              class="page-btn" 
              :disabled="currentPage === 1"
              @click="changePage(currentPage - 1)"
            >
              <i class="fas fa-chevron-left"></i>
            </button>
            <button 
              v-for="page in totalPages" 
              :key="page" 
              class="page-btn"
              :class="{ 'active': currentPage === page }"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
            <button 
              class="page-btn" 
              :disabled="currentPage === totalPages"
              @click="changePage(currentPage + 1)"
            >
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const currentPoints = ref(0)
const loading = ref(true)
const searchKeyword = ref('')

// 分頁與資料邏輯
const allCoupons = ref([])       // 原始所有資料
const currentPage = ref(1)       // 當前頁碼
const itemsPerPage = 12          // 每頁顯示 12 筆

// 計算過濾後的資料
const filteredCoupons = computed(() => {
  return allCoupons.value.filter(c => c.couponStatus === 1)
})

// 計算當前分頁應顯示的資料
const pagedCoupons = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredCoupons.value.slice(start, end)
})

// 計算總頁數
const totalPages = computed(() => {
  return Math.ceil(filteredCoupons.value.length / itemsPerPage)
})

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const getMemberId = () => localStorage.getItem('memberId')

const fetchData = async () => {
  loading.value = true
  try {
    const couponRes = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: searchKeyword.value }
    })
    allCoupons.value = couponRes.data.data || []
    
    // 搜尋後重置到第一頁
    currentPage.value = 1

    const mid = getMemberId()
    if (mid) {
      const memberRes = await axios.get(`http://localhost:8080/api/members/${mid}`)
      currentPoints.value = memberRes.data.data.memPoints || 0
    }
  } catch (error) {
    console.error('資料載入錯誤', error)
  } finally {
    loading.value = false
  }
}

// 防抖搜尋
let searchTimer = null
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchData()
  }, 500)
}

const clearSearch = () => {
  searchKeyword.value = ''
  fetchData()
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  scrollToTop()
}

const canAfford = (points) => {
  const mid = getMemberId()
  if (!mid) return true 
  return currentPoints.value >= points
}

const handleRedeem = (coupon) => {
  const mid = getMemberId();
  if (!mid) {
    Swal.fire({
      title: '請先登入',
      text: '登入會員後即可兌換！',
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: '立即登入',
      confirmButtonColor: '#ffc107'
    }).then((res) => { if (res.isConfirmed) router.push('/login') })
    return
  }
  // ... 核銷邏輯 (與之前相同)
}

onMounted(fetchData)
</script>

<style scoped>
/* ========== 頁面容器 ========== */
.mall-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* ========== 導航欄 ========== */
.mall-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #2c3e50 0%, #3d5a73 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 12px 0;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  transition: transform 0.3s ease;
}

.navbar-brand:hover {
  transform: scale(1.05);
}

.brand-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.brand-text {
  color: white;
  font-size: 1.3rem;
  font-weight: 700;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.points-display {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 50px;
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.points-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.points-info {
  display: flex;
  flex-direction: column;
}

.points-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.points-value {
  font-size: 18px;
  font-weight: 700;
  color: #e6a23c;
}

.game-btn, .home-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.game-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(230, 162, 60, 0.3);
}

.game-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(230, 162, 60, 0.4);
  color: white;
}

.home-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.home-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* ========== 主要內容 ========== */
.mall-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
}

/* ========== 標題區域 ========== */
.mall-header {
  text-align: center;
  margin-bottom: 40px;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  box-shadow: 0 8px 30px rgba(230, 162, 60, 0.3);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.header-text h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #303133;
}

.header-text p {
  margin: 8px 0 0;
  font-size: 1rem;
  color: #909399;
}

/* ========== 搜尋區域 ========== */
.search-section {
  max-width: 600px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 50px;
  padding: 12px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.search-box:focus-within {
  box-shadow: 0 6px 30px rgba(230, 162, 60, 0.2);
}

.search-icon {
  color: #e6a23c;
  font-size: 18px;
  margin-right: 12px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #303133;
  background: transparent;
}

.search-input::placeholder {
  color: #c0c4cc;
}

.clear-btn {
  background: none;
  border: none;
  color: #c0c4cc;
  cursor: pointer;
  padding: 4px;
  transition: color 0.3s ease;
}

.clear-btn:hover {
  color: #909399;
}

/* ========== 載入狀態 ========== */
.loading-state {
  text-align: center;
  padding: 80px 0;
}

.loading-spinner {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.loading-state p {
  color: #909399;
  font-size: 16px;
}

/* ========== 優惠券列表 ========== */
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 32px;
}

.coupon-card-wrapper {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.coupon-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.coupon-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.coupon-card:hover .card-image img {
  transform: scale(1.08);
}

.merchant-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  color: white;
  padding: 8px 16px;
  border-radius: 50px;
  font-size: 13px;
  font-weight: 500;
}

.card-content {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.coupon-title {
  margin: 0 0 12px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #303133;
}

.coupon-description {
  margin: 0 0 20px;
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.points-required {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.points-number {
  font-size: 28px;
  font-weight: 700;
  color: #e6a23c;
}

.points-unit {
  font-size: 14px;
  color: #e6a23c;
  font-weight: 500;
}

.expire-date {
  font-size: 12px;
  color: #909399;
}

.redeem-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(230, 162, 60, 0.3);
}

.redeem-btn:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(230, 162, 60, 0.4);
}

.redeem-btn.disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  box-shadow: none;
  cursor: not-allowed;
}

/* ========== 空狀態 ========== */
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 40px;
  color: #c0c4cc;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #606266;
  font-size: 1.2rem;
}

.empty-state p {
  margin: 0;
  color: #909399;
}

/* ========== 分頁 ========== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}

.pagination {
  display: flex;
  gap: 8px;
}

.page-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 12px;
  background: white;
  color: #606266;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-btn:hover:not(:disabled):not(.active) {
  background: #f5f7fa;
  transform: translateY(-2px);
}

.page-btn.active {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(230, 162, 60, 0.3);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ========== 列表動畫 ========== */
.coupon-list-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.coupon-list-leave-active {
  transition: all 0.3s ease;
}

.coupon-list-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.coupon-list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* ========== 間距工具類 ========== */
.mr-1 { margin-right: 4px; }

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .navbar-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .navbar-right {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .coupon-grid {
    grid-template-columns: 1fr;
  }
  
  .header-text h1 {
    font-size: 1.5rem;
  }
}
</style>