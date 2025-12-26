<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-wrapper layout-fixed sidebar-expand-lg bg-body-tertiary">
    
    <nav class="app-header navbar navbar-expand bg-body border-bottom">
      <div class="container-fluid">
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link" data-lte-toggle="sidebar" href="#" role="button">
              <i class="bi bi-list">☰</i>
            </a>
          </li>
          <li class="nav-item d-none d-md-block">
            <RouterLink to="/" class="nav-link">首頁</RouterLink>
          </li>
        </ul>

        <ul class="navbar-nav ms-auto">
          <li class="nav-item">
            <a class="nav-link" href="#" data-lte-toggle="fullscreen">
              全螢幕
            </a>
          </li>
        </ul>
      </div>
    </nav>

    <aside class="app-sidebar bg-body-secondary shadow" data-bs-theme="dark">
      <div class="sidebar-brand">
        <RouterLink to="/" class="brand-link text-decoration-none">
          <span class="brand-text fw-light">座位租借系統</span>
        </RouterLink>
      </div>

      <div class="sidebar-wrapper">
        <nav class="mt-2">
          <ul class="nav sidebar-menu flex-column" data-lte-toggle="treeview" role="menu" data-accordion="false">
            
            <li class="nav-header">管理選單</li>
            
            <li class="nav-item">
              <RouterLink to="/merchants" class="nav-link">
                <i class="nav-icon bi bi-shop"></i>
                <p>商家管理</p>
              </RouterLink>
            </li>

            <li class="nav-item">
              <RouterLink to="/discounts" class="nav-link">
                <i class="nav-icon bi bi-ticket-perforated"></i>
                <p>優惠券管理</p>
              </RouterLink>
            </li>

          </ul>
        </nav>
      </div>
    </aside>

    <main class="app-main py-3">
      <div class="app-content-header mb-4">
        <div class="container-fluid">
          <div class="row">
            <div class="col-sm-6">
              <h3 class="mb-0">管理後臺</h3>
            </div>
          </div>
        </div>
      </div>

      <div class="app-content">
        <div class="container-fluid">
          <RouterView />
        </div>
      </div>
    </main>

    <footer class="app-footer border-top py-3 text-center bg-white">
      <strong>Copyright &copy; 2025 SeatRent System.</strong>
      All rights reserved.
    </footer>

  </div>
</template>

<style>
/* 確保頁面背景填滿，避免出現白邊 */
html, body, #app {
  height: 100%;
  margin: 0;
}

.app-wrapper {
  min-height: 100vh;
}

/* 側邊欄樣式微調 */
.nav-link p {
  display: inline-block;
  margin-left: 10px;
  margin-bottom: 0;
}

.router-link-active {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff !important;
}
</style>