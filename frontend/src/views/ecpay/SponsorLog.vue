<template>
  <div class="admin-container" style="padding: 20px;">
    <h2>💎 贊助紀錄管理</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>總贊助金額</template>
          <div style="font-size: 24px; font-weight: bold; color: #67C23A;">
            $ {{ totalAmount }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>總贊助筆數</template>
          <div style="font-size: 24px; font-weight: bold; color: #409EFF;">
            {{ tableData.length }} 筆
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="searchQuery" placeholder="搜尋會員 ID 或 交易單號" style="width: 300px;" clearable />
      <el-select v-model="statusFilter" placeholder="支付狀態" clearable>
        <el-option label="待支付" :value="0" />
        <el-option label="成功" :value="1" />
        <el-option label="失敗" :value="2" />
      </el-select>
    </div>

    <el-table :data="filteredData" stripe border v-loading="loading" style="width: 100%">
      <el-table-column prop="merchantTradeNo" label="訂單編號" width="180" />
      <el-table-column prop="memberId" label="會員 ID" width="100" />
      
      <el-table-column prop="amount" label="金額" width="120">
        <template #default="scope">
          <span style="font-weight: bold; color: #E6A23C;">${{ scope.row.amount }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="狀態" width="100">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="sponsorComment" label="贊助留言" show-overflow-tooltip />
      
      <el-table-column prop="paymentType" label="支付方式" width="120" />
      
      <el-table-column prop="createdAt" label="時間" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const loading = ref(false);
const tableData = ref([]);
const searchQuery = ref('');
const statusFilter = ref(null);

// 取得後台數據 (需對應你後端的 API)
const fetchSponsors = async () => {
  loading.value = true;
  try {
    const apiUrl = window.APP_CONFIG?.API_URL || 'http://localhost:8080';
    // 💡 你需要在後端寫一個 GET /api/admin/sponsors 來回傳所有紀錄
    const response = await axios.get(`${apiUrl}/api/payment/admin/sponsors`);
    tableData.value = response.data;
  } catch (error) {
    console.error("讀取贊助紀錄失敗", error);
  } finally {
    loading.value = false;
  }
};

// 狀態標籤樣式
const statusType = (status) => {
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  return 'info';
};

const statusText = (status) => {
  if (status === 1) return '成功';
  if (status === 2) return '失敗';
  return '待支付';
};

// 計算總額 (只算成功的)
const totalAmount = computed(() => {
  return tableData.value
    .filter(item => item.status === 1)
    .reduce((sum, item) => sum + item.amount, 0);
});

// 前端篩選邏輯
const filteredData = computed(() => {
  return tableData.value.filter(item => {
    const matchesSearch = item.merchantTradeNo.includes(searchQuery.value) || 
                         String(item.memberId).includes(searchQuery.value);
    const matchesStatus = statusFilter.value === null || item.status === statusFilter.value;
    return matchesSearch && matchesStatus;
  });
});

onMounted(fetchSponsors);
</script>