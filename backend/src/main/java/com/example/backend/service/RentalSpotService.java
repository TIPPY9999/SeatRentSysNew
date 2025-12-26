package com.example.backend.service;

import com.example.backend.model.RentalSpot;
import com.example.backend.repository.RentalSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RentalSpotService implements IRentalSpotService {

    // @Autowired: 這是 Spring 的「自動裝配」。
    // 我們不需要自己寫 RentalSpotRepository 的實作類別 (DAO)，
    // Spring Data JPA 會在啟動時自動幫我們產生一個代理物件，裡面已經寫好了所有 CRUD 的 SQL 邏輯。
    // 這樣我們就不用再寫 EntityManager 的 persist, merge, remove 等底層程式碼了。
    @Autowired
    private RentalSpotRepository rentalSpotRepository;

    @Override
    public List<RentalSpot> selectAll() {
        // findAll(): 這是 JpaRepository 內建的方法。
        // 它會自動產生 "SELECT * FROM rental_spot" 這樣的 SQL 去資料庫撈全部資料。
        return rentalSpotRepository.findAll();
    }

    @Override
    public RentalSpot selectById(Integer spotId) {
        // findById(): 自動產生 "SELECT * FROM rental_spot WHERE spot_id = ?"。
        // .orElse(null): 因為 findById 回傳的是 Optional (一個可能為空的容器)，
        // 如果找不到資料，我們就回傳 null 給呼叫者，避免 NullPointerException。
        return rentalSpotRepository.findById(spotId).orElse(null);
    }

    @Override
    public RentalSpot insert(RentalSpot spot) {
        // save(): 這是一個聰明的方法。
        // 如果傳進來的物件沒有 ID (或是 ID 在資料庫不存在)，它就會執行 INSERT SQL。
        return rentalSpotRepository.save(spot);
    }

    @Override
    public RentalSpot update(RentalSpot spot) {
        // save(): 如果傳進來的物件有 ID 且資料庫有這筆資料，它就會執行 UPDATE SQL。
        // 所以新增和修改都可以用同一個 save 方法，Spring Data JPA 會幫我們判斷。
        return rentalSpotRepository.save(spot);
    }

    @Override
    public boolean deleteById(Integer spotId) {
        // existsById(): 先檢查這筆 ID 是否存在 (SELECT count(*)...)。
        if (rentalSpotRepository.existsById(spotId)) {
            // deleteById(): 存在就執行 DELETE 語句。
            rentalSpotRepository.deleteById(spotId);
            return true;
        }
        return false;
    }

    @Override
    public List<RentalSpot> findByCondition(String spotCode, String spotName, String spotStatus, Integer merchantId) {
        // findAll(Specification): 這是 JPA 的「動態查詢」功能。
        // 我們不需要手寫 "WHERE 1=1 AND ..." 這種字串拼接 (容易寫錯又怕 SQL Injection)。
        // 這裡是用 Java 物件的方式來描述查詢條件，Spring 會自動幫我們翻譯成正確的 SQL。
        return rentalSpotRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // root: 代表資料表 (RentalSpot)
            // cb (CriteriaBuilder): 用來建立條件 (如 equal, like)

            if (spotCode != null && !spotCode.isBlank()) {
                // 相當於 SQL: spotCode LIKE %...%
                predicates.add(cb.like(root.get("spotCode"), "%" + spotCode + "%"));
            }
            if (spotName != null && !spotName.isBlank()) {
                predicates.add(cb.like(root.get("spotName"), "%" + spotName + "%"));
            }
            if (spotStatus != null && !spotStatus.isBlank()) {
                // 相當於 SQL: spotStatus = ...
                predicates.add(cb.equal(root.get("spotStatus"), spotStatus));
            }
            if (merchantId != null) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }

            // 將所有條件用 AND 連接起來 (WHERE condition1 AND condition2 ...)
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}