import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/spot/list'
    },
    {
      path: '/spot/list',
      name: 'spot-list',
      component: () => import('../views/spot/SpotList.vue')
    },
    {
      path: '/spot/add',
      name: 'spot-add',
      component: () => import('../views/spot/SpotForm.vue')
    },
    {
      path: '/spot/edit/:id',
      name: 'spot-edit',
      component: () => import('../views/spot/SpotForm.vue')
    }
  ]
})

export default router