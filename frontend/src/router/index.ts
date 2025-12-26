import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import LoginView from '@/views/member/LoginView.vue'
import AdminLayout from '@/views/member/AdminLayout.vue'
import AdminHomeView from '@/views/member/AdminHomeView.vue'
import MemberListView from '@/views/member/MemberListView.vue'
import MemberEditView from '@/views/member/MemberEditView.vue'
import MemberCreateView from '@/views/member/MemberCreateView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/admin',
    component: AdminLayout, // 組員的佈局框架
    children: [
      {
        path: '',
        name: 'admin-home',
        component: AdminHomeView,
      },
      // --- 會員管理 ---
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
      // --- 管理員管理 (補上這塊，確保側邊欄不失效) ---
      {
        path: 'admins',
        name: 'admin-list',
        component: () => import('@/views/member/AdminHomeView.vue'), // 暫時指向首頁，之後改為你的管理員元件
      },
      // --- 租借訂單管理 ---
      // 這裡 path 改成 'rec-rent'，請確保 AdminLayout.vue 也要同步改為 go('/admin/rec-rent')
      {
        path: 'rec-rent',
        name: 'rec-rent',
        component: () => import('@/views/rec/RecRentManagement.vue'),
      },
      // --- 維護管理模組 ---
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
    ],
  },
  {
    path: '/',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
