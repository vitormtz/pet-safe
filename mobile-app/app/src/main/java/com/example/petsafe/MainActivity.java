package com.example.petsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petsafe.models.User;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Views
    private Toolbar toolbar;
    private TextView tvUserName;
    private TextView tvTotalPets;
    private TextView tvActivePets;
    private MaterialButton btnAddPet;
    private MaterialButton btnAddDevice;
    private LinearLayout llEmptyAlerts;
    private LinearLayout llEmptyDevices;
    private RecyclerView rvRecentAlerts;
    private RecyclerView rvDevices;
    private BottomNavigationView bottomNavigation;

    // Utils
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUserDetails();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToWelcome();
            return;
        }

        initializeViews();
        setupWindowInsets();
        setupToolbar();
        setupListeners();
        setupBottomNavigation();
        loadUserData();
        loadPetsData();
        loadAlertsData();
        loadDevicesData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvUserName = findViewById(R.id.tvUserName);
        tvTotalPets = findViewById(R.id.tvTotalPets);
        tvActivePets = findViewById(R.id.tvActivePets);
        btnAddPet = findViewById(R.id.btnAddPet);
        btnAddDevice = findViewById(R.id.btnAddDevice);
        llEmptyAlerts = findViewById(R.id.llEmptyAlerts);
        llEmptyDevices = findViewById(R.id.llEmptyDevices);
        rvRecentAlerts = findViewById(R.id.rvRecentAlerts);
        rvDevices = findViewById(R.id.rvDevices);
        bottomNavigation = findViewById(R.id.bottomNavigation);
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

    private void setupListeners() {
        btnAddPet.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PetsActivity.class));
        });

        btnAddDevice.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DevicesActivity.class));
        });

        findViewById(R.id.tvViewAllAlerts).setOnClickListener(v -> {
            // TODO: Navigate to all alerts screen
        });
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

    private void loadUserData() {
        if (currentUser != null) {
            // Display first name and last name
            String fullName = currentUser.getFullName();
            String[] nameParts = fullName.trim().split("\\s+");

            String displayName;
            if (nameParts.length >= 2) {
                // First name + last name
                displayName = nameParts[0] + " " + nameParts[nameParts.length - 1];
            } else {
                // Only one name
                displayName = nameParts[0];
            }

            tvUserName.setText(displayName);
        }
    }

    private void loadPetsData() {
        // TODO: Load pets data from API
        // For now, show placeholder data
        tvTotalPets.setText("0");
        tvActivePets.setText("0");
    }

    private void loadAlertsData() {
        // TODO: Load alerts data from API
        // For now, show empty state
        llEmptyAlerts.setVisibility(View.VISIBLE);
        rvRecentAlerts.setVisibility(View.GONE);

        // Setup RecyclerView
        rvRecentAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvRecentAlerts.setNestedScrollingEnabled(false);
    }

    private void loadDevicesData() {
        // TODO: Load devices data from API
        // For now, show empty state
        llEmptyDevices.setVisibility(View.VISIBLE);
        rvDevices.setVisibility(View.GONE);

        // Setup RecyclerView
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setNestedScrollingEnabled(false);
    }

    private void handleLogout() {
        // Clear session
        sessionManager.logout();

        // Navigate to welcome screen
        navigateToWelcome();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Exit app when back is pressed on main screen
        super.onBackPressed();
        finishAffinity();
    }
}