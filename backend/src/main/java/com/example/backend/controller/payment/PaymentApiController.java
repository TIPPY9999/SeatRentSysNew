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
     * 
     * @param baseUrl 由前端傳入的當前 Tunnel 網址
     */
    @PostMapping(value = "/checkout", produces = "text/html;charset=UTF-8")
    public String checkout(@RequestParam("recId") String recId, @RequestParam("baseUrl") String baseUrl) {
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null)
            return "<h2>訂單不存在</h2>";

        String amount = String.valueOf(order.getRecRequestPay());
        String itemName = "租借費用-" + order.getRecId();
        // 加入時間戳避免綠界重複訂單編號錯誤 (綠界不接受重複編號)
        String tradeNo = order.getRecId() + "X" + System.currentTimeMillis() / 1000;

        return ecpayUtils.genCheckOutForm(tradeNo, amount, itemName, baseUrl);
    }

    /**
     * 2. 產生贊助表單
     */
    @PostMapping(value = "/sponsor", produces = "text/html;charset=UTF-8")
    public String sponsor(@RequestParam("amount") String amount, @RequestParam("baseUrl") String baseUrl) {
        String donateId = "SPN" + System.currentTimeMillis();
        String itemName = "贊助支持-TWD" + amount;
        return ecpayUtils.genCheckOutForm(donateId, amount, itemName, baseUrl);
    }

    /**
     * 3. 綠界瀏覽器跳轉頁 (OrderResultURL)
     * 使用者支付完成後會被導回此處，並觸發 SweetAlert
     */

    @RequestMapping(value = "/payment-success", method = { RequestMethod.GET,
            RequestMethod.POST }, produces = "text/html;charset=UTF-8")
    public String paymentSuccess(@RequestParam Map<String, String> formData) {

        // 取得綠界回傳的狀態 (RtnCode 為 1 代表成功)
        String rtnCode = formData.getOrDefault("RtnCode", "0");
        String message = "1".equals(rtnCode) ? "訂單支付成功！" : "支付過程似乎有誤，請洽管理員。";
        String icon = "1".equals(rtnCode) ? "success" : "error";

        return "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <script src='https://cdn.jsdelivr.net/npm/sweetalert2@11'></script>" +
                "</head>" +
                "<body>" +
                "  <script>" +
                "    window.onload = function() {" +
                "      Swal.fire({" +
                "        title: '" + message + "'," +
                "        icon: '" + icon + "'," +
                "        confirmButtonText: '確定'," +
                "        allowOutsideClick: false" +
                "      }).then((result) => {" +
                "        if (result.isConfirmed) {" +
                "          if (window.opener) {" +
                "            window.opener.postMessage('PAYMENT_SUCCESS', '*');" +
                "            window.close();" +
                "          } else {" +
                "            window.location.href = 'http://localhost:5173/';" +
                "          }" +
                "        }" +
                "      });" +
                "    };" +
                "  </script>" +
                "</body>" +
                "</html>";
    }

    /**
     * 4. 綠界後台非同步回傳 (ReturnURL)
     * 綠界伺服器會主動 POST 此 API，這才是真正更新資料庫的時機
     */
    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> formData) {
        String ecpayTradeNo = formData.get("MerchantTradeNo");
        String rtnCode = formData.get("RtnCode");
        String tradeAmt = formData.get("TradeAmt");
        String paymentType = formData.get("PaymentType");

        if ("1".equals(rtnCode)) {
            // 還原原始 ID (去掉 X 之後的時間戳)
            String realId = ecpayTradeNo.contains("X") ? ecpayTradeNo.split("X")[0] : ecpayTradeNo;

            // A. 處理贊助邏輯
            if (realId.startsWith("SPN")) {
                System.out.println("【贊助成功】ID：" + realId + "，金額：" + tradeAmt);
                return "1|OK";
            }

            // B. 處理租借訂單邏輯
            RecRent order = recRentRepository.findByRecId(realId);
            if (order != null) {
                order.setRecStatus("PAID");
                order.setRecPayment((int) Double.parseDouble(tradeAmt));
                order.setRecPayBy(paymentType);
                recRentRepository.save(order);
                System.out.println("【訂單支付成功】ID：" + realId);
                return "1|OK";
            }
        }
        return "0|Error";
    }
}