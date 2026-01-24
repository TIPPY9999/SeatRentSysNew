<template>
<<<<<<< HEAD
  <div class="mall-page p-4">
    <div class="container-fluid">
      <div class="header-section d-flex flex-wrap justify-content-between align-items-center mb-4 bg-white p-3 rounded shadow-sm border-start border-warning border-4">
        <div class="d-flex align-items-center">
          <h4 class="mb-0 fw-bold text-dark me-3">
            <i class="bi bi-shop me-2 text-warning"></i>點數商城
          </h4>
        </div>

        <div class="d-flex align-items-center gap-3 mt-3 mt-md-0">
          <div class="points-display px-4 py-2 bg-dark rounded-pill shadow-sm">
            <span class="text-white-50 small me-2">可用點數</span>
            <span class="text-warning fw-bold h5 mb-0">{{ currentPoints }}</span>
            <span class="text-warning small ms-1">Pts</span>
          </div>
          <router-link to="/snake" class="btn btn-warning fw-bold px-4 rounded-pill shadow-sm hover-scale">
            <i class="bi bi-controller me-1"></i> 玩遊戲賺點數
=======
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
          </router-link>
        </div>
      </div>

<<<<<<< HEAD
      <div class="text-center mb-5 py-3">
        <h2 class="fw-bold display-6">🎫 專屬優惠兌換</h2>
        <p class="lead text-muted">消費回饋點數，現場即享折扣</p>
        
        <div class="search-container mx-auto mt-4">
          <div class="input-group shadow-sm rounded-pill overflow-hidden border">
            <span class="input-group-text bg-white border-0">
              <i class="bi bi-search text-warning"></i>
            </span>
            <input 
              v-model="searchKeyword" 
              @input="handleSearch"
              type="text" 
              class="form-control border-0 ps-0 shadow-none" 
              placeholder="搜尋優惠券、商家名稱或描述..."
            />
            <button v-if="searchKeyword" @click="clearSearch" class="btn btn-white border-0 text-muted" type="button">
              <i class="bi bi-x-circle"></i>
=======
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
            </button>
          </div>
        </div>
      </div>

<<<<<<< HEAD
        <div v-if="route.query.merchantId" class="mt-3">
          <span class="badge bg-warning-subtle text-dark border border-warning px-3 py-2 rounded-pill">
            正在查看特定商家的優惠
            <button @click="resetAndFetch" class="btn-close ms-2" style="font-size: 0.6rem;"></button>
          </span>
        </div>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-grow text-warning" role="status"></div>
        <p class="mt-3 text-muted fw-bold">正在載入最新優惠...</p>
      </div>

      <div v-else>
        <div class="row g-4">
          <div v-for="coupon in pagedCoupons" :key="coupon.couponId" class="col-sm-12 col-md-6 col-lg-4 col-xl-3">
            <div class="card h-100 border-0 shadow-sm coupon-card">
              <div class="image-wrapper position-relative">
                <img 
                  :src="coupon.couponImg ? `http://localhost:8080/images/${coupon.couponImg}` : 'https://placehold.co/600x400?text=Discount'" 
                  class="card-img-top"
                  alt="coupon"
                />
                <div class="merchant-badge">{{ coupon.merchantName || '特約商家' }}</div>
              </div>

              <div class="card-body p-4 d-flex flex-column">
                <h5 class="fw-bold card-title mb-2 text-dark">{{ coupon.couponName }}</h5>
                <p class="text-secondary small description-text flex-grow-1">
                  {{ coupon.couponDescription }}
                </p>
                
                <hr class="my-3 opacity-10">
                
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-orange h4 fw-bold mb-0">{{ coupon.pointsRequired }} <small class="h6">Pts</small></div>
                    <div class="text-muted x-small">有效期至 {{ coupon.endDate }}</div>
=======
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
                  :src="
                    coupon.couponImg
                      ? `http://localhost:8080/images/${coupon.couponImg}`
                      : 'https://placehold.co/600x400?text=Discount'
                  "
                  :alt="coupon.couponName"
                />
                <div class="merchant-badge">
                  <i class="fas fa-store mr-1"></i>
                  {{
                    coupon.merchantName ||
                    (coupon.merchant ? coupon.merchant.merchantName : '合作商家')
                  }}
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
                  </div>
                  <button
                    @click="handleRedeem(coupon)"
<<<<<<< HEAD
                    class="btn redeem-btn fw-bold px-3 shadow-sm"
                    :class="canAfford(coupon.pointsRequired) ? 'btn-warning' : 'btn-light disabled text-muted'"
=======
                    class="redeem-btn"
                    :class="{ disabled: !canAfford(coupon.pointsRequired) }"
                    :disabled="!canAfford(coupon.pointsRequired)"
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
                  >
                    <i
                      :class="
                        canAfford(coupon.pointsRequired) ? 'fas fa-shopping-cart' : 'fas fa-lock'
                      "
                      class="mr-1"
                    ></i>
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

