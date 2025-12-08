package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class GeofenceRequest {
    private String name;
    private Double latitude;
    private Double longitude;

    @SerializedName("radius_m")
    private Integer radiusM;

    // Constructor
    public GeofenceRequest() {
    }

    public GeofenceRequest(String name, Double latitude, Double longitude, Integer radiusM) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusM = radiusM;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getRadiusM() {
        return radiusM;
    }

    public void setRadiusM(Integer radiusM) {
        this.radiusM = radiusM;
    }
}
