--重置資料庫
--奕穎
--Version Log: 2026/01/21
--===========CLEAR=================
USE SeatRentSys
DROP TABLE recRent;
DROP TABLE merchant;
DROP TABLE discount;
DROP TABLE maintenanceStaff;
DROP TABLE maintenanceInformation;
DROP TABLE renting_Spot;
DROP TABLE seats;
DROP TABLE member;
DROP TABLE admin;
--============BUILD TABLE==============
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
--============TEST DATA==============
INSERT INTO admin
    (admUsername, admPassword, admName, admEmail, admRole)
VALUES
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

INSERT INTO member
    (memUsername, memPassword, memName, memEmail, memPhone,
    memStatus, memPoints, memViolation, memLevel, memInvoice)
VALUES
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
--============END==============
--光宇
CREATE TABLE renting_Spot
(
    spot_Id INT IDENTITY(1,1) PRIMARY KEY,
    -- 租借點編號
    spot_Code VARCHAR(30) NOT NULL UNIQUE,
    -- 租借點主機代號
    spot_Name NVARCHAR(100) NOT NULL,
    -- 租借點位名稱
    spot_Address NVARCHAR(200) NULL,
    -- 租借地址
    spotStatus NVARCHAR(20) NOT NULL,
    -- 租借點位狀況
    merchant_Id INT NULL,
    -- 合作商家 ID
    created_At DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),
    -- 設置時間
    updated_At DATETIME2 NOT NULL
        DEFAULT SYSDATETIME()
    -- 更新時間
);

CREATE TABLE seats
(
    seats_id INT IDENTITY(1,1) PRIMARY KEY,
    -- 設備編號
    seats_name NVARCHAR(100) NOT NULL,
    -- 設備名稱
    seats_type NVARCHAR(50) NOT NULL,
    -- 設備類型
    seats_status NVARCHAR(20) NOT NULL,
    -- 設備狀態
    spot_id INT NULL,
    -- 所屬租借點位
    updated_at DATETIME2 NOT NULL,
    -- 更新時間

    CONSTRAINT FK_seats_spot FOREIGN KEY (spot_id)
        REFERENCES renting_Spot(spot_Id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);
select *
from seats;
select *
from renting_Spot;

ALTER TABLE renting_Spot
ADD latitude  DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL;

ALTER TABLE seats
ADD serial_number VARCHAR(50) NULL,
    created_at    DATETIME DEFAULT(GETDATE());


--seats
EXEC sp_rename 'seats.seats_id', 'seatsId', 'COLUMN';
EXEC sp_rename 'seats.seats_name', 'seatsName', 'COLUMN';
EXEC sp_rename 'seats.seats_type', 'seatsType', 'COLUMN';
EXEC sp_rename 'seats.seats_status', 'seatsStatus', 'COLUMN';
EXEC sp_rename 'seats.spot_id', 'spotId', 'COLUMN';
EXEC sp_rename 'seats.updated_at', 'updatedAt', 'COLUMN';
EXEC sp_rename 'seats.serial_number', 'serialNumber', 'COLUMN';
EXEC sp_rename 'seats.created_at', 'createdAt', 'COLUMN';

-- spot
EXEC sp_rename 'spot.spot_Id', 'spotId', 'COLUMN';
EXEC sp_rename 'spot.spot_Code', 'spotCode', 'COLUMN';
EXEC sp_rename 'spot.spot_Name', 'spotName', 'COLUMN';
EXEC sp_rename 'spot.spot_Address', 'spotAddress', 'COLUMN';
EXEC sp_rename 'spot.spotStatus', 'spotStatus', 'COLUMN';
EXEC sp_rename 'spot.merchant_Id', 'merchantId', 'COLUMN';
EXEC sp_rename 'spot.created_At', 'createdAt', 'COLUMN';
EXEC sp_rename 'spot.updated_At', 'updatedAt', 'COLUMN';



--阿康
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

    CONSTRAINT FK_discount_merchant
        FOREIGN KEY (merchantId)
        REFERENCES merchant(merchantId)
);
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

Select *
from merchant;
INSERT INTO discount
    (couponDescription, pointsRequired, startDate, endDate, merchantId, couponStatus)
