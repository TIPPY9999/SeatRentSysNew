package com.example.backend.service.spot;

import java.time.Year;
import java.util.ArrayList;
import java.util.List; // 確認這裡是 java.util.List

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.spot.Seat;
import com.example.backend.repository.spot.SeatRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SeatService implements ISeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    /**
     * 新增一個座位，並在儲存前檢查序號是否重複。
     * 
     * @param seat 要新增的座位物件
     * @return 儲存後的座位物件
     * @throws IllegalArgumentException 如果序號已存在
     */
    @Override
    public Seat insert(Seat seat) {
        // 1. 判斷是否需要自動產生序號 (若前端傳來的是空值或空字串)
        if (!StringUtils.hasText(seat.getSerialNumber())) {
            String currentYear = String.valueOf(Year.now().getValue()); // 取得 "2026"
            String prefix = "SN-" + currentYear; // 前綴 "SN-2026"

            // 找資料庫中符合此前綴的最大序號
            Seat lastSeat = seatRepository.findTopBySerialNumberStartingWithOrderBySerialNumberDesc(prefix);

            int nextSequence = 1; // 預設從 1 開始

            if (lastSeat != null && StringUtils.hasText(lastSeat.getSerialNumber())) {
                String lastSn = lastSeat.getSerialNumber();
                // 嘗試解析後三碼 (例如 SN-2026005 -> 005)
                if (lastSn.startsWith(prefix)) {
                    try {
                        String seqStr = lastSn.substring(prefix.length());
                        nextSequence = Integer.parseInt(seqStr) + 1;
                    } catch (NumberFormatException e) {
                        // 若格式異常無法解析數字，就維持從 1 開始，或可在此紀錄 Log
                    }
                }
            }

            // 格式化為三位數，例如 "SN-2026006"
            seat.setSerialNumber(prefix + String.format("%03d", nextSequence));

        } else {
            // 2. 若使用者有手動輸入序號，則檢查是否重複
            if (seatRepository.existsBySerialNumber(seat.getSerialNumber())) {
                throw new IllegalArgumentException("序號 (Serial Number) '" + seat.getSerialNumber() + "' 已存在，請使用不同的序號。");
            }
        }

        // 3. 執行儲存
        return seatRepository.save(seat);
    }

    @Override
    public Seat update(Seat seat) {
        // save(): 這裡是修改。
        // [修正] 確保 Update 操作有 ID，避免變成 Insert
        if (seat.getSeatsId() == null) {
            throw new IllegalArgumentException("更新失敗：座位 ID 不能為空");
        }
        return seatRepository.save(seat);
    }

    @Override
    public boolean deleteById(Integer seatsId) {
        if (seatsId == null) {
            return false;
        }
        // 先檢查是否存在，再刪除。
        if (seatRepository.existsById(seatsId)) {
            seatRepository.deleteById(seatsId);
            return true;
        }
        return false;
    }

    @Override
    public Seat selectById(Integer seatsId) {
        if (seatsId == null) {
            return null;
        }
        // findById(): 根據主鍵查詢。
        return seatRepository.findById(seatsId).orElse(null);
    }

    @Override
    public List<Seat> selectAll() {
        // findAll(): 查詢全部。
        return seatRepository.findAll();
    }

    @Override
    public List<Seat> selectBySpotId(Integer spotId) {
        if (spotId == null) {
            return new ArrayList<>();
        }
        return seatRepository.findBySpotId(spotId);
    }

    @Override
    public List<Seat> findByCondition(String seatsName, String seatsType, String seatsStatus, Integer spotId,
            String serialNumber) {
        // 使用 Specification 進行動態查詢，避免手寫 SQL/HQL。
        return seatRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(); // 確認 Predicate 後面沒有 <...>

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

    @Override
    public List<Seat> selectByKeyword(String keyword) {
        return seatRepository.findByKeyword(keyword);
    }

    @Override
    public long countBySpotId(Integer spotId) {
        return seatRepository.countBySpotId(spotId);
    }
}