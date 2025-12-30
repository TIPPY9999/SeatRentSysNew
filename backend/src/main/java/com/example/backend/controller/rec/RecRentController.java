package com.example.backend.controller.rec;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;

@RestController
@RequestMapping("/api/rec-rents")
@CrossOrigin
public class RecRentController {

    @Autowired
    private RecRentRepository recRentRepository;

    // 1. 查詢全部
    @GetMapping
    public List<RecRent> getAll() {
        return recRentRepository.findAll();
    }

    // 2. 依流水號 (PK) 查詢
    @GetMapping("/{id}")
    public RecRent getById(@PathVariable Integer id) {
        return recRentRepository.findById(id).orElse(null);
    }

    // 3. 依業務編號 (Rxxxxxxxxx) 查詢
    @GetMapping("/code/{recId}")
    public RecRent getByRecId(@PathVariable String recId) {
        return recRentRepository.findByRecId(recId);
    }

    // 4. 新增訂單
    @PostMapping
    public RecRent create(@RequestBody RecRent recRent) {
        // recSeqId 會由資料庫自動產生
        // recId 會由資料庫自動計算
        return recRentRepository.save(recRent);
    }

    // 5. 更新訂單
    @PutMapping("/{id}")
    public RecRent update(@PathVariable Integer id, @RequestBody RecRent recRent) {
        // 確保 ID 一致
        recRent.setRecSeqId(id);
        return recRentRepository.save(recRent);
    }

    // 6. 刪除訂單
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        recRentRepository.deleteById(id);
    }
}