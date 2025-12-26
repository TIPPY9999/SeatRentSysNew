<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

// --- 變數宣告 ---
const merchants = ref<any[]>([]);
const keyword = ref('');
const loading = ref(false);

// 控制 Modal 顯示與編輯暫存
const isEditModalOpen = ref(false);
const editingForm = ref<any>({
  merchantId: null,
  merchantName: '',
  merchantPhone: '',
  merchantEmail: '',
  merchantAddress: '',
  merchantStatus: 1
});

// --- API 函式 ---

// 1. 取得清單
const fetchMerchants = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/merchants', { 
      params: { keyword: keyword.value } 
    });
    if (res.data) {
      merchants.value = Array.isArray(res.data) ? res.data : res.data.data;
    }
  } catch (e) {
    Swal.fire('錯誤', '取得資料失敗', 'error');
  } finally {
    loading.value = false;
  }
};

// 2. 開啟編輯彈窗 (核心修正點)
const openEditModal = (m: any) => {
  // 使用解構賦值複製一份資料，避免直接修改到列表
  editingForm.value = { ...m };
  isEditModalOpen.value = true;
};

// 3. 儲存編輯 (對應後端更新 API)
const handleUpdate = async () => {
  try {
    const id = editingForm.value.merchantId;
    // 假設後端接收 PUT http://localhost:8080/api/merchants/{id}
    await axios.put(`http://localhost:8080/api/merchants/${id}`, editingForm.value);
    
    Swal.fire('成功', '商家資料已更新', 'success');
    isEditModalOpen.value = false;
    fetchMerchants(); // 刷新列表
  } catch (e) {
    Swal.fire('失敗', '更新過程發生錯誤', 'error');
  }
};

// 4. 刪除商家
const deleteMerchant = (id: number, name: string) => {
  Swal.fire({
    title: `確定要停用「${name}」嗎？`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/merchants/${id}`);
        Swal.fire('已完成', '狀態已更新', 'success');
        fetchMerchants();
      } catch (e) {
        Swal.fire('失敗', '操作無法完成', 'error');
      }
    }
  });
};

const resetSearch = () => {
  keyword.value = '';
  fetchMerchants();
};

onMounted(fetchMerchants);
</script>

<template>
  <div class="card card-outline card-primary shadow-sm border-0">
    <div class="card-header align-items-center d-flex justify-content-between py-3 bg-white">
      <h3 class="card-title mb-0 fw-bold">
        <i class="bi bi-shop me-2 text-primary"></i> 商家管理系統
      </h3>
      <button class="btn btn-success btn-sm fw-bold shadow-sm px-3">
        <i class="bi bi-plus-lg"></i> 新增商家
      </button>
    </div>
    
    <div class="card-body p-0">
      <div class="p-3 bg-light border-bottom">
        <div class="input-group input-group-sm" style="width: 400px;">
          <input v-model="keyword" class="form-control" placeholder="搜尋..." @keyup.enter="fetchMerchants">
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
              <th>信箱</th>
              <th>地址</th>
              <th class="text-center">狀態</th>
              <th class="text-center">管理操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in merchants" :key="m.merchantId">
              <td class="text-center text-muted small">{{ m.merchantId }}</td>
              <td class="fw-bold text-dark">{{ m.merchantName }}</td>
              <td>{{ m.merchantPhone }}</td>
              <td class="small">{{ m.merchantEmail }}</td>
              <td class="small text-wrap-custom">{{ m.merchantAddress }}</td>
              <td class="text-center">
                <span :class="['badge rounded-pill', m.merchantStatus === 1 ? 'bg-success' : 'bg-danger']">
                  {{ m.merchantStatus === 1 ? '營運中' : '停用中' }}
                </span>
              </td>
              <td class="text-center">
                <div class="btn-group shadow-sm">
                  <button class="btn btn-sm btn-outline-primary px-3" @click="openEditModal(m)">
                    <i class="bi bi-pencil-square"></i> 編輯
                  </button>
                  <button class="btn btn-sm btn-outline-danger px-3" @click="deleteMerchant(m.merchantId, m.merchantName)">
                    <i class="bi bi-trash"></i> 刪除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isEditModalOpen" class="modal-backdrop-custom">
      <div class="modal-dialog-custom card shadow-lg">
        <div class="card-header bg-primary text-white d-flex justify-content-between">
          <h5 class="mb-0">編輯商家資料 - ID: {{ editingForm.merchantId }}</h5>
          <button class="btn-close btn-close-white" @click="isEditModalOpen = false"></button>
        </div>
        <div class="card-body p-4">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-bold">商家名稱</label>
              <input v-model="editingForm.merchantName" type="text" class="form-control">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">聯絡電話</label>
              <input v-model="editingForm.merchantPhone" type="text" class="form-control">
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">電子信箱</label>
              <input v-model="editingForm.merchantEmail" type="email" class="form-control">
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">商家地址</label>
              <textarea v-model="editingForm.merchantAddress" class="form-control" rows="2"></textarea>
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">營運狀態</label>
              <select v-model="editingForm.merchantStatus" class="form-select">
                <option :value="1">營運中 (Active)</option>
                <option :value="0">停用中 (Inactive)</option>
              </select>
            </div>
          </div>
        </div>
        <div class="card-footer bg-light text-end py-3">
          <button class="btn btn-secondary me-2 px-4" @click="isEditModalOpen = false">取消</button>
          <button class="btn btn-primary px-4" @click="handleUpdate">儲存更新</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 原有的 Table 樣式 ... */
.table td { color: #212529 !important; }
.text-wrap-custom { white-space: normal !important; word-break: break-all; }

/* 自定義彈窗樣式 (因為目前沒引入完整 Bootstrap JS) */
.modal-backdrop-custom {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.modal-dialog-custom {
  width: 100%;
  max-width: 600px;
  background: white;
  border-radius: 8px;
}
</style>