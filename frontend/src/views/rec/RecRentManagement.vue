<script setup>
import { ref, onMounted, reactive } from "vue";
import axios from "axios";

// --- 1. 狀態定義 ---
const rentList = ref([]); // 移除 <any[]>
const activeView = ref("list");
const API_URL = "http://localhost:8080/api/rec-rents";

const formTitle = ref("新增訂單");
const form = reactive({
  recSeqId: null,
  memId: "",
  seatsId: "",
  spotIdRent: "",
  recRentDT2: "",
  recViolatInt: 0,
});

// 搜尋條件
const searchCriteria = reactive({
  recId: "",
  memId: "",
  memName: "",
  recStatus: "",
  spotId: "",
  spotName: "",
  returnDate: "",
  rentDate: "",
});

// --- 2. 核心邏輯 ---

// 查詢 (Read)
const loadRents = async () => {
  try {
    const params = new URLSearchParams();
    if (searchCriteria.recId) params.append("recId", searchCriteria.recId);
    if (searchCriteria.memId) params.append("memId", searchCriteria.memId);
    if (searchCriteria.memName) params.append("memName", searchCriteria.memName);
    if (searchCriteria.recStatus)
      params.append("recStatus", searchCriteria.recStatus);
    if (searchCriteria.spotId) params.append("spotId", searchCriteria.spotId);
    if (searchCriteria.spotName)
      params.append("spotName", searchCriteria.spotName);
    if (searchCriteria.returnDate)
      params.append("returnDate", searchCriteria.returnDate);
    if (searchCriteria.rentDate)
      params.append("rentDate", searchCriteria.rentDate);

    const queryString = params.toString();
    const requestUrl = queryString ? `${API_URL}?${queryString}` : API_URL;

    const res = await axios.get(requestUrl);
    rentList.value = res.data;
  } catch (err) {
    console.error("載入失敗:", err);
    alert("無法載入資料，請確認後端伺服器是否已啟動。\n錯誤: " + err.message);
  }
};

// 清除搜尋
const clearSearch = () => {
  for (const key in searchCriteria) {
    searchCriteria[key] = "";
  }
  loadRents();
};

// 新增或更新 (Create / Update)
const saveRent = async () => {
  try {
    const id = form.recSeqId;
    const method = id ? "put" : "post";
    const url = id ? `${API_URL}/${id}` : API_URL;
    const res = await axios[method](url, form);
    if (res.status === 200 || res.status === 201) {
      alert(id ? "更新成功！" : "新增成功！");
      resetForm();
      await loadRents();
      activeView.value = "list";
    } else {
      alert("儲存失敗，請檢查輸入資料。");
    }
  } catch {
    // [修正] 移除未使用的 err 變數以消除警告
    alert("儲存失敗，請檢查輸入資料。");
  }
};

// 刪除 (Delete)
const deleteRent = async (id) => {
  // 移除 : number
  if (!confirm("確定要刪除這筆訂單嗎？(ID: " + id + ")")) return;
  try {
    const res = await axios.delete(`${API_URL}/${id}`);
    if (res.status === 200) {
      await loadRents();
    } else {
      alert("刪除失敗");
    }
  } catch {
    // [修正] 移除未使用的 err 變數以消除警告
    alert("刪除失敗");
  }
};

// 準備編輯資料
const editRent = (rent) => {
  // 移除 : any
  formTitle.value = "編輯訂單 (ID: " + rent.recSeqId + ")";
  form.recSeqId = rent.recSeqId;
  form.memId = rent.memId;
  form.seatsId = rent.seatsId;
  form.spotIdRent = rent.spotIdRent;
  form.recViolatInt = rent.recViolatInt;

  if (rent.recRentDT2) {
    let formattedDate = rent.recRentDT2;
    if (formattedDate.length > 19) {
      formattedDate = formattedDate.substring(0, 19);
    }
    form.recRentDT2 = formattedDate;
  }
  activeView.value = "add";

  setTimeout(() => {
    const mainContent = document.querySelector(".main-content");
    if (mainContent) {
      mainContent.scrollTo({ top: 0, behavior: "smooth" });
    }
  }, 50);
};

// 重置表單
const resetForm = () => {
  formTitle.value = "新增訂單";
  form.recSeqId = null;
  form.memId = "";
  form.seatsId = "";
  form.spotIdRent = "";
  form.recRentDT2 = "";
  form.recViolatInt = 0;
};

// 切換到新增畫面
const goToAddView = () => {
  resetForm();
  activeView.value = "add";
};

// 取消返回列表
const backToList = () => {
  resetForm();
  activeView.value = "list";
};

