package com.example.backend.service.maintenance;

import com.example.backend.dto.maintenance.SpotOptionDto;
import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.model.spot.RentalSpot;
import com.example.backend.repository.maintenance.MaintenanceInformationRepository;
import com.example.backend.repository.maintenance.MaintenanceStaffRepository;
import com.example.backend.repository.spot.RentalSpotRepository;
import com.example.backend.repository.spot.SeatRepository; 
import com.example.backend.model.spot.Seat;
import com.example.backend.model.maintenance.MaintenanceLog;
import com.example.backend.repository.maintenance.MaintenanceLogRepository;
import com.example.backend.dto.maintenance.MaintenanceLogResponseDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class MaintenanceInformationService {

    //工單狀態常數
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

    //維修結果常數
    public static final String RESULT_FIXED       = "FIXED";
    public static final String RESULT_NOT_FIXED   = "NOT_FIXED";
    public static final String RESULT_NO_ISSUE    = "NO_ISSUE";
    public static final String RESULT_NOT_FIXABLE = "NOT_FIXABLE";
    public static final String RESULT_OTHER       = "OTHER";

    //場地狀態常數
    public static final String SPOT_STATUS_OPERATIONAL = "營運中";
    public static final String SPOT_STATUS_MAINTENANCE = "維護中";
        public static final String SPOT_STATUS_SUSPENDED   = "暫停營運";
        public static final String SPOT_STATUS_CLOSED      = "已關閉";

    //椅子狀態常數
    public static final String SEAT_STATUS_NORMAL      = "正常"; 
    public static final String SEAT_STATUS_REPAIRING   = "維修中"; // 正在修
    public static final String SEAT_STATUS_MAINTENANCE = "維護中"; // 定期保養
    public static final String SEAT_STATUS_SCRAPPED    = "已報廢"; // 壞掉丟棄

    
    
    
    private final MaintenanceInformationRepository mtifRepo;
    private final MaintenanceStaffRepository staffRepo;

    //新增租借點 Repository，用來驗證 spotId 是否存在
    private final RentalSpotRepository rentalSpotRepo;
    private final SeatRepository seatRepo;
    private final MaintenanceLogRepository logRepo;

    public MaintenanceInformationService(MaintenanceInformationRepository mtifRepo,
                                         MaintenanceStaffRepository staffRepo,
                                         RentalSpotRepository rentalSpotRepo,
                                         SeatRepository seatRepo,
                                         MaintenanceLogRepository logRepo) {
        this.mtifRepo = mtifRepo;
        this.staffRepo = staffRepo;
        this.rentalSpotRepo = rentalSpotRepo;
        this.seatRepo = seatRepo;
        this.logRepo = logRepo;
    }

    //1.取得所有椅子
    public List<Seat> getAllSeats(){
        return seatRepo.findAll();
        
    }

    //2.取得指定場地的椅子
    public List<Seat> getSeatsBySpot(Integer spotId){
        return seatRepo.findBySpotId(spotId);
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

    // ★ 問題B修復：移除 filter，返回所有 spot（前端負責禁用非營運中）
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
                // .filter(s ->SPOT_STATUS_OPERATIONAL.equals(s.getSpotStatus())) // 移除此行
                .sorted((a, b) -> Integer.compare(a.getSpotId(), b.getSpotId()))
                .toList();
    }

    // ============ 建立 / 更新 / 刪除 ============

    public MaintenanceInformation createTicket(MaintenanceInformation mtif) {
        // 1. 強制清空 ID，防止前端意外傳入 ID 導致 JPA 變成 Update
        mtif.setTicketId(null);
        
        validateForCreate(mtif);
        validateAssignedStaff(mtif.getAssignedStaffId());

        //  2. 資料淨化 (Trim)
        mtif.setIssueType(mtif.getIssueType().trim());
        
        if (!isBlank(mtif.getIssueDesc())) {
            mtif.setIssueDesc(mtif.getIssueDesc().trim());
        } else {
            mtif.setIssueDesc(null); // 空白轉 null
        }

        //  3. 處理優先權 (預設值 + 驗證 + Trim)
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

        MaintenanceInformation saved = mtifRepo.save(mtif);
        
        // ★ 記錄工單建立歷程
        String operator = "系統管理員"; // 實際應從 Spring Security 取得當前使用者
        String comment = String.format("建立工單 | 類型: %s | 優先權: %s", 
                                       saved.getIssueType(), saved.getIssuePriority());
        saveLog(saved, operator, "CREATED", comment);
        
        return saved;
    }

    public MaintenanceInformation updateTicket(MaintenanceInformation mtif) {
        if (mtif.getTicketId() == null) throw new IllegalArgumentException("更新工單時 ticketId 為必填欄位");
        // spotId 不再更新，移除 setSpotId
        if (isBlank(mtif.getIssueType())) throw new IllegalArgumentException("issueType 為必填欄位");

        MaintenanceInformation existing = getRequiredTicket(mtif.getTicketId());

        //  4. 狀態鎖定：已結案/已取消/維修中 不允許修改基本資料
        String currentStatus = existing.getIssueStatus();
        if (STATUS_RESOLVED.equals(currentStatus) || STATUS_CANCELLED.equals(currentStatus)) {
            throw new IllegalStateException("工單已結案或取消，不允許修改內容");
        }
        if (STATUS_UNDER_MAINTENANCE.equals(currentStatus)) {
            throw new IllegalStateException("維修中不允許修改工單基本資訊");
        }

        // ★ 任務2：檢測 assignedStaffId 是否變更
        Integer oldStaffId = existing.getAssignedStaffId();
        Integer newStaffId = mtif.getAssignedStaffId();
        boolean staffChanged = !java.util.Objects.equals(oldStaffId, newStaffId) && newStaffId != null;

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

        // ★ 任務2：更新指派人員
        if (newStaffId != null) {
            validateAssignedStaff(newStaffId);
            existing.setAssignedStaffId(newStaffId);
        }

        // 儲存更新
        MaintenanceInformation saved = mtifRepo.save(existing);

        // ★ 任務2：如果指派人員有變更，記錄 Log 並可能更新狀態
        if (staffChanged) {
            // 查詢新負責人的姓名
            String staffName = staffRepo.findById(newStaffId)
                    .map(MaintenanceStaff::getStaffName)
                    .orElse("未指定");

            // 如果當前狀態是 REPORTED，更新為 ASSIGNED
            if (STATUS_REPORTED.equals(saved.getIssueStatus())) {
                saved.setIssueStatus(STATUS_ASSIGNED);
                saved = mtifRepo.save(saved);
            }

            // 記錄指派歷程
            saveLog(saved, "System", "ASSIGNED", "已指派負責人：" + staffName);
        }

        return saved;
    }

    //
    public void deleteTicket(int ticketId) {
        if (!mtifRepo.existsById(ticketId)) {
            throw new IllegalArgumentException("找不到指定的維修工單，ticketId = " + ticketId);
        }
        mtifRepo.deleteById(ticketId);
    }

    // 取消工單
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
        
        // ★ 記錄取消歷程
        saveLog(mtif, "系統管理員", "CANCELLED", "取消原因: " + 
                (isBlank(cancelReason) ? "未提供" : cancelReason));
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

    //  5. 修改：使用排序過的 findAll
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
        
        // ★ 記錄指派歷程
        if (staffId != null) {
            MaintenanceStaff staff = staffRepo.findById(staffId).orElse(null);
            String staffName = (staff != null) ? staff.getStaffName() : "未知";
            saveLog(mtif, "系統管理員", "ASSIGNED", "指派給維修人員: " + staffName);
        }
    }

    /**
     * 開始維修
     * 1. 檢查狀態與指派
     * 2. 更新工單狀態為 UNDER_MAINTENANCE
     * 3. [新增] 同步更新 RentalSpot 和 Seat的狀態
     */

    public void startTicket(int ticketId) {
        MaintenanceInformation mtif = getRequiredTicket(ticketId);
        String status = mtif.getIssueStatus();

        if (!STATUS_REPORTED.equals(status) && !STATUS_ASSIGNED.equals(status)) {
            throw new IllegalStateException("目前狀態無法開始維修，ticketId = " + ticketId + "，status = " + status);
        }

        if (mtif.getAssignedStaffId() == null) {
            throw new IllegalStateException("尚未指派維修人員，無法開始維修");
        }

        // 1. 更新工單狀態

        if (mtif.getStartAt() == null) {
            mtif.setStartAt(LocalDateTime.now());
        }
        mtif.setIssueStatus(STATUS_UNDER_MAINTENANCE);

        // 2.  鎖定資源狀態 (分開判斷)
        // 如果有機台，更新機台狀態
        if (mtif.getSpotId() != null) {
            RentalSpot spot = rentalSpotRepo.findById(mtif.getSpotId())
                    .orElseThrow(() -> new IllegalArgumentException("找不到指定的租借點 " + mtif.getSpotId()));
            spot.setSpotStatus(SPOT_STATUS_MAINTENANCE);
            rentalSpotRepo.save(spot);
        }

        // 如果有椅子，更新椅子狀態
        if (mtif.getSeatsId() != null) {
            // 注意：這裡假設您的 Entity getter 是 getSeatsId()，若為 getSeatId() 請自行調整
            Seat seat = this.seatRepo.findById(mtif.getSeatsId())
                    .orElseThrow(() -> new IllegalArgumentException("找不到指定的椅子 " + mtif.getSeatsId()));

            seat.setSeatsStatus(SEAT_STATUS_REPAIRING); // 將椅子狀態設為維修中
            this.seatRepo.save(seat);
      
            }
        mtifRepo.save(mtif); // 更新工單狀態
        
        // ★ 記錄開始維修歷程
        MaintenanceStaff staff = staffRepo.findById(mtif.getAssignedStaffId()).orElse(null);
        String staffName = (staff != null) ? staff.getStaffName() : "未知";
        saveLog(mtif, staffName, "STARTED", "開始進行維修作業");
    }


    /**
     * 結案
     * 1. 檢查狀態 (需為維修中)
     * 2. 更新工單狀態為 RESOLVED
     * 3. 檢查該場地是否還有「其他未結案」的工單
     * 4. 若有：維持「維護中」(不變)
     * 5. 若無：更新場地狀態為「營運中」
     */

    /**
     * 結案
     * 1. 檢查狀態 (需為維修中)
     * 2. 更新工單狀態為 RESOLVED
     * 3. 檢查資源 (機台/椅子) 是否還有其他未結案工單
     * 4. 若無其他工單，才釋放資源回「營運中/正常」
     */
    /**
     * 結案 (嚴謹版)
     * 1. 檢查狀態 (需為維修中)
     * 2. 更新工單狀態為 RESOLVED
     * 3. 檢查資源 (機台/椅子) 是否還有其他未結案工單
     * 4. 若無其他工單，才釋放資源回「營運中/正常」
     */
    public void resolveTicket(int ticketId, String resultType, String resolveNote) {
        if (isBlank(resultType)) throw new IllegalArgumentException("維修結果是必填欄位");
        if (!isValidResultType(resultType)) throw new IllegalArgumentException("錯誤的結果類型：" + resultType);

        MaintenanceInformation mtif = getRequiredTicket(ticketId);

        // 狀態檢查
        if (!STATUS_UNDER_MAINTENANCE.equals(mtif.getIssueStatus())) {
            throw new IllegalStateException("只有維修中的工單可以結案");
        }

        // 1. 更新工單狀態
        mtif.setResolvedAt(LocalDateTime.now());
        mtif.setResultType(resultType);
        mtif.setResolveNote(resolveNote);
        mtif.setIssueStatus(STATUS_RESOLVED);
        mtifRepo.save(mtif); // 先存檔，確保這張單已標記完成。
        
        // ★ 記錄結案歷程
        MaintenanceStaff staff = staffRepo.findById(mtif.getAssignedStaffId()).orElse(null);
        String staffName = (staff != null) ? staff.getStaffName() : "未知";
        String comment = String.format("維修完成 | 結果: %s | 備註: %s", 
                                       resultType, resolveNote != null ? resolveNote : "無");
        saveLog(mtif, staffName, "RESOLVED", comment);

        // 定義哪些狀態算是「佔用中/未完成」
        List<String> activeStatuses = Arrays.asList(STATUS_REPORTED, STATUS_ASSIGNED, STATUS_UNDER_MAINTENANCE);

        // 2. 釋放資源邏輯

        // (A) 處理機台釋放
        if (mtif.getSpotId() != null) {
            List<MaintenanceInformation> spotTickets = getTicketsBySpotId(mtif.getSpotId());
            boolean hasOtherActiveTickets = spotTickets.stream()
                    .filter(t -> !t.getTicketId().equals(ticketId)) // 排除自己
                    .anyMatch(t -> activeStatuses.contains(t.getIssueStatus())); // 檢查有無其他未完成

            if (!hasOtherActiveTickets) {
                RentalSpot spot = rentalSpotRepo.findById(mtif.getSpotId())
                        .orElseThrow(() -> new IllegalArgumentException("找不到指定租借點 " + mtif.getSpotId()));
                spot.setSpotStatus(SPOT_STATUS_OPERATIONAL); // 恢復營運
                rentalSpotRepo.save(spot);
            }
        }

        // (B) [修改] 處理椅子釋放 (嚴謹模式)
        if (mtif.getSeatsId() != null) {
            // 1. 使用剛剛在 Repository 新增的方法，查這張椅子的所有工單
            List<MaintenanceInformation> seatTickets = mtifRepo.findBySeatsIdOrderByReportedAtDescTicketIdAsc(mtif.getSeatsId());
            
            // 2. 檢查是否還有其他「未完成」的工單 (排除自己)
            boolean hasOtherActiveTickets = seatTickets.stream()
                    .filter(t -> !t.getTicketId().equals(ticketId)) 
                    .anyMatch(t -> activeStatuses.contains(t.getIssueStatus()));

            // 3. 只有在「沒有」其他未完成工單時，才把椅子變回正常
            if (!hasOtherActiveTickets) {
                Seat seat = this.seatRepo.findById(mtif.getSeatsId())
                        .orElseThrow(() -> new IllegalArgumentException("找不到指定的椅子 " + mtif.getSeatsId()));
                
                seat.setSeatsStatus(SEAT_STATUS_NORMAL); // 恢復為正常
                this.seatRepo.save(seat);
            }
        }
    }
    

    // ============ 驗證 ============

    private void validateForCreate(MaintenanceInformation mtif) {
        if (mtif == null) throw new IllegalArgumentException("維修工單不能為空白");

        boolean hasSpot = (mtif.getSpotId() != null && mtif.getSpotId() > 0);
        boolean hasSeat = (mtif.getSeatsId() != null && mtif.getSeatsId() > 0);

        if (!hasSpot && !hasSeat) {
            throw new IllegalArgumentException("工單必須指定「維修場地(Spot)」或「維修椅子(Seat)」其中之一");
        }
        
        if (isBlank(mtif.getIssueType())) throw new IllegalArgumentException("issueType 為必填欄位");

        // 檢查 ID 是否存在
        if (hasSpot && !rentalSpotRepo.existsById(mtif.getSpotId())) {
            throw new IllegalArgumentException("找不到指定的租借點 " + mtif.getSpotId());
        }
        
        // [新增] 檢查椅子是否存在
        if (hasSeat && !this.seatRepo.existsById(mtif.getSeatsId())) {
            throw new IllegalArgumentException("找不到指定的椅子 " + mtif.getSeatsId());
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

    //  6. 新增優先權驗證
    private boolean isValidPriority(String priority) {
        if (isBlank(priority)) return false;
        return Arrays.asList(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT)
                     .contains(priority);
    }

    /**
     * 統一記錄工單歷程
     * @param ticket   工單物件
     * @param operator 操作者 (員工姓名或帳號)
     * @param action   動作代號 (CREATED, ASSIGNED, STARTED, RESOLVED, CANCELLED, URGENT)
     * @param comment  備註說明 (可為 null)
     */
    private void saveLog(MaintenanceInformation ticket, String operator, String action, String comment) {
        if (ticket == null || isBlank(operator) || isBlank(action)) {
            return; // 防禦性檢查，避免記錄空白資料
        }
        
        MaintenanceLog log = new MaintenanceLog(ticket, operator.trim(), action.trim(), comment);
        logRepo.save(log);
    }

    /**
     * 取得指定工單的完整歷程記錄 (按時間倒序)
     * @param ticketId 工單 ID
     * @return DTO List (已格式化時間、去除敏感資訊)
     */
    @Transactional(readOnly = true)
    public List<MaintenanceLogResponseDto> getTicketLogs(Integer ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("ticketId 不可為空");
        }
        
        // 確認工單存在
        if (!mtifRepo.existsById(ticketId)) {
            throw new IllegalArgumentException("找不到指定的工單，ticketId = " + ticketId);
        }
        
        List<MaintenanceLog> logs = logRepo.findByTicketTicketIdOrderByCreatedAtDesc(ticketId);
        
        return logs.stream()
                   .map(log -> new MaintenanceLogResponseDto(
                       log.getLogId(),
                       log.getOperator(),
                       log.getAction(),
                       log.getComment(),
                       log.getCreatedAt()
                   ))
                   .toList();
    }

    
}
