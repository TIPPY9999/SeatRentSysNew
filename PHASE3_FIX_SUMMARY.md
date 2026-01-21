# Phase 3 實施摘要

## ✅ 已完成修復 (4/5)

### P1: 地圖 API 動態維護狀態計算 ✅
**修改文件：**
- `backend/.../dto/spot/SpotMapDto.java` (新增)
- `backend/.../controller/spot/RentalSpotController.java`
- `backend/.../service/maintenance/MaintenanceInformationService.java`

**實施內容：**
1. 創建 `SpotMapDto` 包含 `hasActiveMaintenance` 欄位
2. `/api/spot/list` 端點返回實時維護狀態
3. 新增 `hasActiveMachineRepair(spotId)` 方法查詢機台級工單

**驗證方式：**
```bash
# 測試 API
curl http://localhost:8080/api/spot/list

# 預期返回
[{
  "spotId": 1,
  "spotName": "機台A",
  "latitude": 25.033,
  "longitude": 121.565,
  "hasActiveMaintenance": true  // ✅ 新增欄位
}]
```

---

### P2: 地圖標記點顏色分級 ✅
**修改文件：**
- `frontend/src/views/EnterancePage.vue`

**實施內容：**
1. 新增 `getMarkerIcon()` 函數動態選擇標記圖示
2. 顏色對應規則：
   - 🔴 紅色：維修中 (`hasActiveMaintenance === true`)
   - 🟢 綠色：營運中 (`spotStatus === '營運中'`)
   - 🟡 黃色：暫停營運 (`spotStatus === '暫停營運'`)
   - ⚫ 灰色：已關閉 (`spotStatus === '已關閉'`)

**驗證方式：**
```javascript
// 在 EnterancePage.vue 中
const testMarkerColor = (spot) => {
  console.log(`機台 ${spot.spotName}:`, getMarkerIcon(spot))
}
```

---

### P3: 租借 API 維護中座位驗證 ✅
**修改文件：**
- `backend/.../controller/rec/RecRentController.java`
- `backend/.../service/maintenance/MaintenanceInformationService.java`

**實施內容：**
1. 新增 `isSeatUnderMaintenance(seatsId)` 方法
2. 在 `create()` 方法中添加驗證邏輯
3. 返回 `409 CONFLICT` 狀態碼阻止訂單建立

**驗證方式：**
```bash
# 1. 創建座位維修工單
POST /api/maintenance/tickets
{
  "seatsId": 101,
  "issueType": "硬體故障",
  "issueStatus": "UNDER_MAINTENANCE"
}

# 2. 嘗試租借該座位
POST /api/rec/rent/create
{
  "seatId": 101,
  "userId": 1,
  ...
}

# 預期返回
HTTP 409 Conflict
{
  "error": "該座位目前正在維護中，無法租借"
}
```

---

### P4: 機台工單級聯更新所有座位 ✅
**修改文件：**
- `backend/.../service/maintenance/MaintenanceInformationService.java`

**實施內容：**
1. 新增 `cascadeSpotMaintenanceToSeats(spotId, setMaintenance)` 方法
2. 在 `createTicket()` 中檢測機台級工單並觸發級聯
3. 在 `resolveTicket()` 中恢復所有座位狀態

**核心邏輯：**
```java
// 創建工單時級聯
if (saved.getSpotId() != null && saved.getSeatsId() == null) {
    cascadeSpotMaintenanceToSeats(saved.getSpotId(), true);
}

// 結案時恢復
if (mtif.getSeatsId() == null && "FIXED".equals(resultType)) {
    cascadeSpotMaintenanceToSeats(mtif.getSpotId(), false);
}

// 級聯方法
private void cascadeSpotMaintenanceToSeats(Integer spotId, boolean setMaintenance) {
    List<Seat> seats = seatRepo.findBySpotId(spotId);
    String targetStatus = setMaintenance ? "維修中" : "啟用";
    
    for (Seat seat : seats) {
        if (!targetStatus.equals(seat.getSeatsStatus())) {
            seat.setSeatsStatus(targetStatus);
            seatRepo.save(seat);
        }
    }
}
```

**驗證方式：**
```sql
-- 1. 創建機台維修工單（spotId=1, seatsId=null）
-- 2. 檢查該機台所有座位狀態
SELECT s.seat_name, s.seats_status 
FROM seats s 
WHERE s.spot_id = 1;

-- 預期結果：所有座位 seats_status = '維修中'

-- 3. 結案該工單 (resultType='FIXED')
-- 4. 再次查詢座位狀態

-- 預期結果：所有座位 seats_status = '啟用'
```

