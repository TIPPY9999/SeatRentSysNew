package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.MerchantBean;
import com.example.backend.model.Result;
import com.example.backend.service.MerchantService;

@RestController
@RequestMapping("/api/merchants")
@CrossOrigin // 允許來自 Vue (如 http://localhost:5173) 的跨域請求
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    // 1. 取得所有或關鍵字搜尋
    @GetMapping
    public Result<List<MerchantBean>> list(@RequestParam(required = false) String keyword) {
        List<MerchantBean> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = merchantService.getByKeyword(keyword);
        } else {
            list = merchantService.getAll();
        }
        return Result.success(list, "查詢成功");
    }

    // 2. 取得特定商家資料 (編輯前置作業)
    @GetMapping("/{id}")
    public Result<MerchantBean> getById(@PathVariable Integer id) {
        MerchantBean merchant = merchantService.getById(id);
        if (merchant != null) {
            return Result.success(merchant, "獲取成功");
        }
        return Result.error("找不到該商家");
    }

    // 3. 新增或更新商家
    @PostMapping
    public Result<String> saveOrUpdate(@RequestBody MerchantBean merchant) {
        try {
            // Service 內部已處理：1. 儲存商家 2. 同步更新旗下優惠券狀態
            merchantService.saveOrUpdate(merchant);
            return Result.success(null, "儲存成功");
        } catch (Exception e) {
            return Result.error("儲存失敗: " + e.getMessage());
        }
    }

    // 4. 刪除商家
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        try {
            merchantService.deleteMerchant(id);
            return Result.success(null, "刪除成功");
        } catch (Exception e) {
            return Result.error("刪除失敗: " + e.getMessage());
        }
    }
}