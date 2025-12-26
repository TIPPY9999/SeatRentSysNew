package com.example.backend.controller;

import java.util.List;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SeatByConditionServ {

    @Autowired
    private SeatService seatService;

    @GetMapping("/seat/condition")
    public String search(
            @RequestParam(value = "seatsName", required = false) String seatsName,
            @RequestParam(value = "seatsType", required = false) String seatsType,
            @RequestParam(value = "seatsStatus", required = false) String seatsStatus,
            @RequestParam(value = "serialNumber", required = false) String serialNumber,
            @RequestParam(value = "spotId", required = false) Integer spotId,
            Model model) {

        List<Seat> seatList = seatService.findByCondition(seatsName, seatsType, seatsStatus, spotId, serialNumber);

        model.addAttribute("seatList", seatList);
        return "seatResult";
    }
}
