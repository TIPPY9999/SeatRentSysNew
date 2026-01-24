<template>
  <div class="mall-page p-4">
    <div class="container-fluid">
      <div class="header-section d-flex flex-wrap justify-content-between align-items-center mb-4 bg-white p-3 rounded shadow-sm border-start border-warning border-4">
        <div class="d-flex align-items-center">
          <h4 class="mb-0 fw-bold text-dark me-3">
            <i class="bi bi-shop me-2 text-warning"></i>點數商城
          </h4>
          <nav aria-label="breadcrumb" class="d-none d-md-block">
            <ol class="breadcrumb mb-0">
              <li class="breadcrumb-item"><router-link to="/">首頁</router-link></li>
              <li class="breadcrumb-item active">優惠兌換</li>
            </ol>
          </nav>
        </div>

        <div class="d-flex align-items-center gap-3 mt-3 mt-md-0">
          <div class="points-display px-4 py-2 bg-dark rounded-pill shadow-sm">
            <span class="text-white-50 small me-2">可用點數</span>
            <span class="text-warning fw-bold h5 mb-0">{{ currentPoints }}</span>
            <span class="text-warning small ms-1">Pts</span>
          </div>
          <router-link to="/snake" class="btn btn-warning fw-bold px-4 rounded-pill shadow-sm hover-scale">
            <i class="bi bi-controller me-1"></i> 玩遊戲賺點數
          </router-link>
        </div>
      </div>

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
            </button>
          </div>
        </div>

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
                  </div>
                  <button 
                    @click="handleRedeem(coupon)"
                    class="btn redeem-btn fw-bold px-3 shadow-sm"
                    :class="canAfford(coupon.pointsRequired) ? 'btn-warning' : 'btn-light disabled text-muted'"
                  >
                    {{ canAfford(coupon.pointsRequired) ? '現場核銷' : '點數不足' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

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

// 響應式抓取會員與點數
const memberId = computed(() => memberStore.member?.memId)
const currentPoints = computed(() => memberStore.member?.memPoints || 0)

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
    }
  } catch (error) {
    console.error('Load Data Failed:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜尋與分頁輔助
 */
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

// 監聽網址參數變化 (從別的商家跳轉回來時)
watch(() => route.query.merchantId, () => {
  currentPage.value = 1
  fetchData()
})

onMounted(fetchData)
</script>

<style scoped>
.mall-page {
  background-color: #f8f9fa;
  min-height: 100vh;
}

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
}

.merchant-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 15px;
  border-radius: 50px;
  font-size: 0.8rem;
  backdrop-filter: blur(4px);
  z-index: 2;
}

.description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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