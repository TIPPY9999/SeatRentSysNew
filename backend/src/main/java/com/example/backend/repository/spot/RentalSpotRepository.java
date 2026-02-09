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
     * 查詢站點即時監控數據 (總座位數 vs 已租借數)。
     * <p>
     * 此方法用於儀表板監控，計算邏輯如下：
     * </p>
     * <ul>
     * <li><b>範圍</b>：僅針對狀態為 '營運中' 的站點。</li>
     * <li><b>總座位 (totalSeats)</b>：該站點下所有狀態為 '啟用' 的座位總數。</li>
     * <li><b>已租借 (rentedCount)</b>：該站點下目前處於 '租借中' 且座位狀態為 '啟用' 的數量。</li>
     * </ul>
     * 
     * @return List<SpotMonitor> 包含站點名稱、總座位數與已租借數的投影資料
     */
    @Query(value = """
                SELECT
                    s.spotId   AS spotId,
                    s.spotName AS spotName,
                    COALESCE(ts.totalSeats, 0) AS totalSeats,
                    -- 可借數 = 原始配置 - 從這裡借出的 + 跨據點還到這裡的
                    COALESCE(ts.totalSeats, 0)
                    - COALESCE(rOut.rentedCount, 0)
                    + COALESCE(rIn.returnedCount, 0) AS availableSeats
                FROM renting_Spot s
                -- 原始配置座位數（以 spotId 為準）
                LEFT JOIN (
                    SELECT spotId, COUNT(*) AS totalSeats
                    FROM seats
                    WHERE seatsStatus = N'啟用'
                    GROUP BY spotId
                ) ts ON ts.spotId = s.spotId
                -- 從這個據點借出去且尚未歸還的數量
                LEFT JOIN (
                    SELECT spotIdRent, COUNT(*) AS rentedCount
                    FROM recRent
                    WHERE recStatus = N'租借中'
                    GROUP BY spotIdRent
                ) rOut ON rOut.spotIdRent = s.spotId
                -- 還到這個據點的數量（跨據點還車）
                LEFT JOIN (
                    SELECT r.spotIdReturn, COUNT(*) AS returnedCount
                    FROM recRent r
                    INNER JOIN (
                        -- 找出每張椅子最後一次歷史紀錄
                        SELECT seatsId, MAX(recSeqId) AS lastSeq
                        FROM recRent
                        WHERE recStatus = N'已完成' AND spotIdReturn IS NOT NULL
                        GROUP BY seatsId
                    ) latest ON r.seatsId = latest.seatsId AND r.recSeqId = latest.lastSeq
                    WHERE r.spotIdRent != r.spotIdReturn  -- 只計算跨據點還車
                    GROUP BY r.spotIdReturn
                ) rIn ON rIn.spotIdReturn = s.spotId
                WHERE s.spotStatus = N'營運中'
            """, nativeQuery = true)
    List<SpotMonitor> getSpotRealtimeStatus();

}