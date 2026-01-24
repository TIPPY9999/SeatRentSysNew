<script setup>
/**
 * MerchantList.vue：商家管理系統
 * [修正] 移除 TypeScript 語法，回歸純 JS 邏輯
 * [修正] 移除 catch 區塊中未使用的錯誤變數 e，解決 ESLint 警告
 */
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'

const merchants = ref([])
const keyword = ref('')
const loading = ref(false)
const isEditModalOpen = ref(false)

// 表單初始狀態
const initialForm = {
  merchantId: null,
  merchantName: '',
  merchantPhone: '',
  merchantEmail: '',
  merchantAddress: '',
  merchantStatus: 1,
}

// 響應式表單物件
const editingForm = ref({ ...initialForm })

// 1. 取得商家清單
const fetchMerchants = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/merchants', {
      params: { keyword: keyword.value },
    })
    // 對應後端 Result 結構
    merchants.value = res.data.data || []
  } catch {
    // ✅ 修正：移除未使用的 (e)
    Swal.fire('錯誤', '連線失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 2. 開啟彈窗 (相容新增與編輯)
const openEditModal = (m = null) => {
  if (m) {
    // 編輯模式：深拷貝資料以免影響列表即時顯示
    editingForm.value = JSON.parse(JSON.stringify(m))
  } else {
    // 新增模式：還原為空表單
    editingForm.value = { ...initialForm }
  }
  isEditModalOpen.value = true
}

// 3. 儲存商家資料 (新增或修改)
const handleSave = async () => {
  const id = editingForm.value.merchantId
  try {
    if (id) {
      // 編輯模式：PUT /api/merchants/{id}
      await axios.put(`http://localhost:8080/api/merchants/${id}`, editingForm.value)
    } else {
      // 新增模式：POST /api/merchants
      await axios.post('http://localhost:8080/api/merchants', editingForm.value)
    }

    Swal.fire('成功', '資料已儲存', 'success')
    isEditModalOpen.value = false
    fetchMerchants()
  } catch (e) {
    // 這裡有用到 e.response 顯示具體錯誤，所以保留
    Swal.fire('失敗', '操作失敗：' + (e.response?.data?.message || '未知錯誤'), 'error')
  }
}

// 4. 刪除商家
const deleteMerchant = (id, name) => {
  Swal.fire({
    title: `確定要刪除「${name}」嗎？`,
    text: '若此商家有優惠券資料，可能會導致刪除失敗！',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '確定刪除',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const res = await axios.delete(`http://localhost:8080/api/merchants/${id}`)

        // 檢查後端 Result code (200=成功, 500=失敗)
        if (res.data.code === 200) {
          Swal.fire('已刪除', '商家已從系統移除', 'success')
          fetchMerchants()
        } else {
          Swal.fire('失敗', res.data.message, 'error')
        }
      } catch {
        // ✅ 修正：移除未使用的 (e)
        Swal.fire('失敗', '伺服器拒絕刪除', 'error')
      }
    }
  })
}

const resetSearch = () => {
  keyword.value = ''
  fetchMerchants()
}

onMounted(fetchMerchants)
</script>

