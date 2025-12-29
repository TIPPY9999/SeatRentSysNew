<script setup>
/**
 * AdminHomeView.vue：後台首頁入口
 * 已轉換為純 JavaScript 寫法
 */
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="home">
    <section class="content-header">
      <div class="container-fluid">
        <h1>後台管理首頁</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="cards">
          <div class="card" @click="router.push('/admin/members')">
            <h3>會員管理</h3>
            <p>管理與新增會員資料</p>
          </div>

          <div class="card" @click="router.push('/admin')">
            <h3>管理員管理</h3>
            <p>管理後台人員帳號</p>
          </div>

          <div class="card" @click="router.push('/admin/rec-rent')">
            <h3>租借紀錄</h3>
            <p>查看租借與歸還紀錄</p>
          </div>

          <div class="card" @click="router.push('/admin/staff-list')">
            <h3>維護人員管理</h3>
            <p>管理維護技術人員</p>
          </div>

          <div class="card" @click="router.push('/admin/mtif-list')">
            <h3>維修工單管理</h3>
            <p>追蹤維修進度與派工</p>
          </div>

          <!-- ✅ 新增：商家管理（搬進 cards） -->
          <div class="card" @click="router.push('/admin/merchants')">
            <h3>商家管理</h3>
            <p>管理商家資料與狀態</p>
          </div>

          <!-- ✅ 新增：優惠券管理（搬進 cards） -->
          <div class="card" @click="router.push('/admin/coupons')">
            <h3>優惠券管理</h3>
            <p>管理優惠券與活動</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home h2 {
  margin-bottom: 20px;
}

.cards {
  display: flex;
  flex-wrap: wrap; /* 讓卡片在小螢幕時自動換行 */
  gap: 20px;
}

.card {
  background: #fff;
  width: 220px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  user-select: none;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  background-color: #f8f9fa;
}

.card h3 {
  font-size: 1.2rem;
  color: #007bff;
  margin-bottom: 10px;
}

.card p {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0;
}
</style>
