--重置資料庫 
--Version Log: 2026/01/21
--===========CLEAR=================
USE SeatRentSys
DROP TABLE recRent;
DROP TABLE maintenanceLog;
DROP TABLE maintenanceInformation;
DROP TABLE maintenanceSchedule;
DROP TABLE maintenanceStaff;
DROP TABLE seats;
DROP TABLE renting_Spot;
DROP TABLE redemption_log;
DROP TABLE discount;
DROP TABLE merchant;
DROP TABLE member;
DROP TABLE admin;
DROP VIEW V_RentDetails
--============BUILD TABLE==============
--===========奕穎  ver 2026/1/21 ==============
/** =========================================================
   1) 建立 member & admin
   ========================================================= */
CREATE TABLE member
(
    memId INT IDENTITY(1,1) PRIMARY KEY,
    --會員ID
    memUsername VARCHAR(50) NOT NULL UNIQUE,
    --帳號
    memPassword VARCHAR(100) NOT NULL,
    --密碼
    memName NVARCHAR(50) NOT NULL,
    --姓名
    memEmail VARCHAR(50) NOT NULL UNIQUE,
    --信箱
    memPhone VARCHAR(20) NOT NULL,
    --手機號碼
    memStatus INT NOT NULL DEFAULT 1,
    --會員狀態
    createdAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    --建立時間
    updatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    --更新時間
    memPoints INT NOT NULL DEFAULT 0,
    --點數累計
    memViolation INT NOT NULL DEFAULT 0,
    --違規紀錄
    memLevel INT NOT NULL DEFAULT 1,
    --會員等級
    memInvoice VARCHAR(20) NULL
    --發票載具
);

CREATE TABLE admin
(
    admId INT IDENTITY(1,1) PRIMARY KEY,
    --管理員ID
    admUsername VARCHAR(50) NOT NULL UNIQUE,
    --帳號
    admPassword VARCHAR(100) NOT NULL,
    --密碼
    admName NVARCHAR(50) NOT NULL,
    --姓名
    admEmail VARCHAR(50) NOT NULL,
    --信箱
    admRole INT NOT NULL DEFAULT 1,
    --管理員權限
    createdAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    --建立時間
    updatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    --更新時間
    admStatus TINYINT NOT NULL DEFAULT 1
    --管理員狀態
);

--========光宇 Table ver.20240121  14:52========
/* =========================================================
   1) 建立 renting_Spot
   ========================================================= */
CREATE TABLE dbo.renting_Spot
(
    spotId INT IDENTITY(1,1),
    spotCode VARCHAR(30) NOT NULL,
    spotName NVARCHAR(100) NOT NULL,
    spotAddress NVARCHAR(200) NULL,
    spotStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_renting_Spot_spotStatus DEFAULT (N'營運中'),
    merchantId INT NULL,
    createdAt DATETIME2(7) NOT NULL
        CONSTRAINT DF_renting_Spot_createdAt DEFAULT (SYSDATETIME()),
    updatedAt DATETIME2(7) NOT NULL
        CONSTRAINT DF_renting_Spot_updatedAt DEFAULT (SYSDATETIME()),
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    spotDescription NVARCHAR(500) NULL,
    spotImage VARCHAR(255) NULL,
    CONSTRAINT PK_renting_Spot PRIMARY KEY CLUSTERED (spotId),
    CONSTRAINT CK_renting_Spot_spotStatus  CHECK (spotStatus IN (N'營運中', N'停用', N'維修中'))
);
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_renting_Spot_spotCode ON dbo.renting_Spot (spotCode);
GO

CREATE TABLE dbo.seats
(
    seatsId INT IDENTITY(1,1),
    seatsName NVARCHAR(100) NOT NULL,
    seatsType NVARCHAR(50) NOT NULL,
    seatsStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_seats_seatsStatus DEFAULT (N'啟用'),
    spotId INT NULL,
    updatedAt DATETIME2(7) NOT NULL
        CONSTRAINT DF_seats_updatedAt DEFAULT (SYSDATETIME()),
    serialNumber VARCHAR(50) NULL,
    createdAt DATETIME2(7) NOT NULL
        CONSTRAINT DF_seats_createdAt DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_seats PRIMARY KEY CLUSTERED (seatsId),
    CONSTRAINT FK_seats_spot
        FOREIGN KEY (spotId)
        REFERENCES dbo.renting_Spot (spotId),
    CONSTRAINT CK_seats_seatsStatus
        CHECK (seatsStatus IN (N'啟用', N'停用', N'維修中'))
);
GO

/* 一般索引：加速以 spotId 查 seats */
CREATE NONCLUSTERED INDEX IX_seats_spotId
ON dbo.seats (spotId);
GO

/* serialNumber 唯一索引：Filtered Unique Index，避免 NULL 互相衝突 */
CREATE UNIQUE NONCLUSTERED INDEX UQ_seats_spot_serialNumber
ON dbo.seats (serialNumber)
WHERE serialNumber IS NOT NULL;
GO

/* =========================================================
   3) updatedAt 自動刷新 Trigger（AFTER UPDATE）
   ========================================================= */
CREATE TRIGGER dbo.trg_renting_Spot_updatedAt
ON dbo.renting_Spot
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE s
    SET updatedAt = SYSDATETIME()
    FROM dbo.renting_Spot s
        INNER JOIN inserted i ON s.spotId = i.spotId;
END;
GO

CREATE TRIGGER dbo.trg_seats_updatedAt
ON dbo.seats
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE s
    SET updatedAt = SYSDATETIME()
    FROM dbo.seats s
        INNER JOIN inserted i ON s.seatsId = i.seatsId;
END;
GO
--==============光宇 TABLE END==============

--==============翌帆 TABLE ver.20260121==============
USE [SeatRentSys];
GO

