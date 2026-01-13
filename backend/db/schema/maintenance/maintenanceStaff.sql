CREATE TABLE [dbo].[maintenanceStaff] (
    [staffId]      INT            IDENTITY (1, 1) NOT NULL,
    [staffName]    NVARCHAR (50)  NOT NULL,
    [staffCompany] NVARCHAR (100) NULL,
    [staffPhone]   VARCHAR (20)   NULL,
    [staffEmail]   VARCHAR (100)  NULL,
    [staffNote]    NVARCHAR (200) NULL,
    [createdAt]    DATETIME2 (7)  DEFAULT (sysdatetime()) NOT NULL,
    [isActive]     BIT            CONSTRAINT [DF_maintenanceStaff_isActive] DEFAULT ((1)) NOT NULL,
    PRIMARY KEY CLUSTERED ([staffId] ASC)
);