VALUES
    (N'滿100折10', 50, '2025-01-01', '2025-12-31', 1, 1),
    (N'第二杯半價', 80, '2025-01-15', '2025-06-30', 2, 1),
    (N'滷味滿200折30', 60, '2025-02-01', '2025-12-31', 3, 1),
    (N'蛋糕買一送一（限9吋）', 200, '2025-03-01', '2025-04-30', 4, 1),
    (N'牛肉麵免費加麵', 40, '2025-01-01', '2025-12-31', 5, 1),
    (N'便當加菜優惠折20', 30, '2025-02-10', '2025-12-31', 6, 1),
    (N'咖啡任選飲品折15', 70, '2025-01-20', '2025-09-30', 7, 1),
    (N'水果禮盒9折', 150, '2025-04-01', '2025-12-31', 8, 1),
    (N'壽司套餐折50', 120, '2025-01-01', '2025-12-31', 9, 1),
    (N'火鍋套餐滿500折100', 180, '2025-02-15', '2025-12-31', 10, 1);
select *
from discount;

select *
from discount;
select *
from merchant;
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
    couponImg NVARCHAR(500)
);
-- 照片路徑 

CONSTRAINT FK_discount_merchant
        FOREIGN KEY
(merchantId)
        REFERENCES merchant
(merchantId)
);
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

Select *
from merchant;
INSERT INTO discount
    (couponName,couponDescription, pointsRequired, startDate, endDate, merchantId, couponStatus,couponImg)
VALUES
    ('滿100折10', '滿100折10', 50, '2025-01-01', '2025-12-31', 1, 1, '滿100折10元.jpg'),
    ('第二杯半價', '第二杯半價', 80, '2025-01-15', '2025-06-30', 2, 1, '第二杯半價.jpg'),
    ('滷味滿200折30', '滷味滿200折30', 60, '2025-02-01', '2025-12-31', 3, 1, '滿200折20.jpg'),
    ('蛋糕買一送一（限9吋）', '蛋糕買一送一（限9吋）', 200, '2025-03-01', '2025-04-30', 4, 1, '買1送1.jpg'),
    ('牛肉麵免費加麵', '牛肉麵免費加麵', 40, '2025-01-01', '2025-12-31', 5, 1, '免費續湯加麵.jpg'),
    ('便當加菜優惠折20', '便當加菜優惠折20', 30, '2025-02-10', '2025-12-31', 6, 1, '20元折扣.jpg'),
    ('咖啡任選飲品折15', '咖啡任選飲品折15', 70, '2025-01-20', '2025-09-30', 7, 1, '15元折扣.jpg'),
    ('水果禮盒9折', '水果禮盒9折', 150, '2025-04-01', '2025-12-31', 8, 1, '9折.jpg'),
    ('壽司套餐折50', '壽司套餐折50', 120, '2025-01-01', '2025-12-31', 9, 1, '折50.jpg'),
    ('火鍋套餐滿500折100', '火鍋套餐滿500折100', 180, '2025-02-15', '2025-12-31', 10, 1, '500元套餐折價100.jpg');
select *
from discount

ALTER TABLE discount
ADD couponName NVARCHAR(500);
ALTER TABLE discount
ADD couponImg NVARCHAR(500);


--奕穎

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
    updatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    --更新時間
);

INSERT INTO member
    (memUsername, memPassword, memName, memEmail, memPhone,
    memStatus, memPoints, memViolation, memLevel, memInvoice)
VALUES
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


--翌帆
CREATE TABLE maintenanceStaff
(
    --維修聯絡人員表 

    staffId INT IDENTITY(1,1) PRIMARY KEY,
    --維護人員編號
    staffName NVARCHAR(50) NOT NULL,
    --維護人姓名
    staffCompany NVARCHAR(100) NULL,
    --廠商名稱  (未來期末專題有機會再建一張廠商表，這個欄位就會換掉，換成廠商ID去FK廠商資料表)
    staffPhone VARCHAR(20) NULL,
    --電話
    staffEmail VARCHAR(100) NULL,
    --信箱
    staffNote NVARCHAR(200) NULL,
    --備註
    createdAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    --建立時間

);
Go

