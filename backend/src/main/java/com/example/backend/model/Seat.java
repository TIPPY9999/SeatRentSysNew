package com.example.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats", schema = "dbo")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatsId")
    private Integer seatsId;

    @Column(name = "seatsName")
    private String seatsName;

    @Column(name = "seatsType")
    private String seatsType;

    @Column(name = "seatsStatus")
    private String seatsStatus;

    @Column(name = "spotId")
    private Integer spotId;

    @Column(name = "serialNumber")
    private String serialNumber;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public Seat() {
    }

    public Seat(Integer seatsId, String seatsName, String seatsType, String seatsStatus,
            Integer spotId, LocalDateTime updatedAt, String serialNumber, LocalDateTime createdAt) {
        this.seatsId = seatsId;
        this.seatsName = seatsName;
        this.seatsType = seatsType;
        this.seatsStatus = seatsStatus;
        this.spotId = spotId;
        this.updatedAt = updatedAt;
        this.serialNumber = serialNumber;
        this.createdAt = createdAt;
    }

    // --- Getter 和 Setter 方法 ---

    public Integer getSeatsId() {
        return seatsId;
    }

    public void setSeatsId(Integer seatsId) {
        this.seatsId = seatsId;
    }

    public String getSeatsName() {
        return seatsName;
    }

    public void setSeatsName(String seatsName) {
        this.seatsName = seatsName;
    }

    public String getSeatsType() {
        return seatsType;
    }

    public void setSeatsType(String seatsType) {
        this.seatsType = seatsType;
    }

    public String getSeatsStatus() {
        return seatsStatus;
    }

    public void setSeatsStatus(String seatsStatus) {
        this.seatsStatus = seatsStatus;
    }

    public Integer getSpotId() {
        return spotId;
    }

    public void setSpotId(Integer spotId) {
        this.spotId = spotId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
