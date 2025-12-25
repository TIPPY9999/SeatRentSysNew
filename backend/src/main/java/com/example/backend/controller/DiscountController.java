package com.example.backend.controller;
import com.example.backend.model.DiscountBean;
import com.example.backend.model.Result;
import com.example.backend.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    // 從 application.properties 讀取上傳路徑 (例如 D:/SeatRentSys/images/)
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    // 1. 取得清單與搜尋
    @GetMapping
    public Result<List<DiscountBean>> list(@RequestParam(required = false) String keyword) {
        List<DiscountBean> list = discountService.getAll(keyword);
        return Result.success(list, "查詢成功");
    }

    // 2. 新增或更新 (處理圖片上傳)
    @PostMapping
    public Result<String> save(
            @RequestPart("discount") DiscountBean discount,
            @RequestPart(value = "image", required = false) MultipartFile file) {
        
        try {
            if (file != null && !file.isEmpty()) {
                // 生成唯一檔名防止重複
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                File dest = new File(uploadPath + fileName);
                
                // 確保目錄存在
                if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
                
                file.transferTo(dest);
                discount.setCouponImg(fileName); // 存入資料庫的是檔名
            }
            
            discountService.save(discount);
            return Result.success(null, "儲存成功");
        } catch (IOException e) {
            return Result.error("圖片上傳失敗: " + e.getMessage());
        }
    }

    // 3. 更新狀態 (上架/下架)
    @PatchMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id, @RequestParam String action) {
        discountService.updateSingleStatus(id, action);
        return Result.success(null, "狀態更新成功");
    }

    // 4. 刪除
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        discountService.delete(id);
        return Result.success(null, "刪除完成");
    }
}
