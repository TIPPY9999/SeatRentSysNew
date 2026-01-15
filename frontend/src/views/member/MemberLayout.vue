<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const logout = () => {
  if (confirm('確定要登出嗎？')) {
    router.push('/login')
  }
}
</script>

<template>
  <div class="member-wrapper">
    <!-- 上方 Header -->
    <header class="member-header">
      <div class="logo">SeatRentSys</div>
    </header>

    <div class="member-body">
      <!-- 左側選單 -->
      <aside class="member-sidebar">

        <ul class="menu">
          <li class="active">個人資料</li>
          <li class="logout-item" @click="logout">登出</li>
        </ul>

      </aside>

      <!-- 右側內容 -->
      <main class="member-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.member-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #ddd;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 20px;
  font-weight: bold;
}

.member-body {
  display: flex;
  height: calc(100vh - 60px);
}

.member-sidebar {
  width: 220px;
  background-color: #f7f8fa;
  border-right: 1px solid #ddd;
  padding: 20px 15px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background-color: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 10px;
}

.username {
  font-weight: bold;
  font-size: 16px;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
  flex-grow: 1;
}

.menu li {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #333;
  margin-bottom: 6px;
}

.menu li.active,
.menu li:not(.logout-item):hover {
  background-color: #e5edff;
  color: #1d4ed8;
}

/* 登出專用 */
.logout-item {
  margin-top: 12px;
  color: #666;
}

.logout-item:hover {
  background-color: #fef2f2;
  color: #b91c1c;
}

.member-content {
  flex: 1;
  padding: 30px;
  background: #f8f9fa;
}
</style>