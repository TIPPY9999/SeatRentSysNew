package com.example.backend.repository.rec;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.rec.RentDetails;

@Repository
public interface RentDetailsRepository extends JpaRepository<RentDetails, String> {

    // 依據會員姓名模糊搜尋 (WHERE memName LIKE %name%)
    List<RentDetails> findByMemNameContaining(String memName);

}
