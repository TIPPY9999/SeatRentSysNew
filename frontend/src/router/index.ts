import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 1. 引入組員的頁面元件 (@ 是 Vite 的 src 路徑別名)
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/views/AdminLayout.vue'
import AdminHomeView from '@/views/AdminHomeView.vue'
import MemberListView from '@/views/MemberListView.vue'
import MemberEditView from '@/views/MemberEditView.vue'
import MemberCreateView from '@/views/MemberCreateView.vue'

// 2. 定義所有的路由規則
const routes: RouteRecordRaw[] = [
  // --- 組員的會員與登入模組 ---
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },
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
    ],
  },

  // --- 你的維護管理模組 ---
  {
    path: '/staff-list',
    name: 'staff-list',
    component: () => import('../views/maintenance/MaintenanceStaffList.vue'),
  },
  {
    path: '/staff-form/:id?',
    name: 'staff-form',
    component: () => import('../views/maintenance/MaintenanceStaffForm.vue'),
  },
  {
    path: '/staff-history',
    name: 'staff-history',
    component: () => import('../views/maintenance/MaintenanceStaffHistory.vue'),
  },
  {
    path: '/mtif-list',
    name: 'mtif-list',
    component: () => import('../views/maintenance/MtifList.vue'),
    props: { historyMode: false },
  },
  {
    path: '/mtif-history',
    name: 'mtif-history',
    component: () => import('../views/maintenance/MtifList.vue'),
    props: { historyMode: true },
  },
  {
    path: '/mtif-form/:id?',
    name: 'mtif-form',
    component: () => import('../views/maintenance/MtifForm.vue'),
  },

  // --- 全域導向 ---
  {
    path: '/',
    redirect: '/login', // 這裡統一先導向登入頁，這是最符合專案邏輯的
  },
]

// 3. 建立路由實體
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
