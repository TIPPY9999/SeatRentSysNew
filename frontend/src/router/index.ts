import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/staff' },
    // 維護人員相關
    { path: '/staff', component: () => import('../views/maintenance/StaffList.vue') },
    { path: '/staff/form/:id?', component: () => import('../views/maintenance/StaffForm.vue') },
    { path: '/staff/history', component: () => import('../views/maintenance/StaffHistory.vue') },
    // 維修工單相關
    { path: '/tickets', component: () => import('../views/maintenance/TicketList.vue') },
    {
      path: '/tickets/history',
      component: () => import('../views/maintenance/TicketList.vue'),
      props: { historyMode: true },
    },
    { path: '/tickets/form/:id?', component: () => import('../views/maintenance/TicketForm.vue') },
  ],
})
export default router
