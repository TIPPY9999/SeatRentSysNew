package com.example.backend.controller;

import java.math.BigDecimal;

import com.example.backend.model.RentalSpot;
import com.example.backend.service.RentalSpotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotUpdateServ {

    // [修正：改用建構子注入]
    // 讓 Controller 的依賴關係在建立物件時就一目了然。
    private final RentalSpotService rentalSpotService;

    public SpotUpdateServ(RentalSpotService rentalSpotService) {
        this.rentalSpotService = rentalSpotService;
    }

    // [處理前端的 GET 請求]
    // 對應前端 axios.get('/spot/update', { params: { spotId: ... } })
    @GetMapping("/spot/update")
    public RentalSpot getOne(HttpServletRequest req) {
        // 1. 接收：從 URL 參數中取出 spotId (例如 ?spotId=123)
        String spotIdStr = req.getParameter("spotId");
        Integer spotId = null;
        if (spotIdStr != null && !spotIdStr.isBlank()) {
            spotId = Integer.valueOf(spotIdStr);
        }

        // 2. 處理：呼叫 Service 去資料庫找資料
        if (spotId != null) {
            // 3. 回傳：直接回傳 RentalSpot (租借據點) 物件。
            // Spring Boot 會自動把它轉換成 JSON 格式 (例如 {"spotId":123, "spotName":"..."}) 傳回給前端。
            return rentalSpotService.selectById(spotId);
        }
        return null;
    }

    // [處理前端的 POST 請求]
    // 對應前端 axios.post('/spot/update', params)
    @PostMapping("/spot/update")
    public RentalSpot update(HttpServletRequest req) {
        // 1. 接收：因為前端是用 URLSearchParams 打包的，所以這裡可以用 req.getParameter() 接收每一個欄位。
        String spotIdStr = req.getParameter("spotId");
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotAddress = req.getParameter("spotAddress");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        String latStr = req.getParameter("latitude");
        String lonStr = req.getParameter("longitude");

        Integer spotId = null;
        Integer merchantId = null;
        BigDecimal latitude = null;
        BigDecimal longitude = null;

        // 資料轉型邏輯...
        spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);
        merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);
        latitude = (latStr == null || latStr.isBlank()) ? null : new BigDecimal(latStr);
        longitude = (lonStr == null || lonStr.isBlank()) ? null : new BigDecimal(lonStr);

        RentalSpot spot = null;
        if (spotId != null) {
            // 2. 處理：先查出舊資料，再更新欄位，最後存回資料庫
            spot = rentalSpotService.selectById(spotId);

            if (spot != null) {
                spot.setSpotCode(spotCode);
                spot.setSpotName(spotName);
                spot.setSpotAddress(spotAddress);
                spot.setSpotStatus(spotStatus);

                spot.setMerchantId(merchantId);
                spot.setLatitude(latitude);
                spot.setLongitude(longitude);

                // 3. 回傳：更新成功後，回傳更新後的物件 (JSON)。
                // 前端收到這個回應，就知道「更新成功」了。
                return rentalSpotService.update(spot);
            }
        }
        return null;
    }
}
