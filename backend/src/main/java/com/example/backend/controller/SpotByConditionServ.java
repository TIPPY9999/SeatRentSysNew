package com.example.backend.controller;

import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotByConditionServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    // [處理前端的查詢請求]
    // 對應前端 axios.get('/spot/condition', { params: { spotName: '...', ... } })
    @GetMapping("/spot/condition")
    public List<RentalSpot> getByCondition(HttpServletRequest req) {
        // 1. 接收：從 URL 參數中取出查詢條件
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        Integer merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);

        // 2. 處理：呼叫 Service 進行資料庫模糊查詢
        // 3. 回傳：回傳 List<RentalSpot> (租借據點列表)，Spring Boot 會自動轉成 JSON 陣列 (例如
        // [{"spotId":1...},
        // {"spotId":2...}])
        return rentalSpotService.findByCondition(spotCode, spotName, spotStatus, merchantId);
    }
}
