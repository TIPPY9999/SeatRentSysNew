<template>
  <div class="spot-form-container">
    <!-- ========== Header 區域 ========== -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-sm-6">
            <div class="page-title-box">
              <div class="title-icon" :class="isEditMode ? 'edit-mode' : 'add-mode'">
                <i :class="isEditMode ? 'fas fa-edit' : 'fas fa-plus-circle'"></i>
              </div>
              <div class="title-content">
                <h1>{{ isEditMode ? '編輯據點' : '新增據點' }}</h1>
                <p class="subtitle">{{ isEditMode ? '修改據點資訊' : '建立新的營業據點' }}</p>
              </div>
            </div>
          </div>
          <div class="col-sm-6 text-right">
            <el-button @click="goBack">
              <i class="fas fa-arrow-left mr-1"></i> 返回列表
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <transition name="zoom-fade" appear>
          <el-card shadow="hover" class="form-card">
            <el-form
              :model="formData"
              label-width="120px"
              label-position="top"
              class="modern-form"
              @submit.prevent="submitForm"
            >
              <!-- ========== 基本資料區 ========== -->
              <div class="form-section">
                <div class="section-header">
                  <i class="fas fa-info-circle"></i>
                  <span>基本資料</span>
                </div>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="據點代碼 (Code)" required>
                      <el-input v-model="formData.spotCode" placeholder="例如: TP001" clearable>
                        <template #prefix><i class="fas fa-barcode"></i></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="據點名稱 (Name)" required>
                      <el-input v-model="formData.spotName" placeholder="例如: 台北車站店" clearable>
                        <template #prefix><i class="fas fa-store"></i></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-form-item label="地址 (Address)" required>
                      <el-input v-model="formData.spotAddress" placeholder="請輸入完整地址" clearable>
                        <template #prefix><i class="fas fa-map-marker-alt"></i></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="所屬商家 ID (Merchant ID)">
                      <el-input v-model.number="formData.merchantId" type="number" :min="1" placeholder="無商家可留空" clearable>
                        <template #prefix><i class="fas fa-building"></i></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="描述 (Description)">
                  <el-input v-model="formData.spotDescription" type="textarea" :rows="3" placeholder="請輸入據點描述..." />
                </el-form-item>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="狀態">
                      <el-select v-model="formData.spotStatus" style="width: 100%">
                        <el-option label="營運中" value="營運中">
                          <i class="fas fa-check-circle text-success mr-2"></i> 營運中
                        </el-option>
                        <el-option label="維修中" value="維修中">
                          <i class="fas fa-tools text-warning mr-2"></i> 維修中
                        </el-option>
                        <el-option label="停用" value="停用">
                          <i class="fas fa-ban text-danger mr-2"></i> 停用
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>

              <!-- ========== 圖片上傳區 (整合原有功能) ========== -->
              <div class="form-section">
                <div class="section-header">
                  <i class="fas fa-image"></i>
                  <span>據點圖片</span>
                </div>

                <el-form-item>
                  <div class="image-upload-area">
                    <input 
                      type="file" 
                      @change="handleFileChange" 
                      accept="image/*"
                      class="file-input"
                      id="image-upload"
                    />
                    <label for="image-upload" class="upload-label">
                      <i class="fas fa-cloud-upload-alt"></i>
                      <span>點擊或拖曳上傳圖片</span>
                    </label>
                  </div>
                  
                  <!-- 預覽區域 -->
                  <div v-if="previewUrl" class="preview-section">
                    <p class="preview-title">圖片預覽：</p>
                    <img :src="previewUrl" alt="預覽圖片" class="preview-image" />
                  </div>
                </el-form-item>
              </div>

              <!-- ========== 按鈕區 ========== -->
              <div class="form-actions">
                <el-button @click="goBack" class="back-btn">
                  <i class="fas fa-times mr-1"></i> 取消
                </el-button>
                <el-button type="primary" @click="submitForm" :loading="isSubmitting" class="submit-btn">
                  <i class="fas fa-save mr-1"></i> {{ isSubmitting ? '處理中...' : '儲存資料' }}
                </el-button>
              </div>
            </el-form>
          </el-card>
        </transition>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import Swal from 'sweetalert2';

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
  spotStatus: '營運中',
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
      Swal.fire({
        title: '載入失敗',
        html: '<div style="display:flex;flex-direction:column;align-items:center;"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg><p style="margin-top:12px;">無法載入據點資料</p></div>',
        confirmButtonColor: '#409eff',
        confirmButtonText: '確定'
      });
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
    // 1. 建構乾淨的 Payload，避免將 GET 取得的額外欄位 (如 merchant 物件) 傳回後端導致錯誤
    const payload = {
      spotCode: formData.value.spotCode,
      spotName: formData.value.spotName,
      spotAddress: formData.value.spotAddress,
      spotDescription: formData.value.spotDescription,
      spotStatus: formData.value.spotStatus,
      spotImage: formData.value.spotImage, // 若有圖片路徑則保留
      // 處理 merchantId: 若為空值則送 null，確保型別正確
      merchantId: (formData.value.merchantId === '' || formData.value.merchantId === null || formData.value.merchantId === undefined) ? null : Number(formData.value.merchantId)
    };

    if (isEditMode.value) {
      // 編輯模式：確保 Body 內也有 ID，許多後端邏輯會檢查 Path ID 與 Body ID 是否一致
      payload.spotId = Number(route.params.id);
      await axios.put(`/api/spot/${route.params.id}`, payload);
      await Swal.fire({
        title: '更新成功',
        html: '<div style="display:flex;flex-direction:column;align-items:center;"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg><p style="margin-top:12px;">據點資料已成功更新</p></div>',
        confirmButtonColor: '#409eff',
        confirmButtonText: '確定'
      });
    } else {
      await axios.post('/api/spot', payload);
      await Swal.fire({
        title: '新增成功',
        html: '<div style="display:flex;flex-direction:column;align-items:center;"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg><p style="margin-top:12px;">據點資料已成功新增</p></div>',
        confirmButtonColor: '#409eff',
        confirmButtonText: '確定'
      });
    }
    
    router.push({ name: 'spot-list' }); // 成功後回列表
  } catch (error) {
    console.error('儲存錯誤:', error);
    // 顯示後端回傳的具體錯誤訊息，方便除錯
    const msg = error.response?.data?.message || error.message || '儲存失敗，請檢查輸入資料或後端連線';
    Swal.fire({
      title: '操作失敗',
      html: `<div style="display:flex;flex-direction:column;align-items:center;"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg><p style="margin-top:12px;">${msg}</p></div>`,
      confirmButtonColor: '#409eff',
      confirmButtonText: '確定'
    });
  } finally {
    isSubmitting.value = false;
  }
};

