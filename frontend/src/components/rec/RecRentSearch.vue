<script setup>
import { ref, reactive, onMounted } from "vue";
import axios from "axios";

const API_URL = "http://localhost:8080/api/rec-rents";

const rentList = ref([]);
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

const emit = defineEmits(["edit-rent", "delete-rent"]);

const loadRents = async () => {
  try {
    const params = new URLSearchParams();
    if (searchCriteria.recId) params.append("recId", searchCriteria.recId);
    if (searchCriteria.memId) params.append("memId", searchCriteria.memId);
    if (searchCriteria.memName) params.append("memName", searchCriteria.memName);
    if (searchCriteria.recStatus) params.append("recStatus", searchCriteria.recStatus);
    if (searchCriteria.spotId) params.append("spotId", searchCriteria.spotId);
    if (searchCriteria.spotName) params.append("spotName", searchCriteria.spotName);
    if (searchCriteria.returnDate) params.append("returnDate", searchCriteria.returnDate);
    if (searchCriteria.rentDate) params.append("rentDate", searchCriteria.rentDate);

    const queryString = params.toString();
    const requestUrl = queryString ? `${API_URL}?${queryString}` : API_URL;

    const res = await axios.get(requestUrl);
    rentList.value = res.data;
  } catch (err) {
    console.error("載入失敗:", err);
    alert("無法載入資料，請確認後端伺服器是否已啟動.\n錯誤: " + err.message);
  }
};

const clearSearch = () => {
  for (const key in searchCriteria) {
    searchCriteria[key] = "";
  }
  loadRents();
};

const editRent = (rent) => {
  emit("edit-rent", rent);
};

const deleteRent = (id) => {
  emit("delete-rent", id);
};

onMounted(loadRents);

// Expose the loadRents method to the parent component
defineExpose({
  loadRents,
});
</script>

<template>
  <div class="view-section active">
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
          <th>會員ID</th>
          <th>會員姓名</th>
          <th>座椅編號</th>
          <th>租借點ID</th>
          <th>租借點名稱</th>
          <th>歸還點ID</th>
          <th>歸還點名稱</th>
          <th>租借時間</th>
          <th>歸還時間</th>
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
</template>

<style scoped>
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
.form-group-search input,
.form-group-search select {
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
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
th, td {
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
.text-center {
    text-align: center;
}
.view-section.active {
  display: block;
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
</style>
