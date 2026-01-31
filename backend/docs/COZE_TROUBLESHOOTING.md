# Coze Web Chat SDK 疑難排解指南

## 📋 系統架構檢查清單

### ✅ 1. 後端配置（Backend）

#### 1.1 環境變數設定
```bash
# 在終端機設定（Windows PowerShell）
$env:COZE_BOT_ID="你的Bot ID"
$env:COZE_PAT="你的Personal Access Token"
$env:COZE_CHAT_SDK_SRC="https://lf-cdn.coze.cn/obj/unpkg/flow-platform/chat-app-sdk/1.0.0-beta.4/libs/oversea/index.js"
```

驗證方式：
```bash
# 檢查環境變數是否設定成功
echo $env:COZE_BOT_ID
echo $env:COZE_PAT
echo $env:COZE_CHAT_SDK_SRC
```

#### 1.2 Backend API 測試
```bash
# 啟動後端（在 backend/ 目錄下）
mvn spring-boot:run

# 測試 Bootstrap API（在新終端機）
curl http://localhost:8080/api/support/coze/bootstrap
```

✅ **預期回應**（200 OK）：
```json
{
  "botId": "7469370888888888888",
  "token": "pat_xxxxxxxxxxxx",
  "sdkSrc": "https://lf-cdn.coze.cn/obj/unpkg/flow-platform/chat-app-sdk/1.0.0-beta.4/libs/oversea/index.js",
  "expiresIn": 0,
  "serverTime": "2026-01-31T12:00:00",
  "note": "⚠️ PAT Token 提醒：此 Token 有過期時間，過期後需要重新建立"
}
```

