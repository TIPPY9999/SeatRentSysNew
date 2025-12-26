package com.example.backend.controller;

import java.math.BigDecimal;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpotInsertServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    @GetMapping("/spot/insert")
    public String showForm() {
        return "spotInsert";
    }

    @PostMapping("/spot/insert")
    public String insert(
            @RequestParam("spotCode") String spotCode,
            @RequestParam("spotName") String spotName,
            @RequestParam("spotAddress") String spotAddress,
            @RequestParam("spotStatus") String spotStatus,
            @RequestParam(value = "merchantId", required = false) Integer merchantId,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude) {

        RentalSpot spot = new RentalSpot();
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);
        if (latitude != null)
            spot.setLatitude(latitude);
        if (longitude != null)
            spot.setLongitude(longitude);

        rentalSpotService.insert(spot);

        return "redirect:/spot/list";
    }
}
