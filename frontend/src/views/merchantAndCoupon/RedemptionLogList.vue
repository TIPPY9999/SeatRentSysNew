<template>
  <div class="log-list-container">
    <!-- ========== Header 區域 ========== -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-12">
            <div class="page-title-box">
              <div class="title-icon">
                <i class="fas fa-history"></i>
              </div>
              <div class="title-content">
                <h1>會員兌換紀錄報表</h1>
                <p class="subtitle">查看所有優惠券兌換記錄</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 統計卡片 ========== -->
    <section class="content">
      <div class="container-fluid">
        <transition name="fade-slide" appear>
          <div>
            <el-row :gutter="20" class="mb-4">
              <el-col :xs="12" :sm="8">
                <div class="stat-card total-card">
                  <div class="stat-icon"><i class="fas fa-exchange-alt"></i></div>
                  <div class="stat-info">
                    <h3>{{ logs.length }}</h3>
                    <span>總兌換次數</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="8">
                <div class="stat-card points-card">
                  <div class="stat-icon"><i class="fas fa-coins"></i></div>
                  <div class="stat-info">
                    <h3>{{ totalPointsSpent }}</h3>
                    <span>消耗總點數</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- ========== 主要表格卡片 ========== -->
            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="card-header-content">
                  <div class="header-left">
                    <span class="header-icon"><i class="fas fa-scroll"></i></span>
                    <span class="header-text">兌換紀錄列表</span>
                    <el-tag type="info" effect="light" size="small" round>{{ logs.length }} 筆</el-tag>
                  </div>
                </div>
              </template>

              <!-- 表格 -->
              <el-table
                :data="logs"
                v-loading="loading"
                stripe
                highlight-current-row
                style="width: 100%"
                :header-cell-style="{ background: '#f5f7fa', fontWeight: 'bold', color: '#303133' }"
                class="modern-table"
              >
                <el-table-column prop="logId" label="紀錄 ID" width="100" align="center">
                  <template #default="{ row }">
                    <span class="id-tag">#{{ row.logId }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="會員資訊" min-width="140">
                  <template #default="{ row }">
                    <div class="member-cell">
                      <div class="member-avatar">
                        <i class="fas fa-user"></i>
                      </div>
                      <div class="member-info">
                        <span class="member-id">會員 #{{ row.memId }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="核銷優惠券" min-width="180">
                  <template #default="{ row }">
                    <div class="coupon-cell">
                      <div class="coupon-name">
                        <i class="fas fa-ticket-alt text-warning mr-1"></i>
                        {{ row.couponName }}
                      </div>
                      <div class="coupon-id">ID: {{ row.couponId }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="消耗點數" width="130" align="center">
                  <template #default="{ row }">
                    <el-tag type="danger" effect="light" size="small" class="points-tag">
                      <i class="fas fa-minus mr-1"></i>{{ row.pointsSpent }} Pts
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="核銷時間" width="180">
                  <template #default="{ row }">
                    <div class="time-cell">
                      <i class="fas fa-clock text-info mr-1"></i>
                      {{ formatDateTime(row.redeemTime) }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="狀態" width="120" align="center">
                  <template #default>
                    <el-tag type="success" effect="light" size="small" class="status-tag">
                      <i class="fas fa-check-circle mr-1"></i>核銷成功
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="logs.length === 0 && !loading" description="暫無兌換紀錄" />
            </el-card>
          </div>
        </transition>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const logs = ref([])
const loading = ref(false)

// 取得所有紀錄
const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/discounts/logs')
    logs.value = res.data.data || []
  } catch (err) {
    console.error('取得紀錄失敗', err)
  } finally {
    loading.value = false
  }
}

// 計算消耗總點數
const totalPointsSpent = computed(() => {
  return logs.value.reduce((sum, log) => sum + log.pointsSpent, 0)
})

// 時間格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(fetchLogs)
</script>

<style scoped>
/* ========== 頁面容器 ========== */
.log-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 20px;
}

/* ========== 頁面標題區 ========== */
.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.title-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  transition: all 0.4s ease;
  box-shadow: 0 4px 15px rgba(144, 147, 153, 0.3);
}

.title-icon:hover {
  transform: scale(1.1) rotate(10deg);
}

.title-content {
  flex: 1;
  min-width: 200px;
}

.title-content h1 {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 700;
  color: #303133;
}

.title-content .subtitle {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

/* ========== 統計卡片 ========== */
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  border-radius: 4px 0 0 4px;
}

.total-card::before { background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); }
.points-card::before { background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%); }

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.total-card .stat-icon { background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); }
.points-card .stat-icon { background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%); }

.stat-info h3 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #303133;
}

.stat-info span {
  font-size: 0.85rem;
  color: #909399;
}

/* ========== 表格卡片 ========== */
.table-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.header-text {
  font-weight: 600;
  font-size: 1.1rem;
  color: #303133;
}

/* ========== 表格樣式 ========== */
.modern-table {
  border-radius: 12px;
  overflow: hidden;
}

.id-tag {
  font-size: 12px;
  color: #909399;
  font-weight: 600;
}

.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.member-info {
  display: flex;
  flex-direction: column;
}

.member-id {
  font-weight: 600;
  color: #303133;
}

.coupon-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-name {
  font-weight: 600;
  color: #303133;
}

.coupon-id {
  font-size: 12px;
  color: #909399;
}

.points-tag {
  border-radius: 20px;
  font-weight: 600;
}

.time-cell {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
}

.status-tag {
  border-radius: 20px;
  padding: 4px 12px;
}

/* ========== 動畫效果 ========== */
.fade-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-slide-leave-active {
  transition: all 0.3s ease-in;
}

.fade-slide-enter-from {
  transform: translateY(30px);
  opacity: 0;
}

.fade-slide-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* ========== 間距工具類 ========== */
.mb-4 { margin-bottom: 1.5rem; }
.mr-1 { margin-right: 4px; }

/* ========== 顏色工具類 ========== */
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }
.text-info { color: #909399; }

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .page-title-box {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
}
</style>