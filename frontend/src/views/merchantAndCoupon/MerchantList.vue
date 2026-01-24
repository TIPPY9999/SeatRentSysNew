<template>
  <div class="merchant-list-container">
    <div class="status-cards">
      <div class="status-card">
        <i class="bi bi-shop text-primary"></i>
        <div class="card-info">
          <span class="card-label">總商家數</span>
          <span class="card-value">{{ totalItems }}</span>
        </div>
      </div>
      <div class="status-card">
        <i class="bi bi-check-circle text-success"></i>
        <div class="card-info">
          <span class="card-label">營運中</span>
          <span class="card-value">{{ activeMerchantsCount }}</span>
        </div>
      </div>
    </div>

    <div class="list-main-section shadow-sm">
      <div class="list-header">
        <div class="header-title">
          <i class="bi bi-briefcase-fill me-2"></i>
          <span>商家管理系統</span>
        </div>
        <div class="header-actions">
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜尋商家名稱..."
              prefix-icon="Search"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </div>
          <el-button type="primary" class="add-btn" @click="openAddModal">
            <i class="bi bi-plus-lg me-1"></i> 新增商家
          </el-button>
        </div>
      </div>

      <el-table 
        :data="merchantList" 
        v-loading="loading"
        style="width: 100%"
        :header-cell-style="{ background: '#f8f9fa', color: '#333', fontWeight: 'bold' }"
      >
        <el-table-column prop="merchantId" label="ID" width="80" sortable />
        
        <el-table-column label="商家名稱" min-width="150">
          <template #default="{ row }">
            <span 
              class="merchant-name-link" 
              @click="goToMerchantCoupons(row.merchantId)"
              title="點擊查看此商家優惠券"
            >
              {{ row.merchantName }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="merchantPhone" label="聯絡電話" width="150" />
        <el-table-column prop="merchantEmail" label="電子郵件" min-width="200" />
        
        <el-table-column label="狀態" width="120">
          <template #default="{ row }">
            <span :class="['status-dot', row.merchantStatus === 1 ? 'active' : 'suspended']"></span>
            {{ row.merchantStatus === 1 ? '營運中' : '已停權' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" circle @click="openEditModal(row)">
              <i class="bi bi-pencil"></i>
            </el-button>
            <el-button type="danger" size="small" circle @click="confirmDelete(row)">
              <i class="bi bi-trash"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改商家資料' : '新增商家資料'"
      width="500px"
    >
      <el-form :model="form" label-width="80px" label-position="top">
        <el-form-item label="商家名稱">
          <el-input v-model="form.merchantName" />
        </el-form-item>
        <el-form-item label="聯絡電話">
          <el-input v-model="form.merchantPhone" />
        </el-form-item>
        <el-form-item label="電子郵件">
          <el-input v-model="form.merchantEmail" />
        </el-form-item>
        <el-form-item label="商家地址">
          <el-input v-model="form.merchantAddress" type="textarea" />
        </el-form-item>
        <el-form-item label="狀態">
          <el-radio-group v-model="form.merchantStatus">
            <el-radio :label="1">營運中</el-radio>
            <el-radio :label="0">停權</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">確認儲存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router'; // 必須引入
import axios from 'axios';
import Swal from 'sweetalert2';
import { Search } from '@element-plus/icons-vue';

const router = useRouter(); // 初始化路由

// 狀態管理
const merchantList = ref([]);
const loading = ref(false);
const searchQuery = ref('');
const dialogVisible = ref(false);
const isEdit = ref(false);

// 分頁狀態
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// 表單狀態
const form = ref({
  merchantId: null,
  merchantName: '',
  merchantPhone: '',
  merchantEmail: '',
  merchantAddress: '',
  merchantStatus: 1
});

// 計算營運中商家
const activeMerchantsCount = computed(() => {
  return merchantList.value.filter(m => m.merchantStatus === 1).length;
});

// 獲取資料
const fetchMerchants = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/merchants', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchQuery.value
      }
    });
    // 假設後端回傳格式為 { data: [...], total: 100 }
    merchantList.value = res.data.data || [];
    totalItems.value = res.data.total || 0;
  } catch (error) {
    console.error('獲取商家失敗:', error);
  } finally {
    loading.value = false;
  }
};

// 跳轉至商城並過濾優惠券
const goToMerchantCoupons = (merchantId) => {
  if (!merchantId) return;
  router.push({
    path: '/mall',
    query: { merchantId: merchantId }
  });
};

// 搜尋與分頁處理
const handleSearch = () => {
  currentPage.value = 1;
  fetchMerchants();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchMerchants();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchMerchants();
};

// 彈窗與表單操作
const openAddModal = () => {
  isEdit.value = false;
  form.value = { merchantName: '', merchantPhone: '', merchantEmail: '', merchantAddress: '', merchantStatus: 1 };
  dialogVisible.value = true;
};

const openEditModal = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  dialogVisible.value = true;
};

const submitForm = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`http://localhost:8080/api/merchants/${form.value.merchantId}`, form.value);
    } else {
      await axios.post('http://localhost:8080/api/merchants', form.value);
    }
    dialogVisible.value = false;
    Swal.fire('成功', isEdit.value ? '資料已更新' : '商家已新增', 'success');
    fetchMerchants();
  } catch (error) {
    Swal.fire('錯誤', '操作失敗', 'error');
  }
};

const confirmDelete = (row) => {
  Swal.fire({
    title: '確定刪除？',
    text: `將刪除商家：${row.merchantName}`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/merchants/${row.merchantId}`);
        Swal.fire('已刪除', '', 'success');
        fetchMerchants();
      } catch (error) {
        Swal.fire('錯誤', '刪除失敗', 'error');
      }
    }
  });
};

onMounted(fetchMerchants);
</script>

<style scoped>
.merchant-list-container {
  padding: 24px;
  background-color: #f4f7f6;
  min-height: 100vh;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.status-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.status-card i { font-size: 2.5rem; margin-right: 15px; }
.card-info .card-label { display: block; color: #666; font-size: 0.9rem; }
.card-info .card-value { font-size: 1.5rem; font-weight: bold; color: #333; }

.list-main-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.merchant-name-link {
  color: #409eff;
  font-weight: bold;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
}

.merchant-name-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot.active { background-color: #52c41a; }
.status-dot.suspended { background-color: #ff4d4f; }

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>