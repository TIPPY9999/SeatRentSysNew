package com.example.backend.controller;

import java.util.List;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.beans.factory.annotation.Autowired;

// 改用 @RestController 註解：
// 1. 告訴 Spring 這是一個控制器組件。
// 2. 所有方法的返回值會自動轉換為 JSON (相當於 @ResponseBody)，無需手動使用 ObjectMapper。
@RestController
public class SeatByConditionServ {

    // 使用 @Autowired 進行依賴注入 (DI)：
    // Spring 容器會自動將已管理的 SeatService 實例注入進來，
    // 取代了傳統手動 new SeatService(session) 的方式，實現解耦與資源管理。
    // [註解掉的原因]：改用建構子注入。雖然 @Autowired 欄位注入很方便，但它會讓單元測試變得困難，
    // 且有隱藏依賴關係的風險。
    // @Autowired
    // private final SeatService seatService;

    // [修正：改用建構子注入]
    // 1. 移除 @Autowired，將變數設為 final，確保它在物件建立後不會被更改。
    private final SeatService seatService;

    // [修正]：補上建構子 (Constructor)
    // 2. 建立建構子，讓 Spring 在建立這個 Controller 時，必須提供一個 SeatService 實例。
    // [優點]：這種方式確保了依賴關係在物件建立時就已確定，且無法被更改，是更穩健的寫法。
    public SeatByConditionServ(SeatService seatService) {
        this.seatService = seatService;
    }

    // 使用 @GetMapping 對應 HTTP GET 請求：
    // 取代了傳統 Servlet 的 doGet 方法判斷。
    @GetMapping("/seat/condition")
    public List<Seat> getByCondition(HttpServletRequest req) {
        String seatsName = req.getParameter("seatsName");
        String seatsType = req.getParameter("seatsType");
        String seatsStatus = req.getParameter("seatsStatus");
        String serialNumber = req.getParameter("serialNumber");

        String spotIdStr = req.getParameter("spotId");
        Integer spotId = (spotIdStr == null || spotIdStr.isBlank()) ? null : Integer.valueOf(spotIdStr);

        // 直接調用 Service 層方法：
        // 1. 交易管理 (Transaction) 已由 Service 層的 @Transactional 處理，Controller
        // 只負責接收請求與回傳資料。
        // 2. 回傳的 List<Seat> (設備(出租的椅子)列表) 會由 Spring Boot 自動序列化為 JSON 格式回傳給前端。
        return seatService.findByCondition(seatsName, seatsType, seatsStatus, spotId, serialNumber);
    }
}
