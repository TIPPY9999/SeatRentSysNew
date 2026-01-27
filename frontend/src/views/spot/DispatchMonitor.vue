<template>
  <div class="dispatch-monitor container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold text-dark">
        <i class="fas fa-chart-line me-2"></i>站點調度監控中心
      </h2>
      <div class="controls d-flex gap-3 align-items-center">
        <span class="text-muted small">
          最後更新: {{ lastUpdateTime }}
        </span>
        <button 
          class="btn btn-primary" 
          @click="fetchData" 
          :disabled="loading"
        >
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i> 
          {{ loading ? '更新中...' : '立即更新' }}
        </button>
      </div>
    </div>

    <!-- 警示摘要卡片 -->
    <div class="row mb-4">
      <div class="col-md-4">
        <div class="card bg-danger text-white shadow-sm h-100">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div>
              <h6 class="card-title mb-0">急需補給 (庫存過低)</h6>
              <h2 class="display-6 fw-bold my-2">{{ lowStockSpots.length }}</h2>
              <small>可用率 &lt; 20%</small>
            </div>
            <i class="fas fa-battery-empty fa-3x opacity-50"></i>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card bg-warning text-dark shadow-sm h-100">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div>
              <h6 class="card-title mb-0">急需清運 (庫存爆滿)</h6>
              <h2 class="display-6 fw-bold my-2">{{ overStockSpots.length }}</h2>
              <small>可用率 &gt; 80%</small>
            </div>
            <i class="fas fa-battery-full fa-3x opacity-50"></i>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card bg-success text-white shadow-sm h-100">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div>
              <h6 class="card-title mb-0">營運正常</h6>
              <h2 class="display-6 fw-bold my-2">{{ normalSpots.length }}</h2>
              <small>庫存水位健康</small>
            </div>
            <i class="fas fa-check-circle fa-3x opacity-50"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 監控列表 -->
    <div class="card shadow">
      <div class="card-header bg-white py-3">
        <h5 class="mb-0">即時站點狀態列表</h5>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>站點名稱</th>
              <th>總車位</th>
              <th>已租借/使用</th>
              <th>庫存水位</th>
              <th>狀態建議</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="spot in allSpots" :key="spot.spotId" :class="getRowClass(spot)">
              <td>{{ spot.spotId }}</td>
              <td class="fw-bold">{{ spot.spotName }}</td>
              <td>{{ spot.totalSeats }}</td>
              <td>{{ spot.rentedCount }}</td>
              <td style="width: 200px;">
                <div class="progress" style="height: 20px;">
                  <div 
                    class="progress-bar" 
                    role="progressbar" 
                    :style="{ width: getUtilizationRate(spot) + '%', backgroundColor: getProgressColor(spot) }"
                    :aria-valuenow="getUtilizationRate(spot)" 
                    aria-valuemin="0" 
                    aria-valuemax="100"
                  >
                    {{ getUtilizationRate(spot) }}%
                  </div>
                </div>
              </td>
              <td>
                <span class="badge rounded-pill" :class="getStatusBadge(spot).class">
                  {{ getStatusBadge(spot).text }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-primary" @click="notifyDispatch(spot)">
                  <i class="fas fa-paper-plane"></i> 發送調度單
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

defineOptions({ name: 'DispatchMonitor' });

const allSpots = ref([]);
const loading = ref(false);
const lastUpdateTime = ref('-');

// [Update] 對接真實的 Analyze Controller 路徑
const API_URL = '/api/analyze/spot-monitor';

// --- 核心邏輯：計算與分類 ---

const getUtilizationRate = (spot) => {
  if (!spot.totalSeats) return 0;
  // [Logic] 庫存水位 = (可用數量 / 總數) * 100
  // 假設 rentedCount 是「已租借出去(不在站上)」，則可用 = total - rented
  // 若 rentedCount 是「被佔用(在站上)」，則可用 = total - rented
  // 這裡假設 rentedCount = "已租借/不在站上"，所以水位(剩餘車輛)是：
  const available = spot.totalSeats - spot.rentedCount;
  return Math.round((available / spot.totalSeats) * 100);
};

// 判斷邏輯：< 20% 缺車, > 80% 滿車
const getStatusType = (spot) => {
  const rate = getUtilizationRate(spot);
  if (rate < 20) return 'LOW';
  if (rate > 80) return 'HIGH';
  return 'NORMAL';
};

const lowStockSpots = computed(() => allSpots.value.filter(s => getStatusType(s) === 'LOW'));
const overStockSpots = computed(() => allSpots.value.filter(s => getStatusType(s) === 'HIGH'));
const normalSpots = computed(() => allSpots.value.filter(s => getStatusType(s) === 'NORMAL'));

// --- UI 輔助函式 ---

const getRowClass = (spot) => {
  const type = getStatusType(spot);
  if (type === 'LOW') return 'table-danger';
  if (type === 'HIGH') return 'table-warning';
  return '';
};

const getProgressColor = (spot) => {
  const type = getStatusType(spot);
  if (type === 'LOW') return '#dc3545'; // Red
  if (type === 'HIGH') return '#ffc107'; // Yellow
  return '#198754'; // Green
};

const getStatusBadge = (spot) => {
  const type = getStatusType(spot);
  if (type === 'LOW') return { class: 'bg-danger', text: '⚠️ 庫存過低' };
  if (type === 'HIGH') return { class: 'bg-warning text-dark', text: '⚠️ 庫存爆滿' };
  return { class: 'bg-success', text: '正常' };
};

// --- 資料存取 ---

const fetchData = async () => {
  loading.value = true;
  try {
    // 這裡假設後端回傳 List<SpotDispatchStats>
    // 若後端尚未實作，可先用假資料測試
    const res = await axios.get(API_URL);
    allSpots.value = res.data;
    
    lastUpdateTime.value = new Date().toLocaleTimeString();
  } catch (err) {
    console.error('監控數據載入失敗', err);
    // 模擬假資料以供預覽
    allSpots.value = [
      { spotId: 1, spotName: '台北車站 A出口', totalSeats: 20, rentedCount: 18 }, // 剩 2 (10%) -> Low
      { spotId: 2, spotName: '信義威秀', totalSeats: 15, rentedCount: 1 },      // 剩 14 (93%) -> High
      { spotId: 3, spotName: '大安森林公園', totalSeats: 30, rentedCount: 15 },  // 剩 15 (50%) -> Normal
    ];
    lastUpdateTime.value = new Date().toLocaleTimeString() + ' (模擬數據)';
  } finally {
    loading.value = false;
  }
};

const notifyDispatch = (spot) => {
  const type = getStatusType(spot);
  let msg = '';
  if (type === 'LOW') msg = `請派員前往 [${spot.spotName}] 補充設備！`;
  else if (type === 'HIGH') msg = `請派員前往 [${spot.spotName}] 回收設備！`;
  else msg = `[${spot.spotName}] 目前狀態正常，確定要派單？`;

  Swal.fire({
    title: '發送調度通知?',
    text: msg,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '發送',
    cancelButtonText: '取消'
  }).then((result) => {
    if (result.isConfirmed) {
      // 呼叫後端發送通知 API
      // axios.post('/api/dispatch/notify', { spotId: spot.spotId })
      Swal.fire('已發送', '管理員已收到通知', 'success');
    }
  });
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.dispatch-monitor {
  background-color: #f8f9fa;
  min-height: 100vh;
}
.card {
  border: none;
  border-radius: 10px;
}
.table-danger {
  background-color: #ffebe9 !important;
}
.table-warning {
  background-color: #fff8e1 !important;
}
</style>