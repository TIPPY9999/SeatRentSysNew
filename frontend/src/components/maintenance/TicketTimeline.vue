<template>
  <div class="ticket-timeline-container">
    <!-- 標題區 -->
    <div class="timeline-header">
      <h3>
        <i class="fas fa-history"></i>
        工單歷程記錄
      </h3>
      <el-button size="small" @click="fetchLogs" :loading="loading" circle>
        <i class="fas fa-sync-alt"></i>
      </el-button>
    </div>

    <!-- Loading 狀態 -->
    <div v-if="loading" class="loading-skeleton">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 無資料提示 -->
    <el-empty v-else-if="logs.length === 0" description="尚無歷程記錄" :image-size="120" />

    <!-- Timeline 主體 -->
    <el-timeline v-else class="timeline-content">
      <el-timeline-item
        v-for="(log, index) in logs"
        :key="log.logId"
        :color="getActionColor(log.action)"
        :icon="getActionIcon(log.action)"
        :size="log.action === 'URGENT' ? 'large' : 'normal'"
        :class="['timeline-item', `animate-delay-${index % 5}`]"
      >
        <!-- 卡片內容 -->
        <el-card shadow="hover" class="log-card" :class="`action-${log.action.toLowerCase()}`">
          <template #header>
            <div class="card-header">
              <span class="action-badge" :class="`badge-${log.action.toLowerCase()}`">
                {{ getActionLabel(log.action) }}
              </span>
              <span class="timestamp">
                <i class="far fa-clock"></i>
                {{ log.createdAt }}
              </span>
            </div>
          </template>

          <div class="card-body">
            <div class="operator">
              <i class="fas fa-user-circle"></i>
              <strong>{{ log.operator }}</strong>
            </div>
            <div v-if="log.comment" class="comment">
              <i class="fas fa-comment-dots"></i>
              {{ formatLogComment(log.comment) }}
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import { ElMessage } from 'element-plus'
import { getPriorityText, getStatusText, getResultText } from '@/composables/maintenance/useTicketConfig'

// ========== Props ==========
const props = defineProps({
  ticketId: {
    type: Number,
    required: true,
  },
})

// ========== State ==========
const logs = ref([])
const loading = ref(false)

// ========== Methods ==========

/**
 * 取得工單歷程
 */
