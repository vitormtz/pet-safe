package com.example.petsafe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.Geofence;
import com.example.petsafe.models.GeofenceRequest;
import com.example.petsafe.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeofenceActivity extends AppCompatActivity {

    private static final String TAG = "GeofenceActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(-23.550520, -46.633308); // São Paulo

    // Views
    private Toolbar toolbar;
    private MapView mapView;
    private TextInputEditText etName, etRadius, etLatitude, etLongitude;
    private MaterialButton btnSaveGeofence;
    private FrameLayout loadingOverlay;

    // OpenStreetMap
    private Marker marker;
    private Polygon editCircle;
    private Polygon savedCircle;
    private FusedLocationProviderClient fusedLocationClient;

    // Data
    private SessionManager sessionManager;
    private ApiService apiService;
    private Geofence existingGeofence;
    private GeoPoint currentLocation;
    private int currentRadius = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_geofence);

        // Initialize
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupToolbar();
        setupMap();
        setupListeners();

        // Load existing geofence
        loadGeofence();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.mapView);
        etName = findViewById(R.id.etName);
        etRadius = findViewById(R.id.etRadius);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnSaveGeofence = findViewById(R.id.btnSaveGeofence);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupMap() {
        // Configure MapView
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false); // Remove zoom buttons
        mapView.getController().setZoom(16.0);

        // Disable parent scroll when touching map
        mapView.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
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

        // Set default location
        currentLocation = DEFAULT_LOCATION;
        mapView.getController().setCenter(currentLocation);

        // Add marker
        marker = new Marker(mapView);
        marker.setPosition(currentLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setDraggable(true);
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_marker));
        marker.setInfoWindow(null); // Remove info window popup
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentLocation = marker.getPosition();
                updateCircle();
                updateCoordinatesDisplay(currentLocation);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });
        mapView.getOverlays().add(marker);

        // Add edit circle (blue)
        editCircle = new Polygon(mapView);
        editCircle.setPoints(Polygon.pointsAsCircle(currentLocation, currentRadius));
        editCircle.setFillColor(0x334285F4);
        editCircle.setStrokeColor(0xFF4285F4);
        editCircle.setStrokeWidth(2);
        mapView.getOverlays().add(editCircle);

        // Update coordinates display
        updateCoordinatesDisplay(currentLocation);

        // Set up map click listener
        mapView.setOnClickListener(v -> {
            // Map click handling will be done through overlays
        });

        // Add map tap listener
        org.osmdroid.views.overlay.Overlay tapOverlay = new org.osmdroid.views.overlay.Overlay() {
            @Override
            public boolean onSingleTapConfirmed(android.view.MotionEvent e, MapView mapView) {
                org.osmdroid.api.IGeoPoint tappedPoint = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
                currentLocation = new GeoPoint(tappedPoint.getLatitude(), tappedPoint.getLongitude());
                updateMapMarkers();
                updateCoordinatesDisplay(currentLocation);
                return true;
            }
        };
        mapView.getOverlays().add(0, tapOverlay);

        // Request location permission
        requestLocationPermission();
    }

    private void setupListeners() {
        btnSaveGeofence.setOnClickListener(v -> saveGeofence());

        // Update circle radius when user changes radius input
        etRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) return;

                try {
                    int radius = Integer.parseInt(s.toString());
                    if (radius < 10) {
                        etRadius.setText("10");
                        radius = 10;
                    } else if (radius > 1000) {
                        etRadius.setText("1000");
                        radius = 1000;
                    }
                    currentRadius = radius;
                    updateCircle();
                } catch (NumberFormatException e) {
                    // Invalid input, ignore
                }
            }
        });
    }

    private void updateMapMarkers() {
        if (marker != null) {
            marker.setPosition(currentLocation);
            mapView.invalidate();
        }
        updateCircle();
    }

    private void updateCircle() {
        if (editCircle != null) {
            editCircle.setPoints(Polygon.pointsAsCircle(currentLocation, currentRadius));
            mapView.invalidate();
        }
    }

    private void updateCoordinatesDisplay(GeoPoint location) {
        etLatitude.setText(String.format("%.6f", location.getLatitude()));
        etLongitude.setText(String.format("%.6f", location.getLongitude()));
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                    // Only update if no existing geofence
                    if (existingGeofence == null) {
                        currentLocation = userLocation;
                        mapView.getController().animateTo(userLocation);
                        updateMapMarkers();
                        updateCoordinatesDisplay(userLocation);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }

    private void loadGeofence() {
        showLoading();

        String token = "Bearer " + sessionManager.getAccessToken();
        apiService.getGeofence(token).enqueue(new Callback<ApiResponse<Geofence>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Geofence>> call,
                                   @NonNull Response<ApiResponse<Geofence>> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    existingGeofence = response.body().getData();
                    displayExistingGeofence();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Geofence>> call, @NonNull Throwable t) {
                hideLoading();
                // No geofence exists, that's okay
            }
        });
    }

    private void displayExistingGeofence() {
        if (existingGeofence == null) return;

        // Update form fields
        etName.setText(existingGeofence.getName());
        etRadius.setText(String.valueOf(existingGeofence.getRadiusM()));
        currentRadius = existingGeofence.getRadiusM();

        // Update map
        currentLocation = new GeoPoint(existingGeofence.getLatitude(), existingGeofence.getLongitude());

        // Remove existing circles
        if (editCircle != null) {
            mapView.getOverlays().remove(editCircle);
        }
        if (savedCircle != null) {
            mapView.getOverlays().remove(savedCircle);
        }

        // Add saved circle (green)
        savedCircle = new Polygon(mapView);
        savedCircle.setPoints(Polygon.pointsAsCircle(currentLocation, currentRadius));
        savedCircle.setFillColor(0x3328a745);
        savedCircle.setStrokeColor(0xFF28a745);
        savedCircle.setStrokeWidth(2);
        mapView.getOverlays().add(savedCircle);

        // Add edit circle (blue) - hidden initially
        editCircle = new Polygon(mapView);
        editCircle.setPoints(Polygon.pointsAsCircle(currentLocation, currentRadius));
        editCircle.setFillColor(0x334285F4);
        editCircle.setStrokeColor(0xFF4285F4);
        editCircle.setStrokeWidth(2);
        mapView.getOverlays().add(editCircle);

        // Update marker
        if (marker != null) {
            marker.setPosition(currentLocation);
        }

        // Move camera
        mapView.getController().animateTo(currentLocation);

        // Update coordinates display
        updateCoordinatesDisplay(currentLocation);

        // Refresh map
        mapView.invalidate();

        // Update button text
        btnSaveGeofence.setText("Atualizar Área");
    }

    private void saveGeofence() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String radiusStr = etRadius.getText() != null ? etRadius.getText().toString().trim() : "";

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome da área", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        if (radiusStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o raio", Toast.LENGTH_SHORT).show();
            etRadius.requestFocus();
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(radiusStr);
            if (radius < 10 || radius > 1000) {
                Toast.makeText(this, "O raio deve estar entre 10 e 1000 metros", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Raio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request
        GeofenceRequest request = new GeofenceRequest(name, currentLocation.getLatitude(),
                currentLocation.getLongitude(), radius);

        showLoading();

        String token = "Bearer " + sessionManager.getAccessToken();

        if (existingGeofence != null) {
            // Update existing geofence
            apiService.updateGeofence(token, request).enqueue(new Callback<ApiResponse<Geofence>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Geofence>> call,
                                       @NonNull Response<ApiResponse<Geofence>> response) {
                    hideLoading();

                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        Toast.makeText(GeofenceActivity.this,
                                "Área segura atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                        existingGeofence = response.body().getData();
                        displayExistingGeofence();
                    } else {
                        String errorMsg = "Erro ao atualizar área segura";
                        if (response.body() != null && response.body().getError() != null) {
                            errorMsg = response.body().getError();
                        }
                        Toast.makeText(GeofenceActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Geofence>> call, @NonNull Throwable t) {
                    hideLoading();
                    Toast.makeText(GeofenceActivity.this,
                            "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new geofence
            apiService.createGeofence(token, request).enqueue(new Callback<ApiResponse<Geofence>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Geofence>> call,
                                       @NonNull Response<ApiResponse<Geofence>> response) {
                    hideLoading();

                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        Toast.makeText(GeofenceActivity.this,
                                "Área segura criada com sucesso!", Toast.LENGTH_SHORT).show();
                        existingGeofence = response.body().getData();
                        displayExistingGeofence();
                    } else {
                        String errorMsg = "Erro ao criar área segura";
                        if (response.body() != null && response.body().getError() != null) {
                            errorMsg = response.body().getError();
                        }
                        Toast.makeText(GeofenceActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Geofence>> call, @NonNull Throwable t) {
                    hideLoading();
                    Toast.makeText(GeofenceActivity.this,
                            "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }
}
