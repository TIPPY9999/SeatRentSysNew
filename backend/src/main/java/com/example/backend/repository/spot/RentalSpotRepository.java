package com.example.backend.repository.spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.model.spot.RentalSpot;

// [移除原因] 重複的註解，保留下方帶有詳細說明的版本即可
// @Repository
@Repository // [註解] 標記這是據點資料的倉庫管理員
public interface RentalSpotRepository extends JpaRepository<RentalSpot, Integer>, JpaSpecificationExecutor<RentalSpot> {
    // [白話文說明]
    // 1. JpaRepository<RentalSpot, Integer>:
    // 讓這個介面直接擁有對 RentalSpot 資料表進行 CRUD (增刪改查) 的能力。
    // Spring 會自動幫我們產生 "SELECT * FROM rental_spot ..." 等 SQL 語法。

    // 2. JpaSpecificationExecutor<RentalSpot>:
    // 讓我們能處理前端傳來的「多重條件查詢」(例如：同時查 據點代碼 + 據點名稱)。
}