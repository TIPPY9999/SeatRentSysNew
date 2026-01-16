package com.example.backend.controller.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.model.member.Admin;
import com.example.backend.repository.member.AdminRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class LoginAdminController {

    private final AdminRepository adminRepository;

    public LoginAdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @PostMapping("/admin")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
            HttpSession session) {

        String username = body.get("admUsername");
        String password = body.get("admPassword");

        // 防呆
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("請輸入帳號與密碼");
        }

        Admin admin = adminRepository.findByAdmUsername(username);

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        if (!password.equals(admin.getAdmPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        // 停權判斷
        if (admin.getAdmStatus() != null && admin.getAdmStatus() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("帳號已停權，請聯絡管理員");
        }

        // 登入成功
        session.setAttribute("loginAdmin", admin);

        // 回傳給前端用的資料
        // 使用 HashMap 避免 Map.of 因欄位為 null 而拋出 NullPointerException
        Map<String, Object> result = new HashMap<>();
        result.put("admUsername", admin.getAdmUsername());
        result.put("admName", admin.getAdmName());
        result.put("admRole", admin.getAdmRole());

        return ResponseEntity.ok(result);
    }
}
