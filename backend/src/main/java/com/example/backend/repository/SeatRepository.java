package com.example.backend.repository;

import com.example.backend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// @Repository: 告訴 Spring 這是一個負責管資料庫的倉庫 (Repository)。
@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer>, JpaSpecificationExecutor<Seat> {
    // 1. extends JpaRepository<Seat, Integer>:
    // - Seat: 這個倉庫是專門用來管理 Seat (設備(出租的椅子)) 資料表的。
    // - Integer: Seat 表的主鍵 (ID) 是整數型態。
    // - 全自動功能: 繼承這個介面後，Spring Data JPA 會自動幫你產生基本的 CRUD 方法。
    // 例如: save() [新增/修改], findById() [查單筆], findAll() [查全部], deleteById() [刪除]。
    // 你完全不用寫 SQL，直接呼叫這些方法，Spring 就會幫你執行對應的資料庫動作。

    // 2. extends JpaSpecificationExecutor<Seat>:
    // - 全自動功能: 這是用來做「複雜條件查詢」的。
    // 當前端傳來一堆不固定的搜尋條件 (例如: 有時候只查名字，有時候查名字+狀態+類型...)，
    // 這個介面允許我們在 Service 層動態組裝查詢條件 (Specification)，
    // 然後傳給 findAll() 方法，Spring 就會自動把它們翻譯成正確的 SQL WHERE 子句。
}
