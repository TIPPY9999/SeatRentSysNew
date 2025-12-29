import { createRouter, createWebHistory } from 'vue-router'

/**
 * ==========================================
 * 1. 靜態導入元件 (Static Imports)
 * ==========================================
 */
import LoginView from '@/views/member/LoginView.vue'
import AdminLayout from '@/views/member/AdminLayout.vue' // 共用的後臺佈局外框
import AdminHomeView from '@/views/member/AdminHomeView.vue'
import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'

/**
 * ==========================================
 * 2. 懶加載導入 (Lazy Loading / Dynamic Imports)
 * ==========================================
 */
// [修正] 將導入路徑補全為 '@/views/merchantAndCoupon/'，以對應正確的檔案位置
const MerchantList = () => import('@/views/merchantAndCoupon/MerchantList.vue')
const DiscountList = () => import('@/views/merchantAndCoupon/DiscountList.vue')

const routes = [
  // --- 登入頁面：獨立路徑，不套用 Admin 佈局框架 ---
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },

  /**
   * --- 管理後臺嵌套路由 (Nested Routes) ---
   * 所有的 children 子路徑都會渲染在 AdminLayout 元件內的 <RouterView /> 位置。
   */
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      // 💡 管理後臺首頁：對應完整路徑為 /admin
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },

      // --- 商家與優惠券 (組員模組) ---
      {
        path: 'merchants',
        name: 'merchants',
        component: MerchantList,
      },
      {
        path: 'discounts',
        name: 'discounts',
        component: DiscountList,
      },

      // --- 會員管理模組 ---
      {
        path: 'members',
        name: 'member-list',
        component: MemberListView,
      },
      {
        path: 'members/edit/:id', // :id 為動態參數，用於獲取指定會員資料
        name: 'member-edit',
        component: MemberEditView,
      },
      {
        path: 'members/create',
        name: 'member-create',
        component: MemberCreateView,
      },
      {
        path: 'admins',
        name: 'admin-list',
        component: AdminHomeView, // 暫時指向首頁，待管理員管理頁面完成後替換
      },

      // --- 租借訂單管理 ---
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentManagement.vue'),
      },

      /**
       * --- 維護管理模組 (含維護員與項目管理) ---
       * 使用 Props 傳遞參數，讓同一個元件能根據模式顯示不同資料
       */
      {
        path: 'staff-list',
        name: 'staff-list',
        component: () => import('@/views/maintenance/MaintenanceStaffList.vue'),
      },
      {
        path: 'staff-form/:id?', // id? 結尾的問號代表此參數為「可選」，適用於新增或編輯
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
        props: { historyMode: false }, // 一般項目列表模式
      },
      {
        path: 'mtif-history',
        name: 'mtif-history',
        component: () => import('@/views/maintenance/MtifList.vue'),
        props: { historyMode: true }, // 歷史紀錄顯示模式
      },
      {
        path: 'mtif-form/:id?',
        name: 'mtif-form',
        component: () => import('@/views/maintenance/MtifForm.vue'),
      },
    ],
  },

  // 根路徑處理：一進首頁時，自動導向登入頁面
  {
    path: '/',
    redirect: '/login',
  },

  /**
   * 404 路由攔截 (Catch-all Route)
   * 當使用者輸入任何不存在的網址時，自動強制導回登入頁面，防止顯示空白頁面。
   */
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

/**
 * 初始化 Vue Router 實體
 * 採用 HTML5 History 模式 (createWebHistory)，讓 URL 保持乾淨 (無 # 號)。
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
