package com.example.backend.service.maintenance;

import com.example.backend.dto.maintenance.SpotOptionDto;
import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.model.spot.RentalSpot;
import com.example.backend.repository.maintenance.MaintenanceInformationRepository;
import com.example.backend.repository.maintenance.MaintenanceStaffRepository;
import com.example.backend.repository.spot.RentalSpotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class MaintenanceInformationService {

    public static final String STATUS_REPORTED          = "REPORTED";
    public static final String STATUS_ASSIGNED          = "ASSIGNED";
    public static final String STATUS_UNDER_MAINTENANCE = "UNDER_MAINTENANCE";
    public static final String STATUS_RESOLVED          = "RESOLVED";
    public static final String STATUS_CANCELLED         = "CANCELLED";

    // 優先權常數
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

    //新增租借點 Repository，用來驗證 spotId 是否存在
    private final RentalSpotRepository rentalSpotRepo;

    public MaintenanceInformationService(MaintenanceInformationRepository mtifRepo,
                                         MaintenanceStaffRepository staffRepo,RentalSpotRepository rentalSpotRepo) {
        this.mtifRepo = mtifRepo;
        this.staffRepo = staffRepo;
        this.rentalSpotRepo = rentalSpotRepo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Transactional(readOnly = true)
    public List<Integer> getAllSpotIds(){
        return rentalSpotRepo.findAll()
                .stream()
                .map(RentalSpot::getSpotId)    
                .sorted(Comparator.naturalOrder())
                .toList();

    }

    public List<SpotOptionDto> getSpotOptions(){
        return rentalSpotRepo.findAll()
                .stream()
                .map(spot -> new SpotOptionDto(
                        spot.getSpotId(),
                        spot.getSpotCode(),
                        spot.getSpotName(),
                        spot.getSpotAddress(),
                        spot.getSpotStatus()
                ))
                .filter(s ->"營運中".equals(s.getSpotStatus()))
                .sorted((a, b) -> Integer.compare(a.getSpotId(), b.getSpotId()))
                .toList();
    }

    // ============ 建立 / 更新 / 刪除 ============

    public MaintenanceInformation createTicket(MaintenanceInformation mtif) {
        // ★ 1. 強制清空 ID，防止前端意外傳入 ID 導致 JPA 變成 Update
        mtif.setTicketId(null);
        
        validateForCreate(mtif);
        validateAssignedStaff(mtif.getAssignedStaffId());

        // ★ 2. 資料淨化 (Trim)
        mtif.setIssueType(mtif.getIssueType().trim());
        
        if (!isBlank(mtif.getIssueDesc())) {
            mtif.setIssueDesc(mtif.getIssueDesc().trim());
        } else {
            mtif.setIssueDesc(null); // 空白轉 null
        }

        // ★ 3. 處理優先權 (預設值 + 驗證 + Trim)
        if (isBlank(mtif.getIssuePriority())) {
            mtif.setIssuePriority(PRIORITY_NORMAL);
        } else {
            String p = mtif.getIssuePriority().trim().toUpperCase(); // 轉大寫防呆
            if (!isValidPriority(p)) {
                throw new IllegalArgumentException("無效的優先權: " + p);
            }
            mtif.setIssuePriority(p);
        }

        // 狀態處理
        if (mtif.getAssignedStaffId() != null) {
            mtif.setIssueStatus(STATUS_ASSIGNED);
        } else {
            mtif.setIssueStatus(STATUS_REPORTED);
        }

        // 安全清理流程欄位
        mtif.setStartAt(null);
        mtif.setResolvedAt(null);
        mtif.setResultType(null);
        mtif.setResolveNote(null);

        return mtifRepo.save(mtif);
    }

    public MaintenanceInformation updateTicket(MaintenanceInformation mtif) {
        if (mtif.getTicketId() == null) throw new IllegalArgumentException("更新工單時 ticketId 為必填欄位");
        // spotId 不再更新，移除 setSpotId
        if (isBlank(mtif.getIssueType())) throw new IllegalArgumentException("issueType 為必填欄位");

        MaintenanceInformation existing = getRequiredTicket(mtif.getTicketId());

        // ★ 4. 狀態鎖定：已結案/已取消/維修中 不允許修改基本資料
        String currentStatus = existing.getIssueStatus();
        if (STATUS_RESOLVED.equals(currentStatus) || STATUS_CANCELLED.equals(currentStatus)) {
            throw new IllegalStateException("工單已結案或取消，不允許修改內容");
        }
        if (STATUS_UNDER_MAINTENANCE.equals(currentStatus)) {
            throw new IllegalStateException("維修中不允許修改工單基本資訊");
        }

        // 更新欄位 (含 Trim)
        existing.setIssueType(mtif.getIssueType().trim());

        if (!isBlank(mtif.getIssueDesc())) {
            existing.setIssueDesc(mtif.getIssueDesc().trim());
        } else {
            existing.setIssueDesc(null); // 允許清空描述
        }
        
        // 更新優先權 (含驗證)
        if (!isBlank(mtif.getIssuePriority())) {
            String p = mtif.getIssuePriority().trim().toUpperCase();
            if (!isValidPriority(p)) {
                throw new IllegalArgumentException("無效的優先權: " + p);
            }
            existing.setIssuePriority(p);
        }

        return mtifRepo.save(existing);
    }

    public void deleteTicket(int ticketId) {
        if (!mtifRepo.existsById(ticketId)) {
            throw new IllegalArgumentException("找不到指定的維修工單，ticketId = " + ticketId);
        }
        mtifRepo.deleteById(ticketId);
    }

    public void cancelTicket(int ticketId, String cancelReason) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);
        String status = mtif.getIssueStatus();

        if (STATUS_UNDER_MAINTENANCE.equals(status)) {
            throw new IllegalStateException("維修中不可取消工單，請先完成維修或聯繫管理員");
        }
        if (STATUS_RESOLVED.equals(status)) {
            throw new IllegalStateException("工單已結案，無法取消");
        }
        if (STATUS_CANCELLED.equals(status)) {
            throw new IllegalStateException("工單已是取消狀態，無需重複操作");
        }

        mtif.setIssueStatus(STATUS_CANCELLED);

        if (!isBlank(cancelReason)) {
            String oldNote = mtif.getResolveNote();
            mtif.setResolveNote(isBlank(oldNote) ? "[取消原因] " + cancelReason : oldNote + "；[取消原因] " + cancelReason);
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

    // ★ 5. 修改：使用排序過的 findAll
    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getAllTickets() {
        return mtifRepo.findAllByOrderByReportedAtDescTicketIdAsc();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getActiveTickets() {
        return mtifRepo.findByIssueStatusInOrderByReportedAtDescTicketIdAsc(
            Arrays.asList(STATUS_REPORTED, STATUS_ASSIGNED, STATUS_UNDER_MAINTENANCE)
        );
    }

    @Transactional(readOnly = true)
    public List<MaintenanceInformation> getHistoryTickets() {
        return mtifRepo.findByIssueStatusInOrderByReportedAtDescTicketIdAsc(
            Arrays.asList(STATUS_RESOLVED, STATUS_CANCELLED)
        );
    }

    // ============ 狀態流程 ============

    public void assignStaff(int ticketId, Integer staffId) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);
        String currentStatus = mtif.getIssueStatus();

        if (STATUS_RESOLVED.equals(currentStatus)
                || STATUS_CANCELLED.equals(currentStatus)
                || STATUS_UNDER_MAINTENANCE.equals(currentStatus)) {
            throw new IllegalStateException("目前狀態無法變更指派 (需為 REPORTED 或 ASSIGNED)");
        }

        validateAssignedStaff(staffId);
        mtif.setAssignedStaffId(staffId);

        if (STATUS_REPORTED.equals(currentStatus) && staffId != null) {
            mtif.setIssueStatus(STATUS_ASSIGNED);
        } else if (staffId == null && STATUS_ASSIGNED.equals(currentStatus)) {
            mtif.setIssueStatus(STATUS_REPORTED);
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
            throw new IllegalStateException("尚未指派維修人員，無法開始維修");
        }

        if (mtif.getStartAt() == null) {
            mtif.setStartAt(LocalDateTime.now());
        }
        mtif.setIssueStatus(STATUS_UNDER_MAINTENANCE);

        mtifRepo.save(mtif);
    }

    public void resolveTicket(int ticketId, String resultType, String resolveNote) {
        if (isBlank(resultType)) throw new IllegalArgumentException("維修結果 resultType 是必填欄位");
        if (!isValidResultType(resultType)) throw new IllegalArgumentException("錯誤的結果類型：" + resultType);

        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        if (!STATUS_UNDER_MAINTENANCE.equals(mtif.getIssueStatus())) {
            throw new IllegalStateException("只有維修中的工單可以結案");
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

        if(!rentalSpotRepo.existsById(mtif.getSpotId())){
            throw new IllegalArgumentException("找不到指定的租借點" + mtif.getSpotId());
        }
    }

    private void validateAssignedStaff(Integer staffId) {
        if (staffId == null) return;
        MaintenanceStaff staff = staffRepo.findById(staffId).orElse(null);
        if (staff == null) throw new IllegalArgumentException("找不到指定的維修人員");
        if (Boolean.FALSE.equals(staff.getIsActive())) throw new IllegalArgumentException("該人員已停用，無法指派任務");
    }

    private boolean isValidResultType(String resultType) {
        if (isBlank(resultType)) return false;
        return Arrays.asList(RESULT_FIXED, RESULT_NOT_FIXED, RESULT_NO_ISSUE, RESULT_NOT_FIXABLE, RESULT_OTHER)
                     .contains(resultType);
    }

    // ★ 6. 新增優先權驗證
    private boolean isValidPriority(String priority) {
        if (isBlank(priority)) return false;
        return Arrays.asList(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT)
                     .contains(priority);
    }

    
}