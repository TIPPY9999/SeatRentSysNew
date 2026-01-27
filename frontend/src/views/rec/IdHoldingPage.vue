<template>
  <div class="container mt-4">
    <div class="card">
      <div class="card-header">
        <h4>使用者條款與隱私權使用聲明</h4>
      </div>
      <div class="card-body">
        <p>
1. 租賃與歸還認定
租借自 WebApp 顯示訂單時起算。歸還須確保 完全插入機台鎖槽 且 App 彈出「結案單」方視為終止。若因使用者原因導致被他人取走，使用者應負遺失賠償之責。
2. 費率計算
採分段計費制：
● 第一階段：0-30 分鐘，固定收費 NT$45。
● 第二階段：30 分鐘後，每 30 分鐘 NT$30。
● 跨日累計：若超過 24 小時未歸還，除租金外，本公司有權先行鎖定帳號。
3. 使用者檢查義務
使用者取車/取物後應立即檢查。若發現損壞應透過 App 「報修功能」上傳照片。10 分鐘後未報修則視為領取時設備完好。
4. 維修與損害賠償
1. 正常耗損由本公司負擔。
2. 遺失或人為毀損（如塗鴉、切割、支架彎曲）：賠償金 NT$1,500。
3. 若因違規使用（如站立）導致他人受傷，由使用者承擔法律責任。
5. 業者維護責任
本公司承諾定期巡檢設備安全，但 不擔保 設備於租借期間因非預見因素產生的突發故障。若發生故障，使用者應立即停止使用。
6. 爭議處理
若對費用有爭議，應於扣款後 14 日內 提出。本條款以中華民國法律為準，以 台灣桃園地方法院 為第一審管轄法院。

二、 使用者隱私權政策與個資蒐集聲明
根據《個資法》第 8 條規定，您必須在 App 註冊頁面告知以下事項：
1. 蒐集目的與類別
蒐集目的： 租賃服務管理、金流扣款、設備追蹤、客服爭議處理、行銷活動推送。
個資類別： 姓名、聯絡電話、電子郵件、地理位置（GPS）、信用卡號或第三方支付代碼。
2. 利用期間與地區
期間： 會員帳號有效期間內及法律規定之保存期限依會計法規定。
地區： 台灣地區及本公司委託之雲端伺服器所在地。
3. 使用者權利
您可隨時透過 App 設定或聯繫客服行使：查詢、閱覽、製給複製本、補充、更正、停止蒐集處理或利用、刪除個人資料之權利。
4. 設備位置追蹤
為確保資產安全，本服務將於「租借期間」內記錄設備之位置資訊。歸還完成後，系統將停止主動追蹤該次行程位置。



【重要租借告知】 在您點擊租借前，請確認以下事項：
計費方式：前 30 分鐘 NT$45，之後每 30 分鐘 NT$30（不足 30 分鐘以 30 分鐘計）。
安全承諾：本座椅載重上限為  100kg，超過限制導致之損害由使用者自負。 僅供坐臥，禁止站立或超載。使用前請檢查結構，如有異常請於 10分鐘內回報。
正確歸還：請務必將座椅插回機台並確認 App 顯示「租借結束訂單」，否則將持續計費。
賠償責任：若設備遺失或人為嚴重損毀，最高需負擔賠償金 NT$1500。
服務細則：閱讀並同意上述條款視同接受我們的詳細服務細則。
</p>
        
        <div v-if="isLoggedIn" class="alert alert-success">
          <h5 class="alert-heading">驗證成功</h5>
          <p>已成功從 Pinia store 中讀取到會員資料。</p>
          <hr>
          <p class="mb-0">
            會員ID (Member ID): <strong>{{ memberId }}</strong>
          </p>
          <p class="mb-0">
            會員帳號 (Username): <strong>{{ memberUsername }}</strong>
          </p>
          <pre class="mt-2" style="background-color: #f0f0f0; padding: 10px; border-radius: 4px;"><code>{{ fullMemberObject }}</code></pre>
        </div>
        
        <div v-else class="alert alert-warning">
          <h5 class="alert-heading">驗證失敗</h5>
          <p>無法從 Pinia store 中讀取到會員資料。</p>
          <p>這可能是因為您尚未登入，或是瀏覽器被重新整理且應用程式沒有將 localStorage 的資料恢復回 Pinia store。</p>
          <router-link to="/member/login" class="btn btn-primary">前往登入頁面</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

// 1. 引入並實例化 auth store
const authStore = useAuthStore();

// 2. 建立 computed 屬性來安全地獲取資料
//    使用 computed 可以讓資料保持響應性
const isLoggedIn = computed(() => authStore.isLogin && authStore.user);
const memberId = computed(() => authStore.user?.memId || 'N/A'); // 假設會員ID的欄位是 memId
const memberUsername = computed(() => authStore.user?.memUsername || 'N/A');
const fullMemberObject = computed(() => JSON.stringify(authStore.user, null, 2));


// (可選) 應用程式初始化邏輯：
// 理想情況下，這段邏輯應該放在 App.vue 的 onMounted 中，確保應用程式一啟動就執行。
// 這裡放一份是為了確保此測試頁面在單獨測試時也能運作。
onMounted(() => {
  // 如果 Pinia store 是空的，但 localStorage 有資料，則進行"恢復"
  if (!authStore.isLogin) {
    const storedUser = localStorage.getItem('member_user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        // 呼叫 action 來恢復狀態，而不是直接修改 state
        authStore.login(userData, 'member'); 
        console.log('從 localStorage 恢復會員資料到 Pinia store。');
      } catch (e) {
        console.error('從 localStorage 讀取會員資料失敗:', e);
        localStorage.removeItem('member_user');
      }
    }
  }
});
</script>

<style scoped>
.container {
  max-width: 800px;
}
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>