package com.example.backend.controller;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotOneServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    // [處理單筆查詢]
    // 對應前端 axios.get('/spot/one', { params: { spotId: ... } })
    @GetMapping("/spot/one")
    public RentalSpot getOne(HttpServletRequest req) {
        String spotIdStr = req.getParameter("spotId");
        if (spotIdStr != null && !spotIdStr.isBlank()) {
            // 回傳單一物件，自動轉 JSON
            return rentalSpotService.selectById(Integer.valueOf(spotIdStr));
        }
        return null;
    }
}
