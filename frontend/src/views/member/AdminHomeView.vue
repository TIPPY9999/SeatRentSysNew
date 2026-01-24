<script setup>
/**
 * AdminHomeView.vue：後台首頁入口
 * 已新增據點管理 (Spot) 功能圖卡
 */
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="admin-home">
    <!-- 頁面標題 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <i class="fas fa-tachometer-alt"></i>
        </div>
        <div class="header-text">
          <h1>後台管理首頁</h1>
          <p>歡迎使用座位租借系統管理後台</p>
        </div>
      </div>
    </div>

    <!-- 功能卡片區域 -->
    <div class="cards-container">
      <!-- 會員管理 -->
      <div class="feature-card member" @click="router.push('/admin/members')">
        <div class="card-icon">
          <i class="fas fa-users"></i>
        </div>
        <div class="card-content">
          <h3>會員管理</h3>
          <p>管理與新增會員資料</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 管理員管理 -->
      <div class="feature-card admin" @click="router.push('/admin/admins')">
        <div class="card-icon">
          <i class="fas fa-user-shield"></i>
        </div>
        <div class="card-content">
          <h3>管理員管理</h3>
          <p>管理後台人員帳號</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 據點管理 -->
      <div class="feature-card spot" @click="router.push('/admin/spot/list')">
        <div class="card-icon">
          <i class="fas fa-map-marker-alt"></i>
        </div>
        <div class="card-content">
          <h3>據點管理</h3>
          <p>管理各區域據點與座位分布</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 座位管理 -->
      <div class="feature-card seat" @click="router.push('/admin/seat/list')">
        <div class="card-icon">
          <i class="fas fa-chair"></i>
        </div>
        <div class="card-content">
          <h3>座位管理</h3>
          <p>管理座位狀態與配置</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 租借紀錄 -->
      <div class="feature-card rent" @click="router.push('/admin/rec-rent')">
        <div class="card-icon">
          <i class="fas fa-clipboard-list"></i>
        </div>
        <div class="card-content">
          <h3>租借紀錄</h3>
          <p>查看租借與歸還紀錄</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 維護人員管理 -->
      <div class="feature-card staff" @click="router.push('/admin/staff-list')">
        <div class="card-icon">
          <i class="fas fa-user-cog"></i>
        </div>
        <div class="card-content">
          <h3>維護人員管理</h3>
          <p>管理維護技術人員</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 維修工單管理 -->
      <div class="feature-card repair" @click="router.push('/admin/mtif-list')">
        <div class="card-icon">
          <i class="fas fa-tools"></i>
        </div>
        <div class="card-content">
          <h3>維修工單管理</h3>
          <p>追蹤維修進度與派工</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 商家管理 -->
      <div class="feature-card merchant" @click="router.push('/admin/merchants')">
        <div class="card-icon">
          <i class="fas fa-store"></i>
        </div>
        <div class="card-content">
          <h3>商家管理</h3>
          <p>管理商家資料與狀態</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 優惠券管理 -->
      <div class="feature-card coupon" @click="router.push('/admin/coupons')">
        <div class="card-icon">
          <i class="fas fa-ticket-alt"></i>
        </div>
        <div class="card-content">
          <h3>優惠券管理</h3>
          <p>管理優惠券與活動</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 金流管理 -->
      <div class="feature-card payment" @click="router.push('/admin/payment')">
        <div class="card-icon">
          <i class="fas fa-credit-card"></i>
        </div>
        <div class="card-content">
          <h3>金流管理</h3>
          <p>管理金流相關設定</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>

      <!-- 小遊戲 -->
      <div class="feature-card game" @click="router.push('/admin/snake-game')">
        <div class="card-icon">
          <i class="fas fa-gamepad"></i>
        </div>
        <div class="card-content">
          <h3>小遊戲</h3>
          <p>貪吃蛇</p>
        </div>
        <div class="card-arrow">
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 頁面容器 ========== */
.admin-home {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* ========== 頁面標題 ========== */
.page-header {
  margin-bottom: 32px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
}

.header-text h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
}

.header-text p {
  margin: 8px 0 0;
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
}

/* ========== 卡片容器 ========== */
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

/* ========== 功能卡片 ========== */
.feature-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border-left: 4px solid;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 255, 255, 0.8) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.feature-card:hover::before {
  opacity: 1;
}

/* 卡片顏色分類 */
.feature-card.member { border-left-color: #409eff; }
.feature-card.admin { border-left-color: #667eea; }
.feature-card.spot { border-left-color: #67c23a; }
.feature-card.seat { border-left-color: #e6a23c; }
.feature-card.rent { border-left-color: #f56c6c; }
.feature-card.staff { border-left-color: #909399; }
.feature-card.repair { border-left-color: #b659ff; }
.feature-card.merchant { border-left-color: #ff6b6b; }
.feature-card.coupon { border-left-color: #ffc107; }
.feature-card.payment { border-left-color: #17a2b8; }
.feature-card.game { border-left-color: #ff85c0; }

.feature-card.member .card-icon { background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); }
.feature-card.admin .card-icon { background: linear-gradient(135deg, #667eea 0%, #9f7aea 100%); }
.feature-card.spot .card-icon { background: linear-gradient(135deg, #67c23a 0%, #95d475 100%); }
.feature-card.seat .card-icon { background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%); }
.feature-card.rent .card-icon { background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%); }
.feature-card.staff .card-icon { background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%); }
.feature-card.repair .card-icon { background: linear-gradient(135deg, #b659ff 0%, #d9a8ff 100%); }
.feature-card.merchant .card-icon { background: linear-gradient(135deg, #ff6b6b 0%, #ffa8a8 100%); }
.feature-card.coupon .card-icon { background: linear-gradient(135deg, #ffc107 0%, #ffdf6e 100%); }
.feature-card.payment .card-icon { background: linear-gradient(135deg, #17a2b8 0%, #6dd5ed 100%); }
.feature-card.game .card-icon { background: linear-gradient(135deg, #ff85c0 0%, #ffc0cb 100%); }

/* ========== 卡片圖示 ========== */
.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

/* ========== 卡片內容 ========== */
.card-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.card-content h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.card-content p {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

/* ========== 卡片箭頭 ========== */
.card-arrow {
  color: #c0c4cc;
  font-size: 16px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.feature-card:hover .card-arrow {
  color: #606266;
  transform: translateX(6px);
}

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .admin-home {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .header-text h1 {
    font-size: 1.5rem;
  }
  
  .cards-container {
    grid-template-columns: 1fr;
  }
}
</style>
