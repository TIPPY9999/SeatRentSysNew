<script setup>
import { ref, onMounted } from 'vue'

// 1. 定義 emit 事件
const emit = defineEmits(['save-rent', 'cancel'])

// 2. 創建一個空的初始表單物件的函式
const createInitialForm = () => ({
  recSeqId: null,
  // recId: '',
  memId: '',
  memName: '',
  seatsId: '',
  recStatus: '租借中',
  spotIdRent: '',
  spotIdReturn: '',
  recRentDT2: '',
  recReturnDT2: '',
  recViolatInt: 0,
  recNote: '', // [修正] 補上備註欄位的初始化
})

// 3. 使用 ref 來儲存表單資料
const form = ref(createInitialForm())
const formTitle = '新增訂單' // 標題是固定的

// 4. 清空/重設表單的函式
const resetForm = () => {
  form.value = createInitialForm()
}

// 5. 提交表單的處理函式
const saveRent = () => {
  const dataToSend = {
    ...form.value,
    recRentDT2: form.value.recRentDT2 || null, // 保持 ISO 格式 (含 'T')，後端才能正確解析
    recReturnDT2: form.value.recReturnDT2 || null, // 保持 ISO 格式 (含 'T')
  }
  emit('save-rent', dataToSend)
  // 成功提交後通常會切換視圖，但如果需要留在原頁面，可以取消註解下一行
  // resetForm();
}

// 6. 取消操作 (返回列表)
const handleCancel = () => {
  emit('cancel')
}

// [新增] 一鍵輸入預設值函式
const fillDefaultValues = () => {
  const now = new Date()

  // 封裝格式化邏輯：將 Date 物件轉換為 datetime-local 所需的 YYYY-MM-DDTHH:mm:ss 格式
  const toLocalISOString = (date) => {
    const offset = date.getTimezoneOffset() * 60000
    return new Date(date.getTime() - offset).toISOString().slice(0, 19)
  }

  // 計算昨天 (語意更清楚的寫法)
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  
  form.value = {
    recSeqId: null,
    // recId: 'TEST-' + Math.floor(Math.random() * 10000), // 隨機產生訂單編號
    memId: 1,
    memName: '測試會員',
    seatsId: '1',
    recStatus: '租借中',
    spotIdRent: 1,
    spotIdReturn: 2,
    recRentDT2: toLocalISOString(yesterday),
    recReturnDT2: toLocalISOString(now),
    recViolatInt: 2,
    recNote: '系統自動帶入測試資料',
  }
}
</script>

<template>
  <div class="view-section form-section active">
    <!-- [修改] 標題區塊加入一鍵輸入按鈕 -->
    <div class="form-header">
      <h2>{{ formTitle }}</h2>
      <button class="btn-warning btn-sm" @click="fillDefaultValues">一鍵輸入</button>
    </div>

    <!-- <div class="form-group">
      <label>訂單編號(recId):</label>
      <input v-model="form.recId" type="text" placeholder="必須..." />
    </div> -->
    <div class="form-group">
      <label>會員編號(memId):</label>
      <input v-model="form.memId" type="number" placeholder="必須..." required />
    </div>
    <div class="form-group">
      <label>會員姓名:</label>
      <input v-model="form.memName" type="text" placeholder="自動帶入..." disabled/>
    </div>
    <div class="form-group">
      <label>座椅編號:</label>
      <input v-model="form.seatsId" type="text" placeholder="必須..." required />
    </div>
    <div class="form-group">
      <label>訂單狀態:</label>
      <select v-model="form.recStatus">
        <option value="租借中">租借中</option>
        <option value="已完成">已完成</option>
        <option value="未歸還">未歸還</option>
        <option value="已取消">已取消</option>
      </select>
    </div>
    <div class="form-group">
      <label>租借站點編號:</label>
      <input v-model="form.spotIdRent" type="number" placeholder="必須、數字..." required />
    </div>
    <div class="form-group">
      <label>歸還站點編號:</label>
      <input v-model="form.spotIdReturn" type="number" placeholder="數字..." />
    </div>
    <div class="form-group">
      <label>租借時間:</label>
      <input v-model="form.recRentDT2" type="datetime-local" step="1" placeholder="必須" required />
    </div>
    <div class="form-group">
      <label>歸還時間:</label>
      <input v-model="form.recReturnDT2" type="datetime-local" step="1" />
    </div>
    <div class="form-group">
      <label>違規記點:</label>
      <input v-model="form.recViolatInt" type="number" value="0" placeholder="數字..." required />
    </div>
    <div class="form-group">
      <label>備註:</label>
      <!-- [修正] 修正綁定錯誤，原本誤綁定到 memName -->
      <input v-model="form.recNote" type="text" placeholder="選填..."/>
    </div>

    <div style="text-align: right">
      <button class="btn-info" @click="resetForm">重設欄位</button>
      <button class="btn-secondary" @click="handleCancel">取消</button>
      <button class="btn-primary" @click="saveRent">儲存訂單</button>
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

/* ========== 表單標題區塊 ========== */
.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* 標題在左，按鈕在右，若要按鈕緊鄰標題可改為 flex-start 並加 gap */
  border-bottom: 2px solid #e4e7ed;
  margin-bottom: 20px;
  padding-bottom: 10px;
}

.form-header h2 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #303133;
  margin: 0; /* 移除預設邊距，由 header 控制 */
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

.btn-info {
  background: #409eff;
  color: white;
}

.btn-info:hover {
  background: #66b1ff;
}

/* [新增] 小按鈕樣式 */
.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
  margin-left: 10px;
  height: auto;
}
</style>
