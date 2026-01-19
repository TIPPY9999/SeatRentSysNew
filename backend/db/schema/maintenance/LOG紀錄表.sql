-- =============================================
-- 表名：maintenanceLog (維修歷程記錄表)
-- 整合版：包含 Idempotency 檢查與效能索引
-- =============================================

-- 1. 建立資料表 (如果表不存在才建立)
IF OBJECT_ID(N'dbo.maintenanceLog', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[maintenanceLog] (
        [logId]     INT            IDENTITY (1, 1) NOT NULL, -- 流水號 PK
        [ticketId]  INT            NOT NULL,                 -- 關聯原本的工單 ID
        [operator]  NVARCHAR (50)  NOT NULL,                 -- 操作者 (支援中文)
        [action]    VARCHAR (50)   NOT NULL,                 -- 動作代號 (英文)
        [comment]   NVARCHAR (500) NULL,                     -- 詳細說明
        [createdAt] DATETIME2 (7)  DEFAULT (sysdatetime()) NOT NULL, -- 發生時間 (預設當下)

        -- 設定主鍵 (Primary Key)
        CONSTRAINT [PK_maintenanceLog] PRIMARY KEY CLUSTERED ([logId] ASC)
    );


-- 2. 建立外鍵關聯 (如果 FK 不存在才建立)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_maintenanceLog_ticket')
BEGIN
    ALTER TABLE [dbo].[maintenanceLog]
    ADD CONSTRAINT [FK_maintenanceLog_ticket] 
    FOREIGN KEY ([ticketId]) 
    REFERENCES [dbo].[maintenanceInformation] ([ticketId])
    ON DELETE CASCADE; -- 工單刪除時，歷史紀錄一併刪除
    PRINT ' 外鍵 FK_maintenanceLog_ticket 建立成功';
END;

-- 3. 建立查詢索引 (針對 Timeline 優化)
-- 這會讓 "SELECT * FROM logs WHERE ticketId = ? ORDER BY createdAt DESC" 飛快
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_maintenanceLog_ticket_createdAt')
BEGIN
    CREATE NONCLUSTERED INDEX [IX_maintenanceLog_ticket_createdAt]
    ON [dbo].[maintenanceLog] ([ticketId] ASC, [createdAt] DESC);
    PRINT '效能索引 IX_maintenanceLog_ticket_createdAt 建立成功';
END;