package com.example.backend.controller;

import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpotByConditionServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @GetMapping("/spot/condition")
    public String search(
            @RequestParam(value = "spotCode", required = false) String spotCode,
            @RequestParam(value = "spotName", required = false) String spotName,
            @RequestParam(value = "spotStatus", required = false) String spotStatus,
            @RequestParam(value = "merchantId", required = false) Integer merchantId,
            Model model) {

        List<RentalSpot> list = rentalSpotService.findByCondition(spotCode, spotName, spotStatus, merchantId);
        model.addAttribute("spotList", list);
        return "spotResult";
    }
}
