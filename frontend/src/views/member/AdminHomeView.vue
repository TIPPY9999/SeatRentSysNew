<script setup>
/**
 * AdminHomeView.vue：後台首頁入口 (Layout 適配修正版)
 * ------------------------------------------------
 * 1. [Fix] 更名 .content-wrapper -> .dashboard-inner 避免與 AdminLayout 衝突
 * 2. [Fix] 移除 min-height: 100vh，改由內容撐開，適配 AdminLTE 結構
 * 3. [Fix] 粒子特效改為 absolute，限制在內容區塊內
 * 4. [Layout] 模組區塊採用 Grid 分箱設計，確保每個類別獨立顯示
 * ------------------------------------------------
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'
import axios from 'axios'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const adminName = computed(() => adminAuthStore.admin?.name || '管理員')

// =======================
// 1. 統計數據狀態
// =======================
const stats = ref({
  totalSpots: 0,
  totalSeats: 0,
  maintenanceCount: 0,
  totalMembers: 0,
  activeRentals: 0,
})

const loading = ref(true)
const currentTime = ref('')
const timeInterval = ref(null)

// =======================
// 2. 圖表設定
// =======================
// (A) 營收趨勢圖
const revenueSeries = ref([])
const revenueOptions = ref({
  chart: {
    type: 'area',
    height: 350,
    toolbar: { show: false },
    fontFamily: 'Helvetica, Arial, sans-serif',
    animations: { enabled: true, easing: 'easeinout', speed: 800 },
    background: 'transparent',
  },
  dataLabels: { enabled: false },
  stroke: { curve: 'smooth', width: 3 },
  xaxis: {
    categories: [],
    axisBorder: { show: false },
    axisTicks: { show: false },
    labels: { style: { colors: '#64748b' } },
  },
  yaxis: { labels: { style: { colors: '#64748b' } } },
  grid: {
    borderColor: '#f1f1f1',
    strokeDashArray: 4,
    padding: { top: 0, right: 0, bottom: 0, left: 10 },
  },
  fill: {
    type: 'gradient',
    gradient: {
      shadeIntensity: 1,
      opacityFrom: 0.6,
      opacityTo: 0.05,
      stops: [0, 90, 100],
    },
  },
  tooltip: { x: { format: 'yyyy-MM-dd' }, theme: 'light' },
  colors: ['#4f46e5'],
})

// (B) 熱門站點排行
const spotSeries = ref([])
const spotOptions = ref({
  chart: {
    type: 'bar',
    height: 350,
    toolbar: { show: false },
    fontFamily: 'Helvetica, Arial, sans-serif',
    background: 'transparent',
  },
  plotOptions: {
    bar: {
      borderRadius: 4,
      horizontal: true,
      barHeight: '55%',
      distributed: true,
    },
  },
  dataLabels: { enabled: false },
  xaxis: {
    categories: [],
    labels: { style: { colors: '#64748b' } },
  },
  yaxis: { labels: { style: { colors: '#64748b', fontSize: '13px', fontWeight: 500 } } },
  grid: { show: false },
  colors: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
  legend: { show: false },
})

// =======================
// 3. 時間更新邏輯
// =======================
const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// =======================
// 4. 資料獲取與處理
// =======================
const fetchStats = async () => {
  loading.value = true
  try {
    const [spotsRes, seatsRes, membersRes, rentalsRes, ticketsRes] = await Promise.all([
      axios.get('/api/spot/list'),
      axios.get('/api/seats'),
      axios.get('/api/members'),
      axios.get('/api/rent-details/all'),
      axios.get('/api/maintenance/tickets/active'),
    ])

    // 基礎數據
    stats.value.totalSpots = spotsRes.data?.length || 0
    stats.value.totalSeats = seatsRes.data?.length || 0
    stats.value.maintenanceCount = (ticketsRes.data || []).length
    stats.value.totalMembers = membersRes.data?.length || 0
    stats.value.activeRentals = (rentalsRes.data || []).filter(
      (r) => r.recStatus === '租借中',
    ).length

    // 圖表數據處理
    const rawRentals = rentalsRes.data || []

    // (A) 每日營收
    const dailyMap = {}
    rawRentals.forEach((item) => {
      const dateKey = item.recRentDT2 ? item.recRentDT2.split('T')[0] : 'Unknown'
      const price = item.recPrice || 0
      if (!dailyMap[dateKey]) dailyMap[dateKey] = 0
      dailyMap[dateKey] += price
    })
    const sortedDates = Object.keys(dailyMap).sort()
    const dailyAmounts = sortedDates.map((date) => dailyMap[date])

    revenueOptions.value = { ...revenueOptions.value, xaxis: { categories: sortedDates } }
    revenueSeries.value = [{ name: '當日營收', data: dailyAmounts }]

    // (B) 熱門站點
    const spotCountMap = {}
    rawRentals.forEach((item) => {
      const spotName = item.rentSpotName || '未知站點'
      spotCountMap[spotName] = (spotCountMap[spotName] || 0) + 1
    })
    const sortedSpots = Object.entries(spotCountMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)

    const topSpotNames = sortedSpots.map((entry) => entry[0])
    const topSpotCounts = sortedSpots.map((entry) => entry[1])

    spotOptions.value = { ...spotOptions.value, xaxis: { categories: topSpotNames } }
    spotSeries.value = [{ name: '租借次數', data: topSpotCounts }]
  } catch (error) {
    console.error('統計載入失敗:', error)
  } finally {
    loading.value = false
  }
}

// =======================
// 5. 模組定義
// =======================
const moduleGroups = [
  {
    title: '場地與座位管理',
    icon: 'fas fa-building',
    modules: [
      {
        name: '據點管理',
        desc: '管理各區域據點',
        icon: 'fas fa-map-marker-alt',
        path: '/admin/spot/list',
      },
      { name: '座位管理', desc: '座位配置與狀態', icon: 'fas fa-chair', path: '/admin/seat/list' },
      {
        name: '據點分析',
        desc: '關鍵統計圖表',
        icon: 'fas fa-chart-line',
        path: '/admin/spot/analyze',
      },
      {
        name: '調度中心',
        desc: '即時監控與調度',
        icon: 'fas fa-broadcast-tower',
        path: '/admin/spot/monitor',
      },
    ],
  },
  {
    title: '會員與權限管理',
    icon: 'fas fa-users-cog',
    modules: [
      { name: '會員列表', desc: '一般會員資料管理', icon: 'fas fa-users', path: '/admin/members' },
      {
        name: '管理員列表',
        desc: '後台權限設定',
        icon: 'fas fa-user-shield',
        path: '/admin/admins',
      },
    ],
  },
  {
    title: '商家與優惠管理',
    icon: 'fas fa-store',
    modules: [
      {
        name: '商家管理',
        desc: '合作商家資料',
        icon: 'fas fa-store-alt',
        path: '/admin/merchants',
      },
      {
        name: '優惠券管理',
        desc: '發放與管理優惠券',
        icon: 'fas fa-ticket-alt',
        path: '/admin/discounts',
      },
      {
        name: '兌換紀錄',
        desc: '點數兌換報表',
        icon: 'fas fa-clipboard-list',
        path: '/admin/redemption-logs',
      },
    ],
  },
  {
    title: '租借與訂單管理',
    icon: 'fas fa-file-invoice-dollar',
    modules: [
      {
        name: '統計圖表',
        desc: '營收與租借分析',
        icon: 'fas fa-chart-pie',
        path: '/admin/rec-chart',
      },
      { name: '訂單管理', desc: '查詢歷史訂單', icon: 'fas fa-list-alt', path: '/admin/rec-rent' },
    ],
  },
  {
    title: '維護與工單管理',
    icon: 'fas fa-tools',
    modules: [
      {
        name: '維護人員管理',
        desc: '人員排班與資料',
        icon: 'fas fa-user-cog',
        path: '/admin/staff-list',
      },
      {
        name: '維修工單管理',
        desc: '追蹤維修進度',
        icon: 'fas fa-wrench',
        path: '/admin/mtif-list',
      },
      {
        name: '定期排程管理',
        desc: '自動化維護排程',
        icon: 'fas fa-calendar-alt',
        path: '/admin/maintenance/schedule',
      },
    ],
  },
]

// =======================
// 6. 生命週期
// =======================
onMounted(() => {
  updateTime()
  timeInterval.value = setInterval(updateTime, 1000)
  fetchStats()
})

onUnmounted(() => {
  if (timeInterval.value) clearInterval(timeInterval.value)
})
</script>

<template>
  <div class="dashboard-container">
    <div class="particles-container">
      <div class="particle" v-for="n in 30" :key="n"></div>
    </div>

    <main class="dashboard-inner fade-in-up">
      <header class="header-section">
        <div class="header-left">
          <h1>Hello, {{ adminName }} <span class="wave">👋</span></h1>
          <p class="subtitle">Take@Seat 營運控制中心</p>
        </div>
        <div class="header-right">
          <div class="time-pill">
            <span class="pulse-dot"></span>
            {{ currentTime }}
          </div>
        </div>
      </header>

      <section class="stats-section">
        <div class="stat-card">
          <div class="icon-circle blue-bg"><i class="fas fa-map-marker-alt"></i></div>
          <div class="stat-content">
            <span class="stat-num">{{ loading ? '...' : stats.totalSpots }}</span>
            <span class="stat-label">營運據點</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="icon-circle purple-bg"><i class="fas fa-chair"></i></div>
          <div class="stat-content">
            <span class="stat-num">{{ loading ? '...' : stats.totalSeats }}</span>
            <span class="stat-label">資產總數</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="icon-circle red-bg"><i class="fas fa-tools"></i></div>
          <div class="stat-content">
            <span class="stat-num">{{ loading ? '...' : stats.maintenanceCount }}</span>
            <span class="stat-label">維護案量</span>
          </div>
          <div class="alert-dot" v-if="stats.maintenanceCount > 0"></div>
        </div>

        <div class="stat-card">
          <div class="icon-circle green-bg"><i class="fas fa-broadcast-tower"></i></div>
          <div class="stat-content">
            <span class="stat-num">{{ loading ? '...' : stats.activeRentals }}</span>
            <span class="stat-label">即時租借</span>
          </div>
        </div>
      </section>

      <section class="charts-section">
        <div class="chart-wrapper">
          <div class="section-header">
            <h3><i class="fas fa-chart-area"></i> 營收趨勢分析</h3>
            <span class="badge-soft">近 7 日</span>
          </div>
          <apexchart
            type="area"
            height="350"
            :options="revenueOptions"
            :series="revenueSeries"
          ></apexchart>
        </div>

        <div class="chart-wrapper">
          <div class="section-header">
            <h3><i class="fas fa-chart-bar"></i> 熱門站點排行</h3>
            <span class="badge-soft">Top 5</span>
          </div>
          <apexchart
            type="bar"
            height="350"
            :options="spotOptions"
            :series="spotSeries"
          ></apexchart>
        </div>
      </section>

      <section class="modules-section">
        <div v-for="(group, gIndex) in moduleGroups" :key="gIndex" class="module-group-box">
          <div class="group-header">
            <i :class="group.icon"></i>
            <span>{{ group.title }}</span>
          </div>

          <div class="module-grid">
            <div
              v-for="(mod, mIndex) in group.modules"
              :key="mIndex"
              class="module-btn"
              @click="router.push(mod.path)"
            >
              <div class="mod-icon">
                <i :class="mod.icon"></i>
              </div>
              <div class="mod-info">
                <h4>{{ mod.name }}</h4>
                <span>{{ mod.desc }}</span>
              </div>
              <i class="fas fa-chevron-right arrow-icon"></i>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
/* =========================================
   1. Layout 適配修正
   ========================================= */
