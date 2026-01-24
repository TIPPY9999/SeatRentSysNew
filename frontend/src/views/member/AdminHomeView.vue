<script setup>
/**
 * AdminHomeView.vue：後台首頁入口
 * 科技數據風儀表板設計 - Glassmorphism 風格
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'
import axios from 'axios'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

// 取得當前登入管理員名稱
const adminName = computed(() => adminAuthStore.admin?.name || '管理員')

// 統計數據 - 從 API 取得
const stats = ref({
  totalSpots: 0,       // 租借點數量
  totalSeats: 0,       // 椅子數量
  maintenanceCount: 0, // 維修/維護中數量
  totalMembers: 0,     // 會員人數
  activeRentals: 0     // 進行中租借
})

// 載入狀態
const loading = ref(true)

// 當前時間
const currentTime = ref('')
const timeInterval = ref(null)

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 取得統計數據
const fetchStats = async () => {
  loading.value = true
  try {
    // 並行請求所有數據
    const [spotsRes, seatsRes, membersRes, rentalsRes, ticketsRes] = await Promise.all([
      axios.get('http://localhost:8080/spot/list'),          // 租借點
      axios.get('http://localhost:8080/seats'),              // 椅子
      axios.get('http://localhost:8080/members'),            // 會員
      axios.get('http://localhost:8080/rec-rent'),           // 租借紀錄
      axios.get('http://localhost:8080/api/maintenance/tickets/active') // 維修工單
    ])

    // 租借點數量
    stats.value.totalSpots = spotsRes.data?.length || 0

    // 椅子數量
    stats.value.totalSeats = seatsRes.data?.length || 0

    // 維修/維護中數量 (狀態為 維修中 或 待處理 的工單數)
    const maintenanceTickets = ticketsRes.data || []
    stats.value.maintenanceCount = maintenanceTickets.length

    // 會員人數
    stats.value.totalMembers = membersRes.data?.length || 0

    // 進行中租借 (狀態為 租借中)
    const rentals = rentalsRes.data || []
    stats.value.activeRentals = rentals.filter(r => r.recStatus === '租借中').length

  } catch (error) {
    console.error('取得統計數據失敗:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  updateTime()
  timeInterval.value = setInterval(updateTime, 1000)
  fetchStats()
})

onUnmounted(() => {
  if (timeInterval.value) {
    clearInterval(timeInterval.value)
  }
})

// 管理功能模組分類
const moduleGroups = [
  {
    title: '會員與權限管理',
    icon: 'fas fa-users-cog',
    modules: [
      { name: '會員管理', desc: '管理會員資料', icon: 'fas fa-users', path: '/admin/members' },
      { name: '管理員管理', desc: '管理後台人員帳號', icon: 'fas fa-user-shield', path: '/admin/admins' }
    ]
  },
  {
    title: '場地與座位管理',
    icon: 'fas fa-building',
    modules: [
      { name: '據點管理', desc: '管理各區域據點', icon: 'fas fa-map-marker-alt', path: '/admin/spot/list' },
      { name: '座位管理', desc: '管理座位狀態與配置', icon: 'fas fa-chair', path: '/admin/seat/list' }
    ]
  },
  {
    title: '租借與金流管理',
    icon: 'fas fa-hand-holding-usd',
    modules: [
      { name: '租借紀錄', desc: '查看租借與歸還紀錄', icon: 'fas fa-clipboard-list', path: '/admin/rec-rent' },
      { name: '金流管理', desc: '管理金流相關設定', icon: 'fas fa-credit-card', path: '/admin/payment' }
    ]
  },
  {
    title: '維護與工單管理',
    icon: 'fas fa-tools',
    modules: [
      { name: '維護人員管理', desc: '管理維護技術人員', icon: 'fas fa-user-cog', path: '/admin/staff-list' },
      { name: '維修工單管理', desc: '追蹤維修進度與派工', icon: 'fas fa-wrench', path: '/admin/mtif-list' }
    ]
  },
  {
    title: '商家與優惠管理',
    icon: 'fas fa-store-alt',
    modules: [
      { name: '商家管理', desc: '管理商家資料與狀態', icon: 'fas fa-store', path: '/admin/merchants' },
      { name: '優惠券管理', desc: '管理優惠券與活動', icon: 'fas fa-ticket-alt', path: '/admin/coupons' }
    ]
  },
  {
    title: '其他功能',
    icon: 'fas fa-ellipsis-h',
    modules: [
      { name: '小遊戲', desc: '貪吃蛇', icon: 'fas fa-gamepad', path: '/admin/snake-game' }
    ]
  }
]
</script>

<template>
  <div class="dashboard">
    <!-- 粒子背景效果 -->
    <div class="particles-bg">
      <div class="particle" v-for="n in 20" :key="n"></div>
    </div>

    <!-- 頂部歡迎區 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>歡迎回來，{{ adminName }}</h1>
          <p class="subtitle">座位租借系統管理後台</p>
        </div>
        <div class="time-display">
          <i class="fas fa-clock"></i>
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 統計卡片區 -->
    <div class="stats-grid">
      <!-- 租借點數量 -->
      <div class="stat-card glass">
        <div class="stat-icon spots">
          <i class="fas fa-map-marker-alt"></i>
        </div>
        <div class="stat-details">
          <span class="stat-value" :class="{ 'loading-text': loading }">
            {{ loading ? '載入中...' : stats.totalSpots }}
          </span>
          <span class="stat-label">租借據點</span>
        </div>
        <div class="stat-indicator" v-if="!loading">
          <span class="dot active"></span> 營運中
        </div>
      </div>

      <!-- 椅子數量 -->
      <div class="stat-card glass">
        <div class="stat-icon seats">
          <i class="fas fa-chair"></i>
        </div>
        <div class="stat-details">
          <span class="stat-value" :class="{ 'loading-text': loading }">
            {{ loading ? '載入中...' : stats.totalSeats }}
          </span>
          <span class="stat-label">座位總數</span>
        </div>
        <div class="stat-badge" v-if="!loading">
          <i class="fas fa-check-circle"></i> 可用
        </div>
      </div>

      <!-- 維修/維護中 -->
      <div class="stat-card glass">
        <div class="stat-icon maintenance">
          <i class="fas fa-tools"></i>
        </div>
        <div class="stat-details">
          <span class="stat-value" :class="{ 'loading-text': loading }">
            {{ loading ? '載入中...' : stats.maintenanceCount }}
          </span>
          <span class="stat-label">維護工單</span>
        </div>
        <div class="stat-badge warning" v-if="!loading && stats.maintenanceCount > 0">
          <i class="fas fa-exclamation-triangle"></i> 處理中
        </div>
        <div class="stat-badge success" v-else-if="!loading">
          <i class="fas fa-check"></i> 無待處理
        </div>
      </div>

      <!-- 會員人數 -->
      <div class="stat-card glass">
        <div class="stat-icon members">
          <i class="fas fa-users"></i>
        </div>
        <div class="stat-details">
          <span class="stat-value" :class="{ 'loading-text': loading }">
            {{ loading ? '載入中...' : stats.totalMembers.toLocaleString() }}
          </span>
          <span class="stat-label">會員總數</span>
        </div>
        <div class="stat-trend up" v-if="!loading">
          <i class="fas fa-user-plus"></i> 成長中
        </div>
      </div>

      <!-- 進行中租借 -->
      <div class="stat-card glass highlight">
        <div class="stat-icon rentals">
          <i class="fas fa-clipboard-check"></i>
        </div>
        <div class="stat-details">
          <span class="stat-value" :class="{ 'loading-text': loading }">
            {{ loading ? '載入中...' : stats.activeRentals }}
          </span>
          <span class="stat-label">進行中租借</span>
        </div>
        <div class="stat-indicator live" v-if="!loading">
          <span class="dot pulse"></span> 即時
        </div>
      </div>
    </div>

    <!-- 功能模組區 - 分類設計 -->
    <div class="modules-section">
      <div class="section-header">
        <div class="section-title">
          <i class="fas fa-th-large"></i>
          <span>管理功能</span>
        </div>
        <p class="section-subtitle">快速存取各項管理功能</p>
      </div>

      <!-- 模組分類群組 -->
      <div class="module-groups">
        <div 
          v-for="(group, gIndex) in moduleGroups" 
          :key="gIndex" 
          class="module-group glass"
        >
          <div class="group-header">
            <div class="group-icon">
              <i :class="group.icon"></i>
            </div>
            <h3 class="group-title">{{ group.title }}</h3>
          </div>
          
          <div class="group-modules">
            <div 
              v-for="(mod, mIndex) in group.modules" 
              :key="mIndex"
              class="module-item"
              @click="router.push(mod.path)"
            >
              <div class="module-icon">
                <i :class="mod.icon"></i>
              </div>
              <div class="module-info">
                <span class="module-name">{{ mod.name }}</span>
                <span class="module-desc">{{ mod.desc }}</span>
              </div>
              <div class="module-arrow">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 主容器 ========== */
