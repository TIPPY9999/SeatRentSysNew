<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

const API_URL = 'http://localhost:8080/rec-rent'

const rentList = ref([])
const searchCriteria = reactive({
  recId: '',
  memId: '',
  memName: '',
  recStatus: '',
  spotId: '',
  spotName: '',
  returnDate: '',
  rentDate: '',
  recPayment: '',
})

const emit = defineEmits(['edit-rent', 'delete-rent'])

const loadRents = async () => {
  try {
    const params = new URLSearchParams()
    if (searchCriteria.recId) params.append('recId', searchCriteria.recId)
    if (searchCriteria.memId) params.append('memId', searchCriteria.memId)
    if (searchCriteria.memName) params.append('memName', searchCriteria.memName)
    if (searchCriteria.recStatus) params.append('recStatus', searchCriteria.recStatus)
    if (searchCriteria.spotId) params.append('spotId', searchCriteria.spotId)
    if (searchCriteria.spotName) params.append('spotName', searchCriteria.spotName)
    if (searchCriteria.returnDate) params.append('returnDate', searchCriteria.returnDate)
    if (searchCriteria.rentDate) params.append('rentDate', searchCriteria.rentDate)
    if (searchCriteria.recPayment) params.append('recPayment', searchCriteria.recPayment)

    const queryString = params.toString()
    const requestUrl = queryString ? `${API_URL}?${queryString}` : API_URL

    const res = await axios.get(requestUrl)
    rentList.value = res.data
  } catch (err) {
    console.error('載入失敗:', err)
    alert('無法載入資料，請確認後端伺服器是否已啟動.\n錯誤: ' + err.message)
  }
}

const clearSearch = () => {
  for (const key in searchCriteria) {
    searchCriteria[key] = ''
  }
  loadRents()
}

const editRent = (rent) => {
  emit('edit-rent', rent)
}

const deleteRent = (id) => {
  emit('delete-rent', id)
}

onMounted(loadRents)

// Expose the loadRents method to the parent component
defineExpose({
  loadRents,
})
</script>

<template>
  <div class="view-section active">
    <h2>
      訂單列表
      <button class="btn-secondary" style="font-size: 0.8em" @click="loadRents">重新整理</button>
    </h2>

    <!-- 搜尋表單 -->
    <div class="search-form-container">
      <div class="search-form">
        <div class="form-group-search">
          <label>訂單編號:</label>
          <input
            v-model="searchCriteria.recId"
            type="text"
            placeholder="依訂單編號"
            @keyup.enter="loadRents"
          />
        </div>
        <div class="form-group-search">
          <label>會員編號:</label>
          <input
            v-model="searchCriteria.memId"
            type="text"
            placeholder="依會員編號"
            @keyup.enter="loadRents"
          />
        </div>
        <div class="form-group-search">
          <label>會員姓名:</label>
          <input
            v-model="searchCriteria.memName"
            type="text"
            placeholder="依會員姓名(模糊)"
            @keyup.enter="loadRents"
          />
        </div>
        <div class="form-group-search">
          <label>訂單狀態:</label>
          <select v-model="searchCriteria.recStatus" @keyup.enter="loadRents">
            <option value="">所有狀態</option>
            <option value="租借中">租借中</option>
            <option value="已完成">已完成</option>
            <option value="未歸還">未歸還</option>
            <option value="已取消">已取消</option>
          </select>
        </div>
        <div class="form-group-search">
          <label>站點ID:</label>
          <input
            v-model="searchCriteria.spotId"
            type="text"
            placeholder="依站點ID"
            @keyup.enter="loadRents"
          />
        </div>
        <div class="form-group-search">
          <label>站點名稱:</label>
          <input
            v-model="searchCriteria.spotName"
            type="text"
            placeholder="依站點名稱(模糊)"
            @keyup.enter="loadRents"
          />
        </div>
        <div class="form-group-search">
          <label>租借日期:</label>
          <input v-model="searchCriteria.rentDate" type="date" />
        </div>
        <div class="form-group-search">
          <label>歸還日期:</label>
          <input v-model="searchCriteria.returnDate" type="date" />
        </div>
      </div>
      <div class="search-actions">
        <button class="btn-primary" @click="loadRents">搜尋</button>
        <button class="btn-secondary" @click="clearSearch">清除</button>
      </div>
    </div>

    <table>
      <thead>
        <tr>
          <th>訂單狀態</th>
          <th>訂單編號</th>
          <th>會員編號</th>
          <th>會員姓名</th>
          <th>座椅編號</th>
          <th>租借點編號</th>
          <th>租借點名稱</th>
          <th>歸還點編號</th>
          <th>歸還點名稱</th>
          <th>租借時間</th>
          <th>歸還時間</th>
          <th>費用</th>
          <th width="150">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="rent in rentList" :key="rent.recSeqId">
          <td>{{ rent.recStatus }}</td>
          <td>
            <span v-if="rent.recId">{{ rent.recId }}</span>
            <span v-else style="color: gray">處理中...</span>
          </td>
          <td>{{ rent.memId }}</td>
          <td>{{ rent.memName }}</td>
          <td>{{ rent.seatsId }}</td>
          <td>{{ rent.spotIdRent }}</td>
          <td>{{ rent.rentSpotName }}</td>
          <td>{{ rent.spotIdReturn }}</td>
          <td>{{ rent.returnSpotName }}</td>
          <td>{{ rent.recRentDT2 ? rent.recRentDT2.replace('T', ' ') : '' }}</td>
          <td>{{ rent.recReturnDT2 ? rent.recReturnDT2.replace('T', ' ') : '' }}</td>
          <td>{{ rent.recPayment }}</td>
          <td>
            <button class="btn-warning" @click="editRent(rent)">編輯</button><span> / </span>
            <button class="btn-danger ml-1" @click="deleteRent(rent.recSeqId)">刪除</button>
          </td>
        </tr>
        <tr v-if="rentList.length === 0">
          <td colspan="12" class="text-center">暫無資料或查無結果</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* ========== 搜尋表單區 - 淺色風格 ========== */
