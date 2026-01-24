<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const member = ref(null)
const form = ref({})
const isEdit = ref(false)
const errorMsg = ref('')

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
  } catch (e) {
    errorMsg.value = '尚未登入'
  }
}

onMounted(fetchProfile)

const formatDate = (dt) => {
  if (!dt) return ''
  return dt.split('T')[0]
}

const startEdit = () => {
  originForm.value = { ...form.value }
  isEdit.value = true
}

const cancelEdit = () => {
  isEdit.value = false
  fetchProfile()
}

const saveEdit = async () => {
  try {
    const res = await axios.put(
      'http://localhost:8080/member/profile',
      {
        memName: form.value.memName.trim(),
        memPhone: form.value.memPhone.trim(),
        memEmail: form.value.memEmail.trim(),
        memInvoice: form.value.memInvoice.trim(),
      },
      { withCredentials: true }
    )

    if (res.data === '更新成功') {
      alert('資料已更新')
      isEdit.value = false
      fetchProfile()
    } else {
      // ⭐ 格式錯誤等情況
      alert(res.data)
      form.value = { ...originForm.value }
    }

  } catch (e) {
    alert('系統錯誤')
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
          <router-link :to="{ name: 'member-user-info' }" class="btn btn-info">查看ID資訊＃TESTPAGE</router-link>
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

        <!-- 租借狀態（給租借組員完成） -->
        <div class="row">
          <label>租借狀態</label>
          <div class="value placeholder">
            等租借同仁完成
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
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.row:last-child {
  border-bottom: none;
}

label {
  width: 120px;
  color: #666;
}

/* input / 顯示共用 */
.value,
.value-input {
  flex: 1;
  font-size: 16px;
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
</style>