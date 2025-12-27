package com.example.backend.model;

import com.example.backend.model.MerchantBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantBean, Integer> {

    // 關鍵字搜尋：Spring Data JPA 會自動解析方法名生成 SQL
    // 這裡我們也可以自定義更複雜的查詢 (類似你原本的 HQL)
    @Query("FROM MerchantBean m WHERE m.merchantName LIKE %:kw% OR m.merchantAddress LIKE %:kw%")
    List<MerchantBean> findByKeyword(@Param("kw") String kw);
}