package com.example.backend.model.spot;

import java.io.Serializable;
import java.util.Date;

public class RentalSpotBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---------- 欄位（與資料表一致） ----------
    private Integer spotId;        // 租借點 ID（PK）
    private String spotCode;       // 租借點代碼
    private String spotName;       // 租借點名稱
    private String spotAddress;    // 租借點地址
    private String spotStatus;     // 使用狀態（啟用/停用）
    private Integer merchantId;    // 所屬店家 ID
    private Date createdAt;        // 建立時間
    private Date updatedAt;        // 更新時間
    private Double latitude;       // 緯度
    private Double longitude;      // 經度

    // ---------- 無參數建構子（必備） ----------
    public RentalSpotBean() {}

    // ---------- 全參數建構子（可選，方便用） ----------
    public RentalSpotBean(Integer spotId, String spotCode, String spotName, String spotAddress,
                          String spotStatus, Integer merchantId, Date createdAt, Date updatedAt,
                          Double latitude, Double longitude) {
        this.spotId = spotId;
        this.spotCode = spotCode;
        this.spotName = spotName;
        this.spotAddress = spotAddress;
        this.spotStatus = spotStatus;
        this.merchantId = merchantId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ---------- Getter & Setter ----------
    public Integer getSpotId() {
        return spotId;
    }

    public void setSpotId(Integer spotId) {
        this.spotId = spotId;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public void setSpotCode(String spotCode) {
        this.spotCode = spotCode;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public String getSpotAddress() {
        return spotAddress;
    }

    public void setSpotAddress(String spotAddress) {
        this.spotAddress = spotAddress;
    }

    public String getSpotStatus() {
        return spotStatus;
    }

    public void setSpotStatus(String spotStatus) {
        this.spotStatus = spotStatus;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

	@Override
	public String toString() {
		return "RentalSpotBean [spotId=" + spotId + ", spotCode=" + spotCode + ", spotName=" + spotName
				+ ", spotAddress=" + spotAddress + ", spotStatus=" + spotStatus + ", merchantId=" + merchantId
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", latitude=" + latitude + ", longitude="
				+ longitude + "]";
	}
    
}
