<script setup>
import { ref, watch, onMounted } from 'vue'

onMounted(() => {
  console.log('RecRentEdit component has been mounted.')
})

// 1. 定義 props
const props = defineProps({
  initialData: {
    type: Object,
    required: true,
  },
})

// 2. 定義 emit 事件
const emit = defineEmits(['save-rent', 'cancel'])

// 3. 使用 ref 來儲存表單資料
const form = ref({})
const formTitle = ref('修改訂單')

// 修正後的日期格式化函式，此方法更安全且能避免時區問題
const formatDateTimeForInput = (dateTimeString) => {
  if (!dateTimeString) {
    return null // v-model 會將 null 處理為空值
  }
  // <input type="datetime-local"> 需要 'YYYY-MM-DDTHH:mm' 格式
  // 假設後端傳來的是 'YYYY-MM-DD HH:mm:ss' 或類似格式
  // 我們直接替換空格為 'T'，並取前 16 位
  return dateTimeString.replace(' ', 'T').substring(0, 16)
}

// 4. 使用 watch 監聽 props.initialData 的變化
watch(
  () => props.initialData,
  (newData) => {
    console.log('在編輯表單中，watch 監聽到傳入的資料:', newData)

    // 只要 newData 是個有內容的物件，就進行更新
    if (newData && Object.keys(newData).length > 0) {
      formTitle.value = newData.recSeqId ? `修改訂單 (SeqID: ${newData.recSeqId})` : '修改訂單'

      // 當 props 更新時，填充表單
      form.value = {
        ...newData,
        recRentDT2: formatDateTimeForInput(newData.recRentDT2),
        recReturnDT2: formatDateTimeForInput(newData.recReturnDT2),
      }
      console.log('表單資料已更新:', form.value)
    }
  },
  { immediate: true, deep: true },
)

// 5. 提交表單的處理函式
const saveRent = () => {
  const dataToSend = {
    ...form.value,
    recRentDT2: form.value.recRentDT2 ? form.value.recRentDT2.replace('T', ' ') : null,
    recReturnDT2: form.value.recReturnDT2 ? form.value.recReturnDT2.replace('T', ' ') : null,
  }
  emit('save-rent', dataToSend)
}

// 6. 取消操作
const backToList = () => {
  emit('cancel')
}
</script>

<template>
  <div class="view-section form-section active">
    <h2 v-text="formTitle"></h2>

    <div class="form-group">
      <label>訂單編號(recId):</label>
      <!-- 設定為 disabled -->
      <input v-model="form.recId" type="text" placeholder="訂單編號" disabled />
    </div>
    <div class="form-group">
      <label>會員編號(memId):</label>
      <!-- 設定為 disabled -->
      <input v-model="form.memId" type="number" placeholder="會員編號" disabled />
    </div>
    <div class="form-group">
      <label>會員 姓名 (memName):</label>
      <input v-model="form.memName" type="text" placeholder="例如: 王大明" />
    </div>
    <div class="form-group">
      <label>座椅編號 (seatsId):</label>
      <input v-model="form.seatsId" type="text" placeholder="例如: S001" required />
    </div>
    <div class="form-group">
      <label>訂單狀態 (recStatus):</label>
      <select v-model="form.recStatus">
        <option value="租借中">租借中</option>
        <option value="已完成">已完成</option>
        <option value="未歸還">未歸還</option>
        <option value="已取消">已取消</option>
      </select>
    </div>
    <div class="form-group">
      <label>租借站點編號 (spotIdRent):</label>
      <input v-model="form.spotIdRent" type="number" placeholder="例如: 1" required />
    </div>
    <div class="form-group">
      <label>歸還站點編號 (spotIdReturn):</label>
      <input v-model="form.spotIdReturn" type="number" placeholder="例如: 2" />
    </div>
    <div class="form-group">
      <label>租借時間 (recRentDT):</label>
      <input v-model="form.recRentDT2" type="datetime-local" step="1" required />
    </div>
    <div class="form-group">
      <label>歸還時間 (recReturnDT):</label>
      <input v-model="form.recReturnDT2" type="datetime-local" step="1" />
    </div>
    <div class="form-group">
      <label>違規記點 (recViolatInt):</label>
      <input v-model="form.recViolatInt" type="number" required />
    </div>

    <div style="text-align: right">
      <button class="btn-secondary" @click="backToList">取消</button>
      <button class="btn-primary" @click="saveRent">儲存變更</button>
    </div>
  </div>
</template>

<style scoped>
/* ========== 表單區塊 - 淺色風格 ========== */
.form-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}

/* ========== 表單標題 ========== */
.form-section h2 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
}

/* ========== 表單組 ========== */
.form-group {
  margin-bottom: 14px;
  display: flex;
  align-items: center;
}

.form-group label {
  width: 180px;
  font-weight: 500;
  font-size: 14px;
  color: #606266;
}

.form-group input,
.form-group select {
  padding: 10px 12px;
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #c0c4cc;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.08);
  outline: none;
}

.form-group input::placeholder {
  color: #c0c4cc;
}

/* ========== 停用輸入框樣式 ========== */
.form-group input:disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
  border: 1px dashed #e4e7ed;
}

.view-section.active {
  display: block;
}

/* ========== 按鈕樣式 ========== */
button {
  margin-left: 10px;
  padding: 10px 18px;
  cursor: pointer;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: #67c23a;
  color: white;
}

.btn-primary:hover {
  background: #85ce61;
}

.btn-secondary {
  background: #909399;
  color: white;
}

.btn-secondary:hover {
  background: #a6a9ad;
}
</style>
