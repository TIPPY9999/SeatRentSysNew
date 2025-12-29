import { createRouter, createWebHistory } from 'vue-router'
// 如果你們還要保留原本的 HomeView，就留著這行；如果不想要了，可以連檔案帶這行一起刪掉
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView, // 讓根目錄指向首頁
    },
    {
      path: '/spot',
      children: [
        {
          path: 'list',
          name: 'spot-list',
          component: () => import('../views/spot/SpotList.vue'),
        },
        {
          path: 'add',
          name: 'spot-add',
          component: () => import('../views/spot/SpotForm.vue'),
        },
        {
          path: 'edit/:id',
          name: 'spot-edit',
          component: () => import('../views/spot/SpotForm.vue'),
        },
      ],
    },
    // 如果不需要 About 頁面，下面這個可以刪掉
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
