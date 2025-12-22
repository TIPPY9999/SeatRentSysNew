package com.example.backend.service.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.repository.maintenance.MaintenanceStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaintenanceStaffService {

    private final MaintenanceStaffRepository staffRepo;

    public MaintenanceStaffService(MaintenanceStaffRepository staffRepo) {
        this.staffRepo = staffRepo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public MaintenanceStaff createStaff(MaintenanceStaff staff) {
        validateForCreate(staff);

        // 若你要強一點：統一 trim
        staff.setStaffName(staff.getStaffName().trim());
        if (!isBlank(staff.getStaffEmail())) {
            staff.setStaffEmail(staff.getStaffEmail().trim());
        }

        // DB 預設 isActive = 1，但 entity nullable=false；保險起見：
        if (staff.getIsActive() == null) {
            staff.setIsActive(true);
        }

        return staffRepo.save(staff);
    }

    public MaintenanceStaff updateStaff(MaintenanceStaff staff) {
        validateForUpdate(staff);

        MaintenanceStaff existing = getRequiredStaff(staff.getStaffId());

        existing.setStaffName(staff.getStaffName());
        existing.setStaffCompany(staff.getStaffCompany());
        existing.setStaffPhone(staff.getStaffPhone());
        existing.setStaffEmail(staff.getStaffEmail());
        existing.setStaffNote(staff.getStaffNote());
        // 不動 createdAt
        // 不一定要動 isActive（看你 UI 是否允許）

        return staffRepo.save(existing);
    }

    /** 軟刪除：isActive = false */
    public void deleteStaff(int staffId) {
        MaintenanceStaff existing = getRequiredStaff(staffId);

        if (Boolean.FALSE.equals(existing.getIsActive())) {
            // 已停用就當成功，或你要丟例外都可以
            return;
        }

        existing.setIsActive(false);
        staffRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public MaintenanceStaff getStaffById(int staffId) {
        return staffRepo.findById(staffId).orElse(null);
    }

    @Transactional(readOnly = true)
    public MaintenanceStaff getRequiredStaff(int staffId) {
        return staffRepo.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定的維護人員，staffId = " + staffId));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceStaff> getAllStaff() {
        return staffRepo.findByIsActiveTrueOrderByStaffIdAsc();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceStaff> getInactiveStaff() {
        return staffRepo.findByIsActiveFalseOrderByStaffIdAsc();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceStaff> getStaffByCompany(String staffCompany) {
        if (isBlank(staffCompany)) {
            throw new IllegalArgumentException("查詢維護人員時，廠商名稱不能為空白");
        }
        return staffRepo.findByStaffCompany(staffCompany.trim());
    }

    private void validateForCreate(MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("維護人員資料不能為 null");
        if (isBlank(staff.getStaffName())) throw new IllegalArgumentException("維護人員姓名 (staffName) 為必填欄位");

        if (!isBlank(staff.getStaffEmail())) {
            String email = staff.getStaffEmail().trim();
            // 你原本是 findByEmail，這裡用 exists 更快
            if (staffRepo.existsByStaffEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("此 Email 已被其他維護人員使用，請改用不同信箱");
            }
        }
    }

    private void validateForUpdate(MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("維護人員資料不能為 null");
        if (staff.getStaffId() == null) throw new IllegalArgumentException("更新維護人員時 staffId 不能為 null");
        if (isBlank(staff.getStaffName())) throw new IllegalArgumentException("維護人員姓名 (staffName) 為必填欄位");

        if (!isBlank(staff.getStaffEmail())) {
            String email = staff.getStaffEmail().trim();
            MaintenanceStaff existing = staffRepo.findByStaffEmail(email);
            if (existing != null && !existing.getStaffId().equals(staff.getStaffId())) {
                throw new IllegalArgumentException("此 Email 已被其他維護人員使用，請改用不同信箱");
            }
        }
    }
}
