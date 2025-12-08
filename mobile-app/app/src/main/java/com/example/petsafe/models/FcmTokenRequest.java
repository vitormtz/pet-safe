package com.example.petsafe.models;

import com.google.gson.annotations.SerializedName;

public class FcmTokenRequest {
    @SerializedName("fcm_token")
    private String fcmToken;

    public FcmTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
