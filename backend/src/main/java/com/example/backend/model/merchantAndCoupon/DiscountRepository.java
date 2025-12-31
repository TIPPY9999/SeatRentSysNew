package com.example.backend.model.merchantAndCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.merchantAndCoupon.DiscountBean;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountBean, Integer> {

    // 1. 關鍵字搜尋 (包含商家名稱的關聯查詢)
    @Query("SELECT d FROM DiscountBean d WHERE d.couponName LIKE %:kw% OR d.couponDescription LIKE %:kw%")
    List<DiscountBean> findByKeyword(@Param("kw") String kw);

    // 2. 根據商家 ID 查詢
    List<DiscountBean> findByMerchantId(Integer merchantId);

    // 3. 自動更新狀態：若結束日期已過且狀態仍為 1(上架)，則更新為 2(過期)
    @Modifying
    @Query("UPDATE DiscountBean d SET d.couponStatus = 2 WHERE d.endDate < CURRENT_DATE AND d.couponStatus = 1")
    int autoUpdateStatus();

    // 4. 連動更新：當商家停用時，更新該商家旗下所有優惠券
    @Modifying
    @Query("UPDATE DiscountBean d SET d.couponStatus = :status WHERE d.merchantId = :mId")
    void updateStatusByMerchant(@Param("mId") Integer mId, @Param("status") Integer status);
}