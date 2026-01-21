# Phase 2: 後端邏輯與畫面狀態同步稽核報告

**稽核日期：** 2026-01-22  
**稽核範圍：** 維修模組 - 地圖狀態同步、資產健康度統計、租借驗證  
**稽核方法：** 代碼審查 + 資料流追蹤 + API 端點分析  

---

## 📊 執行摘要

### 關鍵發現
- **高優先級問題：** 3 個（P1-P3）
- **中優先級問題：** 2 個（P4-P5）
- **影響範圍：** 地圖顯示、資產統計、租借流程、工單管理
- **建議修復時程：** P1-P3 需立即處理，P4-P5 可排入下一迭代

### 問題分類統計
| 類別 | 數量 | 影響程度 |
|------|------|----------|
| 資料不同步 | 2 | 高 |
| 缺少即時驗證 | 1 | 高 |
| 狀態級聯不完整 | 1 | 中 |
| 欄位混用錯誤 | 1 | 中 |

---

## 🔴 P1: 地圖 API 不檢查即時工單狀態（高優先級）

### 問題描述
前端地圖（EnterancePage.vue）呼叫 `/spot/list` 端點獲取據點資訊，但該 API 僅回傳據點的**靜態 `spotStatus` 欄位**，不會動態檢查是否有**進行中的維修工單**。

### 根本原因分析
**檔案路徑：**
- 前端：`frontend/src/views/EnterancePage.vue` (lines 33-67)
- 後端：`backend/src/main/java/.../controller/RentalSpotController.java`
- DTO：`backend/src/main/java/.../dto/SpotMapDto.java`

**程式碼問題：**
```java
// RentalSpotController.java - 當前實作
@GetMapping("/list")
public ResponseEntity<?> getAllSpots() {
    List<RentalSpot> spots = rentalSpotService.findAll();
    // 直接轉換，未檢查維修工單狀態
    List<SpotMapDto> dtos = spots.stream()
        .map(SpotMapDto::fromEntity)
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}
```

**資料流問題：**
```
Database (rental_spot.spot_status) 
  ↓ (靜態讀取)
RentalSpotController 
  ↓ (未查詢 maintenance_information)
SpotMapDto 
  ↓ (過時資料)
前端地圖 (EnterancePage.vue)
```

### 影響範圍
1. **用戶體驗：** 地圖顯示「營運中」，但實際機台維修中無法租借
2. **業務邏輯：** 用戶可能誤以為可以租借，導致訂單失敗
3. **資料一致性：** 工單系統與地圖系統資料脫節

### 真實案例場景
```
時間軸：
09:00 - 機台故障，建立工單 (status=UNDER_MAINTENANCE)
09:05 - 管理員派工給維修人員
09:10 - 用戶打開地圖，看到「綠色」標記（營運中）
09:12 - 用戶前往現場，發現機台維修中無法使用
09:15 - 用戶投訴：「地圖顯示可用，為何無法租借？」
```

### 修復方案

**方案 A：前端即時查詢（不推薦）**
```javascript
// 前端查詢每個據點的工單狀態 - 效能差
for (const spot of spots) {
  const tickets = await axios.get(`/maintenance/by-spot/${spot.id}`)
  spot.hasActiveMaintenance = tickets.some(t => t.status === 'UNDER_MAINTENANCE')
}
```
❌ **缺點：** N+1 查詢問題，100 個據點 = 101 次 API 呼叫

**方案 B：後端一次性計算（推薦）** ✅
```java
// RentalSpotController.java - 改進版
@GetMapping("/list")
public ResponseEntity<?> getAllSpots() {
    List<RentalSpot> spots = rentalSpotService.findAll();
    List<SpotMapDto> dtos = spots.stream()
        .map(spot -> {
            SpotMapDto dto = SpotMapDto.fromEntity(spot);
            // ✅ 動態計算即時狀態
            dto.setHasActiveMaintenance(
                maintenanceService.hasActiveMachineRepair(spot.getSpotId())
            );
            return dto;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}
```

**方案 C：資料庫層級計算（最佳效能）** 🚀
```java
// SpotRepository.java - 使用 JOIN 一次查詢
@Query("""
    SELECT new com.example.dto.SpotMapDto(
        s.spotId, s.spotName, s.lat, s.lng,
        CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    )
    FROM RentalSpot s
    LEFT JOIN MaintenanceInformation m 
        ON m.spotId = s.spotId 
        AND m.issueStatus IN ('REPORTED', 'ASSIGNED', 'UNDER_MAINTENANCE')
    GROUP BY s.spotId, s.spotName, s.lat, s.lng
""")
List<SpotMapDto> findAllWithMaintenanceStatus();
```

