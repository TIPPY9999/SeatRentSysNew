package com.example.backend.controller;

import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpotListServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @GetMapping("/spot/list")
    public String list(Model model) {
        List<RentalSpot> list = rentalSpotService.selectAll();

        model.addAttribute("spotList", list);
        return "spotList";
    }
}
