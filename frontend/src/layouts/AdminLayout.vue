<script setup>
/**
 * AdminLayout.vue：AdminLTE 3 後台版型
 * [修正] 移除 lang="ts" 與型別標註，轉換為純 JS 寫法
 * [新增] 整合 SweetAlert2 登出確認
 */
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter, useRoute, RouterLink, RouterView } from 'vue-router'
import Swal from 'sweetalert2'
import { useAdminAuthStore } from '@/stores/adminAuth'

const adminAuthStore = useAdminAuthStore()

const router = useRouter()
const route = useRoute()

// AdminLTE 3 必備的 Body Class
const bodyClasses = ['hold-transition', 'layout-fixed']

onMounted(() => {
  document.body.classList.add(...bodyClasses)

  const savedAdmin = localStorage.getItem('admin')
  if (savedAdmin) {
    adminAuthStore.setAdmin(JSON.parse(savedAdmin))
  }
})

onBeforeUnmount(() => {
  document.body.classList.remove(...bodyClasses)
})

/**
 * 判斷目前路由是否屬於某個功能組
 */
const isActiveGroup = (prefix) => {
  // 移除 : string
  return route.path.startsWith(prefix)
}

/**
 * 登出功能 (SweetAlert2 版)
 */
const logout = async () => {
  const result = await Swal.fire({
    title: '確定要登出嗎？',
    text: '登出後將無法存取後台頁面',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#667eea',
    cancelButtonColor: '#6c757d',
    confirmButtonText: '確定登出',
    cancelButtonText: '取消',
  })

  // 3. 使用者按了「確定」
  if (!result.isConfirmed) return

  // ✅ 統一清除 localStorage（跟 main.js / router 守衛一致）
  localStorage.removeItem('token')
  localStorage.removeItem('admin')

  // 清除 Pinia 管理員狀態（若有此方法）
  if (typeof adminAuthStore.clearAdmin === 'function') {
    adminAuthStore.clearAdmin()
  }

  // 顯示成功訊息並跳轉
  await Swal.fire({
    title: '已登出！',
    text: '登出成功!',
    icon: 'success',
    timer: 1500,
    showConfirmButton: false,
  })

  router.push('/login')
}
</script>

<template>
  <div class="wrapper">
    <nav class="main-header navbar navbar-expand modern-navbar">
      <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" data-widget="pushmenu" href="#" role="button"
            ><i class="fas fa-bars"></i
          ></a>
        </li>
        <li class="nav-item d-none d-sm-inline-block">
          <RouterLink to="/admin" class="nav-link">後台首頁</RouterLink>
        </li>
      </ul>

      <ul class="navbar-nav ml-auto">
        <li class="nav-item">
          <a class="nav-link text-danger fw-bold" href="#" @click.prevent="logout">
            <i class="fas fa-sign-out-alt"></i> 登出
          </a>
        </li>
      </ul>
    </nav>

    <aside class="main-sidebar modern-sidebar elevation-4">
      <RouterLink to="/admin" class="brand-link">
        <span class="brand-text font-weight-light">SeatRentSys 管理系統</span>
      </RouterLink>

      <div class="sidebar">
        <!-- 管理員資訊區塊 -->
        <div class="user-panel mt-3 pb-3 mb-3 d-flex">
          <div class="admin-avatar">
            <i class="fas fa-user-circle"></i>
          </div>
          <div class="info">
            <div class="admin-name">{{ adminAuthStore.admin.name }}</div>
            <div class="admin-username">@{{ adminAuthStore.admin.username }}</div>
          </div>
        </div>
        <nav class="mt-2">
          <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu">
            <li class="nav-header">據點與座位管理</li>

            <li class="nav-item">
              <RouterLink
                to="/admin/spot/list"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/spot') }"
              >
                <i class="nav-icon fas fa-map-marker-alt"></i>
                <p>據點管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/seat/list"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/seat') }"
              >
                <i class="nav-icon fas fa-chair"></i>
                <p>座位管理</p>
              </RouterLink>
            </li>

            <li class="nav-header">基礎與會員管理</li>

            <li class="nav-item">
              <RouterLink
                to="/admin/members"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/members') }"
              >
                <i class="nav-icon fas fa-users"></i>
                <p>會員列表</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/admins"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/admins') }"
              >
                <i class="nav-icon fas fa-user-cog"></i>
                <p>管理員列表</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/merchants"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/merchants') }"
              >
                <i class="nav-icon fas fa-store"></i>
                <p>商家管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/discounts"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/discounts') }"
              >
                <i class="nav-icon fas fa-ticket-alt"></i>
                <p>優惠券管理</p>
              </RouterLink>
            </li>
            <li class="nav-item">
              <router-link 
              to="/admin/redemption-logs" class="nav-link" active-class="active">
              <i class="nav-icon bi bi-file-earmark-bar-graph"></i>
                <p>兌換紀錄報表</p>
              </router-link>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/payment"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/payment') }"
              >
                <i class="nav-icon fas fa-credit-card"></i>
                <p>金流管理</p>
              </RouterLink>
            </li>

            <li class="nav-header">訂單與維修體系</li>

            <li class="nav-item">
              <RouterLink
                to="/admin/rec-rent"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/rec-rent') }"
              >
                <i class="nav-icon fas fa-file-invoice"></i>
                <p>租借訂單管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/staff-list"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/staff') }"
              >
                <i class="nav-icon fas fa-user-shield"></i>
                <p>維護人員管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/mtif-list"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/mtif') }"
              >
                <i class="nav-icon fas fa-wrench"></i>
                <p>維修工單管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink
                to="/admin/maintenance/schedule"
                class="nav-link"
                :class="{ active: isActiveGroup('/admin/maintenance/schedule') }"
              >
                <i class="nav-icon fas fa-calendar-check"></i>
                <p>定期排程管理</p>
              </RouterLink>
            </li>
          </ul>
        </nav>
      </div>
    </aside>

    <div class="content-wrapper">
      <section class="content py-3">
        <div class="container-fluid">
          <Transition name="page-fade" mode="out-in">
            <RouterView />
          </Transition>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ==================== 現代化配色系統 ==================== */
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --sidebar-bg: #1a1d2e;
  --sidebar-hover: rgba(102, 126, 234, 0.1);
  --active-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ==================== 頂部導航條 ==================== */
