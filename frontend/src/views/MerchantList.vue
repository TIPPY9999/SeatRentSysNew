<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const merchants = ref<any[]>([]);
const keyword = ref('');
const loading = ref(false);
const isEditModalOpen = ref(false);

// 表單初始狀態
const initialForm = {
  merchantId: null,
  merchantName: '',
  merchantPhone: '',
  merchantEmail: '',
  merchantAddress: '',
  merchantStatus: 1
};
const editingForm = ref({ ...initialForm });

// 1. 取得資料
const fetchMerchants = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/merchants', { params: { keyword: keyword.value } });
    merchants.value = res.data.data || [];
  } catch (e) {
    Swal.fire('錯誤', '連線失敗', 'error');
  } finally {
    loading.value = false;
  }
};

// 2. 開啟彈窗 (相容新增與編輯)
const openEditModal = (m: any = null) => {
  if (m) {
    editingForm.value = { ...m }; // 編輯：帶入資料
  } else {
    editingForm.value = { ...initialForm }; // 新增：清空表單
  }
  isEditModalOpen.value = true;
};

// 3. 儲存 (新增或修改)
const handleSave = async () => {
  const id = editingForm.value.merchantId;
  try {
    if (id) {
      // 編輯模式
      await axios.put(`http://localhost:8080/api/merchants/${id}`, editingForm.value);
    } else {
      // 新增模式
      await axios.post('http://localhost:8080/api/merchants', editingForm.value);
    }
    
    Swal.fire('成功', '資料已儲存', 'success');
    isEditModalOpen.value = false;
    fetchMerchants();
  } catch (e: any) {
    Swal.fire('失敗', '操作失敗：' + (e.response?.data?.message || '未知錯誤'), 'error');
  }
};

// 4. 刪除
const deleteMerchant = (id: number, name: string) => {
  Swal.fire({
    title: `確定要刪除「${name}」嗎？`,
    text: "若此商家有優惠券資料，可能會導致刪除失敗！",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '確定刪除'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const res = await axios.delete(`http://localhost:8080/api/merchants/${id}`);
        if (res.data.code === 200) {
          Swal.fire('已刪除', '商家已從系統移除', 'success');
          fetchMerchants();
        } else {
          Swal.fire('失敗', res.data.message, 'error');
        }
      } catch (e) {
        Swal.fire('失敗', '伺服器拒絕刪除', 'error');
      }
    }
  });
};

const resetSearch = () => { keyword.value = ''; fetchMerchants(); };
onMounted(fetchMerchants);
</script>

<template>
  <div class="card card-outline card-primary shadow-sm">
    <div class="card-header d-flex justify-content-between align-items-center bg-white">
      <h3 class="card-title fw-bold mb-0"><i class="bi bi-shop text-primary"></i> 商家管理</h3>
      <button class="btn btn-success btn-sm px-3" @click="openEditModal()">
        <i class="bi bi-plus-lg"></i> 新增商家
      </button>
    </div>
    
    <div class="card-body p-0">
      <div class="p-3 bg-light border-bottom">
        <div class="input-group input-group-sm" style="width: 400px;">
          <input v-model="keyword" class="form-control" placeholder="搜尋名稱或地址..." @keyup.enter="fetchMerchants">
          <button class="btn btn-primary px-3" @click="fetchMerchants">搜尋</button>
          <button class="btn btn-outline-secondary" @click="resetSearch">重置</button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light small fw-bold">
            <tr>
              <th class="text-center" style="width: 70px;">ID</th>
              <th>商家名稱</th>
              <th>電話</th>
              <th class="text-center">狀態</th>
              <th class="text-center">管理操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in merchants" :key="m.merchantId">
              <td class="text-center text-muted small">{{ m.merchantId }}</td>
              <td class="fw-bold text-dark">{{ m.merchantName }}</td>
              <td>{{ m.merchantPhone }}</td>
              <td class="text-center">
                <span :class="['badge rounded-pill', m.merchantStatus === 1 ? 'bg-success' : 'bg-danger']">
                  {{ m.merchantStatus === 1 ? '營運中' : '停用中' }}
                </span>
              </td>
              <td class="text-center">
                <div class="btn-group shadow-sm">
                  <button class="btn btn-sm btn-outline-primary px-3" @click="openEditModal(m)">編輯</button>
                  <button class="btn btn-sm btn-outline-danger px-3" @click="deleteMerchant(m.merchantId, m.merchantName)">刪除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isEditModalOpen" class="modal-backdrop-custom">
      <div class="modal-dialog-custom card shadow-lg">
        <div class="card-header bg-primary text-white">
          <h5 class="mb-0">{{ editingForm.merchantId ? '編輯商家 ID: ' + editingForm.merchantId : '新增商家' }}</h5>
        </div>
        <div class="card-body p-4">
          <div class="row g-3">
            <div class="col-md-6"><label class="form-label fw-bold">名稱</label><input v-model="editingForm.merchantName" class="form-control"></div>
            <div class="col-md-6"><label class="form-label fw-bold">電話</label><input v-model="editingForm.merchantPhone" class="form-control"></div>
            <div class="col-12"><label class="form-label fw-bold">信箱</label><input v-model="editingForm.merchantEmail" class="form-control"></div>
            <div class="col-12"><label class="form-label fw-bold">地址</label><textarea v-model="editingForm.merchantAddress" class="form-control"></textarea></div>
            <div class="col-12">
              <label class="form-label fw-bold">狀態</label>
              <select v-model="editingForm.merchantStatus" class="form-select">
                <option :value="1">營運中</option>
                <option :value="0">停用中</option>
              </select>
            </div>
          </div>
        </div>
        <div class="card-footer text-end bg-light">
          <button class="btn btn-secondary me-2" @click="isEditModalOpen = false">取消</button>
          <button class="btn btn-primary px-4" @click="handleSave">儲存資料</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.table td { color: #212529 !important; }
.modal-backdrop-custom {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 9999;
}
.modal-dialog-custom { width: 100%; max-width: 600px; background: white; border-radius: 8px; }
</style>