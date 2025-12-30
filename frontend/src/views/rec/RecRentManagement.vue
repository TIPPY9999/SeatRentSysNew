<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'

/**
 * RecRentManagement.vue：租借訂單管理系統
 * [修正] 移除 lang="ts"，轉換為純 JavaScript 並清理未使用變數。
 */

// --- 1. 狀態定義 ---
const rentList = ref([]) // 移除 <any[]>
const activeView = ref('list')
const API_URL = 'http://localhost:8080/api/rec-rents'

const formTitle = ref('新增訂單')
const form = reactive({
  recSeqId: null,
  memId: '',
  seatsId: '',
  spotIdRent: '',
  recRentDT2: '',
  recViolatInt: 0,
})

// --- 2. 核心邏輯 ---

// 查詢 (Read)
const loadRents = async () => {
  try {
    const res = await axios.get(API_URL)
    rentList.value = res.data
  } catch (err) {
    console.error('載入失敗:', err)
    alert('無法載入資料，請確認後端伺服器是否已啟動。\n錯誤: ' + err.message)
  }
}

// 新增或更新 (Create / Update)
const saveRent = async () => {
  try {
    const id = form.recSeqId
    const method = id ? 'put' : 'post'
    const url = id ? `${API_URL}/${id}` : API_URL
    const res = await axios[method](url, form)
    if (res.status === 200 || res.status === 201) {
      alert(id ? '更新成功！' : '新增成功！')
      resetForm()
      await loadRents()
      activeView.value = 'list'
    } else {
      alert('儲存失敗，請檢查輸入資料。')
    }
  } catch {
    // [修正] 移除未使用的 err 變數以消除警告
    alert('儲存失敗，請檢查輸入資料。')
  }
}

// 刪除 (Delete)
const deleteRent = async (id) => {
  // 移除 : number
  if (!confirm('確定要刪除這筆訂單嗎？(ID: ' + id + ')')) return
  try {
    const res = await axios.delete(`${API_URL}/${id}`)
    if (res.status === 200) {
      await loadRents()
    } else {
      alert('刪除失敗')
    }
  } catch {
    // [修正] 移除未使用的 err 變數以消除警告
    alert('刪除失敗')
  }
}

