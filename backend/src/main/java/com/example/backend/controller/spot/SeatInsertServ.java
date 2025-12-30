package com.example.backend.controller.spot;

import com.example.backend.model.spot.Seat;
import com.example.backend.service.spot.SeatService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// [改寫說明]
// 統一使用 @RestController，讓後端架構一致，方便維護。
@RestController
public class SeatInsertServ {

    // [修正：改用建構子注入]
    private final SeatService seatService;

    public SeatInsertServ(SeatService seatService) {
        this.seatService = seatService;
    }

    // [資料去向說明]
    // 前端 (Params) -> Controller (req.getParameter) -> Service (insert) -> DB
    // DB -> Service (return Seat) -> Controller (return Seat (設備(出租的椅子))) -> 前端
    // (response.data)
    @PostMapping("/seat/insert")
    public Seat insert(HttpServletRequest req) {
        String seatsName = req.getParameter("seatsName");
        String seatsType = req.getParameter("seatsType");
        String seatsStatus = req.getParameter("seatsStatus");
        String spotIdStr = req.getParameter("spotId");
        String serialNumber = req.getParameter("serialNumber");

        Seat sBean = new Seat();
        sBean.setSeatsName(seatsName);
        sBean.setSeatsType(seatsType);
        sBean.setSeatsStatus(seatsStatus);

        if (spotIdStr != null && !spotIdStr.isBlank()) {
            // 這裡保留基本的防呆，若轉型失敗會拋出例外，Spring Boot 會回傳 500 錯誤給前端
            sBean.setSpotId(Integer.valueOf(spotIdStr.trim()));
        }

        sBean.setSerialNumber((serialNumber == null || serialNumber.isBlank()) ? null : serialNumber.trim());

        // 透過 Spring 管理的 Service 進行交易與資料庫操作
        return seatService.insert(sBean);
    }
}
