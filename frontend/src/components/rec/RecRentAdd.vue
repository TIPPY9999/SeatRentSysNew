<script setup>
import { ref, reactive, onMounted, watch } from "vue";

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["save-rent", "cancel"]);

const formTitle = ref("新增訂單");
const form = reactive({
  recSeqId: null,
  memId: "",
  seatsId: "",
  spotIdRent: "",
  recRentDT2: "",
  recViolatInt: 0,
});

const resetForm = () => {
  formTitle.value = "新增訂單";
  form.recSeqId = null;
  form.memId = "";
  form.seatsId = "";
  form.spotIdRent = "";
  form.recRentDT2 = "";
  form.recViolatInt = 0;
};

// Watch for changes in the initialData prop to populate the form for editing
watch(
  () => props.initialData,
  (newData) => {
    if (newData && newData.recSeqId) {
      formTitle.value = "修改訂單 (ID: " + newData.recSeqId + ")";
      form.recSeqId = newData.recSeqId;
      form.memId = newData.memId;
      form.seatsId = newData.seatsId;
      form.spotIdRent = newData.spotIdRent;
      form.recViolatInt = newData.recViolatInt;

      if (newData.recRentDT2) {
        let formattedDate = newData.recRentDT2;
        if (formattedDate.length > 19) {
          formattedDate = formattedDate.substring(0, 19);
        }
        form.recRentDT2 = formattedDate;
      }
    } else {
      resetForm();
    }
  },
  { immediate: true, deep: true }
);

const saveRent = () => {
  emit("save-rent", { ...form });
};

const backToList = () => {
  emit("cancel");
};
</script>

<template>
  <div class="view-section form-section active">
  
    <h2>新增或修改訂單</h2>
    <div class="form-group">
      <label>訂單 ID (recId):</label>
      <input v-model="form.recId" type="number" placeholder="例如: 1" required />
    </div>
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
.form-group input {
  padding: 5px;
  flex: 1;
  border: 1px solid #ccc;
  border-radius: 3px;
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
</style>