**關鍵驗證場景：**
```
場景 1: 機台報修
┌─────────────────────────────────────────┐
│ 1. POST /api/maintenance/tickets       │
│    { spotId: 1, seatsId: null }        │
│                                         │
│ 2. 觸發級聯：                           │
│    seat #A-101: 啟用 → 維修中          │
│    seat #A-102: 啟用 → 維修中          │
│    seat #A-103: 啟用 → 維修中          │
└─────────────────────────────────────────┘

場景 2: 維修完成
┌─────────────────────────────────────────┐
│ 1. POST /api/maintenance/tickets/1/resolve│
│    { resultType: 'FIXED' }             │
│                                         │
│ 2. 觸發級聯恢復：                       │
│    seat #A-101: 維修中 → 啟用          │
│    seat #A-102: 維修中 → 啟用          │
│    seat #A-103: 維修中 → 啟用          │
└─────────────────────────────────────────┘
```

---

## 🟡 待處理 (1/5)

### P5: 統一狀態欄位使用 (issueStatus vs resultType)

**問題描述：**
前端混用 `issueStatus` (工單流程狀態) 和 `resultType` (結案結果)，導致顯示不一致。

**影響範圍：**
- `frontend/src/views/maintenance/MtifList.vue`
- `frontend/src/views/maintenance/ScheduleList.vue`
- `frontend/src/components/maintenance/TicketCharts.vue`

**預估工時：** 4 小時

**建議方案：**
1. 後端新增 `displayStatus` 計算欄位
2. 前端統一使用 `displayStatus` 或 `issueStatus`
3. 保留 `resultType` 僅用於結案邏輯

**實施優先級：** 中（不影響核心功能，但影響用戶體驗）

---

## 📊 修復統計

| 優先級 | 問題數 | 已完成 | 待處理 | 完成率 |
|--------|--------|--------|--------|--------|
| 高     | 3      | 3      | 0      | 100%   |
| 中     | 2      | 1      | 1      | 50%    |
| **總計** | **5** | **4** | **1** | **80%** |

---

## 🎯 後續建議

### 立即執行 (本週)
- ✅ ~~P1-P4 高優先級修復~~ (已完成)
- ⏳ P5 前端顯示統一化 (預估 4 小時)

### 短期優化 (2 週內)
1. **前端表單驗證增強：**
   - 租借表單添加實時座位狀態檢查
   - 工單表單添加重複工單提示

2. **後端日誌完善：**
   - 記錄級聯更新操作日誌
   - 添加維護狀態變更審計追蹤

3. **測試覆蓋：**
   - 單元測試：`cascadeSpotMaintenanceToSeats()`
   - 整合測試：機台維修完整流程
   - E2E 測試：前端地圖標記更新

### 長期規劃 (1 個月內)
1. **效能優化：**
   - 批次更新座位狀態 (使用 JPA batch update)
   - 添加 Redis 快取機台維護狀態

2. **業務功能擴展：**
   - 支援部分座位維修 (機台部分可用)
   - 預約維護時段通知
   - 維護歷史分析報表

---

## 🧪 測試檢查清單

### P1-P3 整合測試
```bash
# 1. 啟動後端
cd backend
mvn spring-boot:run

# 2. 啟動前端
cd frontend
npm run dev

# 3. 測試流程
# 3.1 創建機台維修工單
# 3.2 檢查地圖標記變紅
# 3.3 嘗試租借該機台下的座位 (應被阻止)
# 3.4 結案工單
# 3.5 檢查地圖標記恢復綠色
# 3.6 確認座位可正常租借
```

### P4 級聯測試
```sql
-- 測試資料準備
INSERT INTO rental_spot (spot_name, spot_status) VALUES ('測試機台', '營運中');
INSERT INTO seats (spot_id, seat_name, seats_status) VALUES 
  (LAST_INSERT_ID(), 'A-101', '啟用'),
  (LAST_INSERT_ID(), 'A-102', '啟用'),
  (LAST_INSERT_ID(), 'A-103', '啟用');

-- 創建機台工單並觀察座位狀態變化
-- ...
```

---

## 📝 變更記錄

| 日期 | 階段 | 內容 | 負責人 |
|------|------|------|--------|
| 2026-01-22 | Phase 1 | UI/UX 美化 | AI Agent |
| 2026-01-22 | Phase 2 | 邏輯審計報告 | AI Agent |
| 2026-01-22 | Phase 3 | P1-P4 修復實施 | AI Agent |

---

## ⚙️ 編譯與部署

### 後端編譯檢查
```bash
cd backend
mvn clean compile

# 檢查錯誤
mvn test-compile
```

### 前端打包
```bash
cd frontend
npm run build

# 預覽生產版本
npm run preview
```

### 資料庫遷移 (如需要)
```sql
-- 無需 schema 變更，僅邏輯修改
-- 建議執行資料一致性檢查：

SELECT 
    COUNT(*) AS inconsistent_count
FROM maintenance_information m
JOIN seats s ON m.seats_id = s.seat_id
WHERE m.issue_status IN ('REPORTED', 'ASSIGNED', 'UNDER_MAINTENANCE')
  AND s.seats_status != '維修中';

-- 預期結果：inconsistent_count = 0
```
