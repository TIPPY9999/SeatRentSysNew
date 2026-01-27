<template>
  <div class="spot-analyze-container">
    <el-card class="mb-4">
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold m-0 flex items-center gap-2">
            <i class="fas fa-chart-line text-blue-500"></i>
            據點營運統計儀表板
          </h2>
          <el-button type="primary" :loading="loading" @click="fetchData">
            <i class="fas fa-sync-alt mr-1"></i> 重新載入
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 第一列：縣市分佈 & 時段熱度 -->
        <el-row :gutter="20" class="mb-4">
          <!-- 縣市分佈 (Donut Chart) -->
          <el-col :md="10" :sm="24" class="mb-4">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-icon city-icon">
                    <i class="fas fa-map-marker-alt"></i>
                  </span>
                  <b>各縣市站點分佈</b>
                </div>
              </template>
              <div v-if="hasData" ref="cityChartRef" class="chart-container"></div>
              <el-empty v-else description="暫無數據" />
            </el-card>
          </el-col>

          <!-- 時段熱度 (Line/Area Chart) -->
          <el-col :md="14" :sm="24" class="mb-4">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-icon heat-icon">
                    <i class="fas fa-fire"></i>
                  </span>
                  <b>24小時租借熱度</b>
                </div>
              </template>
              <div v-if="hasData" ref="heatChartRef" class="chart-container"></div>
              <el-empty v-else description="暫無數據" />
            </el-card>
          </el-col>
        </el-row>

        <!-- 第二列：使用時長 & 站點列表 -->
        <el-row :gutter="20">
          <!-- 使用時長分佈 (Bar Chart) -->
          <el-col :md="10" :sm="24" class="mb-4">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-icon time-icon">
                    <i class="fas fa-clock"></i>
                  </span>
                  <b>租借時長分佈</b>
                </div>
              </template>
              <div v-if="hasData" ref="durationChartRef" class="chart-container"></div>
              <el-empty v-else description="暫無數據" />
            </el-card>
          </el-col>

          <!-- 站點監控 (Table) -->
          <el-col :md="14" :sm="24" class="mb-4">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span class="chart-icon monitor-icon">
                    <i class="fas fa-desktop"></i>
                  </span>
                  <b>站點即時監控 (Top 10)</b>
                </div>
              </template>
              <el-table :data="statsData.spotMonitor" style="width: 100%" height="280" stripe>
                <el-table-column prop="spotName" label="站點名稱" min-width="150" show-overflow-tooltip />
                <el-table-column prop="totalSeats" label="總座位" width="100" align="center" />
                <el-table-column prop="rentedCount" label="使用中" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.rentedCount > 0 ? 'success' : 'info'" size="small">
                      {{ scope.row.rentedCount }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="使用率" width="120" align="center">
                  <template #default="scope">
                    <el-progress 
                      :percentage="calculateUsageRate(scope.row)" 
                      :color="getUsageColor"
                      :stroke-width="10"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import ApexCharts from 'apexcharts'
import Swal from 'sweetalert2'

// -- 狀態 --
const loading = ref(false)
const hasData = ref(false)
const statsData = ref({
  cityDistribution: [], // { city, spotCount }
  spotMonitor: [],      // { spotName, totalSeats, rentedCount }
  hourlyHeatMap: [],    // { hourofDay, rentedCount }
  durationStats: []     // { durationRange, count }
})

// -- 圖表 Refs --
const cityChartRef = ref(null)
const heatChartRef = ref(null)
const durationChartRef = ref(null)

// -- 圖表實例 --
let cityChart = null
let heatChart = null
let durationChart = null

// -- Fetch Data --
const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/analyze/stats')
    // 假設後端回傳結構為 { cityDistribution: [...], spotMonitor: [...], ... }
    if (res.status === 200 && res.data) {
      statsData.value = res.data
      console.log('Analyze Data:', statsData.value) // Debug
      hasData.value = true
      nextTick(() => renderCharts())
    }
  } catch (error) {
    console.error('Fetch error:', error)
    Swal.fire({
      icon: 'error',
      title: '讀取失敗',
      text: '無法取得統計數據，請檢查後端服務',
    })
    hasData.value = false
  } finally {
    loading.value = false
  }
}

// -- Helpers --
const calculateUsageRate = (row) => {
  if (!row.totalSeats || row.totalSeats === 0) return 0
  return Math.round((row.rentedCount / row.totalSeats) * 100)
}

