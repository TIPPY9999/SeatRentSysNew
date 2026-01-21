-- ================================================================
-- P4 級聯更新驗證腳本
-- 用途：測試機台維修工單對所有座位的級聯影響
-- ================================================================

-- 第一部分：準備測試資料
-- ================================================================

-- 1. 查看現有測試機台
SELECT 
    spot_id,
    spot_name,
    spot_status,
    (SELECT COUNT(*) FROM seats WHERE spot_id = rs.spot_id) AS seat_count
FROM rental_spot rs
WHERE spot_name LIKE '%測試%' OR spot_name LIKE '%test%';

-- 2. 如果沒有測試機台，創建一個（可選）
-- INSERT INTO rental_spot (spot_name, spot_status, latitude, longitude, address) 
-- VALUES ('測試機台-P4驗證', '營運中', 25.0330, 121.5654, '測試地址');

-- 3. 為測試機台添加座位（假設 spot_id = 1）
-- INSERT INTO seats (spot_id, seat_name, seats_status) VALUES 
--   (1, 'TEST-A-101', '啟用'),
--   (1, 'TEST-A-102', '啟用'),
--   (1, 'TEST-A-103', '啟用');


-- 第二部分：驗證初始狀態
-- ================================================================

-- 4. 檢查機台和座位的初始狀態
SELECT 
    rs.spot_id,
    rs.spot_name,
    rs.spot_status,
    s.seat_id,
    s.seat_name,
    s.seats_status
FROM rental_spot rs
LEFT JOIN seats s ON s.spot_id = rs.spot_id
WHERE rs.spot_id = 1  -- 替換為實際測試的 spot_id
ORDER BY s.seat_name;

-- 預期結果：
-- | spot_id | spot_name | spot_status | seat_id | seat_name | seats_status |
-- |---------|-----------|-------------|---------|-----------|--------------|
-- | 1       | 測試機台  | 營運中      | 101     | A-101     | 啟用         |
-- | 1       | 測試機台  | 營運中      | 102     | A-102     | 啟用         |
-- | 1       | 測試機台  | 營運中      | 103     | A-103     | 啟用         |


-- 第三部分：測試級聯開啟（創建機台工單）
-- ================================================================

-- 5. ⚠️ 使用 Postman 或前端執行以下 API 請求：
-- POST http://localhost:8080/api/maintenance/tickets
-- Content-Type: application/json
-- 
-- {
--   "spotId": 1,
--   "seatsId": null,
--   "issueType": "硬體故障",
--   "issueDescription": "【P4驗證】測試機台級聯功能",
--   "priority": "HIGH"
-- }

-- 6. 創建工單後，立即檢查座位狀態
SELECT 
    s.seat_id,
    s.seat_name,
    s.seats_status,
    s.updated_at AS last_updated
FROM seats s
WHERE s.spot_id = 1
ORDER BY s.seat_name;

-- ✅ 預期結果：所有座位 seats_status 應該變為 '維修中'
-- | seat_id | seat_name | seats_status | last_updated         |
-- |---------|-----------|--------------|----------------------|
-- | 101     | A-101     | 維修中       | 2026-01-22 14:35:00 |
-- | 102     | A-102     | 維修中       | 2026-01-22 14:35:00 |
-- | 103     | A-103     | 維修中       | 2026-01-22 14:35:00 |

-- 7. 檢查工單是否正確創建
SELECT 
    ticket_id,
    spot_id,
    seats_id,
    issue_type,
    issue_status,
    priority,
    reported_at
FROM maintenance_information
WHERE spot_id = 1 
  AND seats_id IS NULL
ORDER BY ticket_id DESC
LIMIT 1;

-- 預期結果：
-- | ticket_id | spot_id | seats_id | issue_type | issue_status | priority |
-- |-----------|---------|----------|------------|--------------|----------|
-- | 123       | 1       | NULL     | 硬體故障   | REPORTED     | HIGH     |


-- 第四部分：測試級聯恢復（結案工單）
-- ================================================================

-- 8. 先指派工單並開始維修（可選）
-- POST http://localhost:8080/api/maintenance/tickets/123/assign
-- { "staffId": 1 }

-- POST http://localhost:8080/api/maintenance/tickets/123/start

-- 9. ⚠️ 執行結案 API：
-- POST http://localhost:8080/api/maintenance/tickets/123/resolve
-- Content-Type: application/json
--
-- {
--   "resultType": "FIXED",
--   "resolveNote": "【P4驗證】測試級聯恢復功能"
-- }

-- 10. 結案後，立即檢查座位狀態恢復
SELECT 
    s.seat_id,
    s.seat_name,
    s.seats_status,
    s.updated_at AS last_updated
FROM seats s
WHERE s.spot_id = 1
ORDER BY s.seat_name;

-- ✅ 預期結果：所有座位 seats_status 應該恢復為 '啟用'
-- | seat_id | seat_name | seats_status | last_updated         |
-- |---------|-----------|--------------|----------------------|
-- | 101     | A-101     | 啟用         | 2026-01-22 14:40:00 |
-- | 102     | A-102     | 啟用         | 2026-01-22 14:40:00 |
-- | 103     | A-103     | 啟用         | 2026-01-22 14:40:00 |

