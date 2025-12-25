<template>
  <div class="admin-wrapper">
    <!-- 上方 Navbar -->
    <nav class="topbar">
      <div class="brand">SeatRentSys 後台管理</div>
      <div class="logout" @click="logout">登出</div>
    </nav>

    <div class="main">
      <!-- 左側 Sidebar -->
      <aside class="sidebar">
        <h3 class="sidebar-title">SeatRentSys</h3>

        <ul class="menu">
          <li @click="go('/admin')">後台首頁</li>
          <li @click="go('/admin/members')">會員列表</li>
          <li @click="go('/admin/admins')">管理員管理</li>
          <li @click="go('/admin/orders')">維修人員管理</li>
          <li @click="go('/admin/orders')">維修工單管理</li>
          <li @click="go('/admin/orders')">租借紀錄</li>
        </ul>
      </aside>

      <!-- 右側內容 -->
      <section class="content">
        <router-view />
      </section>
    </div>

    <footer class="footer">
      SeatRentSys © 2025
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

function go(path) {
  router.push(path)
}

function logout() {
  router.push('/login')
}
</script>

<style scoped>
.admin-wrapper {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f6f8;
}

/* 上方 */
.topbar {
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.brand {
  font-size: 18px;
  font-weight: bold;
}

.logout {
  color: #d9534f;
  cursor: pointer;
}

/* 中間 */
.main {
  flex: 1;
  display: flex;
}

/* 左邊 */
.sidebar {
  width: 220px;
  background: #343a40;
  color: #fff;
  padding: 20px 0;
}

.sidebar-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 18px;
}

.menu {
  list-style: none;
  padding: 0;
}

.menu li {
  padding: 12px 20px;
  cursor: pointer;
}

.menu li:hover {
  background: #495057;
}

/* 右邊 */
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
  font-size: 14px;
  background: #ffffff;
  border-top: 1px solid #ddd;
}
</style>