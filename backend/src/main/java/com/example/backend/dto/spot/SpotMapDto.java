package com.example.backend.dto.spot;

import com.example.backend.model.spot.RentalSpot;
import java.math.BigDecimal;

/**
 * 地圖用的據點 DTO
 * 包含即時維修狀態資訊
 */
public class SpotMapDto {
    private Integer spotId;
    private String spotName;
    private String spotCode;
    private String spotStatus;           // 據點狀態（營運中/維修中/已關閉）
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String spotAddress;
    private Boolean hasActiveMaintenance; // ✅ 新增：是否有進行中的維修工單

    public SpotMapDto() {}

    public SpotMapDto(Integer spotId, String spotName, String spotCode, String spotStatus,
                      BigDecimal latitude, BigDecimal longitude, String spotAddress,
                      Boolean hasActiveMaintenance) {
        this.spotId = spotId;
        this.spotName = spotName;
        this.spotCode = spotCode;
        this.spotStatus = spotStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.spotAddress = spotAddress;
        this.hasActiveMaintenance = hasActiveMaintenance;
    }

    /**
     * 從 RentalSpot Entity 轉換為 DTO（不含即時狀態）
     */
    public static SpotMapDto fromEntity(RentalSpot spot) {
        return new SpotMapDto(
            spot.getSpotId(),
            spot.getSpotName(),
            spot.getSpotCode(),
            spot.getSpotStatus(),
            spot.getLatitude(),
            spot.getLongitude(),
            spot.getSpotAddress(),
            false // 預設無維修工單，需另外設定
        );
    }

    // Getters and Setters
    public Integer getSpotId() { return spotId; }
    public void setSpotId(Integer spotId) { this.spotId = spotId; }

    public String getSpotName() { return spotName; }
    public void setSpotName(String spotName) { this.spotName = spotName; }

    public String getSpotCode() { return spotCode; }
    public void setSpotCode(String spotCode) { this.spotCode = spotCode; }

    public String getSpotStatus() { return spotStatus; }
    public void setSpotStatus(String spotStatus) { this.spotStatus = spotStatus; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getSpotAddress() { return spotAddress; }
    public void setSpotAddress(String spotAddress) { this.spotAddress = spotAddress; }

    public Boolean getHasActiveMaintenance() { return hasActiveMaintenance; }
    public void setHasActiveMaintenance(Boolean hasActiveMaintenance) { 
        this.hasActiveMaintenance = hasActiveMaintenance; 
    }
}
