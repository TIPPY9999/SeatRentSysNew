package com.example.backend.controller.payment;

import com.example.backend.model.merchantAndCoupon.SponsorshipRecord;
import com.example.backend.model.rec.RecRent;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.service.merchantAndCoupon.PaymentService;
import com.example.backend.utils.EcpayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {

    @Autowired
    private RecRentRepository recRentRepository;

    @Autowired
    private com.example.backend.repository.merchantAndCoupon.SponsorshipRepository sponsorshipRepository; // 💡 注入贊助
                                                                                                          // Repository

    @Autowired
    private EcpayUtils ecpayUtils;

    @Autowired
    private PaymentService paymentService;

    /**
     * 1. 產生租借結帳表單 (維持原樣)
     */
    @PostMapping(value = "/checkout", produces = "text/html;charset=UTF-8")
    public String checkout(@RequestParam("recId") String recId, @RequestParam("baseUrl") String baseUrl) {
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null)
            return "<h2>訂單不存在</h2>";

        String amount = String.valueOf(order.getRecRequestPay());
        String itemName = "租借費用-" + order.getRecId();
        String tradeNo = order.getRecId() + "X" + System.currentTimeMillis() / 1000;

        return ecpayUtils.genCheckOutForm(tradeNo, amount, itemName, baseUrl);
    }

    /**
     * 2. 產生贊助表單
     * 💡 修改：加入 memberId 與 comment，並在跳轉前存入資料庫
     */
    @PostMapping(value = "/sponsor", produces = "text/html;charset=UTF-8")
    public String sponsor(@RequestParam("memberId") Integer memberId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam("baseUrl") String baseUrl) {

        // 所有的髒活（存資料庫、產表單）都丟給 Service
        return paymentService.createSponsorshipOrder(memberId, amount, comment, baseUrl);
    }

    /**
     * 3. 綠界瀏覽器跳轉頁 (OrderResultURL)
     */
    @RequestMapping(value = "/payment-success", method = { RequestMethod.GET,
            RequestMethod.POST }, produces = "text/html;charset=UTF-8")
    public String paymentSuccess(@RequestParam Map<String, String> formData) {

        String rtnCode = formData.getOrDefault("RtnCode", "0");
        String tradeNo = formData.get("MerchantTradeNo");

        if (tradeNo != null && tradeNo.startsWith("SPN")) {
            paymentService.processPaymentResult(formData);
        }

        String message = "1".equals(rtnCode) ? "訂單支付成功！" : "支付過程似乎有誤，請洽管理員。";
        String icon = "1".equals(rtnCode) ? "success" : "error";

        return "<html><head><meta charset='UTF-8'><script src='https://cdn.jsdelivr.net/npm/sweetalert2@11'></script></head>"
                +
                "<body><script>window.onload = function() {" +
                "  Swal.fire({ title: '" + message + "', icon: '" + icon
                + "', confirmButtonText: '確定', allowOutsideClick: false }).then((result) => {" +
                "    if (result.isConfirmed) {" +
                "      if (window.opener) { window.opener.postMessage('PAYMENT_SUCCESS', '*'); window.close(); }" +
                "      else { window.location.href = 'http://localhost:5173/'; }" +
                "    }" +
                "  });" +
                "};</script></body></html>";
    }

    /**
     * 4. 綠界後台非同步回傳 (ReturnURL)
     */
    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> formData) {
        String ecpayTradeNo = formData.get("MerchantTradeNo");
        String rtnCode = formData.get("RtnCode");
        String tradeAmt = formData.get("TradeAmt");

        if ("1".equals(rtnCode)) {
            // A. 處理贊助紀錄更新
            if (ecpayTradeNo.startsWith("SPN")) {
                // 💡 修正：直接呼叫 Service，它會處理狀態變更 + 會員加點
                paymentService.processPaymentResult(formData);
                return "1|OK";
            }

            // B. 處理租借訂單更新
            String realId = ecpayTradeNo.contains("X") ? ecpayTradeNo.split("X")[0] : ecpayTradeNo;
            RecRent order = recRentRepository.findByRecId(realId);
            if (order != null) {
                order.setRecStatus("PAID");
                order.setRecPayment((int) Double.parseDouble(tradeAmt));
                order.setRecPayBy(formData.get("PaymentType"));
                recRentRepository.save(order);
                return "1|OK";
            }
        }
        return "0|Error";
    }

    @GetMapping("/admin/sponsors")
    public ResponseEntity<?> getAllSponsors() {
        // 假設你在 SponsorshipRepository 裡有 findAll()
        return ResponseEntity.ok(sponsorshipRepository.findAllByOrderBySponsorIdDesc());
    }
}