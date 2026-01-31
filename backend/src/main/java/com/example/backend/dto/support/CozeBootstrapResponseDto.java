package com.example.backend.dto.support;

/**
 * Coze Web Chat SDK Bootstrap API 回應 DTO
 * 
 * 用途：提供前端初始化 Coze SDK 所需的配置資訊
 * 
 * 注意：
 * 1. token 欄位含敏感資訊，toString() 已做遮蔽處理
 * 2. expiresIn 若無法取得可設為 0 或 null
 */
public class CozeBootstrapResponseDto {
    
    /**
     * Coze Bot ID（公開資訊）
     */
    private String botId;
    
    /**
     * 認證 Token（敏感資訊，需保密）
     */
    private String token;
    
    /**
     * Coze SDK Script 來源 URL
     */
    private String sdkSrc;
    
    /**
     * Token 過期時間（秒），若無法取得可為 null
     */
    private Integer expiresIn;
    
    /**
     * 伺服器時間（ISO-8601 格式）
     */
    private String serverTime;
    
    /**
     * 備註訊息（例如：提醒 PAT 過期時間）
     */
    private String note;

    // Constructors
    public CozeBootstrapResponseDto() {}

    public CozeBootstrapResponseDto(String botId, String token, String sdkSrc) {
        this.botId = botId;
        this.token = token;
        this.sdkSrc = sdkSrc;
    }

    // Getters and Setters
    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSdkSrc() {
        return sdkSrc;
    }

    public void setSdkSrc(String sdkSrc) {
        this.sdkSrc = sdkSrc;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * 重寫 toString()，避免印出完整 token
     * 僅顯示前 10 字元 + 遮蔽符號
     */
    @Override
    public String toString() {
        return "CozeBootstrapResponseDto{" +
                "botId='" + botId + '\'' +
                ", token='" + (token != null && token.length() > 10 
                        ? token.substring(0, 10) + "..." 
                        : "[HIDDEN]") + '\'' +
                ", sdkSrc='" + sdkSrc + '\'' +
                ", expiresIn=" + expiresIn +
                ", serverTime='" + serverTime + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
