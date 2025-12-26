package com.example.backend.controller;

import java.util.List;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeatListServ {

    @Autowired
    private SeatService seatService;

    @GetMapping("/seat/list")
    public String list(Model model) {
        List<Seat> seatList = seatService.selectAll();

        model.addAttribute("seatList", seatList);
        return "seatList";
    }
}
