package com.example.backend.repository.maintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.maintenance.MaintenanceStaff;

public interface MaintenanceStaffRepository extends JpaRepository<MaintenanceStaff, Integer> {

    boolean existsByStaffEmailIgnoreCase(String staffEmail);

    MaintenanceStaff findByStaffEmail(String staffEmail);

    List<MaintenanceStaff> findByStaffCompany(String staffCompany);

    List<MaintenanceStaff> findByIsActiveTrueOrderByStaffIdAsc();

    List<MaintenanceStaff> findByIsActiveFalseOrderByStaffIdAsc();
    
}
