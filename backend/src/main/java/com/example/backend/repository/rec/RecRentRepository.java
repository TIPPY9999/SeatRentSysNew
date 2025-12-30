package com.example.backend.repository.rec;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.rec.RecRent;

@Repository
public interface RecRentRepository extends JpaRepository<RecRent, Integer> {
    // 支援用業務主鍵 (Rxxxxxxxxx) 查詢
    RecRent findByRecId(String recId);
}