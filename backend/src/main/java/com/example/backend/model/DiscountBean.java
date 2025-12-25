package com.example.backend.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
@Entity
@Table(name = "discount") // 對應資料庫的資料表名稱
@Data
public class DiscountBean implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增
    @Column(name = "couponId")
    private Integer couponId;

    @Column(name = "couponName")
    private String couponName;

    @Column(name = "couponDescription")
    private String couponDescription;

    @Column(name = "pointsRequired")
    private int pointsRequired;

    @Column(name = "startDate")
    private LocalDate startDate;

    @Column(name = "endDate")
    private LocalDate endDate;

    @Column(name = "merchantId")
    private int merchantId;

    @Column(name = "couponStatus")
    private int couponStatus;

    @Column(name = "createdTime", insertable = false, updatable = false) 
    // 通常建立時間由資料庫 DEFAULT CURRENT_TIMESTAMP 產生
    private LocalDateTime createdTime;

    @Column(name = "couponImg")
    private String couponImg;

    @Transient // 這個欄位不在資料庫表中，純粹用於顯示（如之前 DAO 中的 Join 查詢）
    private String merchantName;

    // --- Constructor ---
    public DiscountBean() {
    }

    // --- Getter / Setter ---
    // (保持原樣即可)
    
    public String getMerchantName() { return merchantName; } 
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }

    public String getCouponDescription() { return couponDescription; }
    public void setCouponDescription(String couponDescription) { this.couponDescription = couponDescription; }

    public int getPointsRequired() { return pointsRequired; }
    public void setPointsRequired(int pointsRequired) { this.pointsRequired = pointsRequired; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    public int getCouponStatus() { return couponStatus; }
    public void setCouponStatus(int couponStatus) { this.couponStatus = couponStatus; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public String getCouponImg() { return couponImg; }
    public void setCouponImg(String couponImg) { this.couponImg = couponImg; }

    @Override
    public String toString() {
        return "discountBean [couponId=" + couponId + ", couponName=" + couponName + ", couponDescription=" + couponDescription
                + ", pointsRequired=" + pointsRequired + ", startDate=" + startDate + ", endDate=" + endDate
                + ", merchantId=" + merchantId + ", couponStatus=" + couponStatus + ", createdTime=" + createdTime
                + ", couponImg=" + couponImg + "]";
    }
}