onMounted(() => {
  loadRents();
});
</script>

<template>
  <div class="top-nav">
    <button
      @click="activeView = 'list'"
      :class="{ active: activeView === 'list' }"
      :disabled="activeView === 'list'"
    >
      訂單查詢
    </button>
    <button
      @click="goToAddView"
      :class="{ active: activeView === 'add' }"
      :disabled="activeView === 'add'"
    >
      新增訂單
    </button>
  </div>
  <div class="rec-rent-container">
    <div class="main-content">
      <h1>訂單管理系統 (RecRent)</h1>

      <div v-if="activeView === 'add'" class="view-section form-section active">
        <h2>{{ formTitle }}</h2>
        <div class="form-group">
          <label>會員 ID (memId):</label>
          <input v-model="form.memId" type="number" placeholder="例如: 1" required />
        </div>
        <div class="form-group">
          <label>會員 姓名 (memName):</label>
          <input v-model="form.memId" type="number" placeholder="例如: 1" required />
        </div>
        <div class="form-group">
          <label>座椅編號 (seatsId):</label>
          <input v-model="form.seatsId" type="text" placeholder="例如: S001" required />
        </div>
        <div class="form-group">
          <label>租借站點 ID (spotIdRent):</label>
          <input
            v-model="form.spotIdRent"
            type="number"
            placeholder="例如: 10"
            required
          />
        </div>
        <div class="form-group">
          <label>歸還站點 ID (spotIdReturn):</label>
          <input
            v-model="form.spotIdReturn"
            type="number"
            placeholder="例如: 10"
            required
          />
        </div>
        <div class="form-group">
          <label>租借時間 (recRentDT2):</label>
          <input v-model="form.recRentDT2" type="datetime-local" step="1" required />
        </div>
        <div class="form-group">
          <label>歸還時間 (recReturnDT2):</label>
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
              <select v-model="searchCriteria.recStatus">
                <option value="">所有狀態</option>
                <option value="使用中">使用中</option>
                <option value="已完成">已完成</option>
                <option value="逾期">逾期</option>
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
              <th width="50">ID</th>
              <th>訂單編號</th>
              <th>會員ID</th>
              <th>會員姓名</th>
              <th>座位</th>
              <th>租借站點ID</th>
              <th>租借站點名稱</th>
              <th>歸還站點ID</th>
              <th>歸還站點名稱</th>
              <th>租借時間</th>
              <th>歸還時間</th>
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
              <td>{{ rent.memName }}</td>
              <td>{{ rent.seatsId }}</td>
              <td>{{ rent.spotIdRent }}</td>
              <td>{{ rent.rentSpotName }}</td>
              <td>{{ rent.spotIdReturn }}</td>
              <td>{{ rent.returnSpotName }}</td>
              <td>{{ rent.recRentDT2 ? rent.recRentDT2.replace("T", " ") : "" }}</td>
              <td>{{ rent.recReturnDT2 ? rent.recReturnDT2.replace("T", " ") : "" }}</td>
              <td>
                <button class="btn-warning" @click="editRent(rent)">編輯</button>
                <button class="btn-danger ml-1" @click="deleteRent(rent.recSeqId)">
                  刪除
                </button>
              </td>
            </tr>
            <tr v-if="rentList.length === 0">
              <td colspan="12" class="text-center">暫無資料或查無結果</td>
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
  flex-direction: column;
  height: 100%;
  width: 100%;
  font-family: "Microsoft JhengHei", Arial, sans-serif;
  background-color: #f9f9f9;
}

.top-nav {
  width: 100%;
  background-color: #acacac;
  color: white;
  display: flex;
  /* flex-direction: row; */
  padding-top: 0px;
  flex-shrink: 0;
}

.top-nav button {
  background-color: #01e68e;
  color: #2b2b2b;
  font-weight: 500;
  display: flex;
  margin: 10px;
  border: none;
}

.top-nav h2 {
  color: white;
  text-align: center;
  font-size: 1.1em;
  margin-bottom: 20px;
}

.top-nav a {
  padding: 12px 15px;
  text-decoration: none;
  color: #cfd8dc;
  display: block;
  transition: 0.3s;
  cursor: pointer;
}

.top-nav a:hover,
.top-nav a.active {
  background-color: #0080ff;
  color: white;
}
.top-nav button.active {
  background-color: #00ff9d;
  color: black;
  font-weight: bold;
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
.search-form-container {
  background-color: #f0f8ff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
  align-items: center;
}

.form-group-search {
  display: flex;
  flex-direction: column;
}

.form-group-search label {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 0.9em;
}

.form-group-search input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
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
  background-color: #4e597e;
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