const getUsageColor = (percentage) => {
  if (percentage < 30) return '#909399'
  if (percentage < 70) return '#409eff'
  return '#f56c6c'
}

// -- Charts Configuration --

// 1. 縣市分佈 (Donut)
const createCityChartOptions = () => {
  const data = statsData.value.cityDistribution || []
  const labels = data.map(item => item.city || '未知')
  const series = data.map(item => item.spotCount || 0)

  return {
    series: series,
    labels: labels,
    chart: { type: 'donut', height: 280, toolbar: { show: false } },
    colors: ['#36A2EB', '#FF6384', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
    legend: { position: 'bottom' },
    plotOptions: {
      pie: {
        donut: {
           size: '60%',
           labels: { show: true, total: { show: true, label: '總站點' } }
        }
      }
    },
    tooltip: { y: { formatter: (val) => `${val} 站` } }
  }
}

// 2. 時段熱度 (Area)
const createHeatChartOptions = () => {
  const data = statsData.value.hourlyHeatMap || []
  // 填滿 0-23 小時，若缺少數據則補 0
  const fullData = Array.from({ length: 24 }, (_, i) => {
    const found = data.find(item => item.hourofDay === i)
    return found ? found.rentedCount : 0
  })

  return {
    series: [{ name: '租借次數', data: fullData }],
    chart: { type: 'area', height: 280, toolbar: { show: false } }, // 改用 area 看起來更 premium
    xaxis: {
      categories: Array.from({ length: 24 }, (_, i) => `${i}:00`),
      tooltip: { enabled: false }
    },
    colors: ['#FF4560'],
    stroke: { curve: 'smooth', width: 2 },
    fill: {
       type: 'gradient',
       gradient: { shadeIntensity: 1, opacityFrom: 0.7, opacityTo: 0.3 }
    },
    dataLabels: { enabled: false },
    tooltip: { y: { formatter: (val) => `${val} 次` } }
  }
}

// 3. 時長分佈 (Bar)
const createDurationChartOptions = () => {
  const data = statsData.value.durationStats || []
  const categories = data.map(item => item.durationRange)
  const seriesData = data.map(item => item.count)

  return {
    series: [{ name: '筆數', data: seriesData }],
    chart: { type: 'bar', height: 280, toolbar: { show: false } },
    plotOptions: {
      bar: { borderRadius: 4, horizontal: true, distributed: true }
    },
    colors: ['#008FFB', '#00E396', '#FEB019', '#FF4560', '#775DD0'],
    xaxis: { categories: categories },
    legend: { show: false }, // distributed 模式下隱藏重複 legend
    dataLabels: { enabled: true, formatter: (val) => `${val}筆` },
    tooltip: { y: { formatter: (val) => `${val} 筆` } }
  }
}

// -- Render Charts --
const renderCharts = () => {
  if (!hasData.value) return
  
  // City Chart
  if (cityChartRef.value) {
    if (cityChart) cityChart.destroy()
    cityChart = new ApexCharts(cityChartRef.value, createCityChartOptions())
    cityChart.render()
  }

  // Heat Chart
  if (heatChartRef.value) {
    if (heatChart) heatChart.destroy()
    heatChart = new ApexCharts(heatChartRef.value, createHeatChartOptions())
    heatChart.render()
  }

  // Duration Chart
  if (durationChartRef.value) {
    if (durationChart) durationChart.destroy()
    durationChart = new ApexCharts(durationChartRef.value, createDurationChartOptions())
    durationChart.render()
  }
}

const destroyCharts = () => {
  if (cityChart) cityChart.destroy()
  if (heatChart) heatChart.destroy()
  if (durationChart) durationChart.destroy()
  cityChart = null
  heatChart = null
  durationChart = null
}

// -- Lifecycle --
onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  destroyCharts()
})
</script>

<style scoped>
.spot-analyze-container {
  padding: 0;
}

.chart-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
  background: #fff;
  transition: all 0.3s;
  height: 100%; /* 讓高度一致 */
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #2c3e50;
}

.chart-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.city-icon { background: linear-gradient(135deg, #36A2EB 0%, #007bff 100%); }
.heat-icon { background: linear-gradient(135deg, #FF4560 0%, #f56c6c 100%); }
.time-icon { background: linear-gradient(135deg, #FEB019 0%, #e6a23c 100%); }
.monitor-icon { background: linear-gradient(135deg, #00E396 0%, #28a745 100%); }

.chart-container {
  min-height: 280px;
  position: relative;
}

/* 調整 ElementPlus Card Header Padding */
:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 10px;
}
</style>
