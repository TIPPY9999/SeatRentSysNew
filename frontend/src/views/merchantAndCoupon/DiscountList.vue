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
        <div class="input-group input-group-sm" style="width: 350px">
          <input
            v-model="keyword"
            class="form-control"
            placeholder="搜尋名稱..."
            @keyup.enter="fetchDiscounts"
          />
          <button class="btn btn-warning" @click="fetchDiscounts">搜尋</button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light small fw-bold">
            <tr>
              <th class="text-center">ID</th>
              <th>圖片</th>
              <th>優惠名稱 / 內容</th>
              <th>隸屬商家</th>
              <th class="text-center">所需點數</th>
              <th class="text-center">狀態</th>
              <th>活動期限</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in discounts" :key="d.couponId">
              <td class="text-center text-muted small">{{ d.couponId }}</td>
              <td>
                <img v-if="d.couponImg" :src="`http://localhost:8080/images/${d.couponImg}`"
                     class="rounded border" width="50" height="50" />
                <div v-else class="no-img">無圖</div>
              </td>
              <td>
                <div class="fw-bold">{{ d.couponName }}</div>
                <div class="small text-muted text-truncate" style="max-width: 250px" :title="d.couponDescription">
                  {{ d.couponDescription }}
                </div>
              </td>
              <td>
                <span class="badge bg-light text-primary border">
                  {{ d.merchantName || (d.merchant ? d.merchant.merchantName : '未指定') }}
                </span>
              </td>
              <td class="text-center">
                <span class="fw-bold text-orange" style="color: #fd7e14;">{{ d.pointsRequired }}</span> <small>Pts</small>
              </td>
              <td class="text-center">
                <span v-if="d.couponStatus === 0" class="badge bg-secondary">尚未開始</span>
                <span v-else-if="d.couponStatus === 1" class="badge bg-success">進行中</span>
                <span v-else-if="d.couponStatus === 2" class="badge bg-danger">已結束</span>
                <span v-else-if="d.couponStatus === 3" class="badge bg-dark">已下架</span>
                <span v-else class="badge bg-info">未知</span>
              </td>
              <td class="small">{{ d.startDate }} ~ {{ d.endDate }}</td>
              <td class="text-center">
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-primary" @click="openEditModal(d)" title="編輯">
                    <i class="bi bi-pencil"></i>
                  </button>

                  <button 
                    v-if="d.couponStatus === 0 || d.couponStatus === 1"
                    class="btn btn-sm btn-outline-warning" 
                    @click="handleStatusChange(d.couponId, 'disable')"
                    title="下架"
                  >
                    <i class="bi bi-pause-circle"></i>
                  </button>

                  <button 
                    v-if="d.couponStatus === 3"
                    class="btn btn-sm btn-outline-success" 
                    @click="handleStatusChange(d.couponId, 'relist')"
                    title="上架"
                  >
                    <i class="bi bi-play-circle"></i>
                  </button>

                  <button class="btn btn-sm btn-outline-danger" @click="deleteDiscount(d.couponId)" title="刪除">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="discounts.length === 0 && !loading">
              <td colspan="8" class="text-center py-4 text-muted">暫無優惠券資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isEditModalOpen" class="custom-modal-overlay">
      <div class="custom-modal-container card shadow-lg">
        <div class="card-header bg-warning text-dark d-flex justify-content-between">
          <h5 class="mb-0 fw-bold">{{ editingDiscount.couponId ? '編輯' : '新增' }}優惠券</h5>
          <button class="btn-close" @click="isEditModalOpen = false"></button>
        </div>
        <div class="card-body p-4">
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label fw-bold">優惠名稱</label>
              <input v-model="editingDiscount.couponName" type="text" class="form-control" />
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">優惠描述</label>
              <textarea v-model="editingDiscount.couponDescription" class="form-control" rows="2"></textarea>
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">隸屬商家</label>
              <select v-model="editingDiscount.merchantId" class="form-select">
                <option :value="null" disabled>請選擇商家</option>
                <option v-for="m in merchants" :key="m.merchantId" :value="m.merchantId">
                  {{ m.merchantName }}
                </option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">開始日期</label>
              <input v-model="editingDiscount.startDate" type="date" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">結束日期</label>
              <input v-model="editingDiscount.endDate" type="date" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">所需點數</label>
              <input v-model="editingDiscount.pointsRequired" type="number" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-bold">上傳圖片</label>
              <input type="file" class="form-control" @change="onFileChange" accept="image/*" />
            </div>
            <div class="col-12 text-center" v-if="imagePreview">
              <img :src="imagePreview" class="mt-2 rounded border" style="max-height: 120px" />
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

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

