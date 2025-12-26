<template>
  <div class="admin-wrapper">
    <nav class="topbar">
      <div class="brand">SeatRentSys 後台管理</div>
      <div class="logout" @click="logout"><i class="fas fa-sign-out-alt"></i> 登出</div>
    </nav>

    <div class="main">
      <aside class="sidebar">
        <h3 class="sidebar-title">SeatRentSys</h3>

        <ul class="menu">
          <li @click="go('/admin')" :class="{ active: isRoute('/admin') }">
            <i class="fas fa-home"></i> 後台首頁
          </li>

          <li @click="go('/admin/members')" :class="{ active: isActiveGroup('/admin/members') }">
            <i class="fas fa-users"></i> 會員列表
          </li>

          <li @click="go('/admin/admins')" :class="{ active: isActiveGroup('/admin/admins') }">
            <i class="fas fa-user-shield"></i> 管理員管理
          </li>

          <li @click="go('/admin/rec-rent')" :class="{ active: isActiveGroup('/admin/rec-rent') }">
            <i class="fas fa-clipboard-list"></i> 租借訂單管理
          </li>

          <div class="menu-divider">維修管理</div>

          <li @click="go('/admin/staff-list')" :class="{ active: isActiveGroup('/admin/staff') }">
            <i class="fas fa-user-cog"></i> 維護人員管理
          </li>

          <li @click="go('/admin/mtif-list')" :class="{ active: isActiveGroup('/admin/mtif') }">
            <i class="fas fa-wrench"></i> 維修工單管理
          </li>
        </ul>
      </aside>

      <section class="content">
        <router-view />
      </section>
    </div>

    <footer class="footer">SeatRentSys © 2025 All Rights Reserved.</footer>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

/**
 * 判斷目前是否在該路由 (精確匹配)
 */
function isRoute(path) {
  return route.path === path
}

/**
 * 判斷目前路由是否屬於某個功能組 (模糊匹配)
 * 解決：在子頁面（例如 /members/edit）時，側邊欄對應的主選單也能維持高亮
 */
function isActiveGroup(prefix) {
  return route.path.startsWith(prefix)
}

/**
 * 執行頁面跳轉
 */
function go(path) {
  router.push(path)
}

/**
 * 登出功能
 */
function logout() {
  if (confirm('確定要登出嗎？')) {
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-wrapper {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f6f8;
}

/* 上方 Navbar */
.topbar {
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.brand {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.logout {
  color: #d9534f;
  cursor: pointer;
  font-weight: 500;
  transition: 0.2s;
}

.logout:hover {
  color: #c9302c;
  text-decoration: underline;
}

/* 中間主體 */
.main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左側側邊欄 */
.sidebar {
  width: 240px;
  background: #343a40;
  color: #fff;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.sidebar-title {
  text-align: center;
  margin-bottom: 25px;
  font-size: 20px;
  letter-spacing: 1px;
  color: #fff;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
}

.menu li {
  padding: 12px 25px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  color: #c2c7d0;
}

.menu li i {
  width: 25px;
  margin-right: 10px;
  font-size: 16px;
}

.menu li:hover {
  background: #495057;
  color: #fff;
}

.menu li.active {
  background: #007bff;
  color: #fff;
  font-weight: bold;
}

/* 選單分隔線樣式 */
.menu-divider {
  padding: 15px 25px 5px 25px;
  font-size: 11px;
  text-transform: uppercase;
  color: #6c757d;
  font-weight: bold;
  letter-spacing: 1px;
}

/* 右側內容區 */
.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f4f6f8;
}

/* footer */
.footer {
  height: 40px;
  text-align: center;
  line-height: 40px;
  font-size: 13px;
  background: #ffffff;
  border-top: 1px solid #ddd;
  color: #888;
}
</style>
