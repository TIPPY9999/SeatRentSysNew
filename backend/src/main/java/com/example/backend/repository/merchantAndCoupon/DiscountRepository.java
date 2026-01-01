package com.example.backend.repository.merchantAndCoupon;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import org.springframework.data.jpa.repository.EntityGraph; // 必須有這個 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountBean, Integer> {

    // [重點] 這裡一定要有 @EntityGraph，不然前端拿到資料會沒有 merchantName
    @Override
    @EntityGraph(attributePaths = "merchant")
    List<DiscountBean> findAll();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE discount SET couponStatus = CASE " +
                   "WHEN endDate < CAST(GETDATE() AS DATE) THEN 2 " +
                   "WHEN startDate > CAST(GETDATE() AS DATE) THEN 0 " +
                   "ELSE 1 END " +
                   "WHERE couponStatus IS NULL OR couponStatus != 3", nativeQuery = true)
    int autoUpdateStatus();

    // [重點] 這裡也要有
    @EntityGraph(attributePaths = "merchant")
    @Query("SELECT d FROM DiscountBean d WHERE d.couponName LIKE %:keyword% OR d.couponDescription LIKE %:keyword%")
    List<DiscountBean> findByKeyword(@Param("keyword") String keyword);

    // [重點] 這裡也要有
    @EntityGraph(attributePaths = "merchant")
    List<DiscountBean> findByMerchantId(Integer merchantId);
}