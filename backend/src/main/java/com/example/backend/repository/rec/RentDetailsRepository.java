package com.example.backend.repository.rec;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.rec.RentDetails;

@Repository
public interface RentDetailsRepository extends JpaRepository<RentDetails, String> {

    // 依據會員ID搜尋
    List<RentDetails> findByMemId(Integer memId);

    // 依據會員姓名模糊搜尋 (WHERE memName LIKE %name%)
    List<RentDetails> findByMemNameContaining(String memName);

    // 依據站點ID搜尋 (WHERE staId = id)
    List<RentDetails> findBySpotIdRent(Integer spotId);

    // 依據站點名稱模糊搜尋 (WHERE staName LIKE %name%)
    List<RentDetails> findByRentSpotNameContaining(String spotName);

    // 依據訂單狀態搜尋
    List<RentDetails> findByRecStatus(String rentStatus);

    // 依據租借日期搜尋
    List<RentDetails> findByRecRentDT2(LocalDateTime rentDate);

    // 依據歸還日期搜尋
    List<RentDetails> findByRecReturnDT2(LocalDateTime returnDate);

}