<<<<<<< HEAD
        <div v-if="filteredCoupons.length === 0" class="text-center py-5 mt-5">
          <i class="bi bi-ticket-perforated display-1 text-light"></i>
          <p class="text-muted h5 mt-3">目前找不到符合條件的優惠</p>
          <button @click="resetAndFetch" class="btn btn-outline-warning btn-sm mt-2 rounded-pill">顯示全部優惠</button>
        </div>

        <nav v-if="totalPages > 1" class="mt-5 d-flex justify-content-center">
          <ul class="pagination pagination-md shadow-sm">
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
=======
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
              :class="{ active: currentPage === page }"
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMemberAuthStore } from '@/stores/memberAuth' 
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberAuthStore()

// 狀態管理
const loading = ref(true)
const searchKeyword = ref('')
const allCoupons = ref([])
const currentPage = ref(1)
const itemsPerPage = 8 // 每頁顯示 8 張，版面更整齊

<<<<<<< HEAD
// 響應式抓取會員與點數
const memberId = computed(() => memberStore.member?.memId)
const currentPoints = computed(() => memberStore.member?.memPoints || 0)
=======
// 計算屬性：只顯示上架中 (1) 的優惠券
const filteredCoupons = computed(() => {
  return allCoupons.value.filter((c) => c.couponStatus === 1)
})
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25

// 篩選與分頁邏輯
const filteredCoupons = computed(() => allCoupons.value.filter(c => c.couponStatus === 1))
const pagedCoupons = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredCoupons.value.slice(start, start + itemsPerPage)
})
const totalPages = computed(() => Math.ceil(filteredCoupons.value.length / itemsPerPage))

// 判斷點數是否足夠
const canAfford = (required) => currentPoints.value >= required

/**
 * 核心功能：現場核銷 (Redeem) - 含商家核銷碼驗證
 */
const handleRedeem = async (coupon) => {
  // 1. 檢查登入狀態
  if (!memberStore.isLogin || !memberId.value) {
    const result = await Swal.fire({
      title: '請先登入',
      text: '您需要登入會員帳號才能進行點數兌換。',
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: '立即登入',
      confirmButtonColor: '#ffc107'
    })
    if (result.isConfirmed) router.push('/login')
    return
  }

  // 2. 點數充足檢查
  if (!canAfford(coupon.pointsRequired)) {
    Swal.fire('點數不足', `兌換此優惠需要 ${coupon.pointsRequired} 點。`, 'error')
    return
  }

  // 3. 第一步：確認兌換意願
  const confirmRedeem = await Swal.fire({
    title: '確認兌換？',
    html: `將扣除 <b class="text-danger">${coupon.pointsRequired}</b> 點數。<br><b>「${coupon.couponName}」</b>`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '前往核銷',
    cancelButtonText: '取消',
    confirmButtonColor: '#ffc107'
  })

  if (!confirmRedeem.isConfirmed) return

  // 4. 第二步：輸入商家核銷碼 (由店員操作)
  const { value: merchantCode } = await Swal.fire({
    title: '商家核銷',
    input: 'password', // 使用密碼形式隱藏輸入內容
    inputLabel: '請請店員輸入核銷碼',
    inputPlaceholder: '請在此輸入 4-6 位核銷碼',
    inputAttributes: {
      autocapitalize: 'off',
      autocorrect: 'off'
    },
    showCancelButton: true,
    confirmButtonText: '驗證並扣點',
    cancelButtonText: '返回',
    confirmButtonColor: '#28a745',
    inputValidator: (value) => {
      if (!value) {
        return '核銷碼不能為空！'
      }
    }
  })

  // 5. 第三步：發送請求到後端 (帶上核銷碼)
  if (merchantCode) {
    try {
      // 這裡將 merchantCode 一併送往後端驗證
      const response = await axios.post(`http://localhost:8080/api/discounts/redeem`, {
        memberId: memberId.value,
        couponId: coupon.couponId,
       passcode: merchantCode // 傳送商家輸入的核銷碼
      })

      // 6. 成功提示
      await Swal.fire({
        title: '兌換成功！',
        text: `核銷完成！${response.data.message || ''}`,
        icon: 'success',
        confirmButtonColor: '#28a745'
      })

      // 同步全站點數
      if (typeof memberStore.refreshPoints === 'function') {
        await memberStore.refreshPoints()
      }
      
      fetchData() // 刷新列表

    } catch (err) {
      console.error('Redeem Error:', err)
      // 如果核銷碼錯誤，後端應回傳 400 或 403
      Swal.fire(
        '核銷失敗', 
        err.response?.data?.message || '核銷碼錯誤或點數不足', 
        'error'
      )
    }
  }
}
/**
 * 抓取後端資料
 */
