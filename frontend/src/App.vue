<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  // 加上這行，確保 AdminLTE 的 body class 正確
  document.body.classList.add('hold-transition', 'sidebar-mini', 'layout-fixed')
})
</script>

<template>
  <div class="wrapper">
    <nav class="main-header navbar navbar-expand navbar-white navbar-light">
      <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" data-widget="pushmenu" href="#" role="button"
            ><i class="fas fa-bars"></i
          ></a>
        </li>
      </ul>
    </nav>

    <aside class="main-sidebar sidebar-dark-primary elevation-4">
      <a href="#" class="brand-link">
        <span class="brand-text font-weight-light pl-3">維修管理系統</span>
      </a>

      <div class="sidebar">
        <nav class="mt-2">
          <ul
            class="nav nav-pills nav-sidebar flex-column"
            data-widget="treeview"
            role="menu"
            data-accordion="false"
          >
            <li class="nav-item">
              <router-link to="/staff-list" class="nav-link" active-class="active">
                <i class="nav-icon fas fa-user-cog"></i>
                <p>維護人員列表</p>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/mtif-list" class="nav-link" active-class="active">
                <i class="nav-icon fas fa-tools"></i>
                <p>維修工單列表</p>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link to="/mtif-history" class="nav-link" active-class="active">
                <i class="nav-icon fas fa-history"></i>
                <p>工單歷史紀錄</p>
              </router-link>
            </li>
          </ul>
        </nav>
      </div>
    </aside>

    <div class="content-wrapper">
      <router-view></router-view>
    </div>

    <footer class="main-footer">
      <strong>Copyright &copy; 2025.</strong> All rights reserved.
    </footer>
  </div>
</template>
