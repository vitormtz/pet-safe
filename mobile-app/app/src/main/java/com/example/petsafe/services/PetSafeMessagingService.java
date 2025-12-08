package com.example.petsafe.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.petsafe.MainActivity;
import com.example.petsafe.R;
import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.FcmTokenRequest;
import com.example.petsafe.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetSafeMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PetSafeMessaging";
    private static final String CHANNEL_ID = "petsafe_alerts";
    private static final String CHANNEL_NAME = "Alertas PetSafe";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when a new FCM token is generated
     * This happens on first app install, after clearing app data, or when Firebase refreshes the token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // Send token to server
        sendTokenToServer(token);
    }

    /**
     * Called when a notification is received while app is in foreground or background
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            showNotification(title, body);
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Extract data
            String alertType = remoteMessage.getData().get("alert_type");
            String deviceId = remoteMessage.getData().get("device_id");
            String petName = remoteMessage.getData().get("pet_name");

            // You can customize notification based on data
            if (alertType != null && alertType.equals("geofence_exit")) {
                String title = "⚠️ Alerta de Geofence";
                String body = petName != null
                    ? petName + " saiu da área segura!"
                    : "Seu pet saiu da área segura!";

                showNotification(title, body);
            }
        }
    }

    /**
     * Send FCM token to server
     */
    private void sendTokenToServer(String token) {
        SessionManager sessionManager = new SessionManager(this);

        // Only send token if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping token registration");
            return;
        }

        String authToken = sessionManager.getAuthorizationHeader();
        ApiService apiService = ApiClient.getApiService();

        FcmTokenRequest request = new FcmTokenRequest(token);

        apiService.registerFcmToken(authToken, request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token successfully registered with server");
                } else {
                    Log.e(TAG, "Failed to register FCM token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "Error registering FCM token", t);
            }
        });
    }

    /**
     * Display notification to user
     */
    private void showNotification(String title, String body) {
        // Intent to open MainActivity when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to create this icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Show notification
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    /**
     * Create notification channel (required for Android O and above)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações de alertas do PetSafe");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
