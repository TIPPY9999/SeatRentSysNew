package com.example.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.Member;
import com.example.backend.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginMemberController {

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/member")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
            HttpSession session) {

        String username = body.get("memUsername");
        String password = body.get("memPassword");

        // 基本防呆
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("請輸入帳號與密碼");
        }

        // 查詢會員
        Member member = memberRepository.findByMemUsername(username);

        // 帳號不存在
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        // 密碼比對
        if (!password.equals(member.getMemPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        // 登入成功，存 session
        session.setAttribute("loginMember", member);

        // 回成功訊息
        return ResponseEntity.ok("登入成功");
    }
}
