<template>
  <div class="payment-container">
    <div class="payment-card">
      <h2>訂單結帳</h2>
      <hr />
      
      <div class="order-info">
        <div class="info-item">
          <span>商品名稱：</span>
          <strong>{{ order.itemName }}</strong>
        </div>
        <div class="info-item">
          <span>應付總額：</span>
          <span class="amount">NT$ {{ order.totalAmount }}</span>
        </div>
      </div>

      <div class="payment-methods">
        <p>選擇付款方式：</p>
        <label class="method-option">
          <input type="radio" checked /> 
          <span class="radio-label">綠界全功能支付 (信用卡/ATM/超商)</span>
        </label>
      </div>

      <button 
        @click="handleCheckout" 
        :disabled="isLoading" 
        class="checkout-btn"
      >
        {{ isLoading ? '處理中...' : '前往付款' }}
      </button>

      <p class="note">※ 點擊按鈕後將導向綠界安全支付頁面</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const isLoading = ref(false);
const order = ref({
  itemName: "商家會員方案 - 進階版",
  totalAmount: 1000
});

const handleCheckout = async () => {
  isLoading.value = true;
  try {
    // 1. 呼叫你的 Java Servlet API 取得加密參數
    // 請確認後端 Servlet URL 正確
    const response = await axios.post('http://localhost:8080/api/payment/checkout');
    const paymentData = response.data;

    // 2. 建立一個隱藏的 Form 並動態填入參數
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5';

    // 3. 將 API 回傳的所有參數轉為 hidden input
    Object.keys(paymentData).forEach(key => {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = paymentData[key];
      form.appendChild(input);
    });

    // 4. 加入表單到頁面並執行提交
    document.body.appendChild(form);
    form.submit();

  } catch (error) {
    console.error("支付失敗：", error);
    alert("無法啟動支付流程，請稍後再試。");
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.payment-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  background-color: #f5f7fa;
}
.payment-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
}
.order-info {
  margin: 1.5rem 0;
}
.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.8rem;
}
.amount {
  color: #e63946;
  font-size: 1.2rem;
  font-weight: bold;
}
.checkout-btn {
  width: 100%;
  padding: 1rem;
  background-color: #1d3557;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.3s;
}
.checkout-btn:hover {
  background-color: #457b9d;
}
.checkout-btn:disabled {
  background-color: #ccc;
}
.note {
  font-size: 0.8rem;
  color: #666;
  text-align: center;
  margin-top: 1rem;
}
</style>