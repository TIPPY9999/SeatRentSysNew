package com.example.backend.controller.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.rec.RecRent;
import com.example.backend.model.spot.Seat;
import com.example.backend.repository.rec.RecRentRepository;
import com.example.backend.service.merchantAndCoupon.PaymentService;
import com.example.backend.service.spot.SeatService;
import com.example.backend.utils.EcpayUtils;

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

    @Autowired
    private SeatService seatService;

    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") //
    @PostMapping(value = "/checkout", produces = "text/html;charset=UTF-8")
    public String checkout(
            @RequestParam("recId") String recId,
            @RequestParam("amount") String amount,
            @RequestParam("baseUrl") String baseUrl) {
        RecRent order = recRentRepository.findByRecId(recId);
        if (order == null) {
            return "<h2>訂單不存在</h2>";
        }
        String itemName = "租借費用-" + order.getRecId();
        String tradeNo = order.getRecId() + "X" + (System.currentTimeMillis() / 1000);

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
                "  Swal.fire({ title: '" + message + "', icon: '" + icon +
                "', showDenyButton: true, confirmButtonText: '回到首頁', denyButtonText: '查看訂單', denyButtonColor: '#28a745', allowOutsideClick: false }).then((result) => {"
                +
                "    if (result.isConfirmed) {" +
                "      if (window.opener) { window.opener.postMessage('PAYMENT_SUCCESS', '*'); window.close(); }" +
                "      else { window.location.href = 'http://localhost:5173/'; }" +
                "    } else if (result.isDenied) {" +
                "      window.location.href = 'http://localhost:5173/rent/record';" +
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
            System.out.println("*realId:  " + realId);
            if (order != null) {
                // 1. 更新訂單狀態與付款資訊
                order.setRecStatus("已完成"); // 改為與 RecRentController 一致的狀態
                order.setRecPayment((int) Double.parseDouble(tradeAmt));
                order.setRecPayBy(formData.get("PaymentType"));

                // 若無歸還時間，補上當下時間
                if (order.getRecReturnDT2() == null) {
                    order.setRecReturnDT2(LocalDateTime.now());
                }

                recRentRepository.save(order);

                // 2. 執行歸還邏輯：更新座位位置 (對應 RecRentController.update 的邏輯)
                Integer returnSpotId = order.getSpotIdReturn();

                // [新增] 除錯日誌
                System.out.println("付款回調處理中 - 訂單: " + realId + ", 資料庫中的歸還站點 ID: " + returnSpotId);

                if (returnSpotId != null) {
                    try {
                        Seat seat = seatService.selectById(Integer.valueOf(order.getSeatsId()));
                        seat.setSpotId(returnSpotId);
                        seatService.update(seat);
                        System.out.println("座位 " + seat.getSeatsId() + " 已成功移動至站點 " + returnSpotId);
                    } catch (Exception e) {
                        System.err.println("付款回調更新座位失敗: " + e.getMessage());
                    }
                } else {
                    System.err.println("警告：訂單 " + realId + " 的歸還站點 ID 為空，無法更新座位位置！");
                }

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