.dashboard-container {
  /* 🔥 移除 min-height: 100vh，因為外層 AdminLayout 已經有高度 */
  width: 100%;
  background-color: #f8fafc; /* 確保有底色 */
  color: #0f172a;
  position: relative;
  /* 給一點內距，讓內容不要貼死邊緣 */
  padding: 1.5rem;
  overflow: hidden; /* 防止內部粒子溢出 */
}

/* ✅ 改名為 dashboard-inner，不再與 AdminLTE 的 content-wrapper 衝突 */
.dashboard-inner {
  position: relative;
  z-index: 2;
  width: 100%;
  margin: 0;
  padding: 0;
}

/* =========================================
   2. 粒子特效 (改為 absolute)
   ========================================= */
.particles-container {
  position: absolute; /* 🔥 改為 absolute，只佔滿 dashboard-container */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(59, 130, 246, 0.4);
  border-radius: 50%;
  animation: float 20s infinite linear;
  opacity: 0.6;
}

.particle:nth-child(even) {
  background: rgba(16, 185, 129, 0.3);
  width: 8px;
  height: 8px;
  animation-duration: 25s;
}
.particle:nth-child(3n) {
  background: rgba(245, 158, 11, 0.3);
  width: 4px;
  height: 4px;
  animation-duration: 18s;
}

