package com.example.backend.service.member;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.member.Member;
import com.example.backend.repository.member.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 密碼驗證
    private void validatePassword(String password) {
        String pwdRule = "^(?=.*[A-Za-z])[A-Za-z\\d!@#$%^&*()_+=\\[\\]{}:;\"'<>,.?/\\-]{6,}$";

        if (password == null || !password.matches(pwdRule)) {
            throw new IllegalArgumentException(
                    "密碼格式錯誤：至少6碼，需包含至少1個英文字母");
        }
    }

    // 查全部
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 查單筆
    public Member findById(Integer memId) {
        return memberRepository.findById(memId).orElse(null);
    }

    // 新增（InsertMember）
    public void insert(Member member) {
        validatePassword(member.getMemPassword());
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException("帳號或 Email 已被使用");
        }
    }

    // 修改（UpdateMember）
    public void update(Member old, String newPassword) {

        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            old.setMemPassword(newPassword);
        }

        memberRepository.save(old);
    }

    // 刪除（DeleteMember）
    public void deleteById(Integer memId) {
        memberRepository.deleteById(memId);
    }
}