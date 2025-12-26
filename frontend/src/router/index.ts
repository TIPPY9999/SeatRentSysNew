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
      // --- 你的維護管理模組 (整合進 /admin 之下) ---
      {
        path: 'staff-list',
        name: 'staff-list',
        component: () => import('../views/maintenance/MaintenanceStaffList.vue'),
      },
      {
        path: 'staff-form/:id?',
        name: 'staff-form',
        component: () => import('../views/maintenance/MaintenanceStaffForm.vue'),
      },
      {
        path: 'staff-history',
        name: 'staff-history',
        component: () => import('../views/maintenance/MaintenanceStaffHistory.vue'),
      },
      {
        path: 'mtif-list',
        name: 'mtif-list',
        component: () => import('../views/maintenance/MtifList.vue'),
        props: { historyMode: false },
      },
      {
        path: 'mtif-history',
        name: 'mtif-history',
        component: () => import('../views/maintenance/MtifList.vue'),
        props: { historyMode: true },
      },
      {
        path: 'mtif-form/:id?',
        name: 'mtif-form',
        component: () => import('../views/maintenance/MtifForm.vue'),
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
