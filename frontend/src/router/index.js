import { createRouter, createWebHistory } from 'vue-router'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * ==========================================
 */
import LoginView from '@/views/member/LoginView.vue'
import AdminLayout from '@/views/member/AdminLayout.vue'
import AdminHomeView from '@/views/member/AdminHomeView.vue'
import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'
import PaymentView from '@/views/ecpay/PaymentView.vue'

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

      // [功能] 租借訂單管理
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentManagement.vue'),
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
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
