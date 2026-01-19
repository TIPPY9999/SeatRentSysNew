<template>
  <div class="mall-page">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm sticky-top">
      <div class="container">
        <router-link class="navbar-brand fw-bold" to="/">
          <i class="bi bi-shop me-2"></i> 點數商城
        </router-link>
        <div class="ms-auto d-flex align-items-center">
          <div class="points-display px-3 py-1 rounded-pill">
            <span class="text-white-50 small me-2">可用點數</span>
            <span class="text-warning fw-bold h5 mb-0">{{ currentPoints }}</span>
            <span class="text-warning small ms-1">Pts</span>
          </div>
          <router-link to="/snake" class="btn btn-warning btn-sm ms-3 fw-bold">
    <i class="bi bi-controller me-1"></i> 玩遊戲賺點數
  </router-link>
          <button @click="goBack" class="btn btn-outline-light btn-sm ms-3">返回</button>
        </div>
      </div>
    </nav>

    <div class="container py-5">
      <div class="text-center mb-5">
        <h2 class="fw-bold display-6">🎫 專屬優惠兌換</h2>
        <p class="lead text-muted">在店內結帳時出示此頁面，輸入店家核銷碼即可享折扣</p>
        <div class="divider mx-auto"></div>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-grow text-warning" role="status"></div>
        <p class="mt-3 text-muted">正在載入最新優惠...</p>
      </div>

      <div v-else class="row g-4">
        <div v-for="coupon in coupons" :key="coupon.couponId" class="col-md-6 col-lg-4">
          <div class="card h-100 border-0 shadow-lg coupon-card overflow-hidden">
            <div class="position-relative">
              <img 
                :src="coupon.couponImg ? `http://localhost:8080/images/${coupon.couponImg}` : 'https://placehold.co/600x400?text=Discount'" 
                class="card-img-top"
                alt="coupon image"
              />
              <div class="merchant-badge">{{ coupon.merchantName }}</div>
            </div>

            <div class="card-body p-4">
              <h5 class="fw-bold card-title mb-2 text-dark">{{ coupon.couponName }}</h5>
              <br>
              <hr>
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
                  class="btn redeem-btn fw-bold px-4"
                  :class="canAfford(coupon.pointsRequired) ? 'btn-warning shadow' : 'btn-light disabled text-muted'"
                >
                  {{ canAfford(coupon.pointsRequired) ? '現場核銷' : '點數不足' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="coupons.length === 0" class="col-12 text-center py-5">
          <i class="bi bi-ticket-perforated display-1 text-light"></i>
          <p class="text-muted mt-3">目前暫無可兌換的優惠券</p>
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
// 注意：這裡改為動態獲取，不要在頂層寫死
const getMemberId = () => localStorage.getItem('memberId')

// 1. 初始化資料 (移除強制登入檢查)
const fetchData = async () => {
  try {
    // 所有人都能看優惠券
    const couponRes = await axios.get('http://localhost:8080/api/discounts')
    coupons.value = (couponRes.data.data || []).filter(c => c.couponStatus === 1)
    
    // 只有已登入的人才去抓點數
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

// 2. 判斷點數 (若未登入則預設顯示足夠，讓按鈕維持可點擊狀態)
const canAfford = (points) => {
  const mid = getMemberId()
  if (!mid) return true // 未登入時讓按鈕是黃色的，引導點擊
  return currentPoints.value >= points
}

// 3. 執行核銷 (在此處才檢查登入)
const handleRedeem = (coupon) => {
  const mid = getMemberId()

  // --- 重點：未登入攔截 ---
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
      if (result.isConfirmed) {
        router.push('/login') // 跳轉到你的登入頁
      }
    })
    return
  }

  // --- 已登入：檢查餘額 ---
  if (currentPoints.value < coupon.pointsRequired) {
    Swal.fire('點數不足', `您目前有 ${currentPoints.value} 點，還差 ${coupon.pointsRequired - currentPoints.value} 點`, 'warning')
    return
  }

  // --- 已登入且足夠：開啟核銷彈窗 ---
  Swal.fire({
    title: '確認現場核銷',
    html: `兌換項目：<b>${coupon.couponName}</b><br>消耗點數：<b>${coupon.pointsRequired} Pts</b>`,
    input: 'text',
    inputPlaceholder: '請輸入店家核銷碼',
    showCancelButton: true,
    confirmButtonText: '確定核銷',
    preConfirm: (passcode) => {
      if (!passcode) return Swal.showValidationMessage('請輸入核銷碼')
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
          fetchData(); // 刷新餘額
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

.points-display {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.divider {
  width: 60px;
  height: 4px;
  background: #ffc107;
  border-radius: 2px;
  margin-top: 15px;
}

.coupon-card {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 20px;
}

.coupon-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.15) !important;
}

.card-img-top {
  height: 200px;
  object-fit: cover;
}

.merchant-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 12px;
  border-radius: 50px;
  font-size: 0.8rem;
  backdrop-filter: blur(4px);
}

.text-orange { color: #f39c12; }

.description-text {
  height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.redeem-btn {
  border-radius: 12px;
  transition: all 0.2s;
}

.x-small { font-size: 0.75rem; }

/* SweetAlert 調整 */
:deep(.swal2-input) {
  height: 60px !important;
  border: 2px solid #ffc107 !important;
}
</style>