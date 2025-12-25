package com.example.backend.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant") // 對應資料庫中的 merchant 表
@Data
public class MerchantBean implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "merchantId")
	private Integer merchantId;

	@Column(name = "merchantName")
	private String merchantName;

	@Column(name = "merchantPhone")
	private String merchantPhone;

	@Column(name = "merchantEmail")
	private String merchantEmail;

	@Column(name = "merchantAddress")
	private String merchantAddress;

	@Column(name = "merchantStatus")
	private int merchantStatus;

	@Column(name = "createdTime", insertable = false, updatable = false)
	private LocalDateTime createdTime;

	// --- Constructor ---
	public MerchantBean() {}

	// --- toString, Getters & Setters 保持不變 ---
    @Override
	public String toString() {
		return "merchantBean [merchantId=" + merchantId + ", merchantName=" + merchantName + ", merchantPhone="
				+ merchantPhone + ", merchantEmail=" + merchantEmail + ", merchantAddress=" + merchantAddress
				+ ", merchantStatus=" + merchantStatus + ", createdTime=" + createdTime + "]";
	}

	public int getMerchantId() { return merchantId; }
	public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

	public String getMerchantName() { return merchantName; }
	public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

	public String getMerchantPhone() { return merchantPhone; }
	public void setMerchantPhone(String merchantPhone) { this.merchantPhone = merchantPhone; }

	public String getMerchantEmail() { return merchantEmail; }
	public void setMerchantEmail(String merchantEmail) { this.merchantEmail = merchantEmail; }

	public String getMerchantAddress() { return merchantAddress; }
	public void setMerchantAddress(String merchantAddress) { this.merchantAddress = merchantAddress; }

	public int getMerchantStatus() { return merchantStatus; }
	public void setMerchantStatus(int merchantStatus) { this.merchantStatus = merchantStatus; }

	public LocalDateTime getCreatedTime() { return createdTime; }
	public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}