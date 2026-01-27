package com.example.backend.repository.spot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.spot.RentalSpot;
import com.example.backend.repository.projection.AnalyzeProjections.SpotCountByCity;
import com.example.backend.repository.projection.AnalyzeProjections.SpotMonitor;

@Repository // [註解] 標記這是據點資料的倉庫管理員
public interface RentalSpotRepository extends JpaRepository<RentalSpot, Integer>, JpaSpecificationExecutor<RentalSpot> {

    // 自定義模糊查詢：搜尋 代碼、名稱 或 地址
    @Query("SELECT r FROM RentalSpot r WHERE r.spotCode LIKE %:keyword% OR r.spotName LIKE %:keyword% OR r.spotAddress LIKE %:keyword%")
    List<RentalSpot> findByKeyword(@Param("keyword") String keyword);

    /**
     * 檢查指定的 spotCode 是否已存在於資料庫中。
     * 
     * @param spotCode 要檢查的據點代碼
     * @return 如果存在則返回 true，否則返回 false。
     */
    boolean existsBySpotCode(String spotCode);

    // 需求 5: 站點分布於每一縣市之統計
    // 對應 Projection: SpotCountByCity (getCity, getSpotCount)
    @Query(value = """
                SELECT
                    SUBSTRING(spotAddress, 1, 3) as city,
                    COUNT(*) as spotCount
                FROM renting_Spot
                WHERE spotAddress IS NOT NULL
                GROUP BY SUBSTRING(spotAddress, 1, 3)
                ORDER BY spotCount DESC
            """, nativeQuery = true)
    List<SpotCountByCity> getCityDistribution();

    // 需求 2 & 6: 站點即時監控 (總座位 vs 已租借)
    // 對應 Projection: SpotMonitor (getSpotName, getTotalSeats, getRentedCount)
    @Query(value = """
                SELECT
                    s.spotName as spotName,
                    (SELECT COUNT(*) FROM seats st WHERE st.spotId = s.spotId AND st.seatsStatus = '啟用') as totalSeats,
                    (SELECT COUNT(*) FROM recRent r WHERE r.spotIdRent = s.spotId AND r.recStatus = '租借中') as rentedCount
                FROM renting_Spot s
                WHERE s.spotStatus = '營運中'
            """, nativeQuery = true)
    List<SpotMonitor> getSpotRealtimeStatus();
}