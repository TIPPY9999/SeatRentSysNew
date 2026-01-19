<template>
  <div class="card card-outline card-info shadow-sm border-0">
    <div class="card-header py-3">
      <h3 class="card-title fw-bold mb-0">📜 會員兌換紀錄報表</h3>
    </div>

    <div class="card-body p-0">
      <div class="p-3 bg-light border-bottom d-flex gap-4">
        <div>總兌換次數：<span class="fw-bold text-primary">{{ logs.length }}</span></div>
        <div>消耗總點數：<span class="fw-bold text-danger">{{ totalPointsSpent }} Pts</span></div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light small fw-bold">
            <tr>
              <th class="text-center">紀錄 ID</th>
              <th>會員資訊</th>
              <th>核銷優惠券</th>
              <th class="text-center">消耗點數</th>
              <th>核銷時間</th>
              <th class="text-center">狀態</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.logId">
              <td class="text-center text-muted small">{{ log.logId }}</td>
              <td>
                <div class="fw-bold">會員 #{{ log.memId }}</div>
              </td>
              <td>
                <div class="fw-bold text-primary">{{ log.couponName }}</div>
                <div class="small text-muted">ID: {{ log.couponId }}</div>
              </td>
              <td class="text-center">
                <span class="badge bg-danger-lite text-danger fw-bold">
                  -{{ log.pointsSpent }} Pts
                </span>
              </td>
              <td>
                <div class="small">{{ formatDateTime(log.redeemTime) }}</div>
              </td>
              <td class="text-center">
                <span class="badge bg-success">核銷成功</span>
              </td>
            </tr>
            <tr v-if="logs.length === 0">
              <td colspan="6" class="text-center py-4 text-muted">暫無兌換紀錄</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
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
.bg-danger-lite { background-color: #ffeeee; }
</style>