### 建議實作順序
1. **立即（P1）：** 採用方案 B，快速修復邏輯錯誤
2. **優化（後續）：** 採用方案 C，提升查詢效能

---

## 🔴 P2: 前端地圖標記未依狀態變色（高優先級）

### 問題描述
EnterancePage.vue 接收到據點資料後，**未根據維修狀態改變地圖標記顏色**，所有標記都是預設藍色，無法視覺化區分可用/維修中的據點。

### 根本原因分析
**檔案路徑：** `frontend/src/views/EnterancePage.vue`

**程式碼問題：**
```vue
<!-- 當前實作 - 所有標記都是藍色 -->
<GMapMarker
  v-for="spot in spots"
  :key="spot.spotId"
  :position="{ lat: spot.lat, lng: spot.lng }"
  :title="spot.spotName"
  @click="selectSpot(spot)"
/>
<!-- ❌ 缺少 :icon 屬性來動態變更顏色 -->
```

### 影響範圍
1. **用戶體驗：** 無法一眼辨識哪些據點可用
2. **資訊傳達：** 地圖失去「即時監控」的價值
3. **操作效率：** 用戶需點擊每個標記才能知道狀態

### 視覺對比

**現狀：**
```
🔵 據點A (營運中)
🔵 據點B (維修中) ← 顏色相同，無法區分
🔵 據點C (營運中)
```

**期望：**
```
🟢 據點A (營運中)
🔴 據點B (維修中) ← 紅色警示
🟢 據點C (營運中)
```

### 修復方案

**完整實作代碼：**
```vue
<script setup>
const getMarkerIcon = (spot) => {
  // 根據即時狀態返回不同圖標
  if (spot.hasActiveMaintenance) {
    return {
      url: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',
      scaledSize: { width: 40, height: 40 }
    }
  }
  if (spot.spotStatus === '已關閉') {
    return {
      url: 'http://maps.google.com/mapfiles/ms/icons/grey-dot.png',
      scaledSize: { width: 40, height: 40 }
    }
  }
  return {
    url: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
    scaledSize: { width: 40, height: 40 }
  }
}
</script>

<template>
  <GMapMarker
    v-for="spot in spots"
    :key="spot.spotId"
    :position="{ lat: spot.lat, lng: spot.lng }"
    :title="spot.spotName"
    :icon="getMarkerIcon(spot)"
    @click="selectSpot(spot)"
  />
</template>
```

**圖標對應規則：**
| 狀態 | 顏色 | Google Maps 圖標 URL |
|------|------|----------------------|
| 營運中 + 無工單 | 🟢 綠色 | red-dot.png → green-dot.png |
| 有進行中工單 | 🔴 紅色 | red-dot.png |
| 已關閉 | ⚫ 灰色 | grey-dot.png |
| 暫停營運 | 🟡 黃色 | yellow-dot.png |

---

## 🔴 P3: 租借 API 未驗證資產維修狀態（高優先級）

### 問題描述
前端提交租借訂單時，後端 API **未檢查座位或機台是否正在維修中**，允許用戶租借維修中的資產，導致訂單建立後無法實際使用。

### 根本原因分析
**檔案路徑：**
- 後端：`backend/src/main/java/.../controller/RecRentController.java`
- Service：未引入 MaintenanceInformationService

**程式碼問題：**
```java
// RecRentController.java - 當前實作
@PostMapping("/create")
public ResponseEntity<?> createRental(@RequestBody RentalRequest request) {
    // ❌ 直接建立訂單，未檢查維修狀態
    RecRent rental = recRentService.create(request);
    return ResponseEntity.ok(rental);
}
```

### 影響範圍
1. **業務邏輯錯誤：** 維修中的資產仍可被租借
2. **用戶投訴：** 付款後發現無法使用
3. **退款成本：** 需手動處理退款和客訴

### 真實案例場景
```
時間軸：
10:00 - 座位 #A-101 報修（電源故障）
10:05 - 維修人員接單，狀態=UNDER_MAINTENANCE
10:10 - 用戶在前端選擇座位 A-101
10:12 - 後端 API 通過驗證（❌ 未檢查工單）
10:13 - 訂單建立成功，扣款完成
10:15 - 用戶到現場發現座位無法使用
10:20 - 客服介入，手動退款
```

