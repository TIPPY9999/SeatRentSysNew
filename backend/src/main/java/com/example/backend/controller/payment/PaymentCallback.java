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
            RecRent order = recRentRepository.findByRecId(recId);
            if (order != null) {
                // 更新訂單狀態
                order.setRecStatus("PAID");
                // 更新實際支付金額 (Integer)
                order.setRecPayment((int) Double.parseDouble(formData.get("TradeAmt")));
                // 記錄支付方式
                order.setRecPayBy(formData.get("PaymentType"));

                recRentRepository.save(order);
                return "1|OK"; // 必須回傳給綠界
            }
        }
        return "0|Error";
    }
}