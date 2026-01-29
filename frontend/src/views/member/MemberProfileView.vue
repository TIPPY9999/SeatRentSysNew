<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

import { useMemberAuthStore } from '@/stores/memberAuth'

import RecRentUserRecord from '@/components/rec/RecRentUserRecord.vue'

import Swal from 'sweetalert2';

const memberAuth = useMemberAuthStore()
const member = ref(null)
const form = ref({})
const isEdit = ref(false)
const errorMsg = ref('')

// 租借狀態相關
const activeRent = ref(null) // 進行中的租借訂單
const rentSpotName = ref('') // 租借站點的名稱
const isLoadingRentStatus = ref(true) // 租借狀態的讀取狀態

// [新增] 控制歷史紀錄顯示
const showHistory = ref(false)
const toggleHistory = () => {
  showHistory.value = !showHistory.value
}

const originForm = ref({})

const fetchProfile = async () => {
  try {
    const res = await axios.get('http://localhost:8080/member/profile', {
      withCredentials: true,
    })

    const m = res.data
    member.value = m

    // 一定要 trim，避免空白 bug
    form.value = {
      memName: m.memName?.trim() || '',
      memPhone: m.memPhone?.trim() || '',
      memEmail: m.memEmail?.trim() || '',
      memInvoice: m.memInvoice?.trim() || '',
    }
    memberAuth.setMemberLogin(m); // 更新 Pinia store
    
    // 取得個人資料後，接著取得租借狀態
    await fetchRentalStatus();
    
  } catch (e) {
    errorMsg.value = '尚未登入'
  }
}

// 查詢租借狀態
const fetchRentalStatus = async () => {
  isLoadingRentStatus.value = true;
  try {
    const memId = memberAuth.member?.memId;
    if (!memId) {
      activeRent.value = null;
      return;
    }

    // 查詢會員的租借紀錄，目的是為了找到進行中訂單的 ID
    const rentListRes = await axios.get(`http://localhost:8080/rec-rent?memId=${memId}`);
    const basicRentInfo = rentListRes.data.find(rent => rent.recStatus === '租借中');

    // 如果找到進行中的訂單
    if (basicRentInfo && basicRentInfo.recId) {
      try {
        // 使用訂單 ID 去查詢完整的、包含站點名稱的詳細資訊
        const detailsRes = await axios.get(`http://localhost:8080/api/rent-details/${basicRentInfo.recId}`);
        const currentRentDetails = detailsRes.data;

        if (currentRentDetails) {
          activeRent.value = currentRentDetails;
          // 直接從回傳的詳細資料中取得站點名稱
          rentSpotName.value = currentRentDetails.spotNameRent || '未知站點';
        } else {
           activeRent.value = null; // 如果詳細資料查無，也當作沒有
        }
      } catch (detailsError) {
        console.error(`查詢訂單詳細資訊 ${basicRentInfo.recId} 失敗:`, detailsError);
        // Fallback: 即使詳細資料查詢失敗，仍然顯示基本資訊
        activeRent.value = basicRentInfo;
        rentSpotName.value = '站點資料讀取失敗';
      }
    } else {
      // 如果連基本訂單都找不到，則確認無租借中訂單
      activeRent.value = null;
    }
  } catch (error) {
    console.error('查詢租借狀態失敗:', error);
    activeRent.value = null;
  } finally {
    isLoadingRentStatus.value = false;
  }
}

onMounted(fetchProfile)

const formatDate = (dt) => {
  if (!dt) return ''
  return dt.split('T')[0]
}

// [新增] 格式化日期時間的函式
const formatDateTime = (dt) => {
  if (!dt) return '';
  const date = new Date(dt);
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).replace(/\//g, '-');
};

const startEdit = () => {
  originForm.value = { ...form.value }
  isEdit.value = true
}

const cancelEdit = () => {
  isEdit.value = false
  fetchProfile()
}

