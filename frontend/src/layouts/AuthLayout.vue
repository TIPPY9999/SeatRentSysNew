<script setup>
</script>

<template>
  <div class="auth-page">
    <!-- 上方白色區塊 -->
    <header class="auth-header">
      <h1 class="logo">SeatRentSys</h1>
    </header>

    <!-- 下方灰色背景區：只負責置中，不再包白卡 -->
    <main class="auth-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 上方白色 header */
.auth-header {
  background: #ffffff;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 32px;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.05);
}

.logo {
  font-size: 20px;
  font-weight: 600;
}

/* 下方灰底 + 置中 */
.auth-main {
  flex: 1;
  background-color: #f2f3f5;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 從上開始 */
  padding: 40px 16px;
}
</style>