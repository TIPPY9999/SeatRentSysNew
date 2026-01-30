package com.example.backend.controller.payment;

import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.utils.EcpayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {

    @Autowired
    private RecRentRepository recRentRepository;

    @Autowired
    private EcpayUtils ecpayUtils;

    /**
     * 1. 產生租借結帳表單
     */
    @PostMapping(value = "/checkout", produces = "text/html;charset=UTF-8")
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 允許前端跨域請求
    public String checkout(@RequestParam("recId") String recId, @RequestParam("amount") String amount) {
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null)
            return "<h2>訂單不存在</h2>";

        String itemName = "租借費用-" + order.getRecId();
        // 加入時間戳避免綠界重複訂單編號錯誤
        String tradeNo = order.getRecId() + "X" + System.currentTimeMillis() / 1000;

        return ecpayUtils.genCheckOutForm(tradeNo, amount, itemName);
    }

    /**
     * 2. 產生贊助表單
     */
    @PostMapping(value = "/sponsor", produces = "text/html;charset=UTF-8")
    public String sponsor(@RequestParam("amount") String amount) {
        String donateId = "SPN" + System.currentTimeMillis();
        String itemName = "贊助支持-TWD" + amount;
        return ecpayUtils.genCheckOutForm(donateId, amount, itemName);
    }

    /**
     * 3. 綠界瀏覽器跳轉頁 (OrderResultURL)
     * 負責通知前端 Vue 關閉彈窗
     */
    @RequestMapping(value = "/payment-success", method = { RequestMethod.GET,
            RequestMethod.POST }, produces = "text/html;charset=UTF-8")
    public String paymentSuccess(jakarta.servlet.http.HttpServletRequest request) {
        // 💡 嘗試從 Request 獲取所有參數
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> formData = new java.util.HashMap<>();

        parameterMap.forEach((key, values) -> {
            formData.put(key, values[0]);
        });
        // Log 記錄
        System.out.println(">>> 進入 payment-success，方法：" + request.getMethod());
        System.out.println(">>> 參數內容：" + formData);

        // 💡 關鍵邏輯：如果收到的是 POST 且沒有參數（綠界跳轉常見問題）
        // 或者我們想確保 JavaScript 一定能執行，強制導向一次 GET
        if (request.getMethod().equals("POST") && formData.isEmpty()) {
            System.out.println(">>> 檢測到空 POST，強制重導向至 GET 以觸發跳轉邏輯");
            return "redirect:/api/payment/payment-success";
        }

        // 這裡回傳原本的 HTML/JS 程式碼
        return "payment_success_page"; // 指向你的 HTML 模板或直接回傳 String
    }

    /**
     * 4. 綠界後台非同步回傳 (ReturnURL)
     * 負責正式更新資料庫狀態，並回傳 1|OK 給綠界
     */
    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> formData) {
        String ecpayTradeNo = formData.get("MerchantTradeNo");
        String rtnCode = formData.get("RtnCode");
        String tradeAmt = formData.get("TradeAmt");
        String paymentType = formData.get("PaymentType");

        // 綠界規定：RtnCode 為 "1" 代表付款成功
        if ("1".equals(rtnCode)) {
            // 還原原始 ID (去掉 X 之後的時間戳)
            String realId = ecpayTradeNo.contains("X") ? ecpayTradeNo.split("X")[0] : ecpayTradeNo;

            // A. 處理贊助邏輯
            if (realId.startsWith("SPN")) {
                System.out.println("【贊助成功確認】訂單號：" + realId + "，實收金額：" + tradeAmt);
                return "1|OK";
            }

            // B. 處理租借訂單邏輯
            RecRent order = recRentRepository.findByRecId(realId);
            if (order != null) {
                order.setRecStatus("PAID");
                order.setRecPayment((int) Double.parseDouble(tradeAmt));
                order.setRecPayBy(paymentType); // 紀錄付款方式 (如 CreditCard)
                recRentRepository.save(order);
                System.out.println("【訂單支付成功】訂單號：" + realId);
                return "1|OK";
            }
        }

        System.out.println("【支付失敗或異常】編號：" + ecpayTradeNo + "，狀態碼：" + rtnCode);
        return "0|Error";
    }
}