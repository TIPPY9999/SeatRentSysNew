package com.example.backend.controller.maintenance;

import com.example.backend.dto.maintenance.SpotOptionDto;
import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.model.spot.Seat;
import com.example.backend.service.maintenance.MaintenanceInformationService;
import com.example.backend.service.maintenance.MaintenanceStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "http://localhost:5173") // 允許 Vue 前端呼叫
public class MaintenanceController {

    @Autowired
    private MaintenanceStaffService staffService;

    @Autowired
    private MaintenanceInformationService mtifService;

    @GetMapping("/spots")
    public List<SpotOptionDto> getSpotOptions() {
        return mtifService.getSpotOptions();
    }

    // ================== 維護人員 (Staff) ==================

    // 取得所有維護人員 (前端下拉選單用)
    @GetMapping("/staff")
    public List<MaintenanceStaff> getAllStaff() {
        return staffService.getAllStaff();
    }

    // ★ 新增：建立維護人員
    @PostMapping("/staff")
    public MaintenanceStaff createStaff(@RequestBody MaintenanceStaff staff) {
        return staffService.createStaff(staff);
    }

    // 新增：更新維護人員資料
    @PutMapping("/staff/{id}")
    public MaintenanceStaff updateStaff(@PathVariable Integer id, @RequestBody MaintenanceStaff staff) {
        // 確保路徑上的 ID 跟物件裡的 ID 一樣，避免改錯人
        staff.setStaffId(id);
        return staffService.updateStaff(staff);
    }

    // ★ 新增：刪除維護人員 (軟刪除)
    @DeleteMapping("/staff/{id}")
    public void deleteStaff(@PathVariable Integer id) {
        staffService.deleteStaff(id);
    }

    // ================== 工單查詢 (Read) ==================

    // 1. 取得「待處理」工單 (Active: Reported / Assigned / Under Maintenance)
    @GetMapping("/tickets/active")
    public List<MaintenanceInformation> getActiveTickets() {
        return mtifService.getActiveTickets();
    }

    // 2. 取得「歷史」工單 (History: Resolved / Cancelled)
    @GetMapping("/tickets/history")
    public List<MaintenanceInformation> getHistoryTickets() {
        return mtifService.getHistoryTickets();
    }

    // 3. 取得「全部」工單 (後台管理用)
    @GetMapping("/tickets")
    public List<MaintenanceInformation> getAllTickets() {
        return mtifService.getAllTickets();
    }

    // 4. 依照 SpotId 查詢工單 (看某個機台的維修紀錄)
    @GetMapping("/tickets/spot/{spotId}")
    public List<MaintenanceInformation> getTicketsBySpot(@PathVariable Integer spotId) {
        return mtifService.getTicketsBySpotId(spotId);
    }

    // 5. 取得單張工單詳情 (編輯或查看細節用)
    @GetMapping("/tickets/{id}")
    public MaintenanceInformation getTicketById(@PathVariable Integer id) {
        return mtifService.getRequiredTicket(id);
    }

    // ================== 工單操作 (Create / Update) ==================

    // 6. 新增工單
    // 前端傳送 JSON: { "spotId": 1, "issueType": "硬體", ... }
    @PostMapping("/tickets")
    public MaintenanceInformation createTicket(@RequestBody MaintenanceInformation mtif) {
        return mtifService.createTicket(mtif);
    }

    // 7. 更新工單 (修改問題描述、優先級等)
    @PutMapping("/tickets/{id}")
    public MaintenanceInformation updateTicket(@PathVariable Integer id, 
                                               @RequestBody MaintenanceInformation mtif) {
        // 確保 PathVariable 的 ID 與物件內的 ID 一致
        mtif.setTicketId(id);
        return mtifService.updateTicket(mtif);
    }


    // 1. 依 ID 查詢單一維護人員 (編輯表單用)
    @GetMapping("/staff/{id}")
    public MaintenanceStaff getStaffById(@PathVariable Integer id) {
        return staffService.getRequiredStaff(id);
    }

    // 2. 查詢已停用的維護人員 (歷史紀錄用)
    @GetMapping("/staff/inactive")
    public List<MaintenanceStaff> getInactiveStaff() {
        return staffService.getInactiveStaff();
    }

    // ================== 流程控制 (State Changes) ==================

    // 8. 指派人員
    // 前端傳送 JSON: { "staffId": 5 }  (若不指派傳 null)
    @PostMapping("/tickets/{id}/assign")
    public void assignStaff(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Integer staffId = body.get("staffId");
        mtifService.assignStaff(id, staffId);
    }

    // 9. 開始維修
    // 前端不需要傳 body，只要呼叫這個 API
    @PostMapping("/tickets/{id}/start")
    public void startTicket(@PathVariable Integer id) {
        mtifService.startTicket(id);
    }

    // 10. 結案 (完成維修)
    // 前端傳送 JSON: { "resultType": "FIXED", "resolveNote": "換了零件" }
    @PostMapping("/tickets/{id}/resolve")
    public void resolveTicket(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String resultType = body.get("resultType");
        String resolveNote = body.get("resolveNote");
        mtifService.resolveTicket(id, resultType, resolveNote);
    }

    // 11. 取消工單
    // 前端傳送 JSON: { "reason": "客戶說修好了" }
    @PostMapping("/tickets/{id}/cancel")
    public void cancelTicket(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        mtifService.cancelTicket(id, reason);
    }

    //轉移工單並刪除人員
    @PostMapping("/staff/transfer-and-delete")
    public void transferAndDelete(@RequestBody Map<String, Integer> body) {
        Integer targetStaffId = body.get("targetStaffId");
        Integer deleteStaffId = body.get("deleteStaffId");
        staffService.transferAndDelete(targetStaffId, deleteStaffId);
    }


    //============ 椅子相關 API ============
    // 1. 取得所有椅子
    @GetMapping("/seats")
    public List<Seat> getAllSeats() {
        return mtifService.getAllSeats();
    }

    // 2. 依照 SpotId 篩選椅子 (選了機台後，只顯示該機台的椅子)
    @GetMapping("/seats/spot/{spotId}")
    public List<Seat> getSeatsBySpot(@PathVariable Integer spotId) {
        return mtifService.getSeatsBySpot(spotId);
    }

}