CREATE TABLE [dbo].[maintenanceStaff]
(
    [staffId] INT IDENTITY (1, 1) NOT NULL,
    [staffName] NVARCHAR (50) NOT NULL,
    [staffCompany] NVARCHAR (100) NULL,
    [staffPhone] VARCHAR (20) NULL,
    [staffEmail] VARCHAR (100) NULL,
    [staffNote] NVARCHAR (200) NULL,
    [createdAt] DATETIME2 (7) DEFAULT (sysdatetime()) NOT NULL,
    [isActive] BIT CONSTRAINT [DF_maintenanceStaff_isActive] DEFAULT ((1)) NOT NULL,
    PRIMARY KEY CLUSTERED ([staffId] ASC)
);
--===================================================
USE [SeatRentSys];
GO

CREATE TABLE [dbo].[maintenanceInformation]
(
    [ticketId] INT IDENTITY (1, 1) NOT NULL,
    [spotId] INT NULL,
    [issueType] NVARCHAR (200) NOT NULL,
    [issueDesc] NVARCHAR (500) NULL,
    [issuePriority] VARCHAR (100) DEFAULT ('NORMAL') NOT NULL,
    [issueStatus] VARCHAR (50) DEFAULT ('REPORTED') NOT NULL,
    [assignedStaffId] INT NULL,
    [reportedAt] DATETIME2 (7) DEFAULT (sysdatetime()) NOT NULL,
    [startAt] DATETIME2 (7) NULL,
    [resolvedAt] DATETIME2 (7) NULL,
    [resolveNote] NVARCHAR (500) NULL,
    [resultType] NVARCHAR (50) NULL,
    [seatsId] INT NULL,
    PRIMARY KEY CLUSTERED ([ticketId] ASC),
    CONSTRAINT [FK_maintenanceInformation_seats] FOREIGN KEY ([seatsId]) REFERENCES [dbo].[seats] ([seatsId]),
    CONSTRAINT [fkMaintAssignedStaffId] FOREIGN KEY ([assignedStaffId]) REFERENCES [dbo].[maintenanceStaff] ([staffId])
);
--=================================
USE [SeatRentSys];
GO
-- 建立排程表
CREATE TABLE [dbo].[maintenanceSchedule]
(
    [scheduleId] INT IDENTITY (1, 1) NOT NULL,
    [title] NVARCHAR (100) NOT NULL,
    -- 任務名稱    -- 目標設定 (Polymorphic Association)
    [targetType] VARCHAR (20) NOT NULL,
    -- 'SPOT' 或 'SEAT'
    [targetId] INT NOT NULL,
    -- 對應 renting_Spot.spotId 或 seats.seatsId
    -- 頻率設定
    [scheduleType] VARCHAR (20) NOT NULL,
    -- 'DAILY', 'WEEKLY', 'MONTHLY'
    [dayOfWeek] INT NULL,
    -- 1-7 (週排程用)
    [dayOfMonth] INT NULL,
    -- 1-31 (月排程用)
    [executeTime] TIME (7) NOT NULL,
    -- 預計執行時間
    -- 工單內容預設值
    [issueType] NVARCHAR (200) NOT NULL,
    [issuePriority] VARCHAR (50) DEFAULT ('NORMAL') NOT NULL,
    [assignedStaffId] INT NULL,
    -- 預設指派的虛擬廠商ID
    -- 排程控制
    [isActive] BIT DEFAULT ((1)) NOT NULL,
    [lastExecutedAt] DATETIME2 (7) NULL,
    [nextExecuteAt] DATETIME2 (7) NOT NULL,
    -- 系統自動計算的下次執行時間
    [createdAt] DATETIME2 (7) DEFAULT (sysdatetime()) NOT NULL,
    [updatedAt] DATETIME2 (7) DEFAULT (sysdatetime()) NOT NULL,
    PRIMARY KEY CLUSTERED ([scheduleId] ASC)
);
GO
-- =============================================
-- 以下是AI建議的強力約束 (Constraints)
-- =============================================

