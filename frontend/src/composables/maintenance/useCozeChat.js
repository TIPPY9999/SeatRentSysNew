import { ref, computed } from 'vue'
import supportApi from '@/api/modules/support'
import { useMemberAuthStore } from '@/stores/memberAuth'

/**
 * Coze OpenAPI 聊天 Composable
 * 
 * 【重構說明】2026-02-01
 * - 完全移除 WebSDK / chatapp 整合（已確認 502 TLB 問題）
 * - 改用 Coze OpenAPI（透過後端 Proxy）
 * - 前端自製聊天 UI，不依賴 SDK
 * 
 * 【功能】
 * 1. 透過後端 Proxy 與 Coze OpenAPI 通訊
 * 2. 管理對話歷史（本地）
 * 3. 降級 UI 狀態管理
 * 4. 重試機制（指數退避）
 */
export function useCozeChat() {
  const memberAuthStore = useMemberAuthStore()

  // ==================== BEGIN: 狀態定義 ====================
  const loading = ref(false)
  const initialized = ref(false)
  const error = ref(null)
  const degraded = ref(false) // 降級模式（API 不可用）
  const retryCount = ref(0)
  const maxRetries = 3

  // 聊天相關狀態
  const messages = ref([]) // { role: 'user'|'assistant', content: string, timestamp: Date }
  const conversationId = ref(null)
  const sending = ref(false) // 正在發送訊息
  // ==================== END: 狀態定義 ====================

  // ==================== BEGIN: 計算屬性 ====================
  const status = computed(() => {
    if (loading.value) return 'loading'
    if (degraded.value) return 'degraded'
    if (initialized.value) return 'ready'
    if (error.value) return 'error'
    return 'idle'
  })

  const statusText = computed(() => {
    const statusMap = {
      loading: '初始化中...',
      degraded: '服務暫時不可用',
      ready: '已就緒',
      error: '初始化失敗',
      idle: '未啟動'
    }
    return statusMap[status.value] || '未知狀態'
  })
  // ==================== END: 計算屬性 ====================

  // ==================== BEGIN: 工具函數 ====================
  const getRetryDelay = (attempt) => {
    const delays = [300, 800, 1500]
    return delays[Math.min(attempt, delays.length - 1)]
  }

  const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

  const getUserId = () => {
    if (memberAuthStore.isLogin && memberAuthStore.member?.memId) {
      return `member_${memberAuthStore.member.memId}`
    }
    const storageKey = 'support_user_id'
    let userId = localStorage.getItem(storageKey)
    if (!userId) {
      userId = crypto.randomUUID 
        ? crypto.randomUUID() 
        : `guest_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      localStorage.setItem(storageKey, userId)
    }
    return userId
  }

  const logDiagnostic = (level, message, data = {}) => {
    const timestamp = new Date().toISOString()
    const safeData = { ...data }
    delete safeData.token
    delete safeData.pat

    if (level === 'error') {
      console.error(`[Coze OpenAPI] ${message}`, safeData)
    } else if (level === 'warn') {
      console.warn(`[Coze OpenAPI] ${message}`, safeData)
    } else {
      console.log(`[Coze OpenAPI] ${message}`, safeData)
    }
  }
  // ==================== END: 工具函數 ====================

  // ==================== BEGIN: 初始化（檢查 API 狀態） ====================
  const initCozeChat = async () => {
    if (initialized.value) {
      logDiagnostic('info', '已初始化，跳過')
      return { success: true, cached: true }
    }

    loading.value = true
    error.value = null
    degraded.value = false

    try {
      logDiagnostic('info', '檢查 Coze OpenAPI 狀態...')
      
      const response = await supportApi.checkCozeStatus()
      const statusData = response.data

      if (statusData.available) {
        initialized.value = true
        logDiagnostic('info', '✅ OpenAPI 可用', { mode: statusData.mode })
        return { success: true, mode: 'openapi' }
      } else {
        // API 不可用，進入降級模式
        degraded.value = true
        error.value = statusData.message || 'API 不可用'
        logDiagnostic('warn', '⚠️ OpenAPI 不可用，進入降級模式', { 
          status: statusData.status,
          message: statusData.message 
        })
        return { success: false, degraded: true, error: statusData.message }
      }

    } catch (err) {
      error.value = err.message || '無法連線至伺服器'
      degraded.value = true
      logDiagnostic('error', '❌ 初始化失敗', { error: err.message })
      return { success: false, error: err.message, degraded: true }

    } finally {
      loading.value = false
    }
  }
  // ==================== END: 初始化 ====================

  // ==================== BEGIN: 發送訊息 ====================
  const sendMessage = async (messageText) => {
    if (!messageText || !messageText.trim()) {
      return { success: false, error: '訊息不可為空' }
    }

    if (degraded.value) {
      return { success: false, error: '服務暫時不可用' }
    }

    sending.value = true
    const userId = getUserId()

    // 先將使用者訊息加入列表
    const userMessage = {
      role: 'user',
      content: messageText.trim(),
      timestamp: new Date()
    }
    messages.value.push(userMessage)

    try {
      logDiagnostic('info', '📤 發送訊息', { length: messageText.length })

      const response = await supportApi.sendChatMessage({
        message: messageText.trim(),
        userId: userId,
        conversationId: conversationId.value
      })

      const result = response.data

      if (result.success) {
        // 儲存 conversationId 供後續對話使用
        if (result.conversationId) {
          conversationId.value = result.conversationId
        }

        // 加入 AI 回覆
        const assistantMessage = {
          role: 'assistant',
          content: result.replyText || '（無回覆）',
          timestamp: new Date()
        }
        messages.value.push(assistantMessage)

        logDiagnostic('info', '✅ 收到回覆', { 
          conversationId: result.conversationId,
          replyLength: result.replyText?.length 
        })

        retryCount.value = 0 // 成功後重置重試計數
        return { success: true, reply: result.replyText }

      } else {
        // API 回傳錯誤
        const errorMsg = result.error || '發送失敗'
        logDiagnostic('warn', '⚠️ API 回傳錯誤', { error: errorMsg })

        // 加入錯誤訊息
        messages.value.push({
          role: 'assistant',
          content: `⚠️ ${errorMsg}`,
          timestamp: new Date(),
          isError: true
        })

        return { success: false, error: errorMsg }
      }

    } catch (err) {
      const errorMsg = err.response?.data?.error || err.message || '網路錯誤'
      const isBusinessError = err.response?.data?.isBusinessError === true
      const status = err.response?.status
      
      logDiagnostic('error', '❌ 發送失敗', { error: errorMsg, status, isBusinessError })

      // ==================== BEGIN: 修正重試邏輯 ====================
      // 只有 502/503/504/408 才重試，400/409 業務錯誤不重試
      const shouldRetryStatus = [502, 503, 504, 408]
      const canRetry = !isBusinessError && 
                       shouldRetryStatus.includes(status) && 
                       retryCount.value < maxRetries
      
      if (canRetry) {
        retryCount.value++
        const delay = getRetryDelay(retryCount.value - 1)
        logDiagnostic('info', `重試 ${retryCount.value}/${maxRetries}，延遲 ${delay}ms`)
        
        // 移除剛加入的使用者訊息（重試時會重新加入）
        messages.value.pop()
        
        await sleep(delay)
        return await sendMessage(messageText)
      }
      // ==================== END: 修正重試邏輯 ====================

      // 加入錯誤訊息
      messages.value.push({
        role: 'assistant',
        content: `⚠️ ${errorMsg}`,
        timestamp: new Date(),
        isError: true
      })

      // 只有連線錯誤才進入降級模式，業務錯誤不進入
      if (!isBusinessError && retryCount.value >= maxRetries) {
        degraded.value = true
        logDiagnostic('warn', '重試次數已達上限，進入降級模式')
      }

      return { success: false, error: errorMsg }

    } finally {
      sending.value = false
    }
  }
  // ==================== END: 發送訊息 ====================

  // ==================== BEGIN: 手動重試 ====================
  const manualRetry = async () => {
    retryCount.value = 0
    degraded.value = false
    error.value = null
    initialized.value = false
    
    return await initCozeChat()
  }
  // ==================== END: 手動重試 ====================

  // ==================== BEGIN: 清除對話 ====================
  const clearMessages = () => {
    messages.value = []
    conversationId.value = null
    logDiagnostic('info', '對話已清除')
  }
  // ==================== END: 清除對話 ====================

  // ==================== BEGIN: 銷毀 ====================
  const destroy = () => {
    messages.value = []
    conversationId.value = null
    initialized.value = false
    degraded.value = false
    error.value = null
    retryCount.value = 0
  }
  // ==================== END: 銷毀 ====================

  return {
    // 狀態
    loading,
    initialized,
    error,
    degraded,
    retryCount,
    status,
    statusText,
    messages,
    conversationId,
    sending,
    
    // 方法
    initCozeChat,
    sendMessage,
    manualRetry,
    clearMessages,
    destroy
  }
}
