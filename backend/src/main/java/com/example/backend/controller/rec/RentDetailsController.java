package com.example.backend.controller.rec;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.rec.MonthlyOrderCountDTO;
import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RentDetailsRepository;
import com.example.backend.service.rec.RecDetailMgnService;

@RestController
@RequestMapping("/api/rent-details")
@CrossOrigin // 允許前端跨域呼叫
public class RentDetailsController {

    @Autowired
    private RentDetailsRepository rentDetailsRepository;

    @Autowired
    private RecDetailMgnService recDetailMgnService;

    // 1. 搜尋全部
    @GetMapping("/all")
    public List<RentDetails> getAll() {
        return rentDetailsRepository.findAll();
    }

    // 2. 依 recID 搜尋
    @GetMapping("/{id}")
    public RentDetails getById(@PathVariable String id) {
        return rentDetailsRepository.findById(id).orElse(null);
    }

    // 3. 依時間區間取得每月訂單統計數據
    @GetMapping("/stats/monthly-orders")
    public ResponseEntity<List<MonthlyOrderCountDTO>> getMonthlyOrderStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MonthlyOrderCountDTO> stats = recDetailMgnService.getMonthlyOrderCounts(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
