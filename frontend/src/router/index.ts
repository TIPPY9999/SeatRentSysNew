import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/staff-list' },
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
      props: { historyMode: false }, // 這裡要設定預設值為 false
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
  ],
})

export default router
