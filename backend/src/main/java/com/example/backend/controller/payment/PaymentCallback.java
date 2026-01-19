package com.example.backend.controller.payment;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.web.bind.annotation.*;
import com.example.backend.utils.EcpayUtils;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentCallback {

    // 綠界付款結果回傳通知 (ReturnURL)
    // 注意：綠界是用 POST 傳送 Form Data，所以用 @RequestParam 接收
    @PostMapping("/callback")
    public String handlePaymentCallback(@RequestParam Map<String, String> allParams) {

        // 1. 將接收到的參數轉入 TreeMap 進行排序 (驗證簽章需要排序)
        Map<String, String> params = new TreeMap<>(allParams);

        // 2. 驗證 CheckMacValue 確保資料來源正確
        String receivedMac = params.get("CheckMacValue");

        // 計算我們這邊的簽章 (注意：EcpayUtils 內部邏輯應排除 CheckMacValue 欄位進行計算)
        String calculatedMac = EcpayUtils.generateCheckMacValue(params);

        if (calculatedMac.equals(receivedMac)) {
            // 3. 檢查付款狀態 (RtnCode 為 1 代表付款成功)
            if ("1".equals(params.get("RtnCode"))) {
                String orderId = params.get("MerchantTradeNo");

                // TODO: 這裡寫你的資料庫更新邏輯
                // 例如: merchantService.updatePaymentStatus(orderId, "PAID");

                System.out.println(">>> 綠界通知：訂單 " + orderId + " 付款成功！");
            }

            // 4. 必須回傳 1|OK 給綠界
            return "1|OK";
        } else {
            System.err.println(">>> 警告：收到非法金流回傳通知，簽章不符！");
            return "0|ErrorMessage";
        }
    }
}