package com.example.backend.controller;

import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotListServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @GetMapping("/spot/list")
    public List<RentalSpot> getList() {
        return rentalSpotService.selectAll();
    }
}
