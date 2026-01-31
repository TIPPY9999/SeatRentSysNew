<template>
  <div class="log-list-container">
    <section class="content-header mb-4">
      <div class="container-fluid">
        <div class="page-title-box">
          <div class="title-icon"><i class="fas fa-chart-line"></i></div>
          <div class="title-content">
            <h1>兌換數據分析中心</h1>
            <p class="subtitle">視覺化監控優惠券熱度與兌換趨勢</p>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <transition name="fade-slide" appear>
          <div v-if="logs.length > 0">
            <el-row :gutter="20" class="mb-4">
              <el-col :xs="24" :lg="15">
                <el-card shadow="hover" class="analysis-card h-100">
                  <template #header>
                    <div class="card-header"><i class="fas fa-chart-area me-2 text-primary"></i>近期兌換趨勢</div>
                  </template>
                  <div ref="lineChartRef" style="width: 100%; height: 320px;"></div>
                </el-card>
              </el-col>
              <el-col :xs="24" :lg="9">
                <el-card shadow="hover" class="analysis-card h-100">
                  <template #header>
                    <div class="card-header"><i class="fas fa-chart-pie me-2 text-success"></i>優惠券分佈</div>
                  </template>
                  <div ref="pieChartRef" style="width: 100%; height: 320px;"></div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="mb-4">
              <el-col :span="24">
                <el-card shadow="hover" class="analysis-card">
                  <template #header>
                    <div class="card-header">
                      <i class="fas fa-crown me-2 text-warning"></i>熱門排行：優惠券兌換 TOP 10
                    </div>
                  </template>
                  <div ref="barChartRef" style="width: 100%; height: 400px;"></div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="mb-4">
              <el-col :xs="12" :lg="6">
                <div class="stat-card total-card">
                  <div class="stat-icon"><i class="fas fa-exchange-alt"></i></div>
                  <div class="stat-info">
                    <h3>{{ logs.length }}</h3>
                    <span>總兌換次數</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :lg="6">
                <div class="stat-card points-card">
                  <div class="stat-icon"><i class="fas fa-coins"></i></div>
                  <div class="stat-info">
                    <h3>{{ totalPointsSpent }}</h3>
                    <span>消耗總點數</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="d-flex justify-content-between align-items-center">
                  <div class="card-header"><i class="fas fa-list-ul me-2"></i>兌換紀錄明細</div>
                  <el-button type="success" plain size="small" @click="exportToExcel">
                    <i class="fas fa-file-excel me-1"></i> 匯出 Excel
                  </el-button>
                </div>
              </template>

              <el-table :data="logs" v-loading="loading" stripe style="width: 100%" class="modern-table">
                <el-table-column prop="logId" label="紀錄 ID" width="90" align="center" />
                
                <el-table-column label="券 ID" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" type="warning" effect="dark">#{{ row.couponId }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="couponName" label="優惠券名稱" min-width="200">
                  <template #default="{ row }">
                    <span class="fw-bold">{{ row.couponName }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="會員 ID" width="100" align="center">
                  <template #default="{ row }">#{{ row.memId }}</template>
                </el-table-column>

                <el-table-column label="消耗點數" width="120" align="center">
                  <template #default="{ row }">
                    <span class="text-danger fw-bold">-{{ row.pointsSpent }} P</span>
                  </template>
                </el-table-column>

                <el-table-column label="兌換時間" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.redeemTime) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <el-empty v-else description="目前尚無兌換紀錄可進行分析" />
        </transition>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import * as XLSX from 'xlsx'

const logs = ref([])
const loading = ref(false)
const lineChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)
let charts = []

// 1. 從後端獲取資料
const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/discounts/logs')
    logs.value = res.data.data || []
    console.log("資料載入成功:", logs.value)
    
    // 初始化圖表
    setTimeout(initAllCharts, 300)
  } catch (err) {
    console.error('API 連線失敗:', err)
  } finally {
    loading.value = false
  }
}

// 2. 圖表初始化控制器
const initAllCharts = () => {
  charts.forEach(c => c.dispose())
  charts = []

  if (lineChartRef.value) renderLineChart()
  if (pieChartRef.value) renderPieChart()
  if (barChartRef.value) renderBarChart()
}

// 趨勢圖 (折線圖)
const renderLineChart = () => {
  const chart = echarts.init(lineChartRef.value)
  const dateMap = {}
  logs.value.forEach(l => {
    const d = new Date(l.redeemTime).toLocaleDateString()
    dateMap[d] = (dateMap[d] || 0) + 1
  })
  const dates = Object.keys(dateMap).sort()
  
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ 
      name: '兌換量', 
      type: 'line', 
      smooth: true, 
      data: dates.map(d => dateMap[d]),
      areaStyle: { opacity: 0.2 },
      itemStyle: { color: '#409eff' } 
    }]
  })
  charts.push(chart)
}

