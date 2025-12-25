<template>
  <div class="container">
    <h1 class="title">優惠券管理系統</h1>

    <div class="toolbar">
      <div class="search-group">
        <input v-model="keyword" placeholder="搜尋名稱或內容..." @keyup.enter="fetchDiscounts" class="input-field">
        <button @click="fetchDiscounts" class="btn btn-primary">搜尋</button>
        <button @click="resetSearch" class="btn btn-secondary">清除</button>
      </div>
      <button @click="openModal" class="btn btn-success">新增優惠券</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>圖片</th>
          <th>名稱</th>
          <th>商家</th>
          <th>日期範圍</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in discounts" :key="d.couponId" :class="getRowClass(d)">
          <td>{{ d.couponId }}</td>
          <td>
            <img v-if="d.couponImg" :src="`http://localhost:8080/images/${d.couponImg}`" class="coupon-img">
            <span v-else>無圖片</span>
          </td>
          <td class="bold">{{ d.couponName }}</td>
          <td>{{ d.merchantName }}</td>
          <td>
            <small>{{ d.startDate }} 到</small><br>
            <small>{{ d.endDate }}</small>
          </td>
          <td>
            <span :class="['badge', getStatusClass(d)]">{{ getStatusText(d) }}</span>
          </td>
          <td class="actions">
            <button @click="editDiscount(d)" class="btn-sm">修改</button>
            <button @click="deleteDiscount(d.couponId)" class="btn-sm btn-danger">刪除</button>
            <button @click="toggleStatus(d)" class="btn-sm btn-warning">
              {{ d.couponStatus === 3 ? '重新上架' : '下架' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

// 狀態變數
const discounts = ref([]);
const keyword = ref('');
const today = new Date().toISOString().split('T')[0];

// 1. 取得資料
const fetchDiscounts = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/discounts`, {
      params: { keyword: keyword.value }
    });
    if (res.data.code === 200) {
      discounts.value = res.data.data;
    }
  } catch (error) {
    Swal.fire('錯誤', '連線後端失敗', 'error');
  }
};

// 2. 刪除優惠券
const deleteDiscount = (id) => {
  Swal.fire({
    title: '確定要刪除嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '是的，刪除！'
  }).then(async (result) => {
    if (result.isConfirmed) {
      const res = await axios.delete(`http://localhost:8080/api/discounts/${id}`);
      if (res.data.code === 200) {
        Swal.fire('已刪除', '', 'success');
        fetchDiscounts();
      }
    }
  });
};

// 3. 上下架切換
const toggleStatus = async (discount) => {
  const action = discount.couponStatus === 3 ? 'relist' : 'disable';
  const res = await axios.patch(`http://localhost:8080/api/discounts/${discount.couponId}/status`, null, {
    params: { action: action }
  });
  if (res.data.code === 200) {
    fetchDiscounts();
  }
};

// 4. 判斷 CSS Class (對應你原本 JSP 的邏輯)
const getRowClass = (d) => {
  if (d.couponStatus === 3) return 'row-disabled';
  if (d.endDate && today > d.endDate) return 'row-ended';
  return '';
};

const getStatusText = (d) => {
  if (d.couponStatus === 3) return '已下架';
  if (d.endDate && today > d.endDate) return '已結束';
  if (d.startDate && today < d.startDate) return '尚未開放';
  return '活動中';
};

const getStatusClass = (d) => {
  if (d.couponStatus === 3) return 'badge-gray';
  if (d.endDate && today > d.endDate) return 'badge-red';
  return 'badge-green';
};

const resetSearch = () => { keyword.value = ''; fetchDiscounts(); };

onMounted(fetchDiscounts);
</script>

<style scoped>
/* 這裡實作你原本的顏色邏輯 */
.container { padding: 20px; font-family: sans-serif; }
.data-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
.data-table th, .data-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }

.row-disabled { background-color: #f0f0f0; color: #999; }
.row-ended { background-color: #ffebee; }

.coupon-img { width: 80px; height: auto; border-radius: 4px; }
.badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; color: white; }
.badge-green { background-color: #4caf50; }
.badge-red { background-color: #f44336; }
.badge-gray { background-color: #9e9e9e; }

.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
.btn-primary { background-color: #007bff; color: white; }
.btn-success { background-color: #28a745; color: white; }
.btn-danger { background-color: #dc3545; color: white; }
</style>