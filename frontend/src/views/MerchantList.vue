<template>
  <div class="merchant-container">
    <h2 class="page-title">商家管理系統</h2>

    <div class="toolbar">
      <div class="search-box">
        <input v-model="keyword" placeholder="搜尋商家名稱或地址..." @keyup.enter="fetchMerchants" class="form-control">
        <button @click="fetchMerchants" class="btn btn-primary">搜尋</button>
        <button @click="resetSearch" class="btn btn-outline">重置</button>
      </div>
      <button @click="openModal()" class="btn btn-add">+ 新增商家</button>
    </div>

    <table class="merchant-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>商家名稱</th>
          <th>電話</th>
          <th>信箱</th>
          <th>地址</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in merchants" :key="m.merchantId" :class="{ 'status-off': m.merchantStatus === 0 }">
          <td>{{ m.merchantId }}</td>
          <td class="bold">{{ m.merchantName }}</td>
          <td>{{ m.merchantPhone }}</td>
          <td>{{ m.merchantEmail }}</td>
          <td>{{ m.merchantAddress }}</td>
          <td>
            <span :class="['status-badge', m.merchantStatus === 1 ? 'on' : 'off']">
              {{ m.merchantStatus === 1 ? '營運中' : '停用中' }}
            </span>
          </td>
          <td class="actions">
            <button @click="openModal(m)" class="btn-icon edit">編輯</button>
            <button @click="deleteMerchant(m.merchantId)" class="btn-icon delete">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="isModalOpen" class="modal-mask">
      <div class="modal-wrapper">
        <div class="modal-container">
          <h3>{{ form.merchantId ? '修改商家資料' : '新增商家' }}</h3>
          <hr>
          <div class="form-body">
            <div class="form-item">
              <label>商家名稱</label>
              <input v-model="form.merchantName" type="text">
            </div>
            <div class="form-item">
              <label>聯絡電話</label>
              <input v-model="form.merchantPhone" type="text">
            </div>
            <div class="form-item">
              <label>電子信箱</label>
              <input v-model="form.merchantEmail" type="email">
            </div>
            <div class="form-item">
              <label>商家地址</label>
              <input v-model="form.merchantAddress" type="text">
            </div>
            <div class="form-item">
              <label>營運狀態</label>
              <select v-model="form.merchantStatus">
                <option :value="1">營運中</option>
                <option :value="0">停用中</option>
              </select>
              <p v-if="form.merchantStatus === 0" class="warn-text">* 停用商家將自動下架旗下所有優惠券</p>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="submitForm" class="btn btn-save">儲存</button>
            <button @click="isModalOpen = false" class="btn btn-cancel">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

// 狀態管理
const merchants = ref([]);
const keyword = ref('');
const isModalOpen = ref(false);

// 表單初始狀態
const initialForm = {
  merchantId: null,
  merchantName: '',
  merchantPhone: '',
  merchantEmail: '',
  merchantAddress: '',
  merchantStatus: 1
};
const form = ref({ ...initialForm });

// 1. 取得商家資料
const fetchMerchants = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/merchants', {
      params: { keyword: keyword.value }
    });
    if (res.data.code === 200) {
      merchants.value = res.data.data;
    }
  } catch (error) {
    Swal.fire('錯誤', '無法取得商家資料', 'error');
  }
};

// 2. 開啟彈窗 (新增或編輯)
const openModal = (data = null) => {
  if (data) {
    form.value = { ...data }; // 編輯：帶入現有資料
  } else {
    form.value = { ...initialForm }; // 新增：重設表單
  }
  isModalOpen.value = true;
};

// 3. 提交表單 (新增與更新共用)
const submitForm = async () => {
  try {
    // 因為沒有圖片，直接傳送 JSON 物件即可
    const res = await axios.post('http://localhost:8080/api/merchants', form.value);
    if (res.data.code === 200) {
      Swal.fire('成功', res.data.message, 'success');
      isModalOpen.value = false;
      fetchMerchants(); // 重新整理列表
    }
  } catch (error) {
    Swal.fire('失敗', '儲存過程中發生錯誤', 'error');
  }
};

// 4. 刪除商家
const deleteMerchant = (id) => {
  Swal.fire({
    title: '確定刪除？',
    text: '刪除商家後可能無法復原！',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消'
  }).then(async (result) => {
    if (result.isConfirmed) {
     const res = await axios.get('/api/merchants', { params: { keyword: keyword.value } });
merchants.value = res.data.data;
    }
  });
};

const resetSearch = () => {
  keyword.value = '';
  fetchMerchants();
};

onMounted(fetchMerchants);
</script>

<style scoped>
.merchant-container { padding: 30px; max-width: 1200px; margin: auto; }
.page-title { color: #333; margin-bottom: 25px; border-left: 5px solid #42b983; padding-left: 15px; }

.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.search-box { display: flex; gap: 10px; }

.merchant-table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
.merchant-table th { background: #f8f9fa; padding: 15px; border-bottom: 2px solid #eee; }
.merchant-table td { padding: 15px; border-bottom: 1px solid #eee; text-align: center; }

.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.85em; color: white; }
.status-badge.on { background: #42b983; }
.status-badge.off { background: #ff5252; }
.status-off { color: #aaa; background: #fafafa; }

.btn { padding: 8px 16px; border-radius: 4px; cursor: pointer; border: none; }
.btn-primary { background: #42b983; color: white; }
.btn-add { background: #34495e; color: white; font-weight: bold; }
.btn-icon { padding: 5px 10px; margin: 0 3px; cursor: pointer; border-radius: 3px; border: 1px solid #ddd; }
.btn-icon.delete { color: #ff5252; border-color: #ff5252; }

/* Modal 樣式 */
.modal-mask { position: fixed; z-index: 9998; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; }
.modal-container { width: 500px; background: white; padding: 25px; border-radius: 8px; }
.form-body { margin-top: 20px; }
.form-item { margin-bottom: 15px; display: flex; flex-direction: column; }
.form-item label { font-weight: bold; margin-bottom: 5px; }
.form-item input, .form-item select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.warn-text { color: #ff5252; font-size: 0.8em; margin-top: 5px; }
.modal-footer { margin-top: 25px; display: flex; justify-content: flex-end; gap: 10px; }
</style>