<script setup>
/**
 * AdminLayout.vue：AdminLTE 3 後台版型
 * [修正] 移除 lang="ts" 與型別標註，轉換為純 JS 寫法
 */
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute, RouterLink, RouterView } from 'vue-router'

const router = useRouter()
const route = useRoute()

// AdminLTE 3 必備的 Body Class
const bodyClasses = ['hold-transition', 'sidebar-mini', 'layout-fixed']

onMounted(() => {
  document.body.classList.add(...bodyClasses)

  // [修正] JS 環境中不需要斷言為 any
  const win = window
  if (win.$ && win.$.AdminLTE) {
    win.$(document).trigger('adminlte.layout.activate')
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
 * 登出功能
 */
const logout = () => {
  if (confirm('確定要登出嗎？')) {
    router.push('/login')
  }
}
</script>

<template>
  <div class="wrapper">
    <nav class="main-header navbar navbar-expand navbar-white navbar-light border-bottom">
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

    <aside class="main-sidebar sidebar-dark-primary elevation-4">
      <RouterLink to="/admin" class="brand-link">
        <span class="brand-text font-weight-light">SeatRentSys 管理系統</span>
      </RouterLink>

      <div class="sidebar">
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
          </ul>
        </nav>
      </div>
    </aside>

    <div class="content-wrapper">
      <section class="content py-3">
        <div class="container-fluid">
          <RouterView />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.nav-link p {
  margin-left: 8px;
}
.nav-link.active,
.router-link-active {
  background-color: #007bff !important;
  color: #fff !important;
}
</style>
