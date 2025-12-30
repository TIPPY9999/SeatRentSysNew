package com.example.backend.controller.rec;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.rec.RentDetails;
import com.example.backend.repository.rec.RentDetailsRepository;

@RestController
@RequestMapping("/api/rent-details")
@CrossOrigin // 允許前端跨域呼叫
public class RentDetailsController {

    @Autowired
    private RentDetailsRepository rentDetailsRepository;

    // 1. 搜尋全部
    @GetMapping
    public List<RentDetails> getAll() {
        return rentDetailsRepository.findAll();
    }

    // 2. 依 ID 搜尋
    @GetMapping("/{id}")
    public RentDetails getById(@PathVariable String id) {
        return rentDetailsRepository.findById(id).orElse(null);
    }

    // 3. 依成員姓名搜尋 (例如: /api/rent-details/search?name=Tippy)
    @GetMapping("/search")
    public List<RentDetails> searchByName(@RequestParam String name) {
        // The findByMemNameContaining method returns List<RentDetails>, so no casting
        // is needed.
        // If it were returning List<Object>, we would need to cast each element.
        // For example: return
        // rentDetailsRepository.findByMemNameContaining(name).stream().map(obj ->
        // (RentDetails) obj).collect(Collectors.toList());
        return rentDetailsRepository.findByMemNameContaining(name);
    }
}
