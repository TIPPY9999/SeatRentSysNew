<template>
  <div class="container">
    <h2 class="title">🎟️ 優惠券管理</h2>

    <div class="toolbar">
      <div class="search-group">
        <input v-model="keyword" placeholder="搜尋名稱或內容..." @keyup.enter="fetchDiscounts" class="input-field">
        <button @click="fetchDiscounts" class="btn btn-primary">搜尋</button>
      </div>
      <button @click="openModal()" class="btn btn-success">新增優惠券</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>圖片</th>
          <th>名稱</th>
          <th>商家 ID</th>
          <th>結束日期</th>
          <th>狀態</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in discounts" :key="d.couponId">
          <td>{{ d.couponId }}</td>
          <td>
            <img v-if="d.couponImg" :src="`http://localhost:8080/images/${d.couponImg}`" class="coupon-img">
            <span v-else>無圖片</span>
          </td>
          <td>{{ d.couponName }}</td>
          <td>{{ d.merchantId }}</td>
          <td>{{ d.endDate }}</td>
          <td>
            <span :class="['badge', d.couponStatus === 1 ? 'badge-green' : 'badge-red']">
              {{ d.couponStatus === 1 ? '上架中' : (d.couponStatus === 3 ? '手動下架' : '已過期') }}
            </span>
          </td>
          <td>
            <button @click="deleteDiscount(d.couponId)" class="btn-sm btn-danger">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';
interface Discount {
  couponId: number;
  couponName: string;
  couponDescription: string;
  couponImg: string;
  merchantId: number;
  startDate: string;
  endDate: string;
  couponStatus: number;
}

// 宣告 discounts 是一個存放 Discount 物件的陣列
const discounts = ref<Discount[]>([]);

const keyword = ref('');

// 1. 取得資料
const fetchDiscounts = async () => {
  try {
    const res = await axios.get('/api/discounts', {
      params: { keyword: keyword.value }
    });
    discounts.value = res.data.data;
  } catch (error) {
    console.error('抓取優惠券失敗', error);
  }
};

// 2. 刪除資料
const deleteDiscount = async (id: number) => {
  const result = await Swal.fire({
    title: '確定要刪除嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定'
  });

  if (result.isConfirmed) {
    await axios.delete(`/api/discounts/${id}`);
    Swal.fire('已刪除', '', 'success');
    fetchDiscounts();
  }
};

const openModal = () => {
    Swal.fire('提示', '新增功能開發中', 'info');
};

onMounted(fetchDiscounts);
</script>

<style scoped>
.container { padding: 20px; }
.title { margin-bottom: 20px; color: #333; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.input-field { padding: 8px; border: 1px solid #ccc; border-radius: 4px; width: 250px; }
.data-table { width: 100%; border-collapse: collapse; background: white; }
.data-table th, .data-table td { border: 1px solid #eee; padding: 12px; text-align: left; }
.coupon-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; color: white; }
.badge-green { background-color: #4caf50; }
.badge-red { background-color: #f44336; }
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
.btn-primary { background: #007bff; color: white; }
.btn-success { background: #28a745; color: white; }
.btn-danger { background: #dc3545; color: white; }
.btn-sm { padding: 4px 8px; font-size: 12px; }
</style>