<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'

// --- 1. 狀態定義 (完全對應組員的變數) ---
const rentList = ref<any[]>([])
const activeView = ref('list') // 控制 view-add 與 view-list 的切換
const API_URL = 'http://localhost:8080/api/rec-rents'

// 表單標題與資料物件 (對應 getElementById 與 rentData)
const formTitle = ref('新增訂單')
const form = reactive({
  recSeqId: null, // 隱藏的 ID (對應 hidden input)
  memId: '', // 會員 ID
  seatsId: '', // 座位編號
  spotIdRent: '', // 租借站點 ID
  recRentDT2: '', // 租借時間
  recViolatInt: 0, // 違規記點
})

// --- 2. 核心邏輯 (完整移植組員的 Function) ---

// 查詢 (Read) - 對應 loadRents
const loadRents = async () => {
  try {
    const res = await axios.get(API_URL)
    rentList.value = res.data
  } catch (err: any) {
    console.error('載入失敗:', err)
    alert('無法載入資料，請確認後端伺服器是否已啟動。\n錯誤: ' + err.message)
  }
}

// 新增或更新 (Create / Update) - 對應 saveRent
const saveRent = async () => {
  try {
    const id = form.recSeqId
    const method = id ? 'put' : 'post'
    const url = id ? `${API_URL}/${id}` : API_URL

    // Vue 自動將 reactive 物件轉為 JSON 傳送
    const res = await axios[method](url, form)
    if (res.status === 200 || res.status === 201) {
      alert(id ? '更新成功！' : '新增成功！')
      resetForm()
      await loadRents()
      activeView.value = 'list' // 儲存成功後跳轉回列表 (對應 switchView('list'))
    } else {
      alert('儲存失敗，請檢查輸入資料。')
    }
  } catch (err) {
    alert('儲存失敗，請檢查輸入資料。')
  }
}

// 刪除 (Delete) - 對應 deleteRent
const deleteRent = async (id: number) => {
  if (!confirm('確定要刪除這筆訂單嗎？(ID: ' + id + ')')) return
  try {
    const res = await axios.delete(`${API_URL}/${id}`)
    if (res.status === 200) {
      await loadRents()
    } else {
      alert('刪除失敗')
    }
  } catch (err) {
    alert('刪除失敗')
  }
}

// 準備編輯資料 - 對應 editRent(index)
const editRent = (rent: any) => {
  // 設定標題與填入資料
  formTitle.value = '編輯訂單 (ID: ' + rent.recSeqId + ')'
  form.recSeqId = rent.recSeqId
  form.memId = rent.memId
  form.seatsId = rent.seatsId
  form.spotIdRent = rent.spotIdRent
  form.recViolatInt = rent.recViolatInt

  // 完美移植組員處理 datetime-local 的邏輯 (裁切毫秒)
  if (rent.recRentDT2) {
    let formattedDate = rent.recRentDT2
    if (formattedDate.length > 19) {
      formattedDate = formattedDate.substring(0, 19)
    }
    form.recRentDT2 = formattedDate
  }

  // 切換視圖
  activeView.value = 'add'

  // 完美移植組員的體驗設計：捲動到內容上方
  setTimeout(() => {
    const mainContent = document.querySelector('.main-content')
    if (mainContent) {
      mainContent.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, 50)
}

// 重置表單 - 對應 resetForm()
const resetForm = () => {
  formTitle.value = '新增訂單'
  form.recSeqId = null
  form.memId = ''
  form.seatsId = ''
  form.spotIdRent = ''
  form.recRentDT2 = ''
  form.recViolatInt = 0
}

// 頁面載入執行 - 對應 DOMContentLoaded
onMounted(() => {
  loadRents()
})
</script>

<template>
  <div class="rec-rent-container">
    <div class="sidebar">
      <h2>後台管理系統</h2>
      <a @click="activeView = 'list'" :class="{ active: activeView === 'list' }">訂單查詢</a>
      <a
        @click="
          resetForm()
          activeView = 'add'
        "
        :class="{ active: activeView === 'add' }"
        >新增訂單</a
      >
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
          <button
            class="btn-secondary"
            @click="
              resetForm()
              activeView = 'list'
            "
          >
            重置 / 取消
          </button>
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
/* --- 100% 移植組員樣式，僅微調以適配 Vue 容器 --- */
.rec-rent-container {
  display: flex;
  height: 100%;
  width: 100%;
  font-family: 'Microsoft JhengHei', Arial, sans-serif;
  background-color: #f9f9f9;
}

.sidebar {
  width: 250px;
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
  font-size: 1.2em;
  margin-bottom: 30px;
}

.sidebar a {
  padding: 15px 20px;
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
  padding: 30px;
  overflow-y: auto;
}

h1,
h2 {
  color: #333;
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
  width: 180px;
  font-weight: bold;
}

.form-group input {
  padding: 5px;
  flex: 1;
  border: 1px solid #ccc;
  border-radius: 3px;
}

button {
  padding: 8px 15px;
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

button:hover {
  opacity: 0.9;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

th,
td {
  border: 1px solid #ddd;
  padding: 10px;
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

.badge-info {
  background-color: #17a2b8;
  color: white;
  padding: 3px 7px;
  border-radius: 4px;
  font-size: 0.85em;
}
</style>
