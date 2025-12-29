package com.example.backend.controller.spot;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.spot.SeatService;

@RestController
public class SeatDeleteServ {

    // [修正：改用建構子注入]
    private final SeatService seatService;

    public SeatDeleteServ(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/seat/delete")
    public String delete(HttpServletRequest req) {
        String seatsIdStr = req.getParameter("seatsId");
        if (seatsIdStr != null && !seatsIdStr.isBlank()) {
            seatService.deleteById(Integer.valueOf(seatsIdStr));
        }
        return "Deleted successfully";
    }
}
