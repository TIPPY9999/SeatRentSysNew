package com.example.backend.controller.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.member.Member;
import com.example.backend.service.member.MemberService;

@CrossOrigin(origins = "http://localhost:5173") // 確保跨域沒問題
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 查全部
    @GetMapping
    public List<Member> findAll() {
        return memberService.findAll();
    }

    // 查單筆
    @GetMapping("/find")
    public ResponseEntity<?> findOne(@RequestParam(required = false) Integer memId) {
        if (memId == null) {
            return ResponseEntity.badRequest().body("請輸入 memId");
        }

        Member member = memberService.findById(memId);

        if (member != null) {
            // --- 暴力拆解法：只傳需要的欄位，避開所有可能導致 500 的複雜物件 ---
            Map<String, Object> response = new HashMap<>();
            response.put("memId", member.getMemId());
            response.put("memPoints", member.getMemPoints());
            response.put("memName", member.getMemName());
            response.put("memUsername", member.getMemUsername());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body("查無此會員");
        }
    }

    // 新增
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Member member) {
        try {
            memberService.insert(member);
            return ResponseEntity.ok("會員新增成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 修改
    @PostMapping("/update")
    public String update(@RequestBody Member member) {

        // 先查舊資料
        Member old = memberService.findById(member.getMemId());
        if (old == null) {
            return "查無此會員";
        }

        // 更新「非密碼」欄位
        old.setMemUsername(member.getMemUsername());
        old.setMemName(member.getMemName());
        old.setMemEmail(member.getMemEmail());
        old.setMemPhone(member.getMemPhone());
        old.setMemStatus(member.getMemStatus());
        old.setMemPoints(member.getMemPoints());
        old.setMemViolation(member.getMemViolation());
        old.setMemLevel(member.getMemLevel());
        old.setMemInvoice(member.getMemInvoice());

        String newPassword = member.getMemPassword();
        if (newPassword != null && newPassword.isBlank()) {
            newPassword = null;
        }

        // 呼叫 Service（Service 本身不用改）
        memberService.update(old, newPassword);

        return "會員修改成功";
    }

    // 刪除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer memId) {
        memberService.deleteById(memId);
        return "會員刪除成功（memId=" + memId + "）";
    }

    // 模糊查詢
    @GetMapping("/search")
    public Object search(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "請輸入搜尋關鍵字";
        }
        return memberService.search(keyword);
    }

    // 會員註冊
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            memberService.insert(member);
            return ResponseEntity.ok("註冊成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}