const goBack = () => {
  router.back();
}
</script>

<style scoped>
/* ========== 頁面容器 ========== */
.spot-form-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

/* ========== 頁面標題區 ========== */
.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.title-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  transition: transform 0.3s ease;
}

.title-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.add-mode {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.edit-mode {
  background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%);
}

.title-content h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
}

.title-content .subtitle {
  margin: 4px 0 0;
  font-size: 0.875rem;
  color: #909399;
}

/* ========== 表單卡片 ========== */
.form-card {
  border-radius: 12px;
  max-width: 900px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

/* ========== 圖片上傳 ========== */
.image-upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.image-upload-area:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.05);
}

.file-input {
  display: none;
}

.upload-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  color: #909399;
}

.upload-label i {
  font-size: 48px;
  color: #c0c4cc;
}

.preview-section {
  margin-top: 16px;
  text-align: center;
}

.preview-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.preview-image {
  max-height: 200px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* ========== 按鈕區 ========== */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  margin-top: 24px;
  border-top: 1px solid #ebeef5;
}

.submit-btn {
  min-width: 140px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border: none;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.back-btn {
  min-width: 100px;
  border-radius: 10px;
}

/* ========== 動畫效果 ========== */
.zoom-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.zoom-fade-leave-active {
  transition: all 0.3s ease-in;
}

.zoom-fade-enter-from {
  transform: scale(0.9);
  opacity: 0;
}

.zoom-fade-leave-to {
  transform: scale(0.95);
  opacity: 0;
}

/* ========== 工具類 ========== */
.mr-1 { margin-right: 4px; }
.mr-2 { margin-right: 8px; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .page-title-box {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}
</style>