// 佔比圖 (圓餅圖)
const renderPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  const nameMap = {}
  logs.value.forEach(l => {
    const name = l.couponName || '未知'
    nameMap[name] = (nameMap[name] || 0) + 1
  })

  // 轉換成陣列並排序，只取前 20 或前 15 名，其餘歸類為「其他」
  let data = Object.keys(nameMap).map(k => ({ name: k, value: nameMap[k] }))
    .sort((a, b) => b.value - a.value);

  if (data.length > 20) { 
    const others = data.slice(20).reduce((sum, item) => sum + item.value, 0);
    data = data.slice(0, 20);
    data.push({ name: '其他', value: others });
  }

  chart.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c} 次 ({d}%)' // 懸停時顯示完整名稱與佔比
    },
    legend: { 
      type: 'scroll',      // 💡 開啟滾動模式，避免擠爆
      orient: 'vertical',
      right: 10,
      top: 20,
      bottom: 20,
      textStyle: { fontSize: 10 }
    },
    series: [{
      type: 'pie', 
      radius: ['40%', '70%'],
      center: ['40%', '50%'], // 💡 將圓餅圖往左移，給右邊圖例留空間
      avoidLabelOverlap: true, // 避免標籤重疊
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false }, // 隱藏線條標籤，讓畫面清爽
      data: data
    }]
  })
  charts.push(chart)
}

// 排行圖 (水平長條圖)
const renderBarChart = () => {
  const chart = echarts.init(barChartRef.value)
  const rankMap = {}
  logs.value.forEach(l => {
    const label = l.couponName || "未知券";
    rankMap[label] = (rankMap[label] || 0) + 1
  })

  const sorted = Object.entries(rankMap)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => a.value - b.value)
    .slice(-10)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map(d => d.name) },
    series: [{
      name: '兌換次數', 
      type: 'bar', 
      data: sorted.map(d => d.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }
        ]),
        borderRadius: [0, 5, 5, 0]
      },
      label: { show: true, position: 'right' }
    }]
  })
  charts.push(chart)
}

// 3. 匯出 Excel
const exportToExcel = () => {
  // 1. 建立一個物件來統計次數
  // 格式會像：{ "美式咖啡": { id: 50, count: 5 }, "拿鐵": { id: 51, count: 3 } }
  const summaryMap = {};

  logs.value.forEach(log => {
    const key = log.couponName || "未知優惠券";
    if (!summaryMap[key]) {
      summaryMap[key] = {
        "優惠券 ID": log.couponId,
        "優惠券名稱": key,
        "總兌換次數": 0,
        "累計消耗點數": 0
      };
    }
    summaryMap[key]["總兌換次數"] += 1;
    summaryMap[key]["累計消耗點數"] += log.pointsSpent;
  });

  // 2. 將統計結果轉為陣列，並依照次數從高到低排序
  const excelData = Object.values(summaryMap).sort((a, b) => b["總兌換次數"] - a["總兌換次數"]);

  // 3. 執行匯出
  const worksheet = XLSX.utils.json_to_sheet(excelData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "優惠券兌換統計");
  
  // 4. 檔名也改得專業一點
  XLSX.writeFile(workbook, `優惠券熱度統計報表_${new Date().toLocaleDateString()}.xlsx`);
}

// 工具函數
const totalPointsSpent = computed(() => logs.value.reduce((s, l) => s + l.pointsSpent, 0))
const formatDateTime = (val) => val ? new Date(val).toLocaleString('zh-TW') : '-'
const handleResize = () => charts.forEach(c => c.resize())

onMounted(() => { 
  fetchLogs(); 
  window.addEventListener('resize', handleResize) 
})
onUnmounted(() => { 
  window.removeEventListener('resize', handleResize); 
  charts.forEach(c => c.dispose()) 
})
</script>

<style scoped>
.log-list-container { min-height: 100vh; background: #f4f7f9; padding: 24px; }
.page-title-box { display: flex; align-items: center; gap: 16px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.title-icon { width: 50px; height: 50px; background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.analysis-card { border-radius: 12px; border: none; }
.card-header { font-weight: bold; color: #333; }
.stat-card { background: white; padding: 22px; border-radius: 12px; display: flex; align-items: center; gap: 15px; box-shadow: 0 4px 10px rgba(0,0,0,0.03); }
.total-card { border-left: 5px solid #409eff; }
.points-card { border-left: 5px solid #f56c6c; }
.stat-icon { width: 45px; height: 45px; border-radius: 10px; color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.total-card .stat-icon { background: #409eff; }
.points-card .stat-icon { background: #f56c6c; }
.stat-info h3 { margin: 0; font-size: 1.6rem; color: #2c3e50; }
.table-card { border-radius: 12px; border: none; }
.fw-bold { font-weight: 700; }
</style>