/* 隨機分佈 */
.particle:nth-child(1) {
  top: 10%;
  left: 5%;
}
.particle:nth-child(2) {
  top: 20%;
  left: 85%;
}
.particle:nth-child(3) {
  top: 80%;
  left: 15%;
}
.particle:nth-child(4) {
  top: 50%;
  left: 50%;
}
.particle:nth-child(5) {
  top: 30%;
  left: 10%;
}
.particle:nth-child(6) {
  top: 60%;
  left: 90%;
}
/* ...更多省略 */

@keyframes float {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-100px) rotate(180deg);
    opacity: 0.8;
  }
  100% {
    transform: translateY(0) rotate(360deg);
    opacity: 0.3;
  }
}

/* =========================================
   3. UI 元件
   ========================================= */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-left h1 {
  font-size: 1.75rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}
.subtitle {
  color: #64748b;
  margin-top: 0.25rem;
  font-size: 0.95rem;
}
.time-pill {
  background: white;
  padding: 0.6rem 1.2rem;
  border-radius: 99px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #475569;
  font-size: 0.9rem;
}
.pulse-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2);
  animation: pulse 2s infinite;
}

/* 統計卡片 Grid */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow:
    0 1px 3px 0 rgba(0, 0, 0, 0.05),
    0 1px 2px -1px rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  gap: 1.25rem;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  position: relative;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.icon-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  flex-shrink: 0;
}
.blue-bg {
  background: #eff6ff;
  color: #3b82f6;
}
.purple-bg {
  background: #f5f3ff;
  color: #8b5cf6;
}
.red-bg {
  background: #fef2f2;
  color: #ef4444;
}
.green-bg {
  background: #f0fdf4;
  color: #22c55e;
}

