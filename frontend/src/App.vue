<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  // 這裡維持基本的 Body 類別即可，如果沒用到 AdminLTE 套件，這行也可以刪除
  document.body.classList.add('hold-transition', 'sidebar-mini', 'layout-fixed')
})
</script>

<template>
  <router-view></router-view>
</template>

<style>
/* 移除原本報錯的 @import 'admin-lte/...' */

/* 如果你的圖示 (Icons) 沒有顯示，可以在這裡引入 FontAwesome 的 CDN */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

/* 全域基礎樣式，確保頁面不會有奇怪的邊距 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'PingFang TC', 'Microsoft JhengHei', sans-serif;
}
</style>
