package com.example.backend.service;

import com.example.backend.model.Seat;
import com.example.backend.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SeatService implements ISeatService {

    // @Autowired: 自動注入 SeatRepository。
    // 這是 Spring Data JPA 的核心，它讓我們不用寫 DAO 實作類別就能操作資料庫。
    @Autowired
    private SeatRepository seatRepository;

    @Override
    public Seat insert(Seat seat) {
        // save(): 自動判斷是新增還是修改。這裡是新增。
        return seatRepository.save(seat);
    }

    @Override
    public Seat update(Seat seat) {
        // save(): 這裡是修改。
        return seatRepository.save(seat);
    }

    @Override
    public boolean deleteById(Integer seatsId) {
        // 先檢查是否存在，再刪除。
        if (seatRepository.existsById(seatsId)) {
            seatRepository.deleteById(seatsId);
            return true;
        }
        return false;
    }

    @Override
    public Seat selectById(Integer seatsId) {
        // findById(): 根據主鍵查詢。
        return seatRepository.findById(seatsId).orElse(null);
    }

    @Override
    public List<Seat> selectAll() {
        // findAll(): 查詢全部。
        return seatRepository.findAll();
    }

    @Override
    public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
            String serialNumber) {
        // 使用 Specification 進行動態查詢，避免手寫 SQL/HQL。
        return seatRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (seatsName != null && !seatsName.isBlank()) {
                predicates.add(cb.like(root.get("seatsName"), "%" + seatsName + "%"));
            }
            if (seatsType != null && !seatsType.isBlank()) {
                predicates.add(cb.equal(root.get("seatsType"), seatsType));
            }
            if (seatsStatus != null && !seatsStatus.isBlank()) {
                predicates.add(cb.equal(root.get("seatsStatus"), seatsStatus));
            }
            if (spotId != null) {
                predicates.add(cb.equal(root.get("spotId"), spotId));
            }
            if (serialNumber != null && !serialNumber.isBlank()) {
                predicates.add(cb.like(root.get("serialNumber"), "%" + serialNumber + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}