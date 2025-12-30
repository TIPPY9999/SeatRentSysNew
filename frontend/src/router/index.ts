import { createRouter, createWebHistory } from 'vue-router'
<<<<<<< HEAD
import MerchantList from '@/views/MerchantList.vue'
import DiscountList from '@/views/DiscountList.vue'
=======

>>>>>>> c8ede75c0b930126d3de868f60d383e72b6de1b2
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
<<<<<<< HEAD
      path: '/',
      redirect: '/merchants' // 進來預設導向商家頁面
    },
    {
      path: '/merchants',
      name: 'merchants',
      component: MerchantList
    },
    {
      path: '/discounts',
      name: 'discounts',
      component: DiscountList
    }
  ],
})

export default router
=======
      path: '/tickets/history',
      component: () => import('../views/maintenance/TicketList.vue'),
      props: { historyMode: true },
    },
    { path: '/tickets/form/:id?', component: () => import('../views/maintenance/TicketForm.vue') },
  ],
})
export default router
>>>>>>> c8ede75c0b930126d3de868f60d383e72b6de1b2
