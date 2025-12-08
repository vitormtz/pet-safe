package com.example.petsafe.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.FcmTokenRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmTokenManager {

    private static final String TAG = "FcmTokenManager";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    /**
     * Request notification permission (Android 13+) and register FCM token
     */
    public static void requestNotificationPermissionAndRegisterToken(Activity activity) {
        // Check if Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            } else {
                // Permission already granted, register token
                registerFcmToken(activity);
            }
        } else {
            // Android < 13, no runtime permission needed for notifications
            registerFcmToken(activity);
        }
    }

    /**
     * Get FCM token and send to server
     */
    public static void registerFcmToken(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping FCM token registration");
            return;
        }

        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                        return;
                    }

                    // Get token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Send token to server
                    sendTokenToServer(context, token);
                });
    }

    /**
     * Send FCM token to server
     */
    private static void sendTokenToServer(Context context, String token) {
        SessionManager sessionManager = new SessionManager(context);
        String authToken = sessionManager.getAuthorizationHeader();
        ApiService apiService = ApiClient.getApiService();

        FcmTokenRequest request = new FcmTokenRequest(token);

        apiService.registerFcmToken(authToken, request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token successfully registered with server");
                } else {
                    Log.e(TAG, "Failed to register FCM token with server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "Error registering FCM token with server", t);
            }
        });
    }

    /**
     * Handle permission result from activity
     */
    public static void onRequestPermissionsResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                registerFcmToken(activity);
            } else {
                Log.d(TAG, "Notification permission denied");
            }
        }
    }
}
