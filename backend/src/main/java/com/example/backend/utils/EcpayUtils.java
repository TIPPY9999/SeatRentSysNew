package com.example.backend.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Collectors;

public class EcpayUtils {
    // 綠界測試金鑰
    private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private static final String HASH_IV = "EkRm7iFT261dpevs";

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