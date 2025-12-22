package com.example.backend.controller.maintenance;

import com.example.backend.model.maintenance.MaintenanceInformation;
import com.example.backend.model.maintenance.MaintenanceStaff;
import com.example.backend.repository.maintenance.MaintenanceInformationRepository;
import com.example.backend.service.maintenance.MaintenanceStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController // 1. 告訴 Spring 這是一個 API 入口
@RequestMapping("/api/maintenance") // 2. 設定這個控制器的「總機號碼」
@CrossOrigin(origins = "http://localhost:5173") // 3. 允許前端 Vue 來呼叫 (這很重要！)
public class MaintenanceController {

    @Autowired
    private MaintenanceStaffService maintenanceStaffService;

    // 網址: http://localhost:8080/api/maintenance/staff
    @GetMapping("/staff")
    public List<MaintenanceStaff> getAllStaff() {
        return maintenanceStaffService.getAllStaff(); // 呼叫 Service 拿資料
    }
    
    // 網址: http://localhost:8080/api/maintenance/test
    @GetMapping("/test")
    public String test() {
        return "Backend is working!";
    }
@Autowired
private MaintenanceInformationRepository infoRepo;
    @GetMapping("/tickets")
public List<MaintenanceInformation> getAllTickets() {
    return infoRepo.findAll();
}
}