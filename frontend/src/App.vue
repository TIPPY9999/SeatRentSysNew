<script setup>
import { onMounted } from 'vue';
import { RouterView } from 'vue-router';
import { useMemberAuthStore } from '@/stores/memberAuth';
import { useAdminAuthStore } from '@/stores/adminAuth';

/**
 * App.vue：整個前端的「根容器」
 *
 * [核心邏輯] 應用程式狀態恢復 (Rehydration)
 * 此處的 onMounted 會在整個應用程式啟動時執行一次。
 * 它的任務是檢查 localStorage 中是否儲存了上次的登入資訊，
 * 如果有，就將其恢復到 Pinia store 中。
 * 這可以確保用戶在重新整理頁面後，登入狀態得以保持。
 */
onMounted(() => {
  const memberAuthStore = useMemberAuthStore();
  const adminAuthStore = useAdminAuthStore();

  // 1. 恢復一般會員的登入狀態
  if (!memberAuthStore.isLogin) {
    const storedUser = localStorage.getItem('member_user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        memberAuthStore.setMemberLogin(userData);
        console.log('App.vue: 會員登入狀態已從 localStorage 恢復。');
      } catch (e) {
        console.error('恢復會員狀態失敗:', e);
        // 如果解析失敗，清除損壞的資料
        localStorage.removeItem('member_user');
      }
    }
  }

  // 2. 恢復管理員的登入狀態
  if (!adminAuthStore.isLogin) {
    const storedAdmin = localStorage.getItem('admin');
    if (storedAdmin) {
      try {
        const adminData = JSON.parse(storedAdmin);
        adminAuthStore.setAdmin(adminData);
        console.log('App.vue: 管理員登入狀態已從 localStorage 恢復。');
      } catch (e) {
        console.error('恢復管理員狀態失敗:', e);
        localStorage.removeItem('admin');
      }
    }
  }
});
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