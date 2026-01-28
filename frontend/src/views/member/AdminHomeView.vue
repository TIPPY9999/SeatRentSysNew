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

const adminName = computed(() => adminAuthStore.admin?.name || '管理員')

// 統計數據
const stats = ref({
  totalSpots: 0, totalSeats: 0, maintenanceCount: 0, totalMembers: 0, activeRentals: 0
})

const loading = ref(true)
const currentTime = ref('')
const timeInterval = ref(null)

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

const fetchStats = async () => {
  loading.value = true
  try {
    const [spotsRes, seatsRes, membersRes, rentalsRes, ticketsRes] = await Promise.all([
      axios.get('/api/spot/list'),
      axios.get('/api/seats'),
      axios.get('/api/members'),
      axios.get('/api/rec-rent'),
      axios.get('/api/maintenance/tickets/active'),
    ])

    stats.value.totalSpots = spotsRes.data?.length || 0
    stats.value.totalSeats = seatsRes.data?.length || 0
    stats.value.maintenanceCount = (ticketsRes.data || []).length
    stats.value.totalMembers = membersRes.data?.length || 0
    stats.value.activeRentals = (rentalsRes.data || []).filter((r) => r.recStatus === '租借中').length
  } catch (error) {
    console.error('統計載入失敗:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  updateTime(); timeInterval.value = setInterval(updateTime, 1000)
  fetchStats()
})

onUnmounted(() => { if (timeInterval.value) clearInterval(timeInterval.value) })

const moduleGroups = [
  {
    title: '場地與座位管理',
    icon: 'fas fa-building',
    modules: [
      { name: '據點管理', desc: '管理各區域據點', icon: 'fas fa-map-marker-alt', path: '/admin/spot/list' },
      { name: '據點分析', desc: '關鍵統計圖表', icon: 'fas fa-chart-line', path: '/admin/spot/analyze' },
    ],
  },
  {
    title: '維護與工單管理',
    icon: 'fas fa-tools',
    modules: [
      { name: '維修工單管理', desc: '追蹤維修進度', icon: 'fas fa-wrench', path: '/admin/mtif-list' },
    ],
  }
]
</script>

<template>
  <div class="dashboard">
    <div class="particles-bg"><div class="particle" v-for="n in 15" :key="n"></div></div>

    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text"><h1>歡迎回來，{{ adminName }}</h1><p class="subtitle">營運管理核心儀表板</p></div>
        <div class="time-display"><i class="fas fa-clock"></i><span>{{ currentTime }}</span></div>
      </div>
    </div>

    <!-- 已移除方案 C Widget，恢復原樣 -->

    <div class="stats-grid">
      <div class="stat-card glass"><div class="stat-icon spots"><i class="fas fa-map-marker-alt"></i></div>
        <div class="stat-details"><span class="stat-value">{{ loading ? '...' : stats.totalSpots }}</span><span class="stat-label">營運據點</span></div>
      </div>
      <div class="stat-card glass"><div class="stat-icon seats"><i class="fas fa-chair"></i></div>
        <div class="stat-details"><span class="stat-value">{{ loading ? '...' : stats.totalSeats }}</span><span class="stat-label">資產總數</span></div>
      </div>
      <div class="stat-card glass"><div class="stat-icon maint"><i class="fas fa-tools"></i></div>
        <div class="stat-details"><span class="stat-value">{{ loading ? '...' : stats.maintenanceCount }}</span><span class="stat-label">維護案量</span></div>
      </div>
      <div class="stat-card glass highlight"><div class="stat-icon live"><i class="fas fa-broadcast-tower"></i></div>
        <div class="stat-details"><span class="stat-value">{{ loading ? '...' : stats.activeRentals }}</span><span class="stat-label">即時租借</span></div>
      </div>
    </div>

    <div class="modules-section">
      <div class="module-groups">
        <div v-for="(group, gIndex) in moduleGroups" :key="gIndex" class="module-group glass">
          <div class="group-header"><div class="group-icon"><i :class="group.icon"></i></div><h3 class="group-title">{{ group.title }}</h3></div>
          <div class="group-modules">
            <div v-for="(mod, mIndex) in group.modules" :key="mIndex" class="module-item" @click="router.push(mod.path)">
              <div class="module-icon"><i :class="mod.icon"></i></div><div class="module-info"><span class="module-name">{{ mod.name }}</span></div>
              <i class="fas fa-chevron-right arrow"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { min-height: 100vh; padding: 24px; background: #f8fafc; position: relative; overflow: hidden; }
.particles-bg { position: absolute; inset: 0; pointer-events: none; }
.particle { position: absolute; width: 4px; height: 4px; background: #3b82f6; border-radius: 50%; opacity: 0.2; animation: move 10s infinite; }
@keyframes move { 0% { transform: translate(0, 0); } 50% { transform: translate(100px, 50px); } 100% { transform: translate(0, 0); } }
.glass { background: rgba(255,255,255,0.85); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.5); border-radius: 20px; }
.welcome-section { margin-bottom: 24px; }
.welcome-content { display: flex; justify-content: space-between; align-items: center; padding: 25px 30px; background: white; border-radius: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.02); }
.welcome-text h1 { font-size: 1.6rem; color: #1e3a5f; margin: 0; }
.time-display { padding: 10px 18px; background: #1e3a5f; color: white; border-radius: 12px; font-weight: 600; font-size: 0.85rem; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 30px; }
.stat-card { padding: 20px; display: flex; align-items: center; gap: 15px; }
.stat-icon { width: 45px; height: 45px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; }
.stat-icon.spots { background: #eff6ff; color: #3b82f6; }
.stat-icon.seats { background: #fdf2f8; color: #db2777; }
.stat-icon.maint { background: #fef2f2; color: #ef4444; }
.stat-icon.live { background: #f0fdf4; color: #22c55e; }
.stat-value { font-size: 1.4rem; font-weight: 800; display: block; color: #1e3a5f; }
.stat-label { font-size: 0.8rem; color: #94a3b8; }
.module-groups { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.module-group { overflow: hidden; padding: 10px; }
.group-header { display: flex; align-items: center; gap: 10px; padding: 12px; }
.module-item { display: flex; align-items: center; gap: 12px; padding: 12px 15px; border-radius: 12px; cursor: pointer; transition: 0.2s; }
.module-item:hover { background: #f1f5f9; }
.module-icon { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; color: #64748b; font-size: 1rem; }
.module-name { font-size: 0.85rem; font-weight: 600; color: #334155; flex: 1; }
.arrow { color: #cbd5e1; font-size: 0.7rem; }
</style>
