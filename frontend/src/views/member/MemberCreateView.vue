<script setup>
/**
 * MemberCreateView.vue：新增會員（含格式驗證）
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()

const member = reactive({
  memUsername: '',
  memPassword: '',
  memName: '',
  memEmail: '',
  memPhone: '',
  memInvoice: '',
  memStatus: 1,
  memLevel: 1,
})

const errorMsg = ref('')
const successMsg = ref('')

// --- 格式驗證邏輯 ---
const validateForm = () => {
  // 1. 必填檢查 (除了發票載具)
  if (!member.memUsername || !member.memPassword || !member.memName || !member.memEmail || !member.memPhone) {
    return '除了發票載具，其餘欄位皆為必填'
  }

  // 2. 密碼驗證：至少 6 個字且包含至少一個英文
  // 正則表達式：(?=.*[a-zA-Z]) 表示至少一個英文，.{6,} 表示至少六位
  const pwdRegex = /^(?=.*[a-zA-Z]).{6,}$/
  if (!pwdRegex.test(member.memPassword)) {
    return '密碼需至少 6 個字元，並包含至少一個英文字母'
  }

  // 3. 手機驗證：09 開頭且共 10 位數字
  const phoneRegex = /^09\d{8}$/
  if (!phoneRegex.test(member.memPhone)) {
    return '手機格式錯誤，請輸入 09 開頭的 10 位數字'
  }

  // 4. 信箱驗證：...@....com
  // 嚴格檢查結尾必須是 .com
  const emailRegex = /^[^\s@]+@[^\s@]+\.com$/
  if (!emailRegex.test(member.memEmail)) {
    return '信箱格式錯誤，必須包含 @ 且結尾為 .com'
  }

  return null // 代表驗證通過
}

const submitCreate = async () => {
  // 執行驗證
  const error = validateForm()
  if (error) {
    await Swal.fire({
      icon: 'warning',
      title: '格式錯誤',
      text: error,
      confirmButtonColor: '#e6a23c',
    })
    return // 攔截，不執行 API
  }

  try {
    const res = await axios.post('http://localhost:8080/api/members', {
      ...member,
      memStatus: 1,
      memLevel: 1,
    })

    await Swal.fire({
      icon: 'success',
      title: '新增成功',
      text: res.data?.message || '會員已新增',
      confirmButtonText: '確定',
      confirmButtonColor: '#409eff',
    })

    router.push('/admin/members')
  } catch (err) {
    const msg = err.response?.data?.message || '新增失敗'
    await Swal.fire({
      icon: 'error',
      title: '新增失敗',
      text: msg,
      confirmButtonText: '確定',
      confirmButtonColor: '#f56c6c',
    })
  }
}

const goBack = () => {
  router.push('/admin/members')
}
</script>

<template>
  <div class="container">
    <h2>新增會員</h2>
    <form @submit.prevent="submitCreate" autocomplete="off" novalidate>
      <label>帳號</label>
      <input type="text" v-model="member.memUsername" placeholder="請輸入帳號" autocomplete="new-username" />
      
      <label>密碼</label>
      <input type="password" v-model="member.memPassword" placeholder="至少6字+1英文字母" autocomplete="new-password" />
      
      <label>姓名</label>
      <input type="text" v-model="member.memName" placeholder="請輸入真實姓名" />
      
      <label>信箱</label>
      <input type="email" v-model="member.memEmail" placeholder="example@mail.com" />
      
      <label>手機</label>
      <input type="text" v-model="member.memPhone" placeholder="09xxxxxxxx" maxlength="10" />
      
      <label>發票載具</label>
      <input type="text" v-model="member.memInvoice" placeholder="例：/ABC1234 (選填)" />
      
      <button type="submit" class="btn-submit">確認新增</button>
    </form>
    <a class="home-btn" @click.prevent="goBack">回會員列表</a>
  </div>
</template>

<style scoped>
/* 樣式部分僅加入 hover 強化與細微調整 */
.container {
  width: 420px;
  margin: 40px auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 32px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h2 {
  color: #1e3a5f;
  margin-bottom: 24px;
  font-size: 1.5rem;
  font-weight: 700;
  position: relative;
  padding-bottom: 12px;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 2px;
}

label {
  display: block;
  text-align: left;
  margin-bottom: 6px;
  font-weight: 600;
  font-size: 14px;
  color: #334155;
}

input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  height: 42px;
  box-sizing: border-box;
  margin-bottom: 16px;
  font-size: 14px;
  background: #f8fafc;
  transition: border-color 0.2s, box-shadow 0.2s;
}

input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
  outline: none;
  background: #ffffff;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-submit:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.5);
  transform: translateY(-1px);
}

.home-btn {
  display: inline-block;
  margin-top: 20px;
  text-decoration: none;
  color: #3b82f6;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
}

.home-btn:hover {
  color: #1d4ed8;
  text-decoration: underline;
}
</style>