❌ **常見錯誤**：
- **500 錯誤**：檢查環境變數是否設定
- **404 錯誤**：Controller 未啟動或路徑錯誤
- **CORS 錯誤**：檢查 SecurityConfig.java 是否允許 /api/**

---

### ✅ 2. 前端配置（Frontend）

#### 2.1 檔案檢查
確認以下檔案存在且路徑正確：

```
frontend/
├── .env
├── vite.config.js
├── src/
│   ├── api/
│   │   ├── http.js                    ✅ baseURL: '/api'
│   │   └── modules/
│   │       └── support.js              ✅ getCozeBootstrap()
│   ├── views/
│   │   └── support/
│   │       └── SupportCenterView.vue   ✅ initCozeChat()
│   └── router/
│       └── index.js                    ✅ /support, /support/report
```

#### 2.2 Axios 配置檢查
檔案：`frontend/src/api/http.js`

```javascript
const http = axios.create({
  baseURL: '/api', // ✅ 必須是 '/api'，不能是完整 URL
  timeout: 10000,
})
```

#### 2.3 API 模組路徑檢查
檔案：`frontend/src/api/modules/support.js`

```javascript
export const getCozeBootstrap = () => {
  return http.get('/support/coze/bootstrap') // ✅ 不加 /api 前綴
}
```

完整路徑拼接：
```
baseURL: '/api' + '/support/coze/bootstrap'
= '/api/support/coze/bootstrap'
→ Vite Proxy 轉發 → http://localhost:8080/api/support/coze/bootstrap
```

#### 2.4 Vite Proxy 配置檢查
檔案：`frontend/vite.config.js`

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // ✅ 不需要 rewrite，直接轉發
      },
    },
  },
})
```

---

### ✅ 3. 前端測試步驟

#### 3.1 啟動開發伺服器
```bash
cd frontend
npm run dev
```

#### 3.2 瀏覽器 Console 檢查
開啟 Chrome DevTools（F12），前往 `/support` 頁面，查看 Console：

✅ **成功初始化**：
```
[Coze] 開始載入 Bootstrap 配置...
[Coze] Bootstrap 配置載入成功 { botId: "7469370888888888888", sdkSrc: "https://...", expiresIn: 0 }
[Coze] 使用 SDK 來源: https://lf-cdn.coze.cn/obj/unpkg/flow-platform/chat-app-sdk/1.0.0-beta.4/libs/oversea/index.js
[Coze] SDK 載入成功
[Coze] 使用者 ID: member_123 或 [匿名訪客]
[Coze] 初始化完成 ✅
```

❌ **常見錯誤排查**：

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `GET http://localhost:5173/api/api/support/coze/bootstrap 404` | 路徑重複（api/api） | 確認 `support.js` 路徑為 `/support/...`，不是 `/api/support/...` |
| `[Coze] Bootstrap 配置不完整（缺少 botId 或 token）` | 後端環境變數未設定 | 檢查 `$env:COZE_BOT_ID` 和 `$env:COZE_PAT` |
| `Coze SDK 載入失敗` | SDK 來源 URL 錯誤或網路問題 | 檢查 `COZE_CHAT_SDK_SRC` 是否正確 |
| `Access to XMLHttpRequest blocked by CORS` | CORS 未正確配置 | 檢查 `SecurityConfig.java` CORS 設定 |

#### 3.3 Network Tab 檢查
在 Chrome DevTools → Network Tab，查看 `/bootstrap` 請求：

✅ **成功請求**：
- **URL**：`http://localhost:5173/api/support/coze/bootstrap`
- **Method**：GET
- **Status**：200 OK
- **Response**：包含 `botId`, `token`, `sdkSrc`

❌ **失敗請求**：
- **404 Not Found**：後端 Controller 路徑錯誤
- **500 Internal Server Error**：後端環境變數未設定
- **CORS Error**：CORS 未正確配置

---

## 🔧 快速診斷指令

### Windows PowerShell 診斷腳本
```powershell
# ==================== 診斷腳本 ====================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Coze Web Chat SDK 診斷工具" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. 檢查環境變數
Write-Host "【1】檢查環境變數..." -ForegroundColor Yellow
if ($env:COZE_BOT_ID) {
    Write-Host "  ✅ COZE_BOT_ID: $env:COZE_BOT_ID" -ForegroundColor Green
} else {
    Write-Host "  ❌ COZE_BOT_ID 未設定" -ForegroundColor Red
}

if ($env:COZE_PAT) {
    $maskedPat = $env:COZE_PAT.Substring(0, [Math]::Min(10, $env:COZE_PAT.Length)) + "..."
    Write-Host "  ✅ COZE_PAT: $maskedPat" -ForegroundColor Green
} else {
    Write-Host "  ❌ COZE_PAT 未設定" -ForegroundColor Red
}

if ($env:COZE_CHAT_SDK_SRC) {
    Write-Host "  ✅ COZE_CHAT_SDK_SRC: $env:COZE_CHAT_SDK_SRC" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  COZE_CHAT_SDK_SRC 未設定（將使用前端 .env 的 fallback）" -ForegroundColor Yellow
}

# 2. 檢查後端是否運行
Write-Host "`n【2】檢查後端服務..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/support/coze/bootstrap" -Method GET -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ 後端 Bootstrap API 正常運行" -ForegroundColor Green
        Write-Host "  回應內容: $($response.Content)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ❌ 後端 Bootstrap API 無法連接" -ForegroundColor Red
    Write-Host "  錯誤: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  請確認後端是否啟動: cd backend && mvn spring-boot:run" -ForegroundColor Yellow
}

# 3. 檢查前端配置檔案
Write-Host "`n【3】檢查前端配置檔案..." -ForegroundColor Yellow
$httpJsPath = "frontend/src/api/http.js"
if (Test-Path $httpJsPath) {
    $httpJsContent = Get-Content $httpJsPath -Raw
    if ($httpJsContent -match "baseURL:\s*['""]\/api['""]") {
        Write-Host "  ✅ http.js baseURL 配置正確: '/api'" -ForegroundColor Green
    } else {
        Write-Host "  ❌ http.js baseURL 配置錯誤" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 找不到 $httpJsPath" -ForegroundColor Red
}

$supportJsPath = "frontend/src/api/modules/support.js"
if (Test-Path $supportJsPath) {
    $supportJsContent = Get-Content $supportJsPath -Raw
    if ($supportJsContent -match "http\.get\(['""]\/support\/coze\/bootstrap['""]") {
        Write-Host "  ✅ support.js 路徑配置正確: '/support/coze/bootstrap'" -ForegroundColor Green
    } else {
        Write-Host "  ❌ support.js 路徑配置錯誤（不應該有 /api 前綴）" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 找不到 $supportJsPath" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "診斷完成！" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
```

使用方式：
```powershell
# 將上述腳本儲存為 diagnose-coze.ps1，然後執行
.\diagnose-coze.ps1
```

---

## 🎯 最終檢查清單

### Backend（後端）
- [ ] 環境變數已設定（COZE_BOT_ID, COZE_PAT, COZE_CHAT_SDK_SRC）
- [ ] application.yml 有 `coze:` 配置區塊
- [ ] SupportCozeController.java 存在且路徑為 `/api/support/coze/bootstrap`
- [ ] 執行 `curl http://localhost:8080/api/support/coze/bootstrap` 回傳 200
- [ ] SecurityConfig.java 允許 `/api/**` 且配置 CORS

### Frontend（前端）
- [ ] http.js 的 `baseURL: '/api'`
- [ ] support.js 的路徑為 `/support/coze/bootstrap`（不加 /api）
- [ ] SupportCenterView.vue 有 `initCozeChat()` 方法
- [ ] vite.config.js 有 proxy 配置 `'/api': { target: 'http://localhost:8080' }`
- [ ] .env 有 `VITE_COZE_CHAT_SDK_SRC` fallback
- [ ] router/index.js 有 `/support` 和 `/support/report` 路由
- [ ] MainLayout.vue 有客服入口連結

### Runtime（執行時）
- [ ] 後端啟動無錯誤訊息
- [ ] 前端啟動無錯誤訊息
- [ ] 瀏覽器 Console 顯示 `[Coze] 初始化完成 ✅`
- [ ] Network Tab 看到 `/api/support/coze/bootstrap` 請求且回傳 200
- [ ] 頁面右下角出現 Coze 聊天泡泡

---

## 📞 常見問題 FAQ

### Q1: 為什麼路徑會變成 `/api/api/support/...`？
**A**: 原因是 `support.js` 的路徑寫成 `/api/support/...`，導致與 `http.js` 的 `baseURL: '/api'` 重複拼接。

**解決方式**：
```javascript
// ❌ 錯誤寫法
return http.get('/api/support/coze/bootstrap')

// ✅ 正確寫法
return http.get('/support/coze/bootstrap')
```

### Q2: 泡泡沒有出現，但 Console 顯示初始化成功？
**A**: 可能是 SDK 版本或配置問題。

**檢查步驟**：
1. 確認 `window.__cozeClient` 是否存在
2. 確認 `window.CozeWebSDK` 是否成功載入
3. 檢查 SDK 來源 URL 是否正確
4. 嘗試手動呼叫 `window.__cozeClient.open()` 開啟聊天視窗

### Q3: Token 過期怎麼辦？
**A**: Coze PAT 有過期時間，過期後需要重新建立。

**處理方式**：
1. 登入 Coze 平台
2. 重新建立 Personal Access Token
3. 更新環境變數 `COZE_PAT`
4. 重新啟動後端

前端已實作自動刷新機制（`onRefreshToken`），會在 Token 即將過期時自動呼叫 bootstrap API。

### Q4: CORS 錯誤怎麼解決？
**A**: 確認 SecurityConfig.java 有以下配置：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:5173"); // Vite 開發伺服器
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 🚀 快速啟動流程

```bash
# ==================== 終端機 1：啟動後端 ====================
cd backend

# 設定環境變數（Windows PowerShell）
$env:COZE_BOT_ID="你的Bot ID"
$env:COZE_PAT="你的Personal Access Token"
$env:COZE_CHAT_SDK_SRC="https://lf-cdn.coze.cn/obj/unpkg/flow-platform/chat-app-sdk/1.0.0-beta.4/libs/oversea/index.js"

# 啟動後端
mvn spring-boot:run

# ==================== 終端機 2：啟動前端 ====================
cd frontend
npm run dev

# ==================== 終端機 3：測試 API ====================
# 測試後端 Bootstrap API
curl http://localhost:8080/api/support/coze/bootstrap

# 開啟瀏覽器前往 http://localhost:5173/support
```

---

## 📝 維護建議

### 定期檢查
- [ ] 每 90 天更換 Coze PAT Token
- [ ] 檢查 Coze SDK 版本是否有更新
- [ ] 監控後端 log，確認 bootstrap API 無異常呼叫

### 監控指標
- Bootstrap API 呼叫次數
- Token 刷新頻率
- 初始化失敗率
- 使用者滿意度（Chat 使用率）

---

**最後更新**：2026-01-31  
**維護者**：Take@Seat 開發團隊
