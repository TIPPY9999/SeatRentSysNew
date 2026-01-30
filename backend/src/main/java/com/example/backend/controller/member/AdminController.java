package com.example.backend.controller.member;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.model.member.Admin;
import com.example.backend.service.member.AdminService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 查全部
    @GetMapping
    public List<Admin> findAll() {
        return adminService.findAll();
    }

    // 查單筆
    @GetMapping("/find")
    public Object findOne(@RequestParam(required = false) Integer admId) {

        if (admId == null) {
            return "請輸入 admId";
        }

        Admin admin = adminService.findById(admId);
        return admin != null ? admin : "查無此管理員";
    }

    // 新增
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Admin admin) {
        try {
            adminService.insert(admin);
            return ResponseEntity.ok("管理員新增成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 修改
    @PostMapping("/update")
    public String update(@RequestBody Admin admin) {

        Admin old = adminService.findById(admin.getAdmId());
        if (old == null) {
            return "查無此管理員";
        }

        old.setAdmUsername(admin.getAdmUsername());
        old.setAdmName(admin.getAdmName());
        old.setAdmEmail(admin.getAdmEmail());
        old.setAdmRole(admin.getAdmRole());
        old.setAdminImage(admin.getAdminImage());

        // 密碼：有傳才更新
        if (admin.getAdmPassword() != null && !admin.getAdmPassword().isBlank()) {
            old.setAdmPassword(admin.getAdmPassword());
        }

        adminService.update(old);
        return "管理員修改成功";
    }

    // 刪除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer admId) {
        adminService.deleteById(admId);
        return "管理員刪除成功（admId=" + admId + "）";
    }

    // 模糊查詢
    @GetMapping("/search")
    public List<Admin> search(@RequestParam String keyword) {
        return adminService.findByKeyword(keyword);
    }

    // 停權
    @GetMapping("/disable")
    public String disable(@RequestParam Integer admId) {

        Admin admin = adminService.findById(admId);
        if (admin == null) {
            return "查無此管理員";
        }

        admin.setAdmStatus(0);
        adminService.update(admin);

        return "管理員已停權（admId=" + admId + "）";
    }

    // 啟用
    @GetMapping("/enable")
    public String enable(@RequestParam Integer admId) {

        Admin admin = adminService.findById(admId);
        if (admin == null) {
            return "查無此管理員";
        }

        admin.setAdmStatus(1);
        adminService.update(admin);

        return "管理員已啟用（admId=" + admId + "）";
    }
}