### 資料完整性風險
```sql
-- 可能出現的不一致狀態
SELECT 
    s.seat_name,
    s.seat_status,        -- '啟用'
    m.issue_status,       -- 'UNDER_MAINTENANCE'
    r.rental_status       -- '進行中'  ← 邏輯衝突
FROM seats s
JOIN maintenance_information m ON m.target_id = s.seat_id
JOIN rec_rent r ON r.seat_id = s.seat_id
WHERE m.issue_status = 'UNDER_MAINTENANCE'
  AND r.rental_status = '進行中';
```

### 修復方案

**方案 A：前端驗證（輔助）**
```javascript
// RecRentAdd.vue - 前端預檢查
const checkAvailability = async (seatId) => {
  const res = await axios.get(`/maintenance/check-seat/${seatId}`)
  if (res.data.underMaintenance) {
    Swal.fire({
      icon: 'error',
      title: '座位維修中',
      text: '此座位目前無法租借，請選擇其他座位'
    })
    return false
  }
  return true
}
```
⚠️ **注意：** 前端驗證可繞過，必須搭配後端驗證

**方案 B：後端強制驗證（必須）** ✅
```java
// RecRentController.java - 改進版
@PostMapping("/create")
public ResponseEntity<?> createRental(@RequestBody RentalRequest request) {
    // ✅ 驗證座位可用性
    boolean underMaintenance = maintenanceService.isSeatUnderMaintenance(
        request.getSeatId()
    );
    
    if (underMaintenance) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of(
                "error", "SEAT_UNDER_MAINTENANCE",
                "message", "此座位目前維修中，無法租借",
                "seatId", request.getSeatId()
            )
        );
    }
    
    // 繼續正常流程
    RecRent rental = recRentService.create(request);
    return ResponseEntity.ok(rental);
}
```

**方案 C：資料庫層級約束（最佳）** 🚀
```sql
-- 使用資料庫觸發器防止不一致
CREATE TRIGGER prevent_rent_under_maintenance
BEFORE INSERT ON rec_rent
FOR EACH ROW
BEGIN
    DECLARE maintenance_count INT;
    
    SELECT COUNT(*) INTO maintenance_count
    FROM maintenance_information
    WHERE target_id = NEW.seat_id
      AND issue_status IN ('REPORTED', 'ASSIGNED', 'UNDER_MAINTENANCE')
      AND asset_type = 'SEAT';
    
    IF maintenance_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '座位維修中，無法建立租借訂單';
    END IF;
END;
```

### 建議實作順序
1. **立即（P1）：** 實作方案 B（後端驗證）
2. **強化（P2）：** 加入方案 A（前端預檢）
3. **長期（P3）：** 評估方案 C（資料庫約束）

---

## 🟡 P4: 機台工單未級聯更新所有座位（中優先級）

### 問題描述
當建立**機台級別的維修工單**時，該機台下的所有座位狀態未自動更新為「維修中」，導致座位可能仍顯示為「可用」。

### 根本原因分析
**檔案路徑：**
- Service：`backend/src/main/java/.../service/MaintenanceInformationService.java`
- 方法：`createTicket()` 缺少級聯邏輯

**程式碼問題：**
```java
// MaintenanceInformationService.java - 當前實作
public MaintenanceInformation createTicket(CreateTicketRequest request) {
    MaintenanceInformation ticket = new MaintenanceInformation();
    ticket.setAssetType(request.getAssetType());
    ticket.setTargetId(request.getTargetId());
    ticket.setIssueStatus("REPORTED");
    
    // ❌ 缺少：如果是機台工單，更新所有座位狀態
    
    return maintenanceRepository.save(ticket);
}
```

### 影響範圍
1. **資料不一致：** 機台維修中，但座位顯示可用
2. **查詢錯誤：** 前端查詢「可用座位」時包含維修中機台的座位
3. **統計偏差：** 資產健康度統計不準確

### 業務邏輯關聯
```
機台 (MACHINE) 工單建立
  ↓ (應該級聯)
該機台的所有座位 (SEAT)
  ↓ (目前缺少)
座位狀態更新為 '維修中'
```

### 修復方案

