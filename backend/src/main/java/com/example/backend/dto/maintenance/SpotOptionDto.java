package com.example.backend.dto.maintenance;

public class SpotOptionDto {
    private Integer spotId;
    private String spotCode;
    private String spotName;
    private String spotAddress;
    private String spotStatus;

    public SpotOptionDto() {}

    public SpotOptionDto(Integer spotId, String spotCode, String spotName, String spotAddress, String spotStatus) {
        this.spotId = spotId;
        this.spotCode = spotCode;
        this.spotName = spotName;
        this.spotAddress = spotAddress;
        this.spotStatus = spotStatus;
    }

    public Integer getSpotId() { return spotId; }
    public void setSpotId(Integer spotId) { this.spotId = spotId; }

    public String getSpotCode() { return spotCode; }
    public void setSpotCode(String spotCode) { this.spotCode = spotCode; }

    public String getSpotName() { return spotName; }
    public void setSpotName(String spotName) { this.spotName = spotName; }

    public String getSpotAddress() { return spotAddress; }
    public void setSpotAddress(String spotAddress) { this.spotAddress = spotAddress; }

    public String getSpotStatus() { return spotStatus; }
    public void setSpotStatus(String spotStatus) { this.spotStatus = spotStatus; }
}