.modern-navbar {
  background: white !important;
  border-bottom: 1px solid #e5e7eb !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05) !important;
}

/* ✅ 修正：確保導航連結文字為深色 */
.modern-navbar .nav-link {
  color: #374151 !important;
  font-weight: 500;
  transition: all 0.3s ease;
}

.modern-navbar .nav-link:hover {
  color: #667eea !important;
}

/* ✅ 修正：RouterLink 也需要深色文字 */
.modern-navbar .nav-item a.nav-link,
.modern-navbar .nav-item .router-link-active {
  color: #374151 !important;
}

.modern-navbar .nav-item a.nav-link:hover {
  color: #667eea !important;
}

.modern-navbar .text-danger {
  color: #ef4444 !important;
  font-weight: 600;
}

.modern-navbar .text-danger:hover {
  color: #dc2626 !important;
}

/* ==================== 側邊欄 ==================== */
.modern-sidebar {
  background: var(--sidebar-bg) !important;
}

/* ✅ 修正：增加更具體的選擇器以覆蓋 AdminLTE 預設樣式 */
aside.main-sidebar.modern-sidebar {
  background: #1a1d2e !important;
  background-color: #1a1d2e !important;
}

.modern-sidebar .sidebar {
  background: transparent !important;
}

.brand-link {
  background: rgba(102, 126, 234, 0.15) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  padding: 16px 20px !important;
  transition: all 0.3s ease;
}

.brand-link:hover {
  background: rgba(102, 126, 234, 0.25) !important;
}

.brand-text {
  color: white !important;
  font-weight: 600 !important;
  font-size: 16px !important;
}

/* ==================== 管理員資訊卡片 ==================== */
.user-panel {
  align-items: center;
  padding: 16px 20px !important;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin: 16px 12px !important;
  transition: all 0.3s ease;
}

.user-panel:hover {
  background: rgba(255, 255, 255, 0.08);
}

.admin-avatar {
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-panel .info {
  margin-left: 12px;
  line-height: 1.4;
}

.admin-name {
  color: white;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 2px;
}

.admin-username {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

/* ==================== 導航選單 ==================== */
.nav-header {
  color: rgba(255, 255, 255, 0.5) !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 12px 20px 8px !important;
  margin-top: 8px;
}

.nav-item {
  margin: 2px 8px;
}

.nav-link {
  border-radius: 8px !important;
  padding: 10px 16px !important;
  transition: all 0.3s ease !important;
  color: rgba(255, 255, 255, 0.8) !important;
  display: flex !important;
  align-items: center !important;
}

.nav-link:hover {
  background: var(--sidebar-hover) !important;
  color: white !important;
  transform: translateX(4px);
}

.nav-link.active,
.nav-link.router-link-active {
  background: var(--active-bg) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  font-weight: 600;
}

.nav-icon {
  width: 20px;
  font-size: 16px;
  text-align: center;
  margin-right: 8px;
}

.nav-link p {
  margin: 0 0 0 8px;
  font-size: 14px;
}

/* ==================== 內容區域 ==================== */
.content-wrapper {
  background: #f8f9fa !important;
  min-height: calc(100vh - 57px);
}

.content {
  padding: 24px 0 !important;
}

/* ==================== 響應式設計 ==================== */
@media (max-width: 768px) {
  .admin-avatar {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .admin-name {
    font-size: 14px;
  }
  
  .nav-link {
    padding: 8px 12px !important;
  }
}

/* ==================== 滾動條樣式 ==================== */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* ==================== 頁面切換動畫 ==================== */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.3s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