.dashboard {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e8f4fc 100%);
  position: relative;
  overflow: hidden;
}

/* ========== 粒子背景 ========== */
.particles-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 50%;
  opacity: 0.3;
  animation: float 15s infinite ease-in-out;
}

.particle:nth-child(1) { left: 5%; top: 10%; animation-delay: 0s; }
.particle:nth-child(2) { left: 15%; top: 30%; animation-delay: 1s; }
.particle:nth-child(3) { left: 25%; top: 50%; animation-delay: 2s; }
.particle:nth-child(4) { left: 35%; top: 20%; animation-delay: 3s; }
.particle:nth-child(5) { left: 45%; top: 70%; animation-delay: 4s; }
.particle:nth-child(6) { left: 55%; top: 15%; animation-delay: 5s; }
.particle:nth-child(7) { left: 65%; top: 45%; animation-delay: 6s; }
.particle:nth-child(8) { left: 75%; top: 65%; animation-delay: 7s; }
.particle:nth-child(9) { left: 85%; top: 25%; animation-delay: 8s; }
.particle:nth-child(10) { left: 95%; top: 55%; animation-delay: 9s; }
.particle:nth-child(11) { left: 10%; top: 80%; animation-delay: 10s; }
.particle:nth-child(12) { left: 20%; top: 5%; animation-delay: 11s; }
.particle:nth-child(13) { left: 30%; top: 85%; animation-delay: 12s; }
.particle:nth-child(14) { left: 40%; top: 40%; animation-delay: 13s; }
.particle:nth-child(15) { left: 50%; top: 90%; animation-delay: 14s; }
.particle:nth-child(16) { left: 60%; top: 35%; animation-delay: 0.5s; }
.particle:nth-child(17) { left: 70%; top: 75%; animation-delay: 1.5s; }
.particle:nth-child(18) { left: 80%; top: 8%; animation-delay: 2.5s; }
.particle:nth-child(19) { left: 90%; top: 60%; animation-delay: 3.5s; }
.particle:nth-child(20) { left: 3%; top: 45%; animation-delay: 4.5s; }

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-30px) translateX(10px);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-15px) translateX(-10px);
    opacity: 0.4;
  }
  75% {
    transform: translateY(-40px) translateX(5px);
    opacity: 0.6;
  }
}