const fetchLogs = async () => {
  if (!props.ticketId) return

  loading.value = true
  try {
    const response = await maintenanceApi.getTicketLogs(props.ticketId)
    logs.value = response.data
  } catch (error) {
    console.error('取得歷程記錄失敗:', error)
    ElMessage.error('載入歷程記錄失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

/**
 * 根據 action 決定顏色
 */
const getActionColor = (action) => {
  const colorMap = {
    CREATED: '#409EFF', // 藍色 (Primary)
    ASSIGNED: '#909399', // 灰色 (Info)
    STARTED: '#E6A23C', // 橘色 (Warning)
    RESOLVED: '#67C23A', // 綠色 (Success)
    CANCELLED: '#F56C6C', // 紅色 (Danger)
    URGENT: '#FF6600', // 深橘 (緊急)
  }
  return colorMap[action] || '#409EFF'
}

/**
 * 根據 action 決定圖示
 */
const getActionIcon = (action) => {
  const iconMap = {
    CREATED: 'Plus',
    ASSIGNED: 'User',
    STARTED: 'Tools',
    RESOLVED: 'CircleCheck',
    CANCELLED: 'CircleClose',
    URGENT: 'Warning',
  }
  return iconMap[action] || 'DocumentCopy'
}

/**
 * 根據 action 顯示中文標籤
 */
const getActionLabel = (action) => {
  const labelMap = {
    CREATED: '建立工單',
    ASSIGNED: '指派人員',
    STARTED: '開始維修',
    RESOLVED: '維修完成',
    CANCELLED: '取消工單',
    URGENT: '緊急標記',
  }
  return labelMap[action] || action
}

/**
 * 格式化歷程 comment，將代碼轉換為中文
 * @param {string} comment - 原始 comment 字串
 * @returns {string} 轉換後的中文字串
 */
const formatLogComment = (comment) => {
  if (!comment) return '-'
  
  let formatted = comment
  
  // 替換「優先權: CODE」為「優先權: 中文」
  formatted = formatted.replace(/優先權:\s*([A-Z_]+)/g, (match, code) => {
    return `優先權: ${getPriorityText(code)}`
  })
  
  // 替換「狀態: CODE」為「狀態: 中文」
  formatted = formatted.replace(/狀態:\s*([A-Z_]+)/g, (match, code) => {
    return `狀態: ${getStatusText(code)}`
  })
  
  // 替換「結果: CODE」為「結果: 中文」
  formatted = formatted.replace(/結果:\s*([A-Z_]+)/g, (match, code) => {
    return `結果: ${getResultText(code)}`
  })
  
  return formatted
}

// 把 fetchLogs 暴露給父元件使用
defineExpose({
  fetchLogs,
})

// ========== Lifecycle ==========
onMounted(() => {
  fetchLogs()
})

// 監聽 ticketId 變化 (若在同一頁切換工單)
watch(
  () => props.ticketId,
  () => {
    fetchLogs()
  },
)
</script>

<style scoped>
/* ========== 容器基礎樣式 ========== */
.ticket-timeline-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 12px;
  min-height: 400px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #dcdfe6;
}

.timeline-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.timeline-header h3 i {
  margin-right: 8px;
  color: #409eff;
}

/* ========== Loading 骨架屏 ========== */
.loading-skeleton {
  padding: 20px;
}

/* ========== Timeline 主體 ========== */
.timeline-content {
  padding: 10px 0;
}

/* ========== 卡片樣式 ========== */
.log-card {
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 4px solid transparent;
}

/* Hover 浮起效果 */
.log-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

/* 根據 action 類型顯示不同左側邊框顏色 */
.log-card.action-created {
  border-left-color: #409eff;
}

.log-card.action-assigned {
  border-left-color: #909399;
}

.log-card.action-started {
  border-left-color: #e6a23c;
}

.log-card.action-resolved {
  border-left-color: #67c23a;
}

.log-card.action-cancelled {
  border-left-color: #f56c6c;
}

.log-card.action-urgent {
  border-left-color: #ff6600;
  animation: pulseGlow 2s ease-in-out infinite;
}

/* 卡片頭部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-created {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.badge-assigned {
  background: linear-gradient(135deg, #909399, #b3b8bd);
}

.badge-started {
  background: linear-gradient(135deg, #e6a23c, #f0bc65);
}

.badge-resolved {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.badge-cancelled {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.badge-urgent {
  background: linear-gradient(135deg, #ff6600, #ff9933);
  animation: badgePulse 1.5s ease-in-out infinite;
}

.timestamp {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.timestamp i {
  margin-right: 4px;
}

/* 卡片內容 */
.card-body {
  padding: 12px 0;
}

.operator {
  display: flex;
  align-items: center;
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
}

.operator i {
  margin-right: 8px;
  color: #409eff;
  font-size: 18px;
}

.comment {
  margin-top: 10px;
  padding: 10px;
  background: #f4f4f5;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  display: flex;
  align-items: flex-start;
}

.comment i {
  margin-right: 8px;
  margin-top: 2px;
  color: #909399;
  flex-shrink: 0;
}

/* ========== 動畫效果 ========== */

/* 彈跳進場動畫 (Staggered) */
@keyframes bounceInRight {
  0% {
    opacity: 0;
    transform: translateX(100px) scale(0.9);
  }
  60% {
    opacity: 1;
    transform: translateX(-10px) scale(1.02);
  }
  80% {
    transform: translateX(5px);
  }
  100% {
    transform: translateX(0) scale(1);
  }
}

.timeline-item {
  animation: bounceInRight 0.6s cubic-bezier(0.68, -0.55, 0.27, 1.55) both;
}

/* 依序延遲進場 (每個項目間隔 0.1s) */
.animate-delay-0 {
  animation-delay: 0s;
}
.animate-delay-1 {
  animation-delay: 0.1s;
}
.animate-delay-2 {
  animation-delay: 0.2s;
}
.animate-delay-3 {
  animation-delay: 0.3s;
}
.animate-delay-4 {
  animation-delay: 0.4s;
}

/* 緊急工單呼吸燈效果 */
@keyframes pulseGlow {
  0%,
  100% {
    box-shadow: 0 0 5px rgba(255, 102, 0, 0.3);
  }
  50% {
    box-shadow:
      0 0 20px rgba(255, 102, 0, 0.6),
      0 0 30px rgba(255, 102, 0, 0.4);
  }
}

/* Badge 脈動動畫 */
@keyframes badgePulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .log-card:hover {
    transform: translateY(-2px) scale(1.01);
  }
}
</style>
