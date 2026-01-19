import { createRouter, createWebHistory } from 'vue-router'
import Swal from 'sweetalert2'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * ==========================================
 */
<<<<<<< HEAD
import LoginView from '@/views/member/LoginView.vue'
import AdminLayout from '@/views/member/AdminLayout.vue'
import AdminHomeView from '@/views/member/AdminHomeView.vue'
=======
import LoginView from '@/views/member/LoginView.vue' // 登入頁
import AdminLayout from '@/layouts/AdminLayout.vue' // 後臺主框架 (包含側邊欄與 Header)
import AdminHomeView from '@/views/member/AdminHomeView.vue' // 後臺首頁(儀表板)

>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'
import PaymentView from '@/views/ecpay/PaymentView.vue'

import UserRentView from '@/views/rec/RecRentUserPage.vue'

import AdminListView from '@/views/member/AdminListView.vue'
import AdminCreateView from '@/views/member/AdminCreateView.vue'
import AdminEditView from '@/views/member/AdminEditView.vue'

import MemberLayout from '@/layouts/MemberLayout.vue'
import MemberProfileView from '@/views/member/MemberProfileView.vue'

/**
 * ==========================================
 * 2. 懶加載導入 (Dynamic Imports)
 * ==========================================
 */
const MerchantList = () => import('@/views/merchantAndCoupon/MerchantList.vue')
const DiscountList = () => import('@/views/merchantAndCoupon/DiscountList.vue')
<<<<<<< HEAD
const RedemptionLogList = () => import('@/views/merchantAndCoupon/RedemptionLogList.vue') // 新增：後台紀錄
const CouponMall = () => import('@/views/merchantAndCoupon/CouponMallView.vue') // 新增：前台商城
const SnakeGame = () => import('@/views/game/SnakeGame.vue')

// 定義路由表
const routes = [
  // --- 登入頁面 ---
=======

// 定義路由表
const routes = [
  // --- 登入頁面：獨立路徑，不套用 Admin 佈局框架 ---
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
  {
    path: '/login',
    name: 'login',
    component: LoginView,
<<<<<<< HEAD
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
=======
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
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
  },

  /**
   * ==========================================
   * 3. 管理後臺嵌套路由 (Nested Routes)
<<<<<<< HEAD
=======
   * 所有的 children 子路徑都會渲染在 AdminLayout 內的 <RouterView />
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
   * 網址前綴統一為 /admin
   * ==========================================
   */
  {
    path: '/admin',
    component: AdminLayout,
    children: [
<<<<<<< HEAD
      // 管理後臺首頁
=======
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },

<<<<<<< HEAD
      // [整合] 據點與座位管理 (Spot & Seat)
=======
      // --- Spot ---
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD
=======

      // --- Seat ---
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD

      // [核心功能] 商家與優惠券管理
=======
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
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD
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
=======
      },

      // --- Member ---
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD

      // [功能] 租借訂單管理
=======

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
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentMgnPage.vue'),
      },

<<<<<<< HEAD
      // [功能] 維修管理 (Maintenance)
=======
      // --- Maintenance ---
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD
=======
        path: 'staff-history',
        name: 'staff-history',
        component: () => import('@/views/maintenance/MaintenanceStaffHistory.vue'),
      },
      {
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
<<<<<<< HEAD
    ],
  },

  // ==========================================
  // 4. 路由守衛與轉址 (Redirects)
  // ==========================================
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
=======
      {
        path: 'mtif-form/:id?',
        name: 'mtif-form',
        component: () => import('@/views/maintenance/MtifForm.vue'),
      },
    ],
  },

  // 根路徑：預設導向登入頁
  { path: '/', redirect: '/login' },

  // 404：導回登入頁
  { path: '/:pathMatch(.*)*', redirect: '/login' },
>>>>>>> 5f5b7ccb5a1adb7a59306eca17992fe8add2de40
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