CREATE TABLE maintenanceInformation
(
    --維護資料表

    ticketId INT IDENTITY(1,1) PRIMARY KEY,
    --工單編號
    spotId INT NOT NULL,
    issueType NVARCHAR(200) NOT NULL,
    --問題類型
    issueDesc NVARCHAR(500) NULL,
    --問題描述
    issuePriority VARCHAR(100) NOT NULL DEFAULT 'NORMAL',
    --工單優先順序(影響可能? 會員等級、狀況緊急等等...) 
    issueStatus VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
    --工單狀態
    assignedStaffId INT NULL ,--派工維護人員
    reportedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),-- 報修發生時間
    startAt DATETIME2 NULL,
    -- 開始維修時間
    resolvedAt DATETIME2 NULL,--維修完成時間
    resolveNote NVARCHAR(500) NULL,--維修說明
    resultType NVARCHAR(50) NULL--維修結果

);

ALTER TABLE maintenanceInformation
ADD CONSTRAINT fkMaintAssignedStaffId -- 維護工單 assignedStaffId 對應 maintenanceStaff.staffId
    FOREIGN KEY (assignedStaffId)
    REFERENCES maintenanceStaff(staffId);
GO



ALTER TABLE maintenanceInformation  --等光宇的表好了 我再ALTER
ADD CONSTRAINT fkMaintSpotId          -- 維護工單spotId 對應 rentingSpot.spotId
    FOREIGN KEY (spotId)
    REFERENCES rentingSpot(spotId);
GO




--子桓
--recViews ver.20251210

--R

SELECT *
FROM V_RentDetails

--D
DROP VIEW V_RentDetails;

--C

CREATE VIEW V_RentDetails
AS
    SELECT
        r.recId,
        r.memId,
        m.memName, -- 來自 member 表
        r.couponId,
        d.couponDescription couponDesc,
        r.seatsId,
        r.spotIdRent, -- 租借點資訊
        s1.spotName AS RentSpotName, -- 來自 renting_Spot (借出點)    
        r.spotIdReturn, -- 歸還點資訊 (可能為 NULL)
        s2.spotName AS ReturnSpotName, -- 來自 renting_Spot (歸還點)    
        r.recRentDT2,-- 交易與時間細節
        r.recReturnDT2,
        r.recUsageDT2,
        r.recPrice,
        r.recRequestPay,
        r.recPayment,
        r.recPayBy,
        r.recInvoice,
        r.recCarrier,
        r.recViolatInt,
        r.recNote,
        r.recStatus

    FROM recRent r
        -- 1. 關聯會員 (必定存在，使用 INNER JOIN)
        INNER JOIN member m ON r.memId = m.memId
        LEFT JOIN discount d ON r.couponId = d.couponId
        -- 2. 關聯租借點 (必定存在，使用 INNER JOIN)
        INNER JOIN renting_Spot s1 ON r.spotIdRent = s1.spotId
        -- 3. 關聯歸還點 (可能未還，使用 LEFT JOIN)
        LEFT JOIN renting_Spot s2 ON r.spotIdReturn = s2.spotId;

--spot ver.20251129 
--C TABLE

CREATE TABLE renting_Spot
(
    spotId INT IDENTITY(1,1) PRIMARY KEY,
    -- 租借點編號
    spotCode VARCHAR(30) NOT NULL UNIQUE,
    -- 租借點主機代號
    spotName NVARCHAR(100) NOT NULL,
    -- 租借點位名稱
    spotAddress NVARCHAR(200) NULL,
    -- 租借地址
    spotStatus NVARCHAR(20) NOT NULL,
    -- 租借點位狀況
    merchantId INT NULL,
    -- 合作商家 ID
    createdAt DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),
    -- 設置時間
    updatedAt DATETIME2 NOT NULL
        DEFAULT SYSDATETIME()
    -- 更新時間
);



