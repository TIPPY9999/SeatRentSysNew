package com.example.backend.controller;

import com.example.backend.model.DiscountBean;
import com.example.backend.service.DiscountService;
import com.example.backend.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "*")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @GetMapping
    public Result<List<DiscountBean>> list(@RequestParam(required = false) String keyword) {
        return Result.success(discountService.getAll(keyword), "查詢成功");
    }

    @PostMapping
    public Result<String> save(
            @RequestPart("discount") String discountJson, // 前端傳來的 JSON Blob
            @RequestPart(value = "image", required = false) MultipartFile file) {

        try {
            // 1. 使用 Jackson 解析 JSON 並處理 LocalDate
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            DiscountBean discount = mapper.readValue(discountJson, DiscountBean.class);

            // 2. 處理圖片上傳
            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
                if (!Files.exists(root))
                    Files.createDirectories(root);

                Files.copy(file.getInputStream(), root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                discount.setCouponImg(fileName);
            }

            // 3. 儲存 (JPA 會根據 ID 自動判定新增或更新)
            discountService.save(discount);
            return Result.success(null, "儲存成功");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("伺服器錯誤: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        discountService.delete(id);
        return Result.success(null, "刪除成功");
    }
}