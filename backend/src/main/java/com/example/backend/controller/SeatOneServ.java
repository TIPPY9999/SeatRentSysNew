package com.example.backend.controller;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatOneServ {

    @Autowired
    private SeatService seatService;

    // [處理單筆座位查詢]
    // 對應前端 axios.get('/seat/one', { params: { seatsId: ... } })
    @GetMapping("/seat/one")
    public Seat getOne(HttpServletRequest req) {
        String seatsIdStr = req.getParameter("seatsId");
        if (seatsIdStr != null && !seatsIdStr.isBlank()) {
            // 回傳單一物件，自動轉 JSON
            return seatService.selectById(Integer.valueOf(seatsIdStr));
        }
        return null;
    }
}
