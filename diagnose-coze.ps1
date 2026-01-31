# Coze Web Chat SDK 診斷工具
# 用途：快速檢查 Coze 整合的前後端配置是否正確

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Coze Web Chat SDK 診斷工具" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ==================== 1. 檢查環境變數 ====================
Write-Host "【1】檢查環境變數..." -ForegroundColor Yellow
if ($env:COZE_BOT_ID) {
    Write-Host "  ✅ COZE_BOT_ID: $env:COZE_BOT_ID" -ForegroundColor Green
} else {
    Write-Host "  ❌ COZE_BOT_ID 未設定" -ForegroundColor Red
    Write-Host "     設定方式: `$env:COZE_BOT_ID=`"你的Bot ID`"" -ForegroundColor Yellow
}

if ($env:COZE_PAT) {
    $maskedPat = $env:COZE_PAT.Substring(0, [Math]::Min(10, $env:COZE_PAT.Length)) + "..."
    Write-Host "  ✅ COZE_PAT: $maskedPat" -ForegroundColor Green
} else {
    Write-Host "  ❌ COZE_PAT 未設定" -ForegroundColor Red
    Write-Host "     設定方式: `$env:COZE_PAT=`"你的Personal Access Token`"" -ForegroundColor Yellow
}

if ($env:COZE_CHAT_SDK_SRC) {
    Write-Host "  ✅ COZE_CHAT_SDK_SRC: $env:COZE_CHAT_SDK_SRC" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  COZE_CHAT_SDK_SRC 未設定（將使用前端 .env 的 fallback）" -ForegroundColor Yellow
}

# ==================== 2. 檢查後端服務 ====================
Write-Host "`n【2】檢查後端服務..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/support/coze/bootstrap" -Method GET -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ 後端 Bootstrap API 正常運行" -ForegroundColor Green
        
        # 解析 JSON 回應
        $jsonResponse = $response.Content | ConvertFrom-Json
        Write-Host "  回應內容:" -ForegroundColor Gray
        Write-Host "    - botId: $($jsonResponse.botId)" -ForegroundColor Gray
        Write-Host "    - token: $($jsonResponse.token.Substring(0, [Math]::Min(10, $jsonResponse.token.Length)))..." -ForegroundColor Gray
        Write-Host "    - sdkSrc: $($jsonResponse.sdkSrc)" -ForegroundColor Gray
        Write-Host "    - expiresIn: $($jsonResponse.expiresIn)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ❌ 後端 Bootstrap API 無法連接" -ForegroundColor Red
    Write-Host "  錯誤: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  請確認後端是否啟動: cd backend && mvn spring-boot:run" -ForegroundColor Yellow
}

# ==================== 3. 檢查前端配置檔案 ====================
Write-Host "`n【3】檢查前端配置檔案..." -ForegroundColor Yellow

