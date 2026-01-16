package com.example.backend.repository.spot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.spot.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer>, JpaSpecificationExecutor<Seat> {

    // [修正] 補上 Service 層呼叫的模糊查詢方法
    @Query("SELECT s FROM Seat s WHERE s.seatsName LIKE %:keyword% OR s.serialNumber LIKE %:keyword% OR s.seatsType LIKE %:keyword%")
    List<Seat> findByKeyword(@Param("keyword") String keyword);

    /**
     * 檢查指定的 serialNumber 是否已存在於資料庫中。
     * 
     * @param serialNumber 要檢查的序號
     * @return 如果存在則返回 true，否則返回 false。
     */
    boolean existsBySerialNumber(String serialNumber);
}