package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    // 登入用（新增這行）
    Member findByMemUsername(String memUsername);

    // 模糊查詢
    @Query("""
                SELECT m FROM Member m
                WHERE m.memUsername LIKE %:kw%
                   OR m.memName LIKE %:kw%
                   OR m.memEmail LIKE %:kw%
                   OR m.memPhone LIKE %:kw%
            """)
    List<Member> findByKeyword(@Param("kw") String keyword);
}