<template>
  <div class="merchant-list-container">
    <!-- ========== Header 區域 ========== -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row align-items-center">
          <div class="col-12">
            <div class="page-title-box">
              <div class="title-icon">
                <i class="fas fa-store"></i>
              </div>
              <div class="title-content">
                <h1>商家管理系統</h1>
                <p class="subtitle">管理合作商家資訊與狀態</p>
              </div>
              <div class="title-actions">
                <el-button type="success" class="action-btn add-btn" @click="openEditModal()">
                  <i class="fas fa-plus mr-1"></i> 新增商家
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 統計卡片 ========== -->
    <section class="content">
      <div class="container-fluid">
        <transition name="fade-slide" appear>
          <div>
            <el-row :gutter="20" class="mb-4">
              <el-col :xs="12" :sm="6">
                <div class="stat-card total-card">
                  <div class="stat-icon"><i class="fas fa-store"></i></div>
                  <div class="stat-info">
                    <h3>{{ merchants.length }}</h3>
                    <span>總商家數</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="6">
                <div class="stat-card active-card">
                  <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
                  <div class="stat-info">
                    <h3>{{ merchants.filter((m) => m.merchantStatus === 1).length }}</h3>
                    <span>營運中</span>
                  </div>
                </div>
              </el-col>
              <el-col :xs="12" :sm="6">
                <div class="stat-card inactive-card">
                  <div class="stat-icon"><i class="fas fa-pause-circle"></i></div>
                  <div class="stat-info">
                    <h3>{{ merchants.filter((m) => m.merchantStatus === 0).length }}</h3>
                    <span>停用中</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- ========== 主要表格卡片 ========== -->
            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="card-header-content">
                  <div class="header-left">
                    <span class="header-icon"><i class="fas fa-list"></i></span>
                    <span class="header-text">商家列表</span>
                    <el-tag type="primary" effect="light" size="small" round
                      >{{ merchants.length }} 筆</el-tag
                    >
                  </div>
                </div>
              </template>

              <!-- 搜尋區塊 -->
              <div class="filter-bar">
                <el-input
                  v-model="keyword"
                  placeholder="搜尋名稱或地址..."
                  prefix-icon="Search"
                  clearable
                  class="filter-input"
                  @keyup.enter="fetchMerchants"
                />
                <el-button type="primary" @click="fetchMerchants" class="search-btn">
                  <i class="fas fa-search mr-1"></i> 搜尋
                </el-button>
                <el-button @click="resetSearch" class="reset-btn">
                  <i class="fas fa-redo mr-1"></i> 重置
                </el-button>
              </div>

              <!-- 表格 -->
              <el-table
                :data="merchants"
                v-loading="loading"
                stripe
                highlight-current-row
                style="width: 100%"
                :header-cell-style="{ background: '#f5f7fa', fontWeight: 'bold', color: '#303133' }"
                class="modern-table"
              >
                <el-table-column prop="merchantId" label="ID" width="80" align="center">
                  <template #default="{ row }">
                    <span class="id-tag">#{{ row.merchantId }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="merchantName" label="商家名稱" min-width="150">
                  <template #default="{ row }">
                    <div class="merchant-name-cell">
                      <i class="fas fa-store text-primary mr-2"></i>
                      <span class="name-text">{{ row.merchantName }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="merchantAddress" label="地址" min-width="200">
                  <template #default="{ row }">
                    <div class="address-cell">
                      <i class="fas fa-map-marker-alt text-warning mr-2"></i>
                      <span>{{ row.merchantAddress || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="merchantPhone" label="電話" width="140">
                  <template #default="{ row }">
                    <span class="phone-text"
                      ><i class="fas fa-phone mr-1"></i>{{ row.merchantPhone || '-' }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column prop="merchantEmail" label="Gmail" min-width="180">
                  <template #default="{ row }">
                    <span class="email-text"
                      ><i class="fas fa-envelope mr-1"></i>{{ row.merchantEmail || '-' }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column prop="merchantStatus" label="狀態" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag
                      :type="row.merchantStatus === 1 ? 'success' : 'danger'"
                      effect="light"
                      size="small"
                      class="status-tag"
                    >
                      <i
                        :class="
                          row.merchantStatus === 1 ? 'fas fa-check-circle' : 'fas fa-times-circle'
                        "
                        class="mr-1"
                      ></i>
                      {{ row.merchantStatus === 1 ? '營運中' : '停用中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button-group>
                      <el-tooltip content="編輯" placement="top">
                        <el-button size="small" type="primary" @click="openEditModal(row)">
                          <i class="fas fa-edit"></i>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="刪除" placement="top">
                        <el-button
                          size="small"
                          type="danger"
                          @click="deleteMerchant(row.merchantId, row.merchantName)"
                        >
                          <i class="fas fa-trash-alt"></i>
                        </el-button>
                      </el-tooltip>
                    </el-button-group>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="merchants.length === 0 && !loading" description="目前沒有商家資料" />
            </el-card>
          </div>
        </transition>
      </div>
    </section>

    <!-- ========== 編輯彈窗 ========== -->
    <el-dialog
      v-model="isEditModalOpen"
      :title="editingForm.merchantId ? `編輯商家 #${editingForm.merchantId}` : '新增商家'"
      width="600px"
      class="modern-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="editingForm" label-width="80px" label-position="top" class="modern-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商家名稱" required>
              <el-input v-model="editingForm.merchantName" placeholder="請輸入商家名稱">
                <template #prefix><i class="fas fa-store"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="聯絡電話">
              <el-input v-model="editingForm.merchantPhone" placeholder="請輸入聯絡電話">
                <template #prefix><i class="fas fa-phone"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="電子信箱">
          <el-input v-model="editingForm.merchantEmail" placeholder="請輸入電子信箱">
            <template #prefix><i class="fas fa-envelope"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="商家地址">
          <el-input
            v-model="editingForm.merchantAddress"
            type="textarea"
            :rows="2"
            placeholder="請輸入商家地址"
          />
        </el-form-item>
        <el-form-item label="營運狀態">
          <el-select
            v-model="editingForm.merchantStatus"
            placeholder="請選擇狀態"
            style="width: 100%"
          >
            <el-option :value="1" label="營運中">
              <i class="fas fa-check-circle text-success mr-2"></i>營運中
            </el-option>
            <el-option :value="0" label="停用中">
              <i class="fas fa-times-circle text-danger mr-2"></i>停用中
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="isEditModalOpen = false">
            <i class="fas fa-times mr-1"></i> 取消
          </el-button>
          <el-button type="primary" @click="handleSave" class="save-btn">
            <i class="fas fa-save mr-1"></i> 儲存資料
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ========== 頁面容器 ========== */
.merchant-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 20px;
}

/* ========== 頁面標題區 ========== */
.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.title-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  transition: all 0.4s ease;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.title-icon:hover {
  transform: scale(1.1) rotate(10deg);
}

.title-content {
  flex: 1;
  min-width: 200px;
}

.title-content h1 {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 700;
  color: #303133;
}

.title-content .subtitle {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

.title-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 24px;
  transition: all 0.3s ease;
}

.add-btn {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.add-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(103, 194, 58, 0.4);
}

/* ========== 統計卡片 ========== */
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  border-radius: 4px 0 0 4px;
}

.total-card::before {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}
.active-card::before {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}
.inactive-card::before {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}
.active-card .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}
.inactive-card .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

.stat-info h3 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #303133;
}

.stat-info span {
  font-size: 0.85rem;
  color: #909399;
}

/* ========== 表格卡片 ========== */
.table-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.header-text {
  font-weight: 600;
  font-size: 1.1rem;
  color: #303133;
}

/* ========== 篩選區 ========== */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.filter-input {
  width: 300px;
}

.filter-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.search-btn,
.reset-btn {
  border-radius: 10px;
  font-weight: 500;
}

/* ========== 表格樣式 ========== */
.modern-table {
  border-radius: 12px;
  overflow: hidden;
}

.id-tag {
  font-size: 12px;
  color: #909399;
  font-weight: 600;
}

.merchant-name-cell {
  display: flex;
  align-items: center;
}

.name-text {
  font-weight: 600;
  color: #303133;
}

.address-cell {
  display: flex;
  align-items: center;
  color: #606266;
}

.phone-text {
  color: #606266;
  font-size: 13px;
}

.email-text {
  color: #606266;
  font-size: 13px;
}

.status-tag {
  border-radius: 20px;
  padding: 4px 12px;
}

/* ========== 彈窗樣式 ========== */
.modern-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.modern-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  color: white;
  padding: 16px 20px;
}

.modern-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
}

.modern-form :deep(.el-input__wrapper),
.modern-form :deep(.el-textarea__inner),
.modern-form :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.save-btn {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 24px;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}

/* ========== 動畫效果 ========== */
.fade-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-slide-leave-active {
  transition: all 0.3s ease-in;
}

.fade-slide-enter-from {
  transform: translateY(30px);
  opacity: 0;
}

.fade-slide-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* ========== 間距工具類 ========== */
.mb-4 {
  margin-bottom: 1.5rem;
}
.mr-1 {
  margin-right: 4px;
}
.mr-2 {
  margin-right: 8px;
}

/* ========== 顏色工具類 ========== */
.text-primary {
  color: #409eff;
}
.text-success {
  color: #67c23a;
}
.text-warning {
  color: #e6a23c;
}
.text-danger {
  color: #f56c6c;
}

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .page-title-box {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .title-actions {
    width: 100%;
    justify-content: center;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-input {
    width: 100%;
  }
}
</style>
