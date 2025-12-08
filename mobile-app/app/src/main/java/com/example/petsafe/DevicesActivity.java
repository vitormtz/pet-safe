package com.example.petsafe;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petsafe.adapters.DevicesAdapter;
import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.Device;
import com.example.petsafe.models.DeviceRequest;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesActivity extends AppCompatActivity implements DevicesAdapter.OnDeviceClickListener {

    private static final String TAG = "DevicesActivity";

    // Views
    private Toolbar toolbar;
    private RecyclerView rvDevices;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddDevice;
    private SwipeRefreshLayout swipeRefresh;
    private BottomNavigationView bottomNavigation;

    // Utils
    private SessionManager sessionManager;
    private ApiService apiService;
    private DevicesAdapter devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        if (!sessionManager.isLoggedIn()) {
            navigateToWelcome();
            return;
        }

        initializeViews();
        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupBottomNavigation();
        loadDevices();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvDevices = findViewById(R.id.rvDevices);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAddDevice = findViewById(R.id.fabAddDevice);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupWindowInsets() {
        View appBarLayout = findViewById(R.id.appBarLayout);
        View bottomNav = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

    private void setupRecyclerView() {
        devicesAdapter = new DevicesAdapter(this);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(devicesAdapter);
    }

    private void setupListeners() {
        fabAddDevice.setOnClickListener(v -> showAddDeviceDialog());

        swipeRefresh.setOnRefreshListener(() -> {
            loadDevices();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_devices);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(DevicesActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_pets) {
                startActivity(new Intent(DevicesActivity.this, PetsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_devices) {
                // Already on devices screen
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(DevicesActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadDevices() {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.listDevices(token).enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Device> devices = response.body().getData();
                    devicesAdapter.setDevices(devices);

                    if (devices.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvDevices.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvDevices.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "Devices loaded: " + devices.size());
                } else {
                    Log.e(TAG, "Failed to load devices: " + response.code());
                    Toast.makeText(DevicesActivity.this, "Erro ao carregar dispositivos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                Log.e(TAG, "Error loading devices", t);
                Toast.makeText(DevicesActivity.this, "Erro de conexão ao carregar dispositivos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_device, null);
        builder.setView(dialogView);

        // Initialize views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etSerialNumber = dialogView.findViewById(R.id.etSerialNumber);
        TextInputEditText etModel = dialogView.findViewById(R.id.etModel);
        TextInputEditText etImei = dialogView.findViewById(R.id.etImei);
        TextInputEditText etFirmware = dialogView.findViewById(R.id.etFirmware);

        tvDialogTitle.setText("Adicionar Dispositivo");

        AlertDialog dialog = builder.create();

        // Setup buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String serialNumber = etSerialNumber.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            String imei = etImei.getText().toString().trim();
            String firmware = etFirmware.getText().toString().trim();

            if (serialNumber.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o número de série", Toast.LENGTH_SHORT).show();
                return;
            }

            // IMEI validation only if provided
            if (!imei.isEmpty() && imei.length() != 15) {
                Toast.makeText(this, "O IMEI deve conter 15 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            DeviceRequest deviceRequest = new DeviceRequest(
                serialNumber,
                model.isEmpty() ? null : model,
                imei.isEmpty() ? null : imei,
                firmware.isEmpty() ? null : firmware
            );
            createDevice(deviceRequest);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditDeviceDialog(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_device, null);
        builder.setView(dialogView);

        // Initialize views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etSerialNumber = dialogView.findViewById(R.id.etSerialNumber);
        TextInputEditText etModel = dialogView.findViewById(R.id.etModel);
        TextInputEditText etImei = dialogView.findViewById(R.id.etImei);
        TextInputEditText etFirmware = dialogView.findViewById(R.id.etFirmware);

        tvDialogTitle.setText("Editar Dispositivo");

        // Pre-fill data
        etSerialNumber.setText(device.getSerialNumber());
        etSerialNumber.setEnabled(false); // Número de série não pode ser editado
        if (device.getModel() != null) etModel.setText(device.getModel());
        if (device.getImei() != null) etImei.setText(device.getImei());
        if (device.getFirmware() != null) etFirmware.setText(device.getFirmware());

        AlertDialog dialog = builder.create();

        // Setup buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String serialNumber = etSerialNumber.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            String imei = etImei.getText().toString().trim();
            String firmware = etFirmware.getText().toString().trim();

            if (serialNumber.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o número de série", Toast.LENGTH_SHORT).show();
                return;
            }

            // IMEI validation only if provided
            if (!imei.isEmpty() && imei.length() != 15) {
                Toast.makeText(this, "O IMEI deve conter 15 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            DeviceRequest deviceRequest = new DeviceRequest(
                serialNumber,
                model.isEmpty() ? null : model,
                imei.isEmpty() ? null : imei,
                firmware.isEmpty() ? null : firmware
            );
            updateDevice(device.getId(), deviceRequest);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(Device device) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o dispositivo " + device.getSerialNumber() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> deleteDevice(device))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createDevice(DeviceRequest deviceRequest) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.createDevice(token, deviceRequest).enqueue(new Callback<ApiResponse<Device>>() {
            @Override
            public void onResponse(Call<ApiResponse<Device>> call, Response<ApiResponse<Device>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Device newDevice = response.body().getData();
                    devicesAdapter.addDevice(newDevice);

                    llEmptyState.setVisibility(View.GONE);
                    rvDevices.setVisibility(View.VISIBLE);

                    Toast.makeText(DevicesActivity.this, "Dispositivo adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Device created successfully");
                } else {
                    Log.e(TAG, "Failed to create device: " + response.code());
                    Toast.makeText(DevicesActivity.this, "Erro ao adicionar dispositivo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Device>> call, Throwable t) {
                Log.e(TAG, "Error creating device", t);
                Toast.makeText(DevicesActivity.this, "Erro de conexão ao adicionar dispositivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDevice(Long deviceId, DeviceRequest deviceRequest) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.updateDevice(token, deviceId, deviceRequest).enqueue(new Callback<ApiResponse<Device>>() {
            @Override
            public void onResponse(Call<ApiResponse<Device>> call, Response<ApiResponse<Device>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Device updatedDevice = response.body().getData();
                    devicesAdapter.updateDevice(updatedDevice);
                    Toast.makeText(DevicesActivity.this, "Dispositivo atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Device updated successfully");
                } else {
                    Log.e(TAG, "Failed to update device: " + response.code());
                    Toast.makeText(DevicesActivity.this, "Erro ao atualizar dispositivo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Device>> call, Throwable t) {
                Log.e(TAG, "Error updating device", t);
                Toast.makeText(DevicesActivity.this, "Erro de conexão ao atualizar dispositivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDevice(Device device) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.deleteDevice(token, device.getId()).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful()) {
                    devicesAdapter.removeDevice(device);

                    if (devicesAdapter.getItemCount() == 0) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvDevices.setVisibility(View.GONE);
                    }

                    Toast.makeText(DevicesActivity.this, "Dispositivo excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Device deleted successfully");
                } else {
                    Log.e(TAG, "Failed to delete device: " + response.code());
                    Toast.makeText(DevicesActivity.this, "Erro ao excluir dispositivo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "Error deleting device", t);
                Toast.makeText(DevicesActivity.this, "Erro de conexão ao excluir dispositivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Device device) {
        showEditDeviceDialog(device);
    }

    @Override
    public void onDeleteClick(Device device) {
        showDeleteConfirmDialog(device);
    }

    @Override
    public void onDeviceClick(Device device) {
        // TODO: Navigate to device details screen
        Toast.makeText(this, "Detalhes de " + device.getSerialNumber(), Toast.LENGTH_SHORT).show();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(DevicesActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