/* ========== 玻璃擬態效果 ========== */
.glass {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.08);
}

/* ========== 歡迎區 ========== */
.welcome-section {
  position: relative;
  z-index: 1;
  margin-bottom: 28px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.06);
}

.welcome-text h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e3a5f;
  letter-spacing: -0.5px;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 0.95rem;
  color: #64748b;
}

.time-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 12px;
  color: white;
  font-weight: 500;
  font-size: 0.95rem;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

/* ========== 統計卡片區 ========== */
.stats-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 20px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.15);
}

.stat-card.highlight {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(96, 165, 250, 0.15) 100%);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon.spots {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.stat-icon.seats {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #4f46e5;
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.stat-icon.members {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.stat-icon.rentals {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.stat-details {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e3a5f;
  line-height: 1.2;
}

.stat-value.loading-text {
  font-size: 0.9rem;
  color: #94a3b8;
}

.stat-label {
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 2px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: #059669;
}

.stat-trend.up {
  color: #059669;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: #3b82f6;
}

.stat-badge.warning {
  color: #f59e0b;
}

.stat-badge.success {
  color: #10b981;
}

.stat-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: #64748b;
}

.stat-indicator.live {
  color: #10b981;
  font-weight: 500;
}

.stat-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}

.stat-indicator .dot.active {
  animation: pulse 2s infinite;
}

.stat-indicator .dot.pulse {
  animation: pulse 1s infinite;
  background: #10b981;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
}

/* ========== 功能模組區 ========== */
.modules-section {
  position: relative;
  z-index: 1;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: #3b82f6;
  font-size: 1.1rem;
}

.section-title span {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1e3a5f;
}

.section-subtitle {
  margin: 6px 0 0 26px;
  font-size: 0.85rem;
  color: #94a3b8;
}

/* ========== 模組群組 ========== */
.module-groups {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.module-group {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.module-group:hover {
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.12);
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(96, 165, 250, 0.05) 100%);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

.group-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.group-title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e3a5f;
}

.group-modules {
  padding: 8px;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.module-item:hover {
  background: rgba(59, 130, 246, 0.08);
}

.module-item .module-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #3b82f6;
  transition: all 0.2s ease;
}

.module-item:hover .module-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  transform: scale(1.05);
}

.module-item .module-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.module-item .module-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #1e3a5f;
}

.module-item .module-desc {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 2px;
}

.module-item .module-arrow {
  color: #cbd5e1;
  font-size: 12px;
  transition: all 0.2s ease;
}

.module-item:hover .module-arrow {
  color: #3b82f6;
  transform: translateX(4px);
}

/* ========== 響應式設計 ========== */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .module-groups {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .module-groups {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: row;
    align-items: center;
    padding: 16px;
  }

  .stat-card .stat-icon {
    margin-right: 12px;
  }

  .stat-card .stat-details {
    flex: 1;
  }

  .welcome-text h1 {
    font-size: 1.4rem;
  }
}
</style>
