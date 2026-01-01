<script setup>
/**
 * DiscountList.vue：優惠券管理系統
 * [修正] 移除 TypeScript 語法，改為純 JS 版本
 * [修正] 移除 catch 區塊中未使用的錯誤變數 e，解決 ESLint 警告
 */
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

// [狀態] 基本資料
const discounts = ref([])
const merchants = ref([])
const keyword = ref('')
const loading = ref(false)

// [狀態] Modal 控制與圖片處理
const isEditModalOpen = ref(false)
const selectedFile = ref(null)
const imagePreview = ref(null)

// [物件] 編輯中的優惠券初始結構
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

// 1. 取得優惠券清單
const fetchDiscounts = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/discounts', {
      params: { keyword: keyword.value },
    })
    // 對應後端 Result 結構：res.data.data
    discounts.value = res.data.data || []
  } catch {
    // ✅ 修正：移除未使用的 (e)
    Swal.fire('錯誤', '取得清單失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 2. 取得商家列表 (供下拉選單使用)
const fetchMerchants = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/merchants')
    merchants.value = res.data.data || []
  } catch (e) {
    // 這裡有用到 e，所以保留
    console.error('載入商家失敗', e)
  }
}

// 3. 圖片選取處理
const onFileChange = (e) => {
  // 這裡有用到 e，所以保留
  const file = e.target.files[0]
  if (file) {
    selectedFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

// 4. 開啟編輯/新增視窗
const openEditModal = (item = null) => {
  if (item) {
    // 編輯模式
    editingDiscount.value = { ...item }
    imagePreview.value = item.couponImg ? `http://localhost:8080/images/${item.couponImg}` : null
  } else {
    // 新增模式
    editingDiscount.value = {
      couponId: null,
      couponName: '',
      couponDescription: '',
      merchantId: null,
      couponStatus: 1,
      couponImg: '',
      startDate: '',
      endDate: '',
      pointsRequired: 0,
    }
    imagePreview.value = null
  }
  selectedFile.value = null
  isEditModalOpen.value = true
}

// 5. 儲存資料 (包含檔案上傳)
const handleSave = async () => {
try {
    const formData = new FormData();

    // 1. 包裝 JSON (確保包含 couponId 以觸發修改)
    const jsonBlob = new Blob([JSON.stringify(editingDiscount.value)], { type: 'application/json' });
    formData.append('discount', jsonBlob);

    // 2. 檔案 Key 必須是 'image' (對應後端 @RequestPart("image"))
    if (selectedFile.value) {
      formData.append('image', selectedFile.value);
    }

    // 3. 發送請求
    const res = await axios.post('http://localhost:8080/api/discounts', formData);

    if (res.data.code === 200) {
      Swal.fire('成功', '資料已儲存', 'success');
      isEditModalOpen.value = false;
      fetchDiscounts(); // 重新整理清單
    }
  } catch (e) {
    // 如果報錯 400 或 500，這裡會抓到
    console.error("儲存失敗：", e.response?.data);
    Swal.fire('儲存失敗', e.response?.data?.message || '網路錯誤', 'error');
  }
};

// 6. 刪除優惠券
const deleteDiscount = (id) => {
  Swal.fire({
    title: '確定刪除？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/discounts/${id}`)
        fetchDiscounts()
      } catch {
        // ✅ 修正：移除未使用的 (e)
        Swal.fire('錯誤', '刪除失敗', 'error')
      }
    }
  })
}

onMounted(() => {
  fetchDiscounts()
  fetchMerchants()
})
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
                <img
                  v-if="d.couponImg"
                  :src="`http://localhost:8080/images/${d.couponImg}`"
                  class="rounded border"
                  width="50"
                  height="50"
                />
                <div v-else class="no-img">無圖</div>
              </td>
              <td>
                <div class="fw-bold">{{ d.couponName }}</div>
                <div class="small text-muted text-truncate" style="max-width: 200px">
                  {{ d.couponDescription }}
                </div>
              </td>
              <td>
                <span class="badge bg-light text-primary border">{{
                  d.merchantName || (d.merchant ? d.merchant.merchantName : '')
                }}</span>
              </td>
              <td class="small">{{ d.startDate }} ~ {{ d.endDate }}</td>
              <td class="text-center">
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-primary" @click="openEditModal(d)">
                    編輯
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteDiscount(d.couponId)">
                    刪除
                  </button>
                </div>
              </td>
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
              <textarea
                v-model="editingDiscount.couponDescription"
                class="form-control"
                rows="3"
              ></textarea>
            </div>
            <div class="col-12">
              <label class="form-label fw-bold">隸屬商家</label>
              <select v-model="editingDiscount.merchantId" class="form-select">
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
              <input type="file" class="form-control" @change="onFileChange" />
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

<style scoped>
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.custom-modal-container {
  width: 500px;
  background: white;
  border-radius: 8px;
}
.no-img {
  width: 50px;
  height: 50px;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #999;
}
</style>
