package com.example.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RedemptionLogDTO {
    private Integer logId;
    private Integer memId;
    private Integer couponId;
    private Integer pointsSpent;
    private String couponName;
    private LocalDateTime redeemTime;
    private String merchantName; // 這是前端要的欄位
}