package com.example.backend.controller.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.utils.EcpayUtils;

@RestController
@RequestMapping("/api/payOrder")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PaymentOrderApiController {

    @Autowired
    private RecRentRepository recRentRepository;

    @Autowired
    private EcpayUtils ecpayUtils;

    @PostMapping("/checkout")
    public String checkout(@RequestParam("recId") String recId, @RequestParam("amount") String amount) {
        // 1. 透過訂單ID確認訂單存在，若找不到則回傳錯誤
        System.out.println("/n recId:" + recId + "    amount:" + amount + ";");
        amount.trim();
        System.out.println("/n recId:" + recId + "    amount:" + amount + ";");
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null) {
            return "Error: Order not found.";
        }

        // 3. 設定商品描述
        String itemName = "租借費用 - 訂單號：" + order.getRecId();

        // 4. 產生綠界自動提交的 HTML Form，使用前端傳來的金額
        return ecpayUtils.genCheckOutForm(order.getRecId(), amount, itemName);
    }
}