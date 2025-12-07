package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {

    @SerializedName("full_name")
    private String fullName;

    private String phone;

    public UpdateProfileRequest(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
