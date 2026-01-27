<template>
  <div class="payment-container">
    <div class="payment-card sponsor-card">
      <div class="hero-header">
        <el-icon class="star-icon"><StarFilled /></el-icon>
        <h2>支持我們的願景</h2>
        <p>您的支持是我們進步的最大動力</p>
      </div>
      
      <hr />

      <div class="amount-selector">
        <p class="section-title">選擇贊助金額：</p>
        <div class="price-tags">
          <button 
            v-for="p in [100, 500, 1000]" 
            :key="p" 
            :class="{ active: amount === p }"
            @click="amount = p"
          >
            ${{ p }}
          </button>
        </div>
        <el-input-number v-model="amount" :min="10" :step="50" class="custom-input" />
      </div>

      <div class="payment-methods">
        <p class="section-title">支付平台：</p>
        <label class="method-option">
          <input type="radio" checked /> 
          <span class="radio-label">綠界科技 ECPay 安全支付</span>
        </label>
      </div>

      <button 
        @click="handleSponsorSubmit" 
        :disabled="isLoading" 
        class="checkout-btn sponsor-btn"
      >
        <span v-if="isLoading" class="loader"></span>
        {{ isLoading ? '準備導向支付頁面...' : '立即贊助' }}
      </button>

      <p class="note">※ 點擊後將跳轉至綠界金流頁面，過程受加密保護</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { StarFilled } from '@element-plus/icons-vue';

const isLoading = ref(false);
const amount = ref(100);

/**
 * 💡 改良版：使用隱藏表單 POST 提交
 * 這樣可以完美避開 CSP 腳本執行錯誤
 */
const handleSponsorSubmit = async () => {
  isLoading.value = true;
  try {
    // 1. 取得 HTML 字串
    const response = await axios.post('http://localhost:8080/api/payment/sponsor', null, {
      params: { amount: amount.value }
    });

    // 2. 建立隱藏容器
    const div = document.createElement('div');
    div.style.display = 'none'; // 隱藏起來不影響視覺
    div.innerHTML = response.data;
    document.body.appendChild(div);

    // 3. 由 Vue 主動觸發提交 (這不會觸發 CSP 報錯)
    const form = div.querySelector('#ecpayForm');
    if (form) {
      form.submit();
    }
  } catch (error) {
    console.error("金流導向失敗", error);
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
  min-height: 85vh;
  background-color: #f0f2f5;
}

.payment-card {
  background: white;
  padding: 2.5rem;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.08);
  width: 100%;
  max-width: 420px;
}

.hero-header { text-align: center; margin-bottom: 1.5rem; }
.star-icon { font-size: 40px; color: #f4a261; margin-bottom: 10px; }
.section-title { font-weight: bold; margin-bottom: 10px; color: #333; }

.price-tags { display: flex; gap: 10px; margin-bottom: 15px; }
.price-tags button {
  flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px;
  background: white; cursor: pointer; transition: all 0.3s; font-weight: bold;
}
.price-tags button.active {
  background: #2a9d8f; color: white; border-color: #2a9d8f;
}

.custom-input { width: 100%; margin-top: 10px; }

.payment-methods { margin: 1.5rem 0; }
.method-option {
  display: flex; align-items: center; gap: 10px; padding: 12px;
  border: 1px solid #e0e0e0; border-radius: 8px; background: #fafafa;
}

.checkout-btn {
  width: 100%; padding: 1.2rem; background-color: #2a9d8f; color: white;
  border: none; border-radius: 8px; font-size: 1.2rem; font-weight: 600;
  cursor: pointer; transition: all 0.3s; display: flex;
  justify-content: center; align-items: center; gap: 10px;
}

.sponsor-btn { background-color: #e76f51; }
.sponsor-btn:hover { background-color: #d65d41; transform: translateY(-2px); }

.loader {
  border: 3px solid #f3f3f3; border-top: 3px solid #ffffff;
  border-radius: 50%; width: 18px; height: 18px; animation: spin 1s linear infinite;
}

@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.note { font-size: 0.8rem; color: #888; text-align: center; margin-top: 1.2rem; }
</style>