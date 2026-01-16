CREATE TABLE [dbo].[maintenanceInformation] (
    [ticketId]        INT            IDENTITY (1, 1) NOT NULL,
    [spotId]          INT            NOT NULL,
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
    PRIMARY KEY CLUSTERED ([ticketId] ASC),
    CONSTRAINT [fkMaintAssignedStaffId] FOREIGN KEY ([assignedStaffId]) REFERENCES [dbo].[maintenanceStaff] ([staffId])
);

ALTER TABLE maintenanceInformation
ADD CONSTRAINT fkMaintAssignedStaffId -- 維護工單 assignedStaffId 對應 maintenanceStaff.staffId
    FOREIGN KEY (assignedStaffId)
    REFERENCES maintenanceStaff(staffId);


ALTER TABLE maintenanceInformation  
ADD CONSTRAINT fkMaintSpotId          -- 維護工單spotId 對應 rentingSpot.spotId
    FOREIGN KEY (spotId)
    REFERENCES renting_Spot(spotId);