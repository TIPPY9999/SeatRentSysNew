<script setup>
/**
 * AdminHomeView.vue：後台首頁入口
 * 已新增據點管理 (Spot) 功能圖卡
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

          <div class="card" @click="router.push('/admin/admins')">
            <h3>管理員管理</h3>
            <p>管理後台人員帳號</p>
          </div>

          <div class="card spot-card" @click="router.push('/admin/spot/list')">
            <h3>據點管理</h3>
            <p>管理各區域據點與座位分布</p>
          </div>

          <div class="card seat-card" @click="router.push('/admin/seat/list')">
            <h3>座位管理</h3>
            <p>管理座位狀態與配置</p>
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

          <div class="card" @click="router.push('/admin/merchants')">
            <h3>商家管理</h3>
            <p>管理商家資料與狀態</p>
          </div>

          <div class="card" @click="router.push('/admin/coupons')">
            <h3>優惠券管理</h3>
            <p>管理優惠券與活動</p>
          </div>
          <div class="card" @click="router.push('/admin/payment')">
            <h3>金流管理</h3>
            <p>管理金流相關設定</p>
          </div>
           <div class="card" @click="router.push('/admin/snake-game')">
            <h3>小遊戲</h3>
            <p>貪吃蛇</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ✅ 優化：增強標題樣式 */
.content-header h1 {
  color: #1a1d2e;
  font-weight: 700;
  font-size: 28px;
  margin-bottom: 8px;
}

.home h2 {
  margin-bottom: 20px;
}

.cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

/* ✅ 優化：增強卡片視覺效果 */
.card {
  background: white;
  width: 220px;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* ✅ 新增：漸層裝飾條 */
.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover::before {
  opacity: 1;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1d2e;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.card:hover h3 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 據點管理卡片特殊樣式 */
.spot-card h3 {
  color: #10b981;
}

.spot-card:hover h3 {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 座位管理卡片特殊樣式 */
.seat-card h3 {
  color: #f59e0b;
}

.seat-card:hover h3 {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card p {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 0;
  line-height: 1.5;
}

/* ✅ 響應式設計 */
@media (max-width: 768px) {
  .card {
    width: 100%;
  }
}
</style>
