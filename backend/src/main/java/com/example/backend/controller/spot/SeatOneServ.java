package com.example.backend.controller.spot;

import com.example.backend.model.spot.Seat;
import com.example.backend.service.spot.SeatService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatOneServ {

    // [修正：改用建構子注入]
    private final SeatService seatService;

    public SeatOneServ(SeatService seatService) {
        this.seatService = seatService;
    }

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
