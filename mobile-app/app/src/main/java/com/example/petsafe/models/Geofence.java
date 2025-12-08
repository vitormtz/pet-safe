package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class Geofence {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;

    @SerializedName("radius_m")
    private Integer radiusM;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Geofence() {
    }

    public Geofence(String name, Double latitude, Double longitude, Integer radiusM) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusM = radiusM;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
