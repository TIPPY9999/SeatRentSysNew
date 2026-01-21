<template>
  <div class="spot-form container mt-4">
    <div class="card shadow-sm">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h3 class="mb-0">{{ isEditMode ? '編輯據點' : '新增據點' }}</h3>
        <button class="btn btn-light btn-sm" @click="goBack">
          <i class="fas fa-times"></i> 取消
        </button>
      </div>
      
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <!-- 基本資料區 -->
          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label">據點代碼 (Code) <span class="text-danger">*</span></label>
              <input v-model="formData.spotCode" type="text" class="form-control" required placeholder="例如: TP001" />
            </div>
            <div class="col-md-6">
              <label class="form-label">據點名稱 (Name) <span class="text-danger">*</span></label>
              <input v-model="formData.spotName" type="text" class="form-control" required placeholder="例如: 台北車站店" />
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-8">
              <label class="form-label">地址 (Address) <span class="text-danger">*</span></label>
              <input v-model="formData.spotAddress" type="text" class="form-control" required />
            </div>
            <div class="col-md-4">
              <label class="form-label">所屬商家 ID (Merchant ID) <span class="text-danger">*</span></label>
              <input v-model.number="formData.merchantId" type="number" class="form-control" required min="1" />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label">描述 (Description)</label>
            <textarea v-model="formData.spotDescription" class="form-control" rows="3"></textarea>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label">狀態</label>
              <select v-model="formData.spotStatus" class="form-select">
                <option value="啟用">啟用</option>
                <option value="停用">停用</option>
              </select>
            </div>
          </div>

          <!-- 圖片上傳區 (整合原有功能) -->
          <div class="mb-4 p-3 border rounded bg-light">
            <label class="form-label fw-bold">據點圖片</label>
            <input 
              type="file" 
              @change="handleFileChange" 
              accept="image/*"
              class="form-control mb-2"
            />
            
            <!-- 預覽區域 -->
            <div v-if="previewUrl" class="preview-section">
              <p class="text-muted small mb-1">圖片預覽：</p>
              <img :src="previewUrl" alt="預覽圖片" class="img-thumbnail" style="max-height: 200px;" />
            </div>
          </div>

          <!-- 按鈕區 -->
          <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-secondary me-2" @click="goBack">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <i class="fas fa-save"></i> {{ isSubmitting ? '處理中...' : '儲存資料' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

// 明確定義元件名稱，方便識別與除錯
defineOptions({
  name: 'SpotForm'
})

const route = useRoute();
const router = useRouter();

// 狀態定義
const isEditMode = computed(() => !!route.params.id); // 有 ID 就是編輯模式
const isSubmitting = ref(false);
const selectedFile = ref(null);
const previewUrl = ref('');

// 表單資料模型
const formData = ref({
  spotCode: '',
  spotName: '',
  spotAddress: '',
  merchantId: '',
  spotDescription: '',
  spotStatus: '啟用',
  spotImage: '' 
});

// 初始化：如果是編輯模式，載入資料
onMounted(async () => {
  if (isEditMode.value) {
    try {
      const res = await axios.get(`/api/spot/${route.params.id}`);
      formData.value = res.data;
      // 若原本有圖片，顯示預覽
      if (formData.value.spotImage) {
        previewUrl.value = formData.value.spotImage;
      }
    } catch (err) {
      console.error('載入失敗', err);
      alert('無法載入據點資料');
    }
  }
});

// 處理檔案選擇
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

// 提交表單
const submitForm = async () => {
  isSubmitting.value = true;
  try {
    // 這裡示範傳送 JSON 資料
    // 若需上傳圖片，建議改用 FormData 或分兩階段上傳
    if (isEditMode.value) {
      await axios.put(`/api/spot/${route.params.id}`, formData.value);
      alert('更新成功！');
    } else {
      await axios.post('/api/spot', formData.value);
      alert('新增成功！');
    }
    
    router.push({ name: 'spot-list' }); // 成功後回列表
  } catch (error) {
    console.error(error);
    alert('儲存失敗，請檢查輸入資料或後端連線');
  } finally {
    isSubmitting.value = false;
  }
};

const goBack = () => {
  router.back();
}
</script>

<style scoped>
/* 樣式已改用 Bootstrap class */
</style>
