package com.example.backend.controller.spot;

import com.example.backend.controller.spot.DTO.SpotUpdateRequest;
import com.example.backend.model.spot.RentalSpot;
import com.example.backend.service.spot.RentalSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * [重構] 租借據點 (RentalSpot) 資源的統一控制器
 * - 合併了 List, One, Insert, Update, Delete, ByCondition 等多個 Controller 的功能。
 * - 遵循 RESTful API 設計風格。
 */
@RestController
@RequestMapping("/spot") // [修正] 配合前端 Proxy 移除 /api 前綴的行為，改為監聽 /spots
public class RentalSpotController {

    private final RentalSpotService rentalSpotService;

    public RentalSpotController(RentalSpotService rentalSpotService) {
        this.rentalSpotService = rentalSpotService;
    }

    // region 查詢功能 (Read)

    /**
     * 查詢所有據點 (GET /api/spots)
     * (原 SpotListController)
     */
    @GetMapping("/list")
    public List<RentalSpot> getAllSpots() {
        return rentalSpotService.selectAll();
    }

    /**
     * 根據 ID 查詢單一據點 (GET /api/spots/{id})
     * (原 SpotOneController)
     * 優化：使用 @PathVariable，讓 URL 更語意化。
     * 優化：使用 ResponseEntity 回傳，當找不到據點時回傳 404 Not Found。
     */
    @GetMapping("/{id}")
    public ResponseEntity<RentalSpot> getSpotById(@PathVariable("id") Integer spotId) {
        RentalSpot spot = rentalSpotService.selectById(spotId);
        return (spot != null) ? ResponseEntity.ok(spot) : ResponseEntity.notFound().build();
    }

    /**
     * 條件查詢 (GET /api/spots/search?spotName=...)
     * (原 SpotByConditionController)
     */
    @GetMapping("/search")
    public List<RentalSpot> findSpotsByCondition(@RequestParam(required = false) String spotCode,
            @RequestParam(required = false) String spotName, @RequestParam(required = false) String spotStatus,
            @RequestParam(required = false) Integer merchantId) {
        return rentalSpotService.findByCondition(spotCode, spotName, spotStatus, merchantId);
    }

    // endregion

    // region 編輯功能 (Create / Update / Delete)

    /**
     * 新增據點 (POST /api/spots)
     * (原 SpotInsertController)
     * 優化：回傳 201 Created 狀態碼，表示資源已成功建立。
     */
    @PostMapping
    public ResponseEntity<RentalSpot> createSpot(@RequestBody RentalSpot spot) {
        RentalSpot createdSpot = rentalSpotService.insert(spot);
        return new ResponseEntity<>(createdSpot, HttpStatus.CREATED);
    }

    /**
     * 更新據點 (PUT /api/spots/{id})
     * (原 SpotUpdateController)
     * 優化：使用 PUT 方法表示「完整更新」一個已存在的資源。
     */
    @PutMapping("/{id}")
    public ResponseEntity<RentalSpot> updateSpot(@PathVariable("id") Integer spotId,
            @RequestBody SpotUpdateRequest updateRequest) {
        // 增加一個檢查，確保 URL 中的 ID 與請求內容的 ID 一致
        if (!spotId.equals(updateRequest.getSpotId())) {
            return ResponseEntity.badRequest().build(); // 回傳 400 錯誤請求
        }

        // 1. 先從資料庫查出舊資料
        RentalSpot spot = rentalSpotService.selectById(spotId);

        if (spot == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 使用 BeanUtils 自動將 DTO 的值複製到 Entity
        // 這會自動比對欄位名稱，將 updateRequest 的值複製到 spot 中，省去手寫 set 的繁瑣
        BeanUtils.copyProperties(updateRequest, spot);

        // 3. 呼叫 Service 的 update 方法儲存
        RentalSpot updatedSpot = rentalSpotService.update(spot);
        return ResponseEntity.ok(updatedSpot);
    }

    /**
     * 刪除據點 (DELETE /api/spots/{id})
     * (原 SpotDeleteController)
     * 優化：使用 DELETE 方法，更符合 RESTful 風格。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSpot(@PathVariable("id") Integer spotId) {
        boolean isDeleted = rentalSpotService.deleteById(spotId);
        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "Spot with ID " + spotId + " deleted successfully."));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // endregion
}
