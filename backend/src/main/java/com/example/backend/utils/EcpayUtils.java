package com.example.backend.utils;

import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class EcpayUtils {
    private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private static final String HASH_IV = "EkRm7iFT261dpevs";
    private static final String MERCHANT_ID = "3002607";
    // 💡 記得更換為你目前的 Localtunnel 網址
    private static final String BASE_URL = "https://ceola-unreigning-paraphrastically.ngrok-free.dev";

    public String genCheckOutForm(String tradeNo, String totalAmount, String itemName) {
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", MERCHANT_ID);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", totalAmount);
        params.put("TradeDesc", "PlatformTransaction");
        params.put("ItemName", itemName);
        params.put("ReturnURL", BASE_URL + "/api/payment/callback");
        params.put("OrderResultURL", BASE_URL + "/api/payment/payment-success"); // 💡 支付完跳轉回來的網址
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        StringBuilder form = new StringBuilder();
        form.append(
                "<form id='ecpayForm' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5' method='post'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                    .append(entry.getValue()).append("'>");
        }
        form.append("</form>");
        return form.toString();
    }

    public static String generateCheckMacValue(Map<String, String> params) {
        String rawData = params.entrySet().stream()
                .filter(e -> !"CheckMacValue".equals(e.getKey()) && e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String combinedData = "HashKey=" + HASH_KEY + "&" + rawData + "&HashIV=" + HASH_IV;
        String urlEncoded = ecpayUrlEncode(combinedData).toLowerCase();
        return sha256(urlEncoded).toUpperCase();
    }

    private static String ecpayUrlEncode(String data) {
        try {
            String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
            return encoded.replace("%2d", "-").replace("%5f", "_").replace("%2e", ".")
                    .replace("%2a", "*").replace("%28", "(").replace("%29", ")").replace("%21", "!");
        } catch (Exception e) {
            return "";
        }
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