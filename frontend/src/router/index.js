import { createRouter, createWebHistory } from 'vue-router'
import Swal from 'sweetalert2'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * ==========================================
 */
import LoginView from '@/views/member/LoginView.vue' // 登入頁
import AdminLayout from '@/layouts/AdminLayout.vue' // 後臺主框架 (包含側邊欄與 Header)
import AdminHomeView from '@/views/member/AdminHomeView.vue' // 後臺首頁(儀表板)

import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'
import PaymentView from '@/views/ecpay/PaymentView.vue'

import AdminListView from '@/views/member/AdminListView.vue'
import AdminCreateView from '@/views/member/AdminCreateView.vue'
import AdminEditView from '@/views/member/AdminEditView.vue'

import MemberLayout from '@/layouts/MemberLayout.vue'
import MemberProfileView from '@/views/member/MemberProfileView.vue'

import AuthLayout from '@/layouts/AuthLayout.vue'

/**
 * ==========================================
 * 2. 懶加載導入 (Dynamic Imports)
 * ==========================================
 */
const MerchantList = () => import('@/views/merchantAndCoupon/MerchantList.vue')
const DiscountList = () => import('@/views/merchantAndCoupon/DiscountList.vue')
const RedemptionLogList = () => import('@/views/merchantAndCoupon/RedemptionLogList.vue') // 新增：後台紀錄
const CouponMall = () => import('@/views/merchantAndCoupon/CouponMallView.vue') // 新增：前台商城
const SnakeGame = () => import('@/views/game/SnakeGame.vue')

// 定義路由表
const routes = [
  // --- 登入頁面 ---
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },

  // ==========================================
  // [新增] 前台使用者頁面 (不套用後台側邊欄)
  // ==========================================
  {
    path: '/mall',
    name: 'coupon-mall',
    component: CouponMall,
  },
  {
    path: '/snake', // 方便用戶記憶的短網址
    name: 'user-snake-game',
    component: SnakeGame,
  },

  // 註冊會員
  {
    path: '/register',
    component: AuthLayout,
    children: [
      {
        path: '',
        component: () => import('@/views/member/Register.vue'),
      },
    ],
  },

  // 會員頁面
  {
    path: '/member',
    component: MemberLayout,
    children: [
      {
        path: 'profile',
        name: 'member-profile',
        component: MemberProfileView,
      },
    ],
  },

  /**
   * ==========================================
   * 3. 管理後臺嵌套路由 (Nested Routes)
   * 網址前綴統一為 /admin
   * ==========================================
   */
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      // 管理後臺首頁
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },

      // [整合] 據點與座位管理 (Spot & Seat)
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

      // [核心功能] 商家與優惠券管理
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
      // --- 新增：兌換紀錄報表 ---
      {
        path: 'redemption-logs',
        name: 'admin-redemption-logs',
        component: RedemptionLogList,
      },

      // [功能] 金流與遊戲 (後台也可以進入遊戲)
      {
        path: 'payment',
        name: 'Payment',
        component: PaymentView,
      },
      {
        path: 'snake-game',
        name: 'admin-snake-game',
        component: SnakeGame,
      },

      // [核心功能] 會員管理
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

      // [核心功能] 管理員管理
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

      // [功能] 租借訂單管理
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentMgnPage.vue'),
      },
      {
        path: 'rent/:action?',
        name: 'rec-rent-user',
        component: () => import('@/views/rec/RecRentUserPage.vue'),
      },

      // [功能] 維修管理 (Maintenance)
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

  // ==========================================
  // 4. 路由守衛與轉址 (Redirects)
  // ==========================================
  {
    path: '/',
    name: 'entrance',
    component: () => import('@/views/EnterancePage.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
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
  })
  next('/login')
})

export default router
