package com.example.backend.controller.payment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

import com.example.backend.utils.EcpayUtils;

import org.springframework.web.bind.annotation.*;

@RestController // 改用 Spring 的註解
@RequestMapping("/api/payment") // 定義路徑前綴
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 直接在這裡解決跨域問題
public class PaymentApiController {

    @PostMapping("/checkout") // 對應原本的 doPost
    public String checkout() {
        // 1. 設定訂單基本參數
        String merchantTradeNo = "TC" + System.currentTimeMillis();
        String totalAmount = "1000";

        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", "3002607");
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", totalAmount);
        params.put("TradeDesc", "SeatRent_Order");
        params.put("ItemName", "Seat Rental Service");
        params.put("ReturnURL", "https://lightweight-combat-com-lecture.trycloudflare.com/api/payment/callback");
        // 建議也加上這個，付款完點擊「回到商店」才會回到你的 Vue 畫面
        params.put("ClientBackURL", "http://localhost:5173/");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // 2. 產出簽章 (這裡直接呼叫你寫好的工具類)
        String checkMacValue = EcpayUtils.generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 3. 回傳 JSON 字串
        return new Gson().toJson(params);
    }
}