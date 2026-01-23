<template>
  <div class="mall-page">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm sticky-top">
      <div class="container-fluid px-md-5">
        <router-link class="navbar-brand fw-bold" to="/mall" @click="scrollToTop">
          <i class="bi bi-shop me-2"></i> 點數商城
        </router-link>
        <div class="ms-auto d-flex align-items-center">
          <div class="points-display px-3 py-1 rounded-pill">
            <span class="text-white-50 small me-2">可用點數</span>
            <span class="text-warning fw-bold h5 mb-0">{{ currentPoints }}</span>
            <span class="text-warning small ms-1">Pts</span>
          </div>
          <router-link to="/snake" class="btn btn-warning btn-sm ms-3 fw-bold shadow-sm">
            <i class="bi bi-controller me-1"></i> 玩遊戲賺點數
          </router-link>
          <router-link to="/" class="btn btn-outline-light btn-sm ms-3">返回首頁</router-link>
        </div>
      </div>
    </nav>

    <div class="container py-5 px-md-5">
      <div class="text-center mb-5">
        <h2 class="fw-bold display-6">🎫 專屬優惠兌換</h2>
        <p class="lead text-muted">在店內結帳時出示此頁面，輸入店家核銷碼即可享折扣</p>
        
        <div class="search-container mx-auto mt-4 mb-2">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0">
              <i class="bi bi-search text-warning"></i>
            </span>
            <input 
              v-model="searchKeyword" 
              @input="handleSearch"
              type="text" 
              class="form-control border-start-0 ps-0 shadow-none" 
              placeholder="搜尋優惠券名稱、描述或商家名稱..."
            />
            <button v-if="searchKeyword" @click="clearSearch" class="btn btn-white border-start-0 text-muted" type="button">
              <i class="bi bi-x-circle"></i>
            </button>
          </div>
        </div>
        <div class="divider mx-auto"></div>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-grow text-warning" role="status"></div>
        <p class="mt-3 text-muted">正在載入最新優惠...</p>
      </div>

      <div v-else>
        <div class="row g-5">
          <div v-for="coupon in pagedCoupons" :key="coupon.couponId" class="col-md-6 col-lg-4">
            <div class="card h-100 border-0 shadow-sm coupon-card overflow-hidden">
              <div class="position-relative">
                <img 
                  :src="coupon.couponImg ? `http://localhost:8080/images/${coupon.couponImg}` : 'https://placehold.co/600x400?text=Discount'" 
                  class="card-img-top"
                  alt="coupon image"
                />
                <div class="merchant-badge">{{ coupon.merchantName || (coupon.merchant ? coupon.merchant.merchantName : '合作商家') }}</div>
              </div>

              <div class="card-body p-4">
                <h5 class="fw-bold card-title mb-2 text-dark">{{ coupon.couponName }}</h5>
                <hr class="my-3 opacity-10">
                <p class="text-secondary small mb-4 description-text">
                  {{ coupon.couponDescription }}
                </p>
                <div class="d-flex justify-content-between align-items-end">
                  <div>
                    <div class="text-orange h3 fw-bold mb-0">{{ coupon.pointsRequired }} <small>Pts</small></div>
                    <div class="text-muted x-small mt-1">
                      <i class="bi bi-calendar-check me-1"></i>有效期至 {{ coupon.endDate }}
                    </div>
                  </div>
                  <button 
                    @click="handleRedeem(coupon)"
                    class="btn redeem-btn fw-bold px-4 shadow-sm"
                    :class="canAfford(coupon.pointsRequired) ? 'btn-warning' : 'btn-light disabled text-muted'"
                  >
                    {{ canAfford(coupon.pointsRequired) ? '現場核銷' : '點數不足' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredCoupons.length === 0" class="text-center py-5">
          <i class="bi bi-ticket-perforated display-1 text-light"></i>
          <p class="text-muted h5 mt-3">找不到符合的優惠券</p>
        </div>

        <nav v-if="totalPages > 1" class="mt-5 pt-5 d-flex justify-content-center">
          <ul class="pagination">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">上一頁</a>
            </li>
            <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: currentPage === page }">
              <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">下一頁</a>
            </li>
          </ul>
        </nav>
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
.mall-page {
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* 核心：徹底拉開間距 */
.container {
  max-width: 1440px;
}

.row.g-5 {
  --bs-gutter-x: 4.5rem !important; /* 加寬左右間距 */
  --bs-gutter-y: 5.5rem !important; /* 加寬上下間距 */
}

/* 搜尋框樣式 */
.search-container {
  max-width: 550px;
}
.search-container .input-group {
  border-radius: 50px;
  background: white;
  padding: 5px 15px;
  border: 1px solid #dee2e6;
}

.divider {
  width: 60px;
  height: 4px;
  background: #ffc107;
  border-radius: 2px;
  margin-top: 25px;
  margin-bottom: 30px;
}

/* 卡片與懸停效果 */
.coupon-card {
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  border-radius: 20px;
  border: 1px solid rgba(0,0,0,0.05) !important;
}

.coupon-card:hover {
  transform: translateY(-15px) scale(1.02);
  box-shadow: 0 30px 60px rgba(0,0,0,0.12) !important;
}

.card-img-top {
  height: 220px;
  object-fit: cover;
}

.merchant-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.65);
  color: white;
  padding: 5px 15px;
  border-radius: 50px;
  font-size: 0.85rem;
  backdrop-filter: blur(8px);
}

.description-text {
  min-height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 分頁按鈕美化 */
.pagination .page-link {
  border: none;
  margin: 0 5px;
  border-radius: 8px !important;
  color: #555;
  padding: 10px 20px;
}

.pagination .page-item.active .page-link {
  background-color: #ffc107;
  color: #000;
  font-weight: bold;
}

.points-display { background: rgba(255, 255, 255, 0.1); border: 1px solid rgba(255, 193, 7, 0.3); }
.text-orange { color: #f39c12; }
.redeem-btn { border-radius: 12px; }
.x-small { font-size: 0.75rem; }
</style>