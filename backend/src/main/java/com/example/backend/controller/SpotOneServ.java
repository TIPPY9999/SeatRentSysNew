package com.example.backend.controller;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpotOneServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @GetMapping("/spot/one")
    public String getOne(@RequestParam("spotId") Integer spotId, Model model) {
        RentalSpot spot = rentalSpotService.selectById(spotId);
        model.addAttribute("spot", spot);
        return "spotOne";
    }
}
