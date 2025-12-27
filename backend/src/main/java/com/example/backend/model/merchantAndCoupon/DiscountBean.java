package com.example.backend.model.merchantAndCoupon;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discount")
@Data // 自動生成符合 Integer 規範的 Getter/Setter
@NoArgsConstructor
public class DiscountBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couponId")
    private Integer couponId; // 必須是大寫 Integer

    @Column(name = "couponName")
    private String couponName;

    @Column(name = "couponDescription")
    private String couponDescription;

    @Column(name = "pointsRequired")
    private Integer pointsRequired;

    @Column(name = "startDate")
    private LocalDate startDate; // 對應 HTML <input type="date">

    @Column(name = "endDate")
    private LocalDate endDate;

    @Column(name = "merchantId")
    private Integer merchantId; // 改為 Integer 確保外鍵能正確對接

    @Column(name = "couponStatus")
    private Integer couponStatus;

    @Column(name = "couponImg")
    private String couponImg;

    @Column(name = "createdTime", insertable = false, updatable = false)
    private LocalDateTime createdTime;

    // 關聯商家 (用於顯示名稱)
    @ManyToOne
    @JoinColumn(name = "merchantId", insertable = false, updatable = false)
    private MerchantBean merchant;

    @Transient
    private String merchantName;
}