**完整實作：**
```java
// MaintenanceInformationService.java - 改進版
@Transactional
public MaintenanceInformation createTicket(CreateTicketRequest request) {
    MaintenanceInformation ticket = new MaintenanceInformation();
    ticket.setAssetType(request.getAssetType());
    ticket.setTargetId(request.getTargetId());
    ticket.setIssueStatus("REPORTED");
    
    MaintenanceInformation savedTicket = maintenanceRepository.save(ticket);
    
    // ✅ 如果是機台工單，級聯更新所有座位
    if ("MACHINE".equals(request.getAssetType())) {
        List<Seat> seats = seatRepository.findByMachineId(request.getTargetId());
        seats.forEach(seat -> {
            seat.setSeatStatus("維修中");
            seat.setUpdateTime(LocalDateTime.now());
        });
        seatRepository.saveAll(seats);
        
        log.info("機台工單 #{} 已級聯更新 {} 個座位狀態", 
                 savedTicket.getId(), seats.size());
    }
    
    return savedTicket;
}
```

**反向操作（工單完成時）：**
```java
// 工單解決時，恢復座位狀態
@Transactional
public void resolveTicket(Long ticketId, ResolveRequest request) {
    MaintenanceInformation ticket = findById(ticketId);
    ticket.setIssueStatus("RESOLVED");
    ticket.setResultType(request.getResultType());
    
    // ✅ 如果是機台工單且修復成功，恢復座位狀態
    if ("MACHINE".equals(ticket.getAssetType()) && 
        "REPAIRED".equals(request.getResultType())) {
        
        List<Seat> seats = seatRepository.findByMachineId(ticket.getTargetId());
        seats.forEach(seat -> {
            seat.setSeatStatus("啟用");
            seat.setUpdateTime(LocalDateTime.now());
        });
        seatRepository.saveAll(seats);
        
        log.info("機台工單 #{} 已恢復 {} 個座位狀態", ticketId, seats.size());
    }
    
    maintenanceRepository.save(ticket);
}
```

### 邊界情況處理
1. **部分座位已有獨立工單：** 不覆蓋，保持原狀態
2. **機台報廢：** 座位同步標記為「已報廢」
3. **機台更換：** 需手動處理座位重新分配

---

## 🟡 P5: 統計頁與列表頁狀態欄位混用（中優先級）

### 問題描述
資產統計 API 使用 `resultType`（REPAIRED/REPLACED/SCRAPPED），但工單列表顯示 `issueStatus`（REPORTED/ASSIGNED/UNDER_MAINTENANCE/RESOLVED），導致前端顯示不一致。

### 根本原因分析
**檔案路徑：**
- API：`/maintenance/asset-stats` 使用 `resultType`
- API：`/maintenance/list` 使用 `issueStatus`
- 前端：同時使用兩個欄位，容易混淆

**欄位定義差異：**
| 欄位 | 用途 | 可能值 | 時機 |
|------|------|--------|------|
| `issueStatus` | 工單流程狀態 | REPORTED, ASSIGNED, UNDER_MAINTENANCE, RESOLVED, CANCELLED | 整個生命週期 |
| `resultType` | 工單結果類型 | REPAIRED, REPLACED, SCRAPPED, null | 僅在 RESOLVED 時有值 |

### 資料不一致案例
```javascript
// 前端可能看到的資料
{
  issueStatus: 'RESOLVED',      // 流程狀態：已解決
  resultType: 'REPAIRED',       // 結果類型：已維修
  // ❓ 前端應該顯示哪一個？
}

{
  issueStatus: 'UNDER_MAINTENANCE',  // 流程狀態：維修中
  resultType: null,                  // 結果類型：無
  // ❓ 統計時如何歸類？
}
```

### 影響範圍
1. **前端混亂：** 不同頁面使用不同欄位，維護困難
2. **統計錯誤：** 可能重複計算或遺漏工單
3. **業務邏輯：** 條件判斷時容易出錯

### 修復方案

**方案 A：前端統一使用 `issueStatus`** ✅
```javascript
// 工單列表 - 統一使用 issueStatus
const getStatusText = (ticket) => {
  const statusMap = {
    'REPORTED': '已報修',
    'ASSIGNED': '已派工',
    'UNDER_MAINTENANCE': '維修中',
    'RESOLVED': '已解決',
    'CANCELLED': '已取消'
  }
  return statusMap[ticket.issueStatus] || '未知'
}

// 統計分類 - 根據 issueStatus 分組
const statsData = computed(() => {
  return {
    pending: tickets.value.filter(t => 
      ['REPORTED', 'ASSIGNED'].includes(t.issueStatus)
    ).length,
    inProgress: tickets.value.filter(t => 
      t.issueStatus === 'UNDER_MAINTENANCE'
    ).length,
    completed: tickets.value.filter(t => 
      t.issueStatus === 'RESOLVED'
    ).length
  }
})
```

