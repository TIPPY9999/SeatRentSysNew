<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { faqData } from '@/data/supportFaq'
import { useMemberAuthStore } from '@/stores/memberAuth'
import supportApi from '@/api/modules/support'

const router = useRouter()
const memberAuthStore = useMemberAuthStore()

// ==================== FAQ 相關狀態 ====================
const searchKeyword = ref('')
const activeCategory = ref(faqData[0]?.category || '')
const activeFaqItems = ref([])

/**
 * 【核心功能】搜尋過濾：同時搜尋標題、內容、標籤、關鍵字
 */
const filteredFaqData = computed(() => {
  const keyword = searchKeyword.value.toLowerCase().trim()
  
  if (!keyword) {
    const currentCategory = faqData.find(cat => cat.category === activeCategory.value)
    return currentCategory ? [currentCategory] : []
  }

  return faqData.map(category => ({
    ...category,
    items: category.items.filter(item => {
      const qMatch = item.q.toLowerCase().includes(keyword)
      const aMatch = item.a.toLowerCase().includes(keyword)
      const tagMatch = item.tags?.some(tag => tag.toLowerCase().includes(keyword))
      const keywordMatch = item.keywords?.some(kw => kw.toLowerCase().includes(keyword))
      return qMatch || aMatch || tagMatch || keywordMatch
    })
  })).filter(cat => cat.items.length > 0)
})

/**
 * 切換分類
 */
const handleCategoryChange = (category) => {
  activeCategory.value = category
  activeFaqItems.value = []
  searchKeyword.value = ''
}

/**
 * 【導航】前往問題回報頁面
 */
const goToReport = () => {
  router.push('/support/report')
}

/**
 * 【統計】總 FAQ 數量
 */
const totalFaqCount = computed(() => {
  return faqData.reduce((sum, cat) => sum + cat.items.length, 0)
})

// ==================== Coze Web Chat SDK 初始化 ====================
const cozeLoading = ref(false)
const cozeInitialized = ref(false)

/**
 * 【核心】動態載入 Coze SDK Script
 * 確保不重複插入 <script> 標籤
 */
const loadCozeSDK = (sdkSrc) => {
  return new Promise((resolve, reject) => {
    // ✅ 防止重複插入：檢查是否已存在
    const existingScript = document.querySelector(`script[src="${sdkSrc}"]`)
    if (existingScript) {
      console.log('[Coze] SDK script 已存在，跳過重複載入')
      // 若已載入且 window.CozeWebSDK 已存在，直接 resolve
      if (window.CozeWebSDK) {
        resolve()
      } else {
        // 否則監聽既有 script 的 load 事件
        existingScript.addEventListener('load', resolve)
        existingScript.addEventListener('error', reject)
      }
      return
    }

    // ✅ 建立新的 <script> 標籤
    const script = document.createElement('script')
    script.src = sdkSrc
    script.async = true
    script.onload = () => {
      console.log('[Coze] SDK 載入成功')
      resolve()
    }
    script.onerror = () => {
      console.error('[Coze] SDK 載入失敗')
      reject(new Error('Coze SDK 載入失敗'))
    }
    document.body.appendChild(script)
  })
}

/**
 * 【核心】初始化 Coze Chat SDK
 * 1. 檢查是否已初始化（避免重複）
 * 2. 呼叫後端 bootstrap API
 * 3. 決定 SDK src（後端優先，否則環境變數）
 * 4. 動態載入 SDK
 * 5. 建立 WebChatClient 實例
 */
