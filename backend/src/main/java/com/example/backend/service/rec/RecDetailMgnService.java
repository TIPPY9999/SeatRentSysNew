package com.example.backend.service.rec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.rec.RecRent;
import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.repository.rec.RentDetailsRepository;
import com.example.backend.repository.rec.RentDetailsSpecs;
import com.example.backend.dto.rec.MonthlyOrderCountDTO;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecDetailMgnService {
    @Autowired
    private RentDetailsRepository detailRepos;
    @Autowired
    private RecRentRepository recRepos;

    public List<RentDetails> getAllRec() {
        return detailRepos.findAll();

    }

    public List<RentDetails> search(
            Integer recSeqId,
            String recId,
            Integer memId,
            String memName,
            String recStatus,
            Integer spotId,
            String spotName,
            LocalDate rentDate,
            LocalDate returnDate) {

        Specification<RentDetails> spec = Specification.where(null);

        if (recId != null && !recId.isEmpty()) {
            spec = spec.and(RentDetailsSpecs.hasRecId(recId));
        }
        if (memId != null) {
            spec = spec.and(RentDetailsSpecs.hasMemId(memId));
        }
        if (memName != null && !memName.isEmpty()) {
            spec = spec.and(RentDetailsSpecs.memNameContains(memName));
        }
        if (recStatus != null && !recStatus.isEmpty()) {
            spec = spec.and(RentDetailsSpecs.hasRecStatus(recStatus));
        }
        if (spotId != null) {
            spec = spec.and(RentDetailsSpecs.hasSpotId(spotId));
        }
        if (spotName != null && !spotName.isEmpty()) {
            spec = spec.and(RentDetailsSpecs.spotNameContains(spotName));
        }
        if (rentDate != null) {
            spec = spec.and(RentDetailsSpecs.hasRentDate(rentDate));
        }
        if (returnDate != null) {
            spec = spec.and(RentDetailsSpecs.hasReturnDate(returnDate));
        }

        return detailRepos.findAll(spec);
    }

    public RentDetails getRecById(String recId) {
        // 找不到ID回傳ERR MSG
        return detailRepos.findById(recId).orElseThrow(
                () -> new RuntimeException("RentDetails not found with ID: " + recId));// ??
    }

    // 新增訂單
    public void insertOrUpdateRec(RecRent recRent) {
        recRepos.save(recRent);

    }

    // 呼叫Repository取得原始統計數據，並在服務層將其映射為DTO列表
    public List<MonthlyOrderCountDTO> getMonthlyOrderCounts(LocalDate startDate, LocalDate endDate) {
        // 將LocalDate轉換為LocalDateTime以符合Repository查詢需求
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Object[]> results = detailRepos.findMonthlyOrderCounts(startDateTime, endDateTime);
        return results.stream().map(row -> new MonthlyOrderCountDTO(
                ((Number) row[0]).intValue(), // 年份
                ((Number) row[1]).intValue(), // 月份
                ((Number) row[2]).longValue() // 訂單計數
        )).collect(Collectors.toList());
    }
}
