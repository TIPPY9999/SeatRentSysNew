package com.example.backend.controller;

import java.math.BigDecimal;

import com.example.backend.model.RentalSpot;
import com.example.backend.service.RentalSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SpotUpdateServ {

    @Autowired
    private RentalSpotService rentalSpotService;

    // 先查出資料丟到表單
    @GetMapping("/spot/update")
    public String showForm(@RequestParam("spotId") Integer spotId, Model model) {
        RentalSpot spot = rentalSpotService.selectById(spotId);
        model.addAttribute("spot", spot);
        return "spotUpdate";
    }

    // 接收表單做更新
    @PostMapping("/spot/update")
    public String update(
            @RequestParam("spotId") Integer spotId,
            @RequestParam("spotCode") String spotCode,
            @RequestParam("spotName") String spotName,
            @RequestParam("spotAddress") String spotAddress,
            @RequestParam("spotStatus") String spotStatus,
            @RequestParam(value = "merchantId", required = false) Integer merchantId,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude) {

        RentalSpot spot = new RentalSpot();
        spot.setSpotId(spotId);
        spot.setSpotCode(spotCode);
        spot.setSpotName(spotName);
        spot.setSpotAddress(spotAddress);
        spot.setSpotStatus(spotStatus);
        spot.setMerchantId(merchantId);

        if (latitude != null) {
            spot.setLatitude(latitude);
        }
        if (longitude != null) {
            spot.setLongitude(longitude);
        }

        rentalSpotService.update(spot);

        return "redirect:/spot/list";
    }
}