const initCozeChat = async () => {
  // ✅ 防止重複初始化：檢查全域旗標
  if (window.__coze_inited) {
    console.log('[Coze] 已初始化，跳過重複執行')
    cozeInitialized.value = true
    return
  }

  cozeLoading.value = true

  try {
    // ==================== Step 1：取得 Bootstrap 配置 ====================
    console.log('[Coze] 開始載入 Bootstrap 配置...')
    const response = await supportApi.getCozeBootstrap()
    const { botId, token, sdkSrc, expiresIn } = response.data

    // ✅ 驗證必要欄位
    if (!botId || !token) {
      console.warn('[Coze] Bootstrap 配置不完整（缺少 botId 或 token），取消初始化')
      return
    }

    console.log('[Coze] Bootstrap 配置載入成功', { botId, sdkSrc, expiresIn })

    // ==================== Step 2：決定 SDK 來源 ====================
    // 優先使用後端回傳的 sdkSrc，若為空則 fallback 到環境變數
    const finalSdkSrc = sdkSrc || import.meta.env.VITE_COZE_CHAT_SDK_SRC

    if (!finalSdkSrc) {
      console.warn('[Coze] 無法取得 SDK 來源（後端與環境變數皆為空），取消初始化')
      return
    }

    console.log('[Coze] 使用 SDK 來源:', finalSdkSrc)

    // ==================== Step 3：載入 SDK Script ====================
    await loadCozeSDK(finalSdkSrc)

    // ✅ 驗證 SDK 是否成功載入
    if (!window.CozeWebSDK) {
      console.error('[Coze] SDK 載入後 window.CozeWebSDK 不存在')
      return
    }

    // ==================== Step 4：建立使用者 ID（降低聊天串台） ====================
    const getUserId = () => {
      // 優先使用已登入會員資訊
      if (memberAuthStore.isLogin && memberAuthStore.member?.memId) {
        return `member_${memberAuthStore.member.memId}`
      }

      // 未登入：使用 localStorage 保存的 UUID
      const storageKey = 'support_user_id'
      let userId = localStorage.getItem(storageKey)
      
      if (!userId) {
        // 使用 crypto.randomUUID() 產生唯一 ID（現代瀏覽器支援）
        userId = crypto.randomUUID ? crypto.randomUUID() : `guest_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        localStorage.setItem(storageKey, userId)
      }

      return userId
    }

    const userId = getUserId()
    console.log('[Coze] 使用者 ID:', userId.startsWith('member_') ? userId : '[匿名訪客]')

    // ==================== Step 5：建立 WebChatClient 實例 ====================
    window.__cozeClient = new window.CozeWebSDK.WebChatClient({
      config: {
        bot_id: botId
      },
      componentProps: {
        title: 'Take@Seat 智能客服'
      },
      auth: {
        type: 'token',
        token: token,
        // ✅ Token 過期時自動刷新（重新呼叫 bootstrap API）
        onRefreshToken: async () => {
          console.log('[Coze] Token 即將過期，自動刷新...')
          try {
            const refreshResponse = await supportApi.getCozeBootstrap()
            const newToken = refreshResponse.data.token
            console.log('[Coze] Token 刷新成功')
            return newToken
          } catch (error) {
            console.error('[Coze] Token 刷新失敗:', error.message)
            throw error
          }
        }
      }
    })

    // ✅ 若 SDK 支援設定使用者資訊（依實際 SDK 版本調整）
    if (typeof window.__cozeClient.setUser === 'function') {
      window.__cozeClient.setUser({ id: userId })
    }

    // ✅ 設定全域旗標，防止重複初始化
    window.__coze_inited = true
    cozeInitialized.value = true

    console.log('[Coze] 初始化完成 ✅')

  } catch (error) {
    // ✅ 錯誤處理：僅印出不含 token 的錯誤訊息
    if (error.response?.status === 500 || error.response?.status === 400) {
      console.warn('[Coze] Bootstrap API 回傳錯誤（可能缺少環境變數），泡泡功能不可用')
    } else {
      console.error('[Coze] 初始化失敗:', error.message)
    }
  } finally {
    cozeLoading.value = false
  }
}

/**
 * 【生命週期】組件掛載後自動初始化 Coze
 */
onMounted(() => {
  // ✅ 延遲 1 秒後初始化，避免阻塞頁面渲染
  setTimeout(() => {
    initCozeChat()
  }, 1000)
})

/**
 * 【生命週期】組件卸載時清理（可選）
 * 注意：Coze SDK 泡泡掛在 body 上，不強制銷毀
 * 透過 window.__coze_inited 旗標確保下次進入不重複初始化
 */
onUnmounted(() => {
  // 可選：若需要完全清理，可呼叫 window.__cozeClient?.destroy()
  // 但通常保留泡泡讓使用者在其他頁面也能使用
})
</script>

<template>
  <div class="support-center-container">
    <!-- Hero 區域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-icon">
          <i class="fas fa-life-ring"></i>
        </div>
        <h1 class="hero-title">客服支援中心</h1>
        <p class="hero-subtitle">我們隨時為您提供協助</p>

        <!-- 搜尋框 -->
        <div class="search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜尋常見問題、關鍵字..."
            size="large"
            clearable
            class="search-input"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </div>

        <div class="hero-stats">
          <span><i class="fas fa-book-open"></i> {{ totalFaqCount }} 個常見問題</span>
          <span><i class="fas fa-clock"></i> 24/7 全天候服務</span>
        </div>
      </div>
    </section>

    <!-- 主要內容區 -->
    <section class="content-section">
      <div class="container-fluid">
        <!-- 分類 Tabs -->
        <div class="category-tabs" v-if="!searchKeyword">
          <div
            v-for="category in faqData"
            :key="category.category"
            class="category-tab"
            :class="{ active: activeCategory === category.category }"
            @click="handleCategoryChange(category.category)"
          >
            <i :class="category.icon" :style="{ color: category.color }"></i>
            <span>{{ category.category }}</span>
            <el-badge :value="category.items.length" type="info" />
          </div>
        </div>

        <!-- 搜尋結果提示 -->
        <div v-if="searchKeyword" class="search-result-hint">
          <i class="fas fa-filter"></i>
          搜尋「<b>{{ searchKeyword }}</b
          >」共找到
          <span class="result-count">{{
            filteredFaqData.reduce((sum, cat) => sum + cat.items.length, 0)
          }}</span>
          筆結果
          <el-button text type="primary" size="small" @click="searchKeyword = ''">
            <i class="fas fa-times"></i> 清除搜尋
          </el-button>
        </div>

        <!-- FAQ 列表 -->
        <div class="faq-list">
          <div
            v-for="category in filteredFaqData"
            :key="category.category"
            class="faq-category-block"
          >
            <!-- 分類標題（僅在搜尋模式顯示） -->
            <div v-if="searchKeyword" class="category-title">
              <i :class="category.icon" :style="{ color: category.color }"></i>
              {{ category.category }}
            </div>

            <!-- FAQ Accordion -->
            <el-collapse v-model="activeFaqItems" accordion>
              <el-collapse-item
                v-for="(item, index) in category.items"
                :key="`${category.category}-${index}`"
                :name="`${category.category}-${index}`"
                class="faq-item"
              >
                <template #title>
                  <div class="faq-question">
                    <i class="fas fa-question-circle"></i>
                    <span v-html="item.q"></span>
                  </div>
                </template>
                <div class="faq-answer" v-html="item.a"></div>
                <div class="faq-tags" v-if="item.tags && item.tags.length">
                  <el-tag
                    v-for="tag in item.tags"
                    :key="tag"
                    size="small"
                    type="info"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 無結果提示 -->
          <el-empty
            v-if="filteredFaqData.length === 0"
            description="找不到相關問題"
            :image-size="120"
          >
            <template #image>
              <i class="fas fa-search" style="font-size: 80px; color: #dcdfe6"></i>
            </template>
            <el-button type="primary" @click="goToReport">
              <i class="fas fa-exclamation-circle mr-1"></i> 直接回報問題
            </el-button>
          </el-empty>
        </div>

        <!-- CTA 按鈕區 -->
        <div class="cta-section">
          <!-- AI 客服卡片 -->
          <div class="cta-card cta-card-ai">
            <div class="cta-icon">
              <i class="fas fa-robot"></i>
            </div>
            <div class="cta-content">
              <h3>
                試試 AI 智能客服
                <el-tag v-if="cozeInitialized" type="success" size="small" effect="dark">
                  <i class="fas fa-check-circle"></i> 已就緒
                </el-tag>
                <el-tag v-else-if="cozeLoading" type="warning" size="small" effect="dark">
                  <i class="fas fa-spinner fa-spin"></i> 初始化中
                </el-tag>
              </h3>
              <p>即時解答您的疑問，24/7 全天候服務，請點擊右下角聊天圖示開始對話</p>
            </div>
            <el-button
              type="success"
              size="large"
              :disabled="!cozeInitialized"
              :loading="cozeLoading"
            >
              <i class="fas fa-comments mr-2"></i>
              {{ cozeInitialized ? '泡泡已就緒' : cozeLoading ? '載入中...' : '初始化失敗' }}
            </el-button>
          </div>

          <!-- 問題回報卡片 -->
          <div class="cta-card cta-card-report">
            <div class="cta-icon">
              <i class="fas fa-headset"></i>
            </div>
            <div class="cta-content">
              <h3>人工客服協助</h3>
              <p>若以上內容無法解決您的問題，請填寫問題回報表單，我們會盡快為您處理</p>
            </div>
            <el-button type="primary" size="large" @click="goToReport">
              <i class="fas fa-paper-plane mr-2"></i> 我要回報問題
            </el-button>
          </div>
        </div>

        <!-- 聯繫方式 -->
        <div class="contact-info">
          <div class="contact-item">
            <i class="fas fa-phone"></i>
            <span>客服專線：0968-179-091</span>
          </div>
          <div class="contact-item">
            <i class="fas fa-envelope"></i>
            <span>Email：support@seatrentsys.com</span>
          </div>
          <div class="contact-item">
            <i class="fas fa-clock"></i>
            <span>服務時間：24/7 全天候</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ========== 頁面容器 ========== */
.support-center-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* ========== Hero 區域 ========== */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px 60px;
  color: white;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 1;
}

.hero-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 10px;
}

.hero-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 30px;
}

.search-wrapper {
  max-width: 600px;
  margin: 0 auto 20px;
}

.search-input {
  border-radius: 50px;
  overflow: hidden;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 50px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  padding: 0 20px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 0.9rem;
  opacity: 0.9;
}

.hero-stats span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ========== 內容區域 ========== */
.content-section {
  padding: 40px 20px;
}

/* ========== 分類 Tabs ========== */
.category-tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  white-space: nowrap;
}

.category-tab:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.category-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.category-tab.active i {
  color: white !important;
}

/* ========== 搜尋結果提示 ========== */
.search-result-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  background: #ecf5ff;
  border-radius: 12px;
  margin-bottom: 20px;
  color: #409eff;
  font-size: 14px;
}

.result-count {
  font-weight: 700;
  color: #409eff;
}

/* ========== FAQ 列表 ========== */
.faq-list {
  max-width: 900px;
  margin: 0 auto;
}

.faq-category-block {
  margin-bottom: 30px;
}

.category-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
}

.faq-item {
  margin-bottom: 10px;
}

.faq-item :deep(.el-collapse-item__header) {
  background: white;
  border-radius: 12px;
  padding: 20px;
  font-size: 15px;
  transition: all 0.3s ease;
}

.faq-item :deep(.el-collapse-item__header:hover) {
  background: #f5f7fa;
  transform: translateX(5px);
}

.faq-item :deep(.el-collapse-item__wrap) {
  border: none;
  background: white;
  border-radius: 0 0 12px 12px;
}

.faq-item :deep(.el-collapse-item__content) {
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.faq-question {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #303133;
  font-weight: 500;
}

.faq-question i {
  color: #409eff;
  font-size: 18px;
}

.faq-answer {
  color: #606266;
  line-height: 1.8;
}

.faq-answer :deep(ol),
.faq-answer :deep(ul) {
  margin: 10px 0;
}

.faq-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
}

/* ========== CTA 區域 ========== */
.cta-section {
  margin: 60px 0 40px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.cta-card {
  padding: 40px;
  border-radius: 20px;
  color: white;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.cta-card:hover {
  transform: translateY(-5px);
}

.cta-card-ai {
  background: linear-gradient(135deg, #67c23a 0%, #409eff 100%);
}

.cta-card-report {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.cta-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.cta-content h3 {
  font-size: 1.5rem;
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.cta-content p {
  opacity: 0.9;
  margin-bottom: 25px;
  font-size: 0.95rem;
  line-height: 1.6;
}

.cta-card .el-button {
  background: white;
  border: none;
  font-weight: 600;
  padding: 15px 40px;
  border-radius: 50px;
  transition: all 0.3s ease;
}

.cta-card-ai .el-button {
  color: #67c23a;
}

.cta-card-report .el-button {
  color: #667eea;
}

.cta-card .el-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

/* ========== 聯繫方式 ========== */
.contact-info {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 30px;
  padding: 30px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  max-width: 900px;
  margin: 0 auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 14px;
}

.contact-item i {
  color: #409eff;
  font-size: 18px;
}

/* ========== 響應式設計 ========== */
@media (max-width: 768px) {
  .hero-title {
    font-size: 1.8rem;
  }

  .category-tabs {
    flex-direction: column;
  }

  .cta-section {
    grid-template-columns: 1fr;
  }

  .cta-card {
    padding: 30px 20px;
  }

  .contact-info {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* ========== 輔助類 ========== */
.mr-1 {
  margin-right: 4px;
}
.mr-2 {
  margin-right: 8px;
}
</style>