# 檢查 http.js
$httpJsPath = "frontend/src/api/http.js"
if (Test-Path $httpJsPath) {
    $httpJsContent = Get-Content $httpJsPath -Raw
    if ($httpJsContent -match "baseURL:\s*['""]\/api['""]") {
        Write-Host "  ✅ http.js baseURL 配置正確: '/api'" -ForegroundColor Green
    } else {
        Write-Host "  ❌ http.js baseURL 配置錯誤" -ForegroundColor Red
        Write-Host "     應該是: baseURL: '/api'" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 找不到 $httpJsPath" -ForegroundColor Red
}

# 檢查 support.js
$supportJsPath = "frontend/src/api/modules/support.js"
if (Test-Path $supportJsPath) {
    $supportJsContent = Get-Content $supportJsPath -Raw
    if ($supportJsContent -match "http\.get\(['""]\/support\/coze\/bootstrap['""]") {
        Write-Host "  ✅ support.js 路徑配置正確: '/support/coze/bootstrap'" -ForegroundColor Green
    } elseif ($supportJsContent -match "http\.get\(['""]\/api\/support\/coze\/bootstrap['""]") {
        Write-Host "  ❌ support.js 路徑配置錯誤（不應該有 /api 前綴）" -ForegroundColor Red
        Write-Host "     應該是: http.get('/support/coze/bootstrap')" -ForegroundColor Yellow
    } else {
        Write-Host "  ❌ support.js 路徑配置錯誤" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 找不到 $supportJsPath" -ForegroundColor Red
}

# 檢查 vite.config.js
$viteConfigPath = "frontend/vite.config.js"
if (Test-Path $viteConfigPath) {
    $viteConfigContent = Get-Content $viteConfigPath -Raw
    if ($viteConfigContent -match "['""]\/api['""]:\s*\{[^}]*target:\s*['""]http:\/\/localhost:8080['""]") {
        Write-Host "  ✅ vite.config.js proxy 配置正確" -ForegroundColor Green
    } else {
        Write-Host "  ❌ vite.config.js proxy 配置錯誤" -ForegroundColor Red
        Write-Host "     應該有: '/api': { target: 'http://localhost:8080' }" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 找不到 $viteConfigPath" -ForegroundColor Red
}

# 檢查 SupportCenterView.vue
$supportViewPath = "frontend/src/views/support/SupportCenterView.vue"
if (Test-Path $supportViewPath) {
    $supportViewContent = Get-Content $supportViewPath -Raw
    if ($supportViewContent -match "const\s+initCozeChat\s*=\s*async\s*\(\)") {
        Write-Host "  ✅ SupportCenterView.vue 有 initCozeChat() 方法" -ForegroundColor Green
    } else {
        Write-Host "  ❌ SupportCenterView.vue 缺少 initCozeChat() 方法" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 找不到 $supportViewPath" -ForegroundColor Red
}

# ==================== 4. 檢查路由配置 ====================
Write-Host "`n【4】檢查路由配置..." -ForegroundColor Yellow
$routerPath = "frontend/src/router/index.js"
if (Test-Path $routerPath) {
    $routerContent = Get-Content $routerPath -Raw
    $supportRouteExists = $routerContent -match "path:\s*['""]\/support['""]"
    $reportRouteExists = $routerContent -match "path:\s*['""]\/support\/report['""]"
    
    if ($supportRouteExists -and $reportRouteExists) {
        Write-Host "  ✅ 路由配置正確（/support, /support/report）" -ForegroundColor Green
    } else {
        if (-not $supportRouteExists) {
            Write-Host "  ❌ 缺少 /support 路由" -ForegroundColor Red
        }
        if (-not $reportRouteExists) {
            Write-Host "  ❌ 缺少 /support/report 路由" -ForegroundColor Red
        }
    }
} else {
    Write-Host "  ❌ 找不到 $routerPath" -ForegroundColor Red
}

# ==================== 5. 檢查 MainLayout 入口 ====================
Write-Host "`n【5】檢查 MainLayout 客服入口..." -ForegroundColor Yellow
$mainLayoutPath = "frontend/src/layouts/MainLayout.vue"
if (Test-Path $mainLayoutPath) {
    $mainLayoutContent = Get-Content $mainLayoutPath -Raw
    if ($mainLayoutContent -match "router\.push\(['""]\/support['""]") {
        Write-Host "  ✅ MainLayout 有客服入口連結" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  MainLayout 可能缺少客服入口連結" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 找不到 $mainLayoutPath" -ForegroundColor Red
}

# ==================== 總結 ====================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "診斷完成！" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "📋 快速修正指南：" -ForegroundColor Yellow
Write-Host "1. 如果環境變數未設定，請執行：" -ForegroundColor Gray
Write-Host "   `$env:COZE_BOT_ID=`"你的Bot ID`"" -ForegroundColor Gray
Write-Host "   `$env:COZE_PAT=`"你的Personal Access Token`"" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 如果後端無法連接，請啟動後端：" -ForegroundColor Gray
Write-Host "   cd backend && mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 如果前端配置錯誤，請參考 COZE_TROUBLESHOOTING.md" -ForegroundColor Gray
Write-Host ""
Write-Host "4. 啟動前端開發伺服器：" -ForegroundColor Gray
Write-Host "   cd frontend && npm run dev" -ForegroundColor Gray
Write-Host ""
Write-Host "5. 前往瀏覽器測試：http://localhost:5173/support" -ForegroundColor Gray
Write-Host ""
