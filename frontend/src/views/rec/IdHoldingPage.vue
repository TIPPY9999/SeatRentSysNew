<script setup>
import { useAuthStore } from "@/stores/auth";
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { user, isLogin } = storeToRefs(authStore);

// Helper function for date formatting, assuming createdAt is available
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString();
};

console.log(user);
console.log(user.value);
console.log(user.data);

</script>

<template>
  <div>
    <h1>使用者資訊</h1>
    <div v-if="isLogin && user">
      <div class="info-grid">
        <div class="info-row">
          <label>使用者ID</label>
          <div class="value">{{ user.memUsername || 'N/A' }}</div>
        </div>
        <div class="info-row">
          <label>姓名</label>
          <div class="value">{{ user.memName || 'N/A' }}</div>
        </div>
        <div class="info-row">
          <label>手機</label>
          <div class="value">{{ user.memPhone || 'N/A' }}</div>
        </div>
        <div class="info-row">
          <label>Email</label>
          <div class="value">{{ user.memEmail || 'N/A' }}</div>
        </div>
        <div class="info-row">
          <label>發票載具</label>
          <div class="value">{{ user.memInvoice || 'N/A' }}</div>
        </div>
        <div class="info-row">
          <label>註冊日期</label>
          <div class="value">{{ formatDate(user.createdAt) }}</div>
        </div>
      </div>
      <hr />
      <h3>偵錯用原始資料:</h3>
      <pre>{{ user }}</pre>
    </div>
    <div v-else>
      <p>使用者未登入或資訊載入失敗。</p>
    </div>
  </div>
</template>

<style scoped>
.info-grid {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 1rem;
  max-width: 600px;
  margin-top: 1rem;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.info-row {
  display: contents; /* Allows grid layout to apply to children */
}

.info-row label {
  font-weight: bold;
  text-align: right;
  padding-right: 1rem;
}

.info-row .value {
  background-color: #fff;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

pre {
  background-color: #f4f4f4;
  padding: 1rem;
  border-radius: 5px;
  white-space: pre-wrap;
  word-break: break-all;
}

hr {
  margin: 1.5rem 0;
}
</style>
