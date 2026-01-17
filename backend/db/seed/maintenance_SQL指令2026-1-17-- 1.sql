-- 1. 新增 seatsId 欄位 (名稱對應您的 seats 資料表 PK)
ALTER TABLE [dbo].[maintenanceInformation]
ADD [seatsId] INT NULL;
GO

-- 2. 建立 Foreign Key 關聯
-- 對應到 [dbo].[seats] 表的 [seatsId] 欄位
ALTER TABLE [dbo].[maintenanceInformation]
WITH CHECK ADD CONSTRAINT [FK_maintenanceInformation_seats] 
FOREIGN KEY([seatsId])
REFERENCES [dbo].[seats] ([seatsId]);
GO

-- 3. 修改 spotId 為允許 NULL
-- 這樣才能建立「只修椅子 (seatsId)、不修機台 (spotId is null)」的工單
ALTER TABLE [dbo].[maintenanceInformation]
ALTER COLUMN [spotId] INT NULL;
GO