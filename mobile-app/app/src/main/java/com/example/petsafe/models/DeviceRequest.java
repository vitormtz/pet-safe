package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class DeviceRequest {
    @SerializedName("serial_number")
    private String serialNumber;

    private String imei;
    private String model;
    private String firmware;

    // Constructor
    public DeviceRequest() {
    }

    public DeviceRequest(String serialNumber, String model, String imei, String firmware) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.imei = imei;
        this.firmware = firmware;
    }

    // Getters and Setters
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
}
