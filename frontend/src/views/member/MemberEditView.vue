<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()

// 修改這裡：給予初始結構，確保 v-model 一開始就能找到對象，回填會更順
const member = ref({
  memId: '',
  memUsername: '',
  memName: '',
  memEmail: '',
  memPhone: '',
  memPoints: 0,
  memInvoice: ''
})
const newPassword = ref('')
const errorMsg = ref('')
const isSubmitting = ref(false)
const isLoading = ref(true) // 增加一個讀取狀態

const fetchMember = async () => {
  const id = route.params.id
  isLoading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/members/find`, {
      params: { memId: id },
    })
    
    // 檢查點：如果後端回傳的 key 不是 memEmail 而是 email，這裡要做轉換
    const data = res.data
    member.value = {
      ...data,
      // 萬一後端給的是 email，自動轉給 memEmail 確保帶入
      memEmail: data.memEmail || data.email || '', 
      memPhone: data.memPhone || data.phone || ''
    }
  } catch (err) {
    errorMsg.value = '載入會員資料失敗'
    Swal.fire({ icon: 'error', title: '載入失敗', text: errorMsg.value })
  } finally {
    isLoading.value = false
  }
}

const validateEditForm = () => {
  const m = member.value
  // 1. 必填檢查
  if (!m.memUsername || !m.memName || !m.memEmail || !m.memPhone) {
    return '基本資料欄位皆為必填'
  }

  // 2. 密碼驗證 (有填才查)
  if (newPassword.value) {
    const pwdRegex = /^(?=.*[a-zA-Z]).{6,}$/
    if (!pwdRegex.test(newPassword.value)) {
      return '新密碼需至少 6 個字元，並包含至少一個英文字母'
    }
  }

  // 3. 手機驗證
  const phoneRegex = /^09\d{8}$/
  if (!phoneRegex.test(m.memPhone)) {
    return '手機格式錯誤，請輸入 09 開頭的 10 位數字'
  }

  // 4. 信箱驗證
  const emailRegex = /^[^\s@]+@[^\s@]+\.com$/
  if (!emailRegex.test(m.memEmail)) {
    return '信箱格式錯誤，必須包含 @ 且結尾為 .com'
  }

  // 5. 發票載具驗證：/ 開頭 + 7 碼大寫英文或數字 (共 8 碼)
  if (m.memInvoice) {
    const invoiceRegex = /^\/[A-Z0-9]{7}$/
    if (!invoiceRegex.test(m.memInvoice)) {
      return '載具格式錯誤！請輸入 / 開頭加上 7 碼大寫英數組合'
    }
  }

  return null
}

const submitEdit = async () => {
  if (!member.value) return

  // 先進行格式校驗
  const error = validateEditForm()
  if (error) {
    await Swal.fire({
      icon: 'warning',
      title: '格式錯誤',
      text: error,
      confirmButtonColor: '#e6a23c',
    })
    return
  }

  // --- 新增：二次確認視窗 ---
  const confirmResult = await Swal.fire({
    title: '確定要修改嗎？',
    text: "修改後的資料將會立即生效！",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#409eff',
    cancelButtonColor: '#909399',
    confirmButtonText: '修改',
    cancelButtonText: '取消'
  })

  if (!confirmResult.isConfirmed) return // 使用者按取消就停止

  errorMsg.value = ''
  isSubmitting.value = true

  try {
    const payload = {
      ...member.value,
      ...(newPassword.value ? { memPassword: newPassword.value } : {}),
    }

    await axios.post('http://localhost:8080/api/members/update', payload)

    await Swal.fire({
      icon: 'success',
      title: '修改成功',
      text: '會員資料已更新',
      timer: 1500,
      showConfirmButton: false
    })

    router.push('/admin/members')
  } catch (err) {
    const msg = err?.response?.data?.message || '修改失敗'
    await Swal.fire({
      icon: 'error',
      title: '修改失敗',
      text: msg,
      confirmButtonColor: '#f56c6c',
    })
  } finally {
    isSubmitting.value = false
  }
}

const goBack = () => router.push('/admin/members')

onMounted(fetchMember)
</script>

<template>
  <div class="container">
    <h2>修改會員資料</h2>
    <div v-if="isLoading" class="loading">資料載入中...</div>
    
    <form v-else @submit.prevent="submitEdit">
      <input type="hidden" v-model="member.memId" />
      
      <label>帳號</label>
      <input type="text" v-model="member.memUsername" />
      
      <label>新密碼</label>
      <input type="password" v-model="newPassword" placeholder="不修改請留空" />
      
      <label>姓名</label>
      <input type="text" v-model="member.memName" />
      
      <label>信箱</label>
      <input type="text" v-model="member.memEmail" />
      
      <label>電話</label>
      <input type="text" v-model="member.memPhone" maxlength="10" />

      <label>點數</label>
      <input type="number" v-model="member.memPoints" />
      
      <label>發票載具</label>
      <input type="text" v-model="member.memInvoice" placeholder="未提供" />
      
      <button type="submit" class="primary-btn" :disabled="isSubmitting">
        {{ isSubmitting ? '修改中...' : '確認修改' }}
      </button>
      
      <a class="back-link" @click.prevent="goBack">回會員列表</a>
    </form>
  </div>
</template>

<style scoped>
/* 樣式維持原有的 Glassmorphism 風格，增加 hover 效果 */
.container {
  width: 420px;
  margin: 30px auto;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #1e3a5f;
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
  width: 80px;
  height: 3px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 2px;
}

label {
  font-weight: 600;
  margin-top: 12px;
  display: block;
  color: #334155;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px 14px;
  margin-bottom: 8px;
  margin-top: 6px;
  box-sizing: border-box;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  background: #f8fafc;
  transition: all 0.2s ease;
}

input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
  outline: none;
  background: #ffffff;
}

.error {
  color: #dc2626;
  text-align: center;
  margin-bottom: 16px;
  padding: 10px;
  background: rgba(220, 38, 38, 0.1);
  border-radius: 8px;
  font-weight: 500;
}

.loading {
  text-align: center;
  color: #64748b;
  padding: 20px;
}

.primary-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.primary-btn:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.back-link {
  display: block;
  margin-top: 20px;
  text-align: center;
  color: #3b82f6;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}
</style>