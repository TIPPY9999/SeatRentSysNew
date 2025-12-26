import { createRouter, createWebHistory } from 'vue-router'
import SpotList from '../views/spot/SpotList.vue'
import SpotForm from '../views/spot/SpotForm.vue'

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
      component: SpotList
    },
    {
      path: '/spot/add',
      name: 'spot-add',
      component: SpotForm
    },
    {
      path: '/spot/edit/:id',
      name: 'spot-edit',
      component: SpotForm
    }
  ]
})

export default router