// 基本狀態
const discounts = ref([])
const merchants = ref([])
const keyword = ref('')
const loading = ref(false)

// Modal 與圖片控制
const isEditModalOpen = ref(false)
const selectedFile = ref(null)
const imagePreview = ref(null)

const editingDiscount = ref({
  couponId: null,
  couponName: '',
  couponDescription: '',
  merchantId: null,
  couponStatus: 1,
  couponImg: '',
  startDate: '',
  endDate: '',
  pointsRequired: 0,
})

// 1. 取得列表
const fetchDiscounts = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: keyword.value },
    })
    discounts.value = res.data.data || []
  } catch (err) {
    console.error(err)
    Swal.fire('錯誤', '取得清單失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 2. 取得商家選單
const fetchMerchants = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/merchants')
    merchants.value = res.data.data || []
  } catch (err) {
    console.error('載入商家失敗', err)
  }
}

// 3. 上、下架手動切換 (核心功能)
const handleStatusChange = async (id, action) => {
  const actionText = action === 'relist' ? '上架' : '下架';
  
  const result = await Swal.fire({
    title: `確定要${actionText}嗎？`,
    text: action === 'disable' ? '下架後消費者將無法在商城兌換此券' : '上架將根據活動日期自動判定狀態',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: `確定${actionText}`,
    cancelButtonText: '取消'
  });

  if (!result.isConfirmed) return;

  try {
    const res = await axios.put(`http://localhost:8080/api/discounts/${id}/status`, null, {
      params: { action: action }
    });

    if (res.data.code === 200) {
      Swal.fire('成功', `${actionText}成功`, 'success');
      fetchDiscounts();
    } else {
      Swal.fire('失敗', res.data.message || '操作失敗', 'error');
    }
  } catch (err) {
    console.error(err);
    Swal.fire('錯誤', '伺服器連線異常', 'error');
  }
};

// 4. 圖片處理
const onFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    selectedFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

// 5. 開啟 Modal
const openEditModal = (item = null) => {
  if (item) {
    editingDiscount.value = { ...item };
    imagePreview.value = item.couponImg ? `http://localhost:8080/images/${item.couponImg}` : null;
  } else {
    editingDiscount.value = {
      couponId: null, couponName: '', couponDescription: '',
      merchantId: null, couponStatus: 1, couponImg: '',
      startDate: '', endDate: '', pointsRequired: 0,
    };
    imagePreview.value = null;
  }
  selectedFile.value = null;
  isEditModalOpen.value = true;
}

// 6. 儲存
const handleSave = async () => {
  try {
    const formData = new FormData();
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
    } else {
      Swal.fire('失敗', res.data.message, 'error');
    }
  } catch (err) {
    Swal.fire('儲存失敗', '請檢查資料完整性', 'error');
  }
};

// 7. 刪除
const deleteDiscount = (id) => {
  Swal.fire({
    title: '確定刪除？',
    text: '刪除後無法還原！',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '確定刪除',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/discounts/${id}`)
        Swal.fire('已刪除', '優惠券已移除', 'success');
        fetchDiscounts();
      } catch {
        Swal.fire('錯誤', '刪除失敗', 'error');
      }
    }
  })
}

onMounted(() => {
  fetchDiscounts()
  fetchMerchants()
})
</script>

<style scoped>
.custom-modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1050;
}
.custom-modal-container {
  width: 90%; max-width: 550px;
  background: white; border-radius: 12px;
  max-height: 90vh; overflow-y: auto;
}
.no-img {
  width: 50px; height: 50px; background: #f8f9fa;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: #adb5bd; border: 1px dashed #dee2e6;
}
.table-hover tbody tr:hover {
  background-color: rgba(255, 193, 7, 0.05);
}
.btn-group .btn {
  padding: 0.25rem 0.5rem;
}
</style>