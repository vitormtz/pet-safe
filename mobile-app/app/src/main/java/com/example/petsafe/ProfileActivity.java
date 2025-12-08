package com.example.petsafe;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ChangePasswordRequest;
import com.example.petsafe.models.UpdateProfileRequest;
import com.example.petsafe.models.User;
import com.example.petsafe.models.UserResponse;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private Toolbar toolbar;
    private TextView tvUserInitial;
    private TextView tvUserFullName;
    private TextView tvUserEmail;
    private TextView tvFullName;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvMemberSince;
    private MaterialButton btnEditProfile;
    private MaterialButton btnChangePassword;
    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private ApiService apiService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        currentUser = sessionManager.getUserDetails();

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
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvUserInitial = findViewById(R.id.tvUserInitial);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupWindowInsets() {
        View appBarLayout = findViewById(R.id.appBarLayout);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
            );

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
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_pets) {
                startActivity(new Intent(ProfileActivity.this, PetsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_alerts) {
                // TODO: Navigate to alerts screen
                Toast.makeText(this, "Tela de alertas em desenvolvimento", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Already on profile
                return true;
            }

            return false;
        });
    }

    private void loadUserData() {
        // Sempre buscar dados atualizados da API
        fetchUserProfile();

        // Se já tiver dados em cache, exibir enquanto busca
        if (currentUser != null) {
            displayUserInfo(currentUser);
        }
    }

    private void fetchUserProfile() {
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUserProfile(authHeader).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getData();
                    currentUser = user;
                    sessionManager.updateUser(user);
                    displayUserInfo(user);
                } else {
                    Toast.makeText(ProfileActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching user profile", t);
                Toast.makeText(ProfileActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) {
            Log.w(TAG, "User is null in displayUserInfo");
            return;
        }

        String fullName = user.getFullName() != null ? user.getFullName() : "Usuário";
        String email = user.getEmail() != null ? user.getEmail() : "";
        String phone = (user.getPhone() != null && !user.getPhone().trim().isEmpty())
                ? user.getPhone()
                : "Não informado";

        Log.d(TAG, "Displaying user info - Name: " + fullName + ", Email: " + email + ", Phone: " + phone);

        String initial = fullName.substring(0, 1).toUpperCase();
        tvUserInitial.setText(initial);
        tvUserFullName.setText(fullName);
        tvUserEmail.setText(email);
        tvFullName.setText(fullName);
        tvPhone.setText(phone);
        tvEmail.setText(email);

        if (user.getCreatedAt() != null && !user.getCreatedAt().isEmpty()) {
            try {
                String createdAt = user.getCreatedAt();
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(createdAt);
                if (date != null) {
                    tvMemberSince.setText(outputFormat.format(date));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date", e);
                tvMemberSince.setText(user.getCreatedAt());
            }
        } else {
            tvMemberSince.setText("Data não disponível");
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_profile);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);

        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etPhone.setText(currentUser.getPhone());
        }

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save_changes, (dialog, which) -> {
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (fullName.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfile(fullName, phone);
        });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProfile(String fullName, String phone) {
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);
        apiService.updateUserProfile(authHeader, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body().getData();
                    currentUser = updatedUser;
                    sessionManager.updateUser(updatedUser);
                    displayUserInfo(updatedUser);
                    Toast.makeText(ProfileActivity.this, R.string.success_profile_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, R.string.error_update_profile, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error updating profile", t);
                Toast.makeText(ProfileActivity.this, R.string.error_update_profile, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_password);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save_changes, (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

            if (currentPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_current_password, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_new_password, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 8) {
                Toast.makeText(this, R.string.error_new_password_too_short, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, R.string.error_new_password_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPassword);
        });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
        apiService.changePassword(authHeader, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, R.string.success_password_changed, Toast.LENGTH_LONG).show();
                } else {
                    if (response.code() == 400 || response.code() == 401 || response.code() == 403) {
                        Toast.makeText(ProfileActivity.this, R.string.error_current_password_incorrect, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, R.string.error_change_password, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error changing password", t);
                Toast.makeText(ProfileActivity.this, R.string.error_change_password, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> handleLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void handleLogout() {
        sessionManager.logout();
        navigateToWelcome();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
