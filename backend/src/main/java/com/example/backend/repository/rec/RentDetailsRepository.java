package com.example.backend.repository.rec;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.rec.RentDetails;

public interface RentDetailsRepository
                extends JpaRepository<RentDetails, String>, JpaSpecificationExecutor<RentDetails> {

        // JPQL查詢，用於按月統計指定時間範圍內的訂單數量
        // 返回Object[]而不是DTO，以避免JPA實現中的構造函數類型匹配問題
        @Query("SELECT FUNCTION('YEAR', r.recRentDT2), FUNCTION('MONTH', r.recRentDT2), COUNT(r) " +
                        "FROM RentDetails r " +
                        "WHERE r.recRentDT2 BETWEEN :startDate AND :endDate " +
                        "GROUP BY FUNCTION('YEAR', r.recRentDT2), FUNCTION('MONTH', r.recRentDT2) " +
                        "ORDER BY FUNCTION('YEAR', r.recRentDT2), FUNCTION('MONTH', r.recRentDT2)")
        List<Object[]> findMonthlyOrderCounts(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