// 準備編輯資料
const editRent = (rent) => {
  // 移除 : any
  formTitle.value = '編輯訂單 (ID: ' + rent.recSeqId + ')'
  form.recSeqId = rent.recSeqId
  form.memId = rent.memId
  form.seatsId = rent.seatsId
  form.spotIdRent = rent.spotIdRent
  form.recViolatInt = rent.recViolatInt

  if (rent.recRentDT2) {
    let formattedDate = rent.recRentDT2
    if (formattedDate.length > 19) {
      formattedDate = formattedDate.substring(0, 19)
    }
    form.recRentDT2 = formattedDate
  }
  activeView.value = 'add'

  setTimeout(() => {
    const mainContent = document.querySelector('.main-content')
    if (mainContent) {
      mainContent.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, 50)
}

// 重置表單
const resetForm = () => {
  formTitle.value = '新增訂單'
  form.recSeqId = null
  form.memId = ''
  form.seatsId = ''
  form.spotIdRent = ''
  form.recRentDT2 = ''
  form.recViolatInt = 0
}

// 切換到新增畫面
const goToAddView = () => {
  resetForm()
  activeView.value = 'add'
}

// 取消返回列表
const backToList = () => {
  resetForm()
  activeView.value = 'list'
}

onMounted(() => {
  loadRents()
})
</script>

<template>
  <div class="rec-rent-container">
    <div class="sidebar">
      <h2>訂單管理</h2>
      <a @click="activeView = 'list'" :class="{ active: activeView === 'list' }">訂單查詢</a>
      <a @click="goToAddView" :class="{ active: activeView === 'add' }">新增訂單</a>
    </div>

    <div class="main-content">
      <h1>訂單管理系統 (RecRent)</h1>

      <div v-if="activeView === 'add'" class="view-section form-section active">
        <h2>{{ formTitle }}</h2>
        <div class="form-group">
          <label>會員 ID (memId):</label>
          <input v-model="form.memId" type="number" placeholder="例如: 1" required />
        </div>
        <div class="form-group">
          <label>座位編號 (seatsId):</label>
          <input v-model="form.seatsId" type="text" placeholder="例如: S001" required />
        </div>
        <div class="form-group">
          <label>租借站點 ID (spotIdRent):</label>
          <input v-model="form.spotIdRent" type="number" placeholder="例如: 10" required />
        </div>
        <div class="form-group">
          <label>租借時間 (recRentDT2):</label>
          <input v-model="form.recRentDT2" type="datetime-local" step="1" required />
        </div>
        <div class="form-group">
          <label>違規記點 (recViolatInt):</label>
          <input v-model="form.recViolatInt" type="number" required />
        </div>

        <div style="text-align: right">
          <button class="btn-secondary" @click="backToList">重置 / 取消</button>
          <button class="btn-primary" @click="saveRent">儲存訂單</button>
        </div>
      </div>

      <div v-if="activeView === 'list'" class="view-section active">
        <h2>
          訂單列表
          <button class="btn-secondary" style="font-size: 0.8em" @click="loadRents">
            重新整理
          </button>
        </h2>
        <table>
          <thead>
            <tr>
              <th width="50">ID</th>
              <th>業務編號</th>
              <th>會員</th>
              <th>座位</th>
              <th>租借站點</th>
              <th>租借時間</th>
              <th width="150">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rent in rentList" :key="rent.recSeqId">
              <td>{{ rent.recSeqId }}</td>
              <td>
                <span v-if="rent.recId">{{ rent.recId }}</span>
                <span v-else style="color: gray">處理中...</span>
              </td>
              <td>{{ rent.memId }}</td>
              <td>{{ rent.seatsId }}</td>
              <td>{{ rent.spotIdRent }}</td>
              <td>{{ rent.recRentDT2 ? rent.recRentDT2.replace('T', ' ') : '' }}</td>
              <td>
                <button class="btn-warning" @click="editRent(rent)">編輯</button>
                <button class="btn-danger ml-1" @click="deleteRent(rent.recSeqId)">刪除</button>
              </td>
            </tr>
            <tr v-if="rentList.length === 0">
              <td colspan="7" class="text-center">暫無資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rec-rent-container {
  display: flex;
  height: 100%;
  width: 100%;
  font-family: 'Microsoft JhengHei', Arial, sans-serif;
  background-color: #f9f9f9;
}
.sidebar {
  width: 200px;
  background-color: #343a40;
  color: white;
  display: flex;
  flex-direction: column;
  padding-top: 20px;
  flex-shrink: 0;
}
.sidebar h2 {
  color: white;
  text-align: center;
  font-size: 1.1em;
  margin-bottom: 20px;
}
.sidebar a {
  padding: 12px 15px;
  text-decoration: none;
  color: #cfd8dc;
  display: block;
  transition: 0.3s;
  cursor: pointer;
}
.sidebar a:hover,
.sidebar a.active {
  background-color: #495057;
  color: white;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.form-section {
  background-color: #eef;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
.form-group label {
  width: 160px;
  font-weight: bold;
}
.form-group input {
  padding: 5px;
  flex: 1;
  border: 1px solid #ccc;
  border-radius: 3px;
}
button {
  padding: 6px 12px;
  cursor: pointer;
  border: none;
  border-radius: 3px;
  font-weight: bold;
}
.btn-primary {
  background-color: #007bff;
  color: white;
}
.btn-secondary {
  background-color: #6c757d;
  color: white;
}
.btn-danger {
  background-color: #dc3545;
  color: white;
}
.btn-warning {
  background-color: #ffc107;
  color: black;
}
.ml-1 {
  margin-left: 5px;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
th,
td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
th {
  background-color: #343a40;
  color: white;
}
tr:nth-child(even) {
  background-color: #f2f2f2;
}
.view-section {
  display: none;
}
.view-section.active {
  display: block;
}
</style>