CREATE TABLE seats
(
    seatsId INT IDENTITY(1,1) PRIMARY KEY,
    -- 設備編號
    seatsName NVARCHAR(100) NOT NULL,
    -- 設備名稱
    seatsType NVARCHAR(50) NOT NULL,
    -- 設備類型
    seatsStatus NVARCHAR(20) NOT NULL,
    -- 設備狀態
    spotId INT NULL,
    -- 所屬租借點位
    updatedAt DATETIME2 NOT NULL,
    -- 更新時間
    serialNumber VARCHAR(50) NULL,
    createdAt DATETIME DEFAULT(GETDATE())

        CONSTRAINT FK_seats_spot FOREIGN KEY (spotId)    REFERENCES renting_Spot(spotId)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);
--查看表結構
EXEC sp_help 'seats';
EXEC sp_help 'renting_Spot';
--R

select *
from seats;
select *
from renting_Spot;


--D
DELETE renting_Spot
DELETE seats

DROP TABLE seats
DROP TABLE renting_Spot

--C DATA

INSERT INTO renting_Spot
    (spotCode, spotName, spotAddress, spotStatus, merchantId, createdAt, updatedAt)
VALUES
    (N'TP001', N'台北車站大廳點', N'台北市中正區北平西路3號', N'營運中', 101, SYSDATETIME(), SYSDATETIME()),
    (N'TP002', N'西門紅樓前', N'台北市萬華區成都路10號', N'營運中', 102, SYSDATETIME(), SYSDATETIME()),
    (N'TP003', N'信義威秀影城', N'台北市信義區松壽路20號', N'營運中', 103, SYSDATETIME(), SYSDATETIME()),
    (N'TP004', N'板橋車站B1', N'新北市板橋區縣民大道二段7號', N'營運中', 104, SYSDATETIME(), SYSDATETIME()),
    (N'TP005', N'松山文創園區', N'台北市信義區光復南路133號', N'維護中', 105, DATEADD(DAY, -1, SYSDATETIME()), SYSDATETIME()),
    (N'TP006', N'華山1914文創', N'台北市中正區八德路一段1號', N'營運中', 106, SYSDATETIME(), SYSDATETIME()),
    (N'TP007', N'公館商圈入口', N'台北市中正區羅斯福路四段', N'營運中', 107, SYSDATETIME(), SYSDATETIME()),
    (N'TP008', N'士林夜市基河路', N'台北市士林區基河路101號', N'已停用', 108, DATEADD(MONTH, -1, SYSDATETIME()), SYSDATETIME()),
    (N'TP009', N'美麗華百樂園', N'台北市中山區敬業三路20號', N'營運中', 109, SYSDATETIME(), SYSDATETIME()),
    (N'TP010', N'饒河夜市慈祐宮', N'台北市松山區八德路四段761號', N'營運中', 110, SYSDATETIME(), SYSDATETIME()),
    (N'TP011', N'淡水捷運站前', N'新北市淡水區中正路1號', N'維護中', 111, SYSDATETIME(), SYSDATETIME()),
    (N'TP012', N'新莊棒球場', N'新北市新莊區和興街66號', N'營運中', 112, SYSDATETIME(), SYSDATETIME()),
    (N'TP013', N'南港展覽館', N'台北市南港區經貿二路1號', N'營運中', 113, SYSDATETIME(), SYSDATETIME()),
    (N'TP014', N'大安森林公園', N'台北市大安區新生南路二段1號', N'營運中', 114, SYSDATETIME(), SYSDATETIME()),
    (N'TP015', N'行天宮圖書館', N'台北市中山區民權東路二段', N'營運中', 115, SYSDATETIME(), SYSDATETIME());


--seats
INSERT INTO seats
    (seatsName, seatsType, seatsStatus, spotId, updatedAt, serialNumber, createdAt)
