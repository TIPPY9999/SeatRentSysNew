package com.example.backend.dto.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ✅ P5 修復：工單資料傳輸物件
 * 目的：統一前端狀態顯示，避免 issueStatus 和 resultType 混用
 */
@Data
public class MaintenanceTicketDto {
    private Integer ticketId;
    private Integer spotId;
    private String spotName;
    private Integer seatsId;
    private String seatName;
    private String issueType;
    private String issueDesc;
    private String issuePriority;
    private String issueStatus;
    private String resultType;
    private Integer assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime reportedAt;
    private LocalDateTime startAt;
    private LocalDateTime resolvedAt;
    private String resolveNote;
    
    /**
     * ✅ P5 核心欄位：統一的狀態顯示文字
     * 優先使用 resultType（結案後），否則使用 issueStatus（進行中）
     */
    private String displayStatus;
    
    /**
     * 從 MaintenanceInformation 實體轉換為 DTO
     */
    public static MaintenanceTicketDto fromEntity(MaintenanceInformation entity) {
        if (entity == null) return null;
        
        MaintenanceTicketDto dto = new MaintenanceTicketDto();
        dto.setTicketId(entity.getTicketId());
        dto.setSpotId(entity.getSpotId());
        dto.setSeatsId(entity.getSeatsId());
        dto.setIssueType(entity.getIssueType());
        dto.setIssueDesc(entity.getIssueDesc());
        dto.setIssuePriority(entity.getIssuePriority());
        dto.setIssueStatus(entity.getIssueStatus());
        dto.setResultType(entity.getResultType());
        dto.setAssignedStaffId(entity.getAssignedStaffId());
        dto.setReportedAt(entity.getReportedAt());
        dto.setStartAt(entity.getStartAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setResolveNote(entity.getResolveNote());
        
        // ✅ 計算 displayStatus
        dto.setDisplayStatus(calculateDisplayStatus(entity.getIssueStatus(), entity.getResultType()));
        
        return dto;
    }
    
    /**
     * 計算統一的顯示狀態
     * 優先級：resultType（已結案時） > issueStatus（進行中時）
     */
    private static String calculateDisplayStatus(String issueStatus, String resultType) {
        // 如果工單已結案且有結果類型，顯示結果
        if ("RESOLVED".equals(issueStatus) && resultType != null) {
            return switch (resultType) {
                case "FIXED" -> "已修復";
                case "NOT_FIXED" -> "未修復";
                case "NO_ISSUE" -> "無問題";
                case "NOT_FIXABLE" -> "無法修復";
                case "OTHER" -> "其他";
                default -> "已解決";
            };
        }
        
        // 否則顯示流程狀態
        return switch (issueStatus != null ? issueStatus : "UNKNOWN") {
            case "REPORTED" -> "已報修";
            case "ASSIGNED" -> "已派工";
            case "UNDER_MAINTENANCE" -> "維修中";
            case "RESOLVED" -> "已解決";
            case "CANCELLED" -> "已取消";
            default -> "未知";
        };
    }
    
    /**
     * 取得狀態標記類型（用於前端顯示樣式）
     */
    public String getStatusType() {
        if ("RESOLVED".equals(issueStatus)) {
            return "FIXED".equals(resultType) ? "success" : "info";
        }
        
        return switch (issueStatus != null ? issueStatus : "UNKNOWN") {
            case "REPORTED" -> "warning";
            case "ASSIGNED" -> "primary";
            case "UNDER_MAINTENANCE" -> "danger";
            case "CANCELLED" -> "info";
            default -> "default";
        };
    }
}
