package com.example.backend.controller.support;

import com.example.backend.dto.support.CozeBootstrapResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Support Coze Controller
 * 
 * 用途：提供 Coze Web Chat SDK 初始化所需的 Bootstrap 配置
 * 
 * 端點：GET /api/support/coze/bootstrap
 * 
 * 安全性：
 * 1. 敏感資訊（botId, token）來自環境變數或 application.yml
 * 2. 不在 log 印出完整 token
 * 3. 若缺少必要配置，回傳 400 並提示錯誤訊息
 */
@RestController
@RequestMapping("/api/support/coze")
public class SupportCozeController {

    private static final Logger log = LoggerFactory.getLogger(SupportCozeController.class);

    /**
     * Coze Bot ID
     * 設定方式：application.yml 的 coze.bot-id 或環境變數 COZE_BOT_ID
     */
    @Value("${coze.bot-id:}")
    private String botId;

    /**
     * Coze Personal Access Token (PAT)
     * 設定方式：application.yml 的 coze.pat 或環境變數 COZE_PAT
     * 
     * 注意：PAT 有以下特性
     * 1. 只能在建立時看到一次，無法再次查看
     * 2. 過期後無法延長，只能重新建立
     * 3. 建議定期更換（例如：每 90 天）
     */
    @Value("${coze.pat:}")
    private String pat;

    /**
     * Coze Web Chat SDK Script 來源 URL
     * 設定方式：application.yml 的 coze.chat-sdk-src 或環境變數 COZE_CHAT_SDK_SRC
     * 
     * 預設值：Coze 官方 CDN
     */
    @Value("${coze.chat-sdk-src:}")
    private String chatSdkSrc;

    /**
     * GET /api/support/coze/bootstrap
     * 
     * 回應格式：
     * {
     *   "botId": "7469370888888888888",
     *   "token": "pat_xxxxxxxxxxxx",
     *   "sdkSrc": "https://lf-cdn.coze.cn/obj/unpkg/flow-platform/chat-app-sdk/1.0.0-beta.4/libs/oversea/index.js",
     *   "expiresIn": 0,
     *   "serverTime": "2026-01-31T12:00:00",
     *   "note": "⚠️ PAT Token 提醒：此 Token 有過期時間，過期後需要重新建立"
     * }
     * 
     * 錯誤處理：
     * - 若缺少 botId 或 pat，回傳 400 並提示設定方式
     * 
     * @return CozeBootstrapResponseDto
     */
    @GetMapping("/bootstrap")
    public ResponseEntity<?> getBootstrapConfig() {
        // ==================== Step 1：驗證必要配置 ====================
        if (botId == null || botId.trim().isEmpty()) {
            log.error("❌ Coze Bot ID 未設定！請在 application.yml 設定 coze.bot-id 或環境變數 COZE_BOT_ID");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Coze 配置不完整：缺少 Bot ID");
            errorResponse.put("hint", "請在 application.yml 設定 coze.bot-id 或環境變數 COZE_BOT_ID");
            errorResponse.put("status", 500);
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        if (pat == null || pat.trim().isEmpty()) {
            log.error("❌ Coze PAT Token 未設定！請在 application.yml 設定 coze.pat 或環境變數 COZE_PAT");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Coze 配置不完整：缺少 PAT Token");
            errorResponse.put("hint", "請在 application.yml 設定 coze.pat 或環境變數 COZE_PAT");
            errorResponse.put("status", 500);
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // ==================== Step 2：建立回應 DTO ====================
        CozeBootstrapResponseDto response = new CozeBootstrapResponseDto();
        response.setBotId(botId);
        response.setToken(pat);
        response.setSdkSrc(chatSdkSrc); // 若為空，前端會使用自己的 fallback
        response.setExpiresIn(0); // PAT 過期時間無法透過 API 取得，設為 0
        response.setServerTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        response.setNote("⚠️ PAT Token 提醒：此 Token 有過期時間，過期後需要重新建立");

        // ==================== Step 3：Log 輸出（不印 token） ====================
        log.info("✅ Coze Bootstrap 配置已提供 - Bot ID: {}, SDK Src: {}", 
                botId, 
                chatSdkSrc != null && !chatSdkSrc.isEmpty() ? chatSdkSrc : "[使用前端預設]");

        return ResponseEntity.ok(response);
    }
}