-- 11. 檢查工單是否正確結案
SELECT 
    ticket_id,
    issue_status,
    result_type,
    resolved_at,
    resolve_note
FROM maintenance_information
WHERE ticket_id = 123;  -- 替換為實際的 ticket_id

-- 預期結果：
-- | ticket_id | issue_status | result_type | resolved_at         |
-- |-----------|--------------|-------------|---------------------|
-- | 123       | RESOLVED     | FIXED       | 2026-01-22 14:40:00 |


-- 第五部分：邊界條件測試
-- ================================================================

-- 12. 測試：座位級工單不應觸發級聯
-- POST http://localhost:8080/api/maintenance/tickets
-- {
--   "spotId": 1,
--   "seatsId": 101,  ← 指定單一座位
--   "issueType": "清潔維護"
-- }

-- 驗證：只有 seat_id=101 狀態變更，其他座位不受影響
SELECT s.seat_name, s.seats_status 
FROM seats s 
WHERE s.spot_id = 1;

-- 預期結果：
-- | seat_name | seats_status |
-- |-----------|--------------|
-- | A-101     | 維修中       | ← 只有這個變更
-- | A-102     | 啟用         |
-- | A-103     | 啟用         |


-- 13. 測試：多個機台工單的影響
-- 創建第二個工單（不結案第一個）
-- 驗證：座位應保持 '維修中' 狀態

-- 結案第一個工單
-- 驗證：座位仍應保持 '維修中'（因為還有第二個工單）

-- 結案第二個工單
-- 驗證：座位恢復為 '啟用'


-- 第六部分：清理測試資料（可選）
-- ================================================================

-- 14. 刪除測試工單
-- DELETE FROM maintenance_log WHERE ticket_id IN (SELECT ticket_id FROM maintenance_information WHERE spot_id = 1 AND issue_description LIKE '%P4驗證%');
-- DELETE FROM maintenance_information WHERE spot_id = 1 AND issue_description LIKE '%P4驗證%';

-- 15. 恢復座位狀態（如果需要）
-- UPDATE seats SET seats_status = '啟用' WHERE spot_id = 1;

-- 16. 刪除測試機台和座位（如果是專門創建的）
-- DELETE FROM seats WHERE spot_id IN (SELECT spot_id FROM rental_spot WHERE spot_name LIKE '%P4驗證%');
-- DELETE FROM rental_spot WHERE spot_name LIKE '%P4驗證%';


-- ================================================================
-- 驗證檢查清單
-- ================================================================

-- ✅ 級聯開啟測試
-- [ ] 創建機台工單後，所有座位狀態變為 '維修中'
-- [ ] 後端 console 印出級聯更新日誌
-- [ ] 前端地圖標記變為紅色

-- ✅ 級聯恢復測試
-- [ ] 結案工單後，所有座位狀態恢復為 '啟用'
-- [ ] 後端 console 印出級聯恢復日誌
-- [ ] 前端地圖標記恢復為綠色

-- ✅ 邊界條件測試
-- [ ] 座位級工單不觸發級聯
-- [ ] 多個機台工單時，須全部結案才恢復
-- [ ] 結案為 NOT_FIXED 時不恢復（需手動處理）

-- ✅ 資料一致性
-- [ ] 無孤立的維修中座位（沒有對應工單）
-- [ ] 無遺漏的座位狀態更新
-- [ ] 更新時間戳正確記錄

-- ================================================================
-- 常見問題排查
-- ================================================================

-- Q1: 座位狀態沒有變更？
-- A1: 檢查 cascadeSpotMaintenanceToSeats() 是否被正確調用
--     查看後端日誌是否有 "✅ P4 級聯更新" 訊息

-- Q2: 結案後座位沒有恢復？
-- A2: 檢查 resultType 是否為 'FIXED'
--     確認 resolveTicket() 中的級聯恢復邏輯執行

-- Q3: 部分座位狀態不一致？
-- A3: 可能有並發問題或事務未提交
--     執行一致性檢查 SQL：
SELECT 
    COUNT(*) AS inconsistent_seats
FROM seats s
LEFT JOIN (
    SELECT DISTINCT m.spot_id
    FROM maintenance_information m
    WHERE m.seats_id IS NULL
      AND m.issue_status IN ('REPORTED', 'ASSIGNED', 'UNDER_MAINTENANCE')
) active_tickets ON active_tickets.spot_id = s.spot_id
WHERE (active_tickets.spot_id IS NOT NULL AND s.seats_status != '維修中')
   OR (active_tickets.spot_id IS NULL AND s.seats_status = '維修中');

-- 預期結果：inconsistent_seats = 0

-- ================================================================
-- 效能監控（大量座位場景）
-- ================================================================

-- 檢查更新效率（如果機台有 100+ 座位）
SET STATISTICS TIME ON;

-- 執行級聯更新並觀察耗時
-- POST /api/maintenance/tickets (機台工單)

-- 查看執行計畫
EXPLAIN SELECT * FROM seats WHERE spot_id = 1;

-- 建議：spot_id 應該有索引
-- CREATE INDEX idx_seats_spot_id ON seats(spot_id);