const saveEdit = async () => {
  // 1. 密碼格式初步檢查 (前端擋路)
  const passwordRegex = /^(?=.*[A-Za-z])[A-Za-z\d!@#$%^&*()_+=\[\]{}:;"'<>,.?/\-]{6,}$/;
  if (form.value.memPassword && !passwordRegex.test(form.value.memPassword)) {
    Swal.fire('格式錯誤', '密碼必須至少 6 碼且包含 1 個英文字母', 'error');
    return;
  }

  // 2. 二次確認彈窗
  const confirmResult = await Swal.fire({
    title: '確定要變更資料嗎？',
    text: "修改後將會立即生效",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#22c55e',
    cancelButtonColor: '#9ca3af',
    confirmButtonText: '確定更新',
    cancelButtonText: '取消'
  });

  if (!confirmResult.isConfirmed) return;

  try {
    // 修正這裡的路徑與方法
    const res = await axios.post( // 1. 改回 post
      'http://localhost:8080/api/members/update', // 2. 指向正確的後端路徑
      {
        memId: member.value.memId, // 3. 務必傳送 memId，否則後端 findById 會找不到人
        memName: form.value.memName.trim(),
        memPhone: form.value.memPhone.trim(),
        memEmail: form.value.memEmail.trim(),
        memInvoice: form.value.memInvoice.trim(),
        memPassword: form.value.memPassword,
        memUsername: member.value.memUsername // 建議也帶上，確保資料完整
      },
      { withCredentials: true }
    )

    // 4. 注意：後端回傳的是 "會員修改成功"，這裡要對齊字串
    if (res.data === '會員修改成功') { 
      Swal.fire({
        title: '編輯成功',
        text: '您的資料已同步',
        icon: 'success',
        timer: 1500,
        showConfirmButton: false,
      });
      
      isEdit.value = false
      fetchProfile()
    } else {
      Swal.fire('更新失敗', res.data, 'error');
    }
  } catch (e) {
    Swal.fire('系統錯誤', '無法連接至伺服器', 'error');
    form.value = { ...originForm.value }
  }
}
</script>

<template>
  <div class="profile-page">
    <div v-if="errorMsg">{{ errorMsg }}</div>

    <div v-if="member">
      <!-- 頭像 + 名稱 -->
      <div class="profile-header">
        <div class="avatar">👤</div>
        <div class="name">{{ member.memName }}</div>
      </div>

      <!-- 操作按鈕 -->
      <div class="actions">
        <template v-if="!isEdit">
          <button @click="toggleHistory" class="btn btn-info">
            {{ showHistory ? '隱藏訂單歷史' : '查看訂單歷史資訊' }}
          </button>
          <button class="edit-btn" @click="startEdit">編輯資料</button>
        </template>
        <template v-else>
          <button class="save-btn" @click="saveEdit">儲存變更</button>
          <button class="cancel-btn" @click="cancelEdit">取消</button>
        </template>
      </div>

      <!-- 資料卡片 -->
      <div class="profile-card">
        <div class="row">
          <label>帳號</label>
          <div class="value">{{ member.memUsername }}</div>
        </div>

        <div class="row">
          <label>密碼</label>
          <input
            :type="isEdit ? 'password' : 'text'"
            class="value-input"
            v-model="form.memPassword"
            :placeholder="isEdit ? '輸入新密碼以修改' : '********'"
            :readonly="!isEdit"
          />
        </div>

        <div class="row">
          <label>姓名</label>
          <input
            class="value-input"
            v-model="form.memName"
            :readonly="!isEdit"
          />
        </div>

        <div class="row">
          <label>手機</label>
          <input
            class="value-input"
            v-model="form.memPhone"
            :readonly="!isEdit"
          />
        </div>

        <div class="row">
          <label>Email</label>
          <input
            class="value-input"
            v-model="form.memEmail"
            :readonly="!isEdit"
          />
        </div>

        <!-- 會員點數（唯讀） -->
        <div class="row">
          <label>會員點數</label>
          <div class="value">
            {{ member.memPoints }}
          </div>
        </div>

        <!-- 租借狀態 -->
        <div class="row">
          <label>租借狀態</label>
          <div class="value">
            <!-- 讀取中 -->
            <div v-if="isLoadingRentStatus" class="placeholder">讀取中...</div>
            <!-- 有租借中訂單 -->
            <div v-else-if="activeRent" class="rent-details">
              <div><strong>狀態:</strong> <span class="status-active">{{ activeRent.recStatus }}</span></div>
              <div><strong>訂單編號:</strong> {{ activeRent.recId }}</div>
              <div><strong>租借站點:</strong> {{ activeRent.rentSpotName }}</div>
              <div><strong>租借時間:</strong> {{ formatDateTime(activeRent.recRentDT2) }}</div>
            </div>
            <!-- 無租借中訂單 -->
            <div v-else class="placeholder">目前沒有租借中的訂單</div>
          </div>
        </div>

        <div class="row">
          <label>發票載具</label>
          <input
            class="value-input"
            v-model="form.memInvoice"
            :readonly="!isEdit"
          />
        </div>

        <div class="row">
          <label>註冊日期</label>
          <div class="value">{{ formatDate(member.createdAt) }}</div>
        </div>
      </div>

      <!-- [新增] 訂單歷史紀錄 -->
      <div v-if="showHistory" class="history-container mt-4">
        <RecRentUserRecord />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 整體 */
.profile-page {
  max-width: 720px;
  margin: 0 auto;
  min-height: 100vh;      /* 至少佔滿整個視窗高度 */
  display: flex;          /* 使用 Flexbox 排版 */
  flex-direction: column; /* 內容垂直排列 */
  justify-content: center;/* 垂直置中 */
  padding: 40px 0;        /* 增加上下邊距，避免在小螢幕時貼邊 */
  box-sizing: border-box;
}

/* 頭像 */
.profile-header {
  text-align: center;
  margin-bottom: 20px;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #e0ecff;
  font-size: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
}

.name {
  font-size: 20px;
  font-weight: bold;
}

/* 按鈕 */
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 12px;
}

.edit-btn {
  background: #c9a46a;
  color: #fff;
}

.save-btn {
  background: #22c55e;
  color: #fff;
}

.cancel-btn {
  background: #9ca3af;
  color: #fff;
}

.actions button {
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* 卡片 */
.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 20px 5px 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.08);
}

.row {
  display: flex;
  padding: 4px 0;
  font-size: larger;
  border-bottom: 1px solid #eee;
}

.row:last-child {
  border-bottom: none;
  font-size: larger;
}

label {
  width: 120px;
  color: #666;
}

/* input / 顯示共用 */
.value,
.value-input {
  flex: 1;
  font-size:large;
}

.value-input {
  border: none;
  background: transparent;
}

.value-input:read-only {
  pointer-events: none;
}

.value-input:not(:read-only) {
  border: 1px solid #ccc;
  padding: 6px 8px;
  border-radius: 6px;
  background: #fff;
}

/* 編輯資料 */
.edit-btn:hover {
  background: #b8935a;
}

/* 儲存變更 */
.save-btn:hover {
  background: #16a34a;
}

/* 取消 */
.cancel-btn:hover {
  background: #6b7280;
}

/* 通用 hover 手感 */
.actions button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.placeholder {
  color: #9ca3af;
  font-style: italic;
}

.rent-details > div {
  padding: 2px 0;
}

.status-active {
  background-color: #28a745; /* 綠色背景 */
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}
</style>