package com.example.petsafe;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.Device;
import com.example.petsafe.models.Geofence;
import com.example.petsafe.models.Location;
import com.example.petsafe.models.Pet;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Views
    private Toolbar toolbar;
    private MaterialButton btnConfigureGeofence;
    private BottomNavigationView bottomNavigation;
    private MapView mapView;

    // Utils
    private SessionManager sessionManager;
    private ApiService apiService;

    // Data
    private List<Device> devicesList = new ArrayList<>();
    private List<Pet> petsList = new ArrayList<>();
    private Geofence geofence;
    private Polygon geofenceCircle;
    private Map<Long, Marker> deviceMarkers = new HashMap<>();
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        // Initialize SessionManager and API
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToWelcome();
            return;
        }

        initializeViews();
        setupWindowInsets();
        setupToolbar();
        setupMap();
        setupListeners();
        setupBottomNavigation();

        // Load data
        loadPets();
        loadDevices();
        loadGeofence();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        btnConfigureGeofence = findViewById(R.id.btnConfigureGeofence);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        mapView = findViewById(R.id.mapView);
    }

    private void setupWindowInsets() {
        View appBarLayout = findViewById(R.id.appBarLayout);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top inset to AppBarLayout
            appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
            );

            // Apply bottom inset to BottomNavigation
            bottomNav.setPadding(
                    bottomNav.getPaddingLeft(),
                    bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false); // Remove zoom buttons (+/-)
        mapView.getController().setZoom(15.0);

        // Default location: São Paulo, Brazil
        GeoPoint startPoint = new GeoPoint(-23.550520, -46.633308);
        mapView.getController().setCenter(startPoint);

        // Disable parent scroll when touching map
        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Disable parent scroll
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Re-enable parent scroll
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
    }

    private void setupListeners() {
        btnConfigureGeofence.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GeofenceActivity.class);
            startActivity(intent);
        });
    }

    private void loadPets() {
        String token = sessionManager.getAuthorizationHeader();
        apiService.listPets(token).enqueue(new Callback<ApiResponse<List<Pet>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Pet>>> call, Response<ApiResponse<List<Pet>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    petsList = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Pet>>> call, Throwable t) {
                // Handle error silently
            }
        });
    }

    private void loadDevices() {
        String token = sessionManager.getAuthorizationHeader();
        apiService.listDevices(token).enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    devicesList = response.body().getData();
                    displayDevicesOnMap();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar dispositivos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGeofence() {
        String token = sessionManager.getAuthorizationHeader();
        apiService.getGeofence(token).enqueue(new Callback<ApiResponse<Geofence>>() {
            @Override
            public void onResponse(Call<ApiResponse<Geofence>> call, Response<ApiResponse<Geofence>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    geofence = response.body().getData();
                    displayGeofenceOnMap();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Geofence>> call, Throwable t) {
                // Handle error silently
            }
        });
    }

    private void displayDevicesOnMap() {
        // Clear existing markers
        for (Marker marker : deviceMarkers.values()) {
            mapView.getOverlays().remove(marker);
        }
        deviceMarkers.clear();

        if (devicesList.isEmpty()) {
            return;
        }

        // Add marker for each device
        for (Device device : devicesList) {
            if (device.getLastLatitude() != null && device.getLastLongitude() != null) {
                GeoPoint deviceLocation = new GeoPoint(device.getLastLatitude(), device.getLastLongitude());

                Marker marker = new Marker(mapView);
                marker.setPosition(deviceLocation);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_marker));
                marker.setTitle(device.getSerialNumber());

                // Find pet name for this device
                String petName = "Sem pet associado";
                if (device.getPetId() != null) {
                    for (Pet pet : petsList) {
                        if (pet.getId() != null && pet.getId().equals(device.getPetId())) {
                            petName = pet.getName();
                            break;
                        }
                    }
                }

                final String finalPetName = petName;
                marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                    onDeviceMarkerClicked(device, finalPetName);
                    return true;
                });

                mapView.getOverlays().add(marker);
                deviceMarkers.put(device.getId(), marker);
            }
        }

        // Center map on first device
        if (!devicesList.isEmpty() && devicesList.get(0).getLastLatitude() != null) {
            GeoPoint firstDevice = new GeoPoint(
                    devicesList.get(0).getLastLatitude(),
                    devicesList.get(0).getLastLongitude()
            );
            mapView.getController().setCenter(firstDevice);
        }

        mapView.invalidate();
    }

    private void displayGeofenceOnMap() {
        if (geofence == null) {
            return;
        }

        // Remove existing geofence circle
        if (geofenceCircle != null) {
            mapView.getOverlays().remove(geofenceCircle);
        }

        GeoPoint center = new GeoPoint(geofence.getLatitude(), geofence.getLongitude());

        // Create circle polygon
        geofenceCircle = new Polygon();
        geofenceCircle.setPoints(Polygon.pointsAsCircle(center, geofence.getRadiusM()));
        geofenceCircle.setFillColor(0x1A4CAF50); // Green with transparency
        geofenceCircle.setStrokeColor(0xFF4CAF50); // Solid green border
        geofenceCircle.setStrokeWidth(3f);

        mapView.getOverlays().add(geofenceCircle);
        mapView.invalidate();
    }

    private void onDeviceMarkerClicked(Device device, String petName) {
        // Remove previous polyline if exists
        if (currentPolyline != null) {
            mapView.getOverlays().remove(currentPolyline);
            currentPolyline = null;
        }

        // Show device info dialog
        showDeviceInfoDialog(device, petName);

        // Load and display location history
        loadDeviceLocationHistory(device.getId());
    }

    private void loadDeviceLocationHistory(Long deviceId) {
        String token = sessionManager.getAuthorizationHeader();
        apiService.getDeviceLocations(token, deviceId, 10).enqueue(new Callback<ApiResponse<List<Location>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Location>>> call, Response<ApiResponse<List<Location>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Location> locations = response.body().getData();
                    drawLocationHistory(locations);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Location>>> call, Throwable t) {
                // Handle error silently
            }
        });
    }

    private void drawLocationHistory(List<Location> locations) {
        if (locations.isEmpty()) {
            return;
        }

        // Remove previous polyline
        if (currentPolyline != null) {
            mapView.getOverlays().remove(currentPolyline);
        }

        // Create polyline with smooth settings
        currentPolyline = new Polyline();
        currentPolyline.setColor(0xFF0D6EFD); // Blue color #0D6EFD
        currentPolyline.setWidth(8f); // Thicker line for better visibility
        currentPolyline.getOutlinePaint().setStrokeCap(android.graphics.Paint.Cap.ROUND); // Rounded line caps
        currentPolyline.getOutlinePaint().setStrokeJoin(android.graphics.Paint.Join.ROUND); // Rounded corners
        currentPolyline.getOutlinePaint().setAntiAlias(true); // Smooth edges

        List<GeoPoint> points = new ArrayList<>();
        for (Location location : locations) {
            points.add(new GeoPoint(location.getLatitude(), location.getLongitude()));
        }

        currentPolyline.setPoints(points);

        // Add polyline below markers
        mapView.getOverlays().add(0, currentPolyline);
        mapView.invalidate();
    }

    private void showDeviceInfoDialog(Device device, String petName) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_device_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize dialog views
        TextView tvDialogDeviceSerial = dialog.findViewById(R.id.tvDialogDeviceSerial);
        TextView tvDialogPetName = dialog.findViewById(R.id.tvDialogPetName);
        TextView tvDialogDeviceModel = dialog.findViewById(R.id.tvDialogDeviceModel);
        TextView tvDialogDeviceImei = dialog.findViewById(R.id.tvDialogDeviceImei);
        TextView tvDialogDeviceStatus = dialog.findViewById(R.id.tvDialogDeviceStatus);
        TextView tvDialogDeviceLastComm = dialog.findViewById(R.id.tvDialogDeviceLastComm);
        TextView tvDialogLocationCoords = dialog.findViewById(R.id.tvDialogLocationCoords);
        MaterialButton btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);

        // Set device info
        tvDialogDeviceSerial.setText(device.getSerialNumber());
        tvDialogPetName.setText(petName);
        tvDialogDeviceModel.setText(device.getModel() != null ? device.getModel() : "-");
        tvDialogDeviceImei.setText(device.getImei() != null ? device.getImei() : "-");

        // Set status
        if (device.getActive() != null && device.getActive()) {
            tvDialogDeviceStatus.setText("Ativo");
            tvDialogDeviceStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvDialogDeviceStatus.setText("Inativo");
            tvDialogDeviceStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Set last communication
        if (device.getLastComm() != null) {
            tvDialogDeviceLastComm.setText(formatTimestamp(device.getLastComm()));
        } else {
            tvDialogDeviceLastComm.setText("-");
        }

        // Set location
        if (device.getLastLatitude() != null && device.getLastLongitude() != null) {
            String coords = String.format(Locale.US, "%.6f, %.6f", device.getLastLatitude(), device.getLastLongitude());
            tvDialogLocationCoords.setText(coords);
        } else {
            tvDialogLocationCoords.setText("-");
        }

        // Close button
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sdf.parse(timestamp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.navigation_pets) {
                startActivity(new Intent(MainActivity.this, PetsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_devices) {
                startActivity(new Intent(MainActivity.this, DevicesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        // Exit app when back is pressed on main screen
        super.onBackPressed();
        finishAffinity();
    }
}
