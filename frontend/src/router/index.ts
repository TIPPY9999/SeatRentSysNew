import { createRouter, createWebHistory } from 'vue-router'
import MerchantList from '@/views/MerchantList.vue'
import DiscountList from '@/views/DiscountList.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
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