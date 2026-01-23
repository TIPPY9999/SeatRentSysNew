<template>
  <div class="mall-page">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm sticky-top">
      <div class="container">
        <router-link class="navbar-brand fw-bold" border-bottom to="/mall" @click="scrollToTop">
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
          <button @click="goBack" class="btn btn-outline-light btn-sm ms-3">返回首頁</button>
        </div>
      </div>
    </nav>

    <div class="container py-5">
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
              placeholder="搜尋優惠券名稱、描述或商家..."
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

      <div v-else class="row g-5">
        <div v-for="coupon in coupons" :key="coupon.couponId" class="col-md-6 col-lg-4">
          <div class="card h-100 border-0 shadow-lg coupon-card overflow-hidden">
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
              <div>
                <p class="text-secondary small mb-4 description-text">
                  {{ coupon.couponDescription }}
                </p>
              </div>             
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
        
        <div v-if="coupons.length === 0" class="col-12 text-center py-5 mt-4">
          <i class="bi bi-ticket-perforated display-1 text-light"></i>
          <p class="text-muted h5 mt-3">找不到符合「{{ searchKeyword }}」的優惠券</p>
          <button v-if="searchKeyword" @click="clearSearch" class="btn btn-link text-warning text-decoration-none">清除搜尋條件</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const coupons = ref([])
const currentPoints = ref(0)
const loading = ref(true)
const searchKeyword = ref('') // 搜尋關鍵字
let searchTimer = null // 用於搜尋防抖

// 輔助函式
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const goBack = () => {
  router.push('/')
}

const getMemberId = () => localStorage.getItem('memberId')

// 1. 初始化與獲取資料 (整合模糊查詢)
const fetchData = async () => {
  loading.value = true
  try {
    // 呼叫 API 並帶入 keyword 參數
    const couponRes = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: searchKeyword.value }
    })
    
    // 前端二次過濾：僅顯示狀態為 1 (上架中) 的優惠券
    coupons.value = (couponRes.data.data || []).filter(c => c.couponStatus === 1)
    
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

// 2. 搜尋處理 (防抖邏輯：避免頻繁發送 API)
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchData()
  }, 500) // 使用者停止輸入 0.5 秒後才發送請求
}

const clearSearch = () => {
  searchKeyword.value = ''
  fetchData()
}

// 3. 判斷點數
const canAfford = (points) => {
  const mid = getMemberId()
  if (!mid) return true 
  return currentPoints.value >= points
}

// 4. 執行核銷
const handleRedeem = (coupon) => {
  const mid = getMemberId();

  if (!mid) {
    Swal.fire({
      title: '請先登入',
      text: '登入會員後即可使用點數兌換優惠券！',
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: '立即登入',
      cancelButtonText: '先看看',
      confirmButtonColor: '#ffc107'
    }).then((result) => {
      if (result.isConfirmed) router.push('/login')
    })
    return
  }

  if (currentPoints.value < coupon.pointsRequired) {
    Swal.fire('點數不足', `您目前有 ${currentPoints.value} 點，還差 ${coupon.pointsRequired - currentPoints.value} 點`, 'warning')
    return
  }

  Swal.fire({
    title: '確認現場核銷',
    html: `兌換項目：<b>${coupon.couponName}</b><br>消耗點數：<b>${coupon.pointsRequired} Pts</b>`,
    input: 'password',
    inputPlaceholder: '請輸入店家核銷碼',
    showCancelButton: true,
    confirmButtonText: '確定核銷',
    preConfirm: (passcode) => {
      if (!passcode) return Swal.showValidationMessage('請輸入正確核銷碼')
      return passcode
    }
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const res = await axios.post('http://localhost:8080/api/discounts/redeem', {
          memberId: mid,
          couponId: coupon.couponId,
          passcode: result.value
        });

        if (res.data.code === 200) {
          Swal.fire('核銷成功！', `剩餘點數：${res.data.data.currentPoints}`, 'success')
          fetchData(); 
        } else {
          Swal.fire('錯誤', res.data.message, 'error')
        }
      } catch (error) {
        Swal.fire('系統錯誤', '核銷處理失敗', 'error')
      }
    }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.mall-page {
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* 搜尋框美化 */
.search-container {
  max-width: 550px;
}
.search-container .input-group {
  border-radius: 50px;
  background: white;
  padding: 5px 15px;
  border: 1px solid #dee2e6;
  transition: all 0.3s;
}
.search-container .input-group:focus-within {
  border-color: #ffc107;
  box-shadow: 0 8px 20px rgba(0,0,0,0.05);
}

.points-display {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.divider {
  width: 60px;
  height: 4px;
  background: #ffc107;
  border-radius: 2px;
  margin-top: 20px;
}

/* 卡片效果增強 */
.coupon-card {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 20px;
  background: white;
}

.coupon-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.12) !important;
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
  font-weight: 500;
}

.text-orange { color: #f39c12; }

.description-text {
  min-height: 45px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.redeem-btn {
  border-radius: 12px;
  padding: 10px 20px;
  transition: all 0.2s;
}

.redeem-btn:not(.disabled):hover {
  background-color: #e5ac00;
  transform: scale(1.05);
}

.x-small { font-size: 0.75rem; }

:deep(.swal2-input) {
  height: 55px !important;
  border: 2px solid #ffc107 !important;
}
</style>