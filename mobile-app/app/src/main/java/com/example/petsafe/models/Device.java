package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class Device {
    private Long id;

    @SerializedName("serial_number")
    private String serialNumber;

    private String imei;
    private String model;
    private String firmware;
    private Boolean active;

    @SerializedName("last_comm")
    private String lastComm;

    @SerializedName("last_latitude")
    private Double lastLatitude;

    @SerializedName("last_longitude")
    private Double lastLongitude;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getLastComm() {
        return lastComm;
    }

    public void setLastComm(String lastComm) {
        this.lastComm = lastComm;
    }

    public Double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(Double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public Double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(Double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
