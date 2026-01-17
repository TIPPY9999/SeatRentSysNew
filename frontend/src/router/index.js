import { createRouter, createWebHistory } from 'vue-router'
import Swal from 'sweetalert2'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * 核心頁面預先載入，提升首屏顯示速度
 * ==========================================
 */
import LoginView from '@/views/member/LoginView.vue' // 登入頁
import AdminLayout from '@/layouts/AdminLayout.vue' // 後臺主框架 (包含側邊欄與 Header)
import AdminHomeView from '@/views/member/AdminHomeView.vue' // 後臺首頁(儀表板)

import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'

import UserRentView from '@/views/rec/RecRentUserPage.vue'

import AdminListView from '@/views/member/AdminListView.vue'
import AdminCreateView from '@/views/member/AdminCreateView.vue'
import AdminEditView from '@/views/member/AdminEditView.vue'

import MemberLayout from '@/layouts/MemberLayout.vue'
import MemberProfileView from '@/views/member/MemberProfileView.vue'

/**
 * ==========================================
 * 2. 懶加載導入 (Dynamic Imports)
 * 進入該頁面時才下載程式碼，減輕初始載入負擔
 * ==========================================
 */
const MerchantList = () => import('@/views/merchantAndCoupon/MerchantList.vue')
const DiscountList = () => import('@/views/merchantAndCoupon/DiscountList.vue')

// 定義路由表
const routes = [
  // --- 登入頁面：獨立路徑，不套用 Admin 佈局框架 ---
  {
    path: '/login',
    component: LoginView,
    children: [],
  },

  // 使用者租借頁
  {
    path: '/rent',
    name: 'user-rent',
    component: UserRentView,
  },

  // 會員區（若你們有用到 MemberLayout）
  {
    path: '/member',
    component: MemberLayout,
    children: [
      {
        path: 'profile',
        component: MemberProfileView,
      },
    ],
  },

  /**
   * ==========================================
   * 3. 管理後臺嵌套路由 (Nested Routes)
   * 所有的 children 子路徑都會渲染在 AdminLayout 內的 <RouterView />
   * 網址前綴統一為 /admin
   * ==========================================
   */
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },

      // --- Spot ---
      {
        path: 'spot/list',
        name: 'spot-list',
        component: () => import('@/views/spot/SpotList.vue'),
      },
      {
        path: 'spot/add',
        name: 'spot-add',
        component: () => import('@/views/spot/SpotForm.vue'),
      },
      {
        path: 'spot/edit/:id',
        name: 'spot-edit',
        component: () => import('@/views/spot/SpotForm.vue'),
      },

      // --- Seat ---
      {
        path: 'seat/list',
        name: 'seat-list',
        component: () => import('@/views/spot/SeatList.vue'),
      },
      {
        path: 'seat/insert',
        name: 'seat-insert',
        component: () => import('@/views/spot/SeatInsert.vue'),
      },
      {
        path: 'seat/edit/:id',
        name: 'seat-edit',
        component: () => import('@/views/spot/SeatUpdate.vue'),
      },
      {
        path: 'seat/view/:id',
        name: 'seat-view',
        component: () => import('@/views/spot/SeatOne.vue'),
      },
      {
        path: 'seat/search',
        name: 'seat-search',
        component: () => import('@/views/spot/SeatSearch.vue'),
      },
      {
        path: 'seat/result',
        name: 'seat-result',
        component: () => import('@/views/spot/SeatResult.vue'),
      },

      // --- Merchant & Coupon ---
      {
        path: 'merchants',
        name: 'merchants',
        component: MerchantList,
      },
      {
        path: 'discounts',
        name: 'discounts',
        component: DiscountList,
        alias: 'coupons',
      },

      // --- Member ---
      {
        path: 'members',
        name: 'member-list',
        component: MemberListView,
      },
      {
        path: 'members/edit/:id',
        name: 'member-edit',
        component: MemberEditView,
      },
      {
        path: 'members/create',
        name: 'member-create',
        component: MemberCreateView,
      },

      // --- Admins ---
      {
        path: 'admins',
        name: 'admin-list',
        component: AdminListView,
      },
      {
        path: 'admins/create',
        name: 'admin-create',
        component: AdminCreateView,
      },
      {
        path: 'admins/edit/:id',
        name: 'admin-edit',
        component: AdminEditView,
      },

      // --- Rec ---
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentMgnPage.vue'),
      },

      // --- Maintenance ---
      {
        path: 'staff-list',
        name: 'staff-list',
        component: () => import('@/views/maintenance/MaintenanceStaffList.vue'),
      },
      {
        path: 'staff-form/:id?',
        name: 'staff-form',
        component: () => import('@/views/maintenance/MaintenanceStaffForm.vue'),
      },
      {
        path: 'staff-history',
        name: 'staff-history',
        component: () => import('@/views/maintenance/MaintenanceStaffHistory.vue'),
      },
      {
        path: 'mtif-list',
        name: 'mtif-list',
        component: () => import('@/views/maintenance/MtifList.vue'),
        props: { historyMode: false },
      },
      {
        path: 'mtif-history',
        name: 'mtif-history',
        component: () => import('@/views/maintenance/MtifList.vue'),
        props: { historyMode: true },
      },
      {
        path: 'mtif-form/:id?',
        name: 'mtif-form',
        component: () => import('@/views/maintenance/MtifForm.vue'),
      },

      // --- Schedule (排程) ---
      {
        path: 'maintenance/schedule',
        name: 'schedule-list',
        component: () => import('@/views/maintenance/ScheduleList.vue'),
      },
      {
        path: 'maintenance/schedule/create',
        name: 'schedule-create',
        component: () => import('@/views/maintenance/ScheduleForm.vue'),
      },
      {
        path: 'maintenance/schedule/edit/:id',
        name: 'schedule-edit',
        component: () => import('@/views/maintenance/ScheduleForm.vue'),
      },
    ],
  },

  // 根路徑：預設導向登入頁
  { path: '/', redirect: '/login' },

  // 404：導回登入頁
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

/**
 * ==========================================
 * 全域路由守衛：後台登入驗證（統一 localStorage）
 * ==========================================
 */
router.beforeEach((to, from, next) => {
  // 只保護後台
  if (!to.path.startsWith('/admin')) {
    next()
    return
  }

  const token = localStorage.getItem('token')
  const admin = localStorage.getItem('admin')

  if (token && admin) {
    next()
    return
  }

  Swal.fire({
    icon: 'warning',
    title: '請先登入',
    text: '您沒有權限訪問此頁面，請重新登入。',
    confirmButtonText: '去登入',
    allowOutsideClick: false,
  }).then(() => {
    next('/login')
  })
})

export default router