.stat-content {
  display: flex;
  flex-direction: column;
}
.stat-num {
  font-size: 1.8rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.1;
}
.stat-label {
  font-size: 0.9rem;
  color: #64748b;
  margin-top: 2px;
}
.alert-dot {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  animation: pulse 1s infinite;
}

/* 圖表 Grid */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(600px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.chart-wrapper {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow:
    0 1px 3px 0 rgba(0, 0, 0, 0.05),
    0 1px 2px -1px rgba(0, 0, 0, 0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.section-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #334155;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.badge-soft {
  background: #f1f5f9;
  color: #64748b;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

/* =========================================
   4. 模組區 - 分箱佈局 (Modules)
   ========================================= */
.modules-section {
  display: grid;
  /* 🔥 [關鍵] 大螢幕雙欄，中螢幕單欄 */
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 1.5rem;
}

/* 這是您要的「獨立 Box」 */
.module-group-box {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow:
    0 1px 3px 0 rgba(0, 0, 0, 0.05),
    0 1px 2px -1px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.1rem;
  font-weight: 700;
  color: #334155;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid #f1f5f9;
}
.group-header i {
  color: #3b82f6;
  font-size: 1.2rem;
}

/* 按鈕 Grid */
.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.module-btn {
  background: #f8fafc;
  border-radius: 10px;
  padding: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.module-btn:hover {
  background: white;
  border-color: #3b82f6;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}

.mod-icon {
  width: 40px;
  height: 40px;
  background: white;
  color: #64748b;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: 0.2s;
}
.module-btn:hover .mod-icon {
  background: #3b82f6;
  color: white;
}

.mod-info {
  flex: 1;
}
.mod-info h4 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
}
.mod-info span {
  font-size: 0.75rem;
  color: #94a3b8;
  display: block;
  margin-top: 2px;
}

.arrow-icon {
  font-size: 0.8rem;
  color: #cbd5e1;
  transition: 0.2s;
}
.module-btn:hover .arrow-icon {
  color: #3b82f6;
  transform: translateX(3px);
}

/* 動畫 */
.fade-in-up {
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(15px);
}
@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
@keyframes wave-animation {
  0%,
  100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(15deg);
  }
}
.wave {
  display: inline-block;
  animation: wave-animation 2.5s infinite;
  transform-origin: 70% 70%;
}

/* RWD */
@media (max-width: 768px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  .modules-section {
    grid-template-columns: 1fr;
  }
}
</style>
