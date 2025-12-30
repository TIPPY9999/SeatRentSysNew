<<<<<<< HEAD
<<<<<<< HEAD
<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
=======
<script setup>
/**
 * App.vue：整個前端的「根容器」
 * [關鍵修正] 這裡只留 RouterView，確保登入頁面是乾淨的。
 * 後台的側邊欄與導覽列，已經全部搬家到 AdminLayout.vue 了，這裡不需要留。
 */
import { RouterView } from 'vue-router'
>>>>>>> fb459a06d9e960dae0f1e03cf5a6da04327101c6
</script>

<template>
  <RouterView />
</template>

<style>
/* * 這裡只留全域通用的設定
 * AdminLTE 的具體樣式已經移交給 index.html 和 AdminLayout 處理
 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css');

html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Noto Sans TC', 'PingFang TC', 'Microsoft JhengHei', sans-serif;
}

/* 這是 Vue Router 自動加上的 class，用來做選單高亮 */
.router-link-active {
  background-color: #007bff !important;
  color: #fff !important;
}
</style>
<<<<<<< HEAD
=======
<template>
  <RouterView />
</template>
>>>>>>> c8ede75c0b930126d3de868f60d383e72b6de1b2
=======
>>>>>>> fb459a06d9e960dae0f1e03cf5a6da04327101c6
