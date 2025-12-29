package com.example.backend.controller.merchantAndCoupon;

import com.example.backend.model.merchantAndCoupon.DiscountBean;
import com.example.backend.model.merchantAndCoupon.Result;
import com.example.backend.service.merchantAndCoupon.DiscountService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.dao.EmptyResultDataAccessException; // [新增]
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @GetMapping
    public Result<List<DiscountBean>> list(@RequestParam(required = false) String keyword) {
        return Result.success(discountService.getAll(keyword), "查詢成功");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> save(
            @RequestPart("discount") String discountJson,
            @RequestPart(value = "image", required = false) MultipartFile file) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            DiscountBean discount = mapper.readValue(discountJson, DiscountBean.class);

            if (file != null && !file.isEmpty()) {
                // [安全性修正] 防止檔名為 null (NPE) 以及路徑穿越攻擊
                String originalFilename = file.getOriginalFilename();
                String safeFilename = (originalFilename == null || originalFilename.isBlank())
                        ? "upload.bin" // 預設檔名
                        : Paths.get(originalFilename).getFileName().toString();

                String fileName = UUID.randomUUID().toString() + "_" + safeFilename;
                
                Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
                if (!Files.exists(root)) Files.createDirectories(root);

                Files.copy(file.getInputStream(), root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                discount.setCouponImg(fileName);
            }

            discountService.save(discount);
            return Result.success(null, "儲存成功");

        } catch (IllegalArgumentException e) {
            // [新增] 捕獲 Service 拋出的 "找不到 ID" 錯誤
            return Result.error("儲存失敗：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("伺服器錯誤: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        try {
            discountService.delete(id);
            return Result.success(null, "刪除成功");
        } catch (EmptyResultDataAccessException e) {
            // [防呆] 刪除不存在的 ID
            return Result.error("刪除失敗：找不到該優惠券");
        } catch (Exception e) {
            return Result.error("刪除失敗：" + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id, @RequestParam String action) {
        boolean success = discountService.updateSingleStatus(id, action);
        if (success) {
            return Result.success(null, "狀態更新成功");
        } else {
            return Result.error("更新失敗：找不到資料、日期資料不全或指令錯誤");
        }
    }
}