**方案 B：後端提供統一欄位** 🚀
```java
// MaintenanceTicketResponseDto.java - 新增
public class MaintenanceTicketResponseDto {
    private Long id;
    private String issueStatus;      // 主要流程狀態
    private String resultType;       // 結果類型（可選）
    
    // ✅ 新增：統一的顯示文字
    private String displayStatus;
    
    public String getDisplayStatus() {
        if ("RESOLVED".equals(issueStatus) && resultType != null) {
            return switch (resultType) {
                case "REPAIRED" -> "已維修";
                case "REPLACED" -> "已更換";
                case "SCRAPPED" -> "已報廢";
                default -> "已解決";
            };
        }
        return switch (issueStatus) {
            case "REPORTED" -> "已報修";
            case "ASSIGNED" -> "已派工";
            case "UNDER_MAINTENANCE" -> "維修中";
            case "CANCELLED" -> "已取消";
            default -> "未知";
        };
    }
}
```

**方案 C：資料庫視圖整合** 📊
```sql
-- 建立視圖統一兩個欄位
CREATE VIEW v_maintenance_status AS
SELECT 
    id,
    issue_status,
    result_type,
    CASE 
        WHEN issue_status = 'RESOLVED' AND result_type IS NOT NULL 
            THEN CONCAT('已解決-', result_type)
        ELSE issue_status
    END AS unified_status
FROM maintenance_information;
```

### 建議實作順序
1. **短期（P1）：** 採用方案 A，前端統一使用 `issueStatus`
2. **中期（P2）：** 採用方案 B，後端提供 `displayStatus`
3. **長期（P3）：** 考慮方案 C，資料庫層級整合

---

## 📋 修復優先級與時程建議

### 立即處理（本週內）
| 問題 | 預估工時 | 風險等級 | 建議負責人 |
|------|----------|----------|-----------|
| P1: 地圖 API 動態計算 | 4 小時 | 高 | 後端工程師 |
| P2: 地圖標記變色 | 2 小時 | 中 | 前端工程師 |
| P3: 租借驗證 | 6 小時 | 高 | 後端工程師 |

### 短期處理（下週內）
| 問題 | 預估工時 | 風險等級 | 建議負責人 |
|------|----------|----------|-----------|
| P4: 機台級聯更新 | 8 小時 | 中 | 後端工程師 |
| P5: 狀態欄位統一 | 4 小時 | 低 | 全端協作 |

---

## 🧪 測試計劃

### P1-P3 整合測試情境
```gherkin
Feature: 維修狀態即時同步

Scenario: 機台報修後地圖立即顯示紅色標記
  Given 據點A的機台正常運作
  When 管理員建立機台維修工單
  And 刷新地圖頁面
  Then 據點A的標記應顯示為紅色
  And 點擊標記應顯示「維修中」提示

Scenario: 維修中的座位無法被租借
  Given 座位 A-101 有進行中的維修工單
  When 用戶嘗試租借座位 A-101
  Then API 應返回 409 錯誤
  And 錯誤訊息為「此座位目前維修中，無法租借」

Scenario: 機台工單完成後座位自動恢復
  Given 機台 M-001 有維修工單
  And 該機台下有 10 個座位狀態為「維修中」
  When 工單標記為「已解決-已維修」
  Then 所有 10 個座位狀態應更新為「啟用」
```

---

## 📊 修復後預期效益

### 業務價值
- **減少客訴：** 預估減少 80% 因資訊不一致的投訴
- **提升用戶滿意度：** 地圖資訊準確，避免白跑
- **降低營運成本：** 減少手動退款和客服處理

### 技術指標
- **資料一致性：** 地圖、工單、租借三系統狀態同步
- **API 效能：** 地圖載入時間預估增加 <100ms（可接受）
- **程式碼品質：** 減少 if-else 判斷邏輯，提升可維護性

---

## 🔄 Phase 3 準備事項

### 需要的資源
1. **開發環境：** 確保本地資料庫有測試資料
2. **測試資料：** 準備多種狀態的工單和座位
3. **程式碼審查：** 修改前備份關鍵檔案

### 風險評估
| 風險 | 機率 | 影響 | 應對措施 |
|------|------|------|----------|
| 效能下降 | 中 | 中 | 加入快取機制 |
| 既有功能損壞 | 低 | 高 | 完整回歸測試 |
| 資料遷移問題 | 低 | 中 | 先在測試環境驗證 |

---

**報告產出時間：** 2026-01-22  
**下一步行動：** 等待批准後進入 Phase 3 實作階段  
**預估 Phase 3 總工時：** 24 小時（3 個工作天）
