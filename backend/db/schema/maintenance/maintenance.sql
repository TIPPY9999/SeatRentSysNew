CREATE TABLE [dbo].[maintenanceInformation] (
    [ticketId]        INT            IDENTITY (1, 1) NOT NULL,
    [spotId]          INT            NULL,
    [issueType]       NVARCHAR (200) NOT NULL,
    [issueDesc]       NVARCHAR (500) NULL,
    [issuePriority]   VARCHAR (100)  DEFAULT ('NORMAL') NOT NULL,
    [issueStatus]     VARCHAR (50)   DEFAULT ('REPORTED') NOT NULL,
    [assignedStaffId] INT            NULL,
    [reportedAt]      DATETIME2 (7)  DEFAULT (sysdatetime()) NOT NULL,
    [startAt]         DATETIME2 (7)  NULL,
    [resolvedAt]      DATETIME2 (7)  NULL,
    [resolveNote]     NVARCHAR (500) NULL,
    [resultType]      NVARCHAR (50)  NULL,
    [seatsId]         INT            NULL,
    PRIMARY KEY CLUSTERED ([ticketId] ASC),
    CONSTRAINT [FK_maintenanceInformation_seats] FOREIGN KEY ([seatsId]) REFERENCES [dbo].[seats] ([seatsId]),
    CONSTRAINT [fkMaintAssignedStaffId] FOREIGN KEY ([assignedStaffId]) REFERENCES [dbo].[maintenanceStaff] ([staffId])
);



=================================

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