VALUES
    (N'按摩椅-A01', N'T椅', N'空閒', 1, SYSDATETIME(), 'SN-2025001', DEFAULT),
    (N'按摩椅-A02', N'T椅', N'使用中', 1, SYSDATETIME(), 'SN-2025002', DEFAULT),
    (N'充電椅-B01', N'E椅', N'空閒', 2, SYSDATETIME(), 'SN-2025003', DEFAULT),
    (N'充電椅-B02', N'E椅', N'故障', 2, SYSDATETIME(), 'SN-2025004', DEFAULT),
    (N'按摩椅-C01', N'H椅', N'空閒', 3, SYSDATETIME(), 'SN-2025005', DEFAULT),
    (N'按摩椅-C02', N'T椅', N'維護中', 3, SYSDATETIME(), 'SN-2025006', DEFAULT),
    (N'置物椅-D01', N'T椅', N'使用中', 4, SYSDATETIME(), 'SN-2025007', DEFAULT),
    (N'置物椅-D02', N'H椅', N'空閒', 4, SYSDATETIME(), 'SN-2025008', DEFAULT),
    (N'KTV椅-E01', N'F椅', N'清潔中', 5, SYSDATETIME(), 'SN-2025009', DEFAULT),
    (N'按摩椅-F01', N'T椅', N'空閒', 6, SYSDATETIME(), 'SN-2025010', DEFAULT),
    (N'充電椅-G01', N'E椅', N'使用中', 7, SYSDATETIME(), 'SN-2025011', DEFAULT),
    (N'按摩椅-H01', N'T椅', N'空閒', 8, SYSDATETIME(), 'SN-2025012', DEFAULT),
    -- 關聯到已停用的站點，測試邏輯
    (N'按摩椅-I01', N'H椅', N'空閒', 9, SYSDATETIME(), 'SN-2025013', DEFAULT),
    (N'按摩椅-J01', N'T椅', N'空閒', 10, SYSDATETIME(), 'SN-2025014', DEFAULT),
    (N'備用設備-Z99', N'H椅', N'庫存', NULL, SYSDATETIME(), 'SN-2025999', DEFAULT);
-- 測試 spotId 為 NULL 的情況


--RecRent ver.20251129
--C
CREATE TABLE recRent
(
    recSeqId INT IDENTITY(1,1) NOT NULL,
    --  隱藏的流水號，負責自動遞增
    -- 2. 定義 recId 為「計算欄位」，自動生成 R00001, R00002...    -- 邏輯：'R' + 補零至 9 位數
    recId AS ('R' + RIGHT('000000000' + CAST(recSeqId AS VARCHAR(9)), 9)) PERSISTED,
    memId INT NOT NULL,
    --  外鍵：Member ID (可為 NULL)
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
    -- 6. 設定約束 (Constraints)
    CONSTRAINT PK_recRent_seq PRIMARY KEY (recSeqId),
    -- 技術主鍵
    CONSTRAINT UK_recRent_recId UNIQUE (recId),
    -- 業務主鍵 (R00001)
    -- 7. 設定外鍵關聯 (關聯到 spot 表格的 spotId)
    CONSTRAINT FK_recRent_MemberId FOREIGN KEY (memId) REFERENCES member(memId),
    CONSTRAINT FK_recRent_CouponId FOREIGN KEY (couponId) REFERENCES discount(couponId),

    CONSTRAINT FK_recRent_RentSpot FOREIGN KEY (spotIdRent) REFERENCES renting_Spot(spotId),
    CONSTRAINT FK_recRent_ReturnSpot FOREIGN KEY (spotIdReturn) REFERENCES renting_Spot(spotId)
);

--查看表結構
EXEC sp_help 'spot';
EXEC sp_help 'recRent'
--R
SELECT*
FROM recRent




--D
DELETE recRent;
DROP TABLE recRent;


--C  測試資料

INSERT INTO recRent
    (
    memId, couponId, seatsId, spotIdRent, spotIdReturn,
    recRentDT2, recReturnDT2, recUsageDT2, recStatus,
    recPrice, recRequestPay, recPayment, recPayBy,
    recInvoice, recCarrier, recViolatInt, recNote
    )
VALUES
    -- 1~5: 已完成的訂單 (同點歸還)
    (1, NULL, 'SN-2025001', 1, 1, '2024-10-01 10:00:00', '2024-11-01 11:00:00', 3, N'已完成', 100, 100, 100, 'CreditCard', 'AB-12345678', '/AB12345', 0, NULL),
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
    (10, NULL, 'SN-2025004', 2, 4, '2024-11-09 13:00:00', '2024-10-09 13:45:00', 34, N'已完成', 80, 80, 80, 'ApplePay', 'AB-12345687', '/KL77889', 0, NULL)









