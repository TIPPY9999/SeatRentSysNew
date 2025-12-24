package com.example.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "renting_Spot", schema = "dbo") // 加上schema避免找錯資料表
public class RentalSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spotId")
    private Integer spotId;

    @Column(name = "spotCode")
    private String spotCode;

    @Column(name = "spotName")
    private String spotName;

    @Column(name = "spotAddress")
    private String spotAddress;

    @Column(name = "spotStatus")
    private String spotStatus;

    @Column(name = "merchantId")
    private Integer merchantId;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false) // createdAt 只在建立時設定
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    protected LocalDateTime updatedAt;

    @Column(name = "latitude", precision = 10, scale = 7) // 10 總位數，7 小數位
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7) // 10 總位數，7 小數位
    private BigDecimal longitude;

    public RentalSpot() {
    }

    public RentalSpot(String spotCode, String spotName, String spotStatus) {
        this.spotCode = spotCode;
        this.spotName = spotName;
        this.spotStatus = spotStatus;
    }

    public RentalSpot(String spotCode, String spotName, String spotAddress, String spotStatus, Integer merchantId,
            BigDecimal latitude, BigDecimal longitude) {
        this.spotCode = spotCode;
        this.spotName = spotName;
        this.spotAddress = spotAddress;
        this.spotStatus = spotStatus;
        this.merchantId = merchantId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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

    // 創建時間由系統自動產生，不需set方法
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 更新時間由系統自動產生，不需set方法
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

}