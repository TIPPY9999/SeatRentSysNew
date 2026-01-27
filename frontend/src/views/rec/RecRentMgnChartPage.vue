<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { getMonthlyOrderStats } from '@/api/modules/rec'
import { ElMessage } from 'element-plus'

// --- 1. 狀態定義 ---

// ECharts 圖表實例，使用 shallowRef 避免不必要的深度響應
const chartInstance = shallowRef(null)
// 圖表容器的 DOM 引用
const chartContainer = ref(null)

// 預設日期範圍
const defaultEndDate = new Date()
const defaultStartDate = new Date()
defaultStartDate.setMonth(defaultStartDate.getMonth() - 6)

// 用於日期選擇器的響應式變數
const startDate = ref(defaultStartDate)
const endDate = ref(defaultEndDate)

// --- 2. 核心邏輯 ---

/**
 * 格式化日期為 YYYY-MM-DD
 * @param {Date} date - 日期物件
 * @returns {string} - 格式化後的日期字串
 */
const formatDate = (date) => {
  if (!date) return ''
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 獲取後端數據並繪製圖表
 */
const fetchDataAndDrawChart = async () => {
  if (!startDate.value || !endDate.value) {
    ElMessage.warning('請選擇開始與結束日期')
    return
  }

  const loading = ElMessage({
    message: '正在載入圖表數據...',
    type: 'info',
    duration: 0,
  })

  try {
    // 呼叫 API
    const response = await getMonthlyOrderStats(
      formatDate(startDate.value),
      formatDate(endDate.value)
    )

    // 處理 API 回傳的數據
    const chartData = response.data
    const labels = chartData.map(item => `${item.year}-${item.month.toString().padStart(2, '0')}`)
    const values = chartData.map(item => item.orderCount)

    // ECharts 配置選項
    const options = {
      title: {
        text: '每月訂單數量統計',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: [
        {
          type: 'category',
          data: labels,
          axisTick: {
            alignWithLabel: true,
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          name: '訂單數量',
        },
      ],
      series: [
        {
          name: '訂單數',
          type: 'bar',
          barWidth: '60%',
          data: values,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' },
            ]),
          },
        },
      ],
      dataZoom: [
        {
          type: 'slider',
          start: 0,
          end: 100,
        },
      ],
    }

    // 初始化或更新圖表
    if (!chartInstance.value) {
      chartInstance.value = echarts.init(chartContainer.value)
    }
    chartInstance.value.setOption(options)

  } catch (error) {
    console.error('獲取圖表數據失敗:', error)
    ElMessage.error('載入圖表數據失敗，請稍後再試')
  } finally {
    loading.close()
  }
}

// --- 3. 生命週期鉤子 ---

// 組件掛載後，自動載入預設範圍的圖表
onMounted(() => {
  fetchDataAndDrawChart()
})

// 監聽窗口大小變化，重置圖表
window.addEventListener('resize', () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
})

</script>

<template>
  <div class="chart-page">
    <!-- 頂部控制項 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>訂單統計圖表篩選</span>
        </div>
      </template>
      <el-row :gutter="20" align="middle">
        <el-col :span="10">
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="選擇開始日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="1" style="text-align: center;">-</el-col>
        <el-col :span="10">
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="選擇結束日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="fetchDataAndDrawChart" style="width: 100%;">查詢</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 圖表容器 -->
    <el-card class="box-card chart-container-card">
      <div ref="chartContainer" style="width: 100%; height: 500px;"></div>
    </el-card>
  </div>
</template>

<style scoped>
.chart-page {
  padding: 20px;
}
.box-card {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}
</style>