<script setup>
// import { log } from "node:console";
import { ref,onMounted } from "vue";


onMounted(() => {
  console.log('RecRentAdd component has been mounted.');
});
// 1. 定義 emit 事件
const emit = defineEmits(["save-rent", "cancel"]);

// 2. 創建一個空的初始表單物件的函式
const createInitialForm = () => ({
  recSeqId: null,
  recId: "",
  memId: "",
  memName: "",
  seatsId: "",
  recStatus: "租借中",
  spotIdRent: "",
  spotIdReturn: "",
  recRentDT2: "",
  recReturnDT2: "",
  recViolatInt: 0,
});

// 3. 使用 ref 來儲存表單資料
const form = ref(createInitialForm());
const formTitle = "新增訂單"; // 標題是固定的

// 4. 清空/重設表單的函式
const resetForm = () => {
  form.value = createInitialForm(); 
};

// 5. 提交表單的處理函式
const saveRent = () => {
  const dataToSend = {
    ...form.value,
    recRentDT2: form.value.recRentDT2 ? form.value.recRentDT2.replace('T', ' ') : null,
    recReturnDT2: form.value.recReturnDT2 ? form.value.recReturnDT2.replace('T', ' ') : null,
  };
  emit("save-rent", dataToSend);
  // 成功提交後通常會切換視圖，但如果需要留在原頁面，可以取消註解下一行
  // resetForm(); 
};

// 6. 取消操作 (返回列表)
const handleCancel = () => {
  emit("cancel");
};
</script>

<template>
  <div class="view-section form-section active">
  
    <h2 v-text="formTitle"></h2>
    
    <div class="form-group">
      <label>訂單編號(recId):</label>
      <input v-model="form.recId" type="text" placeholder="例如: R000001" />
    </div>
    <div class="form-group">
      <label>會員編號(memId):</label>
      <input v-model="form.memId" type="number" placeholder="例如: 1" required />
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
      <input
        v-model="form.spotIdRent"
        type="number"
        placeholder="例如: 1"
        required
      />
    </div>
    <div class="form-group">
      <label>歸還站點編號 (spotIdReturn):</label>
      <input
        v-model="form.spotIdReturn"
        type="number"
        placeholder="例如: 2"
      />
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
      <input v-model="form.recViolatInt" type="number" value="0" required />
    </div>

    <div style="text-align: right">
      <button class="btn-info" @click="resetForm">重設欄位</button>
      <button class="btn-secondary" @click="handleCancel">取消</button>
      <button class="btn-primary" @click="saveRent">儲存訂單</button>
    </div> 
  </div>
</template>

<style scoped>
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
.form-group input, .form-group select {
  padding: 5px;
  flex: 1;
  border: 1px solid #ccc;
  border-radius: 3px;
}
.view-section.active {
  display: block;
}
button {
  margin-left: 10px;
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
.btn-info {
  background-color: #17a2b8;
  color: white;
}
</style>
