package com.example.backend.repository;

import com.example.backend.model.RentalSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// @Repository: 這是 Spring 的標籤，標示這個介面是用來存取資料庫的 DAO 層。
@Repository
public interface RentalSpotRepository extends JpaRepository<RentalSpot, Integer>, JpaSpecificationExecutor<RentalSpot> {
    // 1. extends JpaRepository<RentalSpot, Integer>:
    // - RentalSpot: 指定這個 Repository 管理的是 RentalSpot (租借據點) 這個實體。
    // - Integer: 指定主鍵 (SpotId) 的型別是 Integer。
    // - 全自動魔法: 只要寫這樣一行，Spring 啟動時就會自動產生一個實作類別。
    // 它內建了 findAll(), save(), delete() 等方法。
    // 這就是為什麼你在 Service 層可以直接呼叫 rentalSpotRepository.findAll() 而不用寫任何實作程式碼。

    // 2. extends JpaSpecificationExecutor<RentalSpot>:
    // - 動態查詢神器: 專門處理「多條件組合」的查詢。
    // 它讓我們可以使用 findAll(Specification spec) 這個方法。
    // 在 Service 層裡，我們用 Java 程式碼寫的那些 if (name != null) ... 邏輯，
    // 最後就是透過這個介面，讓 Spring 幫我們轉成 SQL 的 SELECT * FROM ... WHERE ... 語法。
}
