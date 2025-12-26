package com.example.backend.controller;

import java.util.List;

import com.example.backend.service.RentalSpotService;
import com.example.backend.model.RentalSpot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotListServ {

    // [註解掉的原因]：改用建構子注入。
    // @Autowired
    // private final RentalSpotService rentalSpotService;

    private final RentalSpotService rentalSpotService;

    // [修正：改用建構子注入]
    // [優點]：與 Service 層的修改原因相同，使用建構子注入可以確保 Controller 建立時，其依賴的 Service 已經準備就緒，
    // 讓程式碼的依賴關係更清晰、更穩固。
    public SpotListServ(RentalSpotService rentalSpotService) {
        this.rentalSpotService = rentalSpotService;
    }

    @GetMapping("/spot/list")
    public List<RentalSpot> getList() {
        return rentalSpotService.selectAll();
    }
}
