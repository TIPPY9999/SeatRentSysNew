package com.example.backend.controller;

import java.math.BigDecimal;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// [改寫說明]
// 1. @RestController: 宣告這是 Spring Boot 的控制器，回傳資料會自動轉 JSON。
// 2. 移除 HttpServlet: 不再需要繼承傳統 Servlet，解耦 Web 容器依賴。
@RestController
public class SpotInsertServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    // [AXIOS POST 流程說明]
    // 1. 接收：前端 Vue 透過 axios.post('/spot/insert', params) 發送請求。
    // 2. 處理：@PostMapping 攔截請求，並透過 req.getParameter 取得資料。
    @PostMapping("/spot/insert")
    public RentalSpot insert(HttpServletRequest req) {
        String spotCode = req.getParameter("spotCode");
        String spotName = req.getParameter("spotName");
        String spotAddress = req.getParameter("spotAddress");
        String spotStatus = req.getParameter("spotStatus");

        String merchantIdStr = req.getParameter("merchantId");
        Integer merchantId = (merchantIdStr == null || merchantIdStr.isBlank()) ? null : Integer.valueOf(merchantIdStr);

        String latStr = req.getParameter("latitude");
        BigDecimal latitude = (latStr == null || latStr.isBlank()) ? null : new BigDecimal(latStr);

        String lonStr = req.getParameter("longitude");
        BigDecimal longitude = (lonStr == null || lonStr.isBlank()) ? null : new BigDecimal(lonStr);

        RentalSpot spot = new RentalSpot();
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);

        // 3. 業務邏輯：呼叫 Service (已由 Spring 管理交易) 進行新增。
        // 4. 回傳：直接回傳新增後的物件，Spring Boot 自動轉為 JSON 給前端。
        // 5. 前端：Vue 收到 JSON 後，執行 router.push 跳轉頁面。
        return rentalSpotService.insert(spot);
    }
}
