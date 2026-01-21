<template>
  <div class="container mt-4">
    <div class="card">
      <div class="card-header">
        <h4>會員ID 持久化測試頁面</h4>
      </div>
      <div class="card-body">
        <p>此頁面用於測試能否從 Pinia store 中成功獲取已登入的會員資料。</p>
        
        <div v-if="isLoggedIn" class="alert alert-success">
          <h5 class="alert-heading">驗證成功</h5>
          <p>已成功從 Pinia store 中讀取到會員資料。</p>
          <hr>
          <p class="mb-0">
            會員ID (Member ID): <strong>{{ memberId }}</strong>
          </p>
          <p class="mb-0">
            會員帳號 (Username): <strong>{{ memberUsername }}</strong>
          </p>
          <pre class="mt-2" style="background-color: #f0f0f0; padding: 10px; border-radius: 4px;"><code>{{ fullMemberObject }}</code></pre>
        </div>
        
        <div v-else class="alert alert-warning">
          <h5 class="alert-heading">驗證失敗</h5>
          <p>無法從 Pinia store 中讀取到會員資料。</p>
          <p>這可能是因為您尚未登入，或是瀏覽器被重新整理且應用程式沒有將 localStorage 的資料恢復回 Pinia store。</p>
          <router-link to="/member/login" class="btn btn-primary">前往登入頁面</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

// 1. 引入並實例化 auth store
const authStore = useAuthStore();

// 2. 建立 computed 屬性來安全地獲取資料
//    使用 computed 可以讓資料保持響應性
const isLoggedIn = computed(() => authStore.isLogin && authStore.user);
const memberId = computed(() => authStore.user?.memId || 'N/A'); // 假設會員ID的欄位是 memId
const memberUsername = computed(() => authStore.user?.memUsername || 'N/A');
const fullMemberObject = computed(() => JSON.stringify(authStore.user, null, 2));


// (可選) 應用程式初始化邏輯：
// 理想情況下，這段邏輯應該放在 App.vue 的 onMounted 中，確保應用程式一啟動就執行。
// 這裡放一份是為了確保此測試頁面在單獨測試時也能運作。
onMounted(() => {
  // 如果 Pinia store 是空的，但 localStorage 有資料，則進行"恢復"
  if (!authStore.isLogin) {
    const storedUser = localStorage.getItem('member_user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        // 呼叫 action 來恢復狀態，而不是直接修改 state
        authStore.login(userData, 'member'); 
        console.log('從 localStorage 恢復會員資料到 Pinia store。');
      } catch (e) {
        console.error('從 localStorage 讀取會員資料失敗:', e);
        localStorage.removeItem('member_user');
      }
    }
  }
});
</script>

<style scoped>
.container {
  max-width: 800px;
}
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>