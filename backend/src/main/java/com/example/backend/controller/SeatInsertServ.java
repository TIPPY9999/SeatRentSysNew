package com.example.backend.controller;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SeatInsertServ {
    @Autowired
    private SeatService seatService;

    @GetMapping("/seat/insert")
    public String showForm() {
        // 對應到 /WEB-INF/view/seatInsert.jsp
        return "seatInsert";
    }

    @PostMapping("/seat/insert")
    public String insert(
            @RequestParam("seatsName") String seatsName,
            @RequestParam("seatsType") String seatsType,
            @RequestParam("seatsStatus") String seatsStatus,
            @RequestParam(value = "spotId", required = false) String spotIdStr,
            @RequestParam(value = "serialNumber", required = false) String serialNumber) {

        Seat sBean = new Seat();
        sBean.setSeatsName(seatsName);
        sBean.setSeatsType(seatsType);
        sBean.setSeatsStatus(seatsStatus);

        // spotId 防呆
        Integer spotId = null;
        try {
            spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("spotId 格式錯誤");
        }
        sBean.setSpotId(spotId);

        sBean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

        seatService.insert(sBean);

        return "redirect:/seat/list";
    }
}
