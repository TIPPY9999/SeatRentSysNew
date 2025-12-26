package com.example.backend.controller;

import com.example.backend.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatDeleteServ {

    @Autowired
    private SeatService seatService;

    @PostMapping("/seat/delete")
    public String delete(HttpServletRequest req) {
        String seatsIdStr = req.getParameter("seatsId");
        if (seatsIdStr != null && !seatsIdStr.isBlank()) {
            seatService.deleteById(Integer.valueOf(seatsIdStr));
        }
        return "Deleted successfully";
    }
}
