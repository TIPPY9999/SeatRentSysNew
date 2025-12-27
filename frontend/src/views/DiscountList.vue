<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

// --- 資料狀態 ---
const discounts = ref<any[]>([]);
const merchants = ref<any[]>([]);
const keyword = ref('');
const loading = ref(false);

// --- 編輯 Modal 狀態 ---
const isEditModalOpen = ref(false);
const selectedFile = ref<File | null>(null);
const imagePreview = ref<string | null>(null);

// 核心修正：加入 startDate 和 endDate 欄位
const editingDiscount = ref<any>({
  couponId: null,
  couponName: '',
  couponDescription: '',
  merchantId: null,
  couponStatus: 1,
  couponImg: '',
  startDate: '', 
  endDate: '',
  pointsRequired: 0
});

// 1. 取得清單
const fetchDiscounts = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: keyword.value }
    });
    discounts.value = res.data.data || [];
  } catch (e) {
    Swal.fire('錯誤', '取得清單失敗', 'error');
  } finally {
    loading.value = false;
  }
};

// 2. 取得商家 (用於下拉選單)
const fetchMerchants = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/merchants');
    merchants.value = res.data.data || [];
  } catch (e) { console.error("載入商家失敗", e); }
};

// 3. 圖片處理
const onFileChange = (e: any) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreview.value = URL.createObjectURL(file);
  }
};

// 4. 開啟 Modal (修正日期載入)
const openEditModal = (item: any = null) => {
  if (item) {
    editingDiscount.value = { ...item };
    // 確保圖片預覽路徑
    imagePreview.value = item.couponImg ? `http://localhost:8080/images/${item.couponImg}` : null;
  } else {
    editingDiscount.value = { 
      couponId: null, couponName: '', couponStatus: 1, 
      merchantId: null, startDate: '', endDate: '', pointsRequired: 0 
    };
    imagePreview.value = null;
  }
  selectedFile.value = null;
  isEditModalOpen.value = true;
};

// 5. 儲存 (關鍵修復：FormData + Blob)
const handleSave = async () => {
  if (!editingDiscount.value.merchantId) {
    Swal.fire('提醒', '請選擇商家', 'warning');
    return;
  }

  try {
    const formData = new FormData();
    // 將 JSON 資料轉為 Blob
    const jsonBlob = new Blob([JSON.stringify(editingDiscount.value)], { type: 'application/json' });
    formData.append('discount', jsonBlob);

    if (selectedFile.value) {
      formData.append('image', selectedFile.value);
    }

    const res = await axios.post('http://localhost:8080/api/discounts', formData);

    if (res.data.code === 200) {
      Swal.fire('成功', '資料已儲存', 'success');
      isEditModalOpen.value = false;
      fetchDiscounts();
    }
  } catch (e: any) {
    Swal.fire('儲存失敗', e.response?.data?.message || '網路錯誤', 'error');
  }
};

const deleteDiscount = (id: number) => {
  Swal.fire({
    title: '確定刪除？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定'
  }).then(async (result) => {
    if (result.isConfirmed) {
      await axios.delete(`http://localhost:8080/api/discounts/${id}`);
      fetchDiscounts();
    }
  });
};

onMounted(() => {
  fetchDiscounts();
  fetchMerchants();
});
</script>

<template>
  <div class="card card-outline card-warning shadow-sm border-0">
    <div class="card-header d-flex justify-content-between align-items-center py-3">
      <h3 class="card-title fw-bold mb-0">🎫 優惠券管理系統</h3>
      <button class="btn btn-success btn-sm fw-bold px-3" @click="openEditModal()">
        <i class="bi bi-plus-lg"></i> 新增優惠券
      </button>
    </div>

    <div class="card-body p-0">
      <div class="p-3 bg-light border-bottom">
        <div class="input-group input-group-sm" style="width: 350px;">
          <input v-model="keyword" class="form-control" placeholder="搜尋名稱..." @keyup.enter="fetchDiscounts">
          <button class="btn btn-warning" @click="fetchDiscounts">搜尋</button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light small fw-bold">
            <tr>
              <th class="text-center">ID</th>
              <th>圖片</th>
              <th>優惠名稱</th>
              <th>隸屬商家</th>
              <th>活動期限</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in discounts" :key="d.couponId">
              <td class="text-center text-muted small">{{ d.couponId }}</td>
              <td>
                <img v-if="d.couponImg" :src="`http://localhost:8080/images/${d.couponImg}`" class="rounded border" width="50" height="50">
                <div v-else class="no-img">無圖</div>
              </td>
              <td>
                <div class="fw-bold">{{ d.couponName }}</div>
                <div class="small text-muted text-truncate" style="max-width: 200px;">{{ d.couponDescription }}</div>
              </td>
              <td>
                <span class="badge bg-light text-primary border">{{ d.merchantName || d.merchant?.merchantName }}</span>
              </td>
              <td class="small">{{ d.startDate }} ~ {{ d.endDate }}</td>
              <td class="text-center">
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-primary" @click="openEditModal(d)">編輯</button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteDiscount(d.couponId)">刪除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isEditModalOpen" class="custom-modal-overlay">
      <div class="custom-modal-container card">
        <div class="card-header bg-warning text-dark d-flex justify-content-between">
          <h5 class="mb-0 fw-bold">{{ editingDiscount.couponId ? '編輯' : '新增' }}優惠券</h5>
          <button class="btn-close" @click="isEditModalOpen = false"></button>
        </div>
        <div class="card-body p-4">
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label fw-bold">優惠名稱</label>
              <input v-model="editingDiscount.couponName" type="text" class="form-control">
            </div>
            <div class="col-12">
  <label class="form-label fw-bold">優惠描述</label>
  <textarea 
    v-model="editingDiscount.couponDescription" 
    class="form-control" 
    rows="3" 
    placeholder="請輸入優惠詳細說明..."
  ></textarea>
</div>
            <div class="col-12">
              <label class="form-label fw-bold">隸屬商家</label>
              <select v-model="editingDiscount.merchantId" class="form-select">
                <option v-for="m in merchants" :key="m.merchantId" :value="m.merchantId">{{ m.merchantName }}</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">開始日期</label>
              <input v-model="editingDiscount.startDate" type="date" class="form-control">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">結束日期</label>
              <input v-model="editingDiscount.endDate" type="date" class="form-control">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">所需點數</label>
              <input v-model="editingDiscount.pointsRequired" type="number" class="form-control">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">上傳圖片</label>
              <input type="file" class="form-control" @change="onFileChange">
            </div>
            <div class="col-12 text-center" v-if="imagePreview">
              <img :src="imagePreview" class="mt-2 rounded border" style="max-height: 120px;">
            </div>
          </div>
        </div>
        <div class="card-footer text-end">
          <button class="btn btn-secondary me-2" @click="isEditModalOpen = false">取消</button>
          <button class="btn btn-primary px-4" @click="handleSave">儲存資料</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 9999;
}
.custom-modal-container { width: 500px; background: white; border-radius: 8px; }
.no-img { width: 50px; height: 50px; background: #eee; display: flex; align-items: center; justify-content: center; font-size: 10px; color: #999; }
</style>