const fetchData = async () => {
  loading.value = true
  try {
<<<<<<< HEAD
    const targetId = route.query.merchantId
    const res = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: searchKeyword.value }
    })
    
    let data = res.data.data || []
    
    // 如果 URL 有商家 ID，進行過濾
    if (targetId) {
      data = data.filter(item => (item.merchantId || item.merchant?.merchantId) == targetId)
    }
    
    allCoupons.value = data

    // 若已登入，同步最新會員資訊 (包含點數)
    if (memberStore.isLogin && typeof memberStore.refreshPoints === 'function') {
      await memberStore.refreshPoints()
=======
    const targetMerchantId = route.query.merchantId

    // 一律請求「所有」優惠券的 API (避開會報 500 的路徑)
    const url = 'http://localhost:8080/api/discounts'
    const params = { keyword: searchKeyword.value }

    const couponRes = await axios.get(url, { params })
    let allData = couponRes.data.data || []

    // 如果 URL 帶有商家 ID，我們在前端手動篩選
    if (targetMerchantId) {
      console.log('正在進行前端過濾，目標商家:', targetMerchantId)
      // 注意：targetMerchantId 是字串，item.merchantId 可能是數字，所以用 ==
      allData = allData.filter((item) => {
        // 同時檢查 item.merchantId 或 item.merchant?.merchantId (看你的 JSON 結構)
        const mId = item.merchantId || item.merchant?.merchantId
        return mId == targetMerchantId
      })
    }

    allCoupons.value = allData
    currentPage.value = 1

    // 更新會員點數
    const mid = getMemberId()
    if (mid) {
      const memberRes = await axios.get(`http://localhost:8080/api/members/${mid}`)
      currentPoints.value = memberRes.data.data.memPoints || 0
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
    }
  } catch (error) {
    console.error('Load Data Failed:', error)
  } finally {
    loading.value = false
  }
}

<<<<<<< HEAD
/**
 * 搜尋與分頁輔助
 */
=======
// 監聽路由變化 (處理從商家 A 點到商家 B 的情況)
watch(
  () => route.query.merchantId,
  () => {
    currentPage.value = 1
    fetchData()
  },
)

// 搜尋處理 (防抖)
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
let searchTimer = null
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchData()
  }, 500)
}

const clearSearch = () => {
  searchKeyword.value = ''
  fetchData()
}

const resetAndFetch = () => {
  searchKeyword.value = ''
  router.push('/mall')
  fetchData()
}

const changePage = (p) => {
  currentPage.value = p
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

<<<<<<< HEAD
// 監聽網址參數變化 (從別的商家跳轉回來時)
watch(() => route.query.merchantId, () => {
  currentPage.value = 1
  fetchData()
})
=======
const canAfford = (points) => {
  const mid = getMemberId()
  if (!mid) return true
  return currentPoints.value >= points
}

const handleRedeem = (coupon) => {
  const mid = getMemberId()
  if (!mid) {
    Swal.fire({
      title: '請先登入',
      text: '登入會員後即可兌換！',
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: '立即登入',
      confirmButtonColor: '#ffc107',
    }).then((res) => {
      if (res.isConfirmed) router.push('/login')
    })
    return
  }

  Swal.fire({
    title: '確認兌換？',
    html: `將扣除 <b class="text-warning">${coupon.pointsRequired}</b> 點數<br>兌換 <b>${coupon.couponName}</b>`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '確定兌換',
    cancelButtonText: '取消',
    confirmButtonColor: '#ffc107',
  }).then(async (result) => {
    if (result.isConfirmed) {
      // 這裡串接後端兌換 API
      try {
        await axios.post(`http://localhost:8080/api/discounts/redeem`, {
          memberId: mid,
          couponId: coupon.couponId,
        })
        Swal.fire('兌換成功！', '請向店員出示核銷畫面', 'success')
        fetchData() // 重新載入點數與狀態
      } catch (err) {
        Swal.fire('兌換失敗', err.response?.data?.message || '點數不足或系統錯誤', 'error')
      }
    }
  })
}
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25

onMounted(fetchData)
</script>

<style scoped>
/* ========== 頁面容器 ========== */
.mall-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

<<<<<<< HEAD
.search-container {
  max-width: 550px;
}

.coupon-card {
  transition: all 0.3s cubic-bezier(.25,.8,.25,1);
  border-radius: 18px;
  overflow: hidden;
}

.coupon-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.12) !important;
}

.image-wrapper {
  height: 220px;
  overflow: hidden;
}

.card-img-top {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.coupon-card:hover .card-img-top {
  transform: scale(1.1);
=======
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

.game-btn,
.home-btn {
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
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
}

.merchant-badge {
  position: absolute;
<<<<<<< HEAD
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.7);
=======
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
  color: white;
  padding: 8px 16px;
  border-radius: 50px;
<<<<<<< HEAD
  font-size: 0.8rem;
  backdrop-filter: blur(4px);
  z-index: 2;
}

.description-text {
=======
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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
<<<<<<< HEAD
  height: 3rem;
  line-height: 1.5;
}

.hover-scale:hover {
  transform: scale(1.05);
  transition: transform 0.2s;
}

.text-orange { color: #f39c12; }
.redeem-btn { border-radius: 12px; }
.x-small { font-size: 0.75rem; }

.pagination .page-link {
  border: none;
  margin: 0 5px;
  border-radius: 10px;
  color: #555;
  padding: 10px 18px;
}

.pagination .active .page-link {
  background-color: #ffc107;
  color: #000;
  font-weight: bold;
}
</style>
=======
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
.mr-1 {
  margin-right: 4px;
}

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
>>>>>>> dd6883a1f54e8a4f2164b7cdf622a0f2bc90da25