.search-form-container {
  background: #f5f7fa;
  padding: 18px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  align-items: end;
}

.form-group-search {
  display: flex;
  flex-direction: column;
}

.form-group-search label {
  font-weight: 500;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.form-group-search input,
.form-group-search select {
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  transition: all 0.3s ease;
}

.form-group-search input:focus,
.form-group-search select:focus {
  border-color: #c0c4cc;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.08);
  outline: none;
}

.form-group-search input::placeholder {
  color: #c0c4cc;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

/* ========== 表格 - 淺色風格 ========== */
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin-top: 12px;
  background: white;
  font-size: 14px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

th,
td {
  border-bottom: 1px solid #ebeef5;
  padding: 14px 10px;
  text-align: center;
}

th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

tbody tr {
  transition: background-color 0.2s ease;
}

tbody tr:hover {
  background-color: #f5f7fa;
}

tbody tr:nth-child(even) {
  background-color: #fafbfc;
}

tbody tr:nth-child(even):hover {
  background-color: #f5f7fa;
}

.text-center {
  text-align: center;
  color: #909399;
  padding: 40px !important;
}

.view-section.active {
  display: block;
}

/* ========== 按鈕樣式 ========== */
button {
  padding: 8px 14px;
  cursor: pointer;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  font-size: 13px;
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-secondary {
  background: #909399;
  color: white;
}

.btn-secondary:hover {
  background: #a6a9ad;
}

/* ========== 圓形操作按鈕 ========== */
.btn-danger {
  width: 30px;
  height: 30px;
  padding: 0;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-danger:hover {
  background: #f89898;
  transform: scale(1.1);
}

.btn-warning {
  width: 30px;
  height: 30px;
  padding: 0;
  background: #409eff;
  color: #ffffff;
  border-radius: 50%;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-warning:hover {
  background: #66b1ff;
  transform: scale(1.1);
}

.ml-1 {
  margin-left: 6px;
}

/* ========== 標題樣式 ========== */
h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.3rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 18px;
}

h2 button {
  padding: 6px 12px;
  font-size: 12px;
}
</style>
