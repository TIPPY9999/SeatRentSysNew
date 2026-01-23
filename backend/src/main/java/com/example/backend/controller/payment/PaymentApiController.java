package com.example.backend.controller.payment;

import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.utils.EcpayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PaymentApiController {

    @Autowired
    private RecRentRepository recRentRepository;

    @Autowired
    private EcpayUtils ecpayUtils;

    @PostMapping("/checkout")
    public String checkout(@RequestParam("recId") String recId) {
        // 1. 取得訂單，若找不到則回傳錯誤訊息
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null) {
            return "Error: Order not found.";
        }

        // 2. 獲取應付金額 (Integer 轉字串，綠界不收小數點)
        String amount = String.valueOf(order.getRecRequestPay());

        // 3. 設定商品描述
        String itemName = "租借費用 - 訂單號：" + order.getRecId();

        // 4. 產生綠界自動提交的 HTML Form
        return ecpayUtils.genCheckOutForm(order.getRecId(), amount, itemName);
    }
}