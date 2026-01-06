package com.example.backend.service.rec;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.rec.RecRent;
import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.repository.rec.RentDetailsRepository;

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

    public RentDetails getRecById(String recId) {
        // 找不到ID回傳ERR MSG
        return detailRepos.findById(recId).orElseThrow(
                () -> new RuntimeException("RentDetails not found with ID: " + recId));// ??
    }

    // 依據會員ID搜尋
    public List<RentDetails> findByMemId(Integer memId) {
        return detailRepos.findByMemId(memId);//
    }

    // 依據會員姓名模糊搜尋 (WHERE memName LIKE %name%)
    public List<RentDetails> findByMemNameContaining(String memName) {
        return detailRepos.findByMemNameContaining(memName);
    }

    // 依據站點ID搜尋 (WHERE staId = id)
    public List<RentDetails> findBySpotIdRent(Integer spotId) {
        return detailRepos.findBySpotIdRent(spotId);
    }

    // 依據站點名稱模糊搜尋 (WHERE staName LIKE %name%)
    public List<RentDetails> findByRentSpotNameContaining(String spotName) {
        return detailRepos.findByRentSpotNameContaining(spotName);
    }

    // 依據訂單狀態搜尋
    public List<RentDetails> findByRecStatus(String rentStatus) {
        return detailRepos.findByRecStatus(rentStatus);
    }

    // 依據租借日期搜尋
    public List<RentDetails> findByRecRentDT2(LocalDateTime rentDate) {
        return detailRepos.findByRecRentDT2(rentDate);
    }

    // 依據歸還日期搜尋
    public List<RentDetails> findByRecReturnDT2(LocalDateTime returnDate) {
        return detailRepos.findByRecReturnDT2(returnDate);
    }

    // 新增訂單
    public void insertOrUpdateRec(RecRent recRent) {
        recRepos.save(recRent);

    }
}