-- 1. 限制 targetType 只能是 SPOT 或 SEAT
ALTER TABLE [dbo].[maintenanceSchedule]
ADD CONSTRAINT [CK_schedule_targetType] 
CHECK ([targetType] IN ('SPOT', 'SEAT'));
-- 2. 限制 scheduleType 只能是三種頻率之一
ALTER TABLE [dbo].[maintenanceSchedule]
ADD CONSTRAINT [CK_schedule_scheduleType] 
CHECK ([scheduleType] IN ('DAILY', 'WEEKLY', 'MONTHLY'));
-- 3. 限制 issuePriority 只能是定義好的優先級
ALTER TABLE [dbo].[maintenanceSchedule]
ADD CONSTRAINT [CK_schedule_priority] 
CHECK ([issuePriority] IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'));
-- 4. 邏輯檢查：確保頻率與參數一致 (防呆)
-- DAILY: 不需要 dayOfWeek 和 dayOfMonth
-- WEEKLY: 必須有 dayOfWeek (1-7)
-- MONTHLY: 必須有 dayOfMonth (1-31)
ALTER TABLE [dbo].[maintenanceSchedule]
ADD CONSTRAINT [CK_schedule_rule_logic]
CHECK (
    ([scheduleType]='DAILY'   AND [dayOfWeek] IS NULL AND [dayOfMonth] IS NULL)
 OR ([scheduleType]='WEEKLY'  AND [dayOfWeek] BETWEEN 1 AND 7 AND [dayOfMonth] IS NULL)
 OR ([scheduleType]='MONTHLY' AND [dayOfMonth] BETWEEN 1 AND 31 AND [dayOfWeek] IS NULL)
);

-- 5. 建立 Foreign Key (只針對 assignedStaffId，因為 targetId 是動態的無法設 FK)
ALTER TABLE [dbo].[maintenanceSchedule]
ADD CONSTRAINT [FK_schedule_staff]
FOREIGN KEY ([assignedStaffId]) REFERENCES [dbo].[maintenanceStaff] ([staffId]);

-- 6. 建立索引 (Index) - 讓排程掃描速度飛快
CREATE NONCLUSTERED INDEX [IX_schedule_due_check]
ON [dbo].[maintenanceSchedule] ([isActive], [nextExecuteAt])
INCLUDE ([scheduleId]); -- 包含 ID 以加速查詢
GO

-- =============================================
-- 表名：maintenanceLog (維修歷程記錄表)
-- =============================================
USE [SeatRentSys];
GO
-- 1. 建立資料表 (如果表不存在才建立)
IF OBJECT_ID(N'dbo.maintenanceLog', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[maintenanceLog]
    (
        [logId] INT IDENTITY (1, 1) NOT NULL,
        -- 流水號 PK
        [ticketId] INT NOT NULL,
        -- 關聯原本的工單 ID
        [operator] NVARCHAR (50) NOT NULL,
        -- 操作者 (支援中文)
        [action] VARCHAR (50) NOT NULL,
        -- 動作代號 (英文)
        [comment] NVARCHAR (500) NULL,
        -- 詳細說明
        [createdAt] DATETIME2 (7) DEFAULT (sysdatetime()) NOT NULL,
        -- 發生時間 (預設當下)
        -- 設定主鍵 (Primary Key)
        CONSTRAINT [PK_maintenanceLog] PRIMARY KEY CLUSTERED ([logId] ASC)
    );
END;
    -- 2. 建立外鍵關聯 (如果 FK 不存在才建立)
    GO
IF NOT EXISTS (SELECT 1
FROM sys.foreign_keys
WHERE name = N'FK_maintenanceLog_ticket')
BEGIN
    ALTER TABLE [dbo].[maintenanceLog]
    ADD CONSTRAINT [FK_maintenanceLog_ticket] 
    FOREIGN KEY ([ticketId]) 
    REFERENCES [dbo].[maintenanceInformation] ([ticketId])
    ON DELETE CASCADE;
    -- 工單刪除時，歷史紀錄一併刪除
    PRINT ' 外鍵 FK_maintenanceLog_ticket 建立成功';
END;

-- 3. 建立查詢索引 (針對 Timeline 優化)
-- 這會讓 "SELECT * FROM logs WHERE ticketId = ? ORDER BY createdAt DESC" 飛快
IF NOT EXISTS (SELECT 1
FROM sys.indexes
WHERE name = N'IX_maintenanceLog_ticket_createdAt')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_maintenanceLog_ticket_createdAt]
    ON [dbo].[maintenanceLog] ([ticketId] ASC, [createdAt] DESC);
    PRINT '效能索引 IX_maintenanceLog_ticket_createdAt 建立成功';
END;
--========================== 翊庭 TABLE ver.20260121===================
CREATE TABLE merchant
(
    merchantId INT IDENTITY(1,1) PRIMARY KEY,
    -- PK
    merchantName NVARCHAR(50) NOT NULL,
    -- 商家名稱
    merchantPhone NVARCHAR(20),
    -- 電話
    merchantEmail NVARCHAR(50) NULL,
    -- Email
    merchantAddress NVARCHAR(200) NOT NULL,
    -- 地址
    merchantStatus INT,
    -- 狀態
    createdTime DATETIME2 DEFAULT SYSDATETIME()
    -- 建立時間
);
CREATE TABLE discount
(
    couponId INT IDENTITY(1,1) PRIMARY KEY,
    -- PK 優惠券ID
    couponName NVARCHAR(500),
    --優惠券名稱 
    couponDescription NVARCHAR(1000),
    -- 優惠內容
    pointsRequired INT NOT NULL,
    -- 點數需求
    startDate DATE,
    -- 生效日期
    endDate DATE,
    -- 結束日期
    merchantId INT,
    -- FK 商家ID
    couponStatus INT NOT NULL,
    -- 上下架
    createdTime DATETIME2 DEFAULT SYSDATETIME(),
    -- 建立時間
    couponImg NVARCHAR(500),
    -- 照片路徑 
    CONSTRAINT FK_discount_merchant
        FOREIGN KEY
(merchantId)
        REFERENCES merchant
(merchantId)
);

CREATE TABLE redemption_log
(
    logId INT IDENTITY(1,1) PRIMARY KEY,
    -- 自動遞增主鍵
    memId INT NOT NULL,
    -- 會員ID
    couponId INT NOT NULL,
    -- 優惠券ID
    pointsSpent INT NOT NULL,
    -- 當時扣除的點數
    couponName NVARCHAR(100),
    -- 優惠券名稱快照
    redeemTime DATETIME2 DEFAULT GETDATE()
    -- 核銷時間，預設為當前時間
);

-- 建議加上索引，未來資料量大時查詢較快
CREATE INDEX idx_redemption_memId ON redemption_log(memId);
CREATE INDEX idx_redemption_time ON redemption_log(redeemTime);
--========================== 翊庭 TABLE END===================
--============子桓 TABLE ver.20260121============
CREATE TABLE recRent
(
    recSeqId INT IDENTITY(11736900,1) NOT NULL,
    --  隱藏的流水號，負責自動遞增
    recId AS ('R' + RIGHT('000000000' + CAST(recSeqId AS VARCHAR(9)), 9)) PERSISTED,-- 'R' + 補零至 9 位數
    memId INT NOT NULL,
    --  外鍵:Member ID (可為 NULL)
    couponId INT NULL,
    --  外鍵：使用的優惠方案
    seatsId VARCHAR(10) NOT NULL,
    --  外鍵：便攜座椅編號
    spotIdRent INT NOT NULL,
    --  外鍵：租用點機台 ID (必須與 spot 表格的 spotId 類型一致)
    spotIdReturn INT NULL,
    --  外鍵：歸還點機台 ID (可為 NULL)
    recRentDT2 DATETIME2(0) NOT NULL,
    -- 租用日期時間
    recReturnDT2 DATETIME2(0) NULL,
    -- 歸還日期時間 (未歸還時為 NULL)
    recUsageDT2 DECIMAL NULL,
    -- 使用時間 DATETIME2
    recStatus NVARCHAR(15) NULL,
    -- 訂單狀態
    recPrice INT NULL,
    -- 原始價格
    recRequestPay INT NULL,
    -- 請款金額
    recPayment INT NULL,
    -- 付款金額
    recPayBy VARCHAR(16) NULL,
    -- 付款方式
    recInvoice VARCHAR(16) NULL,
    -- 發票號碼
    recCarrier VARCHAR(16) NULL,
    -- 發票載具
    recViolatInt INT NOT NULL,
    -- 違規類型
    recNote NVARCHAR(255) NULL,
    -- 備註
    --Constraints
    CONSTRAINT PK_recRent_seq PRIMARY KEY (recSeqId),
    -- 技術主鍵
    CONSTRAINT UK_recRent_recId UNIQUE (recId),
    -- 業務主鍵 (R00001)
    --FK
    CONSTRAINT FK_recRent_MemberId FOREIGN KEY (memId) REFERENCES member(memId),
    CONSTRAINT FK_recRent_CouponId FOREIGN KEY (couponId) REFERENCES discount(couponId),
    CONSTRAINT FK_recRent_RentSpot FOREIGN KEY (spotIdRent) REFERENCES renting_Spot(spotId),
    CONSTRAINT FK_recRent_ReturnSpot FOREIGN KEY (spotIdReturn) REFERENCES renting_Spot(spotId)
);
--========================== 子桓 TABLE END===================
--===========BUILD TABLE END===============



--=========================TEST DATA===========================
--=================奕穎 DATA ver 20260121 =========================
INSERT INTO member
    (memUsername, memPassword, memName, memEmail, memPhone,
    memStatus, memPoints, memViolation, memLevel, memInvoice)
VALUES
    ('zzz', 'zzz', N'zzz', 'zzz@gmail.com', '0999888777', 1, 120, 0, 1, '/zzz9999'),
    ('alan123', 'pass123!', N'林小安', 'alan123@gmail.com', '0912345678', 1, 120, 0, 1, '/HH8U3V9'),
    ('betty5566', 'pass5566!', N'吳小美', 'betty5566@gmail.com', '0922666888', 1, 450, 1, 2, NULL),
    ('chung999', 'pass999!', N'張大中', 'chung999@gmail.com', '0933123456', 1, 80, 0, 1, '/ABCD123'),
    ('milktea07', 'tea2024!', N'陳奶茶', 'milk07@gmail.com', '0956123123', 1, 200, 2, 2, NULL),
    ('yoyo777', 'yo777###', N'黃悠悠', 'yoyo777@gmail.com', '0966333123', 1, 910, 0, 3, '/LL89KK2'),
    ('tony888', 't888pass', N'王東尼', 'tony888@gmail.com', '0911222333', 1, 50, 0, 1, NULL),
    ('qiqi520', 'q520love', N'蔡琪琪', 'qiqi520@gmail.com', '0988999555', 1, 375, 1, 2, '/QR88991'),
    ('jason007', 'j007pwd', N'李宗翰', 'jason007@gmail.com', '0988777666', 1, 135, 0, 1, NULL),
    ('apple321', 'a321pass', N'鄭雅萍', 'apple321@gmail.com', '0977555444', 1, 520, 2, 3, '/UUII77T'),
    ('hsieh111', 'h1110k', N'謝俊宏', 'hsieh111@gmail.com', '0999888777', 1, 60, 0, 1, NULL);

INSERT INTO admin
    (admUsername, admPassword, admName, admEmail, admRole)
VALUES

    ('aaa', 'aaa', N'系統管理員', 'aaa@system.com', 9),
    ('admin001', 'Root001!', N'系統管理員', 'admin001@system.com', 9),
    ('staff01', 'Staff01@', N'王芳儀', 'staff01@system.com', 1),
    ('staff02', 'Staff02@', N'林建宏', 'staff02@system.com', 1),
    ('manager01', 'Mgr2024$', N'陳偉倫', 'manager01@system.com', 9),
    ('super88', 'Super88#', N'張芷晴', 'super88@system.com', 1),
    ('admin002', 'Pass1234', N'王大明', 'admin002@system.com', 1),
    ('sysadmin', 'Pass1234', N'林小華', 'sysadmin@system.com', 1),
    ('manager02', 'Pass1234', N'陳美麗', 'manager02@system.com', 9),
    ('backend01', 'Pass1234', N'張志宏', 'backend01@system.com', 1),
    ('superuser', 'Pass1234', N'黃巧玲', 'superuser@system.com', 9);
--===============奕穎 DATA END=================

--=======================光宇DATA ver1150121===================
-- 桃園市區域測試資料 (20筆)- 不插入 spotStatus，改吃 DEFAULT (N'營運中')
INSERT INTO dbo.renting_Spot
    (spotCode, spotName, spotAddress, merchantId, latitude, longitude)
VALUES
    (N'TYN001', N'桃園高鐵站', N'桃園市中壢區高鐵北路一段6號', NULL, 25.0133300, 121.2144300),
    (N'TYN002', N'中壢火車站', N'桃園市中壢區中和路139號', 101, 24.9537811, 121.2255764),
    (N'TYN003', N'桃園火車站', N'桃園市桃園區萬壽路三段123號', 102, 24.9898236, 121.3134382),
    (N'TYN004', N'內壢火車站', N'桃園市中壢區中華路一段27號', NULL, 24.9748600, 121.2673200),
    (N'TYN005', N'埔心火車站', N'桃園市楊梅區永美路2號', NULL, 24.9284000, 121.1810500),
    (N'TYN006', N'桃園市政府', N'桃園市桃園區縣府路1號', NULL, 24.9930115, 121.3015976),
    (N'TYN007', N'桃園展演中心', N'桃園市桃園區中正路1188號', 105, 25.0152016, 121.3009949),
    (N'TYN008', N'國立中央大學', N'桃園市中壢區中大路300號', NULL, 24.9682000, 121.1921200),
    (N'TYN009', N'元智大學', N'桃園市中壢區遠東路135號', NULL, 24.9715100, 121.2690300),
    (N'TYN010', N'長庚大學', N'桃園市龜山區文化一路259號', NULL, 25.0441500, 121.3857600),
    (N'TYN011', N'台茂購物中心', N'桃園市蘆竹區南崁路一段112號', 108, 25.0475311, 121.2926712),
    (N'TYN012', N'大江國際購物中心', N'桃園市中壢區中園路二段501號', 109, 25.0130692, 121.2335663),
    (N'TYN013', N'華泰名品城', N'桃園市中壢區春德路189號', 110, 25.0425301, 121.2148132),
    (N'TYN014', N'統領廣場', N'桃園市桃園區中正路61號', 111, 24.9902600, 121.3138800),
    (N'TYN015', N'中原夜市', N'桃園市中壢區實踐路', NULL, 24.9575900, 121.2393300),
    (N'TYN016', N'竹圍漁港', N'桃園市大園區沙崙里1鄰10號', NULL, 25.1118100, 121.2096300),
    (N'TYN017', N'石門水庫', N'桃園市大溪區復興里環湖路一段68號', NULL, 24.8118000, 121.2464000),
    (N'TYN018', N'小人國主題樂園', N'桃園市龍潭區高原路891號', NULL, 24.8315100, 121.1895600),
    (N'TYN019', N'桃園國際棒球場', N'桃園市中壢區領航北路一段1號', NULL, 25.0345000, 121.2036000),
    (N'TYN020', N'林口長庚紀念醫院', N'桃園市龜山區復興街5號', NULL, 25.0494400, 121.3713900);


-- 台北市區域測試資料 (20筆) - 不插入 spotStatus，改吃 DEFAULT (N'營運中')
INSERT INTO dbo.renting_Spot
    (spotCode, spotName, spotAddress, merchantId, latitude, longitude)
VALUES
    (N'TPE001', N'台北101', N'台北市信義區信義路五段7號', 201, 25.0339640, 121.5644720),
    (N'TPE002', N'國立故宮博物院', N'台北市士林區至善路二段221號', NULL, 25.1022200, 121.5484200),
    (N'TPE003', N'台北車站', N'台北市中正區黎明里北平西路3號', 202, 25.0477600, 121.5170900),
    (N'TPE004', N'西門紅樓', N'台北市萬華區成都路10號', NULL, 25.0423000, 121.5073600),
    (N'TPE005', N'總統府', N'台北市中正區重慶南路一段122號', NULL, 25.0403000, 121.5117200),
    (N'TPE006', N'中正紀念堂', N'台北市中正區中山南路21號', NULL, 25.0348200, 121.5219200),
    (N'TPE007', N'國立臺灣大學', N'台北市大安區羅斯福路四段1號', NULL, 25.0173500, 121.5397500),
    (N'TPE008', N'台北市立動物園', N'台北市文山區新光路二段30號', NULL, 24.9984900, 121.5810700),
    (N'TPE009', N'松山文創園區', N'台北市信義區光復南路133號', 205, 25.0436900, 121.5601500),
    (N'TPE010', N'士林夜市', N'台北市士林區基河路101號', NULL, 25.0877400, 121.5242400),
    (N'TPE011', N'台北市政府', N'台北市信義區市府路1號', NULL, 25.0375000, 121.5636100),
    (N'TPE012', N'SOGO忠孝館', N'台北市大安區忠孝東路四段45號', 208, 25.0430200, 121.5445200),
    (N'TPE013', N'新光三越 台北信義新天地A8', N'台北市信義區松高路12號', 209, 25.0371300, 121.5663700),
    (N'TPE014', N'美麗華百樂園', N'台北市中山區敬業三路20號', 210, 25.0833900, 121.5562700),
    (N'TPE015', N'台北小巨蛋', N'台北市松山區南京東路四段2號', NULL, 25.0514100, 121.5531700),
    (N'TPE016', N'捷運大安森林公園站', N'台北市大安區信義路三段100號', NULL, 25.0326100, 121.5367500),
    (N'TPE017', N'捷運市政府站', N'台北市信義區忠孝東路五段6號', NULL, 25.0409000, 121.5645000),
    (N'TPE018', N'捷運中山站', N'台北市大同區南京西路16號', NULL, 25.0524400, 121.5204400),
    (N'TPE019', N'捷運東門站', N'台北市中正區信義路二段166號', NULL, 25.0330600, 121.5292400),
    (N'TPE020', N'南港展覽館1館', N'台北市南港區經貿二路1號', NULL, 25.0551800, 121.6154600);

-- seats（不插入 seatsStatus，改吃 DEFAULT(N'啟用')）
INSERT INTO dbo.seats
    (seatsName, seatsType, spotId, updatedAt, serialNumber, createdAt)
VALUES
    (N'置物椅-A01', N'E椅', 11, SYSDATETIME(), 'SN-2026000', DEFAULT),
    (N'置物椅-A01', N'E椅', 11, SYSDATETIME(), 'SN-2026001', DEFAULT),
    (N'置物椅-A02', N'E椅', 11, SYSDATETIME(), 'SN-2026002', DEFAULT),
    (N'置物椅-B01', N'E椅', 12, SYSDATETIME(), 'SN-2026003', DEFAULT),
    (N'置物椅-B02', N'E椅', 12, SYSDATETIME(), 'SN-2026004', DEFAULT),
    (N'基本椅-C01', N'B椅', 13, SYSDATETIME(), 'SN-2026005', DEFAULT),
    (N'基本椅-C02', N'B椅', 13, SYSDATETIME(), 'SN-2026006', DEFAULT),
    (N'置物椅-D01', N'E椅', 14, SYSDATETIME(), 'SN-2026007', DEFAULT),
    (N'置物椅-D02', N'E椅', 14, SYSDATETIME(), 'SN-2026008', DEFAULT),
    (N'基本椅-E01', N'E椅', 15, SYSDATETIME(), 'SN-2026009', DEFAULT),
    (N'基本椅-F01', N'B椅', 16, SYSDATETIME(), 'SN-2026010', DEFAULT),
    (N'基本椅-G01', N'B椅', 17, SYSDATETIME(), 'SN-2026011', DEFAULT),
    (N'置物椅-H01', N'E椅', 18, SYSDATETIME(), 'SN-2026012', DEFAULT),
    (N'置物椅-I01', N'E椅', 19, SYSDATETIME(), 'SN-2026013', DEFAULT),
    (N'置物椅-J01', N'E椅', 20, SYSDATETIME(), 'SN-2026014', DEFAULT),
    (N'置物椅-A01', N'E椅', 1, SYSDATETIME(), 'SN-2025000', DEFAULT),
    (N'置物椅-A01', N'E椅', 1, SYSDATETIME(), 'SN-2025001', DEFAULT),
    (N'置物椅-A02', N'E椅', 1, SYSDATETIME(), 'SN-2025002', DEFAULT),
    (N'置物椅-B01', N'E椅', 2, SYSDATETIME(), 'SN-2025003', DEFAULT),
    (N'置物椅-B02', N'E椅', 2, SYSDATETIME(), 'SN-2025004', DEFAULT),
    (N'基本椅-C01', N'B椅', 3, SYSDATETIME(), 'SN-2025005', DEFAULT),
    (N'基本椅-C02', N'B椅', 3, SYSDATETIME(), 'SN-2025006', DEFAULT),
    (N'置物椅-D01', N'E椅', 4, SYSDATETIME(), 'SN-2025007', DEFAULT),
    (N'置物椅-D02', N'E椅', 4, SYSDATETIME(), 'SN-2025008', DEFAULT),
    (N'基本椅-E01', N'E椅', 5, SYSDATETIME(), 'SN-2025009', DEFAULT),
    (N'基本椅-F01', N'B椅', 6, SYSDATETIME(), 'SN-2025010', DEFAULT),
    (N'基本椅-G01', N'B椅', 7, SYSDATETIME(), 'SN-2025011', DEFAULT),
    (N'置物椅-H01', N'E椅', 8, SYSDATETIME(), 'SN-2025012', DEFAULT),
    (N'置物椅-I01', N'E椅', 9, SYSDATETIME(), 'SN-2025013', DEFAULT),
    (N'置物椅-J01', N'E椅', 10, SYSDATETIME(), 'SN-2025014', DEFAULT),
    (N'備用設備-Z99', N'S椅', NULL, SYSDATETIME(), 'SN-2025999', DEFAULT);
--=========================================光宇 TEST DATA  END===================================

--================翊庭 DATA ver.20260121==============
INSERT INTO merchant
    (merchantName, merchantPhone, merchantEmail, merchantAddress, merchantStatus)
VALUES
    ('小王早餐店', N'0912-111-111', N'wang.breakfast@mail.com', N'台北市大安區和平東路100號', 1),
    ('天天飲料店', N'0922-222-222', N'drinkdaily@mail.com', N'台北市信義區光復南路88號', 1),
    ('阿中滷味', N'0933-333-333', N'achung@mail.com', N'新北市板橋區文化路50號', 1),
    ('幸福蛋糕店', N'0944-444-444', N'happycake@mail.com', N'台中市西屯區台灣大道200號', 1),
    ('老張牛肉麵', N'0955-555-555', N'beefnoodle@mail.com', N'高雄市苓雅區成功一路90號', 1),
    ('元氣便當店', N'0966-666-666', N'lunchbox@mail.com', N'台北市北投區中央北路300號', 1),
    ('慢活咖啡館', N'0977-777-777', N'slowcoffee@mail.com', N'台南市中西區民族路30號', 1),
    ('阿美水果行', N'0988-888-888', N'fruitamei@mail.com', N'桃園市中壢區中正路120號', 1),
    ('新味壽司', N'0999-999-999', N'newtaste@mail.com', N'新竹市東區光明路66號', 1),
    ('樂町火鍋', N'0910-010-010', N'hotpotjoy@mail.com', N'嘉義市西區文化路45號', 1);

INSERT INTO discount
    (couponName,couponDescription, pointsRequired, startDate, endDate, merchantId, couponStatus,couponImg)
VALUES
    ('滿100折10', '滿100折10', 50, '2025-01-01', '2026-12-31', 1, 1, '滿100折10元.jpg'),
    ('第二杯半價', '第二杯半價', 80, '2025-01-15', '2026-06-30', 2, 1, '第二杯半價.jpg'),
    ('滷味滿200折30', '滷味滿200折30', 60, '2025-02-01', '2026-12-31', 3, 1, '滿200折20.jpg'),
    ('蛋糕買一送一（限9吋）', '蛋糕買一送一（限9吋）', 200, '2026-03-01', '2025-04-30', 4, 1, '買1送1.jpg'),
    ('牛肉麵免費加麵', '牛肉麵免費加麵', 40, '2025-01-01', '2026-12-31', 5, 1, '免費續湯加麵.jpg'),
    ('便當加菜優惠折20', '便當加菜優惠折20', 30, '2025-02-10', '2026-12-31', 6, 1, '20元折扣.jpg'),
    ('咖啡任選飲品折15', '咖啡任選飲品折15', 70, '2025-01-20', '2026-09-30', 7, 1, '15元折扣.jpg'),
    ('水果禮盒9折', '水果禮盒9折', 150, '2025-04-01', '2026-12-31', 8, 1, '9折.jpg'),
    ('壽司套餐折50', '壽司套餐折50', 120, '2025-01-01', '2026-12-31', 9, 1, '折50.jpg'),
    ('火鍋套餐滿500折100', '火鍋套餐滿500折100', 180, '2025-02-15', '2026-12-31', 10, 1, '500元套餐折價100.jpg');
--=========================================翊庭 TEST DATA  END===================================
--================翌帆 DATA ver.20260121==============
USE [SeatRentSys];
INSERT INTO [dbo].[maintenanceStaff]
    ([staffName], [staffCompany], [staffPhone], [staffEmail], [staffNote], [isActive])
VALUES
    (N'陳國榮', N'永安機電工程行', '0912-345-678', 'kuorong.chen@yongan-fix.com', N'特約水電師傅，負責電力線路查修', 1),
    (N'林雅婷', N'潔淨家園服務社', '0922-123-456', 'yating.lin@cleanhome.tw', N'外包清潔廠商，負責場地日常打掃', 1),
    (N'黃志明', N'極速電腦工作室', '0933-987-654', 'cm.huang@speedy-pc.tw', N'負責機台硬體故障排除 (螢幕、主機)', 1),
    (N'張惠雯', NULL, '0911-222-333', 'huiwen.chang@gmail.com', N'個人接案清潔人員，配合彈性高', 1),
    (N'李建華', N'光速網路企業社', '0955-666-777', 'ch.lee@lightnet.com.tw', N'網路佈線與連線異常處理廠商', 1),
    (N'王怡君', N'安心監控科技', '0988-555-444', 'yichun.wang@safe-monitor.com', N'負責門禁系統與監視器設備維護', 1),
    (N'劉志偉', N'涼爽空調維修站', '0977-111-222', 'cw.liu@cool-ac.tw', N'負責空調設備保養與通風問題', 1),
    (N'吳淑芬', NULL, '0966-888-999', 'shufen.wu@yahoo.com.tw', N'臨時工，負責支援緊急清潔任務', 1),
    (N'蔡明哲', N'智匯資訊科技', '0921-000-111', 'mingche.tsai@smart-it.com.tw', N'軟體系統重灌與設定支援', 1),
    (N'楊宗翰', N'頂尖程式工作室', '0932-444-555', 'th.yang@top-code.com', N'遠端系統除錯與軟體更新協助', 1),
    (N'許家豪', N'強力電力工程', '0910-123-123', 'chiahao.hsu@power-fix.tw', N'高壓電設備檢修與配電盤維護', 1),
    (N'鄭淑惠', N'亮晶晶清潔公司', '0958-777-888', 'shuhui.cheng@shining.com', N'定期深度清潔與消毒作業', 1),
    (N'謝欣怡', N'訊號通訊行', '0917-555-666', 'hsinyi.hsieh@signal-comm.tw', N'無線網路訊號測試與優化', 1),
    (N'洪志強', N'阿強綜合水電', '0929-333-444', 'cc.hung@gmail.com', N'假日緊急叫修支援 (個人)', 1),
    (N'郭美玲', NULL, '0987-654-321', 'meiling.kuo@hotmail.com', N'合作已終止，暫不派案', 0),
    (N'曾國華', N'順風家電維修', '0935-112-233', 'kh.tseng@wind-fix.com', N'一般電器設備更換與維修', 1),
    (N'廖俊傑', N'全能修繕工程', '0918-999-000', 'jj.liao@all-fix.com', N'桌椅結構損壞修補與更換', 1),
    (N'賴秀英', N'美好環境維護', '0920-555-123', 'hsiuying.lai@nice-env.com', N'負責垃圾清運與資源回收分類', 1),
    (N'徐文雄', N'金鑰匙鎖印行', '0970-111-999', 'wh.hsu@key-lock.tw', N'電子鎖電池更換與開鎖服務', 1),
    (N'蘇郁婷', N'連線通科技', '0916-222-888', 'yuting.su@connect-tech.com', N'路由器與交換器硬體設定', 1);
--=============================== 翌帆 DATA END======================================
--==============子桓 DATAver.20260121===============
INSERT INTO recRent
    (memId, couponId, seatsId, spotIdRent, spotIdReturn, recRentDT2, recReturnDT2, recUsageDT2, recStatus,
    recPrice, recRequestPay, recPayment, recPayBy, recInvoice, recCarrier, recViolatInt, recNote)
VALUES
    -- 1~5: 已完成的訂單 (同點歸還)
    (1, NULL, 'SN-2025001', 1, 9, '2024-10-01 10:00:00', '2024-11-01 11:00:00', 3, N'已完成', 100, 100, 100, 'CreditCard', 'AB-12345678', '/AB12345', 0, NULL),
    (2, 1, 'SN-2025003', 2, 2, '2024-11-01 12:30:00', '2024-11-01 13:00:00', 3, N'已完成', 50, 45, 45, 'LinePay', 'AB-12345679', '/CD67890', 0, NULL),
    (3, NULL, 'SN-2025005', 3, 3, '2024-10-02 09:00:00', '2024-11-02 12:00:00', 4, N'已完成', 300, 300, 300, 'Cash', 'AB-12345680', NULL, 0, NULL),
    (4, NULL, 'SN-2025007', 4, 4, '2024-11-02 14:00:00', '2024-11-02 14:30:00', 4, N'已完成', 50, 50, 50, 'ApplePay', 'AB-12345681', '/EF11223', 0, NULL),
    (5, 2, 'SN-2025001', 1, 1, '2024-10-03 18:00:00', '2024-11-03 19:00:00', 5, N'已完成', 100, 80, 80, 'CreditCard', 'AB-12345682', NULL, 0, NULL),
    -- 6~10: 甲地租乙地還 (已完成)
    (6, NULL, 'SN-2025009', 5, 6, '2024-11-05 10:00:00', '2024-11-05 11:30:00', 1111, N'已完成', 150, 150, 150, 'LinePay', 'AB-12345683', '/GH33445', 0, N'甲租乙還'),
    (7, NULL, 'SN-2025011', 7, 8, '2024-10-06 15:00:00', '2024-11-06 16:00:00', 123, N'已完成', 100, 100, 100, 'Cash', 'AB-12345684', NULL, 0, NULL),
    (8, 3, 'SN-2025013', 9, 10, '2024-11-07 08:00:00', '2024-11-07 09:00:00', 32, N'已完成', 100, 90, 90, 'CreditCard', 'AB-12345685', '/IJ55667', 0, NULL),
    (9, NULL, 'SN-2025002', 1, 3, '2024-10-08 20:00:00', '2024-11-08 22:00:00', 4, N'已完成', 200, 200, 200, 'JKOPay', 'AB-12345686', NULL, 0, NULL),
    (10, NULL, 'SN-2025004', 2, 4, '2024-11-09 13:00:00', '2024-11-09 13:45:00', 125, N'已完成', 80, 80, 80, 'ApplePay', 'AB-12345687', '/KL77889', 0, NULL),
    -- 11~15: 租借中 (Active) - 歸還點與時間為 NULL
    (10, NULL, 'SN-2025002', 1, NULL, SYSDATETIME(), NULL, NULL, N'租借中', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL),
    (3, NULL, 'SN-2025007', 4, NULL, DATEADD(MINUTE, -30, SYSDATETIME()), NULL, NULL, N'租借中', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL),
    (2, NULL, 'SN-2025011', 7, NULL, DATEADD(HOUR, -1, SYSDATETIME()), NULL, NULL, N'租借中', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL),
    (4, NULL, 'SN-2025010', 6, NULL, DATEADD(HOUR, -2, SYSDATETIME()), NULL, NULL, N'租借中', NULL, NULL, NULL, NULL, NULL, NULL, 0, N'長時間使用'),
    (1, NULL, 'SN-2025008', 4, NULL, DATEADD(MINUTE, -10, SYSDATETIME()), NULL, NULL, N'租借中', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL),
    -- 16~18: 異常或違規紀錄
    (1, NULL, 'SN-2025006', 3, 3, '2024-10-01 10:00:00', '2024-10-01 14:00:00', 3, N'已完成', 400, 400, 400, 'Cash', 'AB-12345688', NULL, 1, N'超時歸還'),
    (7, NULL, 'SN-2025004', 2, NULL, '2024-10-05 09:00:00', NULL, NULL, N'未歸還', 0, 0, 0, NULL, NULL, NULL, 2, N'惡意遺失'),
    (8, NULL, 'SN-2025012', 8, 8, '2024-10-10 11:00:00', '2024-10-10 11:10:00', 21, N'已取消', 0, 0, 0, NULL, NULL, NULL, 0, N'設備故障取消'),
    -- 19~20: 跨日租借
    (9, NULL, 'SN-2025014', 10, 10, '2024-11-20 23:00:00', '2024-10-21 01:00:00', 2, N'已完成', 200, 200, 200, 'CreditCard', 'AB-12345689', NULL, 0, N'跨日租借'),
    (2, 5, 'SN-2025001', 1, 2, '2024-11-22 23:50:00', '2024-11-23 00:20:00', 2, N'已完成', 50, 40, 40, 'LinePay', 'AB-12345690', NULL, 0, NULL),
    (1, NULL, 'SN-2025001', 1, 1, '2024-10-06 10:00:00', '2024-10-01 11:00:00', 1, N'已完成', 100, 100, 100, 'CreditCard', 'AB-12345678', '/AB12345', 0, NULL),
    (1, 1, 'SN-2025003', 2, 2, '2024-11-01 12:30:00', '2024-10-01 13:00:00', 12, N'已完成', 50, 45, 45, 'LinePay', 'AB-12345679', '/CD67890', 0, NULL),
    (1, NULL, 'SN-2025005', 3, 3, '2024-10-06 09:00:00', '2024-10-02 12:00:00', 2, N'已完成', 300, 300, 300, 'Cash', 'AB-12345680', NULL, 0, NULL),
    (1, NULL, 'SN-2025007', 4, 4, '2024-11-02 14:00:00', '2024-10-02 14:30:00', 1, N'已完成', 50, 50, 50, 'ApplePay', 'AB-12345681', '/EF11223', 0, NULL),
    (5, 2, 'SN-2025001', 1, 1, '2024-10-08 18:00:00', '2024-10-03 19:00:00', 0, N'已完成', 100, 80, 80, 'CreditCard', 'AB-12345682', NULL, 0, NULL),
    -- 6~10: 甲地租乙地還 (已完成)
    (3, NULL, 'SN-2025009', 5, 6, '2024-11-05 10:00:00', '2024-10-05 11:30:00', 1, N'已完成', 150, 150, 150, 'LinePay', 'AB-12345683', '/GH33445', 0, N'甲租乙還'),
    (3, NULL, 'SN-2025011', 7, 8, '2024-10-13 15:00:00', '2024-10-06 16:00:00', 132, N'已完成', 100, 100, 100, 'Cash', 'AB-12345684', NULL, 0, NULL),
    (8, 3, 'SN-2025013', 9, 10, '2024-11-07 08:00:00', '2024-10-07 09:00:00', 4, N'已完成', 100, 90, 90, 'CreditCard', 'AB-12345685', '/IJ55667', 0, NULL),
    (9, NULL, 'SN-2025002', 1, 3, '2024-10-28 20:00:00', '2024-10-08 22:00:00', 4, N'已完成', 200, 200, 200, 'JKOPay', 'AB-12345686', NULL, 0, NULL),
    (10, NULL, 'SN-2025004', 2, 4, '2024-11-09 13:00:00', '2024-10-09 13:45:00', 34, N'已完成', 80, 80, 80, 'ApplePay', 'AB-12345687', '/KL77889', 0, NULL);
--SQL VIEWS
USE [SeatRentSys];
GO
CREATE VIEW V_RentDetails
AS
    SELECT
        r.recId, r.memId, m.memName, r.couponId, d.couponDescription couponDesc, r.seatsId, r.spotIdRent,
        s1.spotName AS RentSpotName, r.spotIdReturn, s2.spotName AS ReturnSpotName,
        r.recRentDT2, r.recReturnDT2, r.recUsageDT2, r.recPrice, r.recRequestPay, r.recPayment, r.recPayBy,
        r.recInvoice, r.recCarrier, r.recViolatInt, r.recNote, r.recStatus
    FROM recRent r
        -- 1. 關聯會員 (必定存在，使用 INNER JOIN)
        INNER JOIN member m ON r.memId = m.memId
        LEFT JOIN discount d ON r.couponId = d.couponId
        -- 2. 關聯租借點 (必定存在，使用 INNER JOIN)
        INNER JOIN renting_Spot s1 ON r.spotIdRent = s1.spotId
        -- 3. 關聯歸還點 (可能未還，使用 LEFT JOIN)
        LEFT JOIN renting_Spot s2 ON r.spotIdReturn = s2.spotId;
--==================子桓 DATA END====================
--=================DB BUILD END====================
