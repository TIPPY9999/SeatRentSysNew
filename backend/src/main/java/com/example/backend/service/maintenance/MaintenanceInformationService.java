package com.example.backend.service.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.repository.maintenance.MaintenanceInformationRepository;
import com.example.backend.repository.maintenance.MaintenanceStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MaintenanceInformationService {

    public static final String STATUS_REPORTED          = "REPORTED";
    public static final String STATUS_ASSIGNED          = "ASSIGNED";
    public static final String STATUS_UNDER_MAINTENANCE = "UNDER_MAINTENANCE";
    public static final String STATUS_RESOLVED          = "RESOLVED";
    public static final String STATUS_CANCELLED         = "CANCELLED";

    public static final String PRIORITY_LOW    = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH   = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    public static final String RESULT_FIXED       = "FIXED";
    public static final String RESULT_NOT_FIXED   = "NOT_FIXED";
    public static final String RESULT_NO_ISSUE    = "NO_ISSUE";
    public static final String RESULT_NOT_FIXABLE = "NOT_FIXABLE";
    public static final String RESULT_OTHER       = "OTHER";

    private final MaintenanceInformationRepository mtifRepo;
    private final MaintenanceStaffRepository staffRepo;

    public MaintenanceInformationService(MaintenanceInformationRepository mtifRepo,
                                         MaintenanceStaffRepository staffRepo) {
        this.mtifRepo = mtifRepo;
        this.staffRepo = staffRepo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // ============ 建立 / 更新 / 刪除(不建議硬刪) ============

    public MaintenanceInformation createTicket(MaintenanceInformation mtif) {
        validateForCreate(mtif);
        validateAssignedStaff(mtif.getAssignedStaffId());

        if (isBlank(mtif.getIssuePriority())) {
            mtif.setIssuePriority(PRIORITY_NORMAL);
        }
        if (isBlank(mtif.getIssueStatus())) {
            mtif.setIssueStatus(STATUS_REPORTED);
        }

        // reportedAt 由 DB DEFAULT 產生，你的 Entity 也設 insertable=false
        // 所以這裡不要 setReportedAt()

        return mtifRepo.save(mtif);
    }

    public MaintenanceInformation updateTicket(MaintenanceInformation mtif) {
        validateForUpdate(mtif);
        validateAssignedStaff(mtif.getAssignedStaffId());

        // 建議先確認 DB 有這筆，避免 save 變成「插入」
        MaintenanceInformation existing = getRequiredTicket(mtif.getTicketId());

        // 覆寫允許更新的欄位（避免把 DB 其他欄位洗掉）
        existing.setSpotId(mtif.getSpotId());
        existing.setIssueType(mtif.getIssueType());
        existing.setIssueDesc(mtif.getIssueDesc());
        existing.setIssuePriority(mtif.getIssuePriority());
        existing.setIssueStatus(mtif.getIssueStatus());
        existing.setAssignedStaffId(mtif.getAssignedStaffId());
        existing.setStartAt(mtif.getStartAt());
        existing.setResolvedAt(mtif.getResolvedAt());
        existing.setResolveNote(mtif.getResolveNote());
        existing.setResultType(mtif.getResultType());

        return mtifRepo.save(existing);
    }

    /** 真刪除不推薦；保留測試用 */
    public void deleteTicket(int ticketId) {
        if (!mtifRepo.existsById(ticketId)) {
            throw new IllegalArgumentException("找不到指定的維修工單，ticketId = " + ticketId);
        }
        mtifRepo.deleteById(ticketId);
    }

    public void cancelTicket(int ticketId, String cancelReason) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        if (STATUS_RESOLVED.equals(mtif.getIssueStatus())
                || STATUS_CANCELLED.equals(mtif.getIssueStatus())) {
            throw new IllegalStateException("已結案或已取消的工單不能再取消，ticketId = " + ticketId);
        }

        mtif.setIssueStatus(STATUS_CANCELLED);

        if (!isBlank(cancelReason)) {
            String oldNote = mtif.getResolveNote();
            if (isBlank(oldNote)) {
                mtif.setResolveNote("[取消原因] " + cancelReason);
            } else {
                mtif.setResolveNote(oldNote + "；[取消原因] " + cancelReason);
            }
        }

        mtifRepo.save(mtif);
    }

    // ============ 查詢 ============

    @Transactional(readOnly = true)
    public MaintenanceInformation getTicketById(int ticketId) {
        return mtifRepo.findById(ticketId).orElse(null);
    }

    @Transactional(readOnly = true)
    public MaintenanceInformation getRequiredTicket(int ticketId) {
        return mtifRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定的維修工單，ticketId = " + ticketId));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getTicketsBySpotId(int spotId) {
        return mtifRepo.findBySpotIdOrderByReportedAtDescTicketIdAsc(spotId);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getTicketsByStatus(String issueStatus) {
        if (isBlank(issueStatus)) {
            throw new IllegalArgumentException("工單狀態不能為空");
        }
        if (!isValidStatus(issueStatus)) {
            throw new IllegalArgumentException("不支援的工單狀態：" + issueStatus);
        }
        return mtifRepo.findByIssueStatusOrderByReportedAtDescTicketIdAsc(issueStatus);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getAllTickets() {
        return mtifRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getActiveTickets() {
        // 如果你有加 findByIssueStatusIn... 就用一次查詢更好
        List<MaintenanceInformation> list = new ArrayList<>();
        list.addAll(getTicketsByStatus(STATUS_REPORTED));
        list.addAll(getTicketsByStatus(STATUS_ASSIGNED));
        list.addAll(getTicketsByStatus(STATUS_UNDER_MAINTENANCE));
        return list;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getHistoryTickets() {
        List<MaintenanceInformation> list = new ArrayList<>();
        list.addAll(getTicketsByStatus(STATUS_RESOLVED));
        list.addAll(getTicketsByStatus(STATUS_CANCELLED));
        return list;
    }

    // ============ 狀態流程 ============

    public void assignStaff(int ticketId, Integer staffId) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        if (STATUS_RESOLVED.equals(mtif.getIssueStatus())
                || STATUS_CANCELLED.equals(mtif.getIssueStatus())) {
            throw new IllegalStateException("CANNOT_ASSIGN");
        }

        validateAssignedStaff(staffId);
        mtif.setAssignedStaffId(staffId);

        if (STATUS_REPORTED.equals(mtif.getIssueStatus()) && staffId != null) {
            mtif.setIssueStatus(STATUS_ASSIGNED);
        }

        mtifRepo.save(mtif);
    }

    public void startTicket(int ticketId) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        String status = mtif.getIssueStatus();
        if (!STATUS_REPORTED.equals(status) && !STATUS_ASSIGNED.equals(status)) {
            throw new IllegalStateException("目前狀態無法開始維修，ticketId = " + ticketId + "，status = " + status);
        }

        if (mtif.getAssignedStaffId() == null) {
            throw new IllegalStateException("尚未指派維修人員，無法開始維修，ticketId = " + ticketId);
        }

        if (mtif.getStartAt() == null) {
            mtif.setStartAt(LocalDateTime.now());
        }
        mtif.setIssueStatus(STATUS_UNDER_MAINTENANCE);

        mtifRepo.save(mtif);
    }

    public void resolveTicket(int ticketId, String resultType, String resolveNote) {
        if (isBlank(resultType)) {
            throw new IllegalArgumentException("維修結果 resultType 是必填欄位");
        }
        if (!isValidResultType(resultType)) {
            throw new IllegalArgumentException("錯誤的結果類型：" + resultType);
        }

        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        if (!STATUS_UNDER_MAINTENANCE.equals(mtif.getIssueStatus())) {
            throw new IllegalStateException("只有維修中的工單可以結案，ticketId = " + ticketId +
                    "，status = " + mtif.getIssueStatus());
        }

        mtif.setResolvedAt(LocalDateTime.now());
        mtif.setResultType(resultType);
        mtif.setResolveNote(resolveNote);
        mtif.setIssueStatus(STATUS_RESOLVED);

        mtifRepo.save(mtif);
    }

    // ============ 驗證 ============

    private void validateForCreate(MaintenanceInformation mtif) {
        if (mtif == null) throw new IllegalArgumentException("維修工單不能為空白");
        if (mtif.getSpotId() == null) throw new IllegalArgumentException("spotId 為必填欄位");
        if (mtif.getSpotId() <= 0) throw new IllegalArgumentException("spotId 必須是正數");
        if (isBlank(mtif.getIssueType())) throw new IllegalArgumentException("issueType 為必填欄位");
    }

    private void validateForUpdate(MaintenanceInformation mtif) {
        validateForCreate(mtif);
        if (mtif.getTicketId() == null) throw new IllegalArgumentException("更新工單時 ticketId 為必填欄位");
    }

    private void validateAssignedStaff(Integer staffId) {
        if (staffId == null) return;

        MaintenanceStaff staff = staffRepo.findById(staffId).orElse(null);
        if (staff == null) {
            throw new IllegalArgumentException("找不到指定的維修人員，staffId = " + staffId);
        }
        // 可選：如果你要排除停用的人
        // if (Boolean.FALSE.equals(staff.getIsActive())) throw new IllegalArgumentException("維修人員已停用");
    }

    private boolean isValidResultType(String resultType) {
        if (isBlank(resultType)) return false;
        return RESULT_FIXED.equals(resultType)
                || RESULT_NOT_FIXED.equals(resultType)
                || RESULT_NO_ISSUE.equals(resultType)
                || RESULT_NOT_FIXABLE.equals(resultType)
                || RESULT_OTHER.equals(resultType);
    }

    private boolean isValidStatus(String status) {
        return STATUS_REPORTED.equals(status)
                || STATUS_ASSIGNED.equals(status)
                || STATUS_UNDER_MAINTENANCE.equals(status)
                || STATUS_RESOLVED.equals(status)
                || STATUS_CANCELLED.equals(status);
    }
}
