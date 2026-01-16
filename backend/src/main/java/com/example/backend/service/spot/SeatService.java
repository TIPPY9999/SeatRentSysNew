package com.example.backend.service.spot;

import com.example.backend.model.spot.Seat;
import com.example.backend.repository.spot.SeatRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        // 1. 檢查 serialNumber 是否為空或空字串。
        // 只有在使用者確實輸入了序號時，才需要檢查重複性。
        // 這裡使用 Spring Framework 的 StringUtils.hasText 來判斷。
        if (StringUtils.hasText(seat.getSerialNumber())) {

            // 2. 呼叫 Repository 的 existsBySerialNumber 方法進行檢查。
            if (seatRepository.existsBySerialNumber(seat.getSerialNumber())) {

                // 3. 如果序號已存在，拋出一個業務邏輯異常。
                // 建議在 Controller 層或使用 @RestControllerAdvice 捕捉此異常，
                // 並回傳 HTTP 409 (Conflict) 或 400 (Bad Request) 給前端。
                throw new IllegalArgumentException("序號 (Serial Number) '" + seat.getSerialNumber() + "' 已存在，請使用不同的序號。");
            }
        }

        // 4. 如果檢查通過 (序號不存在或為空)，則執行儲存操作。
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

    @Override
    public List<Seat> selectByKeyword(String keyword) {
        return seatRepository.findByKeyword(keyword);
    }
}