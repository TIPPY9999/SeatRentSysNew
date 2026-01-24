<script setup>
import { ref, computed } from 'vue'
import { useMemberAuthStore } from '@/stores/memberAuth'
import { useAdminAuthStore } from '@/stores/adminAuth'

// --- 版面狀態 ---
const isSidebarCollapsed = ref(false) // 控制側邊欄是否收合

/**
 * 切換側邊欄的收合狀態
 */
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const memberAuthStore = useMemberAuthStore()
const adminAuthStore = useAdminAuthStore()

/**
 * UID 顯示邏輯：
 * - 管理員優先
 * - 再來會員
 * - 都沒有就尚未登入
 */
const displayUID = computed(() => {
  if (memberAuthStore.isLogin) {
    return memberAuthStore.member.memUsername
  }
  if (adminAuthStore.isLogin) {
    return adminAuthStore.admin.username
  }
  return null
})

/**
 * 登出：
 * - 清空 Pinia（會員 / 管理員）
 * - 清空 localStorage
 * - 停留在首頁
 */
const logout = () => {
  // 清空 Pinia
  memberAuthStore.clearMemberLogin()
  adminAuthStore.clearAdmin()

  // 清空 localStorage
  localStorage.removeItem('member_user')
  localStorage.removeItem('admin')
  localStorage.removeItem('token')

  // 留在首頁（刷新一次確保畫面同步）
  window.location.href = '/'
}
</script>

<template>
  <div class="page-wrapper" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- 左側邊欄 -->
    <aside class="sidebar">
      <!-- 可收合的功能選單 -->
      <ul class="menu-list">
        <li class="menu-item">
          <router-link to="/" class="member-info">
            <span class="icon-wrapper">
              <el-icon><House /></el-icon>
            </span>
            <span class="logo">Take@Seat</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/login" class="member-info">
            <span class="icon-wrapper">
              <el-icon><Avatar /></el-icon>
            </span>
            <span class="menu-text">會員登入</span>
          </router-link>
        </li>
        <li class="menu-item">
          <span class="menu-text">
            UID：
            <span v-if="displayUID">
              {{ displayUID }}
            </span>
            <span v-else> 尚未登入 </span>
          </span>
        </li>

        <li class="menu-item">
          <router-link to="/SearchSpot" class="member-info">
            <span class="icon-wrapper">
              <el-icon><Pointer /></el-icon>
            </span>
            <span class="menu-text">租借服務</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/mall" class="member-info">
            <span class="icon-wrapper">
              <el-icon><Ticket /></el-icon>
            </span>
            <span class="menu-text">商家優惠</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/snake" class="member-info">
            <span class="icon-wrapper">
              <el-icon><SwitchFilled /></el-icon>
            </span>
            <span class="menu-text">小遊戲</span>
          </router-link>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><MapLocation /></el-icon>
          </span>
          <span class="menu-text">猜你喜歡</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Comment /></el-icon>
          </span>
          <span class="menu-text">分享討論</span>
        </li>
        <li class="menu-item">
          <span class="icon-wrapper">
            <el-icon><Phone /></el-icon>
          </span>
          <span class="menu-text">客服支援</span>
        </li>
        <li class="menu-item">
          <router-link to="/payment" class="member-info">
            <span class="icon-wrapper">
              <el-icon><StarFilled /></el-icon>
            </span>
            <span class="menu-text">支持我們</span>
          </router-link>
        </li>
        <li class="menu-item" @click="logout">
          <span class="icon-wrapper">
            <el-icon><TopLeft /></el-icon>
          </span>
          <span class="menu-text">登出</span>
        </li>
      </ul>

      <!-- 收合按鈕 -->
      <div class="sidebar-footer">
        <button
          @click="toggleSidebar"
          class="toggle-btn"
          :title="isSidebarCollapsed ? '展開' : '收合'"
        >
          <el-icon>
            <DArrowLeft v-if="!isSidebarCollapsed" />
            <DArrowRight v-if="isSidebarCollapsed" />
          </el-icon>
        </button>
      </div>
      <div class="menu-admin" v-if="adminAuthStore.isLogin">
        <router-link to="/admin" class="member-info">
          <span class="icon-wrapper">
            <el-icon><Tools /></el-icon>
          </span>
          <span class="menu-text">後台管理</span>
        </router-link>
      </div>
    </aside>

    <!-- 右側主內容容器 -->
    <main class="main-content-area">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
/* --- 1. CSS 變數 --- */
:root {
  --sidebar-width-expanded: 200px;
  --sidebar-width-collapsed: 70px;
}

/* --- 2. 主佈局 --- */

.logo {
  font-size: 22px;
  font-weight: 600;
  margin: 0 10px;
  padding: 0;
}
.page-wrapper {
  display: flex;
  height: 100vh; /* 改為 100vh 佔滿整個視窗高度 */
  width: 100%;
  background-color: #f4f6f9;
}

/* --- 3. 側邊欄 --- */
.sidebar {
  width: var(--sidebar-width-expanded);
  background-color: #b9f8b9;
  border-right: 1px solid #dee2e6;
  transition: width 0.2s ease;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.page-wrapper.sidebar-collapsed .sidebar {
  width: var(--sidebar-width-collapsed);
}

/* 通用圖示容器樣式 */
.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  font-size: 30px; /* for SVG size */
  color: #484848;
  flex-shrink: 0;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 20px;
  text-decoration: none;
  color: inherit;
}

/* 功能選單 (可隱藏) */
.menu-list {
  list-style: none;
  padding: 0;
  margin: 10px 0;
  flex-grow: 1;
  overflow-y: auto;
  overflow-x: hidden; /* 新增：防止水平滾動條 */
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  gap: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.menu-item:hover,
.menu-admin:hover {
  background-color: #f5f7fa;
}

.menu-admin {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  gap: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}
.menu-text {
  font-size: 20px;
  font-family: fantasy;
  opacity: 1;
  transition:
    opacity 0.2s ease,
    width 0.2s ease;
}

.page-wrapper.sidebar-collapsed .menu-text {
  opacity: 0;
  width: 0;
}

/* 側邊欄頁腳 (收合按鈕) */
.sidebar-footer {
  padding: 5px;
  margin-top: auto; /* 將按鈕推到底部 */
  border-top: 1px solid #e9ecef;
}

.toggle-btn {
  width: 100%;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  padding: 5px;
  font-size: 28px;
  line-height: 1;
  color: #606266;
  display: flex;
  justify-content: center;
  align-items: center;
  transition:
    background-color 0.2s,
    color 0.2s;
}

.toggle-btn:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

/* --- 4. 主內容容器 --- */
.main-content-area {
  flex-grow: 1;
  width: 100%;
  height: 100%;
  position: relative;
  overflow-y: auto; /* 如果內容超長，允許滾動 */
}
</style>
