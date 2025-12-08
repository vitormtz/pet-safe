package com.example.petsafe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.example.petsafe.adapters.PetsAdapter;
import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.Device;
import com.example.petsafe.models.Pet;
import com.example.petsafe.models.PetRequest;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetsActivity extends AppCompatActivity implements PetsAdapter.OnPetClickListener {

    private static final String TAG = "PetsActivity";

    // Views
    private Toolbar toolbar;
    private RecyclerView rvPets;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddPet;
    private SwipeRefreshLayout swipeRefresh;
    private BottomNavigationView bottomNavigation;

    // Utils
    private SessionManager sessionManager;
    private ApiService apiService;
    private PetsAdapter petsAdapter;

    // Data
    private List<Device> availableDevices = new ArrayList<>();
    private Map<String, Long> deviceMap = new HashMap<>(); // Map device display string to device ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pets);

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
        loadPets();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvPets = findViewById(R.id.rvPets);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAddPet = findViewById(R.id.fabAddPet);
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
        petsAdapter = new PetsAdapter(this);
        rvPets.setLayoutManager(new LinearLayoutManager(this));
        rvPets.setAdapter(petsAdapter);
    }

    private void setupListeners() {
        fabAddPet.setOnClickListener(v -> showAddPetDialog());

        swipeRefresh.setOnRefreshListener(() -> {
            loadPets();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_pets);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(PetsActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_pets) {
                // Already on pets screen
                return true;
            } else if (itemId == R.id.navigation_alerts) {
                // TODO: Navigate to alerts screen
                Toast.makeText(this, "Tela de alertas em desenvolvimento", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(PetsActivity.this, ProfileActivity.class));
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
                    availableDevices = response.body().getData();
                    updateDeviceMap();
                    Log.d(TAG, "Devices loaded: " + availableDevices.size());
                } else {
                    Log.e(TAG, "Failed to load devices: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                Log.e(TAG, "Error loading devices", t);
            }
        });
    }

    private void updateDeviceMap() {
        deviceMap.clear();
        Map<String, String> deviceDisplayMap = new HashMap<>();

        for (Device device : availableDevices) {
            String displayText = device.getSerialNumber();
            if (device.getModel() != null && !device.getModel().isEmpty()) {
                displayText += " (" + device.getModel() + ")";
            }
            deviceMap.put(displayText, device.getId());
            deviceDisplayMap.put(String.valueOf(device.getId()), displayText);
        }

        // Update adapter with device display map
        petsAdapter.setDeviceDisplayMap(deviceDisplayMap);
    }

    private void loadPets() {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.listPets(token).enqueue(new Callback<ApiResponse<List<Pet>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Pet>>> call, Response<ApiResponse<List<Pet>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Pet> pets = response.body().getData();
                    petsAdapter.setPets(pets);

                    if (pets.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvPets.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvPets.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "Pets loaded: " + pets.size());
                } else {
                    Log.e(TAG, "Failed to load pets: " + response.code());
                    Toast.makeText(PetsActivity.this, "Erro ao carregar pets", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Pet>>> call, Throwable t) {
                Log.e(TAG, "Error loading pets", t);
                Toast.makeText(PetsActivity.this, "Erro de conexão ao carregar pets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddPetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_pet, null);
        builder.setView(dialogView);

        // Initialize views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etPetName = dialogView.findViewById(R.id.etPetName);
        AutoCompleteTextView actvSpecies = dialogView.findViewById(R.id.actvSpecies);
        TextInputEditText etBreed = dialogView.findViewById(R.id.etBreed);
        AutoCompleteTextView actvDevice = dialogView.findViewById(R.id.actvDevice);
        TextInputEditText etDob = dialogView.findViewById(R.id.etDob);
        TextView tvDeviceHelp = dialogView.findViewById(R.id.tvDeviceHelp);

        tvDialogTitle.setText("Adicionar Pet");
        tvDeviceHelp.setText("Selecione o dispositivo que será vinculado a este pet");

        // Setup Species dropdown
        String[] species = {"Cachorro", "Gato", "Pássaro", "Outro"};
        ArrayAdapter<String> speciesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, species);
        actvSpecies.setAdapter(speciesAdapter);

        // Setup Device dropdown
        String[] devices = deviceMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, devices);
        actvDevice.setAdapter(devicesAdapter);

        // Setup Date picker for DOB
        Calendar calendar = Calendar.getInstance();
        etDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDob.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        // Setup buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etPetName.getText().toString().trim();
            String speciesText = actvSpecies.getText().toString().trim();
            String breed = etBreed.getText().toString().trim();
            String deviceText = actvDevice.getText().toString().trim();
            String dobText = etDob.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o nome do pet", Toast.LENGTH_SHORT).show();
                return;
            }

            if (speciesText.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione a espécie", Toast.LENGTH_SHORT).show();
                return;
            }

            if (deviceText.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione um dispositivo", Toast.LENGTH_SHORT).show();
                return;
            }

            Long deviceId = deviceMap.get(deviceText);
            if (deviceId == null) {
                Toast.makeText(this, "Dispositivo inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert DOB to ISO format if provided
            String dobIso = null;
            if (!dobText.isEmpty()) {
                try {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    dobIso = isoFormat.format(displayFormat.parse(dobText));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date", e);
                }
            }

            PetRequest petRequest = new PetRequest(name, speciesText, breed, String.valueOf(deviceId), dobIso);
            createPet(petRequest);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditPetDialog(Pet pet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_pet, null);
        builder.setView(dialogView);

        // Initialize views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etPetName = dialogView.findViewById(R.id.etPetName);
        AutoCompleteTextView actvSpecies = dialogView.findViewById(R.id.actvSpecies);
        TextInputEditText etBreed = dialogView.findViewById(R.id.etBreed);
        AutoCompleteTextView actvDevice = dialogView.findViewById(R.id.actvDevice);
        TextInputEditText etDob = dialogView.findViewById(R.id.etDob);
        TextView tvDeviceHelp = dialogView.findViewById(R.id.tvDeviceHelp);

        tvDialogTitle.setText("Editar Pet");

        // Pre-fill data
        etPetName.setText(pet.getName());
        actvSpecies.setText(pet.getSpecies(), false);
        if (pet.getBreed() != null) etBreed.setText(pet.getBreed());

        // Setup Species dropdown
        String[] species = {"Cachorro", "Gato", "Pássaro", "Outro"};
        ArrayAdapter<String> speciesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, species);
        actvSpecies.setAdapter(speciesAdapter);

        // Device dropdown (disabled for editing)
        if (pet.getMicrochipId() != null) {
            // Find device display name from the ID
            String deviceDisplayText = getDeviceDisplayText(pet.getMicrochipId());
            actvDevice.setText(deviceDisplayText, false);
        }
        actvDevice.setEnabled(false);
        tvDeviceHelp.setText("O dispositivo não pode ser alterado após o cadastro");

        // DOB
        if (pet.getDob() != null && !pet.getDob().isEmpty()) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDob.setText(displayFormat.format(isoFormat.parse(pet.getDob())));
            } catch (Exception e) {
                etDob.setText(pet.getDob());
            }
        }

        // Setup Date picker for DOB
        Calendar calendar = Calendar.getInstance();
        etDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDob.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        // Setup buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etPetName.getText().toString().trim();
            String speciesText = actvSpecies.getText().toString().trim();
            String breed = etBreed.getText().toString().trim();
            String dobText = etDob.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o nome do pet", Toast.LENGTH_SHORT).show();
                return;
            }

            if (speciesText.isEmpty()) {
                Toast.makeText(this, "Por favor, selecione a espécie", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert DOB to ISO format if provided
            String dobIso = null;
            if (!dobText.isEmpty()) {
                try {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    dobIso = isoFormat.format(displayFormat.parse(dobText));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date", e);
                }
            }

            PetRequest petRequest = new PetRequest(name, speciesText, breed, pet.getMicrochipId(), dobIso);
            updatePet(pet.getId(), petRequest);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(Pet pet) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o pet " + pet.getName() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> deletePet(pet))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createPet(PetRequest petRequest) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.createPet(token, petRequest).enqueue(new Callback<ApiResponse<Pet>>() {
            @Override
            public void onResponse(Call<ApiResponse<Pet>> call, Response<ApiResponse<Pet>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Pet newPet = response.body().getData();
                    petsAdapter.addPet(newPet);

                    llEmptyState.setVisibility(View.GONE);
                    rvPets.setVisibility(View.VISIBLE);

                    Toast.makeText(PetsActivity.this, "Pet adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Pet created successfully");
                } else {
                    Log.e(TAG, "Failed to create pet: " + response.code());
                    Toast.makeText(PetsActivity.this, "Erro ao adicionar pet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Pet>> call, Throwable t) {
                Log.e(TAG, "Error creating pet", t);
                Toast.makeText(PetsActivity.this, "Erro de conexão ao adicionar pet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePet(Long petId, PetRequest petRequest) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.updatePet(token, petId, petRequest).enqueue(new Callback<ApiResponse<Pet>>() {
            @Override
            public void onResponse(Call<ApiResponse<Pet>> call, Response<ApiResponse<Pet>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Pet updatedPet = response.body().getData();
                    petsAdapter.updatePet(updatedPet);
                    Toast.makeText(PetsActivity.this, "Pet atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Pet updated successfully");
                } else {
                    Log.e(TAG, "Failed to update pet: " + response.code());
                    Toast.makeText(PetsActivity.this, "Erro ao atualizar pet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Pet>> call, Throwable t) {
                Log.e(TAG, "Error updating pet", t);
                Toast.makeText(PetsActivity.this, "Erro de conexão ao atualizar pet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePet(Pet pet) {
        String token = "Bearer " + sessionManager.getAccessToken();

        apiService.deletePet(token, pet.getId()).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful()) {
                    petsAdapter.removePet(pet);

                    if (petsAdapter.getItemCount() == 0) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvPets.setVisibility(View.GONE);
                    }

                    Toast.makeText(PetsActivity.this, "Pet excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Pet deleted successfully");
                } else {
                    Log.e(TAG, "Failed to delete pet: " + response.code());
                    Toast.makeText(PetsActivity.this, "Erro ao excluir pet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "Error deleting pet", t);
                Toast.makeText(PetsActivity.this, "Erro de conexão ao excluir pet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Pet pet) {
        showEditPetDialog(pet);
    }

    @Override
    public void onDeleteClick(Pet pet) {
        showDeleteConfirmDialog(pet);
    }

    @Override
    public void onPetClick(Pet pet) {
        // TODO: Navigate to pet details screen
        Toast.makeText(this, "Detalhes de " + pet.getName(), Toast.LENGTH_SHORT).show();
    }

    private String getDeviceDisplayText(String deviceIdStr) {
        try {
            Long deviceId = Long.parseLong(deviceIdStr);

            // Search for the device in the available devices list
            for (Device device : availableDevices) {
                if (device.getId().equals(deviceId)) {
                    String displayText = device.getSerialNumber();
                    if (device.getModel() != null && !device.getModel().isEmpty()) {
                        displayText += " (" + device.getModel() + ")";
                    }
                    return displayText;
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing device ID", e);
        }

        // If device not found, return the ID
        return deviceIdStr;
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(PetsActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
