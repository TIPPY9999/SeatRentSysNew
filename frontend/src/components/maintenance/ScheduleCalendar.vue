<script setup>
import { ref, computed } from 'vue'
import { useScheduleConfig } from '@/composables/maintenance/useScheduleConfig'

const props = defineProps({
  schedules: { type: Array, default: () => [] },
})

const emit = defineEmits(['select-date', 'click-schedule'])

const { scheduleTypeConfig, formatTime } = useScheduleConfig()

const calendarValue = ref(new Date())

// 取得某日期的排程
const getSchedulesForDate = (date) => {
  const dateStr = formatDateKey(date)
  return props.schedules.filter((s) => {
    if (!s.isActive) return false
    const nextDate = formatDateKey(new Date(s.nextExecuteAt))
    return nextDate === dateStr
  })
}

// 格式化日期為 key
const formatDateKey = (date) => {
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 取得該月有排程的日期統計
const monthScheduleCount = computed(() => {
  const counts = {}
  props.schedules.forEach((s) => {
    if (!s.isActive) return
    const dateKey = formatDateKey(new Date(s.nextExecuteAt))
    counts[dateKey] = (counts[dateKey] || 0) + 1
  })
  return counts
})

const handleDateClick = (date) => {
  emit('select-date', date)
}

const handleScheduleClick = (schedule) => {
  emit('click-schedule', schedule)
}
</script>

<template>
  <div class="schedule-calendar">
    <el-calendar v-model="calendarValue">
      <template #date-cell="{ data }">
        <div
          class="calendar-cell"
          :class="{ 'has-schedule': getSchedulesForDate(data.date).length > 0 }"
          @click="handleDateClick(data.date)"
        >
          <div class="date-number">
            {{ data.date.getDate() }}
          </div>

          <!-- 排程標記 -->
          <div class="schedule-dots" v-if="getSchedulesForDate(data.date).length > 0">
            <template
              v-for="schedule in getSchedulesForDate(data.date).slice(0, 3)"
              :key="schedule.scheduleId"
            >
              <el-tooltip
                :content="`${schedule.title} - ${formatTime(schedule.executeTime)}`"
                placement="top"
              >
                <div
                  class="schedule-dot"
                  :style="{ backgroundColor: scheduleTypeConfig[schedule.scheduleType]?.color }"
                  @click.stop="handleScheduleClick(schedule)"
                ></div>
              </el-tooltip>
            </template>
            <span v-if="getSchedulesForDate(data.date).length > 3" class="more-count">
              +{{ getSchedulesForDate(data.date).length - 3 }}
            </span>
          </div>
        </div>
      </template>
    </el-calendar>

    <!-- 圖例 -->
    <div class="calendar-legend">
      <span v-for="(config, key) in scheduleTypeConfig" :key="key" class="legend-item">
        <span class="legend-dot" :style="{ backgroundColor: config.color }"></span>
        {{ config.text }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.schedule-calendar {
  border-radius: 12px;
  overflow: hidden;
}

.calendar-cell {
  height: 100%;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 6px;
}

.calendar-cell:hover {
  background: rgba(64, 158, 255, 0.1);
}

.calendar-cell.has-schedule {
  background: rgba(64, 158, 255, 0.05);
}

.date-number {
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 4px;
}

.schedule-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 3px;
  flex-wrap: wrap;
}

.schedule-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s;
}

.schedule-dot:hover {
  transform: scale(1.3);
}

.more-count {
  font-size: 10px;
  color: #909399;
}

.calendar-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

:deep(.el-calendar-table .el-calendar-day) {
  height: 70px;
  padding: 0;
}

:deep(.el-calendar__header) {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
}
</style>
