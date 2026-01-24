<script setup>
/**
 * AdminCreateView.vue：新增管理員
 */
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

const admin = reactive({
  admUsername: '',
  admPassword: '',
  admName: '',
  admEmail: '',
  admRole: 1,
})

const submitCreate = () => {
  axios
    .post('http://localhost:8080/admins', {
      ...admin,
    })
    .then((res) => {
      Swal.fire({
        title: '新增成功',
        html: `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="16 12 12 8 8 12"></polyline><line x1="12" y1="16" x2="12" y2="8"></line></svg><br><br>${res.data || '管理員已成功建立'}`,
        confirmButtonColor: '#409eff'
      }).then(() => {
        router.push('/admin/admins')
      })
    })
    .catch((err) => {
      Swal.fire({
        title: '新增失敗',
        html: `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg><br><br>${err.response?.data || '請檢查輸入資料'}`,
        confirmButtonColor: '#409eff'
      })
    })
}

const goBack = () => {
  router.push('/admin/admins')
}
</script>

<template>
  <div class="create-page">
    <div class="form-card">
      <!-- 標題區域 -->
      <div class="card-header">
        <div class="header-icon">
          <i class="fas fa-user-plus"></i>
        </div>
        <h2>新增管理員</h2>
        <p>建立新的系統管理員帳號</p>
      </div>

      <!-- 表單內容 -->
      <el-form @submit.prevent="submitCreate" label-position="top" class="admin-form">
        <el-form-item label="帳號" required>
          <el-input
            v-model="admin.admUsername"
            placeholder="請輸入帳號"
            prefix-icon="User"
            autocomplete="new-username"
          />
        </el-form-item>

        <el-form-item label="密碼" required>
          <el-input
            type="password"
            v-model="admin.admPassword"
            placeholder="請輸入密碼"
            prefix-icon="Lock"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" required>
          <el-input
            v-model="admin.admName"
            placeholder="請輸入姓名"
            prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item label="信箱" required>
          <el-input
            type="email"
            v-model="admin.admEmail"
            placeholder="請輸入電子郵件"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="admin.admRole" placeholder="選擇角色" style="width: 100%">
            <el-option :value="1" label="一般管理員">
              <span class="role-option">
                <i class="fas fa-user-tie mr-2"></i> 一般管理員
              </span>
            </el-option>
            <el-option :value="9" label="超級管理員">
              <span class="role-option">
                <i class="fas fa-crown mr-2"></i> 超級管理員
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" size="large" @click="submitCreate" class="submit-btn">
            <i class="fas fa-check mr-2"></i> 確認新增
          </el-button>
          <el-button size="large" @click="goBack" class="back-btn">
            <i class="fas fa-arrow-left mr-2"></i> 回管理員列表
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* ========== 頁面容器 ========== */
.create-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========== 表單卡片 ========== */
.form-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 標題區域 ========== */
.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #e8f4fc 0%, #d4e8f7 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #5b9bd5;
  margin: 0 auto 16px;
  box-shadow: 0 4px 15px rgba(91, 155, 213, 0.15);
}

.card-header h2 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: #303133;
}

.card-header p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 0.95rem;
}

/* ========== 表單樣式 ========== */
.admin-form {
  margin-top: 24px;
}

.admin-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #606266;
}

.admin-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
}

.admin-form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
}

.role-option {
  display: flex;
  align-items: center;
}

/* ========== 按鈕區域 ========== */
.form-actions {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #5b9bd5 0%, #7cb9e8 100%);
  border: none;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(91, 155, 213, 0.35);
}

.back-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 10px;
  border: 2px solid #e8ecf1;
  background: transparent;
  color: #606266;
  transition: all 0.3s ease;
}

.back-btn:hover {
  border-color: #5b9bd5;
  color: #5b9bd5;
  background: rgba(91, 155, 213, 0.05);
}

/* ========== 工具類 ========== */
.mr-2 { margin-right: 8px; }

/* ========== 響應式設計 ========== */
@media (max-width: 520px) {
  .form-card {
    padding: 24px;
    margin: 16px;
  }
  
  .header-icon {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }
}
</style>