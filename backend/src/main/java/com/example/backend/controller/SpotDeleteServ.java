package com.example.backend.controller;

import com.example.backend.service.RentalSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpotDeleteServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @PostMapping("/spot/delete")
    public String delete(@RequestParam("spotId") Integer spotId) {
        rentalSpotService.deleteById(spotId);
        return "redirect:/spot/list";
    }

    @GetMapping("/spot/delete")
    public String deleteGet() {
        return "redirect:/spot/list";
    }
}
