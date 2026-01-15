import { createRouter, createWebHistory } from 'vue-router'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * 核心頁面預先載入，提升首屏顯示速度
 * ==========================================
 */
import LoginView from '@/views/member/LoginView.vue' // 登入頁
import AdminLayout from '@/views/member/AdminLayout.vue' // 後臺主框架 (包含側邊欄與 Header)
import AdminHomeView from '@/views/member/AdminHomeView.vue' // 後臺首頁(儀表板)
import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'
import AdminListView from '@/views/member/AdminListView.vue'
import AdminCreateView from '@/views/member/AdminCreateView.vue'
import AdminEditView from '@/views/member/AdminEditView.vue'
import MemberLayout from '@/views/member/MemberLayout.vue'
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
    name: 'login',
    component: LoginView,
  },

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
   * 所有的 children 子路徑都會渲染在 AdminLayout 內的 <RouterView /> 位置
   * 網址前綴統一為 /admin
   * ==========================================
   */
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      // 💡 管理後臺首頁 (儀表板)
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },

      // ==========================================
      // [整合] 據點管理模組 (Spot) - 來自組員 spot 分支
      // ==========================================
      {
        path: 'spot/list', // 網址: /admin/spot/list
        name: 'spot-list',
        component: () => import('@/views/spot/SpotList.vue'),
      },
      {
        path: 'spot/add', // 網址: /admin/spot/add
        name: 'spot-add',
        component: () => import('@/views/spot/SpotForm.vue'),
      },
      {
        path: 'spot/edit/:id', // 網址: /admin/spot/edit/1
        name: 'spot-edit',
        component: () => import('@/views/spot/SpotForm.vue'),
      },

      // ==========================================
      // [整合] 座位管理模組 (Seat) - 來自組員 TXT 檔案
      // ==========================================
      {
        path: 'seat/list', // 網址: /admin/seat/list
        name: 'seat-list',
        component: () => import('@/views/spot/SeatList.vue'),
      },
      {
        path: 'seat/insert', // 網址: /admin/seat/insert
        name: 'seat-insert',
        component: () => import('@/views/spot/SeatInsert.vue'),
      },
      {
        path: 'seat/edit/:id', // 網址: /admin/seat/edit/5
        name: 'seat-edit',
        component: () => import('@/views/spot/SeatUpdate.vue'),
      },
      {
        path: 'seat/view/:id', // 網址: /admin/seat/view/5 (詳細資料)
        name: 'seat-view',
        component: () => import('@/views/spot/SeatOne.vue'),
      },
      {
        path: 'seat/search', // 網址: /admin/seat/search (條件查詢頁)
        name: 'seat-search',
        component: () => import('@/views/spot/SeatSearch.vue'),
      },
      {
        path: 'seat/result', // 網址: /admin/seat/result (查詢結果頁)
        name: 'seat-result',
        component: () => import('@/views/spot/SeatResult.vue'),
      },

      // ==========================================
      // [既有功能] 商家與優惠券管理
      // ==========================================
      {
        path: 'merchants',
        name: 'merchants',
        component: MerchantList,
      },
      {
        path: 'discounts',
        name: 'discounts',
        component: DiscountList,
        alias: 'coupons', // 設定別名，訪問 /admin/coupons 也會通
      },

      // ==========================================
      // [既有功能] 會員管理模組
      // ==========================================
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
      // 管理員列表 (暫時指向首頁，未來可建立獨立頁面)
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

      // ==========================================
      // [既有功能] 租借訂單管理 (Rec)
      // ==========================================
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentManagement.vue'),
      },

      // ==========================================
      // [既有功能] 維修管理模組 (Maintenance)
      // ==========================================
      {
        path: 'staff-list', // 維修人員列表
        name: 'staff-list',
        component: () => import('@/views/maintenance/MaintenanceStaffList.vue'),
      },
      {
        path: 'staff-form/:id?', // 維修人員表單 (id? 代表可選，用於新增或編輯)
        name: 'staff-form',
        component: () => import('@/views/maintenance/MaintenanceStaffForm.vue'),
      },
      {
        path: 'staff-history', // 維修人員履歷
        name: 'staff-history',
        component: () => import('@/views/maintenance/MaintenanceStaffHistory.vue'),
      },
      {
        path: 'mtif-list', // 維修項目列表 (一般模式)
        name: 'mtif-list',
        component: () => import('@/views/maintenance/MtifList.vue'),
        props: { historyMode: false },
      },
      {
        path: 'mtif-history', // 維修項目列表 (歷史紀錄模式)
        name: 'mtif-history',
        component: () => import('@/views/maintenance/MtifList.vue'),
        props: { historyMode: true },
      },
      {
        path: 'mtif-form/:id?', // 維修項目表單
        name: 'mtif-form',
        component: () => import('@/views/maintenance/MtifForm.vue'),
      },
    ],
  },

  // ==========================================
  // 4. 路由守衛與轉址 (Redirects)
  // ==========================================

  // 根路徑：預設導向登入頁
  {
    path: '/',
    redirect: '/login',
  },

  // 404 處理：捕捉所有未定義路由，強制導回登入頁
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

/**
 * 初始化 Vue Router 實體
 * 使用 HTML5 History 模式 (無 # 字號)
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
