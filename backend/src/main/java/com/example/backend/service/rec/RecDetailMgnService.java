package com.example.backend.service.rec;

import java.time.LocalDate;
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
}
