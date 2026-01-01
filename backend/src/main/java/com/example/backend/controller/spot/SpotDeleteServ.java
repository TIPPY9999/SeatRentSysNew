package com.example.backend.controller.spot;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.spot.RentalSpotService;

@RestController
public class SpotDeleteServ {

    // [修正：改用建構子注入]
    // 確保 Controller 依賴的 Service 在建立時就已備妥。
    private final RentalSpotService rentalSpotService;

    public SpotDeleteServ(RentalSpotService rentalSpotService) {
        this.rentalSpotService = rentalSpotService;
    }

    // [AXIOS POST 流程]
    // 前端發送刪除請求 -> 這裡接收 ID -> 呼叫 Service 刪除 -> 回傳簡單字串
    @PostMapping("/spot/delete")
    public String delete(HttpServletRequest req) {
        String spotIdStr = req.getParameter("spotId");
        if (spotIdStr != null && !spotIdStr.isBlank()) {
            rentalSpotService.deleteById(Integer.valueOf(spotIdStr));
        }
        // 回傳一個簡單的成功訊息，前端收到 200 OK 就會重新整理列表
        return "Deleted successfully";
    }
}
