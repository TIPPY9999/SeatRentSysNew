<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

const discounts = ref<any[]>([]);
const keyword = ref('');

// 編輯用的變數
const isEditModalOpen = ref(false);
const editingDiscount = ref<any>(null);

// 1. 取得優惠券資料
const fetchDiscounts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/discounts', { 
      params: { keyword: keyword.value } 
    });
    // 偵錯用：確認資料結構
    console.log("Discount Data:", res.data);
    if (res.data) {
      discounts.value = Array.isArray(res.data) ? res.data : res.data.data;
    }
  } catch (e) {
    Swal.fire('錯誤', '無法取得資料', 'error');
  }
};

// 2. 開啟編輯彈窗
const openEditModal = (item: any) => {
  // 深拷貝一份資料，避免直接修改到列表中的原始資料
  editingDiscount.value = JSON.parse(JSON.stringify(item));
  isEditModalOpen.value = true;
};

// 3. 儲存編輯結果
const saveEdit = async () => {
  try {
    const id = editingDiscount.value.couponId;
    await axios.put(`http://localhost:8080/api/discounts/${id}`, editingDiscount.value);
    
    Swal.fire('成功', '優惠券已更新', 'success');
    isEditModalOpen.value = false;
    fetchDiscounts(); // 重新整理列表
  } catch (e) {
    Swal.fire('失敗', '更新失敗', 'error');
  }
};

const getStatusBadge = (status: number) => {
  if (status === 1) return { text: '活動中', class: 'bg-success' };
  if (status === 2) return { text: '已結束', class: 'bg-secondary' };
  return { text: '待啟動', class: 'bg-info' };
};

onMounted(fetchDiscounts);
</script>

<template>
  <div class="card card-outline card-warning shadow-sm">
    <div class="card-header d-flex justify-content-between align-items-center">
      <h3 class="card-title fw-bold">🎫 優惠券管理系統</h3>
      <button class="btn btn-success btn-sm">+ 新增優惠券</button>
    </div>
    
    <div class="card-body p-0">
      <div class="p-3 bg-light border-bottom">
        <div class="input-group input-group-sm" style="width: 350px;">
          <input v-model="keyword" class="form-control" placeholder="搜尋內容..." @keyup.enter="fetchDiscounts">
          <button class="btn btn-warning" @click="fetchDiscounts">搜尋</button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="text-center" style="width: 80px;">ID</th>
              <th>名稱</th>
              <th>隸屬商家</th> <th class="text-center">狀態</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in discounts" :key="d.couponId">
              <td class="text-center text-muted small">{{ d.couponId }}</td>
              <td>
                <div class="fw-bold text-dark">{{ d.couponName }}</div>
              </td>
              <td>
                <span class="badge bg-light text-primary border px-2">
                  <i class="bi bi-shop me-1"></i>
                  {{ d.merchantName || d.merchant?.merchantName || '無商家資料' }}
                </span>
              </td>
              <td class="text-center">
                <span :class="['badge rounded-pill', getStatusBadge(d.couponStatus || 1).class]">
                  {{ getStatusBadge(d.couponStatus || 1).text }}
                </span>
              </td>
              <td class="text-center">
                <div class="btn-group shadow-sm">
                  <button class="btn btn-sm btn-outline-primary" @click="openEditModal(d)">
                    <i class="bi bi-pencil-square"></i> 編輯
                  </button>
                  <button class="btn btn-sm btn-outline-danger">
                    <i class="bi bi-trash"></i> 刪除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isEditModalOpen" class="custom-modal-overlay">
      <div class="custom-modal-content card shadow-lg">
        <div class="card-header bg-primary text-white">
          <h5 class="mb-0">編輯優惠券 #{{ editingDiscount.couponId }}</h5>
        </div>
        <div class="card-body">
          <div class="mb-3">
            <label class="form-label">優惠名稱</label>
            <input v-model="editingDiscount.couponName" class="form-control" type="text">
          </div>
          <div class="mb-3">
            <label class="form-label">描述</label>
            <textarea v-model="editingDiscount.couponDescription" class="form-control"></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label">狀態</label>
            <select v-model="editingDiscount.couponStatus" class="form-select">
              <option :value="1">活動中</option>
              <option :value="2">已結束</option>
            </select>
          </div>
        </div>
        <div class="card-footer text-end bg-light">
          <button class="btn btn-secondary me-2" @click="isEditModalOpen = false">取消</button>
          <button class="btn btn-primary" @click="saveEdit">儲存變更</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 讓表格顯示正常的顏色 */
.table td { color: #212529 !important; }

/* 簡易彈窗樣式 */
.custom-modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1050;
}
.custom-modal-content {
  width: 500px;
  background: white;
  border-radius: 8px;
}
</style>