package com.example.petsafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.RegisterRequest;
import com.example.petsafe.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private TextView tvErrorMessage;
    private View cvErrorMessage;
    private ProgressBar progressBar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar API Service
        apiService = ApiClient.getApiService();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        cvErrorMessage = findViewById(R.id.cvErrorMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        // Limpar erros anteriores
        clearErrors();

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validações
        if (!validateFullName(fullName)) {
            return;
        }

        if (!validateEmail(email)) {
            return;
        }

        if (!validatePassword(password)) {
            return;
        }

        if (!validateConfirmPassword(password, confirmPassword)) {
            return;
        }

        // Realizar cadastro
        performRegister(fullName, email, phone, password);
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        hideErrorMessage();
    }

    private boolean validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_empty_name));
            etFullName.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_empty_password));
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegister(String fullName, String email, String phone, String password) {
        showLoading(true);

        // Criar request de cadastro
        RegisterRequest registerRequest = new RegisterRequest(email, password, fullName, phone);

        // Fazer chamada à API
        Call<User> call = apiService.register(registerRequest);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Cadastro bem-sucedido
                    User user = response.body();

                    Toast.makeText(RegisterActivity.this,
                            R.string.success_register,
                            Toast.LENGTH_LONG).show();

                    // Voltar para tela de login
                    finish();
                } else {
                    // Cadastro falhou
                    handleRegisterError(response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Erro ao fazer cadastro", t);

                // Mostrar erro de conexão na mensagem geral
                showErrorMessage(getString(R.string.error_network));
            }
        });
    }

    private void handleRegisterError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 400:
                // Email já existe
                errorMessage = getString(R.string.error_email_exists);
                break;
            case 500:
                // Erro no servidor
                errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                break;
            default:
                // Outro erro
                errorMessage = getString(R.string.error_register_failed);
                break;
        }
        showErrorMessage(errorMessage);
    }

    private void showErrorMessage(String message) {
        tvErrorMessage.setText(message);
        cvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        cvErrorMessage.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etFullName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPhone.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }
}
