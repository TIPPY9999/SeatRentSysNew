<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="40" height="40" />
        <span>座位租借系統</span>
      </div>
      
      <nav class="menu">
        <router-link to="/" class="menu-item">
          <i class="icon">🏠</i> 首頁
        </router-link>
        <router-link to="/merchants" class="menu-item">
          <i class="icon">🏢</i> 商家管理
        </router-link>
        <router-link to="/discounts" class="menu-item">
          <i class="icon">🎫</i> 優惠券管理
        </router-link>
      </nav>
    </aside>

    <main class="main-container">
      <header class="top-header">
        <div class="user-info">管理員 您好</div>
      </header>
      
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style>
/* 移除所有預設 margin */
body {
  margin: 0;
  padding: 0;
  background-color: #f0f2f5;
  font-family: 'PingFang TC', 'Microsoft JhengHei', sans-serif;
}

.admin-layout {
  display: flex;
  height: 100vh;
}

/* 側邊欄樣式 */
.sidebar {
  width: 240px;
  background-color: #001529;
  color: white;
  display: flex;
  flex-direction: column;
}

.brand {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
  font-weight: bold;
  background: #002140;
}

.menu {
  flex: 1;
  padding: 10px 0;
}

.menu-item {
  display: block;
  padding: 15px 25px;
  color: #a6adb4;
  text-decoration: none;
  transition: all 0.3s;
}

.menu-item:hover {
  color: white;
  background-color: #1890ff;
}

/* 啟動中的連結樣式 */
.router-link-active {
  color: white !important;
  background-color: #1890ff !important;
}

/* 主內容區樣式 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.top-header {
  background: white;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 30px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.content {
  padding: 24px;
}
</style>