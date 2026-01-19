<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

const logout = async () => {
  try {
    // 只有按「登出」才會 resolve
    await ElMessageBox.confirm(
      '確定要登出嗎？',
      '登出確認',
      {
        confirmButtonText: '登出',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 使用者真的確認後，才跳轉
    localStorage.removeItem('token')
    router.push('/login')
  } catch (err) {
    // 使用者按取消，什麼都不做
  }
}

const go = (path) => {
  router.push(path)
}
</script>

<template>
  <el-container class="layout">
    <!-- Header -->
    <el-header class="header">
      <div class="logo">SeatRentSys</div>
    </el-header>

    <el-container>
      <!-- Sidebar -->
      <el-aside class="aside" width="220px">
        <el-menu class="menu">
          <el-menu-item index="profile" @click="go('/member/profile')">
            個人資料
          </el-menu-item>

          <div class="divider"></div>

          <!-- 不要 index + 不要 router -->
          <el-menu-item index="logout" class="logout" @click="logout">
            登出
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- Content -->
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  background: #f8f9fa;
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
}

.aside {
  background: #f7f8fa;
  border-right: 1px solid #e5e7eb;
  padding: 12px 10px;
}

.menu {
  border-right: none;
  background: transparent;
}

.divider {
  height: 1px;
  background: #e5e7eb;
  margin: 12px 8px;
}

.main {
  padding: 30px;
}

/* 登出樣式：Element Plus menu-item 需要用 class 覆蓋 */
:deep(.logout) {
  color: #b91c1c;
}
:deep(.logout:hover) {
  background: #fef2f2 !important;
}
</style>