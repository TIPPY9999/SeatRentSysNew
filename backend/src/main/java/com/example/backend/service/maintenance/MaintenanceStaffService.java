package com.example.backend.service.maintenance;

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

        staff.setStaffName(staff.getStaffName().trim());
        if (!isBlank(staff.getStaffEmail())) {
            staff.setStaffEmail(staff.getStaffEmail().trim());
        } else {
            staff.setStaffEmail(null); // 避免存空白字串
        }

        if (staff.getIsActive() == null) {
            staff.setIsActive(true);
        }

        return staffRepo.save(staff);
    }

    public MaintenanceStaff updateStaff(MaintenanceStaff staff) {
        validateForUpdate(staff);

        MaintenanceStaff existing = getRequiredStaff(staff.getStaffId());

        // 1. Name 必填，直接 trim
        existing.setStaffName(staff.getStaffName().trim());
        
        // 2. 其他選填欄位：如果前端傳空字串，代表要「清空」，所以要 set(null)
        if (!isBlank(staff.getStaffCompany())) {
            existing.setStaffCompany(staff.getStaffCompany().trim());
        } else {
            existing.setStaffCompany(null);
        }

        if (!isBlank(staff.getStaffPhone())) {
            existing.setStaffPhone(staff.getStaffPhone().trim());
        } else {
            existing.setStaffPhone(null);
        }

        if (!isBlank(staff.getStaffNote())) {
            existing.setStaffNote(staff.getStaffNote().trim());
        } else {
            existing.setStaffNote(null);
        }

        // 3. Email 特殊處理 (Trim + 空字串轉 null + 唯一性在 validate 已檢查)
        if (!isBlank(staff.getStaffEmail())) {
            existing.setStaffEmail(staff.getStaffEmail().trim());
        } else {
            existing.setStaffEmail(null);
        }
        
        // UI 有開放改 isActive 可以在這裡 set
        if (staff.getIsActive() != null) {
            existing.setIsActive(staff.getIsActive());
        }

        return staffRepo.save(existing);
    }

    public void deleteStaff(int staffId) {
        MaintenanceStaff existing = getRequiredStaff(staffId);
        if (Boolean.FALSE.equals(existing.getIsActive())) {
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
        if (isBlank(staffCompany)) throw new IllegalArgumentException("廠商名稱不能為空白");
        return staffRepo.findByStaffCompany(staffCompany.trim());
    }

    private void validateForCreate(MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("維護人員資料不能為 null");
        if (isBlank(staff.getStaffName())) throw new IllegalArgumentException("維護人員姓名必填");

        if (!isBlank(staff.getStaffEmail())) {
            String email = staff.getStaffEmail().trim();
            if (staffRepo.existsByStaffEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("此 Email 已被使用");
            }
        }
    }

    private void validateForUpdate(MaintenanceStaff staff) {
        if (staff == null) throw new IllegalArgumentException("資料不能為 null");
        if (staff.getStaffId() == null) throw new IllegalArgumentException("ID 不能為 null");
        if (isBlank(staff.getStaffName())) throw new IllegalArgumentException("姓名必填");

        if (!isBlank(staff.getStaffEmail())) {
            String email = staff.getStaffEmail().trim();
            // 使用 IgnoreCase 確保大小寫視為相同
            MaintenanceStaff existing = staffRepo.findByStaffEmailIgnoreCase(email);
            
            // 如果找得到人，且那個人的 ID 不是我現在要修改的這個人 -> 代表跟別人重複了
            if (existing != null && !existing.getStaffId().equals(staff.getStaffId())) {
                throw new IllegalArgumentException("此 Email 已被其他維護人員使用");
            }
        }
    }
}