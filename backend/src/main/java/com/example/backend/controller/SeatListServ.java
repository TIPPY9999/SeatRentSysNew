package com.example.backend.controller;

import java.util.List;

import com.example.backend.model.Seat;
import com.example.backend.service.SeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.beans.factory.annotation.Autowired;

// 改用 @RestController：
// 這就像是給這個類別掛上「Web 櫃台」的招牌。
// 以前我們得自己把資料轉成 JSON 字串 (用 ObjectMapper)，很麻煩。
// 現在有了這個，Spring Boot 就知道我們要回傳資料，會自動幫我們把 List<Seat> (設備(出租的椅子)列表) 轉成 JSON 格式丟給前端。
@RestController
public class SeatListServ {

    // 使用 @Autowired：
    // 這叫「依賴注入」，白話說就是「自動配給」。
    // 以前我們要自己 new SeatService()，還要管 Session。
    // 現在 Spring 容器這個大管家已經幫我們準備好 SeatService 了，
    // 只要喊一聲，它就會自動把做好的實例送進來，我們直接用就好。
    // [註解掉的原因]：改用建構子注入，這是 Spring 官方更推薦的最佳實踐，
    // 可以提升程式碼的穩定性與可測試性。
    // @Autowired
    // private final SeatService seatService;

    // [修正：改用建構子注入]
    // 1. 移除 @Autowired，將變數設為 final，確保它在物件建立後不會被更改。
    private final SeatService seatService;

    // 2. 建立建構子，讓 Spring 在建立這個 Controller 時，必須提供一個 SeatService。
    // [優點]：依賴關係在物件建立時就已固定，無法在執行期間被意外修改，讓程式更可靠。
    public SeatListServ(SeatService seatService) {
        this.seatService = seatService;
    }

    // 使用 @GetMapping：
    // 這就是路標，告訴系統：「只要有人用 GET 方法敲 /seat/list 這個門，就找我處理」。
    // 取代了以前 Servlet 裡面的 doGet 方法，寫法更直覺。
    @GetMapping("/seat/list")
    public List<Seat> getList() {
        // 直接呼叫 Service 拿資料：
        // 以前這裡要寫一堆 try-catch、開關 Transaction (交易)。
        // 現在那些複雜的資料庫管理工作，都交給 Service 層的 @Transactional 處理了。
        // 我們只要輕鬆地把拿到的座位列表回傳回去，任務就完成了。
        return seatService.selectAll();
    }
}
