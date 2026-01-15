package com.example.backend.controller.member;

import org.springframework.web.bind.annotation.*;

import com.example.backend.model.member.Member;
import com.example.backend.repository.member.MemberRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/member")
public class MemberProfileController {

    private final MemberRepository memberRepository;

    public MemberProfileController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 取得會員資料
    @GetMapping("/profile")
    public Object getProfile(HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "尚未登入";
        }

        return loginMember;
    }

    // 更新會員資料
    @PutMapping("/profile")
    public Object updateProfile(
            @RequestBody Member formMember,
            HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "尚未登入";
        }

        // ===== 格式驗證 =====

        // Email：xxx@xxx.com
        if (formMember.getMemEmail() == null ||
                !formMember.getMemEmail().matches("^[A-Za-z0-9+_.-]+@.+\\.com$")) {
            return "Email 格式錯誤";
        }

        // 手機：09 開頭，共 10 碼
        if (formMember.getMemPhone() == null ||
                !formMember.getMemPhone().matches("^09\\d{8}$")) {
            return "手機格式錯誤";
        }

        // 發票載具：/ 開頭，共 8 個字
        if (formMember.getMemInvoice() != null &&
                !formMember.getMemInvoice().matches("^/.{7}$")) {
            return "發票載具格式錯誤";
        }

        // ===== 更新資料 =====
        loginMember.setMemName(formMember.getMemName());
        loginMember.setMemPhone(formMember.getMemPhone());
        loginMember.setMemEmail(formMember.getMemEmail());
        loginMember.setMemInvoice(formMember.getMemInvoice());

        memberRepository.save(loginMember);

        // 更新 session
        session.setAttribute("loginMember", loginMember);

        return "更新成功";
    }
}