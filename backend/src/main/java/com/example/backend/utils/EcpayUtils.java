package com.example.backend.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class EcpayUtils {
    // 綠界測試金鑰
    private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private static final String HASH_IV = "EkRm7iFT261dpevs";
    private static final String MERCHANT_ID = "3002607"; // 綠界測試特店編號

    public String genCheckOutForm(String tradeNo, String totalAmount, String itemName) {
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", "3002607"); // 綠界測試 ID
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", totalAmount);
        params.put("TradeDesc", "RentPayment");
        params.put("ItemName", itemName);
        // 重要：這裏要改成你 ngrok 的網址，綠界才打得進來
        params.put("ReturnURL", "https://your-ngrok-url.ngrok-free.app/api/payment/callback");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");
        params.put("CheckMacValue", generateCheckMacValue(params));

        StringBuilder form = new StringBuilder();
        form.append(
                "<form id='ecpayForm' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5' method='post'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                    .append(entry.getValue()).append("'>");
        }
        form.append("</form><script>document.getElementById('ecpayForm').submit();</script>");
        return form.toString();
    }

    public static String generateCheckMacValue(Map<String, String> params) {
        // 1. 確保使用 TreeMap 進行 A-Z 排序，並過濾掉 CheckMacValue 本身
        String rawData = params.entrySet().stream()
                .filter(e -> !"CheckMacValue".equals(e.getKey()) && e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        // 2. 前加 HashKey，後加 HashIV
        String combinedData = "HashKey=" + HASH_KEY + "&" + rawData + "&HashIV=" + HASH_IV;

        // 3. URL Encode
        String urlEncoded = urlEncode(combinedData);

        // 4. SHA256 加密並轉大寫
        return sha256(urlEncoded).toUpperCase();
    }

    // 關鍵修正：綠界專用的 URL Encode 處理
    private static String urlEncode(String value) {
        String result = URLEncoder.encode(value, StandardCharsets.UTF_8).toLowerCase();

        // 綠界規範：將特定編碼改回符號
        return result.replace("%2d", "-")
                .replace("%5f", "_")
                .replace("%2e", ".")
                .replace("%2a", "*")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%21", "!")
                .replace("%20", "+"); // 注意：空格要轉成 +
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}