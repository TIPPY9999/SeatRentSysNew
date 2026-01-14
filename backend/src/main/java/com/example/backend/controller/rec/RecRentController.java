package com.example.backend.controller.rec;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.rec.RecRent;
import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.service.rec.RecDetailMgnService;

@RestController
@RequestMapping("/api/rec-rents")
@CrossOrigin
public class RecRentController {

    @Autowired
    private RecDetailMgnService recDetailService;

    @Autowired
    private RecRentRepository rentRepos;

    // 1. 查詢全部或依條件搜尋
    @GetMapping
    public List<RentDetails> searchRents(
            @RequestParam(required = false) String recId,
            @RequestParam(required = false) Integer memId,
            @RequestParam(required = false) String memName,
            @RequestParam(required = false) String recStatus,
            @RequestParam(required = false) Integer spotId,
            @RequestParam(required = false) String spotName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {

        // 如果所有參數都為空，則返回所有列表
        if (recId == null && memId == null && memName == null && recStatus == null && spotId == null && spotName == null
                && rentDate == null && returnDate == null) {
            return recDetailService.getAllRec();
        }

        // 否則，呼叫 search service
        return recDetailService.search(recId, memId, memName, recStatus, spotId, spotName, rentDate, returnDate);
    }

    // 2. 依訂單PK (recSeqId) 查詢
    @GetMapping("/{id}")
    public RentDetails getRecById(@PathVariable String id) {
        return recDetailService.getRecById(id);
    }

    // 4. 新增訂單
    @PostMapping
    public RecRent create(@RequestBody RecRent recRent) {
        // recSeqId 會由資料庫自動產生
        // recId 會由資料庫自動計算
        return rentRepos.save(recRent);
    }

    // 5. 更新訂單
    @PutMapping("/{id}")
    public RecRent update(@PathVariable String id, @RequestBody RecRent recRent) {
        // 確保 ID 一致
        recRent.setRecId(id);
        return rentRepos.save(recRent);
    }

    // 6. 刪除訂單`
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        rentRepos.deleteById(id);
    }
}