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
import com.example.backend.repository.projection.AnalyzeProjections.HotSpot;

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

    // 站點分布於每一縣市之統計
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

    /**
     * 查詢站點即時監控數據。
     * <p>
     * 配合各站點 20 張設備之上限基準設定。
     * </p>
     * <ul>
     * <li><b>容量上限 (totalSeats)</b>：固定為 20 (業務標準)。</li>
     * <li><b>可借設備數 (availableSeats)</b>：目前硬體物理位置確實位於該站點的設備數量。</li>
     * </ul>
     */
    @Query(value = """
                SELECT
                    s.spotId   AS spotId,
                    s.spotName AS spotName,
                    -- 【總設備上限】：配合業務需求，強制設定基準容量為 20 張椅子
                    20 AS totalSeats,

                    -- 【可借設備數】：目前實體在站且可借用的數量
                    COALESCE(curr.availableCount, 0) AS availableSeats
                FROM renting_Spot s
                LEFT JOIN (
                    -- 子查詢：統計目前「實體在站」且「狀態啟用」的設備
                    SELECT spotId, COUNT(*) AS availableCount
                    FROM seats
                    WHERE seatsStatus = N'啟用' AND spotId IS NOT NULL
                    GROUP BY spotId
                ) curr ON curr.spotId = s.spotId
                WHERE s.spotStatus = N'營運中'
            """, nativeQuery = true)
    List<SpotMonitor> getSpotRealtimeStatus();

    /**
     * 獲取熱門點位 (依據租借次數排序，取前 4 名)
     * 用於首頁 HomeView 呈現。
     */
    @Query(value = """
                SELECT TOP 4
                    s.spotId,
                    s.spotName,
                    s.spotStatus,
                    COALESCE(curr.availableCount, 0) AS availableSeats,
                    s.spotImage,
                    COUNT(r.recId) AS orderCount
                FROM renting_Spot s
                LEFT JOIN (
                    SELECT spotId, COUNT(*) AS availableCount
                    FROM seats
                    WHERE seatsStatus = N'啟用' AND spotId IS NOT NULL
                    GROUP BY spotId
                ) curr ON curr.spotId = s.spotId
                LEFT JOIN recRent r ON r.spotIdRent = s.spotId
                WHERE s.spotStatus = N'營運中'
                GROUP BY s.spotId, s.spotName, s.spotStatus, curr.availableCount, s.spotImage
                ORDER BY orderCount DESC
            """, nativeQuery = true)
    List<HotSpot> getHotSpots();
}