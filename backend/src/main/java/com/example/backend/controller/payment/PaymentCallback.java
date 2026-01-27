package com.example.backend.controller.payment;

import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentCallback {

    @Autowired
    private RecRentRepository recRentRepository;

    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> formData) {
        // 綠界回傳的訂單編號與狀態代碼
        String recId = formData.get("MerchantTradeNo");
        String rtnCode = formData.get("RtnCode"); // "1" 表示支付成功

        if ("1".equals(rtnCode)) {
            // 💡 判斷是否為贊助訂單
            if (recId != null && recId.startsWith("SPN")) {
                // 這裡是贊助成功的處理邏輯：
                // 你可以選擇存入另一個 Donation 表，或者僅記錄 Log
                System.out.println("收到贊助款項！金額：" + formData.get("TradeAmt"));
                return "1|OK";
            }

            // 💡 原本的租借訂單處理邏輯
            RecRent order = recRentRepository.findByRecId(recId);
            if (order != null) {
                order.setRecStatus("PAID");
                order.setRecPayment((int) Double.parseDouble(formData.get("TradeAmt")));
                order.setRecPayBy(formData.get("PaymentType"));
                recRentRepository.save(order);
                return "1|OK";
            }
        }
        return "0|Error";
    }
}